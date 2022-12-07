package server.listener;

import server.pipe.MessagePipe;

import java.io.File;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.Socket;
import java.util.List;

public class SessionMessageListener implements IMessageListener {

    public String sessionId;
    private final MessagePipe messagePipe = MessagePipe.getInstance();
    private List<String> usernames;

    public SessionMessageListener(String sessionId) {
        this.sessionId = sessionId;
        this.usernames = messagePipe.getUserList(sessionId);
    }

    @Override
    public void onMessageReceived(String sessionID, String message) {
        // Überprüfe, ob die SessionId der Nachricht der Id dieser Session entspricht
        if (!this.sessionId.equals(sessionID)) {
            // Nachricht gehört nicht zu dieser Session
            return;
        }
    }

    @Override
    public void onMessageReceived(String sessionID, String username, String message) {
        // Überprüfe, ob die SessionId der Nachricht der Id dieser Session entspricht
        if (!this.sessionId.equals(sessionID)) {
            // Nachricht gehört nicht zu dieser Session
            return;
        }

    }

    @Override
    public void onFileReceived(String sessionID, String username, File file) {
        // Überprüfe, ob die SessionId der Nachricht der Id dieser Session entspricht
        if (!this.sessionId.equals(sessionID)) {
            // Nachricht gehört nicht zu dieser Session
            return;
        }
    }
}
