package pt.uminho.sysbio.common.bioapis.externalAPI.ncbi;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchSequenceServiceStub;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava3.core.sequence.io.FastaReader;
import org.biojava3.core.sequence.io.GenericFastaHeaderParser;
import org.biojava3.core.sequence.io.ProteinSequenceCreator;

import pt.uminho.sysbio.common.bioapis.externalAPI.datatypes.HomologuesData;

/**
 * @author Oscar
 *
 */
public class NcbiAPI {


	/**
	 * @param map
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, ProteinSequence> getNCBILocusTags(Map<String, ProteinSequence> genome) throws Exception {

		Map<String, ProteinSequence> newGenome = new HashMap<String, ProteinSequence>();

		Map<String, String> idLocus = NcbiAPI.getNCBILocusTags(genome.keySet());

		for (String id : idLocus.keySet()) {

			ProteinSequence pSequence = genome.get(id);
			pSequence.setOriginalHeader(idLocus.get(id));
			newGenome.put(idLocus.get(id), genome.get(id));
		}

		return newGenome;
	}

	/**
	 * @param keys
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> getNCBILocusTags(Set<String> keys) throws Exception {

		NcbiEFetchSequenceStub_API fetchStub = new NcbiEFetchSequenceStub_API(1000);

		Map<String, String> idLocus = fetchStub.getLocusFromID(keys,500);

		return idLocus;
	}


	/**
	 * @param path
	 * @return
	 */
	public static List<String> get_data_file(String path, boolean trim) {
		File aFile = new File(path);
		List<String> contents = new ArrayList<String>();

		try 
		{
			BufferedReader input =  new BufferedReader(new FileReader(aFile));
			String line = null;
			while (( line = input.readLine()) != null)
			{
				if(trim)
				{
					contents.add(line.trim());
				}
				else
				{
					contents.add(line);
				}
				//contents.add(System.getProperty("line.separator"));
			}
			input.close();
		}
		catch (IOException ex){ex.printStackTrace();}

		return contents;
	}

	/**
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public Map<String, ProteinSequence> set_Sequences(String filePath) throws Exception{
		File seqFile = new File(filePath);
		FastaReader<ProteinSequence,AminoAcidCompound> fastaReader = new FastaReader<ProteinSequence,AminoAcidCompound>(
				seqFile,
				new GenericFastaHeaderParser<ProteinSequence,AminoAcidCompound>(), 
				new ProteinSequenceCreator(AminoAcidCompoundSet.getAminoAcidCompoundSet()));
		Map<String, ProteinSequence> queryMap  =  fastaReader.process();
		return queryMap;
	}

	/**
	 * @param queryInpuStream
	 * @return
	 * @throws Exception
	 */
	public static Map<String, ProteinSequence> set_Sequences(InputStream queryInpuStream) throws Exception{
		FastaReader<ProteinSequence,AminoAcidCompound> fastaReader = new FastaReader<ProteinSequence,AminoAcidCompound>(
				queryInpuStream,
				new GenericFastaHeaderParser<ProteinSequence,AminoAcidCompound>(), 
				new ProteinSequenceCreator(AminoAcidCompoundSet.getAminoAcidCompoundSet()));
		Map<String, ProteinSequence> queryMap  =  fastaReader.process();
		return queryMap;
	}

	/**
	 * select and align candidate genes only
	 * 
	 * @param queryMap
	 */
	public static Map<String, ProteinSequence> filterCandidatesGenBank(Set<String> candidates, Map<String, ProteinSequence> queryMap){
		if(candidates!=null)
		{
			for(String geneID: new TreeSet<String>(queryMap.keySet()))
			{
				if(!candidates.contains(geneID))
				{
					queryMap.remove(geneID);
				}
			}
		}
		return queryMap;
	}

	/**
	 * select and align candidate genes only
	 * 
	 * @param queryMap
	 */
	public static  Map<String, ProteinSequence> filterCandidatesGenolevures(Set<String> candidates, Map<String, ProteinSequence> queryMap){
		if(candidates!=null)
		{
			for(String query: new TreeSet<String>(queryMap.keySet()))
			{
				String geneID = query;
				if(query.contains(" "))
				{
					StringTokenizer stq = new StringTokenizer(query," ");
					geneID = stq.nextToken();
				}
				if(!candidates.contains(geneID))
				{
					queryMap.remove(query);
				}
			}
		}
		return queryMap;
	}



	/**
	 * read TMHMM file 
	 * 
	 * @param file
	 * @return set with transport protein candidates
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static Map<String,Integer> readTMHMMGenolevures(File file, int minimum_number_of_helices) throws NumberFormatException, IOException {

		BufferedReader reader = null;
		//Set<String> ids = new TreeSet<String>();
		Map<String,Integer> tmhmmScore=(new TreeMap<String, Integer>());
		reader = new BufferedReader(new FileReader(file));
		String text = null;

		while ((text = reader.readLine()) != null) {
			
			if(text.startsWith("#")) {
				
				text=text.replace("# ", "");
				StringTokenizer st = new StringTokenizer(text," ");
				String id = st.nextToken();
				if(st.nextToken().equals("Number"))
				{
					st = new StringTokenizer(text,":");
					st.nextToken();
					int number_of_helices = Integer.parseInt(st.nextToken().trim());
					if(number_of_helices>=minimum_number_of_helices)
					{
						//	este parser n�o resulta, os identificadores com _ perdem-se. usar alguma api para fazer parsing, por exemplo se come�ar por gi usar entrez protein se come�ar por GNV usar este parser...api
						// verificar para que sao os metodos no construtor e descreve-los bem...
						st = new StringTokenizer(id,"_");
						String locusTag = null;
						while(st.hasMoreTokens()){locusTag= st.nextToken();}
						//ids.add(locusTag.replace("||","_"));
						tmhmmScore.put(locusTag.replace("||","_"),number_of_helices);
					}
				}
			}
		}
		reader.close();
		return tmhmmScore;
	}

	/**
	 * read TMHMM file 
	 * 
	 * @param file
	 * @return set with transport protein candidates
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static Map<String,Integer> readTMHMMGenbank(File file, int minimum_number_of_helices, Map<String, String> ids_locus) throws NumberFormatException, IOException{
		BufferedReader reader = null;
		//Set<String> ids = new TreeSet<String>();
		Map<String,Integer> tmhmmScore=new TreeMap<String, Integer>();
		reader = new BufferedReader(new FileReader(file));
		String text = null;

		while ((text = reader.readLine()) != null) 
		{
			if(text.startsWith("#"))
			{
				text=text.replace("# ", "");
				StringTokenizer st = new StringTokenizer(text," ");
				String id = st.nextToken();
				if(st.nextToken().equals("Number"))
				{
					st = new StringTokenizer(text,":");
					st.nextToken();
					int number_of_helices = Integer.parseInt(st.nextToken().trim());
					if(number_of_helices>=minimum_number_of_helices)
					{
						//	este parser n�o resulta, os identificadores com _ perdem-se. usar alguma api para fazer parsing, por exemplo se come�ar por gi usar entrez protein se come�ar por GNV usar este parser...api
						// verificar para que sao os metodos no construtor e descreve-los bem...

						///String locus = ids_locus.get(id.split("ref_")[1].replace(".","\t").split("\t")[0]);
						String temp=id.split("ref_")[1];
						String locus = ids_locus.get(temp.substring(0, temp.length()-1));
						//ids.add(locus);
						tmhmmScore.put(locus,number_of_helices);
					}
				}
			}
		}
		reader.close();
		return tmhmmScore;
	}


	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static boolean isTMHMMFile(File file) throws IOException {
		BufferedReader reader = null;
		reader = new BufferedReader(new FileReader(file));
		String text = null;

		while ((text = reader.readLine()) != null) {

			if(text.trim().equalsIgnoreCase("TMHMM result")) {

				reader.close();
				return true;
			}

			if(text.trim().equalsIgnoreCase("Number of predicted TMHs")) {

				reader.close();
				return true;
			}
		}
		reader.close();
		return false;
	}

	/**
	 * @param file
	 * @param minimum_number_of_helices
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static Map<String,Integer> readTMHMMGenbankNCBI(File file, int minimum_number_of_helices) throws NumberFormatException, IOException{
		BufferedReader reader = null;
		Map<String,Integer> tmhmmScore=new TreeMap<String, Integer>();
		reader = new BufferedReader(new FileReader(file));
		String text = null;

		while ((text = reader.readLine()) != null) {

			if(text.startsWith("#")) {

				text=text.replace("# ", "");
				StringTokenizer st = new StringTokenizer(text," ");
				String id = st.nextToken();

				if(st.nextToken().equals("Number")) {

					st = new StringTokenizer(text,":");
					st.nextToken();
					int number_of_helices = Integer.parseInt(st.nextToken().trim());

					if(number_of_helices>=minimum_number_of_helices) {

						String separator = "\\|";

						if(id.contains("_") && !id.contains("|"))
							separator = "_";

						String temp;
						if(id.contains("ref")) {

							temp=id.split("ref"+separator)[1];
						} 
						else if(id.contains("emb")) {

							temp=id.split("emb"+separator)[1];
						}
						else if(id.contains("gb")) {

							temp=id.split("gb"+separator)[1];
						}
						else {

							System.out.println("NEW identifier on "+id);
							temp=null;
						}
						tmhmmScore.put(temp.substring(0, temp.length()-1),number_of_helices);
					}
				}
			}
		}
		reader.close();
		return tmhmmScore;
	}

	/**
	 * @param file
	 * @param minimum_number_of_helices
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static Map<String,Integer> readTMHMMGenbank(File file, int minimum_number_of_helices) throws NumberFormatException, NoSuchElementException, IOException{
		BufferedReader reader = null;
		Map<String,Integer> tmhmmScore=new TreeMap<String, Integer>();
		reader = new BufferedReader(new FileReader(file));
		String text = null;

		while ((text = reader.readLine()) != null) {

			if(text.startsWith("#")) {

				text=text.replace("# ", "");
				StringTokenizer st = new StringTokenizer(text," ");
				String id = st.nextToken();

				if(st.nextToken().equals("Number")) {

					st = new StringTokenizer(text,":");
					st.nextToken();
					int number_of_helices = Integer.parseInt(st.nextToken().trim());
					if(number_of_helices>=minimum_number_of_helices) {

						tmhmmScore.put(id,number_of_helices);
					}
				}
			}
		}
		reader.close();
		return tmhmmScore;
	}


	/**
	 * @param ids
	 * @param ids_locus
	 * @return
	 */
	public static Map<String, ProteinSequence> parse_locus(Map<String, ProteinSequence> ids){
		Map<String, ProteinSequence> locus_protein = new HashMap<String, ProteinSequence>();

		for(String key:ids.keySet())
		{
			locus_protein.put(key.split(" ")[0].replace("gnl|GLV|", ""),ids.get(key));
		}
		return locus_protein;
	}

	//		/**
	//		 * @param ids
	//		 * @return
	//		 */
	//		public static Map<String,String> get_ids_locus(Set<String> ids){
	//	
	//			List<String> locus = new ArrayList<String> (ids);
	//			List<String> locus_parsed = new ArrayList<String> ();
	//			
	//			for(int i=0; i<locus.size();i++)
	//			{
	//				String temp_string = locus.get(i).replace(".","\t").split("\t")[0];
	//				locus_parsed.set(i,temp_string);
	//			}
	//			return UtilsMethods.get_locus_from_id(locus);
	//		}



	/**
	 * @param ids
	 * @param ids_locus
	 * @return
	 */
	public static Map<String, ProteinSequence> replace_ids_with_locus(Map<String, ProteinSequence> ids, Map<String,String> ids_locus){
		Map<String, ProteinSequence> locus_protein = new HashMap<String, ProteinSequence>();
		//		Map<String, ProteinSequence> temp = new HashMap<String, ProteinSequence>();

		//		for(int i=0; i<locus.size();i++)
		//		{
		//			String temp_string = locus.get(i).replace(".","\t").split("\t")[0];
		//			temp.put(temp_string, ids.get(locus.get(i)));
		//		}
		//		ids_locus = UtilsMethods.get_locus_from_id(new ArrayList<String>(ids.keySet()));

		for(String key:ids.keySet())
		{
			String surrogate_key = key;
			//			if(key.contains("."))
			//			{
			//				surrogate_key=key.split("\\.")[0];
			//			}
			locus_protein.put(ids_locus.get(surrogate_key),ids.get(key));
		}
		//temp=null;
		return locus_protein;
	}

	/**
	 * @param ids
	 * @param ids_locus
	 * @return
	 */
	public static Map<String, Integer> replace_tmhmm_ids_with_locus(Map<String, Integer> ids, Map<String,String> ids_locus){
		Map<String, Integer> locus_tmhmm = new HashMap<String, Integer>();
		//		Map<String, ProteinSequence> temp = new HashMap<String, ProteinSequence>();

		//		for(int i=0; i<locus.size();i++)
		//		{
		//			String temp_string = locus.get(i).replace(".","\t").split("\t")[0];
		//			temp.put(temp_string, ids.get(locus.get(i)));
		//		}
		//		ids_locus = UtilsMethods.get_locus_from_id(new ArrayList<String>(ids.keySet()));

		for(String key:ids.keySet())
		{
			String surrogate_key = key;
			//			if(key.contains("."))
			//			{
			//				surrogate_key=key.split("\\.")[0];
			//			}
			locus_tmhmm.put(ids_locus.get(surrogate_key),ids.get(key));
		}
		//temp=null;
		return locus_tmhmm;
	}

	/**
	 * @param locus_tag_list
	 * @param initalNumberOfRequests
	 * @return
	 */
	public static Map<String, ProteinSequence> createSequencesMap(List<String> locus_tag_list, int initalNumberOfRequests ){
		Map<String,ProteinSequence> temp = new HashMap<String,ProteinSequence>();
		try
		{	
			boolean go=true;
			int ids_counter = 0;
			while(go)
			{
				String query=locus_tag_list.get(ids_counter);
				int request_counter = 0;
				//while (ids_counter<locus_tag_list.size() && query.length()<8050 && request_counter<250)
				while (request_counter<initalNumberOfRequests)
				{
					//System.out.println(ids.get(ids_counter));
					query=query.concat(","+locus_tag_list.get(ids_counter));
					ids_counter++;
					request_counter++;
				}

				if(ids_counter==locus_tag_list.size()){go=false;}

				EFetchSequenceServiceStub service = new EFetchSequenceServiceStub();
				//call NCBI EFetch utility
				EFetchSequenceServiceStub.EFetchRequest req = new EFetchSequenceServiceStub.EFetchRequest();	
				req.setDb("protein");
				req.setId(query);
				EFetchSequenceServiceStub.EFetchResult res = service.run_eFetch(req);

				for (int i = 0; i < res.getGBSet().getGBSetSequence().length; i++)
				{
					EFetchSequenceServiceStub.GBSeq_type0 obj = res.getGBSet().getGBSetSequence()[i].getGBSeq();

					for(int j=0;j<obj.getGBSeq_featureTable().getGBSeq_featureTableSequence().length;j++)
					{
						if("CDS".equals(obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_key()))
						{
							for(int k=0;k<obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence().length;k++)
							{
								String name = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_name();
								String value = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_value();
								if("locus_tag".equals(name))
								{
									temp.put(value, new ProteinSequence(obj.getGBSeq_sequence().toUpperCase()));
								}
							}
						}
					}
				}
			}
			return temp;
		}
		catch (Exception e) 
		{
			if(initalNumberOfRequests>5)
			{
				initalNumberOfRequests=initalNumberOfRequests/2;
				System.out.println("new initalNumberOfRequests = "+initalNumberOfRequests);
				return createSequencesMap(locus_tag_list, initalNumberOfRequests);
			}
			else
			{
				e.printStackTrace();
				System.err.println("get locus error!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				return null;
			}
		}
	}

	/**
	 * @param homologuesData
	 * @param resultsList
	 * @param queryResponseConcatenationSize
	 * @param trialNumber
	 * @param isNCBIGenome
	 * @param cancel
	 * @param uniprotStatus 
	 * @return
	 * @throws Exception
	 */
	public static HomologuesData getNcbiData(HomologuesData homologuesData, List<String> resultsList, int queryResponseConcatenationSize, 
			int trialNumber, boolean isNCBIGenome, AtomicBoolean cancel, boolean uniprotStatus) throws Exception {

		NcbiEFetchSequenceStub_API stub = new NcbiEFetchSequenceStub_API(1);
		Map<String, String> taxonomyID = new HashMap<>();
		List<String> queryList = new ArrayList<String>();

		if(isNCBIGenome) {

			queryList.add(0, homologuesData.getRefSeqGI());
		}

		EFetchSequenceServiceStub.EFetchRequest req = null;

		try {

			req = new EFetchSequenceServiceStub.EFetchRequest();	

			int index = 0;
			String results = "";

			for(int i=0; i<resultsList.size();i++) {

				if(index>0) {

					results=results.concat(",");
				}

				results=results.concat(resultsList.get(i));
				index++;

				if(index>=queryResponseConcatenationSize) {

					queryList.add(results);
					results="";
					index=0;
				}
			}

			if(!results.isEmpty()) {

				queryList.add(results);
			}

			for(int q = 0 ; q<queryList.size();q++) {

				String query = queryList.get(q);
				req.setId(query);
				req.setDb("protein");

				//System.out.println(NcbiAPI.class+" going for stub... "+query+"\t"+queryList.size());

				homologuesData = stub.getNcbiData(homologuesData, isNCBIGenome, cancel, q, req, query, taxonomyID, uniprotStatus);
				homologuesData = NcbiAPI.processOrganismsTaxonomy(homologuesData, taxonomyID, taxonomyID.size(), 0, cancel);
			}
			return homologuesData;
		}
		catch (Exception e) {

			e.printStackTrace();

			int newTrial = trialNumber+1;
			if(newTrial<20 && !cancel.get()) {

				try {

					Thread.sleep(2000); //miliseconds
				}
				catch (InterruptedException e1) {

					if(!cancel.get()) {

						throw e1;
					}
				}
				if(queryResponseConcatenationSize>1) {

					queryResponseConcatenationSize = queryResponseConcatenationSize/2;
				}
				NcbiAPI.getNcbiData(homologuesData, resultsList, queryResponseConcatenationSize, newTrial, isNCBIGenome, cancel, uniprotStatus);
			}
			else {

				if(!cancel.get()) {

					homologuesData.setDataRetrieved(false);
				}
			}

		}
		return homologuesData;
	}

	/**
	 * @param ncbiData
	 * @param taxonomyID
	 * @param queryResponseConcatenationSize
	 * @param trialNumber
	 * @param cancel
	 * @return
	 * @throws InterruptedException
	 */
	public static HomologuesData processOrganismsTaxonomy(HomologuesData ncbiData, Map<String, String> taxonomyID,
			int queryResponseConcatenationSize, int trialNumber, AtomicBoolean cancel) throws InterruptedException {

		List<String> query = new ArrayList<String>();
		List<String> queryList = new ArrayList<String>();

		for(String taxID : taxonomyID.values()) {

			if(!ncbiData.getTaxonomyMap().containsKey(taxID)) {

				query.add(taxID);

				if(query.size() >= queryResponseConcatenationSize) {

					queryList.add(query.toString());
					query = new ArrayList<String>();
				}
			}
		}

		if(query.size() > 0) {

			queryList.add(query.toString());
		}

		try {

			NcbiTaxonStub_API ncbi = new NcbiTaxonStub_API(queryList.size()+1);

			for(String q : queryList) {

				ncbiData.getTaxonomyMap().putAll(ncbi.getTaxonList(q, 0));
			}

			for(String gene : taxonomyID.keySet()) {

				ncbiData.addOrganism(gene, ncbiData.getTaxonomyMap().get(taxonomyID.get(gene))[0]);
				ncbiData.addTaxonomy(gene, ncbiData.getTaxonomyMap().get(taxonomyID.get(gene))[1]);
			}
			return ncbiData;
		}
		catch (Exception e) {

			e.printStackTrace();

			int newTrial = trialNumber+1;

			if(newTrial<5 && !cancel.get()) {

				try {

					Thread.sleep(500); //miliseconds
				}
				catch (InterruptedException e1) {

					if(!cancel.get()) {

						throw e1;
					}
				}
				if(queryResponseConcatenationSize>1) {

					queryResponseConcatenationSize = queryResponseConcatenationSize/2;
				}
				processOrganismsTaxonomy(ncbiData, taxonomyID,queryResponseConcatenationSize,newTrial, cancel);
			}
			else {

				if(!cancel.get()) {

					ncbiData.setDataRetrieved(false);
				}
			}
		}
		return ncbiData;

	}

	/**
	 * @param orgID
	 * @param trialNumber
	 * @return
	 * @throws Exception
	 */
	public static List<String> newOrgID(String orgID, int trialNumber) throws Exception {

		List<String> results = null;
		List<String> query = new ArrayList<String>();
		query.add(orgID);

		try {

			NcbiESearchStub_API es_stub = new NcbiESearchStub_API(50);

			results = es_stub.getDatabaseIDs("protein", query, 100,0);

			return results;
		}
		catch (Exception e) {

			if(trialNumber<15) {

				trialNumber=trialNumber+1;
				return NcbiAPI.newOrgID(orgID, trialNumber);
			}
			else {

				e.printStackTrace();
				System.out.println(NcbiAPI.class.toString()+" newOrgID error "+e.getMessage()+" trial "+trialNumber+"\nQuery\t"+query);
				throw e;
			}
		}
	}

	/**
	 * @param query
	 * @return
	 * @throws Exception 
	 */
	public static String getLocusTag(String query) throws Exception {

		NcbiEFetchSequenceStub_API fetchStub = new NcbiEFetchSequenceStub_API(50);
		Set<String> querySet = new HashSet<String>();
		querySet.add(query);
		return fetchStub.getLocusFromID(querySet,10).get(query);
	}

	/**
	 * @param resultsList 
	 * @param trialNumber
	 * @return 
	 * @throws Exception 
	 */
	public static List<String> getProteinDatabaseIDS(List<String> resultsList, int trialNumber, int queryResponseConcatenationSize) throws Exception {

		List<String> results = null;
		try {

			NcbiESearchStub_API es_stub =  new NcbiESearchStub_API(50);
			results = es_stub.getDatabaseIDs("protein", resultsList, queryResponseConcatenationSize,0);
			//System.out.println(UtilsMethods.class.toString()+" results "+results);
			return results;
		}
		catch (Exception e) {

			if(trialNumber<5) {

				trialNumber++;
				if(queryResponseConcatenationSize>1) {

					queryResponseConcatenationSize = queryResponseConcatenationSize/2;
				}
				return NcbiAPI.getProteinDatabaseIDS(resultsList, trialNumber, queryResponseConcatenationSize);
			}
			else {

				System.out.println(NcbiAPI.class+" homology data getProteinDatabaseIDS error "+e.getMessage()+" trial "+trialNumber);
				System.out.println(NcbiAPI.class+" "+resultsList);
				throw e;
			}
		}
	} 
	/**
	 * @param orgID
	 * @param trialNumber
	 * @return
	 * @throws Exception 
	 */
	public static String newOrgLocusID(String orgID) throws Exception {

		List<String> results = NcbiAPI.newOrgID(orgID, 0);

		String ret = orgID;
		if(results.size()>0)
			ret = results.get(0);
		else
			System.out.println(NcbiAPI.class+" New locus for "+orgID+" currently unavailable.");

		return ret;
	}

	/**
	 * @param orgID
	 * @param trialNumber
	 * @return
	 * @throws Exception 
	 */
	public static String[] newTaxID(String orgID, int trialNumber) throws Exception {


		try {

			NcbiTaxonStub_API taxon_stub = new NcbiTaxonStub_API(2);

			return taxon_stub.getTaxonList(orgID, 0).get(orgID);
		}
		catch (Error e) {

			throw new Error("Service unavailable");
		}
		catch (Exception e) {

			if(trialNumber<10) {

				trialNumber=trialNumber+10;
				return newTaxID(orgID, trialNumber);
			}
			else {

				System.out.println(NcbiAPI.class+" "+e.getMessage()+" trial "+trialNumber);
				throw e;
			}
		}
	}
	
	/**
	 * @param orgID
	 * @return
	 * @throws Exception 
	 */
	public static String[] ncbiNewTaxID(long orgID) throws Exception {

		try {

			return NcbiAPI.newTaxID(orgID+"", 0);
		}
		catch (Error e) {

			throw new Error("Service unavailable");
		}
		catch (Exception e) {

			throw e;
		}
	}
}
