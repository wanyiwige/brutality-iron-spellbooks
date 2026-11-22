package cn.wanyiwige.brutalityiron;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
/** Explicit level-one and maximum-level endpoints for the original thirty spells. */
public final class NativeBalance {
 private static final JsonObject DATA=load();
 private static JsonObject load(){try(var in=NativeBalance.class.getResourceAsStream("/assets/brutality_iron/native_balance.json")){
  if(in==null)throw new IllegalStateException("Missing native balance");
  var data=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
  if(data.size()!=30)throw new IllegalStateException("Expected thirty native balance entries");return data;
 }catch(IOException e){throw new IllegalStateException(e);}}
 public static float progress(String id,int rank){return Math.max(0,Math.min(1,(rank-1f)/(SpellProgression.get(id).maxLevel()-1)));}
 public static float interpolate(String id,int rank,float first,float last){return first+(last-first)*progress(id,rank);}
 private static float endpoint(String id,int rank,JsonArray values){return interpolate(id,rank,values.get(0).getAsFloat(),values.get(1).getAsFloat());}
 public static float damage(String id,int rank){return endpoint(id,rank,DATA.getAsJsonObject(id).getAsJsonArray("damage"));}
 public static float stat(String id,String type,int rank,float fallback){var stats=DATA.getAsJsonObject(id).getAsJsonObject("stats");return stats.has(type)?endpoint(id,rank,stats.getAsJsonArray(type)):fallback;}
 private NativeBalance(){}
}
