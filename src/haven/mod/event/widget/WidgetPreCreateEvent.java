package haven.mod.event.widget;

import haven.mod.event.widget.WidgetCreateEvent;

public class WidgetPreCreateEvent extends WidgetCreateEvent {

    public WidgetPreCreateEvent(int id, String type, int parent, Object[] pargs, Object... cargs)
    {
        this.id = id;
        this.type = type;
        this.parent = parent;
        this.pargs = pargs;
        this.cargs = cargs;
    }
}
