package parallel_regression;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import edu.rit.pj2.Chunk;
import edu.rit.util.Random;

public class URLDataStrategySeq implements DataStrategy {

	//tot size of the dataset, not used anymore
	public static final int TOT_SIZE = 2396130;
	//number of feautres in the dataset
	public static final int WIDTH = 3231961;
	//Set these to be slices of the features you want to use
	public static final int SLICE = WIDTH;
	//lowest index to train with
	public static final int LOW_SLICE = 1;
	// highest index to train with, exclusive of this value
	// so features go from LOW_SLICE to HIGH_SLICE-1
	public static final int HIGH_SLICE = LOW_SLICE + SLICE;
	
	private ArrayList<Integer> classLabel, testClass, currentLabel;
	private ArrayList<HashMap<Integer, Double>> dataDbl, testDbl, currentDbl;
	private ArrayList<ArrayList<Integer>> dataBool, testBool, currentBool;
	private HashSet<Integer> features;
	private int size, records, rank, K, testSize;
	private int minibatchsize, batchlen, batchstart;
	private int[] batchDirector;
	private Random rand;
	private boolean chunked;
	private double testPercent;

	 /**
	  *	:TODO: fix what happens when the data isn't chunked, parallel team, and user requests 
	  * a set number of records
	  * 
	  * @param rank
	  * @param records
	  * @param minibatchsize
	  * @param seed
	  * @param chunked
	  * @param K
	  * @throws FileNotFoundException
	  */
	public URLDataStrategySeq(int rank, int records, int minibatchsize, long seed, 
			boolean chunked, int K, double testPercent) 
			throws FileNotFoundException {
		
		this.records = records;
		this.minibatchsize = minibatchsize;
		rand = new Random(seed);
		classLabel = new ArrayList<>();
		currentLabel = classLabel;
		dataDbl = new ArrayList<>(); 
		currentDbl = dataDbl;
		dataBool = new ArrayList<>();
		currentBool = dataBool;
		features = new HashSet<>();		
		this.rank = rank;
		this.K = K;
		this.chunked = chunked;
		this.testPercent = testPercent;
		
		if (testPercent != 0) {
			testBool = new ArrayList<>();
			testClass = new ArrayList<>();
			testDbl = new ArrayList<>();
		}
		
		long time = System.currentTimeMillis();
		// data is stored in /var/tmp/rm7536
		File basedir = new File("/var/tmp/rm7536/url_svmlight");
		parseFeatures(new Scanner(new File(basedir.getPath() + File.separator + "FeatureTypes")));
		ArrayList<String> names = new ArrayList<String>(Arrays.asList(basedir.list()));
		for (String name : names) {
			if (!name.equals("FeatureTypes"))
				parseData(new Scanner(new File(basedir.getPath() + File.separator + name)));
			if (size == records)
				break;
		}
		if (rank != -1)
			System.out.printf("Worker %d: ", rank);
		System.out.printf("Read %d instances in %.3f seconds%n", size,  
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
			dataBool.remove(0);
			dataDbl.remove(0);
			classLabel.remove(0);
			size--;
		}
		while(size > slice.length()) {
			dataBool.remove(size-1);
			dataDbl.remove(size-1);
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
		// get a chunk that will be this minibatch and shuffle the array to make it random 
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

	private void parseData(Scanner aScanner) {
		
		String[] line;
	
		while (aScanner.hasNext()) {
			
			line = aScanner.nextLine().split("\\s+");
			
			if (testPercent != 0 && (rand.nextDouble() < testPercent)) {
				testDbl.add(new HashMap<Integer, Double>());
				testBool.add(new ArrayList<Integer>());
				testBool.get(testSize).add(0);
				this.addLine(testBool, testDbl, testClass, testSize++, line);
			}
			else {
				dataDbl.add(new HashMap<Integer, Double>());
				dataBool.add(new ArrayList<Integer>());
				// add a bias term 
				dataBool.get(size).add(0);
				this.addLine(dataBool, dataDbl, classLabel, size++, line);
			}
			//increment size of this data structure
			//records is default to -1, so will read all the data unless it is overwritten
			if (size == records)
					break;
		}
		
		// rank>=0 says this is a parallel team thread
		// if the data isn't chunked, then this thread can throw out 
		// the data used by other threads in the team
		// if it is already chunked for the threads, then the entire dataset will be kept 
		if (rank >= 0 && !chunked) 
			this.reduceData();
	}
			
	private void addLine(ArrayList<ArrayList<Integer>> boolMap, ArrayList<HashMap<Integer, Double>> dblMap, 
			ArrayList<Integer> classList, int currentSize, String[] line) {
		
		String[] entry;
		int featureID;
		int classification = Integer.parseInt(line[0]);
		
		if (classification == -1) classification = 0;
		classList.add(classification);
		
		for(int i=1; i < line.length; i++) {
			entry = line[i].split(":");
			featureID = Integer.parseInt(entry[0]);
			// use a subset of all the possible features
			// low slice is mapped to index 1 in the weight array
			if (featureID >= LOW_SLICE && featureID < HIGH_SLICE) {
				if (features.contains(featureID))  //map the feature index to (1,SLICE)
					dblMap.get(currentSize).put(featureID-LOW_SLICE+1, Double.parseDouble(entry[1]));
				else 
					boolMap.get(currentSize).add(featureID-LOW_SLICE+1);
			}
		}
	}
	
	private void parseFeatures(Scanner scan) {
		while(scan.hasNextInt())
			features.add( scan.nextInt() );
	}

	public int getMinClass() {
		return -1;  // given from the dataset
	}

	public int getMaxClass() {
		return 1;  //given from the dataset
	}

	public int getWidth() {
		return SLICE+1;  
	}

	public int size() {
		return batchlen;  
	}

	public void prepareForTesting() throws NumberFormatException,
			FileNotFoundException {  
		// if you parsed everything into the training data structures then just set
		// test equal to them so that you can test the entire dataset
		if (testPercent == 0) {
			testClass = classLabel;
			testDbl = dataDbl;
			testBool = dataBool;
			testSize = size;
		}
		//set the minibatch to be the entire test dataset
		batchlen = testSize;
		batchstart = 0;
		batchDirector = new int[testSize];
		for( int i=0; i < batchDirector.length; i++)
			batchDirector[i] = i;
		currentBool = testBool;
		currentLabel = testClass;
		currentDbl = testDbl;
	}

	public int getCorrectClass(int i) {
		return currentLabel.get( batchDirector[i+batchstart] );
	}

	public double getEstimate(double[] weights, int i) {

		double sum = 0.0;
		//everything in dataBools is 0/1, so just add weights of (1) values
		for(int j=0; j < currentBool.get(batchDirector[i+batchstart]).size(); j++)
			sum += weights[currentBool.get(batchDirector[i+batchstart]).get(j)];
		//dataDbl has regular double values
		for( int key : currentDbl.get(batchDirector[i+batchstart]).keySet() )
			sum += weights[key]*currentDbl.get(batchDirector[i+batchstart]).get(key);
		double out = 1.0 / (1 + Math.exp(-sum));
        // cap out to never be 0 or 1 to prevent getting stuck in local optima
		if (Double.isNaN(out) || Math.abs(out-1) < .0001)
            out = 0.9999;
		else if (Math.abs(out) < 0.0001)
        	out = 0.0001;
		return out;
	}

	public void addPartials(double[] weightUpdate, double error, int i) {

		//everything in dataBools is 0/1, so just add error as partial deriv
		for(int j=0; j < currentBool.get(batchDirector[i+batchstart]).size(); j++)
			weightUpdate[currentBool.get(batchDirector[i+batchstart]).get(j)] += error;
		//dataDbl has regular double values, so this is normal process
		for( int key : currentDbl.get(batchDirector[i+batchstart]).keySet() )
			weightUpdate[key] += error*currentDbl.get(batchDirector[i+batchstart]).get(key);
	}
	
	// URL can't be called in a parallel thread because it is so sparse
	public void addPartials(double[] weightUpdate, int j, double error, int i) {
		throw new UnsupportedOperationException("URL data is too sparse to get partial derivatives in parallel");
	}
}
