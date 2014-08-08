package morph.common.core.SlimsFix;

import cpw.mods.fml.server.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraftforge.common.*;


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
            if (hasMD) {
                player.registerExtendedProperties(DataHandler.property, dataHandler);
                FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " New data has been Registered!");
            }
            PlayerE = player;
            FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Data is already there, no need to register!");
        } catch (NullPointerException n) {
            FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " hasMD is null!");
        }
    }

    public boolean getHasMD() {
        return hasMD;
    }

    public boolean hasData() {
        FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Checking for Morph data!");
        try {
            FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Has Morph data!");
            return getMorphData().getBoolean("HasData");
        } catch (Exception e) {
            FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Does not have Morph data!");
            return false;
        }
    }

    public void setMorphData(NBTTagCompound morphData) {
        FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Setting Morph data!");
        MorphData = morphData;
        saveNBTData(PlayerE.getEntityData());
    }

    public NBTTagCompound getMorphData() {
        FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Getting Morph data!");
        loadNBTData(PlayerE.getEntityData());
        return MorphData;
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
