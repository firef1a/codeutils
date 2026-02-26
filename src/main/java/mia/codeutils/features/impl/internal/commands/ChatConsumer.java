package mia.codeutils.features.impl.internal.commands;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatConsumer {
    Pattern pattern; Consumer<Matcher> successfulMatch; Runnable timeoutEvent; long timeout, timestamp; boolean cancelMessage;

    public ChatConsumer(Pattern pattern, Consumer<Matcher> successfulMatch, Runnable timeoutEvent, long timeout, boolean cancelMessage) {
        this.pattern = pattern;
        this.successfulMatch = successfulMatch;
        this.timeoutEvent = timeoutEvent;
        this.timeout =timeout;
        this.cancelMessage = cancelMessage;
    }

    public void setSuccessfulMatch(Consumer<Matcher> successfulMatch) {
        this.successfulMatch = successfulMatch;
    }

    public void setTimeoutEvent(Runnable timeoutEvent) {
        this.timeoutEvent = timeoutEvent;
    }

    public Pattern pattern() { return pattern; }
    public Consumer<Matcher> successfulMatch() { return successfulMatch; }
    public Runnable timeoutEvent() { return timeoutEvent; }
    public boolean cancelMessage() { return cancelMessage; }


    public void setTimestamp() {
        this.timestamp = System.currentTimeMillis() + timeout;
    }

    public long timestamp() {
        return timestamp;
    }

}
