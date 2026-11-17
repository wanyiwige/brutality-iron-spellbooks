// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.entity.umbrancy;

import cn.wanyiwige.brutalityiron.NativeDamage;
import net.goo.brutality.entity.base.BrutalityRay;
import net.goo.brutality.magic.BrutalitySpell;
import cn.wanyiwige.brutalityiron.NativeSpellEntity;
import cn.wanyiwige.brutalityiron.logic.umbrancy.PiercingMoonlightSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PiercingMoonlight
extends BrutalityRay
implements NativeSpellEntity, net.minecraftforge.entity.IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Integer> SPELL_LEVEL_DATA = SynchedEntityData.defineId(PiercingMoonlight.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this, (boolean)true);

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);
    }
    @Override public void writeSpawnData(net.minecraft.network.FriendlyByteBuf data) { data.writeVarInt(getOwner()==null?0:getOwner().getId()); }
    @Override public void readSpawnData(net.minecraft.network.FriendlyByteBuf data) { setOwner(level().getEntity(data.readVarInt())); }

    public void setYRot(float pYRot) {
        if (pYRot == 0.0f) {
            return;
        }
        super.setYRot(pYRot);
    }

    public void setXRot(float pXRot) {
        if (pXRot == 0.0f) {
            return;
        }
        super.setXRot(pXRot);
    }

    public PiercingMoonlight(EntityType<? extends BrutalityRay> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.shouldFollowOwner = false;
        this.noPhysics = true;
        this.hasImpulse = false;
    }

    @Override
    public float getSizeScaling() {
        return 0.0f;
    }

    @Override
    public int getSpellLevel() {
        return (Integer)this.entityData.get(SPELL_LEVEL_DATA);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SPELL_LEVEL_DATA, 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > 5) {
            this.discard();
        }
    }

    @Override
    public void setSpellLevel(int spellLevel) {
        this.entityData.set(SPELL_LEVEL_DATA, spellLevel);
    }

    @Override
    public BrutalitySpell getSpell() {
        return new PiercingMoonlightSpell();
    }

    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("spell_level")) {
            this.setSpellLevel(tag.getInt("spell_level"));
        }
    }

    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("spell_level", this.getSpellLevel());
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> state.setAndContinue(RawAnimation.begin().thenLoop("spawn")))});
    }
}

