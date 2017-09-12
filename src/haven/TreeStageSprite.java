package haven;

import java.awt.*;


public class TreeStageSprite extends Sprite {
    private static final Tex[] treestg = new Tex[90];
    private static final Color stagecolor = new Color(255, 227, 168);//new Color(235, 235, 235);
    public int val;
    private Tex tex;
    private static Matrix4f mv = new Matrix4f();
    private Coord wndsz;

    static {
        for (int i = 10; i < 100; i++) {
            treestg[i - 10] = Text.renderstroked(i + "", stagecolor, Color.BLACK, Text.num12boldFnd).tex();
        }
    }

    public TreeStageSprite(int val) {
        super(null, null);
        update(val);
    }

    public void update(int val) {
        this.val = val;
        tex = treestg[val - 10];
    }
}
