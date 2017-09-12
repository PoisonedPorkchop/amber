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

import haven.Config;
import haven.Coord;
import haven.MapMesh;
import haven.MapMesh.Scan;
import haven.Resource.Tileset;
import haven.Tiler;

import java.util.Random;

public class TerrainTile extends Tiler {
    public final Tileset transset;

    public static class Var {
        public double thrl, thrh;
        public double nz;

        public Var(double thrl, double thrh, double nz) {
            this.thrl = thrl;
            this.thrh = thrh;
            this.nz = nz;
        }
    }

    private static final int sr = 12;

    public class Blend {
        final MapMesh m;
        final Scan vs, es;

        private Blend(MapMesh m) {
            this.m = m;
            vs = new Scan(Coord.z.sub(sr, sr), m.sz.add(sr * 2 + 1, sr * 2 + 1));
            es = new Scan(Coord.z, m.sz);
        }

        private void setbase(float[][] bv) {
            if (Config.disableterrainsmooth) {
                for (int y = vs.ul.y; y < vs.br.y - 1; y++) {
                    for (int x = vs.ul.x; x < vs.br.x - 1; x++) {
                        bv[0][vs.o(x, y)] = 1;
                        bv[0][vs.o(x + 1, y)] = 1;
                        bv[0][vs.o(x, y + 1)] = 1;
                        bv[0][vs.o(x + 1, y + 1)] = 1;
                    }
                }
            } else {
                for (int y = vs.ul.y; y < vs.br.y - 1; y++) {
                    for (int x = vs.ul.x; x < vs.br.x - 1; x++) {
                        fall:
                        {
                            bv[0][vs.o(x, y)] = 1;
                            bv[0][vs.o(x + 1, y)] = 1;
                            bv[0][vs.o(x, y + 1)] = 1;
                            bv[0][vs.o(x + 1, y + 1)] = 1;
                        }
                    }
                }
            }
        }
    }

    public final MapMesh.DataID<Blend> blend = new MapMesh.DataID<Blend>() {
        public Blend make(MapMesh m) {
            return (new Blend(m));
        }
    };

    public TerrainTile(int id, Tileset transset) {
        super(id);
        this.transset = transset;
    }

    public void trans(MapMesh m, Random rnd, Tiler gt, Coord lc, Coord gc, int z, int bmask, int cmask) {
        if (transset == null)
            return;
        if (m.map.gettile(gc) <= id)
            return;
    }

    public static class RidgeTile extends TerrainTile implements Ridges.RidgeTile {
        public final int rth;

        public RidgeTile(int id, Tileset transset, int rth, float texh) {
            super(id, transset);
            this.rth = rth;
        }

        public int breakz() {
            return (rth);
        }

        public void model(MapMesh m, Random rnd, Coord lc, Coord gc) {
            if (!m.data(Ridges.id).model(lc))
                super.model(m, rnd, lc, gc);
        }
    }
}
