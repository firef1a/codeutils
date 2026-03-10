package mia.codeutils.mixin.hud;


import mia.codeutils.ColorBank;
import mia.codeutils.Mod;
import mia.codeutils.features.FeatureManager;
import mia.codeutils.features.impl.general.title.IconButtonWidget;
import mia.codeutils.features.impl.general.title.JoinButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MTitleScreen extends Screen {
    public MTitleScreen(Component title) {
        super(title);
    }


    @Unique
    private static IconButtonWidget createIconButton(Button.OnPress onPress) {
        Tooltip tooltip = Tooltip.create(
                Component.literal("Join DF: ").append(Component.literal("\n" + JoinButton.getCustomServerAddress()).withColor(ColorBank.MC_GRAY))
        );

        IconButtonWidget buttonWidget = new IconButtonWidget(
                0,
                0,
                20,
                20,
                new WidgetSprites(
                        Identifier.tryBuild(Mod.MOD_ID, "textures/gui/buttons/" + JoinButton.getJoinIcon().getDisabledPath()), // normal state
                        Identifier.tryBuild(Mod.MOD_ID, "textures/gui/buttons/" + JoinButton.getJoinIcon().getEnabledPath())  // focused state
                ),
                onPress
        );
        buttonWidget.setTooltip(tooltip);

        return buttonWidget;
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addCustomButton(CallbackInfo ci) {
        if (!FeatureManager.getFeature(JoinButton.class).getEnabled()) return;

        IconButtonWidget textIconButtonWidget = this.addRenderableWidget(createIconButton((button) -> {
            connectToServer(JoinButton.getCustomServerAddress());
        }));
        textIconButtonWidget.setPosition((this.width / 2) - 100 + 200 + 4, (this.height / 4) + 48);
    }

    @Unique
    private static void connectToServer(String address) {
        if (Mod.MC.level != null) {
            Mod.MC.level.disconnect(Component.literal("world is null"));
            Mod.MC.disconnectFromWorld(Component.literal("world is null"));
        }

        ServerAddress serverAddress = ServerAddress.parseString(address);
        ServerData serverInfo = new ServerData(address, address, ServerData.Type.OTHER);

        net.minecraft.client.gui.screens.ConnectScreen.startConnecting(Mod.getCurrentScreen(), Mod.MC, serverAddress, serverInfo, true, null);
    }


}
