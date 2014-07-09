package pt.uminho.sysbio.common.bioapis.externalAPI.ncbi;

import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;

import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

/**
 * @author ODias
 *
 */
public class NcbiServiceStub_API {

	private static EUtilsServiceStub service;
	private static int trialCounter;

	/**
	 * @throws AxisFault
	 */
	public NcbiServiceStub_API(int numConnections) throws AxisFault {
		
		trialCounter = 0;
		NcbiServiceStub_API.service = new EUtilsServiceStub();
		NcbiServiceStub_API.service._getServiceClient().getOptions().setProperty(HTTPConstants.HTTP_PROTOCOL_VERSION, HTTPConstants.HEADER_PROTOCOL_10);
		//service._getServiceClient().getOptions().setProperty(HTTPConstants.HTTP_METHOD, HTTPConstants.HTTP_METHOD_POST);
		
		ConfigurationContext context = NcbiServiceStub_API.service._getServiceClient().getServiceContext().getConfigurationContext();
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
	 * @param trialCounter
	 * @return
	 */
	public List<String> getGenomesIDs(int trialCounter, KINGDOM kingdom){

		try 
		{
			EUtilsServiceStub.ESearchRequest req = new EUtilsServiceStub.ESearchRequest();
			//req.setTerm("\"genome taxonomy\"[Filter]");
			//			Bacteria	10081
			//			Archaea	341
			//			Viruses	2951
			//			Viroids	41
			//			Eukaryota	1642

			switch (kingdom){
			case All:
			{
				req.setTerm("\"genome taxonomy\"[Filter]");
				break;
			}
			default: 
			{
				req.setTerm("\"genome taxonomy\"[Filter] AND "+kingdom+"[Organism]");
			}
			}
		
			//req.setTerm("\"below species level\"[Properties]");
			req.setRetMax("100000");
			//req.setDb("taxonomy");
			req.setDb("genome");
			EUtilsServiceStub.ESearchResult res = NcbiServiceStub_API.service.run_eSearch(req);

			int N = res.getIdList().getId().length;

			List<String> list_of_ids = new ArrayList<String>();

			for (int i = 0; i < N; i++)
			{
				String genome=res.getIdList().getId()[i];
				list_of_ids.add(genome);
			}
			return list_of_ids;
		}
		catch(Exception e)
		{
			if(trialCounter<10)
			{
				return this.getGenomesIDs(trialCounter++,kingdom);
			}
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * @param list_of_ids
	 * @param trialCounter
	 * @param originDB
	 * @param objectiveDB
	 * @param queryResponseConcatenationSize
	 * @return
	 */
	public List<List<String>> getLinksList(List<String> list_of_ids, String originDB, String objectiveDB, int queryResponseConcatenationSize){
		try
		{
			List<String> ids_link = new ArrayList<String>();
			EUtilsServiceStub.ELinkRequest req_link = new EUtilsServiceStub.ELinkRequest();
			req_link.setDb(objectiveDB);
			req_link.setDbfrom(originDB);

			List<String> listOfGenomeIDs = new ArrayList<String>();
			int index=0;
			String genome_ids="";
			for(int i=0; i<list_of_ids.size();i++)
			{
				if(index>0)
				{
					genome_ids=genome_ids.concat(",");
				}
				genome_ids=genome_ids.concat(list_of_ids.get(i));
				index++;
				if(index>999)
				{
					listOfGenomeIDs.add(genome_ids);// +"+AND+refseq[Filter]");
					genome_ids="";
					index=0;
				}
			}
			if(!genome_ids.isEmpty())
			{		
				listOfGenomeIDs.add(genome_ids);//+"+AND+refseq[Filter]");
			}

			for(int index_=0;index_<listOfGenomeIDs.size();index_++)
			{
				String[] genomeIDsStringArray = {listOfGenomeIDs.get(index_)};
				req_link.setId(genomeIDsStringArray);
				//System.out.println("txid"+listOfGenomeIDs.get(index_).replace("[", "").replace("[", "").replace(" ", "")+"[Organism:exp]");
				//req_link.setTerm("txid"+listOfGenomeIDs.get(index_).replace("[", "").replace("[", "").replace(" ", "")+"[Organism:exp]");

				EUtilsServiceStub.ELinkResult res_link = NcbiServiceStub_API.getLinkResult(req_link);
				for (int i = 0; i < res_link.getLinkSet().length; i++)
				{
					for (int j = 0; j < res_link.getLinkSet()[i].getLinkSetDb().length; j++)
					{
						for (int k = 0; k < res_link.getLinkSet()[i].getLinkSetDb()[j].getLink().length; k++)
						{
							String org = res_link.getLinkSet()[i].getLinkSetDb()[j].getLink()[k].getId().getString();
							ids_link.add(org);
						}
					}
				}
			}

			List<List<String>> linkList = new ArrayList<List<String>>();
			//			String links="";
			//			index=0;
			//			for(int i=0; i<ids_link.size();i++)
			//			{
			//				if(index>0 && i<ids_link.size())
			//				{
			//					links=links.concat(",");
			//				}
			//				links=links.concat(ids_link.get(i));
			//				index++;
			//				if(index>queryResponseConcatenationSize)
			//				{
			//					linkList.add(links);
			//					links="";
			//					index=0;
			//				}
			//			}
			//			linkList.add(links);
			List<String> links = new ArrayList<String>();
			index=0;
			for(int i=0; i<ids_link.size();i++)
			{
				links.add(ids_link.get(i));
				index++;
				if(index>queryResponseConcatenationSize)
				{
					linkList.add(links);
					links = new ArrayList<String>();
					index=0;
				}
			}
			if(!links.isEmpty())
			{
				linkList.add(links);
			}
			return linkList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(trialCounter<10)
			{
				trialCounter++;
				return this.getLinksList(list_of_ids, originDB,objectiveDB,queryResponseConcatenationSize);
			}
			
		}
		return null;
	}

	/**
	 * @param service
	 * @param req_link
	 * @return
	 */
	private static EUtilsServiceStub.ELinkResult getLinkResult(EUtilsServiceStub.ELinkRequest req_link){
		try
		{
			EUtilsServiceStub.ELinkResult res_link = NcbiServiceStub_API.service.run_eLink(req_link);
			trialCounter = 0;
			return res_link;
		}
		catch(Exception e)
		{
			if(trialCounter<10)
			{
				trialCounter++;
				System.out.println("Trial ELinkResult "+trialCounter);
				return NcbiServiceStub_API.getLinkResult(req_link);
			}
			e.printStackTrace();
		}
		return null;
	}




	public enum KINGDOM{
		Bacteria,
		Eukaryota,
		Archaea,
		Viruses,
		Viroids,
		All
	}
}
