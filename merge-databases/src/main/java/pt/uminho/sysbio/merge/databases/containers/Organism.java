package pt.uminho.sysbio.merge.databases.containers;

/**
 * @author pedro
 *
 */
public class Organism {
	
	private String organism, taxonomy;
	private int taxRank;
	
	
	/**
	 * @param organism
	 * @param taxonomy
	 * @param taxRank
	 */
	public Organism(String organism, String taxonomy, int taxRank) {
		super();
		this.organism = organism;
		this.taxonomy = taxonomy;
		this.taxRank = taxRank;
	}
	/**
	 * 
	 * @return the organism
	 */
	public String getOrganism() {
		return organism;
	}
	/**
	 * @param organism the organism to set
	 */
	public void setOrganism(String organism) {
		this.organism = organism;
	}
	/**
	 * @return the taxonomy
	 */
	public String getTaxonomy() {
		return taxonomy;
	}
	/**
	 * @param taxonomy the taxonomy to set
	 */
	public void setTaxonomy(String taxonomy) {
		this.taxonomy = taxonomy;
	}
	/**
	 * @return the taxRank
	 */
	public int getTaxRank() {
		return taxRank;
	}
	/**
	 * @param taxRank the taxRank to set
	 */
	public void setTaxRank(int taxRank) {
		this.taxRank = taxRank;
	}
}
