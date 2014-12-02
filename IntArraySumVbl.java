package parallel_regression;

import edu.rit.pj2.Vbl;

public class IntArraySumVbl implements Vbl {

	public int[] data;
	
	public IntArraySumVbl(int size) {
		data = new int[size];
	}
	
	public IntArraySumVbl(IntArraySumVbl vbl) {
		data = vbl.data.clone();
	}
	
	public void zeroize() {
		for(int i=0; i < data.length; i++)
			data[i] = 0;
	}

	/**
	 * Make this 4-square set be a copy of the given variable object passed in.
	 * 
	 * @param vbl the 4-square set to copy to this one
	 */
	public void set(Vbl vbl) {
		IntArraySumVbl d = (IntArraySumVbl) vbl;
		System.arraycopy(d.data, 0, this.data, 0, data.length);
	}

	/**
	 * Reduce the given vbl into this one
	 * 
	 * @param vbl the 4-set you are reducing with
	 */
	public void reduce(Vbl vbl) {
		IntArraySumVbl d = (IntArraySumVbl) vbl;
		for(int i = 0; i < d.data.length; i++)
			this.data[i] += d.data[i];
	}
	
	/**
	 * Create a clone of this set
	 * @return Object that is a clone of this
	 */
	public Object clone() {
         return new IntArraySumVbl(this);
	}
}
