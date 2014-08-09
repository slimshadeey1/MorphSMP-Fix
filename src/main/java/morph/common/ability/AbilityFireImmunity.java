package morph.common.ability;

import cpw.mods.fml.common.*;
import cpw.mods.fml.relauncher.*;
import morph.api.*;
import morph.common.*;
import morph.common.core.*;
import morph.common.morph.*;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;


public class AbilityFireImmunity extends Ability {

    public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/fireImmunity.png");

    @Override
    public String getType() {
        return "fireImmunity";
    }

    @Override
    public void tick() {
        MorphInfo info = null;
        if (getParent() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) getParent();
            if (!player.worldObj.isRemote) {
                info = Morph.proxy.tickHandlerServer.playerMorphInfo.get(player.username);
            } else {
                info = Morph.proxy.tickHandlerClient.playerMorphInfo.get(player.username);
            }
        }

        boolean fireproof = true;

        if (info != null && info.nextState.entInstance instanceof EntitySkeleton) {
            EntitySkeleton skele = (EntitySkeleton) info.nextState.entInstance;
            if (skele.getSkeletonType() != 1) {
                fireproof = false;
            }
        }

        if (fireproof) {
            if (!getParent().isImmuneToFire()) {
                try {
                    ObfuscationReflectionHelper.setPrivateValue(Entity.class, getParent(), true, ObfHelper.isImmuneToFire);
                } catch (Exception e) {
                    ObfHelper.obfWarning();
                    e.printStackTrace();
                }
            }
            getParent().extinguish();
        }
    }

    @Override
    public void kill() {
        try {
            ObfuscationReflectionHelper.setPrivateValue(Entity.class, getParent(), false, ObfHelper.isImmuneToFire);
        } catch (Exception e) {
            ObfHelper.obfWarning();
            e.printStackTrace();
        }
    }

    @Override
    public Ability clone() {
        return new AbilityFireImmunity();
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


}
