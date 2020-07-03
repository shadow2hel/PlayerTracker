package shadow2hel.playertracker.setup;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@Mod.EventBusSubscriber
public class Config {
    public static final ForgeConfigSpec SERVER_SPECS;
    public static final Server SERVER;

    static {
        Pair<Server, ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER_SPECS = serverPair.getRight();
        SERVER = serverPair.getLeft();
    }

    public static class Server {
        //General config
        public final ForgeConfigSpec.ConfigValue<String> timeZone;
        public final ForgeConfigSpec.BooleanValue addPlayersFromSave;
        public final ForgeConfigSpec.IntValue maxEntriesPerPage;

        //Encryption
        public final ForgeConfigSpec.BooleanValue encryption;
        public final ForgeConfigSpec.ConfigValue<String> encryptionPassword;

        //Misc.
        public final ForgeConfigSpec.BooleanValue debug;

        public Server(ForgeConfigSpec.Builder builder)
        {
            //General config
            builder.comment(" Config for general settings")
                    .push("General settings");

            timeZone = builder
                    .comment(" Choose the TimeZone to your liking. (Look at https://en.wikipedia.org/wiki/List_of_tz_database_time_zones for a list of choices)")
                    .define("timeZone", "Etc/UTC");

            addPlayersFromSave = builder
                    .comment(" If you enable this, Player Tracker will add players on startup based on the data found under your world/playerdata.\n DO NOT COPY DATA OVER FROM PLAYERS WHO HAVEN'T PLAYED ON THE SERVER!!")
                    .define("addPlayersFromSave", false);

            maxEntriesPerPage = builder
                    .comment(" How many entries per page in statistics you want.")
                    .defineInRange("maxEntriesPerPage", 10, 1, 1000);

            builder.pop();

            builder.comment(" Config for encryption settings")
                    .push("Encryption settings");

            encryption = builder
                    .comment(" Whether you'd like to use encryption in the database for the UUIDS or not.")
                    .define("encryption", true);

            encryptionPassword = builder
                    .comment(" Password used in the encryption.")
                    .define("encryptionPassword", "DEFAULTPASSWORD");

            //END Encryption settings
            builder.pop();

            //Misc. settings
            builder.comment(" Config for misc. settings")
                    .push("Misc settings");

            debug = builder
                    .comment(" Only used for developmental purposes.")
                    .define("debug", false);

            builder.pop();
            //END Misc. settings
        }
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        boolean found = false;
        for (String timezone : TimeZone.getAvailableIDs()) {
            found = Config.SERVER.timeZone.get().equals(timezone);
        }
        if (!found)
            Config.SERVER.timeZone.set("Etc/UTC");
    }
}
