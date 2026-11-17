package cn.wanyiwige.brutalityiron.skills;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Client-only smoothing state, independent from authoritative hit detection. */
public final class SkillMotionTrack {
    public record Pose(Vec3 position,float yaw,float pitch){}
    private Vec3 target=Vec3.ZERO;
    private float yaw,pitch;
    private int remaining;
    public void target(Vec3 point,float yaw,float pitch,int steps){this.target=point;this.yaw=yaw;this.pitch=pitch;remaining=Mth.clamp(steps,1,3);}
    public boolean active(){return remaining>0;}
    public void clear(){remaining=0;}
    public Pose step(Vec3 current,float currentYaw,float currentPitch){
        if(remaining==0)return new Pose(current,currentYaw,currentPitch);
        float fraction=1f/remaining--;
        return new Pose(current.lerp(target,fraction),currentYaw+Mth.wrapDegrees(yaw-currentYaw)*fraction,Mth.lerp(fraction,currentPitch,pitch));
    }
}
