package eu.dlvm.domotics.utils;

public class AverageLong {

	long sum;
	int number, nrSoFar;
	
	public AverageLong(int number) {
		this.number = number;
	}
	
	public void add(long sample) {
		if (nrSoFar >= number)
			throw new IllegalArgumentException("Too many adds.");
		sum += sample;
		nrSoFar++;
	}
	
	public double avgAndClear() {
		if (!enoughSamples())
			throw new IllegalArgumentException("Not enough samples.");
		double avgDouble = ((double)sum / (double)number);
		sum = 0;
		nrSoFar = 0;
		return avgDouble;
	}
	
	public boolean enoughSamples() {
		return nrSoFar == number;
	}
}
