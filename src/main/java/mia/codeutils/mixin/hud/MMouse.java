package mia.codeutils.mixin.hud;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MouseHandler.class)
public abstract class MMouse {

    /*
    @Inject(at = @At("HEAD"), method = "onMouseButton", cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        InGameHudManager.onMouseButton(window, button, action, mods, ci);
        //Mod.MC.mouse.unlockCursor();
    }

     */

}