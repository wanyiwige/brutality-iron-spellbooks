package cn.wanyiwige.brutalityiron;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.block.scroll_forge.ScrollForgeTile;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeMenu;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

/** Explicit opt-in; runs only in a disposable dedicated-server test world. */
@Mod.EventBusSubscriber(modid = BrutalityIron.ID)
public final class ServerAudit {
    private static net.minecraft.server.MinecraftServer running;
    private static long deadline;
    private static int checkedScrolls;
    @SubscribeEvent public static void start(ServerStartedEvent event) {
        if (!Boolean.getBoolean("brutality_iron.audit")) return;
        var log = LogUtils.getLogger();
        var server = event.getServer(); var level = server.overworld();
        try {
            SpellConfigManager.onDatapackSync(new OnDatapackSyncEvent(server.getPlayerList(), null));
            // Dedicated-server fixtures run before any player login; mirror Iron's datapack sync to populate its upgrade type cache.
            io.redspace.ironsspellbooks.api.backwards_compat.UpgradeTypeCache.onDatapackLoad(new OnDatapackSyncEvent(server.getPlayerList(), null));
            require(Content.SPELL_MAP.size() == 30, "thirty spells");
            BookAudit.run(level);
            BookRecipeAudit.run(level);
            SchoolArmorAudit.run(level);
            SchoolMaterialsAudit.run(level);
            var player = FakePlayerFactory.getMinecraft(level);
            var tile = new ScrollForgeTile(BlockPos.ZERO, ((BlockItem)ItemRegistry.SCROLL_FORGE_BLOCK.get()).getBlock().defaultBlockState());
            tile.setLevel(level); var menu = new ScrollForgeMenu(0, player.getInventory(), tile);
            // Check actual menu admission; direct item-handler writes bypass mayPlace.
            for (var entry : java.util.Map.of("nihility", "empty_essence", "celestia", "angel_feather", "inferno", "chthonic_crystal").entrySet()) {
                var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity", entry.getValue()));
                require(item != null, "focus item exists " + entry.getKey());
                var focus = new ItemStack(item);
                require(focus.is(Content.SCHOOL_MAP.get(entry.getKey()).get().getFocus()), "school focus tag " + entry.getKey());
                require(menu.getFocusSlot().mayPlace(focus), "menu focus admission " + entry.getKey());
            }
            require(!menu.getFocusSlot().mayPlace(new ItemStack(Items.STONE)), "menu rejects ordinary stone");
            log.info("BRUTALITY_IRON_FOCUS_SLOT_PASS schools=nihility,celestia,inferno negative=stone");
            int scrolls = 0, crafts = 0, casts = 0;
            var progressionReport = new com.google.gson.JsonArray();
            for (var entry : Content.SPELL_MAP.entrySet()) {
                var spell = Content.spell(entry.getKey());
                require(spell.getSchoolType().getId().equals(Content.school(spell.logic.getSchool())), "school " + entry.getKey());
                require(spell.isEnabled(), "enabled " + entry.getKey());
                require(spell.getMaxLevel() == SpellProgression.get(entry.getKey()).maxLevel(), "default cap " + entry.getKey());
                var rarities = new com.google.gson.JsonArray();
                int previous = -1, legendary = 0;
                for (int rank = 1; rank <= spell.getMaxLevel(); rank++) {
                    var rarity = spell.getRarity(rank);
                    require(rarity.getValue() >= previous, "non-monotonic rarity " + entry.getKey());
                    previous = rarity.getValue();
                    if (rarity == SpellRarity.LEGENDARY) legendary++;
                    rarities.add(rarity.name());
                    var stack = ItemStack.of(Content.scroll(entry.getKey(), rank).save(new net.minecraft.nbt.CompoundTag()));
                    var stored = ISpellContainer.get(stack).getSpellAtIndex(0);
                    require(stored.getSpell() == spell && stored.getLevel() == rank, "scroll roundtrip"); scrolls++;
                }
                require(legendary >= 2 && spell.getRarity(spell.getMaxLevel()) == SpellRarity.LEGENDARY, "legendary tail " + entry.getKey());
                var row = new com.google.gson.JsonObject();
                row.addProperty("id", spell.getSpellId()); row.addProperty("max_level", spell.getMaxLevel());
                row.add("rarities", rarities); progressionReport.add(row);
                Item focus = ForgeRegistries.ITEMS.tags().getTag(spell.getSchoolType().getFocus()).iterator().next();
                for (var rarity : SpellRarity.values()) {
                    var ink = InkItem.getInkForRarity(rarity); if (ink.getRarity() != rarity) continue;
                    tile.getItemHandler().setStackInSlot(0, new ItemStack(ink));
                    tile.getItemHandler().setStackInSlot(1, new ItemStack(Items.PAPER));
                    tile.getItemHandler().setStackInSlot(2, new ItemStack(focus));
                    menu.setRecipeSpell(spell); var result = tile.getItemHandler().getStackInSlot(3);
                    int expected = spell.getMinLevelForRarity(rarity);
                    int scanned = 0;
                    for (int rank = 1; rank <= spell.getMaxLevel(); rank++) if (spell.getRarity(rank) == rarity) { scanned = rank; break; }
                    require(expected == scanned, "rarity boundary " + entry.getKey());
                    require(expected == 0 ? result.isEmpty() : !result.isEmpty() && ISpellContainer.get(result).getSpellAtIndex(0).getSpell() == spell, "forge " + entry.getKey() + " " + rarity);
                    if (expected > 0) {
                        var stored = ISpellContainer.get(result).getSpellAtIndex(0);
                        require(stored.getLevel() == expected && spell.getRarity(stored.getLevel()) == rarity, "ink rank " + entry.getKey());
                    }
                    crafts++;
                }
                for (int x=-3; x<=3; x++) for(int z=-3;z<=12;z++) level.setBlockAndUpdate(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState());
                player.setPos(0,100,0); player.setYRot(0); player.setXRot(0); player.setOnGround(true);
                player.setHealth(player.getMaxHealth()); player.removeAllEffects();
                Cow target = EntityType.COW.create(level); target.setPos(0,100,3); target.setNoAi(true); level.addFreshEntity(target);
                var data = MagicData.getPlayerMagicData(player);
                spell.onServerPreCast(level,1,player,data);
                if(spell.getCastType() == CastType.CONTINUOUS) for(int i=0;i<30;i++) spell.onServerCastTick(level,1,player,data);
                spell.onCast(level,1,player,CastSource.SPELLBOOK,data);
                if (entry.getKey().equals("destruction")) require(target.getHealth()<20,"destruction damage");
                spell.onServerCastComplete(level,1,player,data,false); target.discard(); casts++;
                log.info("BRUTALITY_IRON_CAST_CALLBACK_PASS {}", entry.getKey());
            }
            log.info("BRUTALITY_IRON_DATA_PASS spells=30 scrolls={} crafting={} cast-callbacks={}",scrolls,crafts,casts);
            java.nio.file.Files.writeString(java.nio.file.Path.of("progression-report.json"), new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(progressionReport));
            checkedScrolls = scrolls;
            running = server; deadline = level.getGameTime() + 240;
            cn.wanyiwige.brutalityiron.skills.SkillAudit.start(level);
        } catch(Throwable failure) { log.error("BRUTALITY_IRON_AUDIT_FAILED",failure); server.halt(false); }
    }
    @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (running == null || event.phase != net.minecraftforge.event.TickEvent.Phase.END
                || running.overworld().getGameTime() < deadline
                || !cn.wanyiwige.brutalityiron.skills.SkillAudit.done()) return;
        var server = running; running = null;
        try {
            for (var entity : server.overworld().getAllEntities()) {
                if (entity instanceof cn.wanyiwige.brutalityiron.entity.brimwielder.ExtinctionEntity
                    || entity instanceof cn.wanyiwige.brutalityiron.entity.umbrancy.PiercingMoonlight
                    || entity instanceof cn.wanyiwige.brutalityiron.entity.celestia.HolyBeamEntity)
                    require(!entity.isAlive(), "lingering beam " + entity.getType());
            }
            LogUtils.getLogger().info("BRUTALITY_IRON_AUDIT_PASS spells=30 scrolls={} server-ticks=240 beam-cleanup=pass", checkedScrolls);
        } catch(Throwable failure) { LogUtils.getLogger().error("BRUTALITY_IRON_AUDIT_FAILED",failure); }
        server.halt(false);
    }
    private static void require(boolean ok,String message) { if(!ok) throw new IllegalStateException(message); }
}
