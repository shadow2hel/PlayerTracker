package shadow2hel.playertracker;

import net.minecraft.entity.player.PlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shadow2hel.playertracker.utils.TimeSelector;
import shadow2hel.playertracker.utils.TimeUtils;

import java.sql.*;
import java.util.Calendar;
import java.util.Date;

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
                + "   current_year integer)";
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

    public boolean UpdateUser(PlayerEntity player, long playtime) {
        String uuid = player.getUniqueID().toString();
        String sql = "SELECT * FROM PLAYERACTIVITY WHERE uuid='" + uuid + "'";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            ResultSet rs = stmt.executeQuery(sql);
            if (!rs.next()) {
                PreparedStatement pstInsert = conn.prepareStatement(
                        "INSERT INTO PLAYERACTIVITY (uuid, playtime, playtime_all, playtime_week, playtime_month, playtime_year, current_week, current_month, current_year) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                );
                pstInsert.setString(1, uuid);
                pstInsert.setLong(2, playtime);
                pstInsert.setString(3, TimeUtils.getReadableTime(playtime));
                pstInsert.setString(4, TimeUtils.getReadableTime(playtime));
                pstInsert.setString(5, TimeUtils.getReadableTime(playtime));
                pstInsert.setString(6, TimeUtils.getReadableTime(playtime));

                pstInsert.setInt(7, cal.get(Calendar.WEEK_OF_YEAR));
                pstInsert.setInt(8, cal.get(Calendar.MONTH) + 1);
                pstInsert.setInt(9, cal.get(Calendar.YEAR));
                pstInsert.execute();
                LOGGER.info("Added user " + uuid);
            } else {
                do {
                    PlayerData oldply = new PlayerData(
                            rs.getString(1),
                            rs.getLong(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getString(5),
                            rs.getString(6),
                            rs.getInt(7),
                            rs.getInt(8),
                            rs.getInt(9)
                    );

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

                    LOGGER.info("Updated user " + player.getUniqueID().toString());

                } while (rs.next());
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }

        return true;
    }

    private long getNewTime(PlayerData oldData, PlayerEntity player, long playtime) {
        long newTimePlayedSeconds = playtime;

        // On world reset for example, when the statistics have been reset
        if (newTimePlayedSeconds < oldData.getPlaytime()) {
            newTimePlayedSeconds += oldData.getPlaytime();
        }

        return newTimePlayedSeconds;
    }

    private void updateUserData(Connection conn, long playtime, TimeSelector timeSelector, PlayerData oldData, PlayerEntity player, boolean addTime) throws SQLException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long newTimePlayedSeconds = getNewTime(oldData, player, playtime);
        // Upon reset of the week/month/year activity
        newTimePlayedSeconds = addTime ? newTimePlayedSeconds : newTimePlayedSeconds - oldData.getPlaytime();
        String sqlUpdate = "UPDATE PLAYERACTIVITY SET playtime = ?,\n"
                + "   playtime_all = ?,\n"
                + "   playtime_" + timeSelector.name().toLowerCase() + " = ?,\n"
                + "   current_week = ?,\n"
                + "   current_month = ?, \n"
                + "   current_year = ? "
                + " WHERE uuid = ?";
        PreparedStatement pstUpdate = conn.prepareStatement(sqlUpdate);
        pstUpdate.setLong(1, newTimePlayedSeconds);
        pstUpdate.setString(2, TimeUtils.getReadableTime(newTimePlayedSeconds));
        pstUpdate.setString(3, TimeUtils.getReadableTime(playtime));
        pstUpdate.setInt(4, cal.get(Calendar.WEEK_OF_YEAR));
        pstUpdate.setInt(5, cal.get(Calendar.MONTH) + 1);
        pstUpdate.setInt(6, cal.get(Calendar.YEAR));
        pstUpdate.setString(7, player.getUniqueID().toString());
        pstUpdate.executeUpdate();
    }
}
