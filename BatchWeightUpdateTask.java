package parallel_regression;

import java.io.IOException;
import java.util.ArrayList;

import edu.rit.pj2.IntVbl;
import edu.rit.pj2.Task;

/**
 * WeightUpdateTask task class.
 * Regularization occurs here
 */
public class BatchWeightUpdateTask extends Task {
			
	int K, size, steps, snap;
	double eps;
	double[] weights;
	ArrayList<Double> cost, sqr_err;
	
	// Task main program.
	public void main(String[] args) throws Exception {
		
		// Parse command line arguments.
		eps = Double.parseDouble (args[0]);
		steps = Integer.parseInt (args[1]);
		snap = Integer.parseInt (args[2]);
		K = Integer.parseInt (args[3]);

		// get the starting weights from one of the nodes
		IntVbl tuple = new IntVbl();
		IntVbl wt = null;
		wt = takeTuple(tuple);
		weights = new double[wt.item];
		cost = new ArrayList<>();
		sqr_err = new ArrayList<>();
		
		long t = 0;
		UpdateTuple template = new UpdateTuple();
		UpdateTuple ut = null;
		double log_error_sum, delta, sse;
		long time = System.currentTimeMillis();
		
		for (;;) {
			// Advance to next time step.
			++t;
			//reinitialize variables
			log_error_sum = 0.0;
			sse = 0.0;
			size = 0;
			for(int i=0; i < weights.length; i++)
				weights[i] = 0.0;
			
			// Receive summations from the worker nodes
			template.step = t;
			for (int i = 0; i < K; ++ i) {
				template.rank = i;
				ut = takeTuple(template);
				log_error_sum += ut.partialCost;
				sse += ut.partialSSE;
				size += ut.chunkSize;
				for(int j=0; j < weights.length; j++)
					weights[j] += ut.weightUpdate[j];
			}
			
			//average the weights to be sent back
			for(int j=0; j < weights.length; j++)
				weights[j] /= K;
			
			// cost + regularization 
			cost.add(log_error_sum/size);  
			sqr_err.add(sse/size); 
			if (cost.size() > 1)  // delta is a percent change
				delta = (cost.get(cost.size()-1)-cost.get(cost.size()-2)) / cost.get(cost.size()-2);
			else
				delta = eps + 1; // make sure delta is > epsilon on first iteration
			
			if (snap > 0 && t % snap == 0)
				snapshot(t);
			// Stop when delta is less than convergence threshold
			// Need to inform worker nodes so they can give final results
			if ((steps == 0 && Math.abs(delta) < eps) || (steps != 0 && t == steps)) {
				putTuple(K, new WeightTuple(true, t, weights));
				break;
			}
			else
				putTuple(K, new WeightTuple(false, t, weights));
		}
		System.out.printf("Logistic regression took %.3f seconds in %d iterations%n",  
				(System.currentTimeMillis()-time)/1000.0, t );
		System.out.flush();
		displayResults();
	}
	
	public void snapshot(long t) {
		System.out.printf("Iter %d: Cost %4.4f Sq Error %4.4f%n", t, 
				cost.get(cost.size()-1), sqr_err.get(sqr_err.size()-1));
		printWeights();
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
	
	public void displayResults() throws IOException {
	
		System.out.printf("Init:\tCost %4.4f Sq Error %4.4f%n", cost.get(0), sqr_err.get(0));
		System.out.printf("Final:\tCost %4.4f Sq Error %4.4f%n", 
				cost.get(cost.size()-1), sqr_err.get(sqr_err.size()-1) );
		printWeights();
		System.out.flush();

		long time = System.currentTimeMillis();
		//set up tuples & reduction variables to get results from workers
		ResultsTuple template = new ResultsTuple();
		ResultsTuple rt;
		//this is the final confusion matrix to be printed
		int[] confusion = new int[4];
		int minClass = 0, maxClass=1;
		
		//get the data min/max class labels and reduce over tuple space
		for (int i = 0; i < K; ++ i) {
			template.rank = i;
			rt = takeTuple(template);
			if (rt.rank == 0) { //only need to set these once
				minClass = rt.minClass;  
				maxClass = rt.maxClass;
			}
			for(int j=0; j < 4; j++)
				confusion[j] += rt.confusionMat[j];
		}
		System.out.printf("Classification took %.3f seconds%n", 
				(System.currentTimeMillis()-time)/1000.0 );
		System.out.flush();

		System.out.printf("    %8d %8d", minClass, maxClass);
		for(int i=0; i<2; i++) {
			System.out.printf("%n%3d", (i==0?minClass:maxClass));
			for(int j=0; j<2; j++)
				System.out.printf(" %8d", confusion[2*i+j]);
		}
		
		// Print accuracy and precision and recall
		System.out.printf("%nAccuracy: %3.3f%%%n", 100*(confusion[0]+confusion[3]) / 
				((double)confusion[0]+confusion[1]+confusion[2]+confusion[3]));
		System.out.printf("Precision: %3.3f%%%n", 100*((double)confusion[3])/(confusion[3]+confusion[1]));
		System.out.printf("Recall: %3.3f%%%n", 100*((double)confusion[3])/(confusion[3]+confusion[2]));

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
}