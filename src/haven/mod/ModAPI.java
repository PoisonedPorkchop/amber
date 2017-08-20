package haven.mod;

import haven.*;
import haven.mod.event.*;
import haven.mod.event.flower.FlowerMenuCancelEvent;
import haven.mod.event.flower.FlowerMenuChooseEvent;
import haven.mod.event.flower.FlowerMenuChosenEvent;
import haven.mod.event.flower.FlowerMenuCreateEvent;
import haven.mod.event.widget.*;
import haven.pathfinder.PFListener;
import haven.pathfinder.Pathfinder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static haven.OCache.posres;

/**
 * Framework and API for creating and using mods.
 * @author PoisonedPorkchop
 */
public class ModAPI {

    public ModAPI()
    {
        eventhandlers = new HashMap<>();
        runState = RunState.NONE;
        mods = new HashMap<>();

    }

    public void create()
    {
        loadEvents();
        loadMods(this);
    }

    /**
     * Loads all events
     */
    private void loadEvents() {
        registerEvent(UIMessageEvent.class);
        registerEvent(WidgetMessageEvent.class);
        registerEvent(WidgetPreCreateEvent.class);
        registerEvent(WidgetPostCreateEvent.class);
        registerEvent(WidgetDestroyEvent.class);
        registerEvent(WidgetGrabKeysEvent.class);
        registerEvent(CustomMenuButtonPressEvent.class);
        registerEvent(FlowerMenuCancelEvent.class);
        registerEvent(FlowerMenuChooseEvent.class);
        registerEvent(FlowerMenuChosenEvent.class);
        registerEvent(FlowerMenuCreateEvent.class);
        registerEvent(RunStateChangeEvent.class);
        registerEvent(InventoryCreateEvent.class);
    }

    /**
     * Loads all mods in the mods folder.
     */
    private void loadMods(ModAPI mod) {
        try {
            File originator = new File(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            File parent = originator.getParentFile();
            File mods = new File(parent, "mods");
            if (!mods.exists()) {
                mods.mkdir();
            }
            for(File file : mods.listFiles())
                if(file.isFile())
                    if(file.getName().contains(".")) {
                        String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                        if(extension.contains("jar"))
                        {
                            JarFile jar = new JarFile(file);
                            ZipEntry infoFile = jar.getEntry("info.txt");
                            if(infoFile != null)
                            {
                                Enumeration<? extends JarEntry> entries = jar.entries();
                                String[] lines;
                                InputStream infoFileStream = jar.getInputStream(infoFile);
                                Scanner s = new Scanner(infoFileStream).useDelimiter("\\A");
                                String result = s.hasNext() ? s.next() : "";
                                lines = result.split("\n");
                                String mainclass = null;
                                String name = null;

                                for(String string : lines)
                                {
                                    string = string.replaceAll("\\r|\\n", "");
                                    if(string.startsWith("main="))
                                        mainclass = string.split("=")[1];
                                    else if(string.startsWith("name="))
                                        name = string.split("=")[1];
                                }

                                if(mainclass != null && name != null)
                                {
                                    ArrayList<JarEntry> entryList = new ArrayList<>();
                                    while(entries.hasMoreElements())
                                    {
                                        entryList.add(entries.nextElement());
                                    }
                                    JarEntry main = null;
                                    for(JarEntry entry : entryList)
                                    {
                                        if(mainclass.equals(entry.getName()))
                                        {
                                            main = entry;
                                            break;
                                        }
                                    }
                                    if(main != null)
                                    {
                                        ClassLoader classLoader = URLClassLoader.newInstance(
                                                new URL[] {new URL("jar:" + file.toURI().toURL() + "!/")},
                                                MainFrame.class.getClassLoader()
                                        );

                                        for(JarEntry entry : entryList) {
                                            String entryExtension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                                            if (entryExtension.contains("class"))
                                            {
                                                Class.forName(entry.getName().replaceAll("/", "."), true, classLoader);
                                            }
                                        }
                                        Class<? extends HavenMod> modClass = (Class<? extends HavenMod>) Class.forName(mainclass.replaceAll("/", ".").replaceAll(".class",""), false, classLoader);
                                        Class.forName(mainclass.replaceAll(".class","").replaceAll("/", "."), false, classLoader);
                                        HavenMod havenMod = modClass.newInstance();
                                        havenMod.setJar(jar);
                                        havenMod.setModName(name);
                                        havenMod.start();
                                        mod.registerMod(havenMod,classLoader);
                                        System.out.println("Loaded mod: " + name);
                                    }
                                    else
                                    {
                                        Mod.debug(("Expected: '" + mainclass + "'"));
                                    }
                                }
                                else
                                {
                                    Mod.debug("Information could not be resolved from info.txt");
                                }
                            }
                        }
                    }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    protected HashMap<Class<? extends Event>,ArrayList<Method>> eventhandlers;
    private HashMap<HavenMod,ClassLoader> mods;
    private RunState runState;

    /**
     * Gets current RunState.
     * @return Current RunState
     */
    public RunState getRunState()
    {
        return runState;
    }

    /**
     * Sets current RunState. More on this in the future.
     * @param state RunState to set the system to.
     */
    public void setRunState(RunState state)
    {
        if(state != null)
            runState = state;
    }

    /**
     * Register a new Event. Probably should not be called unless you are implementing your own API, and in such a case
     * you can add your own event so that listeners can be created.
     * @param clazz
     */
    public void registerEvent(Class<? extends Event> clazz)
    {
        if(!eventhandlers.containsKey(clazz))
        {
            Mod.debug("Registered event " + clazz.getSimpleName() + "!");
            eventhandlers.put(clazz, new ArrayList<>());
        }
    }

    /**
     * Registers all listeners (Event Handlers) in a given class.
     * @param clazz Class to find and register Event Handler methods in.
     */
    public void registerListeners(Class clazz)
    {
        for(Method method : clazz.getDeclaredMethods())
        {
            boolean eventhandlerpresent = false;

            for (Annotation annotation : method.getDeclaredAnnotations())
                if(annotation.annotationType().equals(EventHandler.class))
                    eventhandlerpresent = true;

            if(eventhandlerpresent && method.getParameterCount() == 1 && method.getReturnType().equals(Void.TYPE) && Modifier.isStatic(method.getModifiers()))
                for(Class clazz2 : method.getParameterTypes())
                    if (Event.class.isAssignableFrom(clazz2))
                        if (eventhandlers.containsKey(clazz2))
                            if (!eventhandlers.get(clazz2).contains(method))
                                eventhandlers.get(clazz2).add(method);
                            else
                                System.err.println("EventHandler already registered!");
                        else
                            System.err.println("EventHandler registered to unregistered Event!");
        }
    }

    /**
     * Call an event, activating all current event handlers.
     * Probably should not be called, as the events are primarily intended to be called via game code.
     * However if you want other mods to react as if there were a real event happening, or you have registered a custom
     * event, then this function could be useful
     * @param event
     */
    public void callEvent(Event event)
    {
        if(eventhandlers.containsKey(event.getClass()))
        {
            for(Method method : eventhandlers.get(event.getClass()))
            {
                try {
                    method.invoke(null, event);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            System.err.println("Event " + event.getClass().getSimpleName() + " not registered!");
        }
    }

    /**
     * Probably should not be called unless you know what you are doing.
     * Mods are generally supposed to be loaded at the beginning of the game, and are loaded through the mods folder
     * in the game's directory.
     * However this is left here to provide options for dynamic mod loading if one should wish.
     * @param mod Mod to register.
     * @param loader ClassLoader used to load mod.
     */
    public void registerMod(HavenMod mod, ClassLoader loader)
    {
        if(!mods.containsKey(mod))
           mods.put(mod,loader);
    }

    /**
     * Gets all currently loaded mods.
     * @return All loaded mods.
     */
    public ArrayList<HavenMod> getMods()
    {
        ArrayList<HavenMod> temp = new ArrayList<>();
        for(Map.Entry<HavenMod, ClassLoader> entry : mods.entrySet())
            temp.add(entry.getKey());
        return temp;
    }

    /**
     * Create a new widget, recommended to start as a frame.
     * @param widget Widget to add.
     * @param location Location, on screen, to add the widget.
     */
    public void registerCustomWidget(Widget widget, Coord location)
    {
        addChildWidget(HavenPanel.lui.root, widget, location);
    }

    /**
     * Add a widget as a child to an existing widget.
     * @param parent Already registered parent widget.
     * @param child Widget to add.
     * @param location Location, on screen, to add the widget.
     */
    public void addChildWidget(Widget parent, Widget child, Coord location)
    {
        synchronized (HavenPanel.lui) {
            parent.add(child, location);
            int id = 10000;
            while (HavenPanel.lui.widgets.containsKey(id))
                id++;
            HavenPanel.lui.bind(child, id);
        }
    }



}
