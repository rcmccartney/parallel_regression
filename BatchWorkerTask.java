package parallel_regression;


import java.io.FileNotFoundException;

import edu.rit.pj2.DoubleVbl;
import edu.rit.pj2.IntVbl;
import edu.rit.pj2.Loop;
import edu.rit.pj2.Task;
import edu.rit.util.Random;

/**
 * Worker task class.
 */
public class BatchWorkerTask extends Task {

	// variables needed for regression
	int rank, batchtime;
	double threshold, alpha, lambda, gamma;
	double[] weights, batchWeights;
	DoubleArraySumVbl weightUpdateVbl;
	DoubleVbl cost = new DoubleVbl.Sum();
	DoubleVbl sqr_err = new DoubleVbl.Sum();
	IntArraySumVbl confusion;
	DataStrategy data;
	
	// Task main program.
	public void main(String[] args) throws Exception {
		
		// Get worker rank.
		rank = taskRank();
		
		/* Parse command line arguments.
		  0 - datafile
		  1 - seed
		  2 - separator
		  3 - classIdx
		  4 - threshold
		  5 - test
		  6 - chunked
		  7 - records
		  8 - alpha
		  9 -lambda 
		  10 - minibatch
		  11 - gamma
		  12 - batchtime
		  13 - testPercent
		*/
		long seed = Long.parseLong(args[1]);
		
		//set up the data strategy with user-defined variables
		if(args[0].equals("URL"))
			data = new URLDataStrategySeq(rank, Integer.parseInt(args[7]), Integer.parseInt(args[10]), 
					seed, Boolean.parseBoolean(args[6]), groupSize(), Double.parseDouble(args[13]));
		else
			data = new RegularDataStrategy(args[0], args[5], args[2], Integer.parseInt(args[3]), rank, 
					Integer.parseInt(args[7]), Integer.parseInt(args[10]), seed, Boolean.parseBoolean(args[6]),
					groupSize());
		threshold = Double.parseDouble(args[4]);
		alpha = Double.parseDouble(args[8]);
		lambda = Double.parseDouble(args[9]);
		gamma = Double.parseDouble(args[11]);
		batchtime = Integer.parseInt(args[12]);
		
		// Initialize weights, will be the same for all since same seed
		weights = new double[data.getWidth()];
		batchWeights = new double[data.getWidth()];
		Random prng = new Random(Long.parseLong(args[1]));
		for (int i = 0; i < weights.length; ++ i)
			weights[i] = prng.nextDouble()*2 - 1;
		
		//Give the initialized weight size to the master, since he didn't read any
		//data and doesn't know what size it is 
		if (rank==0)
			putTuple(new IntVbl(weights.length));
		
		// set up variables you will need to reduce over node threads 
		// and the tuples for inter-thread communication
		long t = 0;
		// need a separate copy for each thread, this is a lot of memory for URLs..
		weightUpdateVbl = new DoubleArraySumVbl(weights.length);
		WeightTuple weightTemplate = new WeightTuple();
		WeightTuple wt = null;
		double regularize = 0.0, batchCost = 0.0;
		
		//continue until convergence or until master says to stop
		for (;;) {
			
			++t;  // advance to next time step
			data.setMiniBatch();
			System.arraycopy(weights, 0, batchWeights, 0, weights.length);
			
			for( int bt=0; bt < batchtime; bt++) {
				//re-initialize variables
				weightUpdateVbl.zeroize();
				cost.item = 0.0;
				sqr_err.item = 0.0;
				regularize = 0.0;
				batchCost = 0.0;				

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
				for(int i=1; i < weights.length; i++)  {
					regularize += weights[i]*weights[i];
					batchCost += gamma*(batchWeights[i]-weights[i])*(batchWeights[i]-weights[i])/2;
				}
				// update the weight vector using gradient descent with gradient descent
				//don't regularize the intercept term
				batchWeights[0] -= (alpha/data.size())*(weightUpdateVbl.data[0]+gamma*(batchWeights[0]-weights[0]));
				for(int j=1; j < weights.length; j++)
					batchWeights[j] -= (alpha/data.size())*(weightUpdateVbl.data[j]+lambda*batchWeights[j]+gamma*(batchWeights[j]-weights[j]));
			}

			// Send updated summation over training set to the weight update task.
			putTuple(new UpdateTuple(rank, t, batchWeights,
					sqr_err.item, (cost.item+regularize+batchCost), data.size()));

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