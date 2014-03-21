package eu.dlvm.domotics.base;

import eu.dlvm.domotics.service.BlockInfo;

// TODO rename to UiCapable[Block]
public interface IUserInterfaceAPI {

	public BlockInfo getBlockInfo();

	public void update(String action);

	// TODO from Block
	public String getName();
	public String getUi();
}