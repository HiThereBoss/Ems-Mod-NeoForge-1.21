package net.em.ems_mod.block.custom;

import com.mojang.serialization.MapCodec;
import net.em.ems_mod.block.util.BoxPoints;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HorizontalSingleShapedBlock extends HorizontalDirectionalBlock {

    public final VoxelShape NORTH_SHAPE;
    public final VoxelShape SOUTH_SHAPE;
    public final VoxelShape WEST_SHAPE;
    public final VoxelShape EAST_SHAPE;

    public HorizontalSingleShapedBlock(Properties properties, BoxPoints boxPoints) {
        super(properties);
        registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
        NORTH_SHAPE = Shapes.or(Block.box(boxPoints.x1, boxPoints.y1, boxPoints.z1, boxPoints.x2, boxPoints.y2, boxPoints.z2));
        SOUTH_SHAPE = Shapes.or(Block.box(16-boxPoints.x2, boxPoints.y1, 16-boxPoints.z2, 16-boxPoints.x1, boxPoints.y2, 16-boxPoints.z1));
        WEST_SHAPE = Shapes.or(Block.box(boxPoints.z1, boxPoints.y1, boxPoints.x1, boxPoints.z2, boxPoints.y2, boxPoints.x2));
        EAST_SHAPE = Shapes.or(Block.box(16-boxPoints.z2, boxPoints.y1, 16-boxPoints.x2, 16-boxPoints.z1, boxPoints.y2, 16-boxPoints.x1));
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
}
