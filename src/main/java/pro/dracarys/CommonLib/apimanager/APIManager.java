package pro.dracarys.CommonLib.apimanager;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import pro.dracarys.CommonLib.apimanager.exceptions.APIRegistrationException;
import pro.dracarys.CommonLib.apimanager.exceptions.MissingHostException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class APIManager {

    private static final Map<API, RegisteredAPI>                HOST_MAP            = new HashMap<>();
    private static final Map<Class<? extends API>, Set<Plugin>> PENDING_API_CLASSES = new HashMap<>();
    private static final Logger                                 LOGGER              = Logger.getLogger("APIManager");

    /**
     * Register an API <p> Call this in {@link Plugin#onLoad()}
     *
     * @param api {@link API} to register
     * @return the registered API
     * @throws APIRegistrationException if the API is already registered
     */
    public static RegisteredAPI registerAPI(API api) throws APIRegistrationException {
        if (HOST_MAP.containsKey(api)) { throw new APIRegistrationException("API for '" + api.getClass().getName() + "' is already registered"); }
        RegisteredAPI registeredAPI = new RegisteredAPI(api);
        HOST_MAP.put(api, registeredAPI);

        //Call load()
        api.load();

        LOGGER.fine("'" + api.getClass().getName() + "' registered as new API");
        return registeredAPI;
    }

    /**
     * Register an API and the plugin-host <p> Call this in {@link Plugin#onLoad()}
     *
     * @param api  {@link API} to register
     * @param host {@link Plugin} host of the API
     * @return the registered api
     * @throws APIRegistrationException if the API is already registered
     * @throws IllegalArgumentException If the Plugin implements API
     * @see #registerAPI(API)
     * @see #registerAPIHost(API, Plugin)
     */
    public static RegisteredAPI registerAPI(API api, Plugin host) throws IllegalArgumentException, APIRegistrationException {
        validatePlugin(host);
        registerAPI(api);
        return registerAPIHost(api, host);
    }

    /**
     * Register events for an API. Note that the {@link API} class and not the {@link Plugin} class should implement {@link Listener} <p> Call this in {@link Plugin#onEnable()}
     *
     * @param api      {@link API} to register events for
     * @param listener {@link Listener} to register
     * @return the API
     * @throws APIRegistrationException If the API is not registered
     */
    public static API registerEvents(API api, Listener listener) throws APIRegistrationException {
        if (!HOST_MAP.containsKey(api)) { throw new APIRegistrationException("API for '" + api.getClass().getName() + "' is not registered"); }
        RegisteredAPI registeredAPI = HOST_MAP.get(api);
        if (registeredAPI.eventsRegistered) {
            return api;//Only register events once
        }
        Bukkit.getPluginManager().registerEvents(listener, registeredAPI.getNextHost());
        registeredAPI.eventsRegistered = true;
        return api;
    }

    /**
     * Initializes an API
     */
    private static void initAPI(API api) throws APIRegistrationException {
        if (!HOST_MAP.containsKey(api)) { throw new APIRegistrationException("API for '" + api.getClass().getName() + "' is not registered"); }
        RegisteredAPI registeredAPI = HOST_MAP.get(api);

        //Call init()
        registeredAPI.init();
    }

    /**
     * Initializes an API <p> Call this in {@link Plugin#onEnable()}
     *
     * @param clazz {@link API} class to initialize
     */
    public static void initAPI(Class<? extends API> clazz) throws APIRegistrationException {
        API clazzAPI = null;
        for (API api : HOST_MAP.keySet()) {
            if (api.getClass().equals(clazz)) {
                clazzAPI = api;
                break;
            }
        }
        if (clazzAPI == null) {
            if (PENDING_API_CLASSES.containsKey(clazz)) {
                LOGGER.info("API class '" + clazz.getName() + "' is not yet initialized. Creating new instance.");
                try {
                    clazzAPI = clazz.newInstance();
                    registerAPI(clazzAPI);
                    for (Plugin plugin : PENDING_API_CLASSES.get(clazz)) {
                        if (plugin != null) { registerAPIHost(clazzAPI, plugin); }
                    }
                } catch (ReflectiveOperationException e) {
                    LOGGER.warning("API class '" + clazz.getName() + "' is missing valid constructor");
                }
                PENDING_API_CLASSES.remove(clazz);
            } else { throw new APIRegistrationException("API for class '" + clazz.getName() + "' is not registered"); }
        }
        initAPI(clazzAPI);
    }

    /**
     * Disable an API
     */
    private static void disableAPI(API api) {
        if (!HOST_MAP.containsKey(api)) { return; }
        RegisteredAPI registeredAPI = HOST_MAP.get(api);

        //Call disable()
        registeredAPI.disable();

        HOST_MAP.remove(api);
    }

    /**
     * Disable an API <p> Can be called in {@link Plugin#onDisable()}, but should not be necessary
     *
     * @param clazz {@link API} class to disable
     */
    public static void disableAPI(Class<? extends API> clazz) {
        API clazzAPI = null;
        for (API api : HOST_MAP.keySet()) {
            if (api.getClass().equals(clazz)) {
                clazzAPI = api;
                break;
            }
        }
        disableAPI(clazzAPI);
    }

    /**
     * Require an API <p> Call this in {@link Plugin#onLoad()} if you are making a Plugin, <br> or call this in {@link API#load()} if you are making an API
     *
     * @param clazz {@link API} class to require
     * @param host  {@link Plugin} host of the API - may be <code>null</code> if called from {@link API#load()}
     */
    public static void require(Class<? extends API> clazz, Plugin host) {
        try {
            if (host == null) { throw new APIRegistrationException(); }
            registerAPIHost(clazz, host);
        } catch (APIRegistrationException e) {
            if (PENDING_API_CLASSES.containsKey(clazz)) {
                PENDING_API_CLASSES.get(clazz).add(host);
            } else {
                Set<Plugin> hosts = new HashSet<>();
                hosts.add(host);
                PENDING_API_CLASSES.put(clazz, hosts);
            }
        }
    }

    /**
     * Register a new Host for an API
     *
     * @param api  {@link API} to register the host for
     * @param host {@link Plugin}-Host to register
     * @return the registered api
     */
    private static RegisteredAPI registerAPIHost(API api, Plugin host) throws APIRegistrationException {
        validatePlugin(host);
        if (!HOST_MAP.containsKey(api)) { throw new APIRegistrationException("API for '" + api.getClass().getName() + "' is not registered"); }
        RegisteredAPI registeredAPI = HOST_MAP.get(api);
        registeredAPI.registerHost(host);

        LOGGER.fine("'" + host.getName() + "' registered as Host for '" + api + "'");
        return registeredAPI;
    }

    /**
     * Register a new Host for an API
     *
     * @param clazz Class of the {@link API} to register
     * @param host  {@link Plugin}-Host to register
     * @return the registered api
     */
    public static RegisteredAPI registerAPIHost(Class<? extends API> clazz, Plugin host) throws APIRegistrationException {
        validatePlugin(host);
        API clazzAPI = null;
        for (API api : HOST_MAP.keySet()) {
            if (api.getClass().equals(clazz)) {
                clazzAPI = api;
                break;
            }
        }
        if (clazzAPI == null) { throw new APIRegistrationException("API for class '" + clazz.getName() + "' is not registered"); }
        return registerAPIHost(clazzAPI, host);
    }

    /**
     * Get an available {@link Plugin} Host for an API
     *
     * @param api {@link API} to get the host for
     * @return {@link Plugin} instance
     */
    public static Plugin getAPIHost(API api) throws APIRegistrationException, MissingHostException {
        if (!HOST_MAP.containsKey(api)) { throw new APIRegistrationException("API for '" + api.getClass().getName() + "' is not registered"); }
        return HOST_MAP.get(api).getNextHost();
    }

    private static void validatePlugin(Plugin plugin) {
        if (plugin instanceof API) { throw new IllegalArgumentException("Plugin must not implement API"); }
    }

}