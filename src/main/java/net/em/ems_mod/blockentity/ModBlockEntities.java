package net.em.ems_mod.blockentity;

import net.em.ems_mod.EmsMod;
import net.em.ems_mod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EmsMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TrayBlockEntity>> TRAY_BE =
            BLOCK_ENTITIES.register("tray_be",
                    () -> BlockEntityType.Builder.of(TrayBlockEntity::new, ModBlocks.TRAY.get())
                            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MicrowaveBlockEntity>> MICROWAVE_BE =
            BLOCK_ENTITIES.register("microwave_be",
                    () -> BlockEntityType.Builder.of(MicrowaveBlockEntity::new, ModBlocks.MICROWAVE.get())
                            .build(null));

    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }
}
