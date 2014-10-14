import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Splitter extends Thread {
	private static final Random RANDOM = new Random();
	
    private int numThreads;
    private int numRounds;
    private int[][] grid;
    private SplitterThread[] threads;
    private AtomicIntegerArray[] locks;
    private AtomicIntegerArray ids;
    private CountDownLatch latch;
    
    public static void main(String[] args) {
    	int p;
		int n;
		
        try {
            p = Integer.parseInt(args[0]);
            n = Integer.parseInt(args[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Please using the format below :\n"
            		+ "java Splitter p n");
        }
        
        System.out.println("Running using " + p + " threads " + n + " times");
        
        Splitter splitter = new Splitter(p, n);
        splitter.run();
        
        splitter.showHits();

        int sumResults = splitter.sumResults();
        assert sumResults == p*n;
        System.out.println("\nSum: " + sumResults);
    }

    /**
     * Prints out the hit counts of the splitter network
     */
	private void showHits() {
		System.out.println("Hit Counts\n");
		
        for (int i = 0; i < numThreads; i++) {
            for (int j = 0; j < numThreads - i; j++) {
                System.out.print(ids.get(grid[i][j]) + "\t");
                
                assert grid[i][j] <= numRounds;
            }
            System.out.println();
        }
	}

	/**
	 * Returns the sum of the number of acquired ids
	 */
	private int sumResults() {
		int sum = 0;
        
		for (int i = 0; i < ids.length(); i++) {
            sum += ids.get(i);
        }
		return sum;
	}

    public Splitter(int numThreads, int numRuns) {
        this.numThreads = numThreads;
        this.numRounds = numRuns;
        
        this.locks = new AtomicIntegerArray[numThreads];
        this.grid = new int[numThreads][numThreads];

        int count = 1;
        for (int i=0; i < numThreads; i++) {
            for (int j=0; j < (numThreads - i); j++) {
                grid[i][j] = count++;
            }
        }
        
        this.ids = new AtomicIntegerArray(count);
        
        createThreads();
    }

    /**
     * Resets the lock values
     */
    private void resetLocks() {
        for (int i = 0; i < numThreads; i++) {
            locks[i] = new AtomicIntegerArray(numThreads - i);
            for (int j = 0; j < numThreads - i; j++) {
                locks[i].set(j, 0);
            }
        }
    }

    @Override
    public void run() {
    	startThreads();

        //Start renaming rounds
        for (int round=0; round < numRounds; round++) {
            resetLocks();

            latch = new CountDownLatch(numThreads);
            for (SplitterThread thread : threads) {
                thread.stopWaiting();
                synchronized (thread) {
                    thread.notify();
                }
            }
            
            try {
            	// wait for threads to finish
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        terminateThreads();
    }
    
    /**
     * Create threads
     */
    private void createThreads() {
        threads = new SplitterThread[numThreads];
        for (int i=0; i < numThreads; i++) {
            threads[i] = new SplitterThread();
        }
    }
    
    /**
     * Start threads
     */
    private void startThreads() {
    	for (SplitterThread thread : threads) {
        	thread.start();
        }
    }
    
    /**
     * Interrupts all threads
     */
    public void terminateThreads() {
    	for (Thread thread : threads) {
            thread.interrupt();
        }
    }

    class SplitterThread extends Thread {
        private AtomicBoolean waiting = new AtomicBoolean();
        private int id = -1;

        public SplitterThread() {
            startWaiting();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // finish waiting
                    while (waiting.get()) {
                        synchronized (this) {
                            wait();
                        }
                    }

                    int i = 0;
                    int j = 0;
                    // wait to find good id
                    while (!locks[i].compareAndSet(j, 0, 1)) {
                        // randomly go down or right
                        if (RANDOM.nextBoolean()) {
                            i++;
                        } else {
                            j++;
                        }
                    }
                    
                    setId(grid[i][j]);
                    ids.incrementAndGet(id);

                    latch.countDown();
                    
                    startWaiting();
                } catch (Exception e) {
                    return;
                }
            }
        }
        
        public void startWaiting() {
        	waiting.set(true);
        }
        
        public void stopWaiting() {
        	waiting.set(false);
        }
        
        public void setId(int id) {
        	this.id = id;
        }
    }
}