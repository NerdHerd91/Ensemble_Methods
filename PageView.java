public class PageView {
	
	private int label;
	private int[] features;

	public PageView(int label, int[] features) {
		this.label = label;
		this.features = features;
	}

	public int getLabel() {
		return this.label;
	}

	public int[] getFeatures() {
		return this.features;
	}
}
