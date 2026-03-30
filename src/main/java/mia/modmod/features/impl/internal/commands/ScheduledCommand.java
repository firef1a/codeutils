package mia.modmod.features.impl.internal.commands;

import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.List;

public record ScheduledCommand(String command, long delay, List<ChatConsumer> commandConsumers, List<SoundEvent> soundHiders) {
    public ScheduledCommand(String command, long delay, List<ChatConsumer> commandConsumers) {
        this(command, delay, commandConsumers, new ArrayList<>());
    }
    public ScheduledCommand(String command, long delay) {
        this(command, delay, new ArrayList<>());
    }
    public ScheduledCommand(String command) {
        this(command, 0L);
    }
    public long getDelay() {
        return (50L * (command.length()) + 25L) + 250L;
    }
}
