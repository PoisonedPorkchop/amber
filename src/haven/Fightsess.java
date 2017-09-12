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

import haven.mod.Mod;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Fightsess extends Widget {
    public static final Tex cdframe = Resource.loadtex("gfx/hud/combat/cool");
    public static final Tex actframe = Buff.frame;
    public static final Coord actframeo = Buff.imgoff;
    public static final Tex indframe = Resource.loadtex("gfx/hud/combat/indframe");
    public static final Coord indframeo = (indframe.sz().sub(32, 32)).div(2);
    public static final Tex useframe = Resource.loadtex("gfx/hud/combat/lastframe");
    public static final Coord useframeo = (useframe.sz().sub(32, 32)).div(2);
    public static final int actpitch = 50;
    public final Indir<Resource>[] actions;
    public final boolean[] dyn;
    public int use = -1;
    public Coord pcc;
    public int pho;
    private final Fightview fv;
    private final Tex[] keystex = new Tex[10];
    private final Tex[] keysftex = new Tex[10];

    private static final Map<String, Color> openings = new HashMap<String, Color>(4) {{
        put("paginae/atk/dizzy",new Color(8, 103, 136));
        put("paginae/atk/offbalance", new Color(8, 103, 1));
        put("paginae/atk/cornered", new Color(221, 28, 26));
        put("paginae/atk/reeling", new Color(203, 168, 6));
    }};
    private Coord simpleOpeningSz = new Coord(32, 32);

    @RName("fsess")
    public static class $_ implements Factory {
        public Widget create(Widget parent, Object[] args) {
            int nact = (Integer) args[0];
            return (new Fightsess(nact, parent.getparent(GameUI.class).fv));
        }
    }

    @SuppressWarnings("unchecked")
    public Fightsess(int nact, Fightview fv) {
        this.fv = fv;
        pho = -40;
        this.actions = (Indir<Resource>[]) new Indir[nact];
        this.dyn = new boolean[nact];

        for(int i = 0; i < 10; i++) {
            keystex[i] = Text.renderstroked(FightWnd.keys[i], Color.WHITE, Color.BLACK, Text.num12boldFnd).tex();
            if (i < 5)
                keysftex[i] = keystex[i];
            else
                keysftex[i] = Text.renderstroked(FightWnd.keysf[i - 5], Color.WHITE, Color.BLACK, Text.num12boldFnd).tex();
        }

        System.out.println("Fight Session started!");
    }

    public void presize() {
        resize(parent.sz);
        pcc = sz.div(2);
    }

    protected void added() {
        presize();
    }

    private void updatepos() {
        MapView map;
        Gob pl;
        if (((map = getparent(GameUI.class).map) == null) || ((pl = map.player()) == null) || (pl.sc == null))
            return;
        pcc = pl.sc;
        pho = (int) (pl.sczu.mul(20f).y) - 20;
    }

    private static final Resource tgtfx = Resource.local().loadwait("gfx/hud/combat/trgtarw");
    private final Map<Pair<Long, Resource>, Sprite> cfx = new CacheMap<Pair<Long, Resource>, Sprite>();
    private final Collection<Sprite> curfx = new ArrayList<Sprite>();

    //Called when an effect, such as targeting arrow, is used on a target.
    private void fxon(long gobid, Resource fx) {
        MapView map = getparent(GameUI.class).map;
        Gob gob = ui.sess.glob.oc.getgob(gobid);
        if((map == null) || (gob == null))
            return;
        Pair<Long, Resource> id = new Pair<Long, Resource>(gobid, fx);
        Sprite spr = cfx.get(id);
        if(spr == null)
            cfx.put(id, spr = Sprite.create(null, fx, Message.nil));
        curfx.add(spr);
    }

    public void tick(double dt) {
        for(Sprite spr : curfx)
            spr.tick((int)(dt * 1000));
        curfx.clear();
    }

    private static final Text.Furnace ipf = new PUtils.BlurFurn(new Text.Foundry(Text.serif, 18, new Color(128, 128, 255)).aa(true), 1, 1, new Color(48, 48, 96));
    private final Text.UText<?> ip = new Text.UText<Integer>(ipf) {
        public String text(Integer v) {
            return (Config.altfightui ? v.toString() : "IP: " + v);
        }

        public Integer value() {
            return (fv.current.ip);
        }
    };
    private final Text.UText<?> oip = new Text.UText<Integer>(ipf) {
        public String text(Integer v) {
            return (Config.altfightui ? v.toString() : "IP: " + v);
        }

        public Integer value() {
            return (fv.current.oip);
        }
    };

    private static Coord actc(int i) {
        int rl = 5;

        int row = i / rl;
        if (Config.combatkeys == 1)
            row ^= 1;

        return(new Coord((actpitch * (i % rl)) - (((rl - 1) * actpitch) / 2), 125 + (row * actpitch)));
    }

    private static final Coord cmc = new Coord(0, 67);
    private static final Coord usec1 = new Coord(-65, 67);
    private static final Coord usec2 = new Coord(65, 67);
    private Indir<Resource> lastact1 = null, lastact2 = null;
    private Text lastacttip1 = null, lastacttip2 = null;

    private Widget prevtt = null;
    private Text acttip = null;
    public static final String[] keytips = {"1", "2", "3", "4", "5", "Shift+1", "Shift+2", "Shift+3", "Shift+4", "Shift+5"};
    public Object tooltip(Coord c, Widget prev) {
        int cx = gameui().sz.x / 2;

        for (Buff buff : fv.buffs.children(Buff.class)) {
            Coord dc = Config.altfightui ? new Coord(cx - buff.c.x - Buff.cframe.sz().x - 80, 180) : pcc.add(-buff.c.x - Buff.cframe.sz().x - 20, buff.c.y + pho - Buff.cframe.sz().y);
            if (c.isect(dc, buff.sz)) {
                Object ret = buff.tooltip(c.sub(dc), prevtt);
                if (ret != null) {
                    prevtt = buff;
                    return (ret);
                }
            }
        }

        if (fv.current != null) {
            for (Buff buff : fv.current.buffs.children(Buff.class)) {
                Coord dc = Config.altfightui ? new Coord(cx + buff.c.x + 80, 180) : pcc.add(buff.c.x + 20, buff.c.y + pho - Buff.cframe.sz().y);
                if (c.isect(dc, buff.sz)) {
                    Object ret = buff.tooltip(c.sub(dc), prevtt);
                    if (ret != null) {
                        prevtt = buff;
                        return (ret);
                    }
                }
            }
        }

        for (int i = 0; i < actions.length; i++) {
            Coord ca = Config.altfightui ? new Coord(cx - 18, gameui().sz.y - 250).add(actc(i)).add(16, 16) : pcc.add(actc(i));
            Indir<Resource> act = actions[i];
            try {
                if (act != null) {
                    Tex img = act.get().layer(Resource.imgc).tex();
                    ca = ca.sub(img.sz().div(2));
                    if (c.isect(ca, img.sz())) {
                        if (dyn[i])
                            return ("Combat discovery");
                        String tip = act.get().layer(Resource.tooltip).t + " ($b{$col[255,128,0]{" + keytips[i] + "}})";
                        if((acttip == null) || !acttip.text.equals(tip))
                            acttip = RichText.render(tip, -1);
                        return(acttip);
                    }
                }
            } catch (Loading l) {
            }
            ca.x += actpitch;
        }

        try {
            Indir<Resource> lastact = this.lastact1;
            if(lastact != null) {
                Coord usesz = lastact.get().layer(Resource.imgc).sz;
                Coord lac = Config.altfightui ? new Coord(cx - 69, 120).add(usesz.div(2)) : pcc.add(usec1);
                if(c.isect(lac.sub(usesz.div(2)), usesz)) {
                    if(lastacttip1 == null)
                        lastacttip1 = Text.render(lastact.get().layer(Resource.tooltip).t);
                    return(lastacttip1);
                }
            }
        } catch(Loading l) {}
        try {
            Indir<Resource> lastact = this.lastact2;
            if(lastact != null) {
                Coord usesz = lastact.get().layer(Resource.imgc).sz;
                Coord lac = Config.altfightui ? new Coord(cx + 69 - usesz.x, 120).add(usesz.div(2)) : pcc.add(usec2);
                if(c.isect(lac.sub(usesz.div(2)), usesz)) {
                    if(lastacttip2 == null)
                        lastacttip2 = Text.render(lastact.get().layer(Resource.tooltip).t);
                    return(lastacttip2);
                }
            }
        } catch(Loading l) {}
        return (null);
    }

    public void uimsg(String msg, Object... args) {
        if (msg == "act") {
            int n = (Integer) args[0];
            if (args.length > 1) {
                Indir<Resource> res = ui.sess.getres((Integer) args[1]);
                actions[n] = res;
                dyn[n] = ((Integer) args[2]) != 0;
            } else {
                actions[n] = null;
            }
        } else if (msg == "use") {
            this.use = (Integer) args[0];
            System.out.println("Using ability?");
        } else if (msg == "used") {
            System.out.println("Used ability?");
        } else {
            super.uimsg(msg, args);
        }
    }

    public boolean globtype(char key, KeyEvent ev) {
        if (ev.getKeyCode() == KeyEvent.VK_TAB && ev.isControlDown()) {
            Fightview.Relation cur = fv.current;
            if (cur != null) {
                fv.lsrel.remove(cur);
                fv.lsrel.addLast(cur);
            }
            for(Fightview.Relation relation : fv.lsrel)
            {
                GameUI gameUI = new Mod().actions().getGUI();
                BuddyWnd buddies = gameUI.buddies;
                if(buddies == null)
                    System.out.println("NULL BUDDIES!");
                BuddyWnd.Buddy b = buddies.find((int)relation.gobid);
                if (b == null)
                    System.out.println("NULL BUDDY FOUND!");
                else
                    System.out.println("Buddy: " + b.name);
            }
            fv.wdgmsg("bump", (int) fv.lsrel.get(0).gobid);
            System.out.println("Switch!");
            return (true);
        }

        if (Config.combatkeys == 0) {
            if ((key == 0) && (ev.getModifiersEx() & (InputEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) == 0) {
                int n = -1;
                switch(ev.getKeyCode()) {
                    case KeyEvent.VK_1: n = 0; break;
                    case KeyEvent.VK_2: n = 1; break;
                    case KeyEvent.VK_3: n = 2; break;
                    case KeyEvent.VK_4: n = 3; break;
                    case KeyEvent.VK_5: n = 4; break;
                }
                if((n >= 0) && ((ev.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0))
                    n += 5;
                if((n >= 0) && (n < actions.length)) {
                    wdgmsg("use", n);
                    return(true);
                }
            }
        } else { // F1-F5
            if (key == 0) {
                int n = -1;
                switch(ev.getKeyCode()) {
                    case KeyEvent.VK_1: n = 0; break;
                    case KeyEvent.VK_2: n = 1; break;
                    case KeyEvent.VK_3: n = 2; break;
                    case KeyEvent.VK_4: n = 3; break;
                    case KeyEvent.VK_5: n = 4; break;
                    case KeyEvent.VK_F1: n = 5; break;
                    case KeyEvent.VK_F2: n = 6; break;
                    case KeyEvent.VK_F3: n = 7; break;
                    case KeyEvent.VK_F4: n = 8; break;
                    case KeyEvent.VK_F5: n = 9; break;
                }
                if((n >= 0) && (n < actions.length)) {
                    wdgmsg("use", n);
                    return(true);
                }
            }
        }

        return(super.globtype(key, ev));
    }
}
