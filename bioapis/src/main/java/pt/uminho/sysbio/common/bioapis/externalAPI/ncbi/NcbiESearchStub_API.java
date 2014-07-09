/**
 * 
 */
package pt.uminho.sysbio.common.bioapis.externalAPI.ncbi;

import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.ESearchResult;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

/**
 * @author oscar
 *
 */
public class NcbiESearchStub_API {

	private static EUtilsServiceStub service;
	private static int  trialCounter=0;
	/**
	 * @throws AxisFault
	 */
	public NcbiESearchStub_API(int numConnections) throws AxisFault {

		NcbiESearchStub_API.service = new EUtilsServiceStub();
		NcbiESearchStub_API.service._getServiceClient().getOptions().setProperty(HTTPConstants.HTTP_PROTOCOL_VERSION, HTTPConstants.HEADER_PROTOCOL_10);
		//service._getServiceClient().getOptions().setProperty(HTTPConstants.HTTP_METHOD, HTTPConstants.HTTP_METHOD_POST);

		ConfigurationContext context = NcbiESearchStub_API.service._getServiceClient().getServiceContext().getConfigurationContext();
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
	public static EUtilsServiceStub.ESearchResult getSearchResult(EUtilsServiceStub service, EUtilsServiceStub.ESearchRequest req) throws RemoteException {

		try {

			EUtilsServiceStub.ESearchResult res = service.run_eSearch(req);
			return res;
		}
		catch(Exception e) {

			if(trialCounter<10) {

				NcbiESearchStub_API.service = new EUtilsServiceStub();
				System.out.println("Trial EFetchResult\t"+trialCounter+" for request\t"+req.getTerm());
				trialCounter++;
				return NcbiESearchStub_API.getSearchResult(service, req);
			}
			e.printStackTrace();
			throw new RemoteException();
		}
	}

	/**
	 * @param database
	 * @param ids
	 * @param queryResponseConcatenationSize
	 * @param trialNumber
	 * @return
	 * @throws RemoteException 
	 */
	public List<String> getDatabaseIDs(String database, List<String> ids, int queryResponseConcatenationSize, int trialNumber) throws RemoteException {

		try {

			List<String> result = new ArrayList<String>();
			// call NCBI ESearch utility
			// NOTE: search term should be URL encoded
			EUtilsServiceStub.ESearchRequest req = new EUtilsServiceStub.ESearchRequest();
			req.setDb(database);

			List<String> queryList = new ArrayList<String>();
			String results="";
			int index = 0;

			for(int i=0; i<ids.size();i++) {

				if(index>0) {

					results=results.concat(",");
				}

				String id=ids.get(i);
				if(id.contains("|")) {

					String[] idString = id.split("\\|");
					id=idString[1];
					if(id.isEmpty()) {

						if(idString[2].isEmpty()) {

							id = ids.get(i);
							System.err.println(NcbiESearchStub_API.class+" weird id "+id);
						}
						else {

							id = idString[2];
						}
					}
					else if(idString[0].equalsIgnoreCase("pdb")) {

						id=id.concat("_").concat(idString[2]);
					}
				}

				results=results.concat(id);
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

			for(String query : queryList) {
				
				//req.setTerm(queryList.toString().replace("[", "").replace("]", ""));
				req.setTerm(query.toString().replace("[", "").replace("]", ""));
				int querySize = ids.size(); 
				req.setRetMax(querySize+"");

				ESearchResult res = NcbiESearchStub_API.getSearchResult(service, req);
				
				if(res.getIdList().isIdSpecified()){

					// results output
					for (int i = 0; i < res.getIdList().getId().length; i++) {

						result.add(res.getIdList().getId()[i]);
					}
				}
			}
			return result;
		} 
		catch (RemoteException e) {

			if(trialNumber<10) {

				NcbiESearchStub_API.myWait(3000);
				trialNumber=trialNumber+1;
				return this.getDatabaseIDs(database, ids, queryResponseConcatenationSize, trialNumber);
			}
			else {

				e.printStackTrace();
				throw e;
			}
		}
	}

	/**
	 * @param xMilliseconds
	 */
	public static void myWait(long xMilliseconds) {

		try {

			//System.out.println("Thread.sleep(xMilliseconds);
			Thread.sleep(xMilliseconds);
		}
		catch(InterruptedException e) {

			e.printStackTrace();
		}
	}
}
