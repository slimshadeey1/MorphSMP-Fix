package morph.common.morph;

import morph.api.*;
import morph.common.*;
import morph.common.ability.*;
import net.minecraft.nbt.*;
import net.minecraft.network.packet.*;
import net.minecraft.world.*;
import net.minecraftforge.common.*;

import java.io.*;
import java.util.*;

public class MorphInfo {
    public String playerName;
    public MorphState prevState;

    public MorphState nextState;
    public int morphProgress; //up to 80, 3 sec sound files, 0.5 sec between sounds where the skin turns black
    public ArrayList<Ability> morphAbilities = new ArrayList<Ability>();
    public boolean flying;
    public boolean sleeping;
    public boolean firstUpdate;
    private boolean morphing; //if true, increase progress

    public MorphInfo() {
        playerName = "";
    }

    public MorphInfo(String name, MorphState prev, MorphState next) {
        playerName = name;

        prevState = prev;

        nextState = next;

        morphing = false;
        morphProgress = 0;

        flying = false;
        sleeping = false;
        firstUpdate = true;
    }

    public boolean getMorphing() {
        return morphing;
    }

    public void setMorphing(boolean flag) {
        morphing = flag;
    }

    public Packet250CustomPayload getMorphInfoAsPacket() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(bytes);
        try {
            stream.writeByte(0); //id
            stream.writeUTF(playerName);

            stream.writeBoolean(morphing);
            stream.writeInt(morphProgress);

            stream.writeBoolean(prevState != null);
            if (prevState != null) {
                Morph.writeNBTTagCompound(prevState.getTag(), stream);
            }
            stream.writeBoolean(nextState != null);
            if (nextState != null) {
                Morph.writeNBTTagCompound(nextState.getTag(), stream);//hmm
            }

            stream.writeBoolean(flying);
        } catch (IOException e) {

        }
        return new Packet250CustomPayload("Morph", bytes.toByteArray());
    }

    public void writeNBT(NBTTagCompound tag) {
        tag.setString("playerName", playerName);

        tag.setInteger("dimension", nextState.entInstance.dimension);

        tag.setCompoundTag("nextState", nextState.getTag());

        tag.setBoolean("isFlying", flying);
    }

    public void readNBT(NBTTagCompound tag) {
        playerName = tag.getString("playerName");

        World dimension = DimensionManager.getWorld(tag.getInteger("dimension"));

        if (dimension == null) {
            dimension = DimensionManager.getWorld(0);
        }

        nextState = new MorphState(dimension, playerName, playerName, null, false);

        nextState.readTag(dimension, tag.getCompoundTag("nextState"));

        morphing = true;
        morphProgress = 80;

        ArrayList<Ability> newAbilities = AbilityHandler.getEntityAbilities(nextState.entInstance.getClass());
        morphAbilities = new ArrayList<Ability>();
        for (Ability ability : newAbilities) {
            try {
                morphAbilities.add(ability.clone());
            } catch (Exception e1) {
            }
        }

        flying = tag.getBoolean("isFlying");
    }
}

