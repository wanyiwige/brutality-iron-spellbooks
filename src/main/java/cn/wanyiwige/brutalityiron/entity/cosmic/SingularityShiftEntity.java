// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.entity.cosmic;

import com.lowdragmc.photon.client.fx.EntityEffect;
import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import net.goo.brutality.client.entity.BrutalityGeoEntity;
import net.goo.brutality.entity.base.BrutalityAbstractPhysicsProjectile;
import net.goo.brutality.magic.BrutalitySpell;
import cn.wanyiwige.brutalityiron.NativeSpellEntity;
import cn.wanyiwige.brutalityiron.logic.cosmic.SingularityShiftSpell;
import net.goo.brutality.registry.BrutalityModMobEffects;
import net.goo.brutality.registry.BrutalityModParticles;
import net.goo.brutality.util.ModResources;
import net.goo.brutality.util.ModUtils;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SingularityShiftEntity
extends BrutalityAbstractPhysicsProjectile
implements BrutalityGeoEntity,
NativeSpellEntity {
    private static final EntityDataAccessor<Integer> SPELL_LEVEL_DATA = SynchedEntityData.defineId(SingularityShiftEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> WEIGHTLESS_DATA = SynchedEntityData.defineId(SingularityShiftEntity.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public SingularityShiftEntity(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        SingletonGeoAnimatable.registerSyncedAnimatable((GeoAnimatable)this);
    }

    @Override
    public String texture() {
        return "star_stream";
    }

    @Override
    public String model() {
        return "star_stream";
    }

    protected boolean tryPickup(Player pPlayer) {
        return false;
    }

    public boolean fireImmune() {
        return true;
    }

    @Override
    public float getSizeScaling() {
        return 0.0f;
    }

    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    @Override
    public int getSpellLevel() {
        return (Integer)this.entityData.get(SPELL_LEVEL_DATA);
    }

    @Override
    public void setSpellLevel(int spellLevel) {
        this.entityData.set(SPELL_LEVEL_DATA, spellLevel);
    }

    public boolean isWeightless() {
        return (Boolean)this.entityData.get(WEIGHTLESS_DATA);
    }

    public void setWeightless(boolean weightless) {
        this.entityData.set(WEIGHTLESS_DATA, weightless);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SPELL_LEVEL_DATA, 1);
        this.entityData.define(WEIGHTLESS_DATA, true);
    }

    @Override
    public float getGravity() {
        return 0.01f;
    }

    @Override
    protected int getLifespan() {
        return 1500;
    }

    @Override
    public BrutalitySpell getSpell() {
        return new SingularityShiftSpell();
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("spell_level")) {
            this.setSpellLevel(tag.getInt("spell_level"));
        }
        if (tag.contains("weightless")) {
            this.setWeightless(tag.getBoolean("weightless"));
        }
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("spell_level", this.getSpellLevel());
        tag.putBoolean("weightless", this.isWeightless());
    }

    @Override
    protected void onHit(@NotNull HitResult hitResult) {
        super.onHit(hitResult);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)BrutalityModParticles.COSMIC_PARTICLE.get(0).get()), this.getX(), this.getY(), this.getZ(), 2, 0.25, 0.25, 0.25, 0.0);
            serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)BrutalityModParticles.COSMIC_PARTICLE.get(1).get()), this.getX(), this.getY(), this.getZ(), 2, 0.25, 0.25, 0.25, 0.0);
            this.stopTriggeredAnimation(null, "show");
        } else {
            EntityEffect gravityField = new EntityEffect(this.isWeightless() ? ModResources.GRAVITY_FIELD_UP_FX : ModResources.GRAVITY_FIELD_DOWN_FX, this.level(), (Entity)this, EntityEffect.AutoRotate.NONE);
            float scale = this.getSpell().getFinalStat(this.getSpellLevel(), this.getSpell().getStat(BrutalityTooltipHelper.SpellStatComponents.SIZE));
            scale *= 0.4f;
            gravityField.setScale((double)(1.0f + (scale -= 0.8f)), (double)(1.0f + scale), (double)(1.0f + scale));
            gravityField.start();
        }
    }

    public void onAddedToWorld() {
        super.onAddedToWorld();
        this.triggerAnim(null, "show");
    }

    @Override
    public void tick() {
        super.tick();
        if (this.inGround && !this.level().isClientSide() && this.tickCount % 5 == 0) {
            BrutalitySpell spell = this.getSpell();
            float range = spell.getFinalStat(this.getSpellLevel(), spell.getStat(BrutalityTooltipHelper.SpellStatComponents.SIZE));
            List<LivingEntity> entities = ModUtils.getEntitiesInCylinder(LivingEntity.class, (Entity)this, e -> e != this, range + 0.5f, range * 2.0f);
            entities.forEach(e -> {
                MobEffectInstance effectInstance = new MobEffectInstance(this.isWeightless() ? (MobEffect)BrutalityModMobEffects.WEIGHTLESSNESS.get() : (MobEffect)BrutalityModMobEffects.HYPERGRAVITY.get(), 6, this.getSpellLevel(), false, true);
                e.addEffect(effectInstance);
            });
        }
    }

    @Override
    protected int getInGroundLifespan() {
        return (int)this.getSpell().getFinalStat(this.getSpellLevel(), this.getSpell().getStat(BrutalityTooltipHelper.SpellStatComponents.DURATION));
    }

    @NotNull
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.EMPTY;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> state.setAndContinue(RawAnimation.begin().thenPlayAndHold("hide"))).triggerableAnim("show", RawAnimation.begin().thenPlayAndHold("show"))});
    }

    @Override
    protected int getBounceCount() {
        return 0;
    }
}

