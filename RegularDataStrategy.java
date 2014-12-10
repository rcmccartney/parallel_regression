package parallel_regression;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import edu.rit.pj2.Chunk;
import edu.rit.util.Random;


public class RegularDataStrategy implements DataStrategy {
	
	private ArrayList<double[]> data;
	private ArrayList<Integer> classLabel;
	private int width, classIdx, records, size, minClass=100; // use in case the classes aren't labeled as 0/1
	private int minibatchsize, batchlen, batchstart;
	private Scanner train;
	private String test, separator;
	private int[] batchDirector;
	private Random rand;
	private int rank, K;
	private boolean chunked;
	
	/**
	 * Regular two-class data file, with class labels as 0/1 or 1/2, etc.  
	 * This will map it down to 0/1 
	 * 
	 * Assumes the rows have a fixed width
	 * :TODO: fix what happens when the data isn't chunked, parallel team, and user requests 
	 * a set number of records
	 * @throws FileNotFoundException 
	 */
	public RegularDataStrategy(String train, String test, String separator, int classIdx, 
			int rank, int records, int minibatchsize, long seed, boolean chunked, int K) 
					throws FileNotFoundException {
		
		this.train = new Scanner( new File(train) );
		this.test = test;
		this.separator = separator;
		this.classIdx = classIdx;
		this.data = new ArrayList<>();
		this.classLabel = new ArrayList<>();
		this.records = records;
		this.minibatchsize = minibatchsize;
		this.rank = rank;
		this.K = K;
		this.chunked = chunked;
		rand = new Random(seed);
		
		// read the data into the arraylists for batch training
		// also sets up the minClass, data, classLabel, and weights
		long time = System.currentTimeMillis();
		readData(this.train);
		if (rank != -1)
			System.out.printf("Worker %d: ", rank);
		System.out.printf("Read data in %.3f seconds%n", 
				(System.currentTimeMillis()-time)/1000.0 );
		System.out.flush();
		
		//if you don't use batches, the director will just forward on the request i->i
		batchlen = size;
		batchstart = 0;
		batchDirector = new int[size];
		for( int i=0; i < batchDirector.length; i++)
			batchDirector[i] = i;
	}
		
	private void reduceData() {
		Chunk slice = Chunk.partition(0, size-1, K, rank);
		for(int i=0; i < slice.lb(); i++) {
			data.remove(0);
			classLabel.remove(0);
			size--;
		}
		while(size > slice.length()) {
			data.remove(size-1);
			classLabel.remove(size-1);
			size--;
		}
	}

	// Implementing Fisher-Yates shuffle
	private void shuffleArray(int[] ar) {
	    for (int i = ar.length - 1; i > 0; i--) {
	    	int index = rand.nextInt(i + 1);
	    	// Simple swap
	    	int a = ar[index];
	    	ar[index] = ar[i];
	    	ar[i] = a;
	    }	
	}
	
	public void setMiniBatch() {
		if (minibatchsize > 0) {
			shuffleArray(batchDirector);
			int batch = rand.nextInt(minibatchsize);
			Chunk slice = Chunk.partition(0, size-1, minibatchsize, batch);
			batchlen = (int) slice.length();
			batchstart = slice.lb();
		}
		else {
			batchlen = size;
			batchstart = 0;
		}
	}
	
	public int getCorrectClass(int i) {
		// if not using batches batchdirector will just return i
		return classLabel.get( batchDirector[i+batchstart] ) - minClass;
	}
	
	public void addPartials(double[] weightUpdate, double error, int i) {
		
		for(int j=0; j < weightUpdate.length; j++) 
			// if not using batches batchdirector will just return i
			weightUpdate[j] += error*data.get( batchDirector[i+batchstart] )[j]; 
	}
	
	// can be used to addPartials in parallel, each thread getting an index j
	public void addPartials(double[] weightUpdate, int j, double error, int i) {
		
		weightUpdate[j] += error*data.get( batchDirector[i+batchstart] )[j]; 
	}
	
	/**
	 * Here implemented as a sigmoid over the dot product
	 * 
	 * The data knows how to calculate the dot product and sigmoid to 
	 * allow for special implementations based on the dataset
	 * 
	 * @param values
	 * @return
	 */
	public double getEstimate(double[] weights, int i) {
		
		double sum = 0.0;
		for(int j=0; j < weights.length; j++)
			sum += weights[j]*data.get(batchDirector[i+batchstart])[j];
		double out = 1.0 / (1 + Math.exp(-sum));
        // cap out to never be 0 or 1 to prevent getting stuck in local optima
		if (Double.isNaN(out) || Math.abs(out-1) < .0001)
            out = 0.9999;
		else if (Math.abs(out) < 0.0001)
        	out = 0.0001;
		return out;
	}
	

	public void prepareForTesting() throws NumberFormatException, FileNotFoundException {
		
		// change to a new dataset if requested by the user
		if (!test.equals("")) {
			System.out.println("Using separate test set");
			readData(new Scanner(new File(test)));
		}
		batchlen = size;
		batchstart = 0;
		batchDirector = new int[size];
		for( int i=0; i < batchDirector.length; i++)
			batchDirector[i] = i;
	}
	
	/**
	 * This method takes a real-valued data file and stores it into memory
	 * using the training scanner, string separator, and classIdx that the 
	 * user set on the command line 
	 * 
	 * @param aScanner the opened file that will be read into memory
	 * 
	 * @throws NumberFormatException for non-real valued data or non-integer classes
	 */
	private void readData(Scanner aScanner) throws NumberFormatException {
		
		//TODO: add feature normalization 
		
		String[] line;
		boolean firstLine = true;
		int label;
		data.clear();
		classLabel.clear();
		
		while (aScanner.hasNext()) {
			line = aScanner.nextLine().split(separator);
			// will store the new row of data
			// includes room for a bias term b/c class data isn't stored here
			double[] row = new double[line.length];
			row[0] = 1.0;  // set the bias term input value 
			if (firstLine) {
				width = line.length;  // number of attirbutes in a standard row
				if (classIdx == -1)
					classIdx = line.length - 1;
				try {
					for(int i=0, j=1; i < line.length; i++) { // start j one over from bias term 
						if (classIdx == i) {
							label = Integer.parseInt(line[i].trim());
							if ( label<minClass )
								minClass = label;
							classLabel.add( label );
						}
						else
							row[j++] = Double.parseDouble(line[i].trim());
					}
					data.add(row);  // no exception, move on
					size++;
				// only catch the first row b/c it is allowed to be a header row
				} catch (NumberFormatException e)  {
					System.out.println("Header information ( or you forgot sep=char ):");
					for(String item : line)
						System.out.print(item + " ");
					System.out.println();
					System.out.flush();
				}
				firstLine = false;
			}
			else {
				for(int i=0, j=1; i < line.length; i++) { // start j one over from bias term 
					if (classIdx == i) {
						label = Integer.parseInt(line[i].trim());
						if ( label<minClass )
							minClass = label;
						classLabel.add( label );
					}
					else
						row[j++] = Double.parseDouble(line[i].trim());
				}
				data.add(row);  
				size++;
			}
			if (size == records)
				break;
		}
		
		// rank>=0 says this is a parallel team thread
		// if the data isn't chunked, then this thread can throw out 
		// the data used by other threads in the team
		// if it is already chunked for the threads, then the entire dataset will be kept 
		if (rank >= 0 && !chunked) {
			this.reduceData();
		}
	}

	public int getMinClass() {
		return minClass;
	}

	public int getMaxClass() {
		return minClass+1;
	}

	public int getWidth() {
		return width;
	}

	public int size() {
		return batchlen;
	}
}
