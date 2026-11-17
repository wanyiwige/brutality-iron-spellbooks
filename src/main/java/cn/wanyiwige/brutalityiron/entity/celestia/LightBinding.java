// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.entity.celestia;

import cn.wanyiwige.brutalityiron.NativeDamage;
import net.goo.brutality.client.entity.BrutalityGeoEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class LightBinding
extends Entity
implements BrutalityGeoEntity {
    AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this, (boolean)true);

    public LightBinding(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    protected void defineSynchedData() {
    }

    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    protected void addAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController[]{new AnimationController((GeoAnimatable)this, state -> state.setAndContinue(RawAnimation.begin().thenLoop("idle"))).triggerableAnim("spin", RawAnimation.begin().thenPlayAndHold("idle"))});
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

