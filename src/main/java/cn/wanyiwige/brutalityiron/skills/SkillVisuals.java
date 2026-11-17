package cn.wanyiwige.brutalityiron.skills;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.resources.ResourceLocation;

public final class SkillVisuals {
    public record Entry(String model,String texture,String animation,String clip){}
    private static final Map<String,Entry> ENTRIES=load();
    private static Map<String,Entry> load(){
        try(var in=SkillVisuals.class.getResourceAsStream("/assets/brutality_iron/skill_visuals.json")){
            if(in==null)throw new IllegalStateException("Missing source model catalogue");
            var object=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();var map=new HashMap<String,Entry>();
            for(var e:object.entrySet())map.put(e.getKey(),new Gson().fromJson(e.getValue(),Entry.class));return Map.copyOf(map);
        }catch(java.io.IOException e){throw new IllegalStateException(e);}
    }
    public static Entry get(String key){return ENTRIES.getOrDefault(key,ENTRIES.get("star"));}
    public static String clip(String model){return model.isBlank()?"":get(model).clip;}
    private SkillVisuals(){}
}
