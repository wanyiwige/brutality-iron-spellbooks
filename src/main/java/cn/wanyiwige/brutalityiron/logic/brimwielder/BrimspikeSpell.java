// Adapted from Brutality 0.8.5; see THIRD_PARTY_NOTICES.md.
package cn.wanyiwige.brutalityiron.logic.brimwielder;

import java.util.Iterator;
import java.util.List;
import cn.wanyiwige.brutalityiron.NativeDamage;
import cn.wanyiwige.brutalityiron.entity.brimwielder.BrimspikeEntity;
import cn.wanyiwige.brutalityiron.PortedLogic;
import net.goo.brutality.magic.IBrutalitySpell;
import cn.wanyiwige.brutalityiron.PortEntities;
import net.goo.brutality.registry.BrutalityModEntities;
import net.goo.brutality.util.helpers.BrutalityTooltipHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class BrimspikeSpell
extends PortedLogic {
    public BrimspikeSpell() {
        super(IBrutalitySpell.MagicSchool.BRIMWIELDER, List.of(IBrutalitySpell.SpellCategory.CONTINUOUS, IBrutalitySpell.SpellCategory.AOE), "brimspike", 10, 1.0f, 100, 5, 1, List.of(new BrutalityTooltipHelper.SpellStatComponent(BrutalityTooltipHelper.SpellStatComponents.PIERCE, 1.0f, 0.0f, Float.valueOf(1.0f), Float.valueOf(1.0f))));
    }

    @Override
    public float getDamageLevelScaling() {
        return 0.5f;
    }

    @Override
    public float getManaCostLevelScaling() {
        return 0.25f;
    }

    @Override
    public int getCooldownLevelScaling() {
        return 5;
    }

    @Override
    public boolean onCastTick(Player player, ItemStack stack, int spellLevel) {
        Entity entity = this.getSpellEntity(player.level(), player);
        if (entity instanceof BrimspikeEntity) {
            BrimspikeEntity brimspikeEntity = (BrimspikeEntity)entity;
            Vec3 targetPos = player.getEyePosition().add(player.getLookAngle().scale(5.0));
            Vec3 currentPos = brimspikeEntity.position();
            Vector3f newPos = currentPos.lerp(targetPos, 0.2).toVector3f();
            Vec3 movement = new Vec3(newPos).subtract(currentPos);
            brimspikeEntity.setDeltaMovement(movement);
            String damage = String.format("%.2f", brimspikeEntity.getBaseDamage());
            player.displayClientMessage((Component)Component.literal((String)damage), true);
        }
        return true;
    }

    @Override
    public boolean onStartCast(Player player, ItemStack stack, int spellLevel) {
        if (this.getSpellEntity(player.level(), player) == null) {
            BrimspikeEntity brimspikeEntity = new BrimspikeEntity((EntityType<? extends AbstractArrow>)((EntityType)PortEntities.BRIMSPIKE_ENTITY.get()), player.level());
            brimspikeEntity.setOwner((Entity)player);
            brimspikeEntity.setPos(player.getLookAngle().scale(3.0).add(player.getX(), player.getEyeY(), player.getZ()));
            brimspikeEntity.setYRot((float)(1.5707963267948966 - (double)player.getViewYRot(1.0f)));
            brimspikeEntity.setXRot((float)(1.5707963267948966 - (double)player.getViewXRot(1.0f)));
            brimspikeEntity.setSpellLevel(spellLevel);
            brimspikeEntity.setNoGravity(true);
            player.level().addFreshEntity((Entity)brimspikeEntity);
            return true;
        }
        player.displayClientMessage((Component)Component.translatable((String)"spell.brutality.brimspike.fail"), true);
        return false;
    }

    @Override
    public void onEndCast(Player player, ItemStack stack, int spellLevel) {
        Entity entity = this.getSpellEntity(player.level(), player);
        if (entity instanceof BrimspikeEntity) {
            BrimspikeEntity brimspikeEntity = (BrimspikeEntity)entity;
            brimspikeEntity.shootFromRotation((Entity)player, player.getXRot(), player.getYRot(), 0.0f, Math.min(3.5f, (float)stack.getOrCreateTag().getInt("port_cast_ticks") / 40.0f), 0.0f);
        }
    }

    private Entity getSpellEntity(Level level, Player player) {
        Iterator iterator = level.getEntitiesOfClass(BrimspikeEntity.class, player.getBoundingBox().inflate(25.0), e -> e.getOwner() == player).iterator();
        if (iterator.hasNext()) {
            BrimspikeEntity entity = (BrimspikeEntity)iterator.next();
            return entity;
        }
        return null;
    }
}

