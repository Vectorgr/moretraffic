package com.tomas65107.moretraffic.core.lightinstructions;

import com.tomas65107.moretraffic.block.LightControlCabinetBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;


/**
 * Pauses execution until the train signals linked through Create's Display Link are in the wanted state:
 * on means any of them is red, off means none of them is. Without a linked signal it keeps waiting.
 */
public record AwaitTrainSignal(boolean on) implements LightInstructionProperty {

    @Override
    public PropertyTypes getClassType() {
        return on ? PropertyTypes.AWAIT_TRAIN_SIGNAL_ON : PropertyTypes.AWAIT_TRAIN_SIGNAL_OFF;
    }

    @Override
    public boolean executePayload(LightControlCabinetBlockEntity be, LightInstructionProperty instruction, Level level, BlockPos blockPos) {
        if (instruction instanceof AwaitTrainSignal(boolean wanted)) {
            return be.isSignalLinked() && be.isTrainPresent() == wanted;
        }
        be.isRunning = false;
        throw new RuntimeException("Corrupted Instruction passed for execution; Instruction of incorrect type");
    }
}
