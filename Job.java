// OS project
// Simulation of an operating system
// Aliaksandra Havia
// **************************************************************//

public class Job {
	private int number; // p[1] job number
	private int priority; // p[2] 1 is highest priority, 5 is lowest priority
	private int size; // p[3] size...
	private int maxCpuTime; // p[4] max time for alloted for cpu usage
	private int cpuEnterTime; // p[5] time it enters the cpu
	private int cpuTimeUsed; // keep track of CPU time already used by the job
	private int startAddress; // address of job
	private boolean latched; // cuurrently doing i/o
	private boolean blocked; // block until all I/O requests are finished
	private boolean incore; // When I/O is being done for a job, that job must be in core.
	private boolean killBit; // indicate that job should be terminated, once it's current I/O request is finished
	private int ioPending; // job has an I/O reuest pending

	public Job() {

	}

	public Job(int number, int priority, int size, int maxCpuTime, int cpuEnterTime) {
		this.number = number;
		this.priority = priority;
		this.size = size;
		this.maxCpuTime = maxCpuTime;
		this.cpuEnterTime = cpuEnterTime;
	}

	public int getJobNumber() {
		return number;
	}

	public void setJobNumber(int number) {
		this.number = number;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getMaxCpuTime() {
		return maxCpuTime;
	}

	public void setMaxCpuTime(int maxCpuTime) {
		this.maxCpuTime = maxCpuTime;
	}

	public int getCpuEnterTime() {
		return cpuEnterTime;
	}

	public void setCpuEnterTime(int cpuEnterTime) {
		this.cpuEnterTime = cpuEnterTime;
	}

	public int getStartAddress() {
		return startAddress;
	}

	public void setStartAddress(int startAddress) {
		this.startAddress = startAddress;
	}

	public void setCpuTimeUsed(int cpuTimeUsed) {
		this.cpuTimeUsed = cpuTimeUsed;
	}

	public int getCpuTimeUsed() {
		return cpuTimeUsed;
	}

	// Latched => currently doing I/O
	public void setLatched(boolean latched) {
		this.latched = latched; // set value
	}

	public boolean getLatched() {
		return latched; // give the value stored
	}

	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
	}

	public boolean getBlocked() {
		return blocked;
	}
	// 
	public boolean getIncore() {
		return incore;
	}

	public void setIncore(boolean incore) {
		this.incore = incore;
	}
	// OS should set a killBit bit to
	// indicate that this job should be terminated 
	// as soon as its current I/O operation is completed
	public void setKillBit(boolean killBit) {
		this.killBit = killBit;
	}

	public boolean getKillBit() {
		return killBit;
	}

	public int getIOPending() {
		return ioPending;
	}

	public void setIOPending(int ioPending) {
		this.ioPending = ioPending;
	}

}
