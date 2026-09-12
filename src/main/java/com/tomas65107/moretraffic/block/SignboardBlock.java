package com.tomas65107.moretraffic.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.tomas65107.moretraffic.core.blocktypes.MTNormalBlock;
import com.tomas65107.moretraffic.gui.containers.SignboardMenu;
import com.tomas65107.moretraffic.mod.registration.MTRegistrate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.tomas65107.moretraffic.rendering.BlockBoundingBoxes.SIGNBOARD;

public class SignboardBlock extends MTNormalBlock implements IWrenchable, EntityBlock {

    public static final DirectionProperty FACING = DirectionalBlock.FACING;

    public SignboardBlock(@NotNull Properties properties) {
        super(properties, FACING, SIGNBOARD);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof de.mrjulsen.trafficcraft.item.WrenchItem || stack.getItem() instanceof com.simibubi.create.content.equipment.wrench.WrenchItem) {
            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (id, inventory, p) -> new SignboardMenu(id, inventory, pos), Component.empty()
                ), buf -> buf.writeBlockPos(pos));
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }


    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {

        if (this.defaultBlockState().setValue(FACING, context.getClickedFace()).canSurvive(context.getLevel(), context.getClickedPos())) {
            return this.defaultBlockState().setValue(FACING, context.getClickedFace());
        }

        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);

        return canSupportCenter(level, pos.relative(facing.getOpposite()), facing);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SignboardBlockEntity(MTRegistrate.SIGNSBOARD_BE.get(), blockPos, blockState);
    }
}
