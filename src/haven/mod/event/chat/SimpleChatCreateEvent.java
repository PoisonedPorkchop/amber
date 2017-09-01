package haven.mod.event.chat;

import haven.ChatUI;
import haven.mod.event.Event;

public class SimpleChatCreateEvent extends Event{
    private boolean closeable;
    private ChatUI.SimpleChat chat;
    private String name;

    public SimpleChatCreateEvent(boolean closable, String name, ChatUI.SimpleChat chat)
    {
        this.closeable = closable;
        this.chat = chat;
        this.name = name;
    }

    public boolean isCloseable() {
        return closeable;
    }

    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
    }

    public ChatUI.SimpleChat getChat() {
        return chat;
    }

    public String getName() {
        return name;
    }
}
