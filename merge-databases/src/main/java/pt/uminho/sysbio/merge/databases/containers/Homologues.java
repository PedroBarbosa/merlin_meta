package pt.uminho.sysbio.merge.databases.containers;
import java.util.List;

/**
 * @author pedro
 *
 */
public class Homologues {
	private String locusID, definition, product, organelle, referenceID, gene;
	private double calculated_mw, evalue, score;
	private int uniprot_star;
	private Organism org;
	private List<EcNumber> ecNumber;



	public Homologues(String locusID, String definition, double calculated_mw,
			String product,String organelle, int uniprot_star) {
		super();
		this.locusID = locusID;
		this.definition = definition;
		this.product = product;
		this.organelle = organelle;
		this.calculated_mw = calculated_mw;
		this.setUniprot_star(uniprot_star);

	}


	/**
	 * @param locusID
	 * @param definition
	 * @param product
	 * @param organelle
	 * @param calculated_mw
	 * @param uniprot_star
	 */
	public Homologues(String locusID, String definition, double calculated_mw,
			String product,String organelle, int uniprot_star, String referenceID, String gene, double evalue, double score) {
		super();
		this.locusID = locusID;
		this.definition = definition;
		this.product = product;
		this.organelle = organelle;
		this.calculated_mw = calculated_mw;
		this.setUniprot_star(uniprot_star);

		this.referenceID = referenceID;
		this.gene = gene;
		this.evalue = evalue;
		this.score = score;
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
	 * @return the evalue
	 */
	public double getEvalue() {
		return evalue;
	}
	/**
	 * @param evalue the evalue to set
	 */
	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}
	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}
	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
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
}