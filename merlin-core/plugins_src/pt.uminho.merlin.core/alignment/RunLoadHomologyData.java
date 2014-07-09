package alignment;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import pt.uminho.sysbio.merge.databases.containers.FastaSequence;
import pt.uminho.sysbio.merge.databases.containers.GeneBlast;
import pt.uminho.sysbio.merge.databases.containers.GeneHomology;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQLMultiThread;

/**
 * @author pedro
 *
 */
public class RunLoadHomologyData implements Runnable {

	private MySQLMultiThread msqlmt;
	private ConcurrentLinkedQueue<GeneBlast> geneBlastList;
	private ConcurrentLinkedQueue<GeneHomology> geneHomologyList;
	private LoadedData loadedData;
	private String blast_setup_key;
	private String homology_setup_key;


	private ConcurrentLinkedQueue<String> geneBlastLocalList;
	private String blastLocal_setup_key;
	private LinkedHashMap<String, LinkedHashMap<String, String[]>> blastlocalHash;
	private ConcurrentLinkedQueue<String> noSimilarities;
	private HashMap<String, FastaSequence> sequenceHash;
	
//	/**
//	 * @param geneBlastList
//	 */
//	@Deprecated
//	public RunLoadHomologyData(ConcurrentLinkedQueue<GeneBlast> geneBlastList, LoadedData loadedData, String blast_setup_key, MySQLMultiThread msqlmt) {
//	
//			this.geneBlastList = geneBlastList;
//			this.geneHomologyList = null;
//			this.geneBlastLocalList = null;
//			this.noSimilarities = null;
//			this.loadedData = loadedData;
//			this.blast_setup_key = blast_setup_key;
//			this.msqlmt = msqlmt;
//		}

	
	/**
	 * @param geneHomologyList
	 * @param loadedData
	 * @param homology_setup_key
	 * @param msqlmt
	 */
	public RunLoadHomologyData(ConcurrentLinkedQueue<GeneHomology> geneHomologyList, LoadedData loadedData, String homology_setup_key, MySQLMultiThread msqlmt) {

		this.geneHomologyList = geneHomologyList;
		this.geneBlastList = null;
		this.geneBlastLocalList = null;
		this.noSimilarities = null;
		this.loadedData = loadedData;
		this.homology_setup_key = homology_setup_key;
		this.msqlmt = msqlmt;
	}

	
	/**
	 * @param sequenceHash
	 * @param geneBlastLocalList
	 * @param blastlocalHash
	 * @param noSimilarities
	 * @param loadedData2
	 * @param blastLocal_setup_key
	 * @param mySQLMultiThread
	 */
	public RunLoadHomologyData(HashMap<String, FastaSequence> sequenceHash, ConcurrentLinkedQueue<String> geneBlastLocalList,LinkedHashMap<String, 
			LinkedHashMap<String, String[]>> blastlocalHash, ConcurrentLinkedQueue<String> noSimilarities, LoadedData loadedData2, String blastLocal_setup_key, 
			MySQLMultiThread mySQLMultiThread) {
		this.geneBlastLocalList = geneBlastLocalList;
		this.noSimilarities = noSimilarities;
		this.blastlocalHash = blastlocalHash;
		this.blastLocal_setup_key = blastLocal_setup_key;
		this.sequenceHash = sequenceHash;
		this.loadedData = loadedData2;
		this.msqlmt = mySQLMultiThread;	
		this.geneBlastList = null;
		this.geneHomologyList = null;
	}



	@SuppressWarnings("deprecation")
	@Override
	public void run() {

		Statement statement;
		try {

			java.sql.Connection conn = this.msqlmt.openConnection();
			statement = conn.createStatement();
			//############# RemoteBlast OLD structure #################
			if(this.geneBlastList != null){
				while(!geneBlastList.isEmpty()) {

					GeneBlast geneBlast = geneBlastList.poll();

					long startTime = System.currentTimeMillis();    

					LoadSimilarityResultstoDatabaseLocal geneList = new LoadSimilarityResultstoDatabaseLocal(statement, this.blast_setup_key, this.loadedData);

					if (!this.loadedData.getGenesMap().containsKey(geneBlast.getQuery())){
						geneList.loadDataRemote(geneBlast);

						long endTime = System.currentTimeMillis();
						System.out.println("Total elapsed time in execution of "+geneBlast.getLocusTag()+" is :"+ String.format("%d min, %d sec", 
								TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) 
								-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));

						System.out.println("Countdown:\t"+geneBlastList.size() + "\t\tGenes map:\t"+ loadedData.getGenesMap().size());
					}

				}
			}

			//############# RemoteBlast NEW structure #################
			if(this.geneHomologyList != null){
				while(!geneHomologyList.isEmpty()) {

					GeneHomology geneHomology = geneHomologyList.poll();

					long startTime = System.currentTimeMillis();    

					LoadSimilarityResultstoDatabaseLocal geneList = new LoadSimilarityResultstoDatabaseLocal(statement, this.homology_setup_key, this.loadedData);

					if (!this.loadedData.getGenesMap().containsKey(geneHomology.getQuery())){
						geneList.loadDataFromMerge(geneHomology);

						long endTime = System.currentTimeMillis();
						System.out.println("Total elapsed time in execution of "+geneHomology.getLocusTag()+" is :"+ String.format("%d min, %d sec", 
								TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) 
								-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));

						System.out.println("Countdown:\t"+geneHomologyList.size() + "\t\tGenes map:\t"+ loadedData.getGenesMap().size());
					}

				}
			}
			//################# Local Blast ##############
			if(this.geneBlastLocalList != null){
				while(!geneBlastLocalList.isEmpty()){

					String geneid = geneBlastLocalList.poll();

					long startTime = System.currentTimeMillis();    

					LoadSimilarityResultstoDatabaseLocal geneList = new LoadSimilarityResultstoDatabaseLocal(statement, this.blastLocal_setup_key, loadedData);

					if(! this.loadedData.getGenesMap().containsKey(geneid)){
						LinkedHashMap<String, String[] >homologuesHash = blastlocalHash.get(geneid);
						FastaSequence sequence = sequenceHash.get(geneid);

						geneList.loadDataFromLocalBlast(geneid, sequence, homologuesHash);

						long endTime = System.currentTimeMillis();
						System.out.println("Total elapsed time in execution of "+geneid+" is\t:"+ String.format("%d min, %d sec", 
								TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) 
								-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));

						System.out.println("Countdown genes with similarities:\t"+geneBlastLocalList.size() + "\t\tGenes map:\t"+ loadedData.getGenesMap().size());
					}

				}

			}
			// ##########  ADD NO SIMILARITY #######
			if(noSimilarities != null){
				if(!noSimilarities.isEmpty() && this.geneBlastLocalList.isEmpty()){
					System.out.println("\nProcessing now the genes without similarities  ...");
					while(!noSimilarities.isEmpty()){


						String geneid = noSimilarities.poll();
						long startTime = System.currentTimeMillis();    

						LoadSimilarityResultstoDatabaseLocal geneList = new LoadSimilarityResultstoDatabaseLocal(statement,blastLocal_setup_key, loadedData);

						if(! this.loadedData.getGenesMap().containsKey(geneid)){

							FastaSequence sequence = sequenceHash.get(geneid);

							geneList.loadNosimilaritiesGenesFromLocalBlast(geneid, sequence);

							long endTime = System.currentTimeMillis();
							System.out.println("Total elapsed time in execution of "+geneid+" is\t:"+ String.format("%d min, %d sec", 
									TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) 
									-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));

							System.out.println("Countdown genes without similarities:\t"+geneBlastLocalList.size() + "\t\tGenes map:\t"+ loadedData.getGenesMap().size());
						}

					}
				}
			}
			conn.close();
			conn = null;
		} 
		catch (SQLException e) {

			e.printStackTrace();
		}
	}

}