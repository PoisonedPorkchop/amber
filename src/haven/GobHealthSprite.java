package haven;

import java.awt.*;


public class GobHealthSprite extends Sprite {
    private static final Tex hlt0 = Text.renderstroked("25%", new Color(255, 227, 168), Color.BLACK, Text.num12boldFnd).tex();
    private static final Tex hlt1 = Text.renderstroked("50%", new Color(255, 227, 168), Color.BLACK, Text.num12boldFnd).tex();
    private static final Tex hlt2 = Text.renderstroked("75%", new Color(255, 227, 168), Color.BLACK, Text.num12boldFnd).tex();
    public int val;
    private Tex tex;
    private static Matrix4f mv = new Matrix4f();
    private Coord wndsz;

    public GobHealthSprite(int val) {
        super(null, null);
        update(val);
    }

    public void update(int val) {
        this.val = val;
        switch (val - 1) {
            case 0:
                tex = hlt0;
                break;
            case 1:
                tex = hlt1;
                break;
            case 2:
                tex = hlt2;
                break;
        }
    }
}
