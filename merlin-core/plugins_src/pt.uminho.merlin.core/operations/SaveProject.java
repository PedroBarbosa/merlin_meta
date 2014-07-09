package operations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import datatypes.Project;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;

@Operation(description="Save existing project", name="Save Project.")
public class SaveProject {

	private Project project;
	private File file;


	/**
	 * 
	 * @param project project to be saved.
	 */
	@Port(name="Project",direction=Direction.INPUT, validateMethod="checkProject",description="Save Project",order=1)
	public void saveProject(Project project) {
		
		this.project = project;
	}

	/**
	 * 
	 * @param file file in which to save the project.
	 * @throws IOException
	 */
	@Port(name="File", direction=Direction.INPUT, validateMethod="validateFile", description="Select File (or write file name ending in .mer)", order=2)
	public void save(File file) throws IOException {
		
		try {
			
			FileOutputStream fo = new FileOutputStream(this.file);
			ObjectOutputStream oo = new ObjectOutputStream(fo);
			this.project.setFileName(file.getAbsolutePath());
			oo.writeObject(this.project);

			oo.flush();
			fo.flush();
			oo.close();
			fo.close();
			Workbench.getInstance().info("Project saved successfully.");
		}
		catch (Exception e) {
			
			e.printStackTrace();
			Workbench.getInstance().error("There was some problem while saving your project! Please close and reopen all the tabs you are currently using before trying to save again.");
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
		}
	}
	
	/**
	 * @param file
	 */
	public void validateFile(File file) {

		if(file.isDirectory()) {
			
			System.out.println(file.getPath()+File.separator+this.project.getName()+".mer");
			this.file = new File(file.getPath()+File.separator+this.project.getName()+".mer");
			//throw new IllegalArgumentException("Please select a project File or enter the name for the new project File.");
		}

	}
}
