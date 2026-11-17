// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.celestia;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import net.goo.brutality.registry.BrutalityModSounds;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.mcreator.terramity.init.TerramityModMobEffects;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HeavenlyFlightSpell
extends PortedLogic {
    public HeavenlyFlightSpell() {
        super(IBrutalitySpell.MagicSchool.CELESTIA, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.SELF, IBrutalitySpell.SpellCategory.BUFF), "heavenly_flight", 50, 0.0f, 200, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.DURATION, 200.0f, 100.0f, null, null)));
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
        return 140;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        float duration = this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.DURATION));
        player.addEffect(new MobEffectInstance((MobEffect)TerramityModMobEffects.MORTAL_FLIGHT.get(), (int)duration));
        player.playSound((SoundEvent)BrutalityModSounds.WINGS_FLAP.get(), 1.0f, Mth.nextFloat((RandomSource)player.getRandom(), (float)0.8f, (float)1.2f));
        return true;
    }
}

