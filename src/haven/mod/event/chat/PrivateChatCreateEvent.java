package haven.mod.event.chat;

import haven.ChatUI;
import haven.mod.event.Event;

public class PrivateChatCreateEvent extends Event{
    private boolean closeable;
    private ChatUI.PrivChat chat;
    private String name;

    public PrivateChatCreateEvent(boolean closable, String name, ChatUI.PrivChat chat)
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

    public ChatUI.PrivChat getChat() {
        return chat;
    }

    public String getName() {
        return name;
    }
}
