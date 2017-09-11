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

import java.awt.Color;
import java.util.*;

import haven.glsl.*;

import static haven.glsl.Cons.*;
import static haven.glsl.Type.*;

import javax.media.opengl.*;

public class Outlines implements Rendered {
    private boolean symmetric;

    private final static Uniform snrm = new Uniform(SAMPLER2D);
    private final static Uniform sdep = new Uniform(SAMPLER2D);
    private final static Uniform msnrm = new Uniform(SAMPLER2DMS);
    private final static Uniform msdep = new Uniform(SAMPLER2DMS);
    private final static ShaderMacro[] shaders = new ShaderMacro[4];

    static {
	/* XXX: It would be good to have some kind of more convenient
	 * shader internation. */
    }

    public Outlines(final boolean symmetric) {
        this.symmetric = symmetric;
    }

    public boolean setup(RenderList rl) {
        final PView.ConfContext ctx = (PView.ConfContext) rl.state().get(PView.ctx);
        final RenderedNormals nrm = ctx.data(RenderedNormals.id);
        final boolean ms = ctx.cfg.ms > 1;
        ctx.cfg.tdepth = true;
        ctx.cfg.add(nrm);
        rl.prepc(Rendered.postfx);
        rl.add(new Rendered.ScreenQuad(), new States.AdHoc(shaders[(symmetric ? 2 : 0) | (ms ? 1 : 0)]) {
            private TexUnit tnrm;
            private TexUnit tdep;

            public void reapply(GOut g) {
                BGL gl = g.gl;
                gl.glUniform1i(g.st.prog.uniform(!ms ? snrm : msnrm), tnrm.id);
                gl.glUniform1i(g.st.prog.uniform(!ms ? sdep : msdep), tdep.id);
            }

            public void apply(GOut g) {
                if (!ms) {
                    tnrm = g.st.texalloc(g, ((GLFrameBuffer.Attach2D) nrm.tex).tex);
                    tdep = g.st.texalloc(g, ((GLFrameBuffer.Attach2D) ctx.cur.depth).tex);
                } else {
                    tnrm = g.st.texalloc(g, ((GLFrameBuffer.AttachMS) nrm.tex).tex);
                    tdep = g.st.texalloc(g, ((GLFrameBuffer.AttachMS) ctx.cur.depth).tex);
                }
                reapply(g);
            }

            public void unapply(GOut g) {
                tnrm.ufree(g);
                tnrm = null;
                tdep.ufree(g);
                tdep = null;
            }
        });
        return (false);
    }
}
