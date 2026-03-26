package mia.modmod.features.impl.moderation.reports;

public record DatedReport(
        String reporter,
        String offender,
        String offense,
        String private_text,
        String node_text,
        String node_number,
        long timestamp
    ) { }
