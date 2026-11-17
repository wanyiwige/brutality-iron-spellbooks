// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.brimwielder;

import java.util.ArrayList;
import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.entity.brimwielder.AnnihilationEntity;
import net.goo.brutality.event.forge.DelayedTaskScheduler;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import net.goo.brutality.particle.providers.FlatParticleData;
import cn.wanyiwige.brutalityiron.PortEntities;
import net.goo.brutality.registry.BrutalityModEntities;
import net.goo.brutality.registry.BrutalityModParticles;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AnnihilationSpell
extends PortedLogic {
    public AnnihilationSpell() {
        super(IBrutalitySpell.MagicSchool.BRIMWIELDER, List.of(IBrutalitySpell.SpellCategory.CHANNELLING, IBrutalitySpell.SpellCategory.AOE), "annihilation", 100, 5.0f, 200, 80, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.SIZE, 5.0f, 1.0f, Float.valueOf(5.0f), Float.valueOf(15.0f)), new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.CHANCE, 5.0f, 3.0f, Float.valueOf(5.0f), Float.valueOf(50.0f)), new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.QUANTITY, 25.0f, 5.0f, Float.valueOf(25.0f), Float.valueOf(75.0f))));
    }

    @Override
    public int getCastTimeLevelScaling() {
        return 5;
    }

    @Override
    public float getDamageLevelScaling() {
        return 0.5f;
    }

    @Override
    public float getManaCostLevelScaling() {
        return 5.0f;
    }

    @Override
    public int getCooldownLevelScaling() {
        return 10;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        if (!player.onGround()) {
            player.displayClientMessage((Component)Component.translatable((String)"message.brutality.condition.on_ground"), true);
            return false;
        }
        float radius = this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.SIZE));
        int quantity = (int)this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.QUANTITY));
        int globalCount = 1;
        Vec3 spawnPos = player.getPosition(1.0f);
        Level level = player.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            ArrayList<Vec3> points = new ArrayList<Vec3>();
            int pointCount = 5;
            for (int i = 0; i < pointCount; ++i) {
                double angle = Math.PI * 2 * (double)i / (double)pointCount - 1.5707963267948966;
                double x = spawnPos.x() + (double)radius * Math.cos(angle);
                double y = spawnPos.y() + Mth.nextDouble((RandomSource)player.getRandom(), (double)15.0, (double)20.0);
                double z = spawnPos.z() + (double)radius * Math.sin(angle);
                points.add(new Vec3(x, y, z));
            }
            int[] starOrder = new int[]{0, 2, 4, 1, 3, 0};
            ArrayList<Vec3> lancePositions = new ArrayList<Vec3>();
            int segmentsPerEdge = quantity / pointCount;
            for (int i = 0; i < pointCount; ++i) {
                Vec3 current = (Vec3)points.get(starOrder[i]);
                Vec3 next = (Vec3)points.get(starOrder[i + 1]);
                Vec3 direction = next.subtract(current);
                for (int j = 0; j < segmentsPerEdge; ++j) {
                    double t = (double)j / (double)segmentsPerEdge;
                    Vec3 finalPos = current.add(direction.scale(t));
                    lancePositions.add(finalPos);
                }
            }
            for (Vec3 finalPos : lancePositions) {
                AnnihilationEntity annihilationEntity = new AnnihilationEntity((EntityType<? extends AbstractArrow>)((EntityType)PortEntities.ANNIHILATION_ENTITY.get()), (Level)serverLevel);
                annihilationEntity.setPos(finalPos.x, finalPos.y, finalPos.z);
                annihilationEntity.setOwner((Entity)player);
                annihilationEntity.setSpellLevel(spellLevel);
                annihilationEntity.setBaseDamage(this.getFinalDamage((Entity)player, spellLevel));
                DelayedTaskScheduler.queueServerWork((Level)serverLevel, globalCount, () -> serverLevel.addFreshEntity((Entity)annihilationEntity));
                ++globalCount;
            }
        } else {
            FlatParticleData data = new FlatParticleData((ParticleType)BrutalityModParticles.HEXING_CIRCLE_PARTICLE.get(), radius);
            player.level().addAlwaysVisibleParticle(data, true, spawnPos.x, spawnPos.y + (double)0.01f, spawnPos.z - 0.5, 0.0, 0.0, 0.0);
        }
        return true;
    }
}

