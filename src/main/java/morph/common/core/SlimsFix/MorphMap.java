package morph.common.core.SlimsFix;

import cpw.mods.fml.server.*;
import net.minecraft.nbt.*;

import java.util.*;

/**
 * Created by Ben Byers on 8/6/2014.
 */
public class MorphMap {
    public static HashMap<String, NBTTagCompound> morphMap;

    public static void Start() {
        FMLServerHandler.instance().getServer().logInfo("Morph Map has been started!");
        morphMap = new HashMap<String, NBTTagCompound>();
        try {
            if (morphMap.isEmpty()) {
                FMLServerHandler.instance().getServer().logInfo("Morph Map is empty!");
            } else {
                FMLServerHandler.instance().getServer().logInfo("Morph Map is not empty and is now being cleared!");
                try {
                    morphMap.clear();
                    FMLServerHandler.instance().getServer().logInfo("Morph Map has been successfully cleared!");
                } catch (Exception e) {
                    FMLServerHandler.instance().getServer().logWarning("There has been an error while clearing the MorphMap! Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            FMLServerHandler.instance().getServer().logWarning("There has been an error while checking if the MorphMap has data! The error: " + e.getMessage());
        }
        FMLServerHandler.instance().getServer().logInfo("Morph Map starter has finished!");
    }

    public static void Stop() {
        FMLServerHandler.instance().getServer().logInfo("Morph Map has been Stopped!");
        try {
            if (morphMap.isEmpty()) {
                FMLServerHandler.instance().getServer().logInfo("Morph Map is empty, and ready!");
            } else {
                FMLServerHandler.instance().getServer().logInfo("Morph Map is not empty but is being cleared!");
                try {
                    morphMap.clear();
                    FMLServerHandler.instance().getServer().logInfo("Morph Map has been cleared!");
                } catch (Exception e) {
                    FMLServerHandler.instance().getServer().logInfo("Morph Map has not been cleared! Error: " + e.getMessage());
                }

            }
        } catch (Exception e) {
            FMLServerHandler.instance().getServer().logInfo("Morph Map stop check has had an error! Error: " + e.getMessage());
        }
        FMLServerHandler.instance().getServer().logInfo("Morph Map stopping has finished!");
    }
}
