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

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;

public class TexI extends Tex{
    public static ComponentColorModel glcm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8, 8}, true, false, ComponentColorModel.TRANSLUCENT, DataBuffer.TYPE_BYTE);
    public BufferedImage back;
    public boolean mutable;
    private int fmt = GL.GL_RGBA;
    public Mipmapper mmalg = Mipmapper.avg;

    public TexI(BufferedImage img) {
        super(new Coord(0,0));
        back = img;
        mutable = false;
    }

    public TexI(Coord sz) {
        super(sz);
        mutable = true;
    }

    @Override
    public float tcx(int x) {
        return 0;
    }

    @Override
    public float tcy(int y) {
        return 0;
    }

    /* Java's image model is a little bit complex, so these may not be
     * entirely correct. They should be corrected if oddities are
     * detected. */
    public static int detectfmt(BufferedImage img) {
        ColorModel cm = img.getColorModel();
        if (!(img.getSampleModel() instanceof PixelInterleavedSampleModel))
            return (-1);
        PixelInterleavedSampleModel sm = (PixelInterleavedSampleModel) img.getSampleModel();
        int[] cs = cm.getComponentSize();
        int[] off = sm.getBandOffsets();
    /*
    System.err.print(this + ": " + cm.getNumComponents() + ", (");
	for(int i = 0; i < off.length; i++)
	    System.err.print(((i > 0)?" ":"") + off[i]);
	System.err.print("), (");
	for(int i = 0; i < off.length; i++)
	    System.err.print(((i > 0)?" ":"") + cs[i]);
	System.err.print(")");
	System.err.println();
	*/
        if ((cm.getNumComponents() == 4) && (off.length == 4)) {
            if (((cs[0] == 8) && (cs[1] == 8) && (cs[2] == 8) && (cs[3] == 8)) &&
                    (cm.getTransferType() == DataBuffer.TYPE_BYTE) &&
                    (cm.getTransparency() == java.awt.Transparency.TRANSLUCENT)) {
                if ((off[0] == 0) && (off[1] == 1) && (off[2] == 2) && (off[3] == 3))
                    return (GL.GL_RGBA);
                if ((off[0] == 2) && (off[1] == 1) && (off[2] == 0) && (off[3] == 3))
                    return (GL.GL_BGRA);
            }
        } else if ((cm.getNumComponents() == 3) && (off.length == 3)) {
            if (((cs[0] == 8) && (cs[1] == 8) && (cs[2] == 8)) &&
                    (cm.getTransferType() == DataBuffer.TYPE_BYTE) &&
                    (cm.getTransparency() == java.awt.Transparency.OPAQUE)) {
                if ((off[0] == 0) && (off[1] == 1) && (off[2] == 2))
                    return (GL.GL_RGB);
                if ((off[0] == 2) && (off[1] == 1) && (off[2] == 0))
                    return (GL2.GL_BGR);
            }
        }
        return (-1);
    }

    public int getRGB(Coord c) {
        return (back.getRGB(c.x, c.y));
    }

    public TexI mkmask() {
        TexI n = new TexI(back);
        n.fmt = GL.GL_ALPHA;
        return (n);
    }

    public static BufferedImage mkbuf(Coord sz) {
        WritableRaster buf = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, sz.x, sz.y, 4, null);
        BufferedImage tgt = new BufferedImage(glcm, buf, false, null);
        return (tgt);
    }

    public static byte[] convert(BufferedImage img, Coord tsz, Coord ul, Coord sz) {
        WritableRaster buf = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, tsz.x, tsz.y, 4, null);
        BufferedImage tgt = new BufferedImage(glcm, buf, false, null);
        Graphics g = tgt.createGraphics();
        g.drawImage(img, 0, 0, sz.x, sz.y, ul.x, ul.y, ul.x + sz.x, ul.y + sz.y, null);
        g.dispose();
        return (((DataBufferByte) buf.getDataBuffer()).getData());
    }

    public static byte[] convert(BufferedImage img, Coord tsz) {
        return (convert(img, tsz, Coord.z, Utils.imgsz(img)));
    }
}
