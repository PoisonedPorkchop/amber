package haven.mod.event.chat;

import haven.ChatUI;
import haven.mod.event.Event;

public class LogCreateEvent extends Event {

    private String name;
    private ChatUI.Log log;

    public LogCreateEvent(String name, ChatUI.Log log)
    {
        this.name = name;
        this.log = log;
    }

    public String getName() {
        return name;
    }

    public ChatUI.Log getLog() {
        return log;
    }
}
