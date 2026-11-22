package cn.wanyiwige.brutalityiron;
import java.util.Map;
/** Level-one mana, per-level mana growth, cooldown seconds; benchmarked against TO 7.2.0. */
public final class SpellBudgets {
 public record Budget(int mana,int growth,int cooldown) {}
 private static final Map<String,Budget> VALUES=Map.ofEntries(
  Map.entry("annihilation",new Budget(110,25,40)),
  Map.entry("brimspike",new Budget(8,2,12)),
  Map.entry("chthonic_capsule",new Budget(30,5,8)),
  Map.entry("destruction",new Budget(55,8,15)),
  Map.entry("extinction",new Budget(5,2,20)),
  Map.entry("stygian_step",new Budget(25,3,8)),
  Map.entry("divine_retribution",new Budget(120,25,50)),
  Map.entry("heavenly_flight",new Budget(75,15,35)),
  Map.entry("holy_mantle",new Budget(100,30,60)),
  Map.entry("light_binding",new Budget(45,10,20)),
  Map.entry("sacred_oath",new Budget(100,30,60)),
  Map.entry("sacrifice",new Budget(100,20,40)),
  Map.entry("cosmic_cataclysm",new Budget(140,30,60)),
  Map.entry("gravitokinesis",new Budget(35,5,12)),
  Map.entry("meteor_shower",new Budget(10,3,25)),
  Map.entry("singularity_shift",new Budget(100,20,40)),
  Map.entry("star_burst",new Budget(30,5,6)),
  Map.entry("star_stream",new Budget(6,2,15)),
  Map.entry("crescent_dart",new Budget(5,2,13)),
  Map.entry("crescent_scythe",new Budget(35,6,10)),
  Map.entry("duo_lunaris",new Budget(140,35,90)),
  Map.entry("moonlit_mending",new Budget(100,15,60)),
  Map.entry("nightfall",new Budget(100,25,45)),
  Map.entry("piercing_moonlight",new Budget(40,7,10)),
  Map.entry("dematerialize",new Budget(100,35,40)),
  Map.entry("edge_of_oblivion",new Budget(35,5,10)),
  Map.entry("gravitic_implosion",new Budget(60,9,18)),
  Map.entry("intangible",new Budget(100,20,60)),
  Map.entry("spatial_rupture",new Budget(35,5,10)),
  Map.entry("void_walk",new Budget(25,4,6)));
 public static Budget get(String id){var b=VALUES.get(id);if(b==null)throw new IllegalArgumentException("Missing budget "+id);return b;}
 private SpellBudgets(){}
}
