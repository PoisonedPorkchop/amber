package haven.mod.event;

import haven.mod.RunState;

import java.util.ArrayList;
import java.util.Arrays;

public class UIMessageEvent extends CancellableEvent {

    private int id;
    private String msg;
    private Object[] args;

    public UIMessageEvent(int id, String message, Object... arguments){
        this.id = id;
        this.msg = message;
        this.args = arguments;
    }

    @Override
    public ArrayList<RunState> runtype() {
        return (ArrayList<RunState>) Arrays.asList(RunState.ANY);
    }

    @Override
    protected void initialization() {

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
