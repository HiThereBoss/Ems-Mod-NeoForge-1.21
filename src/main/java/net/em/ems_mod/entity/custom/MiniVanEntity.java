package net.em.ems_mod.entity.custom;

import net.minecraft.client.model.BoatModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.loading.math.function.round.LerpFunction;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MiniVanEntity extends VehicleEntity implements GeoEntity{
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYRot;
    private double lerpXRot;
    private int lerpSteps;
    private boolean inputLeft;
    private boolean inputRight;
    private boolean inputDown;
    private boolean inputUp;
    private float deltaRotation = 0f;
    private float invFriction;
    private float landFriction;
    private Status status;

    public enum DoorStates{
        OPEN,CLOSED
    }

    public enum Status{
        ON_LAND,
        IN_AIR
    }


    private DoorStates driversDoor = DoorStates.OPEN;
    public boolean driversDoorShouldAnimate = false;
    private DoorStates passengersDoor = DoorStates.CLOSED;
    public boolean passengersDoorShouldAnimate = false;

    public MiniVanEntity(EntityType<? extends VehicleEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    // -7.8, 11.7, -13.9
    @Override
    public void tick() {
        this.status = getStatus();

        super.tick();
        this.tickLerp();

        if (this.isControlledByLocalInstance()){
            this.doStuff();
            if (this.level().isClientSide() ) {
                LocalPlayer player = (LocalPlayer) this.getFirstPassenger();
                this.setInput(player.input.left, player.input.right,player.input.up,player.input.down);
                this.controlVan();
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }

    }

    private Status getStatus(){
        float f = this.getGroundFriction();
        if (f > 0.0F) {
            this.landFriction = f;
            return Status.ON_LAND;
        } else {
            return Status.IN_AIR;
        }
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity livingentity ? livingentity : super.getControllingPassenger();
    }

    @Nullable
    public Entity getFirstPassenger() {
        return this.getPassengers().isEmpty() ? null : this.getPassengers().getFirst();
    }

    private void doStuff(){
        double g = -this.getGravity();

        if (this.status == Status.IN_AIR) {
            this.invFriction = 0.8F;
        } else if (this.status == Status.ON_LAND) {
            this.invFriction = this.landFriction * 1.5f;
        }

        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * (double)this.invFriction, vec3.y + g, vec3.z * (double)this.invFriction);
        this.deltaRotation = this.deltaRotation * this.invFriction * 0.9f;
    }

    private void controlVan() {
        if (this.isVehicle()) {
            float f = 0.0F;
            if (this.inputLeft) {
                this.deltaRotation--;
            }

            if (this.inputRight) {
                this.deltaRotation++;
            }

            if (this.inputRight != this.inputLeft && !this.inputUp && !this.inputDown) {
                f += 0.005F;
            }

            this.setYRot(this.getYRot() + this.deltaRotation);
            if (this.inputUp) {
                f += 0.04F;
            }

            if (this.inputDown) {
                f -= 0.005F;
            }

            this.setDeltaMovement(
                    this.getDeltaMovement()
                            .add(
                                    (double)(Mth.sin(-this.getYRot() * (float) (Math.PI / 180.0)) * f),
                                    0.0,
                                    (double)(Mth.cos(this.getYRot() * (float) (Math.PI / 180.0)) * f)
                            )
            );
        }
    }

    public float getGroundFriction() {
        AABB aabb = this.getBoundingBox();
        AABB aabb1 = new AABB(aabb.minX, aabb.minY - 0.001, aabb.minZ, aabb.maxX, aabb.minY, aabb.maxZ);
        int i = Mth.floor(aabb1.minX) - 1;
        int j = Mth.ceil(aabb1.maxX) + 1;
        int k = Mth.floor(aabb1.minY) - 1;
        int l = Mth.ceil(aabb1.maxY) + 1;
        int i1 = Mth.floor(aabb1.minZ) - 1;
        int j1 = Mth.ceil(aabb1.maxZ) + 1;
        VoxelShape voxelshape = Shapes.create(aabb1);
        float f = 0.0F;
        int k1 = 0;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int l1 = i; l1 < j; l1++) {
            for (int i2 = i1; i2 < j1; i2++) {
                int j2 = (l1 != i && l1 != j - 1 ? 0 : 1) + (i2 != i1 && i2 != j1 - 1 ? 0 : 1);
                if (j2 != 2) {
                    for (int k2 = k; k2 < l; k2++) {
                        if (j2 <= 0 || k2 != k && k2 != l - 1) {
                            blockpos$mutableblockpos.set(l1, k2, i2);
                            BlockState blockstate = this.level().getBlockState(blockpos$mutableblockpos);
                            if (!(blockstate.getBlock() instanceof WaterlilyBlock)
                                    && Shapes.joinIsNotEmpty(
                                    blockstate.getCollisionShape(this.level(), blockpos$mutableblockpos).move((double)l1, (double)k2, (double)i2),
                                    voxelshape,
                                    BooleanOp.AND
                            )) {
                                f += blockstate.getFriction(this.level(), blockpos$mutableblockpos, this);
                                k1++;
                            }
                        }
                    }
                }
            }
        }

        return f / (float)k1;
    }

    @Override
    public void onPassengerTurned(Entity pEntityToUpdate) {
        this.clampRotation(pEntityToUpdate);
    }

    protected void clampRotation(Entity pEntityToUpdate) {
        pEntityToUpdate.setYBodyRot(this.getYRot());
        float f = Mth.wrapDegrees(pEntityToUpdate.getYRot() - this.getYRot());
        float f1 = Mth.clamp(f, -105.0F, 105.0F);
        pEntityToUpdate.yRotO += f1 - f;
        pEntityToUpdate.setYRot(pEntityToUpdate.getYRot() + f1 - f);
        pEntityToUpdate.setYHeadRot(pEntityToUpdate.getYRot());
    }

    public void setInput(boolean pInputLeft, boolean pInputRight, boolean pInputUp, boolean pInputDown) {
        this.inputLeft = pInputLeft;
        this.inputRight = pInputRight;
        this.inputUp = pInputUp;
        this.inputDown = pInputDown;
    }

    @Override
    public void lerpTo(double pX, double pY, double pZ, float pYRot, float pXRot, int pSteps) {
        this.lerpX = pX;
        this.lerpY = pY;
        this.lerpZ = pZ;
        this.lerpYRot = (double)pYRot;
        this.lerpXRot = (double)pXRot;
        this.lerpSteps = 10;
    }

    @Override
    public double lerpTargetX() {
        return this.lerpSteps > 0 ? this.lerpX : this.getX();
    }

    @Override
    public double lerpTargetY() {
        return this.lerpSteps > 0 ? this.lerpY : this.getY();
    }

    @Override
    public double lerpTargetZ() {
        return this.lerpSteps > 0 ? this.lerpZ : this.getZ();
    }

    @Override
    public float lerpTargetXRot() {
        return this.lerpSteps > 0 ? (float)this.lerpXRot : this.getXRot();
    }

    @Override
    public float lerpTargetYRot() {
        return this.lerpSteps > 0 ? (float)this.lerpYRot : this.getYRot();
    }

    @Override
    public @NotNull Direction getMotionDirection() {
        return this.getDirection().getClockWise();
    }

    private void tickLerp() {
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }

        if (this.lerpSteps > 0) {
            this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
            this.lerpSteps--;
        }
    }

    @Override
    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        return super.interact(pPlayer, pHand);
    }

    @Override
    protected @NotNull Vec3 getPassengerAttachmentPoint(@NotNull Entity pEntity, @NotNull EntityDimensions pDimensions, float pPartialTick) {
        if (driversDoor == DoorStates.OPEN){
            return new Vec3(7.8f/16f, 11.7f/16f, 13.9f/16f).yRot(-this.getYRot() * (float) (Math.PI / 180.0));
        }
        else {
            return super.getPassengerAttachmentPoint(pEntity, pDimensions, pPartialTick);
        }

    }

    @Override
    protected void positionRider(@NotNull Entity pPassenger, Entity.@NotNull MoveFunction pCallback) {
        super.positionRider(pPassenger, pCallback);
        if (!pPassenger.getType().is(EntityTypeTags.CAN_TURN_IN_BOATS)) {
            pPassenger.setYRot(pPassenger.getYRot() + this.deltaRotation);
            pPassenger.setYHeadRot(pPassenger.getYHeadRot() + this.deltaRotation);
            this.clampRotation(pPassenger);
            if (pPassenger instanceof Animal && this.getPassengers().size() == this.getMaxPassengers()) {
                int i = pPassenger.getId() % 2 == 0 ? 90 : 270;
                pPassenger.setYBodyRot(((Animal)pPassenger).yBodyRot + (float)i);
                pPassenger.setYHeadRot(pPassenger.getYHeadRot() + (float)i);
            }
        }
    }

    private int getMaxPassengers() {
        return 4;
    }

    @Override
    protected Item getDropItem() {
        return null;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canCollideWith(@NotNull Entity pEntity) {
        return canVehicleCollide(this, pEntity);
    }

    public static boolean canVehicleCollide(Entity pVehicle, Entity pEntity) {
        return (pEntity.canBeCollidedWith() || pEntity.isPushable()) && !pVehicle.isPassengerOfSameVehicle(pEntity);
    }

    @Override
    public boolean canBeCollidedWith() { return true; }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.08f;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }

    // TODO: Door opening logic and riding logic
    public void onPlayerInteract(Player player){

        // Riding logic (need to add checking for doors open based on position clicked... god)
        player.startRiding(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        //controllers.add(new AnimationController<>(this,"controller",0,this::predicate));
    }

    private PlayState predicate(AnimationState<MiniVanEntity> miniVanEntityAnimationState) {
        if (driversDoorShouldAnimate && (driversDoor == DoorStates.OPEN)){
            miniVanEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.minivan.open_drivers_door", Animation.LoopType.PLAY_ONCE));
        }

        if (miniVanEntityAnimationState.isMoving()){
            miniVanEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.minivan.move", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

        miniVanEntityAnimationState.getController().setAnimation(RawAnimation.begin().then("animation.minivan.idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
