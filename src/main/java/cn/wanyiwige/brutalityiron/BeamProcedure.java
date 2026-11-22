package cn.wanyiwige.brutalityiron;

import net.mcreator.terramity.init.TerramityModMobEffects;
import net.mcreator.terramity.init.TerramityModParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;

/** Original Holy Beam visual, with a native, persisted caster and damage school. */
public final class BeamProcedure {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity beam) {
        if (!(world instanceof ServerLevel level)) return;
        int timer = beam.getPersistentData().getInt("port_timer") + 1;
        beam.getPersistentData().putInt("port_timer", timer);
        if (timer >= 110) { beam.discard(); return; }
        if (timer <= 10 || timer >= 95 || (timer-11)%10!=0) return;
        var tag = beam.getPersistentData();
        Entity owner = tag.hasUUID("port_owner") ? level.getEntity(tag.getUUID("port_owner")) : null;
        if (owner == null) { beam.discard(); return; }
        int rank = Math.max(1, tag.getInt("port_rank"));
        NativeSpell spell = Content.spell("divine_retribution");
        float damage = spell.logic.getFinalDamage(owner, rank);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(x-3.5, y+.5, z-3.5, x+3.5, y+7.5, z+3.5))) {
            if (target == owner || target == beam || owner.isAlliedTo(target)) continue;
            if (NativeDamage.hurt(target, damage, spell.logic, owner, beam))
                target.addEffect(new MobEffectInstance(TerramityModMobEffects.VULNERABLE.get(), 100));
        }
        level.sendParticles(TerramityModParticleTypes.HOLY_GLINT.get(), x, y, z, 3, 3, .5, 3, .05);
    }
    private BeamProcedure() {}
}
