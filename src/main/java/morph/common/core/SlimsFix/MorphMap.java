package morph.common.core.SlimsFix;

import net.minecraft.nbt.*;

import java.util.*;

/**
 * Created by Ben Byers on 8/6/2014.
 */
public class MorphMap {
    public static HashMap<String,NBTTagCompound> morphMap;
    public MorphMap(){
        morphMap = new HashMap<String, NBTTagCompound>();
    }
}
