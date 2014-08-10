package morph.common.core.SlimsFix;

import cpw.mods.fml.server.*;
import morph.common.*;
import morph.common.morph.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraftforge.common.*;

import java.util.*;


/**
 * Created by Ben Byers on 8/5/2014.
 */
public class DataHandler implements IExtendedEntityProperties {
    public static final String property = "MorphsModData";
    private EntityPlayer PlayerE;
    private boolean hasMD;
    private NBTTagCompound MorphData;
    //Keys/Data stored

    //Add more (for each field)

    public DataHandler() {
    }

    public final void register(EntityPlayer player, DataHandler dataHandler) {
        PlayerE = player;
        hasMD = hasData();
        try {
            if (!hasMD) {
                player.registerExtendedProperties(DataHandler.property, dataHandler);
                if (MorphMap.morphMap.isEmpty() || !MorphMap.morphMap.isEmpty() && !MorphMap.morphMap.containsKey(PlayerE.username)) {
                    MorphMap.morphMap.put(PlayerE.username, getNewMorphData());
                    setMorphData();
                    FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " register: 4");
                } else if (!MorphMap.morphMap.isEmpty() && MorphMap.morphMap.containsKey(PlayerE.username)) {
                    setMorphData();
                    FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " register: 3");
                }
            } else {
                if (getMorphData() == null) {
                    if (MorphMap.morphMap.isEmpty() || !MorphMap.morphMap.isEmpty() && !MorphMap.morphMap.containsKey(PlayerE.username)) {
                        MorphMap.morphMap.put(PlayerE.username, getNewMorphData());
                        setMorphData();
                        FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " register: 1");//<--------
                    } else if (!MorphMap.morphMap.isEmpty() && MorphMap.morphMap.containsKey(PlayerE.username)) {
                        setMorphData();
                        FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " register: 0");
                    }
                } else {
                    MorphMap.morphMap.put(PlayerE.username, getNewMorphData());
                    setMorphData();
                    FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " register: 2");
                }
            }
        } catch (NullPointerException n) {
            FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " Error while registering. Code: " + n.getMessage());
        }
    }

    public boolean getHasMD() {
        return hasMD;
    }

    public boolean hasData() {
        FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Checking for Morph data!");
        try {
            return PlayerE.getExtendedProperties(property) != null;
        } catch (Exception e) {
            FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Does not have Morph data!");
            return false;
        }
    }

    public NBTTagCompound getMorphData() {
        FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Getting Morph data!");
        try {
            loadNBTData(PlayerE.getEntityData());
            FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Loaded Morph data!");
        } catch (Exception e) {
            FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Error loading Morph data! Error: " + e.getMessage());
        }
        return MorphData;
    }

    @Deprecated
    public void setMorphData(NBTTagCompound morphData) {
        MorphData = morphData;
    }

    public void setMorphData() {
        FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Setting Morph data!");
        saveData(PlayerE.username);
    }

    public NBTTagCompound getNewMorphData() {
        FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Creating new Morph data!");
        MorphData = new NBTTagCompound();
        MorphData.setBoolean("HasData", true);
        try {
            saveNBTData(PlayerE.getEntityData());
            FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " New Morph data saved!");
            try {
                loadNBTData(PlayerE.getEntityData());
                FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " New Morph data loaded!");

            } catch (Exception e) {
                FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Error getting saved new Morph data!");
            }
        } catch (Exception e) {
            FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Error saving new Morph data!");
        }
        return MorphData;

    }

    @Override
    public void init(Entity entity, World world) {
    }

    private void saveData(String player) {
        NBTTagCompound tag = MorphMap.morphMap.get(player);
        MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player);

        NBTTagCompound tag1 = new NBTTagCompound();
        info.writeNBT(tag1);//Updated
        tag.setCompoundTag(player + "_morphData", tag1);//Updated


        ArrayList<MorphState> states = Morph.proxy.tickHandlerServer.playerMorphs.get(player);
        tag.setInteger(player + "_morphStatesCount", states.size());
        for (int i = 0; i < states.size(); i++) {
            tag.setCompoundTag(player + "_morphState" + i, states.get(i).getTag());
        }
        MorphData = MorphMap.morphMap.get(PlayerE.username);
        saveNBTData(PlayerE.getEntityData());
        FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " MorphData is: " + MorphData.toString());
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {//Basically This processes the NBT Data and returns what we use.
        try {
            MorphData = (NBTTagCompound) compound.getTag(property);
            FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Data loaded!");
        } catch (Exception e) {
            FMLServerHandler.instance().getServer().logWarning("Player: " + PlayerE.username + " Data not loaded, none to load!");
        }
        //data.setByteArray("HatDat",dat); If I see performance issues I will write everything to UTF then save it as a byte array
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound data;
        if (MorphData != null) {
            try {
                data = MorphData;
                //data.setByteArray("MDat",dat); If I see performance issues I will write everything to UTF then save it as a byte array
                //For each field needed, just add here and above where they are initialized.
                compound.setTag(property, data);
                FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Data saved!");
            } catch (Exception e) {
                FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Had an error saving data!");
            }
        } else {
            FMLServerHandler.instance().getServer().logWarning("Player: " + PlayerE.username + " Data not saved, none to save!");
        }
    }
}
