package mia.codeutils.features.listeners.impl;

import mia.codeutils.features.listeners.AbstractEventListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface PacketListener extends AbstractEventListener {
    void receivePacket(Packet<?> packet, CallbackInfo ci);
    void sendPacket(Packet<?> packet, CallbackInfo ci);
}
