package core;

import java.util.List;

import function.GeneralCommandCenter;
import function.GeneralEmbedManager;
import function.ResetMaintenanceTimer;
import function.RoleCommandCenter;
import function.XIVServerStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import scheduler.EventEmbedManager;
import scheduler.SchedulerCommandCenter;
import xivdb.XIVDBCommandCenter;

public class Responder extends ListenerAdapter {
	private static String prefix = ">";

    public static String getPrefix() {
        return prefix;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        Message message = event.getMessage();
        String rawMessage = message.getContentRaw();
        User user = event.getAuthor();
        Member member = event.getMember();
        TextChannel ch = event.getTextChannel();

        if (!user.isBot()) {
            if (rawMessage.startsWith(Responder.getPrefix())) {

                String base;
                GuildController gc = new GuildController(event.getGuild());
                boolean hasParameters = false;

                if (rawMessage.contains(" ")) {
                    base = rawMessage.substring(1, rawMessage.indexOf(" "));
                    hasParameters = true;
                } else {
                    base = rawMessage.substring(1);
                }

                /* COMMAND SELECTOR */
                switch (base) {
                    /* General use commands */

                    case "help":
                        GeneralCommandCenter.help(user, message);
                        break;

                    case "ping":
                        if (hasParameters) {
                            GeneralCommandCenter.ping(message.getContentRaw().split(" ")[1], event);
                        } else {
                            GeneralCommandCenter.ping("www.google.com", event);
                        }
                        break;

                    case "roll":
                        if (hasParameters) {
                            GeneralCommandCenter.roll(message.getContentRaw().split(" ")[1], event);
                        } else {
                            GeneralCommandCenter.roll("100", event);
                        }
                        break;

                    case "serverstatus":
                        if (hasParameters) {
                            XIVServerStatus.requestStatus(rawMessage.split(" ")[1], ch);
                        } else {
                            XIVServerStatus.requestStatus("Behemoth", ch);
                        }
                        break;

                    case "timetoreset":
                        ResetMaintenanceTimer.timeTo(
                                ResetMaintenanceTimer.findNextReset(),
                                ch
                        );
                        break;

                    /* Role commands */
                    case "addjob":
                        if (hasParameters) {
                            RoleCommandCenter.addJob(message.getContentRaw().split(" ")[1], event);
                        } else {
                            RoleCommandCenter.addJob("ERR", event);
                        }
                        break;

                    case "removejob":
                        if (hasParameters) {
                            RoleCommandCenter.removeJob(message.getContentRaw().split(" ")[1], event);
                        } else {
                            RoleCommandCenter.removeJob("ERR", event);
                        }
                        break;

                    case "jobs":
                        List<Member> jobsMentioned = message.getMentionedMembers();
                        if (jobsMentioned.isEmpty()) {
                            RoleCommandCenter.getAllJobs(member, ch);
                        } else {
                            RoleCommandCenter.getAllJobs(jobsMentioned.get(0), ch);
                        }
                        break;

                    /* XIVDB commands */
                    case "char":
                        if (hasParameters) {
                            XIVDBCommandCenter.getCharacterSummary(rawMessage.substring(rawMessage.indexOf(" ") + 1), ch);
                        } else {
                            XIVDBCommandCenter.getCharacterSummary("", ch);
                        }
                        break;

                    case "seejobs":
                        if (hasParameters) {
                            XIVDBCommandCenter.getCharacterClasses(rawMessage.substring(rawMessage.indexOf(" ") + 1), event);
                        } else {
                            XIVDBCommandCenter.getCharacterClasses("", event);
                        }
                        break;

                    case "myjobs":
                        XIVDBCommandCenter.getCharacterClasses("#me", event);
                        break;

                    case "charof":
                        List<User> charOfMentioned = message.getMentionedUsers();
                        if (charOfMentioned.isEmpty()) {
                            ch.sendMessage("Please mention a user!").queue();
                        } else {
                            XIVDBCommandCenter.getCharacter(charOfMentioned.get(0).getIdLong(), member, ch);
                        }
                        break;

                    case "me":
                        XIVDBCommandCenter.getCharacter(user.getIdLong(), member, ch);
                        break;

                    case "setme":
                        if (hasParameters) {
                            try {
                                XIVDBCommandCenter
                                        .setOwnCharacter(Long.parseLong(message.getContentRaw().split(" ")[1]),
                                                user.getIdLong(), member, ch);
                            } catch (NumberFormatException ex) {
                                ch.sendMessage("Please enter a valid ID!").queue();
                            }
                        } else {
                            XIVDBCommandCenter.getCharacter(user.getIdLong(), member, ch);
                        }
                        break;

                    case "clearme":
                        XIVDBCommandCenter.clearOwnCharacter(member, ch);
                        break;

                    case "item":
                        if (hasParameters) {
                            XIVDBCommandCenter.itemSearch(rawMessage.substring(rawMessage.indexOf(" ") + 1), ch);
                        } else {
                            XIVDBCommandCenter.itemSearch("", ch);
                        }
                        break;

                    case "quest":
                        if (hasParameters) {
                            XIVDBCommandCenter.questSearch(rawMessage.substring(rawMessage.indexOf(" ") + 1), ch);
                        } else {
                            XIVDBCommandCenter.questSearch("", ch);
                        }
                        break;

                    case "fate":
                        if (hasParameters) {
                            XIVDBCommandCenter.fateSearch(rawMessage.substring(rawMessage.indexOf(" ") + 1), ch);
                        } else {
                            XIVDBCommandCenter.fateSearch("", ch);
                        }
                        break;

                    case "achievement":
                        if (hasParameters) {
                            XIVDBCommandCenter.achievementSearch(rawMessage.substring(rawMessage.indexOf(" ") + 1), ch);
                        } else {
                            XIVDBCommandCenter.achievementSearch("", ch);
                        }
                        break;

                    case "instance":
                        if (hasParameters) {
                            XIVDBCommandCenter.instanceSearch(rawMessage.substring(rawMessage.indexOf(" ") + 1), ch);
                        } else {
                            XIVDBCommandCenter.instanceSearch("", ch);
                        }
                        break;

                    /* Scheduling commands */
                    case "newevent":
                        if (hasParameters) {
                            SchedulerCommandCenter.createEvent(rawMessage.substring(rawMessage.indexOf(" ") + 1), event);
                        } else {
                        	SchedulerCommandCenter.createEvent("0", event);
                        }
                        break;

                    case "event":
                        if (hasParameters) {
                            EventEmbedManager.sendEventInformationEmbed(
                                    rawMessage.substring(rawMessage.indexOf(" ") + 1).matches("\\d*") ?
                                            Integer.parseInt(rawMessage.substring(rawMessage.indexOf(" ") + 1)) :
                                            0,
                                    event);
                        } else {
                            ch.sendMessage("Please specify an event ID!").queue();
                        }
                        break;

                    case "joinevent":
                        if (hasParameters) {
                            SchedulerCommandCenter.joinUserEvent(member,
                                    rawMessage.substring(rawMessage.indexOf(" ") + 1).matches("\\d*") ?
                                            Integer.parseInt(rawMessage.substring(rawMessage.indexOf(" ") + 1)) :
                                            0,
                                    event);
                        } else {
                            ch.sendMessage("Please specify an event ID!").queue();
                        }
                        break;

                    case "leaveevent":
                        if (hasParameters) {
                            SchedulerCommandCenter.leaveUserEvent(member,
                                    rawMessage.substring(rawMessage.indexOf(" ") + 1).matches("\\d*") ?
                                            Integer.parseInt(rawMessage.substring(rawMessage.indexOf(" ") + 1)) :
                                            0,
                                    event);
                        } else {
                            ch.sendMessage("Please specify an event ID!").queue();
                        }
                        break;

                    case "cancelevent":
                        if (hasParameters) {
                            SchedulerCommandCenter.cancelEvent(member,
                                    rawMessage.substring(rawMessage.indexOf(" ") + 1).matches("\\d*") ?
                                            Integer.parseInt(rawMessage.substring(rawMessage.indexOf(" ") + 1)) :
                                            0,
                                    event);
                        } else {
                            ch.sendMessage("Please specify an event ID!").queue();
                        }
                        break;

                    case "myevents":
                        EventEmbedManager.sendUserEventsEmbed(member, ch);
                        break;

                    /* Administrator-only commands */
                    //TESTING

                    /* Invalid command */
                    default:
                        // Notifies user that they have entered an invalid command
                        ch.sendMessage("Sorry, "
                                + member.getNickname()
                                + ", I don't quite understand. "
                                + "Try **" + prefix + "help** to find out ways in which I can serve you.")
                                .queue();
                }

            }

        }
    }

    /**
     * Automated welcome message
     *
     * @param event
     */
    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Member objMember = event.getMember();
        Guild objGuild = event.getGuild();
        User objUser = event.getUser();

        if (!objUser.isBot()) {
            GuildController gc = new GuildController(event.getGuild());
            gc.addSingleRoleToMember(
                    objMember,
                    event.getGuild().getRolesByName("Guest", false).get(0))
                    .queue();
            GeneralEmbedManager.sendWelcomeEmbed(objGuild.getSystemChannel(), objMember);
        }
    }
}
