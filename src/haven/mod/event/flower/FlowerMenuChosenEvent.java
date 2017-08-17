package haven.mod.event.flower;

import haven.FlowerMenu;
import haven.mod.event.Event;

public class FlowerMenuChosenEvent extends Event{

    private FlowerMenu flowerMenu;
    private FlowerMenu.Petal petal;

    public FlowerMenuChosenEvent(FlowerMenu menu, FlowerMenu.Petal petal)
    {
        this.flowerMenu = menu;
        this.petal = petal;
    }

    @Override
    public boolean isCancellable()
    {
        return false;
    }

    public FlowerMenu getFlowerMenu() {
        return flowerMenu;
    }

    public void setFlowerMenu(FlowerMenu flowerMenu) {
        this.flowerMenu = flowerMenu;
    }

    public String getChosenOption()
    {
        return petal.name;
    }

    public FlowerMenu.Petal getPetal() {
        return petal;
    }

    public void setPetal(FlowerMenu.Petal petal) {
        this.petal = petal;
    }
}
