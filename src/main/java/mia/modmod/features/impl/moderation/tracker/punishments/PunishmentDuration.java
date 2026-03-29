package mia.modmod.features.impl.moderation.tracker.punishments;

public enum PunishmentDuration {
    WARNING(""),
    D1("1d"),
    D3("3d"),
    D7("7d"),
    D14("14d"),
    D30("30d"),
    D90("90d"),
    PERM("permanent");

    private String durationString;

    PunishmentDuration(String durationString) {
        this.durationString = durationString;
    }

    public String getDurationString() { return durationString; }
}
