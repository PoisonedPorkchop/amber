package haven.res.lib.tree;

import haven.*;

public class Tree extends StaticSprite {
    public final float fscale;
    Message sdt;

    public Tree(Owner owner, Resource res, float scale) {
        super(owner, res);
        this.fscale = scale;
    }

    public Tree(Owner owner, Resource res, Message std) {
        this(owner, res, std.eom() ? 1.0F : (float) std.uint8() / 100.0F);
        this.sdt = std;
    }

}
