package cn.wanyiwige.brutalityiron.client;
import cn.wanyiwige.brutalityiron.SchoolArmor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="brutality_iron",value=Dist.CLIENT)
public final class SchoolArmorVisibility {
    private static final java.util.Map<net.minecraft.client.model.PlayerModel<?>,java.util.Deque<boolean[]>> VISIBILITY=new java.util.IdentityHashMap<>();
    @SubscribeEvent public static void before(net.minecraftforge.client.event.RenderLivingEvent.Pre<?,?> e){
        if(e.getRenderer().getModel() instanceof net.minecraft.client.model.PlayerModel<?> model){
            VISIBILITY.computeIfAbsent(model,k->new java.util.ArrayDeque<>()).push(new boolean[]{model.hat.visible,model.jacket.visible,model.leftSleeve.visible,model.rightSleeve.visible});
            if(e.getEntity().getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getItem() instanceof SchoolArmor.Armor)model.hat.visible=false;
            if(e.getEntity().getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem() instanceof SchoolArmor.Armor){model.jacket.visible=false;model.leftSleeve.visible=false;model.rightSleeve.visible=false;}
        }
    }
    @SubscribeEvent public static void after(net.minecraftforge.client.event.RenderLivingEvent.Post<?,?> e){
        if(e.getRenderer().getModel() instanceof net.minecraft.client.model.PlayerModel<?> model){
            var stack=VISIBILITY.get(model);if(stack!=null&&!stack.isEmpty()){var old=stack.pop();model.hat.visible=old[0];model.jacket.visible=old[1];model.leftSleeve.visible=old[2];model.rightSleeve.visible=old[3];if(stack.isEmpty())VISIBILITY.remove(model);}
        }
    }
}
