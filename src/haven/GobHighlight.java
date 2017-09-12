package haven;

import java.awt.*;

public class GobHighlight extends GAttrib {
    private float[] emi = {1.0f, 0.0f, 1.0f, 0.0f};
    private float[] clr = Utils.c2fa(new Color(255, 0, 255, 0));
    private boolean inc = true;
    private static final float EMI_STEP = 0.0625f;
    private static final float ALPHA_STEP = 0.0627450980484f;
    private long lasttime = System.currentTimeMillis();
    public long cycle = 6;

    public GobHighlight(Gob g) {
        super(g);
    }

    public Object staticp() {
        return null;
    }
}
