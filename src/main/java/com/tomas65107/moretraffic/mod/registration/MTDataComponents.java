package com.tomas65107.moretraffic.mod.registration;

import com.tomas65107.moretraffic.core.GroupControllerData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.tomas65107.moretraffic.mod.MoreTraffic.MODID;

public class MTDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GroupControllerData>> GROUP_CONTROLLER =
            DATA_COMPONENTS.register("group_controller", () -> DataComponentType.<GroupControllerData>builder()
                    .persistent(GroupControllerData.CODEC)
                    .networkSynchronized(GroupControllerData.STREAM_CODEC)
                    .build());

}
