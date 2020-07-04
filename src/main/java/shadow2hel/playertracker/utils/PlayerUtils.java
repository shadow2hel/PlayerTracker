package shadow2hel.playertracker.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlayerUtils {
    private static final Logger LOGGER = LogManager.getLogger();

    private PlayerUtils() {
    }

    public static List<PlayerEntity> getAllPlayers() {
        List<PlayerEntity> players = new ArrayList<>();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        File playerFolder = server.getWorld(DimensionType.OVERWORLD).getSaveHandler().getPlayerFolder();
        List<String> uuids = new ArrayList<>();
        for (File file : Objects.requireNonNull(playerFolder.listFiles())) {
            if (file.getName().endsWith(".dat"))
                uuids.add(file.getName().substring(0, file.getName().length() - 4));
        }
        uuids.forEach(p -> players.add(new FakePlayer(server.getWorld(DimensionType.OVERWORLD), Objects.requireNonNull(server.getPlayerProfileCache().getProfileByUUID(UUID.fromString(p))))));
        return players;
    }

    public static PlayerEntity getPlayer(String username) {
        List<PlayerEntity> players = new ArrayList<>();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        PlayerEntity onlinePlayer = server.getPlayerList().getPlayerByUsername(username);
        if (onlinePlayer != null)
            return onlinePlayer;
        File playerFolder = server.getWorld(DimensionType.OVERWORLD).getSaveHandler().getPlayerFolder();
        GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(username);
        if (profile == null)
            return null;
        return new FakePlayer(server.getWorld(DimensionType.OVERWORLD), profile);
    }

    public static String getPlayerUsername(String uuid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        PlayerEntity onlinePlayer = server.getPlayerList().getPlayerByUUID(UUID.fromString(uuid));
        if (onlinePlayer != null)
            return onlinePlayer.getName().getString();
        File playerFolder = server.getWorld(DimensionType.OVERWORLD).getSaveHandler().getPlayerFolder();
        GameProfile profile = server.getPlayerProfileCache().getProfileByUUID(UUID.fromString(uuid));
        if (profile == null)
            return null;
        return profile.getName();
    }
}
