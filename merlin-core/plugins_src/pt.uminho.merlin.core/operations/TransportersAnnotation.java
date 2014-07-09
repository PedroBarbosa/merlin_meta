package operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQLMultiThread;
import pt.uminho.sysbio.common.transporters.core.transport.reactions.TransportReactionsGeneration;
import pt.uminho.sysbio.common.transporters.core.transport.reactions.containerAssembly.PopulateTransportContainer;
import pt.uminho.sysbio.common.transporters.core.transport.reactions.containerAssembly.TransportContainer;
import pt.uminho.sysbio.common.transporters.core.transport.reactions.parseTransporters.AlignedGenesContainer;
import pt.uminho.sysbio.common.utilities.io.FileUtils;
import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;
import utilities.AIBenchUtils;
import datatypes.Project;
import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;
import gui.CustomGUI;

@Operation(name="Transporters Annotation", description="Transport reactions generation for later integration in the Model.")
public class TransportersAnnotation implements Observer {

	private Project project;
	private double alpha;
	private int minimalFrequency;
	private double threshold;
	private double beta;
	private boolean saveOnlyReactionsWithKEGGmetabolites;
	private boolean validateReaction;
	private AtomicBoolean cancel;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private AtomicInteger counter;
	private AtomicInteger querySize; 
	private long startTime;

	@Port(direction=Direction.INPUT, name="alpha",description=("Frequency and taxonomy scores leverage"), defaultValue = "0.3",validateMethod="checkAlpha", order=1)
	public void setAlpha(double alpha){
		this.alpha = alpha;
	}

	@Port(direction=Direction.INPUT, name="Minimum Frequency",description=("Minimum number of times a metabolite has to be referenced"), defaultValue = "2",validateMethod="checkMFrequency", order=2)
	public void setMinimalFrequency(int minimalFrequency){
		this.minimalFrequency = minimalFrequency;
	}

	@Port(direction=Direction.INPUT, name="beta Penalty",description=("Penalty for metabolites with less frequency than the minimum required"), defaultValue = "0.05",validateMethod="checkBeta", order=3)
	public void setBeta(double beta){
		this.beta = beta;
	}

	@Port(direction=Direction.INPUT, name="Cut-off threshold",description=("Cut-off threshold for metabolites selection"), defaultValue = "0.2",validateMethod="checkThreshold", order=4)
	public void setThreshold(double threshold){
		this.threshold = threshold;
	}

	@Port(direction=Direction.INPUT, name="Validate Reaction Balance",description=("Check reaction stoichiometry to validate reaction"), defaultValue = "true",validateMethod="reactionValidation", order=5)
	public void setValidateReaction(boolean validateReaction){
		this.validateReaction = validateReaction;
	}

	@Port(direction=Direction.INPUT, name="Save Only Reactions With KEGG metabolites",description=("Whether to save only reactions with KEGG metabolites"), validateMethod = "checkIsKEGGReaction", defaultValue = "true", order=6)
	public void setSaveOnlyReactionsWithKEGGmetabolites(boolean saveOnlyReactionsWithKEGGmetabolites){
		this.saveOnlyReactionsWithKEGGmetabolites=saveOnlyReactionsWithKEGGmetabolites;
	}

	@Port(direction=Direction.INPUT, name="Project",validateMethod="checkProject",description="Select Project", order=7)
	public void setProject(Project project) throws Exception {

		this.project = project;
		this.cancel = new AtomicBoolean(false);
		this.startTime = GregorianCalendar.getInstance().getTimeInMillis();
		MySQLMultiThread mysql_mt = project.getDatabase().getMySqlCredentials();
		String db_name = mysql_mt.get_database_name();
		String filePrefix = "Th_"+threshold+"__al_"+alpha+"__be_"+beta;
		String dir = (db_name+"/"+filePrefix+"/reactionValidation"+this.validateReaction+"/kegg_only"+this.saveOnlyReactionsWithKEGGmetabolites);
		String path = FileUtils.getCurrentTempDirectory(dir);

		try {

			TransportReactionsGeneration tre = new TransportReactionsGeneration(mysql_mt, project.isNCBIGenome(), project.getTaxonomyID());
			TransportContainer transportContainer = this.project.getTransportContainer();
			if(transportContainer == null || transportContainer.getAlpha()!= this.alpha 
					|| transportContainer.getBeta()!= this.beta 
					|| transportContainer.getMinimalFrequency()!= this.minimalFrequency 
					|| transportContainer.getThreshold()!= this.threshold
					|| transportContainer.isReactionsValidated()!=this.validateReaction 
					|| transportContainer.isKeggMetabolitesReactions()!=this.saveOnlyReactionsWithKEGGmetabolites) {

				transportContainer = null;

				String outPath = path+db_name+"UnAnnotatedTransporters.out";
				List<AlignedGenesContainer> tc_data= tre.getCandidatesFromDatabase(outPath, this.project.getProjectID());
				boolean go = true;

				String fileName = path+db_name+"__"+filePrefix+".transContainer";

				if(project.isTransporterLoaded() && (new File(fileName).exists())) {

					File file = new File(fileName);
					file.createNewFile();
					FileInputStream f_in = new  FileInputStream (file);
					ObjectInputStream obj_in = new ObjectInputStream (f_in);

					try {

						transportContainer = (TransportContainer) obj_in.readObject();
					}
					catch (ClassNotFoundException e) {e.printStackTrace();}
					obj_in.close();
					f_in.close();
					this.querySize = new AtomicInteger(1);
					this.counter = this.querySize;
					progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, this.counter.get(), this.querySize.get());
				}
				else {

					if(tre.getUnAnnotatedTransporters().size()>0) {

						float unAnnotated = tre.getUnAnnotatedTransporters().size();
						float initialSize = tre.getInitialHomolguesSize();
						go = unAnnotatedTransporters(unAnnotated,initialSize,outPath);
					}

					if(go) {
						
						int project_id = project.getProjectID();

						project.setTransporterLoaded(tre.parse_and_load_candidates(tc_data, project_id));

						Connection conn = new Connection(mysql_mt);

						PopulateTransportContainer populateTransportContainer;
						if(project.isNCBIGenome())
							populateTransportContainer = new PopulateTransportContainer(conn, this.alpha, this.minimalFrequency, this.beta, this.threshold, project_id);
						else
							populateTransportContainer = new PopulateTransportContainer(conn, this.alpha, this.minimalFrequency, this.beta, this.threshold, project.getTaxonomyID(),  project_id);

						populateTransportContainer.getDataFromDatabase();
						this.counter = new AtomicInteger(0);
						populateTransportContainer.setCounter(this.counter);
						this.querySize = new AtomicInteger(0);
						populateTransportContainer.addObserver(this);
						populateTransportContainer.setQuerySize(this.querySize);
						populateTransportContainer.setCancel(this.cancel);
						transportContainer = populateTransportContainer.loadContainer(this.saveOnlyReactionsWithKEGGmetabolites);

						this.saveTransportContainerFile(transportContainer, fileName);

						if(this.validateReaction) {

							transportContainer = populateTransportContainer.containerValidation(transportContainer, false);
							transportContainer.setReactionsValidated(this.validateReaction);
						}

						populateTransportContainer.creatReactionsFiles(transportContainer,path+db_name+"__"+filePrefix);
						populateTransportContainer = null;
					}
				}

				if(go) {

					if(this.cancel.get()) {

						Workbench.getInstance().info("Transport reactions generation cancelled!");
					}
					else {

						System.gc();
						this.project.setTransportContainer(transportContainer);
						this.project.setTransporterLoaded(true);
						AIBenchUtils.updateProjectView(project.getName());
						Workbench.getInstance().info("Transport reactions generated!");
					}
				}
			}
			else {

				Workbench.getInstance().info("Transport reactions already loaded!");
			}
		} 
		catch (Exception e) {

			e.printStackTrace(); 
			Workbench.getInstance().error(new Exception(e.getMessage()));
		} 
	}

	/**
	 * @param transportContainer
	 * @param fileName
	 * @return
	 */
	private boolean saveTransportContainerFile(TransportContainer transportContainer, String fileName) {

		try {

			File transContainer = new File(fileName);
			transContainer.createNewFile();
			FileOutputStream f_out = new  FileOutputStream(transContainer);
			ObjectOutputStream obj_out = new ObjectOutputStream (f_out);
			obj_out.writeObject(transportContainer);
			obj_out.close();
			f_out.close();
			return true;
		} 
		catch (IOException e1) {

			e1.printStackTrace();
		} 
		return false;
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
			else {

				if(!project.isNCBIGenome() && project.getTaxonomyID()<=0) {

					throw new IllegalArgumentException("The loaded genome is not in the NCBI fasta format.\nPlease enter the taxonomic identification from NCBI taxonomy.");
				}
			}

			if(!project.isSW_TransportersSearch()) {

				throw new IllegalArgumentException("Please perform Transporters Similarity Search!");
			}
		}
	}

	/**
	 * @param saveOnlyReactionsWithKEGGmetabolites
	 */
	public void checkIsKEGGReaction(boolean saveOnlyReactionsWithKEGGmetabolites) {

		this.saveOnlyReactionsWithKEGGmetabolites = saveOnlyReactionsWithKEGGmetabolites;
	}

	/**
	 * @param validateReaction
	 */
	public void reactionValidation(boolean validateReaction) {

		this.validateReaction = validateReaction;
	}


	/**
	 * @param project
	 */
	public void checkAlpha(double alpha) {

		if(alpha>1 || alpha<0) {

			throw new IllegalArgumentException("Please set a valid alpha value (0<alpha<1).");
		}
		else {

			this.alpha = alpha;
		}
	}

	/**
	 * @param project
	 */
	public void checkMFrequency(int mf) {

		if(mf<0) {

			throw new IllegalArgumentException("Please set a valid minimal Frequency value (0<minimal Frequency<1).");
		}
		else {

			this.minimalFrequency = mf;
		}
	}

	/**
	 * @param project
	 */
	public void checkBeta(double beta) {

		if(beta>1 || beta<0) {

			throw new IllegalArgumentException("Please set a valid beta value (0<beta<1).");
		}
		else {

			this.beta = beta;
		}
	}

	/**
	 * @param project
	 */
	public void checkThreshold(double threshold) {

		if(threshold>1 || threshold<0) {

			throw new IllegalArgumentException("Please set a valid threshold value (0<threshold<1).");
		}
		else {

			this.threshold = threshold;
		}
	}

	/**
	 * @param unAnnotated
	 * @param initialSize
	 * @param location
	 * @return
	 */
	private static boolean unAnnotatedTransporters(float unAnnotated, float initialSize, String location){

		float percentage = (unAnnotated/initialSize)*100;
		int i =CustomGUI.stopQuestion("TCDB Unannoted transporters",
				percentage+"% of the TCDB genes identified as homolgues of the genome being studied are currently not annotated. Do you wish to continue?",
				new String[]{"Continue", "Abort", "Info"});

		String slash = "/", newSlash = "/";

		if(System.getProperty("os.name").toLowerCase().contains("windows")) {

			newSlash = "\\";
		}


		if(i<2) {

			switch (i)
			{
			case 0:return true;
			case 1: return false;
			default:return true;
			}
		}
		else {

			Workbench.getInstance().info("The genome currently being studied has homology with "+initialSize+" TCDB genes. However, "+unAnnotated+" of such genes\n" +
					"are not annotated in merlin's current database.\n" +
					"If you continue, merlin will generate transport reactions with the current database.\n" +
					"However, if you want to generate transport reactions for the whole genome, you\n" +
					"may annotate the missing TCDB entries, filling in the following columns:" +
					"\n\t-direction\n\t-metabolite\n\t-reversibility\n\t-reacting_metabolites\n\t-equation\n" +
					"in the file located at :\n"+location.replace("plugins_bin\\merlin_core","").replace("plugins_bin/merlin_core","").replace("/../..","").replace(slash,newSlash)+
					"\naccording to the example.\n" +
					"Afterwards go to Transporters> New Transporters Loading and select the file.\n" +
					"Please send your file to odias@deb.uminho.pt prior to submission, to check your data.");
			return unAnnotatedTransporters( unAnnotated, initialSize, location);
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

	@Override
	public void update(Observable o, Object arg) {

		progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, this.counter.get(), this.querySize.get());
	}
}
