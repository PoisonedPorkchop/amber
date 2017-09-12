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

import haven.MapView.ClickInfo;
import haven.Skeleton.Pose;
import haven.Skeleton.PoseMod;

import java.util.*;

public class Composited {
    public final Skeleton skel;
    public final Pose pose;
    public Collection<Equ> equ = new LinkedList<Equ>();
    public Poses poses = new Poses();
    public List<MD> nmod = null, cmod = new LinkedList<MD>();
    public List<ED> nequ = null, cequ = new LinkedList<ED>();
    public Sprite.Owner eqowner = null;

    public class Poses {
        public final PoseMod[] mods;
        Pose old;
        float ipold = 0.0f, ipol = 0.0f;
        public float limit = -1.0f;
        public boolean stat, ldone;
        private Random srnd = new Random();
        private float rsmod = (srnd.nextFloat() * 0.1f) + 0.95f;

        public Poses() {
            this.mods = new PoseMod[0];
        }

        public Poses(List<? extends PoseMod> mods) {
            this.mods = mods.toArray(new PoseMod[0]);
            stat = true;
            for (PoseMod mod : this.mods) {
                if (!mod.stat()) {
                    stat = false;
                    break;
                }
            }
        }

        private void rebuild() {
            pose.reset();
            for (PoseMod m : mods)
                m.apply(pose);
            if (ipold > 0.0f)
                pose.blend(old, ipold);
            pose.gbuild();
        }

        public void set(float ipol) {
            if ((this.ipol = ipol) > 0) {
                this.old = skel.new Pose(pose);
                this.ipold = 1.0f;
            }
            Composited.this.poses = this;
            rebuild();
        }

        public void tick(float dt) {
            rsmod = Utils.clip(rsmod + (srnd.nextFloat() * 0.005f) - 0.0025f, 0.90f, 1.10f);
            dt *= rsmod;
            boolean build = false;
            if (limit >= 0) {
                if ((limit -= dt) < 0)
                    ldone = true;
            }
            boolean done = ldone;
            for (PoseMod m : mods) {
                m.tick(dt);
                if (!m.done())
                    done = false;
            }
            if (!stat)
                build = true;
            if (ipold > 0.0f) {
                if ((ipold -= (dt / ipol)) < 0.0f) {
                    ipold = 0.0f;
                    old = null;
                }
                build = true;
            }
            if (build)
                rebuild();
            if (done)
                done();
        }

        @Deprecated
        public void tick(float dt, double v) {
            tick(dt);
        }

        protected void done() {
        }
    }

    public Composited(Skeleton skel) {
        this.skel = skel;
        this.pose = skel.new Pose(skel.bindpose);
    }

    public class SpriteEqu extends Equ {
        private final Sprite spr;

        private SpriteEqu(ED ed) {
            super(ed);
            this.spr = Sprite.create(eqowner, ed.res.res.get(), ed.res.sdt.clone());
        }

        public void tick(int dt) {
            spr.tick(dt);
        }
    }

    public abstract class Equ{
        public final ED desc;
        public final int id;
        private boolean matched;

        private Equ(ED ed) {
            this.desc = ed.clone();
            this.id = desc.id;
        }

        public void tick(int dt) {
        }
    }

    public static class MD implements Cloneable {
        public Indir<Resource> mod;
        public List<ResData> tex;
        public int id = -1;

        public MD(Indir<Resource> mod, List<ResData> tex) {
            this.mod = mod;
            this.tex = tex;
        }

        public boolean equals(Object o) {
            if (!(o instanceof MD))
                return (false);
            MD m = (MD) o;
            return (mod.equals(m.mod) && tex.equals(m.tex));
        }

        public MD clone() {
            try {
                MD ret = (MD) super.clone();
                ret.tex = new ArrayList<ResData>(tex);
                return (ret);
            } catch (CloneNotSupportedException e) {
        /* This is ridiculous. */
                throw (new RuntimeException(e));
            }
        }

        public String toString() {
            return (mod + "+" + tex);
        }
    }

    public static class ED implements Cloneable {
        public int t, id = -1;
        public String at;
        public ResData res;
        public Coord3f off;

        public ED(int t, String at, ResData res, Coord3f off) {
            this.t = t;
            this.at = at;
            this.res = res;
            this.off = off;
        }

        public boolean equals(Object o) {
            if (!(o instanceof ED))
                return (false);
            ED e = (ED) o;
            return ((t == e.t) && at.equals(e.at) && res.equals(e.res) && off.equals(e.off));
        }

        public boolean equals2(Object o) {
            if (!(o instanceof ED))
                return (false);
            ED e = (ED) o;
            return ((t == e.t) && at.equals(e.at) && res.res.equals(e.res.res) && off.equals(e.off));
        }

        public ED clone() {
            try {
                ED ret = (ED) super.clone();
                ret.res = res.clone();
                return (ret);
            } catch (CloneNotSupportedException e) {
        /* This is ridiculous. */
                throw (new RuntimeException(e));
            }
        }

        public String toString() {
            return (String.format("<ED: %d \"%s\" %s(%s) %s>", t, at, res.res, res.sdt, off));
        }
    }

    public static class Desc {
        public Indir<Resource> base;
        public List<MD> mod = new ArrayList<>();
        public List<ED> equ = new ArrayList<>();

        public Desc() {
        }

        public Desc(Indir<Resource> base) {
            this.base = base;
        }

        public static Desc decode(Session sess, Object[] args) {
            Desc ret = new Desc();
            ret.base = sess.getres((Integer) args[0]);
            Object[] ma = (Object[]) args[1];
            for (int i = 0; i < ma.length; i += 2) {
                List<ResData> tex = new ArrayList<ResData>();
                Indir<Resource> mod = sess.getres((Integer) ma[i]);
                Object[] ta = (Object[]) ma[i + 1];
                for (int o = 0; o < ta.length; o++) {
                    Indir<Resource> tr = sess.getres((Integer) ta[o]);
                    Message sdt = Message.nil;
                    if ((ta.length > o + 1) && (ta[o + 1] instanceof byte[]))
                        sdt = new MessageBuf((byte[]) ta[++o]);
                    tex.add(new ResData(tr, sdt));
                }
                ret.mod.add(new MD(mod, tex));
            }
            Object[] ea = (Object[]) args[2];
            for (int i = 0; i < ea.length; i++) {
                Object[] qa = (Object[]) ea[i];
                int n = 0;
                int t = (Integer) qa[n++];
                String at = (String) qa[n++];
                Indir<Resource> res = sess.getres((Integer) qa[n++]);
                Message sdt = Message.nil;
                if (qa[n] instanceof byte[])
                    sdt = new MessageBuf((byte[]) qa[n++]);
                Coord3f off = new Coord3f(((Number) qa[n + 0]).floatValue(), ((Number) qa[n + 1]).floatValue(), ((Number) qa[n + 2]).floatValue());
                ret.equ.add(new ED(t, at, new ResData(res, sdt), off));
            }
            return (ret);
        }

        public String toString() {
            return (String.format("desc(%s, %s, %s)", base, mod, equ));
        }
    }

    private void nequ(boolean nocatch) {
        outer:
        for (Iterator<ED> i = nequ.iterator(); i.hasNext(); ) {
            ED ed = i.next();
            try {
                Equ prev = null;
                for (Equ equ : this.equ) {
                    if (equ.desc.equals(ed)) {
                        equ.matched = true;
                        i.remove();
                        continue outer;
                    } else if ((equ instanceof SpriteEqu) && equ.desc.equals2(ed)) {
                        equ.desc.res.sdt = ed.res.sdt;
                        equ.matched = true;
                        i.remove();
                        continue outer;
                    }
                }
                Equ ne;
                if (ed.t == 0)
                    ne = new SpriteEqu(ed);
                else
                    throw (new RuntimeException("Invalid composite equ-type: " + ed.t));
                ne.matched = true;
                this.equ.add(ne);
                i.remove();
            } catch (Loading e) {
                if (nocatch)
                    throw (e);
            }
        }
        if (nequ.isEmpty()) {
            nequ = null;
            for (Iterator<Equ> i = this.equ.iterator(); i.hasNext(); ) {
                Equ equ = i.next();
                if (!equ.matched)
                    i.remove();
            }
        }
    }

    public void changes(boolean nocatch) {
        if (nequ != null)
            nequ(nocatch);
    }

    public void changes() {
        changes(false);
    }

    private static class CompositeClick extends ClickInfo {
        CompositeClick(ClickInfo prev, Integer id) {
            super(prev, id);
        }
    }

    public ClickInfo clickinfo(ClickInfo prev) {
        return (new CompositeClick(prev, null));
    }

    public void tick(int dt) {
        if (poses != null)
            poses.tick(dt / 1000.0f);
        for (Equ equ : this.equ)
            equ.tick(dt);
    }

    @Deprecated
    public void tick(int dt, double v) {
        tick(dt);
    }

    public void chmod(List<MD> mod) {
        if (mod.equals(cmod))
            return;
        nmod = new LinkedList<MD>();
        for (MD md : mod)
            nmod.add(md.clone());
        cmod = new ArrayList<MD>(mod);
    }

    public void chequ(List<ED> equ) {
        if (equ.equals(cequ))
            return;
        for (Equ oequ : this.equ)
            oequ.matched = false;
        nequ = new LinkedList<ED>();
        for (ED ed : equ)
            nequ.add(ed.clone());
        cequ = new ArrayList<ED>(equ);
    }
}
