package cn.wanyiwige.brutalityiron;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.SlotContext;

/** Exercises actual native item containers and Curios attribute application. */
public final class BookAudit {
    public static void run(ServerLevel level) {
        long expected = BookContent.DEFINITIONS.stream().filter(BookContent.Definition::available).count();
        require(BookContent.BOOKS.size() == expected, "book registration count");
        require(!ForgeRegistries.ITEMS.containsKey(BrutalityIron.id("umbral_tome")), "rejected book registered");
        var player = FakePlayerFactory.getMinecraft(level);
        var report = new com.google.gson.JsonArray();
        for (var registered : BookContent.BOOKS.values()) {
            var book = (SchoolBook)registered.get(); var def = book.definition;
            require(def.schoolType() != null, "missing school " + def.school());
            var stack = book.getDefaultInstance(); var container = ISpellContainer.get(stack);
            require(stack.getRarity() == Rarity.EPIC, "book rarity");
            require(container.getMaxSpellCount() == def.slots() && container.isEmpty(), "blank writable slots");
            require(stack.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                    new ResourceLocation("curios", "spellbook"))), "Curios spellbook tag");
            require(book.getAttributeModifiers(EquipmentSlot.MAINHAND, stack).isEmpty(), "hand attributes duplicate Curios");
            int count = 0;
            for (var spell : Content.SPELL_MAP.values()) {
                boolean added = ISpellContainer.get(stack).addSpell(spell.get(), 1, false, stack);
                if (count < def.slots()) { require(added, "writable slot " + count); count++; }
                else { require(!added, "book overflow"); break; }
            }
            var saved = ItemStack.of(stack.save(new net.minecraft.nbt.CompoundTag()));
            require(ISpellContainer.get(saved).getMaxSpellCount() == def.slots()
                    && ISpellContainer.get(saved).getActiveSpellCount() == def.slots(), "book NBT roundtrip");
            var modifiers = book.getAttributeModifiers(new SlotContext("spellbook", player, 0, false, true), UUID.randomUUID(), stack);
            require(modifiers.size() == 3, "three book bonuses");
            var mana = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", "max_mana"));
            var power = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(def.name().equals("cosmic_tome") && SchoolFallback.usingFallback(new ResourceLocation(def.school())) ? "irons_spellbooks:ender_spell_power" : def.powerAttribute()));
            var extra = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", def.extraAttribute()));
            require(mana != null && power != null && extra != null, "native book attributes exist");
            require(modifiers.get(mana).stream().anyMatch(m -> m.getAmount()==def.mana()
                    && m.getOperation()==AttributeModifier.Operation.ADDITION), "mana modifier");
            require(modifiers.get(power).stream().anyMatch(m -> m.getAmount()==def.power()
                    && m.getOperation()==AttributeModifier.Operation.MULTIPLY_BASE), "school modifier");
            require(modifiers.get(extra).stream().anyMatch(m -> m.getAmount()==def.extraValue()), "extra modifier");
            double beforeMana = player.getAttributeValue(mana), beforePower = def.schoolType().getPowerFor(player);
            player.getAttributes().addTransientAttributeModifiers(modifiers);
            require(Math.abs(player.getAttributeValue(mana)-beforeMana-def.mana())<.001, "equipped mana effect");
            require(def.schoolType().getPowerFor(player)>beforePower, "equipped school power effect");
            player.getAttributes().removeAttributeModifiers(modifiers);
            require(Math.abs(player.getAttributeValue(mana)-beforeMana)<.001
                    && Math.abs(def.schoolType().getPowerFor(player)-beforePower)<.001, "unequip restores attributes");
            var row = new com.google.gson.JsonObject();
            row.addProperty("item", registered.getId().toString()); row.addProperty("school", def.school());
            row.addProperty("slots", def.slots()); row.addProperty("mana", def.mana()); row.addProperty("power", def.power());
            row.addProperty("extra", def.extraAttribute()); row.addProperty("extra_value", def.extraValue());
            row.addProperty("writable_and_equipment_checks", true); report.add(row);
            LogUtils.getLogger().info("BRUTALITY_IRON_BOOK_PASS {} slots={} mana={} school={}", registered.getId(), def.slots(), def.mana(), def.school());
        }
        try { java.nio.file.Files.writeString(java.nio.file.Path.of("book-report.json"),
                new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch(java.io.IOException error) { throw new IllegalStateException(error); }
    }
    private static void require(boolean value, String message) { if(!value) throw new IllegalStateException(message); }
}
