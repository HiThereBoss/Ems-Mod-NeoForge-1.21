package net.em.ems_mod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.em.ems_mod.EmsMod;
import net.em.ems_mod.entity.custom.MiniVanEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MiniVanRenderer extends GeoEntityRenderer<MiniVanEntity> {
    public MiniVanRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MiniVanModel());
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull MiniVanEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(EmsMod.MODID,"textures/entity/minivan.png");
    }

    @Override
    public void render(MiniVanEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(- entityYaw));
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();

    }
}
