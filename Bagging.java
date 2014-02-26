import java.util.*;

public class Bagging {
	
	private ArrayList<Instance> train;
	private String[] features;
	private int samples;
	private int thresh;
	private Set<DTreeNode> trees;

	public Bagging(ArrayList<Instance> train, String[] features, int samples, int thresh) {
		this.train = train;
		this.features = features;
		this.samples = samples;
		this.thresh = thresh;
		this.trees = new HashSet<DTreeNode>();
		this.buildTrees();
	}

	/**
	* Predicts the classification for the Instance passed in.
	*
	* @param instance Instance object of the data point we wish to predict.
	* @return Integer representing the classification predicted.
	*/
	public int classify(Instance instance) {
		// Create samplings and predict from decision trees
		DecisionTree dt = new DecisionTree(this.features.length);
		int positive = 0;
		for (DTreeNode tree : this.trees) {
			if (dt.predictTree(instance, tree) == 1) {
				positive++;
			}
		}
		return (positive >= trees.size() - positive) ? 1 : 0;
	}

	/**
	* Creates decision trees equal to the number of samples requested,
	* and stores them in a list to be used for prediction; Where each tree is
	* trained using a random replicated dataset from the original training data.
	*/
	private void buildTrees() {
		DecisionTree dt = new DecisionTree(this.features.length);
		for (int i = 0; i < this.samples; i++) {
			Set<Instance> randomSet = createRandomSet();
			trees.add(dt.learnTree(randomSet, this.features, new HashSet<Integer>(), this.thresh));
		}
	}

	/**
	* Creates a random Set of Instance objects from the original training data list,
	* using a sample with replacement technique.
	*
	* @return Returns a Set of Instance objects.
	*/
	private Set<Instance> createRandomSet() {
		Set<Instance> instances = new HashSet<Instance>();
		Random rnd = new Random();
		for (int i = 0; i < this.train.size(); i++) {
			int index = rnd.nextInt(train.size());
			instances.add(new Instance(train.get(index).getLabel(),train.get(index).getFeatures()));
		}
		return instances;
	}
}
