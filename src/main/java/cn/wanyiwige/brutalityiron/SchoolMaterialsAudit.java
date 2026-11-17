package cn.wanyiwige.brutalityiron;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.item.UpgradeData;
import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu;
import io.redspace.ironsspellbooks.item.UpgradeOrbTypeData;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.registries.ForgeRegistries;

/** End-to-end Forge-world audit: real recipes, registry, anvil and item attributes. */
public final class SchoolMaterialsAudit {
    public static void run(ServerLevel level){
        var player=FakePlayerFactory.getMinecraft(level);
        var registry=level.registryAccess().registryOrThrow(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY);
        var tag=TagKey.create(Registries.ITEM,new net.minecraft.resources.ResourceLocation("irons_spellbooks:inscribed_rune"));
        int recipes=0,anvils=0;
        for(String school:new String[]{"inferno","celestia","nihility"}){
            var rune=SchoolMaterials.RUNES.get(school).get();var orb=SchoolMaterials.ORBS.get(school).get();
            require(new ItemStack(rune).is(tag),"rune recognized for unscribing "+school);
            var focus=ForgeRegistries.ITEMS.tags().getTag(Content.SCHOOL_MAP.get(school).get().getFocus()).iterator().next();
            craft(level,player,school+"_rune",focus,ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation("irons_spellbooks:blank_rune")),rune);recipes++;
            var crafted=craft(level,player,school+"_upgrade_orb",rune,ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation("irons_spellbooks:upgrade_orb")),orb);recipes++;
            require(UpgradeOrbTypeData.has(crafted) && UpgradeOrbTypeData.get(crafted).type().equals(SchoolMaterials.type(school)),"crafted orb type "+school);
            var holder=registry.getHolder(SchoolMaterials.type(school)).orElseThrow();var type=holder.value();
            var attr=Content.ATTRIBUTE_MAP.get(school+"_spell_power").get();
            require(type.attribute().value()==attr && Math.abs(type.amount()-.05)<1e-9 && type.operation()==AttributeModifier.Operation.MULTIPLY_BASE,"registered orb effect "+school);
            require(type.containerItem().isPresent() && type.containerItem().get().is(orb),"native recovery container "+school);
            for(var target:new ItemStack[]{new ItemStack(Items.IRON_HELMET),SchoolArmor.ITEMS.get(school.equals("inferno")?"blazeborne_helmet":school.equals("celestia")?"seraph_helmet":"masked_fool_helmet").get().getDefaultInstance(),BookContent.BOOKS.get(school.equals("inferno")?"brimwielder_tome":school.equals("celestia")?"celestia_tome":"void_tome").get().getDefaultInstance()}){
                var menu=new ArcaneAnvilMenu(0,player.getInventory(),ContainerLevelAccess.create(level,BlockPos.ZERO));
                require(menu.getSlot(0).mayPlace(target),"anvil accepts equipment "+school);
                require(menu.getSlot(1).mayPlace(crafted),"anvil accepts orb "+school);
                menu.getSlot(0).set(target);menu.getSlot(1).set(crafted.copy());menu.createResult();
                var upgraded=menu.getSlot(2).getItem().copy();
                require(!upgraded.isEmpty() && upgraded.is(target.getItem()),"anvil result "+school);
                var upgrade=UpgradeData.getUpgradeData(upgraded);
                require(upgrade.getTotalUpgrades()==1 && upgrade.upgrades().containsKey(holder),"anvil upgrade NBT "+school);
                var saved=ItemStack.of(upgraded.save(new net.minecraft.nbt.CompoundTag()));
                require(UpgradeData.getUpgradeData(saved).getTotalUpgrades()==1,"saved upgrade "+school);
                if(target.getItem() instanceof ArmorItem){
                    var before=target.getAttributeModifiers(EquipmentSlot.HEAD).get(attr);
                    var after=upgraded.getAttributeModifiers(EquipmentSlot.HEAD).get(attr);
                    double base=before.stream().filter(m->m.getOperation()==AttributeModifier.Operation.MULTIPLY_BASE).mapToDouble(AttributeModifier::getAmount).sum();
                    double result=after.stream().filter(m->m.getOperation()==AttributeModifier.Operation.MULTIPLY_BASE).mapToDouble(AttributeModifier::getAmount).sum();
                    require(Math.abs(result-base-.05)<1e-9,"applied helmet attribute "+school+" before="+base+" after="+result);
                } else {
                    var slot=new top.theillusivec4.curios.api.SlotContext("spellbook",player,0,false,true);var uuid=java.util.UUID.randomUUID();
                    var book=(SchoolBook)upgraded.getItem();var before=book.getAttributeModifiers(slot,uuid,upgraded);
                    double base=before.get(attr).stream().filter(m->m.getOperation()==AttributeModifier.Operation.MULTIPLY_BASE).mapToDouble(AttributeModifier::getAmount).sum();
                    var event=new top.theillusivec4.curios.api.event.CurioAttributeModifierEvent(upgraded,slot,uuid,before);
                    net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
                    double after=event.getModifiers().get(attr).stream().filter(m->m.getOperation()==AttributeModifier.Operation.MULTIPLY_BASE).mapToDouble(AttributeModifier::getAmount).sum();
                    require(Math.abs(after-base-.05)<1e-9,"applied spellbook curio attribute "+school+" before="+base+" after="+after);
                }
                anvils++;
            }
        }
        LogUtils.getLogger().info("BRUTALITY_IRON_SCHOOL_MATERIALS_PASS items=6 recipes={} anvil={} helmets-applied=6 spellbook-curios-applied=3",recipes,anvils);
    }
    private static ItemStack craft(ServerLevel level,net.minecraft.world.entity.player.Player player,String id,Item outer,Item center,Item expected){
        require(outer!=null&&center!=null,"ingredients exist "+id);var grid=new TransientCraftingContainer(new CraftingMenu(0,player.getInventory()),3,3);
        for(int i=0;i<9;i++)grid.setItem(i,new ItemStack(i==4?center:outer));
        var recipe=(CraftingRecipe)level.getRecipeManager().byKey(BrutalityIron.id(id)).orElseThrow();
        require(recipe.matches(grid,level),"matching recipe "+id);var result=recipe.assemble(grid,level.registryAccess());require(result.is(expected),"recipe result "+id);
        grid.setItem(0,new ItemStack(Items.DIRT));require(!recipe.matches(grid,level),"incorrect ingredient rejected "+id);return result;
    }
    private static void require(boolean ok,String msg){if(!ok)throw new IllegalStateException("School materials: "+msg);}
    private SchoolMaterialsAudit(){}
}
