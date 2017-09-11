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

public class IBox {
    public final Tex ctl, ctr, cbl, cbr;
    public final Tex bl, br, bt, bb;

    public IBox(Tex ctl, Tex ctr, Tex cbl, Tex cbr, Tex bl, Tex br, Tex bt, Tex bb) {
        this.ctl = ctl;
        this.ctr = ctr;
        this.cbl = cbl;
        this.cbr = cbr;
        this.bl = bl;
        this.br = br;
        this.bt = bt;
        this.bb = bb;
    }

    public IBox(String base, String ctl, String ctr, String cbl, String cbr, String bl, String br, String bt, String bb) {
        this(Resource.loadtex(base + "/" + ctl),
                Resource.loadtex(base + "/" + ctr),
                Resource.loadtex(base + "/" + cbl),
                Resource.loadtex(base + "/" + cbr),
                Resource.loadtex(base + "/" + bl),
                Resource.loadtex(base + "/" + br),
                Resource.loadtex(base + "/" + bt),
                Resource.loadtex(base + "/" + bb));
    }

    public Coord btloff() {
        return (new Coord(bl.sz().x, bt.sz().y));
    }

    public Coord ctloff() {
        return (ctl.sz());
    }

    public Coord bisz() {
        return (new Coord(bl.sz().x + br.sz().x, bt.sz().y + bb.sz().y));
    }

    public Coord cisz() {
        return (ctl.sz().add(cbr.sz()));
    }

    @Deprecated
    public Coord tloff() {
        return (btloff());
    }

    @Deprecated
    public Coord bsz() {
        return (cisz());
    }
}
