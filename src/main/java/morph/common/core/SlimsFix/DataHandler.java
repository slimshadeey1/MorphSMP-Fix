package morph.common.core.SlimsFix;

import cpw.mods.fml.common.network.*;
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

    public final void register(EntityPlayer player, DataHandler dataHandler, Boolean logout, Boolean save) {
        PlayerE = player;
        hasMD = hasData();
        Boolean mapEmpty;
        Boolean dataEmpty;
        Boolean playerEmpty;
        try {
            mapEmpty = !MorphMap.morphMap.isEmpty();
        } catch (Exception e) {
            mapEmpty = true;
        }
        try {
            if (!mapEmpty)
                playerEmpty = !MorphMap.morphMap.containsKey(PlayerE.username);
            else
                playerEmpty = true;
        } catch (Exception e) {
            playerEmpty = true;
        }
        try {
            if (save) {
                setMorphData();
            } else {
                if (!hasMD) {
                    player.registerExtendedProperties(DataHandler.property, dataHandler);
                    if (mapEmpty || playerEmpty) {
                        FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " Stage: 4");//<--------
                        MorphMap.morphMap.put(PlayerE.username, getNewMorphData());
                        loadData();
                        setMorphData();
                        FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " register: 4");
                    } else if (!playerEmpty) {
                        FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " Stage: 3");//<--------
                        setMorphData();
                        FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " register: 3");
                    }
                    MorphHandler.updatePlayerOfMorphStates((EntityPlayerMP) player, null, true);
                } else {
                    if (!logout) {
                        try {
                            dataEmpty = getMorphData() == null;
                        } catch (Exception e) {
                            dataEmpty = true;
                        }
                        if (dataEmpty) {
                            if (mapEmpty || playerEmpty) {
                                FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " Stage: 1");//<--------
                                MorphMap.morphMap.put(PlayerE.username, getNewMorphData());
                                loadData();
                                setMorphData();
                                FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " register: 1");//<--------
                            } else if (!playerEmpty) {
                                FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " Stage: 0");//<--------
                                setMorphData();
                                loadData();
                                FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " register: 0");
                            }
                        } else {
                            FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " Stage: 2");//<--------
                            MorphMap.morphMap.put(PlayerE.username, getMorphData());
                            loadData();
                            setMorphData();
                            FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " register: 2");
                        }
                        MorphHandler.updatePlayerOfMorphStates((EntityPlayerMP) player, null, true);
                    } else {
                        if (mapEmpty || playerEmpty) {
                            FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " No data and logout, register: 5");
                        } else if (!playerEmpty) {
                            setMorphLogoutData();
                        }
                    }
                }
            }
        } catch (Exception n) {
            FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " Error while registering. Code: " + n.getMessage() + "Cause: " + n.getCause() + "Trace: " + n.getStackTrace().toString());
        }
    }

    public boolean getHasMD() {
        return hasMD;
    }

    public boolean hasData() {
        FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Checking for Morph Properties!");
        try {
            return PlayerE.getExtendedProperties(property) != null;
        } catch (Exception e) {
            FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Does not have Morph Properties!");
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

    private void loadData() {
        EntityPlayer player = PlayerE;
        ArrayList list = Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player.username);

        if (MorphMap.morphMap.get(player.username) != null) {
            NBTTagCompound tag = MorphMap.morphMap.get(player.username);///TODO Here we have the morph save and load

            MorphHandler.addOrGetMorphState(list, new MorphState(player.worldObj, player.username, player.username, null, player.worldObj.isRemote));

            int count = tag.getInteger(player.username + "_morphStatesCount");
            if (count > 0) {

                for (int i = 0; i < count; i++) {
                    MorphState state = new MorphState(player.worldObj, player.username, player.username, null, false);
                    state.readTag(player.worldObj, tag.getCompoundTag(player.username + "_morphState" + i));
                    if (!state.identifier.equalsIgnoreCase("")) {
                        MorphHandler.addOrGetMorphState(list, state);
                    }
                }
            }

            NBTTagCompound tag1 = tag.getCompoundTag(player.username + "_morphData");
            if (tag1.hasKey("playerName")) {
                MorphInfo info = new MorphInfo();
                info.readNBT(tag1);
                if (!info.nextState.playerName.equals(info.nextState.playerMorph)) {
                    Morph.proxy.tickHandlerServer.playerMorphInfo.put(info.playerName, info);
                    MorphHandler.addOrGetMorphState(list, info.nextState);

                    PacketDispatcher.sendPacketToAllPlayers(info.getMorphInfoAsPacket());
                }
            }
        }
    }

    private void setMorphLogoutData() {
        FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Setting Morph data, with logout!");
        saveData(PlayerE.username, true);
    }

    public void setMorphData() {
        FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Setting Morph data!");
        saveData(PlayerE.username, false);
    }

    public NBTTagCompound getNewMorphData() {
        FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Creating new Morph data!");
        MorphData = new NBTTagCompound();
        MorphData.setBoolean("HasData", true);
        try {
            saveNBTData(PlayerE.getEntityData());
            FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " New Morph data saved 1!");
            try {
                loadNBTData(PlayerE.getEntityData());
                FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " New Morph data loaded 2!");

            } catch (Exception e) {
                FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Error getting saved new Morph data 2x0!");
            }
        } catch (Exception e) {
            FMLServerHandler.instance().getServer().logInfo("Player: " + PlayerE.username + " Error saving new Morph data! 1x0");
        }
        return MorphData;

    }

    @Override
    public void init(Entity entity, World world) {
    }

    private void saveData(String player, Boolean logout) {
        NBTTagCompound tag = MorphMap.morphMap.get(player);
        MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player);

        NBTTagCompound tag1 = new NBTTagCompound();
        info.writeNBT(tag1);//Updated
        tag.setCompoundTag(player + "_morphData", tag1);//Updated


        ArrayList<MorphState> states = Morph.proxy.tickHandlerServer.playerMorphs.get(player);
        tag.setInteger(player + "_morphStatesCount", states.size());
        if (states != null) {
            for (int i = 0; i < states.size(); i++) {
                tag.setCompoundTag(player + "_morphState" + i, states.get(i).getTag());
            }
        }
        MorphData = MorphMap.morphMap.get(PlayerE.username);
        saveNBTData(PlayerE.getEntityData());
        if (logout) {
            MorphMap.morphMap.remove(PlayerE.username);
            Morph.proxy.tickHandlerServer.playerMorphInfo.remove(player);
            Morph.proxy.tickHandlerServer.playerMorphs.remove(player);
            FMLServerHandler.instance().getServer().logInfo("Morph Player: " + PlayerE.username + " Morph Map Entry removed on logout");
        }
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
