package shadow2hel.playertracker.utils;

public class StringUtils {
    public static boolean startsWithIgnoreCase(String input, String prefix) {
        if (input.length() < prefix.length())
            return false;
        return input.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
