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
        Set<Permission> userPermList = userPermissions.get(username);
        if (userPermList == null) {
            userPermList = new HashSet<>();
            this.userPermissions.put(username, userPermList);
        }
        userPermList.add(permission);
        Set<Permission> sessionPermList = sessionPermissions.get(sessionID);
        if (sessionPermList == null) {
            sessionPermList = new HashSet<>();
            this.sessionPermissions.put(sessionID, sessionPermList);
        }
        sessionPermList.add(permission);
    }

    public void revokePermission(String sessionID, String username, Permission permission) {
        Set<Permission> userPermList = userPermissions.get(username);
        if (userPermList != null) userPermList.remove(permission);
        Set<Permission> sessionPermList = sessionPermissions.get(sessionID);
        if (sessionPermList != null) sessionPermList.remove(permission);
    }
}
