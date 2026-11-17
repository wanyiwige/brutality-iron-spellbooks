package cn.wanyiwige.brutalityiron.skills;

import java.util.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;

/** Regression coverage for the reported spawn/direction and recast failures. */
@Mod.EventBusSubscriber(modid="brutality_iron")
public final class SkillRevisionAudit {
    private static String current="";private static UUID owner;
    private static final List<SkillVisualEntity> spawned=new ArrayList<>();
    private static final Map<UUID,Vec3> starts=new HashMap<>();
    private static Vec3 bombPrevious;private static int bombSteps;private static double bombLargestStep;
    public static void begin(String id,Player player){current=id;owner=player.getUUID();spawned.clear();starts.clear();bombPrevious=null;bombSteps=0;bombLargestStep=0;}
    public static void observe(SkillVisualEntity entity){
        if(!current.equals("spirit_bomb")||!entity.skill.equals(current)||!entity.role.equals("projectile"))return;
        Vec3 previous=bombPrevious==null?starts.get(entity.getUUID()):bombPrevious;
        if(previous!=null){double distance=entity.position().distanceTo(previous);if(distance>1e-6){bombSteps++;bombLargestStep=Math.max(bombLargestStep,distance);}}
        bombPrevious=entity.position();
    }
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException("Revision: "+message);}
    @SubscribeEvent public static void joined(EntityJoinLevelEvent event){
        if(!Boolean.getBoolean("brutality_iron.audit")||event.getLevel().isClientSide)return;
        if(event.getEntity() instanceof SkillVisualEntity visual&&visual.skill.equals(current)&&Objects.equals(owner,visual.ownerId)){
            if(visual.role.equals("projectile")||visual.role.equals("formation")){
            require(visual.getDeltaMovement().lengthSqr()>0,"projectile velocity set before spawn "+current);
            require(visual.getLookAngle().dot(visual.getDeltaMovement().normalize())>.999,"initial orientation matches velocity "+current);
            }
            var buf=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            visual.writeSpawnData(buf);var copy=new SkillVisualEntity(SkillContent.VISUAL.get(),visual.level());copy.readSpawnData(buf);buf.release();
            require(copy.skill.equals(visual.skill)&&copy.role.equals(visual.role)&&copy.life==visual.life&&Math.abs(copy.radius-visual.radius)<1e-4,"spawn payload "+current);
            spawned.add(visual);
            starts.put(visual.getUUID(),visual.position());
            if(current.equals("stygian_souls")){
                require(visual.role.equals("formation")&&visual.age==0,"all souls spawn before launch");
                require(visual.position().subtract(visual.owner().getEyePosition()).dot(visual.owner().getLookAngle())<-.8,"soul starts behind player");
            }
        }
    }
    public static void finish(String id,Player player){
        int expected=switch(id){case "seeking_blades","stellar_marks"->4;case "stygian_souls"->6;case "devil_armament"->5;default->-1;};
        if(expected>=0)require(spawned.size()==expected,"projectile count "+id+"="+spawned.size());
        if(id.equals("starlight_flurry")){
            require(spawned.size()==5||spawned.size()==6,"original 5-6 star volley");
            require(spawned.stream().map(SkillVisualEntity::model).distinct().count()>=5,"multiple starlight colors");
            require(spawned.stream().map(e->e.position().x).distinct().count()>=3,"starlight distributed around view");
        }
        if(id.equals("stygian_souls")){
            require(spawned.stream().anyMatch(e->e.role.equals("formation")),"later souls still waiting");
            require(spawned.stream().anyMatch(e->e.role.equals("projectile")||e.isRemoved()),"earlier souls launched");
            for(var e:spawned)if(e.role.equals("formation"))require(e.position().distanceToSqr(starts.get(e.getUUID()))<1e-9,"waiting soul stays in formation");
        }
        if(id.startsWith("triple_")){
            var warnings=spawned.stream().filter(e->e.role.equals("pillar_warning")).toList();
            var beams=spawned.stream().filter(e->e.role.equals("descending_beam")).toList();
            require(warnings.size()==3&&beams.size()==3,"three ground circles and original 3D columns");
            Vec3 forward=SkillRevisionRuntime.groundForward(player);
            for(int i=0;i<3;i++){
                var e=warnings.get(i);double distance=e.position().subtract(player.position()).dot(forward);
                require(Math.abs(distance-(3+i*4.5))<.01,"ground circles near to far");
                require(Math.abs(e.getY()-100.12)<.01,"ground circle on floor");
                require(beams.get(i).model().equals(id.equals("triple_judgment")?"holy_beam":"profane_beam"),"original beam model alias");
            }
        }
        if(id.equals("spirit_bomb")){
            require(spawned.size()==1,"charge and flight reuse one entity");
            var ball=spawned.get(0);require(ball.role.equals("projectile"),"charge transitions to flight");
            require(bombSteps>=4&&bombLargestStep<=.701,"spirit bomb travels continuously until nearby collision: steps="+bombSteps+" max-step="+bombLargestStep);
            require(ball.model().equals("spirit_bomb_flight"),"flight avoids zero-size animation restart");
        }
        var spell=SkillContent.spell(id);
        if(spell.cancellableSummon()||spell.cancellableEffect()){
            var data=MagicData.getPlayerMagicData(player);var recasts=data.getPlayerRecasts();
            require(recasts.hasRecastForSpell(spell),"native summon recast exists");
            // The normal casting manager consumes the first cast after onCast.
            recasts.decrementRecastCount(spell);
            spell.onCast(player.level(),1,player,CastSource.SPELLBOOK,data);
            recasts.decrementRecastCount(spell);
            require(!recasts.hasRecastForSpell(spell),"native second cast ends recast");
            require(player.level().getEntitiesOfClass(SkillMinion.class,player.getBoundingBox().inflate(128),m->player.getUUID().equals(m.ownerId)&&m.skill.equals(id)).isEmpty(),"second cast dismisses summons");
            require(player.level().getEntitiesOfClass(SkillVisualEntity.class,player.getBoundingBox().inflate(128),e->player.getUUID().equals(e.ownerId)&&e.skill.equals(id)).isEmpty(),"second cast dismisses effects");
        }
        current="";spawned.clear();
    }
    public static void geometry(Player player){
        int tested=0;
        for(int yaw=0;yaw<360;yaw+=90)for(int pitch:new int[]{-90,-60,0,60,90}){
            player.setYRot(yaw);player.setXRot(pitch);Vec3 look=player.getLookAngle();var f=SkillGeometry.frame(look);
            require(Math.abs(f.right().dot(f.forward()))<1e-6&&Math.abs(f.up().dot(f.forward()))<1e-6,"orthogonal aim basis");
            require(Math.abs(f.right().length()-1)<1e-6&&Math.abs(f.up().length()-1)<1e-6,"vertical aim basis nonzero");
            for(int spread:new int[]{-18,-6,6,18}){
                Vec3 motion=SkillGeometry.spread(look,spread);require(motion.dot(look)>.94,"scatter stays in forward cone");tested++;
            }
        }
        Vec3 bent=SkillGeometry.turn(new Vec3(0,0,1),new Vec3(1,0,0),Math.toRadians(7));
        require(Math.abs(Math.toDegrees(Math.acos(bent.z))-7)<1e-5,"homing angular limit");
        var tracker=new SkillMotionTrack();tracker.target(new Vec3(3,6,9),-179,30,3);
        var step=tracker.step(Vec3.ZERO,179,0);
        require(step.position().distanceTo(new Vec3(1,2,3))<1e-6,"position update interpolates instead of snapping");
        require(Math.abs(step.yaw()-179)<1,"rotation crosses 180 by shortest arc");
        step=tracker.step(step.position(),step.yaw(),step.pitch());step=tracker.step(step.position(),step.yaw(),step.pitch());
        require(step.position().distanceTo(new Vec3(3,6,9))<1e-6&&!tracker.active(),"interpolation reaches authoritative endpoint");
        player.setYRot(0);player.setXRot(0);
        var target=net.minecraft.world.entity.EntityType.COW.create(player.level());target.setPos(100,100,104);
        var probe=SkillRevisionRuntime.make(SkillContent.spell("seeking_blades"),player,1,"projectile","fx:exoblade_beam",player.getEyePosition(),20,1,new Vec3(0,0,1));
        var wall=new net.minecraft.core.BlockPos(100,101,102);var old=player.level().getBlockState(wall);
        player.level().setBlockAndUpdate(wall,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        require(!SkillRuntime.visible(probe,target),"wall blocks homing acquisition");player.level().setBlockAndUpdate(wall,old);
        target.setPos(100,103,102);require(!SkillRevisionRuntime.groundReach(player,target),"ground slam excludes other floors");
        com.mojang.logging.LogUtils.getLogger().info("BRUTALITY_IRON_REVISION_AUDIT_PASS direction-cases={} spawn-order=pass staged-volley=pass multi-color=pass summon-recasts=pass effect-recasts=pass ground-columns=pass continuous-bomb=pass interpolation=pass",tested);
    }
    private SkillRevisionAudit(){}
}
