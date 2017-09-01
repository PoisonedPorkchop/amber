package haven.mod.event;

public class CustomMenuButtonPressEvent extends CancellableEvent {

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

    @Override
    protected void initialization() {

    }
}
