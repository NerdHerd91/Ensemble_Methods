import java.util.*;
import java.io.File;

public class Bagging {
	
	public static void main(String[] args) {
		// Parse the sampling size.
		double samples = 1;
		try {
			thresh = Integer.parseInteger(args[0]);
		} catch (Exception e) {
			System.out.println("Please be sure to include a valid sample size.");
			System.exit(0);
		}

		// Array of String Feature Names.
		String[] featNames = new String[FEATURES];

		// Set of Instance objects for each feature set.
		// Contains class value and array of feature values.
		Set<Instance> train = parseData("./DataSet/train.arff.txt");
		Set<Instance> test = parseData("./DataSet/test.arff.txt");

		// Predict class for each instance in test data.
		SampleReplace sr = new SampleReplace(train, features, samples, 1);
	    int correct = 0;
		for (Instance instance : test) {
			int label = sr.classify(instance);
			if (label == instance.getLabel()) {
				correct++;
			}
		}
		computeAccuracy(correct, test.size());
	}
	
	/**
	* Computes how many predictions were correct against the test-data ouput,
	* Prints to the console the number of matches and the overall percent accuracy.
	*
	* @param correct Number of correct predictions.
	* @param total Total number of predictions made.
	*/
	public static void computeAccuracy(int correct, int total) {
		System.out.println("Test-Data Prediciton Statistics");
		System.out.println("-------------------------------");
		System.out.printf("Matches: %d\n", correct);
		System.out.printf("Accuracy of Data: %.2f%%\n", 100.0 * correct / total); 
	}
}
