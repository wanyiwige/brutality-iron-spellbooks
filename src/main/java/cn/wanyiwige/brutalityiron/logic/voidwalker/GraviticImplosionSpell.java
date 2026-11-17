// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.voidwalker;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.entity.voidwalker.GraviticImplosionEntity;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import cn.wanyiwige.brutalityiron.PortEntities;
import net.goo.brutality.registry.BrutalityModEntities;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

public class GraviticImplosionSpell
extends PortedLogic {
    public GraviticImplosionSpell() {
        super(IBrutalitySpell.MagicSchool.VOIDWALKER, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.AOE), "gravitic_implosion", 30, 5.0f, 80, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.SIZE, 3.0f, 1.0f, Float.valueOf(3.0f), Float.valueOf(50.0f))));
    }

    @Override
    public float getDamageLevelScaling() {
        return 1.0f;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        GraviticImplosionEntity graviticImplosionEntity = new GraviticImplosionEntity((EntityType<? extends AbstractArrow>)((EntityType)PortEntities.GRAVITIC_IMPLOSION_ENTITY.get()), player.level());
        graviticImplosionEntity.setSpellLevel(spellLevel);
        graviticImplosionEntity.setPos(player.getEyePosition());
        graviticImplosionEntity.setOwner((Entity)player);
        graviticImplosionEntity.shootFromRotation((Entity)player, player.getXRot(), player.getYRot(), 0.0f, Math.min(0.5f + (float)spellLevel * 0.1f, 1.0f), 0.0f);
        player.level().addFreshEntity((Entity)graviticImplosionEntity);
        return true;
    }
}

