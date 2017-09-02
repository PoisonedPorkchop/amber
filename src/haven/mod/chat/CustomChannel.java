package haven.mod.chat;

import haven.ChatUI;

public class CustomChannel extends ChatUI.Channel{

    private String name;

    public CustomChannel(boolean closable, String name) {
        super(closable);
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }
}
