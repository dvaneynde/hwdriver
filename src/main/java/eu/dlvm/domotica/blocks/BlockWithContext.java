package eu.dlvm.domotica.blocks;

import eu.dlvm.iohardware.IHardwareIO;


public abstract class BlockWithContext extends Block {

	protected IDomoContext ctx;

	public BlockWithContext(String name, String description, IDomoContext ctx) {
		super(name, description);
		this.ctx = ctx;
	}

	public IHardwareIO hw() {
		return ctx.getHw();
	}
	
	public IDomoContext ctx() {
		return ctx;
	}

}
