package nl.multitime.multiPermissions.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PermissionUser {

    private final UUID uuid;
    private String name;
    private final Set<String> permissions;
    private final Set<String> groups;

    public PermissionUser(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.permissions = new HashSet<>();
        this.groups = new HashSet<>();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getPermissions() {
        return new HashSet<>(permissions);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }

    public boolean removePermission(String permission) {
        return permissions.remove(permission);
    }

    public Set<String> getGroups() {
        return new HashSet<>(groups);
    }

    public boolean inGroup(String group) {
        return groups.contains(group);
    }

    public void addGroup(String group) {
        groups.add(group);
    }

    public boolean removeGroup(String group) {
        return groups.remove(group);
    }
}
