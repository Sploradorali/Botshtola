package scheduler;


import database.DBInitialization;
import core.Responder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.derby.shared.common.error.DerbySQLIntegrityConstraintViolationException;

public class SchedulerCommandCenter {

    /*
     * TO-DO
     * - Introduce limit for number of members joining into events
     * - Figure out more intuitive newevent command
     *
     */

    private SchedulerCommandCenter() {
    }

    public static void createEvent(String args, MessageReceivedEvent mre) {
        if (args == null) {
            mre.getTextChannel().sendMessage(
                    mre.getMember().getNickname() + ", I didn't quite get that.\n" +
                            "```Format: " + Responder.getPrefix() + "newevent 'DESC' DATE(mm/dd/yyyy) TIME(hh:mmAM/PM)\n" +
                            "Example: " + Responder.getPrefix() + "newevent 'sastasha hm' 08/10/2018 11:30PM```"
            ).queue();
            return;
        }

        try {
            String description = args.substring(args.indexOf("'") + 1);
            String dtStr = description.substring(description.indexOf("'") + 2).toUpperCase();

            String dStr = dtStr.substring(0, dtStr.indexOf(" "));
            String tStr = dtStr.substring(dtStr.indexOf(" ") + 1);

            description = description.substring(0, description.indexOf("'"));

            DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            DateTimeFormatter formatTime = DateTimeFormatter.ofPattern("hh:mma");

            LocalDate d = LocalDate.parse(dStr, formatDate);
            LocalTime t = LocalTime.parse(tStr, formatTime);

            pushEvent(description, d, t, mre);

        } catch (DateTimeParseException | StringIndexOutOfBoundsException ex) {
            mre.getTextChannel().sendMessage(
                    mre.getMember().getNickname() + ", I didn't quite get that.\n" +
                            "```Format: " + Responder.getPrefix() + "newevent 'DESC' DATE(mm/dd/yyyy) TIME(hh:mmAM/PM)\n" +
                            "Example: " + Responder.getPrefix() + "newevent 'sastasha hm' 08/10/2018 11:30PM```"
            ).queue();
        }

    }

    /**
     * Inserts a new event into the database
     *
     * @param description
     * @param d
     * @param t
     * @param mre
     */
    public static void pushEvent(String description, LocalDate d, LocalTime t, MessageReceivedEvent mre) {
        LocalDateTime ldt = LocalDateTime.of(d, t).withSecond(0).withNano(0);
        ZonedDateTime utc = ldt.atZone(ZoneId.of("UTC"));
        ZonedDateTime zdt = utc.withZoneSameInstant(ZoneId.of("America/Los_Angeles"));
        long epochsec = zdt.toEpochSecond();

        String insert = "insert into events (creatorid, description, epochtime) values (?, ?, ?)";
        PreparedStatement insertStmt;

        try (Connection con = DBInitialization.getConnection()) {
            insertStmt = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setLong(1, mre.getAuthor().getIdLong());
            insertStmt.setString(2, description);
            insertStmt.setLong(3, epochsec);
            insertStmt.executeUpdate();

            ResultSet rs = insertStmt.getGeneratedKeys();

            Event newEvent = null;

            if (rs.next()) {
                newEvent = pullEvent(rs.getInt(1));
                Event.events.add(newEvent);
                joinUserEvent(mre.getMember(), newEvent.getEventId());
            }

            EventEmbedManager.sendNewEventEmbed(newEvent, mre);

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves single event from the database and produces an Event object
     *
     * @param eventId
     * @return
     */
    public static Event pullEvent(int eventId) {

        EventBuilder builder = new EventBuilder();
        long creatorId = 0;
        String description = null;
        long epochTime;
        ZonedDateTime eventDateTime = null;

        String select = "select * from events where eventid = ?";
        PreparedStatement selectStmt = null;

        try (Connection con = DBInitialization.getConnection()) {
            selectStmt = con.prepareStatement(select);
            selectStmt.setInt(1, eventId);
            ResultSet rs = selectStmt.executeQuery();

            while (rs.next()) {
                creatorId = rs.getLong("creatorid");
                description = rs.getString("description");
                epochTime = rs.getLong("epochtime");
                eventDateTime = LocalDateTime.ofEpochSecond(
                        epochTime, 0, ZoneOffset.UTC).atZone(ZoneId.of("America/Los_Angeles")
                );
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        builder.eventId(eventId);
        builder.creatorId(creatorId);
        builder.description(description);
        builder.dateTime(eventDateTime);
        return builder.build();
    }

    /**
     * Deletes an event by eventId
     *
     * @param eventId
     */
    public static void cancelEvent(Member member, int eventId, MessageReceivedEvent mre) {
        String deleteEvent = "delete from events where eventid = ?";
        String deleteUserEvent = "delete from userevents where eventid = ?";
        PreparedStatement deleteEventStmt = null;
        PreparedStatement deleteUserEventStmt = null;

        Event ev = pullEvent(eventId);

        if (mre.getAuthor().getIdLong() == ev.getCreatorId()) {

            try (Connection con = DBInitialization.getConnection()) {
                deleteEventStmt = con.prepareStatement(deleteEvent);
                deleteEventStmt.setInt(1, eventId);
                deleteEventStmt.executeUpdate();

                deleteUserEventStmt = con.prepareStatement(deleteUserEvent);
                deleteUserEventStmt.setInt(1, eventId);
                deleteUserEventStmt.executeUpdate();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            EventEmbedManager.sendEventCancelledEmbed(ev, member, mre.getTextChannel());
        } else {
            mre.getTextChannel().sendMessage(
                    mre.getMember().getNickname() + ", you can only cancel your own events... " +
                            "A little pretentious, don't you think?"
            ).queue();
        }
    }

    /**
     * Deletes an event by eventId
     *
     * @param eventId
     */
    public static void cancelEvent(int eventId) {
        String deleteEvent = "delete from events where eventid = ?";
        String deleteUserEvent = "delete from userevents where eventid = ?";
        PreparedStatement deleteEventStmt = null;
        PreparedStatement deleteUserEventStmt = null;

        try (Connection con = DBInitialization.getConnection()) {
            deleteEventStmt = con.prepareStatement(deleteEvent);
            deleteEventStmt.setInt(1, eventId);
            deleteEventStmt.executeUpdate();

            deleteUserEventStmt = con.prepareStatement(deleteUserEvent);
            deleteUserEventStmt.setInt(1, eventId);
            deleteUserEventStmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets a user ID to an event ID
     *
     * @param member
     * @param eventId
     */
    public static void joinUserEvent(Member member, int eventId, MessageReceivedEvent mre) {
        String insert = "insert into userevents values (?, ?)";
        PreparedStatement insertStmt = null;

        Event newEvent = null;
        /* Create entry into userevents table */
        try (Connection con = DBInitialization.getConnection()) {
            insertStmt = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setLong(1, member.getUser().getIdLong());
            insertStmt.setInt(2, eventId);
            insertStmt.executeUpdate();

            ResultSet rs = insertStmt.getGeneratedKeys();

            newEvent = pullEvent(rs.getInt(1));
        } catch (DerbySQLIntegrityConstraintViolationException dup) {
            mre.getTextChannel()
                    .sendMessage(member.getNickname() + ", you're already scheduled for that!")
                    .queue();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (newEvent != null) {
            EventEmbedManager.sendJoinEventEmbed(newEvent, mre);
        }
    }

    /**
     * Sets a user ID to an event ID
     *
     * @param member
     * @param eventId
     */
    public static void joinUserEvent(Member member, int eventId) {
        String insert = "insert into userevents values (?, ?)";
        PreparedStatement insertStmt = null;

        /* Create entry into userevents table */
        try (Connection con = DBInitialization.getConnection()) {
            insertStmt = con.prepareStatement(insert);
            insertStmt.setLong(1, member.getUser().getIdLong());
            insertStmt.setInt(2, eventId);
            insertStmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a record from the userevent table
     *
     * @param member
     * @param eventId
     */
    public static void leaveUserEvent(Member member, int eventId, MessageReceivedEvent mre) {
        String delete = "delete from userevents where memberid = ?, eventid = ?";
        PreparedStatement deleteStmt = null;
        Event event = null;

        for (Event ev : Event.events) {
            if (ev.getEventId() == eventId) {
                event = ev;
                break;
            }
        }

        /* Create entry into userevents table */
        try (Connection con = DBInitialization.getConnection()) {
            deleteStmt = con.prepareStatement(delete);
            deleteStmt.setLong(1, member.getUser().getIdLong());
            deleteStmt.setInt(2, eventId);
            deleteStmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        EventEmbedManager.sendLeaveEventEmbed(event, mre);
    }

    /**
     * Queries userevents database for all events a user is participating in
     *
     * @param member
     * @return
     */
    public static List<Event> getUserEvents(Member member) {
        List<Integer> eventIds = new ArrayList<>();
        List<Event> userEvents = new ArrayList<>();

        String selectEvents = "select eventid from userevents where userid = ?";
        PreparedStatement selectEventsStmt = null;

        /* Retrieves all user entries */
        try (Connection con = DBInitialization.getConnection()) {
            selectEventsStmt = con.prepareStatement(selectEvents);
            selectEventsStmt.setLong(1, member.getUser().getIdLong());
            ResultSet eventIdRs = selectEventsStmt.executeQuery();

            /* Creates Event instances for each event ID */
            while (eventIdRs.next()) {
                int id = eventIdRs.getInt("eventid");
                eventIds.add(id);
            }

            for (int eventId : eventIds) {
                userEvents.add(pullEvent(eventId));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return userEvents;
    }

    /**
     * Queries userevents for users participating in an event
     *
     * @param eventId
     * @return
     */
    public static List<Member> getEventUsers(int eventId, Guild guild) {
        List<Member> members = new ArrayList<>();

        String selectMembers = "select userid from userevents where eventid = ?";
        PreparedStatement selectMembersStmt = null;

        try (Connection con = DBInitialization.getConnection()) {
            selectMembersStmt = con.prepareStatement(selectMembers);
            selectMembersStmt.setInt(1, eventId);
            ResultSet membersRs = selectMembersStmt.executeQuery();

            while (membersRs.next()) {
                long memberId = membersRs.getLong("userid");
                Member member = guild.getMemberById(memberId);
                //MainConnection.jda.getUserById(memberId);
                members.add(member);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return members;
    }

    /**
     * Populates the list of events with all database items
     */
    public static void populateEvents() {
        int eventId;
        long creatorId;
        String description;
        long epochTime;
        ZonedDateTime eventDateTime;

        List<Event> newList = new ArrayList<>();

        String select = "select * from events";
        PreparedStatement selectStmt = null;
        ResultSet rs;

        try (Connection con = DBInitialization.getConnection()) {
            selectStmt = con.prepareStatement(select);
            rs = selectStmt.executeQuery();

            while (rs.next()) {
                EventBuilder builder = new EventBuilder();

                eventId = rs.getInt("eventid");
                creatorId = rs.getLong("creatorid");
                description = rs.getString("description");
                epochTime = rs.getLong("epochtime");
                eventDateTime = LocalDateTime.ofEpochSecond(
                        epochTime, 0, ZoneOffset.UTC).atZone(ZoneId.of("America/Los_Angeles")
                );

                builder.eventId(eventId);
                builder.creatorId(creatorId);
                builder.description(description);
                builder.dateTime(eventDateTime);
                newList.add(builder.build());
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        Event.events = newList;
    }

    /**
     * Notifies the members attending an event through private message
     *
     * @param event
     * @param guild
     */
    public static void notifyEventMembers(Event event, Guild guild) {
        List<Member> members = getEventUsers(event.getEventId(), guild);
        for (Member member : members)
            member.getUser().openPrivateChannel().queue(channel -> EventEmbedManager.sendNotificationEmbed(
                    event,
                    member,
                    guild,
                    channel
            ));
    }

    /**
     * Checks event time against current time to flag for member notifciation
     *
     * @param event
     * @param guild
     */
    public static void checkToNotify(Event event, Guild guild) {
        if (!event.isNotified()) {
            Instant eventTime = event.getDateTime().toInstant();
            if (eventTime.minusMillis(1800000).isBefore(Instant.now())
                    && eventTime.isAfter(Instant.now())) {
                notifyEventMembers(event, guild);
                event.setNotified(true);
            }
        }
    }

    /**
     * TESTING ONLY: Prints all events as Discord messages
     *
     * @param ch
     */
    public static void printEvents(MessageChannel ch) {
        for (Event event : Event.events) {
            ch.sendMessage(event.getCreatorId() + ": " + event.getDescription()).queue();
        }
    }

}
