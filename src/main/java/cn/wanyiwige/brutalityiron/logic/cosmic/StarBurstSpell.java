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
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class StarBurstSpell
extends PortedLogic {
    public StarBurstSpell() {
        super(IBrutalitySpell.MagicSchool.COSMIC, List.of(IBrutalitySpell.SpellCategory.INSTANT, IBrutalitySpell.SpellCategory.AOE), "star_burst", 50, 3.0f, 20, 0, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.QUANTITY, 5.0f, 3.0f, Float.valueOf(5.0f), null)));
    }

    @Override
    public float getDamageLevelScaling() {
        return 0.5f;
    }

    @Override
    public float getManaCostLevelScaling() {
        return 15.0f;
    }

    @Override
    public int getCooldownLevelScaling() {
        return 2;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        Level level = player.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            RandomSource random = player.getRandom();
            int i = 0;
            while ((float)i < this.getFinalStat(spellLevel, this.getStat(BrutalityTooltipHelper.SpellStatComponents.QUANTITY))) {
                StarStreamEntity spellEntity = new StarStreamEntity((EntityType<? extends AbstractArrow>)((EntityType)PortEntities.STAR_STREAM_ENTITY.get()), (Level)serverLevel);
                spellEntity.setSpellLevel(spellLevel);
                spellEntity.setOwner((Entity)player);
                Vec3 randomPos = player.getEyePosition().add(random.nextDouble() * 0.2, random.nextDouble() * 0.2, random.nextDouble() * 0.2);
                spellEntity.setPos(randomPos);
                spellEntity.shootFromRotation((Entity)player, player.getXRot(), player.getYRot(), 0.0f, 2.0f, 15.0f);
                serverLevel.addFreshEntity((Entity)spellEntity);
                ++i;
            }
            serverLevel.playSound(null, player.getX(), player.getY(0.5), player.getZ(), (SoundEvent)BrutalityModSounds.BASS_BOOM.get(), SoundSource.AMBIENT, (float)spellLevel * 0.5f, Mth.nextFloat((RandomSource)random, (float)0.7f, (float)1.2f));
        }
        return true;
    }
}

