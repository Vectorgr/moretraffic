package com.tomas65107.moretraffic.mod.ponder;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.trains.signal.SignalBlockEntity;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackShape;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.tomas65107.moretraffic.block.AdvancedTrafficLightBlockEntity;
import com.tomas65107.moretraffic.block.FlashingBlinkerBlockEntity;
import com.tomas65107.moretraffic.core.TrafficLightLight;
import de.mrjulsen.trafficcraft.data.PaintColor;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;

public class ControlCabinetScenes {

    public static void scene1(SceneBuilder sceneBuilder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(sceneBuilder);
        scene.title("cabinet_1", "cabinet");
        scene.showBasePlate();

        BlockPos controlCabinet = util.grid().at(2, 1, 2);
        BlockPos blinker = util.grid().at(3, 1, 1);
        BlockPos trafficLight = util.grid().at(1, 1, 1);

        scene.world().showSection(util.select().position(controlCabinet), Direction.NORTH);
//        scene.world().setBlock(controlCabinet, AllBlocks.LARGE_COGWHEEL.getDefaultState().setValue(CogWheelBlock.AXIS, Direction.Axis.X), false);

        scene.overlay()
                .showText(90)
                .text("1") //Can be used to make complex systems and automations that control other TrafficCraft blocks
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector()
                        .blockSurface(controlCabinet, Direction.SOUTH));
        scene.idle(100);

        scene.overlay()
                .showText(40)
                .text("2") //It has 2 main categories: Groups and Instructions
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector()
                        .blockSurface(controlCabinet, Direction.SOUTH));
        scene.idle(50);

//        scene.overlay().showOutline(PonderPalette.BLUE, "menuOutline1", util.select().fromTo(controlCabinet, controlCabinet), 100);

        scene.world().showSection(util.select().position(trafficLight), Direction.NORTH);
        scene.world().showSection(util.select().position(blinker), Direction.NORTH);
        scene.idle(5);

        scene.overlay().showOutline(PonderPalette.MEDIUM, "1", util.select().fromTo(blinker, blinker), 50);
        scene.overlay().showOutline(PonderPalette.MEDIUM, "2", util.select().fromTo(trafficLight, trafficLight), 50);
        scene.overlay()
                .showText(90)
                .text("3"); //Groups allow you to group different MoreTraffic Blocks and modify them together
        scene.idle(50);
        scene.overlay().showOutline(PonderPalette.GREEN, "3", util.select().fromTo(blinker, trafficLight), 40);

        scene.idle(50);

        scene.world().modifyBlockEntity(blinker, FlashingBlinkerBlockEntity.class, be -> {
            be.lightStatus = true;
            be.setColor(PaintColor.GREEN);
        });
        scene.world().modifyBlockEntity(trafficLight, AdvancedTrafficLightBlockEntity.class, be -> {
            be.lights = new ArrayList<>();
            be.lights.add(new TrafficLightLight(DyeColor.RED, new TrafficLightLight.TrafficLightMask()));
            be.lights.add(new TrafficLightLight(DyeColor.ORANGE, new TrafficLightLight.TrafficLightMask()));
            be.lights.add(new TrafficLightLight(DyeColor.GREEN, new TrafficLightLight.TrafficLightMask()));
        });

        scene.overlay()
                .showText(40)
                .text("4") //You can modify them with instructions
                .colored(PonderPalette.GREEN);
        scene.idle(50);

//        scene.overlay().showControls(util.vector().blockSurface(controlCabinet, Direction.NORTH), Pointing.RIGHT, 87)
//                .withItem(AllItems.BRASS_HAND.asStack());

        scene.overlay()
                .showText(60)
                .text("5") //Open the GUI and explore! There are tips included
                .colored(PonderPalette.RED);
        scene.overlay().showControls(util.vector().blockSurface(controlCabinet, Direction.NORTH), Pointing.RIGHT, 60)
                .withItem(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("trafficcraft", "wrench")).asItem().getDefaultInstance());


        scene.markAsFinished();

    }

    /** Linking the cabinet to a Create train signal with a Display Link. Reuses the cabinet_1 structure. */
    public static void trainSignalScene(SceneBuilder sceneBuilder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(sceneBuilder);
        scene.title("cabinet_2", "Reacting to train signals");
        scene.showBasePlate();

        BlockPos controlCabinet = util.grid().at(2, 1, 2);
        BlockPos trafficLight = util.grid().at(1, 1, 1);
        BlockPos signal = util.grid().at(0, 1, 3);
        BlockPos displayLink = util.grid().at(0, 1, 2);
        Selection track = util.select().fromTo(0, 1, 4, 4, 1, 4);

        //these blocks are not part of the structure file
        scene.world().setBlocks(track, AllBlocks.TRACK.getDefaultState().setValue(TrackBlock.SHAPE, TrackShape.XO), false);
        scene.world().setBlock(signal, AllBlocks.TRACK_SIGNAL.getDefaultState(), false);
        scene.world().setBlock(displayLink, AllBlocks.DISPLAY_LINK.getDefaultState().setValue(BlockStateProperties.FACING, Direction.NORTH), false);
        scene.world().changeSignalState(signal, SignalBlockEntity.SignalState.GREEN);

        scene.world().showSection(util.select().position(controlCabinet), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(track, Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(signal), Direction.DOWN);
        scene.idle(10);

        scene.overlay()
                .showText(70)
                .text("The Control Cabinet can react to Create train signals")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(signal));
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(controlCabinet), Pointing.DOWN, 60)
                .rightClick()
                .withItem(AllBlocks.DISPLAY_LINK.asStack());
        scene.overlay().showOutline(PonderPalette.MEDIUM, "cabinet", util.select().position(controlCabinet), 70);
        scene.overlay()
                .showText(70)
                .text("First, right-click the Cabinet while holding a Display Link")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(controlCabinet, Direction.WEST));
        scene.idle(80);

        scene.world().showSection(util.select().position(displayLink), Direction.SOUTH);
        scene.idle(10);
        scene.overlay()
                .showText(70)
                .text("Then place it on a Train Signal")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(displayLink));
        scene.idle(80);

        scene.overlay()
                .showText(70)
                .text("In the Display Link menu, select 'Signal Occupancy' and confirm")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(displayLink));
        scene.world().flashDisplayLink(displayLink);
        scene.idle(80);

        scene.world().showSection(util.select().position(trafficLight), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showControls(util.vector().topOf(controlCabinet), Pointing.DOWN, 80)
                .rightClick()
                .withItem(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("trafficcraft", "wrench")).getDefaultInstance());
        scene.overlay()
                .showText(80)
                .text("Add 'Await train signal on' and 'off' instructions between your light changes, then run them in a loop")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(controlCabinet, Direction.WEST));
        showTrafficLight(scene, trafficLight, DyeColor.BLACK, DyeColor.GREEN);
        scene.idle(90);

        scene.world().changeSignalState(signal, SignalBlockEntity.SignalState.RED);
        scene.world().flashDisplayLink(displayLink);
        showTrafficLight(scene, trafficLight, DyeColor.RED, DyeColor.BLACK);
        scene.overlay()
                .showText(80)
                .text("'Await train signal on' continues once a train turns the signal red")
                .colored(PonderPalette.RED)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(signal));
        scene.idle(90);

        scene.world().changeSignalState(signal, SignalBlockEntity.SignalState.GREEN);
        scene.world().flashDisplayLink(displayLink);
        showTrafficLight(scene, trafficLight, DyeColor.BLACK, DyeColor.GREEN);
        scene.overlay()
                .showText(80)
                .text("'Await train signal off' continues once the signal is no longer red")
                .colored(PonderPalette.GREEN)
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().topOf(signal));
        scene.idle(90);

        scene.markAsFinished();
    }

    private static void showTrafficLight(CreateSceneBuilder scene, BlockPos pos, DyeColor top, DyeColor bottom) {
        scene.world().modifyBlockEntity(pos, AdvancedTrafficLightBlockEntity.class, be -> {
            be.lights = new ArrayList<>();
            be.lights.add(new TrafficLightLight(top, new TrafficLightLight.TrafficLightMask()));
            be.lights.add(new TrafficLightLight(DyeColor.BLACK, new TrafficLightLight.TrafficLightMask()));
            be.lights.add(new TrafficLightLight(bottom, new TrafficLightLight.TrafficLightMask()));
        });
    }

}
