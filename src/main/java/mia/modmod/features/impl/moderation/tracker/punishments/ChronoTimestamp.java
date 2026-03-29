package mia.modmod.features.impl.moderation.tracker.punishments;

public class ChronoTimestamp {
    private long timestamp;

    private ChronoTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() { return timestamp; }

    public static ChronoTimestamp ABSOLUTE_from_Timestamp(long timestamp) { return new ChronoTimestamp(timestamp); }
    public static ChronoTimestamp PAST_from_DHMS(int d, int h, int m, int s) {
        return new ChronoTimestamp(
                (System.currentTimeMillis()) - ((
                        (s) + (m * 60L) + (h * 60L * 60L) + (d * 24L * 60 * 60L)
                        ) * 1000L)
        );
    }

}
