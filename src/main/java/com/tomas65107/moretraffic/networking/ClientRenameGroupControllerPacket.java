package com.tomas65107.moretraffic.networking;

import com.tomas65107.moretraffic.mod.MoreTraffic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientRenameGroupControllerPacket(boolean mainHand, String name) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientRenameGroupControllerPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MoreTraffic.MODID, "clientsender_group_controller"));

    public static final StreamCodec<FriendlyByteBuf, ClientRenameGroupControllerPacket> STREAM_CODEC =
            CustomPacketPayload.codec(ClientRenameGroupControllerPacket::encode, ClientRenameGroupControllerPacket::decode);

    public static void encode(ClientRenameGroupControllerPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.mainHand());
        buf.writeUtf(msg.name());
    }

    public static ClientRenameGroupControllerPacket decode(FriendlyByteBuf buf) {
        return new ClientRenameGroupControllerPacket(buf.readBoolean(), buf.readUtf());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
