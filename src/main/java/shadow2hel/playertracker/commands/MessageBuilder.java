package shadow2hel.playertracker.commands;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import java.util.LinkedList;
import java.util.List;

public class MessageBuilder {
    private List<ITextComponent> textComponents;

    public MessageBuilder() {
        textComponents = new LinkedList<>();
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
        textComponents.add(header);
        return this;
    }

    public MessageBuilder addFooter(char encasing, int amountOfEncasing) {
        StringBuilder encasingString = new StringBuilder();
        for (int i = 0; i < amountOfEncasing; i++) {
            encasingString.append(encasing);
        }
        textComponents.add(new StringTextComponent(encasingString.toString()).applyTextStyle(TextFormatting.GOLD));
        return this;
    }

    public MessageBuilder addText(String message) {
        textComponents.add(new StringTextComponent(message));
        return this;
    }

    public MessageBuilder addText(String message, TextFormatting formatting) {
        textComponents.add(new StringTextComponent(message).applyTextStyle(formatting));
        return this;
    }

    public MessageBuilder addText(String message, Style style) {
        textComponents.add(new StringTextComponent(message).setStyle(style));
        return this;
    }

    public MessageBuilder addTextComponent(ITextComponent component) {
        textComponents.add(component);
        return this;
    }

    public List<ITextComponent> build() {
        return textComponents;
    }
}
