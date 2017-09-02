package haven.mod.interfaces;

import haven.mod.Mod;

public interface BasicMod {

    public Mod mod = null;

    public void create();

    public void start();

    public void exit();

}
