package com.tomas65107.moretraffic.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.tomas65107.moretraffic.helpers.ColorHelper.rgb;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;

public class RichTextEditBox extends BetterEditBox {

    private Component value = Component.empty();
    private String previousText = "";

    private Consumer<String> externalResponder;

    private static final WidgetSprites SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("widget/text_field"),
            ResourceLocation.withDefaultNamespace("widget/text_field_highlighted")
    );

    public RichTextEditBox(int x, int y, int width, int height) {
        super(x, y, width, height);
        setTextColor(rgb(BLACK)); // hide the normal EditBox text


        super.setResponder(text -> {
            syncRichValue(text);
            if (externalResponder != null) {
                externalResponder.accept(text);
            }

        });
    }

    public void setRichValue(Component text) {
        value = text;
        previousText = text.getString();

        super.setValue(previousText);
    }

    public Component getRichValue() {
        return value;
    }

    private void handleTextChanged(String text) {
        syncRichValue(text);
        if (externalResponder != null) {
            externalResponder.accept(text);
        }
    }

    @Override
    public void onChange(Consumer<String> responder) {
        externalResponder = responder;
    }

    private void syncRichValue(String newText) {
        String oldText = previousText;

        if (oldText.equals(newText))
            return;

        // Get the style of every character in the old Component.
        List<Style> styles = new ArrayList<>();

        value.visit((style, text) -> {
            for (int i = 0; i < text.length(); i++) {
                styles.add(style);
            }

            return Optional.empty();
        }, Style.EMPTY);

        // Find the changed region.
        int start = 0;

        while (start < oldText.length()
                && start < newText.length()
                && oldText.charAt(start) == newText.charAt(start)) {
            start++;
        }

        int oldEnd = oldText.length();
        int newEnd = newText.length();

        while (oldEnd > start
                && newEnd > start
                && oldText.charAt(oldEnd - 1) == newText.charAt(newEnd - 1)) {
            oldEnd--;
            newEnd--;
        }

        // Determine the style for newly inserted text.
        Style insertedStyle = Style.EMPTY;

        if (start > 0 && start - 1 < styles.size()) {
            insertedStyle = styles.get(start - 1);
        } else if (!styles.isEmpty()) {
            insertedStyle = styles.get(0);
        }

        // Remove the styles corresponding to deleted text.
        styles.subList(start, oldEnd).clear();

        // Add styles for newly inserted text.
        for (int i = start; i < newEnd; i++) {
            styles.add(i, insertedStyle);
        }

        // Rebuild the Component.
        MutableComponent result = Component.empty();

        if (!newText.isEmpty()) {
            int segmentStart = 0;
            Style segmentStyle = styles.get(0);

            for (int i = 1; i < newText.length(); i++) {
                if (!styles.get(i).equals(segmentStyle)) {
                    result = result.append(
                            Component.literal(newText.substring(segmentStart, i))
                                    .withStyle(segmentStyle)
                    );

                    segmentStart = i;
                    segmentStyle = styles.get(i);
                }
            }

            result = result.append(
                    Component.literal(newText.substring(segmentStart))
                            .withStyle(segmentStyle)
            );
        }

        value = result;
        previousText = newText;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isVisible())
            return;

        // bg
        if (isBordered()) {
            ResourceLocation sprite = SPRITES.get(isActive(), isFocused());
            guiGraphics.blitSprite(sprite, getX(), getY(), getWidth(), getHeight());
        }

        var font = Minecraft.getInstance().font;

        int x = isBordered() ? getX() + 4 : getX();
        int y = isBordered() ? getY() + (getHeight() - 8) / 2 : getY();

        // rich text
        FormattedCharSequence text = value.getVisualOrderText();
        guiGraphics.drawString(font, text, x, y, rgb(WHITE), true);

        // cursor bb
        if (isFocused()) {
            String beforeCursor = getValue().substring(0, getCursorPosition());
            int cursorX = x + font.width(beforeCursor);

            guiGraphics.fill(
                    cursorX,
                    y - 1,
                    cursorX + 1,
                    y + 10,
                    rgb(WHITE)
            );
        }
    }
}