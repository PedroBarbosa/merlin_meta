package operations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;
import utilities.AIBenchUtils;
import datatypes.IntegrateCompartmentsData;
import datatypes.Project;
import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;

@Operation(name="Compartments Prediction Integration", description= "This operation assigns the model reactions (including the transport reactions) to the predicted compartments")
public class IntegrateCompartmentstoDatabase {

	private boolean compartmentalise;
	private boolean loaded;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private IntegrateCompartmentsData integration;
	private List<String> ignoreList;

	/**
	 * @param project
	 */
	@Port(name="Compartmentalise model",description="Compartmentalise model", defaultValue="true", validateMethod="checkCompartmentalise",direction = Direction.INPUT, order=1)
	public void compartmentalizeModel(boolean compartmentalise){

		this.compartmentalise = compartmentalise; 
	}
	
	/**
	 * @param project
	 */
	@Port(name="Set compartments to ignore",description="Use abbreviations separeted by commas(,)", validateMethod="checkList", direction = Direction.INPUT, order=2)
	public void setProject(String ignore){
	
		
	}

	/**
	 * @param project
	 * @throws SQLException 
	 */
	@Port(name="Select Project",description="Select Project", validateMethod="checkProject", direction = Direction.INPUT, order=3)
	public void setProject(Project project) throws SQLException {

		if(project.isGeneDataAvailable()) {

			integration = new IntegrateCompartmentsData(project);
			integration.setTimeLeftProgress(progress);
			boolean result = false;
			
			if(this.loaded) {
				
				integration.initProcessCompartments();
			}
			else {

				result = integration.performIntegration();
			}
			
			if(this.compartmentalise) {

				result = integration.assignCompartmentsToMetabolicReactions(ignoreList);

				if(project.isTransporterLoaded()) {

					try {
						
						result = integration.assignCompartmentsToTransportReactions(ignoreList);
					}
					catch (Exception e) {
						
						Workbench.getInstance().error(e);
					}
				}

				//project.setCompartmentalisedModel(result);
			}

			if(result) {

				AIBenchUtils.updateAllViews(project.getName());
				Workbench.getInstance().info("Compartments integration complete!");
			}
			else{

				Workbench.getInstance().error("An error occurred while performing the operation.");
			}
		}
		else {

			Workbench.getInstance().error("Gene data for integration unavailable!");
		}

	}

	/**
	 * @param compartmentalise
	 */
	public void checkCompartmentalise(boolean compartmentalise){ 

		this.compartmentalise = compartmentalise; 
	}
	
	/**
	 * @param compartmentalise
	 */
	public void checkList(String ignore){ 

		String[] ignoreArray = ignore.split(",");
		ignoreList = new ArrayList<String>();
		
		for(String ig :ignoreArray) {
			
			ignoreList.add(ig.toLowerCase().trim());
		}
	}

	/**
	 * @param project
	 */
	public void checkProject(Project project){

		if(project == null) {

			throw new IllegalArgumentException("No Project Selected!");
		}
		else {

			if(!project.isCompartmentsLoaded()) {

				throw new IllegalArgumentException("Please perform the compartments prediction operation before integrating compartments data.");
			}
			else if(project.getGeneCompartments()==null) {

				throw new IllegalArgumentException("Please perform the compartments prediction operation before integrating compartments data.");
			}

			Statement stmt;

			try {
				
				Connection connection = new Connection(project.getDatabase().getMySqlCredentials());

				stmt = connection.createStatement();

				ResultSet rs = stmt.executeQuery("SELECT * FROM gene_has_compartment;");

				if(rs.next()) {

					if(!this.compartmentalise) {

						throw new IllegalArgumentException("Compartment information already loaded. Please clean and reload local database before performing new compartment information integration.");
					}
					this.loaded = true;
				}
				stmt.close();
			}
			catch (SQLException e) {e.printStackTrace();}	
		}
	}
	
	/**
	 * @return
	 */
	@Progress
	public TimeLeftProgress getProgress() {

		return this.progress;
	}

	/**
	 * 
	 */
	@Cancel
	public void cancel() {
		
		this.progress.setTime(0,1,1);
		this.integration.setCancel();
	}
}
