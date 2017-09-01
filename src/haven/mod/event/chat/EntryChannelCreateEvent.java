package haven.mod.event.chat;

import haven.ChatUI;
import haven.mod.event.Event;

public class EntryChannelCreateEvent extends Event {

    private boolean closable;
    private ChatUI.EntryChannel channel;

    public EntryChannelCreateEvent(boolean closable, ChatUI.EntryChannel channel)
    {
        this.closable = closable;
        this.channel = channel;
    }

    public ChatUI.EntryChannel getChannel() {
        return channel;
    }

    public boolean isClosable() {
        return closable;
    }

    public void setClosable(boolean closable) {
        this.closable = closable;
    }
}
