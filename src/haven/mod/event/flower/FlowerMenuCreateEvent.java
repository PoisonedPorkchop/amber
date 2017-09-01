package haven.mod.event.flower;

import haven.FlowerMenu;
import haven.mod.event.Event;

public class FlowerMenuCreateEvent extends Event{

    private FlowerMenu menu;
    private String[] options;

    public FlowerMenuCreateEvent(FlowerMenu menu, String... options)
    {
        this.options = options;
        this.menu = menu;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public boolean hasOption(String option)
    {
        for(String opt : options)
            if(opt.equals(option))
                return true;
        return false;
    }

    @Override
    protected void initialization() {

    }

    public FlowerMenu getMenu() {
        return menu;
    }

    public void setMenu(FlowerMenu menu) {
        this.menu = menu;
    }

}
