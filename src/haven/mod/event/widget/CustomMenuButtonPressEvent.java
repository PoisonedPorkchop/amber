package haven.mod.event.widget;

import haven.mod.event.Event;

public class CustomMenuButtonPressEvent extends Event {

    private String[] command;

    public CustomMenuButtonPressEvent(String[] command)
    {
        this.command = command;
    }

    public String[] getCommand() {
        return command;
    }

    public void setCommand(String[] command) {
        this.command = command;
    }
}
