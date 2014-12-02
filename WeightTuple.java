package parallel_regression;

import java.io.IOException;

import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.pj2.Tuple;

/**
 * Tuple that is sent from the master to the slaves with the
 * updated weights for the next epoch of regression
 */
public class WeightTuple extends Tuple {
	
	public boolean stop; // Stop command 
	public long step;     // Time step number
	public double[] weights; // new weights after gradient descent

	public WeightTuple() { }

	public WeightTuple(boolean stop, long step, double[] weights) {
		this.stop = stop;
		this.step = step;
		this.weights = weights;	
	}

	//criteria for knowing this is the right tuple to take
	// based only on the time step, since all workers need to 
	// take the same tuple
	public boolean matchContent(Tuple target) {
		WeightTuple t = (WeightTuple) target;
		return this.step == t.step;
	}

	public void writeOut(OutStream out) throws IOException {
		out.writeBoolean(stop);
		out.writeLong(step);
		out.writeDoubleArray(weights);
	}

	public void readIn(InStream in) throws IOException {
		stop = in.readBoolean();
		step = in.readLong();
		weights = in.readDoubleArray();
	}
}