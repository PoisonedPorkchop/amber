package haven.bot;


import haven.*;
import haven.timers.TimerWdg;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class SilkFarmerWnd extends Window {
    public final GameUI gui;
    public static final int WIDTH = 460;
    public static final int HEIGHT = 600;
    public SilkFarmerAdderWnd adder;

    public SilkFarmerWnd(final GameUI gui) {
        super(Coord.z, "Silk Farmer Window");
        this.gui = gui;

        adder = null;

        Scrollport scrollmaster = new Scrollport(new Coord(440,550))
        {
            @Override
            public void draw(GOut g) {
                g.chcolor(0, 0, 0, 230);
                g.frect(Coord.z, sz);
                g.chcolor();
                super.draw(g);
            }
        };
        add(scrollmaster, new Coord(10, 40));

        Scrollport scrollcupboard = new Scrollport(new Coord(420,170))
        {
            @Override
            public void draw(GOut g) {
                g.chcolor(30, 30, 30, 255);
                g.frect(Coord.z, sz);
                g.chcolor();
                super.draw(g);
            }
        };
        scrollmaster.add(scrollcupboard, new Coord(10, 190));

        Scrollport scrolltable = new Scrollport(new Coord(420,170))
        {
            @Override
            public void draw(GOut g) {
                g.chcolor(30, 30, 30, 255);
                g.frect(Coord.z, sz);
                g.chcolor();
                super.draw(g);
            }
        };
        scrollmaster.add(scrolltable, new Coord(10, 10));

        Scrollport scrollfood = new Scrollport(new Coord(420,170))
        {
            @Override
            public void draw(GOut g) {
                g.chcolor(30, 30, 30, 255);
                g.frect(Coord.z, sz);
                g.chcolor();
                super.draw(g);
            }
        };
        scrollmaster.add(scrollfood, new Coord(10, 370));

        resize();
    }

    public void resize() {
        super.resize(WIDTH, HEIGHT);
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
        if (sender == cbtn) {
            hide();
        } else {
            super.wdgmsg(sender, msg, args);
        }
    }

    @Override
    public boolean type(char key, java.awt.event.KeyEvent ev) {
        if (key == 27) {
            hide();
            return true;
        }
        return super.type(key, ev);
    }
}
