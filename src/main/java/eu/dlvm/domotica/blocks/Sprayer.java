package eu.dlvm.domotica.blocks;

import java.util.ArrayList;
import java.util.List;

public class Sprayer extends Block implements IMsg2Op {

	private List<IMsg2Op> targets;
	
	public Sprayer(String name, String description) {
		super(name, description);
		targets = new ArrayList<>();
	}

	@Override
	public void execute(String op) {
		for (IMsg2Op target:targets)
			target.execute(op);
	}

	public List<IMsg2Op> getTargets() {
		return targets;
	}

	public void setTargets(List<IMsg2Op> targets) {
		this.targets = targets;
	}

}
