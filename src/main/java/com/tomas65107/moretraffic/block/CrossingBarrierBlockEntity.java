package com.tomas65107.moretraffic.block;

import com.tomas65107.moretraffic.core.ICabinetPulsable;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CrossingBarrierBlockEntity extends BlockEntity implements ICabinetPulsable {
    /** Fraction of the movement done per tick: 0.025 takes 2 seconds. */
    private static final float SPEED = 0.025f;

    /** Client animation: 0 is raised, 1 is lowered. */
    private final LerpedFloat arm = LerpedFloat.linear();
    private boolean armInitialised;

    public CrossingBarrierBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** An enabled pulse from the Control Cabinet lowers the barrier. */
    @Override
    public void handlePulseLight(Boolean newStatus) {
        if (level == null) return;
        level.setBlock(worldPosition, getBlockState().setValue(CrossingBarrierBlock.LOWERED, newStatus), Block.UPDATE_ALL);
    }

    public void clientTick() {
        float target = getTarget();
        if (!armInitialised) {
            //do not replay the movement when the chunk loads
            arm.startWithValue(target);
            armInitialised = true;
        }
        arm.chase(target, SPEED, LerpedFloat.Chaser.LINEAR);
        arm.tickChaser();
    }

    public float getLoweredProgress(float partialTicks) {
        return armInitialised ? arm.getValue(partialTicks) : getTarget();
    }

    private float getTarget() {
        return getBlockState().getValue(CrossingBarrierBlock.LOWERED) ? 1 : 0;
    }
}
