package morph.common.core;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.network.*;
import cpw.mods.fml.relauncher.*;
import morph.common.*;
import morph.common.morph.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.packet.*;
import net.minecraft.server.*;

import java.util.*;
import java.util.Map.*;

public class ConnectionHandler
        implements IConnectionHandler, IPlayerTracker {

    @Override
    public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) //client: remove server
    {
        onClientConnected();
    }

    @Override
    public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) //client: local server
    {
        onClientConnected();
    }

    public void onClientConnected() {
        Morph.proxy.tickHandlerClient.playerMorphInfo.clear();
        Morph.proxy.tickHandlerClient.playerMorphCatMap.clear();
    }

    @Override
    public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager) //server
    {
        return null;
    }

    @Override
    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) //client
    {
    }

    @Override
    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) //server
    {

    }

    @Override
    public void connectionClosed(INetworkManager manager) //both
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            Morph.proxy.tickHandlerClient.playerMorphInfo.clear();
            Morph.proxy.tickHandlerClient.playerMorphCatMap.clear();
        }
    }

    //IPlayerTracker area

    @Override
    public void onPlayerLogin(EntityPlayer player) {
        Morph.proxy.tickHandlerServer.updateSession(player);

        ArrayList list = Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player.username);


        NBTTagCompound tag = Morph.proxy.tickHandlerServer.saveData(player.username);

        MorphHandler.addOrGetMorphState(list, new MorphState(player.worldObj, player.username, player.username, null, player.worldObj.isRemote));
        int count;
        try {
            count = tag.getInteger(player.username + "_morphStatesCount");
        } catch (NullPointerException n) {
            count = 0;
        }
        if (count > 0) {

            for (int i = 0; i < count; i++) {
                MorphState state = new MorphState(player.worldObj, player.username, player.username, null, false);
                state.readTag(player.worldObj, tag.getCompoundTag(player.username + "_morphState" + i));
                if (!state.identifier.equalsIgnoreCase("")) {
                    MorphHandler.addOrGetMorphState(list, state);
                }
            }
        }
        NBTTagCompound tag1;
        try {
            tag1 = tag.getCompoundTag(player.username + "_morphData");
        } catch (NullPointerException n) {
            tag1 = new NBTTagCompound();
        }
        if (tag1.hasKey("playerName")) {
            MorphInfo info = new MorphInfo();
            info.readNBT(tag1);
            if (!info.nextState.playerName.equals(info.nextState.playerMorph)) {
                Morph.proxy.tickHandlerServer.playerMorphInfo.put(info.playerName, info);
                MorphHandler.addOrGetMorphState(list, info.nextState);

                PacketDispatcher.sendPacketToAllPlayers(info.getMorphInfoAsPacket());
            }
        }


        MorphHandler.updatePlayerOfMorphStates((EntityPlayerMP) player, null, true);
        for (Entry<String, MorphInfo> e : Morph.proxy.tickHandlerServer.playerMorphInfo.entrySet()) {
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

    @Override
    public void onPlayerLogout(EntityPlayer player) {
        if (Morph.proxy.tickHandlerServer.saveData(player.username) != null) {
            NBTTagCompound tag = Morph.proxy.tickHandlerServer.saveData(player.username);
            MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);
            if (info != null) {
                NBTTagCompound tag1 = new NBTTagCompound();
                info.writeNBT(tag1);
                tag.setCompoundTag(player.username + "_morphData", tag1);
            }

            ArrayList<MorphState> states = Morph.proxy.tickHandlerServer.playerMorphs.get(player.username);
            if (states != null) {
                tag.setInteger(player.username + "_morphStatesCount", states.size());
                for (int i = 0; i < states.size(); i++) {
                    tag.setCompoundTag(player.username + "_morphState" + i, states.get(i).getTag());
                }

            }
            Morph.proxy.tickHandlerServer.saveSaveData(tag, player);
            Morph.proxy.tickHandlerServer.playerMorphs.remove(player.username);
            Morph.proxy.tickHandlerServer.playerMorphInfo.remove(player.username);
        }
    }

    @Override
    public void onPlayerChangedDimension(EntityPlayer player) {
        MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);

        if (info != null) {
            ObfHelper.forceSetSize(player, info.nextState.entInstance.width, info.nextState.entInstance.height);
            player.setPosition(player.posX, player.posY, player.posZ);
            player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer) info.nextState.entInstance).username.equalsIgnoreCase(player.username) ? player.getDefaultEyeHeight() : ((EntityPlayer) info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - player.yOffset;
        }
    }

    @Override
    public void onPlayerRespawn(EntityPlayer player) {
        MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);

        if (info != null) {
            ObfHelper.forceSetSize(player, info.nextState.entInstance.width, info.nextState.entInstance.height);
            player.setPosition(player.posX, player.posY, player.posZ);
            player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer) info.nextState.entInstance).username.equalsIgnoreCase(player.username) ? player.getDefaultEyeHeight() : ((EntityPlayer) info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - player.yOffset;
        }
    }

}
