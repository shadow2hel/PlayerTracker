package shadow2hel.playertracker.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinecraftTime implements Comparable<MinecraftTime> {
    private int days;
    private int hours;
    private int minutes;

    public MinecraftTime(int days, int hours, int minutes) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
    }

    public MinecraftTime(String time) {
        Pattern pattern = Pattern.compile("(\\d+)d(\\d{1,2})h(\\d{1,2})m");
        Matcher matcher = pattern.matcher(time);
        if (matcher.matches()) {
            this.days = Integer.parseInt(matcher.group(1));
            this.hours = Integer.parseInt(matcher.group(2));
            this.minutes = Integer.parseInt(matcher.group(3));
        } else {
            throw new IllegalStateException("No match found");
        }
    }

    public int getDays() {
        return days;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    @Override
    public int compareTo(MinecraftTime o) {
        int diffDays = this.getDays() - o.getDays();
        if (diffDays != 0)
            return diffDays;
        int diffHours = this.getHours() - o.getHours();
        if (diffHours != 0)
            return diffHours;
        return this.getMinutes() - o.getMinutes();
    }

    @Override
    public String toString() {
        return String.format("%dd%dh%dm", getDays(), getHours(), getMinutes());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof MinecraftTime))
            return false;
        MinecraftTime time = (MinecraftTime) obj;
        return this.getDays() == time.getDays()
                && this.getHours() == time.getHours()
                && this.getMinutes() == time.getMinutes();
    }
}
