// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.brimwielder;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import net.goo.brutality.event.forge.DelayedTaskScheduler;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import net.goo.brutality.particle.providers.PointToPointParticleData;
import net.goo.brutality.registry.BrutalityModParticles;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.mcreator.terramity.init.TerramityModSounds;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class StygianStepSpell
extends PortedLogic {
    public StygianStepSpell() {
        super(IBrutalitySpell.MagicSchool.BRIMWIELDER, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.TARGET, IBrutalitySpell.SpellCategory.UTILITY), "stygian_step", 30, 5.0f, 20, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.SPEED, 1.0f, 0.25f, Float.valueOf(1.0f), Float.valueOf(5.0f)), new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.SIZE, 3.0f, 0.0f, Float.valueOf(3.0f), Float.valueOf(3.0f))));
    }

    @Override
    public float getDamageLevelScaling() {
        return 0.5f;
    }

    @Override
    public float getManaCostLevelScaling() {
        return 1.0f;
    }

    @Override
    public int getCooldownLevelScaling() {
        return -1;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        if (!player.onGround()) {
            player.displayClientMessage((Component)Component.translatable((String)"message.brutality.condition.on_ground"), true);
            return false;
        }
        Vec3 lookAngle = player.getLookAngle();
        Vec3 oldPos = player.getPosition(1.0f);
        float speed = this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.SPEED));
        float size = this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.SIZE));
        Vec3 moveVec = lookAngle.scale((double)speed);
        if (player instanceof ServerPlayer) {
            player.push(moveVec.x(), 0.0, moveVec.z());
            ((ServerPlayer)player).connection.send((Packet)new ClientboundSetEntityMotionPacket((Entity)player));
        }
        DelayedTaskScheduler.queueServerWork(player.level(), 5, () -> {
            player.level().getNearbyEntities(LivingEntity.class, TargetingConditions.DEFAULT, (LivingEntity)player, player.getBoundingBox().inflate((double)size)).forEach(e -> {
                NativeDamage.hurt(e, this.getFinalDamage((Entity)player, spellLevel), this, player, player);
                Vec3 pushAng = e.getPosition(1.0f).subtract(player.getPosition(1.0f));
                pushAng.normalize().scale((double)(speed / 10.0f));
                e.push(pushAng.x(), pushAng.y(), pushAng.z());
                if (e instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer)e;
                    serverPlayer.connection.send((Packet)new ClientboundSetEntityMotionPacket((Entity)player));
                }
            });
            PointToPointParticleData pointToPointParticleData = new PointToPointParticleData((ParticleType)BrutalityModParticles.STYGIAN_STEP_PARTICLE.get(), (float)oldPos.x, (float)oldPos.y, (float)oldPos.z, (float)player.getX(), (float)player.getY(), (float)player.getZ());
            Level patt3666$temp = player.level();
            if (patt3666$temp instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)patt3666$temp;
                if (player instanceof ServerPlayer) {
                    ServerPlayer serverPlayer = (ServerPlayer)player;
                    serverLevel.sendParticles(pointToPointParticleData, player.getX(), player.getY(), player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                    serverLevel.playSound(null, player.getOnPos(), (SoundEvent)TerramityModSounds.HEXED.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    serverPlayer.setDeltaMovement(Vec3.ZERO);
                    serverPlayer.connection.send((Packet)new ClientboundSetEntityMotionPacket((Entity)player));
                }
            }
        });
        return true;
    }
}

