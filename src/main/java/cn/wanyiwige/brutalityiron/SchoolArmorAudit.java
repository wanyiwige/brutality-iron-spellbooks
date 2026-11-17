package cn.wanyiwige.brutalityiron;
import com.google.gson.JsonParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
public final class SchoolArmorAudit {
    public static void run(ServerLevel level){
        require(SchoolArmor.SPECS.size()==24,"24 items");var player=FakePlayerFactory.getMinecraft(level);int n=0;
        for(var spec:SchoolArmor.SPECS){
            var armor=(SchoolArmor.Armor)SchoolArmor.ITEMS.get(spec.id()).get();
            var slot=ArmorItem.Type.valueOf(spec.slot()).getSlot();var attr=Content.ATTRIBUTE_MAP.get(spec.school()+"_spell_power").get();
            require(armor.getDefaultAttributeModifiers(slot).containsKey(attr),"school power "+spec.id());
            require(!armor.getDefaultAttributeModifiers(slot==net.minecraft.world.entity.EquipmentSlot.HEAD?net.minecraft.world.entity.EquipmentSlot.CHEST:net.minecraft.world.entity.EquipmentSlot.HEAD).containsKey(attr),"slot power "+spec.id());
            var recipe=(CraftingRecipe)level.getRecipeManager().byKey(BrutalityIron.id(spec.id())).orElseThrow();
            com.google.gson.JsonObject json;try(var stream=SchoolArmorAudit.class.getResourceAsStream("/data/brutality_iron/recipes/"+spec.id()+".json")){json=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();}catch(Exception e){throw new IllegalStateException(e);}
            var grid=new TransientCraftingContainer(new CraftingMenu(0,player.getInventory()),3,3);var keys=json.getAsJsonObject("key");var pattern=json.getAsJsonArray("pattern");
            for(int y=0;y<3;y++)for(int x=0;x<3;x++){String c=String.valueOf(pattern.get(y).getAsString().charAt(x));if(c.equals(" "))continue;var item=ForgeRegistries.ITEMS.getValue(new ResourceLocation(keys.getAsJsonObject(c).get("item").getAsString()));require(item!=null&&item!=Items.AIR,"ingredient "+spec.id()+" "+c);grid.setItem(y*3+x,new ItemStack(item));}
            require(recipe.matches(grid,level),"matches "+spec.id());require(recipe.assemble(grid,level.registryAccess()).is(armor),"output "+spec.id());grid.setItem(1,new ItemStack(Items.DIRT));require(!recipe.matches(grid,level),"focus required "+spec.id());n++;
        }
        for(var family:List.of("gabriel_ultrakill","abberant_predator","shadow_jester")){
            for(var spec:SchoolArmor.SPECS)if(spec.family().equals(family))player.setItemSlot(ArmorItem.Type.valueOf(spec.slot()).getSlot(),SchoolArmor.ITEMS.get(spec.id()).get().getDefaultInstance());
            while(player.tickCount%20!=0)player.tickCount++;
            SchoolArmor.mageSet(new net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent(player));require(player.hasEffect(SchoolArmorEffects.MAGE.get()),"set effect "+family);player.removeEffect(SchoolArmorEffects.MAGE.get());
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,ItemStack.EMPTY);SchoolArmor.mageSet(new net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent(player));require(!player.hasEffect(SchoolArmorEffects.MAGE.get()),"incomplete set "+family);
        }
        for(var slot:List.of(net.minecraft.world.entity.EquipmentSlot.HEAD,net.minecraft.world.entity.EquipmentSlot.CHEST,net.minecraft.world.entity.EquipmentSlot.LEGS,net.minecraft.world.entity.EquipmentSlot.FEET))player.setItemSlot(slot,ItemStack.EMPTY);
        com.mojang.logging.LogUtils.getLogger().info("BRUTALITY_IRON_SCHOOL_ARMOR_PASS parts={} recipes={} school-attributes=pass set-bonus=pass",n,n);
    }
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException("School armor: "+message);}private SchoolArmorAudit(){}
}
