package haven.mod.event;

import haven.Inventory;

public class InventoryCreateEvent extends Event {

    private Inventory inventory;

    public InventoryCreateEvent(Inventory inventory)
    {
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    protected void initialization() {

    }
}
