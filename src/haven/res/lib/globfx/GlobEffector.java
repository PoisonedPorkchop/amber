package haven.res.lib.globfx;

import haven.*;
import haven.Sprite.Owner;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class GlobEffector extends Drawable {
    static Map<Glob, Reference<GlobEffector>> cur = new WeakHashMap<>();
    public final Glob glob;
    Collection<Gob> holder = null;
    Map<Datum, Datum> data = new HashMap<>();

    private GlobEffector(Gob var1) {
        super(var1);
        this.glob = var1.glob;
    }

    public void ctick(int var1) {
    }

    public Resource getres() {
        return null;
    }

    private <T> T create(Class<T> var1) {
        Resource var2 = Resource.classres(var1);

        try {
            try {
                Constructor<T> var3 = var1.getConstructor(Owner.class, Resource.class);
                return var3.newInstance(this.gob, var2);
            } catch (NoSuchMethodException var4) {
                throw new RuntimeException("No valid constructor found for global effect " + var1);
            }
        } catch (InstantiationException var5) {
            throw new RuntimeException(var5);
        } catch (IllegalAccessException var6) {
            throw new RuntimeException(var6);
        } catch (InvocationTargetException var7) {
            if(var7.getCause() instanceof RuntimeException) {
                throw (RuntimeException)var7.getCause();
            } else {
                throw new RuntimeException(var7);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Datum> T getdata(T var1) {
        synchronized(this.data) {
            Datum var3 = this.data.get(var1);
            if(var3 == null) {
                var3 = var1;
                this.data.put(var1, var1);
            }

            return (T)var3;
        }
    }

    private static GlobEffector get(Glob var0) {
        synchronized(cur) {
            Reference<GlobEffector> var2 = cur.get(var0);
            if(var2 == null) {
                GlobEffectorObj var3 = new GlobEffectorObj(var0, Coord2d.z);
                GlobEffector var4 = new GlobEffector(var3);
                var3.setattr(var4);
                var0.oc.ladd(var4.holder = Collections.singleton(var3));
                cur.put(var0, var2 = new WeakReference<>(var4));
            }

            return (GlobEffector)((Reference)var2).get();
        }
    }

    public static <T extends Datum> T getdata(Glob var0, T var1) {
        return get(var0).getdata(var1);
    }

    final static class GlobEffectorObj extends Gob {
        GlobEffectorObj(Glob var1, Coord2d var2) {
            super(var1, var2);
        }

        public Coord3f getc() {
            return Coord3f.o;
        }
    }

}
