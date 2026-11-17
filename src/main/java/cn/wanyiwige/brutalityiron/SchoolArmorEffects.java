package cn.wanyiwige.brutalityiron;
import net.minecraftforge.registries.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
public final class SchoolArmorEffects {
    private static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,BrutalityIron.ID);
    public static final RegistryObject<MobEffect> MAGE=EFFECTS.register("hazen_mage_set",()->{
        var effect=new MobEffect(MobEffectCategory.BENEFICIAL,0xfbb741){};
        effect.addAttributeModifier(AttributeRegistry.CASTING_MOVESPEED.get(),"e969651c-6434-4d6a-a302-cf4eb0cba2fa",.15,AttributeModifier.Operation.MULTIPLY_BASE);
        effect.addAttributeModifier(AttributeRegistry.SPELL_RESIST.get(),"2edeb187-06ae-41b6-a00a-05d08befa507",.1,AttributeModifier.Operation.MULTIPLY_BASE);
        return effect;
    });
    public static void register(IEventBus bus){EFFECTS.register(bus);}private SchoolArmorEffects(){}
}
