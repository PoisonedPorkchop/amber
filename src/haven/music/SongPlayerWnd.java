package haven.music;


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

public class SongPlayerWnd extends Window {
    public final GameUI gui;
    public static final int WIDTH = 460;
    private final static int MAX_ITEMS = 13;

    public SongPlayerWnd(final GameUI gui) {
        super(Coord.z, "Music");
        this.gui = gui;

        Button btna = new Button(50, "Load Midi") {
            public void click() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException e1) {
                    e1.printStackTrace();
                } catch (InstantiationException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                } catch (UnsupportedLookAndFeelException e1) {
                    e1.printStackTrace();
                }
                final JFrame newFrame = new JFrame("Choose MIDI file");
                final JFileChooser filepopup = new JFileChooser();
                filepopup.setAcceptAllFileFilterUsed(false);
                filepopup.setFileFilter(new FileNameExtensionFilter("MIDI","mid"));
                try {
                    filepopup.setCurrentDirectory(new File(SongPlayerWnd.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                filepopup.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if(e.getActionCommand() == "CancelSelection")
                        {
                            newFrame.dispatchEvent(new WindowEvent(newFrame, WindowEvent.WINDOW_CLOSING));
                        }
                        else if(e.getActionCommand() == "ApproveSelection")
                        {
                            try {
                                SongPlayerThread.loadMIDI(filepopup.getSelectedFile());
                            } catch (InvalidMidiDataException e1) {
                                e1.printStackTrace();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            newFrame.dispatchEvent(new WindowEvent(newFrame, WindowEvent.WINDOW_CLOSING));
                        }
                    }
                });
                newFrame.add(filepopup);
                newFrame.pack();
                newFrame.setVisible(true);

                newFrame.addWindowListener(new WindowAdapter()
                {
                    public void windowClosing(WindowEvent e)
                    {
                        newFrame.dispose();
                    }
                });
            }
        };
        add(btna, new Coord(20, 10));

        Button btnb = new Button(50, "Stop") {
            public void click() {
                if(SongPlayerThread.running)
                    SongPlayerThread.stop = true;
            }
        };
        add(btnb, new Coord(20, 40));

        Button btnc = new Button(50, "Delay") {
            public void click() {
                SongPlayerThread.startTime += 100;
            }
        };
        add(btnc, new Coord(100, 40));


        Button btnd = new Button(50, "Skip") {
            public void click() {
                SongPlayerThread.startTime -= 100;
            }
        };
        add(btnd, new Coord(100, 80));

        CheckBox chkloop = new CheckBox("Loop") {
            {
                a = false;
            }

            public void set(boolean val) {
                a = val;
                SongPlayerThread.loop = val;
            }
        };
        add(chkloop, new Coord(350, 15));

        resize();
    }

    public void resize() {
        List<TimerWdg> timers = Glob.timersThread.getall();
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
