package server.monitor;

public interface ActivityMonitor {
     void onUserJoin(String sessionID, String username);
     void onUserLeave(String sessionID, String username);
     void onSessionInactive(String sessionID);
}
