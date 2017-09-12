package haven;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class PartyMemberOutline extends Sprite {
    private final ShortBuffer eidx;
    private Coord2d lc;


    protected PartyMemberOutline(Owner owner, Color color) {
        super(owner, null);
        float rad = 50 / 10.0F;
        int i = Math.max(24, (int) (Math.PI * 2 * rad / 11.0D));
        FloatBuffer posa = Utils.mkfbuf(i * 3);
        FloatBuffer nrma = Utils.mkfbuf(i * 3);
        ShortBuffer eidx = Utils.mksbuf(i);
        for (int j = 0; j < i; j++) {
            float sin = (float) Math.sin(Math.PI * 2 * j / i);
            float cos = (float) Math.cos(Math.PI * 2 * j / i);
            posa.put(j * 3, cos * rad).put(j * 3 + 1, sin * rad).put(j * 3 + 2, 0.1f);
            nrma.put(j * 3, cos).put(j * 3 + 1, sin).put(j * 3 + 2, 0.0F);
            eidx.put(j, (short) j);
        }
        this.eidx = eidx;
    }

    @Override
    public boolean tick(int dt) {
        Coord2d c = ((Gob) this.owner).rc;
        if ((this.lc == null) || (!this.lc.equals(c))) {
            setz(this.owner.glob(), c);
            this.lc = c;
        }
        return false;
    }

    private void setz(Glob glob, Coord2d c) {
    }
}