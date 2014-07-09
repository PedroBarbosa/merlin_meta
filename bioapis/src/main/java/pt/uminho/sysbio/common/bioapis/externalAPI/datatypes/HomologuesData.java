package pt.uminho.sysbio.common.bioapis.externalAPI.datatypes;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.biojava3.core.sequence.ProteinSequence;

/**
 * @author Oscar
 *
 */
public class HomologuesData {

	private ConcurrentHashMap<String, String> locus_Tag;
	private ConcurrentHashMap<String, ProteinSequence> sequences;
	private Map<String, String> organism, taxonomy, product, calculated_mol_wt, definition, blast_locus_tag, organelles, genes;
	private Map<String, Double> eValue, bits;
	private TreeMap<String, String[]> ecnumber;
	private LinkedList<String> locusID = new LinkedList<String>();
	private String organismID, fastaSequence;
	private String chromosome, organelle, locus_protein_note, locus_tag, locus_gene_note, gene;
	private String[] taxonomyID;
	private String sequence_code;
	private ConcurrentHashMap<String, Boolean> uniprotStatus;
	private boolean dataRetrieved;
	private ConcurrentHashMap<String, String[]> taxonomyMap;
	private String refSeqGI;
	private String ncbiLocusTag;
	private String uniProtEntryID;
	private String uniprotLocusTag;

	/**
	 * 
	 */
	public HomologuesData() {

		this.locus_Tag = new ConcurrentHashMap<>();
		this.sequences = new ConcurrentHashMap<>();
		this.organism = new TreeMap<String, String>();
		this.taxonomy= new TreeMap<String, String>();
		this.product= new TreeMap<String, String>();
		this.calculated_mol_wt= new TreeMap<String, String>();
		this.definition= new TreeMap<String, String>();
		this.blast_locus_tag = new TreeMap<String, String>();
		this.organelles = new TreeMap<String, String>();
		this.genes = new TreeMap<String, String>();
		this.uniprotStatus = new ConcurrentHashMap<>();
		this.ecnumber = new TreeMap<>();
		this.taxonomyMap = new ConcurrentHashMap<String, String[]>();
		this.eValue= new TreeMap<String, Double>();
		this.bits= new TreeMap<String, Double>();
		this.gene = "";
		this.chromosome = "";
	}

	/**
	 * @param name
	 * @param locus
	 */
	public void addLocusTag(String name, String locus){

		this.locus_Tag.put(name, locus);
	}

	/**
	 * @param name
	 * @param sequence
	 */
	public void addSequence(String name, ProteinSequence sequence){

		this.sequences.put(name, sequence);
	}

	/**
	 * @param name
	 * @param organism
	 */
	public void addOrganism(String name, String organism){

		this.organism.put(name, organism);
	}

	/**
	 * @param name
	 * @param eValue
	 */
	public void addEValue(String name, double eValue){

		this.eValue.put(name, eValue);
	}

	/**
	 * @param name
	 * @param bits
	 */
	public void addBits(String name, double bits){

		this.bits.put(name, bits);
	}


	/**
	 * @param locus
	 */
	public void addLocusID(String locus){

		this.locusID.add(locus);
	}

	/**
	 * @param primary_acession
	 * @param definition
	 */
	public void addDefinition(String name, String definition) {

		this.definition.put(name, definition);
	}

	/**
	 * @param name
	 * @param product
	 */
	public void addProduct(String name, String product) {

		this.product.put(name, product);
	}

	/**
	 * @param name
	 * @param value
	 */
	public void addCalculated_mol_wt (String name, String value) {

		calculated_mol_wt.put(name , value);
	}

	/**
	 * @param name
	 * @param value
	 */
	public void addOganelles(String name, String value) {

		this.organelles.put(name, value);
	}

	/**
	 * @param name
	 * @param value
	 */
	public void addBlastLocusTags(String name, String value) {

		this.blast_locus_tag.put(name, (String) value);
	}

	/**
	 * @param name
	 * @param value
	 */
	public void addGenes(String name, String value) {

		this.genes.put(name, value);
	}

	/**
	 * @param name
	 * @param value
	 */
	public void addUniprotStatus(String name, boolean value) {

		this.uniprotStatus.put(name, value);
	}

	/**
	 * @param name
	 * @param value
	 */
	public void addECnumbers(String name, String[] value) {

		this.ecnumber.put(name, value);
	}

	/**
	 * @param gene
	 * @param taxonomy
	 */
	public void addTaxonomy(String gene, String taxonomy) {

		this.taxonomy.put(gene, taxonomy);
	}

	/**
	 * @return
	 */
	public Map<String, String> getLocus_Tag() {
		return locus_Tag;
	}

	/**
	 * @param locus_Tag
	 */
	public void setLocus_Tag(ConcurrentHashMap<String, String> locus_Tag) {
		this.locus_Tag = locus_Tag;
	}

	/**
	 * @return
	 */
	public Map<String, ProteinSequence> getSequences() {
		return sequences;
	}

	/**
	 * @param sequences
	 */
	public void setSequences(ConcurrentHashMap<String, ProteinSequence> sequences) {
		this.sequences = sequences;
	}

	public Map<String, String> getOrganism() {
		return organism;
	}

	public void setOrganism(Map<String, String> organism) {
		this.organism = organism;
	}

	public Map<String, String> getTaxonomy() {
		return taxonomy;
	}

	public void setTaxonomy(Map<String, String> taxonomy) {
		this.taxonomy = taxonomy;
	}

	public Map<String, String> getProduct() {
		return product;
	}

	public void setProduct(Map<String, String> product) {
		this.product = product;
	}

	public Map<String, String> getCalculated_mol_wt() {
		return calculated_mol_wt;
	}

	public void setCalculated_mol_wt(Map<String, String> calculated_mol_wt) {
		this.calculated_mol_wt = calculated_mol_wt;
	}

	public Map<String, String> getDefinition() {
		return definition;
	}

	public void setDefinition(Map<String, String> definition) {
		this.definition = definition;
	}

	public Map<String, String> getBlast_locus_tag() {
		return blast_locus_tag;
	}

	public void setBlast_locus_tag(Map<String, String> blast_locus_tag) {
		this.blast_locus_tag = blast_locus_tag;
	}

	public Map<String, String> getOrganelles() {
		return organelles;
	}

	public void setOrganelles(Map<String, String> organelles) {
		this.organelles = organelles;
	}

	public Map<String, String> getGenes() {
		return genes;
	}

	public void setGenes(Map<String, String> genes) {
		this.genes = genes;
	}

	public Map<String, Double> getEValue() {
		return eValue;
	}

	public void setEValue(Map<String, Double> eValue) {
		this.eValue = eValue;
	}

	public Map<String, Double> getBits() {
		return bits;
	}

	public void setBits(Map<String, Double> bits) {
		this.bits = bits;
	}

	public TreeMap<String, String[]> getEcnumber() {
		return ecnumber;
	}

	public void setEcnumber(TreeMap<String, String[]> ecnumber) {
		this.ecnumber = ecnumber;
	}

	/**
	 * @return the locusID
	 */
	public LinkedList<String> getLocusID() {
		return locusID;
	}

	/**
	 * @param locusID the locusID to set
	 */
	public void setLocusID(LinkedList<String> locusID) {
		this.locusID = locusID;
	}

	/**
	 * @return the organismID
	 */
	public String getOrganismID() {
		return organismID;
	}

	/**
	 * @param organismID the organismID to set
	 */
	public void setOrganismID(String organismID) {
		this.organismID = organismID;
	}

	/**
	 * @return the fastaSequence
	 */
	public String getFastaSequence() {
		return fastaSequence;
	}

	/**
	 * @param fastaSequence the fastaSequence to set
	 */
	public void setFastaSequence(String fastaSequence) {
		this.fastaSequence = fastaSequence;
	}

	/**
	 * @param chromosome
	 */
	public void setChromosome(String chromosome) {

		this.chromosome = chromosome;
	}

	/**
	 * @return
	 */
	public String getChromosome() {

		return this.chromosome ;
	}

	/**
	 * @return the locus_protein_note
	 */
	public String getOrganelle() {
		return organelle;
	}

	/**
	 * @param organelle
	 */
	public void setOrganelle(String organelle) {

		this.organelle = organelle;
	}

	/**
	 * @return the locus_protein_note
	 */
	public String getLocus_protein_note() {
		return locus_protein_note;
	}

	/**
	 * @param locus_protein_note the locus_protein_note to set
	 */
	public void setLocus_protein_note(String locus_protein_note) {
		this.locus_protein_note = locus_protein_note;
	}

	/**
	 * @return the locus_tag
	 */
	public String getLocus_tag() {
		return locus_tag;
	}

	/**
	 * @param locus_tag the locus_tag to set
	 */
	public void setLocus_tag(String locus_tag) {
		this.locus_tag = locus_tag;
	}

	/**
	 * @return the taxID
	 */
	public String[] getTaxonomyID() {
		return taxonomyID;
	}

	/**
	 * @param taxID the taxID to set
	 */
	public void setTaxonomyID(String[] taxonomyID) {
		this.taxonomyID = taxonomyID;
	}

	/**
	 * @return the sequence_code
	 */
	public String getSequence_code() {
		return sequence_code;
	}

	/**
	 * @param sequence_code the sequence_code to set
	 */
	public void setSequence_code(String sequence_code) {
		this.sequence_code = sequence_code;
	}

	/**
	 * @return the orgID
	 */
	public String getRefSeqGI() {
		return refSeqGI;
	}

	/**
	 * @param orgID the orgID to set
	 */
	public void setRefSeqGI(String refSeqGI) {
		this.refSeqGI = refSeqGI;
	}

	/**
	 * @return the locus_gene_note
	 */
	public String getLocus_gene_note() {
		return locus_gene_note;
	}

	/**
	 * @param locus_gene_note the locus_gene_note to set
	 */
	public void setLocus_gene_note(String locus_gene_note) {
		this.locus_gene_note = locus_gene_note;
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
	 * @return the uniprotStar
	 */
	public ConcurrentHashMap<String, Boolean> getUniprotStatus() {
		return uniprotStatus;
	}

	/**
	 * @param uniprotStar the uniprotStar to set
	 */
	public void setUniprotStatus(ConcurrentHashMap<String, Boolean> uniprotStatus) {
		this.uniprotStatus = uniprotStatus;
	}

	/**
	 * @param dataRetrieved
	 */
	public void setDataRetrieved(boolean dataRetrieved) {

		this.dataRetrieved = dataRetrieved; 
	}

	/**
	 * @param dataRetrieved
	 */
	public boolean isDataRetrieved() {

		return this.dataRetrieved; 
	}

	/**
	 * @return the taxonomyMap
	 */
	public ConcurrentHashMap<String, String[]> getTaxonomyMap() {
		return taxonomyMap;
	}

	/**
	 * @param taxonomyMap the taxonomyMap to set
	 */
	public void setTaxonomyMap(ConcurrentHashMap<String, String[]> taxonomyMap) {
		this.taxonomyMap = taxonomyMap;
	}
	
	
	/**
	 * @param accessionNumber
	 * @return
	 */
	public double getBits(String accessionNumber){
		
		for (String id : this.getBits().keySet()) {
			
			if(id.contains(accessionNumber)) {
				
				return this.getBits().get(id);
			}
		}
		return -1;
	}

	/**
	 * @param results
	 * @param locus
	 * @return
	 */
	public double getEvalue(String locus){
		
		double eValue = 0;
		
		for (String id : this.getEValue().keySet()) {
			
			if(id.contains(locus)) {
				
				return this.getEValue().get(id);
			}
		}
		return eValue;
	}

	/**
	 * @return the ncbiLocusTag
	 */
	public String getNcbiLocusTag() {
		return ncbiLocusTag;
	}

	/**
	 * @param ncbiLocusTag the ncbiLocusTag to set
	 */
	public void setNcbiLocusTag(String ncbiLocusTag) {
		this.ncbiLocusTag = ncbiLocusTag;
	}

	/**
	 * @return the uniProtEntryID
	 */
	public String getUniProtEntryID() {
		return uniProtEntryID;
	}

	/**
	 * @param uniProtEntryID the uniProtEntryID to set
	 */
	public void setUniProtEntryID(String uniProtEntryID) {
		this.uniProtEntryID = uniProtEntryID;
	}

	/**
	 * @return the uniprotLocusTag
	 */
	public String getUniprotLocusTag() {
		return uniprotLocusTag;
	}

	/**
	 * @param uniprotLocusTag the uniprotLocusTag to set
	 */
	public void setUniprotLocusTag(String uniprotLocusTag) {
		this.uniprotLocusTag = uniprotLocusTag;
	}
}
