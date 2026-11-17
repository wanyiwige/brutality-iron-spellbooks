package cn.wanyiwige.brutalityiron.skills;

import cn.wanyiwige.brutalityiron.BrutalityIron;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public final class SkillEffects {
    private static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,BrutalityIron.ID);
    public static final RegistryObject<MobEffect> PLAGUE=EFFECTS.register("plague",()->new MobEffect(MobEffectCategory.HARMFUL,0x9DDB46){
        @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%20==0;}
        @Override public void applyEffectTick(LivingEntity target,int amplifier){SkillRuntime.plagueTick(target,amplifier);}
    });
    public static final RegistryObject<MobEffect> CORROSION=EFFECTS.register("corrosion",()->new MobEffect(MobEffectCategory.HARMFUL,0x7CB334){}
        .addAttributeModifier(Attributes.ARMOR,"033a90e4-ecb1-4ddf-8b4e-765514890d01",-2,AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<MobEffect> SHATTERED=EFFECTS.register("shattered_core",()->new MobEffect(MobEffectCategory.HARMFUL,0xD7A16A){}
        .addAttributeModifier(Attributes.ARMOR,"033a90e4-ecb1-4ddf-8b4e-765514890d02",-3,AttributeModifier.Operation.ADDITION)
        .addAttributeModifier(Attributes.MAX_HEALTH,"033a90e4-ecb1-4ddf-8b4e-765514890d05",-2,AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<MobEffect> COVENANT=EFFECTS.register("vital_covenant",()->new MobEffect(MobEffectCategory.BENEFICIAL,0xD44858){}
        .addAttributeModifier(Attributes.MAX_HEALTH,"033a90e4-ecb1-4ddf-8b4e-765514890d03",-2,AttributeModifier.Operation.ADDITION)
        .addAttributeModifier(Attributes.ATTACK_DAMAGE,"033a90e4-ecb1-4ddf-8b4e-765514890d04",3,AttributeModifier.Operation.ADDITION));
    public static final RegistryObject<MobEffect> PACT=EFFECTS.register("brimstone_pact",()->new MobEffect(MobEffectCategory.BENEFICIAL,0xF46237){
        @Override public boolean isDurationEffectTick(int d,int a){return d%20==0;}
        @Override public void applyEffectTick(LivingEntity target,int amplifier){
            if(target.getHealth()<=3){target.removeEffect(this);return;}
            target.hurt(target.damageSources().magic(),Math.min(1+amplifier*.15f,target.getHealth()-2));
        }
    });
    public static final RegistryObject<MobEffect> STILL=EFFECTS.register("stillness_ward",()->new MobEffect(MobEffectCategory.BENEFICIAL,0x8DAEAC){});
    public static void register(IEventBus bus){EFFECTS.register(bus);}
    private SkillEffects(){}
}
