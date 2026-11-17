package cn.wanyiwige.brutalityiron.client;

import cn.wanyiwige.brutalityiron.entity.brimwielder.ExtinctionEntity;
import net.minecraft.client.Minecraft;

public final class ClientSounds {
    public static void play(ExtinctionEntity beam) {
        Minecraft.getInstance().getSoundManager().play(new ExtinctionSpellSoundInstance(beam));
    }
    private ClientSounds() {}
}
