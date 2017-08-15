package haven.mod.event.widget;

import haven.mod.event.Event;

public class WidgetCreateEvent implements Event {

    boolean cancelled;
    protected int id;
    protected String type;
    protected int parent;
    protected Object[] pargs;
    protected Object[] cargs;

    @Override
    public boolean isCancellable() {
        return true;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public boolean getCancelled() {
        return cancelled;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public Object[] getPargs() {
        return pargs;
    }

    public void setPargs(Object[] pargs) {
        this.pargs = pargs;
    }

    public Object[] getCargs() {
        return cargs;
    }

    public void setCargs(Object[] cargs) {
        this.cargs = cargs;
    }
}
