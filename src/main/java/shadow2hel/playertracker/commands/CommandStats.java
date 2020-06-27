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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shadow2hel.playertracker.DbManager;
import shadow2hel.playertracker.data.PlayerData;
import shadow2hel.playertracker.utils.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class CommandStats implements Command<CommandSource> {

    private static final CommandStats CMD = new CommandStats();
    private static final DbManager DB_MANAGER = DbManager.getInstance();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, Function<CommandContext<CommandSource>, Integer>> SUBCOMMANDS = new HashMap<>();

    static {
        SUBCOMMANDS.put("weekly", (c) -> {
            List<PlayerData> playerData = DB_MANAGER.getAllPlayerData();
            playerData = DB_MANAGER.getSortedPlayerDatas(playerData, Comparator.comparing(PlayerData::getPlaytime_week), true);
            MessageBuilder msgBuilder = new MessageBuilder()
                    .addHeader(" WEEKLY STATISTICS ", '=', 6);
            for (int i = 0; i < playerData.size() && i < 9; i++) {
                msgBuilder.addText(String.format("%s - %s", playerData.get(i).getUsername(), playerData.get(i).getPlaytime_week()));
            }
            Message msg = msgBuilder.addFooter("", '=', 10)
                    .build();
            msg.send(c.getSource(), true);
            return 0;
        });
        SUBCOMMANDS.put("monthly", (c) -> {
            List<PlayerData> playerData = DB_MANAGER.getAllPlayerData();
            playerData = DB_MANAGER.getSortedPlayerDatas(playerData, Comparator.comparing(PlayerData::getPlaytime_month), true);
            MessageBuilder msgBuilder = new MessageBuilder()
                    .addHeader(" MONTHLY STATISTICS ", '=', 6);
            for (int i = 0; i < playerData.size() && i < 9; i++) {
                msgBuilder.addText(String.format("%s - %s", playerData.get(i).getUsername(), playerData.get(i).getPlaytime_month()));
            }
            Message msg = msgBuilder.addFooter("", '=', 10)
                    .build();
            msg.send(c.getSource(), true);
            return 0;
        });
        SUBCOMMANDS.put("yearly", (c) -> {
            List<PlayerData> playerData = DB_MANAGER.getAllPlayerData();
            playerData = DB_MANAGER.getSortedPlayerDatas(playerData, Comparator.comparing(PlayerData::getPlaytime_year), true);
            MessageBuilder msgBuilder = new MessageBuilder()
                    .addHeader(" YEARLY STATISTICS ", '=', 6);
            for (int i = 0; i < playerData.size() && i < 9; i++) {
                msgBuilder.addText(String.format("%s - %s", playerData.get(i).getUsername(), playerData.get(i).getPlaytime_year()));
            }
            Message msg = msgBuilder.addFooter("", '=', 10)
                    .build();
            msg.send(c.getSource(), true);
            return 0;
        });
        SUBCOMMANDS.put("all", (c) -> {
            List<PlayerData> playerData = DB_MANAGER.getAllPlayerData();
            playerData = DB_MANAGER.getSortedPlayerDatas(playerData, Comparator.comparing(PlayerData::getPlaytime_all), true);
            MessageBuilder msgBuilder = new MessageBuilder()
                    .addHeader(" STATISTICS ", '=', 6);
            for (int i = 0; i < playerData.size() && i < 9; i++) {
                msgBuilder.addText(String.format("%s - %s", playerData.get(i).getUsername(), playerData.get(i).getPlaytime_all()));
            }
            Message msg = msgBuilder.addFooter("", '=', 10)
                    .build();
            msg.send(c.getSource(), true);
            return 0;
        });
    }

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("stats")
                .requires(cs -> cs.hasPermissionLevel(1))
                .then(
                        Commands.argument("time", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    SUBCOMMANDS.keySet().forEach(c -> {
                                        if (StringUtils.startsWithIgnoreCase(c, builder.getRemaining())) {
                                            builder.suggest(c);
                                        } else if (builder.getRemaining().isEmpty()) {
                                            builder.suggest(c);
                                        }
                                    });
                                    return builder.buildFuture();
                                })
                                .executes(CMD)
                );
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String choice = context.getArgument("time", String.class);
        if(!SUBCOMMANDS.containsKey(choice)) {
            SimpleCommandExceptionType exception = new SimpleCommandExceptionType(
                    new LiteralMessage("Time period doesn't exist!"));
            throw new CommandSyntaxException(exception, new LiteralMessage(exception.toString()));
        }
        return SUBCOMMANDS.get(choice).apply(context);
    }
}
