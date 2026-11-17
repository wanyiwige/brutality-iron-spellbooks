// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.brimwielder;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.entity.brimwielder.ChthonicCapsuleEntity;
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

public class ChthonicCapsuleSpell
extends PortedLogic {
    public ChthonicCapsuleSpell() {
        super(IBrutalitySpell.MagicSchool.BRIMWIELDER, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.AOE, IBrutalitySpell.SpellCategory.UTILITY), "chthonic_capsule", 30, 5.0f, 80, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.SIZE, 3.0f, 1.0f, Float.valueOf(3.0f), Float.valueOf(50.0f))));
    }

    @Override
    public float getDamageLevelScaling() {
        return 1.0f;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        Level level = player.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            ChthonicCapsuleEntity chthonicCapsuleSpell = new ChthonicCapsuleEntity((EntityType<? extends AbstractArrow>)((EntityType)PortEntities.CHTHONIC_CAPSULE_ENTITY.get()), (Level)serverLevel);
            chthonicCapsuleSpell.setSpellLevel(spellLevel);
            chthonicCapsuleSpell.setPos(player.getEyePosition());
            chthonicCapsuleSpell.setOwner((Entity)player);
            chthonicCapsuleSpell.shootFromRotation((Entity)player, player.getXRot(), player.getYRot(), 0.0f, Math.min(0.5f + (float)spellLevel * 0.1f, 1.0f), 0.0f);
            serverLevel.addFreshEntity((Entity)chthonicCapsuleSpell);
        }
        return true;
    }
}

