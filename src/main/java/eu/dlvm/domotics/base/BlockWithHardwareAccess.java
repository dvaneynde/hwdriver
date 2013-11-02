package eu.dlvm.domotics.base;

import eu.dlvm.iohardware.IHardwareIO;

public abstract class BlockWithHardwareAccess extends Block implements IHardwareAccess {

	protected IHardwareAccess ctx;

	public BlockWithHardwareAccess(String name, String description, IHardwareAccess ctx) {
		super(name, description);
		this.ctx = ctx;
	}

	@Override
	public IHardwareIO getHw() {
		return ctx.getHw();
	}

	@Override
	public void addSensor(Sensor s) {
		ctx.addSensor(s);
	}

	@Override
	public void addActuator(Actuator a) {
		ctx.addActuator(a);
	}

}
