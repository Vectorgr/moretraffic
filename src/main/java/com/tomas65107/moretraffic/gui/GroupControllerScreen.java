package com.tomas65107.moretraffic.gui;

import com.tomas65107.moretraffic.core.GroupControllerData;
import com.tomas65107.moretraffic.networking.ClientRenameGroupControllerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import org.lwjgl.glfw.GLFW;

import static com.tomas65107.moretraffic.core.ColorsManager.PRIMARY;
import static com.tomas65107.moretraffic.core.ColorsManager.SHEET_BACKGROUND;
import static com.tomas65107.moretraffic.helpers.ColorHelper.rgb;
import static net.neoforged.neoforge.network.PacketDistributor.sendToServer;

/** Changes the name of the group held by a Group Controller. */
public class GroupControllerScreen extends Screen {

    private static final int GUI_WIDTH = 180;
    private static final int GUI_HEIGHT = 80;

    private final InteractionHand hand;
    private String name;
    private EditBox nameBox;

    private GroupControllerScreen(InteractionHand hand, String name) {
        super(Component.translatable("gui.moretraffic.group_controller.title"));
        this.hand = hand;
        this.name = name;
    }

    public static void open(InteractionHand hand, String currentName) {
        Minecraft.getInstance().setScreen(new GroupControllerScreen(hand, currentName));
    }

    @Override
    protected void init() {
        int guiX = (width - GUI_WIDTH) / 2;
        int guiY = (height - GUI_HEIGHT) / 2;

        nameBox = new EditBox(font, guiX + 10, guiY + 25, GUI_WIDTH - 20, 18,
                Component.translatable("gui.moretraffic.control_cabinet.groups.group_name"));
        nameBox.setMaxLength(GroupControllerData.MAX_NAME_LENGTH);
        nameBox.setValue(name);
        nameBox.setResponder(text -> name = text);
        addRenderableWidget(nameBox);
        setInitialFocus(nameBox);

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> saveAndClose())
                .bounds(guiX + 10, guiY + 50, 77, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> onClose())
                .bounds(guiX + GUI_WIDTH - 87, guiY + 50, 77, 20).build());
    }

    private void saveAndClose() {
        sendToServer(new ClientRenameGroupControllerPacket(hand == InteractionHand.MAIN_HAND, name));
        onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            saveAndClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int guiX = (width - GUI_WIDTH) / 2;
        int guiY = (height - GUI_HEIGHT) / 2;
        guiGraphics.fill(guiX, guiY, guiX + GUI_WIDTH, guiY + GUI_HEIGHT, 0xFF000000 | rgb(SHEET_BACKGROUND));
        guiGraphics.drawString(font, title, guiX + 10, guiY + 10, rgb(PRIMARY), true);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
