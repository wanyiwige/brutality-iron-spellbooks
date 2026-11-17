// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.entity.voidwalker;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import net.goo.brutality.client.entity.BrutalityGeoEntity;
import net.goo.brutality.entity.base.BrutalityAbstractPhysicsProjectile;
import net.goo.brutality.event.forge.DelayedTaskScheduler;
import net.goo.brutality.magic.BrutalitySpell;
import cn.wanyiwige.brutalityiron.NativeSpellEntity;
import cn.wanyiwige.brutalityiron.logic.voidwalker.GraviticImplosionSpell;
import net.goo.brutality.network.ClientboundParticlePacket;
import net.goo.brutality.network.PacketHandler;
import net.goo.brutality.registry.BrutalityModSounds;
import net.mcreator.terramity.init.TerramityModParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import software.bernie.geckolib.util.GeckoLibUtil;

public class GraviticImplosionEntity
extends BrutalityAbstractPhysicsProjectile
implements BrutalityGeoEntity,
NativeSpellEntity {
    private static final EntityDataAccessor<Integer> SPELL_LEVEL_DATA = SynchedEntityData.defineId(GraviticImplosionEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public GraviticImplosionEntity(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
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
        return 0.5f;
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
    protected int getLifespan() {
        return 100;
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

    @NotNull
    public SoundEvent getHitGroundSoundEvent() {
        return SoundEvents.EMPTY;
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("spell_level", this.getSpellLevel());
    }

    @Override
    public float getGravity() {
        return 0.025f;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.inGround) {
            this.lockPitch();
            this.lockRoll();
            this.lockYaw();
        }
        if (this.inGroundTime > 20) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        this.triggerAnim("controller", "impact");
        int spellLevel = this.getSpellLevel();
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            DelayedTaskScheduler.queueServerWork((Level)serverLevel, 6, () -> this.playSound((SoundEvent)BrutalityModSounds.SPACE_EXPLOSION.get(), Math.min(spellLevel, 50), 1.5f - (float)spellLevel / 50.0f));
            DelayedTaskScheduler.queueServerWork((Level)serverLevel, 15, () -> {
                for (int i = 0; i < 16 + spellLevel * 4; ++i) {
                    float randomX = (float)this.getRandomX(5 + spellLevel);
                    float randomY = (float)this.getY(this.random.nextDouble() * (double)(5 + spellLevel));
                    float randomZ = (float)this.getRandomZ(5 + spellLevel);
                    PacketHandler.sendToNearbyClients(serverLevel, this.getX(), this.getY(), this.getZ(), 128.0, new ClientboundParticlePacket((SimpleParticleType)TerramityModParticleTypes.ANTIMATTER.get(), true, randomX, randomY, randomZ, 0.0f, 0.0f, 0.0f, (float)(this.getX() - (double)randomX) * (0.035f + 0.015f * (float)(spellLevel + 1)), (float)(this.getY() - (double)randomY) * (0.035f + 0.015f * (float)(spellLevel + 1)), (float)(this.getZ() - (double)randomZ) * (0.035f + 0.015f * (float)(spellLevel + 1)), 1));
                }
                List<Entity> nearbyEntities = this.level().getEntities((Entity)this, this.getBoundingBox().inflate((double)((float)spellLevel / 2.0f + 3.0f)), e -> e instanceof LivingEntity && (!(e instanceof Player) || !e.isSpectator() && !((Player)e).isCreative()));
                for (Entity entity : nearbyEntities) {
                    if (entity == this || entity == this.getOwner()) continue;
                    Vec3 targetPos = entity.getPosition(1.0f);
                    Vec3 sourcePos = this.getPosition(1.0f).add(0.0, (double)this.getBbHeight(), 0.0);
                    Vec3 targetVector = sourcePos.subtract(targetPos).normalize();
                    entity.addDeltaMovement(targetVector.scale((double)0.15f).scale((double)((float)(1 + spellLevel) / 2.0f)));
                    if (entity instanceof ServerPlayer) {
                        ServerPlayer player = (ServerPlayer)entity;
                        player.connection.send((Packet)new ClientboundSetEntityMotionPacket((Entity)player));
                    }
                    float finalDamage = this.getSpell().getFinalDamage(this.getOwner(), spellLevel);
                    if (this.getOwner() != null) {
                        NativeDamage.hurt(entity, finalDamage, this.getSpell(), this.getOwner(), this);
                        continue;
                    }
                    NativeDamage.hurt(entity, finalDamage, this.getSpell(), this.getOwner(), this);
                }
            });
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        this.setNoGravity(true);
        this.setDeltaMovement(0.0, 0.0, 0.0);
    }

    @NotNull
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.EMPTY;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, "controller", state -> state.setAndContinue(RawAnimation.begin().thenLoop("idle"))).triggerableAnim("impact", RawAnimation.begin().thenPlayAndHold("impact"))});
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

