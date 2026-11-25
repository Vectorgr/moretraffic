package com.tomas65107.moretraffic.core.lightinstructions;

import com.tomas65107.moretraffic.block.LightControlCabinetBlockEntity;
import com.tomas65107.moretraffic.core.TrafficDisplayPixels;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

public sealed interface LightInstructionProperty permits AwaitRedstone, AwaitTrainSignal, Delay, ModifyDisplay, ModifyLight, SendPulse {

    enum PropertyTypes {
        DELAY {
            @Override
            public LightInstructionProperty create() {
                return new Delay(0);
            }
        },
        MODIFY_LIGHT {
            @Override
            public LightInstructionProperty create() {
                return new ModifyLight("", DyeColor.BLACK, DyeColor.BLACK, DyeColor.BLACK);
            }
        },
        AWAIT_REDSTONE {
            @Override
            public LightInstructionProperty create() {
                return new AwaitRedstone(0);
            }
        },
        AWAIT_TRAIN_SIGNAL_ON {
            @Override
            public LightInstructionProperty create() {
                return new AwaitTrainSignal(true);
            }
        },
        AWAIT_TRAIN_SIGNAL_OFF {
            @Override
            public LightInstructionProperty create() {
                return new AwaitTrainSignal(false);
            }
        },
        MODIFY_DISPLAY {
            @Override
            public LightInstructionProperty create() {
                return new ModifyDisplay("", new TrafficDisplayPixels());
            }
        },
        SEND_PULSE {
            @Override
            public LightInstructionProperty create() {
                return new SendPulse("", false);
            }
        };

        public abstract LightInstructionProperty create();

        public String getNameOfProperty() {
            return switch(this) {
                case AWAIT_REDSTONE -> "AwaitRedstone";
                case AWAIT_TRAIN_SIGNAL_ON -> "AwaitTrainSignalOn";
                case AWAIT_TRAIN_SIGNAL_OFF -> "AwaitTrainSignalOff";
                case DELAY -> "Delay";
                case MODIFY_LIGHT -> "ModifyLight";
                case MODIFY_DISPLAY -> "ModifyDisplay";
                case SEND_PULSE -> "SendPulse";
            };
        }

        public Component getComponentOfProperty(boolean getMessage) {
            return switch(this) {
                case AWAIT_REDSTONE -> Component.translatable("gui.moretraffic.control_cabinet.instruction.await_redstone" + (getMessage ? ".message" : ""));
                case AWAIT_TRAIN_SIGNAL_ON -> Component.translatable("gui.moretraffic.control_cabinet.instruction.await_train_signal_on" + (getMessage ? ".message" : ""));
                case AWAIT_TRAIN_SIGNAL_OFF -> Component.translatable("gui.moretraffic.control_cabinet.instruction.await_train_signal_off" + (getMessage ? ".message" : ""));
                case DELAY -> Component.translatable("gui.moretraffic.control_cabinet.instruction.delay" + (getMessage ? ".message" : ""));
                case MODIFY_LIGHT -> Component.translatable("gui.moretraffic.control_cabinet.instruction.modify_light" + (getMessage ? ".message" : ""));
                case MODIFY_DISPLAY -> Component.translatable("gui.moretraffic.control_cabinet.instruction.modify_display" + (getMessage ? ".message" : ""));
                case SEND_PULSE -> Component.translatable("gui.moretraffic.control_cabinet.instruction.send_pulse" + (getMessage ? ".message" : ""));
            };
        }

        public static PropertyTypes getPropertyOfName(String name) {
            return switch(name) {
                case "AwaitRedstone" -> AWAIT_REDSTONE;
                case "AwaitTrainSignalOn" -> AWAIT_TRAIN_SIGNAL_ON;
                case "AwaitTrainSignalOff" -> AWAIT_TRAIN_SIGNAL_OFF;
                case "Delay" -> DELAY;
                case "ModifyLight" -> MODIFY_LIGHT;
                case "ModifyDisplay" -> MODIFY_DISPLAY;
                case "SendPulse" -> SEND_PULSE;
                default -> throw new IllegalStateException("Unexpected value: " + name);
            };
        }

        public Class getClassOfProperty() {
            return switch(this) {
                case AWAIT_REDSTONE -> AwaitRedstone.class;
                case AWAIT_TRAIN_SIGNAL_ON, AWAIT_TRAIN_SIGNAL_OFF -> AwaitTrainSignal.class;
                case DELAY -> Delay.class;
                case MODIFY_LIGHT -> ModifyLight.class;
                case MODIFY_DISPLAY -> ModifyDisplay.class;
                case SEND_PULSE -> SendPulse.class;
            };
        }

    }

    PropertyTypes getClassType();
    boolean executePayload(LightControlCabinetBlockEntity be, LightInstructionProperty instruction, Level level, BlockPos blockPos);

}
