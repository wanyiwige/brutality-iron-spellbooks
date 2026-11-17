// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.celestia;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import net.goo.brutality.registry.BrutalityModMobEffects;
import net.goo.brutality.util.ModUtils;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.mcreator.terramity.init.TerramityModParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LightBindingSpell
extends PortedLogic {
    public LightBindingSpell() {
        super(IBrutalitySpell.MagicSchool.CELESTIA, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.TARGET, IBrutalitySpell.SpellCategory.DEBUFF), "light_binding", 40, 0.0f, 200, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.RANGE, 10.0f, 4.0f, Float.valueOf(0.0f), Float.valueOf(40.0f)), new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.DURATION, 20.0f, 10.0f, Float.valueOf(0.0f), Float.valueOf(100.0f))));
    }

    @Override
    public float getManaCostLevelScaling() {
        return 10.0f;
    }

    @Override
    public float getDamageLevelScaling() {
        return 0.0f;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        Player targetPlayer;
        BrutalityTooltipHelper.SpellStatComponent range = this.getStat(BrutalityTooltipHelper.SpellStatComponents.RANGE);
        BrutalityTooltipHelper.SpellStatComponent duration = this.getStat(BrutalityTooltipHelper.SpellStatComponents.DURATION);
        Entity target = ModUtils.getEntityPlayerLookingAt(player, this.getFinalStat(spellLevel, range));
        if (!(target instanceof LivingEntity)) {
            player.displayClientMessage((Component)Component.translatable((String)"spell.brutality.dematerialize.no_target_found"), true);
            return false;
        }
        LivingEntity livingTarget = (LivingEntity)target;
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
                ModUtils.sendParticles(serverLevel, (SimpleParticleType)TerramityModParticleTypes.HOLY_GLINT.get(), true, particlePos, 0.0, 0.0, 0.0, 0, 1.0);
            }
            ModUtils.sendParticles(serverLevel, (SimpleParticleType)TerramityModParticleTypes.HOLY_GLINT.get(), true, target, (double)(target.getBbWidth() / 2.0f), (double)(target.getBbHeight() / 2.0f), (double)(target.getBbWidth() / 2.0f), (int)(target.getBoundingBox().getSize() * 10.0), 0.0);
        }
        livingTarget.addEffect(new MobEffectInstance((MobEffect)BrutalityModMobEffects.LIGHT_BOUND.get(), (int)this.getFinalStat(spellLevel, duration), 0));
        return true;
    }
}

