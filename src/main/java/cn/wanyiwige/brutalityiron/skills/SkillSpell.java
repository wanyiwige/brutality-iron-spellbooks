package cn.wanyiwige.brutalityiron.skills;

import cn.wanyiwige.brutalityiron.BrutalityIron;
import cn.wanyiwige.brutalityiron.SpellSounds;
import cn.wanyiwige.brutalityiron.SchoolFallback;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import java.util.List;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class SkillSpell extends AbstractSpell {
    public boolean cancellableSummon(){return definition.id().equals("infernal_spirits")||definition.id().equals("stone_retinue");}
    public boolean cancellableEffect(){return definition.id().equals("event_horizon")||definition.id().equals("cruel_sun");}
    @Override public int getRecastCount(int rank,LivingEntity caster){return cancellableSummon()||cancellableEffect()?2:super.getRecastCount(rank,caster);}
    @Override public ICastDataSerializable getEmptyCastData(){return cancellableSummon()||cancellableEffect()?new io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData():super.getEmptyCastData();}
    @Override public void onRecastFinished(net.minecraft.server.level.ServerPlayer owner,io.redspace.ironsspellbooks.capabilities.magic.RecastInstance instance,io.redspace.ironsspellbooks.capabilities.magic.RecastResult result,ICastDataSerializable castData){
        if(cancellableSummon()){
            io.redspace.ironsspellbooks.capabilities.magic.SummonManager.recastFinishedHelper(owner,instance,result,castData);
            SkillRuntime.dismissSummons(owner,definition.id());
        }
        if(cancellableEffect())SkillRuntime.dismissEffects(owner,definition.id());
        super.onRecastFinished(owner,instance,result,castData);
    }
    public final SkillDefinition definition;
    private final DefaultConfig defaults;
    public SkillSpell(SkillDefinition definition){
        this.definition=definition;
        defaults=new DefaultConfig().setSchoolResource(SchoolFallback.resolve(new ResourceLocation(definition.school())))
            .setMaxLevel(definition.max_level()).setMinRarity(SpellRarity.valueOf(definition.min_rarity()))
            .setCooldownSeconds(definition.cooldown()).build();
        baseManaCost=definition.mana();manaCostPerLevel=definition.mana_growth();
        baseSpellPower=Math.round(definition.damage());spellPowerPerLevel=Math.max(1,Math.round(definition.damage()*.12f));
        castTime=definition.cast_time();
    }
    @Override public ResourceLocation getSpellResource(){return BrutalityIron.id(definition.id());}
    @Override public DefaultConfig getDefaultConfig(){return defaults;}
    @Override public CastType getCastType(){return castTime==0?CastType.INSTANT:CastType.LONG;}
    @Override public boolean requiresLearning(){return false;}
    @Override public boolean allowLooting(){return true;}
    @Override public SpellRarity getRarity(int rank){return rank>=Math.max(1,getMaxLevel()-1)?SpellRarity.LEGENDARY:super.getRarity(rank);}
    @Override public int getMinLevelForRarity(SpellRarity rarity){for(int i=1;i<=getMaxLevel();i++)if(getRarity(i)==rarity)return i;return 0;}
    public float damage(int rank,LivingEntity caster){return definition.damage()==0?0:(definition.damage()+Math.max(0,rank-1)*definition.damage_growth())*getEntityPowerMultiplier(caster);}
    public int duration(int rank){return Math.min(1200,definition.duration()+Math.max(0,rank-1)*definition.duration_growth());}
    @Override public boolean checkPreCastConditions(Level level,int rank,LivingEntity caster,MagicData data){return caster instanceof Player p&&SkillRuntime.canCast(definition,p,rank);}
    @Override public List<MutableComponent> getUniqueInfo(int rank,LivingEntity caster){
        var lines=new java.util.ArrayList<MutableComponent>();lines.add(Component.translatable("spell.brutality_iron."+definition.id()+".guide"));
        if(definition.damage()>0){ boolean beam=definition.id().equals("triple_judgment")||definition.id().equals("triple_profane_beam"); lines.add(Component.literal((beam?"每次伤害：":"伤害：")+String.format(java.util.Locale.ROOT,"%.1f",damage(rank,caster)*(beam?.25f:1f)))); }
        if(definition.duration()>=20)lines.add(Component.literal("持续："+String.format(java.util.Locale.ROOT,"%.1f",duration(rank)/20f)+" 秒"));return lines;
    }
    @Override public void onServerPreCast(Level level,int rank,LivingEntity caster,MagicData data){
        super.onServerPreCast(level,rank,caster,data);
        if((definition.id().equals("triple_judgment")||definition.id().equals("triple_profane_beam")) && !level.isClientSide) level.playSound(null,caster.blockPosition(),SpellSounds.TRIPLE_CHARGE.get(),net.minecraft.sounds.SoundSource.PLAYERS,.7f,1f);
        if(definition.id().equals("spirit_bomb")&&caster instanceof Player p)SkillRuntime.effect(this,p,rank,"charging_display","spirit_bomb_flight",p.position().add(0,6,0),getCastTime(rank)+10);
        if(definition.id().equals("hellrok_slam")&&caster instanceof Player p)SkillRevisionRuntime.display(this,p,rank,"warmup","fx:inferno_impact",p.position(),getCastTime(rank)+1,4+rank*.2,p.getLookAngle());
    }
    @Override public void onServerCastComplete(Level level,int rank,LivingEntity caster,MagicData data,boolean cancelled){
        if(definition.id().equals("spirit_bomb")&&caster instanceof Player p)SkillRuntime.clearCharging(p,definition.id());
        if(definition.id().equals("hellrok_slam")&&caster instanceof Player p)SkillRuntime.clearWarmup(p,definition.id());
        super.onServerCastComplete(level,rank,caster,data,cancelled);
    }
    @Override public void onCast(Level level,int rank,LivingEntity caster,CastSource source,MagicData data){
        if(!level.isClientSide&&caster instanceof Player p){
            if(cancellableSummon()){
                var recasts=data.getPlayerRecasts();
                if(!recasts.hasRecastForSpell(this)){
                    var summons=new io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData();
                    for(var mob:SkillRuntime.summonNative(this,p,rank))io.redspace.ironsspellbooks.capabilities.magic.SummonManager.initSummon(p,mob,duration(rank),summons);
                    if(!summons.getSummons().isEmpty())recasts.addRecast(new io.redspace.ironsspellbooks.capabilities.magic.RecastInstance(getSpellId(),rank,2,duration(rank),source,summons),data);
                }
            }else if(cancellableEffect()){
                if(!data.getPlayerRecasts().hasRecastForSpell(this)){
                    SkillRuntime.cast(this,p,rank);
                    data.getPlayerRecasts().addRecast(new io.redspace.ironsspellbooks.capabilities.magic.RecastInstance(getSpellId(),rank,2,duration(rank),source,new io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData()),data);
                }
            }else SkillRuntime.cast(this,p,rank);
        }
        super.onCast(level,rank,caster,source,data);
    }
}
