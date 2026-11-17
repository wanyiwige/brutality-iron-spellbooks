package cn.wanyiwige.brutalityiron.client;

import cn.wanyiwige.brutalityiron.entity.brimwielder.ExtinctionEntity;
import net.goo.brutality.registry.BrutalityModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class ExtinctionSpellSoundInstance
extends AbstractTickableSoundInstance {
    private final Entity entity;

    public ExtinctionSpellSoundInstance(ExtinctionEntity entity) {
        super((SoundEvent)BrutalityModSounds.LASER_BEAM.get(), SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.delay = 0;
        this.looping = true;
        this.volume = Mth.clamp((float)((float)entity.getSpellLevel() / 10.0f), (float)0.0f, (float)1.0f);
        this.pitch = 1.0f;
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
        this.attenuation = SoundInstance.Attenuation.LINEAR;
    }

    public void tick() {
        if (!this.entity.isAlive()) {
            this.stop();
            return;
        }
        this.x = (float)this.entity.getX();
        this.y = (float)this.entity.getY();
        this.z = (float)this.entity.getZ();
    }
}

