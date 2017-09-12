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

package haven.resutil;

import haven.*;
import haven.MapMesh.Scan;
import haven.Surface.Vertex;

import java.util.Random;

public class WaterTile extends Tiler {
    public WaterTile(int id) {
        super(id);
    }

    public static class Bottom {
        final MapMesh m;
        final boolean[] s;
        final Vertex[] surf;
        final boolean[] split;
        int[] ed;
        final Scan vs, ss;

        public Bottom(MapMesh m) {
            this.m = m;
            MapMesh.MapSurface ms = m.data(m.gnd);
            this.vs = ms.vs;
            Scan ts = ms.ts;
            this.surf = new Vertex[vs.l];
            this.split = new boolean[ts.l];
            Coord sz = m.sz;
            MCache map = m.map;
            Scan ds = new Scan(new Coord(-10, -10), sz.add(21, 21));
            ss = new Scan(new Coord(-9, -9), sz.add(19, 19));
            int[] d = new int[ds.l];
            s = new boolean[ss.l];
            ed = new int[ss.l];

            for (int y = ss.ul.y; y < ss.br.y; y++) {
                for (int x = ss.ul.x; x < ss.br.x; x++) {
                    int td = d[ds.o(x, y)];
                    td = Math.min(td, d[ds.o(x - 1, y - 1)]);
                    td = Math.min(td, d[ds.o(x, y - 1)]);
                    td = Math.min(td, d[ds.o(x - 1, y)]);
                    ed[ss.o(x, y)] = td;
                    if (td == 0)
                        s[ss.o(x, y)] = true;
                }
            }
            for (int i = 0; i < 8; i++) {
                int[] sd = new int[ss.l];
                for (int y = ss.ul.y + 1; y < ss.br.y - 1; y++) {
                    for (int x = ss.ul.x + 1; x < ss.br.x - 1; x++) {
                        if (s[ss.o(x, y)]) {
                            sd[ss.o(x, y)] = ed[ss.o(x, y)];
                        } else {
                            sd[ss.o(x, y)] = ((ed[ss.o(x, y)] * 4) +
                                    ed[ss.o(x - 1, y)] + ed[ss.o(x + 1, y)] +
                                    ed[ss.o(x, y - 1)] + ed[ss.o(x, y + 1)]) / 8;
                        }
                    }
                }
                ed = sd;
            }
            for (int y = vs.ul.y; y < vs.br.y; y++) {
                for (int x = vs.ul.x; x < vs.br.x; x++) {
                    int vd = ed[ss.o(x, y)];
                    surf[vs.o(x, y)] = new BottomVertex(ms, ms.surf[vs.o(x, y)].add(0, 0, -vd), vd);
                }
            }
            for (int y = ts.ul.y; y < ts.br.y; y++) {
                for (int x = ts.ul.x; x < ts.br.x; x++) {
                    split[ts.o(x, y)] = Math.abs(surf[vs.o(x, y)].z - surf[vs.o(x + 1, y + 1)].z) > Math.abs(surf[vs.o(x + 1, y)].z - surf[vs.o(x, y + 1)].z);
                }
            }
        }

        public static class BottomVertex extends Vertex {
            public final float d;

            public BottomVertex(Surface surf, Coord3f c, float d) {
                surf.super(c);
                this.d = d;
            }
        }

        public int d(int x, int y) {
            return (ed[ss.o(x, y)]);
        }

        public Vertex[] fortilea(Coord c) {
            return (new Vertex[]{
                    surf[vs.o(c.x, c.y)],
                    surf[vs.o(c.x, c.y + 1)],
                    surf[vs.o(c.x + 1, c.y + 1)],
                    surf[vs.o(c.x + 1, c.y)],
            });
        }

        public static final MapMesh.DataID<Bottom> id = MapMesh.makeid(Bottom.class);
    }

    public void model(MapMesh m, Random rnd, Coord lc, Coord gc) {
        super.model(m, rnd, lc, gc);
        Bottom b = m.data(Bottom.id);
        MapMesh.MapSurface s = m.data(MapMesh.gnd);
        if (b.split[s.ts.o(lc)]) {
            s.new Face(b.surf[b.vs.o(lc.x, lc.y)],
                    b.surf[b.vs.o(lc.x, lc.y + 1)],
                    b.surf[b.vs.o(lc.x + 1, lc.y + 1)]);
            s.new Face(b.surf[b.vs.o(lc.x, lc.y)],
                    b.surf[b.vs.o(lc.x + 1, lc.y + 1)],
                    b.surf[b.vs.o(lc.x + 1, lc.y)]);
        } else {
            s.new Face(b.surf[b.vs.o(lc.x, lc.y)],
                    b.surf[b.vs.o(lc.x, lc.y + 1)],
                    b.surf[b.vs.o(lc.x + 1, lc.y)]);
            s.new Face(b.surf[b.vs.o(lc.x, lc.y + 1)],
                    b.surf[b.vs.o(lc.x + 1, lc.y + 1)],
                    b.surf[b.vs.o(lc.x + 1, lc.y)]);
        }
    }

    static final TexCube sky = new TexCube(Resource.loadimg("gfx/tiles/skycube"));
}
