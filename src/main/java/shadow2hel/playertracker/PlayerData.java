package shadow2hel.playertracker;

public class PlayerData {
    private String uuid;
    private long playtime;
    private String playtime_all;
    private String playtime_week;
    private String playtime_month;
    private String playtime_year;
    private int current_week;
    private int current_month;
    private int current_year;

    public PlayerData(String uuid, long playtime, String playtime_all, String playtime_week, String playtime_month, String playtime_year, int current_week, int current_month, int current_year) {
        this.uuid = uuid;
        this.playtime = playtime;
        this.playtime_all = playtime_all;
        this.playtime_week = playtime_week;
        this.playtime_month = playtime_month;
        this.playtime_year = playtime_year;
        this.current_week = current_week;
        this.current_month = current_month;
        this.current_year = current_year;
    }

    public String getUuid() {
        return uuid;
    }

    public long getPlaytime() {
        return playtime;
    }

    public String getPlaytime_all() {
        return playtime_all;
    }

    public String getPlaytime_week() {
        return playtime_week;
    }

    public String getPlaytime_month() {
        return playtime_month;
    }

    public String getPlaytime_year() {
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
}
