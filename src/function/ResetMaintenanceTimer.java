package function;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import net.dv8tion.jda.core.entities.MessageChannel;

public class ResetMaintenanceTimer {

	/**
     * Calculates time left to weekly reset and calls for an Embed
     *
     * @param time
     * @param ch
     */
    public static void timeTo(Instant time, MessageChannel ch) {
        long epochMilli = time.toEpochMilli() - Instant.now().toEpochMilli();
        long days = epochMilli / 86400000;
        long hours = (epochMilli % 86400000) / 3600000;
        long minutes = (epochMilli % 3600000) / 60000;
        long seconds = (epochMilli % 60000) / 1000;

        String daysOutput = String.format("%02d", days);
        String hoursOutput = String.format("%02d", hours);
        String minutesOutput = String.format("%02d", minutes);
        String secondsOutput = String.format("%02d", seconds);

        GeneralEmbedManager.sendTimeToResetEmbed(daysOutput, hoursOutput, minutesOutput, secondsOutput, ch);
    }

    /**
     * Checks for the next weekly reset on FFXIV (typically Tuesday at 1:00am every week)
     *
     * @return
     */
    public static Instant findNextReset() {

        ZonedDateTime current = ZonedDateTime.now(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("America/Los_Angeles"));

        ZonedDateTime one = LocalDateTime.of(
                LocalDate.now(),
                LocalTime.of(1, 0)).atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("UTC"));

        if (current.getDayOfWeek() == DayOfWeek.TUESDAY &&
                current.isAfter(one)) {
            current = current.plusDays(1);
        }

        while (current.getDayOfWeek() != DayOfWeek.TUESDAY) {
            current = current.plusDays(1);
        }

        ZonedDateTime result = LocalDateTime.of(
                current.getYear(), current.getMonth(), current.getDayOfMonth(), 1, 0
        ).atZone(ZoneId.of("America/Los_Angeles"));

        return result.toInstant();
    }

    /**
     * Sends a notification one day and one hour before weekly reset occurs
     *
     * @param instant
     * @param ch
     */
    public static void notifyReset(Instant instant, MessageChannel ch) {
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.of("America/Los_Angeles"));
        LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Los_Angeles")).withNano(0);

        if (now.isEqual(ldt.plusDays(1))) {
            ch.sendMessage("Reminder! Weekly reset is tomorrow night!").queue();
        }

        if (now.isEqual(ldt.plusHours(1))) {
            ch.sendMessage("Reminder! Weekly reset is in an hour!").queue();
        }
        
    }
}
