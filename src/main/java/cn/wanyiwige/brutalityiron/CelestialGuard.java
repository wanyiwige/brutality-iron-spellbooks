package cn.wanyiwige.brutalityiron;

import cn.wanyiwige.brutalityiron.skills.*;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;

@Mod.EventBusSubscriber(modid=BrutalityIron.ID)
public final class CelestialGuard {
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,BrutalityIron.ID);
    private static final RegistryObject<SoundEvent> HIT=SOUNDS.register("celestial_guard_hit",()->SoundEvent.createVariableRangeEvent(BrutalityIron.id("celestial_guard_hit")));
    private static final Map<UUID,Guard> ACTIVE=new HashMap<>();
    private static final class Guard {
        final Player owner; final int rank; final long end; final SkillVisualEntity[] rings=new SkillVisualEntity[3]; int remaining=3;
        Guard(Player owner,int rank){this.owner=owner;this.rank=rank;this.end=owner.level().getGameTime()+duration(rank);}
    }
    public static void register(IEventBus bus){SOUNDS.register(bus);}
    public static int duration(int rank){return 400+100*(rank-1);}
    public static double radius(int rank){return 3+.5*(rank-1);}
    public static double strength(int rank){return .6+.1*(rank-1);}
    public static void cast(Player player,int rank){
        clear(player); Guard state=new Guard(player,Math.max(1,Math.min(5,rank)));ACTIVE.put(player.getUUID(),state);
        String[] names={"inner","middle","outer"};
        for(int i=0;i<3;i++){
            var v=new SkillVisualEntity(SkillContent.VISUAL.get(),player.level());v.owner(player);v.skill="celestial_guard";
            v.rank=state.rank;v.role="guard_active";v.model("celestial_guard_"+names[i]);v.life=duration(state.rank);
            v.setPos(player.position());v.syncVisual();state.rings[i]=v;player.level().addFreshEntity(v);
        }
    }
    static SkillVisualEntity[] auditRings(Player p){var g=ACTIVE.get(p.getUUID());return g==null?new SkillVisualEntity[0]:g.rings;}
    public static void tickVisual(SkillVisualEntity v){v.setPos(v.owner().position());}
    private static void clear(Player player){Guard old=ACTIVE.remove(player.getUUID());if(old!=null)for(var v:old.rings)if(v!=null)v.discard();}
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=false)
    public static void hurt(LivingHurtEvent event){
        if(!(event.getEntity() instanceof Player player)||player.level().isClientSide||event.getAmount()<=6||event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        if(!(event.getSource().getEntity() instanceof LivingEntity attacker)||!SkillRuntime.enemy(player,attacker))return;
        Guard g=ACTIVE.get(player.getUUID());if(g==null)return;
        if(player.level().getGameTime()>=g.end){clear(player);return;}
        event.setCanceled(true);int index=--g.remaining;var v=g.rings[index];
        if(v!=null&&!v.isRemoved()){v.role="guard_break_"+new String[]{"inner","middle","outer"}[index];v.life=v.age+24;v.syncVisual();}
        player.level().playSound(null,player.blockPosition(),HIT.get(),SoundSource.PLAYERS,1f,1f);
        for(var target:SkillRuntime.targets(player,player.position(),radius(g.rank)))if(target instanceof Enemy){
            Vec3 dir=target.position().subtract(player.position());if(dir.horizontalDistanceSqr()<1e-6)dir=new Vec3(1,0,0);
            double resist=Math.max(0,1-target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            Vec3 n=new Vec3(dir.x,0,dir.z).normalize();target.push(n.x*strength(g.rank)*resist,.14*resist,n.z*strength(g.rank)*resist);target.hurtMarked=true;
        }
        if(g.remaining==0)ACTIVE.remove(player.getUUID());
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event){if(event.phase!=TickEvent.Phase.END)return;for(var g:new ArrayList<>(ACTIVE.values()))if(g.owner.isRemoved()||!g.owner.isAlive()||g.owner.level().getGameTime()>=g.end)clear(g.owner);}
    @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity() instanceof Player p)clear(p);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){clear(e.getEntity());}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){clear(e.getEntity());}
    @SubscribeEvent public static void stop(ServerStoppedEvent e){ACTIVE.clear();}
    private CelestialGuard(){}
}
