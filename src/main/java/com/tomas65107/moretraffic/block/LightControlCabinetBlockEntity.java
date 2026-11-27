package com.tomas65107.moretraffic.block;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlock;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.content.trains.signal.SignalBlockEntity;
import com.tomas65107.moretraffic.core.TrafficDisplayPixels;
import com.tomas65107.moretraffic.core.TrafficLightGroup;
import com.tomas65107.moretraffic.core.lightinstructions.*;
import com.tomas65107.moretraffic.gui.containers.LightControlCabinetMenu;
import com.tomas65107.moretraffic.mod.compat.displaylink.SignalOccupancyDisplaySource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.tomas65107.moretraffic.core.lightinstructions.LightInstructionProperty.PropertyTypes.*;

public class LightControlCabinetBlockEntity extends BlockEntity implements MenuProvider {

    public boolean isRunning;
    public int ticksSinceStart;
    public int lastInstructionFinishedTick;
    public short programStep;
    public boolean shouldLoop;
    /** Why the program stopped by itself, shown in the GUI. Null while running or when stopped by a player. */
    public @Nullable Component stopReason;
    public List<TrafficLightGroup> groups = new ArrayList<>();
    public List<LightInstructionProperty> instructions = new ArrayList<>();
    //Display Link position -> whether the train signal it reads was last seen red
    private final Map<BlockPos, Boolean> signalLinks = new LinkedHashMap<>();

    public LightControlCabinetBlockEntity(BlockEntityType type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);

        isRunning = false;
        ticksSinceStart = 0;
        lastInstructionFinishedTick = 0;
        programStep = 0;
        shouldLoop = false;

        instructions.clear();
        groups.clear();
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);

        isRunning = tag.getBoolean("IsRunning");
        shouldLoop = tag.getBoolean("ShouldLoop");
        ticksSinceStart = tag.getInt("TicksSinceStart");
        lastInstructionFinishedTick = tag.getInt("LastInstructionFinishedTick");
        programStep = tag.getShort("ProgramStep");

        stopReason = tag.contains("StopReason", 8) // 8 = StringTag
                ? Component.Serializer.fromJson(tag.getString("StopReason"), registries)
                : null;

        instructions.clear();

        //instructions
        if (tag.contains("Instructions", 9)) { // 9 = ListTag
            ListTag instructionsTag = tag.getList("Instructions", 10); // 10 = CompoundTag

            for (int i = 0; i < instructionsTag.size(); i++) {
                CompoundTag nbtInstruction = instructionsTag.getCompound(i);

                LightInstructionProperty instruction = switch (getPropertyOfName(nbtInstruction.getString("Type"))) {
                    case AWAIT_REDSTONE -> new AwaitRedstone(
                            nbtInstruction.getInt("InternalTimestamp")
                    );

                    case DELAY -> new Delay(
                            nbtInstruction.getInt("Ticks")
                    );

                    case MODIFY_LIGHT -> new ModifyLight(
                            nbtInstruction.getString("Group"),
                            DyeColor.byName(nbtInstruction.getString("LightColor0"), DyeColor.BLACK),
                            DyeColor.byName(nbtInstruction.getString("LightColor1"), DyeColor.BLACK),
                            DyeColor.byName(nbtInstruction.getString("LightColor2"), DyeColor.BLACK)
                    );

                    case MODIFY_DISPLAY -> new ModifyDisplay(
                            nbtInstruction.getString("Group"),
                            TrafficDisplayPixels.deserialize(nbtInstruction.getString("TrafficDisplayPixels"))
                    );

                    case AWAIT_TRAIN_SIGNAL_ON -> new AwaitTrainSignal(true);

                    case AWAIT_TRAIN_SIGNAL_OFF -> new AwaitTrainSignal(false);

                    case SEND_PULSE -> new SendPulse(
                            nbtInstruction.getString("Group"),
                            nbtInstruction.getBoolean("Enable")
                    );

                    case null, default ->
                            throw new IllegalArgumentException("Error loading BE; Unknown instruction type");
                };

                instructions.add(instruction);
            }
        }

        signalLinks.clear();
        ListTag linksTag = tag.getList("SignalLinks", 10); // 10 = CompoundTag
        for (int i = 0; i < linksTag.size(); i++) {
            CompoundTag linkTag = linksTag.getCompound(i);
            signalLinks.put(BlockPos.of(linkTag.getLong("Pos")), linkTag.getBoolean("Occupied"));
        }

        //groups
        groups.clear();
        ListTag groupsTag = tag.getList("Groups", 10); // 10 = CompoundTag
        for (int i = 0; i < groupsTag.size(); i++) {
            CompoundTag Ctag = groupsTag.getCompound(i);
            TrafficLightGroup group = new TrafficLightGroup();
            group.name = Ctag.getString("Name");

            List<BlockPos> positions = new ArrayList<>();
            ListTag positionsTag = Ctag.getList("Positions", 4); // 4 = LongTag
            for (Tag value : positionsTag) if (value instanceof LongTag longTag) positions.add(BlockPos.of(longTag.getAsLong()));

            group.lightsPositions = positions;
            groups.add(group);
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putBoolean("IsRunning", isRunning);
        tag.putBoolean("ShouldLoop", shouldLoop);
        tag.putInt("TicksSinceStart", ticksSinceStart);
        tag.putInt("LastInstructionFinishedTick", lastInstructionFinishedTick);
        tag.putShort("ProgramStep", programStep);
        if (stopReason != null) tag.putString("StopReason", Component.Serializer.toJson(stopReason, registries));

        // instructions
        ListTag instructionsTag = new ListTag();

        for (LightInstructionProperty instruction : instructions) {
            CompoundTag entry = new CompoundTag();

            entry.putString("Type", instruction.getClassType().getNameOfProperty());

            if (instruction instanceof ModifyLight(String group, DyeColor light0, DyeColor light1, DyeColor light2)) {
                entry.putString("Group", group);
                entry.putString("LightColor0", light0.getName());
                entry.putString("LightColor1", light1.getName());
                entry.putString("LightColor2", light2.getName());
            }
            else if (instruction instanceof Delay(int delayInTicks)) {
                entry.putInt("Ticks", delayInTicks);
            }
            else if (instruction instanceof AwaitRedstone(int internalTimestamp)) {
                entry.putInt("InternalTimestamp", internalTimestamp);
            }
            else if (instruction instanceof AwaitTrainSignal) {
                //the type name already stores the wanted state
            }
            else if (instruction instanceof ModifyDisplay(String group, TrafficDisplayPixels trafficDisplayPixels)) {
                entry.putString("Group", group);
                entry.putString("TrafficDisplayPixels", trafficDisplayPixels.serialize());
            } else if (instruction instanceof SendPulse(String group, boolean enable)) {
                entry.putString("Group", group);
                entry.putBoolean("Enable", enable);
            }
            else {
                throw new IllegalArgumentException("Error saving BE; Unknown instruction type");
            }

            instructionsTag.add(entry);
        }

        tag.put("Instructions", instructionsTag);

        // signal links
        ListTag linksTag = new ListTag();
        signalLinks.forEach((linkPos, occupied) -> {
            CompoundTag linkTag = new CompoundTag();
            linkTag.putLong("Pos", linkPos.asLong());
            linkTag.putBoolean("Occupied", occupied);
            linksTag.add(linkTag);
        });
        tag.put("SignalLinks", linksTag);

        // groups
        ListTag groupsTag = new ListTag();
        for (TrafficLightGroup group : groups) {
            CompoundTag Ctag = new CompoundTag();
            Ctag.putString("Name", group.name);

            ListTag positionsTag = new ListTag();
            for (BlockPos pos : group.lightsPositions) positionsTag.add(LongTag.valueOf(pos.asLong()));
            Ctag.put("Positions", positionsTag);

            groupsTag.add(Ctag);
        }
        tag.put("Groups", groupsTag);

    }

    /** Stops the program because the current instruction cannot be executed. */
    public void stopAtCurrentStep(Component reason) {
        isRunning = false;
        stopReason = Component.translatable("gui.moretraffic.control_cabinet.stopped.step",
                programStep + 1,
                instructions.get(programStep).getClassType().getComponentOfProperty(false),
                reason);
    }

    /** For an instruction whose group has no positions to act on. */
    public void stopForUnusableGroup(String groupName) {
        boolean exists = groups.stream().anyMatch(g -> g.name.equals(groupName));
        stopAtCurrentStep(Component.translatable("gui.moretraffic.control_cabinet.stopped." + (exists ? "group_empty" : "group_missing"), groupName));
    }

    /**
     * For a position in a group that holds something the instruction cannot control.
     * @param supportedKey translation key listing the blocks the instruction works with
     */
    public void stopForUnsupportedBlock(BlockPos blockPos, String supportedKey) {
        if (level == null) return;
        stopAtCurrentStep(Component.translatable("gui.moretraffic.control_cabinet.stopped.unsupported_block",
                level.getBlockState(blockPos).getBlock().getName(),
                blockPos.toShortString(),
                Component.translatable("gui.moretraffic.control_cabinet.stopped." + supportedKey)));
    }

    /** Whether at least one Display Link on a train signal is connected to this cabinet. */
    public boolean isSignalLinked() {
        return !signalLinks.isEmpty();
    }

    /** Whether any of the linked train signals is red. */
    public boolean isTrainPresent() {
        return signalLinks.containsValue(true);
    }

    public void addSignalLink(BlockPos linkPos) {
        if (signalLinks.putIfAbsent(linkPos.immutable(), false) != null || level == null) return;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public Map<BlockPos, Boolean> copySignalLinks() {
        return new LinkedHashMap<>(signalLinks);
    }

    /** Used after applying a client edit, so a stale client copy cannot overwrite the server's signal state. */
    public void restoreSignalLinks(Map<BlockPos, Boolean> links) {
        signalLinks.clear();
        signalLinks.putAll(links);
    }

    /**
     * Reads the train signals behind the linked Display Links and forgets the links that no longer exist.
     * Unloaded, redstone-disabled or not yet initialised links keep their last known value.
     *
     * @return true if anything changed
     */
    private boolean refreshSignalLinks(Level level, BlockPos pos) {
        boolean changed = false;

        Iterator<Map.Entry<BlockPos, Boolean>> iterator = signalLinks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Boolean> entry = iterator.next();
            if (!level.isLoaded(entry.getKey())) continue;

            if (!(level.getBlockEntity(entry.getKey()) instanceof DisplayLinkBlockEntity link)
                    || !link.getTargetPosition().equals(pos)
                    || !(link.activeSource instanceof SignalOccupancyDisplaySource)) {
                iterator.remove();
                changed = true;
                continue;
            }

            if (link.getBlockState().getOptionalValue(DisplayLinkBlock.POWERED).orElse(false)) continue;

            BlockPos signalPos = link.getSourcePosition();
            if (!level.isLoaded(signalPos) || !(level.getBlockEntity(signalPos) instanceof SignalBlockEntity signal)) continue;

            SignalBlockEntity.SignalState signalState = signal.getState();
            if (signalState == SignalBlockEntity.SignalState.INVALID) continue;

            boolean occupied = signalState == SignalBlockEntity.SignalState.RED;
            if (occupied != entry.getValue()) {
                entry.setValue(occupied);
                changed = true;
            }
        }

        return changed;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LightControlCabinetBlockEntity be) {

        boolean linksChanged = be.refreshSignalLinks(level, pos);

        //LED follows the running state; checked first because a stopped cabinet returns early below
        if (state.getValue(LightControlCabinetBlock.LIT) != be.isRunning) {
            state = state.setValue(LightControlCabinetBlock.LIT, be.isRunning);
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }

        //a program that was started again forgets why it stopped last time
        if (be.isRunning) be.stopReason = null;

        //Cannot run when no instructions are added
        if (be.isRunning && be.instructions.isEmpty()) {
            be.isRunning = false;
            be.stopReason = Component.translatable("gui.moretraffic.control_cabinet.stopped.no_instructions");
        } else

            //Handle illegal values ui prevents setting (changed to run only when running)
            if ((be.instructions.size() > 64 || be.instructions.size() < be.programStep) && be.isRunning) {
                level.destroyBlock(pos, true);
                return;
            } else

            //Loop handling
            if (be.programStep == be.instructions.size()) {
                if (be.shouldLoop) {
                    be.programStep = 0;
                    be.lastInstructionFinishedTick = 0;
                    be.ticksSinceStart = 0;
                } else {
                    if (be.isRunning) be.stopReason = Component.translatable("gui.moretraffic.control_cabinet.stopped.finished");
                    be.isRunning = false;
                }
            } else

                //Main loop
                if (be.isRunning) {
                    be.ticksSinceStart++;
                    LightInstructionProperty currentInstruction = be.instructions.get(be.programStep);

                    if (currentInstruction.executePayload(be, currentInstruction , level, pos)) {
                        be.lastInstructionFinishedTick = be.ticksSinceStart;
                        be.programStep++;
                    }
                } else if (!linksChanged) return;

        be.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

        @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, lookup);
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
    public Component getDisplayName() {
        return Component.literal("null");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new LightControlCabinetMenu(id, inventory, getBlockPos());
    }
}
