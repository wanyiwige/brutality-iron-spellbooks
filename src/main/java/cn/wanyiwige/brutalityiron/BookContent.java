package cn.wanyiwige.brutalityiron;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.*;

public final class BookContent {
    public record Definition(String name, String school, int slots, int mana, double power,
                             String extraAttribute, double extraValue, String powerAttribute) {
        public SchoolType schoolType() { return SchoolRegistry.getSchool(SchoolFallback.resolve(new ResourceLocation(school))); }
        public boolean available() {
            if (name.equals("daemonic_tome")) {
                if (!ModList.get().isLoaded("bielgg_spells") || !ModList.get().isLoaded("roaring")) return false;
                // The installed Bielgg build can disable its optional school content.
                try {
                    return (boolean)Class.forName("com.example.chaotic_world_content.compat.ContentAvailability")
                            .getMethod("roaringLoaded").invoke(null);
                } catch (ReflectiveOperationException ignored) { return true; }
            }
            return !name.equals("darkist_tome") || ModList.get().isLoaded("echo_spellbooks");
        }
    }
    public static final List<Definition> DEFINITIONS = List.of(
        new Definition("daemonic_tome", "bielgg_spells:shadow", 10, 200, .20, "cast_time_reduction", .15, "bielgg_spells:shadow_spell_power"),
        new Definition("darkist_tome", "echo_spellbooks:echo", 10, 220, .20, "casting_movespeed", .20, "echo_spellbooks:echo_spell_power"),
        new Definition("cosmic_tome", "alex_caves_spellsbook:hazen_cosmic", 12, 300, .15, "mana_regen", .10, "alex_caves_spellsbook:hazen_cosmic_spell_power"),
        new Definition("void_tome", "brutality_iron:nihility", 8, 160, .25, "cooldown_reduction", .15, "brutality_iron:nihility_spell_power"),
        new Definition("brimwielder_tome", "brutality_iron:inferno", 10, 200, .25, "mana_regen", .15, "brutality_iron:inferno_spell_power"),
        new Definition("celestia_tome", "brutality_iron:celestia", 12, 240, .20, "spell_resist", .10, "brutality_iron:celestia_spell_power")
    );
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BrutalityIron.ID);
    public static final Map<String, RegistryObject<Item>> BOOKS = new LinkedHashMap<>();
    static {
        for (var definition : DEFINITIONS) if (definition.available())
            BOOKS.put(definition.name(), ITEMS.register(definition.name(), () -> new SchoolBook(definition)));
    }
    public static void register(IEventBus bus) { ITEMS.register(bus); }
    private BookContent() {}
}
