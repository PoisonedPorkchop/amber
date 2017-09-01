package haven.mod.selector;


import haven.Coord;
import haven.Text;
import haven.Widget;
import haven.mod.HavenMod;

import java.awt.*;

public class ModWdg extends Widget {
    public final static int height = 31;
    private final static int txty = 8;
    private String name;
    private ModThread thread;
    private haven.Label lblname;
    private haven.Button btnstart, btnstop;

    public ModWdg(HavenMod mod) {
        this.name = mod.getModName();

        sz = new Coord(420, height);
        lblname = new haven.Label(name, Text.num12boldFnd, Color.WHITE);
        add(lblname, new Coord(3, txty));

        btnstart = new haven.Button(50, "Start") {
            @Override
            public void click() {
                thread = new ModThread(() -> {
                    try {
                        mod.start();
                    }
                    catch (ThreadDeath e)
                    {
                        mod.exit();
                    }
                });
                thread.start();
                btnstart.hide();
                btnstop.show();
            }
        };
        btnstop = new haven.Button(50, "Stop") {
            @Override
            public void click() {
                thread.stop();
                thread = null;
                btnstop.hide();
                btnstart.show();
            }
        };
        btnstop.hide();

        add(btnstart, new Coord(270, 3));
        add(btnstop, new Coord(270, 3));
    }
}
