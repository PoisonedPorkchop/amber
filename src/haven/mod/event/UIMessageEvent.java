package haven.mod.event;

import java.util.ArrayList;
import java.util.Arrays;

public class UIMessageEvent implements Event {

    private boolean cancelled;
    private int id;
    private String msg;
    private Object[] args;

    public UIMessageEvent(int id, String message, Object... arguments){
        cancelled = false;
        this.id = id;
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

    public int getId()
    {
        return id;
    }

    public String getMessage()
    {
        return msg;
    }

    public Object[] getArguments()
    {
        return args;
    }

    public void setId(int id)
    {
        this.id = id;
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
