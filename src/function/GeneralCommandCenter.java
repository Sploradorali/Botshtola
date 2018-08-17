package function;

import java.io.IOException;
import java.net.InetAddress;
import java.util.GregorianCalendar;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class GeneralCommandCenter {
	
	private GeneralCommandCenter() {
    }

    /**
     * Sends a direct message to the calling user
     * and deletes their help command request
     *
     * @param user
     * @param message
     */
    public static void help(User user, Message message) {
        user.openPrivateChannel().queue((channel) -> GeneralEmbedManager.sendHelpEmbed(channel));
        message.delete().queue();
    }

    /**
     * Pings a designated address from Y'shtola's host server
     *
     * @param addressName
     * @param event
     */
    public static void ping(String addressName, MessageReceivedEvent event) {
        InetAddress address;
        long start;
        long finish;
        String msg;

        try {
            if (addressName == null || addressName.trim().equals("")) {
                address = InetAddress.getByName("www.google.com");
            } else {
                address = InetAddress.getByName(addressName);
            }
            System.out.println("Pinging " + address.getHostName() + "...");
            start = new GregorianCalendar().getTimeInMillis();
            if (address.isReachable(5000)) {
                finish = new GregorianCalendar().getTimeInMillis();
                msg = "**Pong!** " + (finish - start) + "ms to " +
                        address.getHostName();
                event.getChannel().sendMessage(msg).queue();
            } else {
                msg = "Unable to reach " + address.getHostName();
                event.getChannel().sendMessage(msg).queue();
            }
        } catch (IOException e) {
            msg = "Invalid address";
            event.getChannel().sendMessage(msg).queue();
        }
    }

    /**
     * Rolls a Math.random()-generated number up to the integer value specified in the parameter
     *
     * @param parameter
     * @param event
     */
    public static void roll(String parameter, MessageReceivedEvent event) {
        int range = parameter.matches("[0-9]{2,1000}") ? Integer.parseInt(parameter) : 100;
        int rnd = (int) (Math.random() * (range)) + 1;
        event.getTextChannel().sendMessage(event.getMember().getNickname()
                + " rolled a ***" + rnd + "*** out of " + range + "!").queue();
    }
}
