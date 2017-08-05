// OS project
// Simulation of an operating system
// Aliaksandra Havia
// **************************************************************//
import java.util.*;

public class os {
	// initialize job table as a linked list
	private static LinkedList<Job> jobTable = new LinkedList<Job>();
	// initialize drum queue as a linked 
	// remove Drum Queue
	private static LinkedList<Job> drumQueue = new LinkedList<Job>();
	// initialize I/O queue as a linked list
	private static LinkedList<Job> ioQueue = new LinkedList<Job>();
	// initialize ready queue as a linked list
	private static LinkedList<Job> readyQueue = new LinkedList<Job>();
	// initialize Memory Manager
	private static MemoryManager memoryManager = new MemoryManager();
	// running job
	private static Job runningJob = new Job();
	// all parmaeters of the job has to go on Drum or I/O Queues
	private static Job jobInDrumQ = new Job(), jobInIoQ = new Job();
	static boolean ioRunning; // I/O running
	static boolean cpuRunning; // CPU running
	static boolean cpuBusy; // CPU is busy
	static boolean swapping; // swapping
	private static final int TIMEMANAGER = 6; // Initialize time slice
	private static int countBlocked; // 
	private static int cpuStartTime = 0; // CPU start time 

	// sos starts by calling startup function
	public static void startup(){
		// Allows initialization of static system variables 
		// declared above.
 		// Called once at start of the simulation.
 		// sos.ontrace();
	}
	// ***********5 INTERRUPT HANDLERS *****************
	// p is an array of 6 elemnts 
	// a parameter tells what kind of service
	// info about the job is in p array 
	public static void Crint(int[] a, int[] p){
		if (cpuRunning) {
			// update the CPU time left for the job that was interrupted
			bookKeeper(p[5]); // p [5] = current time
			// put back that job to ready queue
			readyQueue.add(runningJob);
		}
		// save a new job
		runningJob = new Job();
		// set all parameters of a new job
		runningJob.setJobNumber(p[1]); // p [1] = job number
		runningJob.setPriority(p[2]); // p [2] = priority
		runningJob.setSize(p[3]); // p [3] = job size, K bytes
		runningJob.setMaxCpuTime(p[4]); // p [4] = max CPU time allowed for job
		runningJob.setStartAddress(-1); // set start address to -1 because it is still not in memory
		jobTable.add(runningJob); // add new job to job table
		swapper(); // call for a swapper
		cpuScheduler(a, p); // call for CPU scheduler function
	}

	// invoked when the disk generates an interrupt
	// the disk has finished an I/O
	public static void Dskint(int[] a, int[] p) {
		// Disk interrupt. 
		// diskint called by sos
		// p [5] = current time, ignore the rest
		if (cpuRunning) {
			bookKeeper(p[5]);
			readyQueue.add(runningJob);
		}
		// The disk has finished an I/O operation. 
		// I/O has been finished for job at top of I/O queue.
		jobInIoQ.setIOPending(0);
		// 	A latch bit or boolean is used to
		// indicate that a job is latched (currently doing I/O)
		// since we finished I/O we set it back o false
		jobInIoQ.setLatched(false);
		// if job in I/O queue is Blocked
		if (jobInIoQ.getBlocked()) {
			// unblock the job that was blocked
			// since I/O is done
			// System.out.println("jobInIoQ.getBlocked() " + jobInIoQ.getBlocked()); // test
			jobInIoQ.setBlocked(false); // set blocked to false
			readyQueue.add(jobInIoQ); // put back on the ready Queue
			// update the counter of blocked jobs - 1
			countBlocked--;
			// System.out.println("countBlocked " + countBlocked); // test
		}
		// check if the job was terminated
		if (jobInIoQ.getKillBit()){
			// free memory 
			memoryManager.freeSpace(jobInIoQ);
		}
		// set I/O running to false
		ioRunning = false;
		// call for I/O Scheduler function
		ioScheduler();
		// call for cpuScheduler to schedule next job to run
		cpuScheduler(a, p);

		return;
	}
	// helper function for Dskint
	public static void ioScheduler() {
		// if I/O is not running and I/O queue is not empty
		if (!ioRunning && !ioQueue.isEmpty()){
			// set I/O to running
			ioRunning = true;
			// To start the disk doing an I/O operation
			// get first elemnt from I/O queue
			sos.siodisk(ioQueue.getFirst().getJobNumber());
			// Latched => currently doing I/O
			ioQueue.getFirst().setLatched(true);
			// save the first job in I/O queue
			// into jobInIoQ
			jobInIoQ = ioQueue.getFirst();
			// remove that job's I/O request from I/O queue
			ioQueue.remove(ioQueue.getFirst());
		}
		return;
	}
	// The drum has finished swapping a job in or out of memory
	public static void Drmint(int[] a, int[] p){
		// book keeping a job
		if (cpuRunning){
			// update the cpu time used  for the program that was interrupted 
			bookKeeper(p[5]);
			// put this job back into a redy queue
			readyQueue.add(runningJob);
		}
		// if job was swapped into memory
		if (!jobInDrumQ.getIncore()){
			// set incore to true because it means that job is in memory
			jobInDrumQ.setIncore(true);
			// put this job to ready Queue so it can scheduled to run by CPU scheduler
			readyQueue.add(jobInDrumQ);
			// if job in Drum Queue has pending I/O
			if (jobInDrumQ.getIOPending() == 1)
				// add this job to I/O queue
				ioQueue.add(jobInDrumQ);
		} else {
			// if job was swapped out of memory
			jobInDrumQ.setIncore(false);
			// put this job back to jobTable
			jobTable.add(jobInDrumQ);
			// job swapped from memory
			// therefore empty memory
			memoryManager.freeSpace(jobInDrumQ);
			// remove that job from drum queue 
			drumQueue.remove(jobInDrumQ);
		}
		// set swapping to false because it finished swapping
		swapping = false;
		// call for swapper
		swapper();
		// call for CPU schedlure to schedule next job to run
		cpuScheduler(a, p);
		return;
	}

	public static void Tro(int[] a, int[] p){
		// CPU stopped running either it used the timeslice 
		// or it requested to be terminated
		bookKeeper(p[5]);
		// if all cpu time used 
		if (runningJob.getMaxCpuTime() == runningJob.getCpuTimeUsed()){
			// check if I/O is pending
			if (runningJob.getIOPending() == 1) {
				// set kill bit to true
				// terminate only once I/O is finished
				runningJob.setKillBit(true);
				// runningJob.setTerminated(true);
			} else {
				// remove from memory
				memoryManager.freeSpace(runningJob);
			}
			// decide if there is enough space to place a new job into memory
			swapper();
		} else {
			// if the job needs more time
			// add it back to a ready queue
			readyQueue.add(runningJob);
		}
		// call CPU scheduler to schedule next job to run
		cpuScheduler(a, p);
		return;
	}

	public static void Svc(int[] a, int[] p) {
		bookKeeper(p[5]);
		readyQueue.add(runningJob);
		drumQueue.remove(runningJob);
		int value = a[0];
		switch (value){
		case 5:{
			// request to terminate
			// remove running job from Ready Queue
			readyQueue.remove(runningJob);
			if (runningJob.getIOPending() == 1){
				// runningJob.setTerminated(true);
				runningJob.setKillBit(true);
			}else{
				// if no I/O pending remove from memory
				memoryManager.freeSpace(runningJob);
			}
			break;
		}
		case 6:{
			// the job is requesting another disk I/O operation
			runningJob.setIOPending(1); // If I/O pending set the value to 1
			// add the job to the I/O queue
			ioQueue.add(runningJob);
			// call I/O scheduler to start I/O
			ioScheduler();
			break;
		}
		case 7:{
			//  the job is requesting to be blocked until all pending I/O requests are completed
			//  When all outstanding I/O has been completed, the job should be unblocked.
			if (runningJob.getIOPending() == 1){
				runningJob.setBlocked(true);
				// remove that job from ready queue
				readyQueue.remove(runningJob);
				if (countBlocked++ > 0 && !runningJob.getLatched()){
					// put back on the drum queue
					drumQueue.add(runningJob);
					// swap that job
					swapper();
				}
				break;
			}
		}
		default:
			break;
		}
		cpuScheduler(a, p);
		return;
	}

	public static void swapper() {
		int large = 0;
		int lgIndex = 0;
		int diff = 0;
		// if not swapping
		if (!swapping) {
			// find the largest job next to be placed  
			for (int i = 0; i < drumQueue.size(); i++){
				// calculate the difference between max CPU time and CPU time used
				diff = drumQueue.get(i).getMaxCpuTime() - drumQueue.get(i).getCpuTimeUsed();
				// the job cannot be swapped out if it is doing I/O
				if (!drumQueue.get(i).getLatched() && diff > large) {
					lgIndex = i;
					large = diff;
					// ready to swap
					swapping = true;
				}
			}
			for (int i = 0; i < jobTable.size(); i++){
				// call memory manager to put the job into memory
				memoryManager.findSpace(jobTable.get(i));
				// if job address is >= 0
				// int address = jobTable.get(i).getStartAddress();
				// System.out.println("address " + address);
				if (jobTable.get(i).getStartAddress() >= 0) {
					// set swapping to true
					swapping = true;
					// 0 swapping into memory
					sos.siodrum(jobTable.get(i).getJobNumber(), jobTable.get(i).getSize(),
							jobTable.get(i).getStartAddress(), 0);
					// save the value of the job swapped in into jobInDrumQ
					jobInDrumQ = jobTable.get(i);
					// Removes the element at the specified position in Job Table
					jobTable.remove(i);
					return;
				}
			}
			// swapper
			if (swapping){
				// add a job to drum
				jobInDrumQ = drumQueue.get(lgIndex);
				// remove from I/O queue since the job is not in memory
				ioQueue.remove(drumQueue.get(lgIndex));
				readyQueue.remove(drumQueue.get(lgIndex));
				// swapp the big job out of memory 
				sos.siodrum(drumQueue.get(lgIndex).getJobNumber(), drumQueue.get(lgIndex).getSize(),
						drumQueue.get(lgIndex).getStartAddress(), 1);
			}	
		} else {
			return;
		}
	}

	public static void cpuScheduler(int[] a, int[] p){
		// set the CPU start time to current time
		// neccessary for bookKeeping once the job gets interrupted
		cpuStartTime = p[5]; 
		int timeRemaining = 0;
		int shortest = 0, shortIndex = 0;
		cpuRunning = false;
		for (int i = 0; i < readyQueue.size(); i++){
			// go through the list of the ready Queue 
			// check if job is not blocked and the size is less than shortest value 
			// or shortest = 0 for the first time it loops
			if (!readyQueue.get(i).getBlocked() && (readyQueue.get(i).getSize() < shortest || shortest == 0)) {
				// save the size of the job in shortest
				shortest = readyQueue.get(i).getSize();
				// also save the index of the job
				shortIndex = i;
				// set CPU to running
				cpuRunning = true;
			}

		}
		// if there are no jobs  to run set a to 1
		a[0] = 1;
		// OS Dispatcher
		if (cpuRunning){
			a[0] = 2; // there is a job to run set a to 2
			p[2] = readyQueue.get(shortIndex).getStartAddress();
			p[3] = readyQueue.get(shortIndex).getSize();
			// p[4] = TIMEMANAGER; // set timeslice
			// sets New Job value to the shortest job that will running on SOS
			runningJob = readyQueue.get(shortIndex);
			// System.out.println("New Job in a = 2 job # " + runningJob.getJobNumber());
			// checking if the CPU time left for this job is less than 
			// the time slice p[4] value
			timeRemaining = runningJob.getMaxCpuTime() - runningJob.getCpuTimeUsed();
			if (timeRemaining < TIMEMANAGER) {
				p[4] = timeRemaining; // if the time left is less than time slice set timeslice to time remaining
			} else {
				p[4] = TIMEMANAGER; // set timeslice
			}
		}
		// loop through the ready queue
		// remove the job from ready Queue
		while (readyQueue.contains(runningJob)){
			readyQueue.remove(runningJob);
		}
		return;
	}
	// the job of OS to make sure that information 
	// about the job that was running is not lost
	// bookKeeper does that
	public static void bookKeeper(int time){
		int cpuTimeUsed = (time - cpuStartTime) + runningJob.getCpuTimeUsed();
		runningJob.setCpuTimeUsed(cpuTimeUsed);
	}
}
