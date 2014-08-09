package morph.common.ability;

import cpw.mods.fml.relauncher.*;
import morph.api.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;


public class AbilityFloat extends Ability {

    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/float.png");
    public double terminalVelocity;
    public boolean negateFallDistance;

    public AbilityFloat() {
        terminalVelocity = -1000D;
        negateFallDistance = false;
    }

    public AbilityFloat(double termVelo, boolean negateFall) {
        terminalVelocity = termVelo;
        negateFallDistance = negateFall;
    }

    @Override
    public Ability parse(String[] args) {
        terminalVelocity = Double.parseDouble(args[0]);
        negateFallDistance = Boolean.parseBoolean(args[1]);
        return this;
    }

    @Override
    public String getType() {
        return "float";
    }

    @Override
    public void tick() {
        boolean flying = false;
        if (getParent() instanceof EntityPlayer) {
            flying = ((EntityPlayer) getParent()).capabilities.isFlying;
        }
        if (!flying && getParent().motionY < terminalVelocity) {
            getParent().motionY = terminalVelocity;
            if (negateFallDistance) {
                getParent().fallDistance = 0.0F;
            }
        }
    }

    @Override
    public void kill() {
    }

    @Override
    public Ability clone() {
        return new AbilityFloat(terminalVelocity, negateFallDistance);
    }

    @Override
    public void postRender() {
    }

    @Override
    public void save(NBTTagCompound tag) {
        tag.setDouble("terminalVelocity", terminalVelocity);
        tag.setBoolean("negateFallDistance", negateFallDistance);
    }

    @Override
    public void load(NBTTagCompound tag) {
        terminalVelocity = tag.getDouble("terminalVelocity");
        negateFallDistance = tag.getBoolean("negateFallDistance");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon() {
        return iconResource;
    }

}
