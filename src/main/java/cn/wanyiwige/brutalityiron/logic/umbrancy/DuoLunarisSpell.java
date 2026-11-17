// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.umbrancy;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DuoLunarisSpell
extends PortedLogic {
    public DuoLunarisSpell() {
        super(IBrutalitySpell.MagicSchool.UMBRANCY, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.AOE), "duo_lunaris", 100, 0.0f, 200, 0, 1, List.of());
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        long noonTime = 6000L;
        long midnightTime = 18000L;
        Level level = player.level();
        long currentTime = level.getDayTime() % 24000L;
        float timeDiffFromNoon = Math.abs(currentTime - noonTime);
        float healingFactor = Math.min(timeDiffFromNoon / (float)(midnightTime - noonTime), 1.0f);
        float healPercent = 0.1f + 0.9f * healingFactor;
        MutableComponent component = Component.translatable((String)"spell.brutality.moonlit_mending.healed_for").append(healPercent * 100.0f + "% Max Health").withStyle(ChatFormatting.GREEN);
        player.displayClientMessage((Component)component, true);
        float maxHealth = player.getMaxHealth();
        float currentHealth = player.getHealth();
        float healAmount = maxHealth * healPercent;
        player.heal(healAmount);
        float missingHealth = maxHealth - currentHealth;
        float excessHealing = Math.max(0.0f, healAmount - missingHealth);
        if (excessHealing > 0.0f) {
            float currentAbsorption = player.getAbsorptionAmount();
            float newAbsorption = Math.min(maxHealth, currentAbsorption + excessHealing);
            player.setAbsorptionAmount(newAbsorption);
        }
        return true;
    }
}

