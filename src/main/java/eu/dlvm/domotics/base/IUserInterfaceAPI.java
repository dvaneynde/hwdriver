package eu.dlvm.domotics.base;

import eu.dlvm.domotics.service.UiInfo;

// TODO rename to UiCapable[Block]
public interface IUserInterfaceAPI {

	public UiInfo getBlockInfo();
	public String getUi();

	public void update(String action);
}