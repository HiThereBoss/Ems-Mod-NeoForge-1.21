package net.em.ems_mod.blockentity;

import net.em.ems_mod.EmsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrayBlockEntity extends BlockEntity {
    private static final Component TITLE =
            Component.translatable("container." + EmsMod.MODID + ".tray_be");

    private static final int SLOTS = 3;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    };

    private final Lazy<ItemStackHandler> optional = Lazy.of(() -> this.inventory);

    public TrayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRAY_BE.get(), pos, state);
    }


    public ItemStackHandler getInventory() {
        return this.inventory;
    }

    public ItemStack[] getRenderStacks(){
        ItemStack[] toReturn = new ItemStack[SLOTS];
        for (int i = 0; i < SLOTS; i++){
            toReturn[i] = this.inventory.getStackInSlot(i);
        }
        return toReturn;
    }

    public void takeItemFromTray(Player player){
        for (int i = 0; i < SLOTS; i++){
            if (!this.inventory.getStackInSlot(i).isEmpty()){
                if (!player.isCreative())
                    player.getInventory().add(this.inventory.extractItem(i,1,false));
                this.inventory.setStackInSlot(i, ItemStack.EMPTY);

                break;
            }
        }
    }

    public void addItemToTray(ItemStack stack, Player player, InteractionHand hand){
        for (int i = 0; i < SLOTS; i++){
            if (this.inventory.getStackInSlot(i).isEmpty()){
                Item item = stack.getItem();
                int stackSize = stack.getCount();
                ItemStack singleItemStack = new ItemStack(item, 1);
                this.inventory.insertItem(i, singleItemStack, false);

                if (!player.isCreative())
                    // New item stack with one less item, or an empty one if 1 or lower items in stack
                    player.setItemInHand(hand, (stackSize > 1) ? new ItemStack(item, stackSize-1) : ItemStack.EMPTY);

                break;
            }
        }
    }

    public Lazy<ItemStackHandler> getOptional() {
        return this.optional;
    }

    public static Component getTITLE() {
        return TITLE;
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
