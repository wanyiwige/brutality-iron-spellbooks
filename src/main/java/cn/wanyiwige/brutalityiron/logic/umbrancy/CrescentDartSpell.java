// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.umbrancy;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.entity.umbrancy.CrescentDart;
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

public class CrescentDartSpell
extends PortedLogic {
    public CrescentDartSpell() {
        super(IBrutalitySpell.MagicSchool.UMBRANCY, List.of(IBrutalitySpell.SpellCategory.CONTINUOUS, IBrutalitySpell.SpellCategory.TARGET, IBrutalitySpell.SpellCategory.AOE), "crescent_dart", 12, 1.0f, 20, 7, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.PIERCE, 1.0f, 0.5f, Float.valueOf(1.0f), Float.valueOf(5.0f))));
    }

    @Override
    public float getDamageLevelScaling() {
        return 0.25f;
    }

    @Override
    public boolean onCastTick(Player player, ItemStack stack, int spellLevel) {
        Level level = player.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            CrescentDart crescentDart = new CrescentDart((EntityType<? extends AbstractArrow>)((EntityType)PortEntities.CRESCENT_DART_ENTITY.get()), (Level)serverLevel);
            crescentDart.setOwner((Entity)player);
            crescentDart.setPos(player.getEyePosition().add(player.getLookAngle().scale(1.5)));
            crescentDart.setSpellLevel(spellLevel);
            crescentDart.shootFromRotation((Entity)player, player.getXRot(), player.getYRot(), 0.0f, 1.0f, 0.0f);
            serverLevel.addFreshEntity((Entity)crescentDart);
        }
        return true;
    }
}

