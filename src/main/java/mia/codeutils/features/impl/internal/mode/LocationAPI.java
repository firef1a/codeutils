package mia.codeutils.features.impl.internal.mode;

import mia.codeutils.Mod;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.FeatureManager;
import mia.codeutils.features.listeners.DFMode;
import mia.codeutils.features.listeners.ModifiableEventData;
import mia.codeutils.features.listeners.ModifiableEventResult;
import mia.codeutils.features.listeners.impl.ChatEventListener;
import mia.codeutils.features.listeners.impl.ModeSwitchEventListener;
import mia.codeutils.features.listeners.impl.PacketListener;
import mia.codeutils.features.listeners.impl.ServerConnectionEventListener;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LocationAPI extends Feature implements ChatEventListener, ServerConnectionEventListener, PacketListener {
    private static DFMode mode = DFMode.NONE;

    private static final Pattern SPAWN_ACTIONBAR_PATTERN = Pattern.compile("(⏵+ - )?⧈ -?\\d+ Tokens {2}ᛥ -?\\d+ Tickets {2}⚡ -?\\d+ Sparks");

    public LocationAPI(Categories category) {
        super(category, "LocationAPI", "locapi", "Tracks state and location across diamondfire");
    }

    public static DFMode getMode() { return mode; }

    private static void modeSwitch(DFMode newMode) {
        FeatureManager.implementFeatureListener(ModeSwitchEventListener.class, feature -> feature.onModeSwitch(newMode, mode));
        mode = newMode;
    }

    @Override
    public ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci) {
        String content = message.base().getString();

        Matcher matcher;

        matcher = Pattern.compile("^» Joined game: (.*) by (.{3,16})\\.").matcher(content);
        if (matcher.find()) {
            modeSwitch(DFMode.PLAY);
            //locationStatus = LocationStatus.LOCATION_CHANGE;
        }

        matcher = Pattern.compile("» You are now in dev mode\\.").matcher(content);
        if (matcher.find()) {
            modeSwitch(DFMode.DEV);
        }

        matcher = Pattern.compile("^» You are now in build mode\\.").matcher(content);
        if (matcher.find()) {
            modeSwitch(DFMode.BUILD);
        }

        matcher = Pattern.compile("^» Sending you to ").matcher(content);
        if (matcher.find()) {
            modeSwitch(DFMode.NONE);
        }

        matcher = Pattern.compile("^» You are now spectating this plots code! Other people cannot see you and this action has been logged\\. Do /spawn to exit\\.").matcher(content);
        if (matcher.find()) {
            modeSwitch(DFMode.CODE_SPECTATE);
            //Mod.displayMessage("Entering code-spectate...");
            ci.cancel();
        }

        return message.pass();
    }

    @Override
    public void serverConnectInit(ClientPacketListener networkHandler, Minecraft minecraftServer) {

    }

    @Override
    public void serverConnectJoin(ClientPacketListener networkHandler, PacketSender sender, Minecraft minecraftServer) {

    }

    @Override
    public void serverConnectDisconnect(ClientPacketListener networkHandler, Minecraft minecraftServer) {

    }

    @Override
    public void DFConnectJoin(ClientPacketListener networkHandler) {
        mode = DFMode.SPAWN;
    }

    @Override
    public void DFConnectDisconnect(ClientPacketListener networkHandler) {
        mode = DFMode.NONE;
    }

    @Override
    public void receivePacket(Packet<?> packet, CallbackInfo ci) {
        if (Mod.MC.player == null) return;
        Matcher matcher;

        if (packet instanceof ClientboundSetActionBarTextPacket(Component text)) {
            matcher = SPAWN_ACTIONBAR_PATTERN.matcher(text.getString());
            if (matcher.find()) {
                mode = DFMode.SPAWN;
            }
        }
    }

    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {

    }
}
