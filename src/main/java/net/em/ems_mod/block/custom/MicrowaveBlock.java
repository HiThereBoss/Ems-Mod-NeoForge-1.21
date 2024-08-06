package net.em.ems_mod.block.custom;

import com.mojang.serialization.MapCodec;
import net.em.ems_mod.block.util.BoxPoints;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MicrowaveBlock extends HorizontalDirectionalBlock implements EntityBlock{
    private static final BoxPoints boxPoints = new BoxPoints(1.7f,0f,4.8f,14.3f,9.7f,13f);

    public static final VoxelShape  NORTH_SHAPE = Shapes.or(Block.box(boxPoints.x1, boxPoints.y1, boxPoints.z1, boxPoints.x2, boxPoints.y2, boxPoints.z2));
    public static final VoxelShape  SOUTH_SHAPE = Shapes.or(Block.box(16-boxPoints.x2, boxPoints.y1, 16-boxPoints.z2, 16-boxPoints.x1, boxPoints.y2, 16-boxPoints.z1));
    public static final VoxelShape  WEST_SHAPE = Shapes.or(Block.box(boxPoints.z1, boxPoints.y1, boxPoints.x1, boxPoints.z2, boxPoints.y2, boxPoints.x2));
    public static final VoxelShape  EAST_SHAPE = Shapes.or(Block.box(16-boxPoints.z2, boxPoints.y1, 16-boxPoints.x2, 16-boxPoints.z1, boxPoints.y2, 16-boxPoints.x1));

    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public MicrowaveBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
        registerDefaultState(this.defaultBlockState().setValue(OPEN, Boolean.FALSE));
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
        pBuilder.add(OPEN);
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

        if (pLevel.isClientSide()) return ItemInteractionResult.SUCCESS;

        // Handles interaction
        return blockEntity.interact(pStack,pPlayer,pHand,pState,pLevel,pPos);

    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        return TickableBlockEntity.getTickerHelper(pLevel);
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MicrowaveBlockEntity blockEntity) {
            if (blockEntity.isChangingStates){
                blockEntity.isChangingStates = false;
                return;
            }

            blockEntity.getOptional().ifPresent(handler -> {
                for (int i = 0; i < handler.getSlots(); i++) {
                    Block.popResource(level, pos, handler.getStackInSlot(i));
                }
            });
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }


}
