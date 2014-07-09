package pt.uminho.sysbio.merge.databases.containers;
import java.util.List;

/**
 * 
 */

/**
 * @author pedro
 *
 */
public class GeneBlast {
	
	private String locusTag, query, gene, chromossome, organelle, status, uniprot_ecnumber;
	private int uniprot_star;
	private List<Homology> homology;
	private FastaSequence sequence;
	private List<ProductRank> product_rank;
	private List<EcNumberRank> ecNumber_rank;
	
	
	public GeneBlast(String locusTag, String query, String gene,
			String chromossome, String organelle, String status,
			String uniprot_ecnumber, int uniprot_star) {
		super();
		this.locusTag = locusTag;
		this.query = query;
		this.gene = gene;
		this.chromossome = chromossome;
		this.organelle = organelle;
		this.status = status;
		this.uniprot_ecnumber = uniprot_ecnumber;
		this.setUniprot_star(uniprot_star);
	}
	
	
	public List<Homology> getHomology() {
		return homology;
	}
	public void setHomology(List<Homology> homology) {
		this.homology = homology;
	}
	public FastaSequence getSequence() {
		return sequence;
	}
	public void setSequence(FastaSequence sequence) {
		this.sequence = sequence;
	}
	public List<ProductRank> getProduct_rank() {
		return product_rank;
	}
	public void setProduct_rank(List<ProductRank> product_rank) {
		this.product_rank = product_rank;
	}
	public List<EcNumberRank> getEcNumber_rank() {
		return ecNumber_rank;
	}
	public void setEcNumber_rank(List<EcNumberRank> ecNumber_rank) {
		this.ecNumber_rank = ecNumber_rank;
	}
	public String getLocusTag() {
		return locusTag;
	}
	public void setLocusTag(String locusTag) {
		this.locusTag = locusTag;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getGene() {
		return gene;
	}
	public void setGene(String gene) {
		this.gene = gene;
	}
	public String getChromossome() {
		return chromossome;
	}
	public void setChromossome(String chromossome) {
		this.chromossome = chromossome;
	}
	public String getOrganelle() {
		return organelle;
	}
	public void setOrganelle(String organelle) {
		this.organelle = organelle;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getUniprot_ecnumber() {
		return uniprot_ecnumber;
	}
	public void setUniprot_ecnumber(String uniprot_ecnumber) {
		this.uniprot_ecnumber = uniprot_ecnumber;
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