package cn.wanyiwige.brutalityiron.skills;

import java.util.*;
import cn.wanyiwige.brutalityiron.SpellSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.effect.*;
import net.minecraft.core.particles.ParticleTypes;

/** Implements the user's retained-spell revisions; the legacy 30 are separate. */
public final class SkillRevisionRuntime {
    public static SkillVisualEntity make(SkillSpell spell,Player owner,int rank,String role,String model,Vec3 position,int life,double radius,Vec3 direction){
        var e=new SkillVisualEntity(SkillContent.VISUAL.get(),owner.level());
        e.owner(owner);e.skill=spell.definition.id();e.rank=rank;e.role=role;e.model(model);
        e.life=life;e.radius=radius;e.damage=spell.damage(rank,owner);e.anchor=direction;e.setPos(position);e.orient(direction);
        return e;
    }
    public static SkillVisualEntity spawn(SkillVisualEntity entity){entity.syncVisual();entity.level().addFreshEntity(entity);return entity;}
    public static SkillVisualEntity display(SkillSpell spell,Player owner,int rank,String role,String model,Vec3 position,int life,double radius,Vec3 direction){return spawn(make(spell,owner,rank,role,model,position,life,radius,direction));}
    public static SkillVisualEntity projectile(SkillSpell spell,Player owner,int rank,String model,Vec3 position,Vec3 motion,boolean homing,boolean piercing,UUID target,float multiplier,int life){
        var e=make(spell,owner,rank,"projectile",model,position,life,1,motion);
        e.setDeltaMovement(motion);e.homing=homing;e.piercing=piercing;e.targetId=target;e.damage*=multiplier;
        return spawn(e);
    }
    private static Vec3 endpoint(Player p){var target=SkillRuntime.aim(p,32);return target==null?p.getEyePosition().add(p.getLookAngle().scale(32)):target.getBoundingBox().getCenter();}
    private static void shot(SkillSpell spell,Player p,int rank,String model,float angle,boolean homing,boolean pierce){
        projectile(spell,p,rank,model,p.getEyePosition().add(p.getLookAngle().scale(.7)),SkillGeometry.spread(p.getLookAngle(),angle).scale(1+rank*.03),homing,pierce,null,1,Math.min(100,spell.duration(rank)));
    }
    public static boolean cast(SkillSpell spell,Player p,int rank){
        var frame=SkillGeometry.frame(p.getLookAngle());String id=spell.definition.id();
        switch(id){
            case "caustic_wave" -> {int count=rank>=5?3:1;for(int i=0;i<count;i++)shot(spell,p,rank,"fx:plague_wave",(i-(count-1)/2f)*14,false,true);}
            case "seeking_blades" -> {
                p.level().playSound(null,p.blockPosition(),SpellSounds.SEEKING_BLADES.get(),SoundSource.PLAYERS,.8f,1f);
                var targets=SkillRuntime.targets(p,p.getEyePosition().add(frame.forward().scale(8)),18).stream().filter(p::hasLineOfSight).filter(t->t.position().subtract(p.position()).dot(frame.forward())>0).toList();
                for(int i=0;i<4;i++){
                    double side=(i-1.5)*.65;Vec3 pos=p.getEyePosition().add(frame.local(side,.2+Math.abs(side)*.2,.8));
                    UUID target=targets.isEmpty()?null:targets.get(i%targets.size()).getUUID();
                    projectile(spell,p,rank,"fx:exoblade_beam",pos,frame.forward().add(frame.right().scale(side*.09)).normalize().scale(1+rank*.03),true,false,target,!targets.isEmpty()&&i>=targets.size()?.65f:1,80);
                }
            }
            case "stellar_marks" -> {
                var marks=p.level().getEntitiesOfClass(SkillVisualEntity.class,p.getBoundingBox().inflate(96),e->p.getUUID().equals(e.ownerId)&&e.skill.equals(id));
                if(!marks.isEmpty()){for(var mark:marks){SkillRuntime.blast(mark,mark.position(),2.5f);mark.discard();}}
                else for(int i=0;i<4;i++){
                    Vec3 dir=frame.forward().add(frame.right().scale((i-1.5)*.085)).add(frame.up().scale(i%2==0?.045:-.045)).normalize();
                    projectile(spell,p,rank,"star",p.getEyePosition().add(frame.forward().scale(.7)),dir.scale(1+rank*.03),false,false,null,1,spell.duration(rank));
                }
            }
            case "starlight_flurry" -> {
                int count=5+p.getRandom().nextInt(2);int color=p.getRandom().nextInt(7);
                for(int i=0;i<count;i++){
                    double a=p.getRandom().nextDouble()*Math.PI*2,r=.5+p.getRandom().nextDouble()*.75;
                    Vec3 pos=p.getEyePosition().add(frame.local(Math.cos(a)*r,Math.sin(a)*r,.5));
                    projectile(spell,p,rank,"fx:starlight_stab_"+((color+i)%7),pos,frame.forward(),false,true,null,1,15);
                }
            }
            case "stygian_souls" -> {
                Vec3 at=p.getEyePosition(),end=endpoint(p);int count=6+rank/2;
                for(int i=0;i<count;i++){
                    double a=i*Math.PI*2/count;Vec3 pos=at.add(frame.local(Math.cos(a)*1.8,.3+Math.sin(a)*.8,-1.3));
                    var soul=make(spell,p,rank,"formation","stygian_soul",pos,100+i*3,1,end.subtract(pos));
                    soul.setDeltaMovement(end.subtract(pos).normalize());soul.homing=true;soul.homingDelay=10+i*3;spawn(soul);
                }
            }
            case "soulreaping_scythes" -> {p.level().playSound(null,p.blockPosition(),SpellSounds.INFERNO_PROJECTILE.get(),SoundSource.PLAYERS,.7f,1f);int count=rank>=5?3:1;for(int i=0;i<count;i++)shot(spell,p,rank,"fx:forbidden_scythe",(i-(count-1)/2f)*15,true,true);}
            case "devil_armament" -> {
                Vec3 end=endpoint(p),at=p.getEyePosition();
                for(int i=0;i<5;i++){
                    int index=i;Vec3 pos=at.add(frame.local((i-2)*.7,.25+Math.abs(i-2)*.12,1));
                    Vec3 motion=end.subtract(pos).normalize().scale(1+rank*.03);
                    var weapon=make(spell,p,rank,"formation",index<3?"fx:devils_scythe":"fx:devils_pitchfork",pos,85,1,motion);
                    weapon.setDeltaMovement(motion);weapon.piercing=true;weapon.homingDelay=4+i*3;spawn(weapon);
                }
            }
            case "crystal_sanctuary" -> display(spell,p,rank,"healing","fx:circle_crystal",p.position().add(0,.18,0),spell.duration(rank),5.5+rank*.3,frame.forward());
            case "gaia_bloom" -> display(spell,p,rank,"growth","fx:circle_nature",p.position(),spell.duration(rank),3+rank*.2,frame.forward());
            case "pestilent_mist" -> {var cloud=display(spell,p,rank,"plague_cloud","fx:plague_cloud",endpointNear(p,12+rank),spell.duration(rank),3+rank*.2,frame.forward());p.level().playSound(null,cloud.blockPosition(),SpellSounds.PLAGUE_IMPACT.get(),SoundSource.PLAYERS,.7f,1f);}
            case "plague_infusion" -> {
                var target=SkillRuntime.aim(p,28+rank);if(target!=null){SkillRuntime.hurt(id,rank,p,target,spell.damage(rank,p),p);SkillRuntime.infect(p,target,100+rank*10,Math.min(3,rank/3));p.level().playSound(null,target.blockPosition(),SpellSounds.PLAGUE_IMPACT.get(),SoundSource.PLAYERS,.7f,1f);puff(spell,p,rank,target.getBoundingBox().getCenter());}
            }
            case "corrosive_claws" -> {
                for(int i=0;i<3;i++){int phase=i;SkillRuntime.later(p,i*5,()->{
                    Vec3 front=p.getLookAngle();display(spell,p,rank,"claws","fx:plague_claws",p.getEyePosition().add(front.scale(1.8)),7,1.6,front);
                    for(var target:SkillRuntime.targets(p,p.position().add(front.scale(2)),3)){
                        SkillRuntime.hurt(id,rank,p,target,spell.damage(rank,p),p);SkillRuntime.infect(p,target,80+rank*5,1);target.addEffect(new MobEffectInstance(SkillEffects.CORROSION.get(),100,Math.min(2,rank/3)));
                    }
                });}
            }
            case "triple_judgment","triple_profane_beam" -> {
                Vec3 ahead=groundForward(p);
                for(int i=0;i<3;i++){
                    Vec3 ground=groundPoint(p,p.position().add(ahead.scale(3+i*4.5)));
                    if(ground==null)continue;
                    var e=make(spell,p,rank,"pillar_warning",id.equals("triple_judgment")?"fx:circle_crystal":"fx:circle_eldritch",ground.add(0,.12,0),14+i*10,2.25,new Vec3(0,0,1));
                    e.speed=12+i*10;spawn(e);
                }
            }
            case "guardian_hand" -> {
                Vec3 at=endpointNear(p,2.5).subtract(frame.forward().scale(.5));
                var hand=make(spell,p,rank,"guardian_hand","tg_hand",at,spell.duration(rank),18+rank,frame.forward());hand.speed=18+rank;spawn(hand);
            }
            case "hellrok_slam" -> {
                display(spell,p,rank,"impact","fx:inferno_impact",p.position(),22,4+rank*.2,frame.forward());
                for(var target:SkillRuntime.targets(p,p.position(),4+rank*.2))if(groundReach(p,target)){
                    SkillRuntime.hurt(id,rank,p,target,spell.damage(rank,p),p);SkillRuntime.push(target,new Vec3(0,.35,0));target.setSecondsOnFire(3+rank/2);
                }
            }
            default -> {return false;}
        }
        return true;
    }
    public static Vec3 groundForward(Player p){
        Vec3 horizontal=p.getLookAngle().multiply(1,0,1);
        return horizontal.lengthSqr()>1e-6?horizontal.normalize():new Vec3(0,0,1).yRot((float)-Math.toRadians(p.getYRot()));
    }
    public static Vec3 groundPoint(Player p,Vec3 point){
        if(!p.level().hasChunkAt(net.minecraft.core.BlockPos.containing(point)))return null;
        Vec3 from=p.position().add(0,1,0),top=new Vec3(point.x,p.getY()+1,point.z);
        if(p.level().clip(new ClipContext(from,top,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()!=HitResult.Type.MISS)return null;
        var hit=p.level().clip(new ClipContext(top.add(0,2,0),top.add(0,-9,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        return hit.getType()==HitResult.Type.MISS||hit.getDirection()!=net.minecraft.core.Direction.UP?null:hit.getLocation();
    }
    private static Vec3 endpointNear(Player p,double range){
        Vec3 start=p.getEyePosition();var hit=p.level().clip(new ClipContext(start,start.add(p.getLookAngle().scale(range)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        return hit.getType()==HitResult.Type.MISS?start.add(p.getLookAngle().scale(range)):hit.getLocation();
    }
    public static boolean groundReach(Player p,LivingEntity target){
        if(Math.abs(target.getY()-p.getY())>1.25||!target.onGround()&&!target.verticalCollisionBelow&&target.level().getBlockState(target.blockPosition().below()).getCollisionShape(target.level(),target.blockPosition().below()).isEmpty())return false;
        return p.level().clip(new ClipContext(p.position().add(0,.45,0),target.position().add(0,.45,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS;
    }
    public static void puff(SkillSpell spell,Player p,int rank,Vec3 at){display(spell,p,rank,"puff","fx:plague_cloud",at,18,.65,new Vec3(0,0,1));}
    public static boolean tick(SkillVisualEntity e){
        if(e.role.equals("formation")){if(e.age>=e.homingDelay){e.role="projectile";if(e.skill.equals("stygian_souls")||e.skill.equals("devil_armament"))e.level().playSound(null,e.blockPosition(),SpellSounds.INFERNO_PROJECTILE.get(),SoundSource.PLAYERS,.3f,1f);}return true;}
        if(e.role.equals("pillar_warning")&&(e.skill.equals("triple_judgment")||e.skill.equals("triple_profane_beam"))){
            if(e.age>=(int)e.speed){
                var spell=SkillContent.spell(e.skill);
                display(spell,e.owner(),e.rank,"descending_beam",e.skill.equals("triple_judgment")?"holy_beam":"profane_beam",e.position().add(0,-.12,0),104,2.25,new Vec3(0,0,1));
                e.discard();
            }
            return true;
        }
        if(e.role.equals("descending_beam")){
            if(e.age>=8 && e.age<=98 && (e.age-8)%10==0)for(var target:e.level().getEntitiesOfClass(LivingEntity.class,new AABB(e.position(),e.position().add(0,12.5,0)).inflate(2.25,0,2.25),t->SkillRuntime.enemy(e.owner(),t)))
                SkillRuntime.hurt(e.skill,e.rank,e.owner(),target,e.damage*.25f,e);
            return true;
        }
        if(e.role.equals("ray_warning")||e.role.equals("ray")){
            if(e.age==(int)e.speed){e.role="ray";e.radius=rayLength(e,e.anchor,28+e.rank);damageRay(e,e.anchor,e.radius);}
            return true;
        }
        if(e.role.equals("guardian_hand")){
            e.radius=rayLength(e,e.anchor,e.speed);
            if(e.age%10==0)damageRay(e,e.anchor,e.radius);return true;
        }
        if(Set.of("impact","warmup","claws","puff").contains(e.role))return true;
        return false;
    }
    public static double rayLength(SkillVisualEntity e,Vec3 direction,double distance){
        return e.position().distanceTo(e.level().clip(new ClipContext(e.position(),e.position().add(direction.normalize().scale(distance)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,e)).getLocation());
    }
    private static void damageRay(SkillVisualEntity e,Vec3 direction,double distance){
        Vec3 from=e.position(),end=from.add(direction.normalize().scale(distance));
        for(var target:SkillRuntime.targets(e.owner(),from.add(end).scale(.5),distance*.5+2)){
            var box=target.getBoundingBox().inflate(.4);if(box.contains(from)||box.clip(from,end).isPresent())SkillRuntime.hurt(e.skill,e.rank,e.owner(),target,e.damage,e);
        }
    }
    private SkillRevisionRuntime(){}
}
