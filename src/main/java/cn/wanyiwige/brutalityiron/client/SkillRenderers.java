package cn.wanyiwige.brutalityiron.client;

import cn.wanyiwige.brutalityiron.BrutalityIron;
import cn.wanyiwige.brutalityiron.skills.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mod.EventBusSubscriber(modid=BrutalityIron.ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class SkillRenderers {
    private static String key(Entity entity){return entity instanceof SkillVisualEntity v?v.model():((SkillMinion)entity).model();}
    private static class Model<T extends Entity & GeoEntity> extends GeoModel<T> {
        @Override public ResourceLocation getModelResource(T entity){return new ResourceLocation(SkillVisuals.get(key(entity)).model());}
        @Override public ResourceLocation getTextureResource(T entity){return new ResourceLocation(SkillVisuals.get(key(entity)).texture());}
        @Override public ResourceLocation getAnimationResource(T entity){return new ResourceLocation(SkillVisuals.get(key(entity)).animation());}
    }
    static class Renderer<T extends Entity & GeoEntity> extends GeoEntityRenderer<T> {
        private static final java.util.Set<ResourceLocation> REPORTED_MISSING=new java.util.HashSet<>();
        Renderer(EntityRendererProvider.Context context){super(context,new Model<>());shadowRadius=.1f;}
        @Override protected void applyRotations(T entity,PoseStack pose,float age,float yaw,float partial){
            if(entity instanceof SkillVisualEntity visual){
                if(visual.model().equals("black_hole")||visual.model().equals("cruel_sun"))return;
                SkillFxRenderer.orient(visual,partial,pose);
                if(visual.visualRole().equals("guardian_hand")){
                    pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
                    pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(visual.visualAge(partial)*6));
                }
            }else super.applyRotations(entity,pose,age,yaw,partial);
        }
        @Override public net.minecraft.client.renderer.RenderType getRenderType(T entity,ResourceLocation texture,MultiBufferSource source,float partial){return net.minecraft.client.renderer.RenderType.entityTranslucent(texture);}
        @Override public void render(T entity,float yaw,float partial,PoseStack stack,MultiBufferSource buffer,int light){
            if(key(entity).isBlank())return;
            var resources=SkillVisuals.get(key(entity));
            var model=new ResourceLocation(resources.model());var animation=new ResourceLocation(resources.animation());
            // Resource reloads or a broken override must not crash world rendering.
            // Check before GeckoLib changes the pose stack; do not swallow render errors.
            if(!software.bernie.geckolib.cache.GeckoLibCache.getBakedModels().containsKey(model)
                || !software.bernie.geckolib.cache.GeckoLibCache.getBakedAnimations().containsKey(animation)){
                if(REPORTED_MISSING.add(model))com.mojang.logging.LogUtils.getLogger().error("Brutality Iron skill visual unavailable in GeckoLib cache: model={}, animation={}",model,animation);
                return;
            }
            REPORTED_MISSING.remove(model);
            super.render(entity,yaw,partial,stack,buffer,net.minecraft.client.renderer.LightTexture.FULL_BRIGHT);
        }
    }
    @SubscribeEvent public static void register(EntityRenderersEvent.RegisterRenderers e){
        e.registerEntityRenderer(SkillContent.VISUAL.get(),SkillFxRenderer::new);
        e.registerEntityRenderer(SkillContent.MINION.get(),Renderer::new);
        e.registerEntityRenderer(SkillContent.STRAY.get(),StrayRenderer::new);
    }
    @SubscribeEvent public static void extraModels(ModelEvent.RegisterAdditional e){
        for(String school:new String[]{"nihility","celestia","inferno","plague"})e.register(new ResourceLocation(BrutalityIron.ID,"item/scroll_"+school));
    }
    private SkillRenderers(){}
}
