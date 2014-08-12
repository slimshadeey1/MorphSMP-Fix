package morph.common.core;

import morph.common.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraftforge.common.*;

import java.io.*;

/**
 * Created by Ben Byers on 8/12/2014.
 */
public class MorphPlayerData implements IExtendedEntityProperties{
    public final String PROPERTY = "MorphData";
    private final EntityPlayer Player;
    protected NBTTagCompound Data;
    public MorphPlayerData(EntityPlayer player){
        Player = player;
    }
    public void Register(){
        Player.registerExtendedProperties(PROPERTY, this);
    }

    @Override
    public void init(Entity entity, World world) {

    }

    public void setData(NBTTagCompound data) {
        Data = data;
        saveNBTData(Player.getEntityData());
    }

    public NBTTagCompound getData() {
        loadNBTData(Player.getEntityData());
        return Data;
    }

    @Override
    public void loadNBTData(NBTTagCompound nbtTagCompound) {
        Data = (NBTTagCompound)nbtTagCompound.getTag(PROPERTY);
    }

    @Override
    public void saveNBTData(NBTTagCompound nbtTagCompound) {
        if(Data != null)
        nbtTagCompound.setTag(PROPERTY,Data);
    }
}
