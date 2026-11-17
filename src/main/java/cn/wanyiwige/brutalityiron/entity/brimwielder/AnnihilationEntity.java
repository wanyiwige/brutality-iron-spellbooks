// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.entity.brimwielder;

import cn.wanyiwige.brutalityiron.NativeDamage;
import java.util.Random;
import net.goo.brutality.client.entity.BrutalityGeoEntity;
import net.goo.brutality.entity.base.BrutalityAbstractArrow;
import net.goo.brutality.entity.explosion.BloodExplosion;
import net.goo.brutality.magic.BrutalitySpell;
import cn.wanyiwige.brutalityiron.NativeSpellEntity;
import cn.wanyiwige.brutalityiron.logic.brimwielder.AnnihilationSpell;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.goo.brutality.util.helpers.ModExplosionHelper;
import net.mcreator.terramity.init.TerramityModMobEffects;
import net.mcreator.terramity.init.TerramityModParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AnnihilationEntity
extends BrutalityAbstractArrow
implements BrutalityGeoEntity,
NativeSpellEntity {
    private static final EntityDataAccessor<Integer> SPELL_LEVEL_DATA = SynchedEntityData.defineId(AnnihilationEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public AnnihilationEntity(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    protected boolean tryPickup(@NotNull Player pPlayer) {
        return false;
    }

    @NotNull
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
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

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SPELL_LEVEL_DATA, 1);
    }

    public SoundEvent getHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void setSpellLevel(int spellLevel) {
        this.entityData.set(SPELL_LEVEL_DATA, spellLevel);
    }

    @Override
    public BrutalitySpell getSpell() {
        return new AnnihilationSpell();
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
    public void onHitEntity(EntityHitResult entityHitResult) {
        LivingEntity entity;
        Entity entity2 = entityHitResult.getEntity();
        if (entity2 instanceof LivingEntity && (entity = (LivingEntity)entity2) != this.getOwner() && !entity.level().isClientSide()) {
            entity.addEffect(new MobEffectInstance((MobEffect)TerramityModMobEffects.HEXED.get(), 60, 0));
        }
    }

    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        Vec3 loc = pResult.getLocation();
        long seed = Double.doubleToLongBits(loc.x) ^ Double.doubleToLongBits(loc.y) ^ Double.doubleToLongBits(loc.z);
        Random seeded = new Random(seed);
        if ((float)seeded.nextInt(0, 100) < this.getSpell().getFinalStat(this.getSpellLevel(), this.getSpell().getStat(BrutalityTooltipHelper.SpellStatComponents.CHANCE))) {
            BloodExplosion explosion = new BloodExplosion(this.level(), this.getOwner(), cn.wanyiwige.brutalityiron.Content.spell(getSpell().getSpellName()).getDamageSource(this, getOwner()), null, loc.x, loc.y, loc.z, 3.0f, false, Level.ExplosionInteraction.NONE);
            explosion.damageScale = (float)this.getSpellLevel() * 0.15f;
            explosion.setEntityFilter(e -> !(e instanceof AnnihilationEntity));
            ModExplosionHelper.Server.explode(explosion, this.level(), true);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() && this.tickCount % 10 == 0 && !this.inGround) {
            this.level().addParticle((ParticleOptions)TerramityModParticleTypes.STYGIAN_PARTICLE.get(), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
        if (this.inGroundTime > 100) {
            this.discard();
        }
    }

    @Override
    public SoundEvent getHitEntitySoundEvent() {
        return SoundEvents.TRIDENT_HIT;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

