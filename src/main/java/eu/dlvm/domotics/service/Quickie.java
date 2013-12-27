package eu.dlvm.domotics.service;

import java.util.ArrayList;
import java.util.List;

public class Quickie {

	private String name;

	public class KeyVal {
		public String key, val;
	}

	public List<KeyVal> actions = new ArrayList<>();

	public Quickie(String name) {
		this.name = name;
	}

	public Quickie add(String key, String val) {
		KeyVal kv = new KeyVal();
		kv.key = key;
		kv.val = val;
		actions.add(kv);
		return this;
	}
	
	public String getName() {
		return name;
	}
}
