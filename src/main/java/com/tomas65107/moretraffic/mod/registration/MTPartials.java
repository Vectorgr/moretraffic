package com.tomas65107.moretraffic.mod.registration;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public class MTPartials {
    public static final PartialModel FLAG = PartialModel.of(ResourceLocation.fromNamespaceAndPath("moretraffic", "block/derailer_flag"));

    public static final PartialModel CROSSING_BARRIER_ARM = PartialModel.of(ResourceLocation.fromNamespaceAndPath("moretraffic", "block/crossing_barrier_arm"));

    public static void init() {}
}
