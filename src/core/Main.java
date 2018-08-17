package core;

import java.sql.SQLException;

import javax.security.auth.login.LoginException;

import database.DBInitialization;
import function.XIVServerStatus;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import scheduler.SchedulerCommandCenter;
import threads.Clock;

public class Main {
	public static JDA jda = null;
	public static Configuration globalConfig = null;

    /* TO-DO
     * - XIVDB classes command
     * - Work on general scalability, make everything extensible to other guilds
     *      - Configurable bot-only channel
     *      - Configurable welcome channel
     * - Split up help command by selectable category
     * -
     */

    /**
     * Requires config.txt file in src folder for full functionality
     *
     * @param args
     * @throws LoginException
     */
    public static void main(String[] args) throws LoginException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);

        try {
			setJDA(Configuration.getConfig(), builder);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

        /* Database access */
        /*
        try {
            //DBInitialization.getConnection();
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
        */

        boolean configLoaded = false;
/*
        while (!configLoaded) {
            if (jda != null) {
                for (Guild guild : jda.getGuilds()) {
                    Clock clock = new Clock(guild);
                    System.out.println("Starting clock threads");
                    clock.startClock();
                }
                configLoaded = true;
            } else {
                setJDA(Configuration.getConfig(), builder);
            }
        }
            */
    }

    private static void setJDA(Configuration config, JDABuilder builder) throws LoginException, InterruptedException {
        while (jda == null) {
            // Cancels start up if no token is specified
            if (Configuration.getConfig().getToken().isEmpty()) {
                System.out.println("Token not found");
                return;
            }

            jda = builder.setToken(Configuration.getConfig().getToken())
                    .addEventListener(new Responder())
                    .setGame(Game.of(Game.GameType.DEFAULT, XIVServerStatus.statusToString("Behemoth")))
                    .buildBlocking();
            System.out.println("0");
            }
        System.out.println(jda.getGuilds().size());
        for (Guild guild : jda.getGuilds()) {
            Clock clock = new Clock(guild);
            System.out.println("Starting clock threads");
            clock.startClock();
        }
    }
}
