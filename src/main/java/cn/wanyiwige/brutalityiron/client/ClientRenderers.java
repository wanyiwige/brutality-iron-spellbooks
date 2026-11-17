package cn.wanyiwige.brutalityiron.client;

import cn.wanyiwige.brutalityiron.BrutalityIron;
import cn.wanyiwige.brutalityiron.PortEntities;
import net.goo.brutality.client.renderers.entity.*;
import net.goo.brutality.client.renderers.layers.BrutalityAutoFullbrightLayer;
import net.goo.brutality.client.renderers.layers.BrutalityAutoFullbrightNoDepthLayer;
import net.goo.brutality.client.renderers.layers.BrutalityAutoFullbrightAlphaLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BrutalityIron.ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientRenderers {
    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PortEntities.GRAVITIC_IMPLOSION_ENTITY.get(), context -> new BrutalityAbstractPhysicsProjectileRenderer<>(context, renderer -> renderer.addRenderLayer(new BrutalityAutoFullbrightNoDepthLayer<>(renderer))));
        event.registerEntityRenderer(PortEntities.COSMIC_CATACLYSM_ENTITY.get(), context -> new BrutalityAbstractPhysicsProjectileRenderer<>(context, renderer -> { renderer.addRenderLayer(new BrutalityAutoFullbrightNoDepthLayer<>(renderer)); renderer.addRenderLayer(new BrutalityAutoFullbrightLayer<>(renderer)); }));
        event.registerEntityRenderer(PortEntities.METEOR_SHOWER_ENTITY.get(), context -> new BrutalityAbstractPhysicsProjectileRenderer<>(context, renderer -> { renderer.addRenderLayer(new BrutalityAutoFullbrightNoDepthLayer<>(renderer)); renderer.addRenderLayer(new BrutalityAutoFullbrightLayer<>(renderer)); }));
        event.registerEntityRenderer(PortEntities.STAR_STREAM_ENTITY.get(), context -> new BrutalityAbstractPhysicsProjectileRenderer<>(context, renderer -> { renderer.addRenderLayer(new BrutalityAutoFullbrightNoDepthLayer<>(renderer)); renderer.addRenderLayer(new BrutalityAutoFullbrightLayer<>(renderer)); }));
        event.registerEntityRenderer(PortEntities.SINGULARITY_SHIFT_ENTITY.get(), context -> new BrutalityAbstractPhysicsProjectileRenderer<>(context, renderer -> { renderer.addRenderLayer(new BrutalityAutoFullbrightNoDepthLayer<>(renderer)); renderer.addRenderLayer(new BrutalityAutoFullbrightLayer<>(renderer)); }));
        event.registerEntityRenderer(PortEntities.CHTHONIC_CAPSULE_ENTITY.get(), BrutalityAbstractPhysicsProjectileRenderer::new);
        event.registerEntityRenderer(PortEntities.ANNIHILATION_ENTITY.get(), context -> new BrutalityAbstractPhysicsProjectileRenderer<>(context, renderer -> renderer.addRenderLayer(new BrutalityAutoFullbrightAlphaLayer<>(renderer))));
        event.registerEntityRenderer(PortEntities.BRIMSPIKE_ENTITY.get(), context -> new BrutalityTridentRenderer<>(context, renderer -> renderer.addRenderLayer(new BrutalityAutoFullbrightNoDepthLayer<>(renderer))));
        event.registerEntityRenderer(PortEntities.DESTRUCTION_ENTITY.get(), context -> new BrutalityEntityRenderer<>(context, renderer -> renderer.addRenderLayer(new BrutalityAutoFullbrightAlphaLayer<>(renderer))));
        event.registerEntityRenderer(PortEntities.EXTINCTION_ENTITY.get(), context -> new BrutalityRayRenderer<>(context, renderer -> renderer.addRenderLayer(new BrutalityAutoFullbrightLayer<>(renderer))));
        event.registerEntityRenderer(PortEntities.PIERCING_MOONLIGHT_ENTITY.get(), context -> new BrutalityRayRenderer<>(context, renderer -> renderer.addRenderLayer(new BrutalityAutoFullbrightLayer<>(renderer))));
        event.registerEntityRenderer(PortEntities.CRESCENT_DART_ENTITY.get(), context -> new BrutalityShurikenRenderer<>(context, renderer -> renderer.addRenderLayer(new BrutalityAutoFullbrightLayer<>(renderer))));
        event.registerEntityRenderer(PortEntities.CRESCENT_SCYTHE_ENTITY.get(), context -> new BrutalityTridentRenderer<>(context, renderer -> renderer.addRenderLayer(new BrutalityAutoFullbrightLayer<>(renderer))));
        event.registerEntityRenderer(PortEntities.LIGHT_BINDING.get(), context -> new BrutalityEntityRenderer<>(context, renderer -> renderer.addRenderLayer(new BrutalityAutoFullbrightLayer<>(renderer))));
        event.registerEntityRenderer(PortEntities.HOLY_BEAM.get(), HolyBeamRenderer::new);
    }
    private ClientRenderers() {}
}
