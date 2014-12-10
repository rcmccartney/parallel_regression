package parallel_regression;


import java.io.FileNotFoundException;

public interface DataStrategy {

	public int getMinClass();
	
	public int getMaxClass();

	public int getWidth();
	
	public int size();
	
	public void prepareForTesting() throws NumberFormatException, FileNotFoundException;
	
	public int getCorrectClass(int i);
	
	public double getEstimate(double[] weights, int i);

	public void addPartials(double[] weightUpdate, double error, int i);

	public void addPartials(double[] weightUpdate, int j, double error, int i);
	
	public void setMiniBatch();
}
