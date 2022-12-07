package server.monitor;

public interface IActivityMonitor {
     void onUserJoin(String sessionID, String username);
     void onUserLeave(String sessionID, String username);
     void onSessionInactive(String sessionID);
}
