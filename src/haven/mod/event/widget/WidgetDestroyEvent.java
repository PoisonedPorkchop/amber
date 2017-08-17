package haven.mod.event.widget;

import haven.Widget;
import haven.mod.event.Event;

public class WidgetDestroyEvent extends Event {

    protected Widget widget;

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
