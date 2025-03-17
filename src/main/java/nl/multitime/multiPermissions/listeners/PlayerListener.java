package nl.multitime.multiPermissions.listeners;

import nl.multitime.multiPermissions.MultiPermissions;
import nl.multitime.multiPermissions.models.PermissionUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerListener implements Listener {

    private final MultiPermissions plugin;

    public PlayerListener(MultiPermissions plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID playerUUID = event.getUniqueId();
        String playerName = event.getName();

        PermissionUser user = plugin.getPermissionManager().getUser(playerUUID);

        if (user == null) {
            plugin.getPermissionManager().createUser(playerUUID, playerName);
        }
        else if (!user.getName().equals(playerName)) {
            user.setName(playerName);
            plugin.getPermissionManager().saveUsers();
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PermissionUser user = plugin.getPermissionManager().getUser(player.getUniqueId());
        if (user == null) {
            user = plugin.getPermissionManager().createUser(player.getUniqueId(), player.getName());
        }

        if (user.getGroups().isEmpty()) {
            String defaultGroup = plugin.getConfig().getString("default-group", "default");
            if (plugin.getPermissionManager().getGroup(defaultGroup) != null) {
                plugin.getPermissionManager().addUserToGroup(player.getUniqueId(), defaultGroup);
            } else {
                plugin.getPermissionManager().createGroup(defaultGroup);
                plugin.getPermissionManager().addUserToGroup(player.getUniqueId(), defaultGroup);
            }
        }

        plugin.getPermissionManager().setupPermissions(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();


        plugin.getPermissionManager().cleanupPermissions(player);
    }
}
