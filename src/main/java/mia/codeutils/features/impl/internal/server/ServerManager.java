package mia.codeutils.features.impl.internal.server;

import mia.codeutils.Mod;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.FeatureManager;
import mia.codeutils.features.listeners.impl.AlwaysEnabled;
import mia.codeutils.features.listeners.impl.PacketListener;
import mia.codeutils.features.listeners.impl.ServerConnectionEventListener;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public final class ServerManager extends Feature implements ServerConnectionEventListener, PacketListener, AlwaysEnabled {
    public static RecognizedServers currentServer = RecognizedServers.NONE;
    public static ServerConnectionStatus connectionStatus = ServerConnectionStatus.NONE;

    public ServerManager(Categories category) {
        super(category,"Server Manager", "server_manager", "Detects and executes features when you join and leave DF. Will literally brick every other feature if disabled.");
    }

    public static boolean isNotOnDiamondFire() { return !isOnDiamondFire(); }
    public static boolean isOnDiamondFire() { return currentServer.equals(RecognizedServers.DIAMONDFIRE); }

    @Override
    public void serverConnectInit(ClientPacketListener networkHandler, Minecraft minecraftServer) { }

    @Override
    public void serverConnectJoin(ClientPacketListener networkHandler, PacketSender sender, Minecraft minecraftServer) {
        if (networkHandler.getServerData() == null) return;
        RecognizedServers server = ServerManager.recognizeServer(networkHandler.getServerData());
        if (server.equals(RecognizedServers.DIAMONDFIRE) && ServerManager.connectionStatus.equals(ServerConnectionStatus.CONNECTING)) {
            connectionStatus = ServerConnectionStatus.CONNECTED;
            currentServer = RecognizedServers.DIAMONDFIRE;
            Mod.warn("Joined DiamondFire");
            FeatureManager.implementFeatureListener(ServerConnectionEventListener.class, feature -> feature.DFConnectJoin(networkHandler));
        }
    }

    @Override
    public void serverConnectDisconnect(ClientPacketListener networkHandler, Minecraft minecraftServer) {
        if (networkHandler.getServerData() == null) return;
        RecognizedServers server = ServerManager.recognizeServer(networkHandler.getServerData());
        if (server.equals(RecognizedServers.DIAMONDFIRE)) {
            connectionStatus = ServerConnectionStatus.NONE;
            currentServer = RecognizedServers.NONE;
            FeatureManager.implementFeatureListener(ServerConnectionEventListener.class, feature -> feature.DFConnectDisconnect(networkHandler));
        }
    }

    @Override
    public void DFConnectJoin(ClientPacketListener networkHandler) {
        Mod.log("Connected to DiamondFire");
    };

    @Override
    public void DFConnectDisconnect(ClientPacketListener networkHandler) {
        Mod.log("Disconnected from DiamondFire");
    };

    @Override
    public void receivePacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClientboundHelloPacket) {
            connectionStatus = ServerConnectionStatus.CONNECTING;
        }
    }

    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) { }

    public static RecognizedServers recognizeServer(ServerData server) {
        String serverAddress = server.ip;
        if (
                serverAddress.contains("mcdiamondfire.com") ||
                        serverAddress.contains("mcdiamondfire.net") ||
                        serverAddress.contains("54.39.29.75") ||
                        serverAddress.contains("diamondfire.games")
        ) return RecognizedServers.DIAMONDFIRE;
        return RecognizedServers.NONE;
    }
}
