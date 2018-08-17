package function;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import core.Main;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.MessageChannel;

public class XIVServerStatus {
	
	//http://frontier.ffxiv.com/worldStatus/current_status.json?

    /**
     * Opens a connection with the Final Fantasy XIV server status JSON database
     *
     * @param serverName
     * @return
     * @throws IOException
     */
    public static int getWorldStatus(String serverName) throws IOException {
        URL url = new URL("http://frontier.ffxiv.com/worldStatus/current_status.json?");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) "
                + "AppleWebKit/537.11 (KHTML, like Gecko) "
                + "Chrome/23.0.1271.95 Safari/537.11");
        try (InputStream in = con.getInputStream()) {
            JSONTokener tokener = new JSONTokener(in);
            JSONObject object = new JSONObject(tokener);
            return object.getInt(serverName);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Requests the status of the provided World server and sends an Embed
     *
     * @param serverName
     * @param ch
     */
    public static void requestStatus(String serverName, MessageChannel ch) {
        serverName = serverName.substring(0, 1).toUpperCase() + serverName.substring(1);
        int flag = -1;
        try {
            flag = getWorldStatus(serverName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        GeneralEmbedManager.sendStatusEmbed(ch, serverName, flag);
    }

    /**
     * Supplies current server status as a string
     *
     * @param serverName
     * @return
     */
    public static String statusToString(String serverName) {
        String status;
        int flag = 0;
        try {
            flag = getWorldStatus(serverName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (flag == 1) {
            status = serverName + " is ONLINE";
        } else {
            status = serverName + " is OFFLINE";
        }
        return status;
    }

    /**
     * Continuously queries the Final Fantasy XIV server status JSON database to check if the provided
     * server is online.
     *
     * @param serverName
     * @param ch
     */
    public static void query(String serverName, MessageChannel ch) {
        AtomicBoolean online = new AtomicBoolean(true);
        AtomicBoolean previouslyOnline = new AtomicBoolean(true);

        int status = 1;
        try {
            status = getWorldStatus(serverName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (status != 1) {
            online.set(false);
        } else {
            online.set(true);
        }


        if (online.get() != previouslyOnline.get()) {
            ch.sendMessage(online.get() ?
                    serverName + " is now ONLINE!" :
                    serverName + " has just gone OFFLINE!")
                    .queue();
            Main.jda.getPresence().setGame(Game.of(Game.GameType.DEFAULT, statusToString("Behemoth")));
        }

        previouslyOnline.set(online.get());
    }
}
