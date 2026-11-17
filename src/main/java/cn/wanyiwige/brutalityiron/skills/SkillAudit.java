package cn.wanyiwige.brutalityiron.skills;

import cn.wanyiwige.brutalityiron.BrutalityIron;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.block.scroll_forge.ScrollForgeTile;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeMenu;
import io.redspace.ironsspellbooks.item.InkItem;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** Disposable-world integration checks; never enabled in ordinary play. */
@Mod.EventBusSubscriber(modid=BrutalityIron.ID)
public final class SkillAudit {
    private static ServerLevel level;private static FakePlayer player;private static Cow target;
    private static Iterator<String> queue;private static String current;private static long next;
    private static boolean complete;private static int casts,guards,scrolls,forges;
    private static float triplePreviousHealth; private static int tripleDamageEvents;
    private static final JsonArray report=new JsonArray();
    private static final Set<String> PROJECTILES=Set.of("sanguine_slash","shadowflame_slash","seeking_blades","stellar_marks","abyssal_volley","stygian_souls","ice_cube_barrage","spirit_bomb","caustic_wave","soulreaping_scythes","devil_armament","ashen_skulls","hellish_bolt","binding_hook");
    public static void start(ServerLevel serverLevel){
        if(!Boolean.getBoolean("brutality_iron.audit"))return;
        level=serverLevel;player=new AuditPlayer(level,new GameProfile(UUID.fromString("b9196af5-46d7-4031-9d02-c8fb45de9bce"),"[SkillAudit]"));
        if(level.getPlayerByUUID(player.getUUID())==null)level.addNewPlayer(player);
        var tile=new ScrollForgeTile(BlockPos.ZERO,((BlockItem)ItemRegistry.SCROLL_FORGE_BLOCK.get()).getBlock().defaultBlockState());tile.setLevel(level);var menu=new ScrollForgeMenu(0,player.getInventory(),tile);
        for(var entry:SkillContent.SKILLS.entrySet()){
            String id=entry.getKey();var spell=SkillContent.spell(id);var d=spell.definition;
            require(spell.isEnabled(),"enabled "+id);require(spell.getSchoolType().getId().equals(cn.wanyiwige.brutalityiron.SchoolFallback.resolve(new net.minecraft.resources.ResourceLocation(d.school()))),"school "+id);
            if(Set.of("ironwill_recovery","eclipse_charge","meteor_reservoir","stellar_marks","pursuit_anchor").contains(id))
                require(d.duration()>d.cooldown()*20+d.cast_time()+20,"recast window exceeds cooldown "+id);
            int legends=0;int previous=-1;var rarities=new JsonArray();
            for(int rank=1;rank<=spell.getMaxLevel();rank++){
                var rarity=spell.getRarity(rank);require(rarity.getValue()>=previous,"rarity order "+id);previous=rarity.getValue();rarities.add(rarity.name());if(rarity==SpellRarity.LEGENDARY)legends++;
                var stack=ItemStack.of(SkillContent.scroll(id,rank).save(new net.minecraft.nbt.CompoundTag()));var data=ISpellContainer.get(stack).getSpellAtIndex(0);require(data.getSpell()==spell&&data.getLevel()==rank,"scroll data "+id);scrolls++;
            }
            require(legends>=2&&spell.getRarity(spell.getMaxLevel())==SpellRarity.LEGENDARY,"legendary tail "+id);
            var focus=ForgeRegistries.ITEMS.tags().getTag(spell.getSchoolType().getFocus()).iterator();require(focus.hasNext(),"focus "+d.school());var item=focus.next();
            for(var rarity:SpellRarity.values()){
                var ink=InkItem.getInkForRarity(rarity);if(ink.getRarity()!=rarity)continue;
                tile.getItemHandler().setStackInSlot(0,new ItemStack(ink));tile.getItemHandler().setStackInSlot(1,new ItemStack(Items.PAPER));tile.getItemHandler().setStackInSlot(2,new ItemStack(item));menu.setRecipeSpell(spell);
                var result=tile.getItemHandler().getStackInSlot(3);int expected=spell.getMinLevelForRarity(rarity);
                require(expected==0?result.isEmpty():!result.isEmpty()&&ISpellContainer.get(result).getSpellAtIndex(0).getLevel()==expected,"native forging "+id+" "+rarity);forges++;
            }
            var row=new JsonObject();row.addProperty("id","brutality_iron:"+id);row.addProperty("school",d.school());row.addProperty("max_level",spell.getMaxLevel());row.add("rarities",rarities);report.add(row);
        }
        require(SkillContent.SKILLS.size()==23,"23 retained reviewed skills");
        for(int x=88;x<=112;x++)for(int z=88;z<=132;z++)level.setBlockAndUpdate(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState());
        level.setDayTime(6000);queue=SkillContent.SKILLS.keySet().iterator();next=level.getGameTime()+1;
        // Repeated audits reuse a disposable world. Remove fixtures left by a
        // failed prior run so coincident old cows cannot intercept projectiles.
        for(var old:level.getEntitiesOfClass(Cow.class,new net.minecraft.world.phys.AABB(80,90,80,120,120,140)))old.discard();
        LogUtils.getLogger().info("BRUTALITY_IRON_SKILL_DATA_PASS spells=23 scrolls={} forging={}",scrolls,forges);
    }
    public static boolean done(){return complete;}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||level==null||complete)return;
        if(current!=null && current.startsWith("triple_") && target!=null){
            float health=target.getHealth(); if(health<triplePreviousHealth-.001f)tripleDamageEvents++; triplePreviousHealth=health;
        }
        if(level.getGameTime()<next)return;
        try{
            if(current!=null)finishCase();
            if(!queue.hasNext()){
                verifySpecials();complete=true;
                java.nio.file.Files.writeString(java.nio.file.Path.of("skill-report.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
                LogUtils.getLogger().info("BRUTALITY_IRON_SKILL_AUDIT_PASS spells=23 positive-casts={} revival-guard={} scrolls={} forging={} lifecycle=pass",casts,guards,scrolls,forges);return;
            }
            current=queue.next();SkillRevisionAudit.begin(current,player);prepareCase(current);next=level.getGameTime()+(current.startsWith("triple_")?65:20);
        }catch(Throwable failure){LogUtils.getLogger().error("BRUTALITY_IRON_SKILL_AUDIT_FAILED current="+current,failure);if(target!=null)target.discard();SkillRuntime.clear(player);level.getServer().halt(false);}
    }
    private static void prepareCase(String id){
        player.removeAllEffects();player.setAbsorptionAmount(0);player.setPos(100,100,100);player.setYRot(0);player.setXRot(12);player.setOnGround(true);player.setDeltaMovement(Vec3.ZERO);player.setHealth(player.getMaxHealth());
        target=EntityType.COW.create(level);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);target.setHealth(200);target.setPos(100,100,103);target.setNoAi(true);level.addFreshEntity(target);triplePreviousHealth=200;tripleDamageEvents=0;
        if(id.equals("ceiling_ascent")){level.setBlockAndUpdate(new BlockPos(100,103,100),Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(new BlockPos(100,104,100),Blocks.STONE.defaultBlockState());}
        var spell=SkillContent.spell(id);var data=MagicData.getPlayerMagicData(player);
        if(id.equals("dream_recall")){
            require(!spell.checkPreCastConditions(level,1,player,data),"revival rejects ordinary spectators");guards++;
            var revived=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("b9196af5-46d7-4031-9d02-c8fb45de9bcf"),"[RevivalAudit]"));
            revived.setPos(102,100,100);if(level.getPlayerByUUID(revived.getUUID())==null)level.addNewPlayer(revived);
            revived.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
            require(!spell.checkPreCastConditions(level,1,player,data),"revival rejects non-death spectator");
            SkillRuntime.onDeath(new net.minecraftforge.event.entity.living.LivingDeathEvent(revived,revived.damageSources().generic()));
            require(spell.checkPreCastConditions(level,1,player,data),"revival permits recent-death fixture");
            spell.onCast(level,1,player,CastSource.SPELLBOOK,data);casts++;
            require(!revived.isSpectator(),"revival restores survival fixture");revived.discard();return;
        }
        require(spell.checkPreCastConditions(level,1,player,data),"positive precast "+id);
        if(id.equals("ironwill_recovery")){player.setHealth(8);player.setAbsorptionAmount(3);}
        spell.onServerPreCast(level,1,player,data);spell.onCast(level,1,player,CastSource.SPELLBOOK,data);spell.onServerCastComplete(level,1,player,data,false);casts++;
        if(PROJECTILES.contains(id))require(!level.getEntitiesOfClass(SkillVisualEntity.class,player.getBoundingBox().inflate(20),v->v.skill.equals(id)).isEmpty(),"projectile created "+id);
        if(Set.of("sacred_lance","infernal_crosscut","stone_body_slam","hellrok_slam","core_shatter","plague_infusion").contains(id))require(target.getHealth()<200,"damage "+id);
        if(Set.of("stray_legion","arcane_retinue","stone_retinue","infernal_spirits","vanishing_decoy").contains(id))require(countSummons()>0,"owned summon "+id);
        if(id.equals("ceiling_ascent"))require(player.getY()>104,"ceiling ascent destination");
        if(id.equals("backstep"))require(player.position().distanceToSqr(new Vec3(100,100,100))>.25,"backstep position");
        if(Set.of("inertia_reversal","gale_recoil").contains(id))require(player.getDeltaMovement().lengthSqr()>.01,"motion "+id);
        if(id.equals("ironwill_recovery")){require(player.getAbsorptionAmount()>3,"shield applied");spell.onCast(level,1,player,CastSource.SPELLBOOK,data);require(player.getAbsorptionAmount()==3&&player.getHealth()>8,"shield recast preserves other absorption");}
        if(Set.of("dawn_triad","eclipse_charge","meteor_reservoir").contains(id)){
            for(int i=0;i<3;i++){target.invulnerableTime=0;target.hurt(player.damageSources().playerAttack(player),1);}
            if(!id.equals("dawn_triad"))require(SkillRuntime.state(player,id).count>0,"attack charge "+id);
            spell.onCast(level,1,player,CastSource.SPELLBOOK,data);
        }
        if(id.equals("stellar_marks"))spell.onCast(level,1,player,CastSource.SPELLBOOK,data);
    }
    private static int countSummons(){int n=0;for(var entity:level.getAllEntities())if(!entity.isRemoved()&&(entity instanceof SkillMinion m&&player.getUUID().equals(m.ownerId)||entity instanceof SkillStray s&&player.getUUID().equals(s.getOwnerUUID())))n++;return n;}
    private static void finishCase(){
        if(Set.of("cruel_sun","soulfrost_wave","charged_detonation","corrosive_claws","starlight_flurry","triple_judgment","triple_profane_beam","guardian_hand").contains(current))require(target.getHealth()<200,"delayed damage "+current);
        if(current.startsWith("triple_"))require(tripleDamageEvents>=2,"repeated beam damage "+current+" hits="+tripleDamageEvents);
        SkillRevisionAudit.finish(current,player);SkillRuntime.clear(player);target.discard();
        require(countSummons()==0,"summon cleanup "+current);
        require(level.getEntitiesOfClass(SkillVisualEntity.class,player.getBoundingBox().inflate(128),v->player.getUUID().equals(v.ownerId)).isEmpty(),"effect cleanup "+current);
        level.setBlockAndUpdate(new BlockPos(100,103,100),Blocks.AIR.defaultBlockState());level.setBlockAndUpdate(new BlockPos(100,104,100),Blocks.AIR.defaultBlockState());
        LogUtils.getLogger().info("BRUTALITY_IRON_SKILL_CAST_PASS {}",current);current=null;
    }
    private static void verifySpecials(){
        player.setPos(100,100,100);player.removeAllEffects();player.setHealth(player.getMaxHealth());
        var cow=EntityType.COW.create(level);cow.setPos(100,100,102);cow.getAttribute(Attributes.ARMOR).setBaseValue(10);level.addFreshEntity(cow);
        cow.addEffect(new net.minecraft.world.effect.MobEffectInstance(SkillEffects.CORROSION.get(),80,1));require(cow.getAttributeValue(Attributes.ARMOR)==6,"corrosion works on ordinary mobs");cow.removeEffect(SkillEffects.CORROSION.get());require(cow.getAttributeValue(Attributes.ARMOR)==10,"corrosion restores mob armor");
        float hp=cow.getHealth();SkillRuntime.infect(player,cow,100,1);require(cow.hasEffect(SkillEffects.PLAGUE.get()),"plague applies");SkillRuntime.plagueTick(cow,1);require(cow.getHealth()<hp,"attributed plague damage");
        // A real ServerLevel contains the fake owner, exercising attribution lookup.
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(SkillEffects.COVENANT.get(),80,1));require(player.getMaxHealth()<20,"covenant cost");player.removeEffect(SkillEffects.COVENANT.get());require(player.getMaxHealth()==20,"covenant restoration");
        cow.discard();SkillRuntime.clear(player);SkillRevisionAudit.geometry(player);
    }
    private static void require(boolean ok,String text){if(!ok)throw new IllegalStateException(text);}
    /** Forge's fake network handler intentionally ignores teleport packets. */
    private static class AuditPlayer extends FakePlayer {
        AuditPlayer(ServerLevel level,GameProfile profile){super(level,profile);}
        @Override public void teleportTo(double x,double y,double z){setPos(x,y,z);}
        @Override public void teleportTo(ServerLevel level,double x,double y,double z,float yaw,float pitch){
            if(level==level()){setPos(x,y,z);setYRot(yaw);setXRot(pitch);}else super.teleportTo(level,x,y,z,yaw,pitch);
        }
    }
    private SkillAudit(){}
}
