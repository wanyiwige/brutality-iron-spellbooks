// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.cosmic;

import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.entity.cosmic.StarStreamEntity;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import cn.wanyiwige.brutalityiron.PortEntities;
import net.goo.brutality.registry.BrutalityModEntities;
import net.goo.brutality.registry.BrutalityModSounds;
import net.goo.brutality.util.ModUtils;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class StarStreamSpell
extends PortedLogic {
    public StarStreamSpell() {
        super(IBrutalitySpell.MagicSchool.COSMIC, List.of(IBrutalitySpell.SpellCategory.CONTINUOUS, IBrutalitySpell.SpellCategory.TARGET), "star_stream", 8, 3.0f, 20, 10, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.SPEED, 1.0f, 0.25f, Float.valueOf(1.0f), Float.valueOf(3.0f))));
    }

    @Override
    public float getDamageLevelScaling() {
        return 0.25f;
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
            Vec3 target;
            ServerLevel serverLevel = (ServerLevel)level;
            ModUtils.RayData<Entity> rayData = ModUtils.getEntitiesInRay(Entity.class, (LivingEntity)player, 20.0f, ClipContext.Fluid.NONE, ClipContext.Block.OUTLINE, 0.5f, e -> e != player && !(e instanceof StarStreamEntity), 1, null);
            if (!rayData.entityList().isEmpty()) {
                Entity targetEntity = rayData.entityList().get(0);
                target = targetEntity.position().add(0.0, (double)(targetEntity.getBbHeight() / 2.0f), 0.0);
            } else {
                BlockPos blockPos = ModUtils.getBlockLookingAt(player, false, 20.0f);
                target = blockPos == null ? player.getEyePosition(1.0f).add(player.getLookAngle().scale(20.0)) : Vec3.atCenterOf((Vec3i)blockPos);
            }
            StarStreamEntity spellEntity = new StarStreamEntity((EntityType<? extends AbstractArrow>)((EntityType)PortEntities.STAR_STREAM_ENTITY.get()), (Level)serverLevel);
            spellEntity.setSpellLevel(spellLevel);
            Vec3 randomPos = player.position().add(player.getRandom().nextGaussian() * 2.0, player.getRandom().nextDouble() * 2.0 + 2.0, player.getRandom().nextGaussian() * 2.0);
            spellEntity.setPos(randomPos);
            spellEntity.setOwner((Entity)player);
            float inaccuracy = (float)(5 - spellLevel) * 0.25f;
            target.add((double)Mth.nextFloat((RandomSource)player.getRandom(), (float)(-inaccuracy), (float)inaccuracy), (double)Mth.nextFloat((RandomSource)player.getRandom(), (float)(-inaccuracy), (float)inaccuracy), (double)Mth.nextFloat((RandomSource)player.getRandom(), (float)(-inaccuracy), (float)inaccuracy));
            Vec3 direction = target.subtract(randomPos).normalize();
            spellEntity.shoot(direction.x, direction.y, direction.z, this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.SPEED)), Math.max(5 - spellLevel, 0));
            serverLevel.addFreshEntity((Entity)spellEntity);
            serverLevel.playSound(null, player.getX(), player.getY(0.5), player.getZ(), (SoundEvent)BrutalityModSounds.BASS_BOP.get(), SoundSource.AMBIENT, 1.5f - (float)Math.min(spellLevel, 10) / 10.0f, Mth.nextFloat((RandomSource)player.getRandom(), (float)0.7f, (float)1.2f));
        }
        return true;
    }
}

