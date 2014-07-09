package operations;

import java.util.List;

import pt.uminho.sysbio.common.database.connector.datatypes.MySQLMultiThread;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQL_Schemas;
import datatypes.Project;
import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;

@Operation(description="Drop database.", name="Drop Database.")
public class DropDatabase{

	private String host;
	private String password;
	private String username;
	private String database;
	private String port;

	/**
	 * @param project
	 */
	@Port(name="Project",description="Select Project",direction=Direction.INPUT,order=1)
	public void setProject(Project project) {
		
		this.host = project.getDatabase().getMySqlCredentials().get_database_host();
		this.password = project.getDatabase().getMySqlCredentials().get_database_password();
		this.username = project.getDatabase().getMySqlCredentials().get_database_user();
		this.port = project.getDatabase().getMySqlCredentials().get_database_port();
		this.database = project.getDatabase().getMySqlCredentials().get_database_name();
	
		MySQL_Schemas schemas = new MySQL_Schemas( this.username, this.password, this.host,this.port);
		
		if(schemas.dropDatabase(this.database)) {
			
			Workbench.getInstance().info("Database "+this.database+" successfuly droped.");
			
			List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);
			for (ClipboardItem item : cl){
				MySQLMultiThread link2 = ((Project)item.getUserData()).getDatabase().getMySqlCredentials();
				if(this.database.equals(link2.get_database_name()) && this.host.equals(link2.get_database_host()))
				{
					Core.getInstance().getClipboard().removeClipboardItem(item);
				}
			}
		}
		else
		{
			Workbench.getInstance().error("There was an error when trying to drop "+this.database+"!!");
		}
		
	}
}
