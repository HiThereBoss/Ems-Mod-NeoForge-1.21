package net.em.ems_mod.blockentity;

import net.em.ems_mod.EmsMod;
import net.em.ems_mod.blockentity.util.TickableBlockEntity;
import net.em.ems_mod.recipe.microwave.MicrowaveInput;
import net.em.ems_mod.recipe.microwave.MicrowaveRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MicrowaveBlockEntity extends BlockEntity implements TickableBlockEntity {

    private static final int SLOTS = 2;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            assert level != null;
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    };

    private final Lazy<ItemStackHandler> optional = Lazy.of(() -> this.inventory);

    private int progress = 0;
    private int maxProgress = 20;

    public MicrowaveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRAY_BE.get(), pos, state);
    }

    public void takeItem(@NotNull ItemStack pStack, Player pPlayer, InteractionHand pHand){
        if (this.inventory.getStackInSlot(0).isEmpty() && this.inventory.getStackInSlot(1).isEmpty()){
            this.inventory.setStackInSlot(0, new ItemStack(pStack.getItem(),1));
            pPlayer.setItemInHand(pHand, new ItemStack(pStack.getItem(), pStack.getCount() - 1));
            System.out.println("Item taken");

            if (hasRecipe()) System.out.println("recipe found");
        }
    }

    public void giveOutputToPlayer(Player player){
        if (!this.inventory.getStackInSlot(1).isEmpty()){
            player.getInventory().add(this.inventory.extractItem(1,1,false));
        }
    }

    @Override
    public void tick(){
        if(hasRecipe()) {
            increaseCraftingProgress();
            System.out.println("Processing...");
            if(hasProgressFinished()) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    private void resetProgress() {
        progress = 0;
    }

    private void craftItem() {
        Optional<RecipeHolder<MicrowaveRecipe>> recipe = getCurrentRecipe();
        ItemStack result = recipe.get().value().getResultItem(null);

        this.inventory.extractItem(0, 1, false);

        this.inventory.setStackInSlot(1, new ItemStack(result.getItem(),
                result.getCount()));
    }

    private boolean hasRecipe() {
        Optional<RecipeHolder<MicrowaveRecipe>> recipe = getCurrentRecipe();

        System.out.println(recipe);

        if(recipe.isEmpty()) {
            return false;
        }

        /* This is what neoforge used, i first need to fix the recipe type not working though... LOL
        ItemStack result = optional
                .map(RecipeHolder::value)
                .map(e -> e.assemble(input, pLevel.registryAccess()))
                .orElse(ItemStack.EMPTY);
         */

        ItemStack result = recipe.get().value().getResultItem(getLevel().registryAccess());



        return canInsertAmountIntoOutputSlot(result.getCount()) && canInsertItemIntoOutputSlot(result.getItem());
    }

    private Optional<RecipeHolder<MicrowaveRecipe>> getCurrentRecipe() {
        MicrowaveInput input = new MicrowaveInput(this.inventory.getStackInSlot(0));

        if (getLevel() == null) return Optional.empty();

        RecipeManager recipes = getLevel().getRecipeManager();

        return recipes.getRecipeFor(
                MicrowaveRecipe.MICROWAVE.get(),
                input,
                getLevel()
        );
    }

    private boolean canInsertItemIntoOutputSlot(Item item) {
        return this.inventory.getStackInSlot(1).isEmpty() || this.inventory.getStackInSlot(1).is(item);
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        return this.inventory.getStackInSlot(1).getCount() + count <= this.inventory.getStackInSlot(1).getMaxStackSize();
    }

    private boolean hasProgressFinished() {
        return progress >= maxProgress;
    }

    private void increaseCraftingProgress() {
        progress++;
    }

    public Lazy<ItemStackHandler> getOptional() {
        return this.optional;
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag pTag, HolderLookup.@NotNull Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        loadClientData(pTag, pRegistries);
    }


    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag, HolderLookup.@NotNull Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        saveClientData(pTag, pRegistries);
    }

    private void saveClientData(CompoundTag tag, HolderLookup.Provider registries){
        var emsData = new CompoundTag();
        emsData.put("Inventory", this.inventory.serializeNBT(registries));
        tag.put(EmsMod.MODID, emsData);
    }

    private void loadClientData(CompoundTag tag, HolderLookup.Provider registries){
        CompoundTag emsData = tag.getCompound(EmsMod.MODID);
        if (emsData.contains("Inventory")){
            this.inventory.deserializeNBT(registries, emsData.getCompound("Inventory"));
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider pRegistries) {
        CompoundTag tag = super.getUpdateTag(pRegistries);
        saveClientData(tag, pRegistries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag != null){
            loadClientData(tag, registries);
        }

    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag, registries);
    }
}
