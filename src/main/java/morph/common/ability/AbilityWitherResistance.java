package morph.common.ability;

import cpw.mods.fml.relauncher.*;
import morph.api.*;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.nbt.*;
import net.minecraft.potion.*;
import net.minecraft.util.*;

public class AbilityWitherResistance extends Ability {

    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/witherResistance.png");

    @Override
    public String getType() {
        return "witherResistance";
    }

    @Override
    public void tick() {
        if (this.getParent().isPotionActive(Potion.wither))
            this.getParent().removePotionEffect(Potion.wither.id);
    }

    @Override
    public void kill() {
    }

    @Override
    public Ability clone() {
        return new AbilityWitherResistance();
    }

    @Override
    public void save(NBTTagCompound tag) {
    }

    @Override
    public void load(NBTTagCompound tag) {
    }

    @Override
    public void postRender() {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean entityHasAbility(EntityLivingBase living) {
        if (living instanceof EntitySkeleton) {
            EntitySkeleton skele = (EntitySkeleton) living;
            if (skele.getSkeletonType() != 1) {
                return false;
            }
        }
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getIcon() {
        return iconResource;
    }
}
