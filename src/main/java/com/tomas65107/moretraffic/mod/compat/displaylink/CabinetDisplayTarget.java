package com.tomas65107.moretraffic.mod.compat.displaylink;

import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

/**
 * Makes the Light Control Cabinet selectable by a Display Link. Text from other sources is ignored;
 * train signals are handled by {@link SignalOccupancyDisplaySource}.
 */
public class CabinetDisplayTarget extends DisplayTarget {

    @Override
    public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {}

    @Override
    public DisplayTargetStats provideStats(DisplayLinkContext context) {
        return new DisplayTargetStats(1, 1, this);
    }
}
