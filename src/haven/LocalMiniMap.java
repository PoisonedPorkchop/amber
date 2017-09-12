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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static haven.MCache.tilesz;
import static haven.OCache.posres;

public class LocalMiniMap extends Widget {
    private static final Tex resize = Resource.loadtex("gfx/hud/wndmap/lg/resize");
    private static final Tex gridblue = Resource.loadtex("gfx/hud/mmap/gridblue");
    private static final Tex gridred = Resource.loadtex("gfx/hud/mmap/gridred");
    public final MapView mv;
    public final MapFile save;
    private Coord cc = null;
    public MapTile cur = null;
    private UI.Grab dragging;
    private Coord doff = Coord.z;
    private Coord delta = Coord.z;
	private static final Resource alarmplayersfx = Resource.local().loadwait("sfx/alarmplayer");
    private static final Resource foragablesfx = Resource.local().loadwait("sfx/awwyeah");
    private static final Resource bearsfx = Resource.local().loadwait("sfx/bear");
    private static final Resource lynxfx = Resource.local().loadwait("sfx/lynx");
    private static final Resource walrusfx = Resource.local().loadwait("sfx/walrus");
    private static final Resource trollsfx = Resource.local().loadwait("sfx/troll");
    private static final Resource mammothsfx = Resource.local().loadwait("sfx/mammoth");
    private static final Resource doomedsfx = Resource.local().loadwait("sfx/doomed");
    private static final Resource swagsfx = Resource.local().loadwait("sfx/swag");
	private final HashSet<Long> sgobs = new HashSet<Long>();
    private final Map<Coord, Tex> maptiles = new LinkedHashMap<Coord, Tex>(100, 0.75f, false) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Coord, Tex> eldest) {
            if (size() > 100) {
                try {
                    eldest.getValue().dispose();
                } catch (RuntimeException e) {
                }
                return true;
            }
            return false;
        }

        @Override
        public void clear() {
            values().forEach(Tex::dispose);
            super.clear();
        }
    };
    private final Map<Pair<MCache.Grid, Integer>, Defer.Future<MapTile>> cache = new LinkedHashMap<Pair<MCache.Grid, Integer>, Defer.Future<MapTile>>(7, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry<Pair<MCache.Grid, Integer>, Defer.Future<MapTile>> eldest) {
            return size() > 7;
        }
    };
    private final static Tex bushicn = Text.renderstroked("\u22C6", Color.CYAN, Color.BLACK, Text.num12boldFnd).tex();
    private final static Tex treeicn = Text.renderstroked("\u25B2", Color.CYAN, Color.BLACK, Text.num12boldFnd).tex();
    private final static Tex bldricn = Text.renderstroked("\u25AA", Color.CYAN, Color.BLACK, Text.num12boldFnd).tex();
    private Map<Color, Tex> xmap = new HashMap<Color, Tex>(6);
    public static Coord plcrel = null;
    public long lastnewgid;


    public static class MapTile {
        public MCache.Grid grid;
        public int seq;

        public MapTile(MCache.Grid grid, int seq) {
            this.grid = grid;
            this.seq = seq;
        }
    }

    private BufferedImage tileimg(int t, BufferedImage[] texes) {
        BufferedImage img = texes[t];
        if (img == null) {
            Resource r = ui.sess.glob.map.tilesetr(t);
            if (r == null)
                return (null);
            Resource.Image ir = r.layer(Resource.imgc);
            if (ir == null)
                return (null);
            img = ir.img;
            texes[t] = img;
        }
        return (img);
    }

    public LocalMiniMap(Coord sz, MapView mv) {
        super(sz);
        this.mv = mv;
        if(ResCache.global != null) {
            save = MapFile.load(ResCache.global);
        } else {
            save = null;
        }
    }

    public Coord p2c(Coord2d pc) {
        return (pc.floor(tilesz).sub(cc).add(sz.div(2)));
    }

    public Coord2d c2p(Coord c) {
        return (c.sub(sz.div(2)).add(cc).mul(tilesz).add(tilesz.div(2)));
    }

    public Gob findicongob(Coord c) {
        OCache oc = ui.sess.glob.oc;
        synchronized (oc) {
            for (Gob gob : oc) {
                try {
                    GobIcon icon = gob.getattr(GobIcon.class);
                    if (icon != null) {
                        Coord gc = p2c(gob.rc);
                        Coord sz = icon.tex().sz();
                        if (c.isect(gc.sub(sz.div(2)), sz)) {
                            Resource res = icon.res.get();
                            CheckListboxItem itm = Config.icons.get(res.basename());
                            if (itm == null || !itm.selected)
                                return gob;
                        }
                    } else { // custom icons
                        Coord gc = p2c(gob.rc);
                        Coord sz = new Coord(18, 18);
                        if (c.isect(gc.sub(sz.div(2)), sz)) {
                            Resource res = gob.getres();
                            if (res != null && Config.additonalicons.containsKey(res.name)) {
                                CheckListboxItem itm = Config.icons.get(res.basename());
                                if (itm == null || !itm.selected)
                                    return gob;
                            }
                        }
                    }
                } catch (Loading l) {
                }
            }
        }
        return (null);
    }

    public void tick(double dt) {
        Gob pl = ui.sess.glob.oc.getgob(mv.plgob);
        if(pl == null)
            this.cc = mv.cc.floor(tilesz);
        else
            this.cc = pl.rc.floor(tilesz);

        if (Config.playerposfile != null && MapGridSave.gul != null) {
            try {
                // instead of synchronizing MapGridSave.gul we just handle NPE
             //   plcrel = pl.rc.sub((MapGridSave.gul.x + 50) * tilesz.x, (MapGridSave.gul.y + 50) * tilesz.y);
            } catch (NullPointerException npe) {
            }
        }
    }

    public void center() {
        delta = Coord.z;
    }

    public boolean mousedown(Coord c, int button) {
        if (button != 2) {
            if (cc == null)
                return false;
            Coord csd = c.sub(delta);
            Coord2d mc = c2p(csd);
            if (button == 1)
                MapView.pllastcc = mc;
            Gob gob = findicongob(csd);
            if (gob == null) {
                mv.wdgmsg("click", rootpos().add(csd), mc.floor(posres), button, ui.modflags());
            } else {
                mv.wdgmsg("click", rootpos().add(csd), mc.floor(posres), button, ui.modflags(), 0, (int) gob.id, gob.rc.floor(posres), 0, -1);
                if (Config.autopickmussels && gob.type == Gob.Type.MUSSEL)
                    mv.startMusselsPicker(gob);
            }
        } else if (button == 2) {
            doff = c;
            dragging = ui.grabmouse(this);
        }
        return true;
    }

    public void mousemove(Coord c) {
        if (dragging != null) {
            delta = delta.add(c.sub(doff));
            doff = c;
        }
    }

    public boolean mouseup(Coord c, int button) {
        if (dragging != null) {
            dragging.remove();
            dragging = null;
        }
        return (true);
    }
}
