package operations;

import java.util.GregorianCalendar;

import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;
import remote.GetAndLoadKeggData;
import utilities.AIBenchUtils;
import datatypes.Project;
import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;

@Operation(name="Load Database",description="Load local database with KEGG metabolic information")
public class Database_loader {


	private Project project;
	private String organism;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private GetAndLoadKeggData kegg_loader;

	@Port(direction=Direction.INPUT, name="Project",description="Select Project",//validateMethod="checkProject",
			order=1)
	public void setUser(Project project){
		this.project = project;
	}
	
//	/**
//	 * @param project
//	 */
//	public void checkProject(Project project){
//		this.project = project;
//
//		if(this.project==null)
//		{
//			throw new IllegalArgumentException("Please select a project.");
//		}
//
//	}

	@Port(direction=Direction.INPUT, name="Organism",//validateMethod="validateOrg",
			order=2)
	public void setOrganism(String organism) {
		
		this.organism=organism;
		this.kegg_loader = new GetAndLoadKeggData(this.project, this.organism);
		
		this.kegg_loader.setTimeLeftProgress(this.progress);
		
		boolean output = this.kegg_loader.get_data();
		
		if (output) {
			
			output = this.kegg_loader.load_data();
		}
		
		if(output) {
			
			if(!this.kegg_loader.isCancel().get()){
				
//				if(!this.organism.trim().isEmpty()) {
//					
//					this.project.setGeneDataAvailable(true);
//				}
//				this.project.setMetabolicDataAvailable(true);
				AIBenchUtils.updateAllViews(project.getName());
				Workbench.getInstance().info("Database successfully loaded.");
			}
			else {
				
				Workbench.getInstance().warn("Database loading cancelled!");
			}
		}
		else {
			
			Workbench.getInstance().info("An error occurred while performing the operation.");
		}
		
	}

//	/**
//	 * @param org
//	 */
//	public void validateOrg(String organism) {
//		
//		this.organism=organism;
//		
//		if(organism.isEmpty()) {
//			
//			Workbench.getInstance().info("No organism related information will be loaded!");
//		}
//		new Get_and_load_Kegg_data(this.project, this.organism);
//	}
	
	/**
	 * @return
	 */
	@Progress
	public TimeLeftProgress getProgress() {
		
		return progress;
	}
	
	/**
	 * 
	 */
	@Cancel
	public void cancel() {
		
		this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-GregorianCalendar.getInstance().getTimeInMillis()),1,1);
		this.kegg_loader.setCancel();
	}
}
