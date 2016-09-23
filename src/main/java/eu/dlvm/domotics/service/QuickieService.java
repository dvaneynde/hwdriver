package eu.dlvm.domotics.service;

import java.util.ArrayList;
import java.util.List;

public class QuickieService {

	private List<Quickie> qs;

	public QuickieService() {
		qs = new ArrayList<>();
		Quickie q;
		q = new Quickie("tv").add("LichtVeranda", "off").add("LichtCircanteRondom", "" + 40).add("LichtZithoek", "" + 50).add("LichtKeuken", "off").add("LichtCircante", "off");
		qs.add(q);
		q = new Quickie("eten").add("LichtVeranda", "100").add("LichtCircanteRondom", "80").add("LichtZithoek", "40").add("LichtKeuken", "on").add("LichtCircante", "off");
		qs.add(q);
		q = new Quickie("fel").add("LichtVeranda", "100").add("LichtCircanteRondom", "100").add("LichtZithoek", "100").add("LichtKeuken", "on").add("LichtCircante", "on");
		qs.add(q);
		q = new Quickie("eco").add("LichtBadk0", "off").add("LichtInkom", "off").add("LichtGaragePoort", "off").add("LichtGarageTuin", "off").add("LichtBureau", "off")
				.add("LichtGangBoven", "off");
		qs.add(q);
	}

	public Quickie find(String name) {
		for (Quickie q : qs)
			if (q.getName().equalsIgnoreCase(name))
				return q;
		return null;
	}

	public String listQuickyNamesNewlineSeparated() {
		StringBuffer sb = new StringBuffer();
		for (Quickie q:qs) 
			sb.append(q.getName()).append('\n');
		return sb.toString();
	}

	@Override
	public String toString() {
		return "QuickieService [qs=" + qs + "]";
	}
}
