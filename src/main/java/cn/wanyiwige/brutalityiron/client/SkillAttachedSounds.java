package cn.wanyiwige.brutalityiron.client;

import cn.wanyiwige.brutalityiron.BrutalityIron;
import cn.wanyiwige.brutalityiron.SpellSounds;
import cn.wanyiwige.brutalityiron.skills.SkillVisualEntity;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=BrutalityIron.ID,value=Dist.CLIENT)
public final class SkillAttachedSounds {
    private static final Set<UUID> SEEN=new HashSet<>();
    private static Object world;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;
        Minecraft mc=Minecraft.getInstance();
        if(mc.level!=world){SEEN.clear();world=mc.level;}
        if(mc.level==null)return;
        Set<UUID> present=new HashSet<>();
        for(var entity:mc.level.entitiesForRendering()){
            if(!(entity instanceof SkillVisualEntity visual))continue;
            present.add(visual.getUUID());
            boolean charge=visual.skill.equals("spirit_bomb")&&visual.visualRole().equals("charging_display");
            boolean hole=visual.skill.equals("event_horizon")&&visual.visualRole().equals("black_hole");
            if((charge||hole)&&SEEN.add(visual.getUUID()))mc.getSoundManager().play(new Attached(visual,charge));
        }
        SEEN.retainAll(present);
    }
    private static final class Attached extends AbstractTickableSoundInstance {
        private final SkillVisualEntity entity;
        private final String role;
        Attached(SkillVisualEntity entity,boolean charge){
            super((charge?SpellSounds.SPIRIT_CHARGE:SpellSounds.BLACK_HOLE).get(),SoundSource.PLAYERS,SoundInstance.createUnseededRandom());
            this.entity=entity;this.role=charge?"charging_display":"black_hole";
            this.looping=!charge;this.volume=charge?.85f:.6f;this.pitch=1f;
            this.x=entity.getX();this.y=entity.getY();this.z=entity.getZ();
            this.attenuation=SoundInstance.Attenuation.LINEAR;
        }
        @Override public void tick(){
            if(entity.isRemoved()||!entity.visualRole().equals(role)){stop();return;}
            this.x=entity.getX();this.y=entity.getY();this.z=entity.getZ();
        }
    }
    private SkillAttachedSounds(){}
}
