package server.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PermissionManager {
    private static PermissionManager instance;

    private Map<String, Set<Permission>> userPermissions;
    private Map<String, Set<Permission>> sessionPermissions;

    private PermissionManager() {
        this.userPermissions = new HashMap<>();
        this.sessionPermissions = new HashMap<>();
    }

    public enum Permission {
        SEND_MESSAGE,
        SEND_FILE,
        KICK_USER,
        BAN_USER
    }

    public static PermissionManager getInstance() {
        if (instance == null) instance = new PermissionManager();
        return instance;
    }

    public void grantPermission(String sessionID, String username, Permission permission) {
        Set<Permission> userPermList = this.userPermissions.computeIfAbsent(username, k -> new HashSet<>());
        userPermList.add(permission);
        Set<Permission> sessionPermList = this.sessionPermissions.computeIfAbsent(sessionID, k -> new HashSet<>());
        sessionPermList.add(permission);
    }

    public void revokePermission(String sessionID, String username, Permission permission) {
        Set<Permission> userPermList = this.userPermissions.get(username);
        if (userPermList != null) userPermList.remove(permission);
        Set<Permission> sessionPermList = this.sessionPermissions.get(sessionID);
        if (sessionPermList != null) sessionPermList.remove(permission);
    }

    public boolean hasPermission(String sessionID, String username, Permission permission) {
        Set<Permission> userPermList = this.userPermissions.get(username);
        if (userPermList != null) return userPermList.contains(permission);
        return false;
    }
}
