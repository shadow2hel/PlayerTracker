package shadow2hel.playertracker;

import com.sun.media.jfxmediaimpl.HostUtils;
import org.junit.Before;
import org.junit.Test;
import shadow2hel.playertracker.data.MinecraftTime;
import shadow2hel.playertracker.data.PlayerData;
import shadow2hel.playertracker.setup.Config;
import shadow2hel.playertracker.utils.TimeSelector;
import shadow2hel.playertracker.utils.TimeUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

public class DbManagerTest {
    private PlayerData data;
    private Calendar cal;

    @Before
    public void init() {
        cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone(Config.SERVER.timeZone.get()));
        data = new PlayerData.Builder("AABBCCDDEEFF", "Test123")
                .withJoinCount(10)
                .withPlaytime(1800300)
                .withPlaytimeAll(new MinecraftTime(20, 20, 5))
                .withPlaytimeThisMonth(new MinecraftTime(2, 22, 10))
                .withPlaytimeThisWeek(new MinecraftTime(1, 10, 40))
                .withPlaytimeThisYear(new MinecraftTime(10, 3, 45))
                .inCurrentYear(cal.get(Calendar.YEAR))
                .inCurrentWeek(cal.get(Calendar.WEEK_OF_YEAR))
                .inCurrentMonth(cal.get(Calendar.MONTH) + 1)
                .withLastPlayed(new Date())
                .build();
    }

    @Test
    public void testIfWeekGetsUpdatedCorrectly(){
        PlayerData newData = data.clone();
        long playtime = 86400; // 1 day
        newData.setCurrent_week(newData.getCurrent_week() + 1);
        newData = checkData(newData, playtime);
        assertNotEquals(newData.getPlaytime_week(), data.getPlaytime_week());
        assertEquals("TIME OF WEEK WAS NOT SUPPOSED TO BE ADDED!", playtime, TimeUtils.getTotalTime(newData.getPlaytime_week()));
        assertEquals("TIME OF MONTH WAS SUPPOSED TO BE ADDED!", TimeUtils.getTotalTime(data.getPlaytime_month()) + playtime, TimeUtils.getTotalTime(newData.getPlaytime_month()));
        assertEquals("TIME OF YEAR WAS SUPPOSED TO BE ADDED!", TimeUtils.getTotalTime(data.getPlaytime_year()) + playtime, TimeUtils.getTotalTime(newData.getPlaytime_year()));
    }

    @Test
    public void testIfMonthGetsUpdatedCorrectly(){
        PlayerData newData = data.clone();
        long playtime = 86400;
        newData.setCurrent_month(newData.getCurrent_month() + 1);
        newData = checkData(newData, playtime);
        assertNotEquals(newData.getPlaytime_month(), data.getPlaytime_month());
        assertEquals("TIME OF WEEK WAS SUPPOSED TO BE ADDED!", TimeUtils.getTotalTime(data.getPlaytime_week()) + playtime, TimeUtils.getTotalTime(newData.getPlaytime_week()));
        assertEquals("TIME OF MONTH WAS NOT SUPPOSED TO BE ADDED!", playtime, TimeUtils.getTotalTime(newData.getPlaytime_month()));
        assertEquals("TIME OF YEAR WAS SUPPOSED TO BE ADDED!", TimeUtils.getTotalTime(data.getPlaytime_week()) + playtime, TimeUtils.getTotalTime(newData.getPlaytime_week()));
    }

    @Test
    public void testIfYearGetsUpdatedCorrectly(){
        PlayerData newData = data.clone();
        long playtime = 86400;
        newData.setCurrent_year(newData.getCurrent_year() + 1);
        newData = checkData(newData, playtime);
        assertNotEquals(newData.getPlaytime_year(), data.getPlaytime_year());
        assertEquals("TIME OF WEEK WAS SUPPOSED TO BE ADDED!", TimeUtils.getTotalTime(data.getPlaytime_week()) + playtime, TimeUtils.getTotalTime(newData.getPlaytime_week()));
        assertEquals("TIME OF MONTH WAS SUPPOSED TO BE ADDED!", TimeUtils.getTotalTime(data.getPlaytime_month()) + playtime, TimeUtils.getTotalTime(newData.getPlaytime_month()));
        assertEquals("TIME OF YEAR WAS NOT SUPPOSED TO BE ADDED!", playtime, TimeUtils.getTotalTime(newData.getPlaytime_year()));
    }

    private PlayerData checkData(PlayerData newData, long playtime) {
        if (newData.getCurrent_year() != cal.get(Calendar.YEAR)) {
            updateData(playtime, TimeSelector.YEAR, newData, false);
        } else if (newData.getCurrent_year() == cal.get(Calendar.YEAR)) {
            updateData(playtime, TimeSelector.YEAR, newData, true);
        }

        if (newData.getCurrent_month() != (cal.get(Calendar.MONTH) + 1)) {
            updateData(playtime, TimeSelector.MONTH, newData, false);
        } else if (newData.getCurrent_month() == (cal.get(Calendar.MONTH) + 1)) {
            updateData(playtime, TimeSelector.MONTH, newData, true);
        }

        if (newData.getCurrent_week() != (cal.get(Calendar.WEEK_OF_YEAR))) {
            updateData(playtime, TimeSelector.WEEK, newData, false);
        } else if (newData.getCurrent_week() == (cal.get(Calendar.WEEK_OF_YEAR))) {
            updateData(playtime, TimeSelector.WEEK, newData, true);
        }
        updateData(playtime, TimeSelector.ALL, newData,true);

        return newData;
    }

    private void updateData(long playtime, TimeSelector timeSelector, PlayerData newData, boolean addTime) {
        Map<TimeSelector, Supplier<Long>> getSelectedTime = new HashMap<>();
        getSelectedTime.put(TimeSelector.ALL, newData::getPlaytime);
        getSelectedTime.put(TimeSelector.WEEK, () -> {
            newData.setCurrent_week(cal.get(Calendar.WEEK_OF_YEAR));
            return TimeUtils.getTotalTime(newData.getPlaytime_week());
        });
        getSelectedTime.put(TimeSelector.MONTH, () -> {
            newData.setCurrent_month(cal.get(Calendar.MONTH) + 1);
            return TimeUtils.getTotalTime(newData.getPlaytime_month());
        });
        getSelectedTime.put(TimeSelector.YEAR, () -> {
            newData.setCurrent_year(cal.get(Calendar.YEAR));
            return TimeUtils.getTotalTime(newData.getPlaytime_year());
        });
        long newTimePlayedSeconds = getNewTime(newData, timeSelector, playtime);
        long timePlaytime = 0;
        timePlaytime = getSelectedTime.get(timeSelector).get();
        // Upon reset of the week/month/year activity
        newTimePlayedSeconds = addTime ? newTimePlayedSeconds : newTimePlayedSeconds - timePlaytime;
        newData.setPlaytimeWithChoice(timeSelector, TimeUtils.getReadableTime(newTimePlayedSeconds));
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

}
