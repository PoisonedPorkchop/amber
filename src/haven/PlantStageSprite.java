package haven;

import java.awt.*;


public class PlantStageSprite extends Sprite {
    private static final Color stagecolor = new Color(255, 227, 168);
    private static final Tex stgmaxtex = Text.renderstroked("\u25CF", new Color(254, 100, 100), Color.BLACK, Text.num12boldFnd).tex();
    private static final Tex stghrvtex = Text.renderstroked("\u25CF", new Color(201, 180, 0), Color.BLACK, Text.num12boldFnd).tex();
    private static final Tex[] stgtex = new Tex[]{
            Text.renderstroked("2", stagecolor, Color.BLACK, Text.num12boldFnd).tex(),
            Text.renderstroked("3", stagecolor, Color.BLACK, Text.num12boldFnd).tex(),
            Text.renderstroked("4", stagecolor, Color.BLACK, Text.num12boldFnd).tex(),
            Text.renderstroked("5", stagecolor, Color.BLACK, Text.num12boldFnd).tex(),
            Text.renderstroked("6", stagecolor, Color.BLACK, Text.num12boldFnd).tex()
    };
    public int stg;
    private Tex tex;
    private static Matrix4f mv = new Matrix4f();
    private Coord wndsz;
    private final boolean multistg;

    public PlantStageSprite(int stg, int stgmax, boolean multistg) {
        super(null, null);
        this.multistg = multistg;
        update(stg, stgmax);
    }

    public void update(int stg, int stgmax) {
        this.stg = stg;
        if (multistg && stg == stgmax - 1)
            tex = stghrvtex;
        else if (stg == stgmax)
            tex = stgmaxtex;
        else
            tex = stgtex[stg - 1];
    }

}
