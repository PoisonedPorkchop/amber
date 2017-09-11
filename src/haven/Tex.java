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

public abstract class Tex {
    protected Coord dim;

    public Tex(Coord sz) {
        dim = sz;
    }

    public Coord sz() {
        return (dim);
    }

    public static int nextp2(int in) {
        int h = Integer.highestOneBit(in);
        return ((h == in) ? h : (h * 2));
    }

    /* Render texture coordinates from ul to br at c to c + sz, scaling if necessary. */

    public abstract float tcx(int x);

    public abstract float tcy(int y);

    public abstract GLState draw();

    public abstract GLState clip();

    public void dispose() {
    }

    public static final Tex empty = new Tex(Coord.z) {
        public void render(GOut g, Coord c, Coord ul, Coord br, Coord sz) {
        }

        public float tcx(int x) {
            return (0);
        }

        public float tcy(int y) {
            return (0);
        }

        public GLState draw() {
            return (null);
        }

        public GLState clip() {
            return (null);
        }
    };
}
