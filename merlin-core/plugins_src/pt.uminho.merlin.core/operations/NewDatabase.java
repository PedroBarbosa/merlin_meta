package operations;

import pt.uminho.sysbio.common.database.connector.datatypes.MySQL_Schemas;
import pt.uminho.sysbio.common.utilities.io.FileUtils;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;

@Operation(description="Creat new empty database.", name="New Database.")
public class NewDatabase {
	private String database;
	private String username;
	private String password;
	private String host;
	private String port;

	@Port(name="Database Name:",description="Set database name.",direction=Direction.INPUT,order=1)
	public void setDatabase(String database){
		this.database = database;
	}

	@Port(name="User Name:", description="Set the user name.",defaultValue="root",direction=Direction.INPUT,order=2)
	public void setUserName(String username){
		this.username = username;
	}

	@Port(name="Password:",description="Set the password.",defaultValue="password",direction=Direction.INPUT,order=3)
	public void setPassword(String password){
		this.password = password;
	}

	@Port(name="Host:",description="Set the host name.",defaultValue="localhost",direction=Direction.INPUT,order=4)
	public void setHostName(String host){
		this.host = host;
	}

	@Port(name="Port:",description="Set the host port.",defaultValue="3306",direction=Direction.INPUT,order=5)
	public void setHostPort(String port){
		this.port = port;

		MySQL_Schemas schemas = new MySQL_Schemas( this.username, this.password, this.host,this.port);
		String[] filePath=new String[6];
		String path = FileUtils.getCurrentLibDirectory()+"/../utilities/";

		filePath[0]=path +"sysbio_KEGG.sql";
		filePath[1]=path +"sysbio_blast.sql";
		filePath[2]=path +"sysbio_metabolites_transporters.sql";
		filePath[3]=path +"sysbio_sw_tcdb.sql";
		filePath[4]=path +"sysbio_compartments.sql";
		filePath[5]=path +"sysbio_metabolites_backup.sql";
		
		if(schemas.newSchemaAndScript(this.database, filePath))
		{
			Workbench.getInstance().info("Database "+this.database+" successfuly created.");
		}
		else
		{
			Workbench.getInstance().error("There was an error when trying to create "+this.database+"!!");
		}
	}
}
