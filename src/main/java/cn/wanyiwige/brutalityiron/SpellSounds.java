package cn.wanyiwige.brutalityiron;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public final class SpellSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS=DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,BrutalityIron.ID);
    private static RegistryObject<SoundEvent> add(String name){return SOUNDS.register(name,()->SoundEvent.createVariableRangeEvent(BrutalityIron.id(name)));}
    public static final RegistryObject<SoundEvent> SCHOOL_INFERNO=add("school_inferno");
    public static final RegistryObject<SoundEvent> SCHOOL_PLAGUE=add("school_plague");
    public static final RegistryObject<SoundEvent> SCHOOL_CELESTIA=add("school_celestia");
    public static final RegistryObject<SoundEvent> SPIRIT_CHARGE=add("spirit_charge");
    public static final RegistryObject<SoundEvent> SPIRIT_IMPACT=add("spirit_impact");
    public static final RegistryObject<SoundEvent> TRIPLE_CHARGE=add("triple_charge");
    public static final RegistryObject<SoundEvent> SEEKING_BLADES=add("seeking_blades");
    public static final RegistryObject<SoundEvent> FIRE_SPIRIT_SHOT=add("fire_spirit_shot");
    public static final RegistryObject<SoundEvent> INFERNO_PROJECTILE=add("inferno_projectile");
    public static final RegistryObject<SoundEvent> PLAGUE_IMPACT=add("plague_impact");
    public static final RegistryObject<SoundEvent> VOID_TELEPORT=add("void_teleport");
    public static final RegistryObject<SoundEvent> BLACK_HOLE=add("black_hole");
    public static void register(IEventBus bus){SOUNDS.register(bus);}
    private SpellSounds(){}
}
