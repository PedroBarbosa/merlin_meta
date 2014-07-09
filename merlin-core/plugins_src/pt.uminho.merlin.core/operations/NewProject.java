package operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.NcbiAPI;
import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQLMultiThread;
import utilities.ProjectUtils;
import datatypes.Database;
import datatypes.Entities;
import datatypes.Project;
import datatypes.Table;
import datatypes.Tables;
import datatypes.metabolic.Compounds;
import datatypes.metabolic.CompoundsReactions;
import datatypes.metabolic.EnzymesContainer;
import datatypes.metabolic.Pathway;
import datatypes.metabolic.ReactionsContainer;
import datatypes.metabolic.ReagentProducts;
import datatypes.metabolic_regulatory.Entity;
import datatypes.metabolic_regulatory.Genes;
import datatypes.metabolic_regulatory.HomologyDataContainer;
import datatypes.metabolic_regulatory.Proteins;
import datatypes.metagenomics.EnzymesMetaContainer;
import datatypes.metagenomics.MetagenomicContainer;
import datatypes.metagenomics.PathwaysMetaContainer;
import datatypes.metagenomics.TaxonomyMetaContainer;
import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;

/**
 * This is the class that creates a new project for the AIbench interface.
 * 
 *
 */
@Operation(name="Project",description="Create a new merlin project")
public class NewProject {

	private String databaseName = null;
	private String usr = null;
	private String pwd = null;
	private String host = null;
	private String port = null;
	private String name = null;
	private String mysqlPID;
	private Map<String, String> oldPID=null;
	private String genomeID;
	private boolean isNCBIGenome;
	private long taxonomyID;
	private boolean isFaaFastaFiles;
	private boolean isFnaFastaFiles;
	private boolean isMetagenomicProject;
	
	/**
	 *  
	 * @param host Host to use to log at the database
	 */
	@Port(direction=Direction.INPUT, name="Host", order=1)
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * 
	 * @param port Port to use to log at the database
	 */
	@Port(direction=Direction.INPUT, name="Port", order=2)
	public void setPort(String port) {
		this.port = port;
	}
	/**
	 * 
	 * @param usr User name to use to log at the database
	 */
	@Port(direction=Direction.INPUT, name="User", order=3)
	public void setUsr(String usr) {
		this.usr = usr;
	}

	/**
	 * 
	 * @param pwd Password to use to log at the database
	 */
	@Port(direction=Direction.INPUT, name="Password", order=4)
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	/**
	 * 
	 * @param db Name of the database to associate the project to.
	 */
	@Port(direction=Direction.INPUT,validateMethod ="validateDataBase", name="Database", order=5)
	public void setDb(String databaseName) {
		this.databaseName = databaseName;
	}

	/**
	 * 
	 * @param name Name of the new project
	 */
	@Port(direction=Direction.INPUT, validateMethod = "validateProjectName" ,name="New project name", order=6)
	public void setName(String name) {
		//this.name = name;
	}

	/**
	 * @param mysqlPID
	 */
	@Port(direction=Direction.INPUT,name="PID",order=7)
	public void setMySQLPID(String mysqlPID) {
		this.mysqlPID = mysqlPID;
	}

	/**
	 * @param oldPID
	 */
	@Port(direction=Direction.INPUT,name="oldPID",order=8)
	public void setOldPID(Map<String,String> oldPID) {
		this.oldPID=oldPID;
	}

	/**
	 * @param directory
	 */
	@Port(direction=Direction.INPUT, name="genomeID",order=9)
	public void setGenomeID(String genomeID) {
		this.genomeID = genomeID;
	}

	/**
	 * @param directory
	 */
	@Port(direction=Direction.INPUT, name="isNCBIGenome",order=10)
	public void setNCBIGenome(boolean isNCBIGenome) {
		this.isNCBIGenome = isNCBIGenome;
	}
	
	/**
	 * @param directory
	 */
	@Port(direction=Direction.INPUT, name="isMetagenomicProject",order=11)
	public void setMetagenomicProject(boolean isMetagenomicProject) {
		this.isMetagenomicProject = isMetagenomicProject;
	}
	
	/**
	 * @param directory
	 */
	@Port(direction=Direction.INPUT, name="TaxonomyID",order=12)
	public void setTaxonomyID(long taxonomyID) {

		this.taxonomyID = taxonomyID;
	}

	/**
	 * @param directory
	 */
	@Port(direction=Direction.INPUT, name="isFaaFastaFiles",order=13)
	public void setIsFaaFastaFiles(boolean isFaaFastaFiles) {

		this.isFaaFastaFiles = isFaaFastaFiles;
	}

	/**
	 * @param directory
	 */
	@Port(direction=Direction.INPUT, name="isFnaFastaFiles",order=14)
	public void setIsFnaFastaFiles(boolean isFnaFastaFiles) {

		this.isFnaFastaFiles = isFnaFastaFiles;
	}

	/**
	 * @param name
	 */
	public void validateProjectName(String name) {

		List <String> projectNames = new ArrayList<String>();

		List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);

		for(int i=0; i<cl.size(); i++) {

			ClipboardItem item = cl.get(i);
			projectNames.add(item.getName());
		}

		if(name.isEmpty()) {

			while(projectNames.contains(name) || name.isEmpty()) {

				name = this.buildName(name);
			}
		}
		else {

			if(projectNames.contains(name)) {

				throw new IllegalArgumentException("Project with the same name already exists!\nPlease insert another name.");
			}
		}

		this.name=name;
	}

	/**
	 * @param databaseName
	 */
	public void validateDataBase(String databaseName) {

		List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);

		for (ClipboardItem item : cl) {

			String host_previous = ((Project)item.getUserData()).getDatabase().getMySqlCredentials().get_database_host();
			//String port_previous = ((Project)item.getUserData()).getDatabase().getMySqlLink().getDb_port();
			String databaseName_previous = ((Project)item.getUserData()).getDatabase().getMySqlCredentials().get_database_name();
			//String usr_previous = ((Project)item.getUserData()).getDatabase().getMySqlLink().getDb_usr();
			//String pwd_previous = ((Project)item.getUserData()).getDatabase().getMySqlLink().getDb_pwd();
			//MySqlLink dsa = new MySqlLink(host_previous, port_previous, databaseName_previous, usr_previous, pwd_previous);
			if(databaseName.equals(databaseName_previous) && host.equals(host_previous))
			{
				throw new IllegalArgumentException("Project connected to the same data base already exists");
			}
		}

	}

	/**
	 * 
	 * Method that returns the new project.
	 */
	/**
	 * @return
	 */
	@Port(direction=Direction.OUTPUT, name="project", order=14)
	public Project getDataBase() {

		MySQLMultiThread mysqlMutithread = new MySQLMultiThread(usr, pwd, host, port, databaseName);

		Database database = new Database(mysqlMutithread);

		String[] tables;

		try  {

			tables = database.getMySqlCredentials().showTables();

			Tables existingTables = new Tables();

			Entity gene = null;
			Entity protein = null;
			Entity enzyme = null;
			Entity compound = null;
			Entity reaction = null;
			Entity path = null;
			Entity repro = null;
			Entity compoundsReactions = null;
			Entity homology = null;
			Entity metagenomic = null;
			Entity metagenomic_enzymes = null;
			Entity metagenomic_pathways = null;
			Entity metagenomic_taxonomy = null;
			//			Entity tf = null;
			//			Entity enzirg = null;
			//			Entity ti = null;
			//			Entity tu = null;
			//			Entity promoter = null;
			Connection connection = new Connection(mysqlMutithread);

			for(int i=0;i<tables.length;i++) {

				String[] meta = mysqlMutithread.getMeta(tables[i], connection);
				Table table = new Table(tables[i], meta, connection);
				existingTables.addToList(table);

				if(tables[i].equals("gene")) {

					gene = new Genes(table, "Genes", database.getUltimlyComplexComposedBy());
				}
				else if(tables[i].equals("protein")) {

					protein = new Proteins(table, "Proteins", database.getUltimlyComplexComposedBy());
				}
				else if(tables[i].equals("enzyme")) {

					enzyme = new EnzymesContainer(table, "Enzymes", database.getUltimlyComplexComposedBy());
					
					if(this.isMetagenomicProject) {
						metagenomic = new MetagenomicContainer(table, "Metagenomics");
						metagenomic_taxonomy = new TaxonomyMetaContainer(table, "Taxonomy");
						metagenomic_enzymes = new EnzymesMetaContainer(table, "Enzymes",(TaxonomyMetaContainer)metagenomic_taxonomy);
						metagenomic_pathways = new PathwaysMetaContainer(table, "Pathways", (EnzymesMetaContainer) metagenomic_enzymes);
					}
				}
				else if(tables[i].equals("compound")) {

					compound = new Compounds(table, "Metabolites");
					//				enzirg = new EnzymeRegulator_UNUSED(dbt, "Enzymatic regulators");
					//				ti = new EnzimeInhibiter_UNUSED(dbt, "Effectors");
				}
				else if(tables[i].equals("stoichiometry")) {

					repro = new ReagentProducts(table, "Reagents/Products");

					compoundsReactions = new CompoundsReactions(table,"Compounds/Reactions");

				}
				else if(tables[i].equals("reaction")) {

					reaction = new  ReactionsContainer(table, "Reactions");
				}
				//			else if(tables[i].equals("regulatory_event"))
				//			{
				//				tf = new  TranscriptionFactor(dbt, "Transcription factors");
				//			}
				//			else if(tables[i].equals("sigma_promoter"))
				//			{
				//			}
				else if(tables[i].equals("pathway")) {

					path = new  Pathway(table, "Pathways");
				}
				//			else if(tables[i].equals("transcription_unit"))
				//			{
				//				tu = new TranscriptionUnit(dbt, "Transcription Units");
				//			}
				//			else if(tables[i].equals("promoter"))
				//			{
				//				promoter = new Promoter(dbt, "Promoters");
				//			}

				if(tables[i].toLowerCase().equalsIgnoreCase("geneblast") || tables[i].equalsIgnoreCase("geneHomology")) {

					homology = new HomologyDataContainer(table, "Homology Genes");

				}
			}

			//		compoundReactions = new CompoundReactions(dbt);

			database.setTables(existingTables);

			ArrayList<Entity> entis = new ArrayList<Entity>();

			if(gene!=null) {

				ArrayList<Entity> subs = new ArrayList<Entity>();
				//		if(tu!=null) subs.add(tu);
				//		if(promoter!=null) subs.add(promoter);

				if(subs!=null) {

					gene.setSubenties(subs);
				}
				entis.add(gene);
			}

			if(protein!=null) {

				ArrayList<Entity> subs = new ArrayList<Entity>();

				if(enzyme!=null) {

					subs.add(enzyme);
				}
				//		if(tf!=null) subs.add(tf);
				if(subs!=null) {

					protein.setSubenties(subs);
				}
				if(protein!=null) {

					entis.add(protein);
				}
			}

			if(compound!=null) {

				ArrayList<Entity> subs = new ArrayList<Entity>();
				//		if(enzirg!=null) subs.add(enzirg);
				//		if(ti!=null) subs.add(ti);

				if(repro!=null) {

					subs.add(repro);
				}

				if(compoundsReactions!=null) {

					subs.add(compoundsReactions);
				}

				if(subs!=null) {

					compound.setSubenties(subs);
				}
				entis.add(compound);
			}

			if(reaction!=null) {

				entis.add(reaction);
			}

			if(path!=null) {

				entis.add(path);
			}

			if(homology!=null) {

				entis.add(homology);	
			}
			
			if(this.isMetagenomicProject){
				ArrayList<Entity> subs = new ArrayList<Entity>();
				subs.add(metagenomic_taxonomy);
				subs.add(metagenomic_enzymes);
				subs.add(metagenomic_pathways);		
				
				if(subs !=null) metagenomic.setSubenties(subs);
				if(metagenomic!=null) entis.add(metagenomic);
			}

			
			Entities ents = new Entities();

			ents.setEnties(entis);

			database.setEnts(ents);

			Project project = new Project(database, this.name);

			project.setOldPID(this.oldPID);

			project.setMysqlPID(this.mysqlPID);

			project.setGenomeCodeName(this.genomeID);

			project.setNCBIGenome(this.isNCBIGenome);
			
			project.setMetagenomicProject(this.isMetagenomicProject);
			
			project.setSW_TransportersSearch(ProjectUtils.isSW_TransportersSearch(connection));

			project.setTransporterLoaded(ProjectUtils.isTransporterLoaded(connection));

			//			project.setCompartmentsLoaded(Project_Utils.findComparmtents(connection));
			//
			//			project.setGeneDataAvailable(Project_Utils.isDatabaseGenesDataLoaded(connection));
			//			
			//			project.setMetabolicDataAvailable(Project_Utils.isMetabolicDataLoaded(connection));
			//			
			//			project.setCompartmentalisedModel(Project_Utils.isCompartmentalisedModel(connection));

			List<Entity> allentities = new ArrayList<Entity>();
			allentities.add(gene);
			allentities.add(protein);
			allentities.add(enzyme);
			allentities.add(compound);
			allentities.add(reaction);
			allentities.add(path);
			allentities.add(repro);
			allentities.add(compoundsReactions);
			allentities.add(homology);
			
			allentities.add(metagenomic);
			allentities.add(metagenomic_enzymes);
			allentities.add(metagenomic_pathways);
			allentities.add(metagenomic_taxonomy);
			
			for(Entity ent : allentities) {

				if(ent != null) {

					ent.setProject(project);
				}
			}
			
			if(this.isFaaFastaFiles || this.isFnaFastaFiles)
				project.setProjectID(ProjectUtils.getProjectID(project));

			project.setFaaFiles(isFaaFastaFiles);
			project.setFnaFiles(isFnaFastaFiles);
			
			if(isNCBIGenome) {
				
				if(isFaaFastaFiles)
					this.taxonomyID = Long.parseLong(ProjectUtils.getGenomeID(CreateGenomeFile.getGenomeFromID(genomeID, ".faa")));
				else if(isFnaFastaFiles)
					this.taxonomyID = Long.parseLong(ProjectUtils.getGenomeID(CreateGenomeFile.getGenomeFromID(genomeID, ".fna")));
			}
			
			if(this.taxonomyID>0) {

				project.setTaxonomyID(this.taxonomyID);
				String [] orgData =  NcbiAPI.ncbiNewTaxID(this.taxonomyID); 
				project.setOrganismName(orgData[0]);
				project.setOrganismLineage(orgData[1]);
			}

			return project;
		}
		catch (Exception e) {e.printStackTrace();}
		return null;

	}

	/**
	 * @param name
	 * @return
	 */
	private String buildName(String name) {

		Project.setCounter(Project.getCounter()+1);
		name="Project_"+Project.getCounter();
		return name;
	}
}
