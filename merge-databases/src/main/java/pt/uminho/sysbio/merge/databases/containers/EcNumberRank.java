package pt.uminho.sysbio.merge.databases.containers;
import java.util.List;


public class EcNumberRank {
	private String ecNumber;
	private int rank;
	private List<Organism> org;
	
	
	public EcNumberRank(String ecNumber, int rank) {
		super();
		this.ecNumber = ecNumber;
		this.rank = rank;
		
	}
	/**
	 * @return the ecNumber
	 */
	public String getEcNumber() {
		return ecNumber;
	}
	/**
	 * @param ecNumber the ecNumber to set
	 */
	public void setEcNumber(String ecNumber) {
		this.ecNumber = ecNumber;
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
	public List<Organism> getOrg() {
		return org;
	}
	/**
	 * @param org the org to set
	 */
	public void setOrg(List<Organism> org) {
		this.org = org;
	}

}
