package cn.wanyiwige.brutalityiron;

import io.redspace.ironsspellbooks.item.UpgradeOrbItem;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

/** Native Iron rune items and datapack-backed equipment upgrade orbs. */
public final class SchoolMaterials {
    private static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,BrutalityIron.ID);
    public static final Map<String,RegistryObject<Item>> RUNES=new LinkedHashMap<>();
    public static final Map<String,RegistryObject<Item>> ORBS=new LinkedHashMap<>();
    public static ResourceKey<UpgradeOrbType> type(String school){return ResourceKey.create(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY,BrutalityIron.id(school+"_power"));}
    static {for(String school:new String[]{"inferno","celestia","nihility"}){
        RUNES.put(school,ITEMS.register(school+"_rune",()->new Item(new Item.Properties())));
        ORBS.put(school,ITEMS.register(school+"_upgrade_orb",()->new UpgradeOrbItem(new Item.Properties().rarity(Rarity.UNCOMMON),type(school))));
    }}
    public static void register(IEventBus bus){ITEMS.register(bus);}
    private SchoolMaterials(){}
}
