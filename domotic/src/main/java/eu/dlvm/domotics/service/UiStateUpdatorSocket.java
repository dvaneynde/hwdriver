package eu.dlvm.domotics.service;

import java.util.ArrayList;
import java.util.List;

import eu.dlvm.domotics.base.Domotic;
import eu.dlvm.domotics.base.IStateChangeRegistrar;
import eu.dlvm.domotics.base.IStateChangedListener;
import eu.dlvm.domotics.base.IUiCapableBlock;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.dlvm.domotics.service.uidata.UiInfo;

/**
 * Is created whenever a websocket is created, so whenever a client connects. If
 * multiple clients connnect at same time, multiple are created.<br/>
 */
@WebSocket
public class UiStateUpdatorSocket implements IStateChangedListener {

	private static final Logger LOG = LoggerFactory.getLogger(UiStateUpdatorSocket.class);
	private static int COUNT = 0;
	private ObjectMapper objectMapper;
	private int id;
	private IStateChangeRegistrar registrar;
	private Session savedSession;

	public UiStateUpdatorSocket(IStateChangeRegistrar registrar) {
		this.registrar = registrar;
		this.objectMapper= new ObjectMapper();
		this.id = COUNT++;
		LOG.debug("Created UiStateUpdatorSocket, id=" + id);
	}

	@OnWebSocketConnect
	public void onOpen(Session session) {
		this.savedSession = session;
		registrar.addStateChangedListener(this);
		LOG.debug("Opened websocket session (id=" + id + ") for remote " + this.savedSession.getRemoteAddress());
	}

	@OnWebSocketClose
	public void onClose(int closeCode, String closeReasonPhrase) {
		this.savedSession = null;
		registrar.removeStateChangedListener(this);
		LOG.debug("Closed websocket session (id=" + id + "), reason=" + closeReasonPhrase);
	}

	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public void updateUi() {
		LOG.debug("updateUI called on websocket id=" + id + ", session=" + savedSession);
		if (savedSession == null)
			return;
		try {
			String json = objectMapper.writeValueAsString(createUiInfos());
			savedSession.getRemote().sendString(json);
		} catch (Exception e) {
			LOG.warn("Cannot send state to client. Perhaps race condition, i.e. closed in parallel to update?", e);
		}
	}

	private List<UiInfo> createUiInfos() {
		List<UiInfo> uiInfos = new ArrayList<>();
		for (IUiCapableBlock ui : Domotic.singleton().getUiCapableBlocks()) {
			if (ui.getUiGroup() == null)
				continue;
			uiInfos.add(ui.getUiInfo());
		}
		return uiInfos;
	}
}
