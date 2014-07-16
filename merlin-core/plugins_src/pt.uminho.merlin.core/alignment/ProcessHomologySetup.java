package alignment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQLMultiThread;
import pt.uminho.sysbio.merge.databases.containers.FastaSequence;
import pt.uminho.sysbio.merge.databases.containers.GeneHomology;
import pt.uminho.sysbio.merge.databases.containers.HomologySetup;

public class ProcessHomologySetup {

	private MySQLMultiThread mySQLMultiThread;
	private static Logger LOGGER = Logger.getLogger(ProcessHomologySetup.class);
	
	
	
	public ProcessHomologySetup(MySQLMultiThread mySQLMultiThread){
		this.mySQLMultiThread = mySQLMultiThread;
	}

	public ProcessHomologySetup(Connection connection){
		this.mySQLMultiThread = connection.getMysqlMutithread();
	}

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
				LOGGER.debug("number Of threads: "+numberOfCores);
				List<Thread> threads = new ArrayList<Thread>();

				for(int i=0; i<numberOfCores; i++) {

					Runnable loadHomologyData = new RunLoadHomologyData(geneHomologyList, loadedData, homology_setup_key,this.mySQLMultiThread);

					Thread thread = new Thread(loadHomologyData);
					threads.add(thread);
					LOGGER.debug("Start "+i);
					thread.start();
				}

				for(Thread thread :threads) {

					thread.join();
				}

			}
			conn.close();
			LOGGER.info("Loading data to internal database finished.");
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
		LOGGER.debug("number Of threads: "+numberOfCores);
		List<Thread> threads = new ArrayList<Thread>();

		for(int i=0; i<numberOfCores; i++) {

			Runnable loadHomologyData = new RunLoadHomologyData(sequencesHash,geneBlastLocalList, blastlocalHash, noSimilarities, loadedData, blastlocal_setup_key, mySQLMultiThread);
			Thread thread = new Thread(loadHomologyData);
			threads.add(thread);
			LOGGER.debug("Start "+i);
			thread.start();
		}

		for(Thread thread :threads) {

			thread.join();
		}

		conn.close();
		LOGGER.debug("Loading data to internal database finished.");

	}


	public static void main (String [] args) throws Exception{


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