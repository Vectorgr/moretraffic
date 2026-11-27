package com.tomas65107.moretraffic.mod.registration;

import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.track.TrackTargetingBlockItem;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tomas65107.moretraffic.block.*;
import com.tomas65107.moretraffic.core.blocktypes.MTBaseColoredBlockEntity;
import com.tomas65107.moretraffic.item.GroupControllerItem;
import com.tomas65107.moretraffic.mod.MoreTraffic;
import com.tomas65107.moretraffic.mod.compat.displaylink.CabinetDisplayTarget;
import com.tomas65107.moretraffic.mod.compat.displaylink.SignalOccupancyDisplaySource;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;

import static com.tomas65107.moretraffic.core.blocktypes.MTBaseColoredBlockEntity.getValidHookedBlocks;

public class MTRegistrate {
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MoreTraffic.MODID);

    public static final BlockEntry<AdvancedTrafficLightBlock> ADV_3_TRAFFIC_LIGHT = datagen(REGISTRATE.block("advanced_3_traffic_light", AdvancedTrafficLightBlock::new)).register();
    public static final BlockEntry<AdvancedTrafficLightBlock> ADV_2_TRAFFIC_LIGHT = datagen(REGISTRATE.block("advanced_2_traffic_light", AdvancedTrafficLightBlock::new)).register();
    public static final BlockEntry<AdvancedTrafficLightBlock> ADV_1_TRAFFIC_LIGHT = datagen(REGISTRATE.block("advanced_1_traffic_light", AdvancedTrafficLightBlock::new)).register();
    public static final BlockEntityEntry<AdvancedTrafficLightBlockEntity> ADV_TRAFFIC_LIGHT_BE = REGISTRATE
            .blockEntity("advanced_traffic_light", AdvancedTrafficLightBlockEntity::new)
            .validBlocks(ADV_3_TRAFFIC_LIGHT, ADV_2_TRAFFIC_LIGHT, ADV_1_TRAFFIC_LIGHT)
            .register();


    public static final BlockEntry<TrafficDisplayBlock> TRAFFIC_DISPLAY = datagen(REGISTRATE
            .block("traffic_display", TrafficDisplayBlock::new)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .properties(p -> p
                    .strength(3.0f, 5.0f)
                    .requiresCorrectToolForDrops()
                    .mapColor(DyeColor.GRAY)
            )
            .item()    .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegisterAfter(Registries.ITEM, item ->
                    TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)))
            .removeTab(CreativeModeTabs.SEARCH)
            .build()
    ).register();
    public static final BlockEntityEntry<TrafficDisplayBlockEntity> TRAFFIC_DISPLAY_BE = REGISTRATE
            .blockEntity("traffic_display", TrafficDisplayBlockEntity::new)
            .validBlock(TRAFFIC_DISPLAY)
            .register();


    public static final BlockEntry<FlashingBlinkerBlock> BLINKER = datagen(REGISTRATE
            .block("blinker", FlashingBlinkerBlock::new)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .properties(p -> p
                    .strength(0.8f, 0.8f)
            )
            .item()    .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegisterAfter(Registries.ITEM, item ->
                    TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)))
            .removeTab(CreativeModeTabs.SEARCH)
            .build()
    ).register();
    public static final BlockEntityEntry<FlashingBlinkerBlockEntity> BLINKER_BE = REGISTRATE
            .blockEntity("blinker", FlashingBlinkerBlockEntity::new)
            .validBlock(BLINKER)
            .register();


    public static final BlockEntry<FloodlightBlock> FLOODLIGHT = datagen(REGISTRATE
            .block("floodlight", FloodlightBlock::new)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .properties(p -> p
                    .strength(2.0f, 5.0f)
                    .requiresCorrectToolForDrops()
                    .mapColor(DyeColor.GRAY)
            )
            .item()    .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegisterAfter(Registries.ITEM, item ->
                    TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)))
            .removeTab(CreativeModeTabs.SEARCH)
            .build()
    ).register();
    public static final BlockEntityEntry<FloodlightBlockEntity> FLOODLIGHT_BE = REGISTRATE
            .blockEntity("floodlight", FloodlightBlockEntity::new)
            .validBlock(FLOODLIGHT)
            .register();


    public static final BlockEntry<LightControlCabinetBlock> LIGHT_CONTROL_CABINET = datagen(REGISTRATE
            .block("control_cabinet", LightControlCabinetBlock::new)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .properties(p -> p
                    .strength(3.3f, 7.0f)
                    .requiresCorrectToolForDrops()
                    .mapColor(DyeColor.LIGHT_GRAY)
            )
            .item()    .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .removeTab(CreativeModeTabs.SEARCH)
            .build()
    ).register();
    public static final BlockEntityEntry<LightControlCabinetBlockEntity> LIGHT_CONTROL_CABINET_BE = REGISTRATE
            .blockEntity("control_cabinet", LightControlCabinetBlockEntity::new)
            .validBlock(LIGHT_CONTROL_CABINET)
            .register();


    public static final BlockEntry<DerailerBlock> DERAILER = datagen(REGISTRATE
            .block("derailer", DerailerBlock::new)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .properties(p -> p
                    .strength(2.0f, 4.0f)
                    .requiresCorrectToolForDrops()
                    .mapColor(DyeColor.YELLOW)
            )
            .item(TrackTargetingBlockItem.ofType(EdgePointType.OBSERVER))    .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .removeTab(CreativeModeTabs.SEARCH)
            .onRegisterAfter(Registries.ITEM, item ->
                    TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)))
            .build()
    ).register();
    public static final BlockEntityEntry<DerailerBlockEntity> DERAILER_BE = REGISTRATE
            .blockEntity("derailer", DerailerBlockEntity::new)
            .validBlock(DERAILER)
            .register();


    public static final BlockEntry<CrossingBarrierBlock> CROSSING_BARRIER = datagen(REGISTRATE
            .block("crossing_barrier", CrossingBarrierBlock::new)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .properties(p -> p
                    .strength(2.0f, 4.0f)
                    .requiresCorrectToolForDrops()
                    .mapColor(DyeColor.LIGHT_GRAY)
                    .noOcclusion()
            )
            .item()    .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegisterAfter(Registries.ITEM, item ->
                    TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)))
            .removeTab(CreativeModeTabs.SEARCH)
            .build()
    ).register();
    public static final BlockEntityEntry<CrossingBarrierBlockEntity> CROSSING_BARRIER_BE = REGISTRATE
            .blockEntity("crossing_barrier", CrossingBarrierBlockEntity::new)
            .validBlock(CROSSING_BARRIER)
            .register();


    public static final BlockEntry<TrafficTrussBlock> TRAFFIC_TRUSS = datagen(REGISTRATE
            .block("traffic_truss", TrafficTrussBlock::new)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .properties(p -> p
                    .strength(3.0f, 5.0f)
                    .requiresCorrectToolForDrops()
                    .mapColor(DyeColor.LIGHT_GRAY)
            )
            .item()    .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegisterAfter(Registries.ITEM, item ->
                    TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)))
            .removeTab(CreativeModeTabs.SEARCH)
            .build()
    ).register();


    public static final BlockEntry<TrafficPillarBlock> TRAFFIC_PILLAR = datagen(REGISTRATE
            .block("traffic_pillar", TrafficPillarBlock::new)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .properties(p -> p
                    .strength(3.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .mapColor(DyeColor.LIGHT_GRAY)
            )
            .item()    .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegisterAfter(Registries.ITEM, item ->
                    TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)))
            .removeTab(CreativeModeTabs.SEARCH)
            .build()
    ).register();


    public static final BlockEntry<BallastBlock> BALLAST_GRAY = datagen(REGISTRATE
            .block("ballast_gray", BallastBlock::new)
            .tag(BlockTags.MINEABLE_WITH_SHOVEL)
            .properties(p -> p
                    .strength(1.0f, 1.5f)
                    .mapColor(DyeColor.GRAY)
            )
            .item()    .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .removeTab(CreativeModeTabs.SEARCH)
            .build()
    ).register();


    public static final BlockEntry<MultiblockTrussBlock> GIRDED_TRUSS = datagen(REGISTRATE
            .block("girded_truss", MultiblockTrussBlock::new)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .properties(p -> p
                    .strength(3.5f, 7.5f)
                    .requiresCorrectToolForDrops()
            )
            .item()    .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegisterAfter(Registries.ITEM, item ->
                    TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)))
            .removeTab(CreativeModeTabs.SEARCH)
            .build()
    ).register();


    public static final BlockEntry<BallastBlock> BALLAST_BROWN = datagen(REGISTRATE
            .block("ballast_brown", BallastBlock::new)
            .tag(BlockTags.MINEABLE_WITH_SHOVEL)
            .properties(p -> p
                    .strength(1.0f, 1.5f)
                    .mapColor(DyeColor.BROWN)
            )
            .item()
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .removeTab(CreativeModeTabs.SEARCH)
            .build()
    ).register();


    public static final BlockEntry<ExposedConcrete> EXPOSED_CONCRETE = datagen(REGISTRATE
            .block("exposed_concrete", ExposedConcrete::new)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .properties(p -> p
                    .strength(2.2f, 8.0f)
                    .requiresCorrectToolForDrops()
                    .mapColor(DyeColor.WHITE)
            )
            .item()
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .removeTab(CreativeModeTabs.SEARCH)
            .build()
    ).register();


    public static final BlockEntry<LEDStripBlock> LEDSTRIP = datagen(REGISTRATE
            .block("led_light", LEDStripBlock::new)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .properties(p -> p
                    .strength(0.8f, 0.8f)
            )
            .item()
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegisterAfter(Registries.ITEM, item ->
                    TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)))
            .removeTab(CreativeModeTabs.SEARCH)
            .build()
    ).register();
    public static final BlockEntityEntry<LEDStripBlockEntity> LED_STRIP_BE = REGISTRATE
            .blockEntity("led_light", LEDStripBlockEntity::new)
            .validBlock(LEDSTRIP)
            .register();


    public static final ItemEntry<Item> LIGHT_DIODE = REGISTRATE.
            item("light", Item::new)
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .onRegisterAfter(Registries.ITEM, item ->
                    TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)))
            .removeTab(CreativeModeTabs.SEARCH)
            .register();


    public static final ItemEntry<GroupControllerItem> GROUP_CONTROLLER = REGISTRATE
            .item("group_controller", GroupControllerItem::new)
            .properties(p -> p.stacksTo(1))
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .onRegisterAfter(Registries.ITEM, item ->
                    TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)))
            .removeTab(CreativeModeTabs.SEARCH)
            .register();


    public static final BlockEntityEntry<MTBaseColoredBlockEntity> BASE_COLORED_BE = REGISTRATE
            .blockEntity("base_colored_block_hook", MTBaseColoredBlockEntity::new)
            .validBlocks(getValidHookedBlocks())
            .register();


    public static final BlockEntry<SignboardBlock> SIGNSBOARD = datagen(REGISTRATE.
            block("signboard", SignboardBlock::new)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .properties(p -> p
                    .strength(0.8f, 0.4f)
            )
            .item()
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .onRegisterAfter(Registries.ITEM, item ->
                    TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)))
            .removeTab(CreativeModeTabs.SEARCH)
            .build()
    ).register();
    public static final BlockEntityEntry<SignboardBlockEntity> SIGNSBOARD_BE = REGISTRATE
            .blockEntity("signboard", SignboardBlockEntity::new)
            .validBlock(SIGNSBOARD)
            .register();

    public static final RegistryEntry<DisplaySource, SignalOccupancyDisplaySource> SIGNAL_OCCUPANCY_SOURCE = REGISTRATE
            .displaySource("signal_occupancy", SignalOccupancyDisplaySource::new)
            .register();
    public static final RegistryEntry<DisplayTarget, CabinetDisplayTarget> CABINET_DISPLAY_TARGET = REGISTRATE
            .displayTarget("control_cabinet", CabinetDisplayTarget::new)
            .register();

    public static void register(IEventBus modEventBus) {
    }

    public static <T extends Block> BlockBuilder<T, CreateRegistrate> datagen(BlockBuilder<T, CreateRegistrate> blockBuilder) {
        return blockBuilder
                .setData(ProviderType.BLOCKSTATE, NonNullBiConsumer.noop())
                .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
                .setData(ProviderType.LANG, NonNullBiConsumer.noop());
    }

}