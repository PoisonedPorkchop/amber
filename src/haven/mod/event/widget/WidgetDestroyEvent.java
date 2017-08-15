package haven.mod.event.widget;

import haven.Widget;
import haven.mod.event.Event;

public class WidgetDestroyEvent implements Event {

    boolean cancelled;
    protected Widget widget;

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

    public Widget getWidget() {
        return widget;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }

    public WidgetDestroyEvent(Widget widget)
    {
        this.widget = widget;
    }
}
