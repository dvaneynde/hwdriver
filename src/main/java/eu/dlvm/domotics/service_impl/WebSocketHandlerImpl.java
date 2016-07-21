package eu.dlvm.domotics.service_impl;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * @author Dirk Vaneynde
 */
@ServerEndpoint("/myapp")
public class WebSocketHandlerImpl {

    @OnMessage
    public void message(String message, Session session) {
        for (Session s : session.getOpenSessions()) {
            s.getAsyncRemote().sendText(message);
        }
    }

}
