package haven.mod;

import haven.mod.event.Event;
import haven.mod.event.EventHandler;
import haven.mod.event.RunState;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ModAPI {

    protected static HashMap<Class<? extends Event>,ArrayList<Method>> eventhandlers;
    private static HashMap<HavenMod,ClassLoader> mods;
    private static RunState runState;

    static {
        eventhandlers = new HashMap<>();
        runState = RunState.NONE;
        mods = new HashMap<>();
    }

    public static RunState getRunState()
    {
        return runState;
    }

    public static void setRunState(RunState state)
    {
        if(state != null)
            runState = state;
    }

    public static void registerEvent(Class<? extends Event> clazz)
    {
        if(!eventhandlers.containsKey(clazz))
        {
            System.out.println("Registered event " + clazz.getSimpleName() + "!");
            eventhandlers.put(clazz, new ArrayList<>());
        }
    }

    public static void registerListeners(Class clazz)
    {
        for(Method method : clazz.getDeclaredMethods())
        {
            boolean eventhandlerpresent = false;

            for (Annotation annotation : method.getDeclaredAnnotations())
            {
                if(annotation.annotationType().equals(EventHandler.class))
                {
                    eventhandlerpresent = true;
                }
            }
            if(eventhandlerpresent && method.getParameterCount() == 1 && method.getReturnType().equals(Void.TYPE) && Modifier.isStatic(method.getModifiers()))
                for(Class clazz2 : method.getParameterTypes()) {
                    if (Event.class.isAssignableFrom(clazz2)) {
                        if (eventhandlers.containsKey(clazz2)) {
                            if (!eventhandlers.get(clazz2).contains(method)) {
                                eventhandlers.get(clazz2).add(method);
                            } else {
                                System.err.println("EventHandler already registered!");
                            }
                        } else {
                            System.err.println("EventHandler registered to unregistered Event!");
                        }
                    }
                }
        }
    }

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

    public static void registerMod(HavenMod mod, ClassLoader loader)
    {
        if(!mods.containsKey(mod))
           mods.put(mod,loader);
    }

    public static ArrayList<HavenMod> getMods()
    {
        ArrayList<HavenMod> temp = new ArrayList<>();
        for(Map.Entry<HavenMod, ClassLoader> entry : mods.entrySet())
            temp.add(entry.getKey());
        return temp;
    }
}
