package net.em.ems_mod.blockentity.util;

import net.em.ems_mod.blockentity.MicrowaveBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public interface TickableBlockEntity {
    void tick();

    static <T extends BlockEntity>BlockEntityTicker<T> getTickerHelper(Level pLevel){
        return pLevel.isClientSide() ? null : (level, pos, state, blockEntity) -> ((TickableBlockEntity)blockEntity).tick();
    }
}
