package mia.codeutils.mixin.hud;

import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public abstract class MToastManager {
    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    public void add(Toast toast, CallbackInfo ci) {
        if (toast.getToken().equals(SystemToast.SystemToastId.UNSECURE_SERVER_WARNING)) ci.cancel();
    }
}