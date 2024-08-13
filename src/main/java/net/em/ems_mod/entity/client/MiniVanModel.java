package net.em.ems_mod.entity.client;

import net.em.ems_mod.EmsMod;
import net.em.ems_mod.entity.custom.MiniVanEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class MiniVanModel extends GeoModel<MiniVanEntity> {

    @Override
    public ResourceLocation getModelResource(MiniVanEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(EmsMod.MODID,"geo/minivan.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MiniVanEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(EmsMod.MODID,"textures/entity/minivan.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MiniVanEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(EmsMod.MODID,"animations/tiger.animation.json");
    }

    // TODO: This is where I can add the custom animations (doors opening)
    @Override
    public void setCustomAnimations(MiniVanEntity animatable, long instanceId, AnimationState<MiniVanEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
    }
}
