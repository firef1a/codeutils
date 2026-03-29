package mia.modmod.features.impl.moderation.tracker.punishments;

public enum ServerPunishmentType {
    KICK("kick"),
    WARN("warn"),
    MUTE("mute"),
    BAN("ban");

    private final String commandHeader;

    ServerPunishmentType(String commandHeader) {
        this.commandHeader = commandHeader;
    }
}
