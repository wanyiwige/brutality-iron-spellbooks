// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.entity.umbrancy;

import cn.wanyiwige.brutalityiron.NativeDamage;
import net.goo.brutality.client.entity.BrutalityGeoEntity;
import net.goo.brutality.entity.base.BrutalityAbstractArrow;
import net.goo.brutality.magic.BrutalitySpell;
import cn.wanyiwige.brutalityiron.NativeSpellEntity;
import cn.wanyiwige.brutalityiron.logic.umbrancy.CrescentScytheSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CrescentScythe
extends BrutalityAbstractArrow
implements BrutalityGeoEntity,
NativeSpellEntity {
    private static final EntityDataAccessor<Integer> SPELL_LEVEL_DATA = SynchedEntityData.defineId(CrescentScythe.class, (EntityDataSerializer)EntityDataSerializers.INT);
    AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public CrescentScythe(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.collideWithBlocks = false;
    }

    protected boolean tryPickup(Player pPlayer) {
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

    @Override
    public void setSpellLevel(int spellLevel) {
        this.entityData.set(SPELL_LEVEL_DATA, spellLevel);
    }

    @Override
    public BrutalitySpell getSpell() {
        return new CrescentScytheSpell();
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
        if (this.getDeltaMovement().lengthSqr() < 64.0) {
            this.addDeltaMovement(this.getDeltaMovement().scale(0.05));
        }
    }

    @Override
    protected int getLifespan() {
        return 20;
    }

    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        LivingEntity livingEntity;
        Entity entity = pResult.getEntity();
        if (entity instanceof LivingEntity && (livingEntity = (LivingEntity)entity) != this.getOwner()) {
            float finalDamage = this.getFinalDamage(this.getSpell(), this.getOwner(), this.getSpellLevel()) * (this.level().isNight() ? 1.5f : 1.0f);
            if (this.getOwner() != null) {
                NativeDamage.hurt(livingEntity, finalDamage, this.getSpell(), this.getOwner(), this);
            } else {
                NativeDamage.hurt(livingEntity, finalDamage, this.getSpell(), this.getOwner(), this);
            }
        }
    }

    @NotNull
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.EMPTY;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> state.setAndContinue(RawAnimation.begin().thenPlayAndHold("spawn")))});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

