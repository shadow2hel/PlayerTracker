package shadow2hel.playertracker.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import shadow2hel.playertracker.PlayerTracker;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> cmdPlayerTracker = dispatcher.register(
                Commands.literal(PlayerTracker.MODID)
                        .then(CommandLookup.register(dispatcher))
        );

        dispatcher.register(Commands.literal("pt").redirect(cmdPlayerTracker));
        dispatcher.register(Commands.literal("track").redirect(cmdPlayerTracker));
    }
}
