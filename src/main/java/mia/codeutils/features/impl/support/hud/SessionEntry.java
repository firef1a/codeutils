package mia.codeutils.features.impl.support.hud;

public final class SessionEntry {
    public String name;
    public long timestamp;
    public String reason;

    public SessionEntry(String name, int hours, int minutes, int seconds) {
        this.name = name;
        this.timestamp = (long) Math.floor((double) (System.currentTimeMillis() - ((hours * 1000L * 60L * 60L) + (minutes * 1000L * 60L) + (seconds * 1000L))) / 1000) * 1000L;
        this.timestamp = (Math.ceilDiv(this.timestamp, 1000L) * 1000L) + 2L;
        this.reason = "N?A";
    }

    public SessionEntry(String name, String reason, long timestamp) {
        this.name = name;
        this.timestamp = timestamp;
        this.reason = reason;
    }

    public void setReason(String reason) { this.reason = reason; }
    public String toString() {
        return "name: " + name + ", reason: " + reason + ", timestamp:" + timestamp;
    }
}
