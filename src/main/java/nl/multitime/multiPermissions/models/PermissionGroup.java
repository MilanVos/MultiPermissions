package nl.multitime.multiPermissions.models;

import java.util.HashSet;
import java.util.Set;

public class PermissionGroup {

    private final String name;
    private final Set<String> permissions;

    public PermissionGroup(String name) {
        this.name = name;
        this.permissions = new HashSet<>();
    }

    public String getName() {
        return name;
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
}
