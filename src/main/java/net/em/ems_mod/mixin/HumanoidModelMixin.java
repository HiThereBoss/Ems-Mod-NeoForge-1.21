package net.em.ems_mod.mixin;

import net.em.ems_mod.entity.custom.MiniVanEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {
    @Inject(method = "setupAnim*", at = @At("TAIL"))
    private void onSetupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof Player player && player.getVehicle() instanceof MiniVanEntity) {
            if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;

            HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;
            float dR = ((MiniVanEntity) player.getVehicle()).getDeltaRotation();
            calculateLeftArmRot(dR);
            calculateRightArmRot(dR);
            // Override arm positions
            model.rightArm.xRot = -degToRad(currentRightArmRot.x);
            model.rightArm.yRot = -degToRad(currentRightArmRot.y);
            model.rightArm.zRot = degToRad(currentRightArmRot.z);
            model.leftArm.xRot = -degToRad(currentLeftArmRot.x);
            model.leftArm.yRot = -degToRad(currentLeftArmRot.y);
            model.leftArm.zRot = degToRad(currentLeftArmRot.z);
        }
    }

    @Unique
    private static float degToRad(double deg) {
        return (float)(deg * (Math.PI / 180F));
    }

    private static Vec3 lerp(Vec3 start, Vec3 end, float t) {
        return new Vec3(start.x + (end.x - start.x) * t, start.y + (end.y - start.y) * t, start.z + (end.z - start.z) * t);
    }

    @Unique
    private Vec3 currentLeftArmRot = new Vec3(0f, 0f, 0f);
    @Unique
    private Vec3 currentRightArmRot = new Vec3(0f, 0f, 0f);
    @Unique
    private Vec3 defaultTarget = new Vec3(70f, 0f, 0f);
    @Unique
    private static final Vec3 leftArmTargetLeftTurn = new Vec3(51.3f, -6.4f, 15.3f);
    @Unique
    private static final Vec3 rightArmTargetLeftTurn = new Vec3(99f, 17.4f, 29.5f);
    @Unique
    private static final Vec3 leftArmTargetRightTurn = new Vec3(99f, -17.4f, -29.5f);
    @Unique
    private static final Vec3 rightArmTargetRightTurn = new Vec3(51.3f, 6.4f, -15.3f);

    private void calculateLeftArmRot(float deltaRotation) {
        if (deltaRotation > 0) {
            currentLeftArmRot = lerp(defaultTarget, leftArmTargetRightTurn, deltaRotation/2.6f);
        } else {
            currentLeftArmRot = lerp(defaultTarget, leftArmTargetLeftTurn, deltaRotation/-2.6f);
        }
    }

    private void calculateRightArmRot(float deltaRotation) {
        if (deltaRotation > 0) {
            currentRightArmRot = lerp(defaultTarget, rightArmTargetRightTurn, deltaRotation/2.8f);
        } else {
            currentRightArmRot = lerp(defaultTarget, rightArmTargetLeftTurn, deltaRotation/-2.8f);
        }
    }
}
