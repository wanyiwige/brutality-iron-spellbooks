package cn.wanyiwige.brutalityiron;

import java.util.List;
import net.goo.brutality.magic.BrutalitySpell;
import net.goo.brutality.magic.IBrutalitySpell;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper.SpellStatComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** Original geometry and effect formulas with native Iron spell power. */
public abstract class PortedLogic extends BrutalitySpell {
    protected PortedLogic(IBrutalitySpell.MagicSchool school,
            List<IBrutalitySpell.SpellCategory> categories, String name,
            int mana, float damage, int cooldown, int castTime, int descriptionCount,
            List<SpellStatComponent> stats) {
        super(school, categories, name, mana, damage, cooldown, castTime, descriptionCount, stats);
    }

    @Override
    public float getFinalStat(int rank, SpellStatComponent stat) {
        return NativeBalance.stat(getSpellName(), stat.type().name(), rank, super.getFinalStat(rank, stat));
    }

    @Override
    public float getFinalDamage(Entity caster, int rank) {
        float base = NativeBalance.damage(getSpellName(), rank);
        NativeSpell spell = Content.spell(getSpellName());
        return base * spell.getEntityPowerMultiplier(caster instanceof LivingEntity living ? living : null);
    }
}
