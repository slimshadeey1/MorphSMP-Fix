package morph.common.ability;

import cpw.mods.fml.relauncher.*;
import morph.api.*;
import morph.common.*;
import morph.common.morph.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;

public class AbilitySunburn extends Ability {

    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/sunburn.png");
    public AbilityFireImmunity fireImmunityInstance = new AbilityFireImmunity();

    @Override
    public String getType() {
        return "sunburn";
    }

    @Override
    public void tick() {
        boolean isChild = false;

        if (!getParent().worldObj.isRemote && getParent() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) getParent();
            MorphInfo info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);

            if (player.capabilities.isCreativeMode) {
                isChild = true;
            }

            if (info != null && info.nextState.entInstance.isChild()) {
                isChild = true;
            }
            //TODO for "The Thing" as well
        }

        if (getParent().worldObj.isDaytime() && !getParent().worldObj.isRemote && !isChild) {
            float f = getParent().getBrightness(1.0F);

            if (f > 0.5F && getParent().getRNG().nextFloat() * 30.0F < (f - 0.4F) * 2.0F && getParent().worldObj.canBlockSeeTheSky(MathHelper.floor_double(getParent().posX), MathHelper.floor_double(getParent().posY), MathHelper.floor_double(getParent().posZ))) {
                boolean flag = true;
                ItemStack itemstack = getParent().getCurrentItemOrArmor(4);

                if (itemstack != null) {
                    if (itemstack.isItemStackDamageable()) {
                        itemstack.setItemDamage(itemstack.getItemDamageForDisplay() + getParent().getRNG().nextInt(2));

                        if (itemstack.getItemDamageForDisplay() >= itemstack.getMaxDamage()) {
                            getParent().renderBrokenItemStack(itemstack);
                            getParent().setCurrentItemOrArmor(4, (ItemStack) null);
                        }
                    }

                    flag = false;
                }

                if (flag) {
                    getParent().setFire(8);
                }
            }
        }
    }

    @Override
    public void kill() {
    }

    @Override
    public Ability clone() {
        return new AbilitySunburn();
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
    public ResourceLocation getIcon() {
        return iconResource;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean entityHasAbility(EntityLivingBase living) {
        if (AbilityHandler.hasAbility(living.getClass(), "fireImmunity") && fireImmunityInstance.entityHasAbility(living)) {
            return false;
        }
        return !living.isChild();
    }
}
