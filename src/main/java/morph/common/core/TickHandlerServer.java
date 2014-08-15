package morph.common.core;

import cpw.mods.fml.common.*;
import cpw.mods.fml.common.network.*;
import morph.api.*;
import morph.common.*;
import morph.common.ability.*;
import morph.common.morph.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import net.minecraft.network.packet.*;
import net.minecraft.world.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.logging.*;

public class TickHandlerServer
        implements ITickHandler {
    public final String PROPERTY = "MorphData";
    public long clock;
    public int lastIndex;
    public HashMap<String, MorphInfo> playerMorphInfo = new HashMap<String, MorphInfo>();
    public HashMap<String, ArrayList<MorphState>> playerMorphs = new HashMap<String, ArrayList<MorphState>>();

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
        if (type.equals(EnumSet.of(TickType.WORLD))) {
            preWorldTick((WorldServer) tickData[0]);
        } else if (type.equals(EnumSet.of(TickType.PLAYER))) {
            prePlayerTick((WorldServer) ((EntityPlayerMP) tickData[0]).worldObj, (EntityPlayerMP) tickData[0]);
        }
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        if (type.equals(EnumSet.of(TickType.WORLD))) {
            worldTick((WorldServer) tickData[0]);
        } else if (type.equals(EnumSet.of(TickType.SERVER))) {
            serverTick();
        } else if (type.equals(EnumSet.of(TickType.PLAYER))) {
            playerTick((WorldServer) ((EntityPlayerMP) tickData[0]).worldObj, (EntityPlayerMP) tickData[0]);
        }
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.WORLD, TickType.PLAYER, TickType.SERVER);
    }

    @Override
    public String getLabel() {
        return "TickHandlerServerMorph";
    }

    public void serverTick() {
        Iterator<Entry<String, MorphInfo>> ite = playerMorphInfo.entrySet().iterator();
        while (ite.hasNext()) {
            Entry<String, MorphInfo> e = ite.next();
            MorphInfo info = e.getValue();

            EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(info.playerName);

            if (info.getMorphing()) {
                info.morphProgress++;
                if (info.morphProgress > 80) {
                    info.morphProgress = 80;
                    info.setMorphing(false);

                    if (player != null) {
                        ObfHelper.forceSetSize(player, info.nextState.entInstance.width, info.nextState.entInstance.height);
                        player.setPosition(player.posX, player.posY, player.posZ);
                        player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer) info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - player.yOffset;

                        ArrayList<Ability> newAbilities = AbilityHandler.getEntityAbilities(info.nextState.entInstance.getClass());
                        ArrayList<Ability> oldAbilities = info.morphAbilities;
                        info.morphAbilities = new ArrayList<Ability>();
                        for (Ability ability : newAbilities) {
                            try {
                                Ability clone = ability.clone();
                                clone.setParent(player);
                                info.morphAbilities.add(clone);
                            } catch (Exception e1) {
                            }
                        }
                        for (Ability ability : oldAbilities) {
                            boolean isRemoved = true;
                            for (Ability newAbility : info.morphAbilities) {
                                if (newAbility.getType().equalsIgnoreCase(ability.getType())) {
                                    isRemoved = false;
                                    break;
                                }
                            }
                            if (isRemoved && ability.getParent() != null) {
                                ability.kill();
                            }
                        }
                    }

                    if (info.nextState.playerMorph.equalsIgnoreCase(e.getKey())) {
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        DataOutputStream stream = new DataOutputStream(bytes);
                        try {
                            stream.writeUTF(e.getKey());

                            PacketDispatcher.sendPacketToAllPlayers(new Packet131MapData((short) Morph.getNetId(), (short) 1, bytes.toByteArray()));
                        } catch (IOException e1) {

                        } catch (Exception e1) {
                            ObfHelper.obfWarning();
                            e1.printStackTrace();
                        }

                        for (Ability ability : info.morphAbilities) {
                            if (ability.getParent() != null) {
                                ability.kill();
                            }
                        }
                        NBTTagCompound tag = saveData(player);
                        try {
                            tag.removeTag(e.getKey() + "_morphData");
                        } catch (NullPointerException n) {

                        }
                        saveSaveData(tag, player);

                        ite.remove();
                    }
                }
            }

            if (player != null) {
                if (player.isPlayerSleeping() && player.sleepTimer > 0) {
                    info.sleeping = true;
                } else if (info.sleeping) {
                    info.sleeping = false;
                    ObfHelper.forceSetSize(player, info.nextState.entInstance.width, info.nextState.entInstance.height);
                    player.setPosition(player.posX, player.posY, player.posZ);
                    player.eyeHeight = info.nextState.entInstance instanceof EntityPlayer ? ((EntityPlayer) info.nextState.entInstance).getDefaultEyeHeight() : info.nextState.entInstance.getEyeHeight() - player.yOffset;
                }
            }

            for (Ability ability : info.morphAbilities) {
                if (player != null && ability.getParent() == player || player == null && ability.getParent() != null) {
                    ability.tick();
                    if (!info.firstUpdate && ability instanceof AbilityFly && player != null) {
                        info.flying = player.capabilities.isFlying;
                    }
                } else {
                    ability.setParent(player);
                }
            }

            info.firstUpdate = false;

            //					if(info.morphProgress > 70)
            //					{
            //						info.nextState.entInstance.isDead = false;
            //						info.nextState.entInstance.setLocationAndAngles(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
            //						info.nextState.entInstance.onUpdate();
            //					}
        }

        //				ArrayList<MorphState> states = getPlayerMorphs(world, "ohaiiChun");
        //				for(MorphState state : states)
        //				{
        //					System.out.println(state.identifier);
        //				}
    }

    public void preWorldTick(WorldServer world) {
    }

    public void worldTick(WorldServer world) {
        if (clock != world.getWorldTime() || !world.getGameRules().getGameRuleBooleanValue("doDaylightCycle")) {
            clock = world.getWorldTime();

//						for(int i = 0 ; i < world.loadedEntityList.size(); i++)
//						{
//							if(world.loadedEntityList.get(i) instanceof EntityCow)
//							{
//								((EntityCow)world.loadedEntityList.get(i)).setDead();
//							}
//						}
        }
    }

    public void prePlayerTick(WorldServer world, EntityPlayerMP player) {
        //		MorphInfo info = playerMorphInfo.get(player.username);
        //		if(info != null)
        //		{
        //		}

    }

    public void playerTick(WorldServer world, EntityPlayerMP player) {
        MorphInfo info = playerMorphInfo.get(player.username);
        if (info != null) {
            float prog = info.morphProgress > 10 ? (((float) info.morphProgress) / 60F) : 0.0F;
            if (prog > 1.0F) {
                prog = 1.0F;
            }

            prog = (float) Math.pow(prog, 2);

            float prev = info.prevState != null && !(info.prevState.entInstance instanceof EntityPlayer) ? info.prevState.entInstance.getEyeHeight() : player.yOffset;
            float next = info.nextState != null && !(info.nextState.entInstance instanceof EntityPlayer) ? info.nextState.entInstance.getEyeHeight() : player.yOffset;
            double ySize = player.yOffset - (prev + (next - prev) * prog);
            player.lastTickPosY += ySize;
            player.prevPosY += ySize;
            player.posY += ySize;
        }

    }

    public MorphState getSelfState(World world, String name) {
        ArrayList<MorphState> list = getPlayerMorphs(world, name);
        for (MorphState state : list) {
            if (state.playerName.equalsIgnoreCase(state.playerMorph)) {
                return state;
            }
        }
        return new MorphState(world, name, name, null, world.isRemote);
    }

    public ArrayList<MorphState> getPlayerMorphs(World world, String name) {
        ArrayList<MorphState> list = playerMorphs.get(name);
        if (list == null) {
            list = new ArrayList<MorphState>();
            playerMorphs.put(name, list);
            list.add(0, new MorphState(world, name, name, null, world.isRemote));
        }
        boolean found = false;
        for (MorphState state : list) {
            if (state.playerMorph.equals(name)) {
                found = true;
                break;
            }
        }
        if (!found) {
            list.add(0, new MorphState(world, name, name, null, world.isRemote));
        }
        return list;
    }

    public boolean hasMorphState(EntityPlayer player, MorphState state) {
        ArrayList<MorphState> states = getPlayerMorphs(player.worldObj, player.username);
        if (!state.playerMorph.equalsIgnoreCase("")) {
            for (MorphState mState : states) {
                if (mState.playerMorph.equalsIgnoreCase(state.playerMorph)) {
                    return true;
                }
            }
        } else {
            for (MorphState mState : states) {
                if (mState.identifier.equalsIgnoreCase(state.identifier)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void updateSession(EntityPlayer player) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(bytes);

            stream.writeBoolean(SessionState.abilities);
            stream.writeBoolean(SessionState.canSleepMorphed);
            stream.writeBoolean(SessionState.allowMorphSelection);
            stream.writeBoolean(SessionState.allowFlight);

            if (player != null) {
                PacketDispatcher.sendPacketToPlayer(new Packet131MapData((short) Morph.getNetId(), (short) 2, bytes.toByteArray()), (Player) player);
            } else {
                PacketDispatcher.sendPacketToAllPlayers(new Packet131MapData((short) Morph.getNetId(), (short) 2, bytes.toByteArray()));
            }
        } catch (IOException e) {

        }
    }
//    public ArrayList<MorphState> loadSaveData(World world, EntityPlayer player){
//        NBTTagCompound tag = saveData(player);
//        int count = tag.getInteger(player.username + "_morphStatesCount");
//        ArrayList<MorphState> list = new ArrayList<MorphState>();
//        if (count > 0) {
//            for (int i = 0; i < count; i++) {
//                MorphState state = new MorphState(player.worldObj, player.username, player.username, null, false);
//                state.readTag(player.worldObj, tag.getCompoundTag(player.username + "_morphState" + i));
//                list.add(state);
//            }
//        }else {
//            list.add(0, new MorphState(world, player.username, player.username, null, world.isRemote));
//        }
//        playerMorphs.put(player.username,list);
//         return getPlayerMorphs(world,player.username);
//    }
    public NBTTagCompound saveData(EntityPlayer player) {
        if(player!=null) {
            MorphPlayerData data = new MorphPlayerData(player);
            if (player.getExtendedProperties(PROPERTY) != null) {
                try {
                    if (data.getData() != null)
                        return data.getData();
                    else
                        return new NBTTagCompound();
                } catch (NullPointerException n) {
                    return new NBTTagCompound();
                }
            } else {
                data.Register();
                try {
                    if (data.getData() != null)
                        return data.getData();
                    else
                        return new NBTTagCompound();
                } catch (NullPointerException n) {
                    return new NBTTagCompound();
                }
            }
        }else{
            FMLLog.getLogger().log(Level.SEVERE,"Error recovering player NBT data! Be prepared for null Pointer Exception");
            return null;
        }
    }

    public void saveSaveData(NBTTagCompound tag, EntityPlayer player) {
            MorphPlayerData data = new MorphPlayerData(player);
            if (player.getExtendedProperties(PROPERTY) == null) {
                data.Register();
            }
            data.setData(tag);
        }
}