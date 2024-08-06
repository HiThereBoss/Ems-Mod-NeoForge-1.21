package net.em.ems_mod.blockentity;

import net.em.ems_mod.EmsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TrayBlockEntity extends BlockEntity {

    private static final int SLOTS = 3;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null)
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    };

    private final Optional<ItemStackHandler> optional = Optional.of(this.inventory);

    public DataComponentMap my_components = DataComponentMap.EMPTY;

    public TrayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TRAY_BE.get(), pos, state);
    }

    public ItemStack[] getRenderStacks(){
        ItemStack[] toReturn = new ItemStack[SLOTS];
        for (int i = 0; i < SLOTS; i++){
            toReturn[i] = this.inventory.getStackInSlot(i);
        }
        return toReturn;
    }

    public void interact(Player player, InteractionHand hand, BlockHitResult hitResult, ItemStack stack){

        enum AXIS_TO_USE{
            X,Z
        }

        AXIS_TO_USE axisToUse = AXIS_TO_USE.X;

        switch (getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)){
            case NORTH, SOUTH -> axisToUse = AXIS_TO_USE.X;
            case EAST, WEST -> axisToUse = AXIS_TO_USE.Z;
        }

        int slot;
        if (axisToUse == AXIS_TO_USE.X){
            double locX = Math.abs(hitResult.getLocation().x - (double)getBlockPos().getX());
            if (locX <= (double)(1f/3f)) {slot = 0;}
            else if (locX >= (double)(2f/3f)) {slot = 2;}
            else slot = 1;
        }
        else{
            double locZ = Math.abs(hitResult.getLocation().z - (double)getBlockPos().getZ());
            if (locZ <= (double)(1f/3f)) {slot = 0;}
            else if (locZ >= (double)(2f/3f)) {slot = 2;}
            else slot = 1;
        }

        if (this.inventory.getStackInSlot(slot).isEmpty()){
            if (!player.isCreative())
                // New item stack with one less item, or an empty one if 1 or lower items in stack
                player.setItemInHand(hand, (stack.getCount() > 1) ? new ItemStack(stack.getItemHolder(), stack.getCount()-1, stack.getComponentsPatch()) : ItemStack.EMPTY);

            // stack.getComponentsPatch() provides components because the stack.getItem() method doesn't for some reason
            this.inventory.insertItem(slot, new ItemStack(stack.getItemHolder(),1, stack.getComponentsPatch()),false);
        }
        else{
            if (!player.isCreative()) player.getInventory().add(this.inventory.extractItem(slot,1,false));
            else this.inventory.extractItem(slot,1,false);
        }

    }

    // Old methods for use event handling
    /*
    public void takeItemFromTray(Player player, BlockHitResult hitResult){
        double locX = Math.abs(hitResult.getLocation().x - (double)getBlockPos().getX());
        double locZ = Math.abs(hitResult.getLocation().z - (double)getBlockPos().getZ());

        enum AXIS_TO_USE{
            X,Z
        }

        AXIS_TO_USE axisToUse = AXIS_TO_USE.X;

        switch (getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)){
            case NORTH, SOUTH -> axisToUse = AXIS_TO_USE.X;
            case EAST, WEST -> axisToUse = AXIS_TO_USE.Z;
        }

        int slot;
        if (axisToUse == AXIS_TO_USE.X){
            if (locX <= (double)(1f/3f)) {slot = 0;}
            else if (locX >= (double)(2f/3f)) {slot = 2;}
            else slot = 1;
        }
        else{
            if (locZ <= (double)(1f/3f)) {slot = 0;}
            else if (locZ >= (double)(2f/3f)) {slot = 2;}
            else slot = 1;
        }

        if (!player.isCreative())
            player.getInventory().add(this.inventory.extractItem(slot,1,false));
        this.inventory.extractItem(slot,1,false);

    }

    public void takeItemFromTray(Player player){
        for (int i = 0; i < SLOTS; i++){
            if (!this.inventory.getStackInSlot(i).isEmpty()){
                if (!player.isCreative())
                    player.getInventory().add(this.inventory.extractItem(i,1,false));
                this.inventory.extractItem(i,1,false);

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

     */

    public Optional<ItemStackHandler> getOptional() {
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
        if (!components().isEmpty())
            this.my_components = components();
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
