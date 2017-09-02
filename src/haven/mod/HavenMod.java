package haven.mod;

import java.util.jar.JarFile;

/**
 * Haven Mod interface
 * @author PoisonedPorkchop
 */
public abstract class HavenMod {
    private JarFile jar;
    private String modName;
    public abstract void create();
    public abstract void start();
    public abstract void exit();
    public JarFile getJar(){
        return jar;
    }
    public void setJar(JarFile jar){
        this.jar = jar;
    }

    public String getModName() {
        return modName;
    }

    public void setModName(String modName) {
        this.modName = modName;
    }
}