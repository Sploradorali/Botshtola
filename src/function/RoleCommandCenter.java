package function;

import java.util.List;

import core.Responder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.GuildController;

public class RoleCommandCenter {
	private RoleCommandCenter() {
    }

    /**
     * Adds a job to a user's roles
     *
     * @param job
     * @param event
     */
    public static void addJob(String job, MessageReceivedEvent event) {
        job = job.toUpperCase();
        if (!job.matches("[A-Z]{3}")) {
            event.getChannel().sendMessage("Please use the following format to add a job:\n"
                    + "```" + Responder.getPrefix() + "addjob JOB```\n\n"
                    + "Valid job abbreviations:\n"
                    + "```AST, BLM, BRD, DRK, DRG, MCH, NIN, PLD, RDM, SAM, SCH, SMN, WAR, WHM```").queue();
            return;
        }


        try {

            Job selection = Job.valueOf(job);

            for (Role role : event.getMember().getRoles()) {
                if (role.getName().matches(selection.getName())) {
                    event.getChannel().sendMessage(event.getMember().getNickname()
                            + ", you're already a " + selection.getName() + ".")
                            .queue();
                    return;
                }
            }

            System.out.println("Valid job entered: " + job + " (" + selection.getName() + ")");
            GuildController gc = new GuildController(event.getGuild());
            gc.addSingleRoleToMember(
                    event.getMember(),
                    event.getGuild().getRolesByName(selection.getName(), true).get(0)
            ).queue();
            GeneralEmbedManager.sendAddJobEmbed(selection, event);
        } catch (IllegalArgumentException ex) {
            event.getChannel().sendMessage("Please use the following format to add a job:\n"
                    + "```" + Responder.getPrefix() + "addjob JOB```\n\n"
                    + "Valid job abbreviations:\n"
                    + "```AST, BLM, BRD, DRK, DRG, MCH, NIN, PLD, RDM, SAM, SCH, SMN, WAR, WHM```").queue();
        }
    }

    /**
     * Checks if a user belongs to a job and, if so, removes the job from their roles
     *
     * @param job
     * @param event
     */
    public static void removeJob(String job, MessageReceivedEvent event) {
        job = job.toUpperCase();
        if (!job.matches("[A-Z]{3}")) {
            event.getChannel().sendMessage("Please use the following format to remove a job:\n"
                    + "```" + Responder.getPrefix() + "removejob JOB```\n\n"
                    + "Valid job abbreviations:\n"
                    + "```AST, BLM, BRD, DRK, DRG, MCH, NIN, PLD, RDM, SAM, SCH, SMN, WAR, WHM```").queue();
            return;
        }

        try {
            Job selection = Job.valueOf(job);
            boolean hasJob = false;

            for (Role r : event.getMember().getRoles()) {
                if (r.getName().matches(selection.getName())) hasJob = true;
            }
            if (hasJob) {
                GuildController gc = new GuildController(event.getGuild());
                gc.removeRolesFromMember(
                        event.getMember(),
                        event.getGuild().getRolesByName(selection.getName(), true).get(0)
                ).queue();
                GeneralEmbedManager.sendRemoveJobEmbed(selection, event);
            } else {
                event.getTextChannel().sendMessage(event.getMember().getNickname()
                        + ", it doesn't look like you're a " + selection.getName() + ".")
                        .queue();
            }
        } catch (IllegalArgumentException ex) {
            event.getChannel().sendMessage("Please use the following format to remove a job:\n"
                    + "```" + Responder.getPrefix() + "removejob JOB```\n\n"
                    + "Valid job abbreviations:\n"
                    + "```AST, BLM, BRD, DRK, DRG, MCH, NIN, PLD, RDM, SAM, SCH, SMN, WAR, WHM```").queue();
        }
    }

    public static void getAllJobs(Member member, MessageChannel ch) {
        List<Role> roles = member.getRoles();
        if (roles.isEmpty()) {
            ch.sendMessage(member.getNickname() + " doesn't seem to have a job...\n"
                    + "```Use " + Responder.getPrefix() + "addjob to get a job.```");
        } else {
            GeneralEmbedManager.sendJobsEmbed(member, ch);
        }
    }
}
