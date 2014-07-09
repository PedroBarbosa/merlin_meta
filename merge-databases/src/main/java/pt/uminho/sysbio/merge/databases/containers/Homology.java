package pt.uminho.sysbio.merge.databases.containers;

import java.util.List;

public class Homology {
	
	private String referenceID, gene;
	private double eValue, bits;
	private Organism org;
	private HomologyDataTableContainer homologyData;
	private List<EcNumber> ecNumber;
	
	
	
	/**
	 * 
	 * 
	 * @param referenceID
	 * @param gene
	 * @param bits
	 * @param eValue
	 */
	public Homology(String referenceID, String gene, double eValue, double bits ) {
		super();
		this.referenceID = referenceID;
		this.gene = gene;
		this.bits = bits;
		this.eValue = eValue;

	}


	/**
	 * @return the referenceID
	 */
	public String getReferenceID() {
		return referenceID;
	}


	/**
	 * @param referenceID the referenceID to set
	 */
	public void setReferenceID(String referenceID) {
		this.referenceID = referenceID;
	}


	/**
	 * @return the gene
	 */
	public String getGene() {
		return gene;
	}


	/**
	 * @param gene the gene to set
	 */
	public void setGene(String gene) {
		this.gene = gene;
	}


	/**
	 * @return the bits
	 */
	public double getBits() {
		return bits;
	}


	/**
	 * @param bits the bits to set
	 */
	public void setBits(double bits) {
		this.bits = bits;
	}


	/**
	 * @return the eValue
	 */
	public double geteValue() {
		return eValue;
	}


	/**
	 * @param eValue the eValue to set
	 */
	public void seteValue(double eValue) {
		this.eValue = eValue;
	}


	/**
	 * @return the org
	 */
	public Organism getOrg() {
		return org;
	}


	/**
	 * @param org the org to set
	 */
	public void setOrg(Organism org) {
		this.org = org;
	}


	/**
	 * @return the homologyData
	 */
	public HomologyDataTableContainer getHomologyData() {
		return homologyData;
	}


	/**
	 * @param homologyData the homologyData to set
	 */
	public void setHomologyData(HomologyDataTableContainer homologyData) {
		this.homologyData = homologyData;
	}


	/**
	 * @return the ecNumber
	 */
	public List<EcNumber> getEcNumber() {
		return ecNumber;
	}


	/**
	 * @param ecNumber the ecNumber to set
	 */
	public void setEcNumber(List<EcNumber> ecNumber) {
		this.ecNumber = ecNumber;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Homology [referenceID=" + referenceID + ", gene=" + gene
				+ ", eValue=" + eValue + ", bits=" + bits + ", org=" + org
				+ ", homologyData=" + homologyData + ", ecNumber=" + ecNumber
				+ "]";
	}


	
	


}
