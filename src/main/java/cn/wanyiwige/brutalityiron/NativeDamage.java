package cn.wanyiwige.brutalityiron;

import io.redspace.ironsspellbooks.damage.DamageSources;
import net.goo.brutality.magic.BrutalitySpell;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class NativeDamage {
    public static boolean hurt(Entity target, float amount, BrutalitySpell logic, Entity owner, Entity projectile) {
        if (target == owner || owner != null && owner.isAlliedTo(target)) return false;
        if (target instanceof LivingEntity living && (living.isSpectator() || !living.isAlive())) return false;
        return DamageSources.applyDamage(target, amount,
                Content.spell(logic.getSpellName()).getDamageSource(projectile, owner));
    }

    private NativeDamage() {}
}
