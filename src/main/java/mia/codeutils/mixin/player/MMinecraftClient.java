package mia.codeutils.mixin.player;

import mia.codeutils.features.FeatureManager;
import mia.codeutils.features.listeners.impl.PlayerUseEventListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MMinecraftClient {
    @Shadow
    public LocalPlayer player;

    @Shadow
    public ClientLevel level;

    @Inject(at = @At("HEAD"), method = "startUseItem")
    private void onRightClick(CallbackInfo ci) {
        FeatureManager.implementFeatureListener(PlayerUseEventListener.class, feature -> feature.useItemCallback(player, level, InteractionHand.MAIN_HAND));

    }
}
