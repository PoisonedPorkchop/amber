package haven.mod.event.widget;

import haven.Widget;
import haven.mod.event.Event;

public class WidgetPostCreateEvent extends Event {

    private int id;
    private String type;
    private int parent;
    private Object[] pargs;
    private Object[] cargs;
    private Widget widget;

    public WidgetPostCreateEvent(int id, String type, int parent, Object[] pargs, Widget widget, Object... cargs)
    {
        this.id = id;
        this.type = type;
        this.parent = parent;
        this.pargs = pargs;
        this.cargs = cargs;
        this.widget = widget;
    }

    @Override
    protected void initialization() {

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

    public Object[] getParentArgs() {
        return pargs;
    }

    public void setParentArgs(Object[] pargs) {
        this.pargs = pargs;
    }

    public Object[] getChildArgs() { return cargs; }

    public void setChildArgs(Object[] cargs) {
        this.cargs = cargs;
    }

    public Widget getWidget() {
        return widget;
    }
}
