package cn.wanyiwige.brutalityiron.skills;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.*;

/** Native finite-lifetime visuals; gameplay never depends on a held source weapon. */
public class SkillVisualEntity extends Entity implements GeoEntity,net.minecraftforge.entity.IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<String> MODEL=SynchedEntityData.defineId(SkillVisualEntity.class,EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ROLE=SynchedEntityData.defineId(SkillVisualEntity.class,EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> RADIUS=SynchedEntityData.defineId(SkillVisualEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> OWNER_ENTITY_ID=SynchedEntityData.defineId(SkillVisualEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIFE=SynchedEntityData.defineId(SkillVisualEntity.class,EntityDataSerializers.INT);
    private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
    public String skill="",role="projectile";
    public UUID ownerId,targetId;
    public int rank=1,life=80,age=0,homingDelay=7;
    private int spawnAge;
    private final SkillMotionTrack motionTrack=new SkillMotionTrack();
    public float damage;
    public double radius=4,speed=1;
    public double trailCarry;
    public final Deque<Vec3> trail=new ArrayDeque<>();
    public boolean homing,piercing;
    public final Set<UUID> hit=new HashSet<>();
    public Vec3 anchor=Vec3.ZERO;
    private Player directOwner;
    public SkillVisualEntity(EntityType<? extends SkillVisualEntity> type,Level level){super(type,level);noPhysics=true;}
    @Override protected void defineSynchedData(){entityData.define(MODEL,"");entityData.define(ROLE,"display");entityData.define(RADIUS,1f);entityData.define(LIFE,80);entityData.define(OWNER_ENTITY_ID,-1);}
    public String visualRole(){return level().isClientSide?entityData.get(ROLE):role;}
    public float visualRadius(){return level().isClientSide?entityData.get(RADIUS):(float)radius;}
    public int visualLife(){return level().isClientSide?entityData.get(LIFE):life;}
    public float visualAge(float partial){return level().isClientSide?spawnAge+tickCount+partial:age+partial;}
    public void syncVisual(){entityData.set(ROLE,role);entityData.set(RADIUS,(float)radius);entityData.set(LIFE,life);}
    public void orient(Vec3 velocity){
        if(velocity.lengthSqr()<1e-8)return;
        setYRot((float)Math.toDegrees(Math.atan2(-velocity.x,velocity.z)));
        setXRot((float)-Math.toDegrees(Math.atan2(velocity.y,velocity.horizontalDistance())));
        if(tickCount==0){yRotO=getYRot();xRotO=getXRot();}
    }
    public String model(){return entityData.get(MODEL);}
    public void model(String value){entityData.set(MODEL,value);}
    public int visualOwnerId(){return entityData.get(OWNER_ENTITY_ID);}
    public void owner(Player p){ownerId=p.getUUID();directOwner=p;entityData.set(OWNER_ENTITY_ID,p.getId());}
    public Player owner(){
        if(directOwner!=null&&!directOwner.isRemoved())return directOwner;
        return ownerId==null?null:level().getPlayerByUUID(ownerId);
    }
    @Override public void lerpTo(double x,double y,double z,float yaw,float pitch,int steps,boolean teleport){
        if(teleport){super.lerpTo(x,y,z,yaw,pitch,steps,true);motionTrack.clear();return;}
        motionTrack.target(new Vec3(x,y,z),yaw,pitch,steps);
    }
    private void interpolate(){
        if(!motionTrack.active())return;
        var next=motionTrack.step(position(),getYRot(),getXRot());
        setPos(next.position());setYRot(next.yaw());setXRot(next.pitch());
    }
    @Override public void tick(){super.tick();if(level().isClientSide){interpolate();if(skill.equals("stygian_souls")){trail.addLast(position());while(trail.size()>12)trail.removeFirst();}}else{if(++age>life||owner()==null||!owner().isAlive()||(!SkillContent.SKILLS.containsKey(skill)&&!skill.equals("celestial_guard"))){discard();return;}SkillRuntime.tick(this);if(Boolean.getBoolean("brutality_iron.audit"))SkillRevisionAudit.observe(this);syncVisual();}}
    @Override public void writeSpawnData(net.minecraft.network.FriendlyByteBuf b){b.writeUtf(skill);b.writeUtf(role);b.writeVarInt(life);b.writeVarInt(age);b.writeFloat((float)radius);b.writeDouble(anchor.x);b.writeDouble(anchor.y);b.writeDouble(anchor.z);b.writeDouble(speed);}
    @Override public void readSpawnData(net.minecraft.network.FriendlyByteBuf b){skill=b.readUtf();role=b.readUtf();life=b.readVarInt();spawnAge=b.readVarInt();radius=b.readFloat();anchor=new Vec3(b.readDouble(),b.readDouble(),b.readDouble());speed=b.readDouble();}
    @Override protected void addAdditionalSaveData(CompoundTag n){
        n.putString("Skill",skill);n.putString("Role",role);n.putString("Model",model());if(ownerId!=null)n.putUUID("Owner",ownerId);if(targetId!=null)n.putUUID("Target",targetId);
        n.putInt("Rank",rank);n.putInt("Life",life);n.putInt("Age",age);n.putFloat("Damage",damage);n.putDouble("Radius",radius);n.putBoolean("Homing",homing);n.putBoolean("Piercing",piercing);
        n.putDouble("AnchorX",anchor.x);n.putDouble("AnchorY",anchor.y);n.putDouble("AnchorZ",anchor.z);
    }
    @Override protected void readAdditionalSaveData(CompoundTag n){
        skill=n.getString("Skill");role=n.getString("Role");model(n.getString("Model"));if(n.hasUUID("Owner"))ownerId=n.getUUID("Owner");if(n.hasUUID("Target"))targetId=n.getUUID("Target");
        rank=n.getInt("Rank");life=n.getInt("Life");age=n.getInt("Age");damage=n.getFloat("Damage");radius=n.getDouble("Radius");homing=n.getBoolean("Homing");piercing=n.getBoolean("Piercing");anchor=new Vec3(n.getDouble("AnchorX"),n.getDouble("AnchorY"),n.getDouble("AnchorZ"));
        // Active casts do not resume without their live caster after a restart.
        life=Math.min(life,age+40);
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
    @Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar c){
        c.add(new AnimationController<>(this,"source",0,state->{String clip=SkillVisuals.clip(model());if(model().startsWith("celestial_guard_")&&visualRole().startsWith("guard_break_"))return state.setAndContinue(RawAnimation.begin().thenPlay("animation.celestia."+visualRole()));return clip.isBlank()?software.bernie.geckolib.core.object.PlayState.STOP:state.setAndContinue(RawAnimation.begin().thenLoop(clip));}));
    }
}
