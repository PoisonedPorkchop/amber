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

import haven.mod.HavenMod;
import haven.mod.Mod;
import haven.mod.ModAPI;
import haven.mod.event.CustomMenuButtonPressEvent;
import haven.mod.event.InventoryCreateEvent;
import haven.mod.event.RunStateChangeEvent;
import haven.mod.event.UIMessageEvent;
import haven.mod.event.flower.FlowerMenuCancelEvent;
import haven.mod.event.flower.FlowerMenuChooseEvent;
import haven.mod.event.flower.FlowerMenuChosenEvent;
import haven.mod.event.flower.FlowerMenuCreateEvent;
import haven.mod.event.widget.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


public class MainFrame implements Runnable, Console.Directory {
    HavenPanel p;
    private final ThreadGroup g;
    public final Thread mt;
    DisplayMode fsmode = null, prefs = null;
    private static final String TITLE = "Ezkutuko Village (Client v1.0) (!G)";

    static {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());

            // Since H&H IPs aren't likely to change (at least mid client run), and the client constantly needs to fetch
            // resources from the server, we enable "cache forever" policy so to overcome sporadic UnknownHostException
            // due to flaky DNS. Bad practice, but still better than forcing the user to modify hosts file.
            // NOTE: this needs to be done early as possible before InetAddressCachePolicy is initialized.
            java.security.Security.setProperty("networkaddress.cache.ttl" , "-1");
        } catch (Exception e) {
        }
    }

    public boolean hasfs() {
        return (prefs != null);
    }

    private Map<String, Console.Command> cmdmap = new TreeMap<String, Console.Command>();

    {
        cmdmap.put("sz", new Console.Command() {
            public void run(Console cons, String[] args) {
                if (args.length == 3) {
                    int w = Integer.parseInt(args[1]),
                            h = Integer.parseInt(args[2]);
                    Utils.setprefc("wndsz", new Coord(w, h));
                } else if (args.length == 2) {
                    if (args[1].equals("dyn")) {
                        Utils.setprefb("wndlock", false);
                    } else if (args[1].equals("lock")) {
                        Utils.setprefb("wndlock", true);
                    }
                }
            }
        });
    }

    public Map<String, Console.Command> findcmds() {
        return (cmdmap);
    }

    public MainFrame(Coord isz) {
        Coord sz;
        if (isz == null) {
            sz = Utils.getprefc("wndsz", new Coord(800, 600));
            if (sz.x < 640) sz.x = 640;
            if (sz.y < 480) sz.y = 480;
        } else {
            sz = isz;
        }
        this.g = new ThreadGroup(HackThread.tg(), "Haven client");
        this.mt = new HackThread(this.g, this, "Haven main thread");
        p = new HavenPanel(sz.x, sz.y);
        p.init();
    }

    public void run() {
        if (Thread.currentThread() != this.mt)
            throw (new RuntimeException("MainFrame is being run from an invalid context"));
        Thread ui = new HackThread(p, "Haven UI thread");
        ui.start();
        try {
            try {
                Session sess = null;
                while (true) {
                    UI.Runner fun;
                    if (sess == null) {
                        Bootstrap bill = new Bootstrap(Config.defserv, Config.mainport);
                        if ((Config.authuser != null) && (Config.authck != null)) {
                            bill.setinitcookie(Config.authuser, Config.authck);
                            Config.authck = null;
                        }
                        fun = bill;
                        System.out.println("Welcome to " + TITLE);
                    } else {
                        fun = new RemoteUI(sess);
                        System.out.println("Welcome to " + TITLE + " \u2013 " + sess.username);
                    }
                    sess = fun.run(p.newui(sess));
                }
            } catch (InterruptedException e) {
            }
        } finally {
            ui.interrupt();
        }
    }

    public static void setupres() {
        if (ResCache.global != null)
            Resource.setcache(ResCache.global);
        if (Config.resurl != null)
            Resource.addurl(Config.resurl);
        if (ResCache.global != null) {
            try {
                Resource.loadlist(Resource.remote(), ResCache.global.fetch("tmp/allused"), -10);
            } catch (IOException e) {
            }
        }
        if (!Config.nopreload) {
            try {
                InputStream pls;
                pls = Resource.class.getResourceAsStream("res-preload");
                if (pls != null)
                    Resource.loadlist(Resource.remote(), pls, -5);
                pls = Resource.class.getResourceAsStream("res-bgload");
                if (pls != null)
                    Resource.loadlist(Resource.remote(), pls, -10);
            } catch (IOException e) {
                throw (new Error(e));
            }
        }
    }

    static {
        WebBrowser.self = DesktopBrowser.create();
    }

    private static void javabughack() throws InterruptedException {
	    /* Work around a stupid deadlock bug in AWT. */
        try {
            javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    PrintStream bitbucket = new PrintStream(new ByteArrayOutputStream());
                    bitbucket.print(LoginScreen.textf);
                    bitbucket.print(LoginScreen.textfs);
                }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
	        /* Oh, how I love Swing! */
            throw (new Error(e));
        }
    }

    private static void main2(String[] args) {
        Config.cmdline(args);

        if (Config.playerposfile != null)
            new Thread(new PlayerPosStreamer(), "Player position thread").start();

        try {
            javabughack();
        } catch (InterruptedException e) {
            return;
        }
        setupres();
        MainFrame f = new MainFrame(null);
        Mod mod = new Mod();
        mod.getAPI().create();

        f.mt.start();
        try {
            f.mt.join();
        } catch (InterruptedException e) {
            f.g.interrupt();
            return;
        }
        dumplist(Resource.remote().loadwaited(), null);
        dumplist(Resource.remote().cached(), null);
        if (ResCache.global != null) {
            try {
                Writer w = new OutputStreamWriter(ResCache.global.store("tmp/allused"), "UTF-8");
                try {
                    Resource.dumplist(Resource.remote().used(), w);
                } finally {
                    w.close();
                }
            } catch (IOException e) {
            }
        }
        System.exit(0);
    }

    public static void main(final String[] args) {
	    /* Set up the error handler as early as humanly possible. */
        final haven.error.ErrorHandler hg = new haven.error.ErrorHandler();
        hg.sethandler(new haven.error.ErrorGui(null) {
            public void errorsent() {
                hg.interrupt();
            }
        });
        ThreadGroup g = hg;

        Thread main = new HackThread(g, new Runnable() {
            public void run() {
                main2(args);
            }
        }, "Haven main thread");
        main.start();
    }

    private static void dumplist(Collection<Resource> list, String fn) {
        try {
            if (fn != null) {
                Writer w = new OutputStreamWriter(new FileOutputStream(fn), "UTF-8");
                try {
                    Resource.dumplist(list, w);
                } finally {
                    w.close();
                }
            }
        } catch (IOException e) {
            throw (new RuntimeException(e));
        }
    }
}
