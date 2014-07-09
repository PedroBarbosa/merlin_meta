/**
 * 
 */
package operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQL_Utilities;
import pt.uminho.sysbio.common.utilities.io.FileUtils;
import datatypes.Project;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;

/**
 * @author ODias
 *
 */
@Operation(name="Correct Reactions Reversibility", description="Correct Reactions Reversibility using Zeng databases. More info in merlin-sysbio.org")
public class CorrectReversibility {

	private Project project;

	@Port(name="Select Project",description="Select Project", validateMethod="checkProject", direction = Direction.INPUT, order=1)
	public void setProject(Project project){

		try {
			
			Connection connection = new Connection(this.project.getDatabase().getMySqlCredentials());

			Statement stmt = connection.createStatement();
			

			File file = new File(FileUtils.getCurrentLibDirectory()+"/../utilities/irr_reactions.txt");

			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

			String text;
			while ((text = bufferedReader.readLine()) != null) {

				String[] data = text.split("\t");

				ResultSet rs = stmt.executeQuery("SELECT * FROM reaction " +
						"WHERE name ='"+MySQL_Utilities.mysqlStrConverter(data[0])+"' " +
								" OR name LIKE '"+MySQL_Utilities.mysqlStrConverter(data[0])+"%_%' "); 
				
				Map<String, String> reactions = new HashMap<String,String>();
				
				while (rs.next()) { 
					
					reactions .put(rs.getString("idreaction"), rs.getString("equation"));
				}
				
				for (String id : reactions.keySet()) {
					
					String equation = reactions.get(id).replace("<=>", "=>");
					
					boolean rever = !MySQL_Utilities.get_boolean_int_to_boolean(MySQL_Utilities.mysqlStrConverter(data[1]));
					
					
					if(rever) {
						
						equation = reactions.get(id).replace(" => ", " <=> ");
					}
					else {
						
						equation = reactions.get(id).replace(" <=> ", " => ");
					}
					
					stmt.execute("UPDATE reaction SET reversible = "+rever+", equation= '"+MySQL_Utilities.mysqlStrConverter(equation)+"', notes='RRC' " +
							" WHERE idreaction ="+id);
				}
			}

			bufferedReader.close();
			
			Workbench.getInstance().info("Reactions reversibility corrected!");

		} catch (Exception e) {
			
			throw new IllegalArgumentException(e);
		//	e.printStackTrace();
		} 
//		catch (FileNotFoundException e) {
//			
//		//	e.printStackTrace();
//		}
//		catch (IOException e) {
//			
//		//	e.printStackTrace();
//		}
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
			
			if(!this.project.isMetabolicDataAvailable()) {
			
				throw new IllegalArgumentException("Please load KEGG Data!");
			}
		}
	}
}
