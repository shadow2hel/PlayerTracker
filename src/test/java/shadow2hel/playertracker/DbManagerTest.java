package shadow2hel.playertracker;

import org.junit.Before;
import org.junit.Test;
import shadow2hel.playertracker.data.MinecraftTime;
import shadow2hel.playertracker.data.PlayerData;
import shadow2hel.playertracker.setup.Config;
import shadow2hel.playertracker.utils.TimeSelector;
import shadow2hel.playertracker.utils.TimeUtils;

import java.util.*;
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
        newData.updatePlaytime(playtime);
        assertNotEquals(newData.getPlaytime_week(), data.getPlaytime_week());
        assertEquals("TIME OF WEEK WAS NOT SUPPOSED TO BE ADDED!", playtime, TimeUtils.getTotalTime(newData.getPlaytime_week().toString()));
        assertEquals("TIME OF MONTH WAS SUPPOSED TO BE ADDED!", TimeUtils.getTotalTime(data.getPlaytime_month().toString()) + playtime, TimeUtils.getTotalTime(newData.getPlaytime_month().toString()));
        assertEquals("TIME OF YEAR WAS SUPPOSED TO BE ADDED!", TimeUtils.getTotalTime(data.getPlaytime_year().toString()) + playtime, TimeUtils.getTotalTime(newData.getPlaytime_year().toString()));
        long anHour = 3600;
        newData.updatePlaytime(anHour);
        assertEquals("TIME OF WEEK WAS SUPPOSED TO BE ADDED!", playtime + anHour, TimeUtils.getTotalTime(newData.getPlaytime_week().toString()));
        assertEquals("TIME OF MONTH WAS SUPPOSED TO BE ADDED!", TimeUtils.getTotalTime(data.getPlaytime_month().toString()) + playtime + anHour, TimeUtils.getTotalTime(newData.getPlaytime_month().toString()));
        assertEquals("TIME OF YEAR WAS SUPPOSED TO BE ADDED!", TimeUtils.getTotalTime(data.getPlaytime_year().toString()) + playtime + anHour, TimeUtils.getTotalTime(newData.getPlaytime_year().toString()));
    }

    @Test
    public void testIfMonthGetsUpdatedCorrectly(){
        PlayerData newData = data.clone();
        long playtime = 86400;
        newData.setCurrent_month(newData.getCurrent_month() + 1);
        newData.updatePlaytime(playtime);
        assertNotEquals(newData.getPlaytime_month(), data.getPlaytime_month());
        assertEquals("TIME OF WEEK WAS SUPPOSED TO BE ADDED!", TimeUtils.getTotalTime(data.getPlaytime_week().toString()) + playtime, TimeUtils.getTotalTime(newData.getPlaytime_week().toString()));
        assertEquals("TIME OF MONTH WAS NOT SUPPOSED TO BE ADDED!", playtime, TimeUtils.getTotalTime(newData.getPlaytime_month().toString()));
        assertEquals("TIME OF YEAR WAS SUPPOSED TO BE ADDED!", TimeUtils.getTotalTime(data.getPlaytime_week().toString()) + playtime, TimeUtils.getTotalTime(newData.getPlaytime_week().toString()));
    }

    @Test
    public void testIfYearGetsUpdatedCorrectly(){
        PlayerData newData = data.clone();
        long playtime = 86400;
        newData.setCurrent_year(newData.getCurrent_year() + 1);
        newData.updatePlaytime(86400);
        assertNotEquals(newData.getPlaytime_year(), data.getPlaytime_year());
        assertEquals("TIME OF WEEK WAS SUPPOSED TO BE ADDED!", TimeUtils.getTotalTime(data.getPlaytime_week().toString()) + playtime, TimeUtils.getTotalTime(newData.getPlaytime_week().toString()));
        assertEquals("TIME OF MONTH WAS SUPPOSED TO BE ADDED!", TimeUtils.getTotalTime(data.getPlaytime_month().toString()) + playtime, TimeUtils.getTotalTime(newData.getPlaytime_month().toString()));
        assertEquals("TIME OF YEAR WAS NOT SUPPOSED TO BE ADDED!", playtime, TimeUtils.getTotalTime(newData.getPlaytime_year().toString()));
    }


}
