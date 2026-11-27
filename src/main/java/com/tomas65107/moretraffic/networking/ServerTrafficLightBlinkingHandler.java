package com.tomas65107.moretraffic.networking;

import com.tomas65107.moretraffic.block.AdvancedTrafficLightBlockEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerTrafficLightBlinkingHandler {

    public static void handle(ClientSenderPacketTrafficLightBlinking payload, IPayloadContext context) {
        context.enqueueWork(() -> {

            Level level = context.player().level();

            if (level.getBlockEntity(payload.pos()) instanceof AdvancedTrafficLightBlockEntity trafficBE) {
                trafficBE.modifyLightBlinking(payload.lightIndex(), payload.blinking());
            }
        });
    }
}
