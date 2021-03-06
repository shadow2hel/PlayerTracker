package shadow2hel.playertracker.commands;

import net.minecraft.command.CommandSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.xml.ws.Holder;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Message {
    private List<ITextComponent> textHeaders;
    private List<ITextComponent> textMain;
    private List<ITextComponent> textFooters;


    public Message(List<ITextComponent> textHeaders, List<ITextComponent> textMain, List<ITextComponent> textFooters) {
        this.textHeaders = textHeaders;
        this.textMain = textMain;
        this.textFooters = textFooters;
    }

    public void send(CommandSource s) {
        if (textHeaders != null && textHeaders.size() > 0)
            textHeaders.forEach(h -> s.sendFeedback(h, false));
        if (textHeaders != null && textHeaders.size() > 0)
            textMain.forEach(t -> s.sendFeedback(t, false));
        if (textFooters != null && textFooters.size() > 0)
            textFooters.forEach(f -> s.sendFeedback(f, false));
    }

}
