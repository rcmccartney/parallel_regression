package parallel_regression;


import java.io.IOException;

import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.pj2.Tuple;

/**
 * Tuple pushed from worker to master node with the results
 * of one epoch over the training examples assigned to this node
 */
public class UpdateTuple extends Tuple {
		
	public int rank;     // Worker task rank
	public long step;     // Time step number
	public double[] weightUpdate; // summation over training examples
	public double partialSSE;  // sqr error summation for this worker
	public double partialCost;  //logistic regression cost summation
	public long chunkSize;  // how many training examples this node used

	public UpdateTuple() { }

	public UpdateTuple(int rank, long step, double[] weightUpdate,
			double partialSSE, double partialCost, long chunkSize) {
		
		this.rank = rank;
		this.step = step;
		this.weightUpdate = weightUpdate;
		this.partialSSE = partialSSE;
		this.partialCost = partialCost;
		this.chunkSize = chunkSize;
	}

	public boolean matchContent(Tuple target) {
		UpdateTuple t = (UpdateTuple) target;
		return this.rank == t.rank && this.step == t.step;
	}

	public void writeOut(OutStream out) throws IOException {
		out.writeInt(rank);
		out.writeLong(step);
		out.writeLong(chunkSize);
		out.writeDouble(partialSSE);
		out.writeDouble(partialCost);
		out.writeDoubleArray(weightUpdate);
	}

	public void readIn(InStream in) throws IOException	{
		rank = in.readInt();
		step = in.readLong();
		chunkSize = in.readLong();
		partialSSE = in.readDouble();
		partialCost = in.readDouble();
		weightUpdate = in.readDoubleArray();
	}
}