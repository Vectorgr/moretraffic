package com.tomas65107.moretraffic.networking;

import com.tomas65107.moretraffic.mod.MoreTraffic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record ClientEditGroupControllerPacket(boolean mainHand, String name, List<BlockPos> removed) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientEditGroupControllerPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MoreTraffic.MODID, "clientsender_group_controller"));

    public static final StreamCodec<FriendlyByteBuf, ClientEditGroupControllerPacket> STREAM_CODEC =
            CustomPacketPayload.codec(ClientEditGroupControllerPacket::encode, ClientEditGroupControllerPacket::decode);

    public static void encode(ClientEditGroupControllerPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.mainHand());
        buf.writeUtf(msg.name());
        buf.writeCollection(msg.removed(), (b, pos) -> b.writeBlockPos(pos));
    }

    public static ClientEditGroupControllerPacket decode(FriendlyByteBuf buf) {
        return new ClientEditGroupControllerPacket(buf.readBoolean(), buf.readUtf(), buf.readList(b -> b.readBlockPos()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
