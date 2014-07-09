/**
 * 
 */
package operations;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.biojava3.core.sequence.ProteinSequence;

import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.NcbiAPI;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQLMultiThread;
import pt.uminho.sysbio.common.local.alignments.core.Run_Similarity_Search.Method;
import pt.uminho.sysbio.common.utilities.io.FileUtils;
import pt.uminho.sysbio.merlin.gpr.rules.core.FilterModelReactions;
import pt.uminho.sysbio.merlin.gpr.rules.core.IdentifyGenomeSubunits;
import pt.uminho.sysbio.merlin.gpr.rules.core.output.ReactionsGPR_CI;
import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;
import utilities.AIBenchUtils;
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
@Operation(description="Generate and integrate gene-reactions connections.",name="GPRs generator.")
public class Create_GPRs {

	private MySQLMultiThread msqlmt;
	private long reference_organism_id;
	private double similarity_threshold;
	private double referenceTaxonomyThreshold;
	private boolean compareToFullGenome;
	private boolean identifyGPRs;
	private boolean integrateToDatabase;
	private boolean keepReactionsWithNotes;
	private boolean originalReaction;
	private boolean generateGPRs;
	private TimeLeftProgress  progress = new TimeLeftProgress();
	private AtomicBoolean cancel;
	private Project project;
	private Map<String, ProteinSequence> genome;
	private boolean keepManualReactions;
	private boolean removeReactions;
	private double threshold;

	/**
	 * @param project
	 */
	@Port(name="Project",description="Select Project",direction=Direction.INPUT,order=1, validateMethod="checkProject")
	public void setProject(Project project) {

		this.project = project;
		this.msqlmt = project.getDatabase().getMySqlCredentials();
		this.reference_organism_id = project.getTaxonomyID();
		this.originalReaction = !project.isCompartmentalisedModel();
	}

	@Port(name="Orthologues Similarity Threshold",description="Set orthologues similarity threshold.",direction=Direction.INPUT,order=2, validateMethod="checkDouble", defaultValue="0.1")
	public void setSimilarity_threshold(double similarity_threshold) {

		this.similarity_threshold = similarity_threshold;
	}

	@Port(name="Paralogues Similarity Threshold",description="Set paralogues similarity threshold.",direction=Direction.INPUT,order=3, validateMethod="checkDouble", defaultValue="0.25")
	public void setReferenceTaxonomyThreshold(double referenceTaxonomyThreshold) {

		this.referenceTaxonomyThreshold = referenceTaxonomyThreshold;
	}

	@Port(name="Compare to full genome", description="Compare to full genome instead of just the annotated genes.",direction=Direction.INPUT,order=4, defaultValue="true")
	public void setCompareToFullGenome(boolean compareToFullGenome) {

		this.compareToFullGenome = compareToFullGenome;
	}

	@Port(name="Identify GPRs", description="Identify the GPRs.",direction=Direction.INPUT,order=5, defaultValue="true")
	public void setIdentifyGPRs(boolean identifyGPRs) {

		this.identifyGPRs = identifyGPRs;
	}

	@Port(name="Generate GPRs", description="Generate the GPRs.",direction=Direction.INPUT,order=6, defaultValue="true")
	public void setGenereateGPRs(boolean generateGPRs) {

		this.generateGPRs = generateGPRs;
	}

	@Port(name="Integrate GPRs", description="Integrate the GPRs in the model.", direction=Direction.INPUT, order=7, defaultValue="false")
	public void setIntegrateGPRs(boolean integrateToDatabase) {

		this.integrateToDatabase = integrateToDatabase;
	}

	@Port(name="Genes integration threshold", description="Genes integration threshold.", direction=Direction.INPUT, order=8, validateMethod="checkDouble", defaultValue="0.2")
	public void setThreshold(double threshold) {

		this.threshold = threshold;
	}

	@Port(name="Remove reactions", description="Remove reactions without GPRs, associated to enzymes with GPRs.", direction=Direction.INPUT, order=9, defaultValue="false")
	public void setRemoveReactions(boolean removeReactions) {

		this.removeReactions = removeReactions;
	}

	//"The following options will only be active when this one is set to true.")

	@Port(name="Keep Reactions With Notes", description="Keep Reactions With Annotations.", direction=Direction.INPUT, order=10, defaultValue="true")
	public void setKeepReactionsWithNotes(boolean keepReactionsWithNotes) {

		this.keepReactionsWithNotes = keepReactionsWithNotes;
	}

	@Port(name="Keep Manual Reactions", description="Keep Reactions Manually Inserted in the model.", direction=Direction.INPUT, order=11, defaultValue="true")
	public void setKeepManualReactions(boolean keepManualReactions) {

		try {

			this.keepManualReactions = keepManualReactions;

			this.cancel = new AtomicBoolean(false);

			Logger logger = Logger.getLogger(Create_GPRs.class .getName());

			FileHandler fileTxt = new FileHandler(FileUtils.getCurrentTempDirectory()+"Logging.txt");
			SimpleFormatter formatterTxt = new SimpleFormatter();
			fileTxt.setFormatter(formatterTxt);
			logger.setUseParentHandlers(false);
			logger.addHandler(fileTxt);
			boolean identifiedWithoutErros = false;

			if(this.identifyGPRs && !this.cancel.get()) {

				Method method = Method.SmithWaterman;

				Map<String, List<String>> ec_numbers = IdentifyGenomeSubunits.getECNumbers(msqlmt);
				System.out.println("Enzymes size:\t"+ec_numbers.keySet().size());

				Map<String, ProteinSequence> newGenome = genome;

				if(project.isNCBIGenome())
					newGenome = NcbiAPI.getNCBILocusTags(genome);
				System.out.println("Genome size:\t"+genome.keySet().size());

				IdentifyGenomeSubunits i = new IdentifyGenomeSubunits(ec_numbers, newGenome, reference_organism_id, msqlmt, similarity_threshold, 
						referenceTaxonomyThreshold, method, cancel, compareToFullGenome);
				i.setProgress(progress);

				identifiedWithoutErros = i.runIdentification();
			}

			if(identifiedWithoutErros) {

				if(this.generateGPRs && !this.cancel.get()) {

					IdentifyGenomeSubunits i = new IdentifyGenomeSubunits(msqlmt);
					IdentifyGenomeSubunits.setLogger(logger);
					Map<String, ReactionsGPR_CI> ret = i.runAssignment(this.threshold);

					FilterModelReactions f = new FilterModelReactions(msqlmt, this.originalReaction);
					FilterModelReactions.setLogger(logger);
					f.filterReactions(ret);

					if(this.integrateToDatabase && !this.cancel.get()) {

						if(this.removeReactions)
							f.removeReactionsFromModel(keepReactionsWithNotes, this.keepManualReactions);
						f.setModelGPRsFromTool();
					}
				}

				if(this.cancel.get())
					Workbench.getInstance().warn("GPR job cancelled!");
				else {
					
					AIBenchUtils.updateAllViews(project.getName());
					Workbench.getInstance().info("GPR job finished!");
				}
			}
			else {

				if(this.cancel.get()) {

					Workbench.getInstance().warn("GPR job cancelled!");
				}

				Workbench.getInstance().error("merlin found some errors whilst performing this operation. Please try again later!");
			}

			fileTxt.close();
		} 
		catch (Exception e) {

			Workbench.getInstance().error("Error "+e.getMessage()+" has occured.");
			e.printStackTrace();
		}

	}

	/**
	 * @param similarity_threshold
	 */
	public void checkDouble(double double_) {

		if(double_>1 || double_<0) {

			throw new IllegalArgumentException("Please set a valid double (0<double<1).");
		}
	}

	/**
	 * @return the progress
	 */
	@Progress
	public TimeLeftProgress getProgress() {

		return progress;
	}

	/**
	 * @param cancel the cancel to set
	 */
	@Cancel
	public void setCancel() {

		progress.setTime(0, 0, 0);
		this.cancel.set(true);
	}

	/**
	 * @param project
	 */
	public void checkProject(Project project) {

		if(project == null) {

			throw new IllegalArgumentException("No Project Selected!");
		}
		else {

			this.project = project;

			if(this.project.getGenomeCodeName()==null) {

				throw new IllegalArgumentException("Please set the project fasta files!");
			}
			else if(project.getTaxonomyID()<0) {

				throw new IllegalArgumentException("Please enter the organism taxonomic identification from NCBI taxonomy to perform this operation.");
			}
			else {

				try {

					this.genome = CreateGenomeFile.getGenomeFromID(this.project.getGenomeCodeName(),".faa");

					if(this.identifyGPRs && this.genome==null) {

						throw new IllegalArgumentException("Please set the project fasta files!");
					}
				} 
				catch (Exception e) {

					e.printStackTrace();
					throw new IllegalArgumentException("Please set the project fasta files!");
				}
			}

			if(!project.isFaaFiles()) {

				throw new IllegalArgumentException("Please add 'faa' files to perform the transporters identification.");
			}
		}
	}
}
