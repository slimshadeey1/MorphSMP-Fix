package morph.common.ability;

import cpw.mods.fml.relauncher.*;
import morph.api.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;

public class AbilityClimb extends Ability {

    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/climb.png");

    @Override
    public String getType() {
        return "climb";
    }

    @Override
    public void tick() {
        if (getParent().isCollidedHorizontally) {
            getParent().fallDistance = 0.0F;
            if (getParent().isSneaking()) {
                getParent().motionY = 0.0D;
            } else {
                getParent().motionY = 0.1176D; //(0.2D - 0.08D) * 0.98D
            }
        }
        if (!getParent().worldObj.isRemote) {
            double motionX = getParent().posX - getParent().lastTickPosX;
            double motionZ = getParent().posZ - getParent().lastTickPosZ;
            double motionY = getParent().posY - getParent().lastTickPosY - 0.765D; //serverside motion is weird.
            if (motionY > 0.0D && (motionX == 0D || motionZ == 0D)) {
                //most likely climbing.
                getParent().fallDistance = 0.0F;
            }
        }
    }

    @Override
    public void kill() {
    }

    @Override
    public Ability clone() {
        return new AbilityClimb();
    }

    @Override
    public void postRender() {
    }

    @Override
    public void save(NBTTagCompound tag) {
    }

    @Override
    public void load(NBTTagCompound tag) {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon() {
        return iconResource;
    }

}
