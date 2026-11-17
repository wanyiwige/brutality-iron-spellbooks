// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.umbrancy;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import net.goo.brutality.entity.base.BrutalityRay;
import cn.wanyiwige.brutalityiron.entity.umbrancy.PiercingMoonlight;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import cn.wanyiwige.brutalityiron.PortEntities;
import net.goo.brutality.registry.BrutalityModEntities;
import net.goo.brutality.util.ModUtils;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;

public class PiercingMoonlightSpell
extends PortedLogic {
    public PiercingMoonlightSpell() {
        super(IBrutalitySpell.MagicSchool.UMBRANCY, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.AOE), "piercing_moonlight", 40, 3.0f, 50, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.PIERCE, 100.0f, 0.0f, null, null), new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.RANGE, 50.0f, 5.0f, Float.valueOf(50.0f), Float.valueOf(100.0f))));
    }

    @Override
    public float getDamageLevelScaling() {
        return 2.0f;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        PiercingMoonlight piercingMoonlight = new PiercingMoonlight((EntityType<? extends BrutalityRay>)((EntityType)PortEntities.PIERCING_MOONLIGHT_ENTITY.get()), player.level());
        piercingMoonlight.setOwner((Entity)player);
        piercingMoonlight.setPos(player.getEyePosition().add(player.getLookAngle().scale(3.0)));
        piercingMoonlight.setSpellLevel(spellLevel);
        piercingMoonlight.setYRot(player.getYRot());
        piercingMoonlight.setXRot(player.getXRot());
        player.level().addFreshEntity((Entity)piercingMoonlight);
        float range = this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.RANGE));
        int pierce = (int)this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.PIERCE));
        float damage = this.getFinalDamage((Entity)player, spellLevel);
        ModUtils.RayData<LivingEntity> rayData = ModUtils.getEntitiesInRay(LivingEntity.class, (LivingEntity)player, range, ClipContext.Fluid.NONE, ClipContext.Block.OUTLINE, 0.0625f, e -> e != player, pierce, null);
        piercingMoonlight.setDataMaxLength(rayData.distance() - 2);
        int nightMult = player.level().isNight() ? 2 : 1;
        rayData.entityList().forEach(e -> NativeDamage.hurt(e, (double)e.distanceTo((Entity)player) > 9.5 && (double)e.distanceTo((Entity)player) < 10.5 ? damage * 3.0f * (float)nightMult : damage * (float)nightMult, this, player, player));
        return true;
    }
}

