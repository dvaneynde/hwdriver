package eu.dlvm.domotics.service_impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.IUserInterfaceAPI;
import eu.dlvm.domotics.service.UiInfo;

@WebSocket
public class UiInfoSocket implements IUIUpdator {

	private static final Logger log = LoggerFactory.getLogger(UiInfoSocket.class);
	private Session session;

	@OnWebSocketConnect
	public void onOpen(Session session) {
		this.session = session;
	}

	@OnWebSocketClose
	public void onClose(int closeCode, String closeReasonPhrase) {
		this.session = null;
	}

	@Override
	public void updateUi(Domotic domotic) {
		if (session == null)
			return;
		// collect ui info and send over websocket
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String json = objectMapper.writeValueAsString(createUiInfos());
			session.getRemote().sendString(json);
		} catch (IOException e) {
			// TODO too many messages if goes wrong...
			log.warn("Cannot send state to client.", e);
		}
	}

	private List<UiInfo> createUiInfos() {
		List<UiInfo> uiInfos = new ArrayList<>();
		for (IUserInterfaceAPI ui : Domotic.singleton().getUiCapableBlocks()) {
			if (ui.getUi() == null)
				continue;
			uiInfos.add(ui.getBlockInfo());
		}
		return uiInfos;
	}
}
