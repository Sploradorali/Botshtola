package threads;

import function.ResetMaintenanceTimer;
import function.XIVServerStatus;
import core.Configuration;
import core.Main;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import scheduler.Event;
import scheduler.SchedulerCommandCenter;

import java.util.concurrent.*;

/**
 * Handles time-based background threads that run throughout the runtime of the bot
 */
public class Clock {

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    static JDA jda = Main.jda;
    private Guild guild;

    public Clock(Guild guild) {
        this.guild = guild;
    }

    /**
     * Initiates time-based threads
     */
    public void startClock() {

    	System.out.println("Initializing clock threads.");
        executor.scheduleAtFixedRate(new EventTimeCheck(), 0, 1, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(new ServerStatusCheck(), 0, 1, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(new WeeklyResetCheck(), 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Calls the checkToNotify method on all events in the Guild
     */
    private class EventTimeCheck implements Runnable {

        @Override
        public void run() {
            for (Event event : Event.events) {
            	try {
	            	System.out.println("Checking event " + event.getEventId());
	                SchedulerCommandCenter.checkEvent(
	                        event,
	                        guild
	                );
            	} catch (Exception ex) {
            		System.out.println(ex.getMessage());
            	}
            }
        }
    }

    /**
     * Calls the notifyReset method
     */
    private class WeeklyResetCheck implements Runnable {

        @Override
        public void run() {
        	try {
	            ResetMaintenanceTimer.notifyReset(
	                    ResetMaintenanceTimer.findNextReset(),
	                    Clock.jda.getTextChannelsByName(Configuration.getConfig().getCheck_channel(),
	                            true).get(0)
	            );
        	} catch (Exception ex) {
        		System.out.println(ex.getMessage());
        	}
        }
    }

    /**
     * Calls the query method to check on server status in real time
     */
    private class ServerStatusCheck implements Runnable {
        @Override
        public void run() {
        	try {
	            XIVServerStatus.query(
	            		Configuration.getConfig().getServer(),
	                    Clock.jda.getTextChannelsByName(Configuration.getConfig().getCheck_channel(), true).get(0)
	            );
        	} catch (Exception ex) {
        		System.out.println(ex.getMessage());
        	}

        }
    }
}