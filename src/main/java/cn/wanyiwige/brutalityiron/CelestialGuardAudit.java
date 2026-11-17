package cn.wanyiwige.brutalityiron;

import cn.wanyiwige.brutalityiron.skills.SkillVisualEntity;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.spells.*;
import java.util.UUID;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Enabled only in the disposable guard test server. */
@Mod.EventBusSubscriber(modid=BrutalityIron.ID)
public final class CelestialGuardAudit {
    private static void check(boolean ok,String message){if(!ok)throw new IllegalStateException("Celestial guard audit: "+message);}
    private static net.minecraft.server.level.ServerLevel level;
    private static net.minecraftforge.common.util.FakePlayer p;
    private static net.minecraft.world.entity.monster.Zombie zombie;
    @SubscribeEvent public static void start(ServerStartedEvent event){
        if(!Boolean.getBoolean("brutality_iron.guard_audit"))return;
        io.redspace.ironsspellbooks.api.config.SpellConfigManager.onDatapackSync(new net.minecraftforge.event.OnDatapackSyncEvent(event.getServer().getPlayerList(),null));
        level=event.getServer().overworld();p=FakePlayerFactory.get(level,new GameProfile(UUID.fromString("c12b072f-4a95-4d21-8c21-965959696721"),"[GuardAudit]"));
        level.addNewPlayer(p);var sp=level.getSharedSpawnPos();p.setPos(sp.getX()+.5,sp.getY()+5,sp.getZ()+.5);zombie=EntityType.ZOMBIE.create(level);check(zombie!=null,"zombie");zombie.setPos(p.getX()+1,p.getY(),p.getZ());level.addFreshEntity(zombie);
        var spell=Content.CELESTIAL_GUARD.get();check(spell.getMaxLevel()==5,"max level");
        for(int i=1;i<=5;i++){check(spell.getRarity(i)==SpellRarity.LEGENDARY,"legendary "+i);check(spell.getManaCost(i)==60+5*(i-1),"mana "+i);}
        CelestialGuard.cast(p,3);
    }
    @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ServerTickEvent event){
        if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END||level==null)return;
        var rings=level.getEntitiesOfClass(SkillVisualEntity.class,p.getBoundingBox().inflate(10),e->e.skill.equals("celestial_guard"));check(rings.size()==3,"three rings actual="+rings.size()+" state="+java.util.Arrays.toString(CelestialGuard.auditRings(p))+" player="+p.position()+" alive="+p.isAlive()+" removed="+p.isRemoved());
        var low=new LivingHurtEvent(p,level.damageSources().mobAttack(zombie),6);CelestialGuard.hurt(low);check(!low.isCanceled(),"threshold boundary");
        for(String name:new String[]{"outer","middle","inner"}){
            var hit=new LivingHurtEvent(p,level.damageSources().mobAttack(zombie),7);CelestialGuard.hurt(hit);check(hit.isCanceled(),"hit blocked "+name);
            check(rings.stream().anyMatch(v->v.role.equals("guard_break_"+name)),"break "+name);
        }
        var fourth=new LivingHurtEvent(p,level.damageSources().mobAttack(zombie),7);CelestialGuard.hurt(fourth);check(!fourth.isCanceled(),"no fourth block");
        LogUtils.getLogger().info("CELESTIAL_GUARD_AUDIT_PASS levels=5 rings=3 threshold=6 order=outer,middle,inner");
        zombie.discard();p.discard();level=null;
    }
}
