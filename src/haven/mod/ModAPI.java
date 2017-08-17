package haven.mod;

import haven.*;
import haven.mod.event.Event;
import haven.mod.event.EventHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static haven.OCache.posres;

/**
 * Framework and API for creating and using mods.
 * @author PoisonedPorkchop
 */
public class ModAPI {

    protected static HashMap<Class<? extends Event>,ArrayList<Method>> eventhandlers;
    private static HashMap<HavenMod,ClassLoader> mods;
    private static RunState runState;

    static {
        eventhandlers = new HashMap<>();
        runState = RunState.NONE;
        mods = new HashMap<>();
    }

    /**
     * Gets current RunState.
     * @return Current RunState
     */
    public static RunState getRunState()
    {
        return runState;
    }

    /**
     * Sets current RunState. More on this in the future.
     * @param state RunState to set the system to.
     */
    public static void setRunState(RunState state)
    {
        if(state != null)
            runState = state;
    }

    /**
     * Register a new Event. Probably should not be called unless you are implementing your own API, and in such a case
     * you can add your own event so that listeners can be created.
     * @param clazz
     */
    public static void registerEvent(Class<? extends Event> clazz)
    {
        if(!eventhandlers.containsKey(clazz))
        {
            System.out.println("Registered event " + clazz.getSimpleName() + "!");
            eventhandlers.put(clazz, new ArrayList<>());
        }
    }

    /**
     * Registers all listeners (Event Handlers) in a given class.
     * @param clazz Class to find and register Event Handler methods in.
     */
    public static void registerListeners(Class clazz)
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
    public static void callEvent(Event event)
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
    public static void registerMod(HavenMod mod, ClassLoader loader)
    {
        if(!mods.containsKey(mod))
           mods.put(mod,loader);
    }

    /**
     * Gets all currently loaded mods.
     * @return All loaded mods.
     */
    public static ArrayList<HavenMod> getMods()
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
    public static void registerCustomWidget(Widget widget, Coord location)
    {
        addChildWidget(HavenPanel.lui.root, widget, location);
    }

    /**
     * Add a widget as a child to an existing widget.
     * @param parent Already registered parent widget.
     * @param child Widget to add.
     * @param location Location, on screen, to add the widget.
     */
    public static void addChildWidget(Widget parent, Widget child, Coord location)
    {
        synchronized (HavenPanel.lui) {
            parent.add(child, location);
            int id = 10000;
            while (HavenPanel.lui.widgets.containsKey(id))
                id++;
            HavenPanel.lui.bind(child, id);
        }
    }

    /**
     * Class that contains convenience methods for carrying out autonomous actions in Haven.
     */
    public static class ModAction {

        /**
         * Move to a location in the world.
         * @param location Location to move to.
         */
        public static void moveTo(Coord location)
        {
            HavenPanel.lui.wdgmsg(getGUI().map,"click", Coord.z, location, 1, 0);
        }

        /**
         * Gets all map objects. Important for finding targets.
         * @return List of all current Game Objects. Be careful not to reference these when they have unloaded.
         */
        public static ArrayList<Gob> getMapObjects()
        {
            ArrayList<Gob> gobs = new ArrayList<>();
            for(Gob gob : HavenPanel.lui.root.findchild(GameUI.class).map.glob.oc)
                gobs.add(gob);
            return gobs;
        }

        /**
         * Get the current player.
         * @return The player's Game Object.
         */
        public static Gob getPlayer()
        {
            return getGUI().map.player();
        }

        /**
         * Right click an object. Can be used to interact with objects such as cupboards, dropped items, plants etc.
         * @param gob Game object to right click.
         */
        public static void rightClick(Gob gob)
        {
            getGUI().map.wdgmsg("click", gob.sc, getLocationOfGob(gob), 3, 0, 0, (int) gob.id, getLocationOfGob(gob), 0, -1);
        }

        /**
         * Right click a location in the world. Primarily for placing carried objects.
         * @param location Location to be right clicked.
         */
        public static void rightClick(Coord location)
        {
            getGUI().map.wdgmsg("click", Coord.z, location, 3, 0);
        }

        /**
         * Picks up an object.
         * @param gob Game Object to be picked up.
         */
        public static void pickUpObject(Gob gob)
        {
            getGUI().menu.wdgmsg("act", "carry");
            getGUI().map.wdgmsg("click", Coord.z, getLocationOfGob(gob), 1, 0, 0, (int) gob.id, getLocationOfGob(gob), 0, -1);
        }

        public static int getFreeSpaceInInventory()
        {
            return getGUI().maininv.getFreeSpace();
        }

        public static WItem getItem(String itemName)
        {
            return getGUI().maininv.getItemPartial(itemName);
        }

        public static List<WItem> getItems(String itemName)
        {
            return getGUI().maininv.getItemsPartial(itemName);
        }

        public static void dropItem(GItem item)
        {
            item.wdgmsg("drop", new Coord(0,0));
        }

        public static void dropItemInHand(boolean shift)
        {
            getGUI().map.wdgmsg("drop", new Coord(0,0), getLocationOfGob(getPlayer()), 0);
        }

        public static void transferItem(GItem item)
        {
            item.wdgmsg("transfer", new Coord(0,0));
        }



        /**
         * Gets the current world location of a Game Object.
         * @param gob Game Object to get the location of.
         * @return Coord that is the location of the game object in world terms.
         */
        public static Coord getLocationOfGob(Gob gob)
        {
            return gob.rc.floor(posres);
        }

        /**
         * Gets the gui. Be careful, as it might not be active.
         * @return Current GameUI
         */
        public static GameUI getGUI()
        {
            return HavenPanel.lui.root.findchild(GameUI.class);
        }

    }

}
