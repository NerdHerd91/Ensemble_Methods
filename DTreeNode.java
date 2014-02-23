import java.util.*;

public class DTreeNode {
	private String attributeName;
	private int attributeIndex;
	private int label;
	private int defaultLabel;
	private Map<Integer, DTreeNode> branches;

	public DTreeNode(String attributeName, int attributeIndex, int defaultLabel, Map<Integer, DTreeNode> branches) {
		this.attributeName = attributeName;
		this.attributeIndex = attributeIndex;
		this.defaultLabel = defaultLabel;
		this.branches = branches;
	}

	public DTreeNode(int label) {
		this.label = label;
		this.branches = null;
	}

	public String getName() {
		return this.attributeName;
	}

	public int getIndex() {
		return this.attributeIndex;
	}

	public int getLabel() {
		return this.label;
	}

	public int getDefaultLabel() {
		return this.defaultLabel;
	}

	public Map<Integer, DTreeNode> getBranches() {
		return this.branches;
	}
}
