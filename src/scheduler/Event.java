package scheduler;


import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Event {

    public static List<Event> events = new ArrayList<>();

    private boolean notified = false;

    private int eventId;
    private long creatorId;
    private String description;
    private ZonedDateTime dateTime;
    private static final String TIME_FORMAT = "MM/dd/yyyy hh:mm a";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT);

    protected Event(final int eventId, final long creatorId, final String description, final ZonedDateTime dateTime) {
        this.eventId = eventId;
        this.creatorId = creatorId;
        this.description = description;
        this.dateTime = dateTime;
    }

    public static DateTimeFormatter getDateTimeFormatter() {
        return DATE_TIME_FORMATTER;
    }

    public int getEventId() {
        return eventId;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public String getDescription() {
        return description;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }
}
