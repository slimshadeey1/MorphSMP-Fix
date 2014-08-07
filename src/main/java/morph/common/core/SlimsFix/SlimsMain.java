package morph.common.core.SlimsFix;

import cpw.mods.fml.server.*;
import net.minecraft.entity.player.*;


/**
 * Created by Ben Byers on 8/6/2014.
 */
public class SlimsMain {
    public static void onLogin(EntityPlayer player){
        DataHandler login = new DataHandler();
        login.register(player);
        MorphMap.morphMap.put(player.username,login.getMorphData());
    }
    public static void onDeathRemove(EntityPlayer player){
        MorphMap.morphMap.remove(player.username);
    }
    public static void onRespawn(EntityPlayer player){
        DataHandler respawn = new DataHandler();
        respawn.register(player);
        if(MorphMap.morphMap.containsKey(player.username)){
            respawn.setMorphData(MorphMap.morphMap.get(player.username));
        } else {
            MorphMap.morphMap.put(player.username,respawn.getMorphData());
        }
        FMLServerHandler.instance().getServer().getConfigurationManager().saveAllPlayerData();
    }
    public static void onLogout(EntityPlayer player){
        DataHandler logout = new DataHandler();
        logout.register(player);
        try {
            logout.setMorphData(MorphMap.morphMap.get(player.username));
        }catch (Exception e){}
        FMLServerHandler.instance().getServer().getConfigurationManager().saveAllPlayerData();
    }
    public static void onServerDisable(){
        for(String s:FMLServerHandler.instance().getServer().getAllUsernames()){
            DataHandler stop = new DataHandler();
            stop.register(FMLServerHandler.instance().getServer().getConfigurationManager().getPlayerForUsername(s));
            stop.setMorphData(MorphMap.morphMap.get(s));
        }
        FMLServerHandler.instance().getServer().getConfigurationManager().saveAllPlayerData();
    }
    public static void onWorldSave() {
        for (String s : FMLServerHandler.instance().getServer().getAllUsernames()) {
            DataHandler save = new DataHandler();
            save.register(FMLServerHandler.instance().getServer().getConfigurationManager().getPlayerForUsername(s));
            save.setMorphData(MorphMap.morphMap.get(s));
        }
        FMLServerHandler.instance().getServer().getConfigurationManager().saveAllPlayerData();
    }
}
