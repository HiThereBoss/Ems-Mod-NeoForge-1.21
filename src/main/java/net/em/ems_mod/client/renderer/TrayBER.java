package net.em.ems_mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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

public class TrayBER implements BlockEntityRenderer<TrayBlockEntity> {

    public TrayBER(BlockEntityRendererProvider.Context ctx) {

    }

    @Override
    public void render(TrayBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        ItemStack[] stacks = pBlockEntity.getRenderStacks();
        Level level = pBlockEntity.getLevel();
        if(level == null)
            return;

        BlockPos pos = pBlockEntity.getBlockPos().above();

        int packedLight = LightTexture.pack(
                level.getBrightness(LightLayer.BLOCK, pos),
                level.getBrightness(LightLayer.SKY, pos)
        );

        float rotation = 0;

        switch (pBlockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)){
            case NORTH -> rotation = 180;
            case SOUTH -> rotation = 0;
            case EAST -> rotation = 270;
            case WEST -> rotation = 90;
        }

        enum FACING{
            NS, EW
        }

        FACING facing = FACING.NS;

        switch (pBlockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)){
            case NORTH, SOUTH -> facing = FACING.NS;
            case EAST, WEST -> facing = FACING.EW;
        }

        float[] translations = {0.25f, 0.5f, 0.75f};


        for (int i = 0; i < stacks.length; i++){
            pPoseStack.pushPose();
            switch (facing){
                case FACING.NS -> pPoseStack.translate(translations[i], 0.065, 0.5);
                case FACING.EW -> pPoseStack.translate(0.5, 0.065, translations[i]);
            }
            pPoseStack.scale(0.35f, 0.35f, 0.35f);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
            itemRenderer.renderStatic(
                    stacks[i],
                    ItemDisplayContext.FIXED,
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
}
