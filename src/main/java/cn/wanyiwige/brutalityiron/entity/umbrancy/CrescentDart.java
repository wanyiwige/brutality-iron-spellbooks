// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.entity.umbrancy;

import com.google.common.collect.Lists;
import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import net.goo.brutality.client.entity.BrutalityGeoEntity;
import net.goo.brutality.entity.base.BrutalityShuriken;
import net.goo.brutality.magic.BrutalitySpell;
import cn.wanyiwige.brutalityiron.NativeSpellEntity;
import cn.wanyiwige.brutalityiron.logic.umbrancy.CrescentDartSpell;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CrescentDart
extends BrutalityShuriken
implements BrutalityGeoEntity,
NativeSpellEntity {
    private static final EntityDataAccessor<Integer> SPELL_LEVEL_DATA = SynchedEntityData.defineId(CrescentDart.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private List<LivingEntity> piercedEntities = Lists.newArrayListWithCapacity((int)Mth.floor((float)this.getSpell().getFinalStat(this.getSpellLevel(), this.getSpell().getStat(BrutalityTooltipHelper.SpellStatComponents.PIERCE))));
    AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache((GeoAnimatable)this);

    public CrescentDart(EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.collideWithBlocks = false;
    }

    protected boolean tryPickup(@NotNull Player pPlayer) {
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
        return new CrescentDartSpell();
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
        BrutalitySpell spell = this.getSpell();
        int pierceCap = Mth.floor((float)spell.getFinalStat(this.getSpellLevel(), spell.getStat(BrutalityTooltipHelper.SpellStatComponents.PIERCE)));
        int remainingCap = pierceCap - this.piercedEntities.size();
        if (remainingCap > 0) {
            LivingEntity nearest = this.level().getNearestEntity(LivingEntity.class, TargetingConditions.DEFAULT.selector(e -> !this.piercedEntities.contains(e)), null, this.getX(), this.getY(), this.getX(), this.getBoundingBox().inflate(15.0));
            if (nearest == null) {
                if (pierceCap != remainingCap) {
                    this.discard();
                }
            } else {
                Vec3 targetVec = nearest.getPosition(1.0f).add(0.0, (double)(nearest.getBbHeight() / 2.0f), 0.0).subtract(this.getPosition(1.0f));
                targetVec.normalize();
                this.setDeltaMovement(targetVec.scale((double)0.35f));
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity entity = pResult.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity target = (LivingEntity)entity;
            if (this.piercedEntities.contains(target)) {
                return;
            }
            this.piercedEntities.add(target);
            float dmg = this.getFinalDamage(this.getSpell(), this.getOwner(), this.getSpellLevel());
            target.invulnerableTime = 0;
            NativeDamage.hurt(target, dmg, this.getSpell(), this.getOwner(), this);
        }
        int pierceCap = Mth.floor((float)this.getSpell().getFinalStat(this.getSpellLevel(), this.getSpell().getStat(BrutalityTooltipHelper.SpellStatComponents.PIERCE)));
        if (this.piercedEntities.size() + 1 > pierceCap) {
            this.discard();
        }
    }

    @Override
    protected int getLifespan() {
        return 100;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

