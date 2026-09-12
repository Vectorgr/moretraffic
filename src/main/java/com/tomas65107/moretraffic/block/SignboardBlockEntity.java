package com.tomas65107.moretraffic.block;

import com.simibubi.create.content.equipment.clipboard.ClipboardCloneable;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.tomas65107.moretraffic.core.BoardElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SignboardBlockEntity extends SmartBlockEntity implements MenuProvider, ClipboardCloneable {

    public List<BoardElement> elements;
    public boolean firstPlace;

    public SignboardBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);

        elements = new ArrayList<>();
        elements.add(new BoardElement.Background(0, 0, DyeColor.WHITE));
        firstPlace = true;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    @Override
    public Component getDisplayName() {
        return null;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return null;
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        firstPlace = tag.getBoolean("firstPlace");
        elements = new ArrayList<>();

        CompoundTag elementsTag = tag.getCompound("elements");

        List<String> keys = new ArrayList<>(elementsTag.getAllKeys());
        keys.sort(Comparator.comparingInt(Integer::parseInt));

        for (String key : keys) {
            elements.add(
                    BoardElement.deserialize(elementsTag.getCompound(key), registries)
            );
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        CompoundTag elementsTag = new CompoundTag();

        int index = 0;
        for (BoardElement element : elements) {
            elementsTag.put(String.valueOf(index), element.serialize());
            index++;
        }

        tag.put("elements", elementsTag);
        tag.putBoolean("firstPlace", firstPlace);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {
        CompoundTag tag = new CompoundTag();
        write(tag, lookup, true);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        ClientboundBlockEntityDataPacket packet = ClientboundBlockEntityDataPacket.create(this);
        return packet;
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider lookup) {
        super.onDataPacket(connection, packet, lookup);
        this.loadAdditional(packet.getTag(), lookup);
    }

    @Override
    public String getClipboardKey() {
        return "compat.moretraffic.signboard";
    }

    @Override
    public boolean writeToClipboard(HolderLookup.@NotNull Provider registries, CompoundTag tag, Direction side) {
        CompoundTag elementsTag = new CompoundTag();

        int index = 0;
        for (BoardElement element : elements) {
            elementsTag.put(String.valueOf(index), element.serialize());
            index++;
        }

        tag.put("elements", elementsTag);
        tag.putBoolean("firstPlace", firstPlace);

        return true;
    }

    @Override
    public boolean readFromClipboard(HolderLookup.@NotNull Provider registries, CompoundTag tag, Player player, Direction side, boolean simulate) {
        if (!tag.contains("elements")) return false;

        if (simulate) return true;

        CompoundTag elementsTag = tag.getCompound("elements");
        List<String> keys = new ArrayList<>(elementsTag.getAllKeys());
        keys.sort(Comparator.comparingInt(Integer::parseInt));
        List<BoardElement> copiedElements = new ArrayList<>();
        for (String key : keys) {
            copiedElements.add(
                    BoardElement.deserialize(elementsTag.getCompound(key), registries)
            );
        }

        elements = copiedElements;
        firstPlace = tag.getBoolean("firstPlace");
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }
}
