import java.util.*;

public class SampleReplace {
	
	private ArrayList<DTreeNode> trees;

	public SampleReplace(Set<Instance> train, int[] features, int samples, int thresh) {
		this.trees = new ArrayList<DTreeNode>();
		this.buildTrees(train, features, samples, thresh);
	}

	public int classify(Instance instance) {
		// Create samplings and predict from decision trees
		return 0;
	}

	private void buildTrees(Set<Instance> train, int[] features, int samples, int thresh) {
		
	}
}
