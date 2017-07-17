package haven;

import groovy.lang.Binding;
import groovy.transform.ThreadInterrupt;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import haven.event.*;

public class Maid {
    private static Maid instance = null;

    private final Object LOCK = new Object();
    private final String scripts_folder;
    private final String scripts[];
    private final GroovyScriptEngine engine;
    private final Binding binding;
    private final ThreadGroup taskGroup;
    private HavenPanel haven;
    private Thread task, wait;
    private TaskListener taskListener;
    private CursorListener cursorListener;
    private MeterListener meterListener;
    private ItemListener itemListener;
    private WidgetListener<?> widgetListener;
    private int menuGridId = 0;

    public Maid() {

        //Creates a thread group called groovy to allow groovy to run in a collection of certain threads.
        taskGroup = new ThreadGroup("groovy");

        //Creates a name space for putting groovy variables?
        binding = new Binding();
        //Sets the variable "Maid" to this instance for groovy to use?...
        binding.setVariable("maid", this);

        //Creates a maid properties file.
        Properties p = initConfig();

        //Creating a scripts variable for storing and referencing scripts?...
        scripts = initScripts(p);
        scripts_folder = initScriptFolder(p);

        //Initiate the groovy engine to handle scripts.
        engine = initGroovy(scripts_folder);

        //No Idea other than possibly setting the type of compiler.
        engine.getConfig().addCompilationCustomizers(
                new org.codehaus.groovy.control.customizers.ASTTransformationCustomizer(ThreadInterrupt.class));
    }

    /**
     * Returns the live instance of maid client.
     * @return Returns maid.
     */
    public static Maid getInstance() {
        if (instance == null) {
            instance = new Maid();
        }

        return instance;
    }

    /**
     * Creates the maid properties file and returns the property instance of it.
     * @return Returns the Properties variable to access the file, in Java.
     */
    private Properties initConfig() {
        Properties p = new Properties();

        File inputFile = new File("maid.conf");
        if (!inputFile.exists()) {
            return p;
        }

        try {
            p.load(new FileInputStream(inputFile));
        } catch (IOException e) {
        }

        return p;
    }

    /**
     * Creates a running list of scripts.
     * @param p The maid properties file.
     * @return The list of scripts.
     */
    private String[] initScripts(Properties p) {
        String[] s = new String[12];
        for (int i = 1; i <= 12; i++) {
            s[i - 1] = p.getProperty("script_f" + i, "f" + i);
        }
        return s;
    }

    /**
     * Creates a script folder if there isn't one and returns it?... I guess?
     * @param p Properties file.
     * @return Return the script folder.
     */
    private String initScriptFolder(Properties p) {
        return p.getProperty("scripts_folder", "scripts");
    }

    /**
     * Initiates the groovy script engine in the script folder.
     * @param scripts_folder The scripts folder.
     * @return Returns the new groovy engine.
     */
    private GroovyScriptEngine initGroovy(String scripts_folder) {
        GroovyScriptEngine gse;
        try {
            gse = new GroovyScriptEngine(scripts_folder);
        } catch (IOException e) {
            doErr("Can't open scripts folder. I will try creating it...");

            boolean success = new File(scripts_folder).mkdir();

            if (success) {
                try {
                    gse = new GroovyScriptEngine(scripts_folder);
                } catch (IOException e2) {
                   doErr("Directory \"" + scripts_folder + "\" gives errors. I give up.");

                    throw new RuntimeException("Can't initialize groovy script engine.", e2);
                }
            } else {
                doErr("Can't read/create \"" + scripts_folder + "\".");

                throw new RuntimeException("Can't initialize groovy script engine.", e);
            }
        }

        return gse;
    }

    public void doSay(Object text) {
        System.out.println(text);
    }

    public void doErr(Object text) {
        System.err.println(text);
    }

    //Does a task with the specific string? How is this even called?...
    void doTask(final String name) {
        //Checks if there isn't already a task running. A bot can only do one thing at a time.
        if (task != null) {
            doSay("-- Already running a task.");
            return;
        }

        //Creates a thread to run the task in to prevent tieing up the client.
        task = new Thread(taskGroup, "maid") {

            //Runs the bot.
            @Override
            public void run() {
                try {
                    //Any pre init stuff that needs to be done... Does not do anything right now.
                    preProcessing();

                    //Runs the script name with the groovy extension and runs it on the binding space...
                    engine.run(name + ".groovy", binding);

                    //Any post stuff that needs to be cleaned up.
                    postProcessing();

                    //Let us know when it is done :)
                    doSay("-- Done\n");
                } catch (ResourceException e) {
                    doSay("Can't find the file.");

                    e.printStackTrace();
                } catch (ScriptException e) {
                    doSay("Something is wrong with this task. I don't understand it.");

                    e.printStackTrace();
                } catch (Throwable t) {
                    doErr("Canceled?");

                    t.printStackTrace();
                } finally {
                    task = null;
                }
            }
        };
        //Runs the newly created task above.
        task.start();
    }

    //Same as before but access scripts by index instead of name.
    void doTask(int i) {
        doTask(scripts[i]);
    }

    //Stops the groovy script.
    void stopTask() {
        //Check if there is even a task running.
        if (task != null) {
            //If task is on wait, do not accidentally stop in middle of process.
            if (wait == null) {
                //Close the thread and force run it's post processing.
                doSay("Interruping...");
                wait = new Thread() {

                    @Override
                    public void run() {
                        doSay(task.toString());

                        task.getThreadGroup().interrupt();

                        wait = null;

                        postProcessing();

                        doSay("Interrupted successfuly.");
                    }
                };

                wait.start();
            } else {
                doSay("Already interrumpting.");
            }

        } else {
            doSay("Nothing to interrupt.");
        }
    }

    //Pre task stuff goes here...
    private void preProcessing() {
    }

    //Post task stuff goes here...
    private void postProcessing() {
        clearListeners();
    }

    //Ah I see how it is run now...
    static {
        //The command to run the script by name.
        Console.setscmd("scstart", (cons, args) -> {
            if (args.length < 1) {
                System.out.println("No scriptname given.");
            }
            Maid.getInstance().doTask(args[1]);
        });

        //Stop the script.
        Console.setscmd("scstop", (cons, args) -> {
            Maid.getInstance().stopTask();
        });
    }

    void clearListeners()
    {
        //Set listeners null here to reset them and clear java memory...
    }
}
