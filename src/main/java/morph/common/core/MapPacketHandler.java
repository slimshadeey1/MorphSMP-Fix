package morph.common.core;

import cpw.mods.fml.common.network.*;
import cpw.mods.fml.relauncher.*;
import cpw.mods.fml.server.*;
import morph.api.*;
import morph.client.entity.*;
import morph.client.morph.*;
import morph.common.*;
import morph.common.morph.*;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.network.*;
import net.minecraft.network.packet.*;

import java.io.*;
import java.util.*;

public class MapPacketHandler
        implements ITinyPacketHandler {

    @Override
    public void handle(NetHandler handler, Packet131MapData mapData) {
        int id = mapData.uniqueID;
        if (handler instanceof NetServerHandler) {
            handleServerPacket((NetServerHandler) handler, mapData.uniqueID, mapData.itemData, (EntityPlayerMP) handler.getPlayer());
        } else {
            handleClientPacket((NetClientHandler) handler, mapData.uniqueID, mapData.itemData);
        }
    }


    public void handleServerPacket(NetServerHandler handler, short id, byte[] data, EntityPlayerMP player) {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
        try {
            switch (id) {
                case 0: {
                    boolean delete = stream.readBoolean();

                    String identifier = stream.readUTF();
                    FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " Morph player ID: "+identifier);
                    MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);
                    if (info != null && info.getMorphing()) {
                        break;
                    }

                    MorphState state = MorphHandler.getMorphState(player, identifier);

                    if (state != null) {
                        if (delete) {
                            FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " Morph player ID: "+identifier+" Delete morph");
                            if (info != null && info.nextState.identifier.equalsIgnoreCase(state.identifier) || state.playerMorph.equalsIgnoreCase(player.username)) {
                                break;
                            }
                            ArrayList<MorphState> states = Morph.proxy.tickHandlerServer.getPlayerMorphs(player.worldObj, player.username);
                            states.remove(state);

                            MorphHandler.updatePlayerOfMorphStates((EntityPlayerMP) player, null, true);
                        } else {
                            MorphState old = info != null ? info.nextState : Morph.proxy.tickHandlerServer.getSelfState(player.worldObj, player.username);

                            MorphInfo info2 = new MorphInfo(player.username, old, state);
                            info2.setMorphing(true);

                            MorphInfo info3 = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);
                            if (info3 != null) {
                                info2.morphAbilities = info3.morphAbilities;
                            }
                            FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " Morph player ID: "+identifier+" morph old: "+old.toString()+" Morph new: "+info2.toString()+" Info3: "+info3.toString());
                            Morph.proxy.tickHandlerServer.playerMorphInfo.put(player.username, info2);

                            PacketDispatcher.sendPacketToAllPlayers(info2.getMorphInfoAsPacket());

                            player.worldObj.playSoundAtEntity(player, "morph:morph", 1.0F, 1.0F);
                        }
                    }

                    break;
                }
                case 1: {
                    boolean favourite = stream.readBoolean();

                    String identifier = stream.readUTF();

                    MorphState state = MorphHandler.getMorphState(player, identifier);
                    if (state != null) {
                        state.isFavourite = favourite;
                        FMLServerHandler.instance().getServer().logInfo("Player: " + player.username + " Morph player ID: "+identifier+" favorite morph state: "+state.toString());
                    }

                    break;
                }
            }
        } catch (IOException e) {
        }

    }

    //TODO Side Split

    @SideOnly(Side.CLIENT)
    public void handleClientPacket(NetClientHandler handler, short id, byte[] data) {
        Minecraft mc = Minecraft.getMinecraft();
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
        try {
            switch (id) {
                case 0: {
                    Entity ent = mc.theWorld.getEntityByID(stream.readInt());
                    Entity ent1 = mc.theWorld.getEntityByID(stream.readInt());

                    if (ent instanceof EntityLivingBase && ent1 instanceof EntityLivingBase) {
                        mc.theWorld.spawnEntityInWorld(new EntityMorphAcquisition(mc.theWorld, (EntityLivingBase) ent, (EntityLivingBase) ent1));
                        ent.setDead();
                    }

                    break;
                }
                case 1: {
                    String name = stream.readUTF();
                    EntityPlayer player = mc.theWorld.getPlayerEntityByName(name);
                    if (player != null) {
                        player.ignoreFrustumCheck = true;
                        MorphInfo info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(name);
                        if (info != null) {
                            ObfHelper.forceSetSize(player, info.nextState.entInstance.width, info.nextState.entInstance.height);
                            player.setPosition(player.posX, player.posY, player.posZ);
                            player.eyeHeight = player.getDefaultEyeHeight();
                            player.ignoreFrustumCheck = false;
                        }
                    }

                    MorphInfoClient info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(name);
                    if (info != null) {
                        for (Ability ability : info.morphAbilities) {
                            if (ability.getParent() != null) {
                                ability.kill();
                            }
                        }
                    }

                    Morph.proxy.tickHandlerClient.playerMorphInfo.remove(name);
                    break;
                }
                case 2: {
                    SessionState.abilities = stream.readBoolean();
                    SessionState.canSleepMorphed = stream.readBoolean();
                    SessionState.allowMorphSelection = stream.readBoolean();
                    SessionState.allowFlight = stream.readBoolean();
                    break;
                }
            }
        } catch (IOException e) {
        }
    }
}