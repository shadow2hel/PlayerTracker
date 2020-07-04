package shadow2hel.playertracker.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shadow2hel.playertracker.DbManager;
import shadow2hel.playertracker.data.PlayerData;
import shadow2hel.playertracker.setup.Config;
import shadow2hel.playertracker.utils.StringUtils;
import shadow2hel.playertracker.utils.TimeSelector;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CommandStats implements Command<CommandSource> {

    private static final CommandStats CMD = new CommandStats();
    private static final DbManager DB_MANAGER = DbManager.getInstance();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final List<String> SUBCOMMANDS = new ArrayList<>();

    static {
        for (TimeSelector value : TimeSelector.values()) {
            String text = value.name().toLowerCase();
            if (value != TimeSelector.ALL) {
                text += "ly";
            }
            SUBCOMMANDS.add(text);
        }
    }

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("stats")
                .requires(cs -> cs.hasPermissionLevel(1))
                .then(
                        Commands.argument("time", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    SUBCOMMANDS.forEach(c -> {
                                        if (StringUtils.startsWithIgnoreCase(c, builder.getRemaining())) {
                                            builder.suggest(c);
                                        } else if (builder.getRemaining().isEmpty()) {
                                            builder.suggest(c);
                                        }
                                    });
                                    return builder.buildFuture();
                                })
                                .then(
                                        Commands.argument("page", IntegerArgumentType.integer())
                                        .executes(CMD)
                                )
                                .executes(CMD)
                );
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String choice = context.getArgument("time", String.class);
        Optional<Integer> pageNumber = Optional.empty();
        if (context.getNodes().size() > 2)
            pageNumber = Optional.of(context.getArgument("page", Integer.class));
        if (pageNumber.isPresent() && pageNumber.get() <= 0) {
            SimpleCommandExceptionType exception = new SimpleCommandExceptionType(
                    new LiteralMessage("Page number needs to be bigger than 0!"));
            throw new CommandSyntaxException(exception, new LiteralMessage(exception.toString()));
        }

        if(!SUBCOMMANDS.contains(choice)) {
            SimpleCommandExceptionType exception = new SimpleCommandExceptionType(
                    new LiteralMessage("Time period doesn't exist!"));
            throw new CommandSyntaxException(exception, new LiteralMessage(exception.toString()));
        }
        return runSubcommand(context, pageNumber.orElse(1), choice);
    }

    private int runSubcommand(CommandContext<CommandSource> context, int pageNumber, String choice) throws CommandSyntaxException {
        List<PlayerData> playerData = DB_MANAGER.getAllPlayerData();
        switch(choice) {
            case "weekly":
                playerData = DB_MANAGER.getSortedPlayerDatas(playerData, Comparator.comparing(PlayerData::getPlaytime_week), true);
                break;
            case "monthly":
                playerData = DB_MANAGER.getSortedPlayerDatas(playerData, Comparator.comparing(PlayerData::getPlaytime_month), true);
                break;
            case "yearly":
                playerData = DB_MANAGER.getSortedPlayerDatas(playerData, Comparator.comparing(PlayerData::getPlaytime_year), true);
                break;
            case "all":
                playerData = DB_MANAGER.getSortedPlayerDatas(playerData, Comparator.comparing(PlayerData::getPlaytime_all), true);
                break;
        }

        int maxEntriesPerPage = Config.SERVER.maxEntriesPerPage.get();
        int possibleEntries = pageNumber * maxEntriesPerPage;
        int bar = ((int)Math.ceil((double)playerData.size() / maxEntriesPerPage)) * maxEntriesPerPage;
        if (possibleEntries > bar) {
            SimpleCommandExceptionType exception = new SimpleCommandExceptionType(
                    new LiteralMessage("Not enough entries!"));
            throw new CommandSyntaxException(exception, new LiteralMessage(exception.toString()));
        }

        String allOrNot = choice.toUpperCase().equals("ALL") ? "" : " " + choice.toUpperCase();
        MessageBuilder msgBuilder = new MessageBuilder()
                .addHeader( allOrNot + " STATISTICS ", '=', 6);

        for (int i = possibleEntries - maxEntriesPerPage; i < playerData.size() && i < possibleEntries; i++) {
            msgBuilder.addText(String.format("%d. %s - %s", i + 1, playerData.get(i).getUsername(), playerData.get(i).getPlaytime_week()));
        }

        int amountPages = (int)Math.ceil((double)playerData.size() / (double)maxEntriesPerPage);
        List<ITextComponent> links = new ArrayList<>();
        for (int i = 0; i < amountPages; i++) {
            StringTextComponent child;
            if (i + 1 == pageNumber) {
                child = new StringTextComponent(String.format(" %d,", (i+1)));
                if(amountPages==1)
                    child = new StringTextComponent(String.format(" %d ", (i+1)));
                if(i + 1 == amountPages)
                    child = new StringTextComponent(String.format(" %d ", (i+1)));
                child.setStyle(new Style().setBold(true));
                child.applyTextStyle(TextFormatting.GREEN);
            } else if (i + 1 == amountPages) {
                child = new StringTextComponent(String.format(" %d ", (i+1)));
                child.setStyle(new Style()
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pt stats " + choice + " " + (i + 1)))
                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("/pt stats " + choice + " " + (i + 1)))));
            } else {
                child = new StringTextComponent( String.format(" %d,", (i + 1)));
                child.setStyle(new Style()
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pt stats " + choice + " " + (i + 1)))
                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("/pt stats " + choice + " " + (i + 1)))));
            }
            links.add(child);
        }

        ITextComponent rootFoot = new StringTextComponent("");
        links.forEach(rootFoot::appendSibling);

        Message msg = msgBuilder.addFooter(rootFoot, '=', 10)
                .build();
        msg.send(context.getSource());
        return 0;
    }
}
