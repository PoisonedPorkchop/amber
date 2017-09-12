package haven;

import java.nio.BufferOverflowException;
import java.nio.FloatBuffer;

import static haven.MCache.tilesz;

public class TileOutline {
    private final MCache map;
    private final FloatBuffer[] vertexBuffers;
    private final int area;
    private Coord ul;
    private int curIndex;

    public TileOutline(MCache map) {
        this.map = map;
        this.area = (MCache.cutsz.x * 5) * (MCache.cutsz.y * 5);
        // double-buffer to prevent flickering
        vertexBuffers = new FloatBuffer[2];
        vertexBuffers[0] = Utils.mkfbuf(this.area * 3 * 4);
        vertexBuffers[1] = Utils.mkfbuf(this.area * 3 * 4);
        curIndex = 0;
    }

    public void update(Coord ul) {
        try {
            this.ul = ul;
            curIndex = (curIndex + 1) % 2; // swap buffers
            Coord c = new Coord();
            Coord size = ul.add(MCache.cutsz.mul(5));
            for (c.y = ul.y; c.y < size.y; c.y++)
                for (c.x = ul.x; c.x < size.x; c.x++)
                    addLineStrip(mapToScreen(c), mapToScreen(c.add(1, 0)), mapToScreen(c.add(1, 1)));
        } catch (Loading e) {
        }
    }

    private Coord3f mapToScreen(Coord c) {
        return new Coord3f((float) ((c.x - ul.x) * tilesz.x), (float) (-(c.y - ul.y) * tilesz.y), Config.disableelev ? 0 : map.getz(c));
    }

    private void addLineStrip(Coord3f... vertices) {
        try {
            FloatBuffer vbuf = getCurrentBuffer();
            for (int i = 0; i < vertices.length - 1; i++) {
                Coord3f a = vertices[i];
                Coord3f b = vertices[i + 1];
                vbuf.put(a.x).put(a.y).put(a.z);
                vbuf.put(b.x).put(b.y).put(b.z);
            }
        } catch (BufferOverflowException boe) {
        }
    }

    private FloatBuffer getCurrentBuffer() {
        return vertexBuffers[curIndex];
    }
}
