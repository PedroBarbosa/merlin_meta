package datatypes;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.transporters.core.compartments.GeneCompartments;
import pt.uminho.sysbio.common.transporters.core.compartments.PSort3_result;
import pt.uminho.sysbio.common.transporters.core.transport.reactions.containerAssembly.TransportContainer;
import utilities.ProjectUtils;
import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

@Datatype(structure = Structure.COMPLEX,namingMethod="getName",removable=true, removeMethod ="remove")
public class Project extends Observable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Database database;
	private String name;
	private String fileName;
	private String proxy_host = "";
	private String proxy_port = "";
	private String proxy_username = "";
	private String proxy_password = "";
	private String mysqlPID="";
	private Map <String,String> oldPID;
	private String genomeCodeName;
	private boolean useProxy;
	private boolean useAuthentication;
	private boolean isNCBIGenome;
	private static int counter=0;
	private TransportContainer transportContainer;
	private boolean sw_TransportersSearch;
	private Map<String, PSort3_result> psSort3_results;
	private boolean transporterLoaded;
	private boolean initialiseHomologyData;
	private Map<String, GeneCompartments> geneCompartments;
	//private boolean compartmentsLoaded;
	//private boolean metabolicDataAvailable;
	//private boolean geneDataAvailable;
	//private boolean compartmentalisedModel;
	private long taxonomyID;
	private boolean fnaFiles, faaFiles;
	private int projectID;
	private String organismName;
	private String organismLineage;
	private boolean isMetagenomicProject;

	/**
	 * @param database
	 * @param name
	 */
	public Project(Database database, String name) {

		this.database = database;
		this.name = name;
		this.setPsort3Results(null);
		this.fileName = "";
		this.setTransportContainer(null);
		this.taxonomyID = -1;
	}

	@Clipboard(name="Database")
	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database db) {
		this.database = db;
	}

	public String getName() {
		return name;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the taxonomyID
	 */
	public long getTaxonomyID() {
		return taxonomyID;
	}

	/**
	 * @param taxonomyID the taxonomyID to set
	 */
	public void setTaxonomyID(long taxonomyID) {

		this.taxonomyID = taxonomyID;
	}

	/**
	 * @return
	 */
	public static int getCounter() {
		return counter;
	}

	/**
	 * @param counter
	 */
	public static void setCounter(int counter) {
		Project.counter = counter;
	}

	public void remove(){

		List<ClipboardItem> items = Core.getInstance().getClipboard().getItemsByClass(Project.class);
		ClipboardItem torem = null;
		for(ClipboardItem item : items){
			if(item.getUserData().equals(this)){
				torem = item;
				break;
			}
		}
		Core.getInstance().getClipboard().removeClipboardItem(torem);
		System.gc();
	}

	/**
	 * @return the proxy_host
	 */
	public String getProxy_host() {

		return proxy_host;
	}

	/**
	 * @param proxy_host the proxy_host to set
	 */
	public void setProxy_host(String proxy_host) {
		this.proxy_host = proxy_host;
	}

	/**
	 * @return the proxy_port
	 */
	public String getProxy_port() {
		return proxy_port;
	}

	/**
	 * @param proxy_port the proxy_port to set
	 */
	public void setProxy_port(String proxy_port) {
		this.proxy_port = proxy_port;
	}

	/**
	 * @return the proxy_username
	 */
	public String getProxy_username() {
		return proxy_username;
	}

	/**
	 * @param proxy_username the proxy_username to set
	 */
	public void setProxy_username(String proxy_username) {
		this.proxy_username = proxy_username;
	}

	/**
	 * @return the proxy_password
	 */
	public String getProxy_password() {
		return proxy_password;
	}

	/**
	 * @param proxy_password the proxy_password to set
	 */
	public void setProxy_password(String proxy_password) {
		this.proxy_password = proxy_password;
	}

	/**
	 * @param useProxy the useProxy to set
	 */
	public void setUseProxy(boolean useProxy) {
		this.useProxy = useProxy;
	}

	/**
	 * @return the useProxy
	 */
	public boolean isUseProxy() {
		return useProxy;
	}

	/**
	 * @param useAuthentication the useAuthentication to set
	 */
	public void setUseAuthentication(boolean useAuthentication) {
		this.useAuthentication = useAuthentication;
	}

	/**
	 * @return the useAuthentication
	 */
	public boolean isUseAuthentication() {
		return useAuthentication;
	}

	/**
	 * @param mysqlPID
	 */
	public void setMysqlPID(String mysqlPID) {
		this.mysqlPID = mysqlPID;
	}

	/**
	 * @return
	 */
	public String getMysqlPID() {
		return mysqlPID;
	}

	/**
	 * @param oldPID the oldPID to set
	 */
	public void setOldPID(Map <String,String> oldPID) {
		this.oldPID = oldPID;
	}

	/**
	 * @return the oldPID
	 */
	public Map <String,String> getOldPID() {
		return oldPID;
	}

	/**
	 * @return the genomeID
	 */
	public String getGenomeCodeName() {
		return genomeCodeName;
	}

	/**
	 * @param genomeID the genomeID to set
	 */
	public void setGenomeCodeName(String genomeID) {
		this.genomeCodeName = genomeID;
	}

	/**
	 * @return the isNCBIGenome
	 */
	public boolean isNCBIGenome() {
		return isNCBIGenome;
	}

	/**
	 * @param isNCBIGenome the isNCBIGenome to set
	 */
	public void setNCBIGenome(boolean isNCBIGenome) {
		this.isNCBIGenome = isNCBIGenome;
	}

	/**
	 * @return the transportContainer
	 */
	public TransportContainer getTransportContainer() {
		return transportContainer;
	}

	/**
	 * @param transportContainer the transportContainer to set
	 */
	public void setTransportContainer(TransportContainer transportContainer) {
		this.transportContainer = transportContainer;
	}

	/**
	 * @return the sw_TransportersSearch
	 */
	public boolean isSW_TransportersSearch() {
		return sw_TransportersSearch;
	}

	/**
	 * @param sw_TransportersSearch the sw_TransportersSearch to set
	 */
	public void setSW_TransportersSearch(boolean sw_TransportersSearch) {
		this.sw_TransportersSearch = sw_TransportersSearch;
	}

	/**
	 * @return
	 */
	public Map<String, PSort3_result> getPsort3Results() {
		return this.psSort3_results;
	}

	/**
	 * @param results the psort3ReportsDirectory to set
	 */
	public void setPsort3Results(Map<String, PSort3_result> psSort3_results) {

		this.psSort3_results = psSort3_results;
	}

	/**
	 * @return
	 */
	public boolean isTransporterLoaded() {
		return transporterLoaded;
	}

	/**
	 * @param transporterLoaded
	 */
	public void setTransporterLoaded(boolean transporterLoaded) {
		this.transporterLoaded = transporterLoaded;

	}

	/**
	 * @return
	 */
	public boolean isInitialiseHomologyData() {
		return initialiseHomologyData;
	}

	/**
	 * @param initialiseHomologyData
	 */
	public void setInitialiseHomologyData(boolean initialiseHomologyData) {
		this.initialiseHomologyData = initialiseHomologyData;
	}

	/**
	 * @return
	 */
	public Map<String, GeneCompartments> getGeneCompartments() {
		return this.geneCompartments;
	}

	/**
	 * @param geneCompartments
	 */
	public void setGeneCompartmens(Map<String, GeneCompartments> geneCompartments) {
		this.geneCompartments = geneCompartments;

	}

	//	/**
	//	 * @param compartmentsPredicted
	//	 * @throws SQLException 
	//	 */
	//	public void setCompartmentsLoaded(boolean compartmentsPredicted) {
	//
	//		this.compartmentsLoaded = compartmentsPredicted;
	//	}

	/**
	 * @return
	 */
	public boolean isCompartmentsLoaded () {

		//if(!this.compartmentsLoaded) {
		boolean compartmentsLoaded = false;
		Connection connection;
		try {

			connection = new Connection(this.database.getMySqlCredentials());
			compartmentsLoaded = ProjectUtils.findComparmtents(connection);
			connection.closeConnection();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		//}

		return compartmentsLoaded;
	}

	//	public void setGeneDataAvailable(boolean geneDataAvailable) {
	//		this.geneDataAvailable = geneDataAvailable;		
	//	}

	/**
	 * @return
	 */
	public boolean isGeneDataAvailable() {

		//if(!this.geneDataAvailable) {

		boolean geneDataAvailable = false;

		Connection connection;

		try {

			connection = new Connection(this.database.getMySqlCredentials());
			geneDataAvailable = ProjectUtils.isDatabaseGenesDataLoaded(connection);
			connection.closeConnection();
			
		}
		catch (SQLException e) {

			e.printStackTrace();
		}
		//}
		return geneDataAvailable;
	}

	//	/**
	//	 * @param metabolicDataAvailable
	//	 */
	//	public void setMetabolicDataAvailable(boolean metabolicDataAvailable) {
	//		
	//		this.metabolicDataAvailable = metabolicDataAvailable;
	//	}

	/**
	 * @return
	 */
	public boolean isMetabolicDataAvailable() {

		//if(!this.metabolicDataAvailable) {
		boolean metabolicDataAvailable = false;
		Connection connection;
		try {

			connection = new Connection(this.database.getMySqlCredentials());
			metabolicDataAvailable = ProjectUtils.isMetabolicDataLoaded(connection);
			connection.closeConnection();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		//}

		return metabolicDataAvailable;
	}

	//	/**
	//	 * @param metabolicDataAvailable
	//	 */
	//	public void setCompartmentalisedModel(boolean compartmentalisedModel) {
	//		this.compartmentalisedModel = compartmentalisedModel;
	//	}

	/**
	 * @return
	 */
	public boolean isCompartmentalisedModel() {

		//if(!this.compartmentalisedModel) {

		boolean compartmentalisedModel = false;
		Connection connection;
		try {

			connection = new Connection(this.database.getMySqlCredentials());
			compartmentalisedModel = ProjectUtils.isCompartmentalisedModel(connection);
			connection.closeConnection();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		//}
		return compartmentalisedModel;
	}

	public boolean isFnaFiles() {
		return fnaFiles;
	}

	public void setFnaFiles(boolean fnaFiles) {
		this.fnaFiles = fnaFiles;
	}

	public boolean isFaaFiles() {
		return faaFiles;
	}

	public void setFaaFiles(boolean faaFiles) {
		this.faaFiles = faaFiles;
	}

	/**
	 * @return the project_id
	 */
	public int getProjectID() {

		return projectID;
	}

	/**
	 * @param project_id the project_id to set
	 */
	public void setProjectID(int projectID) {

		this.projectID = projectID;
	}

	/**
	 * @return the organismName
	 */
	public String getOrganismName() {
		return organismName;
	}

	/**
	 * @param organismName the organismName to set
	 */
	public void setOrganismName(String organismName) {
		this.organismName = organismName;
	}

	/**
	 * @return the organismLineage
	 */
	public String getOrganismLineage() {
		return organismLineage;
	}

	/**
	 * @param organismLineage the organismLineage to set
	 */
	public void setOrganismLineage(String organismLineage) {
		this.organismLineage = organismLineage;
	}
	
	/**
	 * @return the isMetagenomicProject
	 */
	public boolean isMetagenomicProject() {
		return isMetagenomicProject;
	}

	/**
	 * @param isMetagenomicProject the isMetagenomicProject to set
	 */
	public void setMetagenomicProject(boolean isMetagenomicProject) {
		this.isMetagenomicProject = isMetagenomicProject;
	}

}
