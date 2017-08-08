package haven.bot;

import haven.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static haven.OCache.posres;

public class DreamBot extends Thread {

    public static Component comp;
    public ArrayList<Gob> cupboards;
    public HashMap<Gob, DreamCatcher> dreamCatchers;

    public DreamBot() {
        super("DreamBot Thread");
        cupboards = new ArrayList<>();
        dreamCatchers = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GameUI gui = HavenPanel.lui.root.findchild(GameUI.class);

        DreamUpdater dreamupdate = new DreamUpdater();
        dreamupdate.start();

        while (true) {
            for(Map.Entry<Gob,DreamCatcher> entry : dreamCatchers.entrySet())
            {
                gui.map.wdgmsg("click", entry.getKey().sc, entry.getKey().rc.floor(posres), 3, 0, 0, (int) entry.getKey().id, entry.getKey().rc.floor(posres), 0, -1);
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class DreamUpdater extends Thread {

        public DreamUpdater()
        {
            super("DreamUpdater");
        }

        @Override
        public void run()
        {
            long startTime = System.currentTimeMillis();
            long targetTime = startTime + (1000*60);
            while(true) {
                while (startTime < targetTime) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                updateListings();
                startTime = System.currentTimeMillis();
                targetTime = startTime + (5000 * 60);
            }
        }

        public void updateListings()
        {
            OCache oc = HavenPanel.lui.sess.glob.oc;
            ArrayList<Gob> newCupboards = new ArrayList<>();
            ArrayList<Gob> newDreams = new ArrayList<>();
            synchronized (oc) {
                for (Gob gob : oc) {
                    try {
                        Resource res = gob.getres();
                        if(res.name.contains("gfx/terobjs/cupboard"))
                        {
                            newCupboards.add(gob);
                        }
                        else if(res.name.contains("gfx/terobjs/dreca"))
                        {
                            newDreams.add(gob);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            synchronized (cupboards) {
                cupboards = newCupboards;
            }

            synchronized (dreamCatchers) {
                HashMap<Gob, DreamCatcher> dreamsNewMap = new HashMap<>();
                for (Gob gob : newDreams) {
                    if (dreamCatchers.containsKey(gob))
                        dreamsNewMap.put(gob, dreamCatchers.get(gob));
                    else
                        dreamsNewMap.put(gob, new DreamCatcher());
                }
                dreamCatchers = dreamsNewMap;
            }
        }
    }

    public class DreamCatcher {

        public long nextExpectedDream;
        private float quality = 0;

        public DreamCatcher()
        {
        }

        public float getQuality() {
            return quality;
        }
        public void setQuality(float quality)
        {
            this.quality = quality;
        }
    }
}