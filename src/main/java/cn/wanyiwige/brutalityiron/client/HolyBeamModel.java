package cn.wanyiwige.brutalityiron.client;

import cn.wanyiwige.brutalityiron.entity.celestia.HolyBeamEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HolyBeamModel
extends GeoModel<HolyBeamEntity> {
    public ResourceLocation getAnimationResource(HolyBeamEntity entity) {
        return new ResourceLocation("terramity", "animations/holy_beam.animation.json");
    }

    public ResourceLocation getModelResource(HolyBeamEntity entity) {
        return new ResourceLocation("terramity", "geo/holy_beam.geo.json");
    }

    public ResourceLocation getTextureResource(HolyBeamEntity entity) {
        return new ResourceLocation("terramity", "textures/entities/" + entity.getTexture() + ".png");
    }
}

