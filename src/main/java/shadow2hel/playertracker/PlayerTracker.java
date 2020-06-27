package shadow2hel.playertracker;

import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import shadow2hel.playertracker.setup.ModSetup;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(PlayerTracker.MODID)
public class PlayerTracker {
    // Directly reference a log4j logger.
    public static final String MODID = "playertracker";
    public static final String PREFIX = "[PT]";

    public PlayerTracker() {
        // Register the setup method for modloading
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        // Register ourselves for server and other game events we are interested in
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSetup::init);
    }
}
