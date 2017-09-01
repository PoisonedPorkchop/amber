package haven.mod.event.chat;

import haven.ChatUI;
import haven.mod.event.Event;

public class ChannelCreateEvent extends Event {

    private boolean closable;
    private ChatUI.Channel channel;

    public ChannelCreateEvent(boolean closable, ChatUI.Channel channel)
    {
        this.closable = closable;
        this.channel = channel;
    }

    public boolean isClosable() {
        return closable;
    }

    public ChatUI.Channel getChannel() {
        return channel;
    }
}
