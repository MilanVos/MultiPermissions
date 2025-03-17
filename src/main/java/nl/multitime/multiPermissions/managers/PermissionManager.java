package nl.multitime.multiPermissions.managers;

import nl.multitime.multiPermissions.MultiPermissions;
import nl.multitime.multiPermissions.models.PermissionGroup;
import nl.multitime.multiPermissions.models.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

public class PermissionManager {

    private final MultiPermissions plugin;
    private final Map<UUID, PermissionUser> users;
    private final Map<String, PermissionGroup> groups;
    private Map<UUID, PermissionAttachment> permissionAttachment;

    public PermissionManager(MultiPermissions plugin) {
        this.plugin = plugin;
        this.users = new HashMap<>();
        this.groups = new HashMap<>();
        this.permissionAttachment = new HashMap<>();

        loadDefaultGroups();

        loadUsers();
    }

    private void loadDefaultGroups() {
        if (!groups.containsKey("default")) {
            PermissionGroup defaultGroup = new PermissionGroup("default");
            defaultGroup.addPermission("multipermissions.user");
            groups.put("default", defaultGroup);
        }

        if (!groups.containsKey("admin")) {
            PermissionGroup adminGroup = new PermissionGroup("admin");
            adminGroup.addPermission("multipermissions.admin");
            adminGroup.addPermission("*");
            groups.put("admin", adminGroup);
        }

        plugin.getStorageManager().saveGroups(groups);
    }

    private void loadUsers() {
        Map<UUID, PermissionUser> loadedUsers = plugin.getStorageManager().loadUsers();
        if (loadedUsers != null) {
            users.putAll(loadedUsers);
        }
    }

    public void setupPermissions(Player player) {
        UUID uuid = player.getUniqueId();

        if (permissionAttachment.containsKey(uuid)) {
            player.removeAttachment(permissionAttachment.get(uuid));
            permissionAttachment.remove(uuid);
        }

        PermissionAttachment attachment = player.addAttachment(plugin);
        permissionAttachment.put(uuid, attachment);

        PermissionUser user = users.getOrDefault(uuid, new PermissionUser(uuid, player.getName()));
        if (!users.containsKey(uuid)) {
            user.addGroup("default");
            users.put(uuid, user);
        }

        for (String groupName : user.getGroups()) {
            PermissionGroup group = groups.get(groupName);
            if (group != null) {
                for (String permission : group.getPermissions()) {
                    attachment.setPermission(permission, true);
                }
            }
        }

        for (String permission : user.getPermissions()) {
            attachment.setPermission(permission, true);
        }

        player.recalculatePermissions();
    }

    public boolean addUserPermission(UUID uuid, String permission) {
        PermissionUser user = users.get(uuid);
        if (user != null) {
            user.addPermission(permission);

            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                setupPermissions(player);
            }

            plugin.getStorageManager().saveUsers(users);
            return true;
        }
        return false;
    }

    public boolean removeUserPermission(UUID uuid, String permission) {
        PermissionUser user = users.get(uuid);
        if (user != null) {
            boolean result = user.removePermission(permission);

            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                setupPermissions(player);
            }

            plugin.getStorageManager().saveUsers(users);
            return result;
        }
        return false;
    }

    public boolean addUserToGroup(UUID uuid, String group) {
        if (!groups.containsKey(group)) {
            return false;
        }

        PermissionUser user = users.get(uuid);
        if (user != null) {
            user.addGroup(group);

            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                setupPermissions(player);
            }

            plugin.getStorageManager().saveUsers(users);
            return true;
        }
        return false;
    }

    public boolean removeUserFromGroup(UUID uuid, String group) {
        PermissionUser user = users.get(uuid);
        if (user != null) {
            boolean result = user.removeGroup(group);

            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                setupPermissions(player);
            }

            plugin.getStorageManager().saveUsers(users);
            return result;
        }
        return false;
    }

    public PermissionUser getUser(UUID uuid) {
        return users.get(uuid);
    }

    public Collection<PermissionUser> getAllUsers() {
        return users.values();
    }

    public PermissionGroup getGroup(String name) {
        return groups.get(name);
    }

    public Collection<PermissionGroup> getAllGroups() {
        return groups.values();
    }

    public boolean createGroup(String name) {
        if (groups.containsKey(name)) {
            return false;
        }

        groups.put(name, new PermissionGroup(name));
        plugin.getStorageManager().saveGroups(groups);
        return true;
    }

    public boolean deleteGroup(String name) {
        if (!groups.containsKey(name) || name.equals("default") || name.equals("admin")) {
            return false;
        }

        groups.remove(name);

        for (PermissionUser user : users.values()) {
            user.removeGroup(name);
        }

        plugin.getStorageManager().saveGroups(groups);
        plugin.getStorageManager().saveUsers(users);
        return true;
    }

    public void saveUsers() {
        plugin.getStorageManager().saveUsers(users);
    }


    public PermissionUser createUser(UUID playerUUID, String playerName) {
        PermissionUser user = new PermissionUser(playerUUID, playerName);


        users.put(playerUUID, user);
        saveUsers();

        return user;
    }


    public void cleanupPermissions(Player player) {
        if (permissionAttachment.containsKey(player.getUniqueId())) {
            if (permissionAttachment.containsKey(player.getUniqueId())) {
                PermissionAttachment attachment = permissionAttachment.get(player.getUniqueId());
                if (attachment != null) {
                    try {
                        player.removeAttachment(attachment);
                    } catch (IllegalArgumentException e) {
                    }
                }
                permissionAttachment.remove(player.getUniqueId());
        }
        }
    }
}