package shadow2hel.playertracker.commands;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import java.util.LinkedList;
import java.util.List;

public class MessageBuilder {
    private List<ITextComponent> textHeaders;
    private List<ITextComponent> textMain;
    private List<ITextComponent> textFooters;

    public MessageBuilder() {
        textHeaders = new LinkedList<>();
        textMain = new LinkedList<>();
        textFooters = new LinkedList<>();
    }

    public MessageBuilder addHeader(String headerName, char encasing, int amountOfEncasing) {
        StringBuilder encasingString = new StringBuilder();
        for (int i = 0; i < amountOfEncasing; i++) {
            encasingString.append(encasing);
        }
        StringTextComponent header = new StringTextComponent(encasingString.toString());
        header.applyTextStyle(TextFormatting.GOLD);
        header.appendSibling(new StringTextComponent(headerName).applyTextStyle(TextFormatting.WHITE));
        header.appendSibling(new StringTextComponent(encasingString.toString()).applyTextStyle(TextFormatting.GOLD));
        textHeaders.add(header);
        return this;
    }

    public MessageBuilder addFooter(String headerName, char encasing, int amountOfEncasing) {
        StringBuilder encasingString = new StringBuilder();
        for (int i = 0; i < amountOfEncasing; i++) {
            encasingString.append(encasing);
        }
        StringTextComponent footer = new StringTextComponent(encasingString.toString());
        footer.applyTextStyle(TextFormatting.GOLD);
        footer.appendSibling(new StringTextComponent(headerName).applyTextStyle(TextFormatting.WHITE));
        footer.appendSibling(new StringTextComponent(encasingString.toString()).applyTextStyle(TextFormatting.GOLD));
        textFooters.add(footer);
        return this;
    }

    public MessageBuilder addText(String message) {
        textMain.add(new StringTextComponent(message));
        return this;
    }

    public MessageBuilder addText(String message, TextFormatting formatting) {
        textMain.add(new StringTextComponent(message).applyTextStyle(formatting));
        return this;
    }

    public MessageBuilder addText(String message, Style style) {
        textMain.add(new StringTextComponent(message).setStyle(style));
        return this;
    }

    public MessageBuilder addTextComponent(ITextComponent component) {
        textMain.add(component);
        return this;
    }

    public Message build() {
        return new Message(textHeaders, textMain, textFooters);
    }
}
