package cn.wanyiwige.brutalityiron.skills;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public record SkillDefinition(String name, String id, String school, int max_level,
        String min_rarity, int mana, int cooldown, float damage, int duration,
        int cast_time, String visual, String source_mod, String description) {
    public static final Map<String,SkillDefinition> ALL=load();
    private static Map<String,SkillDefinition> load() {
        try(var in=SkillDefinition.class.getResourceAsStream("/assets/brutality_iron/skill_expansion.json")) {
            if(in==null)throw new IllegalStateException("Missing reviewed skill definitions");
            var result=new LinkedHashMap<String,SkillDefinition>();
            for(var d:new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),SkillDefinition[].class)) {
                if(result.put(d.id,d)!=null || !Set.of(3,5,8,10).contains(d.max_level))throw new IllegalStateException("Invalid skill "+d.id);
            }
            if(result.size()!=23)throw new IllegalStateException("Expected 23 retained reviewed skills");
            return Collections.unmodifiableMap(result);
        }catch(java.io.IOException e){throw new IllegalStateException(e);}
    }
}
