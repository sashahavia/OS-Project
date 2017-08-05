// OS project
// Simulation of an operating system
// Aliaksandra Havia
// **************************************************************//

public class MemoryManager {
	//*******************************************************************************// 
	// Memory Manager: Memory is 100 K words, allocated in units of 1 K and     
	// numbered 0-99. At any given time, some words may be free, and some       
	// allocated. In order to swap a job into memory from the drum (say, job    
	// ComingIn), OS must invoke the memory manager to allocate a block of free 
	// memory for the job.                                                      
	// ******************************************************************************//

	private static int MEMORY_SIZE = 100; // max size for memory, jobs 0-99
	// 
	private static int[] memory;
	private static int currentAddress;
	MemoryManager(){
		// initialize a size of an array
		memory = new int[MEMORY_SIZE];

	}
	// find Space for Coming in Job
	public static void findSpace(Job job){
		// check if job size is less than memory size
		// System.out.println("MEMORY_SIZE in findSpace " + MEMORY_SIZE);
		if (job.getSize() <= MEMORY_SIZE){
			// loop for the length of the job and set array value to busy
			for (int i = currentAddress; i < currentAddress + job.getSize(); i++){
				memory[i] = -1; // -1 busy
			}
			// System.out.println("currentAddress " + currentAddress);
			job.setStartAddress(currentAddress); // set start address
			// current address
			FirstFitFreeSpace();
		}
		return;
	}
	// free Space once the job is finished
	public static void freeSpace(Job job){
		int address = job.getStartAddress();
		// start at the beginning address of the job 
		// loop through job size
		for (int i = address; i < address + job.getSize(); i++){
			// set array values to 0
			// 0 = free space in table
			memory[i] = 0;
		}
		// set the job's address to -1, finished
		job.setStartAddress(-1);
		// adjust free space table
		FirstFitFreeSpace();
		return;
	}
	// First Fit Free Space Table
	public static void FirstFitFreeSpace(){
		MEMORY_SIZE = 0; // set memory size to 0
		currentAddress = 0; // current address to 0
		int tempSize = 0; // temporary Size
		int tempAddress = 0; // temporary Address
		// loop through memory
		for (int i = 0; i < 100; i++){
			// if memory is free 
			if (memory[i] == 0){
				// System.out.println("MEMORY_SIZE in FFFS " + MEMORY_SIZE);
				// if temporary size plus 1 is greater than memory size
				if (tempSize++ > MEMORY_SIZE){
					// update the memory size
					MEMORY_SIZE = tempSize; // set memory size to value in temporary memory
					// System.out.println("MEMORY_SIZE in FFFS 2 " + MEMORY_SIZE);
					currentAddress = tempAddress;
				}
			}else{
				// if memory is -1 which is busy
				tempSize = 0; // set tempSize to 0
				tempAddress = i + 1; // temporary address is ponting to the next space
			}
		}
		return;
	}
}
