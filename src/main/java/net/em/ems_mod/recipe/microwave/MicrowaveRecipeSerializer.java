package net.em.ems_mod.recipe.microwave;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class MicrowaveRecipeSerializer implements RecipeSerializer<MicrowaveRecipe> {
    public static final MapCodec<MicrowaveRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(MicrowaveRecipe::getInputItem),
            ItemStack.CODEC.fieldOf("result").forGetter(MicrowaveRecipe::getResult)
    ).apply(inst, MicrowaveRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MicrowaveRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, MicrowaveRecipe::getInputItem,
                    ItemStack.STREAM_CODEC, MicrowaveRecipe::getResult,
                    MicrowaveRecipe::new
            );

    // Return our map codec.
    @Override
    public MapCodec<MicrowaveRecipe> codec() {
        return CODEC;
    }

    // Return our stream codec.
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MicrowaveRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
