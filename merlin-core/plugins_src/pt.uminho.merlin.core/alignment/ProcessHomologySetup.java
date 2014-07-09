package alignment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import alignment.localblast.BlastArguments;
import alignment.localblast.LocalBlast;

import pt.uminho.sysbio.merge.databases.readFromDatabase.RetrieveHomologyData;
import pt.uminho.sysbio.merge.databases.readFromDatabase.RetrieveOldHomologyData;
import pt.uminho.sysbio.merge.databases.containers.BlastSetup;
import pt.uminho.sysbio.merge.databases.containers.FastaSequence;
import pt.uminho.sysbio.merge.databases.containers.GeneBlast;
import pt.uminho.sysbio.merge.databases.containers.GeneHomology;
import pt.uminho.sysbio.merge.databases.containers.HomologySetup;
import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQLMultiThread;

public class ProcessHomologySetup {

	private MySQLMultiThread mySQLMultiThread;

	public ProcessHomologySetup(MySQLMultiThread mySQLMultiThread){

		this.mySQLMultiThread = mySQLMultiThread;
	}

	public ProcessHomologySetup(Connection connection){

		this.mySQLMultiThread = connection.getMysqlMutithread();
	}


//	/**
//	 * @param blastsetupList
//	 * @throws SQLException
//	 * @throws InterruptedException
//	 */
//	public void loadBlastSetup(List<BlastSetup> blastsetupList) throws SQLException, InterruptedException {
//
//		java.sql.Connection conn = this.mySQLMultiThread.openConnection();
//		LoadedData loadedData = new LoadedData(conn.createStatement());
//
//		for (BlastSetup blastSetup : blastsetupList) {
//
//			LoadSimilarityResultstoDatabase loadData = new LoadSimilarityResultstoDatabase(conn.createStatement());
//
//			String blast_setup_key = loadData.loadHomologySetup(blastSetup.getDatabaseID(), blastSetup.getProgram(), blastSetup.getVersion(),
//					blastSetup.geteValue(), blastSetup.getMatrix(), blastSetup.getWordSize(), blastSetup.getGapCosts(),
//					blastSetup.getMaxNumberOfAlignments());
//
//			ConcurrentLinkedQueue<GeneBlast> geneBlastList = blastSetup.getGeneblast();
//
//			int numberOfCores = Runtime.getRuntime().availableProcessors()*3;
//
//			if(geneBlastList.size()<numberOfCores) {
//
//				numberOfCores=geneBlastList.size();
//			}
//			System.out.println("number Of threads: "+numberOfCores);
//			List<Thread> threads = new ArrayList<Thread>();
//
//			for(int i=0; i<numberOfCores; i++) {
//
//				Runnable loadHomologyData = new RunLoadHomologyData(geneBlastList, loadedData, blast_setup_key,this.mySQLMultiThread);
//
//				Thread thread = new Thread(loadHomologyData);
//				threads.add(thread);
//				System.out.println("Start "+i);
//				thread.start();
//			}
//
//			for(Thread thread :threads) {
//
//				thread.join();
//			}
//
//		}
//		conn.close();
//		System.out.println("\n LOADIND DATA TO DATABASE PROCESS IS FINISHED.");
//	}

		public void loadHomologySetup(List<HomologySetup> homologysetupList) throws SQLException, InterruptedException {
	
			java.sql.Connection conn = this.mySQLMultiThread.openConnection();
			LoadedData loadedData = new LoadedData(conn.createStatement());

			for (HomologySetup homologySetup : homologysetupList) {

				LoadSimilarityResultstoDatabaseLocal loadData = new LoadSimilarityResultstoDatabaseLocal(conn.createStatement());

				String homology_setup_key = loadData.loadHomologySetup(homologySetup.getDatabaseID(), homologySetup.getProgram(), homologySetup.getVersion(),
						homologySetup.geteValue(), homologySetup.getMatrix(), homologySetup.getWordSize(), homologySetup.getGapCosts(),
						homologySetup.getMaxNumberOfAlignments());

				ConcurrentLinkedQueue<GeneHomology> geneHomologyList = homologySetup.getGenehomology();

				int numberOfCores = Runtime.getRuntime().availableProcessors()*3;

				if(geneHomologyList.size()<numberOfCores) {

					numberOfCores=geneHomologyList.size();
				}
				System.out.println("number Of threads: "+numberOfCores);
				List<Thread> threads = new ArrayList<Thread>();

				for(int i=0; i<numberOfCores; i++) {

					Runnable loadHomologyData = new RunLoadHomologyData(geneHomologyList, loadedData, homology_setup_key,this.mySQLMultiThread);

					Thread thread = new Thread(loadHomologyData);
					threads.add(thread);
					System.out.println("Start "+i);
					thread.start();
				}

				for(Thread thread :threads) {

					thread.join();
				}

			}
			conn.close();
			System.out.println("\n LOADIND DATA TO DATABASE PROCESS IS FINISHED.");
		}

		
		
	public void loadLocalBlast (HashMap<String, FastaSequence> sequencesHash, LinkedHashMap<String, LinkedHashMap<String, String[]>> blastlocalHash, HomologySetup homologySetup, ConcurrentLinkedQueue<String> noSimilarities) throws SQLException, InterruptedException{

		java.sql.Connection conn = this.mySQLMultiThread.openConnection();
		LoadedData loadedData = new LoadedData(conn.createStatement());
		LoadSimilarityResultstoDatabaseLocal loadData = new LoadSimilarityResultstoDatabaseLocal(conn.createStatement());
		
		ConcurrentLinkedQueue<String> geneBlastLocalList = new ConcurrentLinkedQueue<>();
		
		String blastlocal_setup_key = loadData.loadHomologySetup(homologySetup.getDatabaseID(), homologySetup.getProgram(), homologySetup.getVersion(),
				homologySetup.geteValue(), homologySetup.getMatrix(), homologySetup.getWordSize(), homologySetup.getGapCosts(),
				homologySetup.getMaxNumberOfAlignments());
	

		for (String  geneid : blastlocalHash.keySet()){
			if (! noSimilarities.contains(geneid)){
				geneBlastLocalList.add(geneid);
			}
			
		}


		int numberOfCores = Runtime.getRuntime().availableProcessors()*3;

		if(geneBlastLocalList.size()<numberOfCores) {

			numberOfCores=geneBlastLocalList.size();
		}
		System.out.println("number Of threads: "+numberOfCores);
		List<Thread> threads = new ArrayList<Thread>();

		for(int i=0; i<numberOfCores; i++) {

			Runnable loadHomologyData = new RunLoadHomologyData(sequencesHash,geneBlastLocalList, blastlocalHash, noSimilarities, loadedData, blastlocal_setup_key, mySQLMultiThread);
			Thread thread = new Thread(loadHomologyData);
			threads.add(thread);
			System.out.println("Start "+i);
			thread.start();
		}

		for(Thread thread :threads) {

			thread.join();
		}

		conn.close();
		System.out.println("\nLOADIND DATA TO DATABASE PROCESS IS FINISHED.");

	}






	public static void main (String [] args) throws Exception{

		//######################## MERGE DATABASES OLD STRUCTURE ######################3
		//Connection c1 = (Connection) new datatypes.Connection("192.168.1.143", "3306","HMP", "pedro", "password"); //FILE_3 REMOTELY
		//	Connection c1 = (Connection) new datatypes.Connection("127.0.0.1", "3306","HMP", "root", "password"); // FILE_4 LOCAL
		//	Connection c1 = (Connection) new datatypes.Connection("127.0.0.1", "3306","HMP_FILE1", "root", "password");
		//	Connection c1 = (Connection) new datatypes.Connection("127.0.0.1", "3306","HMP_FILE2", "root", "password");
		//	Connection c1 = (Connection) new datatypes.Connection("127.0.0.1", "3306","HMP_FILE3", "root", "password");
		
//		Connection c1 = (Connection) new datatypes.Connection("192.168.1.80", "3306","rop_pd", "root", "password");
//		Connection c2 = (Connection) new datatypes.Connection("192.168.1.80", "3306","rop_pd_new", "root", "password");
//	
//		
//		RetrieveOldHomologyData r = new RetrieveOldHomologyData(c1);
//		r.retrieveAllData();
//
//		List<BlastSetup> blastSetupList = r.getBlastSetupFromDatabase();
//		System.out.println(blastSetupList);
//
//		ProcessHomologySetup p = new ProcessHomologySetup(c2);
//
//		p.loadBlastSetup(blastSetupList);
		
		
		
		
		//####################### MERGE DATABASES NEW STRUCTURE #####################
		
//		Connection c2 = (Connection) new datatypes.Connection("localhost", "3306","LOCALblast", "root", "password");
//		Connection c1 = (Connection) new datatypes.Connection("localhost", "3306","mergeteste", "root", "password");
//		RetrieveHomologyData r = new RetrieveHomologyData(c2);
//		r.retrieveAllData();
//
//		List<HomologySetup> homologySetupList = r.getHomologySetupFromDatabase();
//		System.out.println(homologySetupList);
//
//		ProcessHomologySetup p = new ProcessHomologySetup(c1);
//
//		p.loadHomologySetup(homologySetupList);	
		
		
		// #################### BLAST LOCAL ###########################
//		String dbName = "LOCALblast";
//		Connection c1 = (Connection) new datatypes.Connection("localhost", "3306",dbName, "root", "password"); 
//		
//		long startTime = System.currentTimeMillis();
//		BlastArguments par  = new BlastArguments("10", 50, "BLOSUM62", "3");
//		String parameters = par.getArguments();
//		LocalBlast teste = new LocalBlast("blastp", parameters);
//		teste.runBlast();
//		
//	
//		LinkedHashMap<String, LinkedHashMap<String, String[]>> blastparse = teste.parseBlastOutput();
//		HashMap< String, FastaSequence > sequencesHash = teste.saveSequences();
//		ConcurrentLinkedQueue<String> noSimilarities = teste.noSimilaritiesGenes;
//		String [] setupInfo = teste.setupInfo;	
//		System.out.println("Retrieving homologues information... This may take a while");
//		LinkedHashMap<String, LinkedHashMap<String, String[]>> hashfinal = teste.retrieveUniprotData(blastparse, teste.uniprotTXTpath);
//	
//		//teste.printHashmap(hashfinal);
//	
//		HomologySetup homologySetup = new HomologySetup(setupInfo[0],setupInfo[1],setupInfo[2], par.getEvalue(), par.getMatrix(), par.getWord_size(),
//				par.getGap_costs(), par.getNum_descriptions());
//		
//		ProcessHomologySetup p = new ProcessHomologySetup(c1);
//		System.out.println("\nLoading now the data into the " + dbName + " database...");
//		p.loadLocalBlast(sequencesHash, hashfinal, homologySetup, noSimilarities);
//		
//		long endTime = System.currentTimeMillis();
//		System.out.println("Total elapsed time in execution of local blast was: "+ String.format("%d min, %d sec", 
//				TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) 
//				-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
	}

}