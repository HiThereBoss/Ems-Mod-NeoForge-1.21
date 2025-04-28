package net.em.ems_mod.event;

import net.em.ems_mod.EmsMod;
import net.em.ems_mod.entity.custom.MiniVanEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = EmsMod.MODID)
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteractSpecific event){
        if (event.getLevel().isClientSide()){
            return;
        }

        if (event.getTarget() instanceof MiniVanEntity miniVanEntity){
            miniVanEntity.onPlayerInteract(event);
        }
    }

//    @SubscribeEvent
//    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
//        Player player = event.getEntity();
//
//        if (player.getVehicle() instanceof MiniVanEntity) {
//            HumanoidModel<?> model = (HumanoidModel<?>) event.getRenderer().getModel();
//
//            model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
//            model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
//            model.attackTime = 0F;
//            model.riding = true;
//            model.young = false;
//            model.rightArm.xRot = -1.5F;
//            model.rightArm.yRot = 0.2F;
//            model.leftArm.xRot = -0.5F;
//            model.leftArm.yRot = -0.2F;
//            System.out.println("RenderPlayerEvent fired for " + player.getName().getString());
//
//        }
//    }
}
