package cn.wanyiwige.brutalityiron.skills;

import cn.wanyiwige.brutalityiron.BrutalityIron;
import cn.wanyiwige.brutalityiron.SpellSounds;
import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.*;
import java.util.function.Predicate;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid=BrutalityIron.ID)
public final class SkillRuntime {
    private static final Map<String,State> STATES=new HashMap<>();
    private static final List<Task> TASKS=new ArrayList<>();
    private static final Map<UUID,Long> RECENT_DEATHS=new HashMap<>();
    private static boolean extraDamage;
    private record Task(ServerLevel level,Player owner,long at,Runnable callback){}
    public static final class State {
        public Player owner;public SkillSpell spell;public int rank,count,still;public long until;
        public Vec3 position;public UUID target;public float baseline,amount;public boolean active;
    }
    private static String key(Player p,String id){return p.getUUID()+":"+id;}
    public static State state(Player p,String id){return STATES.get(key(p,id));}
    private static State remember(SkillSpell spell,Player p,int rank){
        State s=new State();s.owner=p;s.spell=spell;s.rank=rank;s.until=p.level().getGameTime()+spell.duration(rank);s.position=p.position();STATES.put(key(p,spell.definition.id()),s);return s;
    }
    public static void later(Player p,int ticks,Runnable task){TASKS.add(new Task((ServerLevel)p.level(),p,p.level().getGameTime()+ticks,task));}
    public static boolean enemy(Player owner,LivingEntity target){
        if(target==owner||!target.isAlive()||target.isSpectator()||owner.isAlliedTo(target))return false;
        if(target instanceof OwnableEntity own&&owner.getUUID().equals(own.getOwnerUUID()))return false;
        if(target instanceof Player p&&(!owner.canHarmPlayer(p)||owner.getServer()!=null&&!owner.getServer().isPvpAllowed()))return false;
        return true;
    }
    public static List<LivingEntity> targets(Player p,Vec3 pos,double radius){
        var found=p.level().getEntitiesOfClass(LivingEntity.class,new AABB(pos,pos).inflate(radius),e->enemy(p,e)&&e.distanceToSqr(pos)<radius*radius);
        found.sort(Comparator.comparingDouble(e->e.distanceToSqr(pos)));return found;
    }
    public static LivingEntity aim(Player p,double range){
        Vec3 start=p.getEyePosition(),end=start.add(p.getLookAngle().scale(range));
        var block=p.level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        end=block.getLocation();var hit=ProjectileUtil.getEntityHitResult(p.level(),p,start,end,p.getBoundingBox().expandTowards(end.subtract(start)).inflate(1),e->e instanceof LivingEntity l&&enemy(p,l));
        return hit!=null?(LivingEntity)hit.getEntity():null;
    }
    private static Vec3 groundAim(Player p,double range){
        var start=p.getEyePosition();var end=start.add(p.getLookAngle().scale(range));
        return p.level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getLocation();
    }
    private static Vec3 targetPoint(Player p,double range){var target=aim(p,range);return target==null?groundAim(p,range):target.position();}
    private static boolean safe(Player p,Vec3 pos){
        var box=p.getBoundingBox().move(pos.subtract(p.position()));
        return p.level().getWorldBorder().isWithinBounds(BlockPos.containing(pos))&&p.level().hasChunkAt(BlockPos.containing(pos))&&p.level().noCollision(p,box)&&!p.level().containsAnyLiquid(box);
    }
    private static boolean teleport(Player p,Vec3 pos){
        if(!safe(p,pos))return false;
        p.teleportTo(pos.x,pos.y,pos.z);p.fallDistance=0;particles(p.level(),ParticleTypes.PORTAL,pos.add(0,.7,0),25,.5);return true;
    }
    private static Vec3 ascent(Player p){
        BlockPos at=p.blockPosition().above(2);boolean ceiling=false;
        for(int i=0;i<Math.min(160,p.level().getMaxBuildHeight()-at.getY()-2);i++,at=at.above()){
            boolean solid=!p.level().getBlockState(at).getCollisionShape(p.level(),at).isEmpty();
            if(!ceiling){if(i>15)return null;if(solid)ceiling=true;}
            else if(!solid){Vec3 v=Vec3.atBottomCenterOf(at);if(safe(p,v))return v;}
        }return null;
    }
    public static boolean canCast(SkillDefinition d,Player p,int rank){
        return switch(d.id()){
            case "backstep","void_exchange","telekinetic_grasp","core_shatter","plague_infusion"->aim(p,28+rank)!=null;
            case "pursuit_anchor"->state(p,d.id())!=null||aim(p,28+rank)!=null;
            case "ceiling_ascent"->ascent(p)!=null;
            case "dream_recall"->reviveTarget(p)!=null;
            case "vital_covenant"->p.getMaxHealth()>6;
            case "homeward_gale"->p instanceof ServerPlayer;
            default->true;
        };
    }
    public static boolean hurt(String skill,int rank,Player owner,LivingEntity target,float amount,Entity direct){
        if(!SkillContent.SKILLS.containsKey(skill)||!enemy(owner,target)||amount<=0)return false;
        boolean previous=extraDamage;extraDamage=true;
        try{return DamageSources.applyDamage(target,amount,SkillContent.spell(skill).getDamageSource(direct,owner));}
        finally{extraDamage=previous;}
    }
    public static void particles(Level level,ParticleOptions particle,Vec3 pos,int count,double spread){
        if(level instanceof ServerLevel server)server.sendParticles(particle,pos.x,pos.y,pos.z,count,spread,spread,spread,.015);
    }
    private static ParticleOptions dust(float r,float g,float b){return new DustParticleOptions(new Vector3f(r,g,b),1);}
    private static ParticleOptions color(String skill){
        String school=SkillDefinition.ALL.get(skill).school();
        if(school.endsWith("plague"))return dust(.55f,.9f,.18f);
        if(school.endsWith("inferno"))return dust(.65f,.08f,.48f);
        if(school.endsWith("celestia"))return ParticleTypes.END_ROD;
        if(school.endsWith(":geo"))return ParticleTypes.CRIT;
        if(school.endsWith(":wind"))return ParticleTypes.CLOUD;
        return ParticleTypes.WITCH;
    }
    private static void ring(Level level,ParticleOptions particle,Vec3 center,double radius,int points){
        for(int i=0;i<points;i++){double a=i*Math.PI*2/points;particles(level,particle,center.add(Math.cos(a)*radius,.12,Math.sin(a)*radius),1,0);}
    }
    private static void sound(Player p,SoundEvent sound){p.level().playSound(null,p.blockPosition(),sound,SoundSource.PLAYERS,.8f,1);}
    public static SkillVisualEntity effect(SkillSpell spell,Player p,int rank,String role,String model,Vec3 pos,int life){
        var e=new SkillVisualEntity(SkillContent.VISUAL.get(),p.level());e.owner(p);e.skill=spell.definition.id();e.rank=rank;e.damage=spell.damage(rank,p);e.life=life;e.role=role;e.model(model);e.anchor=pos;e.setPos(pos);e.syncVisual();p.level().addFreshEntity(e);return e;
    }
    private static SkillVisualEntity shot(SkillSpell spell,Player p,int rank,String model,float yaw,boolean homing,boolean pierce){
        Vec3 motion=SkillGeometry.spread(p.getLookAngle(),yaw).scale(1.0+rank*.03);
        return SkillRevisionRuntime.projectile(spell,p,rank,model,p.getEyePosition().add(p.getLookAngle().scale(.7)),motion,homing,pierce,null,1,Math.min(100,spell.duration(rank)));
    }
    private static void volley(SkillSpell spell,Player p,int rank,String model,int count,float spread,boolean homing,boolean pierce){
        for(int i=0;i<count;i++)shot(spell,p,rank,model,(i-(count-1)/2f)*spread,homing,pierce);
    }
    private static void zone(SkillSpell spell,Player p,int rank,String role,String model,Vec3 pos,double radius){
        SkillRevisionRuntime.display(spell,p,rank,role,model,pos,spell.duration(rank),radius,new Vec3(0,0,1));
    }
    public static void cast(SkillSpell spell,Player p,int rank){
        if(!(p.level() instanceof ServerLevel level))return;
        String id=spell.definition.id();int duration=spell.duration(rank);float damage=spell.damage(rank,p);
        if(SkillRevisionRuntime.cast(spell,p,rank)){if(!Set.of("seeking_blades","soulreaping_scythes","pestilent_mist","plague_infusion").contains(id))sound(p,SoundEvents.ENCHANTMENT_TABLE_USE);return;}
        switch(id){
            case "event_horizon" -> zone(spell,p,rank,"black_hole","black_hole",p.position().add(0,4,0),6+rank*.3);
            case "cruel_sun" -> zone(spell,p,rank,"sun","cruel_sun",p.position().add(0,8,0),10+rank*.4);
            case "sacred_lance" -> {beam(spell,p,rank,p.getEyePosition(),p.getLookAngle(),28+rank,damage);effect(spell,p,rank,"display","rhongomyniad_ray",p.getEyePosition(),12).setYRot(p.getYRot());}
            case "backstep" -> {var target=aim(p,28+rank);if(target!=null){Vec3 back=target.position().subtract(target.getLookAngle().multiply(1,0,1).normalize().scale(2));if(teleport(p,back))p.setYRot(target.getYRot());}}
            case "soulfrost_wave" -> zone(spell,p,rank,"frost_wave","",p.position(),7+rank*.5);
            case "stray_legion" -> summonStrays(spell,p,rank);
            case "sanguine_slash" -> volley(spell,p,rank,"bloodslash",1,0,false,true);
            case "shadowflame_slash" -> volley(spell,p,rank,"shadowflame_slash",1,0,false,true);
            case "seeking_blades" -> volley(spell,p,rank,"exoblade_beam",4,12,true,false);
            case "stellar_marks" -> {
                var marks=ownedEffects(p,id);if(!marks.isEmpty()){for(var mark:marks){blast(mark,mark.position(),2.5f);mark.discard();}}
                else for(int i=0;i<8;i++){var star=shot(spell,p,rank,"star",i*45,false,false);star.life=duration;}
            }
            case "telekinetic_grasp" -> {var target=aim(p,28+rank);if(target!=null&&target.getMaxHealth()<=60+rank*15){var e=effect(spell,p,rank,"grasp","",target.position(),duration);e.targetId=target.getUUID();}}
            case "infernal_crosscut" -> {for(var target:targets(p,p.position(),5+rank*.25))hurt(id,rank,p,target,damage,p);slashCloud(p,6,24);}
            case "charged_detonation" -> {Vec3 point=targetPoint(p,40+rank*2);zone(spell,p,rank,"detonation","",point,4+rank*.4);}
            case "gale_fist" -> {var e=effect(spell,p,rank,"dash","",p.position(),18);e.anchor=p.getLookAngle().scale(1.25+rank*.06);}
            case "abyssal_volley" -> volley(spell,p,rank,"abyss_projectile",Math.min(12,4+rank),360f/Math.min(12,4+rank),true,false);
            case "void_exchange" -> exchange(p,rank);
            case "ceiling_ascent" -> {var destination=ascent(p);if(destination!=null&&teleport(p,destination))p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,50));}
            case "inertia_reversal" -> {Vec3 motion=p.getDeltaMovement().scale(-2.5);if(motion.lengthSqr()<.01)motion=p.getLookAngle().scale(-.6);push(p,clamp(motion,2.5));}
            case "gale_recoil" -> {push(p,p.getLookAngle().multiply(-1.6,-1.2,-1.6));particles(level,ParticleTypes.CLOUD,p.position(),20,.4);}
            case "guardian_hand" -> zone(spell,p,rank,"guardian_hand","tg_hand",groundAim(p,7).add(0,1,0),18+rank);
            case "crystal_sanctuary" -> zone(spell,p,rank,"healing","",p.position(),4+rank*.2);
            case "gaia_bloom" -> zone(spell,p,rank,"growth","",p.position(),3+rank*.2);
            case "vanishing_decoy" -> {summon(spell,p,rank,"decoy",true,1);p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,60+rank*5));}
            case "pursuit_anchor" -> pursue(spell,p,rank);
            case "burning_zeal" -> {p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,duration,2+rank/3));p.setSecondsOnFire(duration/20);}
            case "homeward_gale" -> homeward(spell,p,rank);
            case "triple_judgment","triple_profane_beam" -> {
                Vec3 target=groundAim(p,24+rank);var victim=aim(p,24+rank);if(victim!=null)target=victim.position();
                for(int i=0;i<3;i++){Vec3 pos=target.add((i-1)*2.5,0,0);var e=effect(spell,p,rank,"pillar_warning","",pos,25+i*12+20);e.radius=1.5;e.speed=25+i*12;}
            }
            case "stygian_souls" -> volley(spell,p,rank,"stygian_soul",2+rank/3,15,true,false);
            case "arcane_retinue" -> summon(spell,p,rank,"conjurling",false,2+rank/4);
            case "ice_cube_barrage" -> volley(spell,p,rank,"ice_cube",1+rank/3,10,false,false);
            case "resonant_roar","seismic_pressure" -> {for(var target:targets(p,p.position(),6+rank*.5)){target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,30+rank*4,2));push(target,new Vec3(0,.2,0));}ring(level,color(id),p.position(),6+rank*.5,40);}
            case "stone_retinue" -> summon(spell,p,rank,"dungeon_effigy",false,2+rank/4);
            case "spirit_bomb" -> {
                Vec3 start=p.position().add(0,6,0),end=targetPoint(p,28+rank);
                var charging=ownedEffects(p,id).stream().filter(v->v.role.equals("charging_display")).findFirst();
                var e=charging.orElseGet(()->SkillRevisionRuntime.make(spell,p,rank,"projectile","spirit_bomb_flight",start,100,4+rank*.3,end.subtract(start)));
                e.role="projectile";e.radius=4+rank*.3;e.life=e.age+100;
                Vec3 motion=end.subtract(e.position()).normalize().scale(.7);e.setDeltaMovement(motion);e.orient(motion);e.syncVisual();
                if(charging.isEmpty())SkillRevisionRuntime.spawn(e);
            }
            case "stone_body_slam","hellrok_slam","core_shatter" -> {
                for(var target:targets(p,p.position(),4+rank*.2)){hurt(id,rank,p,target,damage,p);push(target,new Vec3(0,.35,0));
                    if(id.equals("core_shatter")&&target.getMaxHealth()>6)target.addEffect(new MobEffectInstance(SkillEffects.SHATTERED.get(),100,Math.min(1,rank/5)));
                    if(id.equals("hellrok_slam"))target.setSecondsOnFire(3+rank/2);
                }ring(level,color(id),p.position(),4+rank*.2,40);
            }
            case "gale_blitz" -> {var e=effect(spell,p,rank,"gale_blitz","",p.position(),40);e.anchor=p.getLookAngle();}
            case "caustic_wave" -> volley(spell,p,rank,"acidic_slash",rank>=5?3:1,14,false,true);
            case "soulreaping_scythes" -> volley(spell,p,rank,"forbidden_scythe",rank>=5?3:1,15,true,true);
            case "devil_armament" -> {volley(spell,p,rank,"devils_scythe",3,18,false,true);shot(spell,p,rank,"devils_pitchfork",-28,false,true);shot(spell,p,rank,"devils_pitchfork",28,false,true);}
            case "ashen_skulls" -> volley(spell,p,rank,"gael_skull",3,18,true,false);
            case "hellish_bolt" -> volley(spell,p,rank,"vehemence_bolt",1,0,false,false);
            case "brimstone_pact" -> p.addEffect(new MobEffectInstance(SkillEffects.PACT.get(),duration,Math.min(2,rank/3)));
            case "starlight_flurry" -> {for(int i=0;i<5+rank;i++)later(p,i*2,()->shot(spell,p,rank,"starlight_stab",p.getRandom().nextInt(9)-4,false,true));}
            case "ironwill_recovery" -> shield(spell,p,rank);
            case "binding_hook" -> volley(spell,p,rank,"hook_model",1,0,false,false);
            case "stillness_ward" -> {p.addEffect(new MobEffectInstance(SkillEffects.STILL.get(),duration));remember(spell,p,rank);}
            case "tremor_sense" -> remember(spell,p,rank);
            case "blade_current" -> {remember(spell,p,rank);p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,duration,1+rank/4));p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED,duration,1));var attr=p.getAttribute(Attributes.ATTACK_SPEED);if(attr!=null){attr.removeModifier(BLADE_SPEED);attr.addTransientModifier(new AttributeModifier(BLADE_SPEED,"Gale blade rhythm",.3+rank*.03,AttributeModifier.Operation.MULTIPLY_TOTAL));}}
            case "plague_infusion" -> {var target=aim(p,28+rank);if(target!=null){hurt(id,rank,p,target,damage,p);infect(p,target,100+rank*10,Math.min(3,rank/3));}}
            case "pestilent_mist" -> zone(spell,p,rank,"plague_cloud","",targetPoint(p,12+rank),3+rank*.2);
            case "corrosive_claws" -> {
                for(int i=0;i<3;i++)later(p,i*5,()->{for(var target:targets(p,p.position().add(p.getLookAngle().scale(2)),3)){hurt(id,rank,p,target,damage,p);infect(p,target,80+rank*5,1);target.addEffect(new MobEffectInstance(SkillEffects.CORROSION.get(),100,Math.min(2,rank/3)));}slashCloud(p,3,10);});
            }
            case "infernal_spirits" -> summon(spell,p,rank,"fire_spirit",false,2+rank/3);
            case "dawn_triad","eclipse_charge","meteor_reservoir" -> chargedState(spell,p,rank);
            case "vital_covenant" -> {int amp=Math.min(2,Math.max(0,rank/2));p.addEffect(new MobEffectInstance(SkillEffects.COVENANT.get(),duration,amp));p.setHealth(Math.min(p.getHealth(),p.getMaxHealth()));}
            case "dream_recall" -> {var target=reviveTarget(p);if(target!=null){RECENT_DEATHS.remove(target.getUUID());target.setGameMode(GameType.SURVIVAL);target.setHealth(Math.max(1,target.getMaxHealth()*.3f));target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,100,2));particles(level,ParticleTypes.END_ROD,target.position().add(0,1,0),35,.7);}}
            default -> throw new IllegalArgumentException("Unimplemented reviewed skill "+id);
        }
        sound(p,SoundEvents.ENCHANTMENT_TABLE_USE);
    }
    private static void slashCloud(Player p,double range,int n){
        for(int i=0;i<n;i++)particles(p.level(),ParticleTypes.SWEEP_ATTACK,p.position().add((p.getRandom().nextDouble()-.5)*range,1+p.getRandom().nextDouble(),(p.getRandom().nextDouble()-.5)*range),1,0);
    }
    public static void beam(SkillSpell spell,Player p,int rank,Vec3 start,Vec3 dir,double distance,float damage){
        Vec3 end=p.level().clip(new ClipContext(start,start.add(dir.scale(distance)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getLocation();
        double length=start.distanceTo(end);for(double i=0;i<length;i+=.5)particles(p.level(),color(spell.definition.id()),start.add(dir.scale(i)),1,.03);
        for(var target:targets(p,start.add(end).scale(.5),length*.5+2)){
            Vec3 point=target.getBoundingBox().getCenter();double along=point.subtract(start).dot(dir);Vec3 closest=start.add(dir.scale(Mth.clamp(along,0,length)));
            if(point.distanceToSqr(closest)<1.1&&along>=0&&along<=length+1)hurt(spell.definition.id(),rank,p,target,damage,p);
        }
    }
    private static List<SkillVisualEntity> ownedEffects(Player p,String skill){return p.level().getEntitiesOfClass(SkillVisualEntity.class,p.getBoundingBox().inflate(96),e->p.getUUID().equals(e.ownerId)&&e.skill.equals(skill));}
    public static void clearCharging(Player p,String skill){for(var e:ownedEffects(p,skill))if(e.role.equals("charging_display"))e.discard();}
    public static void dismissEffects(Player p,String skill){
        if(p.getServer()==null)return;
        for(var level:p.getServer().getAllLevels()){
            var effects=new ArrayList<SkillVisualEntity>();
            for(var entity:level.getAllEntities())if(entity instanceof SkillVisualEntity v&&p.getUUID().equals(v.ownerId)&&v.skill.equals(skill))effects.add(v);
            effects.forEach(Entity::discard);
        }
    }
    public static void tick(SkillVisualEntity e){
        if(e.skill.equals("celestial_guard")){cn.wanyiwige.brutalityiron.CelestialGuard.tickVisual(e);return;}
        if(SkillRevisionRuntime.tick(e))return;
        Player p=e.owner();ServerLevel level=(ServerLevel)e.level();SkillSpell spell=SkillContent.spell(e.skill);
        switch(e.role){
            case "projectile" -> projectile(e,p);
            case "star_mark" -> {if(e.targetId!=null&&level.getEntity(e.targetId) instanceof LivingEntity target)e.setPos(target.getBoundingBox().getCenter());if(e.age%8==0)particles(level,ParticleTypes.END_ROD,e.position(),2,.1);}
            case "black_hole" -> {
                e.setPos(p.position().add(0,4,0));if(e.age%5==0)for(var target:targets(p,e.position(),e.radius)){
                    push(target,clamp(e.position().subtract(target.getBoundingBox().getCenter()).normalize().scale(.22),.4));if(e.age%10==0)hurt(e.skill,e.rank,p,target,e.damage,e);
                }
            }
            case "sun" -> {e.setPos(p.position().add(0,8,0));if(e.age%20==0)for(var target:targets(p,e.position(),e.radius))hurt(e.skill,e.rank,p,target,spell.damage(e.rank,p),e);}
            case "frost_wave" -> {
                double r=e.radius*Math.min(1,e.age/20.0);ring(level,ParticleTypes.SNOWFLAKE,e.position(),r,32);
                for(var target:targets(p,e.position(),r))if(e.hit.add(target.getUUID())){hurt(e.skill,e.rank,p,target,e.damage,e);target.setTicksFrozen(Math.min(180,target.getTicksFrozen()+50+e.rank*5));target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,1));if(e.hit.size()<=8)p.heal(1);}
            }
            case "grasp" -> {
                if(!(level.getEntity(e.targetId) instanceof LivingEntity target)||!enemy(p,target)){e.discard();break;}
                Vec3 point=p.getEyePosition().add(p.getLookAngle().scale(6));if(target.distanceToSqr(p)>40*40){e.discard();break;}
                push(target,clamp(point.subtract(target.getBoundingBox().getCenter()).scale(.35),1.2));e.setPos(target.position());
                if(e.age%6==0)particles(level,ParticleTypes.PORTAL,target.getBoundingBox().getCenter(),6,.4);
                if((target.horizontalCollision||target.verticalCollision)&&e.age%15==0)hurt(e.skill,e.rank,p,target,e.damage,e);
                if(e.age>=e.life-1)push(target,p.getLookAngle().scale(1.6));
            }
            case "detonation" -> {
                if(e.age<20)ring(level,ParticleTypes.FLAME,e.position(),e.radius*e.age/20,24);
                if(e.age==20){blast(e,e.position(),(float)e.radius);particles(level,ParticleTypes.EXPLOSION_EMITTER,e.position(),1,0);}
            }
            case "dash" -> {
                Vec3 old=e.position();Vec3 now=p.position();double moved=old.distanceTo(now);
                if(moved>.01){for(double d=.6-e.trailCarry;d<=Math.min(moved,10);d+=.6)particles(level,ParticleTypes.CLOUD,old.add(now.subtract(old).normalize().scale(d)).add(0,.7,0),5,.18);e.trailCarry=(e.trailCarry+moved)%.6;}
                push(p,e.anchor);e.setPos(now);p.fallDistance=0;
                for(var target:targets(p,p.position(),2))if(e.hit.add(target.getUUID())){hurt(e.skill,e.rank,p,target,e.damage,e);push(target,e.anchor.add(0,.2,0));}
                if(p.horizontalCollision)e.discard();
                
            }
            case "gale_blitz" -> {
                e.setPos(p.position());if(e.age%10==1){Vec3 motion=p.getLookAngle().multiply(1,0,1).normalize().scale(.8);push(p,motion);slashCloud(p,5,12);for(var target:targets(p,p.position(),4))hurt(e.skill,e.rank,p,target,e.damage,e);}
            }
            case "guardian_hand" -> {
                e.setYRot(e.age*5f);if(e.age%5==0){double a=Math.toRadians(e.age*5);Vec3 dir=new Vec3(Math.sin(a),0,Math.cos(a));beam(spell,p,e.rank,e.position(),dir,e.radius,e.damage);}
            }
            case "healing" -> {
                if(!e.model().startsWith("fx:")&&e.age%6==0)ring(level,dust(.5f,.95f,.85f),e.position(),e.radius,20);
                if(e.age%20==0)for(var ally:level.getEntitiesOfClass(LivingEntity.class,e.getBoundingBox().inflate(e.radius),t->t==p||p.isAlliedTo(t)||t instanceof OwnableEntity own&&p.getUUID().equals(own.getOwnerUUID())))ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION,50,1));
            }
            case "growth" -> {
                if(!e.model().startsWith("fx:")&&e.age%10==0)ring(level,ParticleTypes.HAPPY_VILLAGER,e.position(),e.radius,16);
                if(e.age%20==0){int grown=0;for(var pos:BlockPos.betweenClosed(e.blockPosition().offset(-4,-1,-4),e.blockPosition().offset(4,2,4))){if(grown>=4)break;var state=level.getBlockState(pos);if(pos.distSqr(e.blockPosition())<=e.radius*e.radius&&state.getBlock() instanceof BonemealableBlock plant&&plant.isValidBonemealTarget(level,pos,state,false)){plant.performBonemeal(level,level.random,pos,state);grown++;}}}
            }
            case "pillar_warning" -> {
                if(e.age<e.speed){if(e.age%4==0)ring(level,color(e.skill),e.position(),e.radius,16);}
                else if(e.age==(int)e.speed){effect(spell,p,e.rank,"display","holy_beam",e.position(),18);for(var target:targets(p,e.position().add(0,1,0),3))if(Math.abs(target.getX()-e.getX())<1.8&&Math.abs(target.getZ()-e.getZ())<1.8)hurt(e.skill,e.rank,p,target,e.damage,e);}
            }
            case "plague_cloud" -> {
                if(!e.model().startsWith("fx:")&&e.age%3==0)particles(level,dust(.55f,.85f,.2f),e.position().add(0,.6,0),12,e.radius*.5);
                if(e.model().startsWith("fx:")&&e.age%4==0)for(int i=0;i<4;i++){
                    double a=level.random.nextDouble()*Math.PI*2,r=Math.sqrt(level.random.nextDouble())*e.radius;
                    particles(level,dust(.28f,.52f,.16f),e.position().add(Math.cos(a)*r,.6+level.random.nextDouble()*2.8,Math.sin(a)*r),1,0);
                }
                if(e.age%20==0)for(var target:level.getEntitiesOfClass(LivingEntity.class,new AABB(e.getX()-e.radius,e.getY()+.15,e.getZ()-e.radius,e.getX()+e.radius,e.getY()+3.8,e.getZ()+e.radius),t->enemy(p,t))){infect(p,target,80+e.rank*5,Math.min(3,e.rank/3));hurt(e.skill,e.rank,p,target,e.damage,e);}
            }
            case "home_portal" -> {
                if(e.age%5==0)ring(level,ParticleTypes.CLOUD,e.position(),1.5,16);
                if(e.age%10==0)for(var ally:level.getEntitiesOfClass(ServerPlayer.class,e.getBoundingBox().inflate(1.2),t->t==p||p.isAlliedTo(t)))returnHome(ally);
            }
            case "display" -> {}
            case "charging_display" -> {e.setPos(p.position().add(0,6,0));if(e.age%6==0)particles(level,ParticleTypes.ENCHANT,e.position(),12,.7);}
            default -> throw new IllegalStateException("Unknown effect role "+e.role);
        }
    }
    private static void projectile(SkillVisualEntity e,Player p){
        ServerLevel level=(ServerLevel)e.level();
        if(e.skill.equals("starlight_flurry")&&e.age>3){e.setDeltaMovement(e.getDeltaMovement().scale(.8));if(e.getDeltaMovement().length()<.15){e.discard();return;}}
        if(e.homing&&e.age>e.homingDelay){
            LivingEntity target=e.targetId==null?null:level.getEntity(e.targetId) instanceof LivingEntity l?l:null;
            if(target==null||!enemy(p,target)||!visible(e,target)){var choices=targets(p,e.position(),14).stream().filter(t->visible(e,t)&&!e.hit.contains(t.getUUID())).toList();target=choices.isEmpty()?null:choices.get(0);if(target!=null)e.targetId=target.getUUID();}
            if(target!=null){Vec3 toward=target.getBoundingBox().getCenter().subtract(e.position()).normalize();e.setDeltaMovement(SkillGeometry.turn(e.getDeltaMovement(),toward,Math.toRadians(7)));}
        }
        Vec3 from=e.position(),to=from.add(e.getDeltaMovement());var block=level.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,e));
        for(int pierced=0;pierced<16;pierced++){
          var hit=ProjectileUtil.getEntityHitResult(level,e,from,block.getLocation(),e.getBoundingBox().expandTowards(e.getDeltaMovement()).inflate(.6),t->t instanceof LivingEntity l&&enemy(p,l)&&!e.hit.contains(t.getUUID()));
          if(hit==null)break;
          var target=(LivingEntity)hit.getEntity();e.hit.add(target.getUUID());
            if(e.skill.equals("stellar_marks")){e.targetId=target.getUUID();e.role="star_mark";e.setDeltaMovement(Vec3.ZERO);e.setPos(target.getBoundingBox().getCenter());return;}
            hurt(e.skill,e.rank,p,target,e.damage,e);onProjectileHit(e,p,target);
            if(!e.piercing){if(Set.of("spirit_bomb","ashen_skulls","hellish_bolt","meteor_reservoir").contains(e.skill))blast(e,target.position(),e.skill.equals("spirit_bomb")?4:2);e.discard();return;}
        }
        if(block.getType()!=HitResult.Type.MISS){
            if(e.skill.equals("stellar_marks")){e.role="star_mark";e.setPos(block.getLocation());e.setDeltaMovement(Vec3.ZERO);return;}
            if(Set.of("spirit_bomb","hellish_bolt","meteor_reservoir").contains(e.skill))blast(e,block.getLocation(),e.skill.equals("spirit_bomb")?4:2);
            e.discard();return;
        }
        e.setPos(to);Vec3 v=e.getDeltaMovement();e.orient(v);
        if(e.age%3==0)particles(level,color(e.skill),e.position(),2,.08);
    }
    private static void onProjectileHit(SkillVisualEntity e,Player p,LivingEntity target){
        switch(e.skill){
            case "sanguine_slash" -> p.heal(Math.min(4,1+e.rank*.4f));
            case "shadowflame_slash","soulreaping_scythes","devil_armament" -> {target.setSecondsOnFire(3);optionalEffect(target,"terramity:nyxium_fire",60,0);}
            case "seeking_blades" -> optionalEffect(target,"brutality:miracle_blight",60,0);
            case "ice_cube_barrage" -> {target.setTicksFrozen(Math.min(180,target.getTicksFrozen()+70));target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,1));}
            case "caustic_wave" -> {infect(p,target,100,Math.min(3,e.rank/3));target.setSecondsOnFire(2);e.level().playSound(null,target.blockPosition(),SpellSounds.PLAGUE_IMPACT.get(),SoundSource.PLAYERS,.5f,1f);}
            case "binding_hook" -> push(target,clamp(p.position().subtract(target.position()).normalize().scale(1.8).add(0,.25,0),2));
            case "infernal_spirits" -> target.setSecondsOnFire(2);
            case "stygian_souls" -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60,0));
            default -> {}
        }
    }
    private static void optionalEffect(LivingEntity target,String id,int time,int amp){var effect=ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(id));if(effect!=null)target.addEffect(new MobEffectInstance(effect,time,amp));}
    public static void blast(SkillVisualEntity e,Vec3 at,float radius){
        if(e.skill.equals("spirit_bomb"))e.level().playSound(null,at.x,at.y,at.z,SpellSounds.SPIRIT_IMPACT.get(),SoundSource.PLAYERS,1f,1f);
        for(var target:targets(e.owner(),at,radius))hurt(e.skill,e.rank,e.owner(),target,e.damage*(float)(1-.4*Math.sqrt(target.distanceToSqr(at))/radius),e);
        particles(e.level(),ParticleTypes.EXPLOSION,at,8,.5);
    }
    public static void infect(Player owner,LivingEntity target,int duration,int amplifier){
        if(!enemy(owner,target))return;target.getPersistentData().putUUID("BrutalityIronPlagueOwner",owner.getUUID());
        target.addEffect(new MobEffectInstance(SkillEffects.PLAGUE.get(),Math.min(240,duration),Math.min(3,amplifier)));
    }
    public static void plagueTick(LivingEntity target,int amplifier){
        if(!(target.level() instanceof ServerLevel level)||!target.getPersistentData().hasUUID("BrutalityIronPlagueOwner"))return;
        var owner=level.getPlayerByUUID(target.getPersistentData().getUUID("BrutalityIronPlagueOwner"));if(owner==null||!enemy(owner,target))return;
        hurt("plague_infusion",1,owner,target,Math.min(4,1+amplifier*.65f)*SkillContent.spell("plague_infusion").getEntityPowerMultiplier(owner),owner);particles(level,dust(.6f,.85f,.2f),target.getBoundingBox().getCenter(),5,.3);
        var instance=target.getEffect(SkillEffects.PLAGUE.get());if(instance==null||instance.getDuration()<40)return;
        for(var other:targets(owner,target.position(),2.5))if(other!=target&&!other.hasEffect(SkillEffects.PLAGUE.get()))infect(owner,other,Math.min(80,instance.getDuration()),Math.max(0,amplifier-1));
    }
    private static void summon(SkillSpell spell,Player p,int rank,String model,boolean decoy,int count){
        var owned=p.level().getEntitiesOfClass(SkillMinion.class,p.getBoundingBox().inflate(64),m->p.getUUID().equals(m.ownerId)&&m.skill.equals(spell.definition.id()));
        int capacity=decoy?1:Math.min(6,2+rank/2);int available=Math.max(0,capacity-owned.size());
        if(decoy)for(var old:owned)old.discard();
        for(int i=0;i<Math.min(count,decoy?1:available);i++){
            var mob=new SkillMinion(SkillContent.MINION.get(),p.level());mob.owner(p);mob.skill=spell.definition.id();mob.rank=rank;mob.remaining=spell.duration(rank);mob.damage=spell.damage(rank,p);mob.decoy=decoy;mob.model(model);
            double a=i*Math.PI*2/Math.max(1,count);mob.moveTo(p.getX()+Math.cos(a)*1.7,p.getY(),p.getZ()+Math.sin(a)*1.7,0,0);
            if(p.level().noCollision(mob))p.level().addFreshEntity(mob);
        }
    }
    private static void summonStrays(SkillSpell spell,Player p,int rank){
        int existing=p.level().getEntitiesOfClass(SkillStray.class,p.getBoundingBox().inflate(64),s->p.getUUID().equals(s.getOwnerUUID())).size();
        int count=Math.min(Math.min(5,2+rank/3),Math.max(0,10-existing));
        for(int i=0;i<count;i++){
            var stray=new SkillStray(SkillContent.STRAY.get(),p.level());stray.setSummoner(p);stray.remaining=spell.duration(rank);stray.spellDamage=spell.damage(rank,p);
            stray.setItemSlot(EquipmentSlot.MAINHAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BOW));stray.setDropChance(EquipmentSlot.MAINHAND,0);stray.reassessWeaponGoal();
            stray.moveTo(p.getX()+(i-count/2.0)*1.2,p.getY(),p.getZ()-1,0,0);if(p.level().noCollision(stray))p.level().addFreshEntity(stray);
        }
    }
    public static java.util.List<SkillMinion> summonNative(SkillSpell spell,Player p,int rank){
        summon(spell,p,rank,spell.definition.id().equals("infernal_spirits")?"fire_spirit":"dungeon_effigy",false,2+rank/(spell.definition.id().equals("infernal_spirits")?3:4));
        return p.level().getEntitiesOfClass(SkillMinion.class,p.getBoundingBox().inflate(64),m->p.getUUID().equals(m.ownerId)&&m.skill.equals(spell.definition.id()));
    }
    public static void dismissSummons(Player owner,String id){
        if(owner.getServer()==null)return;
        for(var level:owner.getServer().getAllLevels()){
            var list=new ArrayList<SkillMinion>();for(var entity:level.getAllEntities())if(entity instanceof SkillMinion m&&owner.getUUID().equals(m.ownerId)&&m.skill.equals(id))list.add(m);
            list.forEach(Entity::discard);
        }
    }
    public static void clearWarmup(Player p,String id){for(var e:ownedEffects(p,id))if(e.role.equals("warmup"))e.discard();}
    public static boolean visible(SkillVisualEntity e,LivingEntity target){return e.level().clip(new ClipContext(e.position(),target.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,e)).getType()==HitResult.Type.MISS;}
    public static void minionShot(SkillMinion minion,Player p,LivingEntity target){
        var spell=SkillContent.spell(minion.skill);
        var e=SkillRevisionRuntime.projectile(spell,p,minion.rank,"stygian_soul",minion.getEyePosition(),target.getBoundingBox().getCenter().subtract(minion.getEyePosition()).normalize().scale(.8),true,false,target.getUUID(),1,60);
        e.damage=minion.damage;
        if(minion.skill.equals("infernal_spirits"))minion.level().playSound(null,minion.blockPosition(),SpellSounds.FIRE_SPIRIT_SHOT.get(),SoundSource.PLAYERS,.6f,1f);
    }
    private static void exchange(Player p,int rank){
        LivingEntity target=aim(p,28+rank);if(target==null)return;Vec3 start=p.position(),end=target.position();
        if(!safe(p,end)||!p.level().noCollision(target,target.getBoundingBox().move(start.subtract(end))))return;
        if(!teleport(p,end))return;moveLiving(target,start);p.level().playSound(null,BlockPos.containing(end),SpellSounds.VOID_TELEPORT.get(),SoundSource.PLAYERS,.8f,1f);
        later(p,100,()->{if(target.isAlive()&&target.level()==p.level()&&safe(p,start)&&p.level().noCollision(target,target.getBoundingBox().move(end.subtract(target.position())))){teleport(p,start);moveLiving(target,end);p.level().playSound(null,BlockPos.containing(start),SpellSounds.VOID_TELEPORT.get(),SoundSource.PLAYERS,.8f,1f);}});
    }
    private static void moveLiving(LivingEntity target,Vec3 point){if(target instanceof ServerPlayer player)player.teleportTo(point.x,point.y,point.z);else target.teleportTo(point.x,point.y,point.z);target.fallDistance=0;}
    private static void pursue(SkillSpell spell,Player p,int rank){
        State s=state(p,spell.definition.id());
        if(s!=null&&s.until>=p.level().getGameTime()&&((ServerLevel)p.level()).getEntity(s.target) instanceof LivingEntity target){
            Vec3 dest=target.position().subtract(target.getLookAngle().multiply(1,0,1).normalize().scale(2));if(teleport(p,dest))STATES.remove(key(p,spell.definition.id()));
        }else{var target=aim(p,28+rank);if(target!=null){s=remember(spell,p,rank);s.target=target.getUUID();particles(p.level(),ParticleTypes.PORTAL,target.getEyePosition(),16,.3);}}
    }
    private static void homeward(SkillSpell spell,Player p,int rank){Vec3 origin=p.position();zone(spell,p,rank,"home_portal","",origin,1.5);if(p instanceof ServerPlayer player)returnHome(player);}
    private static void returnHome(ServerPlayer p){
        var server=p.getServer();if(server==null)return;var dest=server.getLevel(p.getRespawnDimension());if(dest==null)dest=server.overworld();
        BlockPos spawn=p.getRespawnPosition();if(spawn==null)spawn=dest.getSharedSpawnPos();
        var safe=ServerPlayer.findRespawnPositionAndUseSpawnBlock(dest,spawn,p.getRespawnAngle(),p.isRespawnForced(),true).orElse(null);
        if(safe==null){spawn=dest.getSharedSpawnPos();safe=Vec3.atBottomCenterOf(dest.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,spawn));}
        p.teleportTo(dest,safe.x,safe.y,safe.z,p.getYRot(),p.getXRot());p.fallDistance=0;
    }
    private static void shield(SkillSpell spell,Player p,int rank){
        State previous=state(p,spell.definition.id());
        if(previous!=null&&previous.active){float remainder=Math.max(0,p.getAbsorptionAmount()-previous.baseline);p.setAbsorptionAmount(Math.max(previous.baseline,p.getAbsorptionAmount()-remainder));p.heal(Math.min(8+rank,remainder*.5f));STATES.remove(key(p,spell.definition.id()));return;}
        State s=remember(spell,p,rank);s.baseline=p.getAbsorptionAmount();s.amount=Math.min(p.getMaxHealth()*.5f,5+rank*1.2f);s.active=true;p.setAbsorptionAmount(s.baseline+s.amount);
    }
    private static void chargedState(SkillSpell spell,Player p,int rank){
        String id=spell.definition.id();State previous=state(p,id);
        if(previous!=null&&previous.count>0&&!id.equals("dawn_triad")){
            if(id.equals("meteor_reservoir")){
                Vec3 at=groundAim(p,24+rank);var target=aim(p,24+rank);if(target!=null)at=target.position();
                for(int i=0;i<Math.min(8,previous.count);i++){Vec3 point=at.add((i%3-1)*2,12+i*.3,(i/3-1)*2);var e=effect(spell,p,rank,"projectile","meteor",point,50);e.setDeltaMovement(0,-1.1,0);}
            }else{var target=aim(p,28+rank);if(target!=null){hurt(id,rank,p,target,spell.damage(rank,p)*(1+Math.min(5,previous.count)*.4f),p);particles(p.level(),ParticleTypes.PORTAL,target.getEyePosition(),24,.5);}}
            STATES.remove(key(p,id));
        }else remember(spell,p,rank);
    }
    private static ServerPlayer reviveTarget(Player p){
        if(p.getServer()==null||p.getServer().isHardcore())return null;
        return p.level().getEntitiesOfClass(ServerPlayer.class,p.getBoundingBox().inflate(12),other->other.isSpectator()&&RECENT_DEATHS.getOrDefault(other.getUUID(),Long.MIN_VALUE)>p.level().getGameTime()-600).stream().findFirst().orElse(null);
    }
    public static Vec3 clamp(Vec3 v,double max){return v.lengthSqr()>max*max?v.normalize().scale(max):v;}
    public static void push(LivingEntity target,Vec3 velocity){target.setDeltaMovement(velocity);target.hurtMarked=true;if(target instanceof ServerPlayer p)p.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(target));}
    private static final UUID BLADE_SPEED=UUID.fromString("8915c5d7-ce8f-41d7-bd60-65d8eddb9910");
    private static void expire(State s){
        if(s.spell.definition.id().equals("ironwill_recovery")&&s.active)s.owner.setAbsorptionAmount(Math.min(s.owner.getAbsorptionAmount(),s.baseline));
        if(s.spell.definition.id().equals("blade_current")){var attr=s.owner.getAttribute(Attributes.ATTACK_SPEED);if(attr!=null)attr.removeModifier(BLADE_SPEED);}
    }
    @SubscribeEvent public static void serverTick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;
        var due=new ArrayList<Task>();var it=TASKS.iterator();while(it.hasNext()){var task=it.next();if(task.level.getGameTime()>=task.at){due.add(task);it.remove();}}
        for(var task:due)if(task.owner.isAlive()&&!task.owner.isRemoved()&&task.owner.level()==task.level)task.callback.run();
        var states=STATES.entrySet().iterator();while(states.hasNext()){
            State s=states.next().getValue();Player p=s.owner;
            if(!p.isAlive()||p.isRemoved()||p.level().getGameTime()>s.until){expire(s);states.remove();continue;}
            if(p.level().getGameTime()%5!=0)continue;
            switch(s.spell.definition.id()){
                case "stillness_ward" -> {if(!p.hasEffect(SkillEffects.STILL.get())){states.remove();break;}if(p.position().distanceToSqr(s.position)<.0004)s.still+=5;else s.still=0;s.position=p.position();if(s.still>=60)ring(p.level(),ParticleTypes.PORTAL,p.position(),.8,8);}
                case "tremor_sense" -> {for(var target:targets(p,p.position(),12+s.rank*3))if(target.getDeltaMovement().horizontalDistanceSqr()>.001)target.addEffect(new MobEffectInstance(MobEffects.GLOWING,12));}
                case "blade_current" -> {for(var target:targets(p,p.position(),4+s.rank*.2))target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,12,1));}
                default -> {}
            }
        }
    }
    @SubscribeEvent public static void onHurt(LivingHurtEvent event){
        if(extraDamage)return;
        if(event.getEntity() instanceof Player victim){State ward=state(victim,"stillness_ward");if(ward!=null&&ward.still>=60&&!event.getSource().is(DamageTypeTags.BYPASSES_RESISTANCE))event.setAmount(event.getAmount()*.15f);}
        if(!(event.getSource().getEntity() instanceof Player p)||p==event.getEntity()||!enemy(p,event.getEntity()))return;
        if(p.hasEffect(SkillEffects.PACT.get())){event.setAmount(event.getAmount()*1.35f);p.heal(Math.min(3,event.getAmount()*.12f));}
        for(String id:List.of("dawn_triad","eclipse_charge","meteor_reservoir")){
            State s=state(p,id);if(s==null)continue;s.count=Math.min(10,s.count+1);
            if(id.equals("dawn_triad")&&s.count>=3){s.count=0;hurt(id,s.rank,p,event.getEntity(),s.spell.damage(s.rank,p),p);particles(p.level(),ParticleTypes.END_ROD,event.getEntity().getEyePosition(),14,.3);}
        }
    }
    @SubscribeEvent public static void onDeath(LivingDeathEvent e){if(e.getEntity() instanceof Player p){RECENT_DEATHS.put(p.getUUID(),p.level().getGameTime());clear(p);}}
    @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e){clear(e.getEntity());}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){clear(e.getEntity(),true);}
    @SubscribeEvent public static void knockback(LivingKnockBackEvent e){if(e.getEntity() instanceof Player p){var s=state(p,"stillness_ward");if(s!=null&&s.still>=60)e.setCanceled(true);}}
    public static void clear(Player p){clear(p,false);}
    private static void clear(Player p,boolean retainHomePortal){
        TASKS.removeIf(t->t.owner.getUUID().equals(p.getUUID()));
        STATES.entrySet().removeIf(entry->{if(entry.getValue().owner.getUUID().equals(p.getUUID())){expire(entry.getValue());return true;}return false;});
        if(p.getServer()!=null)for(var level:p.getServer().getAllLevels()){
            var snapshot=new ArrayList<Entity>();level.getAllEntities().forEach(snapshot::add);
            for(var entity:snapshot)if(entity instanceof SkillVisualEntity v&&p.getUUID().equals(v.ownerId)&&!(retainHomePortal&&v.role.equals("home_portal"))||entity instanceof SkillMinion m&&p.getUUID().equals(m.ownerId)||entity instanceof SkillStray s&&p.getUUID().equals(s.getOwnerUUID()))entity.discard();
        }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){for(var s:STATES.values())expire(s);STATES.clear();TASKS.clear();RECENT_DEATHS.clear();}
    private SkillRuntime(){}
}
