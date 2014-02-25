import java.util.*;

public class DecisionTree {

	private int features;

	public DecisionTree(int features) {
		this.features = features;
	}

	/**
	* Returns a DTreeNode that holds the attribute name, index and a set of branches to children.
	* 
	* @param pageViews Set of PageView Data to train from
	* @param featNames Array of feature names correspoding to attributes
	* @return A DTreeNode containing the attribute split on and branches to any children
	*/
	public DTreeNode learnTree(Set<Instance> instances, String[] featNames, Set<Integer> testAttr, double thresh) {
		int positive = 0;
		for (Instance instance : instances) {
			if (instance.getLabel() == 1) { positive++; }
		}

		if (positive == instances.size()) {
			return new DTreeNode(1);
		} else if (positive == 0) {
			return new DTreeNode(0);
		} else if (testAttr.size() == this.features) {
			return (positive >= instances.size() - positive) ? new DTreeNode(1) : new DTreeNode(0);
		} else {
			// Compute the attribute containing the maximum information gain.
			int attrIndex = -1;
			double maxGain = -Double.MAX_VALUE;
			for (int i = 0; i < this.features; i++) {
				if (!testAttr.contains(i)) {
					double gain = informationGain(instances, i);
					if (maxGain < gain) {
						maxGain = gain;
						attrIndex = i;
					}
				}
			}

			// Retrieve map of possible values mapped to their subsets.
			// Determine Chi-Square.
			Map<Integer, Set<Instance>> range = computeRange(instances, attrIndex);
			Chi chi = new Chi();
			if (chiSquare(positive, instances.size(), range) <= chi.critchi(thresh, range.keySet().size() - 1)) { 
				return (positive >= instances.size() - positive) ? new DTreeNode(1) : new DTreeNode(0);
			}

			// Create node for attribute we choose to split on.
			// Remove attribute from available list.
			int defaultLabel = (positive >= instances.size() - positive) ? 1 : 0;
			DTreeNode node = new DTreeNode(featNames[attrIndex], attrIndex, defaultLabel, new HashMap<Integer, DTreeNode>());
			testAttr.add(attrIndex);

			// Recursive branching over all possible values for the attribute we are splitting on.
			for (Integer value : range.keySet()) {
				node.getBranches().put(value, learnTree(range.get(value), featNames, new HashSet<Integer>(testAttr), thresh));
				System.out.println("New Level");
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
	public double chiSquare(int positive, int total, Map<Integer, Set<Instance>> range) {
		double sum = 0;
		for (Integer value : range.keySet()) {
			double pPrime = ((double) positive) * range.get(value).size() / total;
			double nPrime = ((double) (total - positive)) * range.get(value).size() / total;
			int pos = 0;
			for (Instance instance : range.get(value)) {
				if (instance.getLabel() == 1) { pos++; }
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
	public double informationGain(Set<Instance> instances, int attributeIndex) {
		double entropyS = entropy(instances);
		double gain = 0;
		Map<Integer, Set<Instance>> values = computeRange(instances, attributeIndex);
		
		// Sum the individual entropies * the weighted fraction for that particular subset.
		for (Integer value : values.keySet()) {
			gain += values.get(value).size() / ((double) instances.size()) * entropy(values.get(value));
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
	public Map<Integer, Set<Instance>> computeRange(Set<Instance> instances, int attributeIndex) {
		Map<Integer, Set<Instance>> values = new HashMap<Integer, Set<Instance>>();
		for (Instance instance : instances) {
			int value = instance.getFeatures()[attributeIndex];
			if (!values.containsKey(value)) {
				values.put(value, new HashSet<Instance>());
			}
			values.get(value).add(instance);
		}
		return values;
	}

	/**
	* Calculates the entropy of a given collection
	*
	* @param pageViews Collection to calculate entropy of.
	* @return Returns a double reprenting the entropy value.
	*/
	public double entropy(Set<Instance> instances) {
		int tot = instances.size();
		int pos = 0;
		for (Instance p : instances) {
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
	public int predictTree(Instance instance, DTreeNode root) {
		if (root.getBranches() == null) {
			return root.getLabel();
		}
		int value = instance.getFeatures()[root.getIndex()];
		if (root.getBranches().get(value) == null) {
			return root.getDefaultLabel();
		}
		return predictTree(instance, root.getBranches().get(value));
	}
}
