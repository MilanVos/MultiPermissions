package nl.multitime.multiPermissions.commands;

import nl.multitime.multiPermissions.MultiPermissions;
import nl.multitime.multiPermissions.models.PermissionGroup;
import nl.multitime.multiPermissions.models.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PermissionsCommand implements CommandExecutor, TabCompleter {

    private final MultiPermissions plugin;

    public PermissionsCommand(MultiPermissions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sendHelpMessage(sender);
                break;

            case "user":
                handleUserCommand(sender, args);
                break;

            case "group":
                handleGroupCommand(sender, args);
                break;

            case "list":
                handleListCommand(sender, args);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Onbekend commando. Gebruik /perms help voor hulp.");
                break;
        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== MultiPermissions Help ===");
        sender.sendMessage(ChatColor.YELLOW + "/perms help " + ChatColor.WHITE + "- Toont dit help menu");
        sender.sendMessage(ChatColor.YELLOW + "/perms user <speler> add <permissie> " + ChatColor.WHITE + "- Voegt een permissie toe aan een speler");
        sender.sendMessage(ChatColor.YELLOW + "/perms user <speler> remove <permissie> " + ChatColor.WHITE + "- Verwijdert een permissie van een speler");
        sender.sendMessage(ChatColor.YELLOW + "/perms user <speler> group add <groep> " + ChatColor.WHITE + "- Voegt een speler toe aan een groep");
        sender.sendMessage(ChatColor.YELLOW + "/perms user <speler> group remove <groep> " + ChatColor.WHITE + "- Verwijdert een speler uit een groep");
        sender.sendMessage(ChatColor.YELLOW + "/perms user <speler> info " + ChatColor.WHITE + "- Toont informatie over een speler");
        sender.sendMessage(ChatColor.YELLOW + "/perms group create <groep> " + ChatColor.WHITE + "- Maakt een nieuwe groep aan");
        sender.sendMessage(ChatColor.YELLOW + "/perms group delete <groep> " + ChatColor.WHITE + "- Verwijdert een groep");
        sender.sendMessage(ChatColor.YELLOW + "/perms group <groep> add <permissie> " + ChatColor.WHITE + "- Voegt een permissie toe aan een groep");
        sender.sendMessage(ChatColor.YELLOW + "/perms group <groep> remove <permissie> " + ChatColor.WHITE + "- Verwijdert een permissie van een groep");
        sender.sendMessage(ChatColor.YELLOW + "/perms group <groep> info " + ChatColor.WHITE + "- Toont informatie over een groep");
        sender.sendMessage(ChatColor.YELLOW + "/perms list users " + ChatColor.WHITE + "- Toont alle gebruikers");
        sender.sendMessage(ChatColor.YELLOW + "/perms list groups " + ChatColor.WHITE + "- Toont alle groepen");
    }

    private void handleUserCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Gebruik: /perms user <speler> <add|remove|group|info>");
            return;
        }

        String playerName = args[1];
        Player targetPlayer = Bukkit.getPlayerExact(playerName);
        UUID targetUUID = null;

        if (targetPlayer != null) {
            targetUUID = targetPlayer.getUniqueId();
        } else {
            for (PermissionUser user : plugin.getPermissionManager().getAllUsers()) {
                if (user.getName().equalsIgnoreCase(playerName)) {
                    targetUUID = user.getUuid();
                    break;
                }
            }

            if (targetUUID == null) {
                sender.sendMessage(ChatColor.RED + "Speler niet gevonden: " + playerName);
                return;
            }
        }

        String action = args[2].toLowerCase();

        switch (action) {
            case "add":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Gebruik: /perms user <speler> add <permissie>");
                    return;
                }

                String permToAdd = args[3];
                if (plugin.getPermissionManager().addUserPermission(targetUUID, permToAdd)) {
                    sender.sendMessage(ChatColor.GREEN + "Permissie '" + permToAdd + "' toegevoegd aan " + playerName);
                } else {
                    sender.sendMessage(ChatColor.RED + "Kon permissie niet toevoegen aan " + playerName);
                }
                break;

            case "remove":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Gebruik: /perms user <speler> remove <permissie>");
                    return;
                }

                String permToRemove = args[3];
                if (plugin.getPermissionManager().removeUserPermission(targetUUID, permToRemove)) {
                    sender.sendMessage(ChatColor.GREEN + "Permissie '" + permToRemove + "' verwijderd van " + playerName);
                } else {
                    sender.sendMessage(ChatColor.RED + "Kon permissie niet verwijderen van " + playerName);
                }
                break;

            case "group":
                if (args.length < 5) {
                    sender.sendMessage(ChatColor.RED + "Gebruik: /perms user <speler> group <add|remove> <groep>");
                    return;
                }

                String groupAction = args[3].toLowerCase();
                String groupName = args[4];

                if (groupAction.equals("add")) {
                    if (plugin.getPermissionManager().addUserToGroup(targetUUID, groupName)) {
                        sender.sendMessage(ChatColor.GREEN + playerName + " toegevoegd aan groep '" + groupName + "'");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Kon " + playerName + " niet toevoegen aan groep '" + groupName + "'");
                    }
                } else if (groupAction.equals("remove")) {
                    if (plugin.getPermissionManager().removeUserFromGroup(targetUUID, groupName)) {
                        sender.sendMessage(ChatColor.GREEN + playerName + " verwijderd uit groep '" + groupName + "'");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Kon " + playerName + " niet verwijderen uit groep '" + groupName + "'");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Gebruik: /perms user <speler> group <add|remove> <groep>");
                }
                break;

            case "info":
                PermissionUser user = plugin.getPermissionManager().getUser(targetUUID);
                if (user != null) {
                    sender.sendMessage(ChatColor.GOLD + "=== Informatie over " + user.getName() + " ===");
                    sender.sendMessage(ChatColor.YELLOW + "UUID: " + ChatColor.WHITE + user.getUuid());

                    sender.sendMessage(ChatColor.YELLOW + "Groepen:");
                    if (user.getGroups().isEmpty()) {
                        sender.sendMessage(ChatColor.WHITE + "  Geen groepen");
                    } else {
                        for (String group : user.getGroups()) {
                            sender.sendMessage(ChatColor.WHITE + "  - " + group);
                        }
                    }

                    sender.sendMessage(ChatColor.YELLOW + "Permissies:");
                    if (user.getPermissions().isEmpty()) {
                        sender.sendMessage(ChatColor.WHITE + "  Geen individuele permissies");
                    } else {
                        for (String perm : user.getPermissions()) {
                            sender.sendMessage(ChatColor.WHITE + "  - " + perm);
                        }
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Kon gebruiker niet vinden: " + playerName);
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Gebruik: /perms user <speler> <add|remove|group|info>");
                break;
        }
    }

    private void handleGroupCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Gebruik: /perms group <create|delete|groepnaam>");
            return;
        }

        String action = args[1].toLowerCase();

        if (action.equals("create")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Gebruik: /perms group create <groepnaam>");
                return;
            }

            String groupName = args[2];
            if (plugin.getPermissionManager().createGroup(groupName)) {
                sender.sendMessage(ChatColor.GREEN + "Groep '" + groupName + "' aangemaakt");
            } else {
                sender.sendMessage(ChatColor.RED + "Groep '" + groupName + "' bestaat al");
            }
            return;
        }

        if (action.equals("delete")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Gebruik: /perms group delete <groepnaam>");
                return;
            }

            String groupName = args[2];
            if (plugin.getPermissionManager().deleteGroup(groupName)) {
                sender.sendMessage(ChatColor.GREEN + "Groep '" + groupName + "' verwijderd");
            } else {
                sender.sendMessage(ChatColor.RED + "Kon groep '" + groupName + "' niet verwijderen");
            }
            return;
        }

        String groupName = args[1];
        PermissionGroup group = plugin.getPermissionManager().getGroup(groupName);

        if (group == null) {
            sender.sendMessage(ChatColor.RED + "Groep niet gevonden: " + groupName);
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.GOLD + "=== Informatie over groep " + group.getName() + " ===");
            sender.sendMessage(ChatColor.YELLOW + "Permissies:");
            if (group.getPermissions().isEmpty()) {
                sender.sendMessage(ChatColor.WHITE + "  Geen permissies");
            } else {
                for (String perm : group.getPermissions()) {
                    sender.sendMessage(ChatColor.WHITE + "  - " + perm);
                }
            }
            return;
        }

        String groupAction = args[2].toLowerCase();

        switch (groupAction) {
            case "add":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Gebruik: /perms group <groep> add <permissie>");
                    return;
                }

                String permToAdd = args[3];
                group.addPermission(permToAdd);
                plugin.getPermissionManager().getGroup(groupName).addPermission(permToAdd);
                plugin.getStorageManager().saveGroups(plugin.getPermissionManager().getAllGroups().stream()
                        .collect(Collectors.toMap(PermissionGroup::getName, g -> g)));

                for (Player player : Bukkit.getOnlinePlayers()) {
                    PermissionUser user = plugin.getPermissionManager().getUser(player.getUniqueId());
                    if (user != null && user.inGroup(groupName)) {
                        plugin.getPermissionManager().setupPermissions(player);
                    }
                }

                sender.sendMessage(ChatColor.GREEN + "Permissie '" + permToAdd + "' toegevoegd aan groep '" + groupName + "'");
                break;

            case "remove":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Gebruik: /perms group <groep> remove <permissie>");
                    return;
                }

                String permToRemove = args[3];
                if (group.removePermission(permToRemove)) {
                    plugin.getStorageManager().saveGroups(plugin.getPermissionManager().getAllGroups().stream()
                            .collect(Collectors.toMap(PermissionGroup::getName, g -> g)));

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        PermissionUser user = plugin.getPermissionManager().getUser(player.getUniqueId());
                        if (user != null && user.inGroup(groupName)) {
                            plugin.getPermissionManager().setupPermissions(player);
                        }
                    }

                    sender.sendMessage(ChatColor.GREEN + "Permissie '" + permToRemove + "' verwijderd van groep '" + groupName + "'");
                } else {
                    sender.sendMessage(ChatColor.RED + "Permissie '" + permToRemove + "' niet gevonden in groep '" + groupName + "'");
                }
                break;

            case "info":
                sender.sendMessage(ChatColor.GOLD + "=== Informatie over groep " + group.getName() + " ===");
                sender.sendMessage(ChatColor.YELLOW + "Permissies:");
                if (group.getPermissions().isEmpty()) {
                    sender.sendMessage(ChatColor.WHITE + "  Geen permissies");
                } else {
                    for (String perm : group.getPermissions()) {
                        sender.sendMessage(ChatColor.WHITE + "  - " + perm);
                    }
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Gebruik: /perms group <groep> <add|remove|info>");
                break;
        }
    }

    private void handleListCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Gebruik: /perms list <users|groups>");
            return;
        }

        String listType = args[1].toLowerCase();

        if (listType.equals("users")) {
            sender.sendMessage(ChatColor.GOLD + "=== Alle gebruikers ===");

            if (plugin.getPermissionManager().getAllUsers().isEmpty()) {
                sender.sendMessage(ChatColor.WHITE + "Geen gebruikers gevonden");
            } else {
                for (PermissionUser user : plugin.getPermissionManager().getAllUsers()) {
                    sender.sendMessage(ChatColor.YELLOW + user.getName() + ChatColor.WHITE + " (" + user.getUuid() + ")");
                }
            }
        } else if (listType.equals("groups")) {
            sender.sendMessage(ChatColor.GOLD + "=== Alle groepen ===");

            if (plugin.getPermissionManager().getAllGroups().isEmpty()) {
                sender.sendMessage(ChatColor.WHITE + "Geen groepen gevonden");
            } else {
                for (PermissionGroup group : plugin.getPermissionManager().getAllGroups()) {
                    sender.sendMessage(ChatColor.YELLOW + group.getName() + ChatColor.WHITE + " (" + group.getPermissions().size() + " permissies)");
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Gebruik: /perms list <users|groups>");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String[] options = {"help", "user", "group", "list"};
            return filterCompletions(options, args[0]);
        } else if (args.length >= 2) {
            String firstArg = args[0].toLowerCase();

            if (firstArg.equals("user")) {
                if (args.length == 2) {
                    return null;
                } else if (args.length == 3) {
                    String[] options = {"add", "remove", "group", "info"};
                    return filterCompletions(options, args[2]);
                } else if (args.length == 4 && args[2].equalsIgnoreCase("group")) {
                    String[] options = {"add", "remove"};
                    return filterCompletions(options, args[3]);
                } else if (args.length == 5 && args[2].equalsIgnoreCase("group") && args[3].equalsIgnoreCase("add")) {
                    return filterCompletions(
                            plugin.getPermissionManager().getAllGroups().stream()
                                    .map(PermissionGroup::getName)
                                    .toArray(String[]::new),
                            args[4]);
                } else if (args.length == 5 && args[2].equalsIgnoreCase("group") && args[3].equalsIgnoreCase("remove")) {
                    Player targetPlayer = Bukkit.getPlayerExact(args[1]);
                    if (targetPlayer != null) {
                        PermissionUser user = plugin.getPermissionManager().getUser(targetPlayer.getUniqueId());
                        if (user != null) {
                            return filterCompletions(
                                    user.getGroups().toArray(new String[0]),
                                    args[4]);
                        }
                    }
                }
            } else if (firstArg.equals("group")) {
                if (args.length == 2) {
                    List<String> options = new ArrayList<>(Arrays.asList("create", "delete"));
                    options.addAll(plugin.getPermissionManager().getAllGroups().stream()
                            .map(PermissionGroup::getName)
                            .collect(Collectors.toList()));
                    return filterCompletions(options.toArray(new String[0]), args[1]);
                } else if (args.length == 3 && !args[1].equalsIgnoreCase("create") && !args[1].equalsIgnoreCase("delete")) {
                    String[] options = {"add", "remove", "info"};
                    return filterCompletions(options, args[2]);
                } else if (args.length == 4 && args[2].equalsIgnoreCase("remove")) {
                    PermissionGroup group = plugin.getPermissionManager().getGroup(args[1]);
                    if (group != null) {
                        return filterCompletions(
                                group.getPermissions().toArray(new String[0]),
                                args[3]);
                    }
                }
            } else if (firstArg.equals("list")) {
                if (args.length == 2) {
                    String[] options = {"users", "groups"};
                    return filterCompletions(options, args[1]);
                }
            }
        }

        return completions;
    }

    private List<String> filterCompletions(String[] options, String input) {
        return Arrays.stream(options)
                .filter(option -> option.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
}
