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

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesChooser;
import javax.media.opengl.GLProfile;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

public class HavenPanel implements Runnable, Console.Directory {
    UI ui;
    public static UI lui;
    boolean inited = false;
    public static int w, h;
    public boolean bgmode = false;
    public static long bgfd = Utils.getprefi("bghz", 200);
    long fd = 10, fps = 0;
    double uidle = 0.0, ridle = 0.0;
    Queue<InputEvent> events = new LinkedList<InputEvent>();
    private String cursmode = "tex";
    private Resource lastcursor = null;
    public Coord mousepos = new Coord(0, 0);
    private MouseEvent mousemv = null;
    public CPUProfile uprof = new CPUProfile(300), rprof = new CPUProfile(300);
    public GPUProfile gprof = new GPUProfile(300);
    private Throwable uncaught = null;
    public static boolean needtotakescreenshot;
    public static boolean isATI;
    private final boolean gldebug = false;

    private static GLCapabilities stdcaps() {
        GLProfile prof = GLProfile.getDefault();
        GLCapabilities cap = new GLCapabilities(prof);
        cap.setDoubleBuffered(true);
        cap.setAlphaBits(8);
        cap.setRedBits(8);
        cap.setGreenBits(8);
        cap.setBlueBits(8);
        cap.setSampleBuffers(true);
        cap.setNumSamples(4);
        return (cap);
    }

    public HavenPanel(int w, int h, GLCapabilitiesChooser cc) {
        newui(null);
        initgl();
        if (Toolkit.getDefaultToolkit().getMaximumCursorColors() >= 256 || Config.hwcursor)
            cursmode = "awt";
    }

    public HavenPanel(int w, int h) {
        this(w, h, null);
    }

    private void initgl() {
        final haven.error.ErrorHandler h = haven.error.ErrorHandler.find();
    }

    public void init() {
        newui(null);
        inited = true;
    }

    UI newui(Session sess) {
        if (ui != null)
            ui.destroy();
        ui = new UI(new Coord(w, h), sess);
        ui.root.guprof = uprof;
        ui.root.grprof = rprof;
        ui.root.ggprof = gprof;
        ui.cons.add(this);
        lui = ui;
        return (ui);
    }

    private static Cursor makeawtcurs(BufferedImage img, Coord hs) {
        java.awt.Dimension cd = Toolkit.getDefaultToolkit().getBestCursorSize(img.getWidth(), img.getHeight());
        BufferedImage buf = TexI.mkbuf(new Coord((int) cd.getWidth(), (int) cd.getHeight()));
        java.awt.Graphics g = buf.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return (Toolkit.getDefaultToolkit().createCustomCursor(buf, new java.awt.Point(hs.x, hs.y), ""));
    }

    private static class Frame {
        CPUProfile.Frame pf;
        CurrentGL on;
        long doneat;

        Frame(CurrentGL on) {
            this.on = on;
        }
    }

    private Frame[] curdraw = {null};

    void dispatch() {
        synchronized (events) {
            if (mousemv != null) {
                mousepos = new Coord(mousemv.getX(), mousemv.getY());
                ui.mousemove(mousemv, mousepos);
                mousemv = null;
            }
            InputEvent e = null;
            while ((e = events.poll()) != null) {
                if (e instanceof MouseEvent) {
                    MouseEvent me = (MouseEvent) e;
                    if (me.getID() == MouseEvent.MOUSE_PRESSED) {
                        ui.mousedown(me, new Coord(me.getX(), me.getY()), me.getButton());
                    } else if (me.getID() == MouseEvent.MOUSE_RELEASED) {
                        ui.mouseup(me, new Coord(me.getX(), me.getY()), me.getButton());
                    } else if (me instanceof MouseWheelEvent) {
                        ui.mousewheel(me, new Coord(me.getX(), me.getY()), ((MouseWheelEvent) me).getWheelRotation());
                    }
                } else if (e instanceof KeyEvent) {
                    KeyEvent ke = (KeyEvent) e;
                    if (ke.getID() == KeyEvent.KEY_PRESSED) {
                        ui.keydown(ke);
                    } else if (ke.getID() == KeyEvent.KEY_RELEASED) {
                        ui.keyup(ke);
                    } else if (ke.getID() == KeyEvent.KEY_TYPED) {
                        ui.type(ke);
                    }
                }
                ui.lastevent = System.currentTimeMillis();
            }
        }
    }

    private Frame bufdraw = null;
    private final Runnable drawfun = new Runnable() {
        private void uglyjoglhack() throws InterruptedException {
            try {
            } catch (RuntimeException e) {
                InterruptedException ie = Utils.hascause(e, InterruptedException.class);
                if (ie != null)
                    throw (ie);
                else
                    throw (e);
            }
        }

        public void run() {
            try {
                uglyjoglhack();
                synchronized (drawfun) {
                    drawfun.notifyAll();
                }
                while (true) {
                    long then = System.currentTimeMillis();
                    int waited = 0;
                    Frame current;
                    synchronized (drawfun) {
                        while ((current = bufdraw) == null)
                            drawfun.wait();
                        bufdraw = null;
                        drawfun.notifyAll();
                        waited += System.currentTimeMillis() - then;
                    }
                    CPUProfile.Frame curf = null;
                    if (Config.profile)
                        current.pf = curf = rprof.new Frame();
                    synchronized (curdraw) {
                        curdraw[0] = current;
                    }
                    uglyjoglhack();
                    if (curf != null) {
                        curf.tick("aux");
                        curf.fin();
                    }
                    long now = System.currentTimeMillis();
                    waited += now - current.doneat;
                    ridle = (ridle * 0.95) + (((double) waited / ((double) (now - then))) * 0.05);
                    current = null; /* Just for the GC. */
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    };

    public void run() {
        try {
            Thread drawthread = new HackThread(drawfun, "Render thread");
            drawthread.start();
            try {
                long now, then;
                long frames[] = new long[128];
                int framep = 0, waited[] = new int[128];
                while (true) {
                    int fwaited = 0;
                    Debug.cycle();
                    UI ui = this.ui;
                    then = System.currentTimeMillis();
                    CPUProfile.Frame curf = null;
                    if (Config.profile)
                        curf = uprof.new Frame();
                    synchronized (ui) {
                        if (ui.sess != null)
                            ui.sess.glob.ctick();
                        dispatch();
                        ui.tick();
                        if ((ui.root.sz.x != w) || (ui.root.sz.y != h))
                            ui.root.resize(new Coord(w, h));
                    }
                    if (curf != null)
                        curf.tick("dsp");
                    if (curf != null)
                        curf.tick("draw");
                    synchronized (drawfun) {
                        now = System.currentTimeMillis();
                        while (bufdraw != null)
                            drawfun.wait();
                        drawfun.notifyAll();
                        fwaited += System.currentTimeMillis() - now;
                    }
                    if (curf != null)
                        curf.tick("aux");

                    now = System.currentTimeMillis();
                    long fd = bgmode ? this.bgfd : this.fd;
                    if (now - then < fd) {
                        synchronized (events) {
                            events.wait(fd - (now - then));
                        }
                        fwaited += System.currentTimeMillis() - now;
                    }

                    frames[framep] = now;
                    waited[framep] = fwaited;
                    for (int i = 0, ckf = framep, twait = 0; i < frames.length; i++) {
                        ckf = (ckf - 1 + frames.length) % frames.length;
                        twait += waited[ckf];
                        if (now - frames[ckf] > 1000) {
                            fps = i;
                            uidle = ((double) twait) / ((double) (now - frames[ckf]));
                            break;
                        }
                    }
                    framep = (framep + 1) % frames.length;

                    if (curf != null)
                        curf.tick("wait");
                    if (curf != null)
                        curf.fin();
                    if (Thread.interrupted())
                        throw (new InterruptedException());
                }
            } finally {
                drawthread.interrupt();
                drawthread.join();
            }
        } catch (InterruptedException e) {
        } finally {
            ui.destroy();
        }
    }

    private Map<String, Console.Command> cmdmap = new TreeMap<String, Console.Command>();

    {
        cmdmap.put("hz", new Console.Command() {
            public void run(Console cons, String[] args) {
                fd = 1000 / Integer.parseInt(args[1]);
            }
        });
        cmdmap.put("bghz", new Console.Command() {
            public void run(Console cons, String[] args) {
                bgfd = 1000 / Integer.parseInt(args[1]);
                Utils.setprefi("bghz", (int) bgfd);
            }
        });
    }

    public Map<String, Console.Command> findcmds() {
        return (cmdmap);
    }

}
