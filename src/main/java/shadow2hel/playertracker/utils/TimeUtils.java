package shadow2hel.playertracker.utils;

import shadow2hel.playertracker.data.MinecraftTime;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {
    private static final int SECONDSINDAY = 86400;
    private static final int SECONDSINHOUR = 3600;
    public static final TimeZone TIMEZONE = TimeZone.getTimeZone("Etc/UTC");

    private TimeUtils() {

    }

    public static long getTotalTime(String time) {
        Pattern pattern = Pattern.compile("(\\d+)d(\\d{1,2})h(\\d{1,2})m");
        Matcher matcher = pattern.matcher(time);
        int days = 0, hours = 0, minutes = 0;

        if(matcher.matches()) {
            days = Integer.parseInt(matcher.group(1));
            hours = Integer.parseInt(matcher.group(2));
            minutes = Integer.parseInt(matcher.group(3));
        }

        return days * SECONDSINDAY + hours * SECONDSINHOUR + minutes * 60;
    }

    // The time gotten by using the stats is calculated based on ticks, so because there's
    // 20 ticks per second we need to do this calculation to get an accurate time.
    public static long getRealTime(long time) {
        return time / 20;
    }

    public static MinecraftTime getReadableTime(long time) {

        return new MinecraftTime(getDays(time), getHours(time), getMinutes(time));
    }

    public static int getDays(long time) {
        return (int) Math.floor(time / SECONDSINDAY);
    }

    public static int getHours(long time) {
        return (int) Math.floor((time % SECONDSINDAY) / SECONDSINHOUR);
    }

    public static int getMinutes(long time) {
        return (int) Math.floor(((time % SECONDSINDAY) % SECONDSINHOUR) / 60);
    }
}
