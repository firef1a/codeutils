package mia.codeutils.features.impl.internal.commands;

import com.mojang.brigadier.CommandDispatcher;
import mia.codeutils.ColorBank;
import mia.codeutils.Mod;
import mia.codeutils.features.Categories;
import mia.codeutils.features.Feature;
import mia.codeutils.features.impl.internal.server.ServerManager;
import mia.codeutils.features.listeners.ModifiableEventData;
import mia.codeutils.features.listeners.ModifiableEventResult;
import mia.codeutils.features.listeners.impl.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.regex.Matcher;

public final class CommandScheduler extends Feature implements TickEvent, ServerConnectionEventListener, ChatEventListener, AlwaysEnabled, RegisterCommandListener, PacketListener {
    private static ArrayList<ScheduledCommand> scheduledCommands;
    private static ArrayList<ChatConsumer> chatConsumers;
    private static ArrayList<SoundEvent> soundHiders;
    private static long nextTimestamp;

    public CommandScheduler(Categories category) {
        super(category, "cmd scheduler", "cmd_scheduler", "Schedules non-player executed commands.");
        scheduledCommands = new ArrayList<>();
        chatConsumers = new ArrayList<>();
        soundHiders = new ArrayList<>();
        nextTimestamp = 0L;
    }

    public static void addCommand(ScheduledCommand scheduledCommand) {
        scheduledCommands.add(scheduledCommand);
    }

    public static void addChatConsumer(ChatConsumer chatConsumer) {
        chatConsumers.add(chatConsumer);
    }

    public static void addSoundHider(SoundEvent soundEvent) {
        soundHiders.add(soundEvent);
    }

    public static void removeSoundHider(SoundEvent soundEvent) {
        soundHiders.remove(soundEvent);
    }
    public static void removeChatConsumer(ChatConsumer chatConsumer) {
        chatConsumers.remove(chatConsumer);
    }

    public static void clearCommandQueue() {
        chatConsumers.clear();
        scheduledCommands.clear();
    }

    public static long getMaxCommandDelay() {
        long delay = 0L;

        for (ScheduledCommand scheduledCommand : scheduledCommands) {
            delay += scheduledCommand.getDelay();
        }

        return delay;
    }

    public static ArrayList<ScheduledCommand> getScheduledCommands() { return scheduledCommands; }

    @Override
    public ModifiableEventResult<Component> chatEvent(ModifiableEventData<Component> message, CallbackInfo ci) {
        String content = message.base().getString();

        ArrayList<ChatConsumer> removeConsumers = new ArrayList<>();
        for (ChatConsumer chatConsumer : chatConsumers) {
            Matcher matcher = chatConsumer.pattern().matcher(content);

            if (matcher.find()) {
                chatConsumer.successfulMatch().accept(matcher);
                if (chatConsumer.cancelMessage()) ci.cancel();
                removeConsumers.add(chatConsumer);
                break;
            }
        }
        chatConsumers.removeAll(removeConsumers);
        return message.pass();
    }

    @Override
    public void tickR(int tick) {
        if (!ServerManager.isOnDiamondFire()) return;

        long currentTimestamp = System.currentTimeMillis();

        if (!scheduledCommands.isEmpty() && Mod.MC.getConnection() != null && Mod.MC.level != null && Mod.MC.player != null) {
            ScheduledCommand scheduledCommand = scheduledCommands.getFirst();
            if (nextTimestamp + scheduledCommand.delay() > currentTimestamp) return;
            scheduledCommands.removeFirst();

            for (ChatConsumer chatConsumer : scheduledCommand.commandConsumers()) {
                chatConsumer.setTimestamp();
                addChatConsumer(chatConsumer);
            }
            for (SoundEvent soundEvent : scheduledCommand.soundHiders()) {
                addSoundHider(soundEvent);
            }

            Mod.sendCommand("/" + scheduledCommand.command());

            nextTimestamp = currentTimestamp + scheduledCommand.getDelay();
        }

        if (!chatConsumers.isEmpty()) {
            ArrayList<ChatConsumer> removeConsumers = new ArrayList<>();
            for (ChatConsumer chatConsumer : chatConsumers) {
                if (System.currentTimeMillis() > chatConsumer.timestamp()) {
                    chatConsumer.timeoutEvent.run();
                    removeConsumers.add(chatConsumer);
                }
            }
            chatConsumers.removeAll(removeConsumers);
        }
    }

    @Override
    public void tickF(int tick) {

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
        nextTimestamp = System.currentTimeMillis() + 250L;
    }

    @Override
    public void DFConnectDisconnect(ClientPacketListener networkHandler) {

    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(ClientCommandManager.literal("commandqueue")
                .executes(commandContext -> {
                    if (!scheduledCommands.isEmpty()) {
                        Mod.message("Command queue stack:");
                        for (ScheduledCommand scheduledCommand : scheduledCommands) {
                            Mod.message(Component.literal(" " + scheduledCommand.command())
                                    .append(Component.literal(" : ").withColor(ColorBank.MC_GRAY))
                                    .append(Component.literal(scheduledCommand.delay() + "ms").withColor(ColorBank.MC_GRAY))

                            );
                        }
                    } else {
                        Mod.message("Command queue stack is empty!");
                    }
                    return 1;
                })
        );

    }

    @Override
    public void receivePacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClientboundSoundPacket playSoundS2CPacket) {
            for (SoundEvent soundEvent : soundHiders) {
                if (playSoundS2CPacket.getSound().value().equals(soundEvent)) {
                    soundHiders.remove(soundEvent);
                    ci.cancel();
                    break;
                };
            }

        }
    }

    @Override
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {

    }
}
