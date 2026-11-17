// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.voidwalker;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import net.goo.brutality.network.ClientboundParticlePacket;
import net.goo.brutality.network.PacketHandler;
import net.goo.brutality.particle.providers.WaveParticleData;
import net.goo.brutality.registry.BrutalityModParticles;
import net.goo.brutality.registry.BrutalityModSounds;
import net.goo.brutality.util.ModUtils;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.mcreator.terramity.init.TerramityModParticleTypes;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SpatialRuptureSpell
extends PortedLogic {
    public SpatialRuptureSpell() {
        super(IBrutalitySpell.MagicSchool.VOIDWALKER, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.AOE), "spatial_rupture", 30, 2.0f, 80, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.SIZE, 3.0f, 1.0f, Float.valueOf(0.0f), Float.valueOf(100.0f))));
    }

    @Override
    public float getManaCostLevelScaling() {
        return 5.0f;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        Level level = player.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            double playerX = player.getX();
            double playerY = player.getY(0.5);
            double playerZ = player.getZ();
            float offset = 0.1f + 0.025f * (float)(spellLevel + 1);
            for (int i = 0; i < 16 + spellLevel * 4; ++i) {
                PacketHandler.sendToNearbyClients(serverLevel, playerX, playerY, playerZ, 128.0, new ClientboundParticlePacket((SimpleParticleType)TerramityModParticleTypes.ANTIMATTER.get(), true, (float)playerX, (float)playerY, (float)playerZ, 0.0f, 0.0f, 0.0f, (player.getRandom().nextFloat() - 0.5f) * offset, (player.getRandom().nextFloat() - 0.5f) * offset, (player.getRandom().nextFloat() - 0.5f) * offset, 1));
            }
            WaveParticleData waveParticleData = new WaveParticleData((ParticleType)BrutalityModParticles.ANTIMATTER_WAVE.get(), this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.SIZE)), 40);
            PacketHandler.sendToNearbyClients(serverLevel, playerX, playerY, playerZ, 128.0, new ClientboundParticlePacket(waveParticleData, true, (float)playerX, (float)playerY, (float)playerZ, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1));
            ModUtils.applyWaveEffect(serverLevel, playerX, playerY, playerZ, Entity.class, waveParticleData, e -> (e instanceof Projectile || e instanceof LivingEntity) && e != player, e -> {
                Vec3 ePos = e.getPosition(1.0f);
                Vec3 playerToEntity = player.getPosition(1.0f).vectorTo(ePos).normalize();
                playerToEntity.add(0.0, 0.1, 0.0).scale(0.2 * (double)spellLevel);
                e.push(playerToEntity.x, playerToEntity.y, playerToEntity.z);
                NativeDamage.hurt(e, this.getFinalDamage((Entity)player, spellLevel), this, player, player);
                if (e instanceof Player) {
                    ((ServerPlayer)player).connection.send((Packet)new ClientboundSetEntityMotionPacket(e));
                }
            });
            serverLevel.playSound(null, player.getOnPos(), (SoundEvent)BrutalityModSounds.BASS_BOOM.get(), SoundSource.PLAYERS, (float)spellLevel, 1.0f);
        }
        return true;
    }
}

