package shadow2hel.playertracker.encryption;

import org.jasypt.util.text.StrongTextEncryptor;
import shadow2hel.playertracker.setup.Config;
import shadow2hel.playertracker.utils.StringUtils;

public class Encryption {

    public static void setupEncryption() {
        if (Config.SERVER.encryption.get() && Config.SERVER.encryptionPassword.get().equals("DEFAULTPASSWORD")) {
            String rndPass = StringUtils.randomString(20);
            Config.SERVER.encryptionPassword.set(rndPass);
        }
    }

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