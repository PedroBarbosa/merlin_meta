package operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

import pt.uminho.sysbio.common.database.connector.datatypes.MySQLMultiThread;
import datatypes.MySQLProcess;
import datatypes.Project;
import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;

@Operation(name="Load project", description="Load existing project")
public class LoadProject {

	private Project project;

	@Port(name="File", direction=Direction.BOTH,validateMethod="validateFile", description="Select File", order=1)
	public Project load(File file) {
		
		return this.project;
	}

	/**
	 * @param file
	 */
	public void validateFile(File file) {
		
		if(file.isDirectory()) {
			
			throw new IllegalArgumentException("Please select a project file");
		}
		
		Project res=null;
		
		try {
			
			FileInputStream fi = new FileInputStream(file);
			ObjectInputStream oi = new ObjectInputStream(fi);

			res = (Project) oi.readObject();

			oi.close();
			fi.close();

			String os_name = System.getProperty("os.name");
			
			if(os_name.contains("Windows")) {
				
				res.setOldPID(MySQLProcess.listMySQLProcess());

				res.setMysqlPID(
						MySQLProcess.starMySQLProcess(
								res.getDatabase().getMySqlCredentials().get_database_user(),
								res.getDatabase().getMySqlCredentials().get_database_password(),
								res.getDatabase().getMySqlCredentials().get_database_host(),
								res.getDatabase().getMySqlCredentials().get_database_port()));
			}
			res.getDatabase().getMySqlCredentials().openConnection();

			MySQLMultiThread dsa1 = new MySQLMultiThread(res.getDatabase().getMySqlCredentials().get_database_user(), res.getDatabase().getMySqlCredentials().get_database_password(), res.getDatabase().getMySqlCredentials().get_database_host(), res.getDatabase().getMySqlCredentials().get_database_port(),
					res.getDatabase().getMySqlCredentials().get_database_name());

			List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);

			for (ClipboardItem item : cl) 
			{
				MySQLMultiThread dsa = ((Project)item.getUserData()).getDatabase().getMySqlCredentials();

				if(dsa1.get_database_name().equals(dsa.get_database_name()) && dsa1.get_database_host().equals(dsa.get_database_host()))
					throw new IllegalArgumentException("Project connected to the same data base already exists");

			}

			this.project = res;
		}
		catch (Exception e) {
			
			e.printStackTrace();
			
			if(e.getCause().toString().toLowerCase().contains("Unknown database".toLowerCase())) {
				
				throw new IllegalArgumentException("The database used in this project " +
						"("+e.getCause().toString().replace("com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException: Unknown database ", "").trim()+")" +
						" was not found in this MySQL server!");
			} 
			else {
				
				e.printStackTrace();
				throw new IllegalArgumentException("Project file corrupt!");
			}
		}
	}
}
