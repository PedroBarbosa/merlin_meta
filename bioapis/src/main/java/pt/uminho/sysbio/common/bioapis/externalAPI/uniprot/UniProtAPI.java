package pt.uminho.sysbio.common.bioapis.externalAPI.uniprot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.axis2.AxisFault;
import org.biojava3.core.sequence.AccessionID;
import org.biojava3.core.sequence.DataSource;
import org.biojava3.core.sequence.ProteinSequence;
import org.springframework.remoting.RemoteAccessException;

import pt.uminho.sysbio.common.bioapis.externalAPI.datatypes.HomologuesData;
import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.NcbiTaxonStub_API;
import uk.ac.ebi.kraken.interfaces.common.Sequence;
import uk.ac.ebi.kraken.interfaces.uniprot.DatabaseCrossReference;
import uk.ac.ebi.kraken.interfaces.uniprot.DatabaseType;
import uk.ac.ebi.kraken.interfaces.uniprot.Gene;
import uk.ac.ebi.kraken.interfaces.uniprot.NcbiTaxon;
import uk.ac.ebi.kraken.interfaces.uniprot.NcbiTaxonomyId;
import uk.ac.ebi.kraken.interfaces.uniprot.Organelle;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntryType;
import uk.ac.ebi.kraken.interfaces.uniprot.description.FieldType;
import uk.ac.ebi.kraken.interfaces.uniprot.genename.GeneNameSynonym;
import uk.ac.ebi.kraken.interfaces.uniprot.genename.ORFName;
import uk.ac.ebi.kraken.interfaces.uniprot.genename.OrderedLocusName;
import uk.ac.ebi.kraken.uuw.services.remoting.EntryIterator;
import uk.ac.ebi.kraken.uuw.services.remoting.EntryRetrievalService;
import uk.ac.ebi.kraken.uuw.services.remoting.Query;
import uk.ac.ebi.kraken.uuw.services.remoting.RemoteDataAccessException;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtJAPI;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtQueryBuilder;
import uk.ac.ebi.kraken.uuw.services.remoting.UniProtQueryService;

/**
 * @author oscar
 *
 */
public class UniProtAPI {

	public UniProtAPI(){System.out.println(UniProtJAPI.factory.getVersion());}

	public static UniProtQueryService uniProtQueryService = UniProtJAPI.factory.getUniProtQueryService();
	public static EntryRetrievalService entryRetrievalService  = UniProtJAPI.factory.getEntryRetrievalService();

	/**
	 * @param uniprot_id
	 * @param errorCount
	 * @return
	 */
	public static UniProtEntry getEntryFromUniProtID(String uniprot_id, int errorCount){

		try {

			return (UniProtEntry) entryRetrievalService.getUniProtEntry(uniprot_id);
		} 
		catch(RemoteAccessException e) {

			if(errorCount<10) {

				errorCount+=1;
				return (UniProtEntry) entryRetrievalService.getUniProtEntry(uniprot_id);
			}
			else {

				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * @param uniprot_id
	 * @param errorCount
	 * @return
	 */
	public static List<UniProtEntry> getEntriesFromUniProtIDs(List<String> uniprotIDs, int errorCount){

		try {

			List<UniProtEntry> uniprotEntries = new ArrayList<>();

			Query query = UniProtQueryBuilder.buildIDListQuery(uniprotIDs);

			EntryIterator<UniProtEntry> entries = uniProtQueryService.getEntryIterator(query);
			for (UniProtEntry entry : entries) {

				uniprotEntries.add(entry);
				//		        System.out.println("entry.getUniProtId() = " + entry.getUniProtId());
			}
			return uniprotEntries;
		} 
		catch(RemoteAccessException e) {

			if(errorCount<10) {

				errorCount+=1;
				return UniProtAPI.getEntriesFromUniProtIDs(uniprotIDs, errorCount);
			}
			else {

				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * @param locusTag
	 * @param errorCount
	 * @return
	 */
	public static UniProtEntry getEntry(String locusTag, int errorCount) {

		try {

			String originalLocusTag = locusTag;
			//Query query = UniProtQueryBuilder.buildQuery(locusTag);
			Query query = UniProtQueryBuilder.buildFullTextSearch(locusTag);
			EntryIterator<UniProtEntry> entryIterator = uniProtQueryService.getEntryIterator(query);

			for(UniProtEntry uniProtEntry : entryIterator) {
				//System.out.println(uniProtEntry.getPrimaryUniProtAccession());

				if(locusTag.contains(".*")) {

					locusTag = locusTag.replace(".*","");
				}

				List<Gene> genes = uniProtEntry.getGenes();

				for(int i=0;i<genes.size();i++) {

					List<OrderedLocusName> locusTags = genes.get(i).getOrderedLocusNames();

					for(int j=0;j<locusTags.size();j++) {

						if(locusTags.get(j).getValue().equalsIgnoreCase(locusTag)) {

							i=genes.size();
							j=locusTags.size();
							return uniProtEntry;
						}
					}

					if(genes.get(i).getGeneName().getValue().equalsIgnoreCase(locusTag)) {

						return uniProtEntry;
					}

					List<ORFName> orfNames = genes.get(i).getORFNames();

					for(int j=0;j<orfNames.size();j++) {

						if(orfNames.get(j).getValue().equalsIgnoreCase(locusTag)) {

							i=genes.size();
							j=orfNames.size();
							return uniProtEntry;
						}
					}
					List<GeneNameSynonym> geneNameSynonyms = genes.get(i).getGeneNameSynonyms();

					for(int j=0;j<geneNameSynonyms.size();j++) {

						if(geneNameSynonyms.get(j).getValue().equalsIgnoreCase(locusTag)) {

							i=genes.size();
							j=geneNameSynonyms.size();
							return uniProtEntry;
						}
					}
				}
				//search for locus in kegg also
				//				for(DatabaseCrossReference dbcr : uniProtEntry.getDatabaseCrossReferences(DatabaseType.KEGG))
				//				{
				//					if(dbcr.toString().split(":")[1].equalsIgnoreCase(locusTag))
				//					{
				//						return uniProtEntry;
				//					}
				//				}
				for(DatabaseCrossReference dbcr : uniProtEntry.getDatabaseCrossReferences()) {

					String[] xRefs = dbcr.toString().split(":");

					for(String xRef:xRefs) {

						if(xRef.contains(locusTag)) {

							return uniProtEntry;
						}
					}
				}

				//				for(DatabaseCrossReference dbcr : uniProtEntry.getDatabaseCrossReferences())
				//				{
				//					String[] xRefs = dbcr.toString().split(":");
				//					for(String xRef:xRefs){
				//						if(xRef.contains(".")){
				//							xRef=xRef.split("\\.")[0];
				//							System.out.println(xRef);
				//							}
				//						if(xRef.equalsIgnoreCase(locusTag)){
				//						return uniProtEntry;
				//						}
				//					}
				//				}
			}
			if(!originalLocusTag.contains(".")) {

				originalLocusTag=originalLocusTag.concat(".*");
				return getEntry(originalLocusTag, 0);
			}
			return null;
		} 
		catch(RemoteAccessException e) {

			if(errorCount<10) {

				return getEntry(locusTag, errorCount+1);
			}
			else {

				throw e;
			}
		}
		catch(OutOfMemoryError err) {

			if(errorCount<10) {

				return getEntry(locusTag, errorCount+1);
			}
			else {

				throw err;
			}
		}
	}

	/**
	 * @param crossReference
	 * @param errorCount
	 * @return
	 */
	public static UniProtEntry getUniProtEntryID(String crossReference, int errorCount){

		try {

			Query query = UniProtQueryBuilder.buildQuery(crossReference);
			EntryIterator<UniProtEntry> entryIterator = uniProtQueryService.getEntryIterator(query);

			if(entryIterator.getResultSize()==0)
				entryIterator = uniProtQueryService.getEntryIterator(UniProtQueryBuilder.buildDatabaseCrossReferenceQuery(crossReference));

			for(UniProtEntry uniProtEntry : entryIterator) {

				//System.out.println("Primary Acc = " +uniProtEntry.getPrimaryUniProtAccession().getValue());
				Iterator<DatabaseCrossReference> it = uniProtEntry.getDatabaseCrossReferences().iterator();
				while(it.hasNext()) {

					DatabaseCrossReference dbcr = it.next();

					DatabaseType database = dbcr.getDatabase();

					if(database.equals(DatabaseType.REFSEQ)) {	

						String x_ref = dbcr.getPrimaryId().getValue();

						for(String id:x_ref.split(":")) {

							if(id.trim().equalsIgnoreCase(crossReference.trim())) {

								return uniProtEntry;
							}

							if(crossReference.contains(".") && id.contains(".")) {

								id = id.trim().split("\\.")[0];
								String cross = crossReference.split("\\.")[0];
								if(id.equalsIgnoreCase(cross)) {

									return uniProtEntry;
								}
							}
						}

					}

//					List<DatabaseCrossReference> cr= uniProtEntry.getDatabaseCrossReferences(database);
//					for(int j=0;j<cr.size();j++) {
//
//						for(String id:cr.get(j).toString().split(":")) {
//
//							if(id.trim().equalsIgnoreCase(crossReference.trim())) {
//
//								return uniProtEntry;
//							}
//
//							if(crossReference.contains(".") && id.contains(".")) {
//
//								id = id.trim().split("\\.")[0];
//								String cross = crossReference.split("\\.")[0];
//								if(id.equalsIgnoreCase(cross)) {
//
//									return uniProtEntry;
//								}
//							}
//						}
//					}
				}
			}
			return null;
		}
		catch(Exception e) {

			if(errorCount<10) {

				return getUniProtEntryID(crossReference, errorCount+1);
			}
			else {

				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * @param taxID
	 * @param errorCount
	 * @return
	 */
	public static TaxonomyContainer getTaxonomyFromNCBITaxnomyID(long taxID, int errorCount) {

		TaxonomyContainer result = new TaxonomyContainer();

		try {

			Query query = UniProtQueryBuilder.buildFullTextSearch(""+taxID);

			EntryIterator<UniProtEntry> entryIterator = uniProtQueryService.getEntryIterator(query);

			for(UniProtEntry uniProtEntry : entryIterator) {

				List<NcbiTaxonomyId> taxa = uniProtEntry.getNcbiTaxonomyIds();

				for(NcbiTaxonomyId taxon : taxa) {

					if(taxon.getValue().equalsIgnoreCase(""+taxID)) {


						result.setSpeciesName(uniProtEntry.getOrganism().getScientificName().getValue());
						result.setTaxonomy(uniProtEntry.getTaxonomy());
						return result;
					}
				}
			}
			return null;
		}
		catch(RemoteAccessException e) {

			if(errorCount<10) {

				errorCount = errorCount+1;
				return getTaxonomyFromNCBITaxnomyID(taxID, errorCount+1);
			}
			else {

				e.printStackTrace();
				return null;
			}
		}

	}

	/**
	 * @param taxID
	 * @param errorCount
	 * @return
	 * @throws AxisFault 
	 */
	public static TaxonomyContainer getTaxonomyFromNCBI(long taxID, int errorCount) throws Exception {

		TaxonomyContainer result = new TaxonomyContainer();
		Map<String,String[]> taxData = null;

		try {

			NcbiTaxonStub_API stub = new NcbiTaxonStub_API(1);

			taxData = stub.getTaxonList(taxID+"", 0);

		}
		catch(Exception e) {

			if(errorCount<10) {

				errorCount = errorCount+1;
				return getTaxonomyFromNCBI(taxID, errorCount+1);
			}
			else {

				e.printStackTrace();
				return null;
			}
		}

		String[] myTax = taxData.get(taxID+"");
		List<NcbiTaxon> list_taxon = new ArrayList<NcbiTaxon>();

		int i = 0;
		for(String taxon : myTax[1].split("; ")) {

			list_taxon.add(i,new MyNcbiTaxon(taxon));
			i++;
		}

		result.setSpeciesName(myTax[0]);
		result.setTaxonomy(list_taxon);

		return result;
	}

	/**
	 * @param entry
	 * @return
	 */
	public static String getProteinExistence(UniProtEntry entry){

		if(entry!=null) {

			return entry.getProteinExistence().getValue();
		}
		return null;
	}

	/**
	 * @param entry
	 * @return
	 */
	public static List<OrderedLocusName> getLocusTag(UniProtEntry entry) {

		if(entry!=null) {

			if(entry.getGenes().size()>0) {

				return entry.getGenes().get(0).getOrderedLocusNames();	
			}
		}
		return null;
	}

	/**
	 * @param query
	 * @return
	 */
	public static String getFirstLocusTag(String query) {

		UniProtEntry uniProtEntry = UniProtAPI.getUniProtEntryID(query,0);

		return UniProtAPI.getLocusTag(uniProtEntry).get(0).getValue();
	}

	/**
	 * @param entry
	 * @return
	 */
	public static List<String> get_ecnumbers(UniProtEntry entry) {

		if(entry!=null) {

			return entry.getProteinDescription().getEcNumbers();
		}
		return null;
	}

	/**
	 * @param entry
	 * @return
	 */
	public static List<String> getECnumbers(UniProtEntry entry){
		if(entry!=null)
		{
			if(entry.getProteinDescription()!=null)
			{
				if(entry.getProteinDescription().getEcNumbers()!=null)
				{
					List<String> ecs = entry.getProteinDescription().getEcNumbers();
					if(!ecs.isEmpty())
					{
						return ecs;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param entry
	 * @return
	 */
	public static String getOrganism(UniProtEntry entry){

		if(entry!=null)
		{
			String organism = entry.getOrganism().getScientificName().getValue();
			if(organism!=null)
			{
				return organism;
			}
		}
		return null;
	}

	/**
	 * @param entryRetrievalService
	 * @param uniprotID
	 * @param errorCount
	 * @return
	 */
	public static UniProtEntry getUniprotEntry(EntryRetrievalService entryRetrievalService, String uniprotID, int errorCount){
		UniProtEntry entry = null;
		try
		{
			entry = (UniProtEntry) entryRetrievalService.getUniProtEntry(uniprotID);
			return entry;
		}
		catch(RemoteAccessException e){if(errorCount<10){entry=getUniprotEntry(entryRetrievalService, uniprotID, (errorCount+1));}}
		return entry;

	}


	/**
	 * @param locusTag
	 * @param errorCount
	 * @return
	 */
	public static TaxonomyContainer setOriginOrganism(String locusTag, int errorCount) {

		TaxonomyContainer result= new TaxonomyContainer();

		try {

			Query query = UniProtQueryBuilder.buildQuery(locusTag);
			EntryIterator<UniProtEntry> entryIterator = uniProtQueryService.getEntryIterator(query);

			for(UniProtEntry uniProtEntry : entryIterator)  {

				if (uniProtEntry != null) {

					result.setSpeciesName(uniProtEntry.getOrganism().getScientificName().getValue());
					result.setTaxonomy(uniProtEntry.getTaxonomy());
					return result;
				}
			}
		}
		catch(RemoteAccessException e) { 

			if(errorCount<10) {

				errorCount = errorCount+1;
				result=UniProtAPI.setOriginOrganism(locusTag, errorCount);
			}
		}

		return result;
	}

	/**
	 * @param entry
	 * @param errorCount
	 * @return
	 */
	public static TaxonomyContainer get_uniprot_entry_organism(String entry, int errorCount){

		TaxonomyContainer result = new TaxonomyContainer();

		try {

			UniProtEntry uniProtEntry = UniProtAPI.getEntryFromUniProtID(entry,0);
			if(uniProtEntry==null) {

				result.setSpeciesName("deleted");
				System.err.println("deleted "+entry);
			}
			else {

				result.setSpeciesName(uniProtEntry.getOrganism().getScientificName().getValue());
				result.setTaxonomy(uniProtEntry.getTaxonomy());
			}
			return result;
		}
		catch(RemoteAccessException e) {

			if(errorCount<3) {

				result=UniProtAPI.get_uniprot_entry_organism(entry, (errorCount+1));
			}
			else {

				System.err.println("RemoteAccessException null "+entry);
			}
		}

		return result;
	}


	/**
	 * @param entry
	 * @return
	 */
	public static String[] getOrganismTaxa(UniProtEntry entry) {
		String [] taxon = new String[2];
		if(entry!=null)
		{
			String organism = entry.getOrganism().getScientificName().getValue();
			if(organism!=null)
			{
				taxon[0] = organism;
			}
			String tax = entry.getTaxonomy().toString();
			if(tax!=null)
			{
				taxon[1] = tax;
			}
			return taxon;
		}
		return null;
	}


	/**
	 * @param locusTag
	 * @return
	 */
	public static boolean isStarred(String locusTag) {

		UniProtEntry entry = UniProtAPI.getEntry(locusTag,0);

		return UniProtAPI.isStarred(entry);
	}

	/**
	 * @param entry
	 * @return
	 */
	public static boolean isStarred(UniProtEntry entry) {

		if(entry==null) {

			throw new NullPointerException("UniProt Entry Does not exist!");
		}

		if(entry.getType().getValue().equals( UniProtEntryType.SWISSPROT.getValue())) {

			return true;
		}
		return false;
	}

	/**
	 * @param ecNumber
	 * @param reviewed
	 * @return
	 */
	public static EntryIterator<UniProtEntry> getEntriesByECNumber(String ecNumber, boolean reviewed) {
		Query query = UniProtQueryBuilder.buildECNumberQuery(ecNumber);
		if(reviewed)
			query = UniProtQueryBuilder.setReviewedEntries(query);

		return uniProtQueryService.getEntryIterator(query);

	};

	/**
	 * @param entry
	 * @return
	 */
	public static ProteinSequence getProteinSequenceFromEntry(UniProtEntry entry) {
		Sequence sequence = entry.getSequence();
		ProteinSequence proteinSequence = new ProteinSequence(sequence.getValue());
		AccessionID accession = 
				new AccessionID(entry.getUniProtId().toString(), DataSource.UNIPROT);
		proteinSequence.setAccession(accession);
		return proteinSequence;
	}

	/**
	 * @param query
	 * @return
	 */
	public static String getUniprotStatus(String query) {

		String star = null;
		try {

			UniProtEntry uniProtEntry = UniProtAPI.getUniProtEntryID(query,0);

			star = UniProtAPI.isStarred(uniProtEntry)+"";
		}
		catch(Exception e) {

			star = null;
			//e.printStackTrace();
		}
		return star;
	}

	/**
	 * @param query
	 * @return
	 */
	public static String getUniprotECnumbers(String query) {

		String uniprot_ecnumber = null;

		try {

			UniProtEntry uniProtEntry = UniProtAPI.getUniProtEntryID(query,0);

			List<String> ecnumbers = UniProtAPI.get_ecnumbers(uniProtEntry);
			if(ecnumbers!= null) {

				uniprot_ecnumber = "";

				for(String ecnumber : ecnumbers) {

					uniprot_ecnumber += ecnumber+", ";
				}
				if(uniprot_ecnumber.contains(", ")) {

					uniprot_ecnumber = uniprot_ecnumber.substring(0, uniprot_ecnumber.lastIndexOf(", "));
				}
			}
		}
		catch(Exception e) {

			e.printStackTrace();
		}
		return uniprot_ecnumber;
	}

	/**
	 * @param uniprotID
	 * @return
	 */
	public static String getRefSeq(String uniprotID) {

		try {

			UniProtEntry uniProtEntry = UniProtAPI.getEntryFromUniProtID(uniprotID, 0);

			if(uniProtEntry!=null && uniProtEntry.getDatabaseCrossReferences(DatabaseType.REFSEQ)!=null) {

				for(DatabaseCrossReference dbcr : uniProtEntry.getDatabaseCrossReferences(DatabaseType.REFSEQ)) {

					for(String ref : dbcr.toString().split(":")) {

						//XM_, XR_, and XP_
						//NM_, NR_, and NP_
						//Nucleotide
						//ref.contains("XM")
						//ref.contains("NM")
						//ref.contains("XR")
						if(ref.contains("NR"))
							System.out.println(UniProtAPI.class+" "+ref);

						if(ref.contains("XM") || ref.contains("XR") ||ref.contains("XP") || ref.contains("NM") ||ref.contains("NR") || ref.contains("NP"))
							return  ref.replace(";", "");
					}
				}
			}
			else {

				System.out.println(UniProtAPI.class+" "+uniProtEntry.getDatabaseCrossReferences()+" "+uniProtEntry.getDatabaseCrossReferences()!= null);
			}
			return null;
		}
		catch (Exception e) {

			System.out.println(uniprotID);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param uniprotID
	 * @return
	 */
	public static Map<String, String> getRefSeq(List<String> uniprotIDs) {

		try {

			Map<String, String> ret = new HashMap<>();
			List<UniProtEntry> uniProtEntries = UniProtAPI.getEntriesFromUniProtIDs(uniprotIDs,0);

			for(UniProtEntry uniProtEntry: uniProtEntries) {

				if(uniProtEntry!=null && uniProtEntry.getDatabaseCrossReferences(DatabaseType.REFSEQ)!=null) {

					for(DatabaseCrossReference dbcr : uniProtEntry.getDatabaseCrossReferences(DatabaseType.REFSEQ)) {

						for(String ref : dbcr.toString().split(":")) {

							//XM_, XR_, and XP_
							//NM_, NR_, and NP_
							//Nucleotide
							//ref.contains("XM")
							//ref.contains("NM")
							//ref.contains("XR")
							if(ref.contains("NR"))
								System.out.println(ref);

							if( ref.contains("XP") || ref.contains("NR") || ref.contains("NP"))
								ret.put(uniProtEntry.getPrimaryUniProtAccession().getValue(), ref.replace(";", ""));
						}
					}
				}
				else {

					System.out.print(UniProtAPI.class+" "+uniProtEntry.getDatabaseCrossReferences());
					System.out.println(" "+uniProtEntry.getDatabaseCrossReferences()!= null);
				}
			}
			return ret;
		}
		catch (Exception e) {

			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param refSeqIDs
	 * @return
	 */
	public static Map<String, List<UniProtEntry>> getUniprotEntriesFromRefSeq(List<String> refSeqIDs, AtomicBoolean cancel) {

		try {

			Map<String, List<UniProtEntry>> ret = new HashMap<>();

			for(String refSeqID: refSeqIDs) {

				List<UniProtEntry> temp = new ArrayList<>();

				EntryIterator<UniProtEntry> refSeqIterator = uniProtQueryService.getEntryIterator(UniProtQueryBuilder.buildDatabaseCrossReferenceQuery(DatabaseType.REFSEQ, refSeqID));

				UniProtEntry uniProtEntry = null;

				while ((uniProtEntry = refSeqIterator.next())!=null && !cancel.get()) {

					temp.add(uniProtEntry);
					//System.out.println("\tuni id "+uniProtEntry.getPrimaryUniProtAccession());
				}
				ret.put(refSeqID,temp);
			}

			return ret;
		}
		catch (Exception e) {

			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param homologuesData
	 * @param homologuesList
	 * @param cancel
	 * @param uniprotStatus
	 * @param isNCBIGenome
	 * @return
	 * @throws Exception
	 */
	public static HomologuesData getUniprotData(HomologuesData homologuesData, List<String> homologuesList, 
			AtomicBoolean cancel, boolean uniprotStatus, boolean isNCBIGenome) throws Exception {

		List<String> genesList = new ArrayList<>();

		UniProtEntry geneEntry = UniProtAPI.getEntryFromUniProtID(homologuesData.getUniProtEntryID(),0); 
		if(geneEntry!=null) {

			genesList.add(0, geneEntry.getPrimaryUniProtAccession().getValue());
			genesList.addAll(1,homologuesList);
		}
		else
			genesList.addAll(homologuesList);

		//System.out.println(genesList);

		Query query = UniProtQueryBuilder.buildIDListQuery(genesList);

		EntryIterator<UniProtEntry> entries = uniProtQueryService.getEntryIterator(query);
		for (UniProtEntry entry : entries) {

			String primary_accession = entry.getPrimaryUniProtAccession().getValue();
			
			//System.out.println(primary_accession);

			String locus = null;

			try {
				locus = entry.getGenes().get(0).getOrderedLocusNames().get(0).getValue();
			}
			catch(Exception e) {locus=null;}

			if(locus==null) {

				try {
					locus = entry.getGenes().get(0).getORFNames().get(0).getValue();
				}
				catch(Exception e) {locus = primary_accession;}

			}

			if(primary_accession.equalsIgnoreCase(homologuesData.getUniProtEntryID())) {

				homologuesData.addEValue(primary_accession,0.0);
				homologuesData.addBits(primary_accession,0.0);
				homologuesData.setLocus_tag(locus);

				if(isNCBIGenome) {

					homologuesData.setOrganismID(entry.getOrganism().getScientificName().getValue());
				}
				else {

					homologuesData.setOrganismID(homologuesData.getTaxonomyID()[0]);
				}
				homologuesData.setFastaSequence(entry.getSequence().getValue());

				try {
					homologuesData.setGene(entry.getGenes().get(0).getGeneName().getValue());
				}
				catch (Exception e) {e.printStackTrace();}
			}

			homologuesData.addLocusID(primary_accession);
			homologuesData.addLocusTag(primary_accession, locus);
			homologuesData.addBlastLocusTags(primary_accession, locus);

			String name = null;

			try {

				name = entry.getProteinDescription().getRecommendedName().getFieldsByType(FieldType.FULL).get(0).getValue();
			}
			catch (Exception e) {name = null;}

			if(name == null) {

				try {

					name = entry.getProteinDescription().getSubNames().get(0).getFieldsByType(FieldType.FULL).get(0).getValue();
				} catch (Exception e) {name=null;}
			}

			homologuesData.addDefinition(primary_accession, name);
			homologuesData.addProduct(primary_accession, name);		
			homologuesData.addOrganism(primary_accession, entry.getOrganism().getScientificName().getValue());
			homologuesData.addTaxonomy(primary_accession, entry.getTaxonomy().toString().replace(",", ";").replace("[", "").replace("]", ""));
			homologuesData.addUniprotStatus(locus, entry.getType().getValue().equals(UniProtEntryType.SWISSPROT.getValue()));

			try {

				List<String> le = entry.getProteinDescription().getEcNumbers();
				if(le.size()>0) {

					String[] ec = new String[le.size()]; 
					homologuesData.addECnumbers(primary_accession, le.toArray(ec));
				}
			} catch (Exception e) {e.printStackTrace();}

			try {
				List<Organelle> organelles = entry.getOrganelles();
				if(organelles.size()>0)
					homologuesData.addOganelles(primary_accession, organelles.toString());

			} catch (Exception e) {e.printStackTrace();}
		}

		return homologuesData;
	}

	/**
	 * @param query
	 * @return
	 */
	public static EntryData getEntryData(String query) {

		return UniProtAPI.getEntryData(query,0);
	}

	/**
	 * @param query
	 * @param errorCount
	 * @return
	 */
	private static EntryData getEntryData(String query, int errorCount) {

		EntryData entry;
		try {

			UniProtEntry uniProtEntry = UniProtAPI.getUniProtEntryID(query,0);

			entry = new EntryData(uniProtEntry.getPrimaryUniProtAccession().getValue());

			String uniprot_ecnumber = null;
			List<String> ecnumbers = UniProtAPI.get_ecnumbers(uniProtEntry);
			if(ecnumbers!= null) {

				uniprot_ecnumber = "";

				for(String ecnumber : ecnumbers) {

					uniprot_ecnumber += ecnumber+", ";
				}
				if(uniprot_ecnumber.contains(", ")) {

					uniprot_ecnumber = uniprot_ecnumber.substring(0, uniprot_ecnumber.lastIndexOf(", "));
				}
			}
			entry.setEcnumber(uniprot_ecnumber);
			entry.setUniprotReviewStatus(UniProtAPI.isStarred(uniProtEntry)+"");

			String locus = null;

			try {
				locus = uniProtEntry.getGenes().get(0).getOrderedLocusNames().get(0).getValue();
			}
			catch(Exception e) {locus=null;}

			if(locus==null) {

				try {

					locus = uniProtEntry.getGenes().get(0).getORFNames().get(0).getValue();
				}
				catch(Exception e) {locus = uniProtEntry.getPrimaryUniProtAccession().getValue();}
			}

			entry.setLocusTag(locus);
		}
		catch (Exception e) {

			errorCount+=1;
			//e. printStackTrace();

			if(errorCount<3) {

				return UniProtAPI.getEntryData(query, errorCount);
			}
			else {

				if(errorCount<5) {

					if(query.contains(".")) {

						query = query.split("\\.")[0];
					}
					return UniProtAPI.getEntryData(query+".*", errorCount);
				}

				else {

					entry = new EntryData(query.replace(".*", ""));
					entry.setUniprotReviewStatus(null);
					entry.setEcnumber(null);
					entry.setLocusTag(query.replace(".*", ""));
				}
			}
		}

		return entry;
	}

	/**
	 * @param orgID
	 * @param i
	 * @return
	 */
	public static String[] newTaxID(String organismmName, int i) {

		try {
			String[] result = new String[2];

			System.out.println(organismmName);

			Query query = UniProtQueryBuilder.buildOrganismQuery(organismmName);
			EntryIterator<UniProtEntry> entryIterator = uniProtQueryService.getEntryIterator(query);

			for(UniProtEntry uniProtEntry : entryIterator) {

				result[0] = uniProtEntry.getOrganism().getScientificName().getValue();
				result[1] = uniProtEntry.getTaxonomy().toString().replace(",", ";").replace("[", "").replace("]", "");
				return result;
			}

		} catch (RemoteDataAccessException e) {

			i++;
			if(i<10)
				return newTaxID(organismmName, i);
		}
		return null;
	} 
}
