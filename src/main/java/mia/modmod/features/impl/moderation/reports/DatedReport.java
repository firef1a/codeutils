package mia.modmod.features.impl.moderation.reports;

public class DatedReport{
    private final String reporter, offender, offense, private_text, node_text, node_number, mode;
    private final long timestamp;
    private boolean handled;
    public DatedReport(
        String reporter,
        String offender,
        String offense,
        String private_text,
        String node_text,
        String node_number,
        String mode,
        long timestamp
    ) {
        this.reporter = reporter;
        this.offender = offender;
        this.offense = offense;
        this.private_text = private_text;
        this.node_text = node_text;
        this.node_number = node_number;
        this.mode = mode;
        this.timestamp = timestamp;
        this.handled = false;
    }

    public String reporter() { return reporter; }
    public String offender() { return offender; }
    public String offense() { return offense; }
    public String private_text() { return private_text; }
    public String node_text() { return node_text; }
    public String node_number() { return node_number; }
    public String mode() { return mode; }
    public long timestamp() { return timestamp; }
    public boolean handled() { return handled; }
    
    public String formattedLocation() { return private_text() + node_text() + " " + node_number(); }
    public String nodeIdentifier() { return private_text.isEmpty() ? "node" + node_number : "private" + node_number;}
    
    public int getReportHash() { return Math.abs((reporter() + offender() + offender() + private_text() + node_text() + node_number() + mode()).hashCode()) % 1000000; }

    public void setHandled(boolean handled) { this.handled = handled; }
}