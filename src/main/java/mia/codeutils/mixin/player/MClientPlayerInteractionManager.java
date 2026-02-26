package mia.codeutils.mixin.player;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MClientPlayerInteractionManager {

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    public void onBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void onBlockInteract(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {

    }

}