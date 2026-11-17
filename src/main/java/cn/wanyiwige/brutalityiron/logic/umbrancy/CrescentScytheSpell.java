// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.umbrancy;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.entity.umbrancy.CrescentScythe;
import net.goo.brutality.event.forge.DelayedTaskScheduler;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import cn.wanyiwige.brutalityiron.PortEntities;
import net.goo.brutality.registry.BrutalityModEntities;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CrescentScytheSpell
extends PortedLogic {
    public CrescentScytheSpell() {
        super(IBrutalitySpell.MagicSchool.UMBRANCY, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.AOE), "crescent_scythe", 40, 3.0f, 100, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.QUANTITY, 1.0f, 0.5f, Float.valueOf(1.0f), null), new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.SPEED, 0.25f, 0.0f, null, null)));
    }

    @Override
    public float getDamageLevelScaling() {
        return 2.0f;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        int quantity = Mth.floor((float)this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.QUANTITY)));
        float speed = this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.SPEED));
        float gap = 15.0f;
        Level level = player.level();
        for (int i = 0; i < quantity; ++i) {
            float angleOffset = ((float)i - (float)(quantity - 1) / 2.0f) * gap;
            CrescentScythe crescentScythe = new CrescentScythe((EntityType<? extends AbstractArrow>)((EntityType)PortEntities.CRESCENT_SCYTHE_ENTITY.get()), level);
            crescentScythe.setSpellLevel(spellLevel);
            crescentScythe.setOwner((Entity)player);
            crescentScythe.setPos(player.getX(), player.getY(0.75), player.getZ());
            crescentScythe.shootFromRotation((Entity)player, player.getXRot(), player.getYRot() + angleOffset, 0.0f, speed, 0.0f);
            if (i > 0) {
                DelayedTaskScheduler.queueServerWork(level, i * 2, () -> level.addFreshEntity((Entity)crescentScythe));
                continue;
            }
            level.addFreshEntity((Entity)crescentScythe);
        }
        return true;
    }
}

