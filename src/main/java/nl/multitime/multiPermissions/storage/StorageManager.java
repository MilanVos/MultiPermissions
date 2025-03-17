package nl.multitime.multiPermissions.storage;

import nl.multitime.multiPermissions.MultiPermissions;
import nl.multitime.multiPermissions.managers.PermissionManager;
import nl.multitime.multiPermissions.models.PermissionGroup;
import nl.multitime.multiPermissions.models.PermissionUser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class StorageManager {

    private final MultiPermissions plugin;
    private final File usersFile;
    private final File groupsFile;
    private FileConfiguration usersConfig;
    private FileConfiguration groupsConfig;

    public StorageManager(MultiPermissions plugin) {
        this.plugin = plugin;
        this.usersFile = new File(plugin.getDataFolder(), "users.yml");
        this.groupsFile = new File(plugin.getDataFolder(), "groups.yml");

        if (!usersFile.exists()) {
            try {
                usersFile.getParentFile().mkdirs();
                usersFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Kon users.yml niet aanmaken", e);
            }
        }

        if (!groupsFile.exists()) {
            try {
                groupsFile.getParentFile().mkdirs();
                groupsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Kon groups.yml niet aanmaken", e);
            }
        }

        usersConfig = YamlConfiguration.loadConfiguration(usersFile);
        groupsConfig = YamlConfiguration.loadConfiguration(groupsFile);
    }

    public Map<UUID, PermissionUser> loadUsers() {
        Map<UUID, PermissionUser> users = new HashMap<>();

        ConfigurationSection usersSection = usersConfig.getConfigurationSection("users");
        if (usersSection == null) {
            return users;
        }

        for (String uuidString : usersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                ConfigurationSection userSection = usersSection.getConfigurationSection(uuidString);

                if (userSection != null) {
                    String name = userSection.getString("name", "Unknown");
                    PermissionUser user = new PermissionUser(uuid, name);

                    for (String permission : userSection.getStringList("permissions")) {
                        user.addPermission(permission);
                    }
                    for (String group : userSection.getStringList("groups")) {
                        user.addGroup(group);
                    }

                    users.put(uuid, user);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Ongeldige UUID in users.yml: " + uuidString);
            }
        }

        return users;
    }

    public Map<String, PermissionGroup> loadGroups() {
        Map<String, PermissionGroup> groups = new HashMap<>();

        ConfigurationSection groupsSection = groupsConfig.getConfigurationSection("groups");
        if (groupsSection == null) {
            return groups;
        }

        for (String groupName : groupsSection.getKeys(false)) {
            ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);

            if (groupSection != null) {
                PermissionGroup group = new PermissionGroup(groupName);

                for (String permission : groupSection.getStringList("permissions")) {
                    group.addPermission(permission);
                }

                groups.put(groupName, group);
            }
        }

        return groups;
    }

    public void saveUsers(Map<UUID, PermissionUser> users) {
        usersConfig.set("users", null);

        for (Map.Entry<UUID, PermissionUser> entry : users.entrySet()) {
            UUID uuid = entry.getKey();
            PermissionUser user = entry.getValue();

            String path = "users." + uuid.toString();
            usersConfig.set(path + ".name", user.getName());
            usersConfig.set(path + ".permissions", user.getPermissions().toArray());
            usersConfig.set(path + ".groups", user.getGroups().toArray());
        }

        try {
            usersConfig.save(usersFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Kon users.yml niet opslaan", e);
        }
    }

    public void saveGroups(Map<String, PermissionGroup> groups) {
        groupsConfig.set("groups", null);

        for (Map.Entry<String, PermissionGroup> entry : groups.entrySet()) {
            String groupName = entry.getKey();
            PermissionGroup group = entry.getValue();

            String path = "groups." + groupName;
            groupsConfig.set(path + ".permissions", group.getPermissions().toArray());
        }

        try {
            groupsConfig.save(groupsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Kon groups.yml niet opslaan", e);
        }
    }

    public void saveData() {
        PermissionManager permissionManager = plugin.getPermissionManager();

        if (permissionManager != null) {
            permissionManager.saveUsers();
        }

        if (permissionManager != null) {
            Map<String, PermissionGroup> groupsMap = new HashMap<>();
            for (PermissionGroup group : permissionManager.getAllGroups()) {
                groupsMap.put(group.getName(), group);
            }
            this.saveGroups(groupsMap);
        }

        try {
            usersConfig.save(usersFile);
            groupsConfig.save(groupsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Kon configuratie niet opslaan", e);
        }

        plugin.getLogger().info("Alle permissie data is opgeslagen.");
    }

}
