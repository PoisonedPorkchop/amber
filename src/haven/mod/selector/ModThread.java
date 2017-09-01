package haven.mod.selector;

public class ModThread extends Thread {

    public ModThread(Runnable runnable)
    {
        super(runnable);
    }

    public void end() throws Exception
    {
        throw new Exception();
    }
}
