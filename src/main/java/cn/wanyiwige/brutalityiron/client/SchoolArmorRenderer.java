package cn.wanyiwige.brutalityiron.client;
import cn.wanyiwige.brutalityiron.SchoolArmor;
import cn.wanyiwige.brutalityiron.BrutalityIron;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
public final class SchoolArmorRenderer extends AddedArmorAttachmentRenderer<SchoolArmor.Armor> {
    public SchoolArmorRenderer(){super(new Model());addRenderLayer(new software.bernie.geckolib.renderer.layer.GeoRenderLayer<SchoolArmor.Armor>(this){
        @Override public void render(com.mojang.blaze3d.vertex.PoseStack pose,SchoolArmor.Armor armor,software.bernie.geckolib.cache.object.BakedGeoModel model,net.minecraft.client.renderer.RenderType original,net.minecraft.client.renderer.MultiBufferSource buffers,com.mojang.blaze3d.vertex.VertexConsumer vertex,float partial,int light,int overlay){
            String family=armor.spec.family();if(!family.equals("blazeborne")&&!family.equals("seraph"))return;
            var texture=BrutalityIron.id("textures/armor/"+family+"_layer.png");var type=net.minecraft.client.renderer.RenderType.entityTranslucent(texture);
            getRenderer().reRender(model,pose,buffers,armor,type,buffers.getBuffer(type),partial,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,1,1,1,.5f);
        }
    });}
    private static final class Model extends GeoModel<SchoolArmor.Armor>{
        public ResourceLocation getModelResource(SchoolArmor.Armor a){return BrutalityIron.id("geo/armor/"+a.spec.family()+".geo.json");}
        public ResourceLocation getTextureResource(SchoolArmor.Armor a){return BrutalityIron.id("textures/armor/"+a.spec.family()+".png");}
        public ResourceLocation getAnimationResource(SchoolArmor.Armor a){return BrutalityIron.id("animations/armor/"+a.spec.family()+".animation.json");}
    }
}
