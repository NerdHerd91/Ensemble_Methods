import java.util.*;
import java.io.File;
import java.io.PrintWriter;

public class Clickstream {
	
	public static final int FEATURES = 274;
	public static int nodeCount = 0;

	public static void main(String[] args) {
		// Parse the pValue threshhold we will use.
		double thresh = 1;
		try {
			thresh = Double.parseDouble(args[0]);
		} catch (Exception e) {
			System.out.println("Please be sure to include a valid threshhold.");
			System.exit(0);
		}

		// Array of String Feature Names.
		String[] featNames = new String[FEATURES];

		// Set of PageView objects for each feature set.
		// Contains class value and array of feature values.
		Set<PageView> trainFeat = new HashSet<PageView>();
		Set<PageView> testFeat = new HashSet<PageView>();

		// Parse the data for each dataset.
		try {
			Scanner sc = new Scanner(new File("./DataSet/featnames.csv"));
			int index = 0;
			while (sc.hasNextLine()) {
				featNames[index] = sc.nextLine().trim();
				index++;
			}
			sc.close();

			buildDataMaps(trainFeat, "./DataSet/trainfeat.csv", "./DataSet/trainlabs.csv");
			buildDataMaps(testFeat, "./DataSet/testfeat.csv", "./DataSet/testlabs.csv");
		} catch (Exception e) {
			System.out.println(e);
		}

		// Build Decision Tree using training data
		DTreeNode root = learnTree(trainFeat, featNames, new HashSet<Integer>(), thresh);
		
		// Predict class for test data
		ArrayList<Integer> labels = new ArrayList<Integer>();
		try {
			PrintWriter writer = new PrintWriter("clickstream_results.txt", "UTF-8");
			for (PageView pageView : testFeat) {
				int label = predictTree(pageView, root);
				labels.add(label);
				writer.println(label);
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Calculate the accuracy for the test data
		System.out.println("Test-Data Prediciton Statistics");
		System.out.println("-------------------------------");
		System.out.printf("Tree size: %d\n", nodeCount);
		computeAccuracy(testFeat, labels);
	}
	
	/**
	* Computes how many predictions were correct against the test-data ouput,
	* Prints to the console the number of matches and the overall percent accuracy.
	*
	* @param pageViews The set of test-data PageView objects
	* @param labels An ArrayList containing the predicted output data
	*/
	public static void computeAccuracy(Set<PageView> pageViews, ArrayList<Integer> labels) {
		int correct = 0;
		int index = 0;
		for (PageView pageView : pageViews) {
			if (pageView.getLabel() == labels.get(index)) {
				correct++;
			}
			index++;
		}
		System.out.printf("Matches: %d\n", correct);
		System.out.printf("Accuracy of Data: %.2f%%\n", 100.0 * correct / labels.size()); 
	}

	/**
	* Parses a file containing features and a file containing the class output data,
	* For each example, creates a PageView object and adds it to the set.
	*
	* @param pageViews Reference to the set to place PageView we create into.
	* @param featurePath File path to the file containing features.
	* @param labPath File path to the file containing output classes.
	*/
	public static void buildDataMaps(Set<PageView> pageViews, String featurePath, String labPath) {
		try {
			Scanner data = new Scanner(new File(featurePath));
			Scanner labs = new Scanner(new File(labPath));

			while (labs.hasNextInt() && data.hasNextLine()) {
				int label = labs.nextInt();
				String dataLine = data.nextLine();
				Scanner sc = new Scanner(dataLine);
				
				int[] features = new int[FEATURES];
				int index = 0;

				while (sc.hasNextInt()) {
					features[index] = sc.nextInt();
					index++;
				}
				
				pageViews.add(new PageView(label, features));
				sc.close();
			}
			data.close();
			labs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	* Returns a DTreeNode that holds the attribute name, index and a set of branches to children.
	* 
	* @param pageViews Set of PageView Data to train from
	* @param featNames Array of feature names correspoding to attributes
	* @return A DTreeNode containing the attribute split on and branches to any children
	*/
	public static DTreeNode learnTree(Set<PageView> pageViews, String[] featNames, Set<Integer> testAttr, double thresh) {
		nodeCount++;
		int positive = 0;
		for (PageView pageView : pageViews) {
			if (pageView.getLabel() == 1) { positive++; }
		}

		if (positive == pageViews.size()) {
			return new DTreeNode(1);
		} else if (positive == 0) {
			return new DTreeNode(0);
		} else if (testAttr.size() == FEATURES) {
			return (positive >= pageViews.size() - positive) ? new DTreeNode(1) : new DTreeNode(0);
		} else {
			// Compute the attribute containing the maximum information gain.
			int attrIndex = -1;
			double maxGain = -Double.MAX_VALUE;
			for (int i = 0; i < FEATURES; i++) {
				if (!testAttr.contains(i)) {
					double gain = informationGain(pageViews, i);
					if (maxGain < gain) {
						maxGain = gain;
						attrIndex = i;
					}
				}
			}

			// Retrieve map of possible values mapped to their subsets.
			// Determine Chi-Square.
			Map<Integer, Set<PageView>> range = computeRange(pageViews, attrIndex);
			Chi chi = new Chi();
			if (chiSquare(positive, pageViews.size(), range) <= chi.critchi(thresh, range.keySet().size() - 1)) { 
				return (positive >= pageViews.size() - positive) ? new DTreeNode(1) : new DTreeNode(0);
			}

			// Create node for attribute we choose to split on.
			// Remove attribute from available list.
			int defaultLabel = (positive >= pageViews.size() - positive) ? 1 : 0;
			DTreeNode node = new DTreeNode(featNames[attrIndex], attrIndex, defaultLabel, new HashMap<Integer, DTreeNode>());
			testAttr.add(attrIndex);

			// Recursive branching over all possible values for the attribute we are splitting on.
			for (Integer value : range.keySet()) {
				node.getBranches().put(value, learnTree(range.get(value), featNames, new HashSet<Integer>(testAttr), thresh));
			}
			return node;
		}
	}

	/**
	* Returns a double to compare against the threshhold.
	*
	* @param positive Total number of positives in the currect set to split on.
	* @param total Total number of examples in the current set.
	* @param range Map of attribute values to subset of examples for each.
	* @return Returns a double indicating Chi-Square value to compare against.
	*/
	public static double chiSquare(int positive, int total, Map<Integer, Set<PageView>> range) {
		double sum = 0;
		for (Integer value : range.keySet()) {
			double pPrime = ((double) positive) * range.get(value).size() / total;
			double nPrime = ((double) (total - positive)) * range.get(value).size() / total;
			int pos = 0;
			for (PageView pageView : range.get(value)) {
				if ( pageView.getLabel() == 1) { pos++; }
			}
			sum += (Math.pow(pPrime - pos, 2) / pPrime) + (Math.pow(nPrime - (range.get(value).size() - pos), 2) / nPrime);
		}
		return sum;
	}

	/**
	* Computes the information gain for a particular attribute to split on.
	*
	* @param pageViews Accepts a set of pageview objects.
	* @param attributeIndex Represents the index of the attribute we wish to split on.
	* @return Returns the information gain for this particular attribute split.
	*/
	public static double informationGain(Set<PageView> pageViews, int attributeIndex) {
		double entropyS = entropy(pageViews);
		double gain = 0;
		Map<Integer, Set<PageView>> values = computeRange(pageViews, attributeIndex);
		
		// Sum the individual entropies * the weighted fraction for that particular subset.
		for (Integer value : values.keySet()) {
			gain += values.get(value).size() / ((double) pageViews.size()) * entropy(values.get(value));
		}
		return entropyS - gain;		
	}

	/**
	* Returns a Mapping of values to subsets of PageViews corresponding to each value.
	*
	* @param pageViews Current set of available examples.
	* @param attributeIndex Index of the attribute we wish to split on.
	* @return Returns a Map of integer to Set<PageView>.
	*/
	public static Map<Integer, Set<PageView>> computeRange(Set<PageView> pageViews, int attributeIndex) {
		Map<Integer, Set<PageView>> values = new HashMap<Integer, Set<PageView>>();
		for (PageView pageView : pageViews) {
			int value = pageView.getFeatures()[attributeIndex];
			if (!values.containsKey(value)) {
				values.put(value, new HashSet<PageView>());
			}
			values.get(value).add(pageView);
		}
		return values;
	}

	/**
	* Calculates the entropy of a given collection
	*
	* @param pageViews Collection to calculate entropy of.
	* @return Returns a double reprenting the entropy value.
	*/
	public static double entropy(Set<PageView> pageViews) {
		int tot = pageViews.size();
		int pos = 0;
		for (PageView p : pageViews) {
			if (p.getLabel() == 1) { pos++; }
		}
		if (pos == 0 || pos == tot) { return 0; }
		double pProp = (-1.0 * pos / tot) * Math.log(1.0 * pos / tot) / Math.log(2);
		double nProp = (1.0 * (tot - pos) / tot) * Math.log(1.0 * (tot - pos) / tot) / Math.log(2);
		return pProp - nProp;
	}

	/**
	* Predicts the class values for a dataset of PageView objects,
	*
	* @param pageViews Set of PageView Data to train from
	* @param root DTreeNode root for the decision tree built using the training data
	* @return Returns the class value prediction
	*/
	public static int predictTree(PageView pageView, DTreeNode root) {
		if (root.getBranches() == null) {
			return root.getLabel();
		}
		int value = pageView.getFeatures()[root.getIndex()];
		if (root.getBranches().get(value) == null) {
			return root.getDefaultLabel();
		}
		return predictTree(pageView, root.getBranches().get(value));
	}
}
