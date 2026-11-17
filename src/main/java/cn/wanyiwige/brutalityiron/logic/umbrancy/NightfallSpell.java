// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.umbrancy;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import net.goo.brutality.event.forge.DelayedTaskScheduler;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class NightfallSpell
extends PortedLogic {
    public NightfallSpell() {
        super(IBrutalitySpell.MagicSchool.UMBRANCY, List.of(IBrutalitySpell.SpellCategory.CHANNELLING, IBrutalitySpell.SpellCategory.AOE, IBrutalitySpell.SpellCategory.UTILITY), "nightfall", 60, 0.0f, 400, 40, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.RANGE, 25.0f, 1.0f, Float.valueOf(25.0f), Float.valueOf(50.0f)), new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.DURATION, 200.0f, 10.0f, Float.valueOf(200.0f), Float.valueOf(400.0f))));
    }

    @Override
    public float getDamageLevelScaling() {
        return 0.0f;
    }

    @Override
    public int getCastTimeLevelScaling() {
        return -1;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        Level level = player.level();
        long originalTime = level.getDayTime();
        int duration = (int)this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.DURATION));
        level.getNearbyEntities(LivingEntity.class, TargetingConditions.DEFAULT.ignoreInvisibilityTesting().ignoreLineOfSight(), (LivingEntity)player, player.getBoundingBox().inflate((double)(this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.RANGE)) / 2.0f))).forEach(e -> {
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration));
            e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration));
        });
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            cn.wanyiwige.brutalityiron.NightfallTime.begin(serverLevel, duration);
        }
        return true;
    }
}

