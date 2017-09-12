package haven.resutil;

import haven.Sprite;
import haven.Utils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class BPRadSprite extends Sprite {
    final ShortBuffer sidx;


    public BPRadSprite(float rad, float basez) {
        super(null, null);
        
        int per = Math.max(24, (int) (2 * Math.PI * (double) rad / 11.0D));
        FloatBuffer pa = Utils.mkfbuf(per * 3 * 2);
        FloatBuffer na = Utils.mkfbuf(per * 3 * 2);
        ShortBuffer sa = Utils.mksbuf(per * 6);

        for (int i = 0; i < per; ++i) {
            float s = (float) Math.sin(2 * Math.PI * (double) i / (double) per);
            float c = (float) Math.cos(2 * Math.PI * (double) i / (double) per);
            pa.put(i * 3 + 0, c * rad).put(i * 3 + 1, s * rad).put(i * 3 + 2, 10.0F);
            pa.put((per + i) * 3 + 0, c * rad).put((per + i) * 3 + 1, s * rad).put((per + i) * 3 + 2, basez);
            na.put(i * 3 + 0, c).put(i * 3 + 1, s).put(i * 3 + 2, 0.0F);
            na.put((per + i) * 3 + 0, c).put((per + i) * 3 + 1, s).put((per + i) * 3 + 2, 0.0F);
            int v = i * 6;
            sa.put(v + 0, (short) i).put(v + 1, (short) (i + per)).put(v + 2, (short) ((i + 1) % per));
            sa.put(v + 3, (short) (i + per)).put(v + 4, (short) ((i + 1) % per + per)).put(v + 5, (short) ((i + 1) % per));
        }

        this.sidx = sa;
    }
}
