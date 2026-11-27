package com.tomas65107.moretraffic.item;

import com.tomas65107.moretraffic.block.AdvancedTrafficLightBlockEntity;
import com.tomas65107.moretraffic.block.LEDStripBlockEntity;
import com.tomas65107.moretraffic.block.LightControlCabinetBlockEntity;
import com.tomas65107.moretraffic.block.TrafficDisplayBlockEntity;
import com.tomas65107.moretraffic.core.GroupControllerData;
import com.tomas65107.moretraffic.core.ICabinetPulsable;
import com.tomas65107.moretraffic.core.TrafficLightGroup;
import com.tomas65107.moretraffic.gui.GroupControllerScreen;
import com.tomas65107.moretraffic.mod.MoreTraffic;
import com.tomas65107.moretraffic.mod.registration.MTDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects the positions of MoreTraffic blocks into a named group, which is then given to a Light Control Cabinet.
 * <p>
 * Right click a controllable block to add or remove it, right click a cabinet to save the group in it,
 * and right click anything outside this mod (or the air) to rename the group.
 */
public class GroupControllerItem extends Item {

    public GroupControllerItem(Properties properties) {
        super(properties);
    }

    public static GroupControllerData getData(ItemStack stack) {
        return stack.getOrDefault(MTDataComponents.GROUP_CONTROLLER.get(), GroupControllerData.EMPTY);
    }

    /** Runs before the block's own interaction, so MoreTraffic blocks do not react to the click. */
    @Override
    public @NotNull InteractionResult onItemUseFirst(@NotNull ItemStack stack, @NotNull UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        //blocks outside the mod keep their interaction; if they have none, use() opens the rename screen
        if (!BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getNamespace().equals(MoreTraffic.MODID)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof LightControlCabinetBlockEntity cabinet) {
                saveInCabinet(stack, cabinet, player);
            } else if (isControllable(be)) {
                togglePosition(stack, pos, player);
            } else {
                showMessage(player, Component.translatable("interaction.moretraffic.group_controller.unsupported").withStyle(ChatFormatting.RED));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) GroupControllerScreen.open(hand, getData(stack).name());
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        GroupControllerData data = getData(stack);
        Component name = data.name().isEmpty()
                ? Component.translatable("item.moretraffic.group_controller.unnamed")
                : Component.literal(data.name());
        tooltip.add(Component.translatable("item.moretraffic.group_controller.group", name).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.moretraffic.group_controller.positions", data.positions().size()).withStyle(ChatFormatting.GRAY));
    }

    /** Whether any cabinet instruction can act on this block. */
    private static boolean isControllable(BlockEntity be) {
        return be instanceof AdvancedTrafficLightBlockEntity
                || be instanceof LEDStripBlockEntity
                || be instanceof TrafficDisplayBlockEntity
                || be instanceof ICabinetPulsable;
    }

    private static void togglePosition(ItemStack stack, BlockPos pos, Player player) {
        GroupControllerData data = getData(stack);
        boolean removing = data.positions().contains(pos);

        if (!removing && data.positions().size() >= GroupControllerData.MAX_POSITIONS) {
            showMessage(player, Component.translatable("interaction.moretraffic.group_controller.full", GroupControllerData.MAX_POSITIONS).withStyle(ChatFormatting.RED));
            return;
        }

        GroupControllerData newData = data.toggle(pos);
        stack.set(MTDataComponents.GROUP_CONTROLLER.get(), newData);
        showMessage(player, Component.translatable("interaction.moretraffic.group_controller." + (removing ? "removed" : "added"),
                pos.toShortString(), newData.positions().size()));
    }

    /** Creates the group in the cabinet, or replaces the positions of the group with the same name. */
    private static void saveInCabinet(ItemStack stack, LightControlCabinetBlockEntity cabinet, Player player) {
        GroupControllerData data = getData(stack);

        if (data.name().isEmpty()) {
            showMessage(player, Component.translatable("interaction.moretraffic.group_controller.no_name").withStyle(ChatFormatting.RED));
            return;
        }
        if (data.positions().isEmpty()) {
            showMessage(player, Component.translatable("interaction.moretraffic.group_controller.no_positions").withStyle(ChatFormatting.RED));
            return;
        }

        List<BlockPos> positions = new ArrayList<>(data.positions());
        TrafficLightGroup existing = cabinet.groups.stream().filter(g -> g.name.equals(data.name())).findFirst().orElse(null);
        if (existing != null) existing.lightsPositions = positions;
        else cabinet.groups.add(new TrafficLightGroup(data.name(), positions));

        Level level = player.level();
        cabinet.setChanged();
        level.sendBlockUpdated(cabinet.getBlockPos(), cabinet.getBlockState(), cabinet.getBlockState(), 3);

        showMessage(player, Component.translatable("interaction.moretraffic.group_controller." + (existing != null ? "updated" : "saved"),
                data.name(), positions.size()).withStyle(ChatFormatting.GREEN));
    }

    private static void showMessage(Player player, Component message) {
        player.displayClientMessage(message, true);
    }
}
