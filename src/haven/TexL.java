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

import haven.Defer.Future;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.LinkedList;

public abstract class TexL extends Tex{
    protected Mipmapper mipmap = null;
    private Future<Prepared> decode = null;

    public abstract BufferedImage fill();

    public TexL(Coord sz) {
        super(sz);
    }

    public void mipmap(Mipmapper mipmap) {
        this.mipmap = mipmap;
    }

    private class Prepared {
        BufferedImage img;
        byte[][] data;
        int ifmt;

        private Prepared() {
            img = fill();
            ifmt = TexI.detectfmt(img);
            LinkedList<byte[]> data = new LinkedList<byte[]>();
            if ((ifmt == GL.GL_RGB) || (ifmt == GL2.GL_BGR)) {
                if ((mipmap != null) && !(mipmap instanceof Mipmapper.Mipmapper3))
                    ifmt = -1;
            }
            if ((ifmt == GL.GL_RGB) || (ifmt == GL2.GL_BGR)) {
                byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                data.add(pixels);
            } else {
                byte[] pixels;
                if ((ifmt == GL.GL_RGBA) || (ifmt == GL.GL_BGRA)) {
                    pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                } else {
                    ifmt = GL.GL_RGBA;
                }
            }
            this.data = data.toArray(new byte[0][]);
        }
    }

    private Future<Prepared> prepare() {
        return (Defer.later(new Defer.Callable<Prepared>() {
            public Prepared call() {
                Prepared ret = new Prepared();
                return (ret);
            }

            public String toString() {
                String nm = loadname();
                if (nm != null)
                    return ("Finalizing " + nm + "...");
                else
                    return ("Finalizing texture...");
            }
        }));
    }

    public String loadname() {
        return (null);
    }

}
