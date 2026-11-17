// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.entity.cosmic;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import net.goo.brutality.client.entity.BrutalityGeoEntity;
import net.goo.brutality.entity.base.BrutalityAbstractArrow;
import net.goo.brutality.magic.BrutalitySpell;
import cn.wanyiwige.brutalityiron.NativeSpellEntity;
import cn.wanyiwige.brutalityiron.logic.cosmic.CosmicCataclysmSpell;
import net.goo.brutality.network.ClientboundParticlePacket;
import net.goo.brutality.network.PacketHandler;
import net.goo.brutality.particle.providers.WaveParticleData;
import net.goo.brutality.registry.BrutalityModParticles;
import net.goo.brutality.registry.BrutalityModSounds;
import net.goo.brutality.util.ModUtils;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CosmicCataclysmEntity
extends BrutalityAbstractArrow
implements BrutalityGeoEntity,
NativeSpellEntity {
    private static final EntityDataAccessor<Integer> SPELL_LEVEL_DATA = SynchedEntityData.defineId(CosmicCataclysmEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    protected boolean shouldApplyWaveEffect = true;
    AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public CosmicCataclysmEntity(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
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
        return 0.5f;
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
        return new CosmicCataclysmSpell();
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
        if (this.inGroundTime > 40) {
            this.discard();
        }
    }

    protected void onHit(@NotNull HitResult hitResult) {
        super.onHit(hitResult);
        int spellLevel = this.getSpellLevel();
        BrutalitySpell spell = this.getSpell();
        float size = spell.getFinalStat(spellLevel, spell.getStat(BrutalityTooltipHelper.SpellStatComponents.SIZE));
        WaveParticleData waveParticleData = new WaveParticleData((ParticleType)BrutalityModParticles.COSMIC_WAVE.get(), size * 2.0f, 50);
        Vec3 loc = hitResult.getLocation();
        double x = loc.x();
        double y = loc.y();
        double z = loc.z();
        this.level().playLocalSound(this.getOnPos(), (SoundEvent)BrutalityModSounds.METEOR_CRASH.get(), SoundSource.AMBIENT, (float)Math.min(spellLevel, 50), 1.5f - (float)spellLevel / 50.0f, false);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            Entity ownerEntity = this.getOwner();
            if (ownerEntity instanceof LivingEntity) {
                LivingEntity owner = (LivingEntity)ownerEntity;
                PacketHandler.sendToNearbyClients(serverLevel, x, y, z, 128.0, new ClientboundParticlePacket(waveParticleData, true, (float)x, (float)y + 0.1f, (float)z, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                if (this.shouldApplyWaveEffect) {
                    ModUtils.applyWaveEffect(serverLevel, (Entity)this, Entity.class, waveParticleData, e -> e != owner, e -> {
                        NativeDamage.hurt(e, spell.getFinalDamage((Entity)owner, spellLevel), this.getSpell(), this.getOwner(), this);
                        if (e instanceof Player) {
                            ((ServerPlayer)owner).connection.send((Packet)new ClientboundSetEntityMotionPacket(e));
                        }
                    });
                }
            }
        }
        List<Entity> nearbyEntities = this.level().getEntities((Entity)this, this.getBoundingBox().inflate((double)((float)spellLevel / 2.0f + 3.0f)), e -> e instanceof LivingEntity && (!(e instanceof Player) || !e.isSpectator() && !((Player)e).isCreative()));
        for (Entity entity : nearbyEntities) {
            if (entity == this || entity == this.getOwner()) continue;
            Vec3 targetPos = entity.getPosition(1.0f);
            Vec3 sourcePos = this.getPosition(1.0f).add(0.0, -2.0, 0.0);
            Vec3 targetVector = targetPos.subtract(sourcePos).normalize();
            entity.addDeltaMovement(targetVector.scale((double)0.2f).scale((double)(1 + spellLevel)));
            if (entity instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer)entity;
                player.connection.send((Packet)new ClientboundSetEntityMotionPacket((Entity)player));
            }
            if (this.getOwner() != null) {
                NativeDamage.hurt(entity, this.getFinalDamage(spell, this.getOwner(), spellLevel), this.getSpell(), this.getOwner(), this);
                continue;
            }
            NativeDamage.hurt(entity, this.getFinalDamage(spell, this.getOwner(), spellLevel), this.getSpell(), this.getOwner(), this);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        this.setNoGravity(true);
    }

    @Override
    public SoundEvent getHitEntitySoundEvent() {
        return SoundEvents.EMPTY;
    }

    public SoundEvent getHitGroundSoundEvent() {
        return SoundEvents.EMPTY;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, 0, this::predicate)});
    }

    private PlayState predicate(AnimationState<CosmicCataclysmEntity> state) {
        if (this.inGround) {
            state.setAndContinue(RawAnimation.begin().thenPlayAndHold("ground"));
        } else {
            state.setAndContinue(RawAnimation.begin().thenPlay("idle"));
        }
        return PlayState.CONTINUE;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

