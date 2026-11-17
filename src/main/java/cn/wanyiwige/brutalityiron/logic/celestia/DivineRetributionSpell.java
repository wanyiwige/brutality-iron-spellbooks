// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.celestia;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import net.goo.brutality.util.ModUtils;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import cn.wanyiwige.brutalityiron.entity.celestia.HolyBeamEntity;
import net.mcreator.terramity.init.TerramityModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DivineRetributionSpell
extends PortedLogic {
    public DivineRetributionSpell() {
        super(IBrutalitySpell.MagicSchool.CELESTIA, List.of(IBrutalitySpell.SpellCategory.CHANNELLING, IBrutalitySpell.SpellCategory.AOE), "divine_retribution", 50, 0.0f, 800, 140, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.RANGE, 15.0f, 5.0f, null, null)));
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
        return -20;
    }

    @Override
    public int getCastTimeLevelScaling() {
        return -10;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        BlockPos block = ModUtils.getBlockLookingAt(player, false, this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.RANGE)));
        if (block == null) {
            return false;
        }
        HolyBeamEntity holyBeam = new HolyBeamEntity((EntityType)cn.wanyiwige.brutalityiron.PortEntities.HOLY_BEAM.get(), player.level());
        holyBeam.setPos(block.getCenter());
        holyBeam.getPersistentData().putUUID("port_owner", player.getUUID());
        holyBeam.getPersistentData().putInt("port_rank", spellLevel);
        player.level().addFreshEntity((Entity)holyBeam);
        return true;
    }
}

