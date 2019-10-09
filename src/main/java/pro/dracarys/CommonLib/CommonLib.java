package pro.dracarys.CommonLib;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pro.dracarys.CommonLib.apimanager.APIManager;

public class CommonLib extends JavaPlugin {

    static CommonLib instance;
    CommonLibAPI CommonLibAPI = new CommonLibAPI();

    @Override
    public void onLoad() {
        instance = this;
        //Register this API if the plugin got loaded
        APIManager.registerAPI(CommonLibAPI, this);
    }

    @Override
    public void onEnable() {

        //Initialize this API of the plugin got loaded
        checkServerVersion();
        APIManager.initAPI(CommonLibAPI.class);
    }

    public static CommonLib getInstance(){
        return instance;
    }

    public static int ver;
    public static void checkServerVersion() {
        ver = Integer.parseInt(Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].replace("1_", "").substring(1).replaceAll("_R\\d", ""));
    }
    public static int getServerVersion() {
        return ver;
    }

}
