/**
 * 
 */
package operations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.biojava3.core.sequence.ProteinSequence;

import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.sysbio.common.local.alignments.core.Run_Similarity_Search;
import pt.uminho.sysbio.common.local.alignments.core.Run_Similarity_Search.Method;
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
@Operation(name="Transporters Identification",description="Perform a semi-automatic identication of the transporter systems encoded in the genome. This process may take several hours, depending on the user's local computer processing unit.")
public class TransportersSimilaritySearch implements Observer {

	private Project project;
	private String tcdb_url;
	private  int minimum_number_of_helices;
	private  double similarity_threshold;
	private Method method;
	private List<File> tmhmmFiles;
	private Map<String, ProteinSequence> sequences;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private AtomicBoolean cancel;
	private AtomicInteger querySize;
	private AtomicInteger counter = new AtomicInteger(0);
	private long startTime;


	/**
	 * @param tcdb_url the tcdb_url to set
	 */
	@Port(direction=Direction.INPUT, name="TCDB URL",description="URL for TCDB sequences fasta file",defaultValue="http://www.tcdb.org/public/tcdb",order=1)
	public void setTcdb_url(String tcdb_url) {
		this.tcdb_url = tcdb_url;
	}

	/**
	 * @param tmhmm_file_dir the tmhmm_file_dir to set
	 */
	@Port(direction=Direction.INPUT, name="TMHMM Files",description="Path to TMHMM files directory",validateMethod="checkTMHMM",order=2)
	public void setTmhmm_file_dir(File tmhmm_file_dir) {
	}

	/**
	 * @param minimum_number_of_helices the minimum_number_of_helices to set
	 */
	@Port(direction=Direction.INPUT, name="Minimum number of helices",description="Minimum number of helices on gene sequence",defaultValue="1",validateMethod="checkMNOH",order=3)
	public void setMinimum_number_of_helices(int minimum_number_of_helices) {
		this.minimum_number_of_helices = minimum_number_of_helices;
	}

	/**
	 * @param similarity_threshold the similarity_threshold to set
	 */
	@Port(direction=Direction.INPUT, name="Similarity threshold",description="Initial threshold for similarity to TCDB gene",defaultValue="0.1",validateMethod="checkSimilarityThreshold",order=4)
	public void setSimilarity_threshold(double similarity_threshold) {
		this.similarity_threshold = similarity_threshold;
	}

	/**
	 * @param method the method to set
	 */
	@Port(direction=Direction.INPUT, name="Method",description="Method for alignment",order=5)
	public void setMethod(Method method) {

		this.method = method;
	}


	/**
	 * @param project
	 */
	@Port(direction=Direction.INPUT, name="Project",description="Select Project",validateMethod="checkProject",order=6)
	public void setProject(Project project) {

		this.project = project;
		int project_id = this.project.getProjectID();

		this.startTime = GregorianCalendar.getInstance().getTimeInMillis();

		try {
			
			this.project.setTransporterLoaded(false);
			this.project.setSW_TransportersSearch(false);

			this.counter = new AtomicInteger(0);
			this.querySize = new AtomicInteger(0);
			this.cancel = new AtomicBoolean(false);
			Run_Similarity_Search run_smith_waterman = new Run_Similarity_Search(this.project.getDatabase().getMySqlCredentials(),
					Run_Similarity_Search.set_TCDB(this.tcdb_url),this.tmhmmFiles,this.minimum_number_of_helices,this.similarity_threshold,this.method,this.project.isNCBIGenome(),
					this.sequences, this.cancel, this.querySize, this.counter, project_id);

			if(project.isUseProxy()) {

				run_smith_waterman.setUseProxy(true);
				run_smith_waterman.setHost(project.getProxy_host());
				run_smith_waterman.setPort(project.getProxy_port());

				if(project.isUseAuthentication()) {

					run_smith_waterman.setUseAuthentication(true);
					run_smith_waterman.setUser(project.getProxy_username());
					run_smith_waterman.setPass(project.getProxy_password());
				}
			}
			
			run_smith_waterman.addObserver(this);
			run_smith_waterman.run_TransportSearch();
			

			if(run_smith_waterman.isAlreadyProcessed()) {

				Workbench.getInstance().info("Transporter candidates already processed.");
				this.project.setSW_TransportersSearch(true);
			}
			else if(run_smith_waterman.isProcessed()) {

				if(this.cancel.get()) {

					Workbench.getInstance().info("Transport candidates search cancelled!");
				}
				else {

					this.project.setSW_TransportersSearch(true);
					AIBenchUtils.updateProjectView(project.getName());
					Workbench.getInstance().info("Transporter candidates search performed.");
				}


			}
		}
		catch (IOException e) {

			e.printStackTrace();

			Workbench.getInstance().error("The TMHMM files are not suitable for this project.\n" +
					" Please check your TMHMM files and try again.");
		}
		catch (Exception e) {

			e.printStackTrace();

			Workbench.getInstance().error("An error occurred while performing the Similarity search.\n" +
					" Please check your TMHMM files and try again.");
		}
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
			else if(!project.isNCBIGenome() && project.getTaxonomyID()<0) {

				throw new IllegalArgumentException("The loaded genome is not in the NCBI fasta format.\nPlease enter the taxonomic identification from NCBI taxonomy.");
			}
			else {

				try {

					this.sequences = CreateGenomeFile.getGenomeFromID(this.project.getGenomeCodeName(),".faa");

					if(this.sequences==null) {

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

	/**
	 * @param tmhmm_file_dir
	 */
	public void checkTMHMM(File tmhmm_file_dir) {

		if(tmhmm_file_dir == null || tmhmm_file_dir.toString().isEmpty()) {

			throw new IllegalArgumentException("TMHMM files directory not set!");
		}
		else {

			if(!tmhmm_file_dir.isDirectory()) {

				tmhmm_file_dir = new File(tmhmm_file_dir.getParent().toString());
			}

			List<File> tmhmmFiles = new ArrayList<File>();
			for(File f: tmhmm_file_dir.listFiles()) {

				if(f.getName().endsWith(".tmhmm")) {

					tmhmmFiles.add(f);
				}
			}

			if(tmhmmFiles.isEmpty()) {

				throw new IllegalArgumentException("Please Select a directory with TMHMM files!");
			}
			else {

				this.tmhmmFiles = tmhmmFiles;
			}
		}
	}

	/**
	 * @param minimum_number_of_helices
	 */
	public void checkMNOH(int minimum_number_of_helices) {

		if(minimum_number_of_helices<0) {

			throw new IllegalArgumentException("The minimum number of helices should be higher than 1!");
		}
		else {

			this.minimum_number_of_helices=minimum_number_of_helices;
		}
	}

	/**
	 * @param similarity_threshold
	 */
	public void checkSimilarityThreshold(double similarity_threshold) {

		if(similarity_threshold>1 || similarity_threshold<0) {

			throw new IllegalArgumentException("Please set a valid threshold (0<threshold<1).");
		}
		else {

			this.similarity_threshold = similarity_threshold;
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

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {

		progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, this.counter.get(), this.querySize.get());
	}
}
