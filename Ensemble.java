import java.util.*;
import java.io.File;

public class Ensemble {
	
	private static final int FEATURES = 57;

	public static void main(String[] args) {
		// Parse the sampling size.
		int samples = 1;
		try {
			samples = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Please be sure to include a valid sample size.");
			System.exit(0);
		}

		// Array of String Feature Names.
		String[] featNames = new String[FEATURES];
		populateFeatureNames(featNames);

		// Set of Instance objects for each feature set.
		// Contains class value and array of feature values.
		ArrayList<Instance> train = parseData("./DataSet/train.arff.txt");
		ArrayList<Instance> test = parseData("./DataSet/test.arff.txt");

		// Predict class for each instance in test data.
		Bagging bag = new Bagging(train, featNames, samples, 1);
	    int correct = 0;
		for (Instance instance : test) {
			int label = bag.classify(instance);
			if (label == instance.getLabel()) {
				correct++;
				System.out.println("CLASSIFY POS");
			}
		}
		computeAccuracy(correct, test.size());
	}

	public static void populateFeatureNames(String[] features) {
		for (int i = 0; i < FEATURES; i++) {
			features[i] = "p-" + (i + 1);
		}
	}

	public static ArrayList<Instance> parseData(String fileName) {
		ArrayList<Instance> instances = new ArrayList<Instance>();

		try {
			Scanner sc = new Scanner(new File(fileName));
			// Skip past attribute tags
			while (sc.hasNextLine()) {
				if (sc.nextLine().equals("@data")) {
					break;
				}
			}
			// Parse the actual data and map to proper int values.
			while (sc.hasNextLine()) {
				String[] tokens = sc.nextLine().split(",");
				int label = (tokens[0].equals("+")) ? 1 : 0;
				int[] features = new int[FEATURES];
				for (int i = 1; i < FEATURES; i++) {
					if (tokens[i].equals("a")) {
						features[i-1] = 1;
					} else if (tokens[i].equals("c")) {
						features[i-1] = 2;
					} else if (tokens[i].equals("g")) {
						features[i-1] = 3;
					} else {
						features[i-1] = 4;
					}
				}
				instances.add(new Instance(label, features));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instances;
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
