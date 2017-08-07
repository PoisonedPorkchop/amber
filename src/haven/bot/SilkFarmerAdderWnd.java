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

public class SilkFarmerAdderWnd extends Window {
    public final GameUI gui;
    public static final int WIDTH = 460;
    private final static int MAX_ITEMS = 13;

    public SilkFarmerAdderWnd(final GameUI gui) {
        super(Coord.z, "SilkFarmerWindow");
        this.gui = gui;

        Button btna = new Button(50, "Load Midi") {
            public void click() {

            }
        };
        add(btna, new Coord(20, 10));

        resize();
    }

    public void resize() {
        super.resize(WIDTH, 100);
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
