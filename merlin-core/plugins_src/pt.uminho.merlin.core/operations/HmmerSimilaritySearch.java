/**
 * 
 */
package operations;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.biojava3.core.sequence.ProteinSequence;

import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.CreateGenomeFile;
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
 * @author ODias
 *
 */
@Operation(description="Perform a semi-automatic (re)annotation of the organism's genome. This process may take several hours, depending on the web-server availability.")
public class HmmerSimilaritySearch {

	private HmmerDatabase database;
	private String numberOfAlignments;
	private String eVal;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private SearchAndLoadHomologueSequences hmmer_loader;
	//private String taxonomicIdentification=null;
	private boolean uniprotStatus;

	@Port(direction=Direction.INPUT, name="Expected Value",defaultValue="1e-30",description="Default: '1e-30'",order=2)
	public void setUser(String eVal){

		if(eVal.isEmpty()) {

			this.eVal="1e-30";
		}
		else {

			this.eVal = eVal;
		}
	}

	@Port(direction=Direction.INPUT, name="Remote database",description="Select the sequence database to run searches against",order=3)
	public void setRemoteDatabase(HmmerDatabase database){
		this.database=database;
	}

	@Port(direction=Direction.INPUT, name="Number of results",defaultValue="100",description="Select the maximum number of aligned sequences to display. Default: '100'",order=4)
	public void setNumberOfAlignments(String numberOfAlignments) {

		if(numberOfAlignments.isEmpty()) {

			this.numberOfAlignments = "100";
		}
		else {

			this.numberOfAlignments = numberOfAlignments;
		}
	}

	/**
	 * @return
	 */
	@Progress
	public TimeLeftProgress getProgress(){
		return this.progress;
	}

	/**
	 * 
	 */
	@Cancel
	public void cancel() {

		this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-GregorianCalendar.getInstance().getTimeInMillis()),1,1);
		this.hmmer_loader.setCancel();
	}


	//	/**
	//	 * @param taxonomicIdentification
	//	 */
	//	@Port(direction=Direction.INPUT, name="Organism Taxonomic identification", validateMethod="checkTaxonomicIdentification", description="Tax ID Required if genome was not downloaded from NCBI FTP webSite.",order=8)
	//	public void genomeIdentification(String taxonomicIdentification){
	//		this.taxonomicIdentification = taxonomicIdentification;
	//	}

	@Port(direction=Direction.INPUT, name="Uniprot Status",description="Retrieve status from uniprot",defaultValue = "false", order=10)
	public void retrieveUniprotStatus(boolean uniprotStatus) {

		this.uniprotStatus = uniprotStatus;
	}

	/**
	 * @param project
	 * @throws SQLException
	 * @throws AxisFault
	 */
	@Port(direction=Direction.INPUT, name="Project",description="Select Project",validateMethod="checkProject", order=9)
	public void selectProject(Project project) throws SQLException, AxisFault{

		try  {

			Map<String, ProteinSequence> sequences = CreateGenomeFile.getGenomeFromID(project.getGenomeCodeName(), ".faa");
			if(sequences == null){
				Workbench.getInstance().error("No sequences to perform the hmmer search. Please add a genome/metagenome file to this project!");
			}
			else{
				this.hmmer_loader = new SearchAndLoadHomologueSequences(sequences, project, project.isNCBIGenome(),Source.hmmer);
				this.hmmer_loader.setTimeLeftProgress(this.progress);
			}

			if(!project.isNCBIGenome()) {

				//this.hmmer_loader.setTaxonomicID(this.taxonomicIdentification);
				this.hmmer_loader.setTaxonomicID(project.getTaxonomyID()+"", this.database);
			}

			int errorOutput =  this.hmmer_loader.hmmerSearchSequences(this.database, Integer.parseInt(this.numberOfAlignments), Double.parseDouble(this.eVal), this.uniprotStatus);

			if(errorOutput == 0) {

				if(this.hmmer_loader.removeDuplicates() && !this.hmmer_loader.isCancel().get()) {

					errorOutput = this.hmmer_loader.hmmerSearchSequences(this.database, Integer.parseInt(this.numberOfAlignments), Double.parseDouble(this.eVal), this.uniprotStatus);
				}

				if(errorOutput == 0 && !hmmer_loader.isCancel().get()) {

					AIBenchUtils.updateHomologyDataView(project.getName());
					Workbench.getInstance().info("Hmmer search complete!");
				}
			}

			if(errorOutput > 0) {

				Workbench.getInstance().error("Errors have ocurred while processsing "+errorOutput+" query(ies).");
			}

			if(this.hmmer_loader.isCancel().get()) {

				Workbench.getInstance().warn("HMMER search cancelled!");
			}
		}
		catch (Error e) {Workbench.getInstance().error(e); e.printStackTrace();}//e.printStackTrace();
		catch (IOException e) {Workbench.getInstance().error(e);e.printStackTrace();}//e.printStackTrace();
		catch (ParseException e) {Workbench.getInstance().error(e);e.printStackTrace();}//e.printStackTrace();
		catch (Exception e) {Workbench.getInstance().error(e);e.printStackTrace();}
	}


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

			if(project.getGenomeCodeName()==null)
			{
				throw new IllegalArgumentException("Set the genome fasta file(s) for project "+project.getName());
			}

			if(!project.isNCBIGenome() && project.getTaxonomyID()<0) {

				throw new IllegalArgumentException("The loaded genome is not in the NCBI fasta format.\nPlease enter the taxonomic identification from NCBI taxonomy.");
			}

			if(!project.isFaaFiles()) {

				throw new IllegalArgumentException("Please add 'faa' files to perform hmmer homology searches.");
			}
		}
	}

	/**
	 * @author ODias
	 *
	 */
	public enum HmmerDatabase {
		
		
		uniprotkb,
		swissprot,
		uniprotrefprot,
		unimes,
		nr,
		refseq,
		env_nr,
		pfamseq,
		rp,
		pdb
		
	}
}
