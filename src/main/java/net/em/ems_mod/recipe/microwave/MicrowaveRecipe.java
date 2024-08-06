package net.em.ems_mod.recipe.microwave;

import net.em.ems_mod.EmsMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class MicrowaveRecipe implements Recipe<MicrowaveInput> {
    private final Ingredient inputItem;
    private final ItemStack output;
    //private final int microwaveTime;

    public MicrowaveRecipe(Ingredient inputItem, ItemStack output) {
        this.inputItem = inputItem;
        this.output = output;
        //this.microwaveTime = microwaveTime;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(this.inputItem);
        return list;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public boolean matches(MicrowaveInput pInput, Level pLevel) {
        return this.inputItem.test(pInput.stack());
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider pRegistries) {
        return this.output;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull MicrowaveInput pInput, HolderLookup.@NotNull Provider pRegistries) {
        return this.output.copy();
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return MICROWAVE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return MICROWAVE.get();
    }

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, EmsMod.MODID);

    public static final Supplier<RecipeType<MicrowaveRecipe>> MICROWAVE =
            RECIPE_TYPES.register(
                    "microwaving",
                    // We need the qualifying generic here due to generics being generics.
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(EmsMod.MODID, "microwaving"))
            );

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, EmsMod.MODID);

    public static final Supplier<RecipeSerializer<MicrowaveRecipe>> MICROWAVE_SERIALIZER =
            RECIPE_SERIALIZERS.register("microwaving", MicrowaveRecipeSerializer::new);

    public static void register(IEventBus eventBus){
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }

    public Ingredient getInputItem() {
        return this.inputItem;
    }

    public ItemStack getResult() {
        return this.output;
    }

    /* Don't know how to add this lmao
    public int getMicrowaveTime(){
        return this.microwaveTime;
    }
     */

}
