package mia.codeutils.mixin.player;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import mia.codeutils.Mod;
import mia.codeutils.core.MiaKeyBind;
import mia.codeutils.features.FeatureManager;
import mia.codeutils.features.impl.development.SignPeek;
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

    @Inject(method = "handleKeybinds", at = @At("HEAD"), cancellable = true)
    private void handleCustomKeybind(CallbackInfo ci) {
        SignPeek signPeek = FeatureManager.getFeature(SignPeek.class);
        if (Mod.MC.options.keySwapOffhand.isDown()) {
            if (FeatureManager.hasFeature(SignPeek.class)) {
                MiaKeyBind getSignPeek = signPeek.getSignName;
                if (getSignPeek.rawIsDown()) {
                    getSignPeek.tick();
                    Mod.MC.options.keySwapOffhand.setDown(false);
                    ci.cancel();
                }
            }
        }
    }
}
