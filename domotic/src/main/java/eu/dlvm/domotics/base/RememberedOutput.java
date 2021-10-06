package eu.dlvm.domotics.base;

import java.util.Arrays;
import java.util.StringTokenizer;

public class RememberedOutput {
	private String blockName;
	private int[] vals;

	public RememberedOutput(String blockName, int vals[]) {
		this.blockName = blockName;
		this.vals = vals;
	}

	public String getBlockName() {
		return blockName;
	}

	public int[] getVals() {
		return vals;
	}

	public String dump() {
		String s = blockName + ":";
		boolean first = true;
		for (int val : vals) {
			if (first)
				first = false;
			else
				s += ',';
			s += Integer.valueOf(val);
		}
		return s;
	}

	public static RememberedOutput parse(String s) {
		if (s==null || s.length()==0)
			return null;
		StringTokenizer st = new StringTokenizer(s, ":,");
		if (!st.hasMoreTokens())
			return null;
		String name = st.nextToken();
		int vals[] = new int[st.countTokens()];
		int i=0;
		while (st.hasMoreTokens())
			vals[i++]=Integer.parseInt(st.nextToken());	
		return new RememberedOutput(name, vals);
	}

	@Override
	public String toString() {
		return "RememberedOutput [blockName=" + blockName + ", vals=" + Arrays.toString(vals) + "]";
	}
}
