package eu.dlvm.domotics.base;

import eu.dlvm.domotics.service.BlockInfo;

public interface IUserInterfaceAPI {

	public BlockInfo getBlockInfo();

	public void update(String action);

}