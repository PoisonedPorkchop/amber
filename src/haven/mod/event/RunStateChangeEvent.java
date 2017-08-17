package haven.mod.event;

import haven.mod.ModAPI;
import haven.mod.RunState;

public class RunStateChangeEvent extends Event {

    private RunState state;

    public RunStateChangeEvent(RunState state)
    {
        this.state = state;
        ModAPI.setRunState(state);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    public RunState getState() {
        return state;
    }

    public void setState(RunState state) {
        this.state = state;
    }
}
