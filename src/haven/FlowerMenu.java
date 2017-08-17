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

import haven.mod.ModAPI;
import haven.mod.event.flower.FlowerMenuCancelEvent;
import haven.mod.event.flower.FlowerMenuChooseEvent;
import haven.mod.event.flower.FlowerMenuChosenEvent;
import haven.mod.event.flower.FlowerMenuCreateEvent;

import java.awt.*;

import static java.lang.Math.PI;

public class FlowerMenu extends Widget {
    public static final Color pink = new Color(255, 0, 128);
    public static final Text.Foundry ptf = new Text.Foundry(Text.dfont, Text.cfg.flowerMenu);
    public static final IBox pbox = Window.wbox;
    public static final Tex pbg = Window.bg;
    public static final int ph = 30;
    public Petal[] opts;
    private FlowerMenu self;
    private UI.Grab mg, kg;
    private static String nextAutoSel;
    private static long nextAutoSelTimeout;
    public static String lastSel;
    
    @RName("sm")
    public static class $_ implements Factory {
        public Widget create(Widget parent, Object[] args) {
            String[] opts = new String[args.length];
            for (int i = 0; i < args.length; i++)
                opts[i] = (String) args[i];
            return (new FlowerMenu(opts));
        }
    }

    public class Petal extends Widget {
        public String name;
        public double ta;
        private final static double rad = 75;
        public int num;
        private Text text;
        private double a = 1;

        public Petal(String name) {
            super(Coord.z);
            this.name = name;
            text = ptf.render(Resource.getLocString(Resource.BUNDLE_FLOWER, name), name.startsWith("Travel ") ? Color.GREEN : Color.YELLOW);
            resize(text.sz().x + 25, ph);
        }

        public void move(double a, double r) {
            this.c = Coord.sc(a, r).sub(sz.div(2));
            // adjust horizontal position for potentially parallel petals to avoid overlap
            if (r == rad) {
                for (Petal p : opts) {
                    if (this.c.x + sz.x >= p.c.x &&
                            (num == 7 && p.num == 1 || num == 6 && p.num == 2 || num == 5 && p.num == 3)) {
                        p.c.x = opts[0].c.x + opts[0].sz.x / 2 + 5;
                        this.c.x = p.c.x - sz.x - 5;
                        break;
                    }
                }
            }
        }

        public void draw(GOut g) {
            g.chcolor(new Color(255, 255, 255, (int) (255 * a)));
            g.image(pbg, new Coord(3, 3), new Coord(3, 3), sz.add(new Coord(-6, -6)));
            // pbg is to short for wide petals
            if (pbg.sz().x < sz.x)
                g.image(pbg, new Coord(pbg.sz().x, 3), new Coord(3, 3), sz.add(new Coord(-6, -6)));
            pbox.draw(g, Coord.z, sz);
            g.image(text.tex(), sz.div(2).sub(text.sz().div(2)));
        }

        public boolean mousedown(Coord c, int button) {
            choose(this);
            return (true);
        }
    }

    public class Opening extends NormAnim {

        private FlowerMenu menu;

        private double a = 0.0;
        private final double s = 1.0 / 0.0;

        Opening() {
            super(0);
        }

        @Override
        public boolean tick(double dt)
        {
            a += dt;
            double na = a * s;
            if (na >= 1.0) {
                ntick(1.0);

                //FlowerMenuCreateEvent
                String[] options = new String[opts.length];
                for(int x = 0; x < opts.length; x++)
                    options[x] = opts[x].name;
                FlowerMenuCreateEvent event = new FlowerMenuCreateEvent(self, options);
                ModAPI.callEvent(event);
                //FlowerMenuCreateEvent

                return (true);
            } else {
                ntick(na);
                return (false);
            }
        }

        public void ntick(double s) {
            for (Petal p : opts) {
                p.move(p.ta + ((1 - s) * PI), p.rad * s);
                p.a = s;
                if (s == 1.0) {
                    CheckListboxItem itm = Config.flowermenus.get(p.name);
                    if (itm != null && itm.selected && !ui.modmeta ||
                            p.name.equals(nextAutoSel) && System.currentTimeMillis() - nextAutoSelTimeout < 2000) {
                        nextAutoSel = null;
                        choose(p);
                        break;
                    }
                }
            }
        }
    }

    public class Chosen extends NormAnim {
        Petal chosen;

        Chosen(Petal c) {
            super(0.75);
            chosen = c;
        }

        public void ntick(double s) {
            for (Petal p : opts) {
                if (p == chosen) {
                    if (s > 0.6) {
                        p.a = 1 - ((s - 0.6) / 0.4);
                    } else if (s < 0.3) {
                        p.move(p.ta, p.rad * (1 - (s / 0.3)));
                    }
                } else {
                    if (s > 0.3)
                        p.a = 0;
                    else
                        p.a = 1 - (s / 0.3);
                }
            }
            if (s == 1.0)
                ui.destroy(FlowerMenu.this);
        }
    }

    public class Cancel extends NormAnim {
        Cancel() {
            super(0);
        }

        public void ntick(double s) {
            for (Petal p : opts) {
                p.move(p.ta + ((s) * PI), p.rad * (1 - s));
                p.a = 1 - s;
            }
            if (s == 1.0)
                ui.destroy(FlowerMenu.this);
        }
    }

    private void organize(Petal[] opts) {
        for (int i = 0 ; i < opts.length; i++) {
            double ta = PI/2 - i * PI/4;

            // slightly adjust 45 degrees angles
            if (ta == PI/4 || ta == -3*PI/4)
                ta -= 0.25;
            if (ta == -PI/4 || ta == -5*PI/4)
                ta += 0.25;

            opts[i].ta = ta;
        }
    }

    public FlowerMenu(String... options) {
        super(Coord.z);
        this.self = this;
        opts = new Petal[options.length];
        for (int i = 0; i < options.length; i++) {
            add(opts[i] = new Petal(options[i]));
            opts[i].num = i;
        }
    }

    protected void added() {
        if (c.equals(-1, -1))
            c = parent.ui.lcc;
        mg = ui.grabmouse(this);
        kg = ui.grabkeys(this);
        organize(opts);

        new Opening();
    }

    public boolean mousedown(Coord c, int button) {
        if (!anims.isEmpty())
            return (true);
        if (!super.mousedown(c, button))
            choose((Petal) null);
        return (true);
    }

    public void uimsg(String msg, Object... args) {
        if (msg == "cancel") {
            new Cancel();
            mg.remove();
            kg.remove();

            //FlowerMenuCancelEvent
            FlowerMenuCancelEvent event = new FlowerMenuCancelEvent(this);
            ModAPI.callEvent(event);
            //FlowerMenuCancelEvent
        } else if (msg == "act") {
            new Chosen(opts[(Integer) args[0]]);
            mg.remove();
            kg.remove();

            //FlowerMenuChosenEvent
            FlowerMenuChosenEvent event = new FlowerMenuChosenEvent(this, opts[(Integer) args[0]]);
            ModAPI.callEvent(event);
            //FlowerMenuChosenEvent
        }
    }

    public void draw(GOut g) {
        super.draw(g, false);
    }

    public boolean keydown(java.awt.event.KeyEvent ev) {
        return (true);
    }

    public boolean type(char key, java.awt.event.KeyEvent ev) {
        if (Config.userazerty)
            key = Utils.azerty2qwerty(key);

        if ((key >= '0') && (key <= '9')) {
            int opt = (key == '0') ? 10 : (key - '1');
            if (opt < opts.length) {
                choose(opts[opt]);
                kg.remove();
            }
            return (true);
        } else if (key == 27) {
            choose((Petal) null);
            kg.remove();
            return (true);
        }
        return (false);
    }

    public void choose(Petal option) {

        //FlowerMenuChooseEvent
        FlowerMenuChooseEvent event = new FlowerMenuChooseEvent(option);
        ModAPI.callEvent(event);
        if(event.getCancelled())
            return;
        //FlowerMenuChooseEvent

        if (option == null) {
            wdgmsg("cl", -1);
            lastSel = null;
        } else {
            wdgmsg("cl", option.num, ui.modflags());
            lastSel = option.name;
            MapView.pllastcc = null;
        }
    }

    /**
     * Choose option by index.
     * @param index Index of option. Remember, indexes start at 0.
     * @return Whether the chosen option exists or not.
     */
    public boolean choose(int index)
    {
        if((opts.length - 1) >= index && opts.length > 0) {
            choose(opts[index]);
            return true;
        }
        else
            return false;
    }

    /**
     * Choose an option by name.
     * @param option Name of the option to choose.
     * @return Whether the chosen option exists or not.
     */
    public boolean choose(String option)
    {
        for(Petal petal : opts)
            if(petal.name.equals(option)) {
                choose(petal);
                return true;
            }
        return false;
    }

    /**
     * Choose an option, whose name contains the input.
     * @param option Name, that is part of the desired option.
     * @return Whether the chosen option exists or not.
     */
    public boolean chooseContains(String option)
    {
        for(Petal petal : opts)
            if(petal.name.contains(option)) {
                choose(petal);
                return true;
            }
        return false;
    }

    public static void setNextSelection(String name) {
        nextAutoSel = name;
        nextAutoSelTimeout = System.currentTimeMillis();
    }

    /**
     * Test whether or not the Flower Menu contains the option.
     * @param option The option, whose existence in the Flower Menu is in question.
     * @return Whether the option exists.
     */
    public boolean hasOption(String option)
    {
        for(Petal opt : opts)
            if(opt.name.equals(option))
                return true;
        return false;
    }
}
