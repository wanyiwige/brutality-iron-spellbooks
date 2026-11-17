// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.voidwalker;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.mcreator.terramity.init.TerramityModMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class IntangibleSpell
extends PortedLogic {
    public IntangibleSpell() {
        super(IBrutalitySpell.MagicSchool.VOIDWALKER, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.SELF, IBrutalitySpell.SpellCategory.BUFF), "intangible", 50, 0.0f, 200, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.DURATION, 20.0f, 10.0f, null, null)));
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
        return 15;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        player.addEffect(new MobEffectInstance((MobEffect)TerramityModMobEffects.PHASING.get(), (int)this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.DURATION))));
        return true;
    }
}

