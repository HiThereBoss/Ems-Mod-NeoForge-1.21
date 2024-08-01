package net.em.ems_mod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoods {
    public static final FoodProperties SHAWARMA = new FoodProperties.Builder().nutrition(9)
            .saturationModifier(5).effect(new MobEffectInstance(MobEffects.REGENERATION, 80), 0.8f).build();

    public static final FoodProperties TEST_SHAWARMA = new FoodProperties.Builder().nutrition(9)
            .saturationModifier(5).effect(new MobEffectInstance(MobEffects.REGENERATION, 80), 0.8f).build();

}
