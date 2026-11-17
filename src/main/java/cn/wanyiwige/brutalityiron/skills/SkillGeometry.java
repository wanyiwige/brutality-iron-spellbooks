package cn.wanyiwige.brutalityiron.skills;

import net.minecraft.world.phys.Vec3;

/** Shared server/client coordinate contract: forward +Z, right +X, up +Y. */
public final class SkillGeometry {
    public record Frame(Vec3 forward,Vec3 right,Vec3 up){
        public Vec3 local(double rightAmount,double upAmount,double forwardAmount){return right.scale(rightAmount).add(up.scale(upAmount)).add(forward.scale(forwardAmount));}
    }
    public static Frame frame(Vec3 direction){
        Vec3 forward=direction.lengthSqr()<1e-8?new Vec3(0,0,1):direction.normalize();
        Vec3 reference=Math.abs(forward.y)>.999?new Vec3(0,0,1):new Vec3(0,1,0);
        Vec3 right=reference.cross(forward).normalize();
        return new Frame(forward,right,forward.cross(right).normalize());
    }
    public static Vec3 spread(Vec3 direction,double degrees){
        var frame=frame(direction);double a=Math.toRadians(degrees);
        return frame.forward.scale(Math.cos(a)).add(frame.right.scale(Math.sin(a))).normalize();
    }
    public static Vec3 turn(Vec3 motion,Vec3 desired,double radians){
        double speed=motion.length();if(speed<1e-8)return desired.normalize();
        Vec3 from=motion.normalize(),to=desired.normalize();double angle=Math.acos(Math.max(-1,Math.min(1,from.dot(to))));
        if(angle<radians)return to.scale(speed);
        Vec3 tangent=to.subtract(from.scale(from.dot(to)));
        if(tangent.lengthSqr()<1e-8)tangent=frame(from).right;
        return from.scale(Math.cos(radians)).add(tangent.normalize().scale(Math.sin(radians))).scale(speed);
    }
    private SkillGeometry(){}
}
