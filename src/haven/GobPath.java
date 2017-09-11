package haven;

import javax.media.opengl.GL2;
import java.awt.*;

public class GobPath extends Sprite {
    private static final States.ColState clrst = new States.ColState(new Color(233, 185, 110));

    public GobPath(Gob gob) {
        super(gob, null);
    }

    public boolean setup(RenderList rl) {
        Location.goback(rl.state(), "gobx");
        rl.prepo(States.xray);
        rl.prepo(clrst);
        return true;
    }

}
