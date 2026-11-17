// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.cosmic;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.entity.cosmic.MeteorShowerEntity;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import cn.wanyiwige.brutalityiron.PortEntities;
import net.goo.brutality.registry.BrutalityModEntities;
import net.goo.brutality.util.ModUtils;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MeteorShowerSpell
extends PortedLogic {
    public MeteorShowerSpell() {
        super(IBrutalitySpell.MagicSchool.COSMIC, List.of(IBrutalitySpell.SpellCategory.CONTINUOUS, IBrutalitySpell.SpellCategory.AOE, IBrutalitySpell.SpellCategory.TARGET), "meteor_shower", 10, 7.0f, 100, 10, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.RANGE, 15.0f, 5.0f, Float.valueOf(0.0f), Float.valueOf(100.0f)), new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.SIZE, 3.0f, 0.0f, Float.valueOf(3.0f), Float.valueOf(3.0f))));
    }

    @Override
    public float getDamageLevelScaling() {
        return 0.0f;
    }

    @Override
    public int getCooldownLevelScaling() {
        return 2;
    }

    @Override
    public int getCastTimeLevelScaling() {
        return -1;
    }

    @Override
    public boolean onCastTick(Player player, ItemStack stack, int spellLevel) {
        Level level = player.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            BlockPos blockPos = ModUtils.getBlockLookingAt(player, false, this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.RANGE)));
            if (blockPos == null) {
                return false;
            }
            Vec3 targetPos = blockPos.getCenter();
            Vec3 spawnPos = targetPos.add((double)Mth.nextFloat((RandomSource)serverLevel.random, (float)-2.0f, (float)2.0f), 20.0, (double)Mth.nextFloat((RandomSource)serverLevel.random, (float)-2.0f, (float)2.0f));
            MeteorShowerEntity spellEntity = new MeteorShowerEntity((EntityType<? extends AbstractArrow>)((EntityType)PortEntities.METEOR_SHOWER_ENTITY.get()), (Level)serverLevel);
            spellEntity.setSpellLevel(1);
            spellEntity.setPos(spawnPos);
            spellEntity.setOwner((Entity)player);
            spellEntity.addDeltaMovement(new Vec3((double)Mth.nextFloat((RandomSource)serverLevel.random, (float)-0.25f, (float)0.25f), -0.5, (double)Mth.nextFloat((RandomSource)serverLevel.random, (float)-0.25f, (float)0.25f)));
            serverLevel.addFreshEntity((Entity)spellEntity);
        }
        return true;
    }
}

