package mia.codeutils.mixin.render;


import mia.codeutils.features.FeatureManager;
import mia.codeutils.features.impl.development.CodeSignColorer;
import mia.codeutils.features.impl.internal.mode.LocationAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignBlockEntity.class)
public abstract class MSignBlockEntity {
    @Shadow
    private SignText frontText;

    @Inject(method = "getFrontText", at = @At("HEAD"), cancellable = true)
    public void getFrontText(CallbackInfoReturnable<SignText> cir) {
        if (LocationAPI.getMode().canViewCode() && FeatureManager.getFeature(CodeSignColorer.class).getEnabled()) {
            SignText orig = this.frontText;
            Component line1 = orig.getMessage(0, false);
            Component line2 = orig.getMessage(1, false);
            Component line3 = orig.getMessage(2, false);
            Component line4 = orig.getMessage(3, false);

            line1 = Component.empty().append(line1).setStyle(Style.EMPTY.withColor(0xAAAAAA));
            line2 = Component.empty().append(line2).setStyle(Style.EMPTY.withColor(0xC5C5C5));
            line3 = Component.empty().append(line3).setStyle(Style.EMPTY.withColor(0xAAFFAA));
            line4 = Component.empty().append(line4).setStyle(Style.EMPTY.withColor(0xFF8800));

            cir.setReturnValue(
                    orig
                            .setMessage(0, line1)
                            .setMessage(1, line2)
                            .setMessage(2, line3)
                            .setMessage(3, line4)
            );

        }
    }
}