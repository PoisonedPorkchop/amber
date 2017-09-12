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

import haven.automation.WItemDestroyCallback;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

import static haven.Inventory.sqsz;

public class WItem extends Widget implements DTarget {
    public static final Resource missing = Resource.local().loadwait("gfx/invobjs/missing");
    public final GItem item;
    public static final Color famountclr = new Color(24, 116, 205);
    private static final Color qualitybg = new Color(20, 20, 20, 255 - Config.qualitybgtransparency);
    public static final Color[] wearclr = new Color[]{
            new Color(233, 0, 14), new Color(218, 128, 87), new Color(246, 233, 87), new Color(145, 225, 60)
    };

    private WItemDestroyCallback destroycb;

    public WItem(GItem item) {
        super(sqsz);
        this.item = item;
    }

    public static BufferedImage shorttip(List<ItemInfo> info) {
        return (ItemInfo.shorttip(info));
    }

    public static BufferedImage longtip(GItem item, List<ItemInfo> info) {
        BufferedImage img = ItemInfo.longtip(info);
        Resource.Pagina pg = item.res.get().layer(Resource.pagina);
        if (pg != null)
            img = ItemInfo.catimgs(0, img, RichText.render("\n" + pg.text, 200).img);
        return (img);
    }

    public BufferedImage longtip(List<ItemInfo> info) {
        return (longtip(item, info));
    }

    public class ItemTip implements Indir<Tex> {
        private final TexI tex;

        public ItemTip(BufferedImage img) {
            if (img == null)
                throw (new Loading());
            tex = new TexI(img);
        }

        public GItem item() {
            return (item);
        }

        public Tex get() {
            return null;
        }
    }

    public class ShortTip extends ItemTip {
        public ShortTip(List<ItemInfo> info) {
            super(shorttip(info));
        }
    }

    public class LongTip extends ItemTip {
        public LongTip(List<ItemInfo> info) {
            super(longtip(info));
        }
    }

    private long hoverstart;
    private ItemTip shorttip = null, longtip = null;
    private List<ItemInfo> ttinfo = null;

    public Object tooltip(Coord c, Widget prev) {
        long now = System.currentTimeMillis();
        if (prev == this) {
        } else if (prev instanceof WItem) {
            long ps = ((WItem) prev).hoverstart;
            if (now - ps < 1000)
                hoverstart = now;
            else
                hoverstart = ps;
        } else {
            hoverstart = now;
        }
        try {
            List<ItemInfo> info = item.info();
            if (info.size() < 1)
                return (null);
            if (info != ttinfo) {
                shorttip = longtip = null;
                ttinfo = info;
            }
            if (now - hoverstart < 1000) {
                if (shorttip == null)
                    shorttip = new ShortTip(info);
                return (shorttip);
            } else {
                if (longtip == null)
                    longtip = new LongTip(info);
                return (longtip);
            }
        } catch (Loading e) {
            return ("...");
        }
    }

    public volatile static int cacheseq = 0;

    public class AttrCache<T> {
        private final Function<List<ItemInfo>, T> data;
        private List<ItemInfo> forinfo = null;
        public T save = null;
        private int forseq = -1;

        public AttrCache(Function<List<ItemInfo>, T> data) {this.data = data;}

        public T get() {
            try {
                List<ItemInfo> info = item.info();
                if ((cacheseq != forseq) || (info != forinfo)) {
                    save = data.apply(info);
                    forinfo = info;
                    forseq = cacheseq;
                }
            } catch (Loading e) {
                return (null);
            }
            return (save);
        }
    }

    public final AttrCache<Color> olcol = new AttrCache<Color>(info -> {
        Color ret = null;
        for(ItemInfo inf : info) {
            if(inf instanceof GItem.ColorInfo) {
                Color c = ((GItem.ColorInfo)inf).olcol();
                if(c != null)
                    ret = (ret == null)?c:Utils.preblend(ret, c);
            }
        }
        return(ret);
    });

    public final AttrCache<Tex> itemnum = new AttrCache<>(info -> {
        GItem.NumberInfo ninf = ItemInfo.find(GItem.NumberInfo.class, info);
        if (ninf == null)
            return null;

        if (ninf instanceof GItem.GildingInfo && ((GItem.GildingInfo) ninf).hasGildableSlots())
            return Text.renderstroked(ninf.itemnum() + "", new Color(0, 169, 224), Color.BLACK).tex();

        return Text.renderstroked(ninf.itemnum() + "", ninf.numcolor(), Color.BLACK).tex();
    });

    public final AttrCache<Double> itemmeter = new AttrCache<Double>(info -> {
        GItem.MeterInfo minf = ItemInfo.find(GItem.MeterInfo.class, info);
        return((minf == null)?null:minf.meter());
    });

    private GSprite lspr = null;

    public void tick(double dt) {
    /* XXX: This is ugly and there should be a better way to
     * ensure the resizing happens as it should, but I can't think
	 * of one yet. */
        GSprite spr = item.spr();
        if ((spr != null) && (spr != lspr)) {
            Coord sz = new Coord(spr.sz());
            if ((sz.x % sqsz.x) != 0)
                sz.x = sqsz.x * ((sz.x / sqsz.x) + 1);
            if ((sz.y % sqsz.y) != 0)
                sz.y = sqsz.y * ((sz.y / sqsz.y) + 1);
            resize(sz);
            lspr = spr;
        }
    }

    public boolean mousedown(Coord c, int btn) {
        if (btn == 1) {
            if (ui.modctrl && ui.modmeta)
                wdgmsg("drop-identical", this.item);
            else if (ui.modctrl && ui.modshift) {
                String name = ItemInfo.find(ItemInfo.Name.class, item.info()).str.text;
                name = name.replace(' ', '_');
                if (!Resource.language.equals("en")) {
                    int i = name.indexOf('(');
                    if (i > 0)
                        name = name.substring(i + 1, name.length() - 1);
                }
                try {
                    WebBrowser.self.show(new URL(String.format("http://ringofbrodgar.com/wiki/%s", name)));
                } catch (WebBrowser.BrowserException e) {
                    getparent(GameUI.class).error("Could not launch web browser.");
                } catch (MalformedURLException e) {
                }
            } else if (ui.modshift && !ui.modmeta) {
                // server side transfer all identical: pass third argument -1 (or 1 for single item)
                item.wdgmsg("transfer", c);
            } else if (ui.modctrl)
                item.wdgmsg("drop", c);
            else if (ui.modmeta)
                wdgmsg("transfer-identical", this.item);
            else
                item.wdgmsg("take", c);
            return (true);
        } else if (btn == 3) {
            if (ui.modmeta && !(parent instanceof Equipory))
                wdgmsg("transfer-identical-asc", this.item);
            else
                item.wdgmsg("iact", c, ui.modflags());
            return (true);
        }
        return (false);
    }

    public boolean drop(Coord cc, Coord ul) {
        return (false);
    }

    public boolean iteminteract(Coord cc, Coord ul) {
        item.wdgmsg("itemact", ui.modflags());
        return (true);
    }

    @Override
    public void reqdestroy() {
        super.reqdestroy();
        if (destroycb != null)
            destroycb.notifyDestroy();
    }

    public void registerDestroyCallback(WItemDestroyCallback cb) {
        this.destroycb = cb;
    }
}
