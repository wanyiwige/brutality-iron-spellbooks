// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.brimwielder;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.entity.brimwielder.DestructionEntity;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import cn.wanyiwige.brutalityiron.PortEntities;
import net.goo.brutality.registry.BrutalityModEntities;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DestructionSpell
extends PortedLogic {
    public DestructionSpell() {
        super(IBrutalitySpell.MagicSchool.BRIMWIELDER, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.AOE), "destruction", 50, 10.0f, 140, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.SIZE, 3.0f, 0.0f, Float.valueOf(3.0f), Float.valueOf(3.0f))));
    }

    @Override
    public float getDamageLevelScaling() {
        return 2.0f;
    }

    @Override
    public float getManaCostLevelScaling() {
        return 10.0f;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        DestructionEntity destructionEntity = new DestructionEntity((EntityType)PortEntities.DESTRUCTION_ENTITY.get(), player.level());
        destructionEntity.setSpellLevel(spellLevel);
        destructionEntity.setPos(player.getX(), player.getY() + 1.0, player.getZ());
        destructionEntity.setYRot(player.getViewYRot(1.0f));
        player.level().addFreshEntity((Entity)destructionEntity);
        for (var target : player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,
                player.getBoundingBox().inflate(3), e -> e != player && !player.isAlliedTo(e))) {
            if (target.distanceToSqr(player) <= 9) NativeDamage.hurt(target, getFinalDamage(player, spellLevel), this, player, destructionEntity);
        }
        return true;
    }
}

