package haven.mod.event.widget;

import haven.Widget;
import haven.mod.event.Event;
import haven.mod.RunState;

import java.util.ArrayList;
import java.util.Arrays;

public class WidgetMessageEvent extends Event {

    private Widget sender;
    private String msg;
    private Object[] args;

    public WidgetMessageEvent(Widget sender, String message, Object... arguments){
        this.sender = sender;
        this.msg = message;
        this.args = arguments;
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
