package shadow2hel.playertracker.utils;

public class TimeUtils {
    private static final double SECONDSINDAY = 86400;
    private static final double SECONDSINHOUR = 3600;

    private TimeUtils() {

    }

    public static long getTotalTime(String time) {
        long totalTime = 0;
        String timeNumbers = time.substring(0, time.length() - 3);
        double roughTime = Double.parseDouble(timeNumbers);
        if (time.charAt(time.length() - 1) == 'd') {
            totalTime = (long) Math.floor(roughTime * SECONDSINDAY);
        } else if (time.charAt(time.length() - 1) == 'h') {
            totalTime = (long) Math.floor(roughTime * SECONDSINHOUR);
        } else if (time.charAt(time.length() - 1) == 'm') {
            totalTime = (long) Math.floor(roughTime * 60);
        }
        return totalTime;
    }

    // The time gotten by using the stats is calculated based on ticks, so because there's
    // 20 ticks per second we need to do this calculation to get an accurate time.
    public static long getRealTime(long time) {
        return time / 20;
    }

    public static String getReadableTime(long time) {
        int amountOfDays = getDays(time);
        int amountOfHours = getHours(time);
        int amountOfMinutes = getMinutes(time);

        return String.format("%dd%dh%dm", amountOfDays, amountOfHours, amountOfMinutes);
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
