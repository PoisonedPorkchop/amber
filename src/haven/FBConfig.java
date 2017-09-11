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

import java.util.*;
import javax.media.opengl.*;

import haven.glsl.*;
import haven.GLProgram.VarID;
import haven.GLFrameBuffer.Attachment;

public class FBConfig {
    private static Map<ShaderMacro[], ShaderMacro> rescache = new WeakHashMap<ShaderMacro[], ShaderMacro>();
    public final PView.ConfContext ctx;
    public Coord sz;
    public boolean hdr, tdepth;
    public int ms = 1;
    public GLFrameBuffer fb;
    public PView.RenderState wnd;
    public Attachment color[], depth;
    public GLState state;
    private RenderTarget[] tgts = new RenderTarget[0];
    private GLState resp;

    public FBConfig(PView.ConfContext ctx, Coord sz) {
        this.ctx = ctx;
        this.sz = sz;
    }

    private static <T> boolean hasuo(T[] a, T[] b) {
        outer:
        for (T ae : a) {
            for (T be : b) {
                if (Utils.eq(ae, be))
                    continue outer;
            }
            return (false);
        }
        return (true);
    }

    public static boolean equals(FBConfig a, FBConfig b) {
        if (!a.sz.equals(b.sz))
            return (false);
        if ((a.hdr != b.hdr) || (a.tdepth != b.tdepth))
            return (false);
        if (a.ms != b.ms)
            return (false);
        if (!hasuo(a.tgts, b.tgts) || !hasuo(b.tgts, a.tgts))
            return (false);
        return (true);
    }

    private void subsume(FBConfig last) {
        fb = last.fb;
        wnd = last.wnd;
        color = last.color;
        depth = last.depth;
        tgts = last.tgts;
        resp = last.resp;
        state = last.state;
    }

    public RenderTarget add(RenderTarget tgt) {
        if (tgt == null)
            throw (new NullPointerException());
        for (RenderTarget p : tgts) {
            if (Utils.eq(tgt, p))
                return (p);
        }
        int i;
        for (i = 0; i < tgts.length; i++) {
            if (tgts[i] == null)
                tgts[i] = tgt;
            return (tgt);
        }
        tgts = Utils.extend(tgts, i + 1);
        tgts[i] = tgt;
        return (tgt);
    }

    public static abstract class RenderTarget {
        public Attachment tex;

        public Attachment maketex(FBConfig cfg) {
            if (cfg.ms <= 1)
                return (tex = Attachment.mk(new TexE(cfg.sz, GL.GL_RGBA, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE)));
            else
                return (tex = Attachment.mk(new TexMSE(cfg.sz, cfg.ms, GL.GL_RGBA, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE)));
        }

        public GLState state(FBConfig cfg, int id) {
            return (null);
        }

        public ShaderMacro code(FBConfig cfg, int id) {
            return (null);
        }
    }

    public static final Uniform numsamples = new Uniform.AutoApply(Type.INT) {
        public void apply(GOut g, VarID loc) {
            g.gl.glUniform1i(loc, ((PView.ConfContext) g.st.get(PView.ctx)).cur.ms);
        }
    };
}
