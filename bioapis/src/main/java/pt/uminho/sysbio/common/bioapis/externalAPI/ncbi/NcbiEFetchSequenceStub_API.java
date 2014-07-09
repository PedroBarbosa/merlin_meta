package pt.uminho.sysbio.common.bioapis.externalAPI.ncbi;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchSequenceServiceStub;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.biojava3.core.sequence.ProteinSequence;

import pt.uminho.sysbio.common.bioapis.externalAPI.datatypes.HomologuesData;
import pt.uminho.sysbio.common.bioapis.externalAPI.datatypes.NcbiData;
import pt.uminho.sysbio.common.bioapis.externalAPI.uniprot.UniProtAPI;
import pt.uminho.sysbio.common.utilities.datastructures.pair.Pair;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import uk.ac.ebi.kraken.interfaces.uniprot.genename.OrderedLocusName;


/**
 * @author ODias
 *
 */
public class NcbiEFetchSequenceStub_API {


	private static EFetchSequenceServiceStub service;
	private static int  trialCounter=0;

	/**
	 * @param numConnections
	 * @throws Exception
	 */
	public NcbiEFetchSequenceStub_API(int numConnections) throws Exception {

		NcbiEFetchSequenceStub_API.service = new EFetchSequenceServiceStub();
		NcbiEFetchSequenceStub_API.service._getServiceClient().getOptions().setProperty(HTTPConstants.HTTP_PROTOCOL_VERSION, HTTPConstants.HEADER_PROTOCOL_10);

		ConfigurationContext context = NcbiEFetchSequenceStub_API.service._getServiceClient().getServiceContext().getConfigurationContext();
		MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager(); 

		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setDefaultMaxConnectionsPerHost(numConnections);

		multiThreadedHttpConnectionManager.setParams(params);

		HttpClient httpClient = new HttpClient(multiThreadedHttpConnectionManager);
		context.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient); 
		context.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, true);
	}

	/**
	 * @param req
	 * @param trialCounter
	 * @return
	 * @throws Exception 
	 */
	public static EFetchSequenceServiceStub.EFetchResult getFetchResult(EFetchSequenceServiceStub service, EFetchSequenceServiceStub.EFetchRequest req) throws RemoteException {

		try {

			EFetchSequenceServiceStub.EFetchResult res = service.run_eFetch(req);
			return res;
		}
		catch(RemoteException e) {

			if(trialCounter<10) {

				NcbiEFetchSequenceStub_API.service = new EFetchSequenceServiceStub();
				System.out.println("Trial EFetchResult\t"+trialCounter+" for request\t"+req.getId());
				trialCounter++;
				return NcbiEFetchSequenceStub_API.getFetchResult(service, req);
			}
			e.printStackTrace();
			throw new RemoteException();
		}
	}

	/**
	 * @param genes
	 * @param queryResponseConcatenationSize
	 * @return
	 */
	public Map<String,String> getLocusFromID(Set<String> genes, int queryResponseConcatenationSize) {

		List<String> temp = new ArrayList<String>();
		Map<String,String> result = new HashMap<String,String>();
		List<String> ids_list = new ArrayList<String>(genes);

		try {	

			List<String> queryList = new ArrayList<String>();
			String links="";
			int index=0;

			for(int i=0; i<ids_list.size();i++) {

				if(index>0) {

					links=links.concat(",");
				}
				links=links.concat(ids_list.get(i).replace("\"", ""));
				index++;

				if(index>queryResponseConcatenationSize) {

					queryList.add(new String(links));
					links="";
					index=0;
				}
			}
			queryList.add(links);

			for(String query:queryList) {

				EFetchSequenceServiceStub.EFetchRequest req = new EFetchSequenceServiceStub.EFetchRequest();
				req.setDb("protein");
				req.setRetmax("10000");
				query = new String(query.getBytes(),"UTF-8");
				req.setId(query);

				EFetchSequenceServiceStub.EFetchResult res = NcbiEFetchSequenceStub_API.getFetchResult(service, req);

				for (int i = 0; i < res.getGBSet().getGBSetSequence().length; i++) {

					EFetchSequenceServiceStub.GBSeq_type0 obj = res.getGBSet().getGBSetSequence()[i].getGBSeq();

					for(int j=0;j<obj.getGBSeq_featureTable().getGBSeq_featureTableSequence().length;j++) {

						if("CDS".equals(obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_key())) {

							for(int k=0;k<obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence().length;k++) {

								String name = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_name();
								String value = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_value();

								if("locus_tag".equals(name)) {

									result.put(obj.getGBSeq_accessionVersion(), value);
									temp.add(obj.getGBSeq_accessionVersion());
									//System.out.println(value);
									//temp.put(obj.getGBSeq_locus(), value);
								}
							}
						}
					}
					if(!temp.contains(obj.getGBSeq_accessionVersion())) {

						//System.out.println(obj.getGBSeq_accessionVersion());
						UniProtEntry uni = UniProtAPI.getUniProtEntryID(obj.getGBSeq_accessionVersion(),0);
						List<OrderedLocusName> g = UniProtAPI.getLocusTag(uni); 
						if(g!= null && g.size()>0) {

							String locus = g.get(0).getValue();
							result.put(obj.getGBSeq_accessionVersion(), locus);
						}
					}
				}
			}
			return result;
		}
		catch (Exception e) {

			if(queryResponseConcatenationSize>0) {

				int ind = (queryResponseConcatenationSize/2);

				if(ind<=2) {

					queryResponseConcatenationSize = 0;
				}
				else {

					queryResponseConcatenationSize=queryResponseConcatenationSize/2;
				}
				System.out.println("new initalNumberOfRequests = "+queryResponseConcatenationSize);
				return getLocusFromID(genes, queryResponseConcatenationSize);
			}
			else {

				e.printStackTrace();
				System.err.println("get locus error!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				return null;
			}
		}
	}

	/**
	 * @param geneID
	 * @return
	 */
	public String getTaxonomy(String geneID) {

		String taxID = null;
		EFetchSequenceServiceStub.EFetchRequest req = new EFetchSequenceServiceStub.EFetchRequest();
		req.setDb("protein");

		try {

			req.setId(geneID);

			EFetchSequenceServiceStub.EFetchResult res = NcbiEFetchSequenceStub_API.getFetchResult(service, req);

			for (int i = 0; i < res.getGBSet().getGBSetSequence().length; i++) {

				EFetchSequenceServiceStub.GBSeq_type0 obj = res.getGBSet().getGBSetSequence()[i].getGBSeq();

				for(int j=0;j<obj.getGBSeq_featureTable().getGBSeq_featureTableSequence().length;j++) {

					if("source".equals(obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_key())) {

						for(int k=0;k<obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence().length;k++) {

							String name = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_name();
							String value = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_value();

							if("db_xref".equals(name)) {

								if(value.startsWith("taxon")) {

									taxID = value.replace("taxon:", "");
								}
							}
						}

					}
				}
			}
		}
		catch (Exception e) {

			e.printStackTrace();
			return null;
		}
		return taxID;	
	}


	/**
	 * @param ids_list
	 * @param queryResponseConcatenationSize
	 * @return
	 */
	public Map<String, ProteinSequence> createSequencesMap(List<String> ids_list, int queryResponseConcatenationSize){
		Map<String,ProteinSequence> sequences = new HashMap<String,ProteinSequence>();
		Set<String> added= new TreeSet<String>();
		EFetchSequenceServiceStub.EFetchRequest req = new EFetchSequenceServiceStub.EFetchRequest();
		req.setDb("protein");

		try {

			List<String> queryList = new ArrayList<String>();
			String links="";
			int index=0;
			for(int i=0; i<ids_list.size();i++) {

				if(index>0) {

					links=links.concat(",");
				}
				links=links.concat(ids_list.get(i));
				index++;
				if(index>queryResponseConcatenationSize) {

					queryList.add(links);
					links="";
					index=0;
				}
			}
			queryList.add(links);

			for(String query:queryList) {

				req.setId(query);
				EFetchSequenceServiceStub.EFetchResult res = NcbiEFetchSequenceStub_API.getFetchResult(service, req);
				for (int i = 0; i < res.getGBSet().getGBSetSequence().length; i++) {

					EFetchSequenceServiceStub.GBSeq_type0 obj = res.getGBSet().getGBSetSequence()[i].getGBSeq();

					for(int j=0;j<obj.getGBSeq_featureTable().getGBSeq_featureTableSequence().length;j++) {

						if("CDS".equals(obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_key())) {

							for(int k=0;k<obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence().length;k++) {

								String name = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_name();
								String value = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_value();
								if("locus_tag".equals(name)) {

									sequences.put(value, new ProteinSequence(obj.getGBSeq_sequence().toUpperCase()));
									added.add(obj.getGBSeq_accessionVersion());
								}
							}
						}

					}
					String acc = obj.getGBSeq_accessionVersion();
					if(acc!=null) {

						if(!added.contains(obj.getGBSeq_accessionVersion())) {

							UniProtEntry uni = UniProtAPI.getUniProtEntryID(acc,0); 
							if(uni!=null) {

								String locus = UniProtAPI.getLocusTag(uni).get(0).getValue();
								sequences.put(locus, 
										new ProteinSequence(obj.getGBSeq_sequence().toUpperCase()));
								added.add(obj.getGBSeq_accessionVersion());
							}
						}
					}
				}
			}
			return sequences;
		}
		catch (Exception e)  {

			if(queryResponseConcatenationSize>5) {

				//System.out.println("Trial EFetchResult "+trialCounter);
				//trialCounter++;
				queryResponseConcatenationSize=queryResponseConcatenationSize/2;
				return this.createSequencesMap(ids_list, queryResponseConcatenationSize);
			}
		}
		return null;
	}

	/**
	 * @param ids_list
	 * @param queryResponseConcatenationSize
	 * @return
	 */
	public Pair<Map<String, String>,Map<String, ProteinSequence>> getLocusAndSequencePairFromID(List<List<String>> ids_list, int queryResponseConcatenationSize, String sourceDB){
		try
		{	
			List<List<String>> queryList = new ArrayList<List<String>>();
			List<String> links= new ArrayList<String>();
			int index=0;
			for(int i=0; i<ids_list.size();i++)
			{
				for(int j=0; j<ids_list.get(i).size();j++)
				{
					links.add(ids_list.get(i).get(j).trim());
				}
				index++;
				if(index>=queryResponseConcatenationSize)
				{
					if(!links.isEmpty())
					{
						queryList.add(links);
					}
					links=new ArrayList<String>();
					index=0;
				}
			}
			if(!links.isEmpty())
			{
				queryList.add(links);
			}

			List<Thread> threads = new ArrayList<Thread>();
			ConcurrentLinkedQueue<List<String>> queryArray = new ConcurrentLinkedQueue<List<String>>(queryList);

			int numberOfCores = Runtime.getRuntime().availableProcessors()*2;
			if(queryArray.size()<numberOfCores){numberOfCores=queryArray.size();}
			System.out.println("number Of threads: "+numberOfCores);

			ConcurrentHashMap<String,ProteinSequence> sequences = new ConcurrentHashMap<String,ProteinSequence>();
			ConcurrentHashMap<String,String> locus_Tag = new ConcurrentHashMap<String,String>();

			for(int i=0; i<numberOfCores; i++)
			{
				Runnable lc	= new Runnable_Sequences_retriever(queryArray,locus_Tag,sequences,sourceDB);
				Thread thread = new Thread(lc);
				threads.add(thread);
				System.out.println("Start "+i);
				thread.start();
			}
			for(Thread thread :threads)
			{
				thread.join();
			}

			Map<String,ProteinSequence> sequences_Pair = new HashMap<String,ProteinSequence>(sequences);
			Map<String,String> locus_Tag_Pair = new HashMap<String,String>(locus_Tag);

			return new Pair<Map<String, String>,Map<String, ProteinSequence>>(locus_Tag_Pair,sequences_Pair);
		}
		catch (Exception e) 
		{
			if(queryResponseConcatenationSize>5)
			{
				System.out.println("Reducing concatenation size to:\t"+queryResponseConcatenationSize);
				//trialCounter++;
				queryResponseConcatenationSize=queryResponseConcatenationSize/2;
				return this.getLocusAndSequencePairFromID(ids_list, queryResponseConcatenationSize,sourceDB);
			}
		}
		return null;
	}

	/**
	 * @param query
	 * @param req
	 * @throws RemoteException 
	 * @throws Exception 
	 */
	public NcbiData getSequences(List<String> query, EFetchSequenceServiceStub.EFetchRequest req, String sourceDB, NcbiData ncbiData) throws RemoteException {

		List<String> added = new ArrayList<String>();
		req.setId(query.toString().replace("[", "").replace("]", "").replace(" ", ""));

		EFetchSequenceServiceStub.EFetchResult res = NcbiEFetchSequenceStub_API.getFetchResult(service, req);

		for (int i = 0; i < res.getGBSet().getGBSetSequence().length; i++) {

			EFetchSequenceServiceStub.GBSeq_type0 obj = res.getGBSet().getGBSetSequence()[i].getGBSeq();

			if(sourceDB=="" || obj.getGBSeq_sourceDb().startsWith(sourceDB)) {

				for(int j=0;j<obj.getGBSeq_featureTable().getGBSeq_featureTableSequence().length;j++) {

					if("CDS".equals(obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_key())) {

						for(int k=0;k<obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence().length;k++)
						{
							String name = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_name();
							String value = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_value();
							if("locus_tag".equals(name)) {

								ncbiData.addLocusTag(obj.getGBSeq_accessionVersion(), value);
								ncbiData.addSequence(obj.getGBSeq_accessionVersion(), new ProteinSequence(obj.getGBSeq_sequence().toUpperCase()));
								added.add(obj.getGBSeq_accessionVersion());
							}
						}
					}
				}
				String acc = obj.getGBSeq_accessionVersion();

				if(acc!=null &&!added.contains(acc)) {

					UniProtEntry uniProtEntry = UniProtAPI.getUniProtEntryID(acc,0);
					if(uniProtEntry!=null) {

						if(UniProtAPI.getLocusTag(uniProtEntry)!=null && UniProtAPI.getLocusTag(uniProtEntry).size()>0) {

							String locus = UniProtAPI.getLocusTag(uniProtEntry).get(0).getValue();
							ncbiData.addLocusTag(acc, locus);
							ncbiData.addSequence(acc, new ProteinSequence(obj.getGBSeq_sequence().toUpperCase()));
							added.add(acc);
						}
						else {

							//System.err.println(UniprotAPI.getUniProtEntryID(acc).getUniProtId());
						}
					}
				}
			}
		}

		return ncbiData;
	}

	/**
	 * @param ncbiData
	 * @param isNCBIGenome
	 * @param cancel
	 * @param q
	 * @param req
	 * @param query
	 * @param taxonomyID
	 * @param uniprotStatus 
	 * @return
	 * @throws Exception
	 */
	public HomologuesData getNcbiData(HomologuesData ncbiData, boolean isNCBIGenome, AtomicBoolean cancel, int q,
			EFetchSequenceServiceStub.EFetchRequest req, String query, Map<String, String> taxonomyID, boolean uniprotStatus) throws Exception {

		EFetchSequenceServiceStub.EFetchResult res = NcbiEFetchSequenceStub_API.getFetchResult(NcbiEFetchSequenceStub_API.service, req);

		//System.out.println("start "+ncbiData.getRefSeqGI());
		List<String> accessionsNumbers = new ArrayList<>();
		for (int i = 0; i < res.getGBSet().getGBSetSequence().length; i++) {

			//String uniProtReference = null;

			if(cancel.get()) {

				i=res.getGBSet().getGBSetSequence().length;
			}
			else {

				EFetchSequenceServiceStub.GBSeq_type0 obj = res.getGBSet().getGBSetSequence()[i].getGBSeq();
				String primary_accession=obj.getGBSeq_primaryAccession();

				if(q==0 && i==0 && isNCBIGenome) {

					ncbiData.addEValue(primary_accession,0.0);
					ncbiData.addBits(primary_accession,0.0);
				}
				else {

					if(ncbiData.getBits(primary_accession)<0) {

						String tempID = primary_accession;

						if(tempID.contains(".")) {

							String[] tempID_array=tempID.split("\\.");
							tempID=tempID_array[0];
						}
						else if(ncbiData.getBits(tempID)<0 && tempID.contains("_")) {

							String[] tempID_array=tempID.split("_");

							if(tempID_array[1].length()<2) {

								tempID=tempID.split("_")[0];
							}
						}

						if(ncbiData.getBits(tempID)<0) {

							System.out.println(NcbiEFetchSequenceStub_API.class);
							System.out.println(primary_accession+"\t"+tempID+"\t"+ncbiData.getBits(tempID)+"\t"+ncbiData.getBits(primary_accession)+"\t\t"+ncbiData.getLocus_tag());
							System.out.println(ncbiData.getBits().keySet());
							System.out.println(ncbiData.getBits());
							System.out.println(ncbiData.getBits().size());
							System.out.println();
							//this.initialiseClass();
							throw new Exception("WRONG IDS!");
						}
						primary_accession=tempID;
					}

					ncbiData.addEValue(primary_accession,ncbiData.getEvalue(primary_accession));
					ncbiData.addBits(primary_accession,ncbiData.getBits(primary_accession));
				}
				ncbiData.addLocusID(primary_accession);
				ncbiData.addDefinition(primary_accession, obj.getGBSeq_definition());

				if(q==0 && i==0) {

					if(isNCBIGenome) {

						ncbiData.setOrganismID(obj.getGBSeq_organism());
					}
					else {

						ncbiData.setOrganismID(ncbiData.getTaxonomyID()[0]);
					}
					ncbiData.setFastaSequence(obj.getGBSeq_sequence());
				}

				for(int j=0;j<obj.getGBSeq_featureTable().getGBSeq_featureTableSequence().length;j++) {
					
					if(!accessionsNumbers.contains(obj.getGBSeq_accessionVersion()))
						accessionsNumbers.add(obj.getGBSeq_accessionVersion());

					LinkedList<String> ecn = new LinkedList<String>();
					String key =obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_key();

					if("source".equals(key)) {

						for(int k=0;k<obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence().length;k++) {

							String name = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_name();
							String value = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_value();

							if("db_xref".equals(name)) {

								if(value.startsWith("taxon")) {

									taxonomyID.put(primary_accession, value.replace("taxon:", ""));
								}
							}

							if("chromosome".equals(name)) {

								if(isNCBIGenome) {

									if(q==0 && i==0) {

										ncbiData.setChromosome(value);								
									}

									if(q==0 && i==1 && ncbiData.getChromosome()==null) {

										ncbiData.setChromosome(value);
									}
								}
							}

							if("organelle".equals(name)) {

								if(q==0 && i==0) {

									if(isNCBIGenome) {

										ncbiData.setOrganelle(value);
									}
								}
								else {

									ncbiData.addOganelles(primary_accession, value);
								}
							}

							if("EC_number".equals(name)) {

								ecn.add(value);
							}
						}
					}

					if("Protein".equals(key)) {

						for(int k=0;k<obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence().length;k++) {

							String name = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_name();
							String value = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_value();

							if("product".equals(name)) {

								ncbiData.addProduct(primary_accession, value);
							}

							if("calculated_mol_wt".equals(name)) {

								ncbiData.addCalculated_mol_wt(primary_accession,value);
							}

							if("EC_number".equals(name)) {

								ecn.add(value);
							}

							if("note".equals(name)) {

								ncbiData.setLocus_protein_note(value);
							}
						}
					}					

					if("CDS".equals(key)) {

						for(int k=0;k<obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence().length;k++) {

							String name = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_name();
							String value = obj.getGBSeq_featureTable().getGBSeq_featureTableSequence()[j].getGBFeature().getGBFeature_quals().getGBFeature_qualsSequence()[k].getGBQualifier().getGBQualifier_value();

							if("locus_tag".equals(name)) {

								if(q==0 && i==0) {

									if(isNCBIGenome) {

										ncbiData.setLocus_tag((String) value);
									}
								}
								else {

									ncbiData.addBlastLocusTags(primary_accession, (String) value);
								}
							}

							if("coded_by".equals(name)) {

								if(q==0 && i==0) {

									if(isNCBIGenome) {

										ncbiData.setSequence_code(value.split(":")[1]);
									}
								}
								else {

									if(ncbiData.getLocus_tag()==null && ncbiData.getSequence_code().equalsIgnoreCase(value.split(":")[1])) {

										ncbiData.setLocus_tag(ncbiData.getBlast_locus_tag().get(primary_accession));
									}
								}
							}

							if("note".equals(name) && q==0 && i==0 && !value.contains("")) {

								ncbiData.setLocus_gene_note((String) value);
							}

							if(q==0 && i==1 && ncbiData.getLocus_tag()==null && isNCBIGenome){ //&& locus_gene_note==null && locus_protein_note==null) {

								ncbiData.setLocus_tag(ncbiData.getBlast_locus_tag().get(primary_accession));
							}

							if("gene".equals(name)) {

								if(q==0 && i==0 && isNCBIGenome) {

									String tempGene=(String) value;

									if(tempGene!=null)
										ncbiData.setGene(tempGene);
									else
										ncbiData.setGene("");
								}
								else {

									ncbiData.addGenes(primary_accession, (String) value);
								}
							}

							if(q==0 && i==1 && (ncbiData.getGene()==null || ncbiData.getGene().isEmpty()) && isNCBIGenome) {

								String tempGene=ncbiData.getGenes().get(primary_accession);

								if(tempGene!=null)
									ncbiData.setGene(tempGene);
								else
									ncbiData.setGene("");
							}

							if("db_xref".equals(name)) {

								if(value.startsWith("UniProtKB/Swiss-Prot")) {

									//uniProtReference = value.replace("UniProtKB/Swiss-Prot:", "");
									ncbiData.addUniprotStatus(primary_accession,true);
									accessionsNumbers.remove(obj.getGBSeq_accessionVersion());
								}

								if(value.startsWith("UniProtKB/TrEMBL")) {

									//uniProtReference = value.replace("UniProtKB/TrEMBL:", "");
									ncbiData.addUniprotStatus(primary_accession,false);
									accessionsNumbers.remove(obj.getGBSeq_accessionVersion());
								}	
							}
						}
					}
					if(!ecn.isEmpty()) {

						String[] ecnb = new String[ecn.size()];
						for(int e=0;e<ecn.size();e++){ecnb[e]=ecn.get(e);}
						ncbiData.addECnumbers(primary_accession, ecnb);
					}
				}
				//System.out.println("------------------------------------------");

				if(q==0 && i==0 && ncbiData.getLocus_tag() == null) { 

//					if(uniProtReference == null) {
//
//						UniProtEntry uniProtEntry = UniProtAPI.getEntry(primary_accession, 0);
//
//						if(uniProtEntry!= null && uniProtEntry.getGenes().size()>0 && uniProtEntry.getGenes().get(0).getOrderedLocusNames().size()>0) {
//
//							ncbiData.setLocus_tag(uniProtEntry.getGenes().get(0).getOrderedLocusNames().get(0).getValue());
//							ncbiData.addUniprotStatus(primary_accession, Boolean.valueOf(UniProtAPI.isStarred(uniProtEntry)));
//						}					
//					}
					if(ncbiData.getUniprotLocusTag()!=null)
						ncbiData.setLocus_tag(ncbiData.getUniprotLocusTag());
				}
			}
		}
		//System.out.println("end "+ncbiData.getRefSeqGI());

		if(uniprotStatus && accessionsNumbers.size()>0 && !cancel.get()) {

			//System.out.println("accs "+accessionsNumbers);

			Map<String, List<UniProtEntry>> uniProtEntries = UniProtAPI.getUniprotEntriesFromRefSeq(accessionsNumbers, cancel);

			for(String accessionsNumber:uniProtEntries.keySet()) {

				if(!cancel.get()) {

					//System.out.println("acc "+accessionsNumber);

					for(UniProtEntry uniProtEntry : uniProtEntries.get(accessionsNumber)) {

						String primary_accession = accessionsNumber.split("\\.")[0];

						if(ncbiData.getUniprotStatus().containsKey(primary_accession) && ncbiData.getUniprotStatus().get(primary_accession)) {

							//System.out.println("Exists entry for "+primary_accession+". UniProtEntries size "+uniProtEntries.size());
						}
						else
							ncbiData.addUniprotStatus(primary_accession, UniProtAPI.isStarred(uniProtEntry));
					} }
			}
		}
		return ncbiData;
	}

}
