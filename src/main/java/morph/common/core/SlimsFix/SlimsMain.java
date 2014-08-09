package morph.common.core.SlimsFix;

import cpw.mods.fml.server.*;
import morph.common.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;


/**
 * Created by Ben Byers on 8/6/2014.
 */
public class SlimsMain {

    /*
    public static void onLogin(EntityPlayer player){
        try {
            FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " OnLogin Morphs started!");
            onRespawn(player);
        }catch(Exception e){
            FMLServerHandler.instance().getServer().logInfo("Player: "+player.username+" There was an error, on Login invoking onRespawn!");
        }
    }
    */

    public static void onRespawn(EntityPlayer player) {
        try {
            FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " Starting MorphData!");
            DataHandler respawn = new DataHandler();
            respawn.register(player, respawn);
            FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " MorphData registered!");
            Boolean morphNew = false;
            try {
                if (MorphMap.morphMap.containsKey(player.username))
                    morphNew = true;
            } catch (Exception e) {

            }
            if (morphNew) {
                try {
                    respawn.setMorphData(MorphMap.morphMap.get(player.username));
                } catch (Exception e) {
                    FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " Morph data was no able to be copied from the map!");
                }
            } else {
                FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " has to get new Morph data!");
                try {
                    NBTTagCompound morphData;
                    if (respawn.getHasMD()) {
                        morphData = respawn.getMorphData();
                    } else {
                        morphData = respawn.getNewMorphData();
                    }
                    MorphMap.morphMap.put(player.username, morphData);

                    Morph.proxy.tickHandlerServer.updateSession(player);

                } catch (Exception n) {
                    FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " Error in loading new Morph Data in map! SHIT");
                    FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " Error in MorphMap data loading is" + n.getMessage());
                    n.getCause().printStackTrace();
                }
            }
            Boolean mapCheck = false;
            try {
                if (MorphMap.morphMap.containsKey(player.username))
                    mapCheck = true;
            } catch (Exception e) {
            }
            if (mapCheck) {
                FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " is in the map! Data: " + MorphMap.morphMap.get(player.username).toString());
            } else {
                FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " is not in the map!");
            }
            FMLServerHandler.instance().getServer().getConfigurationManager().saveAllPlayerData();
            FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " On Respawn succesful!");
        } catch (Exception e) {
            FMLServerHandler.instance().getServer().logWarning("Player: " + player.username + " On Respawn failed! :O");
        }
    }

    public static void onDeathRemove(EntityPlayer player) {
        try {
            FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " On Death Remove of morphs!");
            MorphMap.morphMap.remove(player.username);
        } catch (Exception e) {
            FMLServerHandler.instance().getServer().logWarning("Player: " + player.username + " On Death Remove of morphs has failed! :O");
        }
    }

    public static void onLogout(EntityPlayer player) {
        try {
            FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " Logout Player save morph started!");
            DataHandler logout = new DataHandler();
            logout.register(player, logout);
            try {
                logout.setMorphData(MorphMap.morphMap.get(player.username));
                FMLServerHandler.instance().getServer().getConfigurationManager().saveAllPlayerData();
                FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " Logout Player save morph successful!");
            } catch (Exception e) {
            }
        } catch (Exception e) {
            FMLServerHandler.instance().getServer().logWarning("Player: " + player.username + " On morph logout failed!");
        }
    }

    public static void onServerDisable() {
        try {
            for (String s : FMLServerHandler.instance().getServer().getAllUsernames()) {
                try {
                    FMLServerHandler.instance().getServer().logInfo("Player: " + s + " Work save Morphs started");
                    DataHandler stop = new DataHandler();
                    stop.register(FMLServerHandler.instance().getServer().getConfigurationManager().getPlayerForUsername(s), stop);
                    stop.setMorphData(MorphMap.morphMap.get(s));
                    FMLServerHandler.instance().getServer().logInfo("Player: " + s + " Work save Morphs has been successful!");
                } catch (Exception e) {
                    FMLServerHandler.instance().getServer().logWarning("Player: " + s + " Work save Morphs failed!");
                }
            }
        } catch (Exception e) {
        }
        FMLServerHandler.instance().getServer().getConfigurationManager().saveAllPlayerData();
        MorphMap.Stop();
    }

    public static void onWorldSave() {
        try {
            for (String s : FMLServerHandler.instance().getServer().getAllUsernames()) {
                try {
                    FMLServerHandler.instance().getServer().logInfo("Player: " + s + " World save started morph!");
                    DataHandler save = new DataHandler();
                    save.register(FMLServerHandler.instance().getServer().getConfigurationManager().getPlayerForUsername(s), save);
                    save.setMorphData(MorphMap.morphMap.get(s));
                    FMLServerHandler.instance().getServer().logInfo("Player: " + s + " World save morphs successful!");
                } catch (Exception e) {
                    FMLServerHandler.instance().getServer().logWarning("Player: " + s + " World save morphs failed!");
                }
            }
        } catch (Exception e) {
        }
        FMLServerHandler.instance().getServer().getConfigurationManager().saveAllPlayerData();
    }
}
