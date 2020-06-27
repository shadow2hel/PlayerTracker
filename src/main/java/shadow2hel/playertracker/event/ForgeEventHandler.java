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
import shadow2hel.playertracker.setup.Config;
import shadow2hel.playertracker.utils.PlayerUtils;

import java.util.List;


public class ForgeEventHandler {
    private DbManager dbManager;

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        ModCommands.register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        dbManager = DbManager.getInstance();

        if (Config.SERVER.addPlayersFromSave.get()) {
            List<PlayerEntity> players = PlayerUtils.getAllPlayers();
            players.forEach(dbManager::updateUserPlaytime);
            players.forEach(Entity::remove);
        }
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        for (PlayerEntity player : event.getServer().getPlayerList().getPlayers()) {
            dbManager.updateUserPlaytime(player);
        }
    }
}
