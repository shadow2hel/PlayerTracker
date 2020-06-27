package shadow2hel.playertracker.encryption;

import org.jasypt.util.text.StrongTextEncryptor;

public class Encryption {
    private static final String PASSWORD = "SH@D0WZ1SG@Y";
    private static final boolean USEENCRYPTION = false;

    public static String encrypt(String unencrypted) {
        if (!USEENCRYPTION)
            return unencrypted;
        StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
        textEncryptor.setPassword(PASSWORD);

        return textEncryptor.encrypt(unencrypted);

    }

    public static String decrypt(String encrypted) {
        if (!USEENCRYPTION)
            return encrypted;
        StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
        textEncryptor.setPassword(PASSWORD);
        return textEncryptor.decrypt(encrypted);
    }
}