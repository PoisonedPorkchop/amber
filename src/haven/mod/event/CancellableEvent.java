package haven.mod.event;

public abstract class CancellableEvent extends Event {

    private boolean cancelled;

    public void setCancelled(boolean cancel)
    {
        this.cancelled = cancel;
    }

    public boolean getCancelled()
    {
        return cancelled;
    }

    public boolean callAndGetCancelled()
    {
        call();
        if(cancelled)
            return true;
        return false;
    }
}
