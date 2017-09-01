package haven.mod.event;

import haven.mod.Mod;
import haven.mod.RunState;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class Event {

    public ArrayList<RunState> runtype() {
        return (ArrayList<RunState>) Arrays.asList(RunState.UNKNOWN);
    }

    public void call()
    {
        new Mod().getAPI().callEvent(this);
    }

    public void initialize() throws Exception {
        if(!new Mod().getAPI().isEventRegistered(this.getClass()))
        {
            initialization();
        }
    }

    protected void initialization() throws Exception
    {

    }
}
