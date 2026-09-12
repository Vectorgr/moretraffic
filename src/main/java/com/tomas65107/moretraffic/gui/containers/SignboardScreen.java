package com.tomas65107.moretraffic.gui.containers;

import com.tomas65107.moretraffic.block.SignboardBlock;
import com.tomas65107.moretraffic.block.SignboardBlockEntity;
import com.tomas65107.moretraffic.core.AbstractSheet;
import com.tomas65107.moretraffic.core.BoardElement;
import com.tomas65107.moretraffic.core.SpritesManager;
import com.tomas65107.moretraffic.gui.AbstractTomiContainerScreen;
import com.tomas65107.moretraffic.gui.components.*;
import com.tomas65107.moretraffic.gui.components.buttons.AdvancedButton;
import com.tomas65107.moretraffic.gui.components.buttons.ColorButton;
import com.tomas65107.moretraffic.gui.makers.GridMaker;
import com.tomas65107.moretraffic.gui.tooltip.NoticeBoxTooltip;
import com.tomas65107.moretraffic.mod.MoreTraffic;
import com.tomas65107.moretraffic.networking.ClientSyncSignboard;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.tomas65107.moretraffic.core.ColorsManager.*;
import static com.tomas65107.moretraffic.core.SpritesManager.INFO;
import static com.tomas65107.moretraffic.core.SpritesManager.PALETTE;
import static com.tomas65107.moretraffic.core.SpritesManager.WARNING;
import static com.tomas65107.moretraffic.gui.components.buttons.AdvancedButton.NORMAL_HEIGHT;
import static com.tomas65107.moretraffic.helpers.ColorHelper.rgb;
import static com.tomas65107.moretraffic.helpers.HelperFunctions.ff;
import static com.tomas65107.moretraffic.helpers.HelperFunctions.roundUpToSupportedDigits;
import static com.tomas65107.moretraffic.helpers.TextCutter.cutTextComponent;
import static com.tomas65107.moretraffic.helpers.TextHelper.Alignment.CENTER;
import static com.tomas65107.moretraffic.helpers.TextHelper.align;
import static net.neoforged.neoforge.network.PacketDistributor.sendToServer;

public class SignboardScreen extends AbstractTomiContainerScreen<SignboardMenu> {

    private final SignboardMenu menu;
    private final BlockPos pos;
    private final SignboardBlockEntity be;

    private int guiX;
    private int guiY;

    protected boolean queueRefresh;
    private int timer = 0;

    private boolean OOBEVisible;
    boolean toggleoobe = false;

    boolean TEXT_showingRendered = true;
    boolean TEXT_showingMoreColors = false;

    int guiWidth = 191;
    int guiHeight = 164;

    AtomicInteger focusedIndex = new AtomicInteger(-1);

    BlockEntityRenderDispatcher beDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();

    public SignboardScreen(SignboardMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.menu = menu;
        this.be = menu.be;
        this.pos = be.getBlockPos();
        OOBEVisible = be.firstPlace;
    }

    @Override
    protected void init() {
        super.init();

        if (be.elements.size() > 7) throw new IllegalArgumentException("Tried to access Signboard elements that overflow max limit!");

        guiX = ((this.width - guiWidth) / 2);
        guiY = (this.height - guiHeight) / 2;

        assert Minecraft.getInstance().level != null;
        assert Minecraft.getInstance().level.isClientSide;
        assert be != null;

        if (OOBEVisible && !toggleoobe) {toggleoobe = true; showOOBE(be.elements.size()-1);}
        if (OOBEVisible) return;

        addBaseWidget(
                new LabelWidget(guiX+10, guiY +10, Component.translatable("gui.moretraffic.signboard.title").withStyle(ChatFormatting.BOLD), 0xFFFFFF, true)
        );

        NoticeBoxTooltip tooltip1 = new NoticeBoxTooltip(
                Component.translatable("gui.moretraffic.signboard.title"),
                Component.translatable("gui.moretraffic.signboard.message"),
                null, NoticeBoxTooltip.TooltipType.INFORMATIVE);
        int finalWidthOfPrevContent = Minecraft.getInstance().font.width(Component.translatable("gui.moretraffic.signboard.title").withStyle(ChatFormatting.BOLD));
        addBaseWidget(
                new HelpElementWidget(guiX+finalWidthOfPrevContent + 10, guiY +6, INFO, tooltip1)
        );

        if (focusedIndex.get() == -1) {
            int offset = guiY +43;
            for (Component component : cutTextComponent(Component.translatable("gui.moretraffic.signboard.none_selected"), 0, 85, true)) {
                addBaseWidget(new LabelWidget(align(CENTER, component, (this.width/2)-46), offset, component, rgb(PRIMARY), true));
                offset += 10;
            }
        }

        if (focusedIndex.get() != -1) {
            int YOffset = 30;
            var element = be.elements.get(focusedIndex.get());

            addBaseWidget(new LabelWidget(guiX+10, guiY+YOffset, Component.translatable("gui.moretraffic.led_light.startpos").withColor(rgb(SECONDARY)), 0xFFFFFF, true));
            YOffset += 11;

            BetterEditBox startposbox = new NumberEditBox(guiX+10, guiY + YOffset);
            startposbox.setValue(element.startX + ", " + element.startY);
            startposbox.onSave(() -> {
                try {
                    String[] parts = startposbox.getValue().split(",");

                    float x = roundUpToSupportedDigits(Float.parseFloat(parts[0].trim()));
                    float y = roundUpToSupportedDigits(Float.parseFloat(parts[1].trim()));

                    if (!(x <= 16 && y <= 16 && x >= 0 && y >= 0)) throw new Exception();

                    element.startX = x; element.startY = y;
                    updateBEAndRefreshBE();
                } catch (Exception ignored) {}
            });

            startposbox.onChange(text -> {
                try {
                    String[] parts = text.split(",");

                    float x = roundUpToSupportedDigits(Float.parseFloat(parts[0].trim()));
                    float y = roundUpToSupportedDigits(Float.parseFloat(parts[1].trim()));

                    if (!(x <= 16 && y <= 16 && x >= 0 && y >= 0)) throw new Exception();

                    startposbox.setTextColor(rgb(PRIMARY));
                    startposbox.hideFloatingTooltip();
                } catch (Exception e) {
                    startposbox.setTextColor(rgb(ERROR));
                    startposbox.showFloatingTooltip(new NoticeBoxTooltip(Component.translatable("gui.moretraffic.error_saving"), Component.literal("Start position incorrectly formated or is outside bounds!"), NoticeBoxTooltip.TooltipType.ERROR));
                }
            });
            addBaseWidget(startposbox);
            YOffset += 20;


            if (element instanceof BoardElement.Background) {


                // add presets for position (oobe)
                NoticeBoxTooltip tooltipwarning = new NoticeBoxTooltip(
                        Component.literal("Recent update introduced changes regarding Signboard position"),
                        Component.literal("RECOMMENDED USAGE: Use the preset size picker.\nYou can also manually input the numbers but that may present some errors."),
                        null, NoticeBoxTooltip.TooltipType.WARNING);
                addBaseWidget(
                        new HelpElementWidget(guiX+10+66, guiY +39, WARNING, tooltipwarning)
                );

                addBaseWidget(new AdvancedButton(guiX+58, guiY+40-2, 18, 18, null, SpritesManager.LIBRARY, new NoticeBoxTooltip(Component.literal("Show preset sizes (recommended)")), button -> showOOBE(focusedIndex.get())));

                addBaseWidget(new LabelWidget(guiX+10, guiY+YOffset, Component.literal("Color").withColor(rgb(SECONDARY)), 0xFFFFFF, true));
                YOffset += 11;
                addBaseWidget(new ColorButton(guiX+10, guiY+YOffset, 16, 16, ((BoardElement.Background) element).color.getTextureDiffuseColor(), b->{
                    int sheetWidth = 173;
                    int sheetHeight = 94;
                    int sheetX = guiX + (guiWidth - sheetWidth) / 2;
                    int sheetY = guiY + (guiHeight - sheetHeight) / 2;
                    this.addScreenSheet(
                            new AbstractSheet(sheetX, sheetY, Component.literal("Select color").getString(), true, sheetWidth, sheetHeight) {
                                @Override
                                public void init(Consumer<AbstractWidget> adder) {
                                    new GridMaker(10, 25, adder, c -> {
                                        ((BoardElement.Background) be.elements.get(focusedIndex.get())).color = c; updateBEAndRefreshBE(); refreshContent();
                                    }, ((BoardElement.Background) be.elements.get(focusedIndex.get())).color, true);
                                }
                            });
                }, false, true));
                YOffset += 20;

            } else if (element instanceof BoardElement.Text) {

                addBaseWidget(new AdvancedButton(guiX+58, guiY+40-2, 18, 18, null, SpritesManager.CENTER, new NoticeBoxTooltip(Component.translatable("gui.moretraffic.led_light.center"), Component.literal("Center text horizontally and apply centered position")), button -> {
                    var textElement = (BoardElement.Text) be.elements.get(focusedIndex.get());
                    int widthOfTextAdjusted = Minecraft.getInstance().font.width(Objects.requireNonNull(Component.Serializer.fromJson(textElement.json, Minecraft.getInstance().level.registryAccess())));

                    float textScale = textElement.size / 8f;

                    textElement.startX = 8f - widthOfTextAdjusted * textScale / 2f;
                    updateBEAndRefreshBE();
                }));

                addBaseWidget(new LabelWidget(guiX+10, guiY+YOffset, Component.literal("Text").withColor(rgb(SECONDARY)), 0xFFFFFF, true));
                YOffset += 11;

                addBaseWidget(new AdvancedButton(guiX+10, guiY+YOffset, 80, NORMAL_HEIGHT, Component.translatable("gui.moretraffic.configure").withColor(rgb(HEADER)), SpritesManager.EDIT_DISPLAY, p->{
                    int sheetWidth = 270;
                    int sheetHeight = 150;
                    int sheetX = guiX + (guiWidth - sheetWidth) / 2;
                    int sheetY = guiY + (guiHeight - sheetHeight) / 2;

                    this.addScreenSheet(
                            new AbstractSheet(sheetX, sheetY, Component.literal("Configure text").getString(), true, sheetWidth, sheetHeight) {
                                @Override
                                public void init(Consumer<AbstractWidget> adder) {

                                    final int[] offset = {25};
                                    for (Component component : cutTextComponent(Component.literal("Type in your text, or if you want to customize color and more, use JSON format"), 0, 250, true)) {
                                        adder.accept(new LabelWidget(10, offset[0], component.copy().withColor(rgb(SECONDARY)), rgb(PRIMARY), true));
                                        offset[0] += 10;
                                    }
                                    offset[0] += 5;

                                    BetterEditBox texteditbox;
                                    var valueComponent = Objects.requireNonNull(Component.Serializer.fromJson(((BoardElement.Text) be.elements.get(focusedIndex.get())).json, Minecraft.getInstance().level.registryAccess()));

                                    if (!TEXT_showingRendered) {
                                        texteditbox = new BetterEditBox(10, offset[0], 250, NORMAL_HEIGHT);
                                        texteditbox.setMaxLength(900);
                                        texteditbox.setValue(((BoardElement.Text) be.elements.get(focusedIndex.get())).json);
                                        texteditbox.setTextColor(rgb(145, 165, 255));

                                        texteditbox.onSave(() -> {
                                            if (getJsonParseErrorMessage(texteditbox.getValue()) == null) {
                                                String json = new String(texteditbox.getValue()); // force snapshot copy

                                                ((BoardElement.Text) be.elements.get(focusedIndex.get())).json = json;
                                                updateBEAndRefreshBE();
                                            }
                                        });

                                    } else {
                                        texteditbox = new RichTextEditBox(10, offset[0], 250, NORMAL_HEIGHT);
                                        texteditbox.setMaxLength(900);
                                        ((RichTextEditBox) texteditbox).setRichValue(valueComponent);

                                        texteditbox.onSave(() -> {
                                            String json = new String(Component.Serializer.toJson(((RichTextEditBox) texteditbox).getRichValue(), Minecraft.getInstance().level.registryAccess())); // force snapshot copy

                                            ((BoardElement.Text) be.elements.get(focusedIndex.get())).json = json;
                                            updateBEAndRefreshBE();
                                        });

                                    }
                                    texteditbox.onChange((s -> {
                                        if (getJsonParseErrorMessage(s) == null) {
                                            texteditbox.hideFloatingTooltip();
                                            texteditbox.setTextColor(rgb(PRIMARY));
                                        } else {
                                            texteditbox.showFloatingTooltip(new NoticeBoxTooltip(Component.translatable("gui.moretraffic.error_saving"), Component.nullToEmpty(getJsonParseErrorMessage(s)), NoticeBoxTooltip.TooltipType.ERROR));
                                            texteditbox.setTextColor(rgb(ERROR));
                                        }
                                    }));
                                    adder.accept(texteditbox);

                                    offset[0] += 26;

                                    adder.accept(new LabelWidget(10, offset[0], Component.translatable("gui.moretraffic.signboard.quick_actions.title").withColor(rgb(SECONDARY)), rgb(PRIMARY), true));
                                    adder.accept(new HelpElementWidget(10+Minecraft.getInstance().font.width(Component.translatable("gui.moretraffic.signboard.quick_actions.title")), offset[0]-5, WARNING, new NoticeBoxTooltip(Component.translatable("gui.moretraffic.signboard.quick_actions.warning"), Component.translatable("gui.moretraffic.signboard.quick_actions.message"), NoticeBoxTooltip.TooltipType.WARNING)));
                                    offset[0] += 11;

                                    var buttonSpacing = NORMAL_HEIGHT + 1;

                                    adder.accept(new AdvancedButton(10, offset[0], NORMAL_HEIGHT, NORMAL_HEIGHT, Component.literal("B").withStyle(ChatFormatting.BOLD), (NoticeBoxTooltip) null, b->{
                                        ((BoardElement.Text) be.elements.get(focusedIndex.get())).json = Component.Serializer.toJson(valueComponent.copy().withStyle(ChatFormatting.BOLD), Minecraft.getInstance().level.registryAccess());
                                        updateBEAndRefreshBE();
                                    }).setCenterText(true));
                                    adder.accept(new AdvancedButton(buttonSpacing+10, offset[0], NORMAL_HEIGHT, NORMAL_HEIGHT, Component.literal("I").withStyle(ChatFormatting.ITALIC), (NoticeBoxTooltip) null, b->{
                                        ((BoardElement.Text) be.elements.get(focusedIndex.get())).json = Component.Serializer.toJson(valueComponent.copy().withStyle(ChatFormatting.ITALIC), Minecraft.getInstance().level.registryAccess());
                                        updateBEAndRefreshBE();
                                    }).setCenterText(true));
                                    adder.accept(new AdvancedButton((buttonSpacing*2)+10, offset[0], NORMAL_HEIGHT, NORMAL_HEIGHT, Component.literal("U").withStyle(ChatFormatting.UNDERLINE), (NoticeBoxTooltip) null, b->{
                                        ((BoardElement.Text) be.elements.get(focusedIndex.get())).json = Component.Serializer.toJson(valueComponent.copy().withStyle(ChatFormatting.UNDERLINE), Minecraft.getInstance().level.registryAccess());
                                        updateBEAndRefreshBE();
                                    }).setCenterText(true));
                                    adder.accept(new AdvancedButton((buttonSpacing*3)+10, offset[0], NORMAL_HEIGHT, NORMAL_HEIGHT, PALETTE, null, false,b->{
                                        TEXT_showingMoreColors = !TEXT_showingMoreColors;
                                        refreshContent();
                                    }));
                                    adder.accept(new AdvancedButton((buttonSpacing*4)+10+2, offset[0], NORMAL_HEIGHT, NORMAL_HEIGHT, Component.literal("X").withStyle(ChatFormatting.RED), (NoticeBoxTooltip) null, b->{
                                        ((BoardElement.Text) be.elements.get(focusedIndex.get())).json = Component.Serializer.toJson(valueComponent.copy().withStyle(ChatFormatting.RESET), Minecraft.getInstance().level.registryAccess());
                                        updateBEAndRefreshBE();
                                    }).setCenterText(true));

                                    adder.accept(new AdvancedButton(sheetWidth-90-10, offset[0], 90, NORMAL_HEIGHT, TEXT_showingRendered ? Component.literal("Rendered").withColor(rgb(PRIMARY)) : Component.literal("Raw JSON").withColor(rgb(PRIMARY)), SpritesManager.TEXT_SHIFT , new NoticeBoxTooltip(Component.literal("Currently showing " + (TEXT_showingRendered ? "Rendered" : "Raw JSON")), null, Component.translatable("gui.moretraffic.change"), null), b->{
                                        TEXT_showingRendered = !TEXT_showingRendered;
                                        refreshContent();
                                    }));

                                    offset[0] += 23;

                                    if (TEXT_showingMoreColors) {
                                        new GridMaker(10, offset[0], adder, c -> {
                                            ((BoardElement.Text) be.elements.get(focusedIndex.get())).json = Component.Serializer.toJson(valueComponent.copy().withColor(c.getTextColor()), Minecraft.getInstance().level.registryAccess());
                                            updateBEAndRefreshBE();
                                            refreshContent();
                                        }, null, true);
                                    }

                                }

//                                static String adaptString(String string, boolean presentToUser) {
//                                    boolean isTheStringJson = !(string.startsWith("\"") && string.endsWith("\""));
//                                    if (isTheStringJson) {
//                                        return string;
//                                    } else {
//                                        if (presentToUser) {
//                                            return string.substring(1, string.length()-1);
//                                        } else {
//                                            return "\""+string+"\"";
//                                        }
//                                    }
//                                }

                                static String getJsonParseErrorMessage(String json) {
                                    try {
                                        Component.Serializer.fromJson(json, Minecraft.getInstance().level.registryAccess());
                                        return null; // valid
                                    } catch (Exception e) {
                                        return e.getCause() != null ? e.getCause().getMessage() : e.getMessage(); // or custom friendly message
                                    }
                                }
                            }
                    );
                }));
                YOffset += 26;

                addBaseWidget(new LabelWidget(guiX+10, guiY+YOffset, Component.literal("Text Size").withColor(rgb(SECONDARY)), 0xFFFFFF, true));
                YOffset += 11;
                var sizebox = new NumberEditBox(guiX+10, guiY+YOffset);
                sizebox.setValue(String.valueOf(((BoardElement.Text) element).size));
                sizebox.onSave(() -> {
                    try {
                        if (Float.parseFloat(sizebox.getValue()) > 15 || 0.5 > Float.parseFloat(sizebox.getValue())) throw new NumberFormatException();
                        ((BoardElement.Text) element).size = Float.parseFloat(sizebox.getValue());
                        updateBEAndRefreshBE();
                    } catch (NumberFormatException ignored) {}
                });
                sizebox.onChange(text -> {
                    try {
                        if (Float.parseFloat(text) > 15 || 0.5 > Float.parseFloat(text)) throw new NumberFormatException();

                        sizebox.setTextColor(rgb(PRIMARY));
                        sizebox.hideFloatingTooltip();
                    } catch (NumberFormatException ignored) {
                        sizebox.setTextColor(rgb(ERROR));
                        sizebox.showFloatingTooltip(new NoticeBoxTooltip(Component.translatable("gui.moretraffic.error_saving"), Component.literal("Invalid number or not an float number!"), NoticeBoxTooltip.TooltipType.ERROR));
                    }
                });
                addBaseWidget(sizebox);
                addBaseWidget(new AdvancedButton(guiX+58, guiY+YOffset, 12, 12, SpritesManager.TEXT_SMALLER, new NoticeBoxTooltip(Component.literal("Smaller")), true, button -> {
                    sizebox.setValue(String.valueOf(Float.parseFloat(sizebox.getValue()) - 0.5f));
                    sizebox.triggerSaveCode();
                }));
                addBaseWidget(new AdvancedButton(guiX+58+15, guiY+YOffset, 12, 12, SpritesManager.TEXT_BIGGER, new NoticeBoxTooltip(Component.literal("Bigger")), true, button -> {
                    sizebox.setValue(String.valueOf(Float.parseFloat(sizebox.getValue()) + 0.5f));
                    sizebox.triggerSaveCode();
                }));


            }
        }

        int index = 0;
        for (var element : be.elements) {
            int finalIndex = index;
            addBaseWidget(
                    new InLineActionLabel(guiX+97, guiY+40+(finalIndex*13),
                    BoardElement.BoardElementType.byClass(element.getClass()).getComponentOfProperty().copy().withStyle(isTheMainBg(finalIndex) ? ChatFormatting.BOLD : ChatFormatting.RESET),
                    rgb(PRIMARY),
                    true,
                    !isTheMainBg(finalIndex) ? List.of(
                            new AdvancedButton(0, 0, 0, 0, SpritesManager.ICON_TRASHCAN, safeToRenderTooltips() ? new NoticeBoxTooltip(Component.translatable("gui.moretraffic.signboard.element.delete")): null, true, a->{
                                if (finalIndex < be.elements.size()) be.elements.remove(element);
                                updateBEAndRefreshBE();
                            }),
                            new AdvancedButton(0, 0, 0, 0, SpritesManager.MOVE, safeToRenderTooltips() ? new NoticeBoxTooltip(Component.translatable("gui.moretraffic.signboard.element.move")) : null, true, a->{
                                if (finalIndex > 0) {
                                    Collections.swap(be.elements, finalIndex, finalIndex - 1);
                                    updateBEAndRefreshBE();
                                }
                            }),
                            new AdvancedButton(0, 0, 0, 0, SpritesManager.EXPORT, safeToRenderTooltips() ? new NoticeBoxTooltip(Component.translatable("gui.moretraffic.signboard.element.duplicate")) : null, true, a->{
                                if (be.elements.size() > 6) return;
                                be.elements.add(finalIndex, be.elements.get(finalIndex));
                                updateBEAndRefreshBE();
                            })
                    ) : List.of(),
                    82, () -> {
                        if (focusedIndex.get() == finalIndex) {
                            focusedIndex.set(-1);
                        } else {
                            focusedIndex.set(finalIndex);
                        }
                        queueRefresh = true;
                    }, (finalIndex == focusedIndex.get())
                    )
            );
            index++;
        }

        addBaseWidget(new AdvancedButton(guiX+51, guiY+139, 15, 15, SpritesManager.PLUS, new NoticeBoxTooltip(Component.translatable("gui.moretraffic.signboard.plus")), true, b->{
            int sheetWidth = 150;
            int sheetHeight = 130;

            int sheetX = guiX + (guiWidth - sheetWidth) / 2;
            int sheetY = guiY + (guiHeight - sheetHeight) / 2;

            this.addScreenSheet(
                    new AbstractSheet(sheetX, sheetY, Component.translatable("gui.moretraffic.signboard.plus").getString(), true, sheetWidth, sheetHeight) {
                        @Override
                        public void init(Consumer<AbstractWidget> adder) {
                            int offsetY = 25;

                            for (BoardElement.BoardElementType property : BoardElement.BoardElementType.values()) {
                                if (property.equals(BoardElement.BoardElementType.SPRITE)) continue;
                                adder.accept(new AdvancedButton(10, offsetY, 130, NORMAL_HEIGHT, property.getComponentOfProperty(), new NoticeBoxTooltip(property.getComponentOfProperty()), b -> {
                                    if (be.elements.size() > 6) return;
                                    be.elements.addFirst((BoardElement) property.defaultValue);
                                    updateBEAndRefreshBE();
                                    onClose();
                                }));
                                offsetY += 23;
                            }

                        }
                    }
            );
        }));

        addBaseWidget(new AdvancedButton(guiX+10, guiY+139, 15, 15, SpritesManager.IMPORT, new NoticeBoxTooltip(Component.translatable("gui.moretraffic.control_cabinet.import")), true, b->{

            int sheetWidth = 180;
            int sheetHeight = 90;

            int sheetX = guiX + (guiWidth - sheetWidth) / 2;
            int sheetY = guiY + (guiHeight - sheetHeight) / 2;

            this.addScreenSheet(
                    new AbstractSheet(sheetX, sheetY, Component.translatable("gui.moretraffic.control_cabinet.import").getString(), true, sheetWidth, sheetHeight) {

                        @Override
                        public void init(Consumer<AbstractWidget> adder) {

                            String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();

                            boolean validClipboardContents = false;
                            CompoundTag tagToSave = new CompoundTag();

                            if (!clipboard.isBlank()) {
                                try {

                                    CompoundTag parsed = TagParser.parseTag(clipboard);

                                    be.saveAdditional(tagToSave, Minecraft.getInstance().level.registryAccess());

                                    if (parsed.contains("elements")) {
                                        tagToSave.put("elements", parsed.get("elements").copy());
                                    }

                                    //everything finished without throwing
                                    validClipboardContents = true;

                                } catch (Exception e) {
                                    validClipboardContents = false;
                                }
                            }

                            var buttonSuccess = new AdvancedButton(10, 25, 160, NORMAL_HEIGHT, Component.translatable("gui.moretraffic.control_cabinet.import.from_clipboard"), null, null, b -> {
                                if (!tagToSave.isEmpty()) {
                                    be.read(tagToSave, Minecraft.getInstance().level.registryAccess(), true);
                                    updateBEAndRefreshBE();
                                    onClose();
                                }
                            });

                            if (validClipboardContents && (!tagToSave.isEmpty())) {
                                adder.accept(buttonSuccess);
                            } else {
                                int offset = 23;
                                for (Component component : cutTextComponent(Component.translatable("gui.moretraffic.import_invalid_data"), 0, 170, true)) {
                                    adder.accept(new LabelWidget(align(CENTER, component, sheetWidth/2), offset, component, rgb(ERROR), true));
                                    offset += 10;
                                }
                            }
                        }
                    });

        }));
        addBaseWidget(new AdvancedButton(guiX+26, guiY+139, 15, 15, SpritesManager.EXPORT, new NoticeBoxTooltip(Component.translatable("gui.moretraffic.control_cabinet.export")), true, b->{

            int sheetWidth = 180;
            int sheetHeight = 90;

            int sheetX = guiX + (guiWidth - sheetWidth) / 2;
            int sheetY = guiY + (guiHeight - sheetHeight) / 2;

            this.addScreenSheet(
                    new AbstractSheet(sheetX, sheetY, Component.translatable("gui.moretraffic.control_cabinet.export").getString(), true, sheetWidth, sheetHeight) {

                        AdvancedButton button;

                        @Override
                        public void init(Consumer<AbstractWidget> adder) {

                            adder.accept(new LabelWidget(10, 25, Component.translatable("gui.moretraffic.signboard.export_all"), rgb(SECONDARY), true));

                            button = new AdvancedButton(10, sheetHeight - 30, 100, NORMAL_HEIGHT, Component.translatable("gui.moretraffic.control_cabinet.export.to_clipboard"), null, null, b -> {
                                CompoundTag exportTag = new CompoundTag();
                                be.saveAdditional(exportTag, Minecraft.getInstance().level.registryAccess());

                                CompoundTag toCopy = new CompoundTag();

                                if (exportTag.contains("elements"))
                                    toCopy.put("elements", exportTag.get("elements").copy());

                                Minecraft.getInstance().keyboardHandler.setClipboard(toCopy.toString());
                                onClose();
                            });
                            adder.accept(button);
                        }

                    });
        }));


    }

    public void showOOBE(int indexToModify) { //def: be.elements.size()-1
        if (be.firstPlace) {
            be.firstPlace = false;
            updateBEAndRefreshBE();
        }

        int sheetWidth = 150;
        int sheetHeight = 160;

        int sheetX = guiX + (guiWidth - sheetWidth) / 2;
        int sheetY = guiY + (guiHeight - sheetHeight) / 2;

//        if (!isTheMainBg(be.elements.size()-1)) return;

        addScreenSheet(
                new AbstractSheet(sheetX, sheetY, Component.translatable("gui.moretraffic.signboard_oobe.title").getString(), true, sheetWidth, sheetHeight) {
                    @Override
                    public void init(Consumer<AbstractWidget> adder) {
                        int offsetY = 25;

                        DyeColor currentColor = ((BoardElement.Background)(be.elements.get(indexToModify))).color;

                        for (BoardElement.BoardSizes property : BoardElement.BoardSizes.values()) {
                            adder.accept(new AdvancedButton(10, offsetY, 130, NORMAL_HEIGHT, Component.literal(ff(property.sizeX) + "x" + ff(property.sizeY)), (NoticeBoxTooltip) null, b -> {
                                be.elements.set(indexToModify, new BoardElement.Background((16 - property.sizeX) / 2, (16 - property.sizeY) / 2, currentColor));
                                OOBEVisible = false;
                                updateBEAndRefreshBE();
                                onClose();
                            }));
                            offsetY += 23;
                        }

                    }
                }
        );
    }

    private void updateBEAndRefreshBE() {
        if (be == null || minecraft == null || minecraft.player == null) return;
        assert minecraft.level != null;

        // serialize BE state into NBT
        CompoundTag tag = new CompoundTag();
        be.saveAdditional(tag, minecraft.level.registryAccess());

        // create and send packet
        ClientSyncSignboard packet = new ClientSyncSignboard(be.getBlockPos(), tag);
        sendToServer(packet);

        //queue update on ui
        queueRefresh = true;
    }

    @Override
    protected void containerTick() {
        if (OOBEVisible && safeToRenderTooltips()) {
            OOBEVisible = false;
            refreshContent();
        }

        if (queueRefresh) {
            MoreTraffic.LOGGER.debug("queuing ui refresh...");
            queueRefresh = false;
            timer = 2;
        }
        if (timer == 0) {
            refreshContent();
            timer = -1;
        } else if (timer != -1) {
            timer--;
        }

        super.containerTick();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!OOBEVisible) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(guiX -150, guiY +137, 10);

            BlockState modelToRender = be.getBlockState().setValue(SignboardBlock.FACING, Direction.SOUTH);

            float scale = 127f;
            guiGraphics.pose().scale(scale, -scale, scale);

            BlockState originalBeBs = be.getBlockState();
            be.setBlockState(modelToRender);
            beDispatcher.render(be, 0f, guiGraphics.pose(), Minecraft.getInstance().renderBuffers().bufferSource());
            be.setBlockState(originalBeBs);

            guiGraphics.pose().popPose();
        }

        if (!(this.getFocused() instanceof EditBox) && focusedIndex.get() != -1 && safeToRenderTooltips()) {
            String text = "You can move selected element using the arrow keys on your keyboard.";

            int textWidth = Minecraft.getInstance().font.width(text);
            int padding = 6;

            int boxWidth = textWidth + padding * 2;
            int boxHeight = 16;

            int x = (guiGraphics.guiWidth() - boxWidth) / 2;
            int y = (guiGraphics.guiHeight() - 50);

            guiGraphics.fill(
                    x,
                    y,
                    x + boxWidth,
                    y + boxHeight,
                    rgb(0, 0, 0, 167)
            );

            int textX = x + padding;
            int textY = y + 4;

            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    text,
                    textX,
                    textY,
                    rgb(SECONDARY),
                    true
            );
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        if (OOBEVisible) return;

        ResourceLocation BG_TEXTURE =
                ResourceLocation.fromNamespaceAndPath("moretraffic", "textures/gui/signboard_gui.png");

        int x = (this.width - guiWidth) / 2;
        int y = (this.height - guiHeight) / 2;

        guiGraphics.blit(
                BG_TEXTURE,
                x, y,          // screen position
                0, 0,          // texture UV start
                guiWidth, guiHeight, // size to draw
                256, 256       // full texture size
        );

    }

    private boolean isTheMainBg(int index) {
        for (int i = be.elements.size() - 1; i >= 0; i--) {
            if (be.elements.get(i) instanceof BoardElement.Background) {
                return (i == index);
            }
        }
        throw new ArrayStoreException("Invalid array passed for arithmetic operation; does not contain Background"); // no background found
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.getFocused() instanceof EditBox || focusedIndex.get() == -1 || !safeToRenderTooltips()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        var elementF = be.elements.get(focusedIndex.get());

        switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> {
                if (elementF.startY-0.5f <= 16 && elementF.startY-0.5f >= 0) {
                    elementF.startY -= 0.5f;
                    updateBEAndRefreshBE();
                    return true;
                }
            }

            case GLFW.GLFW_KEY_DOWN -> {
                if (elementF.startY+0.5f <= 16 && elementF.startY+0.5f >= 0) {
                    elementF.startY += 0.5f;
                    updateBEAndRefreshBE();
                    return true;
                }
            }

            case GLFW.GLFW_KEY_LEFT -> {
                if (elementF.startX-0.5f <= 16 && elementF.startX-0.5f >= 0) {
                    elementF.startX -= 0.5f;
                    updateBEAndRefreshBE();
                    return true;
                }
            }

            case GLFW.GLFW_KEY_RIGHT -> {
                if (elementF.startX+0.5f <= 16 && elementF.startX+0.5f >= 0) {
                    elementF.startX += 0.5f;
                    updateBEAndRefreshBE();
                    return true;
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
