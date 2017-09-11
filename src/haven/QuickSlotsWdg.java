package haven;

public class QuickSlotsWdg extends Widget implements DTarget {
    private static final Tex sbg = Resource.loadtex("gfx/hud/slots");
    public static final Coord lc =  new Coord(6, 6);
    public static final Coord rc = new Coord(56, 6);
    private static final Coord ssz = new Coord(44, 44);
    private UI.Grab dragging;
    private Coord dc;

    public QuickSlotsWdg() {
        super(new Coord(44 + 44 + 6, 44));
    }

    @Override
    public boolean drop(Coord cc, Coord ul) {
        Equipory e = gameui().getequipory();
        if (e != null) {
            e.wdgmsg("drop", cc.x <= 47 ? 6 : 7);
            return true;
        }
        return false;
    }

    @Override
    public boolean iteminteract(Coord cc, Coord ul) {
        Equipory e = gameui().getequipory();
        if (e != null) {
            WItem w = e.quickslots[cc.x <= 47 ? 6 : 7];
            if (w != null) {
                return w.iteminteract(cc, ul);
            }
        }
        return false;
    }

    @Override
    public boolean mousedown(Coord c, int button) {
       if (ui.modmeta)
            return true;
        Equipory e = gameui().getequipory();
        if (e != null) {
            WItem w = e.quickslots[c.x <= 47 ? 6 : 7];
            if (w != null) {
                dragging = null;
                w.mousedown(new Coord(w.sz.x / 2, w.sz.y / 2), button);
                return true;
            } else if (button == 1) {
                dragging = ui.grabmouse(this);
                dc = c;
                return true;
            }
        }
        return false;
    }

    public void simulateclick(Coord c) {
        Equipory e = gameui().getequipory();
        if (e != null) {
            WItem w = e.quickslots[c.x <= 47 ? 6 : 7];
            if (w != null)
                w.item.wdgmsg("take", new Coord(w.sz.x / 2, w.sz.y / 2));
        }
    }

    @Override
    public boolean mouseup(Coord c, int button) {
        if (dragging != null) {
            dragging.remove();
            dragging = null;
            Utils.setprefc("quickslotsc", this.c);
            return true;
        }
        return super.mouseup(c, button);
    }

    @Override
    public void mousemove(Coord c) {
        if (dragging != null) {
            this.c = this.c.add(c.x, c.y).sub(dc);
            return;
        }
        super.mousemove(c);
    }
}