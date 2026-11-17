package cn.wanyiwige.brutalityiron;

import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BrutalityIron.ID)
public final class ProjectileDamageEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(LivingAttackEvent event) {
        if (event.getSource() instanceof SpellDamageSource) return;
        if (event.getSource().getDirectEntity() instanceof Projectile projectile
                && projectile instanceof NativeSpellEntity nativeEntity) {
            event.setCanceled(true);
            NativeDamage.hurt(event.getEntity(), event.getAmount(), nativeEntity.getSpell(), projectile.getOwner(), projectile);
        }
    }
    private ProjectileDamageEvents() {}
}
