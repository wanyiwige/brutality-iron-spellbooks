package cn.wanyiwige.brutalityiron.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cn.wanyiwige.brutalityiron.entity.celestia.HolyBeamEntity;
import cn.wanyiwige.brutalityiron.client.HolyBeamLayer;
import cn.wanyiwige.brutalityiron.client.HolyBeamModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;

public class HolyBeamRenderer
extends GeoEntityRenderer<HolyBeamEntity> {
    public HolyBeamRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, (GeoModel)new HolyBeamModel());
        this.shadowRadius = 0.0f;
        this.addRenderLayer(new HolyBeamLayer((GeoRenderer<HolyBeamEntity>)this));
    }

    public RenderType getRenderType(HolyBeamEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent((ResourceLocation)this.getTextureLocation(animatable));
    }

    public void preRender(PoseStack poseStack, HolyBeamEntity entity, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        float scale;
        this.scaleHeight = scale = 1.0f;
        this.scaleWidth = scale;
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    protected float getDeathMaxRotation(HolyBeamEntity entityLivingBaseIn) {
        return 0.0f;
    }
}

