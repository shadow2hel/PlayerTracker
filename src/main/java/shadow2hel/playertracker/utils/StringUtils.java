package shadow2hel.playertracker.utils;

import java.util.Random;

public class StringUtils {
    public static boolean startsWithIgnoreCase(String input, String prefix) {
        if (input.length() < prefix.length())
            return false;
        return input.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    public static String randomString(int length) {
        int leftBound = 48;
        int rightBound = 122;
        int rndStringLength = length;
        Random rnd = new Random();

        return rnd.ints(leftBound, rightBound)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(rndStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
