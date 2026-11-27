package com.tomas65107.moretraffic.networking;

import com.tomas65107.moretraffic.mod.MoreTraffic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientSenderPacketTrafficLightBlinking(
        BlockPos pos,
        int lightIndex,
        boolean blinking
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientSenderPacketTrafficLightBlinking> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MoreTraffic.MODID, "clientsender_trafficlight_blinking"));


    public static final StreamCodec<FriendlyByteBuf, ClientSenderPacketTrafficLightBlinking> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ClientSenderPacketTrafficLightBlinking::pos,
                    ByteBufCodecs.VAR_INT,
                    ClientSenderPacketTrafficLightBlinking::lightIndex,
                    ByteBufCodecs.BOOL,
                    ClientSenderPacketTrafficLightBlinking::blinking,
                    ClientSenderPacketTrafficLightBlinking::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
