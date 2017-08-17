package haven.mod.event.flower;

import haven.FlowerMenu;
import haven.mod.event.Event;

public class FlowerMenuChooseEvent extends Event {

    private FlowerMenu.Petal petal;

    public FlowerMenuChooseEvent(FlowerMenu.Petal petal)
    {
        this.petal = petal;
    }

    public String getChosenOption()
    {
        return petal.name;
    }

}
