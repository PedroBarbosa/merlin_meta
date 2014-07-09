package datatypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.ImageIcon;

import pt.uminho.sysbio.common.bioapis.externalAPI.uniprot.UniProtAPI;
import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import utilities.IntegrationReport;
import datatypes.metabolic_regulatory.HomologyDataContainer;
import es.uvigo.ei.aibench.workbench.Workbench;
import gui.IntegrationConflictsGUI;

/**
 * @author ODias
 *
 * Integrate homology data with the data from the local database
 *
 */
public class IntegrateHomologyData implements IntegrateData {

	private Map<String, String> homologyProduct;
	private Map<String, String> homologyName;
	private Map<String, Set<String>> homologyEnzymes;
	private Map<String, String> integratedProduct;
	private Map<String, String> integratedName;
	private Map<String, Set<String>> integratedEnzymes;
	private Map<String, String> integratedProduct_clone;
	private Map<String, String> integratedName_2;
	private Map<String, Set<String>> integratedEnzymes_clone;	
	private boolean existshomologyInstance;
	private Map<String, String> newNameConflicts;//, newProductsConflicts ;
	private Map<String, String[]> nameConflictsDatabase, nameConflictsHomology;
	//private Map<String, String[]> productConflictsDatabase;
	private Map<String, String> oldLocusNewTag;
	private Map<String, String> chromosome;
	private IntegrationReport iReport;
	private Map<String,List<String>> allGeneNames;
	private Map<String,String> existingChromosome;
	private Map<String,Set<String>> existingECNumbers;
	private Map<String,Set<String>> allPathways;
	private Map<String,List<String>> allProteinNames;
	private Map<String,Set<String>> existsPathway;
	private Connection connection;
	private List<String> homologyLocusTags;
	private IntegrationType integrationNames;
	private IntegrationType integrationProducts;
	private IntegrationType integrationEnzymes;
	private String projectName;
	private boolean isEukaryote;
	private boolean integrateFull, integratePartial;


	/**
	 * @param homologyDataContainer
	 * @param integrationNames
	 * @param integrationEnzymes
	 * @param integrationProducts
	 * @param integrateFull 
	 * @param integratePartial
	 */
	public IntegrateHomologyData(HomologyDataContainer homologyDataContainer, IntegrationType integrationNames, 
			IntegrationType integrationEnzymes , IntegrationType integrationProducts, boolean integrateFull, boolean integratePartial) {

		this.integrateFull = integrateFull;
		this.integratePartial = integratePartial;

		this.integrationNames = integrationNames;
		this.integrationEnzymes = integrationEnzymes;
		this.integrationProducts = integrationProducts;

		this.projectName = homologyDataContainer.getProject().getName();

		this.homologyLocusTags = new ArrayList<String>();

		this.connection = homologyDataContainer.getConnection();

		this.homologyName = new TreeMap<String, String>();
		this.homologyProduct = new TreeMap<String, String>();
		this.homologyEnzymes = new TreeMap<String, Set<String>>();
		this.integratedProduct = new TreeMap<String, String>();
		this.integratedEnzymes = new TreeMap<String, Set<String>>();
		this.integratedName = new TreeMap<String, String>();
		this.integratedProduct_clone = new TreeMap<String, String>();
		this.integratedEnzymes_clone = new TreeMap<String, Set<String>>();
		this.integratedName_2 = new TreeMap<String, String>();
		this.newNameConflicts = new TreeMap<String, String>();
		//this.newProductsConflicts = new TreeMap<String, String>();
		this.chromosome = new TreeMap<String, String>();
		this.isEukaryote = homologyDataContainer.isEukaryote();

		for(Integer key : homologyDataContainer.getIntegrationLocusList().keySet()) {

			if(homologyDataContainer.getIntegrationSelectedGene().containsKey(key) && homologyDataContainer.getIntegrationSelectedGene().get(key)) {

				this.homologyLocusTags.add(homologyDataContainer.getIntegrationLocusList().get(key));

				String locusTag =  homologyDataContainer.getIntegrationLocusList().get(key);

				if(homologyDataContainer.getIntegrationNamesList().containsKey(key)) {

					this.homologyName.put(locusTag,homologyDataContainer.getIntegrationNamesList().get(key));
				}

				if(homologyDataContainer.getIntegrationProdItem().containsKey(key) && !homologyDataContainer.getIntegrationProdItem().get(key).isEmpty()) {

					this.homologyProduct.put(locusTag,homologyDataContainer.getIntegrationProdItem().get(key));
				}

				if(homologyDataContainer.getIntegrationEcItem().containsKey(key) && !homologyDataContainer.getIntegrationEcItem().get(key).isEmpty()) {

					this.homologyEnzymes.put(locusTag, new HashSet<String>(Arrays.asList(homologyDataContainer.getIntegrationEcItem().get(key).split(", "))));
				}

				if(this.isEukaryote && homologyDataContainer.getIntegrationChromosome().containsKey(key)) {

					this.chromosome.put(locusTag, homologyDataContainer.getIntegrationChromosome().get(key));
				}
			}
		}
		this.setExistshomologyInstance(true);
		this.iReport = new IntegrationReport();
	}

	/**
	 * Performs the data integration
	 */
	public boolean performIntegration() {

		Map<String,String> existingNames = new TreeMap<String, String>();
		Map<String, Set<String>> existingGeneNamesAlias = new TreeMap<String, Set<String>>();

		Map<String,String> existingProducts = new TreeMap<String, String>();
		Map<String, Set<String>> existingProductsAlias = new TreeMap<String, Set<String>>();

		this.existingECNumbers = new TreeMap<String, Set<String>>();
		this.existingChromosome = new TreeMap<String, String>();
		this.existsPathway = new TreeMap<String, Set<String>>();
		this.allPathways = new TreeMap<String, Set<String>>();
		Set<String> enzymes  = new TreeSet<String>();

		try {

			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT gene.name, locusTag, chromosome.name FROM gene "+
					"INNER JOIN chromosome ON (idchromosome=chromosome_idchromosome) " +
					"WHERE origin='KEGG'");

			while(rs.next()) {

				String name = "";
				if(rs.getString(1)!=null) {

					name = rs.getString(1);
				}
				existingNames.put(rs.getString(2), name);
				this.existingECNumbers.put(rs.getString(2), new TreeSet<String>());
				existingProducts.put(rs.getString(2), "");
				this.existingChromosome.put(rs.getString(2), rs.getString(3));
			}

			Set<String> alias;
			rs = stmt.executeQuery("SELECT locusTag, alias FROM gene " +
					"INNER JOIN aliases ON (idgene=aliases.entity) " +
					"WHERE class='g'  AND  origin='KEGG'");

			while(rs.next()) {

				alias = new TreeSet<String>();

				if(existingGeneNamesAlias.containsKey(rs.getString(1))) {

					alias = existingGeneNamesAlias.get(rs.getString(1));
				}
				alias.add(rs.getString(2));
				existingGeneNamesAlias.put(rs.getString(1), alias);
			}

			rs = stmt.executeQuery("SELECT locusTag, enzyme_ecNumber FROM gene " +
					"INNER JOIN subunit ON (idgene=gene_idgene) "
					+"WHERE origin='KEGG'");

			while(rs.next()) {

				enzymes  = new TreeSet<String>();

				if(existingECNumbers.containsKey(rs.getString(1))) {

					enzymes = existingECNumbers.get(rs.getString(1));
				}
				enzymes.add(rs.getString(2));
				existingECNumbers.put(rs.getString(1), enzymes);
			}

			rs = stmt.executeQuery("SELECT locusTag, protein.name FROM gene " +
					"INNER JOIN subunit ON (idgene=gene_idgene) " +
					"INNER JOIN protein ON (subunit.enzyme_protein_idprotein=idprotein) "+
					"WHERE origin='KEGG'");

			while(rs.next()) {

				existingProducts.put(rs.getString(1), rs.getString(2));
			}

			rs = stmt.executeQuery("SELECT locusTag, alias FROM gene " +
					"INNER JOIN subunit ON (idgene=gene_idgene) " +
					"INNER JOIN aliases ON (subunit.enzyme_protein_idprotein=aliases.entity)" +
					" WHERE class='p' AND origin='KEGG'");

			while(rs.next()) {

				alias = new TreeSet<String>();

				if(existingProductsAlias.containsKey(rs.getString(1))) {

					alias = existingProductsAlias.get(rs.getString(1));
				}
				alias.add(rs.getString(2));
				existingProductsAlias.put(rs.getString(1), alias);
			}

			rs = stmt.executeQuery("SELECT pathway.idpathway, pathway.name, pathway_has_enzyme.enzyme_ecnumber FROM pathway " +
					"INNER JOIN pathway_has_enzyme ON (pathway.idpathway=pathway_idpathway) " +
					"ORDER BY idpathway");

			//for each enzyme in the pathways
			while(rs.next()) {

				Set<String> enz= new TreeSet<String>();

				if(allPathways.containsKey(rs.getString(2))) {

					enz = allPathways.get(rs.getString(2)); 
				}
				enz.add(rs.getString(3));
				allPathways.put(rs.getString(2), enz);
			}

			rs = stmt.executeQuery("SELECT pathway.idpathway, pathway.name, enzyme.ecnumber FROM pathway " +
					"INNER JOIN pathway_has_enzyme ON (pathway.idpathway=pathway_idpathway) " +
					"INNER JOIN enzyme ON (enzyme.ecnumber=pathway_has_enzyme.enzyme_ecnumber) " +
					"WHERE enzyme.inModel ORDER BY idpathway");

			//for each enzyme in the pathways in the model
			while(rs.next()) {

				Set<String> enz= new TreeSet<String>();

				if(existsPathway.containsKey(rs.getString(2))) {

					enz = existsPathway.get(rs.getString(2)); 
				}
				enz.add(rs.getString(3));
				existsPathway.put(rs.getString(2), enz);
			}
		}
		catch (SQLException e) {

			e.printStackTrace();
			return false;
		}

		if(!this.isEukaryote) {

			this.chromosome = existingChromosome;
		}

		this.nameConflictsDatabase = new TreeMap<String, String[]>();
		this.nameConflictsHomology = new TreeMap<String, String[]>();
		//this.productConflictsDatabase = new TreeMap<String, String[]>();
		//this.productConflictshomology = new TreeMap<String, String[]>();

		//////////////////////////////////////////////////////////////////////////////Pre-process

		this.setNewLocusTags();

		Set<String> oldList = new TreeSet<String>(existingNames.keySet());

		for(String key: oldList) {

			if(this.oldLocusNewTag.containsKey(key)) {

				existingNames.put(this.oldLocusNewTag.get(key), existingNames.get(key));
				existingNames.remove(key);
				this.existingChromosome.put(this.oldLocusNewTag.get(key), this.existingChromosome.get(key));
				this.existingChromosome.remove(key);
			}
		}

		oldList = new TreeSet<String>(existingECNumbers.keySet());
		for(String key: oldList) {

			if(this.oldLocusNewTag.containsKey(key)) {

				existingECNumbers.put(this.oldLocusNewTag.get(key), existingECNumbers.get(key));
				existingECNumbers.remove(key);
			}
		}

		oldList = new TreeSet<String>(existingProducts.keySet());
		for(String key: oldList) {

			if(this.oldLocusNewTag.containsKey(key)) {

				existingProducts.put(this.oldLocusNewTag.get(key), existingProducts.get(key));
				existingProducts.remove(key);
			}
		}

		oldList = new TreeSet<String>(existingProductsAlias.keySet());
		for(String key: oldList) {

			if(this.oldLocusNewTag.containsKey(key)) {

				existingProductsAlias.put(this.oldLocusNewTag.get(key), existingProductsAlias.get(key));
				existingProductsAlias.remove(key);
			}
		}

		/////////////////////////////////////////////////////////////////////////////Genes

		this.compareGenes(existingNames, existingGeneNamesAlias);
		/////////////////////////////////////////////////////////////////////////////EnzymesContainer

		this.compareEnzymes(enzymes);

		/////////////////////////////////////////////////////////////////////////////PRODUCTS

		this.compareProteins(existingProducts, existingProductsAlias);

		return true;
	}

	/**
	 * @param enzymes
	 * @param integrationEnzymes
	 */
	private void compareEnzymes(Set<String> enzymes) {

		for(String key : this.homologyEnzymes.keySet()) {

			boolean exists=false;
			if(existingECNumbers.containsKey(key)) {

				exists=true;
			}

			switch (integrationEnzymes) 
			{
			case MERGE:
			{
				if(exists) {

					if(!this.homologyEnzymes.get(key).isEmpty()) {

						if(existingECNumbers.get(key).isEmpty()) {

							this.integratedEnzymes.put(key, this.homologyEnzymes.get(key));
							this.integratedEnzymes_clone.put(key, this.homologyEnzymes.get(key));
						}
						else {

							enzymes = new TreeSet<String>(existingECNumbers.get(key));
							enzymes.addAll(this.homologyEnzymes.get(key));
							this.integratedEnzymes.put(key,enzymes);
							this.integratedEnzymes_clone.put(key,enzymes);
						}
					}
				}
				else {

					this.integratedEnzymes.put(key, this.homologyEnzymes.get(key));
					this.integratedEnzymes_clone.put(key, this.homologyEnzymes.get(key));
				}
				break;
			}
			case LOCAL_DATABASE:
			{
				if(exists) {

					if(existingECNumbers.isEmpty() && !this.homologyEnzymes.get(key).isEmpty()) {

						this.integratedEnzymes.put(key, this.homologyEnzymes.get(key));
						this.integratedEnzymes_clone.put(key, this.homologyEnzymes.get(key));
					}	
				}
				else {

					this.integratedEnzymes.put(key, this.homologyEnzymes.get(key));
					this.integratedEnzymes_clone.put(key, this.homologyEnzymes.get(key));
				}
				break;
			}
			case HOMOLOGY:
			{
				this.integratedEnzymes.put(key, homologyEnzymes.get(key));
				this.integratedEnzymes_clone.put(key, homologyEnzymes.get(key));
			}
			}
		}
	}

	/**
	 * @param existingNames
	 * @param integrationNames
	 * @param existingGeneNamesAlias
	 */
	private void compareGenes(Map<String, String> existingNames, Map<String, Set<String>> existingGeneNamesAlias) {

		for(String key : this.homologyName.keySet()) {

			boolean exists=false;
			if(existingNames.containsKey(key)) {

				exists=true;
			}

			switch (integrationNames) 
			{

			case MERGE:
			{
				if(exists) {

					if(existingNames.get(key).isEmpty()) {

						if(!this.homologyName.get(key).isEmpty()) {

							this.integratedName.put(key, this.homologyName.get(key));
							this.integratedName_2.put(key, this.homologyName.get(key));
						}
						else {

							//do nothing
						}
					}
					else {

						if(!(existingNames.get(key).equals(this.homologyName.get(key))) && !this.homologyName.get(key).isEmpty()) {

							if(existingGeneNamesAlias.containsKey(key)) {

								if(!(existingGeneNamesAlias.containsKey(key) && existingGeneNamesAlias.get(key).contains(this.homologyName.get(key)))) {

									List<String> temp = new ArrayList<String>();
									temp.add(existingNames.get(key));

									if(existingGeneNamesAlias.get(key)!=null) {

										temp.addAll(existingGeneNamesAlias.get(key));
									}

									this.nameConflictsDatabase.put(key, temp.toArray(new String[temp.size()]));
									this.nameConflictsHomology.put(key, new String[]{this.homologyName.get(key)});
								}
								else {

								}
							}
							else {

								this.nameConflictsDatabase.put(key, new String[]{existingNames.get(key)});
								this.nameConflictsHomology.put(key, new String[]{this.homologyName.get(key)});
							}
						}
					}
				}
				else {

					this.integratedName.put(key, this.homologyName.get(key));
					this.integratedName_2.put(key, this.homologyName.get(key));
				}
				break;
			}
			case LOCAL_DATABASE:
			{

				if(exists) {

					if(existingNames.get(key).isEmpty()) {

						this.integratedName.put(key, this.homologyName.get(key));
						this.integratedName_2.put(key, this.homologyName.get(key));
					}
				}
				else {

					this.integratedName.put(key, this.homologyName.get(key));
					this.integratedName_2.put(key, this.homologyName.get(key));
				}

				if(this.isEukaryote) {

					if(this.existingChromosome.containsKey(key) && this.existingChromosome.get(key).equalsIgnoreCase(this.chromosome.get(key))) {

						this.chromosome.put(key, this.existingChromosome.get(key));
					}
				}

				break;
			}
			case HOMOLOGY:
			{
				this.integratedName.put(key, this.homologyName.get(key));
				this.integratedName_2.put(key, this.homologyName.get(key));
			}
			}
		}

		allGeneNames = new TreeMap<String, List<String>>();

		for(String locus: existingNames.keySet()) {

			List<String> temp = new ArrayList<String>();
			temp.add(existingNames.get(locus));

			if(existingGeneNamesAlias.containsKey(locus)) {

				temp.addAll(existingGeneNamesAlias.get(locus));
			}
			allGeneNames.put(locus,temp);
		}
	}

	/**
	 * @param existingProducts
	 * @param integrationProducts
	 * @param existingProductsAlias
	 */
	private void compareProteins(Map<String, String> existingProducts, Map<String, Set<String>> existingProductsAlias) {

		for(String key : this.homologyProduct.keySet()) {

			boolean exists=false; 

			if(existingProducts.containsKey(key)) {

				exists=true;
			}

			switch (integrationProducts) 
			{
			case MERGE:
			{
				if(exists && !existingProducts.get(key).isEmpty()) {

					if(existingProducts.get(key).equals("hypothetical protein")) {

						if(!this.homologyProduct.get(key).contains("hypothetical protein")) {

							this.integratedProduct.put(key, this.homologyProduct.get(key));
							this.integratedProduct_clone.put(key, this.homologyProduct.get(key));
						}
					}
					else {

						if(this.homologyProduct.get(key).contains(existingProducts.get(key))) {

							this.integratedProduct.put(key,existingProducts.get(key));
							this.integratedProduct_clone.put(key,existingProducts.get(key));
						}
						else {

							this.integratedProduct.put(key, this.homologyProduct.get(key));
							this.integratedProduct_clone.put(key, this.homologyProduct.get(key));
						}
					}
				}
				else {

					this.integratedProduct.put(key, this.homologyProduct.get(key));
					this.integratedProduct_clone.put(key, this.homologyProduct.get(key));
				}
				break;
			}

			case LOCAL_DATABASE:
			{
				if(exists) {

					if(!existingProducts.containsKey(key)) {

						this.integratedProduct.put(key, this.homologyProduct.get(key));
						this.integratedProduct_clone.put(key, this.homologyProduct.get(key));
					}
				}
				else {

					this.integratedProduct.put(key, this.homologyProduct.get(key));
					this.integratedProduct_clone.put(key, this.homologyProduct.get(key));
				}
				break;
			}

			case HOMOLOGY:
			{

				this.integratedProduct.put(key, this.homologyProduct.get(key));
				this.integratedProduct_clone.put(key, this.homologyProduct.get(key));
			}
			}
		}

		allProteinNames = new TreeMap<String, List<String>>();

		for(String locus: existingProducts.keySet()) {

			List<String> temp = new ArrayList<String>();
			temp.add(existingProducts.get(locus));
			if(existingProductsAlias.containsKey(locus)) {

				temp.addAll(existingProductsAlias.get(locus));
			}
			allProteinNames.put(locus,temp);
		}
	}

	/**
	 * 
	 */
	public void generateReports() {

		//		System.out.println("Before reports");
		//		System.out.println("\t"+homologyEnzymes.get("KLLA0A00759g"));
		this.iReport.saveLocusTagReport(this.projectName,new TreeMap<String,String>(this.oldLocusNewTag));
		//		System.out.println("locus Report");
		//		System.out.println("\t"+homologyEnzymes.get("KLLA0A00759g"));
		this.iReport.saveGeneNameReport(this.projectName, allGeneNames, new TreeMap<String,String>(this.homologyName),new TreeMap<String,String>(this.integratedName));
		//		System.out.println("gene report");
		//		System.out.println("\t"+homologyEnzymes.get("KLLA0A00759g"));		
		this.iReport.saveEnzymesReport(this.projectName,existingECNumbers,new TreeMap<String,Set<String>>(this.homologyEnzymes), new TreeMap<String,Set<String>>(this.integratedEnzymes));
		//		System.out.println("enzyme report");
		//		System.out.println("\t"+homologyEnzymes.get("KLLA0A00759g"));
		this.iReport.saveProteinNamesReport(this.projectName,allProteinNames,new TreeMap<String,String>(this.homologyProduct), new TreeMap<String,String>(this.integratedProduct));
		//this.iReport.pathwaysIntegrationReport(this.database, allPathways, existsPathway, new TreeMap<String,Set<String>>(this.integratedEnzymes));
		//		System.out.println("protein report");
		//		System.out.println("\t"+homologyEnzymes.get("KLLA0A00759g"));
		this.iReport.saveGeneNamesConflicts(this.projectName,new TreeMap<String,String>(this.newNameConflicts));
		//		System.out.println("names conflits");
		//		System.out.println("\t"+homologyEnzymes.get("KLLA0A00759g"));
		this.iReport.pathwaysIntegrationReport(this.projectName, allPathways, existsPathway, new TreeMap<String,Set<String>>(this.homologyEnzymes));
		//		System.out.println("After reports");
		//		System.out.println("\t"+homologyEnzymes.get("KLLA0A00759g"));
	}

	/**
	 * Loads the local database with the integrated data 
	 */
	public void loadLocalDatabase(boolean processProteinNames) {

		if(nameConflictsHomology.size()>0) {

			Workbench.getInstance().warn("There were "+nameConflictsHomology.size()+" unsolved conflicts during the gene names integration!");
			this.newNameConflicts = new TreeMap<String, String>();
			IntegrationConflictsGUI inst = new IntegrationConflictsGUI(this, true);
			inst.setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
			inst.setVisible(true);		
			inst.setAlwaysOnTop(true);
		}
		else {

			Workbench.getInstance().info("There were no conflits found throughout the gene names integration!");
		}


		Statement stmt;

		try {

			///////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("Pre-Processing Genes...");
			///////////////////////////////////////////////////////////////////////////////////////////
			stmt = this.connection.createStatement();

			for(String key :this.oldLocusNewTag.keySet()) {

				ResultSet rs = stmt.executeQuery("SELECT idgene FROM gene WHERE locusTag = '"+key+"';");
				rs.next();
				String idgene = rs.getString(1);

				rs = stmt.executeQuery("SELECT idgene FROM gene WHERE locusTag = '"+this.oldLocusNewTag.get(key)+"';");

				if(rs.next()) {

					String newinserted = rs.getString(1);
					stmt.execute("UPDATE subunit SET gene_idgene = '"+newinserted+"' WHERE gene_idgene = '"+idgene+"'");
					stmt.execute("DELETE FROM gene WHERE idgene = '"+idgene+"'");
				}
				else {

					stmt.execute("UPDATE gene SET locusTag = '"+this.oldLocusNewTag.get(key)+"' WHERE idgene = '"+idgene+"'");
				}

				stmt.execute("INSERT INTO aliases (class, entity, alias) VALUES('g','"+idgene+"','"+key+"')");
				rs.close();
			}

			///////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("Processing Genes...");
			///////////////////////////////////////////////////////////////////////////////////////////

			for(String key :this.homologyLocusTags) {

				ResultSet rs = stmt.executeQuery("SELECT idgene FROM gene WHERE locusTag = '"+key+"';");

				if(!rs.next()) {

					String chromosome="";
					if(this.chromosome.containsKey(key)) {

						chromosome = this.chromosome.get(key);
					}

					rs = stmt.executeQuery("SELECT idchromosome FROM chromosome WHERE name = '"+chromosome+"'");
					String idchromosome;

					if(rs.next()) {

						idchromosome = rs.getString(1);
					}
					else {

						stmt.execute("INSERT INTO chromosome (name) VALUES('"+chromosome+"')");
						rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
						rs.next();
						idchromosome = rs.getString(1);
					}

					stmt.execute("INSERT INTO gene (locusTag,chromosome_idchromosome,origin) VALUES('"+key+"','"+idchromosome+"','HOMOLOGY')");
				}
				rs.close();
			}


			///////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("Processing Gene Names...");
			///////////////////////////////////////////////////////////////////////////////////////////
			Set<String> nameKeysList = new TreeSet<String>(this.integratedName_2.keySet());

			this.integratedName_2.putAll(this.newNameConflicts);

			for(String key :this.integratedName_2.keySet()) {

				if(this.integratedName_2.get(key)!=null && !this.integratedName_2.get(key).isEmpty()) {

					if(!stmt.execute("UPDATE gene SET name = '"+this.integratedName_2.get(key).replace("\'", "\\'")+"' WHERE locusTag = '"+key+"'")) {

						nameKeysList.remove(key);
					}
				}
			}
			//{Workbench.getInstance().warn(nameKeysList.size() +" gene names could not be assigned.");}

			///////////////////////////////////////////////////////////////////////////////////////////omar 
			System.out.println("Processing Enzymes...");
			///////////////////////////////////////////////////////////////////////////////////////////
			Set<String> enzymeKeysList = new TreeSet<String>(this.integratedEnzymes.keySet());

			Map<String,String> nameProteinToID = new TreeMap<String, String>();
			//this.integratedProduct.putAll(this.newProductsConflicts);

			for(String key :this.integratedEnzymes_clone.keySet()) {

				boolean go = false;

				for(String enzyme : this.integratedEnzymes_clone.get(key)) {

					if((enzyme.contains(".-") && this.integratePartial) || (!enzyme.contains(".-") && this.integrateFull) ) {

						go = true;
					}
				}

				if(go) {

					boolean removeProductIfInsertedInEnzyme = false;

					String idGene;
					ResultSet rs = stmt.executeQuery("SELECT idgene FROM gene WHERE locusTag = '"+key+"'");

					if(rs.next()) {

						idGene = rs.getString(1);

						rs = stmt.executeQuery("SELECT enzyme_ecnumber FROM subunit WHERE gene_idgene = "+idGene);
						Set<String> ecs = new HashSet<>();
						while(rs.next())
							ecs.add(rs.getString(1));

						for(String ec: ecs)
							if(!this.integratedEnzymes_clone.get(key).contains(ec))
								stmt.execute("DELETE FROM subunit WHERE gene_idgene = "+idGene+" AND enzyme_ecnumber='"+ec+"'");
					}
					else {

						String chromosome="";
						if(this.chromosome.containsKey(key)) {

							chromosome = this.chromosome.get(key);
						}
						rs = stmt.executeQuery("SELECT idchromosome FROM chromosome WHERE name = '"+chromosome+"'");
						String idchromosome;

						if(rs.next()) {

							idchromosome = rs.getString(1);
						}
						else {

							stmt.execute("INSERT INTO chromosome (name) VALUES('"+chromosome+"')");
							rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
							rs.next();
							idchromosome = rs.getString(1);
						}
						rs.close();
						stmt.execute("INSERT INTO gene (locusTag,chromosome_idchromosome,origin) VALUES('"+key+"','"+idchromosome+"','HOMOLOGY')");
						rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
						rs.next();
						idGene = rs.getString(1);
						rs.close();
					}

					for(String enzyme : this.integratedEnzymes_clone.get(key)) {

						if(((enzyme.contains(".-") && this.integratePartial) || (!enzyme.contains(".-") && this.integrateFull)) && !enzyme.isEmpty()) {

							rs = stmt.executeQuery("SELECT protein_idprotein FROM enzyme WHERE ecnumber = '"+enzyme+"'");
							String idProtein;

							if(rs.next()) {

								idProtein = rs.getString(1);
								rs= stmt.executeQuery("SELECT inModel FROM enzyme WHERE protein_idprotein="+idProtein+" AND ecnumber='"+enzyme+"'");
								rs.next();

								if(rs.getBoolean(1)) {

									enzymeKeysList.remove(key);
								}
								else {

									stmt.execute("UPDATE enzyme SET inModel = true, source = 'HOMOLOGY' WHERE protein_idprotein="+idProtein+" AND ecnumber='"+enzyme+"'");

									if(!enzyme.contains(".-")) {

										rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reaction " +
												"LEFT JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = idreaction " +
												"INNER JOIN pathway_has_enzyme ON pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein  " +
												"INNER JOIN pathway_has_reaction ON pathway_has_enzyme.pathway_idpathway = pathway_has_reaction.pathway_idpathway  " +
												"WHERE pathway_has_reaction.reaction_idreaction = idreaction " +
												"AND reaction_has_enzyme.enzyme_protein_idprotein = '"+idProtein+"' " +
												"AND reaction_has_enzyme.enzyme_ecnumber = '"+enzyme+"'");

										List<String> reactions_ids = new ArrayList<String>();

										while(rs.next()) {

											reactions_ids.add(rs.getString(1));
										}

										rs= stmt.executeQuery("SELECT idreaction FROM reactions_view_noPath_or_noEC " +
												"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction=idreaction " +
												"WHERE enzyme_protein_idprotein = '"+idProtein+"' AND enzyme_ecnumber = '"+enzyme+"'");

										while(rs.next()) {

											reactions_ids.add(rs.getString(1));
										}

										for(String idreaction: reactions_ids) {

											stmt.execute("UPDATE reaction SET inModel = true, source = 'HOMOLOGY' WHERE idreaction = '"+idreaction+"'");
										}
									}
									enzymeKeysList.remove(key);
								}
							}
							else {

								Set<String> temp= new TreeSet<String>();
								temp.addAll(nameProteinToID.keySet());
								String sTemp = this.integratedProduct_clone.get(key);

								if(!temp.contains(sTemp)) {

									rs = stmt.executeQuery("SELECT idprotein FROM protein WHERE name = '"+sTemp.replace("\'", "\\'")+"'");

									if(!rs.next()) {

										stmt.execute("INSERT INTO protein (name) VALUES('"+sTemp.replace("\'", "\\'")+"')");
										rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
										rs.next();
									}
									idProtein = rs.getString(1);
									nameProteinToID.put(sTemp, idProtein);
									rs.close();
								}

								idProtein = nameProteinToID.get(sTemp);
								stmt.execute("INSERT INTO enzyme (protein_idprotein, ecnumber, inModel, source) VALUES("+idProtein+",'"+enzyme+"',true,'HOMOLOGY')");

								if(!enzyme.contains(".-") ) {

									rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reaction " +
											"LEFT JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = idreaction " +
											"INNER JOIN pathway_has_enzyme ON pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein  " +
											"INNER JOIN pathway_has_reaction ON pathway_has_enzyme.pathway_idpathway = pathway_has_reaction.pathway_idpathway  " +
											"WHERE pathway_has_reaction.reaction_idreaction = idreaction " +
											"AND reaction_has_enzyme.enzyme_protein_idprotein = '"+idProtein+"' " +
											"AND reaction_has_enzyme.enzyme_ecnumber = '"+enzyme+"'");

									List<String> reactions_ids = new ArrayList<String>();

									while(rs.next()) {

										reactions_ids.add(rs.getString(1));
									}	

									rs= stmt.executeQuery("SELECT idreaction FROM reactions_view_noPath_or_noEC " +
											"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction=idreaction " +
											"WHERE enzyme_protein_idprotein = '"+idProtein+"' AND enzyme_ecnumber = '"+enzyme+"'");

									while(rs.next()) {

										reactions_ids.add(rs.getString(1));
									}

									for(String idreaction: reactions_ids) {

										stmt.execute("UPDATE reaction SET inModel = true, source = 'HOMOLOGY' WHERE idreaction = '"+idreaction+"'");
									}
								}
								enzymeKeysList.remove(key);
								removeProductIfInsertedInEnzyme=true;
							}

							rs = stmt.executeQuery("SELECT enzyme_protein_idprotein FROM subunit WHERE gene_idgene = '"+idGene+"' AND enzyme_protein_idprotein ='"+idProtein+"' AND enzyme_ecnumber='"+enzyme+"'");
							if(!rs.next()) {

								stmt.execute("INSERT INTO subunit (gene_idgene, enzyme_protein_idprotein, enzyme_ecnumber) VALUES ("+idGene+","+idProtein+",'"+enzyme+"')");
								enzymeKeysList.remove(key);
							}
						}

						if(removeProductIfInsertedInEnzyme) {

							this.integratedProduct_clone.remove(key);
							removeProductIfInsertedInEnzyme=false;
						}
						rs.close();
					}
				}
			}

			if(processProteinNames) {

				///////////////////////////////////////////////////////////////////////////////////////////
				System.out.println("Processing Protein Names...");
				///////////////////////////////////////////////////////////////////////////////////////////
				Set<String> productKeysList = new TreeSet<String>(this.integratedProduct_clone.keySet());
				ResultSet rs;

				for(String key :this.integratedProduct_clone.keySet()) {

					String idProtein;
					if(nameProteinToID.containsKey(this.integratedProduct_clone.get(key))) {

						idProtein = nameProteinToID.get(this.integratedProduct_clone.get(key));
					}
					else {

						rs = stmt.executeQuery("SELECT idprotein FROM protein WHERE name = '"+this.integratedProduct_clone.get(key).replace("\'", "\\'")+"'");

						if(!rs.next()) {

							stmt.execute("INSERT INTO protein (name) VALUES('"+this.integratedProduct_clone.get(key).replace("\'", "\\'")+"')");
							rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
							rs.next();
						}
						idProtein =  rs.getString(1);
					}

					if(this.integratedEnzymes_clone.containsKey(key)) {

						if(!this.integratedEnzymes_clone.get(key).isEmpty()) {

							//////////////////////////////////////////////////////////////////////////////////////////
							//se foi inserido nas enzimas inserir nome como alias
							if(!this.integratedProduct_clone.get(key).isEmpty()) {

								rs = stmt.executeQuery("SELECT enzyme_protein_idprotein FROM subunit LEFT JOIN gene ON (subunit.gene_idgene=gene.idgene) " +
										"WHERE locusTag = '"+key+"'");

								if(!rs.next()) {

									rs = stmt.executeQuery("SELECT * FROM enzyme " +
											"WHERE ecnumber = 'no-ecnumber' "+
											"AND protein_idprotein = "+idProtein+" ");

									if(!rs.next()) {

										stmt.execute("INSERT INTO enzyme (ecnumber,protein_idprotein, source, inModel) VALUES('no-ecnumber',"+idProtein+",'HOMOLOGY',true)");
									}

									this.insertGene(stmt, key, idProtein);
								}

								stmt.execute("INSERT INTO aliases (class, entity, alias) VALUES ('p',"+idProtein+",'"+this.integratedProduct_clone.get(key).replace("\'", "\\'")+"')");
							}
						}
					}
					else {

						////////////////////////////////////////////////////////////////////////////////////////////
						//se nao tiver sido inserido o nome das proteinas inserir agora ou ir buscar o id!

						ResultSet rs_ = stmt.executeQuery("SELECT ecnumber FROM enzyme WHERE protein_idprotein = '"+idProtein+"'");
						if(!rs_.next()) {

							stmt.execute("INSERT INTO enzyme (ecnumber,protein_idprotein, source, inModel) " +
									"VALUES('no-ecnumber',"+idProtein+",'HOMOLOGY',true)");
						}
						rs_.close();
						////////////////////////////////////////////////////////////////////////////////////////////
						//se nao tiver sido inserido o gene inserir!
						this.insertGene(stmt, key, idProtein);

					}
					productKeysList.remove(key);
				}
			}

			///////////////////////////////////////////////////////////////////////////////////////////	
			Workbench.getInstance().info("Integration Finished...");
			///////////////////////////////////////////////////////////////////////////////////////////
			stmt.close();
		} 
		catch (SQLException e) {e.printStackTrace();}
	}

	/**
	 * @param stmt
	 * @param key
	 * @param idProtein
	 * @return
	 * @throws SQLException
	 */
	private String insertGene(Statement stmt, String key, String idProtein) throws SQLException {

		String idGene;
		ResultSet rs = stmt.executeQuery("SELECT idgene FROM gene WHERE locusTag = '"+key+"'");

		if(rs.next()) {

			idGene = rs.getString(1);
		}
		else {


			rs = stmt.executeQuery("SELECT idchromosome FROM chromosome WHERE name = '"+this.chromosome.get(key)+"'");
			String idchromosome;

			if(rs.next()) {

				idchromosome = rs.getString(1);
			}
			else {

				String chromosome="";
				if(this.chromosome.containsKey(key)) {

					chromosome = this.chromosome.get(key);
				}

				stmt.execute("INSERT INTO chromosome (name) VALUES('"+chromosome+"')");
				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
				rs.next();
				idchromosome = rs.getString(1);
			}
			stmt.execute("INSERT INTO gene (locusTag,chromosome_idchromosome) VALUES('"+key+"','"+idchromosome+"')");
			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
			rs.next();
			idGene = rs.getString(1);
		}

		////////////////////////////////////////////////////////////////////////////////////////////
		//se nao tiver sido inserida subunidade inserir

		rs = stmt.executeQuery("SELECT enzyme_protein_idprotein FROM subunit WHERE gene_idgene = '"+idGene+"' AND enzyme_protein_idprotein ='"+idProtein+"'");
		if(!rs.next()) {

			stmt.execute("INSERT INTO subunit (gene_idgene, enzyme_protein_idprotein, enzyme_ecnumber) VALUES('"+idGene+"','"+idProtein+"','no-ecnumber')");

		}
		return idGene;
	}

	/**
	 * @param existshomologyInstance
	 * 
	 * Whether there is or not a homology instance
	 * 
	 */
	public void setExistshomologyInstance(boolean existshomologyInstance) {
		this.existshomologyInstance = existshomologyInstance;
	}

	/**
	 * @return Boolean of the existence of the homology instance
	 */
	public boolean isExistshomologyInstance() {
		return existshomologyInstance;
	}

	/**
	 * @author ODias
	 *
	 */
	public enum IntegrationType{
		MERGE,
		HOMOLOGY,
		LOCAL_DATABASE
	}

	public Map<String, String[]> getNameConflictsDatabase() {
		return nameConflictsDatabase;
	}

	public Map<String, String[]> getNameConflictsHomology() {
		return nameConflictsHomology;
	}

	//	public void setNameConflictsDatabase(Map<String, String[]> nameConflictsDatabase) {
	//		this.nameConflictsDatabase = nameConflictsDatabase;
	//	}

	//	public void setNameConflictshomology(Map<String, String[]> nameConflictshomology) {
	//		this.nameConflictshomology = nameConflictshomology;
	//	}

	//	public Map<String, String[]> getProductConflictsDatabase() {
	//		return productConflictsDatabase;
	//	}

	//	public void setProductConflictsDatabase(
	//			Map<String, String[]> productConflictsDatabase) {
	//		this.productConflictsDatabase = productConflictsDatabase;
	//	}

	//	public Map<String, String[]> getProductConflictshomology() {
	//		return productConflictshomology;
	//	}
	//
	//	public void setProductConflictshomology(Map<String, String[]> productConflictshomology) {
	//		this.productConflictshomology = productConflictshomology;
	//	}

	/**
	 * @param newProductsConflicts the newProductsConflicts to set
	 */
	//	public void setNewProductsConflicts(Map<String, String> newProductsConflicts) {
	//		this.newProductsConflicts = newProductsConflicts;
	//	}
	//
	//	/**
	//	 * @return the newProductsConflicts
	//	 */
	//	public Map<String, String> getNewProductsConflicts() {
	//		return newProductsConflicts;
	//	}
	//
	/**
	 * @param newNameConflicts the newNameConflicts to set
	 */
	public void setNewNameConflicts(Map<String, String> newNameConflicts) {
		this.newNameConflicts = newNameConflicts;
	}
	//
	//	/**
	//	 * @return the newNameConflicts
	//	 */
	//	public Map<String, String> getNewNameConflicts() {
	//		return newNameConflicts;
	//	}

	/**
	 * @return The alternative locus tags for all the existent primary homology locus tags.
	 */
	//	public Map<String, List<String>> getAllAlternativeLocusTag(){
	//
	//		//MySQLMultiThread dsa =  new MySQLMultiThread( this.host, this.port, this.database, this.user, this.password);
	//		Map<String, List<String>> alternativeLocusTag = new TreeMap<String,List<String>>();
	//
	//		Statement stmt;
	//		try 
	//		{
	//			stmt = this.connection.createStatement();
	//
	//			ResultSet rs = stmt.executeQuery("SELECT locusTag, locusID FROM genehomology " +
	//			"JOIN homologyData ON (genehomology.s_key=homology_genehomology_s_key)");
	//
	//			String locus = "";
	//			List<String> alternativeLocus = new ArrayList<String>();
	//			boolean exists;
	//			String newLocus="";
	//			while(exists=rs.next())
	//			{
	//				locus = rs.getString(1);
	//				if(newLocus.equals(locus))
	//				{		
	//					alternativeLocus.add(rs.getString(2));
	//					while(exists= rs.next() && locus.equals(rs.getString(1)))
	//					{
	//						alternativeLocus.add(rs.getString(2));
	//					}
	//					alternativeLocusTag.put(locus, alternativeLocus);
	//				}
	//				else
	//				{
	//					alternativeLocusTag.put(newLocus, alternativeLocus);
	//				}
	//
	//				if(exists)
	//				{
	//					alternativeLocus = new ArrayList<String>();
	//					newLocus = rs.getString(1);
	//					alternativeLocus.add(rs.getString(2));
	//				}
	//
	//			}
	//			rs.close();
	//			this.connection.closeStatement(stmt);
	//			this.connection.closeConnection();
	//		}
	//		catch (SQLException e){e.printStackTrace();}
	//		return alternativeLocusTag;
	//	}

	/**
	 * @return The local database locus tags.
	 */
	public Set<String> getAllDatabaseGenes(){

		Set<String> locusTag = new TreeSet<String>();

		Statement stmt;
		try 
		{
			stmt = this.connection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT locusTag FROM gene WHERE origin='KEGG'");

			//origin, KEGG, HOMOLOGY, MANUAL!!!ADICIONAR AO ESQUEMA!!!

			while(rs.next()){locusTag.add(rs.getString(1));}

			rs.close();
			stmt.close();
		}
		catch (SQLException e){e.printStackTrace();}
		return locusTag;
	}

	/**
	 * Operation that assigns new updated locus tags to the local database.
	 */
	public void setNewLocusTags() {

		this.oldLocusNewTag =  new TreeMap<String, String>();

		List<String> oldLocusTagsDatabase = new ArrayList<String>(this.getAllDatabaseGenes());
		//	System.out.println(oldLocusTagsDatabase.size()+"\t"+oldLocusTagsDatabase);
		//	System.out.println(this.homologyLocusTags.size()+"\t"+this.homologyLocusTags);

		oldLocusTagsDatabase.removeAll(this.homologyLocusTags);
		List<String> oldLocusTagsHomology = new ArrayList<String>(this.homologyLocusTags);
		oldLocusTagsHomology.removeAll(this.getAllDatabaseGenes());

		List<String> iteratorList = new ArrayList<String>(oldLocusTagsDatabase);

		for(int index = 0 ; index<iteratorList.size() ; index++) {

			//System.out.println(iteratorList.get(index)+"\t"+index+"\t"+iteratorList.size());

			if(oldLocusTagsHomology.isEmpty() || oldLocusTagsDatabase.isEmpty()) {

				index = iteratorList.size();
			}
			else {

				String oldLocusTag = iteratorList.get(index);
				UniProtEntry uniProtEntry = UniProtAPI.getUniProtEntryID(oldLocusTag,0);
				String newLocusTag = null;

				if(uniProtEntry!=null && UniProtAPI.getLocusTag(uniProtEntry)!=null && 
						UniProtAPI.getLocusTag(uniProtEntry).size()>0) {

					newLocusTag = UniProtAPI.getLocusTag(uniProtEntry).get(0).getValue();

					if(!homologyLocusTags.contains(newLocusTag)) {

						this.oldLocusNewTag.put(oldLocusTag,newLocusTag);

						oldLocusTagsDatabase.remove(oldLocusTag);
						oldLocusTagsHomology.remove(newLocusTag);
					}
				}
			}
		}

		//		Map<String, List<String>> alternativeLocusTag = this.getAllAlternativeLocusTag();
		//				this.oldLocusNewTag =  new TreeMap<String, String>();
		//				for(String locus:alternativeLocusTag.keySet())
		//				{
		//					if(!oldLocusTags.contains(locus)) // if the homology locus tag is not available in the local database
		//					{
		//						boolean found=false;
		//						for(int i =0 ;i< alternativeLocusTag.get(locus).size();i++) //for each alternative (ordered)
		//						{
		//							String alternative = alternativeLocusTag.get(locus).get(i);
		//		
		//							if(oldLocusTags.contains(alternative) && !found)// if the alternative is present in the local database && is the first available alternative
		//							{
		//								if(!alternativeLocusTag.containsKey(alternative)) // if the alternative is a homology locusTag it is not available
		//								{
		//									this.oldLocusNewTag.put(alternative,locus);
		//									found=true;
		//								}
		//							}
		//						}
		//					}
		//				}	
		//				System.out.println("SIZE:\t"+this.oldLocusNewTag.size());
		//		
		//				for(String key: this.oldLocusNewTag.keySet())
		//				{
		//					System.out.println("old: "+key+"\tnew: "+this.oldLocusNewTag.get(key));
		//				}

	}


}
