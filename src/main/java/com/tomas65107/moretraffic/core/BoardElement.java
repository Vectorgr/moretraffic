package com.tomas65107.moretraffic.core;

import com.tomas65107.moretraffic.block.SignboardBlock;
import com.tomas65107.moretraffic.block.SignboardBlockEntity;
import com.tomas65107.moretraffic.rendering.helpers.LegacyCube;
import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.mcdragonlib.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec2;

import java.util.*;

import static com.tomas65107.moretraffic.helpers.HelperFunctions.roundUpToSupportedDigits;
import static com.tomas65107.moretraffic.rendering.MaterialValues.*;
import static com.tomas65107.moretraffic.rendering.TrafficLightBlockEntityRenderer.getBoostedTint;

public abstract class BoardElement {
    public float startX;
    public float startY;
    public abstract void render(BERGraphics<? extends BlockEntity> graphics);
    public abstract CompoundTag serialize();

    public static BoardElement deserialize(CompoundTag tag, HolderLookup.Provider registries) {
        return switch (BoardElementType.byId(tag.getString("Type"))) {
            case TEXT -> Text.deserialize(tag, registries);
            case BACKGROUND -> Background.deserialize(tag, registries);
            case SPRITE -> Sprite.deserialize(tag, registries);
        };
    }

    public enum BoardSizes {
        TINY(4, 4),
        SMALL(8, 6),
        MEDIUM(12, 10),
        FULL(16, 16);

        public final float sizeX;
        public final float sizeY;

        BoardSizes(int sizeX, int sizeY) {
            this.sizeX = sizeX;
            this.sizeY = sizeY;
        }
    }

    public enum BoardElementType {
        TEXT("Text", Text.class, new Text(0, 0, "\"\"", 3)),
        BACKGROUND("Background", Background.class, new Background(0,0, DyeColor.WHITE)),
        SPRITE("Sprite",  Sprite.class, new Sprite(0, 0));

        public final String id;
        public final Class<? extends BoardElement> clazz;
        public final Object defaultValue;

        BoardElementType(String id, Class<? extends BoardElement> clazz, Object newValue) {
            this.id = id;
            this.clazz = clazz;
            this.defaultValue = newValue;
        }

        public static BoardElementType byClass(Class<? extends BoardElement> classToFind) {
            for (BoardElementType type : values()) {
                if (type.clazz.equals(classToFind)) return type;
            }
            throw new IllegalArgumentException("No element found for class " + classToFind);
        }

        public static BoardElementType byId(String id) {
            for (BoardElementType type : values()) {
                if (type.id.equals(id)) return type;
            }
            throw new IllegalArgumentException("Unknown BoardElementType: " + id);
        }

        public Component getComponentOfProperty() {
            return switch(this) {
                case TEXT -> Component.translatable("gui.moretraffic.signboard.element.text");
                case BACKGROUND -> Component.translatable("gui.moretraffic.signboard.element.bg");
                case SPRITE -> Component.translatable("gui.moretraffic.signboard.element.sprite");
                case null, default -> throw new IllegalArgumentException("Unknown BoardElementType: " + id);
            };
        }
    }

    private BoardElement(float startX, float startY) {
        this.startX = startX;
        this.startY = startY;
    }

    public static class Text extends BoardElement {
        public String json;
        public float size;

        public Text(float startX, float startY, String json, float size) {
            super(startX, startY);
            this.json = json;
            this.size = size;
        }

        @Override
        public void render(BERGraphics<?> graphics) {
            Font font = Minecraft.getInstance().font;
            SignboardBlockEntity board = (SignboardBlockEntity) graphics.blockEntity();

            graphics.poseStack().pushPose();

            try {
                float bgWidth = 16f;
                float bgHeight = 16f;

                if (!board.elements.isEmpty()
                        && board.elements.getLast() instanceof Background background) {

                    bgWidth = 16f - background.startX;
                    bgHeight = 16f - background.startY;
                }

                float bgLeft = (16f - bgWidth) / 2f;
                float bgTop = (16f - bgHeight) / 2f;

                // startX/startY are relative to the BG's top-left corner.
                float textX = bgLeft + startX;
                float textY = bgTop + startY;

                float scale = size / 8f;

                Component component = Objects.requireNonNull(
                        Component.Serializer.fromJson(
                                json,
                                Minecraft.getInstance().level.registryAccess()
                        )
                );

                int visibleCharacters = getVisibleCharacterCount(
                        font,
                        component.getString(),
                        bgWidth,
                        bgHeight,
                        startX,
                        startY,
                        size
                );

                if (visibleCharacters > 0) {
                    graphics.poseStack().translate(textX, textY-15.7, 0);
                    graphics.poseStack().scale(scale, scale, 1);

                    font.drawInBatch(
                            substringComponent(component, visibleCharacters),
                            0,
                            0,
                            0xFFFFFF,
                            false,
                            graphics.poseStack().last().pose(),
                            graphics.multiBufferSource(),
                            Font.DisplayMode.NORMAL,
                            0,
                            graphics.packedLight()
                    );
                }

            } catch (Exception ignored) {
            }

            graphics.poseStack().popPose();
        }

        private static Component substringComponent(Component component, int length) {
            MutableComponent result = Component.empty();

            final int[] remaining = {length};

            component.visit((style, text) -> {
                if (remaining[0] <= 0) {
                    return Optional.of(Unit.INSTANCE);
                }

                int take = Math.min(text.length(), remaining[0]);

                if (take > 0) {
                    result.append(
                            Component.literal(text.substring(0, take))
                                    .withStyle(style)
                    );

                    remaining[0] -= take;
                }

                return Optional.empty();
            }, Style.EMPTY);

            return result;
        }

        private static int getVisibleCharacterCount(Font font, String text, float bgWidth, float bgHeight, float textStartX, float textStartY, float textScale) {
            float bgLeft = (16f - bgWidth) / 2f;
            float bgRight = (16f + bgWidth) / 2f;

            float bgTop = (16f - bgHeight) / 2f;
            float bgBottom = (16f + bgHeight) / 2f;

            float textX = bgLeft + textStartX;
            float textY = bgTop + textStartY;

            float scale = textScale / 8f;

            // Y clipping.
            if (textY + font.lineHeight * scale > bgBottom+1) {
                return 0;
            }

            float availableWidth = bgRight - textX;

            if (availableWidth <= 0f) {
                return 0;
            }

            float maxTextWidth = availableWidth / scale;

            float currentWidth = 0f;

            for (int i = 0; i < text.length(); i++) {
                float charWidth = font.width(String.valueOf(text.charAt(i)));

                if (currentWidth + charWidth > maxTextWidth) {
                    return i;
                }

                currentWidth += charWidth;
            }

            return text.length();
        }


        @Override
        public CompoundTag serialize() {
            assert Minecraft.getInstance().level != null;
            CompoundTag tag = new CompoundTag();
            tag.putString("Type", "Text");
            tag.putFloat("startX", roundUpToSupportedDigits(startX));
            tag.putFloat("startY", roundUpToSupportedDigits(startY));
            tag.putFloat("size", size);
            tag.putString("json", json);
            return tag;
        }

        public static Text deserialize(CompoundTag tag, HolderLookup.Provider registries) {
            assert Minecraft.getInstance().level != null;
            float x = tag.getFloat("startX");
            float y = tag.getFloat("startY");
            float size = tag.getFloat("size");
            String json = tag.getString("json");
            return new Text(x, y, json, size);
        }
    }

    public static class Background extends BoardElement {
        public DyeColor color;

        public Background(float startX, float startY, DyeColor color) {
            super(startX, startY);
            this.color = color;
        }

        @Override
        public void render(BERGraphics<? extends BlockEntity> graphics) {
            float width = 16f - startX;
            float height = 16f - startY;

            graphics.poseStack().translate(startX / 2f, -startY / 2f, 0);

            LegacyCube light = LegacyCube.cube(
                    ResourceLocation.fromNamespaceAndPath("moretraffic", "textures/block/light_on_solid.png"),
                    width, height, 0f,
                    dir -> dir == Direction.SOUTH || dir == Direction.NORTH,
                    dir -> Pair.of(new Vec2(0f, 0f), new Vec2(1f, 1f))
            );
            light.setLight(graphics.packedLight());
            light.setTint(color.getTextureDiffuseColor());
            light.render(graphics);
        }

        @Override
        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Type", "Background");
            tag.putFloat("startX", roundUpToSupportedDigits(startX));
            tag.putFloat("startY", roundUpToSupportedDigits(startY));
            tag.putString("color", color.getSerializedName());
            return tag;
        }

        public static Background deserialize(CompoundTag tag, HolderLookup.Provider registries) {
            assert Minecraft.getInstance().level != null;
            float x = tag.getFloat("startX");
            float y = tag.getFloat("startY");
            DyeColor color = DyeColor.byName(tag.getString("color"), DyeColor.BLACK);
            return new Background(x, y, color);
        }
    }

    public static class Sprite extends BoardElement {
        public List<DyeColor> rows;

        public Sprite(float startX, float startY) {
            super(startX, startY);
            this.rows = new ArrayList<>(Collections.nCopies(256, DyeColor.BLACK));
        }

        public Sprite(int startX, int startY, List<DyeColor> rows) {
            super(startX, startY);
            this.rows = rows;
        }

        public DyeColor get(int x, int y) {
            if (x < 0 || x >= 16 || y < 0 || y >= 16) throw new IndexOutOfBoundsException("Pixel getter input out of bounds");
            return rows.get(y * 16 + x);
        }

        public void set(int x, int y, DyeColor color) {
            if (x < 0 || x >= 16 || y < 0 || y >= 16) throw new IndexOutOfBoundsException("Pixel getter input out of bounds");
            rows.set(y * 16 + x, color);
        }

        @Override
        public void render(BERGraphics<? extends BlockEntity> guiGraphics) {

        }

        @Override
        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Type", "Sprite");
            tag.putFloat("startX", roundUpToSupportedDigits(startX));
            tag.putFloat("startY", roundUpToSupportedDigits(startY));

            StringBuilder sb = new StringBuilder(256);
            for (DyeColor pixel : rows) {
                sb.append(Integer.toHexString(pixel.getId()));
            }
            tag.putString("rows", sb.toString());

            return tag;
        }

        public static Sprite deserialize(CompoundTag tag, HolderLookup.Provider registries) {
            assert Minecraft.getInstance().level != null;
            float x = tag.getFloat("startX");
            float y = tag.getFloat("startY");
            String data = tag.getString("rows");

            if (data.length() != 256) throw new IllegalArgumentException("Invalid pixel data length: " + data.length());

            Sprite pixels = new Sprite(x, y);
            for (int i = 0; i < 256; i++) {
                int id = Character.digit(data.charAt(i), 16);
                pixels.rows.set(i, DyeColor.byId(id));
            }
            return pixels;
        }
    }
}
