package cn.wanyiwige.brutalityiron.skills;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Independent owned summons, without hostile source-mob AI or loot. */
public class SkillMinion extends PathfinderMob implements GeoEntity,OwnableEntity {
    private static final EntityDataAccessor<String> MODEL=SynchedEntityData.defineId(SkillMinion.class,EntityDataSerializers.STRING);
    private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
    public UUID ownerId;private Player directOwner;
    public String skill="";public int rank=1,remaining=200;public float damage=4;public boolean decoy;
    public SkillMinion(EntityType<? extends SkillMinion> type,Level level){super(type,level);xpReward=0;}
    public static AttributeSupplier.Builder attributes(){return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,24).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.ATTACK_DAMAGE,4).add(Attributes.FOLLOW_RANGE,24);}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(MODEL,"conjurling");}
    public String model(){return entityData.get(MODEL);}public void model(String id){entityData.set(MODEL,id);}
    public void owner(Player p){ownerId=p.getUUID();directOwner=p;}
    @Override public UUID getOwnerUUID(){return ownerId;}
    @Override public LivingEntity getOwner(){return directOwner!=null&&!directOwner.isRemoved()?directOwner:ownerId==null?null:level().getPlayerByUUID(ownerId);}
    @Override protected void registerGoals(){}
    @Override protected void dropAllDeathLoot(net.minecraft.world.damagesource.DamageSource source){/* no source-mob loot farming */}
    @Override public boolean canAttack(LivingEntity target){return getOwner() instanceof Player p&&SkillRuntime.enemy(p,target);}
    @Override public void tick(){
        super.tick();if(level().isClientSide)return;
        if(!SkillContent.SKILLS.containsKey(skill)){discard();return;}
        if(--remaining<=0||!(getOwner() instanceof Player owner)||!owner.isAlive()){discard();return;}
        if(owner.distanceToSqr(this)>900){moveTo(owner.position());getNavigation().stop();}
        if(decoy){
            if(tickCount%10==0)for(var mob:level().getEntitiesOfClass(Mob.class,getBoundingBox().inflate(10),m->m.getTarget()==owner))mob.setTarget(this);
            return;
        }
        if(tickCount%10==0){
            var targets=SkillRuntime.targets(owner,position(),18);setTarget(targets.isEmpty()?null:targets.get(0));
            if(getTarget()!=null){getNavigation().moveTo(getTarget(),1.1);getLookControl().setLookAt(getTarget(),40,40);}
            else if(distanceToSqr(owner)>16)getNavigation().moveTo(owner,1.15);
        }
        if(getTarget()!=null&&tickCount%25==0){
            if(skill.equals("arcane_retinue")||skill.equals("infernal_spirits")){
                if(hasLineOfSight(getTarget()))SkillRuntime.minionShot(this,owner,getTarget());
            }else if(distanceToSqr(getTarget())<5)SkillRuntime.hurt(skill,rank,owner,getTarget(),damage,this);
        }
    }
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(ownerId!=null)n.putUUID("SpellOwner",ownerId);n.putString("Skill",skill);n.putString("Model",model());n.putInt("Rank",rank);n.putInt("Remaining",remaining);n.putFloat("SpellDamage",damage);n.putBoolean("Decoy",decoy);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);if(n.hasUUID("SpellOwner"))ownerId=n.getUUID("SpellOwner");skill=n.getString("Skill");model(n.getString("Model"));rank=n.getInt("Rank");remaining=Math.min(n.getInt("Remaining"),40);damage=n.getFloat("SpellDamage");decoy=n.getBoolean("Decoy");}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
    @Override public void remove(RemovalReason reason){
        if(!level().isClientSide&&reason.shouldDestroy())io.redspace.ironsspellbooks.capabilities.magic.SummonManager.removeSummon(this);
        super.remove(reason);
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar c){
        c.add(new software.bernie.geckolib.core.animation.AnimationController<>(this,"source",0,state->{String clip=SkillVisuals.clip(model());return clip.isBlank()?software.bernie.geckolib.core.object.PlayState.STOP:state.setAndContinue(software.bernie.geckolib.core.animation.RawAnimation.begin().thenLoop(clip));}));
    }
}
