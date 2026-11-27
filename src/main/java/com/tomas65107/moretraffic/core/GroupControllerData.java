package com.tomas65107.moretraffic.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

/** Group stored on a Group Controller item: its name and the positions of the blocks in it. */
public record GroupControllerData(String name, List<BlockPos> positions) {

    public static final int MAX_NAME_LENGTH = 17;
    public static final int MAX_POSITIONS = 64;

    public static final GroupControllerData EMPTY = new GroupControllerData("", List.of());

    public static final Codec<GroupControllerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(GroupControllerData::name),
            BlockPos.CODEC.listOf().fieldOf("positions").forGetter(GroupControllerData::positions)
    ).apply(instance, GroupControllerData::new));

    public static final StreamCodec<ByteBuf, GroupControllerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, GroupControllerData::name,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), GroupControllerData::positions,
            GroupControllerData::new
    );

    public GroupControllerData {
        positions = List.copyOf(positions);
    }

    public GroupControllerData withName(String newName) {
        return new GroupControllerData(newName, positions);
    }

    /** Adds the position, or removes it if it was already in the group. */
    public GroupControllerData toggle(BlockPos pos) {
        List<BlockPos> newPositions = new ArrayList<>(positions);
        if (!newPositions.remove(pos)) newPositions.add(pos.immutable());
        return new GroupControllerData(name, newPositions);
    }
}
