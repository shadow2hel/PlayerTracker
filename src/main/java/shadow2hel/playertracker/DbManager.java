package shadow2hel.playertracker;

import com.ibm.icu.impl.Grego;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.stats.ServerStatisticsManager;
import net.minecraft.stats.Stats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shadow2hel.playertracker.data.MinecraftTime;
import shadow2hel.playertracker.data.PlayerData;
import shadow2hel.playertracker.encryption.Encryption;
import shadow2hel.playertracker.setup.Config;
import shadow2hel.playertracker.utils.PlayerUtils;
import shadow2hel.playertracker.utils.TimeSelector;
import shadow2hel.playertracker.utils.TimeUtils;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.*;
import java.util.function.Supplier;

public class DbManager {
    private final String name = "playertracker.db";
    private static DbManager dbManager_instance = null;
    private static final Logger LOGGER = LogManager.getLogger();

    private DbManager() {
        initializeDatabase();
    }

    public static DbManager getInstance() {
        if (dbManager_instance == null)
            dbManager_instance = new DbManager();
        return dbManager_instance;
    }

    private void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS PLAYERACTIVITY(\n"
                + "   uuid nvarchar(40) PRIMARY KEY,\n"
                + "   playtime BIGINT,\n"
                + "   playtime_all nvarchar(10),\n"
                + "   playtime_week nvarchar(10),\n"
                + "   playtime_month nvarchar(10),\n"
                + "   playtime_year nvarchar(10),\n"
                + "   current_week integer,\n"
                + "   current_month integer, \n"
                + "   current_year integer, \n" +
                "last_played text, \n" +
                "join_count integer)";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private Connection connect() {
        final String url = "jdbc:sqlite:" + name;
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(url);
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error("CONNECTION IS ALL FUCKED UP!");
        }
        return conn;
    }

    private String getEncryptedUUID(String uuid) {
        String decryptedUUID;
        String sql = "SELECT * FROM PLAYERACTIVITY";
        try (Connection conn = this.connect()) {
            PreparedStatement pstSelect = conn.prepareStatement(sql);
            ResultSet rs = pstSelect.executeQuery();
            while (rs.next()) {
                decryptedUUID = Encryption.decrypt(rs.getString(1));
                if (uuid.equals(decryptedUUID))
                    uuid = rs.getString(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return uuid;
    }

    public PlayerData generatePlayerData(ResultSet rs) throws SQLException {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z Z");
        format.setTimeZone(TimeZone.getTimeZone(Config.SERVER.timeZone.get()));
        Date date = null;
        try {
            date = (rs.getString(10) != null) ? format.parse(rs.getString(10)) : null;
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        PlayerData playerData = new PlayerData(
//                rs.getString(1),
//                rs.getLong(2),
//                rs.getString(3),
//                rs.getString(4),
//                rs.getString(5),
//                rs.getString(6),
//                rs.getInt(7),
//                rs.getInt(8),
//                rs.getInt(9),
//                date,
//                rs.getInt(11)
//        );
        return new PlayerData.Builder(rs.getString(1), PlayerUtils.getPlayerUsername(rs.getString(1)))
                .withPlaytime(rs.getLong(2))
                .withPlaytimeAll(new MinecraftTime(rs.getString(3) != null ? rs.getString(3) : "0d0h0m"))
                .withPlaytimeThisWeek(new MinecraftTime(rs.getString(4) != null ? rs.getString(4) : "0d0h0m"))
                .withPlaytimeThisMonth(new MinecraftTime(rs.getString(5) != null ? rs.getString(5) : "0d0h0m"))
                .withPlaytimeThisYear(new MinecraftTime(rs.getString(6) != null ? rs.getString(6) : "0d0h0m"))
                .inCurrentWeek(rs.getInt(7))
                .inCurrentMonth(rs.getInt(8))
                .inCurrentYear(rs.getInt(9))
                .withLastPlayed(date)
                .withJoinCount(rs.getInt(11))
                .build();
    }

    public PlayerData getPlayerData(String uuid) {
        String encryptedUUID = getEncryptedUUID(uuid);
        String sql = "SELECT * FROM PLAYERACTIVITY WHERE uuid = ?";
        PlayerData playerData = null;

        try (Connection conn = this.connect()) {
            PreparedStatement pstSelect = conn.prepareStatement(sql);
            pstSelect.setString(1, encryptedUUID);
            ResultSet rs = pstSelect.executeQuery();
            if (rs.isBeforeFirst())
                playerData = generatePlayerData(rs);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return playerData;
    }

    public List<PlayerData> getAllPlayerData() {
        List<PlayerData> playerDataList = new LinkedList<>();
        String sqlSelect = "SELECT * FROM PLAYERACTIVITY";
        try (Connection conn = this.connect()) {
            PreparedStatement pstSelect = conn.prepareStatement(sqlSelect);
            ResultSet rs = pstSelect.executeQuery();
            while (rs.next()) {
                playerDataList.add(generatePlayerData(rs));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return playerDataList;
    }

    public List<PlayerData> getSortedPlayerDatas(List<PlayerData> playerDataList, Comparator<PlayerData> comp, boolean reversed) {
        if (reversed)
            playerDataList.sort(comp.reversed());
        else
            playerDataList.sort(comp);
        return playerDataList;
    }

    public boolean updateUserPlaytime(PlayerEntity player) {
        long unRealisticPlaytime = ((ServerPlayerEntity) player).getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_ONE_MINUTE));
        long playtime = TimeUtils.getRealTime(unRealisticPlaytime);
        String uuid = player.getUniqueID().toString();
        String sql = "SELECT * FROM PLAYERACTIVITY WHERE uuid='" + getEncryptedUUID(player.getUniqueID().toString()) + "'";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            Calendar cal = new GregorianCalendar();;
            cal.setTimeZone(TimeZone.getTimeZone(Config.SERVER.timeZone.get()));
            ResultSet rs = stmt.executeQuery(sql);
            if (!rs.next()) {
                PreparedStatement pstInsert = conn.prepareStatement(
                        "INSERT INTO PLAYERACTIVITY (uuid, playtime, playtime_all, playtime_week, playtime_month, playtime_year, current_week, current_month, current_year) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                );
                pstInsert.setString(1, Encryption.encrypt(uuid));
                pstInsert.setLong(2, playtime);
                pstInsert.setString(3, TimeUtils.getReadableTime(playtime).toString());
                pstInsert.setString(4, TimeUtils.getReadableTime(playtime).toString());
                pstInsert.setString(5, TimeUtils.getReadableTime(playtime).toString());
                pstInsert.setString(6, TimeUtils.getReadableTime(playtime).toString());

                pstInsert.setInt(7, cal.get(Calendar.WEEK_OF_YEAR));
                pstInsert.setInt(8, cal.get(Calendar.MONTH) + 1);
                pstInsert.setInt(9, cal.get(Calendar.YEAR));
                pstInsert.execute();
                if (Config.SERVER.debug.get())
                    LOGGER.info("Inserted user " + uuid);
            } else {
                do {
                    PlayerData oldply = generatePlayerData(rs);

                    if (oldply.getCurrent_year() != cal.get(Calendar.YEAR)) {
                        updateUserData(conn, playtime, TimeSelector.YEAR, oldply, player, false);
                    } else if (oldply.getCurrent_year() == cal.get(Calendar.YEAR)) {
                        updateUserData(conn, playtime, TimeSelector.YEAR, oldply, player, true);
                    }

                    if (oldply.getCurrent_month() != (cal.get(Calendar.MONTH) + 1)) {
                        updateUserData(conn, playtime, TimeSelector.MONTH, oldply, player, false);
                    } else if (oldply.getCurrent_month() == (cal.get(Calendar.MONTH) + 1)) {
                        updateUserData(conn, playtime, TimeSelector.MONTH, oldply, player, true);
                    }

                    if (oldply.getCurrent_week() != (cal.get(Calendar.WEEK_OF_YEAR))) {
                        updateUserData(conn, playtime, TimeSelector.WEEK, oldply, player, false);
                    } else if (oldply.getCurrent_week() == (cal.get(Calendar.WEEK_OF_YEAR))) {
                        updateUserData(conn, playtime, TimeSelector.WEEK, oldply, player, true);
                    }
                    updateUserData(conn, playtime, TimeSelector.ALL, oldply, player,true);
                    if (Config.SERVER.debug.get())
                        LOGGER.info("Updated user " + player.getUniqueID().toString());

                } while (rs.next());
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }

        return true;
    }

    private long getNewTime(PlayerData oldData, TimeSelector selector, long playtime) {
        long newTimePlayedSeconds = playtime;

        if (selector == TimeSelector.ALL) {
            // On world reset for example, when the statistics have been reset
            long total = oldData.getPlaytime();
            if (newTimePlayedSeconds < total) {
                newTimePlayedSeconds += total;
            }
        }

        if (selector == TimeSelector.WEEK) {
            long week = TimeUtils.getTotalTime(oldData.getPlaytime_week());
            if (newTimePlayedSeconds < week) {
                newTimePlayedSeconds += week;
            }
        }

        if (selector == TimeSelector.MONTH) {
            long month = TimeUtils.getTotalTime(oldData.getPlaytime_month());
            if (newTimePlayedSeconds < month) {
                newTimePlayedSeconds += month;
            }
        }

        if (selector == TimeSelector.YEAR) {
            long year = TimeUtils.getTotalTime(oldData.getPlaytime_year());
            if (newTimePlayedSeconds < year) {
                newTimePlayedSeconds += year;
            }
        }

        if (selector == null)
            throw new IllegalArgumentException("TimeSelector doesn't exist!");

        return newTimePlayedSeconds;
    }

    private void updateUserData(Connection conn, long playtime, TimeSelector timeSelector, PlayerData oldData, PlayerEntity player, boolean addTime) throws SQLException {
        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone(Config.SERVER.timeZone.get()));
        Map<TimeSelector, Supplier<Long>> getSelectedTime = new HashMap<>();
        getSelectedTime.put(TimeSelector.ALL, oldData::getPlaytime);
        getSelectedTime.put(TimeSelector.WEEK, () -> {
            oldData.setCurrent_week(cal.get(Calendar.WEEK_OF_YEAR));
            return TimeUtils.getTotalTime(oldData.getPlaytime_week());
        });
        getSelectedTime.put(TimeSelector.MONTH, () -> {
            oldData.setCurrent_month(cal.get(Calendar.MONTH) + 1);
            return TimeUtils.getTotalTime(oldData.getPlaytime_month());
        });
        getSelectedTime.put(TimeSelector.YEAR, () -> {
            oldData.setCurrent_year(cal.get(Calendar.YEAR));
            return TimeUtils.getTotalTime(oldData.getPlaytime_year());
        });
        Map<TimeSelector, Supplier<Integer>> getCurrentTime = new HashMap<TimeSelector, Supplier<Integer>>(){
            {
                put(TimeSelector.WEEK, oldData::getCurrent_week);
                put(TimeSelector.MONTH, oldData::getCurrent_month);
                put(TimeSelector.YEAR, oldData::getCurrent_year);
            }
        };
        long newTimePlayedSeconds = getNewTime(oldData, timeSelector, playtime);
        long timePlaytime = 0;
        timePlaytime = getSelectedTime.get(timeSelector).get();
        // Upon reset of the week/month/year activity
        newTimePlayedSeconds = addTime ? newTimePlayedSeconds : newTimePlayedSeconds - timePlaytime;
        oldData.setPlaytimeWithChoice(timeSelector, TimeUtils.getReadableTime(newTimePlayedSeconds));
        if (timeSelector != TimeSelector.ALL) {
            String sqlUpdate = "UPDATE PLAYERACTIVITY SET "
                    + " playtime_" + timeSelector.name().toLowerCase() + " = ?,\n"
                    + " current_" + timeSelector.name().toLowerCase() + " = ? "
                    + " WHERE uuid = ?";
            PreparedStatement pstUpdate = conn.prepareStatement(sqlUpdate);
            pstUpdate.setString(1, TimeUtils.getReadableTime(getSelectedTime.get(timeSelector).get()).toString());
            pstUpdate.setString(2, "" + getCurrentTime.get(timeSelector).get());
            pstUpdate.setString(3, getEncryptedUUID(player.getUniqueID().toString()));

            pstUpdate.executeUpdate();
        } else {
            String sqlUpdate = "UPDATE PLAYERACTIVITY SET "
                    + " playtime = ?,\n"
                    + " playtime_" + timeSelector.name().toLowerCase() + " = ? "
                    + " WHERE uuid = ?";
            PreparedStatement pstUpdate = conn.prepareStatement(sqlUpdate);
            pstUpdate.setString(1, "" + getSelectedTime.get(timeSelector).get());
            pstUpdate.setString(2, TimeUtils.getReadableTime(getSelectedTime.get(timeSelector).get()).toString());
            pstUpdate.setString(3, getEncryptedUUID(player.getUniqueID().toString()));

            pstUpdate.executeUpdate();
        }
    }

    public void updateUserStats(PlayerEntity player) {
        ServerStatisticsManager stats = ((ServerPlayerEntity) player).getStats();
        int joinCount = stats.getValue(Stats.CUSTOM.get(Stats.LEAVE_GAME)) + 1;
        try (Connection conn = this.connect()) {
            Calendar cal = new GregorianCalendar();
            cal.setTimeZone(TimeZone.getTimeZone(Config.SERVER.timeZone.get()));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z Z");
            formatter.setTimeZone(TimeZone.getTimeZone(Config.SERVER.timeZone.get()));
            String date = formatter.format(cal.getTime());

            String sqlSelect = "SELECT * FROM PLAYERACTIVITY WHERE uuid = ?";
            PreparedStatement pstSelect = conn.prepareStatement(sqlSelect);
            pstSelect.setString(1, getEncryptedUUID(player.getUniqueID().toString()));
            ResultSet rs = pstSelect.executeQuery();
            if (!rs.next()) {

                String sqlInsert = "INSERT INTO PLAYERACTIVITY (uuid, last_played, join_count) VALUES (?, ?, ?)";
                PreparedStatement pstInsert = conn.prepareStatement(sqlInsert);
                pstInsert.setString(1, Encryption.encrypt(player.getUniqueID().toString()));
                pstInsert.setString(2, date);
                pstInsert.setInt(3, joinCount);
                pstInsert.execute();
            } else {
                do {
                    String sqlUpdate = "UPDATE PLAYERACTIVITY SET last_played = ?, \n" +
                            " join_count = ? " +
                            " WHERE uuid = ?";
                    PreparedStatement pstUpdate = conn.prepareStatement(sqlUpdate);
                    pstUpdate.setString(1, date);
                    pstUpdate.setInt(2, joinCount);
                    pstUpdate.setString(3, getEncryptedUUID(player.getUniqueID().toString()));
                    pstUpdate.executeUpdate();
                } while (rs.next());
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void updateUser(PlayerEntity player) {
        updateUserStats(player);
        updateUserPlaytime(player);
    }
}
