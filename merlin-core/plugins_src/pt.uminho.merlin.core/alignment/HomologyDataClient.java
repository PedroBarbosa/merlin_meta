
package alignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import operations.HmmerSimilaritySearch.HmmerDatabase;
import pt.uminho.sysbio.common.bioapis.externalAPI.datatypes.HomologuesData;
import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.NcbiAPI;
import pt.uminho.sysbio.common.bioapis.externalAPI.uniprot.EntryData;
import pt.uminho.sysbio.common.bioapis.externalAPI.uniprot.UniProtAPI;
import alignment.blast.ReadBlasttoList;
import org.biojava.bio.search.SeqSimilaritySearchHit;
import org.biojava.bio.search.SeqSimilaritySearchResult;
import alignment.hmmer.ReadHmmertoList;

/**
 * This class retrieves homology data related to the results list provided by the Sequence 
 * Similarity Search Results from the Entrez Protein Database
 * 
 * @author oDias
 */
public class HomologyDataClient {

	/**
	 * homology data variables  
	 */
	private String 
	organismID, blastSetupID, databaseID, version, program, query; 
	private AtomicBoolean cancel;
	private boolean dataRetrieved;
	private int numberOfHits;
	private boolean isNCBIGenome;
	private String uniProtStarred;
	private boolean noSimilarity;
	private String uniprot_ecnumber;
	private HomologuesData homologuesData;
	private HomologySearchServer homologySearchServer;

	/**
	 * @param project
	 * @param query
	 * @param program
	 * @param cancel
	 * @param uniprotStatus 
	 * @throws Exception
	 */
	public HomologyDataClient(String query, String program, AtomicBoolean cancel, boolean uniprotStatus, HomologySearchServer homologySearchServer) throws Exception {

		this.setBlastServer(homologySearchServer);
		this.setCancel(cancel);
		this.setQuery(query);
		this.setProgram(program);
		this.setNoSimilarity(true);
		this.homologuesData = new HomologuesData();

		try {

			String locusTag=null;

			this.processQueryInformation(query);
			if(this.getBlastServer().equals(HomologySearchServer.NCBI))
				locusTag = NcbiAPI.getLocusTag(this.homologuesData.getRefSeqGI());

			if(locusTag==null && this.homologuesData.getUniprotLocusTag()!=null)
				locusTag = this.homologuesData.getUniprotLocusTag();

			if(locusTag==null)
				locusTag=query;

			this.homologuesData.setLocus_tag(locusTag);
		}
		catch(Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * @param blastList
	 * @param taxonomyMap
	 * @param uniprotStar
	 * @param cancel
	 * @param homologySearchServer
	 * @param hitListSize
	 * @param uniprotStatus
	 * @throws Exception
	 */
	public HomologyDataClient(ReadBlasttoList blastList, ConcurrentHashMap<String, String[]> taxonomyMap, 
			ConcurrentHashMap<String, Boolean> uniprotStar, AtomicBoolean cancel, HomologySearchServer homologySearchServer, 
			int hitListSize, boolean uniprotStatus
			) throws Exception {

		this.setNumberOfHits(hitListSize);
		this.setBlastServer(homologySearchServer);
		this.setCancel(cancel);
		this.setNoSimilarity(false);
		this.setNCBIGenome(true);

		this.homologuesData = new HomologuesData();
		this.homologuesData.setTaxonomyMap(taxonomyMap);
		this.homologuesData.setUniprotStatus(uniprotStar);

		List<String> rawResultsList = this.parseResults(blastList.getResults()); // also parses scores and evalues
		List<String> resultsList = this.initialiseClass(blastList.getQuery(),blastList.getDatabaseId(), blastList.getVersion(), blastList.getProgram(), rawResultsList);		

		this.processQueryInformation(blastList.getQuery());

		//System.out.println(HomologyDataNcbiClient.class+" Getting data for "+resultsList);
		if(this.homologySearchServer.equals(HomologySearchServer.NCBI))
			this.homologuesData = NcbiAPI.getNcbiData(this.homologuesData, resultsList, 99, 1, this.isNCBIGenome(), this.cancel, uniprotStatus);

		if(this.homologySearchServer.equals(HomologySearchServer.EBI))
			this.homologuesData = UniProtAPI.getUniprotData(this.homologuesData, resultsList, cancel, uniprotStatus, this.isNCBIGenome());

	}

	/**
	 * @param hmmerList
	 * @param project
	 * @param taxonomyMap
	 * @param uniprotStar
	 * @param cancel
	 * @throws Exception
	 */
	public HomologyDataClient(ReadHmmertoList hmmerList, ConcurrentHashMap<String, String[]> taxonomyMap, 
			ConcurrentHashMap<String, Boolean> uniprotStar, AtomicBoolean cancel, boolean uniprotStatus
			, HomologySearchServer homologySearchServer
			) throws Exception {


		this.setCancel(cancel);
		this.setNoSimilarity(false);
		this.setNCBIGenome(true);
		this.setBlastServer(homologySearchServer);

		this.homologuesData = new HomologuesData();
		this.homologuesData.setBits(hmmerList.getScores());
		this.homologuesData.setEValue(hmmerList.getEValues());
		this.homologuesData.setTaxonomyMap(taxonomyMap);
		this.homologuesData.setUniprotStatus(uniprotStar);

		this.processQueryInformation(hmmerList.getQuery());

		List<String> rawResultsList = hmmerList.getResults();
		List<String> resultsList = this.initialiseClass(hmmerList.getQuery(),hmmerList.getDatabaseId().toString(), hmmerList.getVersion(), hmmerList.getProgram(), rawResultsList);

		if(hmmerList.getDatabase().equals(HmmerDatabase.uniprotkb)
				|| hmmerList.getDatabase().equals(HmmerDatabase.unimes)
				|| hmmerList.getDatabase().equals(HmmerDatabase.uniprotrefprot)
				)
			this.homologuesData = UniProtAPI.getUniprotData(this.homologuesData, resultsList, cancel, true, this.isNCBIGenome());
		else 
			this.homologuesData = NcbiAPI.getNcbiData(homologuesData, resultsList, 99, 1, isNCBIGenome, this.cancel, uniprotStatus);
	}

	/**
	 * @param blastList
	 * @param project
	 * @param taxID
	 * @param taxonomyMap
	 * @param uniprotStar
	 * @param cancel
	 * @param blastServer 
	 * @param hitListSize 
	 * @param uniprotStatus 
	 * @throws Exception
	 */
	public HomologyDataClient(ReadBlasttoList blastList, String[] taxID, ConcurrentHashMap<String, String[]> taxonomyMap, 
			ConcurrentHashMap<String, Boolean> uniprotStar, AtomicBoolean cancel, HomologySearchServer homologySearchServer, int hitListSize, boolean uniprotStatus
			) throws Exception {

		this.setNumberOfHits(hitListSize);
		this.setBlastServer(homologySearchServer);
		this.setNoSimilarity(false);
		this.setNCBIGenome(false);
		this.setCancel(cancel);

		this.homologuesData = new HomologuesData();
		this.homologuesData.setTaxonomyMap(taxonomyMap);
		this.homologuesData.setUniprotStatus(uniprotStar);
		this.homologuesData.setTaxonomyID(taxID);
		this.homologuesData.setSequence_code(blastList.getQuery());

		this.processQueryInformation(blastList.getQuery());

		List<String> resultsList = this.initialiseClass(blastList.getQuery(),blastList.getDatabaseId(), blastList.getVersion(), blastList.getProgram(), this.parseResults(blastList.getResults()));

		if(this.homologySearchServer.equals(HomologySearchServer.NCBI))
			homologuesData = NcbiAPI.getNcbiData(this.homologuesData, resultsList, 99, 1, isNCBIGenome, this.cancel, uniprotStatus);

		if(this.homologySearchServer.equals(HomologySearchServer.EBI))
			this.homologuesData = UniProtAPI.getUniprotData(homologuesData, resultsList, cancel, uniprotStatus, this.isNCBIGenome());
	}

	/**
	 * @param hmmerList
	 * @param taxID
	 * @param taxonomyMap
	 * @param uniprotStar
	 * @param cancel
	 * @param uniprotStatus
	 * @param homologySearchServer
	 * @throws Exception
	 */
	public HomologyDataClient(ReadHmmertoList hmmerList, String[] taxID, ConcurrentHashMap<String, String[]> taxonomyMap,
			ConcurrentHashMap<String, Boolean> uniprotStar, AtomicBoolean cancel, boolean uniprotStatus
			, HomologySearchServer homologySearchServer) throws Exception {

		this.setCancel(cancel);
		this.setNoSimilarity(false);	
		this.setNCBIGenome(false);
		this.setBlastServer(homologySearchServer);

		this.homologuesData = new HomologuesData();
		this.homologuesData.setTaxonomyMap(taxonomyMap);
		this.homologuesData.setUniprotStatus(uniprotStar);
		this.homologuesData.setBits(hmmerList.getScores());
		this.homologuesData.setEValue(hmmerList.getEValues());
		this.homologuesData.setTaxonomyID(taxID);
		this.homologuesData.setSequence_code(hmmerList.getQuery());

		this.processQueryInformation(hmmerList.getQuery());

		List<String> resultsList = this.initialiseClass(hmmerList.getQuery(), hmmerList.getDatabaseId().toString(), hmmerList.getVersion(), hmmerList.getProgram(), hmmerList.getResults());		

		if(hmmerList.getDatabase().equals(HmmerDatabase.uniprotkb)
				|| hmmerList.getDatabase().equals(HmmerDatabase.unimes)
				|| hmmerList.getDatabase().equals(HmmerDatabase.uniprotrefprot)
				)
			this.homologuesData = UniProtAPI.getUniprotData(this.homologuesData, resultsList, cancel, true, this.isNCBIGenome());
		else 
			this.homologuesData = NcbiAPI.getNcbiData(homologuesData, resultsList, 99, 1, isNCBIGenome, this.cancel, uniprotStatus);
	}

	/**
	 * @param query
	 * @throws Exception
	 */
	private void processQueryInformation(String query) throws Exception{

		EntryData entryData = UniProtAPI.getEntryData(query);
		this.setUniprot_ecnumber(entryData.getEcnumber());
		this.setUniProtStarred(entryData.getUniprotReviewStatus());
		this.homologuesData.setUniProtEntryID(entryData.getEntryID());
		this.homologuesData.setUniprotLocusTag(entryData.getLocusTag());

		this.homologuesData.setRefSeqGI(NcbiAPI.newOrgLocusID(query));
	}


	/**
	 * @param query
	 * @param databaseID
	 * @param version
	 * @param program
	 * @param rawResultsList
	 * @return
	 * @throws Exception
	 */
	private List<String> initialiseClass(String query, String databaseID, String version, String program, List<String> rawResultsList) throws Exception {

		this.setQuery(query);
		this.setDataRetrieved(true);
		this.setDatabaseID(databaseID);
		this.setVersion(version);
		this.setProgram(program);

		List<String> resultsList = rawResultsList;
		if(this.homologySearchServer.equals(HomologySearchServer.NCBI))
			resultsList = this.getProteinDatabaseIDS(rawResultsList);

		return resultsList;
	}

	/**
	 * @param resultsList 
	 * @param trialNumber
	 * @return 
	 * @throws Exception 
	 */
	private List<String> getProteinDatabaseIDS(List<String> resultsList) throws Exception {

		return NcbiAPI.getProteinDatabaseIDS(resultsList, 0, 100);
	} 

	/**
	 * @param list
	 * @return 
	 */
	private List<String> parseResults(List<SeqSimilaritySearchResult> list){

		this.homologuesData.setBits(new HashMap<String, Double>());
		this.homologuesData.setEValue(new HashMap<String, Double>());
		List<String> resultsList = new ArrayList<String>();

		for (SeqSimilaritySearchResult result : list) {

			@SuppressWarnings("unchecked")
			List<SeqSimilaritySearchHit> hits = (List<SeqSimilaritySearchHit>) result.getHits();

			for (int i = 0; i<hits.size();i++ ){

				SeqSimilaritySearchHit hit = hits.get(i);
				String id = hit.getSubjectID();

				if(this.homologySearchServer.equals(HomologySearchServer.EBI)) {

					id = this.parseUniProtIds(hit);
				}

				if(id!=null) {

					resultsList.add(id);
					this.homologuesData.addBits(id, hit.getScore());
					this.homologuesData.addEValue(id, hit.getEValue());
				}
			}
		}
		return resultsList;
	}


	/**
	 * @param id
	 * @return
	 */
	private String parseUniProtIds(SeqSimilaritySearchHit hit) {

		String id = hit.getAnnotation().getProperty("subjectDescription").toString();
		String[] xrefs = id.split("\\s");
		String uni = xrefs[0];

		return uni;
	}

	/**getLocus_tag
	 * @return the identification locus
	 */
	public LinkedList<String> getLocusID(){
		return homologuesData.getLocusID();
	}

	/**
	 * @return the locus of all the blasted entities
	 */
	public Map<String,String> getBlastLocusTag(){
		return homologuesData.getBlast_locus_tag();
	}

	/**
	 * @return the organism
	 */
	public Map<String, String> getOrganism() {
		return homologuesData.getOrganism();
	}

	/**
	 * @return taxonomy
	 */
	public Map<String, String> getTaxonomy() {
		return homologuesData.getTaxonomy();
	}

	/**
	 * @return the product
	 */
	public Map<String, String> getProduct() {
		return homologuesData.getProduct();
	}

	/**
	 * @return the molecular weight
	 */
	public Map<String, String> getCalculated_mol_wt() {
		return homologuesData.getCalculated_mol_wt();
	}

	/**
	 * @return the definition
	 */
	public Map<String, String> getDefinition() {
		return homologuesData.getDefinition();
	}

	/**
	 * @return the enzyme comission number
	 */
	public Map<String, String[]> getEcnumber() {
		return homologuesData.getEcnumber();
	}

	/**
	 * @return the eValue
	 */
	public Map<String, Double> getEValue() {
		return this.homologuesData.getEValue();
	}

	/**
	 * @return the score
	 */
	public Map<String, Double> getScore() {
		return this.homologuesData.getBits();
	}

	/**
	 * @return the gene name
	 */
	public  String getGene() {
		return homologuesData.getGene();
	}

	/**
	 * @return the genes names
	 */
	public Map<String, String> getGenes() {
		return homologuesData.getGenes();
	}

	/**
	 * @return the chromosome where the gene is coded
	 */
	public String getChromossome() {
		return homologuesData.getChromosome();
	}

	/**
	 * @return the organelle where the gene is from
	 */
	public String getOrganelle() {
		return homologuesData.getOrganelle();
	}

	/**
	 * @return the organelle where the gene is from
	 */
	public Map<String, String> getOrganelles() {
		return homologuesData.getOrganelles();
	}

	/**
	 * @return the blast'ed organism name
	 */
	public String getOrganismID(){
		return this.organismID;		
	}

	/**
	 * @return the locus_tag
	 */
	public String getLocus_tag() {

		if(homologuesData.getLocus_tag()==null) {

			return this.query;
		}
		return homologuesData.getLocus_tag();
	}

	/**
	 * @return the locus_note
	 */
	public String getLocus_protein_note() {
		return homologuesData.getLocus_protein_note();
	}

	/**
	 * @return the locus_gene_note
	 */
	public String getLocus_gene_note() {
		return homologuesData.getLocus_gene_note();
	}

	/**
	 * @return the readBlast instance
	 */
	public String getFastaSequence() {
		return homologuesData.getFastaSequence();
	}

	/**
	 * @param fastaSequence the fastaSequence to set
	 */
	public void setFastaSequence(String fastaSequence) {
		homologuesData.setFastaSequence(fastaSequence);
	}

	/**
	 * @param cancel
	 */
	public void setCancel(AtomicBoolean cancel) {

		this.cancel = cancel;
	}

	/**
	 * @param dataRetrieved the dataRetrieved to set
	 */
	public void setDataRetrieved(boolean dataRetrieved) {
		this.dataRetrieved = dataRetrieved;
	}

	/**
	 * @return the dataRetrieved
	 */
	public boolean isDataRetrieved() {
		return dataRetrieved;
	}

	/**
	 * @return the databaseID
	 */
	public String getDatabaseID() {
		return databaseID;
	}

	/**
	 * @param databaseID the databaseID to set
	 */
	public void setDatabaseID(String databaseID) {
		this.databaseID = databaseID;
	}

	/**
	 * @return the blastSetupID
	 */
	public String getBlastSetupID() {
		return blastSetupID;
	}

	/**
	 * @param blastSetupID the blastSetupID to set
	 */
	public void setBlastSetupID(String blastSetupID) {
		this.blastSetupID = blastSetupID;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the program
	 */
	public String getProgram() {
		return program;
	}

	/**
	 * @param program the program to set
	 */
	public void setProgram(String program) {
		this.program = program;
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

	public String[] getTaxonomyID() {
		return homologuesData.getTaxonomyID();
	}

	public void setTaxonomyID(String[] taxID) {
		homologuesData.setTaxonomyID(taxID);
	}

	public boolean isNCBIGenome() {
		return isNCBIGenome;
	}

	public void setNCBIGenome(boolean isNCBIGenome) {
		this.isNCBIGenome = isNCBIGenome;
	}

	/**
	 * @return the uniProtStarred
	 */
	public String isUniProtStarred() {
		return uniProtStarred;
	}

	/**
	 * @param uniProtStarred the uniProtStarred to set
	 */
	public void setUniProtStarred(String uniProtStarred) {
		this.uniProtStarred = uniProtStarred;
	}

	/**
	 * @return the noSimilarity
	 */
	public boolean isNoSimilarity() {
		return noSimilarity;
	}

	/**
	 * @param noSimilarity the noSimilarity to set
	 */
	public void setNoSimilarity(boolean noSimilarity) {
		this.noSimilarity = noSimilarity;
	}

	/**
	 * @return the uniprotStar
	 */
	public ConcurrentHashMap<String, Boolean> getUniprotStar() {
		return homologuesData.getUniprotStatus();
	}

	/**
	 * @param uniprotStar the uniprotStar to set
	 */
	public void setUniprotStar(ConcurrentHashMap<String, Boolean> uniprotStar) {
		homologuesData.setUniprotStatus(uniprotStar);;
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
	 * @return the blastServer
	 */
	public HomologySearchServer getBlastServer() {
		return homologySearchServer;
	}


	/**
	 * @param blastServer the blastServer to set
	 */
	public void setBlastServer(HomologySearchServer blastServer) {
		this.homologySearchServer = blastServer;
	}

	/**
	 * @return the numberOfHits
	 */
	public int getNumberOfHits() {
		return numberOfHits;
	}

	/**
	 * @param numberOfHits the numberOfHits to set
	 */
	public void setNumberOfHits(int numberOfHits) {
		this.numberOfHits = numberOfHits;
	}

	public enum HomologySearchServer {

		EBI,
		NCBI,
		HMMER
	}

}

