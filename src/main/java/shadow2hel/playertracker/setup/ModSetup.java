package shadow2hel.playertracker.setup;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import shadow2hel.playertracker.event.ForgeEventHandler;
import shadow2hel.playertracker.event.PlayerEventHandler;

public class ModSetup {
    public static void init(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
    }
}
