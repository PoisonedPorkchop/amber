package haven.mod.event.widget;

import haven.Widget;
import haven.mod.event.Event;

public class WidgetGrabKeysEvent extends Event {

    protected Widget widget;

    public WidgetGrabKeysEvent(Widget widget)
    {
        this.widget = widget;
    }

    public Widget getWidget() {
        return widget;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }
}
