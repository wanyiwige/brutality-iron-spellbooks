package cn.wanyiwige.brutalityiron;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.registries.ForgeRegistries;

public final class BookRecipeAudit {
    public static void run(ServerLevel level) {
        var player=FakePlayerFactory.getMinecraft(level);
        int crafted=0,skipped=0,negative=0,materials=0;
        for(var def:BookContent.DEFINITIONS) {
            var id=BrutalityIron.id(def.name());
            var json=readRecipe(def.name());
            for(var entry:json.getAsJsonObject("key").entrySet()) {
                var materialId=new ResourceLocation(entry.getValue().getAsJsonObject().get("item").getAsString());
                var material=ForgeRegistries.ITEMS.getValue(materialId);
                require(material!=null && material!=Items.AIR,"registered recipe material " + materialId);materials++;
            }
            var found=level.getRecipeManager().byKey(id);
            if(!BookContent.BOOKS.containsKey(def.name())) {
                require(found.isEmpty(),"optional book recipe should be skipped " + id);
                require(level.getServer().getAdvancements().getAdvancement(BrutalityIron.id("recipes/equipment/"+def.name()))==null,
                        "optional book advancement should be skipped " + id);
                skipped++;continue;
            }
            require(found.orElseThrow() instanceof CraftingRecipe,"native crafting recipe " + id);
            var recipe=(CraftingRecipe)found.get();
            var grid=new TransientCraftingContainer(new CraftingMenu(0,player.getInventory()),3,3);
            {
                var key=json.getAsJsonObject("key");var pattern=json.getAsJsonArray("pattern");
                for(int y=0;y<3;y++)for(int x=0;x<3;x++) {
                    String symbol=String.valueOf(pattern.get(y).getAsString().charAt(x));
                    var itemId=new ResourceLocation(key.getAsJsonObject(symbol).get("item").getAsString());
                    var item=ForgeRegistries.ITEMS.getValue(itemId);
                    require(item!=null && item!=Items.AIR,"real material " + itemId);
                    grid.setItem(y*3+x,new ItemStack(item));
                }
            }
            require(recipe.matches(grid,level),"recipe match " + id);
            var result=recipe.assemble(grid,level.registryAccess());
            require(result.getCount()==1 && result.is(BookContent.BOOKS.get(def.name()).get()),"recipe output " + id);
            ((SchoolBook)result.getItem()).onCraftedBy(result,level,player);
            require(ISpellContainer.get(result).isEmpty() && ISpellContainer.get(result).getMaxSpellCount()==def.slots(),
                    "crafted blank native book " + id);
            require(level.getServer().getAdvancements().getAdvancement(BrutalityIron.id("recipes/equipment/"+def.name()))!=null,
                    "recipe unlock advancement " + id);
            var core=grid.getItem(1);grid.setItem(1,new ItemStack(Items.DIRT));
            require(!recipe.matches(grid,level),"wrong focus rejected " + id);negative++;
            grid.setItem(1,core);grid.setItem(4,ItemStack.EMPTY);
            require(!recipe.matches(grid,level),"missing book rejected " + id);negative++;
            crafted++;LogUtils.getLogger().info("BRUTALITY_IRON_BOOK_RECIPE_PASS {}",id);
        }
        LogUtils.getLogger().info("BRUTALITY_IRON_BOOK_RECIPES_PASS crafted={} optional-skipped={} negative={} materials={}",crafted,skipped,negative,materials);
    }
    private static com.google.gson.JsonObject readRecipe(String name) {
        try(var stream=BookRecipeAudit.class.getResourceAsStream("/data/brutality_iron/recipes/"+name+".json")) {
            return JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
        }catch(java.io.IOException error){throw new IllegalStateException(error);}
    }
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
}
