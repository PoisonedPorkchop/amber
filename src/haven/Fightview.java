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
import java.util.*;

public class Fightview extends Widget {
    static Tex bg = Resource.loadtex("gfx/hud/bosq");
    static int height = 5;
    static int ymarg = 5;
    static int width = 165;
    static Coord avasz = new Coord(27, 27);
    public LinkedList<Relation> lsrel = new LinkedList<Relation>();
    public Relation current = null;
    public Indir<Resource> blk, batk, iatk;
    public double atkcs, atkct;
    public Indir<Resource> lastact = null;
    public long lastuse = 0;
    public int atkcd;
    private GiveButton curgive;
    private Button curpurs;
    public final Bufflist buffs = add(new Bufflist());
    {
        buffs.hide();
    }
    private static final Color combatLogMeClr = new Color(86, 153, 191);
    private static final Color combatLogOpClr = new Color(234, 105, 105);

    public class Relation {
        public final long gobid;
        public final GiveButton give;
        public final Button purs;
        public final Bufflist buffs = add(new Bufflist());
        {
            buffs.hide();
        }
        public int ip, oip;
        public Indir<Resource> lastact = null;
        public long lastuse = 0;

        public Relation(long gobid) {
            this.gobid = gobid;
            add(this.give = new GiveButton(0, new Coord(15, 15)));
            add(this.purs = new Button(70, "Pursue"));
        }

        public void give(int state) {
            if (this == current)
                curgive.state = state;
            this.give.state = state;
        }

        public void show(boolean state) {
            give.show(state);
            purs.show(state);
        }

        public void remove() {
            ui.destroy(give);
            ui.destroy(purs);
        }

        public void use(Indir<Resource> act) {
            lastact = act;
            lastuse = System.currentTimeMillis();
            if (lastact != null && Config.logcombatactions) {
                try {
                    Resource res = lastact.get();
                    Resource.Tooltip tt = res.layer(Resource.tooltip);
                    if (tt == null) {
                        gameui().syslog.append("Combat: WARNING! tooltip is missing for " + res.name + ". Notify Jorb/Loftar about this.", combatLogOpClr);
                        return;
                    }
                    gameui().syslog.append(String.format("%d: %s, ip %d - %d", gobid, tt.t, ip, oip), combatLogOpClr);
                } catch (Loading l) {
                }
            }
        }
    }

    public void use(Indir<Resource> act) {
        lastact = act;
        lastuse = System.currentTimeMillis();
        if (lastact != null && Config.logcombatactions) {
            try {
                Resource res = lastact.get();
                Resource.Tooltip tt = res.layer(Resource.tooltip);
                if (tt == null) {
                    gameui().syslog.append("Combat: WARNING! tooltip is missing for " + res.name + ". Notify Jorb/Loftar about this.", combatLogMeClr);
                    return;
                }
                String cd = Utils.fmt1DecPlace(atkct - System.currentTimeMillis() / 1000.0);
                gameui().syslog.append(String.format("me: %s, ip %d - %d, cd %ss", tt.t, current.ip, current.oip, cd), combatLogMeClr);
            } catch (Loading l) {
            }
        }
    }

    @RName("frv")
    public static class $_ implements Factory {
        public Widget create(Widget parent, Object[] args) {
            return (new Fightview());
        }
    }

    public Fightview() {
        super(new Coord(width, (bg.sz().y + ymarg) * height));
    }

    public void addchild(Widget child, Object... args) {
        if (args[0].equals("buff")) {
            Widget p;
            if (args[1] == null)
                p = buffs;
            else
                p = getrel((Integer) args[1]).buffs;
            p.addchild(child);
        } else {
            super.addchild(child, args);
        }
    }

    private void setcur(Relation rel) {
        if ((current == null) && (rel != null)) {
            add(curgive = new GiveButton(0));
            add(curpurs = new Button(70, "Pursue"));
            curgive.state = rel.give.state;
        } else if ((current != null) && (rel == null)) {
            ui.destroy(curgive);
            ui.destroy(curpurs);
            curgive = null;
            curpurs = null;
        } else if ((current != null) && (rel != null)) {
            curgive.state = rel.give.state;
        }
        current = rel;
    }

    public void destroy() {
        setcur(null);
        super.destroy();
    }

    public static class Notfound extends RuntimeException {
        public final long id;

        public Notfound(long id) {
            super("No relation for Gob ID " + id + " found");
            this.id = id;
        }
    }

    private Relation getrel(long gobid) {
        for (Relation rel : lsrel) {
            if (rel.gobid == gobid)
                return (rel);
        }
        throw (new Notfound(gobid));
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
        if (sender == curgive) {
            wdgmsg("give", (int) current.gobid, args[0]);
            return;
        } else if (sender == curpurs) {
            wdgmsg("prs", (int) current.gobid);
            return;
        }
        for (Relation rel : lsrel) {
            if (sender == rel.give) {
                wdgmsg("give", (int) rel.gobid, args[0]);
                return;
            } else if (sender == rel.purs) {
                wdgmsg("prs", (int) rel.gobid);
                return;
            }
        }
        super.wdgmsg(sender, msg, args);
    }

    private Indir<Resource> n2r(int num) {
        if (num < 0)
            return (null);
        return (ui.sess.getres(num));
    }

    public void uimsg(String msg, Object... args) {
        if (msg == "new") {
            Relation rel = new Relation((Integer) args[0]);
            rel.give((Integer) args[1]);
            rel.ip = (Integer) args[2];
            rel.oip = (Integer) args[3];
            lsrel.addFirst(rel);
            ui.sess.glob.oc.isfight = true;
            return;
        } else if (msg == "del") {
            Relation rel = getrel((Integer) args[0]);
            OCache oc = ui.sess.glob.oc;
            oc.removedmgoverlay(rel.gobid);
            rel.remove();
            lsrel.remove(rel);
            if (lsrel.size() == 0) {
                oc.removedmgoverlay(MapView.plgob);
                oc.isfight = false;
            }
            if (rel == current)
                setcur(null);
            return;
        } else if (msg == "upd") {
            Relation rel = getrel((Integer) args[0]);
            rel.give((Integer) args[1]);
            rel.ip = (Integer) args[2];
            rel.oip = (Integer) args[3];
            return;
        } else if(msg == "used") {
            use((args[0] == null)?null:ui.sess.getres((Integer)args[0]));
            return;
        } else if(msg == "ruse") {
            Relation rel = getrel((Integer)args[0]);
            rel.use((args[1] == null)?null:ui.sess.getres((Integer)args[1]));
            return;
        } else if (msg == "cur") {
            try {
                Relation rel = getrel((Integer) args[0]);
                lsrel.remove(rel);
                lsrel.addFirst(rel);
                setcur(rel);
            } catch (Notfound e) {
                setcur(null);
            }
            return;
        } else if (msg == "atkc") {
            atkcd = (Integer) args[0];
            atkcs = System.currentTimeMillis() / 1000.0;
            atkct = atkcs + (atkcd * 0.06);
            return;
        } else if (msg == "blk") {
            blk = n2r((Integer) args[0]);
            return;
        } else if (msg == "atk") {
            batk = n2r((Integer) args[0]);
            iatk = n2r((Integer) args[1]);
            return;
        }
        super.uimsg(msg, args);
    }
}
