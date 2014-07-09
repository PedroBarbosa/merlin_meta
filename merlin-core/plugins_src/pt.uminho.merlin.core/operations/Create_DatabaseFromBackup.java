/**
 * 
 */
package operations;

import java.io.File;
import java.io.IOException;

import pt.uminho.sysbio.common.database.connector.datatypes.MySQL_Schemas;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;

/**
 * @author pedro
 *
 */

@Operation(description="Create a new database from a '.sql' Merlin database backup file.Useful when importing databases from other computers", name="New database from SQL file")
public class Create_DatabaseFromBackup {
	

	private String database;
	private String username;
	private String password;
	private String host;
	private String port;
	private File sqlfile;

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
	}
	
	@Port(name="Path for SQL backup file:",validateMethod ="validateSQLfile",description="Set the file.",defaultValue="",direction=Direction.INPUT,order=6)
	public void setSQLfile(File file) throws IOException, InterruptedException{
	
		
		MySQL_Schemas schemas = new MySQL_Schemas( this.username, this.password, this.host,this.port);

		this.sqlfile = file;
		if(schemas.newSchemaAndScriptFromBackup(this.database, this.sqlfile))
		{
			Workbench.getInstance().info("Database "+this.database+" successfuly created.");
		}
		else
		{
			Workbench.getInstance().error("There was an error when trying to create "+this.database+"!!");
		}
//		this.command7line+= "mysql", "-u " + this.username+ " -p" + this.password + " --execute='CREATE DATABASE " + this.database + "'";
//		String[] cmd = {"mysql", "-u", this.username, "-p" + this.password};
//		
//		
//		System.out.println(Arrays.toString(cmd));
//		Process runtimeProcess = Runtime.getRuntime().exec(cmd);
//		
//		BufferedWriter bw = new BufferedWriter( new OutputStreamWriter(runtimeProcess.getOutputStream()));
//		bw.write("CREATE DATABASE " + this.database + ";\n");
//		//BufferedReader br = new BufferedReader( new InputStreamReader( runtimeProcess.getInputStream()));
//		bw.write("exit\n");
//		bw.flush();
//		bw.close();
//
//		
//		
//		int processComplete = runtimeProcess.waitFor();
//		if (!(processComplete == 0)){
//			Workbench.getInstance().error("Error creating " + this.database + " database. Please check if this database name already exists");
//		}
//		
//		else{
			//String[] cmd2 = {"mysql", "-u", this.username, "-p" + this.password, "<", "~/Desktop/HMP_MERGE.sql"};
			//Process runtimeProcess2 = Runtime.getRuntime().exec(cmd);
			
//			String[] executeCmd = new String[]{"mysql", "--user=" + this.username, "--password=" + this.password, "-e", "source " + this.sqlfile};
//			bw = new BufferedWriter(new OutputStreamWriter(runtimeProcess2.getOutputStream()));
//			bw.write("USE " + this.database + ";\n");
//			bw.write("SET foreign_key_checks = 0;");
//			BufferedReader br = new BufferedReader(new FileReader(this.sqlfile));
//			String line;
//			while ( (line = br.readLine()) != null) {
//				bw.write(line);
//				bw.flush();
//			}
//			bw.write("exit\n");
//			bw.flush();
//			bw.close();
//			br.close();
//			runtimeProcess = Runtime.getRuntime().exec(executeCmd);
//			processComplete = runtimeProcess.waitFor();
//			if (processComplete == 0) {
//				Workbench.getInstance().info("Creation of "+this.database+" completed");
//			} else {
//				Workbench.getInstance().error("something is wrong");
//			}
//		}

	}

	
	
	public void validateSQLfile(File sqlfile){
		
		if(sqlfile == null || sqlfile.toString().isEmpty()) {

			throw new IllegalArgumentException("SQL file directory not set!");
		}
		else {

			if(sqlfile.getName().endsWith(".sql")) {
				this.sqlfile = sqlfile;				
			}
			else {
				throw new IllegalArgumentException("Please Select a directory with a .sql file!");				
			}
		}
	}
}