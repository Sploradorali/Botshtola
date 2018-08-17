package function;

import java.awt.Color;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import core.Responder;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class GeneralEmbedManager {
	/**
     * Generates and sends a help embed listing public commands
     *
     * @param channel
     */
    public static void sendHelpEmbed(MessageChannel channel) {
        channel.sendMessage(
                new EmbedBuilder()
                        .setColor(Color.LIGHT_GRAY)
                        .setTitle("Help", null)
                        .setDescription(
                                "All commands begin with \"" + Responder.getPrefix() + "\"\n\n"
                                        + "_General commands_:\n"
                                        + ":small_blue_diamond: **" + Responder.getPrefix() + "addjob <JOBABBR>**: Add a job to your list of roles.\n"
                                        + ":small_blue_diamond: **" + Responder.getPrefix() + "removejob <JOBABBR>**: Remove a job from your list of roles.\n"
                                        + ":small_blue_diamond: **" + Responder.getPrefix() + "jobs <@MENTION>**: List a members jobs. Leave blank to list own.\n"
                                        + ":small_blue_diamond: **" + Responder.getPrefix() + "roll <MAX>**: Roll for gear! Default: up to 100.\n"
                                        + ":small_blue_diamond: **" + Responder.getPrefix() + "ping <URL>**: Pings a provided URL (leave blank for Google).\n"
                                        + ":small_blue_diamond: **" + Responder.getPrefix() + "serverstatus <OPTIONAL SERVER>**: Gets an FFXIV server status.\n"
                                        + ":small_blue_diamond: **" + Responder.getPrefix() + "timetoreset**: Gets the time until the next weekly reset.\n"
                                        + "\n"
                                        + "_Scheduler commands_:\n"
                                        + ":calendar: **" + Responder.getPrefix() + "newevent <'description'> <DATE MM/dd/yyyy> <TIME hh:mmAM/PM>** Creates an event.\n"
                                        + ":calendar: **" + Responder.getPrefix() + "event <EVENT ID>**: Looks up event information.\n"
                                        + ":calendar: **" + Responder.getPrefix() + "joinevent <EVENT ID>**: Joins the roster of an event.\n"
                                        + ":calendar: **" + Responder.getPrefix() + "leaveevent <EVENT ID>**: Leaves the roster of an event.\n"
                                        + ":calendar: **" + Responder.getPrefix() + "cancelevent <EVENT ID>**: Cancels an event if you're the creator.\n"
                                        + ":calendar: **" + Responder.getPrefix() + "myevents**: Brings up a list of events you're signed up for.\n"
                                        + "\n"
                                        + "_XIVDB (FFXIV database) commands_:\n"
                                        + ":book: **" + Responder.getPrefix() + "char <KEYWORD or ID>**: Search for a character.\n"
                                        + ":book: **" + Responder.getPrefix() + "setme <CHARACTER ID>**: Sets your in-game character to your Discord identity.\n"
                                        + ":book: **" + Responder.getPrefix() + "me <CHARACTER ID>**: Quickly produces your personal character info.\n"
                                        + ":book: **" + Responder.getPrefix() + "charof <@MENTION>**: Brings up the personal character info of the tagged user.\n"
                                        + ":book: **" + Responder.getPrefix() + "clearme**: Clears your character info.\n"
                                        + ":book: **" + Responder.getPrefix() + "seejobs <CHARACTER ID>**: Brings up the selected character's classes.\n"
                                        + ":book: **" + Responder.getPrefix() + "myjobs**: Brings up your selected character's classes."
                                        + ":book: **" + Responder.getPrefix() + "item <KEYWORD(s) or ID>**: Searches for an item.\n"
                                        + ":book: **" + Responder.getPrefix() + "quest <KEYWORD(s) or ID>**: Search for a quest.\n"
                                        + ":book: **" + Responder.getPrefix() + "fate <KEYWORD(s) or ID>**: Search for a FATE.\n"
                                        + ":book: **" + Responder.getPrefix() + "achievement <KEYWORD(s) or ID>**: Search for an achievement.\n"
                                        + ":book: **" + Responder.getPrefix() + "instance <KEYWORD(s) or ID>**: Search for an instance (i.e. dungeon, raid, etc.).\n"
                        )
                        .build()
        ).queue();
    }

    /**
     * Sends an embed to confirm a member's Job add
     *
     * @param job
     * @param event
     */
    public static void sendAddJobEmbed(Job job, MessageReceivedEvent event) {
        event.getTextChannel().sendMessage(
                new EmbedBuilder()
                        .setColor(Color.decode(job.getColorHex()))
                        .setTitle("New job!", null)
                        .setDescription("\n"
                                + event.getMember().getAsMention() + " has added "
                                + job.getName() + " to their list of jobs!")
                        .setThumbnail(job.getImgUrl())
                        .build()
        ).queue();
    }

    /**
     * Sends an embed to confirm a member's Job removal
     *
     * @param job
     * @param event
     */
    public static void sendRemoveJobEmbed(Job job, MessageReceivedEvent event) {
        event.getTextChannel().sendMessage(
                new EmbedBuilder()
                        .setColor(Color.decode(job.getColorHex()))
                        .setTitle("Removed job...", null)
                        .setDescription("\n"
                                + event.getMember().getAsMention() + " has removed "
                                + job.getName() + " from their list of jobs!")
                        .setThumbnail(job.getImgUrl())
                        .build()
        ).queue();
    }

    /**
     * Sends an embed listing member's Job roles
     *
     * @param member
     * @param ch
     */
    public static void sendJobsEmbed(Member member, MessageChannel ch) {
        List<Role> jobs = member.getRoles();
        StringBuilder sb = new StringBuilder();
        for (Role job : jobs) {
            sb.append(":small_blue_diamond: ")
                    .append(job.getName())
                    .append("\n");
        }

        ch.sendMessage(
                new EmbedBuilder()
                        .setColor(Color.BLACK)
                        .setTitle(member.getNickname() + "'s Jobs")
                        .setDescription(sb.toString())
                        .setThumbnail(member.getUser().getAvatarUrl())
                        .build()
        ).queue();
    }

    public static void sendStatusEmbed(MessageChannel ch, String serverName, int flag) {
        StringBuilder sb = new StringBuilder(serverName)
                .append(" is currently ");
        Color embedColor = null;
        switch (flag) {
            case 1:
                sb.append("**ONLINE**! :white_check_mark:");
                embedColor = Color.GREEN;
                break;
            case 0:
                sb.append("**OFFLINE**. :red_circle:");
                embedColor = Color.RED;
                break;
            default:
                sb.append("**UNKNOWN**.");
                embedColor = Color.GRAY;
                break;
        }

        ch.sendMessage(
                new EmbedBuilder()
                        .setColor(embedColor)
                        .setTitle("Status: " + serverName)
                        .setDescription(
                                sb.toString()
                        ).build()
        ).queue();
    }

    /**
     * Welcomes members
     *
     * @param channel
     * @param objMember
     */
    public static void sendWelcomeEmbed(MessageChannel channel, Member objMember) {

        channel.sendMessage(
                new EmbedBuilder()
                        .setColor(Color.CYAN)
                        .setTitle("Welcome!", null)
                        .setDescription(
                                objMember.getAsMention()
                                        + " has joined the server!\n\n"
                                        + "You've been added to the Guest role by default.\n"
                                        + "Please tag @Staff with your in-game name if you're an FC member!\n\n"
                                        + "Please use "
                                        + Responder.getPrefix()
                                        + "addjob <JOBABBR> to add your job(s) to your server roles in #bot-center\n"
                                        + "and use"
                                        + Responder.getPrefix()
                                        + "help for other ways I can help you!"
                        )
                        .setThumbnail(objMember.getUser().getAvatarUrl())
                        .build()
        ).queue();
    }

    public static void sendTimeToResetEmbed(String d, String h, String m, String s, MessageChannel ch) {
        ch.sendMessage(
                new EmbedBuilder()
                        .setTitle("Time to Weekly Reset")
                        .setDescription(d + "d, " + h + "h, " + m + "m, " + s + "s")
                        .setTimestamp(
                                ZonedDateTime.now().withZoneSameInstant(ZoneId.of("America/Los_Angeles"))
                        )
                        .build()
        ).queue();
    }
}
