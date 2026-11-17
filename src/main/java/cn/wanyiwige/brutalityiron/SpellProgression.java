package cn.wanyiwige.brutalityiron;

import com.google.gson.JsonParser;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Shipped defaults; Iron's server spell configuration can still override them. */
public final class SpellProgression {
    public record Entry(int maxLevel, SpellRarity minimumRarity) {}
    private static final Map<String, Entry> ENTRIES = load();
    private static Map<String, Entry> load() {
        try (var stream = SpellProgression.class.getResourceAsStream("/assets/brutality_iron/spell_progression.json")) {
            if (stream == null) throw new IllegalStateException("Missing spell progression defaults");
            var json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, Entry> entries = new LinkedHashMap<>();
            for (var pair : json.entrySet()) {
                var value = pair.getValue().getAsJsonObject();
                int max = value.get("max_level").getAsInt();
                if (max != 3 && max != 5 && max != 8 && max != 10) throw new IllegalArgumentException("Invalid max level: " + pair.getKey());
                entries.put(pair.getKey(), new Entry(max, SpellRarity.valueOf(value.get("min_rarity").getAsString())));
            }
            if (entries.size() != 30) throw new IllegalStateException("Expected 30 progression entries");
            return Map.copyOf(entries);
        } catch (java.io.IOException error) { throw new IllegalStateException(error); }
    }
    public static Entry get(String name) {
        var entry = ENTRIES.get(name);
        if (entry == null) throw new IllegalArgumentException("Missing spell progression: " + name);
        return entry;
    }
    private SpellProgression() {}
}
