/**
 * 
 */
package remote.loader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQL_Utilities;
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
public class KEGG_loader {

	private Connection connection;
	private int compartmentID;
	private ConcurrentHashMap<String,Integer> genes_id, metabolites_id, chromosome_id, proteins_id, reactions_id, pathways_id, modules_id,
	orthologues_id, similar_metabolites_to_load;
	private ConcurrentLinkedQueue<String> enzymesInModel;
	private ConcurrentHashMap<Integer,Set<String>> reactionsPathway, metabolitesPathway, modulesPathway;
	private ConcurrentHashMap<String,Set<String>> enzymesPathway;
	private ConcurrentLinkedQueue<Integer> reactionsPathwayList, metabolitesPathwayList, modulesPathwayList;
	private ConcurrentLinkedQueue<String> compoundsWithBiologicalRoles, enzymesPathwayList;
	private ConcurrentHashMap<String,List<String>> genes_modules_id;


	/**
	 * @param databaseInitialData
	 * @param compoundsWithBiologicalRoles
	 * @throws SQLException
	 */
	public KEGG_loader(DatabaseInitialData databaseInitialData, ConcurrentLinkedQueue<String> compoundsWithBiologicalRoles) throws SQLException {

		this.enzymesInModel = databaseInitialData.getEnzymesInModel();
		this.genes_id= databaseInitialData.getGenes_id();
		this.chromosome_id=databaseInitialData.getChromosome_id();
		this.metabolites_id=databaseInitialData.getMetabolites_id();
		this.proteins_id=databaseInitialData.getProteins_id();
		this.reactions_id=databaseInitialData.getReactions_id();
		this.pathways_id=databaseInitialData.getPathways_id();
		this.modules_id=databaseInitialData.getModules_id();
		this.orthologues_id=databaseInitialData.getOrthologues_id();
		this.reactionsPathway=databaseInitialData.getReactionsPathway();
		this.enzymesPathway=databaseInitialData.getEnzymesPathway();
		this.metabolitesPathway=databaseInitialData.getMetabolitesPathway();
		this.modulesPathway=databaseInitialData.getModulesPathway();
		this.similar_metabolites_to_load=databaseInitialData.getSimilar_metabolites();
		this.reactionsPathwayList=databaseInitialData.getReactionsPathwayList();
		this.enzymesPathwayList=databaseInitialData.getEnzymesPathwayList();
		this.modulesPathwayList=databaseInitialData.getModulesPathwayList();
		this.metabolitesPathwayList=databaseInitialData.getMetabolitesPathwayList();
		this.setCompoundsWithBiologicalRoles(compoundsWithBiologicalRoles);

		this.genes_modules_id = new ConcurrentHashMap<String, List<String>>();
		this.compartmentID = -1;
	}

	/**
	 * @param gene_entry
	 * @throws SQLException
	 */
	public void loadGene(Gene_entry gene_entry) throws SQLException {

		Statement stmt = this.connection.createStatement();

		int chromosome_idchromosome, gene_id;
		ResultSet rs;

		String chromosome;
		if(gene_entry.getChromosome_name()!=null) {

			chromosome = MySQL_Utilities.mysqlStrConverter(gene_entry.getChromosome_name());
		}
		else {

			chromosome = "none";
		}

		if(!this.chromosome_id.containsKey(chromosome)) {

			stmt.execute("INSERT INTO chromosome(name) VALUES('"+chromosome+"')");
			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
			this.chromosome_id.put(chromosome,rs.getInt(1));
		}

		chromosome_idchromosome= this.chromosome_id.get(chromosome);

		if(!this.genes_id.containsKey(gene_entry.getEntry())) {

			if(gene_entry.getName()!=null) {

				stmt.execute("INSERT INTO gene(locusTag, name, chromosome_idchromosome, origin) VALUES('"+gene_entry.getEntry()+"','"+MySQL_Utilities.mysqlStrConverter(gene_entry.getName())+"',"+chromosome_idchromosome+",'KEGG')");	
			}
			else {

				stmt.execute("INSERT INTO gene(locusTag, chromosome_idchromosome, origin) VALUES('"+gene_entry.getEntry()+"', "+chromosome_idchromosome+",'KEGG')");
			}
			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
			this.genes_id.put(gene_entry.getEntry(),rs.getInt(1));

			if(gene_entry.getModules()!=null) {

				this.genes_modules_id.put(gene_entry.getEntry(),gene_entry.getModules());
			}
		}
		//gene_id= rs.getInt(1);
		gene_id= this.genes_id.get(gene_entry.getEntry());

		if(gene_entry.getLeft_end_position()!=null && gene_entry.getRight_end_position()!=null)
		{
			stmt.execute("UPDATE gene SET left_end_position='"+MySQL_Utilities.mysqlStrConverter(gene_entry.getLeft_end_position())+"', right_end_position='"+MySQL_Utilities.mysqlStrConverter(gene_entry.getRight_end_position())+"' WHERE idgene="+gene_id);
		}

		if(gene_entry.getDblinks()!=null)
		{
			for(String dbLink:gene_entry.getDblinks())
			{
				String database = dbLink.split(":")[0], link = dbLink.split(":")[1];
				rs = stmt.executeQuery("SELECT internal_id FROM dblinks WHERE class='g' AND internal_id="+gene_id+" AND external_database='"+database+"'");
				if(!rs.next())
				{
					stmt.execute("INSERT INTO dblinks(class,internal_id,external_database,external_id) VALUES('g',"+gene_id+",'"+database+"','"+link+"')");
				}
			}
		}

		if(gene_entry.getNames()!=null)
		{	
			for(String synonym:gene_entry.getNames())
			{
				rs = stmt.executeQuery("SELECT entity FROM aliases WHERE class='g' AND entity="+gene_id+" AND alias='"+MySQL_Utilities.mysqlStrConverter(synonym)+"'");
				if(!rs.next())
				{
					stmt.execute("INSERT INTO aliases(class,entity,alias) VALUES('g',"+gene_id+",'"+MySQL_Utilities.mysqlStrConverter(synonym)+"')");
				}
			}
		}


		if(gene_entry.getAasequence()!=null)
		{
			rs = stmt.executeQuery("SELECT gene_idgene FROM sequence WHERE gene_idgene="+gene_id+" AND sequence_type='aa'");
			if(!rs.next())
			{
				stmt.execute("INSERT INTO sequence(gene_idgene,sequence_type,sequence,sequence_length) VALUES("+gene_id+",'aa','"+gene_entry.getAasequence()+"',"+gene_entry.getAalength()+")");
			}
		}
		if(gene_entry.getNtsequence()!=null)
		{
			rs = stmt.executeQuery("SELECT gene_idgene FROM sequence WHERE gene_idgene="+gene_id+" AND sequence_type='nt'");
			if(!rs.next())
			{
				stmt.execute("INSERT INTO sequence(gene_idgene,sequence_type,sequence,sequence_length) VALUES("+gene_id+", 'nt','"+gene_entry.getNtsequence()+"',"+gene_entry.getNtlength()+")");
			}
		}

		if(gene_entry.getOrthologues()!=null)
		{
			for(String orthologue:gene_entry.getOrthologues())
			{
				if(!this.orthologues_id.containsKey(orthologue))
				{
					stmt.execute("INSERT INTO orthology (entry_id) VALUES('"+orthologue+"')");
					rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
					rs.next();
					this.orthologues_id.put(orthologue,rs.getInt(1));
				}

				rs = stmt.executeQuery("SELECT * FROM gene_has_orthology WHERE gene_idgene="+gene_id+" AND orthology_id="+this.orthologues_id.get(orthologue));
				if(!rs.next())
				{
					stmt.execute("INSERT INTO gene_has_orthology (gene_idgene, orthology_id) VALUES("+gene_id+","+this.orthologues_id.get(orthologue)+")");
				}
			}
		}
		stmt.close();
	}

	/**
	 * @param metabolite_entry
	 * @throws SQLException
	 */
	public void loadMetabolite(Metabolite_entry metabolite_entry) throws SQLException {

		Statement stmt = this.connection.createStatement();		

		String entry_type = null;
		if(metabolite_entry.getEntry().startsWith("C"))
		{entry_type="COMPOUND";}
		if(metabolite_entry.getEntry().startsWith("G"))
		{entry_type="GLYCAN";}
		if(metabolite_entry.getEntry().startsWith("D"))
		{entry_type="DRUGS";}
		if(metabolite_entry.getEntry().startsWith("B"))
		{entry_type="BIOMASS";}

		ResultSet rs;
		if(!this.metabolites_id.containsKey(metabolite_entry.getEntry())) {

			stmt.execute("INSERT INTO compound(entry_type,kegg_id) VALUES('"+entry_type+"','"+metabolite_entry.getEntry()+"')");	
			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
			this.metabolites_id.put(metabolite_entry.getEntry(), rs.getInt(1));

			if(metabolite_entry.getName()!=null) {

				stmt.execute("UPDATE compound SET name='"+MySQL_Utilities.mysqlStrConverter(metabolite_entry.getName())+"' WHERE idcompound="+this.metabolites_id.get(metabolite_entry.getEntry()));
			}

			if(metabolite_entry.getFormula()!=null) {

				stmt.execute("UPDATE compound SET formula='"+MySQL_Utilities.mysqlStrConverter(metabolite_entry.getFormula())+"' WHERE idcompound="+this.metabolites_id.get(metabolite_entry.getEntry()));
			}

			if(metabolite_entry.getMolecular_weight()!=null) {

				stmt.execute("UPDATE compound SET molecular_weight='"+MySQL_Utilities.mysqlStrConverter(metabolite_entry.getMolecular_weight())+"' WHERE idcompound="+this.metabolites_id.get(metabolite_entry.getEntry()));
			}

			if(this.getCompoundsWithBiologicalRoles().contains(metabolite_entry.getEntry())) {

				stmt.execute("UPDATE compound SET hasBiologicalRoles=true WHERE idcompound="+this.metabolites_id.get(metabolite_entry.getEntry()));
			}
		}
		//		int metabolite_id = rs.getInt(1);
		int metabolite_id = this.metabolites_id.get(metabolite_entry.getEntry());

		if(metabolite_entry.getDblinks()!=null) {

			for(String dbLink:metabolite_entry.getDblinks()) {

				String database = dbLink.split(":")[0], link = dbLink.split(":")[1];
				rs = stmt.executeQuery("SELECT internal_id FROM dblinks WHERE class='c' AND internal_id="+metabolite_id+" AND external_database='"+database+"'");

				if(!rs.next()) {

					stmt.execute("INSERT INTO dblinks(class,internal_id,external_database,external_id) VALUES('c',"+metabolite_id+",'"+database+"','"+link+"')");
				}
			}
		}

		//		rs = stmt.executeQuery("SELECT similar_metabolite_id FROM same_as WHERE similar_metabolite_id="+metabolite_entry.getEntry());
		//		while(rs.next())
		//		{
		//			stmt.execute("UPDATE same_as SET similar_metabolite_id ="+metabolite_id+" WHERE similar_metabolite_id="+metabolite_entry.getEntry());
		//		}

		for(String same_as:metabolite_entry.getSame_as()) {

			if(metabolites_id.contains(same_as)) {

				rs = stmt.executeQuery("SELECT metabolite_id, similar_metabolite_id FROM same_as WHERE metabolite_id="+metabolite_id+" AND similar_metabolite_id="+metabolites_id.get(same_as));
				boolean firstCase = !rs.next();

				rs = stmt.executeQuery("SELECT metabolite_id, similar_metabolite_id FROM same_as WHERE metabolite_id="+metabolites_id.get(same_as)+" AND similar_metabolite_id="+metabolite_id);
				boolean secondCase = !rs.next();

				if(firstCase && secondCase) {

					stmt.execute("INSERT INTO same_as (metabolite_id, similar_metabolite_id) VALUES("+metabolite_id+","+metabolites_id.get(same_as)+")");
				}
			}
			else {

				similar_metabolites_to_load.put(same_as,metabolite_id);
			}
		}

		if(similar_metabolites_to_load.keySet().contains(metabolite_entry.getEntry())) {

			int original_metabolite_id=similar_metabolites_to_load.get(metabolite_entry.getEntry());

			rs = stmt.executeQuery("SELECT metabolite_id, similar_metabolite_id FROM same_as WHERE metabolite_id="+original_metabolite_id+" AND similar_metabolite_id="+metabolite_id);
			boolean firstCase = !rs.next();

			rs = stmt.executeQuery("SELECT metabolite_id, similar_metabolite_id FROM same_as WHERE metabolite_id="+metabolite_id+" AND similar_metabolite_id="+original_metabolite_id);
			boolean secondCase = !rs.next();

			if(firstCase && secondCase) {

				stmt.execute("INSERT INTO same_as (metabolite_id, similar_metabolite_id) VALUES("+original_metabolite_id+","+metabolite_id+")");
			}
			similar_metabolites_to_load.remove(metabolite_entry.getEntry());	
		}

		if(metabolite_entry.getNames() != null) {

			for(String synonym:metabolite_entry.getNames()) {

				rs = stmt.executeQuery("SELECT entity FROM aliases WHERE class='c' AND entity="+metabolite_id+" AND alias='"+MySQL_Utilities.mysqlStrConverter(synonym)+"'");
				if(!rs.next())
				{
					stmt.execute("INSERT INTO aliases(class,entity,alias) VALUES('c',"+metabolite_id+",'"+MySQL_Utilities.mysqlStrConverter(synonym)+"')");
				}
			}
		}

		if(metabolite_entry.getPathways()!=null) {

			this.metabolitesPathway.put(metabolite_id, metabolite_entry.getPathways().keySet());
			this.metabolitesPathwayList.add(metabolite_id);
		}

		stmt.close();
	}

	/**
	 * @param enzyme_entry
	 * @throws SQLException
	 */
	public void loadProtein(Enzyme_entry enzyme_entry) throws SQLException {

		ResultSet rs = null;
		Statement stmt = this.connection.createStatement();
		int enzymeClass = Integer.valueOf(enzyme_entry.getEntry().substring(0,1));

		if(enzyme_entry.getName()!=null) {

			String protein_name = enzyme_entry.getName();
			//			if(enzyme_entry.getName().startsWith("Transferred to"))
			//			{
			//				System.out.println("TRANSFERRED "+enzyme_entry.getEntry());
			//			}
			//			else if(enzyme_entry.getName().startsWith("Deleted entry"))
			//			{
			//				System.out.println("Deleted "+enzyme_entry.getEntry());
			//			}
			//			else
			{

				if(!this.proteins_id.containsKey(enzyme_entry.getEntry())) {

					stmt.execute("INSERT INTO protein(name,class) VALUES('"+MySQL_Utilities.mysqlStrConverter(protein_name)+"','"+getEnzymeClass(enzymeClass)+"')");	
					rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
					rs.next();
					this.proteins_id.put(enzyme_entry.getEntry(), rs.getInt(1));
					//}
					//rs = stmt.executeQuery("SELECT ecnumber FROM enzyme WHERE protein_idprotein="+protein_id+" AND ecnumber='"+enzyme_entry.getEntry()+"'");
					//if(!rs.next())
					//{
					boolean inModel=false;

					if(enzyme_entry.getGenes()!=null) {

						inModel=true;
						this.enzymesInModel.add(enzyme_entry.getEntry());
					}
					stmt.execute("INSERT INTO enzyme(protein_idprotein,ecnumber,inModel, source) VALUES ("+this.proteins_id.get(enzyme_entry.getEntry())+",'"+enzyme_entry.getEntry()+"',"+inModel+", 'KEGG')");
				}

				int protein_id = this.proteins_id.get(enzyme_entry.getEntry());

				if(enzyme_entry.getGenes()!=null) {

					for(String gene:enzyme_entry.getGenes()) {

						if(!this.genes_id.containsKey(gene)) {

							rs = stmt.executeQuery("SELECT idgene from gene WHERE locusTag='"+gene+"'");
							if(rs.next()) {
								this.genes_id.put(gene,rs.getInt(1));
							}
						}

						if(this.genes_modules_id.contains(gene)) {


							for (String module : this.genes_modules_id.get(gene)) {

								rs = stmt.executeQuery("SELECT enzyme_protein_idprotein from subunit WHERE enzyme_protein_idprotein="+protein_id+" " +
										"AND gene_idgene="+this.genes_id.get(gene)+" AND enzyme_ecnumber='"+enzyme_entry.getEntry()+"'"+" AND module_id='"+this.modules_id.get(module)+"'");

								if(!rs.next()) {

									stmt.execute("INSERT INTO subunit (enzyme_protein_idprotein,gene_idgene,enzyme_ecnumber,module_id) " +
											"VALUES ("+protein_id+","+this.genes_id.get(gene)+",'"+enzyme_entry.getEntry()+"','"+this.modules_id.get(module)+"')");
								}
							}
						}
						else {
							rs = stmt.executeQuery("SELECT enzyme_protein_idprotein from subunit WHERE enzyme_protein_idprotein="+protein_id+" " +
									"AND gene_idgene="+this.genes_id.get(gene)+" AND enzyme_ecnumber='"+enzyme_entry.getEntry()+"'");

							if(!rs.next()) {

								stmt.execute("INSERT INTO subunit (enzyme_protein_idprotein,gene_idgene,enzyme_ecnumber) " +
										"VALUES ("+protein_id+","+this.genes_id.get(gene)+",'"+enzyme_entry.getEntry()+"')");
							}
						}
					}
				}

				if(enzyme_entry.getCofactors()!=null) {

					for(String cofactor_string:enzyme_entry.getCofactors()) {

						if(!this.metabolites_id.containsKey(cofactor_string)) {

							rs = stmt.executeQuery("SELECT idcompound from compound WHERE kegg_id='"+cofactor_string+"'");
							rs.next();
							this.metabolites_id.put(cofactor_string,rs.getInt(1));
						}

						rs = stmt.executeQuery("SELECT protein_idprotein from enzymatic_cofactor WHERE protein_idprotein="+protein_id+" AND compound_idcompound="+this.metabolites_id.get(cofactor_string));

						if(!rs.next()) {

							stmt.execute("INSERT INTO enzymatic_cofactor (protein_idprotein,compound_idcompound) VALUES ("+protein_id+","+this.metabolites_id.get(cofactor_string)+")");
						}
					}
				}

				if(enzyme_entry.getDblinks()!=null) {

					for(String dbLink:enzyme_entry.getDblinks()) {

						String database = dbLink.split(":")[0], link = dbLink.split(":")[1];
						rs = stmt.executeQuery("SELECT internal_id FROM dblinks WHERE class='p' AND internal_id="+protein_id+" AND external_database='"+database+"'");
						if(!rs.next()) {

							stmt.execute("INSERT INTO dblinks(class,internal_id,external_database,external_id) VALUES('p',"+protein_id+",'"+database+"','"+link+"')");
						}
					}
				}

				if(enzyme_entry.getNames()!=null) {

					for(String synonym:enzyme_entry.getNames()) {

						rs = stmt.executeQuery("SELECT entity FROM aliases WHERE class='p' AND entity="+protein_id+" AND alias='"+MySQL_Utilities.mysqlStrConverter(synonym)+"'");

						if(!rs.next()) {
							stmt.execute("INSERT INTO aliases(class,entity,alias) VALUES('p',"+protein_id+",'"+MySQL_Utilities.mysqlStrConverter(synonym).replace(";", "")+"')");
						}
					}
				}

				if(enzyme_entry.getPathways()!=null) {

					this.enzymesPathway.put(enzyme_entry.getEntry(), enzyme_entry.getPathways().keySet());
					this.enzymesPathwayList.add(enzyme_entry.getEntry());
				}

			}
			stmt.close();
		}
		else {

			//System.out.println("NULL NAME "+enzyme_entry.getEntry());
			rs = stmt.executeQuery("SELECT idprotein FROM protein WHERE name='-' AND class='"+getEnzymeClass(enzymeClass)+"'");
			if(!rs.next()) {

				stmt.execute("INSERT INTO protein(name,class) VALUES('-','"+getEnzymeClass(enzymeClass)+"')");	
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
			}

			this.proteins_id.put(enzyme_entry.getEntry(), rs.getInt(1));
			rs = stmt.executeQuery("SELECT * FROM enzyme WHERE protein_idprotein="+this.proteins_id.get(enzyme_entry.getEntry())+" AND ecnumber='"+enzyme_entry.getEntry()+"'");
			if(!rs.next()) {

				stmt.execute("INSERT INTO enzyme(protein_idprotein,ecnumber,inModel, source) VALUES ("+this.proteins_id.get(enzyme_entry.getEntry())+",'"+enzyme_entry.getEntry()+"',"+false+",'KEGG')");
			}
			rs.close();
			stmt.close();
		}
	}

	/**
	 * @param reaction_entry
	 * @throws SQLException
	 */
	public void loadReaction(Reaction_entry reaction_entry) throws SQLException {

		Statement stmt = this.connection.createStatement();

		if (this.compartmentID<0) {
			stmt.execute("LOCK TABLES compartment WRITE");
			ResultSet rs = stmt.executeQuery("SELECT idcompartment FROM compartment WHERE name ='inside'");

			if(!rs.next()) {

				stmt.execute("INSERT INTO compartment (name, abbreviation) VALUES('inside','in')");
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
			}
			this.compartmentID = rs.getInt(1);
			stmt.execute("UNLOCK TABLES");
		}

		Map<String,String[]> results = new HashMap<String,String[]>();

		if(reaction_entry.getReactantsStoichiometry()!=null) {

			//results.putAll(reaction_entry.getReactantsStoichiometry());

			for(String reactant : reaction_entry.getReactantsStoichiometry().keySet()) {

				results.put("-"+reactant,reaction_entry.getReactantsStoichiometry().get(reactant));		
			}
		}

		if(reaction_entry.getProductsStoichiometry()!=null) {

			results.putAll(reaction_entry.getProductsStoichiometry());
		}

		boolean go = true;
		for(String metabolite: results.keySet()) {

			if(!this.metabolites_id.containsKey(metabolite.replace("-", ""))) {

				go = false;
				System.out.println(metabolite);
				System.out.println(this.metabolites_id);
			}
		}

		if(go) {

			boolean inModel = false;

			for(String enzyme : reaction_entry.getEnzymes()) {

				if(this.enzymesInModel.contains(enzyme)) {

					if(reaction_entry.getPathways()== null) {

						inModel=true;
					}
					else {

						if(this.enzymesPathway.containsKey(enzyme)) {

							for(String path : reaction_entry.getPathways().keySet()) {

								if(this.enzymesPathway.get(enzyme).contains(path)) {

									inModel=true;
								}
							}
						}
					}
				}
			}
			ResultSet rs;

			if (reaction_entry.isSpontaneous()) {

				if(!this.pathways_id.contains("SPONT")) {

					rs = stmt.executeQuery("SELECT * FROM pathway WHERE name='Spontaneous' AND code='SPONT'");
					if(!rs.next()) {

						stmt.execute("INSERT INTO pathway (name,code) VALUES('Spontaneous','SPONT')");
						rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
						rs.next();
					}
					this.pathways_id.put("SPONT", rs.getInt(1));
				}

				Map<String,String> reaction_pathways;

				if(reaction_entry.getPathways()==null) { 

					reaction_pathways = new HashMap<String, String>();
				}
				else {

					reaction_pathways = reaction_entry.getPathways();
				}
				reaction_pathways.put("SPONT","Spontaneous");
				reaction_entry.setPathways(reaction_pathways);

				inModel = true;
			} 

			if(reaction_entry.isNon_enzymatic()) {

				if(!this.pathways_id.contains("NOENZ")) {

					rs = stmt.executeQuery("SELECT * FROM pathway WHERE name='Non enzymatic' AND code='NOENZ'");
					if(!rs.next()) {

						stmt.execute("INSERT INTO pathway (name,code) VALUES('Non enzymatic','NOENZ')");
						rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
						rs.next();
					}
					this.pathways_id.put("NOENZ", rs.getInt(1));
				}

				Map<String,String> reaction_pathways;

				if(reaction_entry.getPathways()==null) { 

					reaction_pathways = new HashMap<String, String>();
				}
				else {

					reaction_pathways = reaction_entry.getPathways();
				}
				reaction_pathways.put("NOENZ","Non enzymatic");
				reaction_entry.setPathways(reaction_pathways);

				inModel = true;
			}

			if(!this.reactions_id.containsKey(reaction_entry.getEntry())) {

				stmt.execute("INSERT INTO reaction (name,equation,reversible,inModel,isSpontaneous,isNonEnzymatic,isGeneric,source, originalReaction, compartment_idcompartment) " +
						"VALUES('"+MySQL_Utilities.mysqlStrConverter(reaction_entry.getEntry())+
						"','"+MySQL_Utilities.mysqlStrConverter(reaction_entry.getEquation())+"',true,"+inModel+","+reaction_entry.isSpontaneous()+","+reaction_entry.isNon_enzymatic()+","+reaction_entry.isGeneric()+",'KEGG',true,"+this.compartmentID+")");
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
				this.reactions_id.put(reaction_entry.getEntry(),rs.getInt(1));
			}

			int reaction_id = this.reactions_id.get(reaction_entry.getEntry());

			for(String metabolite: results.keySet()) {

				if(this.metabolites_id.containsKey(metabolite.replace("-", ""))) {

					int metabolite_id = this.metabolites_id.get(metabolite.replace("-", ""));
					rs = stmt.executeQuery("SELECT * FROM stoichiometry" +
							" WHERE compound_idcompound="+ metabolite_id +
							" AND reaction_idreaction="+reaction_id+"" +
							" AND compartment_idcompartment = "+this.compartmentID +
							" AND stoichiometric_coefficient = '"+results.get(metabolite)[0]+"'" +
							" AND numberofchains = '"+results.get(metabolite)[1]+"'");

					if(!rs.next()) {

						stmt.execute("INSERT INTO stoichiometry(reaction_idreaction,compound_idcompound,compartment_idcompartment,stoichiometric_coefficient,numberofchains) " +
								"VALUES("+reaction_id+","+metabolite_id+","+this.compartmentID+",'"+results.get(metabolite)[0]+"','"+results.get(metabolite)[1]+"')");
					} 
				}
			}


			if(reaction_entry.getEnzymes()!=null) {

				for(String enzymes:reaction_entry.getEnzymes()) {

					if(!enzymes.contains("-")) {

						int protein_id=this.proteins_id.get(enzymes);

						rs = stmt.executeQuery("SELECT * " +
								"FROM reaction_has_enzyme " +
								"WHERE enzyme_protein_idprotein="+protein_id+" " +
								"AND enzyme_ecnumber='"+enzymes+"' " +
								"AND reaction_idreaction="+reaction_id+" ");

						if(!rs.next()) {

							stmt.execute("INSERT INTO reaction_has_enzyme(enzyme_protein_idprotein,enzyme_ecnumber,reaction_idreaction) " +
									"VALUES("+protein_id+",'"+enzymes+"',"+reaction_id+")");
						}
					}
				}
			}

			if(reaction_entry.getNames()!=null) {

				for(String synonym:reaction_entry.getNames()) {

					rs = stmt.executeQuery("SELECT entity " +
							"FROM aliases " +
							"WHERE class='r' " +
							"AND entity='"+reaction_id+"' " +
							"AND alias='"+MySQL_Utilities.mysqlStrConverter(synonym)+"'");

					if(!rs.next()) {

						stmt.execute("INSERT INTO aliases(class,entity,alias) " +
								"VALUES('r',"+reaction_id+",'"+MySQL_Utilities.mysqlStrConverter(synonym)+"')");
					}
				}
			}

			if(reaction_entry.getPathways()!=null) {

				this.reactionsPathway.put(reaction_id, reaction_entry.getPathways().keySet());
				this.reactionsPathwayList.add(reaction_id);
			}
		}
		else {

			System.out.println("\t reaction "+reaction_entry.getEntry()+" has unexisting metabolites.");
		}
		stmt.close();
	}

	/**
	 * @param module_entry
	 * @throws SQLException
	 */
	public void loadModule(Module_entry module_entry) throws SQLException{

		Statement stmt = this.connection.createStatement();		

		ResultSet rs;

		if(!this.modules_id.containsKey(module_entry.getEntry())) {

			stmt.execute("INSERT INTO module (id,type) VALUES('"+module_entry.getEntry()+"','"+module_entry.getModuleType()+"')");
			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
			this.modules_id.put((module_entry.getEntry()),rs.getInt(1));

			if(module_entry.getName()!=null) {

				stmt.execute("UPDATE module SET name='"+MySQL_Utilities.mysqlStrConverter(module_entry.getName())+"'" +
						" WHERE id="+this.modules_id.get(module_entry.getEntry()));
			}

			if(module_entry.getStoichiometry()!=null) {

				stmt.execute("UPDATE module SET stoichiometry='"+MySQL_Utilities.mysqlStrConverter(module_entry.getStoichiometry())+"'" +
						" WHERE id="+this.modules_id.get(module_entry.getEntry()));
			}

			if(module_entry.getDefinition()!=null) {

				stmt.execute("UPDATE module SET definition='"+MySQL_Utilities.mysqlStrConverter(module_entry.getDefinition())+"'" +
						" WHERE id="+this.modules_id.get(module_entry.getEntry()));
			}

			if(module_entry.getModuleHieralchicalClass()!=null) {

				stmt.execute("UPDATE module SET hieralchical_class='"+MySQL_Utilities.mysqlStrConverter(module_entry.getModuleHieralchicalClass())+"'" +
						" WHERE id="+this.modules_id.get(module_entry.getEntry()));
			}

		}
		int module_id = this.modules_id.get((module_entry.getEntry()));

		for(String orthologue:module_entry.getOrthologues()) {

			if(!this.orthologues_id.containsKey(orthologue)) {

				stmt.execute("INSERT INTO orthology (entry_id) VALUES('"+orthologue+"')");
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
				this.orthologues_id.put(orthologue,rs.getInt(1));
			}

			rs = stmt.executeQuery("SELECT * FROM module_has_orthology WHERE module_id="+module_id+" AND orthology_id="+this.orthologues_id.get(orthologue));

			if(!rs.next()) {

				stmt.execute("INSERT INTO module_has_orthology (module_id, orthology_id) VALUES("+module_id+","+this.orthologues_id.get(orthologue)+")");
			}
		}

		if(module_entry.getPathways()!=null) {

			this.modulesPathway.put(module_id, module_entry.getPathways().keySet());
			this.modulesPathwayList.add(module_id);
		}
		stmt.close();
	}

	/**
	 * @param pathways_hierarchy
	 * @throws SQLException
	 */
	public void loadPathways(Pathways_hierarchy pathways_hierarchy) throws SQLException{

		Statement stmt = this.connection.createStatement();		

		ResultSet rs;

		rs = stmt.executeQuery("SELECT idpathway from pathway WHERE name='"+pathways_hierarchy.getSuper_pathway()+"'");

		if(!rs.next()) {

			stmt.execute("INSERT INTO pathway (code,name) VALUES('','"+pathways_hierarchy.getSuper_pathway()+"')");
			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
		}

		int super_pathway_id = rs.getInt(1);

		for(String intermediary_pathway: pathways_hierarchy.getPathways_hierarchy().keySet()) {

			rs = stmt.executeQuery("SELECT idpathway from pathway WHERE name='"+intermediary_pathway+"'");

			if(!rs.next()) {

				stmt.execute("INSERT INTO pathway (code,name) VALUES('','"+intermediary_pathway+"')");
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
			}

			int intermediary_pathway_id = rs.getInt(1);

			rs = stmt.executeQuery("SELECT * FROM superpathway WHERE pathway_idpathway="+intermediary_pathway_id+" AND superpathway="+super_pathway_id);

			if(!rs.next()) {

				stmt.execute("INSERT INTO superpathway VALUES("+intermediary_pathway_id+","+super_pathway_id+")");
			}

			for(String[] pathway: pathways_hierarchy.getPathways_hierarchy().get(intermediary_pathway)) {

				if(!this.pathways_id.contains(pathway[0])) {

					rs = stmt.executeQuery("SELECT * FROM pathway WHERE name='"+MySQL_Utilities.mysqlStrConverter(pathway[1])+"' AND code='"+pathway[0]+"'");
					if(!rs.next()) {

						stmt.execute("INSERT INTO pathway (name,code) VALUES('"+MySQL_Utilities.mysqlStrConverter(pathway[1])+"','"+pathway[0]+"')");
						rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
						rs.next();
					}
					this.pathways_id.put(pathway[0], rs.getInt(1));
				}
				int pathway_id = this.pathways_id.get(pathway[0]);

				rs = stmt.executeQuery("SELECT * FROM superpathway WHERE pathway_idpathway="+pathway_id+" AND superpathway="+intermediary_pathway_id);
				if(!rs.next()) {

					stmt.execute("INSERT INTO superpathway VALUES("+pathway_id+","+intermediary_pathway_id+")");
				}
			}
		}
		//}
		stmt.close();
	}

	/**
	 * @param idreaction
	 * @throws SQLException
	 */
	public void load_ReactionsPathway(int idreaction) throws SQLException{
		Statement stmt = this.connection.createStatement();		

		ResultSet rs;

		for(String pathway:this.reactionsPathway.get(idreaction)) {

			if(this.pathways_id.containsKey(pathway)) {

				rs = stmt.executeQuery("SELECT reaction_idreaction FROM pathway_has_reaction WHERE reaction_idreaction="+idreaction+" AND pathway_idpathway="+this.pathways_id.get(pathway));
				if(!rs.next()) {

					stmt.execute("INSERT INTO pathway_has_reaction (reaction_idreaction, pathway_idpathway) VALUES("+idreaction+","+this.pathways_id.get(pathway)+")");
				}
			}
		}
		stmt.close();
	}

	/**
	 * @param ecnumber
	 * @throws SQLException
	 */
	public void load_EnzymesPathway(String ecnumber) throws SQLException{
		Statement stmt = this.connection.createStatement();		
		ResultSet rs;

		for(String pathway:this.enzymesPathway.get(ecnumber)) {

			rs = stmt.executeQuery("SELECT * FROM pathway_has_enzyme WHERE enzyme_protein_idprotein="+this.proteins_id.get(ecnumber)+" AND pathway_idpathway="+this.pathways_id.get(pathway));

			if(!rs.next()) {

				stmt.execute("INSERT INTO pathway_has_enzyme (enzyme_protein_idprotein, pathway_idpathway, enzyme_ecnumber) VALUES("+this.proteins_id.get(ecnumber)+","+this.pathways_id.get(pathway)+",'"+ecnumber+"')");
			}
		}
		stmt.close();
	}

	/**
	 * @param module_id
	 * @throws SQLException
	 */
	public void load_ModulePathway(int module_id) throws SQLException{
		Statement stmt = this.connection.createStatement();		
		ResultSet rs;
		for(String pathway:this.modulesPathway.get(module_id))
		{
			rs = stmt.executeQuery("SELECT module_id FROM pathway_has_module WHERE module_id="+module_id+" and pathway_idpathway="+this.pathways_id.get(pathway));
			if(!rs.next())
			{
				stmt.execute("INSERT INTO pathway_has_module (module_id, pathway_idpathway) VALUES("+module_id+","+this.pathways_id.get(pathway)+")");
			}
		}
		stmt.close();
	}

	/**
	 * @param metabolite_id
	 * @throws SQLException
	 */
	public void load_MetabolitePathway(int metabolite_id) throws SQLException{
		Statement stmt = this.connection.createStatement();		
		ResultSet rs;
		for(String pathway:this.metabolitesPathway.get(metabolite_id)) {

			if(this.pathways_id.contains(pathway)) {
				rs = stmt.executeQuery("SELECT compound_idcompound FROM pathway_has_compound WHERE compound_idcompound="+metabolite_id+" and pathway_idpathway="+this.pathways_id.get(pathway));

				if(!rs.next()) {

					stmt.execute("INSERT INTO pathway_has_compound (compound_idcompound, pathway_idpathway) VALUES("+metabolite_id+","+this.pathways_id.get(pathway)+")");
				}
			}

		}
		stmt.close();
	}

	/**
	 * @param module_id
	 * @throws SQLException
	 */
	public static void build_Views(Connection connection) throws SQLException {

		Statement stmt = connection.createStatement();
		stmt.execute("SELECT * FROM reactions_view;");
		stmt.execute("SELECT * FROM reactions_view_noPath_or_noEC;");
		//stmt.execute("SELECT * FROM sbml_query;");
		stmt.close();
	}

	/**
	 * @param enzyme
	 * @return
	 */
	private static String getEnzymeClass(int enzymeClass){
		String classes = null;

		switch (enzymeClass)  {

		case 1:  classes = "Oxidoreductases";break;
		case 2:  classes = "Transferases";break;
		case 3:  classes = "Hydrolases";break;
		case 4:  classes = "Lyases";break;
		case 5:  classes = "Isomerases";break;
		case 6:  classes = "Ligases";break;
		}
		return classes;
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

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}


}
