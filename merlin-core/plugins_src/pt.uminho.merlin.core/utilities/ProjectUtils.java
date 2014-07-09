package utilities;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.biojava3.core.sequence.ProteinSequence;

import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.NcbiEFetchSequenceStub_API;
import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import datatypes.Project;

public class ProjectUtils {

	/**
	 * 
	 */
	public ProjectUtils() {

	}

	/**
	 * @param project
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static int getProjectID(Project project) throws IOException, SQLException {

		int project_id = -1;

		if(project.getGenomeCodeName()==null)
			throw new  IOException("GenomeID not set");

		long genomeID = project.getTaxonomyID();

		Connection conn = new Connection(project.getDatabase().getMySqlCredentials());
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT id FROM projects WHERE organism_id = "+genomeID+" AND latest_version");

		if(!rs.next()) {

			long time = System.currentTimeMillis();
			Timestamp timestamp = new Timestamp(time);
			stmt.execute("INSERT INTO projects (organism_id, date, latest_version, version) VALUES("+genomeID+",true,'"+timestamp+"',1)");
			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
		}

		project_id = rs.getInt(1);
		rs.close();
		stmt.close();
		conn.closeConnection();

		return project_id;

	}

	/**
	 * @param genome
	 * @return
	 * @throws Exception 
	 */
	public static String getGenomeID(Map<String, ProteinSequence> genome) throws Exception {

		if(genome.keySet().size()>0) {

			List<String> list = new ArrayList<>(genome.keySet());
			String geneID = list.get(0);

			NcbiEFetchSequenceStub_API stub = new NcbiEFetchSequenceStub_API(1);
			String res = stub.getTaxonomy(geneID);

			return res;
		}
		return null;

	}
	
	/**
	 * @param connection
	 * @return
	 */
	public static boolean isCompartmentalisedModel(Connection connection) {
		boolean ret = false;
		
		try  {
			
			Statement stmt = connection.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM reaction WHERE NOT originalReaction;");

			if(rs.next()) {
				
				ret=true;
			}
			
			stmt.close();
		} 
		catch (SQLException e) {e.printStackTrace();}
		return ret;
	}

	/**
	 * @param dsa
	 * @return
	 */
	public static boolean isSW_TransportersSearch(Connection connection) {
		boolean ret = false;
		
		try  {
			
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM sw_similarities;");
			
			if(rs.next()) {
				
				ret=true;
			}
			stmt.close();
		} 
		catch (SQLException e) {e.printStackTrace();}
		return ret;
	}
	
	/**
	 * @param dsa
	 * @return
	 */
	public static boolean isDatabaseGenesDataLoaded(Connection connection) {
		boolean ret = false;
		
		try  {
			
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM gene;");
			
			if(rs.next()) {
				
				ret=true;
			}
			stmt.close();
		} 
		catch (SQLException e) {e.printStackTrace();}
		return ret;
	}
	
	/**
	 * @param dsa
	 * @return
	 */
	public static boolean isMetabolicDataLoaded(Connection connection) {
		boolean ret = false;
		
		try  {
			
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM compound;");
			
			if(rs.next()) {
				
				ret=true;
			}
			stmt.close();
		} 
		catch (SQLException e) {e.printStackTrace();}
		return ret;
	}
	
	/**
	 * @param dsa
	 * @return
	 */
	public static boolean findComparmtents(Connection connection) {
		boolean ret = false;
	
		try  {
			
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM psort_reports;");
		
			if(rs.next()) {
				
				ret=true;
			}
			stmt.close();
		} 
		catch (SQLException e) {e.printStackTrace();}
		return ret;
	}
	
	/**
	 * @param connection
	 * @return
	 */
	public static boolean isTransporterLoaded(Connection connection) {
		boolean ret = false;
	
		try  {
			
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM pathway WHERE code='T0001';");
		
			if(rs.next()) {
				
				ret=true;
			}
			stmt.close();
		} 
		catch (SQLException e) {e.printStackTrace();}
		return ret;
	}
	
}
