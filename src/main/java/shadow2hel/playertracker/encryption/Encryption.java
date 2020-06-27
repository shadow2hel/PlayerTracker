package shadow2hel.playertracker.encryption;

import org.jasypt.util.text.StrongTextEncryptor;
import shadow2hel.playertracker.setup.Config;

public class Encryption {

    public static String encrypt(String unencrypted) {
        if (!Config.SERVER.encryption.get())
            return unencrypted;
        StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
        textEncryptor.setPassword(Config.SERVER.encryptionPassword.get());

        return textEncryptor.encrypt(unencrypted);

    }

    public static String decrypt(String encrypted) {
        if (!Config.SERVER.encryption.get())
            return encrypted;
        StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
        textEncryptor.setPassword(Config.SERVER.encryptionPassword.get());
        return textEncryptor.decrypt(encrypted);
    }
}