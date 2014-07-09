package operations;

import java.io.File;
import java.io.IOException;



import com.thoughtworks.xstream.io.path.Path;

import datatypes.Project;
import es.uvigo.ei.aibench.Paths;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;

/**
 * @author pedro
 *
 */
@Operation(description="Create a '.sql' backup file for the selected project database", name="Create backup file")
public class Create_Database_Backup {


	private String password;
	private String username;
	private String database;
	private String commandline ="";
	private File backup;


	/**
	 * @param project
	 * @throws InterruptedException 
	 */
	@Port(name="Backup path", description="Select the path to save the backup", direction=Direction.INPUT,order=1)
	public void setPath(File path) {
		this.backup = path;
	}
	@Port(name="Project",description="Select Project",direction=Direction.INPUT,order=2)
	public void setProject(Project project) throws InterruptedException {

		this.password = project.getDatabase().getMySqlCredentials().get_database_password();
		this.username = project.getDatabase().getMySqlCredentials().get_database_user();
		this.database = project.getDatabase().getMySqlCredentials().get_database_name();
		try 
		{
	
				File backupdb = new File(this.backup.getAbsolutePath().concat("/"+this.database+".sql"));
				boolean check = backupdb.exists();
				if (check){
					Workbench.getInstance().info("A backup of this database in this directory already exists");

				}
				else{
					this.backup = backupdb;
					this.commandline += "mysqldump -n -u " + this.username+ " -p" + this.password + " " + this.database + " -r " + this.backup;
					System.out.println(this.commandline);


					Process runtimeProcess = Runtime.getRuntime().exec(this.commandline);

					int processComplete = runtimeProcess.waitFor();

					if (processComplete == 0) {
						Workbench.getInstance().info("Backup of "+this.database+" completed");
					} else {
						Workbench.getInstance().error("Backup of " + this.database + "Failed. Please select a directory instead of a file");
					}

				}

			}
		
		catch (IOException ex) {
			Workbench.getInstance().error("Error at Backup" + ex.getMessage());
		}

	}


}


