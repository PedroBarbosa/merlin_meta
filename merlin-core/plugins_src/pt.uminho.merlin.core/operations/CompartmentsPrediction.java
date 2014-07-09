package operations;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.NcbiServiceStub_API.KINGDOM;
import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQLMultiThread;
import pt.uminho.sysbio.common.transporters.core.compartments.GeneCompartments;
import pt.uminho.sysbio.common.transporters.core.compartments.PSort3;
import pt.uminho.sysbio.common.transporters.core.compartments.PSortInterface;
import pt.uminho.sysbio.common.transporters.core.compartments.WoLFPSORT;
import utilities.AIBenchUtils;
import datatypes.Project;
import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;

@Operation(name="Perform compartments prediction", description="Compartments prediction operation.")
public class CompartmentsPrediction {

	private KINGDOM kingdom;
	private double threshold;
	private OrganismType type;
	private PSortInterface psort;

	/**
	 * @param kingdom
	 */
	@Port(direction=Direction.INPUT, name="KINGDOM",description=("Select kingdom"), defaultValue = "Eukaryota", validateMethod="checkKingdom", order=1)
	public void setKingdom(KINGDOM kingdom) {

		this.kingdom = kingdom;
	}

	/**
	 * @param type
	 */
	@Port(direction=Direction.INPUT, name="Organism type",description=("Select organism type. If the organism is not an Eukaryota this selection will be ignored!"), defaultValue = "fungi", validateMethod="checkOrganismType", order=2)
	public void setKingdom(OrganismType type) {

		this.type = type;
	}

	@Port(direction=Direction.INPUT, name="Threshold",description=("Select threshold for secondary compartments selection"), defaultValue = "10", validateMethod="checkThreshold", order=2)
	public void setKingdom(double threshold) {

		this.threshold = threshold;
	}

	@Port(direction=Direction.INPUT, name="Project",description="Select Project",validateMethod="checkProject", order=3)
	public void setProject(Project project) throws Exception {

		MySQLMultiThread mysqlmt = project.getDatabase().getMySqlCredentials();
		Connection conn = new Connection(mysqlmt);
		String genomeCode = project.getGenomeCodeName();
		boolean isNCBI = project.isNCBIGenome();

		boolean go=false;

		if(project.isFaaFiles()) {

			if(project.getGeneCompartments()==null) {

				if(this.kingdom.equals(KINGDOM.Eukaryota)) {

					this.psort = new WoLFPSORT(conn, genomeCode,isNCBI, project.getProjectID());

					if(project.isCompartmentsLoaded()) {

						go = false;
					}
					else {

						go = this.psort.getCompartments(this.type.toString());
					}
				}
				else {

					if(project.isCompartmentsLoaded()) {

						this.psort = new PSort3(conn, project.getProjectID());
						go=false;
					}
					else {

						this.psort = new PSort3(conn, project.getPsort3Results(), project.getProjectID());
						go = this.psort.getCompartments(null);
					}
				}

				try {

					if(go) {

						this.psort.loadCompartmentsInformation();
					}

					Map<String, GeneCompartments> geneCompartments = this.psort.getBestCompartmentsByGene(this.threshold);

					if(this.psort.isCancel().get()) {

						project.setGeneCompartmens(null);
						//project.setCompartmentsLoaded(false);
						Workbench.getInstance().warn("Compartments prediction cancelled!");
					}
					else {

						project.setGeneCompartmens(geneCompartments);	
						//project.setCompartmentsLoaded(true);
						AIBenchUtils.updateProjectView(project.getName());
						Workbench.getInstance().info("Compartments prediction complete!");
					}
				}
				catch (Exception e) {

					e.printStackTrace();

					Workbench.getInstance().error("An error occurred while performing the compartments prediction.\n" +
							" Please try again later.");
				}
			}
			else {

				//project.setCompartmentsLoaded(true);
				Workbench.getInstance().info("Compartments prediction already performed!");
			}
		}
		else {

			//project.setCompartmentsLoaded(true);
			Workbench.getInstance().info("Please set amino acid fasta files!");
		}
	}

	/**
	 * 
	 */
	@Cancel
	public void cancel() {

		this.psort.setCancel(new AtomicBoolean(true));
	}

	/**
	 * @param kingdom
	 */
	public void checkKingdom(KINGDOM kingdom){
		this.kingdom = kingdom;
	}

	/**
	 * @param kingdom
	 */
	public void checkOrganismType(OrganismType type){
		this.type = type;
	}

	/**
	 * @param similarity_threshold
	 */
	public void checkThreshold(double threshold) {

		if(threshold>100 || threshold<1) {

			throw new IllegalArgumentException("Please set a valid threshold (0<threshold<100).");
		}
		else {

			this.threshold = threshold;
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

			if(!this.kingdom.equals(KINGDOM.Eukaryota)) {

				if(!project.isCompartmentsLoaded()) {

					if(project.getPsort3Results() == null) {

						throw new IllegalArgumentException("Please perform the PSORT3 compartments identification, on http://www.psort.org/psortb/index.html," +
								" save the outputFiles and submit the results using the Load PSort3 Results function!");
					}
				}
				//			try 
				//			{
				//				CreateGenomeFile.getGenomeFromID(project.getGenomeID());
				//			} 
				//			catch (Exception e)
				//			{
				//				throw new IllegalArgumentException("Please set the project fasta files!");
				//			}
			}
		}
	}

	/**
	 * @author ODias
	 *
	 */
	enum OrganismType{
		animal,
		plant,
		fungi
	}
}
