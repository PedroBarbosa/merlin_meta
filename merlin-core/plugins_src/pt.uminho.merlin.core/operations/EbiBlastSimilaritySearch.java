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

/**
 * @author Oscar
 *
 */
@Operation(description="Perform a semi-automatic (re)annotation of the organism's genome. This process may take several hours, depending on the web-server availability.")
public class EbiBlastSimilaritySearch {

	private String program;
	private BlastMatrix blastMatrix=BlastMatrix.AUTOSELECTION;
	private RemoteDatabasesEnum database;
	private String numberOfAlignments;
	private ExpectedValues eVal;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private SearchAndLoadHomologueSequences ebiBlastLoader;
	private String organism;
	private boolean autoEval;
	private int latencyWaitingPeriod;
	private SequenceType sequenceType;
	
	
	@Port(direction=Direction.INPUT, name="BLAST type", validateMethod="checkProgram",defaultValue="blastp",description="Blast program. Default: 'blastp'",order=1)
	public void setDatabaseName(BlastProgram program) {

		this.program = program.toString();
	}
	
	@Port(direction=Direction.INPUT, name="Sequence Type",defaultValue="protein",description="Default: 'protein'",order=2)
	public void setSequence(SequenceType sequenceType) {

			this.sequenceType=sequenceType;
	}

	@Port(direction=Direction.INPUT, name="E-Value",defaultValue="_1E_minus_10",description="Default: '1E-10'",order=3)
	public void setUser(ExpectedValues eVal) {

			this.eVal=eVal;
	}

	@Port(direction=Direction.INPUT, name="Adjust E-Value",defaultValue="true",description="Automatically adjust e-value for smaller sequences search",order=4)
	public void setEValueAutoSelection(boolean autoEval){
		this.autoEval=autoEval;
	}

	@Port(direction=Direction.INPUT, name="Remote database",defaultValue="uniprotkb",description="Select the sequence database to run searches against",order=5)
	public void setRemoteDatabase(RemoteDatabasesEnum ls){
		this.database=ls;
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
		ebiBlastLoader.setCancel();
		//Workbench.getInstance().warn("BLAST cancelled!");
	}

	@Port(direction=Direction.INPUT, name="Latency period",description="Request latency waiting period (minutes)",validateMethod="checkLatencyWaitingPeriod", defaultValue = "30", order=10)
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
			this.ebiBlastLoader = new SearchAndLoadHomologueSequences(sequences, project, project.isNCBIGenome(), Source.ebi);
			
			//it has to be milliseconds
			this.ebiBlastLoader.setLatencyWaitingPeriod(this.latencyWaitingPeriod*60000);
			this.ebiBlastLoader.setRetrieveUniprotStatus(true);

			if(!project.isNCBIGenome()) {

				this.ebiBlastLoader.setTaxonomicID(project.getTaxonomyID()+"");
			}

			if(!this.organism.equals("no_org")) {

				this.ebiBlastLoader.setOrganism(this.organism);
			}

			if(blastMatrix!=BlastMatrix.AUTOSELECTION) {

				if(blastMatrix==BlastMatrix.BLOSUM62)
				{
					this.ebiBlastLoader.setBlastMatrix(Matrix.BLOSUM62);
				}
				if(blastMatrix==BlastMatrix.BLOSUM45)
				{
					this.ebiBlastLoader.setBlastMatrix(Matrix.BLOSUM45);
				}
				if(blastMatrix==BlastMatrix.BLOSUM80)
				{
					this.ebiBlastLoader.setBlastMatrix(Matrix.BLOSUM80);
				}
				if(blastMatrix==BlastMatrix.PAM30)
				{
					this.ebiBlastLoader.setBlastMatrix(Matrix.PAM30);
				}
				if(blastMatrix==BlastMatrix.PAM70)
				{
					this.ebiBlastLoader.setBlastMatrix(Matrix.PAM70);
				}
			}
			this.ebiBlastLoader.setTimeLeftProgress(this.progress);

//			System.out.println(this.program.toString());
//			System.out.println(this.database.toString());
//			System.out.println(new Integer(this.numberOfAlignments));
//			System.out.println(this.eVal.index());
//			System.out.println(this.autoEval);
//			System.out.println(this.sequenceType.toString());
			
			int errorOutput = this.ebiBlastLoader.blastSequencesEBI(this.program.toString(), this.database.toString(), 
					new Integer(this.numberOfAlignments), this.eVal.index(), this.autoEval, this.sequenceType.toString());

			if(errorOutput == 0) {

				if(this.ebiBlastLoader.removeDuplicates() && !this.ebiBlastLoader.isCancel().get()) {

					errorOutput = this.ebiBlastLoader.blastSequencesEBI(this.program.toString(), this.database.toString(), 
							new Integer(this.numberOfAlignments), this.eVal.index(), this.autoEval, this.sequenceType.toString());
				}

				if(errorOutput == 0 && !this.ebiBlastLoader.isCancel().get()) {

					AIBenchUtils.updateHomologyDataView(project.getName());
					Workbench.getInstance().info("BLAST process complete!");
				}
			}
			
			if(errorOutput > 0) {

				
				Workbench.getInstance().error("Errors have ocurred while processsing "+errorOutput+" query(ies).");
				//Workbench.getInstance().warn("Errors occurred while performing the operation.");
			}
			
			if(this.ebiBlastLoader.isCancel().get()) {

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
		
		uniprotkb,
		trembl
	}
	
	enum ExpectedValues{
		
		_1E_minus_200 (1e-200),
		_1E_minus_100 (1e-100),
		_1E_minus_50 (1e-50),
		_1E_minus_10 (1e-10),
		_1E_minus_5 (1e-5),
		_1E_minus_4 (1e-4),
		_1E_minus_3 (1e-3),
		_1E_minus_2 (1e-2),
		_1E_minus_1 (1e-1),
		_1 (1.0),
		_100 (100),
		_1000 (1000);
		
		private final double index;   

		ExpectedValues(double index) {
			this.index = index;
		}

		public double index() { 
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

	enum SequenceType {
		
		protein,
		dna,
		rna
	}

}
