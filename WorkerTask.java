package parallel_regression;

import java.io.FileNotFoundException;

import edu.rit.pj2.DoubleVbl;
import edu.rit.pj2.Loop;
import edu.rit.pj2.Task;
import edu.rit.util.Random;

/**
 * Worker task class.
 */
public class WorkerTask extends Task {

	// variables needed for regression
	int rank;
	double threshold;
	double[] weights;
	DoubleArraySumVbl weightUpdateVbl;
	DoubleVbl cost = new DoubleVbl.Sum();
	DoubleVbl sqr_err = new DoubleVbl.Sum();
	IntArraySumVbl confusion;
	DataStrategy data;
	
	public void main(String[] args) throws Exception {
		
		/* Parse command line arguments.
		  0 - datafile
		  1 - seed
		  2 - separator
		  3 - classIdx
		  4 - threshold
		  5 - testFile
		  6 - chunked
		  7 - records
		  8 - testPercent
		*/
		
		rank = taskRank();
		// Parse command line arguments.
		//set up the data strategy with user-defined variables
		// since this is a regular worker, we know minibatch is 0 and the seed is ignored
		if(args[0].equals("URL"))
			data = new URLDataStrategySeq(rank, Integer.parseInt(args[7]), 0, 1L, 
					Boolean.parseBoolean(args[6]), groupSize(), Double.parseDouble(args[8]));
		else
			data = new RegularDataStrategy(args[0], args[5], args[2], Integer.parseInt(args[3]), rank, 
					Integer.parseInt(args[7]), 0, 1L, Boolean.parseBoolean(args[6]), groupSize());
		threshold = Double.parseDouble(args[4]);
		
		// Initialize weights, will be the same for all since same seed
		weights = new double[data.getWidth()];
		Random prng = new Random(Long.parseLong(args[1]));
		for (int i = 0; i < weights.length; ++ i)
			weights[i] = prng.nextDouble()*2 - 1;
		
		//Give the initialized weights to the master, since he didn't read any
		//data and doesn't know what size it is 
		if (rank==0)
			putTuple(new WeightTuple(false, 0, weights));
		
		// set up variables you will need to reduce over node threads 
		// and the tuples for inter-thread communication
		long t = 0;
		// need a separate copy for each thread, this is a lot of memory for URLs..
		weightUpdateVbl = new DoubleArraySumVbl(weights.length);
		WeightTuple weightTemplate = new WeightTuple();
		WeightTuple wt = null;
		
		//continue until convergence or until master says to stop
		for (;;) {
		
			//re-initialize variables
			weightUpdateVbl.zeroize();
			cost.item = 0.0;
			sqr_err.item = 0.0;

			// Threads will run iterations in parallel on the node
			parallelFor(0, data.size()-1).exec(new Loop() {
				
				// thread-local variables
				DoubleVbl thrCost;
				DoubleVbl thrError;
				DoubleArraySumVbl thrWeightUpdate;
				double output, error;
				int correct;
				
				public void start() {
					thrCost = threadLocal(cost);
					thrError = threadLocal(sqr_err);
					thrWeightUpdate = threadLocal(weightUpdateVbl);
				}

				public void run (int i) {
					//get the dot product, then apply sigmoid function
					output = data.getEstimate(weights, i); 
					// scale output label to be 0/1
					correct = data.getCorrectClass(i);
					//get the error between predicted class and actual class
					error = output-correct;
					thrError.item += error*error;
					// definiton of cost fn in logistic regression
					thrCost.item += (-correct * Math.log(output)) -
								((1-correct)*Math.log(1-output));
					//weightUpdate stores the sum of all first partial derivatives
					data.addPartials(thrWeightUpdate.data, error, i);
				}
			});

			// Advance to next time step.
			++t;
			// Send updated summation over training set to the weight update task.
			putTuple(new UpdateTuple(rank, t, weightUpdateVbl.data,
					sqr_err.item, cost.item, data.size()));

			// Receive new tuple of updated weights from the master node
			weightTemplate.step = t;
			wt = takeTuple(weightTemplate);
			//update weight vector for next iteration
			weights = wt.weights;
			// Stop when master tells this node to stop & move on to testing
			if (wt.stop)
				break;
		}
		//after convergence, test data to get the confusion matrix
		classify();
		putTuple(new ResultsTuple(rank, data.getMinClass(), data.getMaxClass(), confusion.data));
	}
	
	private void classify() throws NumberFormatException, FileNotFoundException {
		
		// change to a new dataset if requested by the user
		data.prepareForTesting();
		confusion = new IntArraySumVbl(4);
		
		// Threads will run iterations in parallel on the node
		parallelFor(0, data.size()-1).exec(new Loop() {
			// thread-local variables
			IntArraySumVbl thrMat;
			int decision;

			public void start() {
				//set up automatic sum-reduce
				thrMat = threadLocal(confusion);
			}

			public void run (int i) {
				// use threshold to classify as 0/1
				decision = (data.getEstimate(weights, i)>=threshold)?1:0;
				// [data.getCorrectClass(i)][decision] is the normal confusion matrix
				// Here using a 1-dimensional array, so
				// [0][0] maps to [0], [0][1] = 1, [1][0] = 2 and [1][1] = 3
				thrMat.data[data.getCorrectClass(i)*2 + decision]++;
			}
		}); //thrMat are sum-reduced into confusion here
	}
}