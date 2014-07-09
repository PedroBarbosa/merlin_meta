package pt.uminho.sysbio.merge.databases.containers;

public class HomologyDataTableContainer {

	private String locusID, definition, product, organelle;
	private double calculated_mw;
	private int uniprot_star;


	/**
	 * @param locusID
	 * @param definition
	 * @param product
	 * @param organelle
	 * @param calculated_mw
	 * @param uniprot_star
	 * @param org
	 */
	public HomologyDataTableContainer(String locusID, String definition, double calculated_mw, String product,
			String organelle, int uniprot_star) {
		super();
		this.locusID = locusID;
		this.definition = definition;
		this.product = product;
		this.organelle = organelle;
		this.calculated_mw = calculated_mw;
		this.setUniprot_star(uniprot_star);
		
	}
	
	/**
	 * @return the locusID
	 */
	public String getLocusID() {
		return locusID;
	}
	/**
	 * @param locusID the locusID to set
	 */
	public void setLocusID(String locusID) {
		this.locusID = locusID;
	}
	/**
	 * @return the definition
	 */
	public String getDefinition() {
		return definition;
	}
	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}
	/**
	 * @return the product
	 */
	public String getProduct() {
		return product;
	}
	/**
	 * @param product the product to set
	 */
	public void setProduct(String product) {
		this.product = product;
	}
	/**
	 * @return the organelle
	 */
	public String getOrganelle() {
		return organelle;
	}
	/**
	 * @param organelle the organelle to set
	 */
	public void setOrganelle(String organelle) {
		this.organelle = organelle;
	}
	/**
	 * @return the calculated_mw
	 */
	public double getCalculated_mw() {
		return calculated_mw;
	}
	/**
	 * @param calculated_mw the calculated_mw to set
	 */
	public void setCalculated_mw(double calculated_mw) {
		this.calculated_mw = calculated_mw;
	}

	/**
	 * @return the uniprot_star
	 */
	public int getUniprot_star() {
		return uniprot_star;
	}

	/**
	 * @param uniprot_star the uniprot_star to set
	 */
	public void setUniprot_star(int uniprot_star) {
		this.uniprot_star = uniprot_star;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HomologyDataTableContainer [locusID=" + locusID
				+ ", definition=" + definition + ", product=" + product
				+ ", organelle=" + organelle + ", calculated_mw="
				+ calculated_mw + ", uniprot_star=" + uniprot_star + "]";
	}



	

}
