package parallel_regression;


import java.io.FileNotFoundException;
import java.util.ArrayList;

import edu.rit.pj2.Loop;
import edu.rit.pj2.Task;
import edu.rit.util.Random;

/**
 * This sequential program uses the gradient descent method of 
 * logistic regression to classify a data set. Data set must be a 
 * two-class classification problem.  Regularization is used to 
 * prevent overfitting of the data.  Our goal is to get a low 
 * classification error on unseen instances of data
 * 
 * For Stochaistic version we don't use regularization
 * 
 * Usage: java LogRegSeq <trainFile> 
 * 
 * @author  Robert McCartney
 * @version 9-Nov-2014
 *
 */
public class LogRegParSGD extends Task {

	// Program shared variables.
	// Command line arguments.
	private String test, separator;
	private int classIdx, snap, records, converge;
	private double eps, alpha, threshold, testPercent;
	private double[] weights, weightUpdate;
	private long steps, seed;
	private ArrayList<Double> cost, sqr_err;
	private Random prng;
	// encapsulates reading and storing the data in order to allow for 
	// different types of data strategies
	private DataStrategy data;
	
	/**
	 * The main program
	 * 
	 * @param args command-line arguments
	 * @throws IllegalArgumentException for improper command-line arguments
	 * @throws FileNotFoundException 
	 */
	public void main(String[] args) throws IllegalArgumentException, FileNotFoundException {
		
		//Parse arguments and print error on improper usage
		if ( args.length < 1 ) usageError();
		test = "";
		separator = "\\s+";
		classIdx = -1;
		alpha = 0.05;
		eps = 0.000001;
		threshold = 0.5;
		steps = 0;
		snap = 0;
		seed = 1L;
		records = -1;
		converge = 500;
		testPercent = 0.0;
		cost = new ArrayList<>();
		sqr_err = new ArrayList<>();
		try {
			//optional arguments
			for(int i = 1; i < args.length; i++) {
				String[] curr = args[i].split("="); 
				if (curr[0].equals("seed"))
					seed = Long.parseLong(curr[1]);
				else if (curr[0].equals("testFile"))
					test = curr[1];
				else if (curr[0].equals("sep"))
					separator = curr[1];
				else if (curr[0].equals("class"))
					classIdx = Integer.parseInt(curr[1]);
				else if (curr[0].equals("alpha"))
					alpha = Double.parseDouble(curr[1]);
				else if (curr[0].equals("eps"))
					eps = Double.parseDouble(curr[1]);
				else if (curr[0].equals("thr"))
					threshold = Double.parseDouble(curr[1]);
				else if (curr[0].equals("steps"))
					steps = Long.parseLong(curr[1]);
				else if (curr[0].equals("snap"))
					snap = Integer.parseInt(curr[1]);
                else if (curr[0].equals("records"))
                    records = Integer.parseInt(curr[1]);
                else if (curr[0].equals("converge"))
                    records = Integer.parseInt(curr[1]);
                else if (curr[0].equals("test"))
                    testPercent = Double.parseDouble(curr[1]);
				else
					usageError();
			}
		} catch (Exception e) { e.printStackTrace(); usageError(); }
		
		if (testPercent < 0 || testPercent > 1) usageError();
		if (converge >= steps) converge = (int) (steps / 2);

		//set up the data strategy with user-defined variables
		//sequential regression can't be chunked since it is on a single node
		//but true tells DataStrategy not to try to chunk it
		if(args[0].equals("URL"))
			data = new URLDataStrategySeq(-1, records, 0, seed, true, 0, testPercent);
		else   //args[0] is training filename
			data = new RegularDataStrategy( args[0], test, separator, classIdx, -1, 
					records, 0, seed, true, 0);
		
		weights = new double[data.getWidth()];
		weightUpdate = new double[weights.length];
		
		// initialize weight vectors to small starting values in range (-1,1)
		prng = new Random(seed);
		for(int i=0; i < weights.length; i++)
			weights[i] = prng.nextDouble()*2 - 1;
		
		// change in error is used to detect convergence
		double log_error_sum=0, sse=0, output=0, 
				error=0;
		int correct;
		double delta = 0.0;
		long t = 0;  //timesteps
		long time = System.currentTimeMillis();
		int index;
		
		//continue until convergence or user defined steps
		for(;;) {
			
			++t;  // advance to next time step
			// SGD continues back at 0 after seeing all examples 
			index = (int) (t % data.size());  
			// reinitialize variables
			for(int i=0; i<weightUpdate.length; i++) {
				weightUpdate[i] = 0.0;					
			}
			// the stochaistic training portion of gradient descent
			output = data.getEstimate( weights, index );
			//get the error between predicted class and actual class
			correct = data.getCorrectClass(index);
			error = output-correct;
			// definiton of cost fn in logistic regression
			//weightUpdate stores the sum of all first partial derivatives
			data.addPartials(weightUpdate, error, index);

			// update the weight vector using gradient descent 
			//don't regularize 
			parallelFor(0, weightUpdate.length-1).exec(new Loop() {
				public void run(int i) throws Exception {
					weights[i] -= alpha*weightUpdate[i];					
				}
			});
			// cost + regularization term
			sse += error*error;
			log_error_sum += (-correct*Math.log(output))-((1-correct)*Math.log(1-output));
			// store the average cost after converge iterations and check for convergence
			if (t % converge == 0) {  
				cost.add(log_error_sum/converge);  
				sqr_err.add(sse/converge); // store average squared error
				log_error_sum = 0.0;
				sse = 0.0;
				if (cost.size() > 1) {  // delta is a percent change
					delta = (cost.get(cost.size()-1)-cost.get(cost.size()-2)) / cost.get(cost.size()-2);
					if (steps == 0 && Math.abs(delta) < eps) 
						break;
				}
			}
			
			if (snap > 0 && t % snap == 0 && t >= converge)
				snapshot(t);
			// delta must be below eps% change to quit
			if (steps != 0 && t == steps) 
				break;
		}
		System.out.printf("Logistic regression took %.3f seconds in %d iterations%n", 
				(System.currentTimeMillis()-time)/1000.0, t);
		System.out.flush();
		displayResults();
	}
	
	public void snapshot(long t) {
		System.out.printf("Iter %d: Cost %4.4f Sq Error %4.4f%n", t, 
				cost.get(cost.size()-1), sqr_err.get(sqr_err.size()-1));
		System.out.flush();
	}
	
	public void printWeights() {
		//only print 10 features or less (plus bias makes 11)
		System.out.print("Weight vector: ");
		int wlen = (weights.length>11?11:weights.length);
		for(int i=0; i < wlen; i++)
			System.out.printf("w%d=%.3f ", i, weights[i]);
		System.out.println( (weights.length>11?"...":"") );
		System.out.println("Number of features with bias term: " + weights.length);
	}
	
	public void displayResults() throws NumberFormatException, FileNotFoundException {

		System.out.printf("Init:\tCost %4.4f Sq Error %4.4f%n", cost.get(0), sqr_err.get(0));
		System.out.printf("Final:\tCost %4.4f Sq Error %4.4f%n", 
				cost.get(cost.size()-1), sqr_err.get(sqr_err.size()-1) );
		printWeights();
		System.out.flush();
		
		// now print the confusion matrix from the training or test set
		long time = System.currentTimeMillis();
		int[][] confusion = classify();
		System.out.printf("Classification took %.3f seconds%n", 
				(System.currentTimeMillis()-time)/1000.0 );
		System.out.flush();
		
		System.out.printf("    %8d %8d", data.getMinClass(), data.getMaxClass());
		for(int i=0; i< confusion.length; i++) {
			System.out.printf("%n%3d", (i==0?data.getMinClass():data.getMaxClass()));
			for(int j=0; j < confusion[0].length; j++)
				System.out.printf(" %8d", confusion[i][j]);
		}
		// Print accuracy and precision and recall
		System.out.printf("%nAccuracy: %3.3f%%%n", 100*(confusion[0][0]+confusion[1][1])/((double)data.size()) );
		System.out.printf("Precision: %3.3f%%%n", 100*((double)confusion[1][1])/(confusion[1][1]+confusion[0][1]) );
		System.out.printf("Recall: %3.3f%%%n", 100*((double)confusion[1][1])/(confusion[1][1]+confusion[1][0]) );
		System.out.flush();

		/* doesn't seem to work
		//plot the cost
		ListXYSeries ySE = new ListXYSeries();
		for(int i=0; i < cost.size(); i++) {
			ySE.add( i, sqr_err.get(i) );
		}
		Plot plot = new Plot()
			.plotTitle("Square Error vs. Epochs")
	    	.xAxisTitle("Iteration")
	    	.xAxisStart(0)
	    	.xAxisEnd(sqr_err.size())
	    	.xAxisMajorDivisions(sqr_err.size()/10)
	    	.yAxisTitle ("Square Error")
	    	.seriesStroke (null)
	    	.xySeries(ySE);
		plot.getFrame().setVisible(true);
		*/
	}
	
	private int[][] classify() throws NumberFormatException, FileNotFoundException {
	
		// change to a new dataset if requested by the user
		data.prepareForTesting();
		int decision;
		
		//create the confusion matrix
		int[][] mat = new int[2][2];
		
		//fill in confusion matrix
		for(int i=0; i < data.size(); i++) {
			// use threshold to classify as 0/1
			decision = (data.getEstimate(weights,i)>=threshold)?1:0;
			// scale output label to be 0/1
			mat[data.getCorrectClass(i)][decision]++;
		}
		return mat;
	}
	
	/**
	 * Print proper command line argument usage
	 * 
	 * @throws IllegalArgumentException for improper command-line arguments
	 */
	private static void usageError() {
		System.err.println("Usage: java LogRegSGD <trainFile>");
		System.err.println("<trainFile> is a real-valued data file");
		System.err.println("#####################");
		System.err.println("The following are additional options available:");
		System.err.println("[seed=long] value to seed the prng, default is 1L");
		System.err.println("[testFile=file] specify test set, default is <trainFile>");
		System.err.println("[alpha=double] specify learning rate, default is 0.05");
		System.err.println("[sep=char] specify separator, default is whitespace");
		System.err.println("[class=int] specify class location, default is last column in row");
		System.err.println("[eps=double] specify convergence threshold, defaults to 0.000001");
		System.err.println("[steps=long] specify number of iterations instead of eps convergence");
		System.err.println("[thr=double] specify threhold of class decision, default is 0.5");
		System.err.println("[snap=int] snapshot interval, default is 0");
		System.err.println("[records=int] number of records to process in the data, default is all data");
		System.err.println("[converge=int] number of steps before checking convergence, default is 500");
		System.err.println("[test=double] percent (between 0-1) of train examples to set aside for test, default is 0");
		System.err.println("#####################");
		System.err.flush();
		throw new IllegalArgumentException();
	}
}