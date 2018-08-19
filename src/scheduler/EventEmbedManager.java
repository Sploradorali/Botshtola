
package scheduler;

import core.Responder;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

public class EventEmbedManager {

    /*
     * TO-DO
     * - Cap events listed by sendUserEventsEmbed; try additional pages
     */

    /**
     * Sends an Embed signifying the creation of a new event
     *
     * @param event
     * @param e
     */
    public static void sendNewEventEmbed(Event event, MessageReceivedEvent e) {
        e.getTextChannel().sendMessage(
                new EmbedBuilder()
                        .setTitle("New event!")
                        .setThumbnail("https://ffxiv.gamerescape.com/w/images/5/54/Main_Command_14_Icon.png")
                        .setDescription(e.getMember().getAsMention() + " created a new event.\n")
                        .addField("Description", event.getDescription(), false)
                        .addField("Time", event.getDateTime().format(Event.getDateTimeFormatter()) + " Pacific", false)
                        .setFooter("Type " + Responder.getPrefix() + "joinevent " + event.getEventId() + " to join in!", null)
                        .build()
        ).queue();
    }

    /**
     * Sends an Embed signifying that a user has joined the roster of an event
     *
     * @param event
     * @param e
     */
    public static void sendJoinEventEmbed(Event event, MessageReceivedEvent e) {
        e.getTextChannel().sendMessage(
                new EmbedBuilder()
                        .setTitle("Joined event!")
                        .setThumbnail("https://ffxiv.gamerescape.com/w/images/5/54/Main_Command_14_Icon.png")
                        .setDescription(e.getMember().getAsMention() + " joined "
                                + event.getDescription() + "! (ID: " + event.getEventId() + ")\n\n")
                        .addField("Time", event.getDateTime().format(Event.getDateTimeFormatter()) + " Pacific", false)
                        .setFooter("Type " + Responder.getPrefix() + "leaveevent " + event.getEventId() + " to leave this roster.", null)
                        .build()
        ).queue();
    }

    /**
     * Sends an Embed signifying that a user has left the roster of an event
     *
     * @param event
     * @param e
     */
    public static void sendLeaveEventEmbed(Event event, MessageReceivedEvent e) {
        e.getTextChannel().sendMessage(
                new EmbedBuilder()
                        .setTitle("Left event!")
                        .setThumbnail("https://ffxiv.gamerescape.com/w/images/5/54/Main_Command_14_Icon.png")
                        .setDescription(e.getMember().getAsMention() + " left "
                                + event.getDescription() + "! (ID: " + event.getEventId() + ")\n\n")
                        .addField("Description", event.getDescription(), false)
                        .addField("Time", event.getDateTime().format(Event.getDateTimeFormatter()) + " Pacific", false)
                        .setFooter("Type " + Responder.getPrefix() + "joinevent " + event.getEventId()
                                + " to rejoin this event.", null)
                        .build()
        ).queue();
    }

    /**
     * Sends an Embed detailing information about an event
     *
     * @param eventId
     * @param e
     */
    public static void sendEventInformationEmbed(int eventId, MessageReceivedEvent e) {
        StringBuilder sb = new StringBuilder();
        List<Member> members = SchedulerCommandCenter.getEventUsers(eventId, e.getGuild());
        Event event = null;

        for (Event ev : Event.events) {
            if (ev.getEventId() == eventId) {
                event = ev;
                break;
            }
        }

        if (event == null) {
            e.getTextChannel().sendMessage(e.getMember().getNickname().isEmpty() ?
            		e.getMember().getEffectiveName() : e.getMember().getNickname() +
                    ", I couldn't find that event scheduled."
            ).queue();
            return;
        }

        for (Member member : members) {
            sb.append(":small_blue_diamond: ")
                    .append(member.getNickname().isEmpty() ?
                    		member.getEffectiveName() : member.getNickname())
                    .append("\n");
        }

        e.getTextChannel().sendMessage(
                new EmbedBuilder()
                        .setTitle("Event Information")
                        .setThumbnail("https://ffxiv.gamerescape.com/w/images/5/54/Main_Command_14_Icon.png")
                        .addField("Description", event.getDescription(), false)
                        .addField("Time", event.getDateTime().format(Event.getDateTimeFormatter()) + " Pacific", false)
                        .addField("Current Members", sb.toString(), false)
                        .setFooter("Type " + Responder.getPrefix() + "joinevent " + event.getEventId() + " to join in!", null)
                        .build()
        ).queue();
    }

    /**
     * Sends an Embed listing all events a user is a part of
     *
     * @param member
     * @param ch
     */
    public static void sendUserEventsEmbed(Member member, MessageChannel ch) {
        StringBuilder eventSB = new StringBuilder();

        List<Event> userEvents = SchedulerCommandCenter.getUserEvents(member);

        for (Event event : userEvents) {
            eventSB.append("\n")
                    .append(":small_blue_diamond: ")
                    .append(event.getEventId())
                    .append(": ")
                    .append(event.getDescription());
        }

        ch.sendMessage(
                new EmbedBuilder()
                        .setTitle(member.getNickname().isEmpty() ?
                        		member.getEffectiveName() : member.getNickname() + "'s Events")
                        .setDescription(eventSB.toString())
                        .build()
        ).queue();
    }

    /**
     * Sends an Embed signifying a cancelled event
     *
     * @param event
     * @param member
     * @param ch
     */
    public static void sendEventCancelledEmbed(Event event, Member member, MessageChannel ch) {
        if (event.getCreatorId() == member.getUser().getIdLong()) {
            ch.sendMessage(
                    new EmbedBuilder()
                            .setTitle("Event CANCELLED")
                            .setThumbnail("https://ffxiv.gamerescape.com/w/images/5/54/Main_Command_14_Icon.png")
                            .setDescription(
                                    member.getAsMention() + " has cancelled " +
                                            "**" + event.getDescription() + " (" + event.getEventId() + ")**!"
                            )
                            .build()
            ).queue();
        }
    }

    /**
     * Sends an Embed notifying a member of an approaching event
     *
     * @param event
     * @param member
     * @param ch
     */
    public static void sendNotificationEmbed(Event event, Member member, Guild guild, MessageChannel ch) {
        StringBuilder sb = new StringBuilder();
        List<Member> members = SchedulerCommandCenter.getEventUsers(event.getEventId(), guild);

        for (Event ev : Event.events) {
            if (ev.getEventId() == event.getEventId()) {
                event = ev;
                break;
            }
        }

        for (Member m : members) {
            sb.append(":small_blue_diamond: ")
                    .append(m.getNickname().isEmpty() ?
                    		m.getEffectiveName() : m.getNickname())
                    .append("\n");
        }

        Member creator = guild.getMemberById(event.getCreatorId());

        ch.sendMessage(
                new EmbedBuilder()
                        .setTitle("Event soon")
                        .setDescription("Hello, " + (member.getNickname().isEmpty() ?
                        		member.getEffectiveName() : member.getNickname()) + "!\n\n"
                                + "Just reminding you that you're scheduled for the following event!")
                        .addField("Description", event.getDescription(), false)
                        .addField("Time", event.getDateTime().format(Event.getDateTimeFormatter()) + " Pacific", false)
                        .addField("Creator", creator.getNickname().isEmpty() ?
                        		creator.getEffectiveName() : creator.getNickname(), false)
                        .addField("Current Members", sb.toString(), false)
                        .setFooter("Type " + Responder.getPrefix() + "joinevent " + event.getEventId() + " to join in!", null)
                        .setFooter("ID: " + event.getEventId(), null)
                        .build()
        ).queue();
    }

}
