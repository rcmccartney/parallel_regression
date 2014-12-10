package parallel_regression;


import java.io.IOException;

import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.pj2.Tuple;

/**
 * Tuple pushed from worker to master node with the testing results
 */
public class ResultsTuple extends Tuple {
		
	public int rank;     // Worker task rank
	public int minClass, maxClass; 
	public int[] confusionMat;

	public ResultsTuple() { }

	public ResultsTuple(int rank, int minClass, int maxClass, int[] confusionMat) {
		this.rank = rank;
		this.minClass = minClass;
		this.maxClass = maxClass;
		this.confusionMat = confusionMat;
	}

	public boolean matchContent(Tuple target) {
		ResultsTuple t = (ResultsTuple) target;
		return this.rank == t.rank;
	}

	public void writeOut(OutStream out) throws IOException {
		out.writeInt(rank);
		out.writeInt(minClass);
		out.writeInt(maxClass);
		out.writeIntArray(confusionMat);
	}

	public void readIn(InStream in) throws IOException	{
		rank = in.readInt();
		minClass = in.readInt();
		maxClass = in.readInt();
		confusionMat = in.readIntArray();
	}
}