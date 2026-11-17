// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.celestia;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import net.goo.brutality.particle.providers.WaveParticleData;
import net.goo.brutality.registry.BrutalityModParticles;
import net.goo.brutality.util.ModUtils;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SacrificeSpell
extends PortedLogic {
    public SacrificeSpell() {
        super(IBrutalitySpell.MagicSchool.CELESTIA, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.AOE, IBrutalitySpell.SpellCategory.BUFF), "sacrifice", 80, 0.0f, 600, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.HEALING, 10.0f, 2.0f, null, null), new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.RANGE, 5.0f, 1.0f, null, Float.valueOf(25.0f))));
    }

    @Override
    public float getDamageLevelScaling() {
        return 0.0f;
    }

    @Override
    public float getManaCostLevelScaling() {
        return 10.0f;
    }

    @Override
    public int getCooldownLevelScaling() {
        return -5;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        BrutalityTooltipHelper.SpellStatComponent healthStat = this.getStat(BrutalityTooltipHelper.SpellStatComponents.HEALING);
        float healPool = this.getFinalStat(spellLevel, healthStat);
        Level level = player.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            WaveParticleData waveParticleData = new WaveParticleData((ParticleType)BrutalityModParticles.HEAL_WAVE.get(), this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.RANGE)), 20);
            serverLevel.sendParticles(waveParticleData, player.getX(), player.getY(0.1), player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
        List<Player> nearbyPlayers = ModUtils.getEntitiesInSphere(Player.class, (Entity)player, p -> p != player, this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.RANGE)));
        float casterMissingHealth = player.getMaxHealth() - player.getHealth();
        float casterHealAmount = Math.min(casterMissingHealth, healPool);
        player.heal(casterHealAmount);
        healPool -= casterHealAmount;
        if (healPool < 0.0f) {
            return true;
        }
        float excessHealing = 0.0f;
        if (!nearbyPlayers.isEmpty()) {
            float healingPerPlayer = healPool / (float)nearbyPlayers.size();
            for (Player receiver : nearbyPlayers) {
                float requiredHealing = receiver.getMaxHealth() - receiver.getHealth();
                if (requiredHealing < healingPerPlayer) {
                    receiver.heal(requiredHealing);
                    excessHealing += healingPerPlayer - requiredHealing;
                    continue;
                }
                receiver.heal(healingPerPlayer);
            }
        } else {
            return true;
        }
        if (excessHealing > 0.0f) {
            ArrayList<Player> eligiblePlayers = new ArrayList<Player>(nearbyPlayers);
            while (excessHealing > 0.0f && !eligiblePlayers.isEmpty()) {
                Player lowestHealthPlayer = eligiblePlayers.stream().min(Comparator.comparingDouble(p -> p.getHealth() / p.getMaxHealth())).orElse(null);
                float requiredHealing = lowestHealthPlayer.getMaxHealth() - lowestHealthPlayer.getHealth();
                if (requiredHealing <= 0.001f) {
                    eligiblePlayers.remove(lowestHealthPlayer);
                    continue;
                }
                if (requiredHealing < excessHealing) {
                    lowestHealthPlayer.heal(requiredHealing);
                    excessHealing -= requiredHealing;
                    eligiblePlayers.remove(lowestHealthPlayer);
                    continue;
                }
                lowestHealthPlayer.heal(excessHealing);
                break;
            }
        }
        return true;
    }
}

