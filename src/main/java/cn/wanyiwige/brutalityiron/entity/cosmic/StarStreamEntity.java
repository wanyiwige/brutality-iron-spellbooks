// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.entity.cosmic;

import cn.wanyiwige.brutalityiron.NativeDamage;
import net.goo.brutality.client.entity.BrutalityGeoEntity;
import net.goo.brutality.entity.base.BrutalityAbstractArrow;
import net.goo.brutality.magic.BrutalitySpell;
import cn.wanyiwige.brutalityiron.NativeSpellEntity;
import cn.wanyiwige.brutalityiron.logic.cosmic.StarStreamSpell;
import net.goo.brutality.registry.BrutalityModParticles;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

public class StarStreamEntity
extends BrutalityAbstractArrow
implements BrutalityGeoEntity,
NativeSpellEntity {
    private static final EntityDataAccessor<Integer> SPELL_LEVEL_DATA = SynchedEntityData.defineId(StarStreamEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final BrutalitySpell spell = new StarStreamSpell();
    AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public StarStreamEntity(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
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
    public float getGravity() {
        return 0.01f;
    }

    @Override
    protected int getLifespan() {
        return 240;
    }

    @Override
    public void setSpellLevel(int spellLevel) {
        this.entityData.set(SPELL_LEVEL_DATA, spellLevel);
    }

    @Override
    public BrutalitySpell getSpell() {
        return spell;
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

    protected void onHit(@NotNull HitResult hitResult) {
        super.onHit(hitResult);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)BrutalityModParticles.COSMIC_PARTICLE.get(0).get()), this.getX(), this.getY(), this.getZ(), 2, 0.25, 0.25, 0.25, 0.0);
            serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)BrutalityModParticles.COSMIC_PARTICLE.get(1).get()), this.getX(), this.getY(), this.getZ(), 2, 0.25, 0.25, 0.25, 0.0);
        }
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity victim = pResult.getEntity();
        if (victim == this.getOwner()) {
            return;
        }
        victim.invulnerableTime = 0;
        if (this.getOwner() != null) {
            NativeDamage.hurt(victim, this.getFinalDamage(this.getSpell(), this.getOwner(), this.getSpellLevel()), this.getSpell(), this.getOwner(), this);
        } else {
            NativeDamage.hurt(victim, this.getFinalDamage(this.getSpell(), this.getOwner(), this.getSpellLevel()), this.getSpell(), this.getOwner(), this);
        }
    }

    @NotNull
    public SoundEvent getHitGroundSoundEvent() {
        return SoundEvents.EMPTY;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

