package haven.mod;

public class Mod {

    private static ModAPI modAPI = null;
    static boolean debug = true;

    public Mod()
    {
    }

    public ModAPI init()
    {
        if(modAPI == null || modAPI.equals(null)) {
            modAPI = new ModAPI();
        }
        return modAPI;
    }

    public ModAPI getAPI()
    {
        return init();
    }

    public ModAction actions()
    {
        return new ModAction();
    }

    public static void debug(String msg)
    {
        if(debug) {
            System.out.println(msg);
        }
    }

}
