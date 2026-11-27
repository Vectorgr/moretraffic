package com.tomas65107.moretraffic.networking;

import com.tomas65107.moretraffic.core.GroupControllerData;
import com.tomas65107.moretraffic.item.GroupControllerItem;
import com.tomas65107.moretraffic.mod.registration.MTDataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerHandleGroupController {

    public static void handle(ClientRenameGroupControllerPacket payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ItemStack stack = ctx.player().getItemInHand(payload.mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            if (!(stack.getItem() instanceof GroupControllerItem)) return;

            String name = payload.name().trim();
            if (name.length() > GroupControllerData.MAX_NAME_LENGTH) name = name.substring(0, GroupControllerData.MAX_NAME_LENGTH);

            stack.set(MTDataComponents.GROUP_CONTROLLER.get(), GroupControllerItem.getData(stack).withName(name));
        });
    }

}
