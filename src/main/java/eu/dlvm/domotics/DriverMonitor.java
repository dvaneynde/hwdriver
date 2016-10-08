package eu.dlvm.domotics;

public class DriverMonitor {

	private ProcessWatch prWatch;
	private ProcessReader prStdout;
	private ProcessReader prStderr;

	public DriverMonitor(Process process, String processName) {
		initialize(process, processName);
	}

	private void initialize(Process process, String processName) {
		prWatch = new ProcessWatch(process, "Driver Process Watch", processName);
		prStdout = new ProcessReader(process.getInputStream(), "Driver STDOUT Reader");
		prStderr = new ProcessReader(process.getErrorStream(), "Driver STDERR Reader");
		prWatch.startWatching();
		prStdout.startReading();
		prStderr.startReading();
	}

	public void terminate() {
		prWatch.terminate();
		prStdout.terminate();
		prStderr.terminate();
	}

	public boolean driverNotReady() {
		return prStdout.driverNotReady();
	}

	public boolean everythingSeemsWorking() {
		return (prWatch.isRunning() && prStdout.isRunning() && prStderr.isRunning());
	}

	public ProcessWatch getProcessWatch() {
		return prWatch;
	}
	
	public String report() {
		String s = "";
		s += "\n\tprocess watch: " + prWatch.toString();
		s += "\n\tprocess stdout: " + prStdout.toString();
		s += "\n\tprocess stderr: " + prStderr.toString();
		return s;
	}
}
