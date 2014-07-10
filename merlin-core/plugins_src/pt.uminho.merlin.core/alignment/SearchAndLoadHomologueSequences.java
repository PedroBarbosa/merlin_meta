/**
 * 
 */
package alignment;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import operations.HmmerSimilaritySearch.HmmerDatabase;

import org.biojava3.core.sequence.ProteinSequence;

import pt.uminho.sysbio.common.bioapis.externalAPI.uniprot.UniProtAPI;
import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.local.alignments.core.PairwiseSequenceAlignement.Matrix;
import pt.uminho.sysbio.merlin.utilities.DatabaseProgressStatus;
import pt.uminho.sysbio.merlin.utilities.MySleep;
import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;
import alignment.blast.SubmitEbiBlast;
import alignment.blast.SubmitNcbiBlast;
import alignment.blast.ebi.rest.EbiBlastClientRest;
import alignment.blast.org.biojava3.ws.alignment.RemotePairwiseAlignmentService;
import alignment.blast.org.biojava3.ws.alignment.qblast.NCBIQBlastAlignmentProperties;
import alignment.blast.org.biojava3.ws.alignment.qblast.NcbiBlastService;
import alignment.hmmer.SubmitHMMER;
import datatypes.Project;

/**
 * @author ODias
 *
 */
public class SearchAndLoadHomologueSequences {

	/**
	 * 
	 */
	private ConcurrentLinkedQueue<String> blosum62, blosum80, pam30, pam70, smaller, otherSequences;
	private short gapExtensionPenalty, gapOpenPenalty;
	private Project project;
	private Set<String> loadedGenes;
	private boolean isNCBIGenome;
	private String[] orgArray= null;
	private short wordSize;
	private Matrix blastMatrix;
	private Map<String, ProteinSequence> sequenceFile;
	private String organism;
	private AtomicBoolean cancel;
	private TimeLeftProgress progress;
	private long startTime;
	private boolean similaritySearchProcessAvailable;
	private int sequences_size;
	private ArrayList<Runnable> runnables;
	private AtomicInteger sequencesCounter;
	private ConcurrentHashMap<String,String[]> taxonomyMap;
	private ConcurrentHashMap<String, Boolean> uniprotStar;
	private int geneticCode;
	private long latencyWaitingPeriod;
	private boolean uniprotStatus;
	private int sequencesWithErrors;
	private Source source;


	/**
	 * @param sequences
	 * @param project
	 * @param isNCBIGenome
	 * @throws Exception
	 */
	public SearchAndLoadHomologueSequences(Map<String, ProteinSequence> sequences, Project project, boolean isNCBIGenome, Source source) throws Exception {

		this.gapExtensionPenalty=-1;
		this.gapOpenPenalty=-1;
		this.wordSize=-1;
		this.blastMatrix = null;
		this.wordSize = -1;
		this.organism=null;
		this.isNCBIGenome = isNCBIGenome;
		this.project=project;
		this.sequenceFile = sequences;
		this.cancel = new AtomicBoolean(false);
		this.startTime = GregorianCalendar.getInstance().getTimeInMillis();
		this.similaritySearchProcessAvailable=true;
		this.sequencesCounter = new AtomicInteger(0);
		this.taxonomyMap = new ConcurrentHashMap<String, String[]>();
		this.uniprotStar = new ConcurrentHashMap<String, Boolean>();
		this.geneticCode = -1;
		this.source = source;
	}

	/**
	 * @param orgTaxonomyID
	 * @throws Exception 
	 */
	public void setTaxonomicID(String orgTaxonomyID, HmmerDatabase database) throws Exception {

		String[] orgData = new String[2];
		orgData[0] = this.project.getOrganismName();
		orgData[1] = this.project.getOrganismLineage();
		this.orgArray = orgData;

		if(database.equals(HmmerDatabase.uniprotkb)
				|| database.equals(HmmerDatabase.unimes)
				|| database.equals(HmmerDatabase.uniprotrefprot)
				)
			this.orgArray = this.ebiNewTaxID(this.orgArray[0]);


		this.taxonomyMap.put(orgTaxonomyID, this.orgArray);
	}

	/**
	 * @param orgTaxonomyID
	 * @throws Exception 
	 */
	public void setTaxonomicID(String orgTaxonomyID) throws Exception {
		
		String[] orgData = new String[2];
		orgData[0] = this.project.getOrganismName();
		orgData[1] = this.project.getOrganismLineage();
		this.orgArray = orgData;
		
		if(Source.ebi.equals(this.source))
			this.orgArray = this.ebiNewTaxID(this.orgArray[0]);

		this.taxonomyMap.put(orgTaxonomyID, this.orgArray);
	}

	/**
	 * @param conn
	 * @param eVal
	 * @param matrix
	 * @param numberOfAlignments
	 * @param program
	 * @return
	 */
	private Set<String> getGenesFromDatabase(Connection conn, double eVal, Matrix matrix, int numberOfAlignments, String program) {

		Set<String> loadedGenes = new HashSet<String>();
		Statement statement;

		String matrix_string = null;

		if(matrix != null) {

			matrix_string = matrix.toString().toUpperCase();
		}

		try  {

			statement = conn.createStatement();

			Set<String> deleteGenes = new HashSet<String>();
			deleteGenes.addAll(this.getProcessingGenes(statement, program));

			ResultSet rs =statement.executeQuery("SELECT query, program " +
					"FROM geneHomology LEFT JOIN homologySetup ON (homologySetup.s_key = homologySetup_s_key) " +
					"WHERE status = '"+DatabaseProgressStatus.PROCESSED +"'"+
					";");

			while(rs.next()) {

				if(rs.getString(2).contains(program) ) {

					loadedGenes.add(rs.getString(1));
				}
			}

			rs =statement.executeQuery(
					"SELECT query, program FROM geneHomology " +
							"LEFT JOIN homologySetup ON (homologySetup.s_key = homologySetup_s_key) " +
							"WHERE status = '"+DatabaseProgressStatus.NO_SIMILARITY+"' "
					);

			while(rs.next()) {

				if(rs.getString(2).contains(program) ) {

					loadedGenes.add(rs.getString(1));
				}
			}

			rs =statement.executeQuery("SELECT geneHomology.s_key, query, program  " +
					"FROM geneHomology " +
					"LEFT JOIN homologySetup ON (homologySetup.s_key = homologySetup_s_key) " +
					"WHERE status = '"+DatabaseProgressStatus.NO_SIMILARITY+"' " +
					"AND eValue > "+eVal+" " +
					"AND matrix = '"+matrix_string+"' " +
					"AND wordSize = '"+this.wordSize+"' " +
					"AND maxNumberOfAlignments = '"+numberOfAlignments+"';");

			while(rs.next()) {

				if(rs.getString(3).contains(program) ) {

					loadedGenes.remove(rs.getString(2));
					deleteGenes.add(rs.getString(1));
				}
			}

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			rs =statement.executeQuery("SELECT query, program FROM geneHomology " +
					"LEFT JOIN homologySetup ON (homologySetup.s_key = homologySetup_s_key) " +
					"WHERE status = '"+DatabaseProgressStatus.PROCESSED+"' " +
					"AND matrix = '"+matrix_string+"' " +
					"AND wordSize = '"+this.wordSize+"';");

			while(rs.next()) {

				if(rs.getString(2).contains(program) ) {

					loadedGenes.add(rs.getString(1));
				}
			}

			rs =statement.executeQuery("SELECT geneHomology.s_key, COUNT(referenceID), query, program " +
					"FROM homologySetup " +
					"LEFT JOIN geneHomology ON (homologySetup.s_key = homologySetup_s_key) " +
					"LEFT JOIN geneHomology_has_homologues ON (geneHomology.s_key = geneHomology_s_key) " +
					"WHERE status='PROCESSED' " +
					"AND geneHomology_has_homologues.eValue <= "+eVal+"  " +
					"AND homologySetup.eValue >  "+eVal+"  " +
					"AND matrix = '"+matrix_string+"' AND wordSize = '"+this.wordSize+"' " +
					"GROUP BY geneHomology.s_key;");

			while(rs.next()) {

				if(rs.getInt(2)<numberOfAlignments && rs.getString(4).contains(program) ) {

					//System.out.println(rs.getString(1)+"\t"+rs.getString(3)+"\t"+rs.getString(2));
					loadedGenes.remove(rs.getString(3));
					deleteGenes.add(rs.getString(1));
				}
			}
			//			System.out.println(loadedGenes);
			//			System.out.println(deleteGenes);
			//			System.out.println(loadedGenes.size());
			//			System.out.println(deleteGenes.size());
			//			System.out.println();

			this.deleteSetOfGenes(deleteGenes, statement);

			rs.close();
			statement.close();
			statement=null;
		}
		catch (SQLException e) {

			System.out.println("search and load homologue sequences SQL connection error!");
			e.printStackTrace();
			return null;
		}
		return loadedGenes;
	}

	/**
	 * @param map
	 * @return
	 */
	public ConcurrentLinkedQueue<String> getRequestsList(Map<String, ProteinSequence> map) {
		ConcurrentLinkedQueue<String> result = new ConcurrentLinkedQueue<String>();
		String request="";

		String beginning = "%3E", returnCode="%0D%0A";

		if(this.source==Source.ebi) {

			beginning = ">";
			returnCode="\n";
		}

		for(String key: map.keySet()) {

			if(!(this.getLoadedGenes()!=null && this.getLoadedGenes().contains(key))) {
				this.sequences_size++;
				request += beginning+key.trim()+returnCode;
				request += map.get(key).getSequenceAsString()+"%3E";
				result.add(request);
				request="";
			}
		}
		return result;
	} 

	/**
	 * @param program
	 * @param database
	 * @param numberOfAlignments
	 * @param expectedVal
	 * @param wordSize
	 * @param requests
	 * @param matrix
	 * @param gapExtensionPenalty
	 * @param gapOpenPenalty
	 * @throws Exception 
	 */
	private int blastSingleSequenceNcbi(String program, String database, int numberOfAlignments, double expectedVal, short wordSize, ConcurrentLinkedQueue<String> requests,
			Matrix matrix, short gapExtensionPenalty, short gapOpenPenalty) throws Exception {

		sequencesWithErrors = 0;

		Map<String,String> queryRIDMap = new HashMap<String, String>();

		NCBIQBlastAlignmentProperties rqb = new NCBIQBlastAlignmentProperties();
		rqb.setBlastProgram(program);
		rqb.setBlastDatabase(database);
		rqb.setBlastExpect(expectedVal);
		rqb.setBlastMatrix(matrix.toString().toUpperCase());
		if(this.geneticCode>0) {

			rqb.setGeneticCode(this.geneticCode);
		}

		if(gapOpenPenalty!=-1) {

			rqb.setBlastGapCreation(gapOpenPenalty);
		}

		if(gapExtensionPenalty!=-1) {

			rqb.setBlastGapExtension(gapExtensionPenalty);
		}

		rqb.setBlastWordSize(wordSize);
		rqb.setHitlistSize(numberOfAlignments);

		if(this.organism!=null) {

			System.out.println("Setting organism to: "+this.organism);
			rqb.setOrganism(this.organism);
		}

		if(!this.cancel.get()) {

			AtomicInteger errorCounter = new AtomicInteger(0);

			int threadsNumber=0;
			int numberOfCores = Runtime.getRuntime().availableProcessors()*4;
			List<Thread> threads = new ArrayList<Thread>();
			this.runnables = new ArrayList<Runnable>();

			if(requests.size()<numberOfCores) {

				threadsNumber=requests.size();
			}
			else {

				threadsNumber=numberOfCores;
			}

			List<ConcurrentLinkedQueue<String>> rids = new ArrayList<ConcurrentLinkedQueue<String>>();
			NcbiBlastService[] ncbiBlastServiceArray = new NcbiBlastService[threadsNumber];

			int t=0;
			while( t<threadsNumber) {

				ConcurrentLinkedQueue<String> rid = new ConcurrentLinkedQueue<String>();
				rids.add(t,rid);
				ncbiBlastServiceArray[t] = new NcbiBlastService(30000, this.project);
				t++;
			}

			t=0;
			while(!requests.isEmpty() && !this.cancel.get()) {

				if(!this.similaritySearchProcessAvailable || this.cancel.get()) {

					requests.clear();
				}

				if(!requests.isEmpty()) {

					String query=requests.poll();

					this.processQuery(query, ncbiBlastServiceArray[t], rqb, rids.get(t), queryRIDMap, 0);
					t++;
					if(t>=threadsNumber) {

						t=0;
					}
				}
			}

			if(this.similaritySearchProcessAvailable  && !this.cancel.get() && queryRIDMap.size()>0) {

				for(int i=0; i<threadsNumber; i++) {

					Runnable lc	= new SubmitNcbiBlast(ncbiBlastServiceArray[i], rids.get(i), numberOfAlignments, this.project, queryRIDMap, rqb, 
							this.isNCBIGenome, this.orgArray, this.cancel, this.sequences_size, this.startTime, this.progress, 
							i,this.sequencesCounter, this.taxonomyMap, this.uniprotStar, errorCounter, uniprotStatus, this.latencyWaitingPeriod);
					Thread thread = new Thread(lc);
					this.runnables.add(lc);
					threads.add(thread);
					System.out.println("Start "+i);
					thread.start();
				}

				for(Thread thread :threads) {

					thread.join();
				}

				//System.out.println("Error Count "+ errorCount);

				if(errorCounter.get()>0) {

					sequencesWithErrors += errorCounter.get();
					//System.out.println("\tError Count "+ errorCount);
					//Workbench.getInstance().error("Errors have ocurred while processsing "+errorCounter+" query(ies).");
					errorCounter.set(0);
					//this.similaritySearchProcessAvailable = false;
				}
			}
		}

		return sequencesWithErrors;
	}

	/**
	 * @param program
	 * @param database
	 * @param numberOfAlignments
	 * @param expectedVal
	 * @param wordSize
	 * @param requests
	 * @param matrix
	 * @param gapExtensionPenalty
	 * @param gapOpenPenalty
	 * @return
	 * @throws Exception
	 */
	private int blastSingleSequenceEbi(String program, String database, int numberOfAlignments, double expectedVal, ConcurrentLinkedQueue<String> requests,
			Matrix matrix, short gapExtensionPenalty, short gapOpenPenalty) throws Exception {

		sequencesWithErrors = 0;

		Map<String,String> queryRIDMap = new HashMap<String, String>();

		NCBIQBlastAlignmentProperties rqb = new NCBIQBlastAlignmentProperties();
		rqb.setBlastProgram(program);
		rqb.setBlastDatabase(database);
		rqb.setBlastExpect(expectedVal);
		rqb.setBlastMatrix(matrix.toString().toUpperCase());

		if(gapOpenPenalty!=-1) {

			rqb.setBlastGapCreation(gapOpenPenalty);
		}

		if(gapExtensionPenalty!=-1) {

			rqb.setBlastGapExtension(gapExtensionPenalty);
		}

		rqb.setHitlistSize(numberOfAlignments);

		if(this.organism!=null) {

			System.out.println("Setting organism to: "+this.organism);
			rqb.setOrganism(this.organism);
		}

		if(!this.cancel.get()) {

			AtomicInteger errorCounter = new AtomicInteger(0);

			int threadsNumber=0;
			int numberOfCores = Runtime.getRuntime().availableProcessors()*4;
			List<Thread> threads = new ArrayList<Thread>();
			this.runnables = new ArrayList<Runnable>();

			if(requests.size()<numberOfCores) {

				threadsNumber=requests.size();
			}
			else {

				threadsNumber=numberOfCores;
			}

			List<ConcurrentLinkedQueue<String>> rids = new ArrayList<ConcurrentLinkedQueue<String>>();
			EbiBlastClientRest[] rbwArray = new EbiBlastClientRest[threadsNumber];

			int t=0;
			while( t<threadsNumber) {

				ConcurrentLinkedQueue<String> rid = new ConcurrentLinkedQueue<String>();
				rids.add(t,rid);
				rbwArray[t] = new EbiBlastClientRest(30000, this.project);
				t++;
			}

			t=0;
			while(!requests.isEmpty() && !this.cancel.get()) {

				if(!this.similaritySearchProcessAvailable || this.cancel.get()) {

					requests.clear();
				}

				if(!requests.isEmpty()) {

					String query=requests.poll();

					this.processQuery(query, rbwArray[t], rqb, rids.get(t), queryRIDMap, 0);
					t++;
					if(t>=threadsNumber) {

						t=0;
					}
				}
			}

			if(this.similaritySearchProcessAvailable  && !this.cancel.get() && queryRIDMap.size()>0) {

				for(int i=0; i<threadsNumber; i++) {

					Runnable lc	= new SubmitEbiBlast(rbwArray[i], rids.get(i), numberOfAlignments, this.project, queryRIDMap, rqb, 
							this.isNCBIGenome, this.orgArray, this.cancel, this.sequences_size, this.startTime, this.progress, 
							i,this.sequencesCounter, this.taxonomyMap, this.uniprotStar, errorCounter, uniprotStatus, this.latencyWaitingPeriod);
					Thread thread = new Thread(lc);
					this.runnables.add(lc);
					threads.add(thread);
					System.out.println("Start "+i);
					thread.start();
				}

				for(Thread thread :threads) {

					thread.join();
				}

				if(errorCounter.get()>0) {

					sequencesWithErrors += errorCounter.get();
					errorCounter.set(0);
				}
			}
		}

		return sequencesWithErrors;
	}

	/**
	 * @param sequence
	 * @param rbwArray
	 * @param rqb
	 * @param rids
	 * @param queryRIDMap
	 * @param counter
	 * @return
	 */
	private String processQuery(String sequence, RemotePairwiseAlignmentService rbwArray, NCBIQBlastAlignmentProperties rqb,
			ConcurrentLinkedQueue<String> rids, Map<String,String> queryRIDMap, int counter) {

		try {

			String newRid = rbwArray.sendAlignmentRequest(sequence,rqb);

			if(newRid == null) {

				if(counter<30) {

					counter++;
					return this.processQuery(sequence, rbwArray, rqb, rids, queryRIDMap, counter);
				}
				else {

					System.out.println("Error getting rid for sequence \t"+sequence);
				}
			}
			else {

				rids.offer(newRid);
				queryRIDMap.put(newRid, sequence);
				return newRid;
			}

			// http://www.ncbi.nlm.nih.gov/staff/tao/URLAPI/new/node96.html
			// b. For URLAPI scripts, do NOT send requests faster than once every 3 seconds. 
			MySleep.myWait(3000);
		}
		catch (IOException e) {

			e.printStackTrace();

			counter++;

			if(counter<50) {

				return this.processQuery(sequence, rbwArray, rqb, rids, queryRIDMap, counter);
			}
			else {

				System.out.println("Search and load homologue sequences. IO exception request for "+sequence+" Aborting.");
			}
		}
		catch (Exception e) {



			counter++;

			if(e!=null && e.getMessage()!=null && e.getMessage().contains("NCBI QBlast refused this request because")) {

				if(counter<50) {

					MySleep.myWait(3000);
					return this.processQuery(sequence, rbwArray, rqb, rids, queryRIDMap, counter);
				}
				else {

					System.err.println("Search and load homolgue sequences. NCBI QBlast refused this request for "+sequence+"\n" +
							"because: "+e.getMessage().replace("<ul id=\"msgR\" class=\"msg\"><li class=\"error\"><p class=\"error\">", "")+" Aborting.");
				}
			}
			else if(e!=null && e.getMessage()!=null && e.getMessage().contains("Cannot get RID for alignment")) {

				if(counter<50) {

					MySleep.myWait(3000);
					return this.processQuery(sequence, rbwArray, rqb, rids, queryRIDMap, counter);
				}
				else {

					System.out.println("Search and load homolgue sequences. Cannot get RID for sequence "+sequence+"\nRetrying query! Trial\t"+counter);
				}
			}
			else {

				if(counter<50) {

					MySleep.myWait(3000);
					return this.processQuery(sequence, rbwArray, rqb, rids, queryRIDMap, counter);
				}
				else {

					System.out.println("Message: "+e.getMessage());
					e.printStackTrace();
				}
			}

		}

		System.err.println("Search and load homolgue sequences. BLAST Failed for sequence:\t"+sequence);
		sequencesWithErrors++;

		//Workbench.getInstance().error("Cannot perform BLAST at this time, try again later!");
		this.similaritySearchProcessAvailable = false;

		return null;
	}


	/**
	 * @param program
	 * @param database
	 * @param numberOfAlignments
	 * @param eVal
	 * @param eValueAutoAdjust
	 * @param word
	 * @return
	 * @throws Exception
	 */
	public int blastSequencesNCBI(String program, String database, int numberOfAlignments, double eVal, boolean eValueAutoAdjust, short word) throws Exception {

		try {

			int errorCount = 0;

			Connection conn = new Connection(this.project.getDatabase().getMySqlCredentials());
			Matrix matrix;
			//int maxRequests = Runtime.getRuntime().availableProcessors()*2*10;
			int maxRequests = 50;

			if(blastMatrix==null) {

				Map<String, ProteinSequence> smaller = new HashMap<String, ProteinSequence>();
				Map<String, ProteinSequence> pam30 = new HashMap<String, ProteinSequence>();
				Map<String, ProteinSequence> pam70 = new HashMap<String, ProteinSequence>();
				Map<String, ProteinSequence> blosum62 = new HashMap<String, ProteinSequence>();
				Map<String, ProteinSequence> blosum80 = new HashMap<String, ProteinSequence>();

				int unitLength = 1;

				if(!program.equalsIgnoreCase("blastp") ) {

					unitLength=3;

					if(eVal==1E-30) {

						eVal = 10;
					}
				}
			
				for(String key:this.sequenceFile.keySet()) {

					int seqSize = this.sequenceFile.get(key).getLength()/unitLength;

					if(seqSize<15){smaller.put(key,this.sequenceFile.get(key));}
					else if(seqSize<35){pam30.put(key,this.sequenceFile.get(key));}
					else if(seqSize<50){pam70.put(key,this.sequenceFile.get(key));}
					else if(seqSize<85){blosum80.put(key,this.sequenceFile.get(key));}
					else{blosum62.put(key,this.sequenceFile.get(key));} 
				}

				if(!this.cancel.get()) {

					this.wordSize = word;
					matrix = this.selectMatrix(86);
					this.setLoadedGenes(this.getGenesFromDatabase(conn, eVal, matrix,numberOfAlignments, program));
					this.setBlosum62(this.getRequestsList(blosum62));

					if(this.blosum62.size()>0 && !this.cancel.get()) {

						errorCount += this.blastProcessGenesListNcbi(this.blosum62, program, database, numberOfAlignments, eVal, matrix, maxRequests);
					}
				}

				if(!this.cancel.get()) {

					this.wordSize = word;
					matrix = this.selectMatrix(80);
					this.setLoadedGenes(this.getGenesFromDatabase(conn, eVal, matrix,numberOfAlignments, program));
					this.setBlosum80(this.getRequestsList(blosum80));

					if(this.blosum80.size()>0 && !this.cancel.get()) {

						errorCount +=	this.blastProcessGenesListNcbi(this.blosum80,program, database, numberOfAlignments, eVal, matrix, maxRequests);
					}
				}

				if(!this.cancel.get()) {

					this.wordSize = word;
					matrix = this.selectMatrix(40);
					this.setLoadedGenes(this.getGenesFromDatabase(conn, eVal, matrix,numberOfAlignments, program));
					this.setPam70(this.getRequestsList(pam70));

					if(this.pam70.size()>0 && !this.cancel.get()) {

						errorCount +=this.blastProcessGenesListNcbi(this.pam70,program, database, numberOfAlignments, eVal, matrix, maxRequests);
					}
				}


				if(!this.cancel.get()) {

					double newEval = eVal;
					if (eValueAutoAdjust) {

						newEval = 200000;
						System.out.println("Setting e-value to "+200000+" for <35mer sequences.");
					}
					this.wordSize = word;
					matrix= this.selectMatrix(30);
					this.setLoadedGenes(this.getGenesFromDatabase(conn, newEval, matrix,numberOfAlignments, program));
					this.setPam30(this.getRequestsList(pam30));
					if(this.pam30.size()>0 && !this.cancel.get()) {

						errorCount += this.blastProcessGenesListNcbi(this.pam30,program, database, numberOfAlignments, newEval, matrix, maxRequests);
					}
				}

				if(!this.cancel.get()) {

					double newEval = eVal;
					if (eValueAutoAdjust) {

						newEval = 200000;
						System.out.println("Setting e-value to "+newEval+" for 10-15mer or shorter sequences.");
					}
					this.wordSize = word;
					matrix= this.selectMatrix(15);
					this.setLoadedGenes(this.getGenesFromDatabase(conn, newEval, matrix,numberOfAlignments, program));
					this.setSmaller(this.getRequestsList(smaller));

					if(this.smaller.size()>0 && !this.cancel.get()) {

						errorCount += this.blastProcessGenesListNcbi(this.smaller,program, database, numberOfAlignments, newEval, matrix, maxRequests);
					}
				}
			}
			else {

				Map<String, ProteinSequence> query = new HashMap<String, ProteinSequence>();

				this.wordSize = word;

				for(String key:this.sequenceFile.keySet()) {

					query.put(key,this.sequenceFile.get(key));
				}

				this.setOtherSequences(this.getRequestsList(query));

				if(this.wordSize == -1) {

					this.wordSize=3;
				}

				if(!this.cancel.get()) {

					errorCount += this.blastProcessGenesListNcbi(this.otherSequences,program, database, numberOfAlignments, eVal, this.blastMatrix, maxRequests);
				}
			}

			Statement statement = conn.createStatement();
			this.deleteSetOfGenes(this.getProcessingGenes(statement, program), statement);
			conn.closeConnection();

			return errorCount;
		}
		catch(Exception e){e.printStackTrace();return -1;}
	}

	/**
	 * @param program
	 * @param database
	 * @param numberOfAlignments
	 * @param eVal
	 * @param eValueAutoAdjust
	 * @param sequenceType 
	 * @param word
	 * @return
	 * @throws Exception
	 */
	public int blastSequencesEBI(String program, String database, int numberOfAlignments, double eVal, boolean eValueAutoAdjust, String sequenceType) throws Exception {

		try {

			int errorCount = 0;

			Connection conn = new Connection(this.project.getDatabase().getMySqlCredentials());
			Matrix matrix;
			int maxRequests = 50;

			if(blastMatrix==null) {

				Map<String, ProteinSequence> smaller = new HashMap<String, ProteinSequence>();
				Map<String, ProteinSequence> pam30 = new HashMap<String, ProteinSequence>();
				Map<String, ProteinSequence> pam70 = new HashMap<String, ProteinSequence>();
				Map<String, ProteinSequence> blosum62 = new HashMap<String, ProteinSequence>();
				Map<String, ProteinSequence> blosum80 = new HashMap<String, ProteinSequence>();

				int unitLength = 1;

				if(!program.equalsIgnoreCase("blastp") ) {

					unitLength=3;

					if(eVal==1E-10) {

						eVal = 10;
					}
				}

				for(String key:this.sequenceFile.keySet()) {

					int seqSize = this.sequenceFile.get(key).getLength()/unitLength;

					if(seqSize<15){smaller.put(key,this.sequenceFile.get(key));}
					else if(seqSize<35){pam30.put(key,this.sequenceFile.get(key));}
					else if(seqSize<50){pam70.put(key,this.sequenceFile.get(key));}
					else if(seqSize<85){blosum80.put(key,this.sequenceFile.get(key));}
					else{blosum62.put(key,this.sequenceFile.get(key));} 
				}

				if(!this.cancel.get()) {

					matrix = this.selectMatrix(86);
					this.setLoadedGenes(this.getGenesFromDatabase(conn, eVal, matrix,numberOfAlignments, program));
					this.setBlosum62(this.getRequestsList(blosum62));

					if(this.blosum62.size()>0 && !this.cancel.get()) {

						errorCount += this.blastProcessGenesListEbi(this.blosum62, program, database, numberOfAlignments, eVal, matrix, maxRequests);
					}
				}

				if(!this.cancel.get()) {

					matrix = this.selectMatrix(80);
					this.setLoadedGenes(this.getGenesFromDatabase(conn, eVal, matrix,numberOfAlignments, program));
					this.setBlosum80(this.getRequestsList(blosum80));

					if(this.blosum80.size()>0 && !this.cancel.get()) {

						errorCount +=	this.blastProcessGenesListEbi(this.blosum80,program, database, numberOfAlignments, eVal, matrix, maxRequests);
					}
				}

				if(!this.cancel.get()) {

					matrix = this.selectMatrix(40);
					this.setLoadedGenes(this.getGenesFromDatabase(conn, eVal, matrix,numberOfAlignments, program));
					this.setPam70(this.getRequestsList(pam70));

					if(this.pam70.size()>0 && !this.cancel.get()) {

						errorCount +=this.blastProcessGenesListEbi(this.pam70,program, database, numberOfAlignments, eVal, matrix, maxRequests);
					}
				}


				if(!this.cancel.get()) {

					double newEval = eVal;
					if (eValueAutoAdjust) {

						newEval = 200000;
						System.out.println("Setting e-value to "+200000+" for <35mer sequences.");
					}
					matrix= this.selectMatrix(30);
					this.setLoadedGenes(this.getGenesFromDatabase(conn, newEval, matrix,numberOfAlignments, program));
					this.setPam30(this.getRequestsList(pam30));
					if(this.pam30.size()>0 && !this.cancel.get()) {

						errorCount += this.blastProcessGenesListEbi(this.pam30,program, database, numberOfAlignments, newEval, matrix, maxRequests);
					}
				}

				if(!this.cancel.get()) {

					double newEval = eVal;
					if (eValueAutoAdjust) {

						newEval = 200000;
						System.out.println("Setting e-value to "+newEval+" for 10-15mer or shorter sequences.");
					}
					matrix= this.selectMatrix(15);
					this.setLoadedGenes(this.getGenesFromDatabase(conn, newEval, matrix,numberOfAlignments, program));
					this.setSmaller(this.getRequestsList(smaller));

					if(this.smaller.size()>0 && !this.cancel.get()) {

						errorCount += this.blastProcessGenesListEbi(this.smaller,program, database, numberOfAlignments, newEval, matrix, maxRequests);
					}
				}
			}
			else {

				Map<String, ProteinSequence> query = new HashMap<String, ProteinSequence>();

				for(String key:this.sequenceFile.keySet()) {

					query.put(key,this.sequenceFile.get(key));
				}

				this.setOtherSequences(this.getRequestsList(query));

				if(this.wordSize == -1) {

					this.wordSize=3;
				}

				if(!this.cancel.get()) {

					errorCount += this.blastProcessGenesListEbi(this.otherSequences,program, database, numberOfAlignments, eVal, this.blastMatrix, maxRequests);
				}
			}

			Statement statement = conn.createStatement();
			this.deleteSetOfGenes(this.getProcessingGenes(statement, program), statement);
			conn.closeConnection();

			return errorCount;
		}
		catch(Exception e){e.printStackTrace();return -1;}
	}



	/**
	 * @param list
	 * @param program
	 * @param database
	 * @param numberOfAlignments
	 * @param eVal
	 * @param matrix
	 * @param maxRequests
	 * @throws Exception 
	 */
	private int blastProcessGenesListNcbi(ConcurrentLinkedQueue<String> list, String program, String database, int numberOfAlignments, double eVal, Matrix matrix, int maxRequests) throws Exception  {

		int errorCount = 0;
		int requests = 0;
		System.out.println(matrix+" size "+list.size());
		ConcurrentLinkedQueue<String> sequencesSubmited = new ConcurrentLinkedQueue<String>();

		while(list.size()>0) {

			if(this.cancel.get()) {

				list.clear();
			}
			else {

				sequencesSubmited.add(list.poll());
				requests++;

				if(requests>maxRequests) {

					errorCount += this.blastSingleSequenceNcbi(program, database, numberOfAlignments, eVal, this.wordSize, sequencesSubmited, matrix, this.gapExtensionPenalty, this.gapOpenPenalty);
					sequencesSubmited = new ConcurrentLinkedQueue<String>();
					requests = 0;
				}
			}
		}

		if(sequencesSubmited.size()>0 && !this.cancel.get()) {

			errorCount += this.blastSingleSequenceNcbi(program, database, numberOfAlignments, eVal, this.wordSize, sequencesSubmited, matrix, this.gapExtensionPenalty, this.gapOpenPenalty);
		}

		return errorCount;
	}

	/**
	 * @param list
	 * @param program
	 * @param database
	 * @param numberOfAlignments
	 * @param eVal
	 * @param matrix
	 * @param maxRequests
	 * @return
	 * @throws Exception
	 */
	private int blastProcessGenesListEbi(ConcurrentLinkedQueue<String> list, String program, String database, int numberOfAlignments, double eVal, Matrix matrix, int maxRequests) throws Exception  {

		int errorCount = 0;
		int requests = 0;
		System.out.println(matrix+" size "+list.size());
		ConcurrentLinkedQueue<String> sequencesSubmited = new ConcurrentLinkedQueue<String>();

		while(list.size()>0) {

			if(this.cancel.get()) {

				list.clear();
			}
			else {

				sequencesSubmited.add(list.poll());
				requests++;

				if(requests>maxRequests) {

					errorCount += this.blastSingleSequenceEbi(program, database, numberOfAlignments, eVal, sequencesSubmited, matrix, this.gapExtensionPenalty, this.gapOpenPenalty);
					sequencesSubmited = new ConcurrentLinkedQueue<String>();
					requests = 0;
				}
			}
		}

		if(sequencesSubmited.size()>0 && !this.cancel.get()) {

			errorCount += this.blastSingleSequenceEbi(program, database, numberOfAlignments, eVal, sequencesSubmited, matrix, this.gapExtensionPenalty, this.gapOpenPenalty);
		}

		return errorCount;
	}

	/**
	 * @param seqLength
	 * @return
	 */
	private Matrix selectMatrix(int seqLength) {

		if(seqLength<16) {

			if(this.wordSize==-1) {

				this.wordSize=2; 
			}
			return Matrix.PAM30;
		}

		if(seqLength<35) {

			if(this.wordSize==-1)
			{
				this.wordSize=2; 
			}
			return Matrix.PAM30;
		}

		else if(seqLength<50) {

			if(this.wordSize==-1) {

				this.wordSize=3; 
			}
			return Matrix.PAM70;
		}

		else if(seqLength<85) {

			if(this.wordSize==-1) {

				this.wordSize=3; 
			}
			return Matrix.BLOSUM80;
		}
		else {

			if(this.wordSize==-1) {

				this.wordSize=3; 
			}
			return Matrix.BLOSUM62;
		}
	}


	/**
	 * @param database
	 * @param numberOfAlignments
	 * @param eVal
	 * @param uniprotStatus
	 * @return
	 */
	public int hmmerSearchSequences(HmmerDatabase database, int numberOfAlignments, double eVal, boolean uniprotStatus) {

		int errorCount = 0;

		try {

			Connection conn = new Connection(this.project.getDatabase().getMySqlCredentials());
			int maxRequests = Runtime.getRuntime().availableProcessors()*2*10;
			this.setLoadedGenes(this.getGenesFromDatabase(conn, eVal, null, numberOfAlignments,"hmmer"));
			Map<String, ProteinSequence> query = new HashMap<String, ProteinSequence>();
			this.uniprotStatus = uniprotStatus;

			for(String key:this.sequenceFile.keySet()) {

				if(!(this.getLoadedGenes()!=null && this.getLoadedGenes().contains(key))) {

					query.put(key,this.sequenceFile.get(key));
				}
			}
			errorCount += this.hmmerProcessGenesList(query, database, numberOfAlignments, eVal, maxRequests);
		}
		catch(Exception e){e.printStackTrace();return -1;}
		return errorCount;
	}


	/**
	 * @param list
	 * @param database
	 * @param numberOfAlignments
	 * @param eVal
	 * @param maxRequests
	 * @throws Exception
	 */
	private int hmmerProcessGenesList(Map<String, ProteinSequence> query, HmmerDatabase database, int numberOfAlignments, double eVal, int maxRequests) throws Exception{

		int errorCount = 0;
		int requests = 0;
		ConcurrentLinkedQueue<String> list = new ConcurrentLinkedQueue<String>(query.keySet());
		System.out.println("HMMER size "+list.size());
		this.sequences_size = new Integer(list.size());
		ConcurrentLinkedQueue<String> sequencesSubmited = new ConcurrentLinkedQueue<String>();

		while(list.size()>0 && !this.cancel.get()) {

			if(this.cancel.get()) {

				list.clear();
			}
			else {

				sequencesSubmited.add(list.poll());
				requests++;

				if(requests>maxRequests) {

					errorCount += this.hmmerSearchSingleSequence(database, numberOfAlignments, eVal, sequencesSubmited, query);
					sequencesSubmited = new ConcurrentLinkedQueue<String>();
					requests = 0;
				}
			}
		}
		if(sequencesSubmited.size()> 0 && !this.cancel.get()) {

			errorCount += this.hmmerSearchSingleSequence(database, numberOfAlignments, eVal, sequencesSubmited, query);
		}

		return errorCount;
	}

	/**
	 * @param database
	 * @param numberOfAlignments
	 * @param expectedVal
	 * @param requests
	 * @param query
	 * @throws InterruptedException
	 */
	private int hmmerSearchSingleSequence(HmmerDatabase database, int numberOfAlignments,
			double expectedVal, ConcurrentLinkedQueue<String> requests, Map<String, ProteinSequence> query) throws InterruptedException {

		sequencesWithErrors = 0;

		if(!this.cancel.get()) {

			int threadsNumber=0;
			int numberOfCores = Runtime.getRuntime().availableProcessors()*2;
			List<Thread> threads = new ArrayList<Thread>();
			this.runnables = new ArrayList<Runnable>();
			if(requests.size()<numberOfCores){threadsNumber=requests.size();}
			else{threadsNumber=numberOfCores;}
			AtomicInteger errorCounter = new AtomicInteger(0);
			AtomicLong time = new AtomicLong(System.currentTimeMillis());

			if(this.similaritySearchProcessAvailable  && !this.cancel.get() && query.size()>0) {

				ConcurrentLinkedQueue<String> currentlyBeingProcessed = new ConcurrentLinkedQueue<String>();
				for(int i=0; i<threadsNumber; i++) {

					Runnable lc	= new SubmitHMMER(requests, query, expectedVal, numberOfAlignments, this.project, this.isNCBIGenome, 
							this.orgArray, database, this.cancel, this.sequences_size, this.startTime, this.progress, currentlyBeingProcessed,
							this.sequencesCounter, this.taxonomyMap, this.uniprotStar, errorCounter, uniprotStatus, time);
					Thread thread = new Thread(lc);
					this.runnables.add(lc);
					threads.add(thread);
					System.out.println("Start "+i);
					thread.start();
				}

				for(Thread thread :threads) {

					thread.join();
				}

				if(errorCounter.get()>0) {

					sequencesWithErrors += errorCounter.get();
					//System.out.println("\tError Count "+ errorCount);
					//Workbench.getInstance().error("Errors have ocurred while processsing "+errorCounter+" query(ies).");
					errorCounter.set(0);
					//this.similaritySearchProcessAvailable = false;
				}

			}
		}

		return sequencesWithErrors;
	}

	/**
	 * @param blosum62 the blosum62 to set
	 */
	public void setBlosum62(ConcurrentLinkedQueue<String> blosum62) {
		this.blosum62 = blosum62;
	}

	/**
	 * @return the blosum62
	 */
	public ConcurrentLinkedQueue<String> getBlosum62() {
		return blosum62;
	}

	/**
	 * @param blosum80 the blosum80 to set
	 */
	public void setBlosum80(ConcurrentLinkedQueue<String> blosum80) {
		this.blosum80 = blosum80;
	}

	/**
	 * @return the blosum80
	 */
	public ConcurrentLinkedQueue<String> getBlosum80() {
		return blosum80;
	}

	/**
	 * @param pam30 the pam30 to set
	 */
	public void setPam30(ConcurrentLinkedQueue<String> pam30) {
		this.pam30 = pam30;
	}

	/**
	 * @return the pam30
	 */
	public ConcurrentLinkedQueue<String> getPam30() {
		return pam30;
	}

	/**
	 * @param pam70 the pam70 to set
	 */
	public void setPam70(ConcurrentLinkedQueue<String> pam70) {
		this.pam70 = pam70;
	}

	/**
	 * @return the pam70
	 */
	public ConcurrentLinkedQueue<String> getPam70() {
		return pam70;
	}

	/**
	 * @return
	 */
	public Set<String> getLoadedGenes() {
		return loadedGenes;
	}

	/**
	 * @param loadedGenes
	 */
	public void setLoadedGenes(Set<String> loadedGenes) {
		this.loadedGenes = loadedGenes;
	}

	

	/**
	 * @param orgID
	 * @return
	 * @throws Exception 
	 */
	public String[] ebiNewTaxID(String orgID) throws Exception {

		try {

			return UniProtAPI.newTaxID(orgID, 0);
		}
		catch (Error e) {

			throw new Error("Service unavailable");
		}
		catch (Exception e) {

			this.similaritySearchProcessAvailable = false;
			throw e;
		}
	}

	/**
	 * @return the smaller
	 */
	public ConcurrentLinkedQueue<String> getSmaller() {
		return smaller;
	}

	/**
	 * @param smaller the smaller to set
	 */
	public void setSmaller(ConcurrentLinkedQueue<String> smaller) {
		this.smaller = smaller;
	}

	public short getGapExtensionPenalty() {
		return gapExtensionPenalty;
	}

	public void setGapExtensionPenalty(short gapExtensionPenalty) {
		this.gapExtensionPenalty = gapExtensionPenalty;
	}

	public short getGapOpenPenalty() {
		return gapOpenPenalty;
	}

	public void setGapOpenPenalty(short gapOpenPenalty) {
		this.gapOpenPenalty = gapOpenPenalty;
	}

	public short getWordSize() {
		return wordSize;
	}

	public void setWordSize(short wordSize) {
		this.wordSize = wordSize;
	}

	/**
	 * @return the orgArray
	 */
	public String[] getOrgArray() {
		return orgArray;
	}

	/**
	 * @param orgArray the orgArray to set
	 */
	public void setOrgArray(String[] orgArray) {
		this.orgArray = orgArray;
	}

	/**
	 * @return the blastMatrix
	 */
	public Matrix getBlastMatrix() {
		return blastMatrix;
	}

	/**
	 * @param blastMatrix the blastMatrix to set
	 */
	public void setBlastMatrix(Matrix blastMatrix) {
		this.blastMatrix = blastMatrix;
	}

	/**
	 * @return the otherSequences
	 */
	public ConcurrentLinkedQueue<String> getOtherSequences() {
		return otherSequences;
	}

	/**
	 * @param otherSequences the otherSequences to set
	 */
	public void setOtherSequences(ConcurrentLinkedQueue<String> otherSequences) {
		this.otherSequences = otherSequences;
	}

	/**
	 * @return the organism
	 */
	public String getOrganism() {
		return organism;
	}

	/**
	 * @param organism the organism to set
	 */
	public void setOrganism(String organism) {
		this.organism = organism;
	}

	/**
	 * @return the cancel
	 */
	public AtomicBoolean isCancel() {
		return cancel;
	}

	/**
	 * 
	 */
	public void setCancel() {

		this.cancel.set(true);
		for(Runnable lc :this.runnables) {

			if(lc.getClass().equals(SubmitNcbiBlast.class)) {

				((SubmitNcbiBlast) lc).setCancel(this.cancel);
			}
			else if(lc.getClass().equals(SubmitEbiBlast.class)) {

				((SubmitEbiBlast) lc).setCancel(this.cancel);
			}
			else {

				((SubmitHMMER) lc).setCancel(this.cancel);
			}
		}
	}

	/**
	 * @param progress
	 */
	public void setTimeLeftProgress(TimeLeftProgress progress) {
		this.progress = progress;		
	}

	/**
	 * @return
	 */
	public boolean removeDuplicates() {

		boolean result = false;
		Set<String> duplicateQueries = new HashSet<String>();

		Connection conn;
		try {

			conn = new Connection(this.project.getDatabase().getMySqlCredentials());

			Statement statement = conn.createStatement();

			ResultSet rs = statement.executeQuery("SELECT COUNT(*), query, locusTag, program FROM geneHomology " +
					"INNER JOIN homologySetup ON (homologySetup.s_key = homologySetup_s_key)" +
					"GROUP by program, query "+
					"HAVING COUNT(*)>1");

			while (rs.next()) {

				duplicateQueries.add(rs.getString("query"));
				result = true;
			}

			for(String query : duplicateQueries) {

				statement.execute("DELETE FROM geneHomology WHERE query ='"+query+"'");
			}
		}
		catch (SQLException e) {

			result = false;
			e.printStackTrace();
		}
		return result;
	}

	public void setGeneticCode(int geneticCode) {

		this.geneticCode = geneticCode;		
	}

	/**
	 * @param uniprotStatus
	 */
	public void setRetrieveUniprotStatus(boolean uniprotStatus) {

		this.uniprotStatus = uniprotStatus;
	}


	public long getLatencyWaitingPeriod() {
		return latencyWaitingPeriod;
	}

	public void setLatencyWaitingPeriod(long latencyWaitingPeriod) {
		this.latencyWaitingPeriod = latencyWaitingPeriod;
	}

	/**
	 * @param statement
	 * @param program
	 * @return
	 * @throws SQLException
	 */
	private Set<String> getProcessingGenes(Statement statement, String program) throws SQLException {

		Set<String> deleteGenes = new HashSet<String>();

		ResultSet rs =statement.executeQuery("SELECT geneHomology.s_key, program " +
				"FROM geneHomology LEFT JOIN homologySetup ON (homologySetup.s_key = homologySetup_s_key) " +
				"WHERE status = '"+DatabaseProgressStatus.PROCESSING+"';");

		while(rs.next()) {

			if(rs.getString(2).contains(program) ) {

				deleteGenes.add(rs.getString(1));
			}
		}

		return deleteGenes;

	}

	/**
	 * @param deleteGenes
	 * @param statement
	 * @throws SQLException 
	 */
	private void deleteSetOfGenes(Set<String> deleteGenes, Statement statement) throws SQLException {

		for(String s_key : deleteGenes) {

			statement.execute("DELETE FROM geneHomology WHERE s_key='"+s_key+"'");
		}
	}

	public enum Source{

		ncbi,
		ebi,
		hmmer
	}

}