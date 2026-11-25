package com.tomas65107.moretraffic.mod.compat.displaylink;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.trains.signal.SignalBlockEntity;
import com.tomas65107.moretraffic.block.LightControlCabinetBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

/**
 * Lets a Display Link read a Create train signal. When the target is a Light Control Cabinet the link only
 * registers itself there; the cabinet then reads the signal every tick, so it reacts immediately instead of
 * waiting for the Display Link refresh interval.
 */
public class SignalOccupancyDisplaySource extends DisplaySource {

    @Override
    public void transferData(DisplayLinkContext context, DisplayTarget target, int line) {
        if (context.getTargetBlockEntity() instanceof LightControlCabinetBlockEntity cabinet) {
            cabinet.addSignalLink(context.blockEntity().getBlockPos());
            return;
        }
        super.transferData(context, target, line);
    }

    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof SignalBlockEntity signal)) return EMPTY;
        return List.of(Component.literal(signal.getState().name()));
    }
}
