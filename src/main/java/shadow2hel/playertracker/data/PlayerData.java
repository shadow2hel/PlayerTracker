package shadow2hel.playertracker.data;

import shadow2hel.playertracker.setup.Config;
import shadow2hel.playertracker.utils.TimeSelector;
import shadow2hel.playertracker.utils.TimeUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PlayerData implements Cloneable {
    private String uuid;
    private String username;
    private long playtime = 0;
    private MinecraftTime playtime_all = null;
    private MinecraftTime playtime_week = null;
    private MinecraftTime playtime_month = null;
    private MinecraftTime playtime_year = null;
    private int current_week = -1;
    private int current_month = -1;
    private int current_year = -1;
    private Date last_played = null;
    private int join_count = -1;
    private Calendar cal;

    private PlayerData(String uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone(Config.SERVER.timeZone.get()));
    }

    public String getUuid() {
        return uuid;
    }

    public long getPlaytime() {
        return playtime;
    }

    public MinecraftTime getPlaytime_all() {
        return playtime_all;
    }

    public MinecraftTime getPlaytime_week() {
        return playtime_week;
    }

    public MinecraftTime getPlaytime_month() {
        return playtime_month;
    }

    public MinecraftTime getPlaytime_year() {
        return playtime_year;
    }

    public int getCurrent_week() {
        return current_week;
    }

    public int getCurrent_month() {
        return current_month;
    }

    public int getCurrent_year() {
        return current_year;
    }

    public Date getLast_played() {
        return last_played;
    }

    public int getJoin_count() {
        return join_count;
    }

    public String getUsername() {
        return username;
    }

    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }

    public void setPlaytimeWithChoice(TimeSelector selector, MinecraftTime playtime) {
        Map<TimeSelector, Consumer<MinecraftTime>> setTime = new HashMap<>();
        setTime.put(TimeSelector.ALL, (minecraftTime) -> {
            this.playtime_all = minecraftTime;
            this.playtime = TimeUtils.getTotalTime(minecraftTime.toString());
        });
        setTime.put(TimeSelector.WEEK, (minecraftTime) -> this.playtime_week = playtime);
        setTime.put(TimeSelector.MONTH, (minecraftTime) -> this.playtime_month = playtime);
        setTime.put(TimeSelector.YEAR, (minecraftTime) -> this.playtime_year = playtime);

        setTime.get(selector).accept(playtime);
        if (selector == null)
            throw new IllegalArgumentException("TimeSelector doesn't exist!");
    }

    public void setPlaytime_all(MinecraftTime playtime_all) {
        this.playtime_all = playtime_all;
    }

    public void setPlaytime_week(MinecraftTime playtime_week) {
        this.playtime_week = playtime_week;
    }

    public void setPlaytime_month(MinecraftTime playtime_month) {
        this.playtime_month = playtime_month;
    }

    public void setPlaytime_year(MinecraftTime playtime_year) {
        this.playtime_year = playtime_year;
    }

    public void setCurrent_week(int current_week) {
        this.current_week = current_week;
    }

    public void setCurrent_month(int current_month) {
        this.current_month = current_month;
    }

    public void setCurrent_year(int current_year) {
        this.current_year = current_year;
    }

    public void setLast_played(Date last_played) {
        this.last_played = last_played;
    }

    public void setJoin_count(int join_count) {
        this.join_count = join_count;
    }

    public void updatePlaytime(long addPlaytime) {
        if (this.getCurrent_year() != cal.get(Calendar.YEAR)) {
            updatePlaytimeData(addPlaytime, TimeSelector.YEAR, false);
        } else if (this.getCurrent_year() == cal.get(Calendar.YEAR)) {
            updatePlaytimeData(addPlaytime, TimeSelector.YEAR, true);
        }

        if (this.getCurrent_month() != (cal.get(Calendar.MONTH) + 1)) {
            updatePlaytimeData(addPlaytime, TimeSelector.MONTH, false);
        } else if (this.getCurrent_month() == (cal.get(Calendar.MONTH) + 1)) {
            updatePlaytimeData(addPlaytime, TimeSelector.MONTH, true);
        }

        if (this.getCurrent_week() != (cal.get(Calendar.WEEK_OF_YEAR))) {
            updatePlaytimeData(addPlaytime, TimeSelector.WEEK, false);
        } else if (this.getCurrent_week() == (cal.get(Calendar.WEEK_OF_YEAR))) {
            updatePlaytimeData(addPlaytime, TimeSelector.WEEK, true);
        }
        updatePlaytimeData(addPlaytime, TimeSelector.ALL, true);
    }

    private void updatePlaytimeData(long addPlaytime, TimeSelector selector, boolean addTime) {
        Map<TimeSelector, Supplier<Long>> getSelectedTime = new HashMap<>();
        getSelectedTime.put(TimeSelector.ALL, this::getPlaytime);
        getSelectedTime.put(TimeSelector.WEEK, () -> {
            this.setCurrent_week(cal.get(Calendar.WEEK_OF_YEAR));
            return TimeUtils.getTotalTime(this.getPlaytime_week().toString());
        });
        getSelectedTime.put(TimeSelector.MONTH, () -> {
            this.setCurrent_month(cal.get(Calendar.MONTH) + 1);
            return TimeUtils.getTotalTime(this.getPlaytime_month().toString());
        });
        getSelectedTime.put(TimeSelector.YEAR, () -> {
            this.setCurrent_year(cal.get(Calendar.YEAR));
            return TimeUtils.getTotalTime(this.getPlaytime_year().toString());
        });
        long oldTotalPlaytime = 0;
        oldTotalPlaytime = getSelectedTime.get(selector).get();
        long newTimePlayedSeconds = addTime ? addPlaytime + oldTotalPlaytime : addPlaytime;
        this.setPlaytimeWithChoice(selector, TimeUtils.getReadableTime(newTimePlayedSeconds));
    }

    @Override
    public String toString() {
        return String.format("UUID:\t\t\t\t\t%s%n" +
                "Username:\t\t\t\t%s%n" +
                "Playtime:\t\t\t\t%d%n" +
                "Playtime_All:\t\t\t%s%n" +
                "Playtime_Week:\t\t\t%s%n" +
                "Playtime_Month:\t\t\t%s%n" +
                "Playtime_Year:\t\t\t%s%n" +
                "Current_week:\t\t\t%d%n" +
                "Current-month:\t\t\t%d%n" +
                "Current_year:\t\t\t%d%n" +
                "Last_played:\t\t\t%s%n" +
                "Join_count:\t\t\t\t%d%n",
                getUuid(), getUsername(), getPlaytime(),
                getPlaytime_all(), getPlaytime_week(), getPlaytime_month(),
                getPlaytime_year(), getCurrent_week(), getCurrent_month(),
                getCurrent_year(), getLast_played().toString(),
                getJoin_count());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof PlayerData))
            return false;
        PlayerData newPly = (PlayerData) obj;
        return this.getUuid().equals(newPly.getUuid())
                && this.getUsername().equals(newPly.getUsername())
                && this.getJoin_count() == newPly.getJoin_count()
                && this.getPlaytime_year().equals(newPly.getPlaytime_year())
                && this.getPlaytime_month().equals(newPly.getPlaytime_month())
                && this.getPlaytime_week().equals(newPly.getPlaytime_week())
                && this.getPlaytime_all().equals(newPly.getPlaytime_all())
                && this.getPlaytime() == newPly.getPlaytime();
    }

    @Override
    public PlayerData clone() {
        PlayerData playerData = null;
        try {
            playerData = (PlayerData) super.clone();
        } catch (CloneNotSupportedException e) {
            playerData = new Builder(this.getUuid(), this.getUsername())
                    .withPlaytime(this.playtime)
                    .withPlaytimeAll(this.playtime_all)
                    .withPlaytimeThisWeek(this.playtime_week)
                    .withPlaytimeThisMonth(this.playtime_month)
                    .withPlaytimeThisYear(this.playtime_year)
                    .inCurrentWeek(this.current_week)
                    .inCurrentMonth(this.current_month)
                    .inCurrentYear(this.current_year)
                    .withLastPlayed(this.last_played)
                    .withJoinCount(this.join_count)
                    .build();
        }
        return playerData;
    }

    public static class Builder {
        private String uuid;
        private String username;
        private long playtime = 0;
        private MinecraftTime playtime_all = null;
        private MinecraftTime playtime_week = null;
        private MinecraftTime playtime_month = null;
        private MinecraftTime playtime_year = null;
        private int current_week = -1;
        private int current_month = -1;
        private int current_year = -1;
        private Date last_played = null;
        private int join_count = -1;

        public Builder(String uuid, String username) {
            this.uuid = uuid;
            this.username = username;
        }

        public Builder withPlaytime(long seconds) {
            this.playtime = seconds;
            return this;
        }

        public Builder withPlaytimeAll(MinecraftTime time) {
            this.playtime_all = time;
            return this;
        }

        public Builder withPlaytimeThisWeek(MinecraftTime time) {
            this.playtime_week = time;
            return this;
        }

        public Builder withPlaytimeThisMonth(MinecraftTime time) {
            this.playtime_month = time;
            return this;
        }

        public Builder withPlaytimeThisYear(MinecraftTime time) {
            this.playtime_year = time;
            return this;
        }

        public Builder withPlaytimeTimeChoice(TimeSelector selector, MinecraftTime time) {
            if (selector == TimeSelector.YEAR)
                this.playtime_year = time;
            if (selector == TimeSelector.MONTH)
                this.playtime_month = time;
            if (selector == TimeSelector.WEEK)
                this.playtime_week = time;
            else if (selector == null)
                throw new IllegalArgumentException("TimeSelector doesn't exist!");
            return this;
        }

        public Builder inCurrentWeek(int weekOfYear) {
            this.current_week = weekOfYear;
            return this;
        }

        public Builder inCurrentMonth(int month) {
            this.current_month = month;
            return this;
        }

        public Builder inCurrentYear(int year) {
            this.current_year = year;
            return this;
        }

        public Builder withLastPlayed(Date time) {
            this.last_played = time;
            return this;
        }

        public Builder withJoinCount(int joinCount) {
            this.join_count = joinCount;
            return this;
        }

        public PlayerData build() {
            PlayerData playerInfo = new PlayerData(this.uuid, this.username);
            playerInfo.playtime = this.playtime;
            playerInfo.playtime_all = this.playtime_all;
            playerInfo.playtime_week = this.playtime_week;
            playerInfo.playtime_month = this.playtime_month;
            playerInfo.playtime_year = this.playtime_year;
            playerInfo.current_week = this.current_week;
            playerInfo.current_month = this.current_month;
            playerInfo.current_year = this.current_year;
            playerInfo.last_played = this.last_played;
            playerInfo.join_count = this.join_count;

            return playerInfo;
        }

    }
}
