package haven.music;


import haven.Glob;
import haven.Session;
import haven.Utils;
import haven.Widget;
import haven.timers.TimerWdg;
import javafx.scene.input.KeyCode;
import org.json.JSONException;
import org.json.JSONObject;

import javax.sound.midi.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SongPlayerThread extends Thread {
    static int[] keys = new int[]{
            KeyEvent.VK_Z,
            KeyEvent.VK_S,
            KeyEvent.VK_X,
            KeyEvent.VK_D,
            KeyEvent.VK_C,
            KeyEvent.VK_V,
            KeyEvent.VK_G,
            KeyEvent.VK_B,
            KeyEvent.VK_H,
            KeyEvent.VK_N,
            KeyEvent.VK_J,
            KeyEvent.VK_M
    };

    public static Component comp;

    public Widget widg;

    public static final int TEMPO = 0x51;
    public static final int NOTE_OFF = 0x80;
    public static final int NOTE_ON = 0x90;
    public static ArrayList<WrappedShortMessage> notes = new ArrayList<WrappedShortMessage>();

    public static long startTime = System.currentTimeMillis();
    public static double tickTime = 0;
    public static long targetTime = System.currentTimeMillis();

    public static int bpm = 90;
    public static int resolution = 0;
    public static int octaveBase = 5;

    public static boolean hasSong = false;
    public static boolean running = false;
    public static boolean loop = false;
    public static boolean stop = false;
    public static boolean grab = false;

    public SongPlayerThread() {
        super("Music Thread");
    }

    @Override
    public void run() {

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (hasSong && running) {
                startTime = System.currentTimeMillis();
                for(WrappedShortMessage note : notes)
                {
                    targetTime = startTime + ((long)(tickTime*((double)note.getTick())));

                    while(System.currentTimeMillis() < targetTime);
                    {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if(stop) {
                        running = false;
                        stop = false;
                        if(widg != null) {
                            for (int key : keys) {
                                KeyEvent keyevent1 = new KeyEvent(SongPlayerThread.comp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), InputEvent.CTRL_MASK, key, KeyEvent.CHAR_UNDEFINED);
                                KeyEvent keyevent2 = new KeyEvent(SongPlayerThread.comp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, key, KeyEvent.CHAR_UNDEFINED);
                                KeyEvent keyevent3 = new KeyEvent(SongPlayerThread.comp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), InputEvent.SHIFT_MASK, key, KeyEvent.CHAR_UNDEFINED);
                                widg.keyup(keyevent1);
                                widg.keyup(keyevent2);
                                widg.keyup(keyevent3);
                            }
                        }
                        break;
                    }

                    int octave = (note.message.getData1() / 12);


                    int keymask = 0;
                    if(octave < octaveBase)
                        keymask = InputEvent.CTRL_MASK;
                    if(octave > octaveBase)
                        keymask = InputEvent.SHIFT_MASK;

                    if(widg != null) {
                        if (note.message.getCommand() == NOTE_ON) {
                            KeyEvent keyevent = new KeyEvent(SongPlayerThread.comp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), keymask, keys[note.message.getData1() % 12], KeyEvent.CHAR_UNDEFINED);
                            widg.keydown(keyevent);
                        } else {
                            KeyEvent keyevent = new KeyEvent(SongPlayerThread.comp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), keymask, keys[note.message.getData1() % 12], KeyEvent.CHAR_UNDEFINED);
                            widg.keyup(keyevent);
                        }
                    }
                    else
                    {
                        running = false;
                        break;
                    }
                }
                if (!loop)
                    running = false;
            }
        }
    }

    public static void loadMIDI(File file) throws InvalidMidiDataException, IOException {
        Sequence sequence = MidiSystem.getSequence(file);

        SongPlayerThread.notes.clear();

        resolution = sequence.getResolution();

        int trackNumber = 0;
        for (Track track : sequence.getTracks()) {
            trackNumber++;
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();
                if(message instanceof MetaMessage)
                {
                    MetaMessage mm = (MetaMessage)message;
                    if(mm.getType() == TEMPO)
                    {
                        byte[] data = mm.getData();
                        int tempo = (data[0] & 0xff) << 16 | (data[1] & 0xff) << 8 | (data[2] & 0xff);
                        bpm = 60000000 / tempo;
                    }
                }
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getCommand() == NOTE_ON) {
                        WrappedShortMessage wsm = new WrappedShortMessage(sm, event.getTick());
                        wsm.setTick(event.getTick());
                        notes.add(wsm);
                    } else if (sm.getCommand() == NOTE_OFF) {
                        WrappedShortMessage wsm = new WrappedShortMessage(sm, event.getTick());
                        wsm.setTick(event.getTick());
                        notes.add(wsm);
                    }
                    else
                    {

                    }
                } else {
                }
            }
        }

        tickTime = (1000d / (resolution * (bpm/60)));

        hasSong = true;
        running = true;
    }

    public static class WrappedShortMessage
    {
        public WrappedShortMessage(ShortMessage message, long tickTime)
        {
            this.message = message;
            this.tickTime = tickTime;
        }

        ShortMessage message;
        long tickTime;

        public void setTick(long tick)
        {
            tickTime = tick;
        }

        public long getTick()
        {
            return tickTime;
        }
    }
}

