package de.conet.srtc.playground.wsclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.conet.srtc.playground.wsclient.message.WebSocketMessage;
import de.conet.srtc.playground.wsclient.message.WebSocketOfferMessage;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.jsdp.Description;
import net.sourceforge.jsdp.SDPException;
import net.sourceforge.jsdp.SDPFactory;
import net.sourceforge.jsdp.SessionDescription;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.URI;
import java.nio.ByteBuffer;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

@Slf4j
@ClientEndpoint
public class WebSocketClient {
    Session session = null;
    final ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketClient(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("opening websocket");
        this.session = session;
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("closing websocket");
        this.session = null;
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            WebSocketMessage socketMessage = objectMapper.readValue(message, WebSocketMessage.class);
            log.info("Received message: {}", socketMessage);
            if (socketMessage instanceof WebSocketOfferMessage) {
                // convert to SessionDescription
                String sdp = ((WebSocketOfferMessage) socketMessage).getData().getSdp();
                SessionDescription sessionDescription = SDPFactory.createSessionDescription();
//                log.info("SessionDescription: {}", sdp);
            }
        } catch (JsonProcessingException | SDPException e) {
            throw new RuntimeException(e);
        }
    }

    @OnMessage
    public void onMessage(ByteBuffer bytes) {
        System.out.println("Handle byte buffer");
    }

    public void sendMessage(String message) {
        this.session.getAsyncRemote().sendText(message);
    }
}
