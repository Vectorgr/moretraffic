package com.tomas65107.moretraffic.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tomas65107.moretraffic.block.CrossingBarrierBlock;
import com.tomas65107.moretraffic.block.CrossingBarrierBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import static com.tomas65107.moretraffic.mod.registration.MTPartials.CROSSING_BARRIER_ARM;

public class CrossingBarrierRenderer implements BlockEntityRenderer<CrossingBarrierBlockEntity> {

    //Pivot of the arm, in pixels. The model is drawn lowered, pointing east, around ARM_PIVOT_IN_MODEL;
    //it is then moved so that this point sits at ARM_PIVOT_IN_BLOCK. Keeping the model pivot far west
    //leaves more room for a long arm inside the -16..32 limit of block models.
    private static final float[] ARM_PIVOT_IN_MODEL = {-12, 13, 4};
    private static final float[] ARM_PIVOT_IN_BLOCK = {8, 13, 4};
    private static final float ARM_LENGTH_SCALE = 5f / 3f;

    public CrossingBarrierRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(CrossingBarrierBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        //blockstate "y" rotation of this facing: north = 0, east = 90...
        float facingRotation = be.getBlockState().getValue(CrossingBarrierBlock.FACING).toYRot() + 180;
        float raisedAngle = (1 - be.getLoweredProgress(partialTicks)) * 90;

        SuperByteBuffer arm = CachedBuffers.partial(CROSSING_BARRIER_ARM, be.getBlockState());

        //transforms apply to the model in reverse order
        arm.center()
                .rotateYDegrees(-facingRotation)
                .uncenter()
                .translate(ARM_PIVOT_IN_BLOCK[0] / 16, ARM_PIVOT_IN_BLOCK[1] / 16, ARM_PIVOT_IN_BLOCK[2] / 16)
                .rotateZDegrees(raisedAngle)
                .scale(ARM_LENGTH_SCALE, 1, 1)
                .translate(-ARM_PIVOT_IN_MODEL[0] / 16, -ARM_PIVOT_IN_MODEL[1] / 16, -ARM_PIVOT_IN_MODEL[2] / 16);

        arm.light(light).renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(CrossingBarrierBlockEntity be) {
        //the arm reaches outside its block
        return new AABB(be.getBlockPos()).inflate(5);
    }
}
