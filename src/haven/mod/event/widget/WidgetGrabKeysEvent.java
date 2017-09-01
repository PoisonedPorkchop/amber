package haven.mod.event.widget;

import haven.Widget;
import haven.mod.event.CancellableEvent;

public class WidgetGrabKeysEvent extends CancellableEvent {

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

    @Override
    protected void initialization() {

    }
}
