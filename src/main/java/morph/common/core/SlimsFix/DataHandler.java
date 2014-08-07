package morph.common.core.SlimsFix;

import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.*;
import net.minecraftforge.common.*;

/**
 *
 *  Created by Ben Byers on 8/5/2014.
 */
public class DataHandler implements IExtendedEntityProperties {
    public static final String property = "MorphsModData";
    private EntityPlayer PlayerE;

    private NBTTagCompound MorphData;
    //Keys/Data stored

    //Add more (for each field)

    public DataHandler(){
    }

    public final void register(EntityPlayer player) {
        if(player.getExtendedProperties(property) == null) {
            player.registerExtendedProperties(DataHandler.property, new DataHandler());
        }
        PlayerE = player;
    }

    public void setMorphData(NBTTagCompound morphData) {
        MorphData = morphData;
        saveNBTData(PlayerE.getEntityData());
    }

    public NBTTagCompound getMorphData() {
        loadNBTData(PlayerE.getEntityData());
        return MorphData;
    }

    @Override
    public void init(Entity entity, World world) {}

    @Override
    public void loadNBTData(NBTTagCompound compound) {//Basically This processes the NBT Data and returns what we use.
        MorphData = (NBTTagCompound) compound.getTag(property);
        //data.setByteArray("HatDat",dat); If I see performance issues I will write everything to UTF then save it as a byte array
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound data = MorphData;
        //data.setByteArray("MDat",dat); If I see performance issues I will write everything to UTF then save it as a byte array
        //For each field needed, just add here and above where they are initialized.
        compound.setTag(property, data);
    }

}
