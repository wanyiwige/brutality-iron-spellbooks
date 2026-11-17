package cn.wanyiwige.brutalityiron.client;

import cn.wanyiwige.brutalityiron.SchoolBook;
import net.minecraft.resources.ResourceLocation;
import net.goo.brutality.client.renderers.layers.BrutalityAutoFullbrightLayer;
import net.goo.brutality.client.renderers.layers.BrutalityAutoFullbrightNoDepthLayer;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** Same original geo/UV/texture as Brutality; the GUI uses a separate baked model. */
public final class SchoolBookRenderer extends GeoItemRenderer<SchoolBook> {
    public SchoolBookRenderer(SchoolBook book) {
        super(new GeoModel<>() {
            public ResourceLocation getModelResource(SchoolBook item) {
                return new ResourceLocation("brutality", "geo/item/tome/" + item.definition.name() + "_handheld.geo.json");
            }
            public ResourceLocation getTextureResource(SchoolBook item) {
                return new ResourceLocation("brutality", "textures/item/tome/" + item.definition.name() + "/" + item.definition.name() + "_handheld.png");
            }
            public ResourceLocation getAnimationResource(SchoolBook item) {
                return new ResourceLocation("brutality", "animations/item/tome/" + item.definition.name() + "_handheld.animation.json");
            }
        });
        // Match upstream's actual layer registration; the plain Daemonic cover
        // has no glow layer, and Void alone uses the no-depth layer.
        if (book.definition.name().equals("void_tome"))
            addRenderLayer(new BrutalityAutoFullbrightNoDepthLayer<>(this));
        else if (!book.definition.name().equals("daemonic_tome"))
            addRenderLayer(new BrutalityAutoFullbrightLayer<>(this));
    }
}
