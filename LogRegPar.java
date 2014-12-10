package parallel_regression;


import edu.rit.pj2.Job;

/**
 * Input files are assumed to be split among the nodes of the cluster already, with
 * each file on the node given the same name.  Assumes every node has all the data, so
 * the data is chunked by the node itself   
 * 
 *
 * This sequential program uses the gradient descent method of 
 * logistic regression to classify a data set. Data set must be a 
 * two-class classification problem
 * 
 * Our goal is to get a low classification error on unseen instances of data
 * 
 * Usage: java pj2 LogRegSeq <trainFile>
 *  
 * @author  Robert McCartney
 * @version 9-Nov-2014
 */
public class LogRegPar extends Job {

	/**
	 * Job main program.
	 */
	public void main(String[] args) {
		//Parse arguments and print error on improper usage
		if ( args.length < 2 ) usageError();
		String separator = "\\s+";
		long seed = 1L;
		int classIdx = -1;
		double alpha = 0.05;
		double eps = 0.000001;
		double threshold = 0.5;
		double lambda = 1.0;
		long steps = 0;
		int snap = 0;
		int records = -1;
		String test = "";
		boolean chunked=true;
		int minibatch = 0;
		double gamma = 100.0;
		int batchtime = 5;
		double testPercent = 0.0;
		try {
			//optional arguments
			for(int i = 2; i < args.length; i++) {
				String[] curr = args[i].split("="); 
				if (curr[0].equals("seed"))
					seed = Long.parseLong(curr[1]);
				else if (curr[0].equals("chunked"))
					chunked = Boolean.parseBoolean(curr[1]);
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
				else if (curr[0].equals("lambda"))
					lambda = Double.parseDouble(curr[1]);
				else if (curr[0].equals("steps"))
					steps = Long.parseLong(curr[1]);
				else if (curr[0].equals("snap"))
					snap = Integer.parseInt(curr[1]);
                else if (curr[0].equals("records"))
                    records = Integer.parseInt(curr[1]);
                else if (curr[0].equals("batched"))
                	minibatch = Integer.parseInt(curr[1]);
                else if (curr[0].equals("gamma"))
                	gamma = Double.parseDouble(curr[1]);
                else if (curr[0].equals("biter"))
                	batchtime = Integer.parseInt(curr[1]);
                else if (curr[0].equals("test"))
                    testPercent = Double.parseDouble(curr[1]);
                else
					usageError();
			}
		} catch (Exception e) { e.printStackTrace(); usageError(); }
		
		if (testPercent < 0 || testPercent > 1) usageError();

		// Set up a task group of K worker tasks.
		int K = workers();
		if (K == DEFAULT_WORKERS) K = 1;

		// each worker does a fraction of the total records requested
		// records must be divisible by K or there will be rounding error
		if (records > 0) 
			records /= K;
		
		if (minibatch != 0) {
			//args[0] is train filename
			// these tasks will perform the summations over training examples
			rule().task(K, BatchWorkerTask.class).args(args[0], 
												  ""+seed,
												  ""+separator,
												  ""+classIdx,
												  ""+threshold,
												  test,
												  ""+chunked,
												  ""+records,
												  ""+alpha,
												  ""+lambda, 
												  ""+minibatch,
												  ""+gamma, 
												  ""+batchtime, 
												  ""+testPercent);
			// this task will update weights before next iteration
			rule().task(BatchWeightUpdateTask.class).args(""+eps,
													 ""+steps,
													 ""+snap,
													 ""+K).runInJobProcess();
		}
		else {  // normal LogRegression
			// these tasks will perform the summations over training examples
			rule().task(K, WorkerTask.class).args(args[0], 
												  ""+seed,
												  ""+separator,
												  ""+classIdx,
												  ""+threshold,
												  test,
												  ""+chunked,
												  ""+records,
												  ""+testPercent);
			// this task will update weights before next iteration
			rule().task(WeightUpdateTask.class).args(""+eps,
													 ""+steps,
													 ""+snap,
													 ""+alpha,
													 ""+lambda, 
													 ""+K).runInJobProcess();
		}
	}
	
	/**
	 * Print proper command line argument usage
	 * 
	 * @throws IllegalArgumentException for improper command-line arguments
	 */
	private static void usageError() {
		System.err.println("Usage: java pj2 LogRegSeq <trainFile> <chunked>");
		System.err.println("<trainFile> is a real-valued data file");
		System.err.println("The following are additional options available:");
		System.err.println("#####################");
		System.err.println("[chunked=boolean] if data was chunked into sub-files on cluster nodes, default is True");
		System.err.println("[seed=long] value to seed the prng, default is 1L");
		System.err.println("[testFile=file] specify test set, default is <trainFile>");
		System.err.println("[alpha=double] specify learning rate, default is 0.05");
		System.err.println("[sep=char] specify separator, default is whitespace");
		System.err.println("[class=int] specify class location, default is last column in row");
		System.err.println("[eps=double] specify convergence threshold, defaults to 0.000001");
		System.err.println("[steps=long] specify number of iterations instead of eps convergence");
		System.err.println("[thr=double] specify threhold of class decision, default is 0.5");
		System.err.println("[lambda=double] specify regularization parameter, default is 1.0");
		System.err.println("[snap=int] snapshot interval, default is 0");
		System.err.println("[records=int] number of records to process in the data, default is all data");
		System.err.println("[batched=int] number of batches to use, default is none");
		System.err.println("[gamma=double] gamma used for batch cost, default is 100");
		System.err.println("[biter=int] iterations to use on one batch, default is 5");
		System.err.println("[test=double] percent (between 0-1) of train examples to set aside for test, default is 0");
		System.err.println("#####################");
		System.err.flush();
		throw new IllegalArgumentException();
	}
}