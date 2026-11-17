// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.voidwalker;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.SpellSounds;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import net.goo.brutality.util.ModUtils;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.mcreator.terramity.init.TerramityModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class VoidWalkSpell
extends PortedLogic {
    public VoidWalkSpell() {
        super(IBrutalitySpell.MagicSchool.VOIDWALKER, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.TARGET, IBrutalitySpell.SpellCategory.UTILITY), "void_walk", 40, 0.0f, 160, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.RANGE, 20.0f, 10.0f, Float.valueOf(0.0f), Float.valueOf(250.0f))));
    }

    @Override
    public int getCooldownLevelScaling() {
        return -10;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        float baseRange = this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.RANGE));
        Level level = player.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            BlockPos blockPos = ModUtils.getBlockLookingAt(player, false, baseRange);
            if (blockPos == null) {
                return false;
            }
            BlockPos validPos = this.getValidPos((Level)serverLevel, blockPos, 3);
            if (validPos == null) {
                return false;
            }
            Vec3 startPos = player.getPosition(1.0f);
            Vec3 blockCenter = validPos.getCenter();
            player.teleportTo(blockCenter.x(), (double)validPos.getY(), blockCenter.z());
            Vec3 endPos = validPos.getCenter();
            Vec3 direction = startPos.vectorTo(endPos);
            double distance = direction.length();
            Vec3 unitDir = direction.normalize();
            for (double d = 0.0; d < distance; d += 0.5) {
                Vec3 intervalPos = new Vec3(unitDir.toVector3f()).scale(d);
                Vec3 particlePos = startPos.add(intervalPos);
                ModUtils.sendParticles(serverLevel, (SimpleParticleType)TerramityModParticleTypes.ANTIMATTER.get(), true, particlePos, 0.05, 0.05, 0.05, 1, 0.0);
            }
            ModUtils.sendParticles(serverLevel, (SimpleParticleType)TerramityModParticleTypes.ANTIMATTER.get(), true, (Entity)player, 0.0, 0.0, 0.0, 20, 0.0);
            serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)TerramityModParticleTypes.ANTIMATTER.get()), player.getX(), player.getY(), player.getZ(), 20, 1.0, 1.0, 1.0, 0.0);
            serverLevel.playSound(null, BlockPos.containing((Position)endPos), SpellSounds.VOID_TELEPORT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 0, false, false, true));
        }
        return true;
    }

    private BlockPos getValidPos(Level level, BlockPos basePos, int distance) {
        for (int i = 0; i < distance; ++i) {
            BlockPos floorPos = basePos.above(i);
            if (!ModUtils.isStandable((BlockGetter)level, floorPos)) continue;
            BlockPos air1 = floorPos.above();
            BlockPos air2 = air1.above();
            if (!level.getBlockState(air1).isAir() || !level.getBlockState(air2).isAir()) continue;
            return air1;
        }
        return null;
    }
}

