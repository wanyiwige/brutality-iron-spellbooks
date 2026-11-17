package cn.wanyiwige.brutalityiron;

import io.redspace.ironsspellbooks.api.attribute.MagicPercentAttribute;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import java.util.LinkedHashMap;
import java.util.Map;
import net.goo.brutality.magic.IBrutalitySpell.MagicSchool;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.*;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public final class Content {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, BrutalityIron.ID);
    private static final DeferredRegister<SchoolType> SCHOOLS = DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, BrutalityIron.ID);
    private static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, BrutalityIron.ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BrutalityIron.ID);
    public static final Map<String, RegistryObject<Attribute>> ATTRIBUTE_MAP = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<SchoolType>> SCHOOL_MAP = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<AbstractSpell>> SPELL_MAP = new LinkedHashMap<>();

    public static final RegistryObject<AbstractSpell> CELESTIAL_GUARD = SPELLS.register("celestial_guard", CelestialGuardSpell::new);

    static {
        addSchool("nihility", 0xA8B7D3);
        addSchool("celestia", 0xFFE862);
        addSchool("inferno", 0xFF5A3F);
        addSchool("plague", 0xA6DB48);
        for (PortedLogic logic : SpellCatalog.create()) {
            SPELL_MAP.put(logic.getSpellName(), SPELLS.register(logic.getSpellName(), () -> new NativeSpell(logic)));
        }
        TABS.register("spells", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.brutality_iron.spells"))
                .icon(() -> scroll("void_walk", 1))
                .displayItems((parameters, output) -> {
                    BookContent.BOOKS.values().forEach(book -> output.accept(book.get().getDefaultInstance()));
                    SchoolArmor.ITEMS.values().forEach(item -> output.accept(item.get().getDefaultInstance()));
                    SchoolMaterials.RUNES.values().forEach(item -> output.accept(item.get().getDefaultInstance()));
                    SchoolMaterials.ORBS.values().forEach(item -> output.accept(item.get().getDefaultInstance()));
                    for (var school : new String[]{"nihility", "celestia", "inferno"}) {
                        String material = switch (school) {
                            case "nihility" -> "empty_essence";
                            case "celestia" -> "angel_feather";
                            default -> "chthonic_crystal";
                        };
                        output.accept(ForgeRegistries.ITEMS.getValue(new ResourceLocation("terramity", material)));
                    }
                    for (String name : SPELL_MAP.keySet()) {
                        for (int rank = 1; rank <= spell(name).getMaxLevel(); rank++) output.accept(scroll(name, rank));
                    }
                    for (int rank=1;rank<=5;rank++) { ItemStack guardScroll = new ItemStack(ItemRegistry.SCROLL.get()); ISpellContainer.createScrollContainer(CELESTIAL_GUARD.get(),rank,guardScroll); output.accept(guardScroll); }
                    cn.wanyiwige.brutalityiron.skills.SkillContent.SKILLS.forEach((name,registered)->{
                        for(int rank=1;rank<=registered.get().getMaxLevel();rank++)output.accept(cn.wanyiwige.brutalityiron.skills.SkillContent.scroll(name,rank));
                    });
                }).build());
    }

    private static void addSchool(String name, int color) {
        var power = ATTRIBUTES.register(name + "_spell_power", () -> new MagicPercentAttribute(
                "attribute.brutality_iron." + name + "_spell_power", 1, -100, 100).setSyncable(true));
        var resistance = ATTRIBUTES.register(name + "_magic_resist", () -> new MagicPercentAttribute(
                "attribute.brutality_iron." + name + "_magic_resist", 1, -100, 100).setSyncable(true));
        ATTRIBUTE_MAP.put(name + "_spell_power", power);
        ATTRIBUTE_MAP.put(name + "_magic_resist", resistance);
        SCHOOL_MAP.put(name, SCHOOLS.register(name, () -> new SchoolType(BrutalityIron.id(name),
                TagKey.create(Registries.ITEM, BrutalityIron.id(name + "_focus")),
                Component.translatable("school.brutality_iron." + name).withStyle(style -> style.withColor(color)),
                power, resistance, switch (name) {
                    case "inferno" -> SpellSounds.SCHOOL_INFERNO;
                    case "plague" -> SpellSounds.SCHOOL_PLAGUE;
                    case "celestia" -> SpellSounds.SCHOOL_CELESTIA;
                    default -> SoundRegistry.EVOCATION_CAST;
                },
                ResourceKey.create(Registries.DAMAGE_TYPE, BrutalityIron.id(name + "_magic")))));
    }

    public static ResourceLocation school(MagicSchool school) {
        return SchoolFallback.resolve(switch (school) {
            case VOIDWALKER -> BrutalityIron.id("nihility");
            case CELESTIA -> BrutalityIron.id("celestia");
            case BRIMWIELDER -> BrutalityIron.id("inferno");
            case COSMIC -> new ResourceLocation("alex_caves_spellsbook", "hazen_cosmic");
            case UMBRANCY -> new ResourceLocation("alex_caves_spellsbook", "hazen_shadow");
            default -> throw new IllegalArgumentException("Unsupported school " + school);
        });
    }

    public static NativeSpell spell(String name) {
        return (NativeSpell) SPELL_MAP.get(name).get();
    }

    public static ItemStack scroll(String name, int rank) {
        ItemStack stack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spell(name), rank, stack);
        return stack;
    }

    public static void register(IEventBus bus) {
        ATTRIBUTES.register(bus);
        SCHOOLS.register(bus);
        SPELLS.register(bus);
        TABS.register(bus);
        bus.addListener((EntityAttributeModificationEvent event) -> event.getTypes().forEach(type ->
                ATTRIBUTE_MAP.values().forEach(attribute -> event.add(type, attribute.get()))));
    }

    private Content() {}
}
