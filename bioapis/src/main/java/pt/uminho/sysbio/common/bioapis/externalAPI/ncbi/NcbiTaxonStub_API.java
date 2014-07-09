package pt.uminho.sysbio.common.bioapis.externalAPI.ncbi;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchTaxonServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchTaxonServiceStub.EFetchResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

/**
 * @author odias
 *
 */
public class NcbiTaxonStub_API {

	private static EFetchTaxonServiceStub service_taxon ;

	/**
	 * @param numConnections
	 * @throws AxisFault
	 */
	public NcbiTaxonStub_API(int numConnections) throws Exception {

		NcbiTaxonStub_API.service_taxon = new EFetchTaxonServiceStub();
		NcbiTaxonStub_API.service_taxon._getServiceClient().getOptions().setProperty(HTTPConstants.HTTP_PROTOCOL_VERSION, HTTPConstants.HEADER_PROTOCOL_10);

		ConfigurationContext context = NcbiTaxonStub_API.service_taxon._getServiceClient().getServiceContext().getConfigurationContext();
		MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager(); 

		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setDefaultMaxConnectionsPerHost(numConnections);

		multiThreadedHttpConnectionManager.setParams(params);

		HttpClient httpClient = new HttpClient(multiThreadedHttpConnectionManager);
		context.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient); 

		context.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, true);
		context.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);
	}


	/**
	 * @param taxonomy_ids_list
	 * @param trialCounter
	 */
	public Map<String,String[]> getTaxID_and_Superkingdom(List<List<String>> taxonomy_ids_list, int trialCounter) {

		Map<String,String[]> result = null;
		try {


			result = this.getTaxID_and_Superkingdom(taxonomy_ids_list);
		} 
		catch(Exception e) {

			int newTrial = trialCounter+1;

			if(newTrial<10) {

				result = null;
				System.out.println("Trial getTaxonList "+newTrial);
				result = this.getTaxID_and_Superkingdom(taxonomy_ids_list, newTrial);
			}
		}
		return result;
	}

	/**
	 * @param taxonomy_ids_list
	 * @param trialCounter
	 * @throws Exception 
	 */
	private Map<String,String[]> getTaxID_and_Superkingdom(List<List<String>> taxonomy_ids_list) throws Exception {

		Map<String,String[]> result = new HashMap<String, String[]>();

		try {

			EFetchTaxonServiceStub.EFetchRequest req_taxon = new EFetchTaxonServiceStub.EFetchRequest();

			for (int index_=0;index_<taxonomy_ids_list.size();index_++) {

				//req_taxon.setId("9685,522328");
				req_taxon.setId(taxonomy_ids_list.get(index_).toString().replace("[", "").replace("]", "").replace(" ", ""));
				EFetchResult res_taxon = NcbiTaxonStub_API.getResult(service_taxon, req_taxon);
				
				for (int i = 0; i < res_taxon.getTaxaSet().getTaxon().length; i++) {

					if(!result.containsKey(res_taxon.getTaxaSet().getTaxon()[i].getScientificName())) {

						String[] array = new String[2];
						array[0] = res_taxon.getTaxaSet().getTaxon()[i].getTaxId();
						//array[1] = res_taxon.getTaxaSet().getTaxon()[i].getLineage();

						if(res_taxon.getTaxaSet().getTaxon()[i].getLineageEx().getTaxon()!=null) {

							//j = 0 cellular organism, thus start with j = 1
							for (int j = 1; j < res_taxon.getTaxaSet().getTaxon()[i].getLineageEx().getTaxon().length; j++) {

								if(res_taxon.getTaxaSet().getTaxon()[i].getLineageEx().getTaxon()[j].getRank().toString().equalsIgnoreCase("superkingdom")) {

									array[1] = res_taxon.getTaxaSet().getTaxon()[i].getLineageEx().getTaxon()[j].getScientificName();
								}
							}
						}
						result.put(res_taxon.getTaxaSet().getTaxon()[i].getScientificName(), array);
					}
				}
			}
			return result;
		}
		catch(Exception e) {

			throw e;
		}
	}

	/**
	 * @param taxonomy_ids
	 * @param trialCounter
	 * @throws Exception 
	 */
	public Map<String,String[]> getTaxonList(String taxonomy_ids) throws Exception {

		return this.getTaxonList(taxonomy_ids,0);
	}

	/**
	 * @param taxonomy_ids
	 * @param trialCounter
	 * @throws Exception 
	 */
	public Map<String,String[]> getTaxonList(String taxonomy_ids, int trialCounter) throws Exception {

		Map<String,String[]> result = null;
		try {

			result = this.getTaxonListMethod(taxonomy_ids);
		}
		catch (Error e) {

			int newTrial = trialCounter+1;
			result = null;
			if(newTrial<10) {

				System.err.println("Trial getTaxonList "+newTrial);
				result = this.getTaxonList(taxonomy_ids, newTrial);
			}
			else {

				e.printStackTrace();
				//throw new Error("Service unavailable");
				throw e;
			}
		}
		catch(Exception e) {

			int newTrial = trialCounter+1;
			result = null;
			if(newTrial<10) {

				System.err.println("Trial getTaxonList "+newTrial);
				result = this.getTaxonList(taxonomy_ids, newTrial);
			}
			else {

				e.printStackTrace();
				throw e;
			}
		}
		return result;
	}

	/**
	 * @param taxonomy_ids
	 * @param trialCounter
	 * @throws Exception 
	 */
	private Map<String,String[]> getTaxonListMethod(String taxonomy_ids) throws Exception {

		Map<String,String[]> result = new HashMap<String, String[]>();

		try {

			
			String[] array = new String[2];
			EFetchTaxonServiceStub.EFetchRequest req_taxon = new EFetchTaxonServiceStub.EFetchRequest();
			req_taxon.setId(taxonomy_ids);
			EFetchResult res_taxon = NcbiTaxonStub_API.getResult(service_taxon, req_taxon);

			for (int i = 0; i < res_taxon.getTaxaSet().getTaxon().length; i++) {

				array[0] = res_taxon.getTaxaSet().getTaxon()[i].getScientificName();
				array[1] = "";

				if(res_taxon.getTaxaSet().getTaxon()[i].getLineageEx()!=null) {

					if(res_taxon.getTaxaSet().getTaxon()[i].getLineageEx().getTaxon()!=null) {

						//j = 0 cellular organism, thus start with j = 1
						for (int j = 1; j < res_taxon.getTaxaSet().getTaxon()[i].getLineageEx().getTaxon().length; j++) {

							array[1]+= res_taxon.getTaxaSet().getTaxon()[i].getLineageEx().getTaxon()[j].getScientificName();
							if(j<res_taxon.getTaxaSet().getTaxon()[i].getLineageEx().getTaxon().length-1) {

								array[1]+= "; ";
							}
						}
					}
				}

				result.put(res_taxon.getTaxaSet().getTaxon()[i].getTaxId(), array);
				array = new String[2];
			}
			return result;
		}
		catch (Error e) {

			//e.printStackTrace();
			throw new Error("Service unavailable!");
		}
		catch(Exception e) {

			//e.printStackTrace();
			throw e; 
		}
	}

	/**
	 * @param service_taxon
	 * @param req_taxon
	 * @param trialCounter
	 * @return
	 * @throws Exception 
	 */
	private static EFetchResult getResult(EFetchTaxonServiceStub service_taxon, EFetchTaxonServiceStub.EFetchRequest req_taxon) throws Exception {

		try {

			EFetchResult res_taxon = service_taxon.run_eFetch(req_taxon);
			return res_taxon;
		}
		catch (Error e) {

			//e.printStackTrace();
			throw new Error("Service unavailable!");
		}
		catch(Exception e) {

			throw e;
		}
	}
}
