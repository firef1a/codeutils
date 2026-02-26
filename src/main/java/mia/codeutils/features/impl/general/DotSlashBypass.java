package mia.codeutils.features.impl.general;

import mia.codeutils.Mod;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.listeners.impl.PacketListener;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;

public final class DotSlashBypass extends Feature implements PacketListener {
    public DotSlashBypass(Categories category) {
        super(category, "Dot Slash Bypass", "dotslash", "Automatically removes the . before / and @ commands");
    }

    @Override
    public void receivePacket(Packet<?> packet, CallbackInfo ci) {

    }

    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ServerboundChatPacket(String chatMessage, Instant timestamp, long salt, MessageSignature signature, LastSeenMessages.Update acknowledgment)) {
            if (chatMessage.startsWith("./") || chatMessage.startsWith(".@")) {
                String modifiedMessage = chatMessage.substring(1);
                if (Mod.MC.getConnection() != null) {
                    Mod.MC.getConnection().sendChat(modifiedMessage);
                    ci.cancel();
                }
            }
        }
    }
}
