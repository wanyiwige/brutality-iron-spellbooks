package cn.wanyiwige.brutalityiron;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.*;
import net.minecraftforge.eventbus.api.IEventBus;
import cn.wanyiwige.brutalityiron.entity.brimwielder.AnnihilationEntity;
import cn.wanyiwige.brutalityiron.entity.brimwielder.BrimspikeEntity;
import cn.wanyiwige.brutalityiron.entity.brimwielder.ChthonicCapsuleEntity;
import cn.wanyiwige.brutalityiron.entity.brimwielder.DestructionEntity;
import cn.wanyiwige.brutalityiron.entity.brimwielder.ExtinctionEntity;
import cn.wanyiwige.brutalityiron.entity.celestia.LightBinding;
import cn.wanyiwige.brutalityiron.entity.cosmic.CosmicCataclysmEntity;
import cn.wanyiwige.brutalityiron.entity.cosmic.MeteorShowerEntity;
import cn.wanyiwige.brutalityiron.entity.cosmic.SingularityShiftEntity;
import cn.wanyiwige.brutalityiron.entity.cosmic.StarStreamEntity;
import cn.wanyiwige.brutalityiron.entity.umbrancy.CrescentDart;
import cn.wanyiwige.brutalityiron.entity.umbrancy.CrescentScythe;
import cn.wanyiwige.brutalityiron.entity.umbrancy.PiercingMoonlight;
import cn.wanyiwige.brutalityiron.entity.voidwalker.GraviticImplosionEntity;
import cn.wanyiwige.brutalityiron.entity.celestia.HolyBeamEntity;
public final class PortEntities {
    private static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BrutalityIron.ID);
    public static final RegistryObject<EntityType<GraviticImplosionEntity>> GRAVITIC_IMPLOSION_ENTITY = REGISTRY.register("gravitic_implosion", () -> EntityType.Builder.<GraviticImplosionEntity>of(GraviticImplosionEntity::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(96).updateInterval(1).build("brutality_iron:gravitic_implosion"));
    public static final RegistryObject<EntityType<CosmicCataclysmEntity>> COSMIC_CATACLYSM_ENTITY = REGISTRY.register("cosmic_cataclysm", () -> EntityType.Builder.<CosmicCataclysmEntity>of(CosmicCataclysmEntity::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).clientTrackingRange(96).updateInterval(1).build("brutality_iron:cosmic_cataclysm"));
    public static final RegistryObject<EntityType<MeteorShowerEntity>> METEOR_SHOWER_ENTITY = REGISTRY.register("meteor_shower", () -> EntityType.Builder.<MeteorShowerEntity>of(MeteorShowerEntity::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).clientTrackingRange(96).updateInterval(1).build("brutality_iron:meteor_shower"));
    public static final RegistryObject<EntityType<StarStreamEntity>> STAR_STREAM_ENTITY = REGISTRY.register("star_stream", () -> EntityType.Builder.<StarStreamEntity>of(StarStreamEntity::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(96).updateInterval(1).build("brutality_iron:star_stream"));
    public static final RegistryObject<EntityType<SingularityShiftEntity>> SINGULARITY_SHIFT_ENTITY = REGISTRY.register("singularity_shift", () -> EntityType.Builder.<SingularityShiftEntity>of(SingularityShiftEntity::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(96).updateInterval(1).build("brutality_iron:singularity_shift"));
    public static final RegistryObject<EntityType<AnnihilationEntity>> ANNIHILATION_ENTITY = REGISTRY.register("annihilation", () -> EntityType.Builder.<AnnihilationEntity>of(AnnihilationEntity::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(96).updateInterval(1).build("brutality_iron:annihilation"));
    public static final RegistryObject<EntityType<ChthonicCapsuleEntity>> CHTHONIC_CAPSULE_ENTITY = REGISTRY.register("chthonic_capsule", () -> EntityType.Builder.<ChthonicCapsuleEntity>of(ChthonicCapsuleEntity::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(96).updateInterval(1).build("brutality_iron:chthonic_capsule"));
    public static final RegistryObject<EntityType<DestructionEntity>> DESTRUCTION_ENTITY = REGISTRY.register("destruction", () -> EntityType.Builder.<DestructionEntity>of(DestructionEntity::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(96).updateInterval(1).build("brutality_iron:destruction"));
    public static final RegistryObject<EntityType<ExtinctionEntity>> EXTINCTION_ENTITY = REGISTRY.register("extinction", () -> EntityType.Builder.<ExtinctionEntity>of(ExtinctionEntity::new, (MobCategory)MobCategory.MISC).sized(0.0f, 0.0f).clientTrackingRange(96).updateInterval(1).build("brutality_iron:extinction"));
    public static final RegistryObject<EntityType<PiercingMoonlight>> PIERCING_MOONLIGHT_ENTITY = REGISTRY.register("piercing_moonlight", () -> EntityType.Builder.<PiercingMoonlight>of(PiercingMoonlight::new, (MobCategory)MobCategory.MISC).sized(0.0f, 0.0f).clientTrackingRange(96).updateInterval(1).build("brutality_iron:piercing_moonlight"));
    public static final RegistryObject<EntityType<CrescentDart>> CRESCENT_DART_ENTITY = REGISTRY.register("crescent_dart", () -> EntityType.Builder.<CrescentDart>of(CrescentDart::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.0625f).clientTrackingRange(96).updateInterval(1).build("brutality_iron:crescent_dart"));
    public static final RegistryObject<EntityType<BrimspikeEntity>> BRIMSPIKE_ENTITY = REGISTRY.register("brimspike", () -> EntityType.Builder.<BrimspikeEntity>of(BrimspikeEntity::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(96).updateInterval(1).build("brutality_iron:brimspike"));
    public static final RegistryObject<EntityType<CrescentScythe>> CRESCENT_SCYTHE_ENTITY = REGISTRY.register("crescent_scythe", () -> EntityType.Builder.<CrescentScythe>of(CrescentScythe::new, (MobCategory)MobCategory.MISC).sized(3.0f, 0.0625f).clientTrackingRange(96).updateInterval(1).build("brutality_iron:crescent_scythe"));
    public static final RegistryObject<EntityType<LightBinding>> LIGHT_BINDING = REGISTRY.register("light_binding", () -> EntityType.Builder.<LightBinding>of(LightBinding::new, (MobCategory)MobCategory.MISC).sized(0.0f, 0.0f).clientTrackingRange(96).updateInterval(1).build("brutality_iron:light_binding"));
    public static final RegistryObject<EntityType<HolyBeamEntity>> HOLY_BEAM = REGISTRY.register("holy_beam", () -> EntityType.Builder.<HolyBeamEntity>of(HolyBeamEntity::new, MobCategory.MISC).sized(1, 1).clientTrackingRange(96).updateInterval(1).build("brutality_iron:holy_beam"));
    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
        bus.addListener((net.minecraftforge.event.entity.EntityAttributeCreationEvent event) -> event.put(HOLY_BEAM.get(), HolyBeamEntity.createAttributes().build()));
    }
    private PortEntities() {}
}
