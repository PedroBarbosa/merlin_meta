/**
 * 
 */
package remote.retriever;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import pt.uminho.sysbio.common.bioapis.externalAPI.kegg.KeggAPI;
import remote.GetAndLoadKeggData;
import remote.retriever.KEGG_data_retriever.Entity_Type;
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
public class Retrieve_kegg_data {
	
	private static Logger LOGGER = Logger.getLogger(Retrieve_kegg_data.class);
	private ConcurrentLinkedQueue<Metabolite_entry> resultMetabolites;
	private ConcurrentLinkedQueue<Enzyme_entry> resultEnzymes;
	private ConcurrentLinkedQueue<Reaction_entry> resultReactions;
	private ConcurrentLinkedQueue<Gene_entry> resultGenes;
	private ConcurrentLinkedQueue<Module_entry> resultModules;
	private ConcurrentLinkedQueue<Pathways_hierarchy> kegg_Pathways_Hierarchy;
	private ConcurrentLinkedQueue<String> orthologueEntities,compoundsWithBiologicalRoles;
	private AtomicBoolean cancel;

	/**
	 * @param organismID
	 * @throws Exception
	 */
	public Retrieve_kegg_data(String organismID) throws Exception {

		this.resultMetabolites = new ConcurrentLinkedQueue<Metabolite_entry> ();
		this.resultEnzymes = new ConcurrentLinkedQueue<Enzyme_entry> ();
		this.resultReactions = new ConcurrentLinkedQueue<Reaction_entry> ();
		this.resultGenes = new ConcurrentLinkedQueue<Gene_entry> ();
		this.resultModules = new ConcurrentLinkedQueue<Module_entry> ();
		this.cancel = new AtomicBoolean(false);
		List<Entity_Type> data = new ArrayList<Entity_Type>();
		data.add(Entity_Type.Drugs);
		data.add(Entity_Type.Compound);
		data.add(Entity_Type.Glycan);
		data.add(Entity_Type.Reaction);
		data.add(Entity_Type.Pathways);
		data.add(Entity_Type.Enzyme);
		
		if (organismID.isEmpty() || organismID.equalsIgnoreCase("no_org")) {
		
			LOGGER.debug(organismID);
		}
		else {			
			
			data.add(Entity_Type.Gene);
		}
		data.add(Entity_Type.Module);

		long startTime = System.currentTimeMillis();

		ConcurrentHashMap<String,Metabolite_entry> resultMetabolites=new ConcurrentHashMap<String,Metabolite_entry>();
		ConcurrentHashMap<String,Enzyme_entry> resultEnzymes=new ConcurrentHashMap<String, Enzyme_entry>();
		ConcurrentHashMap<String,Reaction_entry> resultReactions=new ConcurrentHashMap<String, Reaction_entry>();
		ConcurrentHashMap<String,Gene_entry> resultGenes=new ConcurrentHashMap<String, Gene_entry>();
		ConcurrentHashMap<String,Module_entry> resultModules=new ConcurrentHashMap<String, Module_entry>();

		this.kegg_Pathways_Hierarchy= new ConcurrentLinkedQueue<Pathways_hierarchy>();
		this.orthologueEntities = new ConcurrentLinkedQueue<String>();

		int numberOfProcesses =  Runtime.getRuntime().availableProcessors()*2;

		{	
			long startTime_cbr = System.currentTimeMillis();
			this.setCompoundsWithBiologicalRoles(KEGG_data_retriever.getCompoundsWithBiologicalRoles());
			long endTime_process_cbr = System.currentTimeMillis();

			LOGGER.debug("Total elapsed time in execution of method setCompoundsWithBiologicalRoles is :"+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime_process_cbr-startTime_cbr),TimeUnit.MILLISECONDS.toSeconds(endTime_process_cbr-startTime_cbr) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime_process_cbr-startTime_cbr))));
		}
		//Concurrent Data structures 
		List<Thread> threads = new ArrayList<Thread>();

		for(Entity_Type entity_Type_String:data) {

			long startTime_process = System.currentTimeMillis();
			ConcurrentLinkedQueue<String> entity = new ConcurrentLinkedQueue<String>();

			if(!this.cancel.get()) {

				if(entity_Type_String.equals(Entity_Type.Compound)||entity_Type_String.equals(Entity_Type.Drugs)||entity_Type_String.equals(Entity_Type.Glycan)) {

						entity.addAll(KEGG_data_retriever.getEntities(KeggAPI.getInfo(entity_Type_String.getEntity_Type()[0]/*+"/"+entity_Type_String.getEntity_Type()[1]+suffix+i*/), entity_Type_String.getEntity_Type()[1]));
				}

				if(entity_Type_String.equals(Entity_Type.Enzyme)) {

						entity.addAll(KEGG_data_retriever.getEntities(KeggAPI.getInfo(Entity_Type.Enzyme.getEntity_Type()[0]/*+"/"+Entity_Type.Enzyme.getEntity_Type()[1]+":"+i*/),Entity_Type.Enzyme.getEntity_Type()[1]));
				}

				if(entity_Type_String.equals(Entity_Type.Gene)) {
					
						entity = KEGG_data_retriever.getEntities(KeggAPI.getInfo(/*Entity_Type.Gene.getEntity_Type()[0]+"/"+*/organismID/*+":"*/),organismID.toLowerCase());
				}

				if(entity_Type_String.equals(Entity_Type.Reaction)) {
					
						entity = KEGG_data_retriever.getEntities(KeggAPI.getInfo(Entity_Type.Reaction.getEntity_Type()[0]/*+"/"+Entity_Type.Reaction.getEntity_Type()[1]+":R"+i*/),Entity_Type.Reaction.getEntity_Type()[1]);
				}
				
				if(entity_Type_String.equals(Entity_Type.Module)) {

					entity = KEGG_data_retriever.getStructuralComplexModules();//entity = KEGG_data_retriever.getEntities(KeggAPI.getinfo(Entity_Type.Module.getEntity_Type()[0]+" "+Entity_Type.Module.getEntity_Type()[1]),Entity_Type.Module.getEntity_Type()[1]);
				}

				if(entity_Type_String.equals(Entity_Type.Pathways)) {

					this.kegg_Pathways_Hierarchy = KEGG_data_retriever.get_Kegg_Pathways_Hierarchy();//resultPathways = KEGG_data_retriever.get_All_Kegg_Pathways();
				}
				
				Metabolite_entry  metabolite_entry = new Metabolite_entry("Biomass");
				metabolite_entry.setName("Biomass");
				resultMetabolites.put("Biomass",metabolite_entry);
				
				boolean entityIsNotSet=true;
				numberOfProcesses=Runtime.getRuntime().availableProcessors()*10;
				
				for(int i=0; i<numberOfProcesses; i++) {
					
					Runnable kegg_data_retriever = new KEGG_data_retriever(entity,entity_Type_String,entityIsNotSet,organismID,
							resultMetabolites, resultEnzymes,resultReactions,resultGenes,resultModules, this.cancel);
					Thread thread = new Thread(kegg_data_retriever);
					threads.add(thread);
					//System.out.println("Start "+i);
					thread.start();
					
					try {

						Thread.sleep(1000);

					} 
					catch (InterruptedException e1){

						Thread.currentThread().interrupt();
					}
					
				}

				for(Thread thread :threads) {
					
					thread.join();
				}

			}

			long endTime_process = System.currentTimeMillis();

			LOGGER.debug("Total elapsed time in execution of method "+entity_Type_String+" is :"+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime_process-startTime_process),TimeUnit.MILLISECONDS.toSeconds(endTime_process-startTime_process) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime_process-startTime_process))));
		}
		long endTime = System.currentTimeMillis();

		LOGGER.debug("Total elapsed time in execution of method GLOBAL is :"+ String.format("%d min, %d sec", 
				TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));

		//from maps to lists
		for(String entry : resultMetabolites.keySet()) {
			
			this.resultMetabolites.add(resultMetabolites.get(entry));
		}
		for(String entry : resultEnzymes.keySet())
		{
			this.resultEnzymes.add(resultEnzymes.get(entry));
		}
		if(!organismID.isEmpty()) {
			
			for(String entry : resultGenes.keySet())
			{
				this.resultGenes.add(resultGenes.get(entry));
			}
		}
		
		for(String entry : resultReactions.keySet()) {
			
			this.resultReactions.add(resultReactions.get(entry));
		}
		
		for(String entry : resultModules.keySet()) {
			
			this.resultModules.add(resultModules.get(entry));
		}

	}

	/**
	 * @return the kegg_Pathways_Hierarchy
	 */
	public ConcurrentLinkedQueue<Pathways_hierarchy> getKegg_Pathways_Hierarchy() {
		
		return kegg_Pathways_Hierarchy;
	}

	/**
	 * @param keggPathwaysHierarchy the kegg_Pathways_Hierarchy to set
	 */
	public void setKegg_Pathways_Hierarchy(
			ConcurrentLinkedQueue<Pathways_hierarchy> keggPathwaysHierarchy) {
		
		kegg_Pathways_Hierarchy = keggPathwaysHierarchy;
	}

	/**
	 * @return the orthologueEntities
	 */
	public ConcurrentLinkedQueue<String> getOrthologueEntities() {
		
		return orthologueEntities;
	}

	/**
	 * @param orthologueEntities the orthologueEntities to set
	 */
	public void setOrthologueEntities(
			ConcurrentLinkedQueue<String> orthologueEntities) {
		
		this.orthologueEntities = orthologueEntities;
	}

	/**
	 * @param resultEnzymes the resultEnzymes to set
	 */
	public void setResultEnzymes(ConcurrentLinkedQueue<Enzyme_entry> resultEnzymes) {
		
		this.resultEnzymes = resultEnzymes;
	}

	/**
	 * @return the resultEnzymes
	 */
	public ConcurrentLinkedQueue<Enzyme_entry> getResultEnzymes() {
		
		return resultEnzymes;
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
	 * @param resultGenes the resultGenes to set
	 */
	public void setResultGenes(ConcurrentLinkedQueue<Gene_entry> resultGenes) {
		
		this.resultGenes = resultGenes;
	}



	/**
	 * @return the resultGenes
	 */
	public ConcurrentLinkedQueue<Gene_entry> getResultGenes() {
		
		return resultGenes;
	}



	/**
	 * @param resultModules the resultModules to set
	 */
	public void setResultModules(ConcurrentLinkedQueue<Module_entry> resultModules) {
		
		this.resultModules = resultModules;
	}



	/**
	 * @return the resultModules
	 */
	public ConcurrentLinkedQueue<Module_entry> getResultModules() {
		
		return resultModules;
	}



	/**
	 * @param resultMetabolites the resultMetabolites to set
	 */
	public void setResultMetabolites(ConcurrentLinkedQueue<Metabolite_entry> resultMetabolites) {
		
		this.resultMetabolites = resultMetabolites;
	}



	/**
	 * @return the resultMetabolites
	 */
	public ConcurrentLinkedQueue<Metabolite_entry> getResultMetabolites() {
		
		return resultMetabolites;
	}

	/**
	 * @param compoundsWithBiologicalRoles the compoundsWithBiologicalRoles to set
	 */
	public void setCompoundsWithBiologicalRoles(
			ConcurrentLinkedQueue<String> compoundsWithBiologicalRoles) {
		
		this.compoundsWithBiologicalRoles = compoundsWithBiologicalRoles;
	}

	/**
	 * @return the compoundsWithBiologicalRoles
	 */
	public ConcurrentLinkedQueue<String> getCompoundsWithBiologicalRoles() {
		
		return compoundsWithBiologicalRoles;
	}
	
	/**
	 * @param cancel
	 */
	public void setCancel(AtomicBoolean cancel) {

		this.cancel = cancel;
	}

}
