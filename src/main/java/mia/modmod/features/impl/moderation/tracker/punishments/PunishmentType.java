package mia.modmod.features.impl.moderation.tracker.punishments;

public enum PunishmentType {
    WARN("warn"),
    MUTE("mute"),
    BAN("ban");

    private final String commandHeader;

    PunishmentType(String commandHeader) {
        this.commandHeader = commandHeader;
    }
}
