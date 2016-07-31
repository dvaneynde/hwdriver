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
import eu.dlvm.domotics.base.IDomoticContext;
import eu.dlvm.domotics.base.IUserInterfaceAPI;
import eu.dlvm.domotics.service.UiInfo;

/**
 * Is created whenever a websocket is created, so whenever a client connects. If
 * multiple clients connnect at same time, multiple are created.<br/>
 */
@WebSocket
public class UiUpdatorSocket implements IUIUpdator {

	private static final Logger LOG = LoggerFactory.getLogger(UiUpdatorSocket.class);
	private static int COUNT = 0;
	private ObjectMapper objectMapper;
	private int id;
	private IDomoticContext context;
	private Session savedSession;

	public UiUpdatorSocket(IDomoticContext context) {
		this.context = context;
		this.objectMapper= new ObjectMapper();
		this.id = COUNT++;
		LOG.info("Created UiUpdatorSocket, id=" + id);
	}

	@Override
	public int getId() {
		return id;
	}
	
	@OnWebSocketConnect
	public void onOpen(Session session) {
		this.savedSession = session;
		context.addUiUpdator(this);
		LOG.info("Opened websocket session (id=" + id + ") for remote " + this.savedSession.getRemoteAddress());
	}

	@OnWebSocketClose
	public void onClose(int closeCode, String closeReasonPhrase) {
		this.savedSession = null;
		context.removeUiUpdator(this);
		LOG.info("Closed websocket session (id=" + id + "), reason=" + closeReasonPhrase);
	}

	@Override
	public void updateUi(Domotic domotic) {
		LOG.debug("updateUI called on websocket id=" + id + ", session=" + savedSession);
		if (savedSession == null)
			return;
		try {
			String json = objectMapper.writeValueAsString(createUiInfos());
			savedSession.getRemote().sendString(json);
		} catch (IOException e) {
			// TODO too many messages if goes wrong...
			LOG.warn("Cannot send state to client.", e);
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
