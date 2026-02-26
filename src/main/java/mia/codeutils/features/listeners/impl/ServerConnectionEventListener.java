package mia.codeutils.features.listeners.impl;

import mia.codeutils.features.listeners.AbstractEventListener;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public interface ServerConnectionEventListener extends AbstractEventListener {
    void serverConnectInit(ClientPacketListener networkHandler, Minecraft minecraftServer);
    void serverConnectJoin(ClientPacketListener networkHandler, PacketSender sender, Minecraft minecraftServer);
    void serverConnectDisconnect(ClientPacketListener networkHandler, Minecraft minecraftServer);

    void DFConnectJoin(ClientPacketListener networkHandler);
    void DFConnectDisconnect(ClientPacketListener networkHandler);
}
