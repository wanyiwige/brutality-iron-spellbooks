// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.brimwielder;

import java.util.Iterator;
import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import net.goo.brutality.entity.base.BrutalityRay;
import cn.wanyiwige.brutalityiron.entity.brimwielder.ExtinctionEntity;
import net.goo.brutality.event.forge.DelayedTaskScheduler;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import cn.wanyiwige.brutalityiron.PortEntities;
import net.goo.brutality.registry.BrutalityModEntities;
import net.goo.brutality.util.ModUtils;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.mcreator.terramity.init.TerramityModParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ExtinctionSpell
extends PortedLogic {
    public ExtinctionSpell() {
        super(IBrutalitySpell.MagicSchool.BRIMWIELDER, List.of(IBrutalitySpell.SpellCategory.CONTINUOUS, IBrutalitySpell.SpellCategory.AOE), "extinction", 1, 1.0f, 200, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.RANGE, 25.0f, 5.0f, Float.valueOf(25.0f), Float.valueOf(100.0f)), new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.PIERCE, 25.0f, 5.0f, Float.valueOf(25.0f), Float.valueOf(100.0f)), new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.SIZE, 0.5f, 0.25f, Float.valueOf(0.5f), Float.valueOf(5.0f))));
    }

    @Override
    public float getDamageLevelScaling() {
        return 0.5f;
    }

    @Override
    public float getManaCostLevelScaling() {
        return 0.25f;
    }

    @Override
    public int getCooldownLevelScaling() {
        return 5;
    }

    @Override
    public boolean onCastTick(Player player, ItemStack stack, int spellLevel) {
        Entity entity = this.getSpellEntity(player.level(), player);
        if (entity instanceof ExtinctionEntity) {
            ExtinctionEntity extinctionEntity = (ExtinctionEntity)entity;
            float range = this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.RANGE));
            float radius = this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.SIZE)) / 2.0f;
            int pierce = (int)this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.PIERCE));
            Level level = player.level();
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                ModUtils.RayData<LivingEntity> rayData = ModUtils.getEntitiesInRay(LivingEntity.class, (LivingEntity)player, range, ClipContext.Fluid.NONE, ClipContext.Block.OUTLINE, radius, e -> e != player, pierce, null);
                extinctionEntity.setDataMaxLength(rayData.distance() - 2);
                if (rayData.entityList() == null) {
                    return false;
                }
                for (LivingEntity target : rayData.entityList()) {
                    target.invulnerableTime = 0;
                    NativeDamage.hurt(target, this.getFinalDamage((Entity)player, spellLevel), this, player, player);
                }
                Vec3 endPos = rayData.endPos();
                serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)TerramityModParticleTypes.STYGIAN_PARTICLE.get()), endPos.x, endPos.y, endPos.z, 5, 1.0, 1.0, 1.0, 0.0);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        if (this.getSpellEntity(player.level(), player) == null) {
            ExtinctionEntity extinctionEntity = new ExtinctionEntity((EntityType<? extends BrutalityRay>)((EntityType)PortEntities.EXTINCTION_ENTITY.get()), player.level());
            extinctionEntity.setOwner((Entity)player);
            extinctionEntity.setPos(player.getEyePosition().add(player.getLookAngle().scale(3.0)));
            extinctionEntity.setSpellLevel(spellLevel);
            player.level().addFreshEntity((Entity)extinctionEntity);
            return true;
        }
        return false;
    }

    @Override
    public void onEndCast(Player player, ItemStack stack, int spellLevel) {
        super.onEndCast(player, stack, spellLevel);
        Entity entity = this.getSpellEntity(player.level(), player);
        if (entity instanceof ExtinctionEntity) {
            ExtinctionEntity extinctionEntity = (ExtinctionEntity)entity;
            extinctionEntity.triggerAnim("controller", "despawn");
            DelayedTaskScheduler.queueServerWork(player.level(), 20, () -> ((ExtinctionEntity)extinctionEntity).discard());
        }
    }

    private Entity getSpellEntity(Level level, Player player) {
        Iterator iterator = level.getEntitiesOfClass(ExtinctionEntity.class, player.getBoundingBox().inflate(25.0), e -> e.getOwner() == player).iterator();
        if (iterator.hasNext()) {
            ExtinctionEntity entity = (ExtinctionEntity)iterator.next();
            return entity;
        }
        return null;
    }
}

