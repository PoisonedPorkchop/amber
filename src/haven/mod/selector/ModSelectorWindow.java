package haven.mod.selector;

import haven.*;
import haven.mod.HavenMod;
import haven.mod.Mod;
import haven.timers.TimerWdg;

import java.util.ArrayList;
import java.util.List;

public class ModSelectorWindow extends Window {

    public static final int WIDTH = 460;
    public final Scrollport port;
    private final static int MAX_ITEMS = 10;

    public ModSelectorWindow() {
        super(Coord.z, "ModSelector");

        int portHeight = new Mod().getAPI().getMods().size() > MAX_ITEMS ? TimerWdg.height * MAX_ITEMS : new Mod().getAPI().getMods().size() * TimerWdg.height;
        port = new Scrollport(new Coord(WIDTH - 20 - 15, portHeight), TimerWdg.height) {
            @Override
            public void draw(GOut g) {
                g.chcolor(0, 0, 0, 128);
                g.frect(Coord.z, sz);
                g.chcolor();
                super.draw(g);
            }
        };
        add(port, new Coord(20, 50));

        ArrayList<HavenMod> selectedMods = new ArrayList<>();
        for(HavenMod mod : new Mod().getAPI().getMods())
            if(!mod.isRunOnStart())
                selectedMods.add(mod);

        for (int i = 0; i < selectedMods.size(); i++)
            port.cont.add(new ModWdg(selectedMods.get(i)), new Coord(0, i * TimerWdg.height));

        resize();
    }

    public void resize() {
        List<TimerWdg> timers = Glob.timersThread.getall();
        int portHeight = timers.size() > MAX_ITEMS ? TimerWdg.height * MAX_ITEMS : timers.size() * TimerWdg.height;
        port.resize(port.sz.x, portHeight);
        port.cont.update();
        port.bar.resize(portHeight);
        port.bar.changed();
        super.resize(WIDTH, portHeight + 60);
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
        if (sender == cbtn) {
            hide();
        } else {
            super.wdgmsg(sender, msg, args);
        }
    }

}
