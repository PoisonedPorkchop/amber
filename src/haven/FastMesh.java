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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FastMesh implements Disposable{
    public FastMesh from;
    private Compiler compiler;
    private Coord3f nb, pb;

    public FastMesh(ShortBuffer ind) {
        int lo = 65536, hi = 0;
        for(int i = 0; i < ind.capacity(); i++) {
            int idx = ((int)ind.get(i)) & 0xffff;
            lo = Math.min(lo, idx);
            hi = Math.max(hi, idx);
        }
    }
    public FastMesh(short[] ind) {
        this(Utils.bufcp(ind));
    }


    public FastMesh(FastMesh from) {
        this.from = from;
    }

    public static abstract class Compiled {
        public abstract void dispose();
    }

    public abstract class Compiler {
        private Entry[] cache = new Entry[0];

        private class Entry {
            Compiled mesh;
            Object id;
        }

        public void dispose() {
            for(Entry ent : cache)
                ent.mesh.dispose();
            cache = new Entry[0];
        }
    }

    public class DLCompiler extends Compiler {
    }

    private void cbounds() {
        Coord3f nb = null, pb = null;
        this.nb = nb;
        this.pb = pb;
    }

    public Coord3f nbounds() {
        if(nb == null) cbounds();
        return(nb);
    }
    public Coord3f pbounds() {
        if(pb == null) cbounds();
        return(pb);
    }

    protected boolean compile() {
        return(true);
    }

    /* XXX: One might start to question if it isn't about time to
     * dispose of display-list drawing. */

    public void dispose() {
        if(compiler != null) {
            compiler.dispose();
            compiler = null;
        }
    }

    public static class ResourceMesh extends FastMesh {
        public final int id;
        public final Resource res;

        public ResourceMesh(short[] ind, MeshRes info) {
            super(ind);
            this.id = info.id;
            this.res = info.getres();
        }

        public String toString() {
            return("FastMesh(" + res.name + ", " + id + ")");
        }
    }

    @Resource.LayerName("mesh")
    public static class MeshRes extends Resource.Layer implements Resource.IDLayer<Integer> {
        public transient FastMesh m;
        public transient Material.Res mat;
        public final Map<String, String> rdat;
        private transient short[] tmp;
        public final int id, ref;
        private int matid;

        public MeshRes(Resource res, Message buf) {
            res.super();
            int fl = buf.uint8();
            int num = buf.uint16();
            matid = buf.int16();
            if((fl & 2) != 0) {
                id = buf.int16();
            } else {
                id = -1;
            }
            if((fl & 4) != 0) {
                ref = buf.int16();
            } else {
                ref = -1;
            }
            Map<String, String> rdat = new HashMap<String, String>();
            if((fl & 8) != 0) {
                while(true) {
                    String k = buf.string();
                    if(k.equals(""))
                        break;
                    rdat.put(k, buf.string());
                }
            }
            this.rdat = Collections.unmodifiableMap(rdat);
            if((fl & ~15) != 0)
                throw(new Resource.LoadException("Unsupported flags in fastmesh: " + fl, getres()));
            short[] ind = new short[num * 3];
            for(int i = 0; i < num * 3; i++)
                ind[i] = (short)buf.uint16();
            this.tmp = ind;
        }

        public void init() {
            this.m = new ResourceMesh( this.tmp, this);
            this.tmp = null;
            if(matid >= 0) {
                for(Material.Res mr : getres().layers(Material.Res.class)) {
                    if(mr.id == matid)
                        this.mat = mr;
                }
                if(this.mat == null)
                    throw(new Resource.LoadException("Could not find specified material: " + matid, getres()));
            }
        }

        public Integer layerid() {
            return(id);
        }
    }
}
