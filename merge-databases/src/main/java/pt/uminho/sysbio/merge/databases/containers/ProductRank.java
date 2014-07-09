package pt.uminho.sysbio.merge.databases.containers;
import java.util.List;



public class ProductRank {

	private String productName;
	private int rank;
	private List<Organism> org;
	
	
	public ProductRank(String productName, int rank	) {
		super();
		this.productName = productName;
		this.rank = rank;
		
	}
	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}
	/**
	 * @param productName the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}
	/**
	 * @return the rank
	 */
	public int getRank() {
		return rank;
	}
	/**
	 * @param rank the rank to set
	 */
	public void setRank(int rank) {
		this.rank = rank;
	}
	/**
	 * @return the org
	 */
	public List<Organism> getOrgList() {
		return org;
	}
	/**
	 * @param org the org to set
	 */
	public void setOrg(List<Organism> org) {
		this.org = org;
	}

	
	
}
