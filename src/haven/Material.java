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

import java.awt.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Material {

    public static final float[] defamb = {0.2f, 0.2f, 0.2f, 1.0f};
    public static final float[] defdif = {0.8f, 0.8f, 0.8f, 1.0f};
    public static final float[] defspc = {0.0f, 0.0f, 0.0f, 1.0f};
    public static final float[] defemi = {0.0f, 0.0f, 0.0f, 1.0f};

    @Resource.PublishedCode(name = "mat")
    public static interface Factory {
	public Material create(Glob glob, Resource res, Message sdt);
    }

    public static Material fromres(Glob glob, Resource res, Message sdt) {
	Factory f = res.getcode(Factory.class, false);
	if(f != null)
	    return(f.create(glob, res, sdt));
	return(res.layer(Material.Res.class).get());
    }

    public static class Res extends Resource.Layer implements Resource.IDLayer<Integer> {
        public final int id;
        private transient Material m;
        private boolean mipmap = false, linear = false;


        public Res(Resource res, int id) {
            res.super();
            this.id = id;
        }

        public Material get() {
            synchronized (this) {
                if (m == null) {
                    m = new Material() {
                        public String toString() {
                            return (super.toString() + "@" + getres().name);
                        }
                    };
                }
                return (m);
            }
        }

        public void init() {
            for (Resource.Image img : getres().layers(Resource.imgc)) {
            }
        }

        public Integer layerid() {
            return (id);
        }
    }

    @Resource.LayerName("mat")
    public static class OldMat implements Resource.LayerFactory<Res> {
        private static Color col(Message buf) {
            return (new Color((int) (buf.cpfloat() * 255.0),
                    (int) (buf.cpfloat() * 255.0),
                    (int) (buf.cpfloat() * 255.0),
                    (int) (buf.cpfloat() * 255.0)));
        }

        public Res cons(final Resource res, Message buf) {
            int id = buf.uint16();
            Res ret = new Res(res, id);
            while (!buf.eom()) {
                String thing = buf.string().intern();
                if (thing == "col") {
                    Color amb = col(buf);
                    Color dif = col(buf);
                    Color spc = col(buf);
                    double shine = buf.cpfloat();
                    Color emi = col(buf);
                } else if (thing == "linear") {
                    ret.linear = true;
                } else if (thing == "mipmap") {
                    ret.mipmap = true;
                } else if (thing == "texlink") {
                    final String nm = buf.string();
                    final int ver = buf.uint16();
                    final int tid = buf.uint16();
                } else if (thing == "light") {
                    String l = buf.string();
                } else {
                    throw (new Resource.LoadException("Unknown material part: " + thing, res));
                }
            }
            return (ret);
        }
    }

    @dolda.jglob.Discoverable
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ResName {
        public String value();
    }

    @Resource.LayerName("mat2")
    public static class NewMat implements Resource.LayerFactory<Res> {
        public Res cons(Resource res, Message buf) {
            int id = buf.uint16();
            Res ret = new Res(res, id);
            while (!buf.eom()) {
                String nm = buf.string();
                Object[] args = buf.list();
                if (nm.equals("linear")) {
            /* XXX: These should very much be removed and
             * specified directly in the texture layer
		     * instead. */
                    ret.linear = true;
                } else if (nm.equals("mipmap")) {
                    ret.mipmap = true;
                } else {
                }
            }
            return (ret);
        }
    }
}
