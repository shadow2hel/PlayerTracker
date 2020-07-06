package shadow2hel.playertracker.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import shadow2hel.playertracker.DbManager;
import shadow2hel.playertracker.commands.ModCommands;
import shadow2hel.playertracker.encryption.Encryption;
import shadow2hel.playertracker.setup.Config;
import shadow2hel.playertracker.utils.PlayerUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ForgeEventHandler {
    private DbManager dbManager;
    private Timer timer;

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        ModCommands.register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        dbManager = DbManager.getInstance();

        Encryption.setupEncryption();

        if (Config.SERVER.addPlayersFromSave.get()) {
            List<PlayerEntity> players = PlayerUtils.getAllPlayers();
            players.forEach(dbManager::updateUserPlaytime);
            players.forEach(Entity::remove);
        }

        dbManager.populateDatabaseWithFakes(20);
        dbManager.resetUsersPlaytime();
        TimerTask task = new TimerTask(){
            @Override
            public void run() {
                dbManager.resetUsersPlaytime();
            }
        };
        timer = new Timer("UserPlaytimeReset");
        timer.scheduleAtFixedRate(task, 1000L, 1000L * 3600L * 6L);
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        for (PlayerEntity player : event.getServer().getPlayerList().getPlayers()) {
            dbManager.updateUserPlaytime(player);
        }
        timer.cancel();
    }
}
