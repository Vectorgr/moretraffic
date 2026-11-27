package com.tomas65107.moretraffic.gui;

import com.tomas65107.moretraffic.core.GroupControllerData;
import com.tomas65107.moretraffic.networking.ClientEditGroupControllerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.tomas65107.moretraffic.core.ColorsManager.PRIMARY;
import static com.tomas65107.moretraffic.core.ColorsManager.SECONDARY;
import static com.tomas65107.moretraffic.core.ColorsManager.SHEET_BACKGROUND;
import static com.tomas65107.moretraffic.helpers.ColorHelper.rgb;
import static net.neoforged.neoforge.network.PacketDistributor.sendToServer;

/**
 * Changes the name of the group held by a Group Controller and removes blocks from it.
 * Nothing changes on the item until the player confirms.
 */
public class GroupControllerScreen extends Screen {

    private static final int GUI_WIDTH = 220;
    private static final int GUI_HEIGHT = 230;
    private static final int ROWS_PER_PAGE = 5;
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_TOP = 62;

    private final InteractionHand hand;
    private final List<BlockPos> positions;
    private final List<BlockPos> removed = new ArrayList<>();
    private String name;
    private int page;

    private GroupControllerScreen(InteractionHand hand, GroupControllerData data) {
        super(Component.translatable("gui.moretraffic.group_controller.title"));
        this.hand = hand;
        this.name = data.name();
        this.positions = new ArrayList<>(data.positions());
    }

    public static void open(InteractionHand hand, GroupControllerData data) {
        Minecraft.getInstance().setScreen(new GroupControllerScreen(hand, data));
    }

    private int guiX() {
        return (width - GUI_WIDTH) / 2;
    }

    private int guiY() {
        return (height - GUI_HEIGHT) / 2;
    }

    private int pageCount() {
        return Math.max(1, (positions.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE);
    }

    @Override
    protected void init() {
        int guiX = guiX();
        int guiY = guiY();
        page = Math.max(0, Math.min(page, pageCount() - 1));

        EditBox nameBox = new EditBox(font, guiX + 10, guiY + 25, GUI_WIDTH - 20, 18,
                Component.translatable("gui.moretraffic.control_cabinet.groups.group_name"));
        nameBox.setMaxLength(GroupControllerData.MAX_NAME_LENGTH);
        nameBox.setValue(name);
        nameBox.setResponder(text -> name = text);
        addRenderableWidget(nameBox);
        setInitialFocus(nameBox);

        for (int i = page * ROWS_PER_PAGE; i < Math.min(positions.size(), (page + 1) * ROWS_PER_PAGE); i++) {
            BlockPos pos = positions.get(i);
            int rowY = guiY + LIST_TOP + (i - page * ROWS_PER_PAGE) * ROW_HEIGHT;
            addRenderableWidget(Button.builder(Component.literal("X"), b -> {
                        positions.remove(pos);
                        removed.add(pos);
                        rebuildWidgets();
                    })
                    .bounds(guiX + GUI_WIDTH - 30, rowY, 20, 20)
                    .tooltip(Tooltip.create(Component.translatable("gui.moretraffic.group_controller.remove")))
                    .build());
        }

        int pagerY = guiY + LIST_TOP + ROWS_PER_PAGE * ROW_HEIGHT + 4;
        if (pageCount() > 1) {
            Button previous = Button.builder(Component.literal("<"), b -> changePage(-1)).bounds(guiX + 10, pagerY, 20, 20).build();
            Button next = Button.builder(Component.literal(">"), b -> changePage(1)).bounds(guiX + 70, pagerY, 20, 20).build();
            previous.active = page > 0;
            next.active = page < pageCount() - 1;
            addRenderableWidget(previous);
            addRenderableWidget(next);
        }

        Button removeAll = Button.builder(Component.translatable("gui.moretraffic.group_controller.remove_all"), b -> {
            removed.addAll(positions);
            positions.clear();
            rebuildWidgets();
        }).bounds(guiX + GUI_WIDTH - 100, pagerY, 90, 20).build();
        removeAll.active = !positions.isEmpty();
        addRenderableWidget(removeAll);

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> saveAndClose())
                .bounds(guiX + 10, guiY + GUI_HEIGHT - 28, 95, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> onClose())
                .bounds(guiX + GUI_WIDTH - 105, guiY + GUI_HEIGHT - 28, 95, 20).build());
    }

    private void changePage(int offset) {
        int newPage = Math.max(0, Math.min(page + offset, pageCount() - 1));
        if (newPage == page) return;
        page = newPage;
        rebuildWidgets();
    }

    private void saveAndClose() {
        sendToServer(new ClientEditGroupControllerPacket(hand == InteractionHand.MAIN_HAND, name, removed));
        onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //enter confirms only while typing the name, so it still presses a focused button
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && getFocused() instanceof EditBox) {
            saveAndClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0) changePage(scrollY > 0 ? -1 : 1);
        return true;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int guiX = guiX();
        int guiY = guiY();
        guiGraphics.fill(guiX, guiY, guiX + GUI_WIDTH, guiY + GUI_HEIGHT, 0xFF000000 | rgb(SHEET_BACKGROUND));
        guiGraphics.drawString(font, title, guiX + 10, guiY + 10, rgb(PRIMARY), true);
        guiGraphics.drawString(font, Component.translatable("item.moretraffic.group_controller.positions", positions.size()),
                guiX + 10, guiY + 50, rgb(SECONDARY), true);

        if (positions.isEmpty()) {
            guiGraphics.drawString(font, Component.translatable("gui.moretraffic.group_controller.empty"),
                    guiX + 10, guiY + LIST_TOP + 6, rgb(SECONDARY), true);
        }

        for (int i = page * ROWS_PER_PAGE; i < Math.min(positions.size(), (page + 1) * ROWS_PER_PAGE); i++) {
            BlockPos pos = positions.get(i);
            int rowY = guiY + LIST_TOP + (i - page * ROWS_PER_PAGE) * ROW_HEIGHT;
            guiGraphics.drawString(font, font.plainSubstrByWidth(blockName(pos).getString(), GUI_WIDTH - 46),
                    guiX + 10, rowY + 1, rgb(PRIMARY), true);
            guiGraphics.drawString(font, pos.toShortString(), guiX + 10, rowY + 11, rgb(SECONDARY), true);
        }

        if (pageCount() > 1) {
            guiGraphics.drawCenteredString(font, (page + 1) + "/" + pageCount(),
                    guiX + 50, guiY + LIST_TOP + ROWS_PER_PAGE * ROW_HEIGHT + 10, rgb(SECONDARY));
        }
    }

    /** Name of the block currently at the position, which the client only knows if the chunk is loaded. */
    private Component blockName(BlockPos pos) {
        if (minecraft == null || minecraft.level == null || !minecraft.level.isLoaded(pos)) {
            return Component.translatable("gui.moretraffic.group_controller.unloaded");
        }
        return minecraft.level.getBlockState(pos).getBlock().getName();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
