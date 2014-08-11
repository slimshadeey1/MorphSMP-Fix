package morph.common.core;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.network.*;
import cpw.mods.fml.relauncher.*;
import morph.common.*;
import morph.common.core.SlimsFix.*;
import morph.common.morph.*;
import net.minecraft.entity.player.*;
import net.minecraft.network.*;
import net.minecraft.network.packet.*;
import net.minecraft.server.*;

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
        SlimsMain.onRespawn(player);
       /* Boolean morphReady = false;
        try {
            if (MorphMap.morphMap.get(player.username) != null)
                morphReady = true;
        }catch (Exception e){
            FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " does not have data ready in map!");
        }

        if (morphReady) {
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
        }*/
    }

    @Override
    public void onPlayerLogout(EntityPlayer player) {
        /*if (MorphMap.morphMap.get(player.username) != null) {
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
        }*/
        SlimsMain.onLogout(player);
    }

    @Override
    public void onPlayerChangedDimension(EntityPlayer player) {
        MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);

        if (info != null) {
            ObfHelper.forceSetSize(player, info.nextState.entInstance.width, info.nextState.entInstance.height);
            player.setPosition(player.posX, player.posY, player.posZ);
            player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer) info.nextState.entInstance).username.equalsIgnoreCase(player.username) ? player.getDefaultEyeHeight() : ((EntityPlayer) info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - player.yOffset;
        }

        if (player.dimension == -1 && MorphMap.morphMap.get(player.username) != null) {
            if (!MorphMap.morphMap.get(player.username).getBoolean("travelledToNether")) {
                MorphMap.morphMap.get(player.username).setBoolean("travelledToNether", true);
                if (Morph.disableEarlyGameFlight == 1) {
                    SessionState.allowFlight = true;
                    Morph.proxy.tickHandlerServer.updateSession(null);
                }
            }
        }
    }

    @Override
    public void onPlayerRespawn(EntityPlayer player) {
        SlimsMain.onRespawn(player);
    }

}
