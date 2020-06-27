package shadow2hel.playertracker.event;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shadow2hel.playertracker.DbManager;
import shadow2hel.playertracker.data.PlayerData;
import shadow2hel.playertracker.utils.TimeUtils;

import java.util.Comparator;
import java.util.List;

public class PlayerEventHandler {
    private final DbManager dbManager = DbManager.getInstance();
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        dbManager.updateUser(event.getPlayer());
        List<PlayerData> playerDataList = dbManager.getSortedPlayerDatas(dbManager.getAllPlayerData(), Comparator.comparing(PlayerData::getPlaytime_week), true);
        playerDataList.forEach(p -> LOGGER.info(p.getPlaytime_week()));
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        long unRealisticPlaytime = ((ServerPlayerEntity) event.getPlayer()).getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_ONE_MINUTE));
        long playtime = TimeUtils.getRealTime(unRealisticPlaytime);
        dbManager.updateUserPlaytime(event.getPlayer());
    }

//    @SubscribeEvent
//    public void onBlockHarvest(BlockEvent.BreakEvent event) {
//        int silklevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, event.getPlayer().getHeldItemMainhand());
//        BlockPos pos = event.getPos();
//        String blockName = event.getState().getBlock().getTranslationKey();
//        PlayerInteractionManager plyManager = ((ServerPlayerEntity)event.getPlayer()).interactionManager;
//        if (silklevel == 0 && blockName.equals("block.minecraft.diamond_ore") && plyManager.getGameType() == GameType.SURVIVAL )
//            LOGGER.info("Added a diamond count!");
//        LOGGER.info("Trying to break here!");
//        LOGGER.info(event.getState().getBlock().getTranslationKey());
//    }
}
