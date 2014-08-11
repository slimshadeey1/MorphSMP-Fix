package morph.common.core.SlimsFix;

import cpw.mods.fml.common.network.*;
import cpw.mods.fml.server.*;
import morph.common.*;
import morph.common.core.*;
import morph.common.morph.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;

import java.util.*;


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
        Boolean onLogin = false;
        Boolean onRespawn = false;
        if (MorphMap.Online.isEmpty() || !MorphMap.Online.isEmpty() && !MorphMap.Online.contains(player.username)) {
            onLogin = true;
            MorphMap.Online.add(player.username);
        } else if (!MorphMap.Online.isEmpty()) {
            onRespawn = true;
        }

        try {
            FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " Starting MorphData!");
            DataHandler respawn = new DataHandler();
            respawn.register(player, respawn, false, false);
            FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " MorphData registered!");

            //--------------------------------------------------------------------------------------------------------------

            if (onLogin) {
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

                MorphHandler.updatePlayerOfMorphStates((EntityPlayerMP) player, null, true);
                for (Map.Entry<String, MorphInfo> e : Morph.proxy.tickHandlerServer.playerMorphInfo.entrySet()) {
                    if (e.getKey().equalsIgnoreCase(player.username)) {
                        continue;
                    }
                    PacketDispatcher.sendPacketToPlayer(e.getValue().getMorphInfoAsPacket(), (Player) player);
                }

                MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);

                if (info != null) {
                    ObfHelper.forceSetSize(player, info.nextState.entInstance.width, info.nextState.entInstance.height);
                    player.setPosition(player.posX, player.posY, player.posZ);
                    player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer) info.nextState.entInstance).username.equalsIgnoreCase(player.username) ? player.getDefaultEyeHeight() : ((EntityPlayer) info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - player.yOffset;
                }
            }
            if (onRespawn) {
                MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);

                if (info != null) {
                    ObfHelper.forceSetSize(player, info.nextState.entInstance.width, info.nextState.entInstance.height);
                    player.setPosition(player.posX, player.posY, player.posZ);
                    player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer) info.nextState.entInstance).username.equalsIgnoreCase(player.username) ? player.getDefaultEyeHeight() : ((EntityPlayer) info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - player.yOffset;
                }
            }
        } catch (Exception e) {
        }
    }

    public static void onDeathRemove(EntityPlayer player) {
    }

    public static void onLogout(EntityPlayer player) {
        try {
            FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " Logout Player save morph started!");
            if (MorphMap.morphMap.get(player.username) != null) {
                MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);
                if (info != null) {
                    NBTTagCompound tag1 = new NBTTagCompound();
                    info.writeNBT(tag1);
                    MorphMap.morphMap.get(player.username).setCompoundTag(player.username + "_morphData", tag1);
                }

                ArrayList<MorphState> states = Morph.proxy.tickHandlerServer.playerMorphs.get(player.username);
                if (states != null) {
                    MorphMap.morphMap.get(player.username).setInteger(player.username + "_morphStatesCount", states.size());
                    for (int i = 0; i < states.size(); i++) {
                        MorphMap.morphMap.get(player.username).setCompoundTag(player.username + "_morphState" + i, states.get(i).getTag());
                    }

                }
            }
            MorphMap.Online.remove(player.username);
            DataHandler logout = new DataHandler();
            try {
                logout.register(player, logout, true, false);
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
                    stop.register(FMLServerHandler.instance().getServer().getConfigurationManager().getPlayerForUsername(s), stop, true, false);
                    FMLServerHandler.instance().getServer().logInfo("Player: " + s + " Work save Morphs has been successful!");
                } catch (Exception e) {
                    FMLServerHandler.instance().getServer().logWarning("Player: " + s + " Work save Morphs failed!");
                }
            }
        } catch (Exception e) {
        }
        MorphMap.Stop();
    }

    public static void onWorldSave() {
        try {
            for (String s : FMLServerHandler.instance().getServer().getAllUsernames()) {
                try {
                    FMLServerHandler.instance().getServer().logInfo("Player: " + s + " World save started morph!");
                    DataHandler save = new DataHandler();
                    save.register(FMLServerHandler.instance().getServer().getConfigurationManager().getPlayerForUsername(s), save, false, true);
                    FMLServerHandler.instance().getServer().logInfo("Player: " + s + " World save morphs successful!");
                } catch (Exception e) {
                    FMLServerHandler.instance().getServer().logWarning("Player: " + s + " World save morphs failed!");
                }
            }
        } catch (Exception e) {
        }
    }
}
