// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.entity.brimwielder;

import cn.wanyiwige.brutalityiron.NativeDamage;
import net.goo.brutality.client.entity.BrutalityGeoEntity;
import net.goo.brutality.magic.BrutalitySpell;
import cn.wanyiwige.brutalityiron.NativeSpellEntity;
import cn.wanyiwige.brutalityiron.logic.brimwielder.DestructionSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DestructionEntity
extends Entity
implements BrutalityGeoEntity,
NativeSpellEntity {
    private static final EntityDataAccessor<Integer> SPELL_LEVEL_DATA = SynchedEntityData.defineId(DestructionEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public DestructionEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public boolean fireImmune() {
        return true;
    }

    @Override
    public float getSizeScaling() {
        return 0.05f;
    }

    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    @Override
    public int getSpellLevel() {
        return (Integer)this.entityData.get(SPELL_LEVEL_DATA);
    }

    protected void defineSynchedData() {
        this.entityData.define(SPELL_LEVEL_DATA, 1);
    }

    @Override
    public void setSpellLevel(int spellLevel) {
        this.entityData.set(SPELL_LEVEL_DATA, spellLevel);
    }

    @Override
    public BrutalitySpell getSpell() {
        return new DestructionSpell();
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("spell_level")) {
            this.setSpellLevel(tag.getInt("spell_level"));
        }
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("spell_level", this.getSpellLevel());
    }

    public void tick() {
        super.tick();
        if (this.tickCount > 20) {
            this.discard();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> state.setAndContinue(RawAnimation.begin().thenPlayAndHold("spawn")))});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

