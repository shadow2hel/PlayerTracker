package shadow2hel.playertracker.data;

import java.time.LocalDateTime;

public class PlayerData {
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
    private LocalDateTime last_played = null;
    private int join_count = -1;

    private PlayerData(String uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public String getUuid() {
        return uuid;
    }

    public long getPlaytime() {
        return playtime;
    }

    public String getPlaytime_all() {
        return playtime_all.toString();
    }

    public String getPlaytime_week() {
        return playtime_week.toString();
    }

    public String getPlaytime_month() {
        return playtime_month.toString();
    }

    public String getPlaytime_year() {
        return playtime_year.toString();
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

    public LocalDateTime getLast_played() {
        return last_played;
    }

    public int getJoin_count() {
        return join_count;
    }

    public String getUsername() {
        return username;
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
        private LocalDateTime last_played = null;
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

        public Builder withLastPlayed(LocalDateTime time) {
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
