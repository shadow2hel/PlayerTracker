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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shadow2hel.playertracker.DbManager;
import shadow2hel.playertracker.data.PlayerData;
import shadow2hel.playertracker.setup.Config;
import shadow2hel.playertracker.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

public class CommandMe implements Command<CommandSource> {
    private static final CommandMe CMD = new CommandMe();
    private static final DbManager DB_MANAGER = DbManager.getInstance();
    private static final Logger LOGGER = LogManager.getLogger();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("me")
                .requires(cs -> cs.hasPermissionLevel(1) || Config.SERVER.allowSelfLookup.get())
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        Optional<Entity> player = Optional.ofNullable(context.getSource().getEntity());
        if(!player.isPresent()) {
            SimpleCommandExceptionType exception = new SimpleCommandExceptionType(
                    new LiteralMessage("You are not allowed to use this command in console!"));
            throw new CommandSyntaxException(exception, new LiteralMessage(exception.toString()));
        }
        PlayerData foundPlayer = DB_MANAGER.getPlayerData(player.get().getUniqueID().toString());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z Z");
        format.setTimeZone(TimeZone.getTimeZone(Config.SERVER.timeZone.get()));
        String date = null;
        try {
            if (foundPlayer.getLast_played() == null)
                date = "None";
            else
                date = format.format(foundPlayer.getLast_played());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Message message = new MessageBuilder()
                .addHeader(" SELF LOOKUP ", '=', 6)
                .addText(String.format("Player: %s", foundPlayer.getUsername()))
                .addText(String.format("Total playtime: %s", foundPlayer.getPlaytime_all()))
                .addText(String.format("Weekly playtime: %s", foundPlayer.getPlaytime_week()))
                .addText(String.format("Monthly playtime: %s", foundPlayer.getPlaytime_month()))
                .addText(String.format("Yearly playtime: %s", foundPlayer.getPlaytime_year()))
                .addText(String.format("Last joined: %s", date))
                .addText(String.format("Times joined: %s", foundPlayer.getJoin_count()))
                .addFooter("", '=', 7)
                .build();

        message.send(context.getSource());
        return 0;
    }
}
