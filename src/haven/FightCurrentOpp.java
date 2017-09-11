package haven;

import javax.media.opengl.GL2;
import java.awt.*;

public class FightCurrentOpp extends Sprite {
    private static final States.ColState clrstate = new States.ColState(new Color(204, 0, 0, 200));

    public FightCurrentOpp() {
        super(null, null);
    }

    public boolean setup(RenderList rl) {
        rl.prepo(clrstate);
        return true;
    }

}
