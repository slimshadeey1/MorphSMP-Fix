package morph.common.core;

import cpw.mods.fml.common.network.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.EnumStatus;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.*;
import net.minecraftforge.common.*;

/**
 * Created by Ben Byers on 8/5/2014.
 */
public class DataHandler implements IExtendedEntityProperties {
    public static String property = "MorphsModData";
    private static EntityPlayer PlayerE;

    //Keys/Data stored
    private String morphsKey = "Morphs";
    private String MdataKey = "Mdata";

    private String morphs;
    private String Mdata;
    //Add more (for each field)

    public DataHandler(EntityPlayer player){
        PlayerE = player;
    }

    public static final void register(EntityPlayer player) {
         player.registerExtendedProperties(DataHandler.property, new DataHandler(player));
    }

    public static final DataHandler getDataHandler(EntityPlayer player)
    {
        return (DataHandler) player.getExtendedProperties(property);
    }
    @Override
    public void init(Entity entity, World world) {}
    @Deprecated
    @Override
    public void loadNBTData(NBTTagCompound compound) {
        NBTTagCompound data = (NBTTagCompound) compound.getTag(property);
        morphs = data.getString(morphsKey);
        Mdata = data.getString(MdataKey);

        //data.setByteArray("HatDat",dat); If I see performance issues I will write everything to UTF then save it as a byte array
    }
    @Deprecated
    @Override
    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound data = new NBTTagCompound();
        //data.setByteArray("MDat",dat); If I see performance issues I will write everything to UTF then save it as a byte array
        data.setString(morphsKey,morphs);
        data.setString(MdataKey,Mdata);
        //For each field needed, just add here and above where they are initialized.
        compound.setTag(property,data);
    }
    private void getData(){
        loadNBTData(PlayerE.getEntityData());
    }
    private void addMorph()
    public static String
}
