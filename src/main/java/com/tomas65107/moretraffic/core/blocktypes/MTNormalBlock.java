package com.tomas65107.moretraffic.core.blocktypes;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import static com.tomas65107.moretraffic.helpers.HelperFunctions.rotateShape;
import static com.tomas65107.moretraffic.helpers.HelperFunctions.rotateShapeSpecial;

public class MTNormalBlock extends Block {

    public final DirectionProperty INTERNAL_FACING; //DirectionalBlock or HorizontalDirectionalBlock
    public final VoxelShape INTERNAL_SHAPE;

    public MTNormalBlock(@NotNull Properties properties, DirectionProperty FACING, VoxelShape SHAPE) {
        super(properties);
        INTERNAL_FACING = FACING;
        INTERNAL_SHAPE = SHAPE;
    }

    protected VoxelShape figureOutShapeRotation(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (INTERNAL_FACING == null) return INTERNAL_SHAPE;
        if (INTERNAL_FACING.equals(DirectionalBlock.FACING)) return rotateShapeSpecial(state.getValue(INTERNAL_FACING), INTERNAL_SHAPE);
        if (INTERNAL_FACING.equals(HorizontalDirectionalBlock.FACING)) return rotateShape(state.getValue(INTERNAL_FACING), INTERNAL_SHAPE);
        return INTERNAL_SHAPE;
    }

    @Override public @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {return figureOutShapeRotation(state, level, pos, context);}
    @Override public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {return figureOutShapeRotation(state, level, pos, context);}

    @Override
    public @NotNull BlockState rotate(@NotNull BlockState pState, @NotNull Rotation pRotation) {
        if (INTERNAL_FACING == null) return pState;
        return (BlockState)pState.setValue((DirectionProperty)INTERNAL_FACING, pRotation.rotate(pState.getValue((DirectionProperty)INTERNAL_FACING)));
    }

    @Override
    public @NotNull BlockState mirror(@NotNull BlockState pState, @NotNull Mirror pMirror) {
        if (INTERNAL_FACING == null) return pState;
        return pState.rotate(pMirror.getRotation((Direction)pState.getValue((DirectionProperty)INTERNAL_FACING)));
    }
}
