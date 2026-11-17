// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.cosmic;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.entity.cosmic.SingularityShiftEntity;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import cn.wanyiwige.brutalityiron.PortEntities;
import net.goo.brutality.registry.BrutalityModEntities;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SingularityShiftSpell
extends PortedLogic {
    public SingularityShiftSpell() {
        super(IBrutalitySpell.MagicSchool.COSMIC, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.UTILITY, IBrutalitySpell.SpellCategory.AOE), "singularity_shift", 80, 0.0f, 1200, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.SIZE, 2.0f, 1.0f, Float.valueOf(2.0f), Float.valueOf(7.0f)), new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.DURATION, 300.0f, 100.0f, Float.valueOf(300.0f), Float.valueOf(1100.0f))));
    }

    @Override
    public float getManaCostLevelScaling() {
        return 10.0f;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        Level level = player.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            SingularityShiftEntity singularityShiftEntity = new SingularityShiftEntity((EntityType<? extends AbstractArrow>)((EntityType)PortEntities.SINGULARITY_SHIFT_ENTITY.get()), (Level)serverLevel);
            singularityShiftEntity.setSpellLevel(spellLevel);
            singularityShiftEntity.setPos(player.getEyePosition());
            singularityShiftEntity.setOwner((Entity)player);
            singularityShiftEntity.setWeightless(player.isShiftKeyDown());
            singularityShiftEntity.shootFromRotation((Entity)player, player.getXRot(), player.getYRot(), 0.0f, Math.min(0.5f + (float)spellLevel * 0.1f, 1.0f), 0.0f);
            serverLevel.addFreshEntity((Entity)singularityShiftEntity);
        }
        return true;
    }
}

