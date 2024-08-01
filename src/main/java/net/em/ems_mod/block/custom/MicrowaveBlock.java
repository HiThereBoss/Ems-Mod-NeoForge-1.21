package net.em.ems_mod.block.custom;

import com.mojang.serialization.MapCodec;
import net.em.ems_mod.blockentity.MicrowaveBlockEntity;
import net.em.ems_mod.blockentity.ModBlockEntities;
import net.em.ems_mod.blockentity.util.TickableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MicrowaveBlock extends HorizontalDirectionalBlock implements EntityBlock{
    public static final VoxelShape NORTH_SHAPE = Block.box(1.7,0,4.9,14.3,9.7,13);
    public static final VoxelShape SOUTH_SHAPE = Block.box(16-14.3,0,16-13,16-1.7,9.7,16-4.9);
    public static final VoxelShape WEST_SHAPE = Block.box(4.9,0,1.7,13,9.7,14.3);
    public static final VoxelShape EAST_SHAPE = Block.box(16-13,0,16-14.3,16-4.9,9.7,16-1.7);

    public MicrowaveBlock(Properties properties) {
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
                return NORTH_SHAPE;
            case SOUTH:
                return SOUTH_SHAPE;
            case EAST:
                return EAST_SHAPE;
            case WEST:
                return WEST_SHAPE;
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
        return ModBlockEntities.MICROWAVE_BE.get().create(pos, state);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack pStack, @NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer, @NotNull InteractionHand pHand, @NotNull BlockHitResult pHitResult) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if (!(be instanceof MicrowaveBlockEntity blockEntity))
            return ItemInteractionResult.FAIL;
        System.out.println("Before client check");


        if (!pPlayer.getItemInHand(pHand).isEmpty()){
            blockEntity.takeItem(pStack, pPlayer, pHand);
            return ItemInteractionResult.SUCCESS;
        }
        else{
            blockEntity.giveOutputToPlayer(pPlayer);
            return ItemInteractionResult.CONSUME_PARTIAL;
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {

        if (pLevel.isClientSide()) return null;

        return TickableBlockEntity.getTickerHelper(pLevel);
    }

    /*
    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TrayBlockEntity blockEntity) {
            blockEntity.getOptional().ifPresent(handler -> {
                for (int i = 0; i < handler.getSlots(); i++) {
                    Block.popResource(level, pos, handler.getStackInSlot(i));
                }
            });
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

     */
}
