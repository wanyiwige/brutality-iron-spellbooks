// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.voidwalker;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import net.goo.brutality.registry.BrutalityDamageTypes;
import net.goo.brutality.util.ModUtils;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.mcreator.terramity.init.TerramityModParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DematerializeSpell
extends PortedLogic {
    public DematerializeSpell() {
        super(IBrutalitySpell.MagicSchool.VOIDWALKER, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.TARGET), "dematerialize", 40, 7.0f, 120, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.RANGE, 10.0f, 2.0f, Float.valueOf(0.0f), Float.valueOf(50.0f))));
    }

    @Override
    public float getDamageLevelScaling() {
        return 3.0f;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        Player targetPlayer;
        Entity target = ModUtils.getEntityPlayerLookingAt(player, this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.RANGE)));
        if (!(target instanceof LivingEntity)) {
            player.displayClientMessage((Component)Component.translatable((String)"spell.brutality.dematerialize.no_target_found"), true);
            return false;
        }
        LivingEntity livingTarget = (LivingEntity)target;
        if (target == player || player.isAlliedTo(target)) return false;
        if (livingTarget.getHealth() > this.getFinalDamage((Entity)player, spellLevel)) {
            player.displayClientMessage((Component)Component.translatable((String)"spell.brutality.dematerialize.too_much_health"), true);
            return false;
        }
        if (livingTarget.isSpectator() || livingTarget instanceof Player && (targetPlayer = (Player)livingTarget).isCreative()) {
            player.displayClientMessage((Component)Component.translatable((String)"spell.brutality.dematerialize.invalid_target"), true);
            return false;
        }
        Vec3 startPos = player.getPosition(1.0f).add(0.0, (double)(player.getBbHeight() / 2.0f), 0.0);
        Vec3 endPos = target.getPosition(1.0f).add(0.0, (double)(target.getBbHeight() / 2.0f), 0.0);
        Vec3 direction = startPos.vectorTo(endPos);
        double distance = direction.length();
        Vec3 unitDir = direction.normalize();
        Level level = player.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            for (double d = 0.0; d < distance; d += 0.5) {
                Vec3 intervalPos = new Vec3(unitDir.toVector3f()).scale(d);
                Vec3 particlePos = startPos.add(intervalPos);
                ModUtils.sendParticles(serverLevel, (SimpleParticleType)TerramityModParticleTypes.ANTIMATTER.get(), true, particlePos, 0.0, 0.0, 0.0, 0, 1.0);
            }
            ModUtils.sendParticles(serverLevel, (SimpleParticleType)TerramityModParticleTypes.ANTIMATTER.get(), true, target, (double)(target.getBbWidth() / 2.0f), (double)(target.getBbHeight() / 2.0f), (double)(target.getBbWidth() / 2.0f), (int)(target.getBoundingBox().getSize() * 10.0), 0.0);
        }
        if (livingTarget instanceof Player) {
            Player targetPlayer2 = (Player)livingTarget;
            NativeDamage.hurt(targetPlayer2, getFinalDamage(player, spellLevel), this, player, player);
            
        } else {
            NativeDamage.hurt(livingTarget, getFinalDamage(player, spellLevel), this, player, player);
        }
        return true;
    }
}

