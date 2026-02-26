package mia.codeutils.mixin.player;

import mia.codeutils.features.FeatureManager;
import mia.codeutils.features.impl.internal.staff.VanishTracker;
import mia.codeutils.features.impl.moderation.VanishFly;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MEntity {

    @Inject(method = "baseTick", at = @At("HEAD"))
    private void constantSpectatorMovement(CallbackInfo ci) {
        if ((Object) this instanceof LocalPlayer player) {
            if (player.getAbilities().mayfly && FeatureManager.getFeature(VanishFly.class).getEnabled() && player.isSpectator() && FeatureManager.getFeature(VanishTracker.class).isInModVanish()) {
                float forward = player.zza;
                float sideways = player.xxa;

                Vec3 lookVec = player.getLookAngle();
                Vec3 flatLookVec = new Vec3(lookVec.x, 0, lookVec.z).normalize();
                Vec3 sideVec = new Vec3(flatLookVec.z, 0, -flatLookVec.x);

                float flySpeed = player.getAbilities().getFlyingSpeed() * 22.5f;
                Vec3 newVelocity = flatLookVec.scale(forward * flySpeed).add(sideVec.scale(sideways * flySpeed));

                if (player.input.keyPresses.jump()) newVelocity = newVelocity.add(0, flySpeed, 0);
                if (player.input.keyPresses.shift()) newVelocity = newVelocity.subtract(0, flySpeed, 0);
                player.setDeltaMovement(newVelocity);

            }
        }
    }


}
