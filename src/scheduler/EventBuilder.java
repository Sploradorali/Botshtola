package scheduler;

import java.time.ZonedDateTime;

public class EventBuilder {

    private int eventId;
    private long creatorId;
    private String description;
    private ZonedDateTime dateTime;

    public EventBuilder() {
    }

    public void eventId(int eventId) {
        this.eventId = eventId;
    }

    public void creatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public void description(String description) {
        this.description = description;
    }

    public void dateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Event build() {
        return new Event(eventId, creatorId, description, dateTime);
    }
}
