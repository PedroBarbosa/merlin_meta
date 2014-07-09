package operations;

import java.io.File;

import pt.uminho.sysbio.common.transporters.core.transport.reactions.TransportReactionsGeneration;
import datatypes.Project;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;

@Operation(name="Load Transporters", description="Load TCDB transporters annotations.")
public class NewTransportersLoading {

	private Project project;
	private File file;

	@Port(direction=Direction.INPUT, name="Project",description="Select Project",validateMethod="checkProject", order=5)
	public void load(Project project) {
		
		this.project = project;
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
		}
	}

	@Port(name="File", direction=Direction.INPUT,validateMethod="validateFile", description="Select File", order=2)
	public void LoadNewTransporter(File file) {
		
		TransportReactionsGeneration tre = new TransportReactionsGeneration(this.project.getDatabase().getMySqlCredentials());
		boolean output = tre.parseAndLoadTransportersDatabase(this.file,false);
		
		if(output) {
			
			Workbench.getInstance().info("Information successfully loaded!");
		}
		else {
			
			Workbench.getInstance().info("An error occurred while loading the information.");
		}
	}

	/**
	 * @param file
	 */
	public void validateFile(File file) {
		
		if(file.isDirectory()) {
			
			throw new IllegalArgumentException("Please select a single file");
		}

		if(file.getName().endsWith("UnAnnotatedTransporters.out")) {
			
			throw new IllegalArgumentException("Please send this file to odias@deb.uminho.pt for format validation!");
		}
		else
		{
			if(file.getName().endsWith(".odiasCheckedTransportersFile")) {
				
				this.file = file;
			}
			else {
				
				throw new IllegalArgumentException("Please select a file with transporters annotation!");
			}
		}
	}

}
