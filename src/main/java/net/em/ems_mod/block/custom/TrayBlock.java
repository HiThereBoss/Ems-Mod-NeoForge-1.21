package net.em.ems_mod.block.custom;

import com.mojang.serialization.MapCodec;
import net.em.ems_mod.block.ModBlocks;
import net.em.ems_mod.blockentity.ModBlockEntities;
import net.em.ems_mod.blockentity.TrayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrayBlock extends HorizontalDirectionalBlock implements EntityBlock{
    public static final VoxelShape NS_SHAPE = Block.box(1,0,3,15,1,13);
    public static final VoxelShape EW_SHAPE = Block.box(3,0,1,13,1,15);

    public TrayBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return null;
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        switch ((Direction)pState.getValue(FACING)) {
            case NORTH:
            default:
                return NS_SHAPE;
            case EAST, WEST:
                return EW_SHAPE;
        }
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return ModBlockEntities.TRAY_BE.get().create(pos, state);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack pStack, @NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHitResult) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if (!(be instanceof TrayBlockEntity blockEntity))
            return ItemInteractionResult.FAIL;

        if (pLevel.isClientSide()){
            return ItemInteractionResult.SUCCESS;
        }

        blockEntity.interact(pPlayer,pHand,pHitResult,pStack);

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof TrayBlockEntity blockEntity) {

            if (player.isShiftKeyDown() && !level.isClientSide()){
                ItemStack itemStackToDrop = new ItemStack(ModBlocks.TRAY.get());

                // Save before reading the data
                blockEntity.saveCustomAndMetadata(level.registryAccess());

                // Take stored data variable and set it to block_entity_data component type in item to drop
                CustomData.set(DataComponents.BLOCK_ENTITY_DATA, itemStackToDrop, blockEntity.data);

                Block.popResource(level,pos,itemStackToDrop);
            }
            else if (!player.isCreative()){
                blockEntity.getItemStackHandler().ifPresent(handler -> {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        Block.popResource(level, pos, handler.getStackInSlot(i));
                    }
                });
                Block.popResource(level, pos, new ItemStack(ModBlocks.TRAY.get()));
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }
}
