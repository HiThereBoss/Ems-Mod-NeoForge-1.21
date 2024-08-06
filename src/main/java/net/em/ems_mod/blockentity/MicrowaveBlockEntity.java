package net.em.ems_mod.blockentity;

import net.em.ems_mod.EmsMod;
import net.em.ems_mod.block.custom.MicrowaveBlock;
import net.em.ems_mod.blockentity.util.TickableBlockEntity;
import net.em.ems_mod.recipe.microwave.MicrowaveInput;
import net.em.ems_mod.recipe.microwave.MicrowaveRecipe;
import net.em.ems_mod.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.SoundEvent;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class MicrowaveBlockEntity extends BlockEntity implements TickableBlockEntity {

    public enum INTERACTION_RESULTS{
        SUCCESS, INVALID, GAVE_OUTPUT, FULL
    }

    public enum MicrowaveState{
        CLOSED, OPEN
    }

    public MicrowaveState microwaveState = MicrowaveState.CLOSED;

    public boolean isChangingStates = false;

    private static final int SLOTS = 2;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null)
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    };

    private final Optional<ItemStackHandler> optional = Optional.of(this.inventory);

    private int progress = 0;
    private int maxProgress = 100;

    private boolean canProcess = false;

    // private Player user;

    public MicrowaveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MICROWAVE_BE.get(), pos, state);
    }

    public ItemInteractionResult interact(@NotNull ItemStack pStack, Player pPlayer, InteractionHand pHand, BlockState pState, Level pLevel, BlockPos pPos){
        if (microwaveState == MicrowaveState.CLOSED &&
                ( ( (inputIsInside() && hasProgressFinished())
                        || !inputIsInside()) )){

            // Opening process
            isChangingStates = true;
            pLevel.setBlock(pPos, pState.setValue(MicrowaveBlock.OPEN, Boolean.TRUE), Block.UPDATE_ALL);
            microwaveState = MicrowaveState.OPEN;

            pLevel.playSeededSound(null,pPos.getX(),pPos.getY(),pPos.getZ(), ModSounds.MICROWAVE_OPEN, SoundSource.BLOCKS,0.5f,1,0);

            return ItemInteractionResult.SUCCESS;
        }
        else if(microwaveState == MicrowaveState.OPEN){
            // Interacts with the microwave and returns an enum value of the outcome
            Enum<INTERACTION_RESULTS> interactionResult = takeItem(pStack, pPlayer, pHand);

            if ((interactionResult == INTERACTION_RESULTS.GAVE_OUTPUT)
                    || (interactionResult == INTERACTION_RESULTS.SUCCESS)) return ItemInteractionResult.SUCCESS;

            // Closing process
            isChangingStates = true;
            pLevel.setBlock(pPos, pState.setValue(MicrowaveBlock.OPEN, Boolean.FALSE), Block.UPDATE_ALL);
            microwaveState = MicrowaveState.CLOSED;

            pLevel.playSeededSound(null,pPos.getX(),pPos.getY(),pPos.getZ(), ModSounds.MICROWAVE_CLOSE, SoundSource.BLOCKS,0.5f,1,0);

            if (inputIsInside()){
                startProcessing();
                pLevel.playSeededSound(null,getBlockPos().getX(),getBlockPos().getY(),getBlockPos().getZ(), ModSounds.MICROWAVE_HUM, SoundSource.BLOCKS,1f,1,0);
            }


            return ItemInteractionResult.SUCCESS;
        }
        else return ItemInteractionResult.FAIL;
    }

    public INTERACTION_RESULTS takeItem(@NotNull ItemStack pStack, Player pPlayer, InteractionHand pHand){
        if (this.inventory.getStackInSlot(0).isEmpty() && this.inventory.getStackInSlot(1).isEmpty()){

            // Gives input slot a single item from the stack that is passed in
            this.inventory.insertItem(0, new ItemStack(pStack.getItem(),1), false);

            // Remove the invalid item from inventory, and doesn't take the item away from player
            if (hasRecipe()){
                pPlayer.setItemInHand(pHand, new ItemStack(pStack.getItem(), pStack.getCount() - 1));
                // user = pPlayer;
                return INTERACTION_RESULTS.SUCCESS;
            }
            else{
                this.inventory.extractItem(0,1,false);
                return INTERACTION_RESULTS.INVALID;
            }
        } else if (this.inventory.getStackInSlot(0).isEmpty() && !this.inventory.getStackInSlot(1).isEmpty()) {
            giveOutputToPlayer(pPlayer);
            return INTERACTION_RESULTS.GAVE_OUTPUT;
        }
        return INTERACTION_RESULTS.FULL;
    }

    public boolean inputIsInside(){
        return !this.inventory.getStackInSlot(0).isEmpty();
    }

    public void startProcessing(){
        this.canProcess = true;
    }

    public void giveOutputToPlayer(Player player){
        if (!this.inventory.getStackInSlot(1).isEmpty()){
            player.getInventory().add(this.inventory.extractItem(1,1,false));
        }
    }

    @Override
    public void tick() {
        if(checkCanProcess()) {
            increaseCraftingProgress();

            /*
            if (user != null)
                user.displayClientMessage(Component.literal("Processing..."),true);
             */

            if(hasProgressFinished()) {
                craftItem();
                resetProgress();

                /*
                if (user != null)
                    user.displayClientMessage(Component.literal(""),true);
                 */

                if (level != null)
                    level.playSeededSound(null,getBlockPos().getX(),getBlockPos().getY(),getBlockPos().getZ(), ModSounds.MICROWAVE_FINISHED, SoundSource.BLOCKS,0.5f,1,0);
            }
        } else {
            resetProgress();
        }
    }

    private boolean checkCanProcess(){
        return canProcess && hasRecipe();
    }

    private void resetProgress() {
        this.canProcess = false;
        progress = 0;
    }

    private void craftItem() {
        Optional<RecipeHolder<MicrowaveRecipe>> recipe = getCurrentRecipe();

        // Map is the same thing as doing some_lazy_optional.isPresent(holder -> holder.whatever()) in Forge's LazyOptional
        ItemStack result = recipe.map(microwaveRecipeRecipeHolder -> microwaveRecipeRecipeHolder.value().getResultItem(null)).orElse(ItemStack.EMPTY);

        this.inventory.extractItem(0, 1, false);

        this.inventory.setStackInSlot(1, new ItemStack(result.getItem(),
                result.getCount()));
    }

    private boolean hasRecipe() {
        Optional<RecipeHolder<MicrowaveRecipe>> recipe = getCurrentRecipe();

        if(recipe.isEmpty()) {
            return false;
        }

        ItemStack result = recipe.get().value().getResultItem(Objects.requireNonNull(getLevel()).registryAccess());

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

    public boolean hasProgressFinished() {
        return progress >= maxProgress;
    }

    private void increaseCraftingProgress() {
        progress++;
    }

    public Optional<ItemStackHandler> getOptional() {
        return this.optional;
    }

    public ItemStack getRenderItem(){
        if (!this.inventory.getStackInSlot(1).isEmpty()){ return this.inventory.getStackInSlot(1); }
        else if (!this.inventory.getStackInSlot(0).isEmpty()){ return this.inventory.getStackInSlot(0); }
        else return ItemStack.EMPTY;
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
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        loadClientData(tag, registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(@NotNull Connection connection, ClientboundBlockEntityDataPacket pkt, HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag, registries);
    }
}
