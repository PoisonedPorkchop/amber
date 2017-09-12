import haven.*;
import haven.Material.Res;
import haven.glsl.Type;
import haven.glsl.Uniform;
import haven.res.lib.env.Environ;

import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ISmoke extends Sprite {
    FloatBuffer posb = null;
    FloatBuffer colb = null;
    final Material mat;
    final List<Boll> bollar = new LinkedList<>();
    final Random rnd = new Random();
    final float sz;
    float den;
    final float fadepow;
    final float initzv;
    float life;
    float srad;
    boolean spawn = true;
    float de = 0.0F;
    private static final Uniform bollsz = new Uniform(Type.FLOAT);
    private final float r, g, b, a;

    public ISmoke(Owner owner, Resource res, Message sdt) {
        super(owner, res);
        this.mat = (res.layer(Res.class, Integer.valueOf(sdt.uint8()))).get();
        this.sz = (float) sdt.uint8() / 10.0F;
        String boffid = sdt.string();
        int clr = sdt.uint16();
        r = (float) (((clr & 0xf000) >> 12) * 17) / 255.0F;
        g = (float) (((clr & 0x0f00) >> 8) * 17) / 255.0F;
        b = (float) (((clr & 0x00f0) >> 4) * 17) / 255.0F;
        a = (float) (((clr & 0x000f) >> 0) * 17) / 255.0F;
        this.den = (float) sdt.uint8();
        this.fadepow = (float) sdt.uint8() / 10.0F;
        this.life = (float) sdt.uint8() / 10.0F;
        this.initzv = (float) sdt.uint8() / this.life;
        this.srad = (float) sdt.uint8() / 10.0F;

        Resource ownres = owner.getres();
        if (ownres.name.endsWith("tarkiln")) {
            if (boffid.equals("s0")) {
                srad = 90.0F / 10.F;
                den = 60.0F;
                life = 3;
            } else {
                spawn = false;
            }
        }
    }

    public boolean tick(int dt) {
        float var2 = (float) dt / 1000.0F;
        de += var2;

        while (spawn && (double) de > 0.1D) {
            de = (float) ((double) de - 0.1D);
            int var3 = (int) ((1.0F + rnd.nextFloat() * 0.5F) * den);

            for (int i = 0; i < var3; ++i) {
                float e = 0;
                float a = rnd.nextFloat() * (float) Math.PI * 2.0F;
                float r = (float) Math.sqrt((double) rnd.nextFloat()) * srad;

                float x = (float) Math.cos(a) * (float) Math.cos(e) * r + (float) (rnd.nextGaussian() * 0.3D);
                float y = (float) Math.sin(a) * (float) Math.cos(e) * r + (float) (rnd.nextGaussian() * 0.3D);
                float z = (float) Math.sin(e) * r;
                bollar.add(new Boll(x, y, z));
            }
        }

        Coord3f var6 = Environ.get(((Gob) owner).glob).wind().mul(0.4F).rot(Coord3f.zu, (float) ((Gob) owner).a);

        Iterator it = bollar.iterator();
        while (it.hasNext()) {
            Boll boll = (Boll) it.next();
            if (boll.tick(var2, var6))
                it.remove();
        }

        return !spawn && bollar.isEmpty();
    }

    public void delete() {
        spawn = false;
    }

    class Boll {
        float x;
        float y;
        float z;
        float xv;
        float yv;
        float zv;
        float t = 0;

        Boll(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.xv = (float) rnd.nextGaussian() * 0.3F;
            this.yv = (float) rnd.nextGaussian() * 0.3F;
            this.zv = initzv;
        }

        public boolean tick(float dt, Coord3f dspl) {
            float var3 = this.xv - dspl.x;
            float var4 = this.yv - dspl.y;
            float var5 = this.zv - dspl.z;
            float var6 = -var3 * 0.2F + (float) rnd.nextGaussian() * 0.5F;
            float var7 = -var4 * 0.2F + (float) rnd.nextGaussian() * 0.5F;
            float var8 = (-var5 + initzv) * 0.2F + (float) rnd.nextGaussian() * 2.0F;
            this.xv += dt * var6;
            this.yv += dt * var7;
            this.zv += dt * var8;
            this.x += this.xv * dt;
            this.y += this.yv * dt;
            this.z += this.zv * dt;
            this.t += dt;
            return t > life;
        }
    }
}
