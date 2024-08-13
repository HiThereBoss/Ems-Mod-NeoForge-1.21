package net.em.ems_mod.event;

import net.em.ems_mod.EmsMod;
import net.em.ems_mod.entity.custom.MiniVanEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = EmsMod.MODID)
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteractSpecific event){
        if (event.getLevel().isClientSide()){
            return;
        }

        if (event.getTarget() instanceof MiniVanEntity miniVanEntity){
            miniVanEntity.onPlayerInteract(event.getEntity());
        }
    }
}
