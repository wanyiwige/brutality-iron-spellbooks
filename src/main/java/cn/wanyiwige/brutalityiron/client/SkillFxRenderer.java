package cn.wanyiwige.brutalityiron.client;

import cn.wanyiwige.brutalityiron.skills.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.registries.ForgeRegistries;

/** Pixel textures rendered as explicit planes; weapons have exactly one layer. */
public final class SkillFxRenderer extends EntityRenderer<SkillVisualEntity> {
    private final SkillRenderers.Renderer<SkillVisualEntity> source,blackHole,sun;
    public SkillFxRenderer(EntityRendererProvider.Context context){
        super(context);source=new SkillRenderers.Renderer<>(context);
        blackHole=new SkillRenderers.Renderer<>(context);sun=new SkillRenderers.Renderer<>(context);
        blackHole.addRenderLayer(new net.goo.brutality.client.renderers.layers.BrutalityAutoFullbrightNoDepthLayer<>(blackHole));
        sun.addRenderLayer(new net.goo.brutality.client.renderers.layers.BrutalityAutoFullbrightNoDepthLayer<>(sun));
        sun.addRenderLayer(new net.goo.brutality.client.renderers.layers.BrutalityAutoFullbrightAlphaLayer<>(sun));
    }
    private static ResourceLocation texture(String name){return new ResourceLocation("brutality_iron","textures/revision/"+name+".png");}
    @Override public ResourceLocation getTextureLocation(SkillVisualEntity e){return e.model().startsWith("fx:")?texture(e.model().substring(3)):new ResourceLocation(SkillVisuals.get(e.model()).texture());}
    @Override public boolean shouldRender(SkillVisualEntity e,net.minecraft.client.renderer.culling.Frustum frustum,double x,double y,double z){return true;}
    @Override public void render(SkillVisualEntity e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
        String model=e.model(),role=e.visualRole();if(model.isBlank())return;
        if(model.startsWith("celestial_guard_") && Minecraft.getInstance().options.getCameraType().isFirstPerson() && Minecraft.getInstance().player!=null && e.visualOwnerId()==Minecraft.getInstance().player.getId())return;
        if(!model.startsWith("fx:")){
            pose.pushPose();
            if(role.equals("descending_beam"))pose.translate(0,Math.max(0,1-e.visualAge(partial)/8)*6,0);
            if(role.equals("charging_display")&&e.skill.equals("spirit_bomb")){
                float progress=Mth.clamp(e.visualAge(partial)/Math.max(1,e.visualLife()-10),0,1);
                float scale=.08f+.92f*progress;pose.scale(scale,scale,scale);
            }
            (model.equals("black_hole")?blackHole:model.equals("cruel_sun")?sun:source).render(e,yaw,partial,pose,buffers,light);
            pose.popPose();
            if(e.skill.equals("stygian_souls"))soulTrail(e,pose,buffers);
            if(role.equals("guardian_hand")&&(int)e.visualAge(partial)%10<3){pose.pushPose();orient(e,partial,pose);beam(pose,buffers,"column_holy",e.visualRadius(),.4f,.6f);pose.popPose();}
            return;
        }
        String id=model.substring(3);float age=e.visualAge(partial),radius=e.visualRadius();
        float fade=Math.min(1,Math.max(0,(e.visualLife()-age)/5));
        pose.pushPose();
        if(role.equals("healing")||role.equals("growth")){
            pose.translate(0,.035,0);circle(pose,buffers,id,radius,age,fade);
            if(role.equals("healing"))crystals(pose,buffers,partial,light);
        }else if(role.equals("impact")||role.equals("warmup")){
            pose.translate(0,.04,0);pose.mulPose(Axis.XP.rotationDegrees(90));
            float growth=role.equals("warmup")?Math.min(1,age/Math.max(1,e.visualLife())):Math.min(1,age/6);
            quad(pose,buffers,texture(id),radius*2*growth,radius*2*growth,0,1,fade*(role.equals("warmup")?.45f:1));
        }else if(role.equals("plague_cloud")||role.equals("puff")){
            int clouds=role.equals("puff")?3:28;
            for(int i=0;i<clouds;i++){
                pose.pushPose();double a=i*2.39996,d=role.equals("puff")?.15:radius*Math.sqrt((i+.5)/clouds);
                float cycle=(age+i*3)%24,cloudFade=role.equals("puff")?1:Math.min(1,Math.min(cycle/5,(24-cycle)/5));
                double height=role.equals("puff")?.35:.75+(i%5)*.55+.18*Math.sin(a+age*.04);
                pose.translate(Math.cos(a)*d,height,Math.sin(a)*d);
                pose.mulPose(entityRenderDispatcher.cameraOrientation());
                int frame=((int)(age/2)+i*3)%10;float size=role.equals("puff")?radius:1.0f+(i%3)*.22f;
                quad(pose,buffers,texture("plague_cloud_frames"),size,size,frame/10f,(frame+1)/10f,fade*.8f*cloudFade);
                pose.popPose();
            }
        }else if(role.equals("pillar_warning")){
            float revealAge=age-(float)e.speed+12;
            if(revealAge>=0)circle(pose,buffers,id,radius,revealAge*4,fade);
        }else if(role.equals("ray_warning")||role.equals("ray")){
            orient(e,partial,pose);
            // The casting disc is normal to the locked forward direction.
            String circle=id.equals("column_holy")?"circle_crystal":"circle_eldritch";
            int frame=Math.min(31,(int)age/2);quad(pose,buffers,texture(circle+"_frames"),1.5f,1.5f,frame/32f,(frame+1)/32f,fade);
            if(role.equals("ray"))beam(pose,buffers,id,radius,.8f,fade);
        }else{
            orient(e,partial,pose);
            if(id.equals("forbidden_scythe")||id.equals("devils_scythe")){
                // The sprite lies in the right/forward plane, not the view plane.
                pose.mulPose(Axis.XP.rotationDegrees(90));
                pose.mulPose(Axis.ZP.rotationDegrees(age*27));quad(pose,buffers,texture(id),1.2f,2.4f,0,1,fade);
            }else if(id.equals("exoblade_beam")||id.equals("devils_pitchfork")||id.startsWith("starlight_stab_")){
                if(id.equals("exoblade_beam"))pose.mulPose(Axis.YP.rotationDegrees(-45));
                pose.mulPose(Axis.XP.rotationDegrees(90));
                quad(pose,buffers,texture(id),id.equals("exoblade_beam")?1.8f:1.2f,id.equals("exoblade_beam")?1.8f:2.4f,0,1,fade);
            }else{
                // Crescent bulge (+sprite X) points forward (+local Z), like a sword wave.
                if(id.equals("plague_wave"))pose.mulPose(Axis.YP.rotationDegrees(-90));
                if(role.equals("claws"))pose.mulPose(Axis.ZP.rotationDegrees((e.getId()%3-1)*8));
                float w=id.equals("plague_wave")?2.0f:id.equals("plague_claws")?3.0f:1.7f;
                float h=id.equals("plague_claws")?3:3.2f;
                quad(pose,buffers,texture(id),w,h,0,1,fade);
            }
        }
        pose.popPose();
    }
    static void orient(SkillVisualEntity entity,float partial,PoseStack pose){
        pose.mulPose(Axis.YP.rotationDegrees(-Mth.rotLerp(partial,entity.yRotO,entity.getYRot())));
        pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partial,entity.xRotO,entity.getXRot())));
    }
    private void soulTrail(SkillVisualEntity e,PoseStack pose,MultiBufferSource buffers){
        if(e.trail.size()<2)return;
        var vertices=buffers.getBuffer(RenderType.lightning());var points=e.trail.toArray(net.minecraft.world.phys.Vec3[]::new);
        var side=new org.joml.Vector3f(1,0,0).rotate(entityRenderDispatcher.cameraOrientation());
        for(int i=1;i<points.length;i++){
            float fraction=i/(float)points.length,width=.055f*fraction,alpha=.45f*fraction;
            var a=points[i-1].subtract(e.position());var b=points[i].subtract(e.position());
            trailVertex(vertices,pose,(float)a.x-side.x*width,(float)a.y-side.y*width,(float)a.z-side.z*width,alpha);
            trailVertex(vertices,pose,(float)a.x+side.x*width,(float)a.y+side.y*width,(float)a.z+side.z*width,alpha);
            trailVertex(vertices,pose,(float)b.x+side.x*width,(float)b.y+side.y*width,(float)b.z+side.z*width,alpha);
            trailVertex(vertices,pose,(float)b.x-side.x*width,(float)b.y-side.y*width,(float)b.z-side.z*width,alpha);
        }
    }
    private static void trailVertex(VertexConsumer out,PoseStack pose,float x,float y,float z,float alpha){out.vertex(pose.last().pose(),x,y,z).color(.65f,.12f,.52f,alpha).endVertex();}
    private static void circle(PoseStack pose,MultiBufferSource buffers,String id,float radius,float age,float alpha){
        pose.pushPose();pose.mulPose(Axis.XP.rotationDegrees(90));
        int frame=Math.min(31,(int)age/2);quad(pose,buffers,texture(id+"_frames"),radius*2,radius*2,frame/32f,(frame+1)/32f,alpha);pose.popPose();
    }
    private static void crystals(PoseStack pose,MultiBufferSource buffers,float partial,int light){
        var blocks=Minecraft.getInstance().getBlockRenderer();
        var base=ForgeRegistries.BLOCKS.getValue(new ResourceLocation("terramity","prismatic_crystal_block"));
        var cluster=ForgeRegistries.BLOCKS.getValue(new ResourceLocation("terramity","prismatic_crystal_cluster"));
        if(base==null||cluster==null)return;
        pose.pushPose();pose.translate(-.35,0,-.35);pose.scale(.7f,.45f,.7f);blocks.renderSingleBlock(base.defaultBlockState(),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
        for(int i=0;i<5;i++){
            pose.pushPose();double a=i*Math.PI*.5;double d=i==4?0:.55;
            pose.translate(Math.cos(a)*d-.5,i==4?.45:.02,Math.sin(a)*d-.5);
            blocks.renderSingleBlock(cluster.defaultBlockState(),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
        }
    }
    private static void beam(PoseStack pose,MultiBufferSource buffers,String id,float length,float width,float alpha){
        // Tile 16x64 textures in 4-block segments; avoid stretching one pixel
        // along the entire spell range. Crossed planes are used only for beams.
        for(float at=0;at<length;at+=4){float segment=Math.min(4,length-at);
            for(int side=0;side<2;side++){
                pose.pushPose();pose.mulPose(Axis.ZP.rotationDegrees(side*90));pose.translate(0,0,at+segment*.5);
                pose.mulPose(Axis.XP.rotationDegrees(90));quad(pose,buffers,texture(id),width,segment,0,segment/4,alpha);pose.popPose();
            }
        }
    }
    private static void quad(PoseStack pose,MultiBufferSource buffers,ResourceLocation texture,float width,float height,float v0,float v1,float alpha){
        var vertices=buffers.getBuffer(RenderType.entityTranslucent(texture));var p=pose.last();
        vertex(vertices,p,-width/2,-height/2,0,0,v1,alpha);vertex(vertices,p,width/2,-height/2,0,1,v1,alpha);
        vertex(vertices,p,width/2,height/2,0,1,v0,alpha);vertex(vertices,p,-width/2,height/2,0,0,v0,alpha);
    }
    private static void vertex(VertexConsumer out,PoseStack.Pose p,float x,float y,float z,float u,float v,float alpha){out.vertex(p.pose(),x,y,z).color(1f,1f,1f,alpha).uv(u,v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(p.normal(),0,0,1).endVertex();}
}
