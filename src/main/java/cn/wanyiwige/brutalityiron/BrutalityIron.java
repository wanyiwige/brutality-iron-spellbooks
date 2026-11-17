package cn.wanyiwige.brutalityiron;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BrutalityIron.ID)
public final class BrutalityIron {
    public static final String ID = "brutality_iron";

    public BrutalityIron() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        SpellSounds.register(bus);
        Content.register(bus);
        CelestialGuard.register(bus);
        BookContent.register(bus);
        SchoolMaterials.register(bus);
        SchoolArmor.register(bus);
        SchoolArmorEffects.register(bus);
        PortEntities.register(bus);
        cn.wanyiwige.brutalityiron.skills.SkillContent.register(bus);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }
}
