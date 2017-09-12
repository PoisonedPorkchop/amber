package haven.res.lib.plants;

import haven.FastMesh.MeshRes;
import haven.Message;
import haven.Resource;
import haven.Sprite;
import haven.Sprite.Factory;
import haven.Sprite.Owner;
import haven.Sprite.ResourceException;
import haven.resutil.CSprite;

import java.util.ArrayList;
import java.util.Iterator;

public class GrowingPlant implements Factory {
    public final int num;

    public GrowingPlant(int num) {
        this.num = num;
    }

    public Sprite create(Owner owner, Resource res, Message sdt) {
        int stg = sdt.uint8();
        ArrayList<MeshRes> meshes = new ArrayList<MeshRes>();
        Iterator allmeshes = res.layers(MeshRes.class).iterator();

        while(allmeshes.hasNext()) {
            MeshRes mesh = (MeshRes)allmeshes.next();
            if(mesh.id / 10 == stg) {
                meshes.add(mesh);
            }
        }

        if(meshes.size() < 1) {
            throw new ResourceException("No variants for grow stage " + stg, res);
        } else {
            CSprite cs = new CSprite(owner, res);
            return cs;
        }
    }
}
