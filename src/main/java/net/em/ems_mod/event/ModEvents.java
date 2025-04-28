package net.em.ems_mod.event;

import net.em.ems_mod.EmsMod;
import net.em.ems_mod.blockentity.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = EmsMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void EntityAttributeEvent(EntityAttributeCreationEvent event){
        // event.put(ModEntities.MINIVAN_ENTITY.get(), MiniVanEntity.setAttributes());
    }

    @SubscribeEvent
    public static void registerCapabilitiesEvent(RegisterCapabilitiesEvent event){
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.TRAY_BE.get(), (blockEntity, side) -> (blockEntity).getInventory());
    }
}
