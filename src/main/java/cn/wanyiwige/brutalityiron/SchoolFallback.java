package cn.wanyiwige.brutalityiron;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

/** Choose the installed school per launch, leaving other schools unchanged. */
public final class SchoolFallback {
    public static ResourceLocation resolve(ResourceLocation school) {
        String id = school.toString();
        String fallback = switch (id) {
            case "alex_caves_spellsbook:hazen_cosmic", "alex_caves_spellsbook:hazen_shadow" -> "irons_spellbooks:ender";
            case "wind_spellbooks:wind", "gtbcs_geomancy_plus:geo" -> "irons_spellbooks:evocation";
            default -> null;
        };
        return fallback != null && !ModList.get().isLoaded(school.getNamespace()) ? new ResourceLocation(fallback) : school;
    }
    public static boolean usingFallback(ResourceLocation school) { return !resolve(school).equals(school); }
    private SchoolFallback() {}
}
