// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.cosmic;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import net.goo.brutality.util.ModUtils;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.mcreator.terramity.init.TerramityModParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GravitokinesisSpell
extends PortedLogic {
    public GravitokinesisSpell() {
        super(IBrutalitySpell.MagicSchool.COSMIC, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.TARGET, IBrutalitySpell.SpellCategory.UTILITY), "gravitokinesis", 40, 0.0f, 160, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.RANGE, 5.0f, 2.0f, Float.valueOf(5.0f), Float.valueOf(100.0f)), new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.SIZE, 1.0f, 0.5f, Float.valueOf(1.0f), Float.valueOf(5.0f)), new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.PIERCE, 3.0f, 1.0f, Float.valueOf(1.0f), null)));
    }

    @Override
    public int getCooldownLevelScaling() {
        return -5;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        float radius = this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.SIZE));
        float baseRange = this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.RANGE));
        float pierceCap = this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.PIERCE));
        Level level = player.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            ModUtils.RayData<LivingEntity> entitiesInRay = ModUtils.getEntitiesInRay(LivingEntity.class, (LivingEntity)player, baseRange, ClipContext.Fluid.NONE, ClipContext.Block.OUTLINE, radius, e -> e != player, (int)pierceCap, null);
            List<LivingEntity> targets = entitiesInRay.entityList();
            Vec3 pushVec = player.getLookAngle().scale((double)(1.0f + (float)spellLevel / 3.0f));
            Vec3 pullVec = pushVec.scale(-1.0);
            if (player.isShiftKeyDown()) {
                targets.forEach(target -> {
                    target.push(pushVec.x(), pushVec.y(), pushVec.z());
                    serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)TerramityModParticleTypes.COSMIC_BOOM.get()), target.getX(), target.getY(0.5), target.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
                    if (target instanceof ServerPlayer) {
                        ServerPlayer serverPlayer = (ServerPlayer)target;
                        serverPlayer.connection.send((Packet)new ClientboundSetEntityMotionPacket((Entity)target));
                    }
                });
                player.push(pullVec.x(), pullVec.y() + 0.25, pullVec.z());
                ((ServerPlayer)player).connection.send((Packet)new ClientboundSetEntityMotionPacket((Entity)player));
            } else {
                targets.forEach(target -> {
                    target.push(pullVec.x(), pullVec.y(), pullVec.z());
                    serverLevel.sendParticles((ParticleOptions)((SimpleParticleType)TerramityModParticleTypes.COSMIC_BOOM.get()), target.getX(), target.getY(0.5), target.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
                    if (target instanceof ServerPlayer) {
                        ServerPlayer serverPlayer = (ServerPlayer)target;
                        serverPlayer.connection.send((Packet)new ClientboundSetEntityMotionPacket((Entity)target));
                    }
                });
            }
        }
        return true;
    }
}

