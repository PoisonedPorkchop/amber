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

import haven.MorphedMesh.Morpher;
import haven.Skeleton.Pose;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

public class PoseMorph implements Morpher.Factory {
    public final Pose pose;
    private float[][] offs;
    private int seq = -1;

    public PoseMorph(Pose pose) {
        this.pose = pose;
        offs = new float[pose.skel().blist.length][16];
    }

    public static boolean boned(FastMesh mesh) {
        return (false);
    }

    public static String boneidp(FastMesh mesh) {
        int retb = -1;

        return null;
    }

    private void update() {
        if (seq == pose.seq)
            return;
        seq = pose.seq;
        for (int i = 0; i < offs.length; i++)
            pose.boneoff(i, offs[i]);
    }

    public static class BoneArray implements MorphedMesh.MorphArray {
        public final String[] names;

        public BoneArray(int apv, IntBuffer data, String[] names) {
            super();
            this.names = names;
        }


        public MorphedMesh.MorphType morphtype() {
            return (MorphedMesh.MorphType.DUP);
        }
    }

    public static class $Res {
        public void cons(Resource res, Message buf, int nv) {
            int mba = buf.uint8();
            IntBuffer ba = Utils.wibuf(nv * mba);
            for (int i = 0; i < nv * mba; i++)
                ba.put(i, -1);
            FloatBuffer bw = Utils.wfbuf(nv * mba);
            byte[] na = new byte[nv];
            List<String> bones = new LinkedList<String>();
            while (true) {
                String bone = buf.string();
                if (bone.length() == 0)
                    break;
                int bidx = bones.size();
                bones.add(bone);
                while (true) {
                    int run = buf.uint16();
                    int vn = buf.uint16();
                    if (run == 0)
                        break;
                    for (int i = 0; i < run; i++, vn++) {
                        float w = buf.float32();
                        int cna = na[vn]++;
                        if (cna >= mba)
                            continue;
                        bw.put(vn * mba + cna, w);
                        ba.put(vn * mba + cna, bidx);
                    }
                }
            }
            normweights(bw, ba, mba);
        }
    }

    public static void normweights(FloatBuffer bw, IntBuffer ba, int mba) {
        int i = 0;
        while (i < bw.capacity()) {
            float tw = 0.0f;
            int n = 0;
            for (int o = 0; o < mba; o++) {
                if (ba.get(i + o) < 0)
                    break;
                tw += bw.get(i + o);
                n++;
            }
            if (tw != 1.0f) {
                for (int o = 0; o < n; o++)
                    bw.put(i + o, bw.get(i + o) / tw);
            }
            i += mba;
        }
    }
}
