/**
 * 
 */
package remote.loader;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQLMultiThread;
import pt.uminho.sysbio.merlin.utilities.TimeLeftProgress;
import remote.retriever.Retrieve_kegg_data;
import remote.retriever.entry_types.Enzyme_entry;
import remote.retriever.entry_types.Gene_entry;
import remote.retriever.entry_types.Metabolite_entry;
import remote.retriever.entry_types.Module_entry;
import remote.retriever.entry_types.Pathways_hierarchy;
import remote.retriever.entry_types.Reaction_entry;

/**
 * @author ODias
 *
 */
public class Load_KEGG_Data implements Runnable{

	private ConcurrentLinkedQueue<Metabolite_entry> resultMetabolites;
	private ConcurrentLinkedQueue<Enzyme_entry> resultEnzymes;
	private ConcurrentLinkedQueue<Reaction_entry> resultReactions;
	private ConcurrentLinkedQueue<Gene_entry> resultGenes;
	private ConcurrentLinkedQueue<Module_entry> resultModules;
	private ConcurrentLinkedQueue<String[]> resultPathways;
	private ConcurrentLinkedQueue<Pathways_hierarchy> kegg_Pathways_Hierarchy;
	private ConcurrentLinkedQueue<String> enzymesPathwayList,orthologueEntities;
	private KEGG_loader kegg_loader;
	private ConcurrentLinkedQueue<Integer> reactionsPathwayList, metabolitesPathwayList, modulesPathwayList;
	private ConcurrentHashMap<String,Integer> pathways_id;
	private MySQLMultiThread msqlmt;
	private TimeLeftProgress progress;
	private AtomicBoolean cancel;
	private long startTime;
	private int dataSize;
	private AtomicInteger datum;
	private boolean error;

	/**
	 * @param msqlmt
	 * @param organismID
	 * @param retrieve_kegg_data
	 * @param databaseInitialData
	 * @param progress
	 * @throws RemoteException
	 * @throws ServiceException
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	public Load_KEGG_Data(MySQLMultiThread msqlmt, String organismID, Retrieve_kegg_data retrieve_kegg_data, DatabaseInitialData databaseInitialData, TimeLeftProgress progress) throws RemoteException, InterruptedException, SQLException {

		this.cancel = new AtomicBoolean(false);
		
		this.msqlmt = msqlmt;
		this.progress = progress;
		this.setResultMetabolites(retrieve_kegg_data.getResultMetabolites());
		this.dataSize = this.dataSize + new Integer(retrieve_kegg_data.getResultMetabolites().size());

		this.setResultEnzymes(retrieve_kegg_data.getResultEnzymes());
		this.dataSize = this.dataSize + new Integer(retrieve_kegg_data.getResultEnzymes().size());

		this.setResultReactions(retrieve_kegg_data.getResultReactions());
		this.dataSize = this.dataSize + new Integer(retrieve_kegg_data.getResultReactions().size());

		this.setResultGenes(retrieve_kegg_data.getResultGenes());
		this.dataSize = this.dataSize + new Integer(retrieve_kegg_data.getResultGenes().size());

		this.setResultModules(retrieve_kegg_data.getResultModules());
		this.dataSize = this.dataSize + new Integer(retrieve_kegg_data.getResultModules().size());

		this.setKegg_Pathways_Hierarchy(retrieve_kegg_data.getKegg_Pathways_Hierarchy());
		this.dataSize = this.dataSize + new Integer(retrieve_kegg_data.getKegg_Pathways_Hierarchy().size());

		this.setOrthologueEntities(retrieve_kegg_data.getOrthologueEntities());
		this.dataSize = this.dataSize + new Integer(retrieve_kegg_data.getOrthologueEntities().size());



		this.setPathways_id(databaseInitialData.getPathways_id());
		this.dataSize = this.dataSize + databaseInitialData.getPathways_id().size();

		this.setReactionsPathwayList(databaseInitialData.getReactionsPathwayList());
		this.dataSize = this.dataSize + databaseInitialData.getReactionsPathwayList().size();

		this.setEnzymesPathwayList(databaseInitialData.getEnzymesPathwayList());
		this.dataSize = this.dataSize + databaseInitialData.getEnzymesPathwayList().size();

		this.setMetabolitesPathwayList(databaseInitialData.getMetabolitesPathwayList());
		this.dataSize = this.dataSize + databaseInitialData.getMetabolitesPathwayList().size();

		this.setModulesPathwayList(databaseInitialData.getModulesPathwayList());
		this.dataSize = this.dataSize + databaseInitialData.getModulesPathwayList().size();


		this.kegg_loader = new KEGG_loader(databaseInitialData, retrieve_kegg_data.getCompoundsWithBiologicalRoles());
		this.dataSize = this.dataSize + retrieve_kegg_data.getCompoundsWithBiologicalRoles().size();

		this.startTime = System.currentTimeMillis();
		this.datum = new AtomicInteger();
		this.datum.set(0);

		this.setError(false);
	}

	@Override
	public void run() {

		try {

			if(!this.cancel.get()) {

				Connection conn = new Connection(this.msqlmt);
				this.kegg_loader.setConnection(conn);
				this.loadPathways();

				this.loadCompounds();
				this.loadGenes();
				this.loadEnzymes();
				this.loadModules();
				this.loadReactions();

				this.kegg_loader.setPathways_id(this.getPathways_id());

				this.loadModulesPathwayList();
				this.loadReactionsPathwayList();
				this.loadEnzymesPathwayList();
				this.loadMetabolitePathwayList();
			}
		}
		catch (RemoteException e) {e.printStackTrace(); this.setError(true);}
		catch (SQLException e) {e.printStackTrace(); this.setError(true);}
	}


	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadGenes() throws RemoteException, SQLException{

		while(!this.resultGenes.isEmpty()) {

			Gene_entry geneList;
			if((geneList = this.resultGenes.poll()) != null) {

				this.kegg_loader.loadGene(geneList);
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize);
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadCompounds() throws RemoteException, SQLException{

		while(!this.resultMetabolites.isEmpty() && !this.cancel.get()) {

			Metabolite_entry metaboliteList;
			if((metaboliteList = this.resultMetabolites.poll()) != null) {

				this.kegg_loader.loadMetabolite(metaboliteList);
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize);
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadEnzymes() throws RemoteException, SQLException{

		while(!this.resultEnzymes.isEmpty() && !this.cancel.get()) {

			Enzyme_entry enzymesList;
			if((enzymesList = this.resultEnzymes.poll()) != null) {

				this.kegg_loader.loadProtein(enzymesList);
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize);
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadReactions() throws RemoteException, SQLException{

		while(!this.resultReactions.isEmpty() && !this.cancel.get()) {

			Reaction_entry reactionList;
			if((reactionList =this.resultReactions.poll()) != null) {

				this.kegg_loader.loadReaction(reactionList);
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize);
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadModules() throws RemoteException, SQLException{

		while(!this.resultModules.isEmpty() && !this.cancel.get()) {

			Module_entry moduleList;
			if((moduleList =this.resultModules.poll()) != null) {

				this.kegg_loader.loadModule(moduleList);
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize);
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadPathways() throws RemoteException, SQLException{

		while(!this.getKegg_Pathways_Hierarchy().isEmpty() && !this.cancel.get()) {
			
			Pathways_hierarchy reactionPathList;
			if((reactionPathList = this.getKegg_Pathways_Hierarchy().poll()) != null) {

				this.kegg_loader.loadPathways(reactionPathList);
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize);
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadReactionsPathwayList() throws RemoteException, SQLException{

		while(!this.getReactionsPathwayList().isEmpty() && !this.cancel.get()) {

			Integer reactionPathList;
			
			if((reactionPathList = this.getReactionsPathwayList().poll()) != null) {

				this.kegg_loader.load_ReactionsPathway(reactionPathList);
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize);
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadEnzymesPathwayList() throws RemoteException, SQLException{

		while(!this.getEnzymesPathwayList().isEmpty() && !this.cancel.get()) {

			String enzymesPathwayList;
			if((enzymesPathwayList = this.getEnzymesPathwayList().poll()) != null) {
				
				this.kegg_loader.load_EnzymesPathway(enzymesPathwayList);
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize);
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadModulesPathwayList() throws RemoteException, SQLException{

		while(!this.getModulesPathwayList().isEmpty() && !this.cancel.get()) {

			Integer modulePathList;
			if((modulePathList = this.getModulesPathwayList().poll()) != null) {

				this.kegg_loader.load_ModulePathway(modulePathList);
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize);
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadMetabolitePathwayList() throws RemoteException, SQLException{

		while(!this.getMetabolitesPathwayList().isEmpty() && !this.cancel.get()) {

			Integer metaPathList;
			if((metaPathList = this.getMetabolitesPathwayList().poll()) != null) {

				this.kegg_loader.load_MetabolitePathway(metaPathList);
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize);
			}
		}
	}

	/**
	 * @param resultReactions the resultReactions to set
	 */
	public void setResultReactions(ConcurrentLinkedQueue<Reaction_entry> resultReactions) {
		this.resultReactions = resultReactions;
	}



	/**
	 * @return the resultReactions
	 */
	public ConcurrentLinkedQueue<Reaction_entry> getResultReactions() {
		return resultReactions;
	}



	/**
	 * @param resultPathways the resultPathways to set
	 */
	public void setResultPathways(ConcurrentLinkedQueue<String[]> resultPathways) {
		this.resultPathways = resultPathways;
	}



	/**
	 * @return the resultPathways
	 */
	public ConcurrentLinkedQueue<String[]> getResultPathways() {
		return resultPathways;
	}



	/**
	 * @param kegg_Pathways_Hierarchy the kegg_Pathways_Hierarchy to set
	 */
	public void setKegg_Pathways_Hierarchy(ConcurrentLinkedQueue<Pathways_hierarchy> kegg_Pathways_Hierarchy) {
		this.kegg_Pathways_Hierarchy = kegg_Pathways_Hierarchy;
	}



	/**
	 * @return the kegg_Pathways_Hierarchy
	 */
	public ConcurrentLinkedQueue<Pathways_hierarchy> getKegg_Pathways_Hierarchy() {
		return kegg_Pathways_Hierarchy;
	}



	/**
	 * @param orthologueEntities the orthologueEntities to set
	 */
	public void setOrthologueEntities(ConcurrentLinkedQueue<String> orthologueEntities) {
		this.orthologueEntities = orthologueEntities;
	}



	/**
	 * @return the orthologueEntities
	 */
	public ConcurrentLinkedQueue<String> getOrthologueEntities() {
		return orthologueEntities;
	}

	/**
	 * @return the resultMetabolites
	 */
	public ConcurrentLinkedQueue<Metabolite_entry> getResultMetabolites() {
		return resultMetabolites;
	}

	/**
	 * @param resultMetabolites the resultMetabolites to set
	 */
	public void setResultMetabolites(
			ConcurrentLinkedQueue<Metabolite_entry> resultMetabolites) {
		this.resultMetabolites = resultMetabolites;
	}

	/**
	 * @return the resultEnzymes
	 */
	public ConcurrentLinkedQueue<Enzyme_entry> getResultEnzymes() {
		return resultEnzymes;
	}

	/**
	 * @param resultEnzymes the resultEnzymes to set
	 */
	public void setResultEnzymes(ConcurrentLinkedQueue<Enzyme_entry> resultEnzymes) {
		this.resultEnzymes = resultEnzymes;
	}

	/**
	 * @return the resultGenes
	 */
	public ConcurrentLinkedQueue<Gene_entry> getResultGenes() {
		return resultGenes;
	}

	/**
	 * @param resultGenes the resultGenes to set
	 */
	public void setResultGenes(ConcurrentLinkedQueue<Gene_entry> resultGenes) {
		this.resultGenes = resultGenes;
	}

	/**
	 * @return the resultModules
	 */
	public ConcurrentLinkedQueue<Module_entry> getResultModules() {
		return resultModules;
	}

	/**
	 * @param resultModules the resultModules to set
	 */
	public void setResultModules(ConcurrentLinkedQueue<Module_entry> resultModules) {
		this.resultModules = resultModules;
	}

	/**
	 * @return the kegg_loader
	 */
	public KEGG_loader getKegg_loader() {
		return kegg_loader;
	}

	/**
	 * @param keggLoader the kegg_loader to set
	 */
	public void setKegg_loader(KEGG_loader keggLoader) {
		kegg_loader = keggLoader;
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
	 * @param pathways_id the pathways_id to set
	 */
	public void setPathways_id(ConcurrentHashMap<String,Integer> pathways_id) {
		this.pathways_id = pathways_id;
	}

	/**
	 * @return the pathways_id
	 */
	public ConcurrentHashMap<String,Integer> getPathways_id() {
		return pathways_id;
	}

	/**
	 * @param cancel
	 */
	public void setCancel(AtomicBoolean cancel) {

		this.cancel = cancel;
	}

	/**
	 * @return the error
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(boolean error) {
		this.error = error;
	}

}
