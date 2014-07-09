package pt.uminho.sysbio.merge.databases.containers;
import java.util.List;

/**
 * @author pedro
 *
 */
public class GeneHomology {

		private String locusTag, query, gene, chromossome, organelle, status, uniprot_ecnumber;
		private int uniprot_star;
		private List<Homologues> homologues;
		private FastaSequence sequence;
		private List<ProductRank> productRank;
		private List<EcNumberRank> ecnumberRank;
		
		public GeneHomology(String locusTag, String query, String gene,
				String chromossome, String organelle, int uniprot_star,String status,
				String uniprot_ecnumber) {
			super();
			this.locusTag = locusTag;
			this.query = query;
			this.gene = gene;
			this.chromossome = chromossome;
			this.organelle = organelle;
			this.status = status;
			this.uniprot_ecnumber = uniprot_ecnumber;
			this.uniprot_star = uniprot_star;
		}
		
		/**
		 * @return the locusTag
		 */
		public String getLocusTag() {
			return locusTag;
		}



		/**
		 * @param locusTag the locusTag to set
		 */
		public void setLocusTag(String locusTag) {
			this.locusTag = locusTag;
		}



		/**
		 * @return the query
		 */
		public String getQuery() {
			return query;
		}



		/**
		 * @param query the query to set
		 */
		public void setQuery(String query) {
			this.query = query;
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
		 * @return the chromossome
		 */
		public String getChromossome() {
			return chromossome;
		}



		/**
		 * @param chromossome the chromossome to set
		 */
		public void setChromossome(String chromossome) {
			this.chromossome = chromossome;
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
		 * @return the status
		 */
		public String getStatus() {
			return status;
		}



		/**
		 * @param status the status to set
		 */
		public void setStatus(String status) {
			this.status = status;
		}



		/**
		 * @return the uniprot_ecnumber
		 */
		public String getUniprot_ecnumber() {
			return uniprot_ecnumber;
		}



		/**
		 * @param uniprot_ecnumber the uniprot_ecnumber to set
		 */
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

		/**
		 * @return the sequence
		 */
		public FastaSequence getSequence() {
			return sequence;
		}



		/**
		 * @param sequence the sequence to set
		 */
		public void setSequence(FastaSequence sequence) {
			this.sequence = sequence;
		}



		/**
		 * @return the product_rank
		 */
		public List<ProductRank> getProduct_rank() {
			return productRank;
		}



		/**
		 * @param product_rank the product_rank to set
		 */
		public void setProduct_rank(List<ProductRank> product_rank) {
			this.productRank = product_rank;
		}



		/**
		 * @return the ecNumber_rank
		 */
		public List<EcNumberRank> getEcNumber_rank() {
			return ecNumber_rank;
		}



		/**
		 * @param ecNumber_rank the ecNumber_rank to set
		 */
		public void setEcNumber_rank(List<EcNumberRank> ecNumber_rank) {
			this.ecNumber_rank = ecNumber_rank;
		}
		private List<EcNumberRank> ecNumber_rank;
		
		

		/**
		 * @return the homologues
		 */
		public List<Homologues> getHomologues() {
			return homologues;
		}
		/**
		 * @param homologues the homologues to set
		 */
		public void setHomologues(List<Homologues> homologues) {
			this.homologues = homologues;
		}

		/**
		 * @return the ecnumberRank
		 */
		public List<EcNumberRank> getEcnumberRank() {
			return ecnumberRank;
		}

		/**
		 * @param ecnumberRank the ecnumberRank to set
		 */
		public void setEcnumberRank(List<EcNumberRank> ecnumberRank) {
			this.ecnumberRank = ecnumberRank;
		}
		
	}
