/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import haven.automation.AreaSelectCallback;
import haven.automation.GobSelectCallback;
import haven.automation.MusselPicker;
import haven.automation.SteelRefueler;
import haven.mod.Mod;
import haven.mod.RunState;
import haven.mod.event.RunStateChangeEvent;
import haven.pathfinder.PFListener;
import haven.pathfinder.Pathfinder;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;

import static haven.MCache.tilesz;
import static haven.OCache.posres;

public class MapView extends Widget implements DTarget, Console.Directory, PFListener {
    public static boolean clickdb = false;
    public static long plgob = -1;
    public static Coord2d pllastcc;
    public Coord2d cc;
    public final Glob glob;
    private static final int view = 2;
    private Plob placing = null;
    private int[] visol = new int[32];
    private Grabber grab;
    private Selector selection;
    private Coord3f camoff = new Coord3f(Coord3f.o);
    public double shake = 0.0;
    public static int plobgran = 8;
    private String tooltip;
    private boolean showgrid;
    private TileOutline gridol;
    private Coord lasttc = Coord.z;
    private long lastmmhittest = System.currentTimeMillis();
    private Coord lasthittestc = Coord.z;
    public AreaMine areamine;
    private GobSelectCallback gobselcb;
    private AreaSelectCallback areaselcb;
    private Pathfinder pf;
    public Thread pfthread;
    public SteelRefueler steelrefueler;
    private Thread musselPicker;
    public static final Set<Long> markedGobs = new HashSet<>();
    public Gob lastItemactGob;
    private int lastItemactMeshId;

    public interface Grabber {
        boolean mmousedown(Coord mc, int button);

        boolean mmouseup(Coord mc, int button);

        boolean mmousewheel(Coord mc, int amount);

        void mmousemove(Coord mc);
    }

    @RName("mapview")
    public static class $_ implements Factory {
        public Widget create(Widget parent, Object[] args) {
            Coord sz = (Coord) args[0];
            Coord2d mc = ((Coord)args[1]).mul(posres);
            int pgob = -1;
            if (args.length > 2)
                pgob = (Integer) args[2];
            return (new MapView(sz, parent.ui.sess.glob, mc, pgob));
        }
    }

    public MapView(Coord sz, Glob glob, Coord2d cc, long plgob) {
        super(sz);
        this.glob = glob;
        this.cc = cc;
        this.plgob = plgob;
        this.gridol = new TileOutline(glob.map);
        setcanfocus(true);
        markedGobs.clear();

        RunStateChangeEvent event = new RunStateChangeEvent(RunState.INGAME);
        new Mod().getAPI().callEvent(event);
    }

    public boolean visol(int ol) {
        return (visol[ol] > 0);
    }

    public void enol(int... overlays) {
        for (int ol : overlays)
            visol[ol]++;
    }

    public void disol(int... overlays) {
        for (int ol : overlays)
            visol[ol]--;
    }

    public static class ChangeSet implements OCache.ChangeCallback {
        public final Set<Gob> changed = new HashSet<Gob>();
        public final Set<Gob> removed = new HashSet<Gob>();

        public void changed(Gob ob) {
            changed.add(ob);
        }

        public void removed(Gob ob) {
            changed.remove(ob);
            removed.add(ob);
        }
    }

    public String toString() {
        return(String.format("Camera[%s (%s)], Caches[%s]", getcc()));
    }

    private Coord3f smapcc = null;
    private long lsmch = 0;

    public Gob player() {
        return (glob.oc.getgob(plgob));
    }

    public Coord3f getcc() {
        Gob pl = player();
        if (pl != null)
            return (pl.getc());
        else
            return(glob.map.getzp(cc));
    }

    public static class ClickInfo {
        public final ClickInfo from;
        public final Gob gob;
        public final Integer id;

        private ClickInfo(ClickInfo from, Gob gob, Integer id) {
            this.from = from; this.gob = gob; this.id = id;
        }

        public ClickInfo(ClickInfo prev, Integer id) {
            this(prev, prev.gob, id);
        }

        public ClickInfo() {
            this(null, null, null);
        }

        public boolean equals(Object obj) {
            if(!(obj instanceof ClickInfo))
                return(false);
            ClickInfo o = (ClickInfo)obj;
            return((gob == o.gob) && (id == o.id));
        }

        public int hashCode() {
            return((((System.identityHashCode(gob) * 31)) * 31) + System.identityHashCode(id));
        }

        public String toString() {
            return(String.format("<%s %s %s %x>", getClass(), gob, (id == null)?-1:id));
        }

        public int clickid() {
            return((id == null)?-1:id);
        }
    }

    static class PolText {
        Text text; long tm;
        PolText(Text text, long tm) {this.text = text; this.tm = tm;}
    }

    private static final Text.Furnace polownertf = new PUtils.BlurFurn(new Text.Foundry(Text.serif, 30).aa(true), 3, 1, Color.BLACK);
    private final Map<Integer, PolText> polowners = new HashMap<Integer, PolText>();


    public void setpoltext(int id, String text) {
        synchronized(polowners) {
            polowners.put(id, new PolText(polownertf.render(text), System.currentTimeMillis()));
        }
    }

    private Loading camload = null, lastload = null;

    public void tick(double dt) {
        camload = null;
        try {
            if ((shake = shake * Math.pow(100, -dt)) < 0.01)
                shake = 0;
            camoff.x = (float) ((Math.random() - 0.5) * shake);
            camoff.y = (float) ((Math.random() - 0.5) * shake);
            camoff.z = (float) ((Math.random() - 0.5) * shake);
        } catch (Loading e) {
            camload = e;
        }
        if (placing != null)
            placing.ctick((int) (dt * 1000));
    }

    public void resize(Coord sz) {
        super.resize(sz);
    }

    public static interface PlobAdjust {
        public void adjust(Plob plob, Coord pc, Coord2d mc, int modflags);

        public boolean rotate(Plob plob, int amount, int modflags);
    }

    public static class StdPlace implements PlobAdjust {
        boolean freerot = false;
        Coord2d gran = (plobgran == 0)?null:new Coord2d(1.0 / plobgran, 1.0 / plobgran).mul(tilesz);

        public void adjust(Plob plob, Coord pc, Coord2d mc, int modflags) {
            if ((modflags & 2) == 0)
                plob.rc = mc.floor(tilesz).mul(tilesz).add(tilesz.div(2));
            else if(gran != null)
                plob.rc = mc.add(gran.div(2)).floor(gran).mul(gran);
            else
                plob.rc = mc;
            Gob pl = plob.mv().player();
            if ((pl != null) && !freerot)
                plob.a = Math.round(plob.rc.angle(pl.rc) / (Math.PI / 2)) * (Math.PI / 2);
        }

        public boolean rotate(Plob plob, int amount, int modflags) {
            if ((modflags & 1) == 0)
                return (false);
            freerot = true;
            if ((modflags & 2) == 0)
                plob.a = (Math.PI / 4) * Math.round((plob.a + (amount * Math.PI / 4)) / (Math.PI / 4));
            else
                plob.a += amount * Math.PI / 16;
            plob.a = Utils.cangle(plob.a);
            return (true);
        }
    }

    public class Plob extends Gob {
        public PlobAdjust adjust = new StdPlace();
        Coord lastmc = null;

        private Plob(Indir<Resource> res, Message sdt) {
            super(MapView.this.glob, Coord2d.z);
            setattr(new ResDrawable(this, res, sdt));
        }

        public MapView mv() {
            return (MapView.this);
        }

    }

    private int olflash;
    private long olftimer;

    private void unflashol() {
        for (int i = 0; i < visol.length; i++) {
            if ((olflash & (1 << i)) != 0)
                visol[i]--;
        }
        olflash = 0;
        olftimer = 0;
    }

    public void uimsg(String msg, Object... args) {
        if (msg == "place") {
            int a = 0;
            Indir<Resource> res = ui.sess.getres((Integer) args[a++]);
            Message sdt;
            if ((args.length > a) && (args[a] instanceof byte[]))
                sdt = new MessageBuf((byte[]) args[a++]);
            else
                sdt = Message.nil;
            placing = new Plob(res, sdt);
            while (a < args.length) {
                Indir<Resource> ores = ui.sess.getres((Integer) args[a++]);
                Message odt;
                if ((args.length > a) && (args[a] instanceof byte[]))
                    odt = new MessageBuf((byte[]) args[a++]);
                else
                    odt = Message.nil;
            }
        } else if (msg == "unplace") {
            placing = null;
        } else if (msg == "move") {
            cc = ((Coord)args[0]).mul(posres);
        } else if (msg == "plob") {
            if (args[0] == null)
                plgob = -1;
            else
                plgob = (Integer) args[0];
        } else if (msg == "flashol") {
            unflashol();
            olflash = (Integer) args[0];
            for (int i = 0; i < visol.length; i++) {
                if ((olflash & (1 << i)) != 0)
                    visol[i]++;
            }
            olftimer = System.currentTimeMillis() + (Integer) args[1];
        } else if (msg == "sel") {
            boolean sel = ((Integer) args[0]) != 0;
            synchronized (this) {
                if (sel && (selection == null)) {
                    selection = new Selector();
                } else if (!sel && (selection != null)) {
                    selection.destroy();
                    selection = null;
                }
            }
        } else if (msg == "shake") {
            shake = ((Number) args[0]).doubleValue();
        } else {
            super.uimsg(msg, args);
        }
    }

    private UI.Grab camdrag = null;

    public void registerGobSelect(GobSelectCallback callback) {
        this.gobselcb = callback;
    }

    public void unregisterGobSelect() {
        this.gobselcb = null;
    }

    public void registerAreaSelect(AreaSelectCallback callback) {
        this.areaselcb = callback;
    }

    public void unregisterAreaSelect() {
        this.areaselcb = null;
    }

    public Pathfinder pfLeftClick(Coord mc, String action) {
        Gob player = player();
        if (player == null)
            return null;
        synchronized (Pathfinder.class) {
            if (pf != null) {
                pf.terminate = true;
                pfthread.interrupt();
                // cancel movement
                if (player.getattr(Moving.class) != null)
                    wdgmsg("gk", 27);
            }

            Coord src = player.rc.floor();
            int gcx = haven.pathfinder.Map.origin - (src.x - mc.x);
            int gcy = haven.pathfinder.Map.origin - (src.y - mc.y);
            if (gcx < 0 || gcx >= haven.pathfinder.Map.sz || gcy < 0 || gcy >= haven.pathfinder.Map.sz)
                return null;

            pf = new Pathfinder(this, new Coord(gcx, gcy), action);
            pf.addListener(this);
            pfthread = new Thread(pf, "Pathfinder");
            pfthread.start();
            return pf;
        }
    }

    public Pathfinder pfRightClick(Gob gob, int meshid, int clickb, int modflags, String action) {
        Gob player = player();
        if (player == null)
            return null;
        synchronized (Pathfinder.class) {
            if (pf != null) {
                pf.terminate = true;
                pfthread.interrupt();
                // cancel movement
                if (player.getattr(Moving.class) != null)
                    wdgmsg("gk", 27);
            }

            Coord src = player.rc.floor();
            int gcx = haven.pathfinder.Map.origin - (src.x - gob.rc.floor().x);
            int gcy = haven.pathfinder.Map.origin - (src.y - gob.rc.floor().y);
            if (gcx < 0 || gcx >= haven.pathfinder.Map.sz || gcy < 0 || gcy >= haven.pathfinder.Map.sz)
                return null;

            pf = new Pathfinder(this, new Coord(gcx, gcy), gob, meshid, clickb, modflags, action);
            pf.addListener(this);
            pfthread = new Thread(pf, "Pathfinder");
            pfthread.start();
            return pf;
        }
    }

    public void pfDone(final Pathfinder thread) {
        if (haven.pathfinder.Map.DEBUG_TIMINGS)
            System.out.println("-= PF DONE =-");
    }

    public void grab(Grabber grab) {
        this.grab = grab;
    }

    public void release(Grabber grab) {

        if (this.grab == grab)
            this.grab = null;
    }

    public boolean mousedown(Coord c, int button) {
        return (true);
    }

    public void mousemove(Coord c) {
    }

    public boolean mouseup(Coord c, int button) {
        return true;
    }

    public boolean mousewheel(Coord c, int amount) {
        return false;
    }

    public boolean drop(final Coord cc, final Coord ul) {
        return (true);
    }

    public boolean iteminteract(Coord cc, Coord ul) {
        return (true);
    }

    public void iteminteractreplay() {
        Coord grc = lastItemactGob.rc.floor(posres);
        wdgmsg("itemact", Coord.z, lastItemactGob.rc.floor(posres) , 1, 0, (int) lastItemactGob.id, grc, 0, lastItemactMeshId);
    }

    public boolean keydown(KeyEvent ev) {
        return (super.keydown(ev));
    }

    public boolean globtype(char c, KeyEvent ev) {
        return (false);
    }

    public Object tooltip(Coord c, Widget prev) {
        if (selection != null) {
            if (selection.tt != null)
                return (selection.tt);
        } else if (tooltip != null && ui.modshift) {
            return Text.render(tooltip);
        }
        return (super.tooltip(c, prev));
    }

    public class GrabXL implements Grabber {
        private final Grabber bk;
        public boolean mv = false;

        public GrabXL(Grabber bk) {
            this.bk = bk;
        }

        public boolean mmousedown(Coord cc, final int button) {
            return (true);
        }

        public boolean mmouseup(Coord cc, final int button) {
            return (true);
        }

        public boolean mmousewheel(Coord cc, final int amount) {
            return (true);
        }

        public void mmousemove(Coord cc) {
            if (mv) {
            }
        }
    }

    private class Selector implements Grabber {
        Coord sc;
        MCache.Overlay ol;
        UI.Grab mgrab;
        int modflags;
        Text tt;
        MapView mv;
        final GrabXL xl = new GrabXL(this) {
            public boolean mmousedown(Coord cc, int button) {
                if (button != 1)
                    return (false);
                return (super.mmousedown(cc, button));
            }

            public boolean mmousewheel(Coord cc, int amount) {
                return (false);
            }
        };

        public Selector() {
        }

        public Selector(MapView mv) {
            this.mv = mv;
        }

        {
            grab(xl);
            enol(17);
        }

        public boolean mmousedown(Coord mc, int button) {
            synchronized (MapView.this) {
                if (selection != this)
                    return (false);
                if (sc != null) {
                    ol.destroy();
                    mgrab.remove();
                }
                sc = mc.div(MCache.tilesz2);
                modflags = ui.modflags();
                xl.mv = true;
                mgrab = ui.grabmouse(MapView.this);
                synchronized (glob.map.grids) {
                    ol = glob.map.new Overlay(sc, sc, 1 << 17);
                }
                return (true);
            }
        }

        public boolean mmouseup(Coord mc, int button) {
            synchronized (MapView.this) {
                if (sc != null) {
                    Coord ec = mc.div(MCache.tilesz2);
                    xl.mv = false;
                    tt = null;
                    ol.destroy();
                    mgrab.remove();
                    if (mv != null) {
                        if (areaselcb != null) {
                            areaselcb.areaselect(ol.getc1(), ol.getc2());
                        } else { //  TODO: should reimplement miner to use callbacks
                            areamine = new AreaMine(ol.getc1(), ol.getc2(), mv);
                            new Thread(areamine, "Area miner").start();
                            if (selection != null) {
                                selection.destroy();
                                selection = null;
                            }
                        }
                    } else {
                        wdgmsg("sel", sc, ec, modflags);
                    }
                    sc = null;
                }
                return (true);
            }
        }

        public boolean mmousewheel(Coord mc, int amount) {
            return (false);
        }

        public void mmousemove(Coord mc) {
            synchronized (MapView.this) {
                if (sc != null) {
                    Coord tc = mc.div(MCache.tilesz2);
                    Coord c1 = new Coord(Math.min(tc.x, sc.x), Math.min(tc.y, sc.y));
                    Coord c2 = new Coord(Math.max(tc.x, sc.x), Math.max(tc.y, sc.y));
                    ol.update(c1, c2);
                    tt = Text.render(String.format("%d\u00d7%d", c2.x - c1.x + 1, c2.y - c1.y + 1));
                }
            }
        }

        public void destroy() {
            synchronized (MapView.this) {
                if (sc != null) {
                    ol.destroy();
                    mgrab.remove();
                }
                release(xl);
                disol(17);
            }
        }
    }

    private Map<String, Console.Command> cmdmap = new TreeMap<String, Console.Command>();

    {
        Console.setscmd("placegrid", new Console.Command() {
            public void run(Console cons, String[] args) {
                if ((plobgran = Integer.parseInt(args[1])) < 0)
                    plobgran = 0;
            }
        });
        cmdmap.put("whyload", (cons, args) -> {
            Loading l = lastload;
            if (l == null)
                throw (new Exception("Not loading"));
            l.printStackTrace(cons.out);
        });
        Console.setscmd("clickdb", (cons, args) -> clickdb = Utils.parsebool(args[1], false));
    }

    public Map<String, Console.Command> findcmds() {
        return (cmdmap);
    }

    public void togglegrid() {
        showgrid = !showgrid;
        if (showgrid) {
            Coord tc = new Coord((int) (cc.x / tilesz.x / MCache.cutsz.x - view - 1) * MCache.cutsz.x,
                    (int) (cc.y / tilesz.y / MCache.cutsz.y - view - 1) * MCache.cutsz.y);
            lasttc = tc;
            gridol.update(tc);
        }
    }

    public void aggroclosest() {
        OCache oc = ui.sess.glob.oc;
        synchronized (oc) {
            Gob gobcls = null;
            double gobclsdist = Double.MAX_VALUE;

            for (Gob gob : oc) {
                try {
                    Resource res = gob.getres();
                    if (res != null && "body".equals(res.basename()) && gob.id != player().id) {
                        if (!gob.isFriend()) {
                            double dist = player().rc.dist(gob.rc);
                            if (dist < gobclsdist) {
                                gobcls = gob;
                                gobclsdist = dist;
                            }
                        }
                    }
                } catch (Loading l) {
                }
            }

            if (gobcls != null) {
                gameui().act("aggro");
                wdgmsg("click", gobcls.sc, Coord.z, 1, ui.modflags(), 0, (int) gobcls.id, gobcls.rc.floor(posres), 0, 0);
                Gob pl = player();
                wdgmsg("click", pl.sc, pl.rc.floor(posres), 3, 0);
            }
        }
    }

    public void canceltasks() {
        if (pf != null)
            pf.terminate = true;
        if (areamine != null)
            areamine.terminate();
        if (steelrefueler != null)
            steelrefueler.terminate();
        if (musselPicker != null)
            musselPicker.interrupt();
    }

    public void refreshGobsAll() {
        OCache oc = glob.oc;
        synchronized (oc) {
            for (Gob gob : oc)
                oc.changed(gob);
        }
    }

    public void refreshGobsHidable() {
        OCache oc = glob.oc;
        synchronized (oc) {
            for (Gob gob : oc) {
                if (gob.type == Gob.Type.TREE)
                    oc.changed(gob);
            }
        }
    }

    public void refreshGobsGrowthStages() {
        OCache oc = glob.oc;
        synchronized (oc) {
            for (Gob gob : oc) {
                if (Gob.Type.PLANT.has(gob.type) || gob.type == Gob.Type.TREE || gob.type == Gob.Type.BUSH)
                    oc.changed(gob);
            }
        }
    }

    public void startMusselsPicker(Gob gob) {
        musselPicker = new Thread(new MusselPicker(gameui(), gob), "MusselPicker");
        musselPicker.start();
    }
}
