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

import javax.media.opengl.*;
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
    public static final GLState.Slot<GLState> global = new GLState.Slot<GLState>(GLState.Slot.Type.SYS, GLState.class);
    public static final GLState.Slot<GLState> proj2d = new GLState.Slot<GLState>(GLState.Slot.Type.SYS, GLState.class, global);
    private GLState gstate, rtstate, ostate;
    private Throwable uncaught = null;
    private GLState.Applier state = null;
    private GLConfig glconf = null;
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
    }

    public HavenPanel(int w, int h) {
        this(w, h, null);
    }

    public static abstract class OrthoState extends GLState {
        protected abstract Coord sz();

        public void apply(GOut g) {
            Coord sz = sz();
            g.st.proj = Projection.makeortho(new Matrix4f(), 0, sz.x, sz.y, 0, -1, 1);
        }

        public void unapply(GOut g) {
        }

        public void prep(Buffer buf) {
            buf.put(proj2d, this);
        }

        public static OrthoState fixed(final Coord sz) {
            return (new OrthoState() {
                protected Coord sz() {
                    return (sz);
                }
            });
        }
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
        if (glconf != null)
            ui.cons.add(glconf);
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
        BufferBGL buf;
        CPUProfile.Frame pf;
        CurrentGL on;
        long doneat;

        Frame(BufferBGL buf, CurrentGL on) {
            this.buf = buf;
            this.on = on;
        }
    }

    private Frame[] curdraw = {null};

    void redraw(GL2 gl) {
        if (uncaught != null)
            throw (new RuntimeException("Exception occurred during init but was somehow discarded", uncaught));
        if ((state == null) || (state.cgl.gl != gl))
            state = new GLState.Applier(new CurrentGL(gl, glconf));

        Frame f;
        synchronized (curdraw) {
            f = curdraw[0];
            curdraw[0] = null;
        }
        if ((f != null) && (f.on.gl == gl)) {
            GPUProfile.Frame curgf = null;
            if (Config.profilegpu)
                curgf = gprof.new Frame((GL3) gl);
            if (f.pf != null)
                f.pf.tick("awt");
            f.buf.run(gl);
            GOut.checkerr(gl);
            if (f.pf != null)
                f.pf.tick("gl");
            if (curgf != null) {
                curgf.tick("draw");
                curgf.fin();
            }

            if (glconf.pref.dirty) {
                glconf.pref.save();
                glconf.pref.dirty = false;
            }
            f.doneat = System.currentTimeMillis();
        }
    }

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
            synchronized (drawfun) {
                while (state == null)
                    drawfun.wait();
            }
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

                    BufferBGL buf = new BufferBGL();
                    GLState.Applier state = this.state;
                    if (curf != null)
                        curf.tick("draw");
                    synchronized (drawfun) {
                        now = System.currentTimeMillis();
                        while (bufdraw != null)
                            drawfun.wait();
                        bufdraw = new Frame(buf, state.cgl);
                        drawfun.notifyAll();
                        fwaited += System.currentTimeMillis() - now;
                    }

                    ui.audio.cycle();
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
