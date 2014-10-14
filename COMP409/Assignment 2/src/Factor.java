import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;



public class Factor implements Runnable {
	private int p;
	private BigInteger n;
	private ArrayList<BigInteger> factors;
	private Thread[] threads;
	
	public static void main(String[] args) {
		if (args.length != 2) {
            System.out.println("Invalid input");
            return;
        }
		
		int p = 1;
		BigInteger n;
        try {
            p = Integer.parseInt(args[0]);
            n = new BigInteger(args[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Please using the format below where p is the "
            		+ "number of threads to use and n is the number to factor:\n"
            		+ "Java Factor p n");
        }
        
        System.out.println("Factoring " + n);
        factorNumber(p, n);
        //superRunner(4, n);
	}
	
	public static void superRunner(int numThreads, BigInteger n) {
		System.out.println("Using up to " + numThreads + " threads");
		
		for (int i=1; i<=numThreads; i++) {
			factorNumber(i, n);
		}
	}
	
	public static void factorNumber(int p, BigInteger n) {
		Factor factor = new Factor(p, n);
        
        System.out.print(p + " threads: ");
        
        long startTime = System.currentTimeMillis();
        factor.run();
        long endTime = System.currentTimeMillis();
        
        System.out.println((endTime - startTime)/1000 + " seconds");
        
        //print out all factors
      	System.out.println(factor.printFactors());
	}
	
	/**
	 * Default constructor
	 * @param p the number of threads to use
	 * @param n the number to factor
	 */
	public Factor(int p, BigInteger n) {
		this.p = p;
		this.n = n;
		
		factors = new ArrayList<BigInteger>();
		threads = new Thread[p];
	}
	
	/**
	 * Prints out "Prime" if the number is prime, otherwise it prints a list of factors
	 */
	@Override
	public void run() {
		BigInteger start = BigInteger.ONE.add(BigInteger.ONE);
		BigInteger end = sqrt(n);
		BigInteger sizePerThread = end.subtract(BigInteger.ONE).divide(new BigInteger(p+""));
		
		//create threads
		for (int i=0; i< threads.length; i++) {
			if (i != threads.length - 1) {
				threads[i] = new FactorThread(n, start, start.add(sizePerThread));
			} else {
				threads[i] = new FactorThread(n, start, end);
			}
			
			start = start.add(sizePerThread).add(BigInteger.ONE);
		}
		
		//start threads
		for(Thread thread : threads) {
			thread.start();
		}
		
		//wait for threads to finish
		for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
		
		for (int i=0; i< factors.size(); i++) {
			BigInteger factor = factors.get(i);
			if(!factor.isProbablePrime(100)) {
				factors.remove(i);
			}
		}
	}
	
	private String printFactors() {
		if (factors.size() == 0) {
			return "prime";
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("Factors: ");
		for (int i=0; i<factors.size(); i++) {
			buffer.append(factors.get(i));
			
			if (i != factors.size()-1) {
				buffer.append(", ");
			}
		}
		
		return buffer.toString();
	}
	
	/**
	 * Adds a factor to a list
	 */
	public void addFactor(BigInteger factor) {
		factors.add(factor);
	}
	
	/**
	 * Computes the square root of a big integer
	 * @throws IllegalArgumentException
	 */
	public static BigInteger sqrt(BigInteger x) throws IllegalArgumentException {
	    if (x.compareTo(BigInteger.ZERO) < 0) {
	        throw new IllegalArgumentException("Negative argument.");
	    }
	    
	    // square roots of 0 and 1 are trivial
	    if (x == BigInteger.ZERO || x == BigInteger.ONE) {
	        return x;
	    }
	    
	    BigInteger two = BigInteger.valueOf(2L);
	    BigInteger y;
	    
	    // starting with y = x / 2 avoids magnitude issues with x squared
	    for (y = x.divide(two); y.compareTo(x.divide(y)) > 0; y = ((x.divide(y)).add(y)).divide(two));
	    
	    return y;
	}
	
	
	private class FactorThread extends Thread {
		private BigInteger n;
		private BigInteger start;
		private BigInteger end;
		
		private FactorThread(BigInteger n, BigInteger start, BigInteger end) {
			this.n = n;
			this.start = start;
			this.end = end;
		}
		
		private void findFactors() {
			BigInteger index = start;
			
			while(!index.equals(end.add(BigInteger.ONE))) {
				// check for divisibility
				if(n.mod(index).equals(BigInteger.ZERO)) {
					addFactor(index);
					addFactor(n.divide(index));
					
					//System.out.println("Found factor: " + index);
				}
				
				//increment index
				index = index.add(BigInteger.ONE);
			}
		}

		@Override
		public void run() {
			System.out.println("Start: " + start + " End: " + end);
			findFactors();
		}
	}
}