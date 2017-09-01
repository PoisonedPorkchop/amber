package haven.mod.event.flower;

import haven.FlowerMenu;
import haven.mod.event.CancellableEvent;

public class FlowerMenuChooseEvent extends CancellableEvent {

    private FlowerMenu.Petal petal;

    public FlowerMenuChooseEvent(FlowerMenu.Petal petal)
    {
        this.petal = petal;
    }

    public String getChosenOption()
    {
        return petal.name;
    }

    @Override
    protected void initialization() {

    }
}
