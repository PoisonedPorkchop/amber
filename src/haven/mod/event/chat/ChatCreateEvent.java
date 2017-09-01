package haven.mod.event.chat;

import haven.ChatUI;
import haven.mod.event.Event;

public class ChatCreateEvent extends Event {

    private boolean closeable;
    private String name;
    private int urgency;
    private ChatUI.MultiChat chat;

    public ChatCreateEvent(boolean closable, String name, int urgency, ChatUI.MultiChat chat)
    {
        this.closeable = closable;
        this.name = name;
        this.urgency = urgency;
        this.chat = chat;
    }

    public boolean isCloseable() {
        return closeable;
    }

    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUrgency() {
        return urgency;
    }

    public void setUrgency(int urgency) {
        this.urgency = urgency;
    }

    public ChatUI.MultiChat getChat() {
        return chat;
    }
}
