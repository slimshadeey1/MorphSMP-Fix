package morph.common.core;

import cpw.mods.fml.common.registry.*;
import cpw.mods.fml.relauncher.*;
import morph.client.core.*;
import morph.common.*;
import morph.common.ability.mod.*;
import morph.common.morph.mod.*;
import net.minecraft.command.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.server.*;

import java.util.*;
import java.util.Map.*;

public class CommonProxy {
    public TickHandlerClient tickHandlerClient;
    public TickHandlerServer tickHandlerServer;
    public ArrayList<Class> compatibleEntities = new ArrayList<Class>();

    public void initMod() {
        String[] classes = Morph.blacklistedMobs.split(", *");
        for (String className : classes) {
            if (!className.trim().isEmpty()) {
                try {
                    Class clz = Class.forName(className.trim());
                    if (EntityLivingBase.class.isAssignableFrom(clz) && !Morph.blacklistedClasses.contains(clz)) {
                        Morph.blacklistedClasses.add(clz);
                        Morph.console("Blacklisting class: " + clz.getName(), false);
                    }
                } catch (Exception e) {
                    Morph.console("Could not find class to blacklist: " + className.trim(), true);
                }
            }
        }

        String[] names = Morph.whitelistedPlayers.split(", *");
        boolean added = false;
        for (String playerName : names) {
            if (!playerName.trim().isEmpty()) {
                added = true;
                if (!Morph.whitelistedPlayerNames.contains(playerName.trim())) {
                    Morph.whitelistedPlayerNames.add(playerName.trim());
                }
            }
        }
        if (!Morph.whitelistedPlayerNames.isEmpty()) {
            StringBuilder sb = new StringBuilder("Whitelisted players: ");
            for (int i = 0; i < Morph.whitelistedPlayerNames.size(); i++) {
                sb.append(Morph.whitelistedPlayerNames.get(i));
                if (i < Morph.whitelistedPlayerNames.size() - 1) {
                    sb.append(", ");
                }
            }
            Morph.console(sb.toString(), false);
        }
    }

    public void initPostMod() {
        Iterator ite = EntityList.classToStringMapping.entrySet().iterator();
        while (ite.hasNext()) {
            Entry e = (Entry) ite.next();
            Class clz = (Class) e.getKey();
            if (EntityLivingBase.class.isAssignableFrom(clz) && !compatibleEntities.contains(clz)) {
                compatibleEntities.add(clz);
            }
        }
        compatibleEntities.add(EntityPlayer.class);

        if (Morph.modAbilityPatch == 1) {
            AbilitySupport.getInstance().mapAbilities();
        }
        if (Morph.modNBTStripper == 1) {
            NBTStripper.getInstance().mapStripperInfo();
        }
    }

    public void initTickHandlers() {
        tickHandlerServer = new TickHandlerServer();
        TickRegistry.registerTickHandler(tickHandlerServer, Side.SERVER);
    }

    public void initCommands(MinecraftServer server) {
        ICommandManager manager = server.getCommandManager();
        if (manager instanceof CommandHandler) {
            CommandHandler handler = (CommandHandler) manager;
            handler.registerCommand(new CommandMorph());
        }
    }
}
