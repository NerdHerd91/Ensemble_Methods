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

	public int classify(Instance instance) {
		// Create samplings and predict from decision trees
		DecisionTree dt = new DecisionTree(this.features.length);
		int positive = 0;
		for (DTreeNode tree : this.trees) {
			if (dt.predictTree(instance, tree) == 1) {
				positive++;
				System.out.println("YAY");
			}
		}
		return Math.max(positive, trees.size() - positive);
	}

	private void buildTrees() {
		DecisionTree dt = new DecisionTree(this.features.length);
		for (int i = 0; i < this.samples; i++) {
			Set<Instance> randomSet = createRandomSet();
			System.out.println("New Tree");
			trees.add(dt.learnTree(randomSet, this.features, new HashSet<Integer>(), this.thresh));
		}
	}

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
