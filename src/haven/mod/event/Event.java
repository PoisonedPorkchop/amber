package haven.mod.event;

import haven.mod.RunState;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class Event {

    private boolean cancelled;

    public boolean isCancellable()
    {
        return true;
    }

    public void setCancelled(boolean cancel)
    {
        this.cancelled = cancel;
    }

    public boolean getCancelled()
    {
        return cancelled;
    }

    public ArrayList<RunState> runtype() {
        return (ArrayList<RunState>) Arrays.asList(RunState.UNKNOWN);
    }
}
