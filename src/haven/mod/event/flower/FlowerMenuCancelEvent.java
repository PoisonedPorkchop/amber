package haven.mod.event.flower;

import haven.FlowerMenu;
import haven.mod.event.Event;

public class FlowerMenuCancelEvent extends Event {

    private FlowerMenu flowerMenu;

    public FlowerMenuCancelEvent(FlowerMenu menu)
    {
        this.flowerMenu = menu;
    }

    @Override
    protected void initialization() {

    }

    public FlowerMenu getFlowerMenu() {
        return flowerMenu;
    }

    public void setFlowerMenu(FlowerMenu flowerMenu) {
        this.flowerMenu = flowerMenu;
    }


}
