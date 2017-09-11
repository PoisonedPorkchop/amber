package haven;

import javax.media.opengl.GL2;
import java.awt.*;

public class GobArcheryVector extends Sprite {
    private static final float[] friend = Utils.c2fa(new Color(78, 154, 6));
    private static final float[] foe = Utils.c2fa(new Color(164, 0, 6));
    private final static int DISTANCE = 400;
    private float[] clr;
    private final Gob followGob;

    public GobArcheryVector(Gob pl, Gob followGob) {
        super(pl, null);
        this.followGob = followGob;
    }

    public boolean setup(RenderList rl) {
        Gob gob = (Gob) owner;
        Location.goback(rl.state(), "gobx");
        rl.prepo(States.xray);
        clr = gob.isplayer() || gob.isFriend() ? friend : foe;
        rl.prepo(States.vertexcolor);
        return true;
    }
}
