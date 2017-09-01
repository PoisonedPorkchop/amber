package haven.mod.event;

import haven.mod.Mod;
import haven.mod.RunState;

public class RunStateChangeEvent extends Event {

    private RunState state;

    public RunStateChangeEvent(RunState state)
    {
        this.state = state;
        new Mod().getAPI().setRunState(state);
    }

    @Override
    protected void initialization() {

    }

    public RunState getState() {
        return state;
    }

    public void setState(RunState state) {
        this.state = state;
    }

}
