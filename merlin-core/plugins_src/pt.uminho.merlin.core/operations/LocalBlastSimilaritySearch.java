/**
 * 
 */
package operations;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import java.util.GregorianCalendar;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;
import operations.LocalBlastSimilaritySearch.WordSize;

import org.apache.axis2.AxisFault;
import org.biojava3.core.sequence.ProteinSequence;

import pt.uminho.sysbio.merge.databases.containers.FastaSequence;
import pt.uminho.sysbio.merge.databases.containers.HomologySetup;
import pt.uminho.sysbio.common.utilities.io.FileUtils;



import pt.uminho.sysbio.common.local.alignments.core.PairwiseSequenceAlignement.Matrix;
import alignment.ProcessHomologySetup;
import alignment.SearchAndLoadHomologueSequences;
import alignment.localblast.BlastArguments;
import alignment.localblast.LocalBlast;
import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import datatypes.Project;
import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.CreateGenomeFile;

/**
 * @author pedro
 *
 */
@Operation(description = "Perform a semi-automatic (re)annotation of the organism's genome / metagenome. This process may take several hours/ days, depending on the computer power availability" +
		" and the number of genes to be processed. It also requires that blast is locally installed and one of the below available databases are created. If you are having troubles on setting up" +
		" one of this prerequisites please go to the 'How To's' link on Merlin website where detailed user help on these steps is provided : http://www.merlin-sysbio.org/ ")
public class LocalBlastSimilaritySearch {
	private String program, uniprot_url;
	private File txt_file, uniprotdb_path;
	private Project project;
	private BlastMatrix blastMatrix=BlastMatrix.BLOSUM62;
	private String database, databaseDirectory;
	private String numberOfAlignments;
	private String eVal;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private SearchAndLoadHomologueSequences blast_loader;
	private BlastArguments blastArgumetns;
	private LocalBlast localBlast;
	private WordSize wordsize;
	private String organism;
	private boolean autoEval;


	@Port(direction=Direction.INPUT, name="BLAST type", validateMethod="checkProgram",defaultValue="blastp",description="Blast program.",order=1)
	public void setDatabaseName(BlastProgram program) {

		this.program = program.toString();
	}

	@Port(direction=Direction.INPUT, name="Local database",description="Select the sequence database to run searches against",order=2)
	public void setRemoteDatabase(RemoteDatabasesEnum ls){
		this.database=ls.toString();
		if(this.database == "uniprotKB"){
			throw new IllegalArgumentException("You must download both the UniProtKB/Swiss-Prot and UniProtKB/TrEMBL fasta and text files and merge the two files of each extension with commands such as cat, for linux ");
		}
	}

	@Port(direction=Direction.INPUT, name="Databases URL",defaultValue="http://www.uniprot.org/downloads",description="URL for download database sequences in fasta and text file",order=3)
	public void setUniprotDBdownload(String uniprot_url) {
		this.uniprot_url = uniprot_url;
	}


	@Port(direction=Direction.INPUT, name="Local Database",validateMethod= "checkDatabase", defaultValue="/path/to/database_file",description="Select one of the files ('database_name.psq', 'database_name.phr', 'database_name.pin') to make the link to the local database ",order=4)
	public void setUniprotDBpath(File uniprotdb_path) {
		this.uniprotdb_path = uniprotdb_path;
	}

	@Port(direction=Direction.INPUT, name="Text database file",validateMethod= "checkTextfile", defaultValue="/path/to/database_textfile",description="Select the path for the text file, for the retrieve of homologues details",order=5)
	public void setUniprotTXTpath(File txt_file) {
		this.txt_file = txt_file;
	}

	@Port(direction=Direction.INPUT, name="E-Value",defaultValue="1E-10", validateMethod="checkEvalue",description="Default: '1E-10'",order=6)
	public void setUser(String eVal) {

		if(eVal.isEmpty()) {

			this.eVal="1E-10";
		}
		else {

			this.eVal = eVal;
		}
	}


	@Port(direction=Direction.INPUT, name="Number of results",defaultValue="50",validateMethod="checkNumber_of_align",description="Select the maximum number of aligned sequences to display. Default: '50'",order=7)
	public void setNumberOfAlignments(String numberOfAlignments) {

		if(numberOfAlignments.isEmpty()) {

			this.numberOfAlignments = "50";
		}
		else {

			this.numberOfAlignments = numberOfAlignments;
		}
	}

//	@Port(direction=Direction.INPUT, name="Substitution matrix",defaultValue="AUTO",description="Assigns a score for aligning pairs of residues. Default: 'Adapts to Sequence length'.",order=8)
//	public void setMatrix(BlastMatrix blastMatrix){
//		this.blastMatrix = blastMatrix;
//	}
//
//	@Port(direction=Direction.INPUT, name="Word size",defaultValue="auto",description="The length of the seed that initiates an alignment. Default: '3'",order=9)
//	public void setWordSize(WordSize wordSize){
//		this.wordsize = wordSize;
//	}

	/**
	 * @return
	 */
	@Progress
	public TimeLeftProgress getProgress(){

		return progress;
	}

	/**
	 * 
	 */
	@Cancel
	public void cancel() {

		this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-GregorianCalendar.getInstance().getTimeInMillis()),1,1);
		this.localBlast.setCancel();
		
	}




	/**
	 * @param project
	 * @throws SQLException
	 * @throws AxisFault
	 */
	@Port(direction=Direction.INPUT, name="Project",description="Select Project",validateMethod="checkProject", order =10)
	public void selectProject(Project project) throws SQLException, AxisFault{

		try {

			String extension = ".faa";

			//Map<String, ProteinSequence> sequences = CreateGenomeFile.getGenomeFromID(project.getGenomeID(), extension);

//			short word = -1;
//			if(this.wordsize.index()!=0) {
//
//				word = Short.parseShort(this.wordsize.index()+"");
//			}
//			if(word == -1){
//				word = 3;
//			}
			
			
			short word = 3;
			this.blastArgumetns = new BlastArguments(this.eVal, Integer.parseInt(this.numberOfAlignments), this.blastMatrix.toString(), Short.toString(word));
			File queryfile = new File(FileUtils.getCurrentTempDirectory().concat(project.getGenomeCodeName().concat(extension)));
			this.localBlast = new LocalBlast(this.program, queryfile, this.txt_file, this.databaseDirectory, blastArgumetns.getArguments(), project);
			long startTime = System.currentTimeMillis();
			this.localBlast.setProgress(this.progress);
			while(!this.localBlast.isCancel().get()){

//						if(!project.isNCBIGenome()) {
//			
//							this.blast_loader.setTaxonomicID(project.getTaxonomyID()+"");
//						}


				this.localBlast.runBlast();

				LinkedHashMap<String, LinkedHashMap<String, String[]>> blastparse = this.localBlast.parseBlastOutput();
				System.out.println("Blast output file parsed.\n");
				HashMap< String, FastaSequence > sequencesHash = this.localBlast.saveSequences();
				ConcurrentLinkedQueue<String> noSimilarities = this.localBlast.noSimilaritiesGenes;
				String [] setupInfo = this.localBlast.setupInfo;
			
				System.out.println("Number of genes to be processed:\t" + sequencesHash.size());
				System.out.println("Blast output file parsed.\n" + "Number of genes without similarities:\t" + noSimilarities.size());
				System.out.println("Retrieving homologues information... This may take a while");
			
				LinkedHashMap<String, LinkedHashMap<String, String[]>> hashfinal = this.localBlast.retrieveUniprotData(blastparse, this.txt_file);
			
				System.out.println("\n\nRetrieve of the Uniprot data finished!!");
			
				HomologySetup homologySetup = new HomologySetup(setupInfo[0],setupInfo[1],setupInfo[2],this.blastArgumetns.getEvalue(),
					this.blastArgumetns.getMatrix(), this.blastArgumetns.getWord_size(),
					this.blastArgumetns.getGap_costs(), this.blastArgumetns.getNum_descriptions());



				Connection conn = (Connection) new Connection(this.project.getDatabase().getMySqlCredentials().get_database_host(), 
					this.project.getDatabase().getMySqlCredentials().get_database_port(),this.project.getDatabase().getMySqlCredentials().get_database_name(), 
					this.project.getDatabase().getMySqlCredentials().get_database_user(), 	this.project.getDatabase().getMySqlCredentials().get_database_password());


				ProcessHomologySetup p = new ProcessHomologySetup(conn);
				System.out.println("\nLoading now the data into the " + this.project.getDatabase().getMySqlCredentials().get_database_name() + " database...");
				p.loadLocalBlast(sequencesHash, hashfinal, homologySetup, noSimilarities);
			

				long endTime = System.currentTimeMillis();
				System.out.println("Total elapsed time in execution of local blast was: "+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) 
					-TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
				Workbench.getInstance().info("Loading Local Blast data into " +this.project.getDatabase().getMySqlCredentials().get_database_name()+" finished!");

		}
			if(this.localBlast.isCancel().get()) {
				Workbench.getInstance().warn("BLAST process cancelled!");					
			}
		}
		catch (Error e) {e.printStackTrace();}//e.printStackTrace();
		catch (IOException e) {e.printStackTrace();}//e.printStackTrace();
		catch (ParseException e) {e.printStackTrace();}//e.printStackTrace();
		catch (Exception e) {e.printStackTrace();}
	}



	public void checkProject(Project project) {

		if(project == null) {

			throw new IllegalArgumentException("No Project Selected!");
		}
		else {

			if(project.getGenomeCodeName()==null) {

				throw new IllegalArgumentException("Set the genome fasta file(s) for project "+project.getName());
			}

			if(!project.isNCBIGenome() && project.getTaxonomyID()<0) {

				System.out.println(project.getTaxonomyID());
				throw new IllegalArgumentException("The loaded genome is not in the NCBI fasta format.\nPlease enter the taxonomic identification from NCBI taxonomy.");
			}

			if(this.program.toString().equalsIgnoreCase("blastp") && !project.isFaaFiles()) {

				throw new IllegalArgumentException("Please add 'faa' files to perform blastp homology searches.");
			}

			if(this.program.toString().equalsIgnoreCase("blastx") && !project.isFnaFiles()) {

				throw new IllegalArgumentException("Please add 'fna' files to perform blastx homology searches.");
			}

			this.project = project;
		}
	}

	/**
	 * @param project
	 */
	public void checkProgram(BlastProgram program) {

		this.program = program.toString();
	}


	public void checkDatabase(File uniprotdb_path){

		if(uniprotdb_path == null){
			throw new IllegalArgumentException("Local database file directory not set!");
		}

		else{
			if(uniprotdb_path.exists()){
				if(uniprotdb_path.getName().endsWith(".phr") || uniprotdb_path.getName().endsWith(".pin") || uniprotdb_path.getName().endsWith(".psq")){
					String s = uniprotdb_path.getAbsoluteFile().toString();
					this.databaseDirectory = s.substring(0, s.indexOf('.'));
					System.out.println(this.databaseDirectory);
				}
				else{
					throw new IllegalArgumentException("Please select a correct database file ('.phr' or '.pin' or '.psq').");
				}
			}
			else{
				throw new IllegalArgumentException("The local database file does not exist. Can not proceed!");	
			}
			
		}
	}

	public void checkTextfile(File txtfile){

		if(txtfile == null || txtfile.toString().isEmpty()) {

			throw new IllegalArgumentException("Txt file directory not set!");
		}
		else {

			//			if(!txtfile.isDirectory()) {
			//
			//				txtfile = new File(txtfile.getParent().toString());
			//			}
			//
			if(txtfile.exists()){
				if(txtfile.getName().endsWith(".dat") || txtfile.getName().endsWith(".txt")  ) {
					this.txt_file = txtfile;				
				}
				else {
					throw new IllegalArgumentException("Please Select a directory with dat or txt files!");				
				}
			}
			else {
				throw new IllegalArgumentException("The text database file does not exist. Can not proceed!");	
			}
			
		}
	}

	public void checkEvalue(String evalue){
		Scanner s = new Scanner(evalue);
		if (s.hasNextDouble()){
			double ev = Double.parseDouble(evalue);
			if(ev > 20000 || ev < 1E-200){
				throw new IllegalArgumentException("Please select an eValue in a valid range");
			}
			else{
				this.eVal = evalue;
			}
		}
		else{
			throw new IllegalArgumentException("Please select a valid eValue");
		}


	}

	public void checkNumber_of_align(String num_align){
		Scanner s = new Scanner(num_align);
		if(s.hasNextInt()){
			int nb_algn = Integer.parseInt(num_align);
			if(nb_algn < 1){
				throw new IllegalArgumentException("Please select a number of results higher than 0");
			}
			else{
				this.numberOfAlignments = num_align;
			}
		}
		else{
			throw new IllegalArgumentException("Please select a valid number of results");
		}
	}
	enum RemoteDatabasesEnum{
		swissprot,
		uniprotKB
	}

	enum BlastMatrix{
		BLOSUM62,
		BLOSUM45,
		//	BLOSUM50,
		BLOSUM80,
		//	BLOSUM90,
		PAM30,
		PAM70,
		//	PAM250
	}

	/**
	 * @author ODias
	 *
	 */
	enum BlastProgram{
		blastp,
		//blastx,

	}

	enum WordSize{

		auto (-1),
		wordSize_2 (2),
		wordSize_3 (3);

		private final int index;   

		WordSize(int index) {
			this.index = index;
		}

		public int index() { 
			return index; 
		}
	}
}