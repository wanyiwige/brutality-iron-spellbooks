package cn.wanyiwige.brutalityiron;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.armor.ImbuableChestplateArmorItem;
import io.redspace.ironsspellbooks.item.armor.IronsExtendedArmorMaterial;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;
import software.bernie.geckolib.core.animation.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** User-selected Hazennstuff equipment, registered independently of the source mods. */
@Mod.EventBusSubscriber(modid=BrutalityIron.ID)
public final class SchoolArmor {
    public record Bonus(String attribute,double value,String operation,boolean optional) {}
    public record Spec(String family,String school,String slot,int[] defense,int durability,float toughness,
                       float knockback,int enchantment,boolean flight,boolean imbuable,boolean unbreakable,
                       boolean fire_resistant,boolean mage_set,boolean animated,List<Bonus> bonuses) {
        public String id(){return family+"_"+slot.toLowerCase(Locale.ROOT);}
    }
    private static final DeferredRegister<Item> REGISTER=DeferredRegister.create(ForgeRegistries.ITEMS,BrutalityIron.ID);
    public static final Map<String,RegistryObject<Item>> ITEMS=new LinkedHashMap<>();
    public static final List<Spec> SPECS;
    static {
        try(var reader=new InputStreamReader(Objects.requireNonNull(SchoolArmor.class.getResourceAsStream("/school-armor.json")),StandardCharsets.UTF_8)) {
            SPECS=List.of(new Gson().fromJson(reader,Spec[].class));
        } catch(Exception e){throw new IllegalStateException("Cannot load school armor",e);}
        for(var spec:SPECS)ITEMS.put(spec.id(),REGISTER.register(spec.id(),()->new Armor(spec)));
    }
    public static void register(IEventBus bus){REGISTER.register(bus);}
    public static final class Armor extends ImbuableChestplateArmorItem {
        public final Spec spec;
        public Armor(Spec spec){super(new Material(spec),Type.valueOf(spec.slot()),properties(spec));this.spec=spec;}
        private static Properties properties(Spec s){var p=new Properties().stacksTo(1).durability(s.durability()).rarity(Rarity.EPIC);return s.fire_resistant()?p.fireResistant():p;}
        @Override public Multimap<Attribute,AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot){
            if(spec==null||slot!=getEquipmentSlot())return super.getDefaultAttributeModifiers(slot);
            var out=ImmutableMultimap.<Attribute,AttributeModifier>builder().putAll(super.getDefaultAttributeModifiers(slot));
            int i=0; for(var bonus:spec.bonuses()){
                var attribute=ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(bonus.attribute()));
                if(attribute==null){if(bonus.optional())continue;throw new IllegalStateException("Missing armor attribute "+bonus.attribute());}
                var operation=switch(bonus.operation()){case "ADD_VALUE"->AttributeModifier.Operation.ADDITION;case "ADD_MULTIPLIED_BASE"->AttributeModifier.Operation.MULTIPLY_BASE;case "ADD_MULTIPLIED_TOTAL"->AttributeModifier.Operation.MULTIPLY_TOTAL;default->throw new IllegalArgumentException(bonus.operation());};
                String key=BrutalityIron.ID+"/"+spec.id()+"/"+slot+"/"+(i++);
                out.put(attribute,new AttributeModifier(UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)),key,bonus.value(),operation));
            }return out.build();
        }
        @Override public ItemStack getDefaultInstance(){var s=super.getDefaultInstance();if(spec.imbuable())initializeSpellContainer(s);return s;}
        @Override public void initializeSpellContainer(ItemStack stack){if(spec!=null&&spec.imbuable())super.initializeSpellContainer(stack);}
        @Override public void inventoryTick(ItemStack stack,net.minecraft.world.level.Level level,net.minecraft.world.entity.Entity entity,int slot,boolean selected){super.inventoryTick(stack,level,entity,slot,selected);if(spec.imbuable())initializeSpellContainer(stack);}
        @Override public int getMaxDamage(ItemStack stack){return spec.unbreakable()?0:super.getMaxDamage(stack);}
        @Override public boolean isDamageable(ItemStack stack){return !spec.unbreakable()&&super.isDamageable(stack);}
        @Override public boolean canElytraFly(ItemStack stack,net.minecraft.world.entity.LivingEntity entity){return spec.flight()&&getEquipmentSlot()==EquipmentSlot.CHEST&&(!stack.isDamageableItem()||stack.getDamageValue()<stack.getMaxDamage()-1);}
        @Override public boolean elytraFlightTick(ItemStack stack,net.minecraft.world.entity.LivingEntity entity,int ticks){return canElytraFly(stack,entity);}
        @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers){if(spec!=null&&spec.animated())controllers.add(new AnimationController<>(this,"source",0,state->state.setAndContinue(RawAnimation.begin().thenLoop("idle"))));}
        @Override @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
        public software.bernie.geckolib.renderer.GeoArmorRenderer<?> supplyRenderer(){return new cn.wanyiwige.brutalityiron.client.SchoolArmorRenderer();}
    }
    private record Material(Spec spec) implements IronsExtendedArmorMaterial {
        private int index(Type t){return switch(t){case HELMET->0;case CHESTPLATE->1;case LEGGINGS->2;case BOOTS->3;};}
        public int getDurabilityForType(Type t){return spec.durability();} public int getDefenseForType(Type t){return spec.defense()[index(t)];}
        public int getEnchantmentValue(){return spec.enchantment();} public SoundEvent getEquipSound(){return SoundEvents.ARMOR_EQUIP_LEATHER;}
        public Ingredient getRepairIngredient(){var item=ForgeRegistries.ITEMS.getValue(new ResourceLocation("irons_spellbooks:mithril_weave"));return item==null?Ingredient.EMPTY:Ingredient.of(item);}
        public String getName(){return BrutalityIron.ID+":"+spec.family();} public float getToughness(){return spec.toughness();} public float getKnockbackResistance(){return spec.knockback();}
        public Map<Attribute,AttributeModifier> getAdditionalAttributes(){return Map.of();}
    }
    @SubscribeEvent public static void mageSet(LivingEvent.LivingTickEvent event){
        var entity=event.getEntity();if(entity.level().isClientSide||!(entity instanceof net.minecraft.world.entity.player.Player)||entity.tickCount%20!=0)return;
        String family=null;for(var slot:List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET)){
            if(!(entity.getItemBySlot(slot).getItem() instanceof Armor a)||!a.spec.mage_set())return;
            if(family==null)family=a.spec.family();else if(!family.equals(a.spec.family()))return;
        }
        // Same Mage set bonuses as the existing Hazen recreation, without a dependency on it.
        if(!entity.hasEffect(SchoolArmorEffects.MAGE.get()))entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(SchoolArmorEffects.MAGE.get(),320,0,false,false,false));
    }
    private SchoolArmor(){}
}
