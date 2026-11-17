package cn.wanyiwige.brutalityiron.skills;

import cn.wanyiwige.brutalityiron.BrutalityIron;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public final class SkillContent {
    private static final DeferredRegister<AbstractSpell> SPELLS=DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY,BrutalityIron.ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES=DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,BrutalityIron.ID);
    public static final Map<String,RegistryObject<AbstractSpell>> SKILLS=new LinkedHashMap<>();
    public static final RegistryObject<EntityType<SkillVisualEntity>> VISUAL=ENTITIES.register("skill_effect",()->EntityType.Builder.<SkillVisualEntity>of(SkillVisualEntity::new,MobCategory.MISC).sized(.35f,.35f).clientTrackingRange(12).updateInterval(1).build("brutality_iron:skill_effect"));
    public static final RegistryObject<EntityType<SkillMinion>> MINION=ENTITIES.register("skill_minion",()->EntityType.Builder.<SkillMinion>of(SkillMinion::new,MobCategory.CREATURE).sized(.7f,1.6f).clientTrackingRange(10).updateInterval(2).build("brutality_iron:skill_minion"));
    public static final RegistryObject<EntityType<SkillStray>> STRAY=ENTITIES.register("skill_stray",()->EntityType.Builder.<SkillStray>of(SkillStray::new,MobCategory.CREATURE).sized(.6f,1.99f).clientTrackingRange(10).build("brutality_iron:skill_stray"));
    static {for(var d:SkillDefinition.ALL.values())SKILLS.put(d.id(),SPELLS.register(d.id(),()->new SkillSpell(d)));}
    public static SkillSpell spell(String id){return (SkillSpell)SKILLS.get(id).get();}
    public static ItemStack scroll(String id,int rank){
        var stack=new ItemStack(io.redspace.ironsspellbooks.registries.ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spell(id),rank,stack);return stack;
    }
    public static void register(IEventBus bus){
        SPELLS.register(bus);ENTITIES.register(bus);SkillEffects.register(bus);
        bus.addListener((EntityAttributeCreationEvent e)->e.put(MINION.get(),SkillMinion.attributes().build()));
        bus.addListener((EntityAttributeCreationEvent e)->e.put(STRAY.get(),net.minecraft.world.entity.monster.AbstractSkeleton.createAttributes().build()));
    }
    private SkillContent(){}
}
