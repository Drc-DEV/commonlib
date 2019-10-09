package pro.dracarys.CommonLib;

import org.bukkit.plugin.Plugin;
import pro.dracarys.CommonLib.apimanager.API;

public class CommonLibAPI implements API {

    CommonLibAPI() {}

    //This gets called either by the API manager if a plugin requires this API
    @Override
    public void load() {

    }

    //This gets called either by #initAPI in one of the requiring plugins
    @Override
    public void init(Plugin plugin) {
        CommonLib.checkServerVersion();
    }


    @Override
    public void disable(Plugin plugin) {
    }

}
