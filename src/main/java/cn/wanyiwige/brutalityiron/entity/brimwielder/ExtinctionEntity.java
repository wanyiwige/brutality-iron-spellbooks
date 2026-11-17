// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.entity.brimwielder;

import cn.wanyiwige.brutalityiron.NativeDamage;
import net.goo.brutality.client.ClientAccess;
import net.goo.brutality.client.entity.BrutalityGeoEntity;
import net.goo.brutality.entity.base.BrutalityRay;
import net.goo.brutality.magic.BrutalitySpell;
import cn.wanyiwige.brutalityiron.NativeSpellEntity;
import cn.wanyiwige.brutalityiron.logic.brimwielder.ExtinctionSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ExtinctionEntity
extends BrutalityRay
implements BrutalityGeoEntity,
NativeSpellEntity {
    private static final EntityDataAccessor<Integer> SPELL_LEVEL_DATA = SynchedEntityData.defineId(ExtinctionEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this, (boolean)true);

    public ExtinctionEntity(EntityType<? extends BrutalityRay> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noCulling = true;
    }

    @Override
    public void tick() {
        if (this.firstTick && this.level().isClientSide()) {
            cn.wanyiwige.brutalityiron.client.ClientSounds.play(this);
        }
        super.tick();
    }

    public void onClientRemoval() {
        super.onClientRemoval();
        // Each sound instance stops when its own entity is removed.
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
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
    public void setSpellLevel(int spellLevel) {
        this.entityData.set(SPELL_LEVEL_DATA, spellLevel);
    }

    @Override
    public BrutalitySpell getSpell() {
        return new ExtinctionSpell();
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
}

