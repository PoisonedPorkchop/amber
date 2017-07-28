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
    public static long tickTime = 0;
    public static long targetTime = System.currentTimeMillis();

    public static int bpm = 90;
    public static int resolution = 0;
    public static int octaveBase = 5;

    public static boolean hasSong = false;
    public static boolean running = false;
    public static boolean loop = false;

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
                    targetTime = startTime + (tickTime*note.getTick());
                    while(System.currentTimeMillis() < targetTime);
                    {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    int octave = (note.message.getData1() / 12);

                    System.out.println("Octave: " + octave + ", Normal: " + SongPlayerThread.octaveBase);

                    if(octave == octaveBase-1)
                    {
                        if(note.message.getCommand() == NOTE_ON)
                        {
                            KeyEvent keyevent2 = new KeyEvent(SongPlayerThread.comp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), InputEvent.CTRL_MASK, keys[note.message.getData1()%12], KeyEvent.CHAR_UNDEFINED);
                            widg.keydown(keyevent2);
                        }
                        else
                        {
                            KeyEvent keyevent2 = new KeyEvent(SongPlayerThread.comp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), InputEvent.CTRL_MASK, keys[note.message.getData1()%12], KeyEvent.CHAR_UNDEFINED);
                            widg.keyup(keyevent2);
                        }
                    }
                    else if(octave == octaveBase)
                    {
                        if(note.message.getCommand() == NOTE_ON)
                        {
                            KeyEvent keyevent1 = new KeyEvent(SongPlayerThread.comp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keys[note.message.getData1()%12], KeyEvent.CHAR_UNDEFINED);
                            widg.keydown(keyevent1);
                        }
                        else
                        {
                            KeyEvent keyevent1 = new KeyEvent(SongPlayerThread.comp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, keys[note.message.getData1()%12], KeyEvent.CHAR_UNDEFINED);
                            widg.keyup(keyevent1);
                        }
                    }
                    else if(octave == octaveBase +1)
                    {
                        if(note.message.getCommand() == NOTE_ON)
                        {
                            KeyEvent keyevent2 = new KeyEvent(SongPlayerThread.comp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), InputEvent.SHIFT_MASK, keys[note.message.getData1()%12], KeyEvent.CHAR_UNDEFINED);
                            widg.keydown(keyevent2);
                        }
                        else
                        {
                            KeyEvent keyevent2 = new KeyEvent(SongPlayerThread.comp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), InputEvent.SHIFT_MASK, keys[note.message.getData1()%12], KeyEvent.CHAR_UNDEFINED);
                            widg.keyup(keyevent2);
                        }
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
                        /**
                         int key = sm.getData1();
                         int octave = (key / 12) - 1;
                         int note = key % 12;*/
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

        tickTime = (long) (1000f / (resolution * (bpm/60)));

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

