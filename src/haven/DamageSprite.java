package haven;

import java.awt.*;

public class DamageSprite extends Sprite {
    public static final int ID = -1000;
    private static final Text.Furnace dfrn = new PUtils.BlurFurn(new Text.Foundry(Text.sans, 14, new Color(251, 78, 78)).aa(true), 1, 1, new Color(188, 0, 0));
    private static final Text.Furnace afrn = new PUtils.BlurFurn(new Text.Foundry(Text.sans, 14, new Color(76, 202, 98)).aa(true), 1, 1, new Color(0, 142, 24));
    public int dmg, arm;
    private Tex dmgtex, armtex;
    private Gob gob;
    private static int ywinfix = Config.iswindows ? 2 : 0;

    public DamageSprite(int dmg, boolean isarmor, Gob gob) {
        super(null, null);
        if (isarmor) {
            this.arm = dmg;
            this.armtex = afrn.render(dmg + "").tex();
            this.dmgtex = dfrn.render("").tex();
        } else {
            this.dmg = dmg;
            this.dmgtex = dfrn.render(dmg + "").tex();
            this.armtex = afrn.render("").tex();
        }
        this.gob = gob;
    }

    public DamageSprite(int dmg, int arm, Gob gob) {
        super(null, null);
        this.arm = arm;
        this.armtex = afrn.render(arm + "").tex();
        this.dmg = dmg;
        this.dmgtex = dfrn.render(dmg + "").tex();
        this.gob = gob;
    }

    public void update(int dmg, boolean isarmor) {
        if (isarmor) {
            this.arm += dmg;
            this.armtex = afrn.render(this.arm + "").tex();
        } else {
            this.dmg += dmg;
            this.dmgtex = dfrn.render(this.dmg + "").tex();
        }
    }
}
