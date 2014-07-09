package operations;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;
import utilities.AIBenchUtils;
import datatypes.IntegrateCompartmentsData;
import datatypes.IntegrateTransportersData;
import datatypes.Project;
import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;

@Operation(name="Transporters Reactions Integration", description= "This operation integrates the generated transport reactions with the model reactions")
public class IntegrateTransporterstoDatabase {


	private IntegrateTransportersData transportersData;
	private TimeLeftProgress progress = new TimeLeftProgress();

	@Port(name="Select Project",description="Select Project", validateMethod="checkProject", direction = Direction.INPUT, order=1)
	public void setProject(Project project) throws Exception{


		if(project.isGeneDataAvailable()) {

			if(project.isMetabolicDataAvailable()) {
				
				if(project.getTransportContainer()!= null) {

					this.transportersData = new IntegrateTransportersData(project);
					this.transportersData.setTimeLeftProgress(this.progress);

					boolean result = this.transportersData.performIntegration();
					
					if(result) {

						if(!transportersData.isCancel().get()) {
							
							if(project.isCompartmentalisedModel()) {
								
								IntegrateCompartmentsData integration = new IntegrateCompartmentsData(project);
								integration.setTimeLeftProgress(progress);
								integration.performIntegration();
								result = integration.assignCompartmentsToTransportReactions(new ArrayList<String>());
							}
							
							Workbench.getInstance().info("Transporters integration complete!");
							project.setTransporterLoaded(result);
							AIBenchUtils.updateAllViews(project.getName());
						}
						else {

							Workbench.getInstance().warn("Transporters integration cancelled!");
						}
					}
					else{

						Workbench.getInstance().info("An error occurred while performing the operation.");
					}
				}
				else {

					if(project.isSW_TransportersSearch()) {

						Workbench.getInstance().info("Please generate the tranport reactions!");
					}
					else {

						if(project.isTransporterLoaded()) {

							Workbench.getInstance().info("Please perform the tranporters identification and generate the tranport reactions!");
						}
						else {

							Workbench.getInstance().info("Please build the tranporters container!");
						}
					}
				}
			}
			else {

				Workbench.getInstance().warn("Metabolic data for integration unavailable!");
			}
		}
		else {

			Workbench.getInstance().error("Gene data for integration unavailable!");
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
	public void cancel(){
		this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-GregorianCalendar.getInstance().getTimeInMillis()),1,1);
		this.transportersData.setCancel();
	}

	/**
	 * @param project
	 */
	public void checkProject(Project project){

		if(project == null) {

			throw new IllegalArgumentException("No Project Selected!");
		}
		else {

			if(project.getTransportContainer()==null) {

				throw new IllegalArgumentException("Please perform the Transporters Annotation operation before integrating transporters data.");
			}
		}
	}
}
