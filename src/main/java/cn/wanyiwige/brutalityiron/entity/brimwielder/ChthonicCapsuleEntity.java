// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.entity.brimwielder;

import cn.wanyiwige.brutalityiron.NativeDamage;
import net.goo.brutality.client.entity.BrutalityGeoEntity;
import net.goo.brutality.entity.base.BrutalityAbstractPhysicsProjectile;
import net.goo.brutality.entity.explosion.BloodExplosion;
import net.goo.brutality.magic.BrutalitySpell;
import cn.wanyiwige.brutalityiron.NativeSpellEntity;
import cn.wanyiwige.brutalityiron.logic.voidwalker.GraviticImplosionSpell;
import net.goo.brutality.registry.BrutalityModSounds;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.goo.brutality.util.helpers.ModExplosionHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ChthonicCapsuleEntity
extends BrutalityAbstractPhysicsProjectile
implements BrutalityGeoEntity,
NativeSpellEntity {
    private static final EntityDataAccessor<Integer> SPELL_LEVEL_DATA = SynchedEntityData.defineId(ChthonicCapsuleEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public ChthonicCapsuleEntity(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    protected boolean tryPickup(Player pPlayer) {
        return false;
    }

    public boolean fireImmune() {
        return true;
    }

    @Override
    public float getSizeScaling() {
        return 2.0f;
    }

    @Override
    public float getModelHeight() {
        return 8.0f;
    }

    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    @Override
    public int getSpellLevel() {
        return (Integer)this.entityData.get(SPELL_LEVEL_DATA);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SPELL_LEVEL_DATA, 1);
    }

    @Override
    public void setSpellLevel(int spellLevel) {
        this.entityData.set(SPELL_LEVEL_DATA, spellLevel);
    }

    @Override
    public BrutalitySpell getSpell() {
        return new GraviticImplosionSpell();
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("spell_level")) {
            this.setSpellLevel(tag.getInt("spell_level"));
        }
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("spell_level", this.getSpellLevel());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.inGround) {
            this.lockPitch();
            this.lockRoll();
            this.lockYaw();
        }
        if (this.inGroundTime >= 100) {
            this.doExplosion();
            if (this.inGroundTime > 100) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        this.triggerAnim("controller", "impact");
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        this.doExplosion();
        this.discard();
    }

    private void doExplosion() {
        BloodExplosion explosion = new BloodExplosion(this.level(), this.getOwner(), cn.wanyiwige.brutalityiron.Content.spell(getSpell().getSpellName()).getDamageSource(this, getOwner()), null, this.getX(), this.getY(), this.getZ(), 3.0f, false, Level.ExplosionInteraction.NONE);
        explosion.damage = Float.valueOf(this.getFinalDamage(this.getSpell(), this.getOwner(), this.getSpellLevel()));
        explosion.setEntityFilter(e -> !(e instanceof ChthonicCapsuleEntity) && e != this.getOwner());
        ModExplosionHelper.Server.explode(explosion, this.level(), true);
        float size = this.getSpell().getFinalStat(this.getSpellLevel(), this.getSpell().getStat(BrutalityTooltipHelper.SpellStatComponents.SIZE));
        this.level().getEntitiesOfClass(Entity.class, this.getBoundingBox().inflate((double)size), e -> e != this).forEach(e -> {
            Vec3 pushAng = e.getPosition(1.0f).subtract(this.getPosition(1.0f));
            pushAng.normalize().scale((double)((float)this.getSpellLevel() / 3.0f));
            e.push(pushAng.x(), pushAng.y(), pushAng.z());
            if (e instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)e;
                serverPlayer.connection.send((Packet)new ClientboundSetEntityMotionPacket((Entity)serverPlayer));
            }
        });
    }

    @NotNull
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return (SoundEvent)BrutalityModSounds.SQUELCH.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "controller", state -> PlayState.CONTINUE).triggerableAnim("impact", RawAnimation.begin().thenPlayAndHold("impact"))});
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected int getBounceCount() {
        return 0;
    }
}

