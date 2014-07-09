package operations;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.biojava3.core.sequence.ProteinSequence;

import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.sysbio.common.local.alignments.core.PairwiseSequenceAlignement.Matrix;
import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;
import utilities.AIBenchUtils;
import alignment.SearchAndLoadHomologueSequences;
import alignment.SearchAndLoadHomologueSequences.Source;
import datatypes.Project;
import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;

@Operation(description="Perform a semi-automatic (re)annotation of the organism's genome. This process may take several hours, depending on the web-server availability.")
public class NcbiBlastSimilaritySearch {
	private String program;
	private BlastMatrix blastMatrix=BlastMatrix.AUTOSELECTION;
	private String database;
	private String numberOfAlignments;
	private String eVal;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private SearchAndLoadHomologueSequences blast_loader;
	private WordSize wordSize;
	private String organism;
	//private String taxonomicIdentification=null;
	private boolean autoEval;
	private GeneticCode geneticCode;
	private boolean uniprotStatus;
	private int latencyWaitingPeriod;

	@Port(direction=Direction.INPUT, name="BLAST type", validateMethod="checkProgram",defaultValue="blastp",description="Blast program. Default: 'blastp'",order=1)
	public void setDatabaseName(BlastProgram program) {

		this.program = program.toString();
	}

	@Port(direction=Direction.INPUT, name="Genetic Code",defaultValue="Standard",description="Genetic Code. Used for blastX, else ignored.",order=2)
	public void setGeneticCode(GeneticCode geneticCode) {

		this.geneticCode = geneticCode;
	}


	@Port(direction=Direction.INPUT, name="E-Value",defaultValue="1E-30",description="Default: '1E-30'",order=3)
	public void setUser(String eVal) {

		if(eVal.isEmpty()) {

			this.eVal="1E-30";
		}
		else {

			this.eVal = eVal;
		}
	}

	@Port(direction=Direction.INPUT, name="Adjust E-Value",defaultValue="true",description="Automatically adjust e-value for smaller sequences search",order=4)
	public void setEValueAutoSelection(boolean autoEval){
		this.autoEval=autoEval;
	}

	@Port(direction=Direction.INPUT, name="Remote database",description="Select the sequence database to run searches against",order=5)
	public void setRemoteDatabase(RemoteDatabasesEnum ls){
		this.database=ls.toString();
	}

	@Port(direction=Direction.INPUT, name="Number of results",defaultValue="100",description="Select the maximum number of aligned sequences to display. Default: '100'",order=6)
	public void setNumberOfAlignments(String numberOfAlignments) {

		if(numberOfAlignments.isEmpty()) {

			this.numberOfAlignments = "100";
		}
		else {

			this.numberOfAlignments = numberOfAlignments;
		}
	}

	@Port(direction=Direction.INPUT, name="Substitution matrix",defaultValue="AUTO",description="Assigns a score for aligning pairs of residues. Default: 'Adapts to Sequence length'.",order=7)
	public void setMatrix(BlastMatrix blastMatrix){
		this.blastMatrix = blastMatrix;
	}

	@Port(direction=Direction.INPUT, name="Word size",defaultValue="3",description="The length of the seed that initiates an alignment. Default: '3'",order=8)
	public void setWordSize(WordSize wordSize){
		this.wordSize = wordSize;
	}

	@Port(direction=Direction.INPUT, name="Organism",defaultValue="",description="(Optional) Enter organism ncbi taxonomy id",order=9)
	public void setOrganism(String organism) {

		if(organism.isEmpty()) {

			this.organism = "no_org";
		}
		else {

			this.organism = organism;
		}
	}

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

		progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-GregorianCalendar.getInstance().getTimeInMillis()),1,1);
		blast_loader.setCancel();
		//Workbench.getInstance().warn("BLAST cancelled!");
	}


	//	/**
	//	 * @param taxonomicIdentification
	//	 */
	//	@Port(direction=Direction.INPUT, name="Organism Taxonomic identification", validateMethod="checkTaxonomicIdentification", description="Tax ID Required if genome was not downloaded from NCBI FTP webSite.",order=9)
	//	public void genomeIdentification(String taxonomicIdentification){
	//		this.taxonomicIdentification = taxonomicIdentification;
	//	}
	
	
	@Port(direction=Direction.INPUT, name="Uniprot Status",description="Retrieve homologue genes status from uniprot", defaultValue = "false", order=10)
	public void retrieveUniprotStatus(boolean uniprotStatus) {
	
		this.uniprotStatus = uniprotStatus;
	}
	
	@Port(direction=Direction.INPUT, name="Latency period",description="Request latency waiting period (minutes)",validateMethod="checkLatencyWaitingPeriod", defaultValue = "30", order=11)
	public void setLatencyWaitingPeriod(int latencyWaitingPeriod) {
	
		this.latencyWaitingPeriod = latencyWaitingPeriod;
	}
	
	
	/**
	 * @param project
	 * @throws SQLException
	 * @throws AxisFault
	 */
	@Port(direction=Direction.INPUT, name="Project",description="Select Project",validateMethod="checkProject", order=12)
	public void selectProject(Project project) throws SQLException, AxisFault{

		try {

			String extension = ".faa";
			if(this.program == "blastx") {

				extension = ".fna";
			}
			Map<String, ProteinSequence> sequences = CreateGenomeFile.getGenomeFromID(project.getGenomeCodeName(), extension);
			this.blast_loader = new SearchAndLoadHomologueSequences(sequences, project, project.isNCBIGenome(),Source.ncbi);
			
			//it has to be milliseconds
			this.blast_loader.setLatencyWaitingPeriod(this.latencyWaitingPeriod*60000);
			this.blast_loader.setRetrieveUniprotStatus(uniprotStatus);

			if(this.program == "blastx") {

				this.blast_loader.setGeneticCode(this.geneticCode.index());
			}
			
			if(!project.isNCBIGenome()) {

				//this.blast_loader.setTaxonomicID(this.taxonomicIdentification);
				this.blast_loader.setTaxonomicID(project.getTaxonomyID()+"");
			}

			short word = -1;
			if(this.wordSize.index()!=0) {

				word = Short.parseShort(this.wordSize.index()+"");
				blast_loader.setWordSize(word);
			}

			if(!this.organism.equals("no_org")) {

				this.blast_loader.setOrganism(this.organism);
			}

			if(blastMatrix!=BlastMatrix.AUTOSELECTION) {

				if(blastMatrix==BlastMatrix.BLOSUM62)
				{
					this.blast_loader.setBlastMatrix(Matrix.BLOSUM62);
				}
				if(blastMatrix==BlastMatrix.BLOSUM45)
				{
					this.blast_loader.setBlastMatrix(Matrix.BLOSUM45);
				}
				if(blastMatrix==BlastMatrix.BLOSUM80)
				{
					this.blast_loader.setBlastMatrix(Matrix.BLOSUM80);
				}
				if(blastMatrix==BlastMatrix.PAM30)
				{
					this.blast_loader.setBlastMatrix(Matrix.PAM30);
				}
				if(blastMatrix==BlastMatrix.PAM70)
				{
					this.blast_loader.setBlastMatrix(Matrix.PAM70);
				}
			}
			this.blast_loader.setTimeLeftProgress(this.progress);

			int errorOutput = this.blast_loader.blastSequencesNCBI(this.program, this.database, Integer.parseInt(this.numberOfAlignments), Double.parseDouble(this.eVal), this.autoEval, word);

			if(errorOutput == 0) {

				if(this.blast_loader.removeDuplicates() && !this.blast_loader.isCancel().get()) {

					errorOutput = this.blast_loader.blastSequencesNCBI(this.program, this.database, Integer.parseInt(this.numberOfAlignments), Double.parseDouble(this.eVal), this.autoEval, word);
				}

				if(errorOutput == 0 && !this.blast_loader.isCancel().get()) {

					AIBenchUtils.updateHomologyDataView(project.getName());
					Workbench.getInstance().info("BLAST process complete!");
				}
			}
			
			if(errorOutput > 0) {

				
				Workbench.getInstance().error("Errors have ocurred while processsing "+errorOutput+" query(ies).");
				//Workbench.getInstance().warn("Errors occurred while performing the operation.");
			}
			
			if(this.blast_loader.isCancel().get()) {

				Workbench.getInstance().warn("BLAST search cancelled!");
			}
		}
		catch (Error e) {e.printStackTrace();}//e.printStackTrace();
		catch (IOException e) {e.printStackTrace();}//e.printStackTrace();
		catch (ParseException e) {e.printStackTrace();}//e.printStackTrace();
		catch (Exception e) {e.printStackTrace();}
	}


	/**
	 * @return

	private boolean loadUnloadedDataGui(){
		int i =CustomGUI.stopQuestion("Load data with no similarity search Results?",
				"Automatically BLAST and Load data for which no similarity search results were found?",
				new String[]{"Yes", "No", "Info"});
		if(i<2)
		{
			switch (i)
			{
			case 0:return true;
			default:return false;
			}
		}
		else
		{
			Workbench.getInstance().info(
					"Some sequences are too short (usually having a length of 30 or less nucleotide" +
					"\nor amino acids) to be processed by BLOck SUbstitution Matrices, " +
					"\nhence search parameters have to be adjusted to search for short input sequences." +
					"\nAutomatic BLAST parameters for short sequences are:\n\t-program: blastp\n\t-e Value: 20000\n\t-database: nr\n\t-number of sequences: 100" +
			"\n\t-matrix: PAM30\n\t-word size: 2");
			return loadUnloadedDataGui();
		}
	}
	 */

	//	/**
	//	 * @param taxonomicIdentification
	//	 */
	//	public void checkTaxonomicIdentification(String  taxonomicIdentification){
	//		if(!taxonomicIdentification.isEmpty())
	//		{
	//			this.taxonomicIdentification = taxonomicIdentification;
	//		}
	//	}

	/**
	 * @param project
	 */
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
		}
	}

	/**
	 * @param project
	 */
	public void checkProgram(BlastProgram program) {

		this.program = program.toString();
	}

	/**
	 * @param project
	 */
	public void checkLatencyWaitingPeriod(int latencyWaitingPeriod) {

		if(latencyWaitingPeriod <0) {
			
			throw new IllegalArgumentException("The latency waiting period must be greater than 0 (zero)");
		}
			
	}
	
	/**
	 * @author ODias
	 *
	 */
	enum RemoteDatabasesEnum{
		nr,
		swissprot,
		yeast,
		refseq_protein,
		ecoli,
		pdb
	}

	/**
	 * @author ODias
	 *
	 */
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

	/**
	 * @author ODias
	 *
	 */
	enum BlastMatrix{
		AUTOSELECTION,
		BLOSUM62,
		BLOSUM45,
		BLOSUM80,
		PAM30,
		PAM70
	}

	/**
	 * @author ODias
	 *
	 */
	enum BlastProgram{
		//ncbi-
		blastp,
		//ncbi-
		//		blastn,
		blastx,
		//		tblastn,
		//		tblastx
	}

	enum GeneticCode {

		Standard (1),
		Vertebrate_Mitochondrial (2),
		Mitochondrial (3),
		MoldProtoCoelMitoMycoSpiro (4),
		Invertebrate_Mitochondrial (5),
		Ciliate_Macronuclear (6),
		Echinodermate_Mitochondrial (9),
		Alt_Ciliate_Macronuclear (10),
		Eubacterial (11),
		Alternative_Yeast (12),
		Ascidian_Mitochondrial (13),
		Flatworm_Mitochondrial (14),
		Blepharisma_Macronuclear (15);

		private final int index;   

		GeneticCode(int index) {
			this.index = index;
		}

		public int index() { 
			return index; 
		}
	}

}
