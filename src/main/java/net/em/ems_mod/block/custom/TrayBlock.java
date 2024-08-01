package net.em.ems_mod.block.custom;

import com.mojang.serialization.MapCodec;
import net.em.ems_mod.blockentity.ModBlockEntities;
import net.em.ems_mod.blockentity.TrayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
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

        // TODO: ALL OF THIS WORKS ON CLIENT SIDE TOO, SHOULD I FIX?

        /*
        if (pLevel.isClientSide()){
            return ItemInteractionResult.SUCCESS;
        }
        */

        // Functionality
        if (pPlayer.getItemInHand(pHand).isEmpty()){
            blockEntity.takeItemFromTray(pPlayer);
            return ItemInteractionResult.SUCCESS;
        }
        else {
            blockEntity.addItemToTray(pStack, pPlayer, pHand);
            return ItemInteractionResult.SUCCESS;
        }
    }

    /*
    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TrayBlockEntity blockEntity) {
            blockEntity.getOptional().(handler -> {
                for (int i = 0; i < handler.getSlots(); i++) {
                    Block.popResource(level, pos, handler.getStackInSlot(i));
                }
            });
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

     */
}
