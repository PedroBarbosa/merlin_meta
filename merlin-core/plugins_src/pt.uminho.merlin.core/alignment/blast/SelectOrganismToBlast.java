package alignment.blast;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.axis2.AxisFault;

import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.NcbiServiceStub_API;
import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.NcbiTaxonStub_API;
import datatypes.Project;

public class SelectOrganismToBlast {


	public SelectOrganismToBlast(){

	}

	/**
	 * @param project
	 */
	public SelectOrganismToBlast(Project project) {
		//		this.proxy_port = project.getProxy_port();
		//		this.proxy_host = project.getProxy_host();
		//		this.proxy_username=project.getProxy_username();
		//		this.proxy_password=project.getProxy_password();
		//		this.useProxy = project.isUseProxy();
		//		this.useAuthentication = project.isUseAuthentication();
	}

	/**
	 * @param kingdom
	 * @throws RemoteException
	 */
	public void getOrganismList(NcbiServiceStub_API.KINGDOM kingdom) throws Exception {
		
		try  {
			
			NcbiServiceStub_API stub = new NcbiServiceStub_API(2);
			NcbiTaxonStub_API taxon_stub = new NcbiTaxonStub_API(2);
			long startTime = System.currentTimeMillis();

			List<String> list_of_ids = stub.getGenomesIDs(0,kingdom);
			List<List<String>> linkList = stub.getLinksList(list_of_ids, "genome","taxonomy",999);
			Map<String,String[]> result = taxon_stub.getTaxID_and_Superkingdom(linkList, 0);

			List<String> ordered_name = new ArrayList<String>(result.keySet());
			java.util.Collections.sort(ordered_name);
			for(String name : ordered_name) {
				
				System.out.println(name +"\t ncbi id: "+result.get(name)[0] );
				System.out.println();
			}

			long endTime = System.currentTimeMillis();
			System.out.println("Total elapsed time in execution of query is :"+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));

		}
		catch (AxisFault e) {
			
			e.printStackTrace();
		}
	}

//	public static void main(String[] args) throws Exception {
//		String database_name = 
//				//"blast_test_genome2", genomeID =  "284590"; int numberOfHits=100; double eVal = 1E-30;
//				//"blast_cpasteurianum", genomeID =  "cpasteurianum"; int numberOfHits=100;  double eVal = 1E-30;
//				"stpcc", genomeID =  "NC_000915"; int numberOfHits=100;  //double eVal = 1E-30;
//				double eVal = 20000;
//		//"blast_hpylori_big", genomeID =  "NC_000915"; int numberOfHits=10000;  double eVal = 1E-30;
//		//"blast_test", genomeID =  "284590_test"; int numberOfHits=100;  double eVal = 1E-30;
//		//"blast_test2", genomeID =  "284590_test2"; int numberOfHits=100;  double eVal = 1E-30;
//		MySQLMultiThread mysql = new MySQLMultiThread("root","password","localhost",3306,database_name);
//		Database database = new Database(mysql);
//		Project project = new Project(database, database_name);
//		boolean isNCBIGenome=true;
//		long startTime = System.currentTimeMillis();
//		//CreateGenomeFile genomeData = new CreateGenomeFile(genomeID, 90, "REFSEQ");
//		//Map<String, ProteinSequence> sequences = genomeData.getGenome();
//		
//		Map<String, ProteinSequence> sequences = new TreeMap<String, ProteinSequence>();
//		sequences.put("NP_414542.1",new ProteinSequence("mkristtitttitittgngag".toUpperCase().trim()));
//
//		SearchAndLoadHomologueSequences blast_loader = new SearchAndLoadHomologueSequences(sequences, project, isNCBIGenome);
//		//BlastAndLoadSequences blast_loader = new BlastAndLoadSequences(sequences, project,"1501");
//		blast_loader.blastSequences("blastp", "nr", numberOfHits, eVal,true, (short) -1);
//
//		long endTime = System.currentTimeMillis();
//		System.out.println("Total elapsed time in execution of method is :"+ String.format("%d min, %d sec", 
//				TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
//	}
}
