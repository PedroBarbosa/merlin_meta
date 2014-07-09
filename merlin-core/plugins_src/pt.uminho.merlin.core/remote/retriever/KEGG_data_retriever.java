/**
 * 
 */
package remote.retriever;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.uminho.sysbio.common.bioapis.externalAPI.kegg.KeggAPI;
import pt.uminho.sysbio.common.bioapis.externalAPI.kegg.KeggOperation;
import pt.uminho.sysbio.common.bioapis.externalAPI.kegg.KeggRestful;
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
public class KEGG_data_retriever implements Runnable{

	private ConcurrentHashMap<String,Metabolite_entry> resultMetabolites;
	private ConcurrentHashMap<String,Enzyme_entry> resultEnzymes;
	private ConcurrentHashMap<String,Reaction_entry> resultReactions;
	private ConcurrentHashMap<String,Gene_entry> resultGenes;
	private ConcurrentHashMap<String,Module_entry> resultModules;
	private String organismID;
	private Entity_Type entity_Type_String;
	private ConcurrentLinkedQueue<String> entity;
	//private ConcurrentLinkedQueue<String> orthologueEntities;
	private int errorCount;
	private AtomicBoolean cancel;

	/**
	 * @param entity
	 * @param entity_Type_String
	 * @param entityIsNotSet
	 * @param organismID
	 * @param resultMetabolites
	 * @param resultEnzymes
	 * @param resultReactions
	 * @param resultGenes
	 * @param resultModules
	 * @param cancel
	 */
	public KEGG_data_retriever(
			ConcurrentLinkedQueue<String> entity,
			Entity_Type entity_Type_String,
			boolean entityIsNotSet,
			String organismID, 
			ConcurrentHashMap<String, Metabolite_entry> resultMetabolites,
			ConcurrentHashMap<String, Enzyme_entry> resultEnzymes, 
			ConcurrentHashMap<String, Reaction_entry> resultReactions, 
			ConcurrentHashMap<String, Gene_entry> resultGenes, 
			ConcurrentHashMap<String, Module_entry> resultModules,
			AtomicBoolean cancel) {

		this.setEntity(entity);
		this.setEntity_Type_String(entity_Type_String);
		this.setOrganismID(organismID);
		this.setResultMetabolites(resultMetabolites);
		this.setResultEnzymes(resultEnzymes);
		this.setResultReactions(resultReactions);
		this.setResultGenes(resultGenes);
		this.setResultModules(resultModules);
		this.cancel = cancel;
	}


	@Override
	public void run() {

		List<String> poooledEntities=new ArrayList<String>();
		String poooledEntity;

		try  {

			if(entity_Type_String.equals(Entity_Type.Compound)||entity_Type_String.equals(Entity_Type.Drugs)||entity_Type_String.equals(Entity_Type.Glycan)) {

				while(entity.size()>0 && !this.cancel.get()) {

					poooledEntity = entity.poll();
					poooledEntities.add(poooledEntity);
					String query = poooledEntity;
					int i=0;

					while(i<9 && entity.size()>0) {

						poooledEntity= entity.poll();
						poooledEntities.add(poooledEntity);
						query=query.concat("+"+poooledEntity);
						i++;
					}

					ConcurrentHashMap<String,Metabolite_entry> result=this.getResultMetabolites();
					result.putAll(getMetabolitesArray(KeggRestful.fetch(KeggOperation.get,query)));
					this.setResultMetabolites(result);
					poooledEntities=new ArrayList<String>();
					//System.out.println("countdown " + entity.size());
				}
			}

			if(entity_Type_String.equals(Entity_Type.Enzyme)) {

				while(entity.size()>0 && !this.cancel.get()) {

					poooledEntity= entity.poll();
					poooledEntities.add(poooledEntity);
					String query = poooledEntity;
					int i=0;

					while(i<9 && entity.size()>0) {

						poooledEntity= entity.poll();
						poooledEntities.add(poooledEntity);
						query=query.concat("+"+poooledEntity);
						i++;
					}
					ConcurrentHashMap<String,Enzyme_entry> result = this.getResultEnzymes();
					result.putAll(getEnzymesArray(KeggRestful.fetch(KeggOperation.get,query),organismID));
					this.setResultEnzymes(result);
					//System.out.println("countdown " + entity.size());
					poooledEntities=new ArrayList<String>();
				}
			}

			if(entity_Type_String.equals(Entity_Type.Reaction)) {

				while(entity.size()>0 && !this.cancel.get()) {

					poooledEntity= entity.poll(); 
					poooledEntities.add(poooledEntity);
					String query = poooledEntity;
					int i=0;

					while(i<9 && entity.size()>0) {

						poooledEntity= entity.poll();
						poooledEntities.add(poooledEntity);
						query=query.concat("+"+poooledEntity);
						i++;
					}
					ConcurrentHashMap<String,Reaction_entry> result=this.getResultReactions();
					result.putAll(getReactionsArray(KeggRestful.fetch(KeggOperation.get,query)));
					this.setResultReactions(result);
					//System.out.println("countdown " + entity.size());
					poooledEntities=new ArrayList<String>();
				}
			}

			if(entity_Type_String.equals(Entity_Type.Gene)) {

				while(entity.size()>0 && !this.cancel.get()) {

					poooledEntity= entity.poll();
					poooledEntities.add(poooledEntity);
					String query = poooledEntity;
					int i=0;

					while(i<9 && entity.size()>0 && !this.cancel.get()) {

						poooledEntity= entity.poll();
						poooledEntities.add(poooledEntity);
						query=query.concat("+"+poooledEntity);
						i++;
					}
					ConcurrentHashMap<String,Gene_entry> result=this.getResultGenes();
					result.putAll(getGeneArray(KeggRestful.fetch(KeggOperation.get,query)));
					this.setResultGenes(result);
					//System.out.println("countdown " + entity.size());
					poooledEntities=new ArrayList<String>();
				}
			}

			if(entity_Type_String.equals(Entity_Type.Module)) {

				while(entity.size()>0 && !this.cancel.get()) {

					poooledEntity= entity.poll();
					poooledEntities.add(poooledEntity);
					String query = poooledEntity;
					int i=0;

					while(i<9 && entity.size()>0 && !this.cancel.get()) {

						poooledEntity= entity.poll();
						poooledEntities.add(poooledEntity);
						query=query.concat("+"+poooledEntity);
						i++;
					}
					ConcurrentHashMap<String,Module_entry> result=this.getResultModules();
					result.putAll(getStructuralComplexArray(KeggRestful.fetch(KeggOperation.get,query)));
					this.setResultModules(result);
					//System.out.println("countdown " + entity.size());
					poooledEntities=new ArrayList<String>();
				}
			}
		}
		catch (Exception e)  {

			if(this.errorCount<10) {

				this.entity.addAll(poooledEntities);
				this.errorCount = this.errorCount+1;
				//System.err.println("\n\n\n\n\n\n\n\nTEM DE SE FAZER A REQUERY senao perdemos dados!!!!\n\n\n\n\n\n\n\n");
				System.out.println("readicionadas"+poooledEntities);

				try {

					Thread.sleep(60000);

				} 
				catch (InterruptedException e1){

					Thread.currentThread().interrupt();
				}

				this.run();
			}
			else {

				e.printStackTrace();
			}
		}

	}

	/**
	 * @param metabolite_Type
	 * @return
	 * @throws ServiceException
	 * @throws RemoteException

	public Map<String,Metabolite_entry> get_All_Kegg_Metabolites(Entity_Type metabolite_Type) throws ServiceException, RemoteException{

		long startTime = System.currentTimeMillis();

		KEGGPortType serv = KeggAPI.getLocator().getKEGGPort();
		Map<String,Metabolite_entry> result = new HashMap<String, Metabolite_entry>();

		this.entity = getEntities(KeggAPI.getinfo(metabolite_Type.getEntity_Type()[0]+" "+metabolite_Type.getEntity_Type()[1]), metabolite_Type.getEntity_Type()[1]);

		for(int index = 0; index<entity.size(); index++)
		{
			String gene = entity.get(index);
			int i=0;
			while(i<9 && index+1<entity.size())
			{
				index++;
				metabolite=metabolite.concat("+"+entity.get(index));
				i++;
			}
			result.putAll(getMetabolitesArray(KeggRestful.fetch(KEGGOPERATION.GET,metabolite)));
		}

		long endTime = System.currentTimeMillis();

		//System.out.println("Total elapsed time in execution of method callMethod() is :"+ String.format("%d min, %d sec", 
				TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
		return result;
	}*/

	/**
	 * @param resultArray
	 * @return
	 */
	private Map<String ,Metabolite_entry> getMetabolitesArray(String resultArray) {

		this.errorCount=0;
		Map<String ,Metabolite_entry> result = new HashMap<String, Metabolite_entry>();

		for(String results : resultArray.split("///")) {

			results=results.trim();
			if(!results.isEmpty()) {

				results=results.concat("\n///");
				Map<String, List<String>> resultsParsed = KeggAPI.parseFullEntry(results);
				String entry = KeggAPI.getFirstIfExists(KeggAPI.splitWhiteSpaces(resultsParsed.get("ENTRY")));

				if(entry!=null) {

					Metabolite_entry metabolite_entry = new Metabolite_entry(entry);

					metabolite_entry.setFormula(KeggAPI.getFirstIfExists(resultsParsed.get("FORMULA")));
					metabolite_entry.setMolecular_weight(KeggAPI.getFirstIfExists(resultsParsed.get("MOL_WEIGHT")));
					String rawName = KeggAPI.getFirstIfExists(resultsParsed.get("NAME"));

					if(rawName != null) {

						metabolite_entry.setName(rawName.replace(";", "").trim());
					}
					String same_as = KeggAPI.getFirstIfExists(resultsParsed.get("REMARK"));

					if(same_as != null) {

						if(same_as.contains("Same as:")) metabolite_entry.setSame_as(same_as.split(":")[1].trim());
					}

					List<String> names = resultsParsed.get("NAME");
					if(names == null) names = new ArrayList<String>();
					names.remove(rawName);
					metabolite_entry.setNames(names);

					List<String> ecnumbers = KeggAPI.splitWhiteSpaces(resultsParsed.get("ENZYME"));
					if(ecnumbers == null) ecnumbers = new ArrayList<String>();
					metabolite_entry.setEnzymes(ecnumbers);

					List<String> reactions = KeggAPI.splitWhiteSpaces(resultsParsed.get("REACTION"));
					if(reactions == null) reactions = new ArrayList<String>();
					metabolite_entry.setReactions(reactions);

					List<String> pathwaysData = resultsParsed.get("PATHWAY");

					if(pathwaysData != null) {

						Map<String, String> pathways = new HashMap<String, String>();

						for(String path: pathwaysData) {

							pathways.put(path.split("\\s")[0].replace("ko", "").replace("map", ""), path.replace(path.split("\\s")[0], "").trim());
						}
						metabolite_entry.setPathways(pathways);

						//System.out.println(entry+"\t"+pathways);
					}

					List<String> crossRefs = resultsParsed.get("DBLINKS");
					if(crossRefs == null) crossRefs = new ArrayList<String>();
					metabolite_entry.setDblinks(crossRefs);

					result.put(entry,metabolite_entry);
					////System.out.println(result.get(entry).getEntry());
					////System.out.println(result.get(entry));
				}
			}
		}
		return result;
	}

	/**
	 * @param organismID
	 * @return
	 * @throws ServiceException
	 * @throws RemoteException

	public Map<String,Gene_entry> get_Kegg_Genes(String organismID) throws ServiceException, RemoteException{

		long startTime = System.currentTimeMillis();

		KEGGPortType serv = KeggAPI.getLocator().getKEGGPort();
		Map<String ,Gene_entry> result = new HashMap<String, Gene_entry>();

		entity = getEntities(KeggAPI.getinfo(Entity_Type.Gene.getEntity_Type()[0]+" "+organismID+":"),organismID.toLowerCase()+":");

		for(int index = 0; index<entity.size(); index++)
		{
			String gene = entity.get(index);
			int i=0;
			while(i<9 && index+1<entity.size())
			{
				index++;
				gene=gene.concat("+"+entity.get(index));
				i++;
			}
			result.putAll(getGeneArray(KeggRestful.fetch(KEGGOPERATION.GET,gene)));
		}

		long endTime = System.currentTimeMillis();

		//System.out.println("Total elapsed time in execution of method callMethod() is :"+ String.format("%d min, %d sec", 
				TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
		return result;
	}*/

	/**
	 * @param resultArray
	 * @return
	 */
	private Map<String ,Gene_entry> getGeneArray(String resultArray) {

		this.errorCount=0;
		Map<String ,Gene_entry> result = new HashMap<String, Gene_entry>();

		for(String results : resultArray.split("///")) {

			results=results.trim();

			if(!results.isEmpty()) {

				results=results.concat("\n///");
				Map<String, List<String>> resultsParsed = KeggAPI.parseFullEntry(results);
				String entry = KeggAPI.getFirstIfExists(KeggAPI.splitWhiteSpaces(resultsParsed.get("ENTRY")));

				if(entry!=null)  {

					Gene_entry gene_entry = new Gene_entry(entry);

					String rawName = KeggAPI.getFirstIfExists(resultsParsed.get("NAME"));

					if(rawName != null) {

						gene_entry.setName(rawName.replace(";", "").trim());
					}

					List<String> names = resultsParsed.get("NAME");

					if(names == null)
						names = new ArrayList<String>();

					names.remove(rawName);
					gene_entry.setNames(names);


					List<String> orthologyData = resultsParsed.get("ORTHOLOGY");

					if(orthologyData != null) {

						List<String> orthologues = new ArrayList<String>();

						for(String orthologue: orthologyData) {

							orthologues.add(orthologue.split("\\s")[0]);
						}
						gene_entry.setOrthologues(orthologues);
					}


					List<String> modulesData = resultsParsed.get("MODULE");

					if(modulesData != null) {

						List<String> modules = new ArrayList<String>();

						for(String module: modulesData) {

							modules.add(module.split("\\s")[0]);
						}
						gene_entry.setModules(modules);
					}

					String position = KeggAPI.getFirstIfExists(resultsParsed.get("POSITION"));

					if(position != null) {

						String[] position_array = position.split(":");

						if(position_array.length>1) {

							String chromosome_name = position_array[0];
							gene_entry.setChromosome_name(chromosome_name);
							//position = position.replace(chromosome_name+":", "").trim();
							position = position_array[1];
						}

						position = position.replaceFirst("\\.\\.", ":");
						gene_entry.setLeft_end_position(position.split(":")[0]);
						gene_entry.setRight_end_position(position.split(":")[1]);
					}

					List<String> sequenceAAData = resultsParsed.get("AASEQ");

					if(sequenceAAData != null) {
						gene_entry.setAalength(sequenceAAData.get(0));
						sequenceAAData.remove(0);
						String aaSequence = "";
						for (String aa :sequenceAAData) 
						{
							aaSequence=aaSequence.concat(aa);
						}
						gene_entry.setAasequence(aaSequence);
					}

					List<String> sequenceNTData = resultsParsed.get("NTSEQ");

					if(sequenceNTData != null) {

						gene_entry.setNtlength(sequenceNTData.get(0));
						sequenceNTData.remove(0);
						String ntSequence = "";

						for (String nt :sequenceNTData)  {

							ntSequence=ntSequence.concat(nt);
						}
						gene_entry.setNtsequence(ntSequence);
					}

					List<String> crossRefs = resultsParsed.get("DBLINKS");
					if(crossRefs == null) crossRefs = new ArrayList<String>();
					gene_entry.setDblinks(crossRefs);

					result.put(entry,gene_entry);
					////System.out.println(result.get(entry).getEntry());
					////System.out.println(result.get(entry));
				}
			}
		}
		return result;
	}


	/**
	 * @param organismID
	 * @return
	 * @throws ServiceException
	 * @throws RemoteException

	public Map<String,Enzyme_entry> get_All_Kegg_Enzymes(String organismID) throws ServiceException, RemoteException{

		long startTime = System.currentTimeMillis();

		KEGGPortType serv = KeggAPI.getLocator().getKEGGPort();
		Map<String ,Enzyme_entry> result = new HashMap<String, Enzyme_entry>();

		entity = getEntities(KeggAPI.getinfo(Entity_Type.Enzyme.getEntity_Type()[0]+" "+Entity_Type.Enzyme.getEntity_Type()[1]),Entity_Type.Enzyme.getEntity_Type()[1]);
		for(int index = 0; index<entity.size(); index++)
		{
			String ecnumber = entity.get(index);
			int i=0;
			while(i<9 && index+1<entity.size())
			{
				index++;
				ecnumber=ecnumber.concat("+"+entity.get(index));
				i++;
			}
			result.putAll(getEnzymesArray(KeggRestful.fetch(KEGGOPERATION.GET,ecnumber), organismID));
		}

		long endTime = System.currentTimeMillis();

		//System.out.println("Total elapsed time in execution of method callMethod() is :"+ String.format("%d min, %d sec", 
				TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
		return result;
	}*/

	/**
	 * @param resultArray
	 * @param organismID
	 * @return
	 */
	private Map<String,Enzyme_entry> getEnzymesArray(String resultArray, String organismID) {

		this.errorCount=0;
		Map<String ,Enzyme_entry> result = new TreeMap<String, Enzyme_entry>();

		for(String results : resultArray.split("///")) {

			results=results.trim();
			if(!results.isEmpty()) {

				results=results.concat("\n///");
				Map<String, List<String>> resultsParsed = KeggAPI.parseFullEntry(results);
				String entry = KeggAPI.getSecondIfExists(KeggAPI.splitWhiteSpaces(resultsParsed.get("ENTRY")));

				if(entry!=null) {

					Enzyme_entry enzyme_entry = new Enzyme_entry(entry.replace("EC ", "").trim());

					List<String> names = resultsParsed.get("NAME");
					if(names == null) names = new ArrayList<String>();
					enzyme_entry.setNames(names);


					enzyme_entry.setName(KeggAPI.getFirstIfExists(resultsParsed.get("SYSNAME")));

					if(enzyme_entry.getName()==null) {

						if(!enzyme_entry.getNames().isEmpty()) {

							enzyme_entry.setName(enzyme_entry.getNames().get(0));
							names = enzyme_entry.getNames();
							names.remove(enzyme_entry.getName());
							enzyme_entry.setNames(names);
						}
					}

					enzyme_entry.setEnzyme_class(KeggAPI.getFirstIfExists(resultsParsed.get("CLASS")));

					List<String> genesList = resultsParsed.get("GENES");
					List<String> genes = new ArrayList<String>();

					if(genesList != null) {

						for(int i=0; i<genesList.size(); i++) {

							String gene = genesList.get(i);		

							if(gene.startsWith(organismID.toUpperCase())) {

								gene=gene.split(":")[1].trim();

								for(String geneID: gene.split("\\s")) {

									if(geneID.contains("(")) {

										geneID=geneID.split("\\(")[0].trim();
									}
									genes.add(geneID.trim());
									i= genesList.size();
								}
								enzyme_entry.setGenes(genes);
							}
						}
					}

					List<String> cofactorsList = resultsParsed.get("COFACTOR");
					List<String> cofactors = new ArrayList<String>();

					if(cofactorsList!=null) {

						for(String cofactor:cofactorsList) {

							cofactors.add(cofactor.split(":")[1].replace("]", "").replace(";", "").trim());
						}
						enzyme_entry.setCofactors(cofactors);
					}

					List<String> reactions = resultsParsed.get("ALL_REAC");
					if(reactions == null) reactions = new ArrayList<String>();
					enzyme_entry.setReactions(reactions);

					List<String> pathwaysData = resultsParsed.get("PATHWAY");

					if(pathwaysData != null) {

						Map<String, String> pathways = new HashMap<String, String>();

						for(String path: pathwaysData) {

							pathways.put(path.split("\\s")[0].replace("ec", ""), path.replace(path.split("\\s")[0], "").trim());
						}
						enzyme_entry.setPathways(pathways);
					}

					List<String> orthologyData = resultsParsed.get("ORTHOLOGY");

					if(orthologyData != null) {

						List<String> orthologues = new ArrayList<String>();

						for(String orthologue: orthologyData) {

							orthologues.add(orthologue.split("\\s")[0]);
						}
						enzyme_entry.setOrthologues(orthologues);
					}

					List<String> crossRefs = resultsParsed.get("DBLINKS");
					if(crossRefs == null) crossRefs = new ArrayList<String>();
					enzyme_entry.setDblinks(crossRefs);

					result.put(entry,enzyme_entry);
				}
			}
		}
		return result;
	}


	/**
	 * @param resultArray
	 * @return
	 */
	private Map<String,Reaction_entry> getReactionsArray(String resultArray) {

		this.errorCount=0;
		//System.out.println(resultArray);
		Map<String,Reaction_entry> result = new TreeMap<String, Reaction_entry>();

		for(String results : resultArray.split("///")) {

			results=results.trim();
			if(!results.isEmpty()) {

				results=results.concat("\n///");
				Map<String, List<String>> resultsParsed = KeggAPI.parseFullEntry(results);
				String entry = KeggAPI.getFirstIfExists(KeggAPI.splitWhiteSpaces(resultsParsed.get("ENTRY")));

				if(entry!=null) {

					Reaction_entry reaction_entry = new Reaction_entry(entry);

					reaction_entry.setName(KeggAPI.getFirstIfExists(resultsParsed.get("NAME")));

					List<String> names = resultsParsed.get("NAME");
					if(names == null) {

						names = new ArrayList<String>();
					}
					reaction_entry.setNames(names);

					reaction_entry.setEquation(KeggAPI.getFirstIfExists(resultsParsed.get("DEFINITION")));

					String stoichiometry= KeggAPI.getFirstIfExists(resultsParsed.get("EQUATION"));
					String[] data=stoichiometry.split("<=>");
					String[] reactants = data[0].split("\\s\\+\\s");
					String[] products = data[1].split("\\s\\+\\s");

					reaction_entry.setReactantsStoichiometry(this.parseReactions(reactants,"-"));
					reaction_entry.setProductsStoichiometry(this.parseReactions(products,""));

					List<String> enzymes = KeggAPI.splitWhiteSpaces(resultsParsed.get("ENZYME"));
					if(enzymes == null) enzymes = new ArrayList<String>();
					reaction_entry.setEnzymes(enzymes);

					List<String> generics = resultsParsed.get("COMMENT");

					if(generics!=null) {

						for(String generic:generics) {

							if(generic.trim().toLowerCase().contains("general reaction")) {

								reaction_entry.setGeneric(true);
							}

							if(generic.trim().toLowerCase().contains("non-enzymatic") || generic.trim().toLowerCase().contains("non enzymatic")) {

								reaction_entry.setNon_enzymatic(true);
							}

							if(generic.trim().toLowerCase().contains("spontaneous")) {

								reaction_entry.setSpontaneous(true);
							}

						}
						reaction_entry.setComments(generics);
					}

					List<String> pathwaysData = resultsParsed.get("PATHWAY");

					if(pathwaysData != null) {

						Map<String, String> pathways = new HashMap<String, String>();
						for(String path: pathwaysData)
						{
							pathways.put(path.split("\\s")[0].replace("rn", ""), path.replace(path.split("\\s")[0], "").trim());
						}
						reaction_entry.setPathways(pathways);
					}

					List<String> crossRefs = resultsParsed.get("DBLINKS");

					if(crossRefs == null) {

						crossRefs = new ArrayList<String>();
					}
					reaction_entry.setDblinks(crossRefs);

					result.put(entry,reaction_entry);
				}
			}
		}
		return result;
	}

	/**
	 * @return
	 * @throws ServiceException
	 * @throws RemoteException

	public Map<String,Module_entry> get_Kegg_Complex_Module() throws ServiceException, RemoteException{

		long startTime = System.currentTimeMillis();

		KEGGPortType serv = KeggAPI.getLocator().getKEGGPort();
		Map<String,Module_entry> result = new HashMap<String,Module_entry>();

		entity = getEntities(KeggAPI.getinfo(Entity_Type.Module.getEntity_Type()[0]+" "+Entity_Type.Module.getEntity_Type()[1]),Entity_Type.Module.getEntity_Type()[1]);

		for(int index = 0; index<entity.size(); index++)
		{
			String module = entity.get(index);
			int i=0;
			while(i<9 && index+1<entity.size())
			{
				index++;
				module=module.concat("+"+entity.get(index));
				i++;
			}
			result.putAll(getStructuralComplexArray(KeggRestful.fetch(KEGGOPERATION.GET,module)));
		}

		long endTime = System.currentTimeMillis();

		//System.out.println("Total elapsed time in execution of method callMethod() is :"+ String.format("%d min, %d sec", 
				TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
		return result;
	}
	 */

	/**
	 * @param resultArray
	 * @return
	 */
	private Map<String,Module_entry> getStructuralComplexArray(String resultArray) {

		this.errorCount=0;
		Map<String,Module_entry> result = new TreeMap<String, Module_entry>();
		for(String results : resultArray.split("///"))
		{
			results=results.trim();
			if(!results.isEmpty())
			{
				results=results.concat("\n///");
				Map<String,List<String>> resultsParsed = KeggAPI.parseFullEntry(results);
				String entry = KeggAPI.getFirstIfExists(KeggAPI.splitWhiteSpaces(resultsParsed.get("ENTRY")));
				String moduleType = KeggAPI.getSecondIfExists(KeggAPI.splitWhiteSpaces(resultsParsed.get("ENTRY")));
				if(entry!=null && moduleType.toLowerCase().equals("complex"))
				{
					Module_entry module_entry = new Module_entry(entry);
					module_entry.setName(KeggAPI.getFirstIfExists(resultsParsed.get("NAME")));
					module_entry.setDefinition(KeggAPI.getFirstIfExists(resultsParsed.get("DEFINITION")));
					module_entry.setModuleType(moduleType);

					List<String> orthologues = KeggAPI.splitLinesGetOrthologues(resultsParsed.get("ORTHOLOGY"));
					module_entry.setOrthologues(orthologues);


					List<String> comments = resultsParsed.get("COMMENT");
					if(comments!=null)
					{
						for(String comment:comments)
						{
							if(comment.trim().startsWith("Stoichiometry"))
							{
								module_entry.setStoichiometry(comment.replace("Stoichiometry: ", "").trim());
							}

							if(comment.trim().startsWith("Substrate"))
							{
								List<String> data = new ArrayList<String>();
								String[] substrates = comment.replace("Substrate: ", "").split(",");
								Pattern pat = Pattern.compile("[C|G|D|K]\\d{5}");
								Matcher m;
								for(String substrate:substrates)
								{
									m = pat.matcher(substrate);
									if (m.find()) 
									{
										data.add(m.group());				
									}
								}
								if(!data.isEmpty())
								{
									module_entry.setSubstrates(data);
								}
							}
						}
					}

					List<String> pathwaysData = resultsParsed.get("PATHWAY");
					if(pathwaysData != null)
					{
						Map<String, String> pathways = new HashMap<String, String>();
						for(String path: pathwaysData)
						{
							pathways.put(path.split("\\s")[0].replace("map", ""), path.replace(path.split("\\s")[0], "").trim());
						}
						module_entry.setPathways(pathways);
					}
					String moduleHieralchicalClass = KeggAPI.getFirstIfExists(resultsParsed.get("CLASS"));
					module_entry.setModuleHieralchicalClass(moduleHieralchicalClass);

					result.put(entry,module_entry);
					//					//System.out.println(result.get(entry).getEntry());
					//					//System.out.println(result.get(entry));
					//					//System.out.println(module_entry);

				}
			}

		}
		return result;
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public static ConcurrentLinkedQueue<String[]> get_All_Kegg_Pathways() throws Exception {

		//long startTime = System.currentTimeMillis();

		List<String[]> pathways = KeggAPI.getPathways();
		ConcurrentLinkedQueue<String[]> result = new ConcurrentLinkedQueue<String[]>();

		for(int index = 0; index<pathways.size(); index++) {

			String[] data = new String[2];
			data[0]=pathways.get(index)[1].replace("map", "");
			data[1]=pathways.get(index)[0].replace(" - Reference pathway", "").trim();
			result.add(data);
		}

		//long endTime = System.currentTimeMillis();
		//System.out.println("Total elapsed time in execution of method callMethod() is :"+ String.format("%d min, %d sec", 
		//	TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));

		return result;
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public static ConcurrentLinkedQueue<Pathways_hierarchy> get_Kegg_Pathways_Hierarchy() throws Exception {

		//long startTime = System.currentTimeMillis();
		ConcurrentLinkedQueue<Pathways_hierarchy> result = new ConcurrentLinkedQueue<Pathways_hierarchy>();
		String resultString = KeggAPI.getXMLDataFromBriteID("br08901");	
		//System.out.println(resultString);

		String[] lines = resultString.split("\n");

		String key=null;
		Pathways_hierarchy pathways_hierarchy=null;
		Map<String,List<String[]>> pathways_hierarchy_map = null;
		List<String[]> pathways=null;

		for (int i = 0; i < lines.length; i++)  {

			String data = lines[i];

			if(data.startsWith("A")) {

				if(pathways_hierarchy!=null) {

					pathways_hierarchy_map.put(key, pathways);
					pathways_hierarchy.setPathways_hierarchy(pathways_hierarchy_map);
					result.add(pathways_hierarchy);
				}
				pathways_hierarchy = new Pathways_hierarchy(data.substring(1));
				pathways_hierarchy_map = new HashMap<String, List<String[]>>();
				key=null;
			}
			else {

				if(data.startsWith("B")) {

					if(key!=null) {

						pathways_hierarchy_map.put(key, pathways);
						pathways_hierarchy.setPathways_hierarchy(pathways_hierarchy_map);
					}
					key=data.substring(1).trim();
					pathways = new ArrayList<String[]>();
				}
				else {

					if(data.startsWith("C")) {

						String[] path = new String[2];
						path[0]=data.substring(1).trim().split("\\s")[0].trim();
						path[1]=data.substring(1).trim().replace(path[0],"").trim();
						pathways.add(path);												
					}
					else {

						if(data.equals("///")) {

							pathways_hierarchy_map.put(key, pathways);
							pathways_hierarchy.setPathways_hierarchy(pathways_hierarchy_map);
							result.add(pathways_hierarchy);
						}
					}
				}
			}
		}
		//long endTime = System.currentTimeMillis();
		//System.out.println("Total elapsed time in execution of method callMethod() is :"+ String.format("%d min, %d sec", 
		//	TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
		return result;

	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public static ConcurrentLinkedQueue<String> getStructuralComplexModules() throws Exception {

		String resultString = KeggAPI.getXMLDataFromBriteID("ko00002");
		String[] lines = resultString.split("\n");

		ConcurrentLinkedQueue<String> result = new ConcurrentLinkedQueue<String>();
		for (int i = 0; i < lines.length; i++) 
		{
			String data = lines[i];
			if(data.startsWith("AStructural Complex"))
			{
				i++;
				data = lines[i];
				while(!data.startsWith("#"))
				{
					if(data.startsWith("B"))
					{
						i++;
						data = lines[i];
					}
					else
					{
						if(data.startsWith("C"))
						{
							i++;
							data = lines[i];
						}
						else
						{
							if(data.startsWith("D"))
							{
								String module_id=data.substring(1).trim().split("\\s")[0].trim();
								result.add("md:"+module_id);		
								i++;
								data = lines[i];
							}
						}
					}
				}
				i=lines.length;
			}
		}
		return result;
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public static ConcurrentLinkedQueue<String> getCompoundsWithBiologicalRoles() throws Exception {

		//Compounds with biological roles 	br08001

		String resultString = KeggAPI.getXMLDataFromBriteID("br08001");
		String[] lines = resultString.split("\n");

		ConcurrentLinkedQueue<String> result = new ConcurrentLinkedQueue<String>();
		for (int i = 0; i < lines.length; i++) 
		{
			String data=lines[i];
			if(data.startsWith("D"))
			{
				String metabolite_id=data.substring(1).trim().split("\\s")[0].trim();
				result.add(metabolite_id);	
			}
		}
		return result;
	}


	/**
	 * @param response
	 * @param pattern
	 * @return
	 */
	public static ConcurrentLinkedQueue<String> getEntities(String response, String pattern) {

		String[] lines = response.split("\n");

		Pattern p = Pattern.compile("^"+pattern+":"+".+");
		Matcher m;
		ConcurrentLinkedQueue<String> data = new ConcurrentLinkedQueue<String>();

		for (int i = 0; i < lines.length; i++)  {

			m = p.matcher(lines[i]);

			if (m.matches())  {

				data.add(m.group().split("\\s")[0]);				
			}
		}
		
		return data;
	}

	/**
	 * @param results
	 * @param metabolites
	 * @param signal
	 */
	private Map<String, String[]> parseReactions(String[] metabolites, String signal) {

		Map<String, String[]> result = new HashMap<String, String[]>();

		String metabolite_ID = null, stoichiometry, chains;

		Pattern pat = Pattern.compile("[C|G|D]\\d{5}");

		for(String metabolite:metabolites) {

			String[] data = new String[2];

			metabolite=metabolite.trim();
			Matcher mat = pat.matcher(metabolite);

			if(mat.find()) { 

				metabolite_ID=mat.group();
			}

			if(metabolite.startsWith(metabolite_ID)) {

				stoichiometry=signal.concat("1");
			}
			else {

				stoichiometry=signal.concat(metabolite.split(metabolite_ID)[0].trim());
			}

			if(metabolite.endsWith(metabolite_ID)) {

				chains="1";
			}
			else {

				chains=metabolite.split(metabolite_ID)[1].trim();
			}

			data[0]=stoichiometry;
			data[1]=chains;
			result.put(metabolite_ID, data);
		}

		return result;
	}

	/**
	 * @author ODias
	 *
	 */
	public static enum Entity_Type {

		Drugs(new String[]{"drug","dr"}),
		Compound(new String[]{"compound","cpd"}),
		Glycan(new String[]{"glycan","gl"}),
		Reaction(new String[]{"reaction","rn"}),
		Pathways(new String[]{"pathways","path"}),
		Enzyme(new String[]{"enzyme","ec"}),
		Gene(new String[]{"genes",""}),
		Module(new String[]{"module","md"});

		private String[] entity_Type;

		/**
		 * @param entity_Type
		 */
		private Entity_Type(String[] entity_Type){
			this.entity_Type = entity_Type;
		}

		/**
		 * @return
		 */
		public String[] getEntity_Type(){
			return this.entity_Type;
		}
	}

	/**
	 * @param resultMetabolites the resultMetabolites to set
	 */
	public void setResultMetabolites(ConcurrentHashMap<String,Metabolite_entry> resultMetabolites) {
		this.resultMetabolites = resultMetabolites;
	}

	/**
	 * @return the resultMetabolites
	 */
	public ConcurrentHashMap<String,Metabolite_entry> getResultMetabolites() {
		return resultMetabolites;
	}

	/**
	 * @param resultEnzymes the resultEnzymes to set
	 */
	public void setResultEnzymes(ConcurrentHashMap<String,Enzyme_entry> resultEnzymes) {
		this.resultEnzymes = resultEnzymes;
	}

	/**
	 * @return the resultEnzymes
	 */
	public ConcurrentHashMap<String,Enzyme_entry> getResultEnzymes() {
		return resultEnzymes;
	}

	/**
	 * @param resultReactions the resultReactions to set
	 */
	public void setResultReactions(ConcurrentHashMap<String,Reaction_entry> resultReactions) {
		this.resultReactions = resultReactions;
	}

	/**
	 * @return the resultReactions
	 */
	public ConcurrentHashMap<String,Reaction_entry> getResultReactions() {
		return resultReactions;
	}

	/**
	 * @param resultGenes the resultGenes to set
	 */
	public void setResultGenes(ConcurrentHashMap<String,Gene_entry> resultGenes) {
		this.resultGenes = resultGenes;
	}

	/**
	 * @return the resultGenes
	 */
	public ConcurrentHashMap<String,Gene_entry> getResultGenes() {
		return resultGenes;
	}

	/**
	 * @param resultModules the resultModules to set
	 */
	public void setResultModules(ConcurrentHashMap<String,Module_entry> resultModules) {
		this.resultModules = resultModules;
	}

	/**
	 * @return the resultModules
	 */
	public ConcurrentHashMap<String,Module_entry> getResultModules() {
		return resultModules;
	}

	/**
	 * @param organismID the organismID to set
	 */
	public void setOrganismID(String organismID) {
		this.organismID = organismID;
	}


	/**
	 * @return the organismID
	 */
	public String getOrganismID() {
		return organismID;
	}

	/**
	 * @return the entity
	 */
	public ConcurrentLinkedQueue<String> getEntity() {
		return entity;
	}


	/**
	 * @param entity the entity to set
	 */
	public void setEntity(ConcurrentLinkedQueue<String> entity) {
		this.entity = entity;
	}


	/**
	 * @return the entity_Type_String
	 */
	public Entity_Type getEntity_Type_String() {
		return entity_Type_String;
	}


	/**
	 * @param entityTypeString the entity_Type_String to set
	 */
	public void setEntity_Type_String(Entity_Type entityTypeString) {
		entity_Type_String = entityTypeString;
	}


	/**
	 * @return the resultPathways

	public ConcurrentLinkedQueue<String[]> getResultPathways() {
		return resultPathways;
	}*/


	/**
	 * @param resultPathways the resultPathways to set

	public void setResultPathways(ConcurrentLinkedQueue<String[]> resultPathways) {
		this.resultPathways = resultPathways;
	}*/


	/**
	 * @param orthologueEntities the orthologueEntity to set

	public void setOrthologueEntities(ConcurrentLinkedQueue<String> orthologueEntities) {
		this.orthologueEntities = orthologueEntities;
	}


	/**
	 * @return the orthologueEntity

	public ConcurrentLinkedQueue<String> getOrthologueEntities() {
		return orthologueEntities;
	}


	/**
	 * @param args
	 * @throws RemoteException
	 * @throws ServiceException
	 * @throws InterruptedException

	public static void main(String[] args) throws RemoteException, ServiceException, InterruptedException{
		List<Entity_Type> data = new ArrayList<Entity_Type>();
		data.add(Entity_Type.Drugs);
		data.add(Entity_Type.Compound);
		data.add(Entity_Type.Glycan);
		data.add(Entity_Type.Reaction);
		data.add(Entity_Type.Pathways);
		data.add(Entity_Type.Enzyme);
		data.add(Entity_Type.Gene);
		data.add(Entity_Type.Module);

		long startTime = System.currentTimeMillis();

		ConcurrentHashMap<String,Metabolite_entry> resultMetabolites=new ConcurrentHashMap<String, Metabolite_entry>();
		ConcurrentHashMap<String,Enzyme_entry> resultEnzymes=new ConcurrentHashMap<String, Enzyme_entry>();
		ConcurrentHashMap<String,Reaction_entry> resultReactions=new ConcurrentHashMap<String, Reaction_entry>();
		ConcurrentHashMap<String,Gene_entry> resultGenes=new ConcurrentHashMap<String, Gene_entry>();
		ConcurrentHashMap<String,Module_entry> resultModules=new ConcurrentHashMap<String, Module_entry>();
		ConcurrentLinkedQueue<String[]> resultPathways = null;
		ConcurrentLinkedQueue<Pathways_hierarchy> kegg_Pathways_Hierarchy = null;
		ConcurrentLinkedQueue<String> orthologueEntities = new ConcurrentLinkedQueue<String>();
		int numberOfProcesses =  Runtime.getRuntime().availableProcessors()*2;
		//Concurrent Data structures 
		List<Thread> threads = new ArrayList<Thread>();

		for(Entity_Type entity_Type_String:data)
		{
			long startTime_process = System.currentTimeMillis();
			ConcurrentLinkedQueue<String> entity = new ConcurrentLinkedQueue<String>();

			if(entity_Type_String.equals(Entity_Type.Compound)||entity_Type_String.equals(Entity_Type.Drugs)||entity_Type_String.equals(Entity_Type.Glycan))
			{
				entity = KEGG_data_retriever.getEntities(KeggAPI.getinfo(entity_Type_String.getEntity_Type()[0]+" "+entity_Type_String.getEntity_Type()[1]), entity_Type_String.getEntity_Type()[1]);
			}
			if(entity_Type_String.equals(Entity_Type.Enzyme))
			{
				entity = getEntities(KeggAPI.getinfo(Entity_Type.Enzyme.getEntity_Type()[0]+" "+Entity_Type.Enzyme.getEntity_Type()[1]),Entity_Type.Enzyme.getEntity_Type()[1]);
			}
			if(entity_Type_String.equals(Entity_Type.Gene))
			{
				entity = getEntities(KeggAPI.getinfo(Entity_Type.Gene.getEntity_Type()[0]+" KLA:"),"KLA".toLowerCase()+":");
			}
			if(entity_Type_String.equals(Entity_Type.Reaction))
			{
				entity = getEntities(KeggAPI.getinfo(Entity_Type.Reaction.getEntity_Type()[0]+" "+Entity_Type.Reaction.getEntity_Type()[1]),Entity_Type.Reaction.getEntity_Type()[1]);
			}
			if(entity_Type_String.equals(Entity_Type.Module))
			{
				entity = getEntities(KeggAPI.getinfo(Entity_Type.Module.getEntity_Type()[0]+" "+Entity_Type.Module.getEntity_Type()[1]),Entity_Type.Module.getEntity_Type()[1]);
			}
			if(entity_Type_String.equals(Entity_Type.Pathways))
			{
				resultPathways = get_All_Kegg_Pathways();
				kegg_Pathways_Hierarchy = get_Kegg_Pathways_Hierarchy();
			}

			boolean entityIsNotSet=true;
			for(int i=0; i<numberOfProcesses; i++)
			{
				Runnable kegg_data_retriever = new KEGG_data_retriever(entity,entity_Type_String,entityIsNotSet,"KLA",resultMetabolites,resultEnzymes,resultReactions,
						resultGenes,resultModules
						//	,resultPathways
						,orthologueEntities,kegg_Pathways_Hierarchy);
				Thread thread = new Thread(kegg_data_retriever);
				threads.add(thread);
				//System.out.println("Start "+i);
				thread.start();
			}

			for(Thread thread :threads)
			{
				thread.join();
			}

			long endTime_process = System.currentTimeMillis();

			System.out.println("Total elapsed time in execution of method "+entity_Type_String+" is :"+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime_process-startTime_process),TimeUnit.MILLISECONDS.toSeconds(endTime_process-startTime_process) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime_process-startTime_process))));
		}
		System.out.println(resultEnzymes);
		System.out.println(resultPathways);
		long endTime = System.currentTimeMillis();

		System.out.println("Total elapsed time in execution of method GLOBAL is :"+ String.format("%d min, %d sec", 
				TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));

		//		//System.out.println(kgdr.get_Kegg_Genes("KLA"));
		//		//System.out.println(kgdr.get_All_Kegg_Metabolites(Entity_Type.Drugs));
		//		//System.out.println(kgdr.get_All_Kegg_Reactions());
		//		//System.out.println(kgdr.get_All_Kegg_Enzymes("KLA"));	
		//		//System.out.println(kgdr.get_All_Kegg_Pathways());
		////System.out.println(kgdr.get_Kegg_Complex_Module());
	} 


	/**
	 * @param kegg_Pathways_Hierarchy the kegg_Pathways_Hierarchy to set

	public void setKegg_Pathways_Hierarchy(ConcurrentLinkedQueue<Pathways_hierarchy> kegg_Pathways_Hierarchy) {
		this.kegg_Pathways_Hierarchy = kegg_Pathways_Hierarchy;
	}


	/**
	 * @return the kegg_Pathways_Hierarchy

	public ConcurrentLinkedQueue<Pathways_hierarchy> getKegg_Pathways_Hierarchy() {
		return kegg_Pathways_Hierarchy;
	}*/
}
