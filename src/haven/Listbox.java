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

public abstract class Listbox<T> extends ListWidget<T> {
    public int h;
    public final Scrollbar sb;

    public Listbox(int w, int h, int itemh) {
        super(new Coord(w, h * itemh), itemh);
        this.h = h;
        this.sb = adda(new Scrollbar(sz.y, 0, 0), sz.x, 0, 1, 0);
    }

    public boolean mousewheel(Coord c, int amount) {
        sb.ch(amount);
        return (true);
    }

    protected void itemclick(T item, int button) {
        if (button == 1)
            change(item);
    }

    public T itemat(Coord c) {
        int idx = (c.y / itemh) + sb.val;
        if (idx >= listitems())
            return (null);
        return (listitem(idx));
    }

    public boolean mousedown(Coord c, int button) {
        if (super.mousedown(c, button))
            return (true);
        T item = itemat(c);
        if ((item == null) && (button == 1))
            change(null);
        else if (item != null)
            itemclick(item, button);
        return (true);
    }

    public void display(int idx) {
        if (idx < sb.val) {
            sb.val = idx;
        } else if (idx >= sb.val + h) {
            sb.val = Math.max(idx - (h - 1), 0);
        }
    }

    public void display(T item) {
        int p = find(item);
        if (p >= 0)
            display(p);
    }

    public void display() {
        display(sel);
    }

    public void resize(Coord sz) {
	super.resize(sz);
    this.h = Math.max(sz.y / itemh, 1);
	sb.resize(sz.y);
	sb.c = new Coord(sz.x - sb.sz.x, 0);
    }
}
