package net.em.ems_mod.blockentity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.em.ems_mod.blockentity.MicrowaveBlockEntity;
import net.em.ems_mod.blockentity.TrayBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class MicrowaveBER implements BlockEntityRenderer<MicrowaveBlockEntity> {

    public MicrowaveBER(BlockEntityRendererProvider.Context ctx) {

    }

    @Override
    public void render(MicrowaveBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        ItemStack stack = pBlockEntity.getRenderItem();
        Level level = pBlockEntity.getLevel();
        if(level == null)
            return;

        BlockPos pos = pBlockEntity.getBlockPos();

        int packedLight = LightTexture.pack(
                level.getBrightness(LightLayer.BLOCK, pos),
                level.getBrightness(LightLayer.SKY, pos)
        );

        float rotation = 0;

        switch (pBlockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)){
            case NORTH -> rotation = 180;
            case SOUTH -> rotation = 0;
            case EAST -> rotation = 90;
            case WEST -> rotation = 270;
        }

        enum FACING{
            NORTH, SOUTH, EAST, WEST
        }

        FACING facing = FACING.NORTH;

        switch (pBlockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)){
            case NORTH -> facing = FACING.NORTH;
            case SOUTH -> facing = FACING.SOUTH;
            case WEST -> facing = FACING.WEST;
            case EAST -> facing = FACING.EAST;
        }

        pPoseStack.pushPose();
        switch (facing){
            case FACING.NORTH -> pPoseStack.translate(0.625, 0.12, 0.55);
            case FACING.SOUTH -> pPoseStack.translate(1-0.625, 0.12, 0.45);
            case FACING.WEST -> pPoseStack.translate(0.55, 0.12, 0.375);
            case FACING.EAST -> pPoseStack.translate(0.45, 0.12, 1-0.375);
        }
        pPoseStack.scale(0.35f, 0.35f, 0.35f);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        pPoseStack.mulPose(Axis.XP.rotationDegrees(270));
        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.GUI,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                pPoseStack,
                pBufferSource,
                level,
                0
        );
        pPoseStack.popPose();

    }
}
