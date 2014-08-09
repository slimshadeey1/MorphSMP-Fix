package morph.common.morph.mod;

import com.google.gson.*;
import morph.common.*;
import net.minecraft.entity.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class NBTStripper {

    private static final String jsonPath = "/src/main/resources/assets/morph/mod/NBTStripper.json";
    public static NBTStripper instance = null;
    private static HashMap<Class<? extends EntityLivingBase>, ArrayList<String>> stripperMappings = new HashMap<Class<? extends EntityLivingBase>, ArrayList<String>>();
    private HashMap<String, String[]> modNBTStripper = new HashMap<String, String[]>();

    public static NBTStripper getInstance() {
        if (instance == null) {
            Gson gson = new Gson();
            Reader fileIn = null;
            try {
                fileIn = new InputStreamReader(new URL("https://raw.githubusercontent.com/slimshadeey1/Morph/BteamDevelopment" + jsonPath).openStream());
            } catch (Exception e) {
                Morph.console("Failed to retrieve nbt stripper mappings from GitHub!", true);
                e.printStackTrace();
                try {
                    fileIn = new InputStreamReader(Morph.class.getResourceAsStream(jsonPath));
                } catch (Exception e1) {
                    fileIn = null;
                    Morph.console("Failed to read local copy of nbt stripper mappings", true);
                    e1.printStackTrace();
                }
            }

            if (fileIn == null) {
                instance = new NBTStripper();
            } else {
                instance = gson.fromJson(fileIn, NBTStripper.class);
            }
        }
        return instance;
    }

    public static void addStripperMappings(Class<? extends EntityLivingBase> clz, String... tagNames) {
        ArrayList<String> mappings = getNBTTagsToStrip(clz);
        for (String tagName : tagNames) {
            if (!mappings.contains(tagName)) {
                mappings.add(tagName);
            }
        }
    }

    public static ArrayList<String> getNBTTagsToStrip(EntityLivingBase ent) {
        ArrayList<String> tagsToStrip = new ArrayList<String>();
        Class clz = ent.getClass();
        while (clz != EntityLivingBase.class) {
            tagsToStrip.addAll(getNBTTagsToStrip(clz));
            clz = clz.getSuperclass();
        }
        return tagsToStrip;
    }

    public static ArrayList<String> getNBTTagsToStrip(Class<? extends EntityLivingBase> clz) {
        ArrayList<String> list = stripperMappings.get(clz);
        if (list == null) {
            list = new ArrayList<String>();
            stripperMappings.put(clz, list);
        }
        return list;
    }

    public void mapStripperInfo() {
        String[] keys = modNBTStripper.keySet().toArray(new String[0]);
        for (String key : keys) {
            Class<? extends EntityLivingBase> entityClass = null;
            try {
                entityClass = (Class<? extends EntityLivingBase>) Class.forName(key);
            } catch (ClassNotFoundException e) {
                modNBTStripper.remove(key);
            }
        }
        for (String mob : modNBTStripper.keySet()) {
            String[] tagNames = modNBTStripper.get(mob);
            try {
                addStripperMappings((Class<? extends EntityLivingBase>) Class.forName(mob), tagNames);
            } catch (ClassNotFoundException e) {
            }
        }
    }
}
