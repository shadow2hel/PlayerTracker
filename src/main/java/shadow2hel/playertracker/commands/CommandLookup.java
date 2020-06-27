package shadow2hel.playertracker.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shadow2hel.playertracker.DbManager;
import shadow2hel.playertracker.PlayerTracker;
import shadow2hel.playertracker.data.PlayerData;
import shadow2hel.playertracker.utils.StringUtils;

import java.util.List;

public class CommandLookup implements Command<CommandSource> {

    private static final CommandLookup CMD = new CommandLookup();
    private static final DbManager DB_MANAGER = DbManager.getInstance();
    private static final Logger LOGGER = LogManager.getLogger();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("lookup")
                .requires(cs -> cs.hasPermissionLevel(1))
                .then(
                        Commands.argument("name", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    DB_MANAGER.getAllPlayerData().forEach(p -> {
                                        if (StringUtils.startsWithIgnoreCase(p.getUsername(), builder.getRemaining())) {
                                            builder.suggest(p.getUsername());
                                        } else if (builder.getRemaining().isEmpty()) {
                                            builder.suggest(p.getUsername());
                                        }
                                    });
                                    return builder.buildFuture();
                                })
                                .executes(CMD)
                )
                .executes(c -> {
                    c.getSource().sendFeedback(new StringTextComponent("You haven't provided a name!"), false);
                    return 0;
                });
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String username = context.getArgument("name", String.class);
        List<PlayerData> allPlayers = DB_MANAGER.getAllPlayerData();
        PlayerData foundPlayer = allPlayers.stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
        if (foundPlayer == null) {
            SimpleCommandExceptionType exception = new SimpleCommandExceptionType(
                    new LiteralMessage(username + " doesn't exist!"));
            throw new CommandSyntaxException(exception, new LiteralMessage(PlayerTracker.PREFIX + " " + exception.toString()));
        }


        List<ITextComponent> text = new MessageBuilder()
                .addHeader(" PLAYER TRACKER ", '=', 6)
                .addText("Player: " + foundPlayer.getUsername())
                .addText("Total playtime: " + foundPlayer.getPlaytime_all())
                .addText("Weekly playtime: " + foundPlayer.getPlaytime_week())
                .addText("Monthly playtime: " + foundPlayer.getPlaytime_month())
                .addText("Yearly playtime: " + foundPlayer.getPlaytime_year())
                .addText("Last joined: " + foundPlayer.getLast_played())
                .addText("Times joined: " + foundPlayer.getJoin_count())
                .addFooter('~', 15)
                .build();
        text.forEach(t -> context.getSource().sendFeedback(t, false));
        return 0;
    }
}
