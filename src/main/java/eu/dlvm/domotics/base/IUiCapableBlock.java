package eu.dlvm.domotics.base;

import eu.dlvm.domotics.service.uidata.UiInfo;

/**
 * Blocks that can be shown and updated via GUI.
 * 
 * @author dirk
 */
public interface IUiCapableBlock {

	/**
	 * Status and description information for displaying on GUI. The
	 * identifying name of this block is in {@link UiInfoOnOff#getName()}.
	 */
	public UiInfo getUiInfo();

	/**
	 * For UI only, where to position on screen. E.g. "Nutsruimtes:6" means
	 * group Nutsruimtes, and 6th index therein.
	 */
	public String getUiGroup();

	/**
	 * Update a Block through UI.
	 * 
	 * @param action
	 */
	public void update(String action);
}