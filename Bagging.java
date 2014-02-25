import java.util.*;

public class Bagging {
	
	private ArrayList<DTreeNode> trees;

	public Bagging(ArrayList<Instance> train, String[] features, int samples, int thresh) {
		this.trees = new ArrayList<DTreeNode>();
		this.buildTrees(train, features, samples, thresh);
	}

	public int classify(Instance instance) {
		// Create samplings and predict from decision trees
		return 0;
	}

	private void buildTrees(ArrayList<Instance> train, String[] features, int samples, int thresh) {
		
	}
}
