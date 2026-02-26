package mia.codeutils.mixin.network;

import io.netty.channel.ChannelFutureListener;
import mia.codeutils.Mod;
import mia.codeutils.features.FeatureManager;
import mia.codeutils.features.impl.internal.superdupertopsecrte.VerboseLogger;
import mia.codeutils.features.listeners.ModifiableEventData;
import mia.codeutils.features.listeners.impl.ChatEventListener;
import mia.codeutils.features.listeners.impl.PacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class MClientConnection {
    @Unique
    private static boolean canceled;


    @ModifyVariable(method = "genericsFtw", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static Packet<?> handlePacket(Packet<?> packet) {
        if (packet instanceof ClientboundSystemChatPacket(Component content, boolean overlay)) {
            canceled = false;

            CallbackInfo ci = new CallbackInfo("", true);
            ModifiableEventData<Component> eventData = new ModifiableEventData<>(content.copy(), content.copy());


            if (FeatureManager.getFeature(VerboseLogger.class).getEnabled()) {
                Mod.warn(content.getString());
                Mod.warn(content.toString());
            }

            for (ChatEventListener feature :  FeatureManager.getFeaturesByIdentifier(ChatEventListener.class)) {
                eventData = feature.chatEvent(eventData, ci).eventResult(content, eventData.modified());
            }

            ModifiableEventData<Component> modifiableEventData = eventData;

            /*
            if (FeatureManager.getFeature(MessageChatHudFeature.class).addMessage(modifiableEventData)) {
                canceled = true;
            }

             */
            if (ci.isCancelled()) {
                canceled = true;
            }


            return new ClientboundSystemChatPacket(modifiableEventData.modified(), overlay);
        }
        return packet;
    }

    @Inject(method = "genericsFtw", at = @At("HEAD"), cancellable = true)
    private static void handlePacket(Packet<?> packet, net.minecraft.network.PacketListener listener, CallbackInfo ci) {
        FeatureManager.implementFeatureListener(PacketListener.class, (feature) -> { feature.receivePacket(packet, ci); });
        if (packet instanceof ClientboundSystemChatPacket(Component content, boolean overlay)) {
            if (canceled) ci.cancel();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
    private void handlePacket(Packet<?> packet, @org.jspecify.annotations.Nullable ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        FeatureManager.implementFeatureListener(PacketListener.class, (feature) -> { feature.sendPacket(packet, ci); });
    }



}
