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

import java.nio.ShortBuffer;
import java.util.*;

import static haven.MCache.tilesz;

public class MapMesh implements Disposable {
    public final Coord ul, sz;
    public final MCache map;
    private final long rnd;
    private Map<DataID, Object> data = new LinkedHashMap<DataID, Object>();
    private FastMesh[] flats;
    private List<Disposable> dparts = new ArrayList<Disposable>();

    public interface DataID<T> {
        public T make(MapMesh m);
    }

    public static <T> DataID<T> makeid(Class<T> cl) {
        try {
            final java.lang.reflect.Constructor<T> cons = cl.getConstructor(MapMesh.class);
            return (new DataID<T>() {
                public T make(MapMesh m) {
                    return (Utils.construct(cons, m));
                }
            });
        } catch (NoSuchMethodException e) {
        }
        try {
            final java.lang.reflect.Constructor<T> cons = cl.getConstructor();
            return (new DataID<T>() {
                public T make(MapMesh m) {
                    return (Utils.construct(cons));
                }
            });
        } catch (NoSuchMethodException e) {
        }
        throw (new Error("No proper data-ID constructor found"));
    }

    @SuppressWarnings("unchecked")
    public <T> T data(DataID<T> id) {
        T ret = (T) data.get(id);
        if (ret == null)
            data.put(id, ret = id.make(this));
        return (ret);
    }

    public static class Scan {
        public final Coord ul, sz, br;
        public final int l;

        public Scan(Coord ul, Coord sz) {
            this.ul = ul;
            this.sz = sz;
            this.br = sz.add(ul);
            this.l = sz.x * sz.y;
        }

        public int o(int x, int y) {
            return ((x - ul.x) + ((y - ul.y) * sz.x));
        }

        public int o(Coord in) {
            return (o(in.x, in.y));
        }
    }

    public class MapSurface extends haven.Surface {
        public final Scan vs = new Scan(new Coord(-1, -1), sz.add(3, 3));
        public final Scan ts = new Scan(Coord.z, sz);
        public final Vertex[] surf = new Vertex[vs.l];
        public final boolean[] split = new boolean[ts.l];

        public MapSurface() {
            for (int y = vs.ul.y; y < vs.br.y; y++) {
                for (int x = vs.ul.x; x < vs.br.x; x++) {
                    surf[vs.o(x, y)] = new Vertex(x * (float)tilesz.x, y * -(float)tilesz.y, Config.disableelev ? 0 : map.getz(ul.add(x, y)));
                }
            }
            for (int y = ts.ul.y; y < ts.br.y; y++) {
                for (int x = ts.ul.x; x < ts.br.x; x++) {
                    split[ts.o(x, y)] = Math.abs(surf[vs.o(x, y)].z - surf[vs.o(x + 1, y + 1)].z) > Math.abs(surf[vs.o(x + 1, y)].z - surf[vs.o(x, y + 1)].z);
                }
            }
        }

        public Vertex fortile(Coord c) {
            return (surf[vs.o(c)]);
        }

        public Vertex[] fortilea(Coord c) {
            return (new Vertex[]{
                    surf[vs.o(c.x, c.y)],
                    surf[vs.o(c.x, c.y + 1)],
                    surf[vs.o(c.x + 1, c.y + 1)],
                    surf[vs.o(c.x + 1, c.y)],
            });
        }

        public void sfin() {
            fin();
        }

        public void calcnrm() {
        }

        public void postcalcnrm(Random rnd) {
        }

        public boolean clean() {
            return (true);
        }
    }

    public static final DataID<MapSurface> gnd = makeid(MapSurface.class);

    private MapMesh(MCache map, Coord ul, Coord sz, Random rnd) {
        this.map = map;
        this.ul = ul;
        this.sz = sz;
        this.rnd = rnd.nextLong();
    }

    public Random rnd() {
        return (new Random(this.rnd));
    }

    public Random rnd(Coord c) {
        Random ret = rnd();
        ret.setSeed(ret.nextInt() + c.x);
        ret.setSeed(ret.nextInt() + c.y);
        return (ret);
    }

    public static Random grnd(Coord c) {
        Random ret = new Random(1192414289);
        ret.setSeed(ret.nextInt() + c.x);
        ret.setSeed(ret.nextInt() + c.y);
        return (ret);
    }

    private static void dotrans(MapMesh m, Random rnd, Coord lc, Coord gc) {
        Tiler ground = m.map.tiler(m.map.gettile(gc));
        int tr[][] = new int[3][3];
        int max = -1;
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                if ((x == 0) && (y == 0))
                    continue;
                int tn = m.map.gettile(gc.add(x, y));
                tr[x + 1][y + 1] = tn;
                if (tn > max)
                    max = tn;
            }
        }
        int bx[] = {0, 1, 2, 1};
        int by[] = {1, 0, 1, 2};
        int cx[] = {0, 2, 2, 0};
        int cy[] = {0, 0, 2, 2};
        for (int i = max; i >= 0; i--) {
            int bm = 0, cm = 0;
            for (int o = 0; o < 4; o++) {
                if (tr[bx[o]][by[o]] == i)
                    bm |= 1 << o;
            }
            for (int o = 0; o < 4; o++) {
                if ((bm & ((1 << o) | (1 << ((o + 1) % 4)))) != 0)
                    continue;
                if (tr[cx[o]][cy[o]] == i)
                    cm |= 1 << o;
            }
            if ((bm != 0) || (cm != 0)) {
                Tiler t = m.map.tiler(i);
                if (t == null)
                    continue;
            }
        }
    }

    public static MapMesh build(MCache mc, Random rnd, Coord ul, Coord sz) {
        MapMesh m = new MapMesh(mc, ul, sz, rnd);
        Coord c = new Coord();
        rnd = m.rnd();

        for (c.y = 0; c.y < sz.y; c.y++) {
            for (c.x = 0; c.x < sz.x; c.x++) {
                Coord gc = c.add(ul);
                long ns = rnd.nextLong();
                mc.tiler(mc.gettile(gc)).model(m, rnd, c, gc);
                rnd.setSeed(ns);
            }
        }
        for (c.y = 0; c.y < sz.y; c.y++) {
            for (c.x = 0; c.x < sz.x; c.x++) {
                Coord gc = c.add(ul);
                long ns = rnd.nextLong();
                if (!Config.disabletiletrans)
                    dotrans(m, rnd, c, gc);
                rnd.setSeed(ns);
            }
        }

        m.consflat();

        m.clean();
        return (m);
    }

    public static class GroundMod implements Disposable {
        public final Coord2d cc;
        public final FastMesh mesh;

        public GroundMod(MCache map, final Coord2d cc, final Coord3f ul, final Coord3f br, double a) {
            final float si = (float) Math.sin(a), co = (float) Math.cos(a);
            this.cc = cc;
            final MeshBuf buf = new MeshBuf();
            final float cz = Config.disableelev ? 0 : (float)map.getcz(cc);
            final Coord ult, brt;
            {
                Coord tult = null, tbrt = null;
                for (Coord3f corn : new Coord3f[]{ul, new Coord3f(ul.x, br.y, 0), br, new Coord3f(br.x, ul.y, 0)}) {
                    float cx = (float)((cc.x + co * corn.x - si * corn.y) / tilesz.x);
                    float cy = (float)((cc.y + co * corn.y + si * corn.x) / tilesz.y);
                    if (tult == null) {
                        tult = new Coord((int) Math.floor(cx), (int) Math.floor(cy));
                        tbrt = new Coord((int) Math.ceil(cx), (int) Math.ceil(cy));
                    } else {
                        tult.x = Math.min(tult.x, (int) Math.floor(cx));
                        tult.y = Math.min(tult.y, (int) Math.floor(cy));
                        tbrt.x = Math.max(tbrt.x, (int) Math.ceil(cx));
                        tbrt.y = Math.max(tbrt.y, (int) Math.ceil(cy));
                    }
                }
                ult = tult;
                brt = tbrt;
            }

            Coord t = new Coord();
            for (t.y = ult.y; t.y < brt.y; t.y++) {
                for (t.x = ult.x; t.x < brt.x; t.x++) {
                    MapMesh cut = map.getcut(t.div(MCache.cutsz));
                    Tiler tile = map.tiler(map.gettile(t));
                }
            }
            mesh = buf.mkmesh();
        }

        public void dispose() {
            mesh.dispose();
        }
    }

    private void clean() {
        int on = data.size();
        for (Iterator<Map.Entry<DataID, Object>> i = data.entrySet().iterator(); i.hasNext(); ) {
            Object d = i.next().getValue();
        }
    }

    private void consflat() {
        class Buf {
            int vn = 0, in = 0, vl = sz.x * sz.y * 4;
            float[] pos = new float[vl * 3];
            float[] col = new float[vl * 4];
            short[] ind = new short[sz.x * sz.y * 6];

            public void faces(MapMesh m, Tiler.MPart d) {
                if (vn + d.v.length > vl) {
                    vl *= 2;
                    pos = Utils.extend(pos, vl * 12);
                    col = Utils.extend(col, vl * 16);
                }
                float cx = (d.lc.x + 1) / 256.0f, cy = (d.lc.y + 1) / 256.0f;
                for (int i = 0; i < d.v.length; i++) {
                    int pb = (vn + i) * 3, cb = (vn + i) * 4;
                    pos[pb + 0] = d.v[i].x;
                    pos[pb + 1] = d.v[i].y;
                    pos[pb + 2] = d.v[i].z;
                    col[cb + 0] = cx;
                    col[cb + 1] = cy;
                    col[cb + 2] = d.tcx[i];
                    col[cb + 3] = d.tcy[i];
                }
                if (in + d.f.length > ind.length)
                    ind = Utils.extend(ind, ind.length * 2);
                for (int fi : d.f)
                    ind[in++] = (short) (vn + fi);
                vn += d.v.length;
            }
        }
        Buf buf = new Buf();
        Coord c = new Coord();
        for (c.y = 0; c.y < sz.y; c.y++) {
            for (c.x = 0; c.x < sz.x; c.x++) {
                Coord gc = c.add(ul);
            }
        }
        float[] pos = buf.pos, col = buf.col;
        short[] ind = buf.ind;
        if (pos.length != buf.vn * 3) pos = Utils.extend(pos, buf.vn * 3);
        if (col.length != buf.vn * 4) col = Utils.extend(col, buf.vn * 4);
        if (ind.length != buf.in) ind = Utils.extend(ind, buf.in);
        ShortBuffer indb = ShortBuffer.wrap(ind);
        flats = new FastMesh[]{
        };
    }

    public void dispose() {
        for (Disposable p : dparts)
            p.dispose();
    }
}
