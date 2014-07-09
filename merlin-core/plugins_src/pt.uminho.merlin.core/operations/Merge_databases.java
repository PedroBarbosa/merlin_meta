/**
 * 
 */
package operations;

import java.util.List;

import pt.uminho.sysbio.merge.databases.readFromDatabase.RetrieveHomologyData;
import pt.uminho.sysbio.merge.databases.containers.HomologySetup;
import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import alignment.ProcessHomologySetup;
import datatypes.Project;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;

/**
 * @author pedro
 *
 */

@Operation(description="Merge database from a different project. This can be useful when a user performed the annotation process in parallel in different machines (e.g. big file that was divided in several ones) so in the end it is possible to gather all information again in one project", name="Merge databases")
public class Merge_databases {

		private Project host_project;
		private Project target_project;
		
		
		@Port(direction=Direction.INPUT,validateMethod ="validateHostProject", name="Host Project", description= "Select the project that will be populated ", order=1)
		public void selectedHostProject(Project project){
			
		}
		@Port(direction=Direction.INPUT,validateMethod ="validateDesiredProject", name="Target Project", description= "Select the project from where you want to load information", order=2)
		public void selectedTargetProject(Project project) {
	
			
			
		try{
			
			
			Workbench.getInstance().info("This operation may take several hours, depending on the database size");
			
			Connection c1 = (Connection) new Connection(this.host_project.getDatabase().getMySqlCredentials().get_database_host(), 
					this.host_project.getDatabase().getMySqlCredentials().get_database_port(),this.host_project.getDatabase().getMySqlCredentials().get_database_name(), 
					this.host_project.getDatabase().getMySqlCredentials().get_database_user(), 	this.host_project.getDatabase().getMySqlCredentials().get_database_password());
		
			Connection c2 = (Connection) new Connection(this.target_project.getDatabase().getMySqlCredentials().get_database_host(), 
					this.target_project.getDatabase().getMySqlCredentials().get_database_port(), this.target_project.getDatabase().getMySqlCredentials().get_database_name(), 
					this.target_project.getDatabase().getMySqlCredentials().get_database_user(), 	this.target_project.getDatabase().getMySqlCredentials().get_database_password());
			
			
			RetrieveHomologyData r = new RetrieveHomologyData(c2);
			r.retrieveAllData();
	
			List<HomologySetup> homologySetupList = r.getHomologySetupFromDatabase();
			System.out.println(homologySetupList);
	
			ProcessHomologySetup p = new ProcessHomologySetup(c1);
	
			p.loadHomologySetup(homologySetupList);	
			Workbench.getInstance().info("Merging database process completed");
		}
		catch (Exception e) {
			Workbench.getInstance().error("Errors occurred while performing the operation " + e);
		}

		}

		
		
		

		/**
		 * @param project
		 */
		public void validateHostProject(Project project) {

			if(project == null){

				throw new IllegalArgumentException("No Project Selected in the Host Project field");
			}
			else {

				this.host_project = project;

			}
		}
		
		public void validateDesiredProject(Project project){
			
			if(project == null){
				throw new IllegalArgumentException("No Project Selected in the Desired Project field");
			}
			else{
				
				if(project.getName().equals(this.host_project.getName())){
					
					throw new IllegalArgumentException("Same project selected. Please select another project in one of the fields");
				}
				else{
					this.target_project = project;
				}
			}
		}
}