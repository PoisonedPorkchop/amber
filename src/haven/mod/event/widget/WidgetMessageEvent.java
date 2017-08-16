package haven.mod.event.widget;

import haven.Widget;
import haven.mod.event.Event;
import haven.mod.RunState;

import java.util.ArrayList;
import java.util.Arrays;

public class WidgetMessageEvent implements Event {

    private boolean cancelled;
    private Widget sender;
    private String msg;
    private Object[] args;

    public WidgetMessageEvent(Widget sender, String message, Object... arguments){
        cancelled = false;
        this.sender = sender;
        this.msg = message;
        this.args = arguments;
    }

    @Override
    public boolean isCancellable() {
        return true;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override public boolean getCancelled()
    {
        return cancelled;
    }

    @Override
    public ArrayList<RunState> runtype() {
        return (ArrayList<RunState>) Arrays.asList(RunState.ANY);
    }

    public Widget getSender()
    {
        return sender;
    }

    public String getMessage()
    {
        return msg;
    }

    public Object[] getArguments()
    {
        return args;
    }

    public void setSender(Widget sender)
    {
        this.sender = sender;
    }

    public void setMessage(String message)
    {
        this.msg = message;
    }

    public void setArguments(Object[] arguments)
    {
        this.args = arguments;
    }
}
