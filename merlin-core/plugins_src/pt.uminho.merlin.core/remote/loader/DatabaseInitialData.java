/**
 * 
 */
package remote.loader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;

/**
 * @author ODias
 *
 */
public class DatabaseInitialData {
	private Connection connection;
	private int compartmentID;
	private ConcurrentHashMap<String,Integer> genes_id, metabolites_id, chromosome_id, proteins_id, reactions_id,
	pathways_id, modules_id,orthologues_id, similar_metabolites_to_load;
	private ConcurrentHashMap<Integer,Set<String>> reactionsPathway, metabolitesPathway, modulesPathway;
	private ConcurrentHashMap<String,Set<String>> enzymesPathway;
	private ConcurrentLinkedQueue<Integer> reactionsPathwayList, metabolitesPathwayList, modulesPathwayList;
	private ConcurrentLinkedQueue<String> enzymesPathwayList;
	private ConcurrentLinkedQueue<String> enzymesInModel;

	/**
	 * @param connection
	 * @throws SQLException
	 */
	public DatabaseInitialData(Connection connection) throws SQLException{
		
		this.similar_metabolites_to_load=new ConcurrentHashMap<String, Integer>();
		this.connection=connection;
		
		//this.setCompartmentID(this.getCompartmentID("Cytosol"));

		Statement stmt = this.connection.createStatement();

		this.chromosome_id=new ConcurrentHashMap<String, Integer>();
		ResultSet rs = stmt.executeQuery("SELECT name,idchromosome FROM chromosome");
		while(rs.next())
		{
			this.chromosome_id.put(rs.getString(1), rs.getInt(2));
		}

		this.genes_id=new ConcurrentHashMap<String, Integer>();
		rs = stmt.executeQuery("SELECT locusTag, idgene FROM gene");
		while(rs.next())
		{
			this.genes_id.put(rs.getString(1), rs.getInt(2));
		}

		this.proteins_id=new ConcurrentHashMap<String, Integer>();
		rs = stmt.executeQuery("SELECT ecnumber, protein_idprotein FROM enzyme");
		while(rs.next())
		{
			this.proteins_id.put(rs.getString(1), rs.getInt(2));
		}
		
		this.pathways_id=new ConcurrentHashMap<String, Integer>();
		rs = stmt.executeQuery("SELECT code, idpathway FROM pathway");
		while(rs.next())
		{
			this.pathways_id.put(rs.getString(1), rs.getInt(2));
		}
		
		this.modules_id=new ConcurrentHashMap<String, Integer>();
		rs = stmt.executeQuery("SELECT entry_id, id FROM module");
		while(rs.next())
		{
			this.modules_id.put(rs.getString(1), rs.getInt(2));
		}
		
		this.metabolites_id=new ConcurrentHashMap<String, Integer>();
		rs = stmt.executeQuery("SELECT kegg_id, idcompound FROM compound");
		while(rs.next())
		{
			this.metabolites_id.put(rs.getString(1), rs.getInt(2));
		}
		
		this.reactions_id=new ConcurrentHashMap<String, Integer>();
		rs = stmt.executeQuery("SELECT name, idreaction FROM reaction");
		while(rs.next())
		{
			this.reactions_id.put(rs.getString(1), rs.getInt(2));
		}
		
		this.orthologues_id=new ConcurrentHashMap<String, Integer>();
		rs = stmt.executeQuery("SELECT entry_id, id FROM orthology");
		while(rs.next())
		{
			this.orthologues_id.put(rs.getString(1), rs.getInt(2));
		}
		
		this.reactionsPathway=new ConcurrentHashMap<Integer,Set<String>>();
		this.enzymesPathway=new ConcurrentHashMap<String,Set<String>>();
		this.metabolitesPathway=new ConcurrentHashMap<Integer,Set<String>>();
		this.modulesPathway=new ConcurrentHashMap<Integer,Set<String>>();
		this.setEnzymesInModel(new ConcurrentLinkedQueue<String>());
		this.reactionsPathwayList=new ConcurrentLinkedQueue<Integer>();
		this.enzymesPathwayList=new ConcurrentLinkedQueue<String>();
		this.metabolitesPathwayList=new ConcurrentLinkedQueue<Integer>();
		this.modulesPathwayList=new ConcurrentLinkedQueue<Integer>();

		stmt.close();
	}
	
//	/**
//	 * @param compartment
//	 * @throws SQLException
//	 */
//	public int getCompartmentID(String compartment) throws SQLException{
//
//		Statement stmt = this.connection.createStatement();
//
//		String abbreviation;
//		if(compartment.length()>3)
//		{
//			abbreviation=compartment.substring(0,3).toUpperCase();
//		}
//		else
//		{
//			abbreviation=compartment.toUpperCase().concat("_");
//			while(abbreviation.length()<4)
//			{
//				abbreviation=abbreviation.concat("_");
//			}
//		}
//
//		ResultSet rs = stmt.executeQuery("SELECT idcompartment FROM compartment WHERE name ='"+compartment+"' AND abbreviation ='"+abbreviation+"'");
//		if(!rs.next())
//		{
//			stmt.execute("INSERT INTO compartment(name, abbreviation) VALUES('"+compartment+"','"+abbreviation+"')");
//			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
//			rs.next();
//		}
//		int idCompartment =rs.getInt(1);
//
//		return idCompartment;
//	}

	/**
	 * @param compartmentID the compartmentID to set
	 */
	public void setCompartmentID(int compartmentID) {
		this.compartmentID = compartmentID;
	}

	/**
	 * @return the compartmentID
	 */
	public int getCompartmentID() {
		return compartmentID;
	}

	/**
	 * @return the genes_id
	 */
	public ConcurrentHashMap<String, Integer> getGenes_id() {
		return genes_id;
	}

	/**
	 * @param genesId the genes_id to set
	 */
	public void setGenes_id(ConcurrentHashMap<String, Integer> genesId) {
		genes_id = genesId;
	}

	/**
	 * @return the metabolites_id
	 */
	public ConcurrentHashMap<String, Integer> getMetabolites_id() {
		return metabolites_id;
	}

	/**
	 * @param metabolitesId the metabolites_id to set
	 */
	public void setMetabolites_id(ConcurrentHashMap<String, Integer> metabolitesId) {
		metabolites_id = metabolitesId;
	}

	/**
	 * @return the chromosome_id
	 */
	public ConcurrentHashMap<String, Integer> getChromosome_id() {
		return chromosome_id;
	}

	/**
	 * @param chromosomeId the chromosome_id to set
	 */
	public void setChromosome_id(ConcurrentHashMap<String, Integer> chromosomeId) {
		chromosome_id = chromosomeId;
	}

	/**
	 * @return the proteins_id
	 */
	public ConcurrentHashMap<String, Integer> getProteins_id() {
		return proteins_id;
	}

	/**
	 * @param proteinsId the proteins_id to set
	 */
	public void setProteins_id(ConcurrentHashMap<String, Integer> proteinsId) {
		proteins_id = proteinsId;
	}

	/**
	 * @return the reactions_id
	 */
	public ConcurrentHashMap<String, Integer> getReactions_id() {
		return reactions_id;
	}

	/**
	 * @param reactionsId the reactions_id to set
	 */
	public void setReactions_id(ConcurrentHashMap<String, Integer> reactionsId) {
		reactions_id = reactionsId;
	}

	/**
	 * @return the pathways_id
	 */
	public ConcurrentHashMap<String, Integer> getPathways_id() {
		return pathways_id;
	}

	/**
	 * @param pathwaysId the pathways_id to set
	 */
	public void setPathways_id(ConcurrentHashMap<String, Integer> pathwaysId) {
		pathways_id = pathwaysId;
	}

	/**
	 * @return the modules_id
	 */
	public ConcurrentHashMap<String, Integer> getModules_id() {
		return modules_id;
	}

	/**
	 * @param modulesId the modules_id to set
	 */
	public void setModules_id(ConcurrentHashMap<String, Integer> modulesId) {
		modules_id = modulesId;
	}

	/**
	 * @return the orthologues_id
	 */
	public ConcurrentHashMap<String, Integer> getOrthologues_id() {
		return orthologues_id;
	}

	/**
	 * @param orthologuesId the orthologues_id to set
	 */
	public void setOrthologues_id(ConcurrentHashMap<String, Integer> orthologuesId) {
		orthologues_id = orthologuesId;
	}

	/**
	 * @return the reactionsPathway
	 */
	public ConcurrentHashMap<Integer, Set<String>> getReactionsPathway() {
		return reactionsPathway;
	}

	/**
	 * @param reactionsPathway the reactionsPathway to set
	 */
	public void setReactionsPathway(
			ConcurrentHashMap<Integer, Set<String>> reactionsPathway) {
		this.reactionsPathway = reactionsPathway;
	}

	/**
	 * @return the enzymesPathway
	 */
	public ConcurrentHashMap<String, Set<String>> getEnzymesPathway() {
		return enzymesPathway;
	}

	/**
	 * @param enzymesPathway the enzymesPathway to set
	 */
	public void setEnzymesPathway(
			ConcurrentHashMap<String, Set<String>> enzymesPathway) {
		this.enzymesPathway = enzymesPathway;
	}

	/**
	 * @return the metabolitesPathway
	 */
	public ConcurrentHashMap<Integer, Set<String>> getMetabolitesPathway() {
		return metabolitesPathway;
	}

	/**
	 * @param metabolitesPathway the metabolitesPathway to set
	 */
	public void setMetabolitesPathway(
			ConcurrentHashMap<Integer, Set<String>> metabolitesPathway) {
		this.metabolitesPathway = metabolitesPathway;
	}

	/**
	 * @return the modulesPathway
	 */
	public ConcurrentHashMap<Integer, Set<String>> getModulesPathway() {
		return modulesPathway;
	}

	/**
	 * @param modulesPathway the modulesPathway to set
	 */
	public void setModulesPathway(
			ConcurrentHashMap<Integer, Set<String>> modulesPathway) {
		this.modulesPathway = modulesPathway;
	}

	/**
	 * @return
	 */
	public ConcurrentHashMap<String,Integer> getSimilar_metabolites() {
			
		return this.similar_metabolites_to_load;
	}

	/**
	 * @param reactionsPathwayList the reactionsPathwayList to set
	 */
	public void setReactionsPathwayList(ConcurrentLinkedQueue<Integer> reactionsPathwayList) {
		this.reactionsPathwayList = reactionsPathwayList;
	}

	/**
	 * @return the reactionsPathwayList
	 */
	public ConcurrentLinkedQueue<Integer> getReactionsPathwayList() {
		return reactionsPathwayList;
	}

	/**
	 * @param enzymesPathwayList the enzymesPathwayList to set
	 */
	public void setEnzymesPathwayList(ConcurrentLinkedQueue<String> enzymesPathwayList) {
		this.enzymesPathwayList = enzymesPathwayList;
	}

	/**
	 * @return the enzymesPathwayList
	 */
	public ConcurrentLinkedQueue<String> getEnzymesPathwayList() {
		return enzymesPathwayList;
	}

	/**
	 * @param metabolitesPathwayList the metabolitesPathwayList to set
	 */
	public void setMetabolitesPathwayList(ConcurrentLinkedQueue<Integer> metabolitesPathwayList) {
		this.metabolitesPathwayList = metabolitesPathwayList;
	}

	/**
	 * @return the metabolitesPathwayList
	 */
	public ConcurrentLinkedQueue<Integer> getMetabolitesPathwayList() {
		return metabolitesPathwayList;
	}

	/**
	 * @param modulesPathwayList the modulesPathwayList to set
	 */
	public void setModulesPathwayList(ConcurrentLinkedQueue<Integer> modulesPathwayList) {
		this.modulesPathwayList = modulesPathwayList;
	}

	/**
	 * @return the modulesPathwayList
	 */
	public ConcurrentLinkedQueue<Integer> getModulesPathwayList() {
		return modulesPathwayList;
	}

	/**
	 * @return the enzymesInModel
	 */
	public ConcurrentLinkedQueue<String> getEnzymesInModel() {
		return enzymesInModel;
	}

	/**
	 * @param enzymesInModel the enzymesInModel to set
	 */
	public void setEnzymesInModel(ConcurrentLinkedQueue<String> enzymesInModel) {
		this.enzymesInModel = enzymesInModel;
	}
}
