package nl.multitime.multiPermissions;

import nl.multitime.multiPermissions.commands.PermissionsCommand;
import nl.multitime.multiPermissions.listeners.PlayerListener;
import nl.multitime.multiPermissions.managers.PermissionManager;
import nl.multitime.multiPermissions.storage.StorageManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class MultiPermissions extends JavaPlugin {

    private static MultiPermissions instance;
    private PermissionManager permissionManager;
    private StorageManager storageManager;
    private Logger logger;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();

        saveDefaultConfig();

        storageManager = new StorageManager(this);
        permissionManager = new PermissionManager(this);

        getCommand("perms").setExecutor(new PermissionsCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        logger.info("MultiPermissions is succesvol geladen!");
    }

    @Override
    public void onDisable() {
        if (storageManager != null) {
            storageManager.saveData();
        }

        logger.info("MultiPermissions is uitgeschakeld!");
    }

    public static MultiPermissions getInstance() {
        return instance;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }
}
