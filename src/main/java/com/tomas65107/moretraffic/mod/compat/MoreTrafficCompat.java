package com.tomas65107.moretraffic.mod.compat;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.tomas65107.moretraffic.block.Derailer;
import com.tomas65107.moretraffic.mod.registration.MTRegistrate;
import net.minecraft.resources.ResourceLocation;

import static com.simibubi.create.content.trains.graph.EdgePointType.register;

public final class MoreTrafficCompat {
    public static EdgePointType<Derailer> DERAILER;

    public static void init() {
        DERAILER = register(ResourceLocation.fromNamespaceAndPath("moretraffic", "derailer"), Derailer::new);
    }

    /** Needs Create's blocks to exist, so it must run during common setup. */
    public static void registerDisplayLinkBehaviours() {
        DisplaySource.BY_BLOCK.add(AllBlocks.TRACK_SIGNAL.get(), MTRegistrate.SIGNAL_OCCUPANCY_SOURCE.get());
        DisplayTarget.BY_BLOCK.register(MTRegistrate.LIGHT_CONTROL_CABINET.get(), MTRegistrate.CABINET_DISPLAY_TARGET.get());
    }
}