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

public class Buff extends Widget {
    public static final Text.Foundry nfnd = new Text.Foundry(Text.dfont, 12);   // FIXME: size. where is this used???
    public static final Tex frame = Resource.loadtex("gfx/hud/buffs/frame");
    public static final Tex cframe = Resource.loadtex("gfx/hud/buffs/cframe");
    static final Coord imgoff = new Coord(3, 3);
    static final Coord ameteroff = new Coord(3, 37), ametersz = new Coord(32, 3);
    Indir<Resource> res;
    String tt = null;
    int ameter = -1;
    int nmeter = -1;
    int cmeter = -1;
    int cticks = -1;
    long gettime;
    Tex ntext = null;
    int a = 255;
    boolean dest = false;
    public Tex atex;

    @RName("buff")
    public static class $_ implements Factory {
        public Widget create(Widget parent, Object[] args) {
            Indir<Resource> res = parent.ui.sess.getres((Integer) args[0]);
            return (new Buff(res));
        }
    }

    public Buff(Indir<Resource> res) {
        super(cframe.sz());
        this.res = res;
    }

    private Tex nmeter() {
        if (ntext == null)
            ntext = new TexI(Utils.outline2(nfnd.render(Integer.toString(nmeter), Color.WHITE).img, Color.BLACK));
        return (ntext);
    }

    private String shorttip() {
        if (tt != null)
            return (tt);
        String ret = res.get().layer(Resource.tooltip).t;
        if (ameter >= 0)
            ret = ret + " (" + ameter + "%)";
        return (ret);
    }

    private long hoverstart;
    private Text shorttip, longtip;

    public Object tooltip(Coord c, Widget prev) {
        long now = System.currentTimeMillis();
        if (prev != this)
            hoverstart = now;
        try {
            if (now - hoverstart < 1000) {
                if (shorttip == null)
                    shorttip = Text.render(shorttip());
                return (shorttip.tex());
            } else {
                if (longtip == null) {
                    String text = RichText.Parser.quote(shorttip());
                    Resource.Pagina pag = res.get().layer(Resource.pagina);
                    if (pag != null)
                        text += "\n\n" + pag.text;
                    longtip = RichText.render(text, 200);
                }
                return (longtip.tex());
            }
        } catch (Loading e) {
            return ("...");
        }
    }

    public void reqdestroy() {
        anims.clear();
        final Coord o = this.c;
        dest = true;
        new NormAnim(0.5) {
            public void ntick(double a) {
                Buff.this.a = 255 - (int) (255 * a);
                Buff.this.c = o.add(0, (int) (a * cframe.sz().y));
                if (a == 1.0)
                    destroy();
            }
        };
    }

    public void move(Coord c) {
        if (dest)
            return;
        final Coord o = this.c;
        final Coord d = c.sub(o);
        new NormAnim(0.5) {
            public void ntick(double a) {
                Buff.this.c = o.add(d.mul(Utils.smoothstep(a)));
            }
        };
    }

    public void uimsg(String msg, Object... args) {
        if (msg == "ch") {
            this.res = ui.sess.getres((Integer) args[0]);
        } else if (msg == "tip") {
            String tt = (String) args[0];
            this.tt = tt.equals("") ? null : tt;
            shorttip = longtip = null;
        } else if (msg == "am") {
            this.ameter = (Integer) args[0];
            shorttip = longtip = null;
            if (atex != null)
                atex.dispose();
            atex = null;
        } else if (msg == "nm") {
            this.nmeter = (Integer) args[0];
        } else if (msg == "cm") {
            this.cmeter = (Integer) args[0];
            this.cticks = (args.length > 1) ? ((Integer) args[1]) : -1;
            gettime = System.currentTimeMillis();
        } else {
            super.uimsg(msg, args);
        }
    }

    public boolean mousedown(Coord c, int btn) {
        wdgmsg("cl", c.sub(imgoff), btn, ui.modflags());
        return (true);
    }
}
