package haven.mod.event;

import java.util.ArrayList;
import java.util.Arrays;

public interface Event {
    default boolean isCancellable()
    {
        return false;
    }

    void setCancelled(boolean cancel);
    boolean getCancelled();

    default ArrayList<RunState> runtype() {
        return (ArrayList<RunState>) Arrays.asList(RunState.UNKNOWN);
    }
}
