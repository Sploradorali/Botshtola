package scheduler;


import database.DBInitialization;
import function.GeneralEmbedManager;
import core.Responder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
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
    	
    	boolean hasYear = true;
    	
        if (args == null) {
            mre.getTextChannel().sendMessage(
                    mre.getMember().getNickname().isEmpty() ?
                    		mre.getMember().getEffectiveName() : mre.getMember().getNickname() + ", I didn't quite get that.\n" +
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

            DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("M/d/yyyy");
            DateTimeFormatter formatTime = DateTimeFormatter.ofPattern("h:mma");

            if (dStr.matches("\\d{1,2}/\\d{1,2}")) {
            	int year = LocalDate.now().getYear();
            	dStr += "/" + year;
            }
            
            LocalDate d = LocalDate.parse(dStr, formatDate);
            if (d.plusDays(1).isBefore(LocalDate.now())) {
            	d = d.plusYears(1);
            }
            
            LocalTime t = LocalTime.parse(tStr, formatTime);

            pushEvent(description, d, t, mre);

        } catch (DateTimeParseException | StringIndexOutOfBoundsException ex) {
            mre.getTextChannel().sendMessage(
                    mre.getMember().getNickname().isEmpty() ?
                    		mre.getMember().getEffectiveName() : mre.getMember().getNickname() + ", I didn't quite get that.\n" +
                            "```Format: " + Responder.getPrefix() + "newevent 'DESC' DATE(mm/dd/yyyy) TIME(hh:mmAM/PM)\n" +
                            "Example: " + Responder.getPrefix() + "newevent 'sastasha hm' 8/10/2018 11:30PM```"
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

        populateEvents();
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
            
            populateEvents();
            
            EventEmbedManager.sendEventCancelledEmbed(ev, member, mre.getTextChannel());
        	
        } else {
            mre.getTextChannel().sendMessage(
                    mre.getMember().getNickname().isEmpty() ?
                    		mre.getMember().getEffectiveName() : mre.getMember().getNickname() + ", you can only cancel your own events... " +
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
            System.out.println("Deleted event " + eventId);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        populateEvents();
    }

    /**
     * Sets a user ID to an event ID
     *
     * @param member
     * @param eventId
     */
    public static void joinUserEvent(Member member, int eventId, MessageReceivedEvent mre) {
        Event newEvent = null;
        
        /* Create entry into userevents table */
        try (Connection con = DBInitialization.getConnection()) {
        	
            newEvent = pullEvent(eventId);
            
            if (newEvent != null) {
                EventEmbedManager.sendJoinEventEmbed(newEvent, mre);
            }
            
        } catch (DerbySQLIntegrityConstraintViolationException dup) {
            mre.getTextChannel()
                    .sendMessage(member.getNickname().isEmpty() ?
                    		mre.getMember().getEffectiveName() : mre.getMember().getNickname() + ", you're already scheduled for that!")
                    .queue();
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
    public static int joinUserEvent(Member member, int eventId) throws SQLException {
        String insert = "insert into userevents values (?, ?, ?)";
        PreparedStatement insertStmt = null;
        ResultSet rs = null;

        /* Create entry into userevents table */
        try (Connection con = DBInitialization.getConnection()) {
        	insertStmt = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setLong(1, member.getUser().getIdLong());
            insertStmt.setInt(2, eventId);
            insertStmt.setBoolean(3, false);
            insertStmt.executeUpdate();

            rs = insertStmt.getGeneratedKeys();

            return rs.next() ? rs.getInt("eventid") : 0;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Deletes a record from the userevent table. If the record refers to the creator
     * of the event, passCreator is called. If the creator role is passed successfully,
     * the member is removed from the event;
     *
     * @param member
     * @param eventId
     */
    public static void leaveUserEvent(Member member, int eventId, MessageReceivedEvent mre) {
        String delete = "delete from userevents where userid = ? and eventid = ?";
        PreparedStatement deleteStmt = null;
        Event event = null;

        for (Event ev : Event.events) {
            if (ev.getEventId() == eventId) {
                event = ev;
                break;
            }
        }

        if (event.getCreatorId() == member.getUser().getIdLong() && !passCreator(event, mre)) return;
        
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
     * Checks if the event has more than one member. If so, the creator role is
     * passed onto the next member. If not, the event is cancelled.
     * 
     * @param event
     * @param mre
     */
    public static boolean passCreator(Event event, MessageReceivedEvent mre) {
    	boolean passedToNewCreator = false;
    	
    	String select = "select userid from userevents where eventid = ?";
    	PreparedStatement statement = null;
    	ResultSet rs;
    	
    	try (Connection con = DBInitialization.getConnection()) {
    		statement = con.prepareStatement(select);
    		statement.setInt(1, event.getEventId());
    		rs = statement.executeQuery();
    		
    		if (rs.next()) {
    			long newCreator = rs.getLong("userid");
    			if (newCreator != event.getCreatorId()) {
    				String update = "update events set creatorid = ? where eventid = ?";
    				statement = con.prepareStatement(update);
    				statement.setLong(1, newCreator);
    				statement.setInt(2, event.getEventId());
    				mre.getTextChannel().sendMessage(
    						mre.getGuild().getMemberById(newCreator) 
	    						+ " is now the party leader of " 
								+ event.getDescription() 
	    						+ " (" + event.getEventId() + ")!"
    						).queue();
    				passedToNewCreator = true;
    			} else {
    				cancelEvent(mre.getMember(), event.getEventId(), mre);
    			}
    		} else {
				cancelEvent(mre.getMember(), event.getEventId(), mre);
    		}
    		
    	} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
    	
    	return passedToNewCreator;    	
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
            
            System.out.println("Event list updated.");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        Event.events = newList;
    }

    /**
     * Notifies the members attending an event through private message, then sets
     * notified field to true
     *
     * @param event
     * @param guild
     */
    public static void notifyEventMembers(Event event, Guild guild) {
    	
    	String selectMembers = "select * from userevents where eventid = ? and notified = false";
    	PreparedStatement statement = null;
    	ResultSet rs = null;
    	
    	try (Connection con = DBInitialization.getConnection()) {
    		statement = con.prepareStatement(selectMembers);
    		statement.setInt(1, event.getEventId());
    		rs = statement.executeQuery();
    		
    		while (rs.next()) {
    			long userId = rs.getLong("userid");
    			
    			Member member = guild.getMemberById(userId);
    			
    			System.out.println(userId);
    			System.out.println(rs.getBoolean("notified"));
    			member.getUser()
    				.openPrivateChannel()
    				.queue((channel) -> 
    					EventEmbedManager.sendNotificationEmbed(event, member, guild, channel)
    					);
    			
    			String updateMembers = "update userevents set notified = true where eventid = ? and userid = ?";
    			statement = con.prepareStatement(updateMembers);
    			statement.setInt(1, event.getEventId());
    			statement.setLong(2, userId);
    			statement.executeUpdate();
    			
    		}
    	} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
    }

    /**
     * Checks event time against current time to flag for member notification and deletes
     * events more than 30 minutes past event time
     *
     * @param event
     * @param guild
     */
    public static void checkEvent(Event event, Guild guild) {
    	
    	Instant eventTimeAsInstant = event.getDateTime().toInstant();
    	long eventTimeMillis = eventTimeAsInstant.toEpochMilli();
    	
    	long nowMillis = Instant.now().toEpochMilli();

    	if ((eventTimeMillis + 1800000L) <= nowMillis) {
    		cancelEvent(event.getEventId());
    		populateEvents();
    	}
    	
    	if ((eventTimeMillis - 1800000L) <= nowMillis && eventTimeMillis > nowMillis) {
    		notifyEventMembers(event, guild);
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
