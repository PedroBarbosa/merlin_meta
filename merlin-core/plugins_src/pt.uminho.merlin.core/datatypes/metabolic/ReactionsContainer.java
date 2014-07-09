package datatypes.metabolic;

import java.awt.Color;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import pt.uminho.sysbio.common.biocomponents.container.io.readers.merlinAux.MetaboliteContainer;
import pt.uminho.sysbio.common.biocomponents.validation.chemestry.BalanceValidator;
import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQL_Utilities;
import datatypes.DataTable;
import datatypes.Table;
import datatypes.metabolic_regulatory.Entity;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import es.uvigo.ei.aibench.workbench.Workbench;

/**
 * @author ODias
 *
 */
@Datatype(structure= Structure.SIMPLE, namingMethod="getName",removable=true,removeMethod ="remove")
public class ReactionsContainer extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String,String> namesIndex;
	private HashMap<String,String> formulasIndex;
	private Map<Integer, String> ids;
	private Map<Integer, Integer> selectedPathIndexID;
	private  Map<Integer,Color> pathwayColors;
	private String[] paths;
	private Connection connection;
	private Integer[] tableColumnsSize;
	private Set<String> activeReactions;
	private Set<String> gapReactions;
	private BalanceValidator balanceValidator;
	private Map<String, String> externalModelIds;

	/**
	 * @param dbt
	 * @param name
	 */
	public ReactionsContainer(Table dbt, String name) {

		super(dbt, name);
		this.selectedPathIndexID = new TreeMap<Integer, Integer>();
		this.externalModelIds = new HashMap<>();
		this.connection=dbt.getConnection();
		this.colorPaths();
	}


	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		int num=0;
		int noname=0;
		int noequation=0;
		int reversible=0;
		int irreversible=0;

		String[][] res = new String[16][];
		Statement stmt;

		try {

			String aux = "";
			if(this.getProject().isCompartmentalisedModel()) {

				aux = aux.concat(" WHERE NOT originalReaction");
			}
			else {

				aux = aux.concat(" WHERE originalReaction");
			}

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM reaction"+aux);

			while(rs.next()) {

				num++;
				if(rs.getString(2)==null || rs.getString(2).trim().equals("")) {

					noname++;
				}

				if(rs.getString(3)==null || rs.getString(3).trim().equals("")) {

					noequation++;
				}

				if(rs.getString(4)!= null) {

					if(rs.getString(4).equals("0")){irreversible++;}
					else{reversible++;}
				}
			}



			int i=0;

			rs = stmt.executeQuery("SELECT COUNT(DISTINCT(idreaction)) FROM reaction "+aux+" AND  inModel");
			rs.next();
			res[i] = new String[] {"Toatal number of reactions in the model", ""+(rs.getInt(1))};
			i++;

			rs = stmt.executeQuery("SELECT COUNT(DISTINCT(idreaction)) FROM reaction "+aux+" AND inModel AND source ='KEGG'");
			rs.next();
			res[i] = new String[] {"Number of KEGG reactions in the model", ""+(rs.getInt(1))};
			i++;

			rs = stmt.executeQuery("SELECT COUNT(DISTINCT(idreaction)) FROM reaction "+aux+" AND inModel AND source ='HOMOLOGY'");
			rs.next();
			res[i] = new String[] {"Number of reactions inserted by HOMOLOGY in the model", ""+(rs.getInt(1))};
			i++;

			rs = stmt.executeQuery("SELECT COUNT(DISTINCT(idreaction)) FROM reaction "+aux+" AND inModel AND source='TRANSPORTERS'");
			rs.next();
			res[i] = new String[] {"Number of reactions from the TRANSPORTERS annotation tool in the model", ""+(rs.getInt(1))};
			i++;

			rs = stmt.executeQuery("SELECT COUNT(DISTINCT(idreaction)) FROM reaction "+aux+" AND inModel AND source='MANUAL'");
			rs.next();
			res[i] = new String[] {"Number of reactions inserted MANUALLY in the model", ""+(rs.getInt(1))};
			i++;

			res[i] = new String[] {"", ""};
			i++;

			res[i] = new String[] {"Number of reactions", ""+num};
			i++;

			res[i] = new String[] {"Number of reversible reactions", ""+reversible};
			i++;

			res[i] = new String[] {"Number of irreversible reactions", ""+irreversible};
			i++;


			rs = stmt.executeQuery("SELECT COUNT(DISTINCT(idreaction)) FROM reaction "+aux+" AND source='KEGG'");
			rs.next();
			res[i] = new String[] {"Number of reactions from KEGG", ""+rs.getInt(1)};
			i++;

			rs = stmt.executeQuery("SELECT COUNT(DISTINCT(idreaction)) FROM reaction "+aux+" AND source='TRANSPORTERS'");
			rs.next();
			res[i] = new String[] {"Number of reactions from the TRANSPORTERS annotation tool", ""+rs.getInt(1)};
			i++;

			res[i] = new String[] {"	Number of reactions with no name associated", ""+noname};
			i++;

			res[i] = new String[] {"	Number of reactions with no equation associated", ""+noequation};
			i++;

			rs = stmt.executeQuery("SELECT count(distinct(idreaction)) FROM reaction JOIN pathway_has_reaction ON idreaction = reaction_idreaction "+aux);
			rs.next();
			res[i] = new String[] {"Number of reactions with no pathway associated", ""+(num-rs.getInt(1))};
			i++;


			double nreagents = 0.0;
			rs = stmt.executeQuery("SELECT count(distinct(compound_idcompound)), reaction_idreaction, stoichiometric_coefficient " +
					"FROM stoichiometry " +
					"LEFT JOIN reaction ON (reaction_idreaction = reaction.idreaction) " +
					aux+" AND stoichiometric_coefficient<0 " +
					"GROUP BY reaction_idreaction");
			while(rs.next()) {

				nreagents += rs.getDouble(1);
			}
			res[i] = new String[] {"Average number of reagents by reaction",""+(nreagents/(new Double(num)).doubleValue())};
			i++;


			double nproducts = 0.0;
			rs = stmt.executeQuery("SELECT count(distinct(compound_idcompound)), reaction_idreaction, stoichiometric_coefficient " +
					"FROM stoichiometry " +
					"LEFT JOIN reaction ON (reaction_idreaction = reaction.idreaction) " +
					aux+" AND stoichiometric_coefficient>0 " +
					"GROUP BY reaction_idreaction");
			while(rs.next()) {

				nproducts += rs.getDouble(1);
			}
			res[i] = new String[] {"Average number of products by reaction", ""+(nproducts/(new Double(num)).doubleValue())};

			rs.close();
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		return res;
	}

	/**
	 * @param encodedOnly
	 * @param completeOnly
	 * @return
	 */
	public PathwayReaction getReactionsData(boolean encodedOnly) { //, boolean completeOnly){

		this.ids = new TreeMap<Integer,String>(); 
		this.namesIndex = new HashMap<String,String>();
		this.activeReactions = new HashSet<>();
		this.formulasIndex = new HashMap<String,String>();
		PathwayReaction reactionsData=null;
		List<String> pathwaysList = new ArrayList<String>();
		Map <String, Integer> pathID = new TreeMap<String, Integer>();
		pathID.put("", 0);
		pathwaysList.add("");
		Set<String> pathwaysSet=new TreeSet<String>();
		Map <String, String> pathways = new TreeMap <String, String>();
		Statement stmt;

		try {

			stmt = this.connection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT idpathway, name FROM pathway");

			while(rs.next()) {

				pathways.put(rs.getString(1), rs.getString(2));
			}

			ArrayList<String> columnsNames = new ArrayList<String>();
			columnsNames.add("Info");
			columnsNames.add("Pathway Name");
			columnsNames.add("Reaction Name");
			columnsNames.add("Equation");
			columnsNames.add("Source");
			columnsNames.add("notes");
			columnsNames.add("Reversible");
			columnsNames.add("Generic");
			//if(!encodedOnly) {
			columnsNames.add("In model");
			//	}


			reactionsData = new PathwayReaction(columnsNames, "Reactions", pathways, encodedOnly) {

				private static final long serialVersionUID = 6629060675011336218L;
				@Override
				public boolean isCellEditable(int row, int col){
					if (col==0 || col>4) {

						return true;
					}
					else return false;
				}
			};

			String aux = " WHERE ";
			if(encodedOnly) {

				aux = aux.concat(" inModel AND ");
				this.setTableColumnsSize(new Integer[]{320,150,1000,110,100,75});
			}
			else {

				this.setTableColumnsSize(new Integer[]{320,150,1000,110,100,75,75});
			}

			if(this.getProject().isCompartmentalisedModel()) {

				aux = aux.concat("  NOT originalReaction");
			}
			else {

				aux = aux.concat(" originalReaction");
			}

			rs = stmt.executeQuery("SELECT * FROM reactions_view " + aux+ " " +
					"UNION SELECT * FROM reactions_view_noPath_or_noEC "+aux +" " +
					"ORDER BY IF(ISNULL(pathway_name),1,0),pathway_name, reaction_name; ");


			int r=0;

			while(rs.next()) {

				this.activeReactions.add(rs.getString(2));
				this.ids.put(r,rs.getString(1));
				r++;
				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add("");

				if(rs.getString(6)!=null) {

					ql.add(rs.getString(6));
				}
				else {

					ql.add("");
				}

				ql.add(rs.getString(2));
				ql.add(rs.getString(3));
				ql.add(rs.getString(9));
				ql.add(rs.getString(12));
				ql.add(rs.getBoolean(4));
				ql.add(rs.getBoolean(8));
				//if(!encodedOnly) {
				ql.add(rs.getBoolean(7));
				//}

				if(rs.getString(5)==null) {

					reactionsData.addLine(ql, rs.getString(1), "0");
				}
				else {

					reactionsData.addLine(ql, rs.getString(1), rs.getString(5));
				}

				this.namesIndex.put(rs.getString(1), rs.getString(2));
				this.formulasIndex.put(rs.getString(1), rs.getString(3));

				if(rs.getString(6) != null) {

					pathID.put(rs.getString(6), Integer.parseInt(rs.getString(5)));
					pathwaysSet.add(rs.getString(6));
				}
			}

			pathwaysList.addAll(pathwaysSet);
			java.util.Collections.sort(pathwaysList);
			this.paths = new String[pathwaysList.size()+1];
			this.paths[0] = "All";

			for(int i=0;i<pathwaysList.size();i++) {

				this.selectedPathIndexID.put(i+1, pathID.get(pathwaysList.get(i)));				
				this.paths[i+1] = pathwaysList.get(i);
			}

			rs.close();
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return reactionsData;
	}

	/**
	 * @return a list with all pathways, except the SuperPathways
	 */
	public String[] getPathsBoolean(boolean encoded) {

		try {

			Statement stmt;
			stmt = this.connection.createStatement();
			List<String> pathways = new ArrayList<String>();
			Map <String, Integer> pathID = new TreeMap<String, Integer>();
			pathID.put("", 0);
			pathways.add("");
			Set<String> pathwaysSet=new TreeSet<String>();
			ResultSet rs;

			if(encoded) {

				rs = stmt.executeQuery(
						"SELECT DISTINCT(idpathway), pathway.name" +
								" FROM reaction" +
								" LEFT JOIN reaction_has_enzyme ON idreaction = reaction_has_enzyme.reaction_idreaction" +
								" LEFT JOIN enzyme ON reaction_has_enzyme.enzyme_ecnumber= enzyme.ecnumber" +
								" LEFT JOIN pathway_has_reaction ON idreaction=pathway_has_reaction.reaction_idreaction" +
								" LEFT JOIN pathway_has_enzyme ON enzyme.protein_idprotein=pathway_has_enzyme.enzyme_protein_idprotein" +
								" LEFT JOIN pathway ON pathway.idpathway=pathway_has_reaction.pathway_idpathway" +
								" WHERE pathway_has_reaction.pathway_idpathway=pathway_has_enzyme.pathway_idpathway " +
								" AND  reaction.inModel='1' " +
								" ORDER BY pathway.name, enzyme.ecnumber, reaction.name "
						);
			}
			else {

				rs = stmt.executeQuery("SELECT idpathway, name FROM pathway ORDER BY name");
			}

			while(rs.next()) {

				pathID.put(rs.getString(2), Integer.parseInt(rs.getString(1)));
				pathwaysSet.add(rs.getString(2));					
			}

			pathways.addAll(pathwaysSet);
			java.util.Collections.sort(pathways);
			this.paths = new String[pathways.size()+1];
			this.paths[0] = "All";

			for(int i=0;i<pathways.size();i++) {

				this.selectedPathIndexID.put(i+1, pathID.get(pathways.get(i)));				
				this.paths[i+1] = pathways.get(i);
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return paths;
	}

	/**
	 * @param rowID
	 * @return
	 */
	private String[] getEnzymes(String rowID) {

		Statement stmt;
		String[] res = null;

		try {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT enzyme_ecnumber, protein_idprotein, protein.name FROM reaction_has_enzyme " +
							" LEFT JOIN protein ON protein.idprotein = protein_idprotein " +
							"WHERE reaction_idreaction='"+rowID+"'"
					);

			rs.last();
			res = new String[rs.getRow()];
			rs.first();
			int col=0;
			while(col<res.length)
			{
				res[col] = rs.getString(1)+"___"+rs.getString(3)+"___"+rs.getString(2);
				rs.next();
				col++;
			}
			rs.close();
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
			// handle any errors
			//			System.out.println("SQLException: " + ex.getMessage());
			//			System.out.println("SQLState: " + ex.getSQLState());
			//			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return res;
	}

	/**
	 * @param rowID
	 * @return
	 */
	public String[] getEnzymes(String rowID, int pathway) {

		if(pathway < 0) {

			return this.getEnzymes(rowID);
		}

		Statement stmt;
		String[] res = null;

		try {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT DISTINCT enzyme.ecnumber, enzyme.protein_idprotein, protein.name FROM reaction" +
							" LEFT JOIN reaction_has_enzyme ON idreaction = reaction_has_enzyme.reaction_idreaction" +
							" LEFT JOIN enzyme ON reaction_has_enzyme.enzyme_ecnumber= enzyme.ecnumber AND reaction_has_enzyme.enzyme_protein_idprotein = enzyme.protein_idprotein " +
							" LEFT JOIN pathway_has_reaction ON idreaction=pathway_has_reaction.reaction_idreaction" +
							" LEFT JOIN pathway_has_enzyme ON enzyme.ecnumber=pathway_has_enzyme.enzyme_ecnumber AND enzyme.protein_idprotein=pathway_has_enzyme.enzyme_protein_idprotein" +
							" LEFT JOIN protein ON protein.idprotein = protein_idprotein " +
							" WHERE pathway_has_reaction.pathway_idpathway=pathway_has_enzyme.pathway_idpathway" +
							" AND pathway_has_enzyme.pathway_idpathway=\'"+pathway+"\'" +
							" AND reaction.idreaction=\'"+rowID+"\'"
					);

			rs.last();
			res = new String[rs.getRow()];
			rs.first();
			int col=0;

			while(col<res.length) {

				res[col] = rs.getString(1)+"___"+rs.getString(3)+"___"+rs.getString(2);
				rs.next();
				col++;
			}
			rs.close();
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * @return
	 */
	public String[] getEnzymesModel() {

		Statement stmt;
		ArrayList<String> lls = new ArrayList<String>();

		try {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT ecnumber, protein_idprotein, protein.name FROM enzyme " +
					" LEFT JOIN protein ON protein.idprotein = protein_idprotein " +
					"ORDER BY ecnumber");

			while(rs.next()) {

				lls.add(rs.getString(1)+"___"+rs.getString(3)+"___"+rs.getString(2));
			}
			rs.close();
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		String[] res = new String[lls.size()+1];

		res[0] = "";

		for(int i=0;i<lls.size();i++){res[i+1] = lls.get(i);}

		return res;
	}

	/**
	 * @param rowID
	 * @return
	 */
	public Set<String> getEnzymesForReaction(String rowID) {

		Statement stmt;
		Set<String> res = new TreeSet<String>();

		try  {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT enzyme_ecnumber, enzyme_protein_idprotein, protein.name " +
							" FROM reaction_has_enzyme " +
							" LEFT JOIN protein ON protein.idprotein = enzyme_protein_idprotein " +
							" WHERE reaction_idreaction = "+rowID);

			while(rs.next()) {

				res.add(rs.getString(1)+"___"+rs.getString(3)+"___"+rs.getString(2));
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * @param rowID
	 * @return
	 */
	public String[] getPathways(String rowID) {

		Statement stmt;
		String[] res = new String[0];

		try  {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT pathway.name FROM pathway " +
							" LEFT JOIN pathway_has_reaction ON (pathway_has_reaction.pathway_idpathway=pathway.idpathway ) " +
							" WHERE reaction_idreaction = "+rowID);

			boolean exists = rs.last();

			if(exists) {

				res = new String[rs.getRow()];
				rs.beforeFirst();

				int col=0;
				while(rs.next()) {

					res[col] = rs.getString(1);
					col++;
				}
				rs.close();
				stmt.close();
			}
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * @return list of all pathways, including superpathways
	 */
	public String[] getPathways() {

		Statement stmt;
		List<String> lls = new ArrayList<String>();
		try
		{
			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT idpathway, name FROM pathway " +
							"ORDER BY name"
					);

			while(rs.next())
			{
				lls.add(rs.getString(2));
			}
			rs.close();
			stmt.close();

		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			// handle any errors
			//			System.out.println("SQLException: " + ex.getMessage());
			//			System.out.println("SQLState: " + ex.getSQLState());
			//			System.out.println("VendorError: " + ex.getErrorCode());
		}

		String[] res = new String[lls.size()+1];

		res[0] = "";

		for(int i=0;i<lls.size();i++){res[i+1] = lls.get(i);}

		return res;
	}

	/**
	 * @param name
	 * @return
	 */
	public int getPathwayID(String name) {

		Statement stmt;
		int res=-1;

		try {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT idpathway, name FROM pathway " +
							"WHERE name='"+MySQL_Utilities.mysqlStrConverter(name)+"'"
					);

			while(rs.next()) {

				res = rs.getInt(1);
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * @param name
	 * @return
	 */
	public String getPathwayCode(String name) {

		Statement stmt;
		String res="";

		try {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT code FROM pathway " +
							"WHERE name='"+MySQL_Utilities.mysqlStrConverter(name)+"'"
					);

			while(rs.next()) {

				res=rs.getString(1);
			}
			rs.close();
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getRowInfo(java.lang.String)
	 */
	public DataTable[] getRowInfo(String id) {

		DataTable[] results = new DataTable[5];
		if(this.getBalanceValidator()!= null)
			results = new DataTable[6];
			
		List<String> columnsNames = new ArrayList<String>();
		columnsNames.add("Metabolite");
		columnsNames.add("Formula");
		columnsNames.add("KEGG ID");
		columnsNames.add("Compartment");
		columnsNames.add("Stoichiometric Coefficient");
		columnsNames.add("Number of Chains");
		results[0] = new DataTable(columnsNames, "Reaction");

		columnsNames = new ArrayList<String>();
		columnsNames.add("EC/TC nubmers");
		columnsNames.add("Proteins");
		columnsNames.add("In Model");
		results[1] = new DataTable(columnsNames, "Enzymes");

		columnsNames = new ArrayList<String>();
		columnsNames.add("Property");
		columnsNames.add("Values");
		results[2] = new DataTable(columnsNames, "Properties");

		columnsNames = new ArrayList<String>();
		columnsNames.add("Synonyms");
		results[3] = new DataTable(columnsNames, "Synonyms");

		columnsNames = new ArrayList<String>();
		columnsNames.add("Pathways");
		results[4] = new DataTable(columnsNames, "Pathways");
		
		if(this.getBalanceValidator()!= null) {
			
			columnsNames = new ArrayList<String>();
			columnsNames.add("Stoichiometric Balance");
			columnsNames.add("Values");
			results[5] = new DataTable(columnsNames, "Balance");
		}

		ArrayList<String> resultsList = new ArrayList<String>();
		Statement stmt;
		
		try {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT compound.name, compound.formula, compartment.name, stoichiometric_coefficient, numberofchains, kegg_id FROM stoichiometry " +
							"LEFT JOIN compound ON compound_idcompound = compound.idcompound " +
							"LEFT JOIN compartment ON compartment_idcompartment = idcompartment " +
							"WHERE reaction_idreaction = " + id);

			while(rs.next()) {

				resultsList = new ArrayList<String>();

				if(rs.getString(1)==null || rs.getString(1).isEmpty()) { 

					resultsList.add(rs.getString(6));
				}
				else {

					resultsList.add(rs.getString(1));
				}
				resultsList.add(rs.getString(2));
				resultsList.add(rs.getString(6));
				resultsList.add(rs.getString(3));
				resultsList.add(rs.getString(4));
				resultsList.add(rs.getString(5));
				results[0].addLine(resultsList);
			}

			rs = stmt.executeQuery(
					"SELECT enzyme_ecnumber, name, inModel, enzyme_protein_idprotein " +
							" FROM reaction_has_enzyme " +
							" LEFT JOIN enzyme ON reaction_has_enzyme.enzyme_protein_idprotein = enzyme.protein_idprotein AND reaction_has_enzyme.enzyme_ecnumber = enzyme.ecnumber " +
							" LEFT JOIN protein ON reaction_has_enzyme.enzyme_protein_idprotein = protein.idprotein AND reaction_has_enzyme.enzyme_ecnumber = enzyme.ecnumber " +
							" WHERE reaction_idreaction = "+id);

			while(rs.next()) {

				resultsList = new ArrayList<String>();
				resultsList.add(rs.getString(1));
				resultsList.add(rs.getString(2));
				resultsList.add(rs.getBoolean(3)+"");
				results[1].addLine(resultsList);
			}

			rs = stmt.executeQuery(
					"SELECT isGeneric, isSpontaneous, isNonEnzymatic, lowerBound, upperBound, reversible, name FROM reaction WHERE idreaction = " + id);

			String name = "";

			while(rs.next()) {

				resultsList = new ArrayList<String>();
				resultsList.add("Generic");
				resultsList.add(rs.getBoolean(1)+"");
				results[2].addLine(resultsList);

				resultsList = new ArrayList<String>();
				resultsList.add("Spontaneous");
				resultsList.add(rs.getBoolean(2)+"");
				results[2].addLine(resultsList);

				resultsList = new ArrayList<String>();
				resultsList.add("Non Enzymatic");
				resultsList.add(rs.getBoolean(3)+"");
				results[2].addLine(resultsList);

				String lb = rs.getString(4);
				if(rs.getInt(4)==0 && rs.getBoolean(6))
					lb = "-10000";

				String ub = rs.getString(4);
				if(rs.getInt(4)==0)
					ub = "10000";

				resultsList = new ArrayList<String>();
				resultsList.add("Lower Bound");
				resultsList.add(lb);
				results[2].addLine(resultsList);

				resultsList = new ArrayList<String>();
				resultsList.add("Upper Bound");
				resultsList.add(ub);
				results[2].addLine(resultsList);


				name=rs.getString(7);
			}

			if(name.contains("_C")) {

				rs = stmt.executeQuery("SELECT idreaction FROM reaction WHERE name = '"+name.split("_C")[0]+"'");
				if(rs.next()) {

					id = rs.getString(1);
				}
			}

			rs = stmt.executeQuery("SELECT alias FROM aliases WHERE class = 'r' AND entity = "+id);

			while(rs.next()) {

				resultsList = new ArrayList<String>();
				resultsList.add(rs.getString(1));
				results[3].addLine(resultsList);
			}
			
			if(this.getBalanceValidator()!= null) {

				resultsList = new ArrayList<String>();
				resultsList.add("Sum of reactants");
				resultsList.add(balanceValidator.getSumOfReactantsToString(this.externalModelIds.get(name)));
				results[5].addLine(resultsList);
				resultsList = new ArrayList<String>();
				resultsList.add("Sum of products");
				resultsList.add(balanceValidator.getSumOfProductsToString(this.externalModelIds.get(name)));
				results[5].addLine(resultsList);
				resultsList = new ArrayList<String>();
				resultsList.add("Balance");
				resultsList.add(balanceValidator.getDifResultToString(this.externalModelIds.get(name)));
				results[5].addLine(resultsList);
//				resultsList = new ArrayList<String>();
//				resultsList.add("Info");
//				resultsList.add(balanceValidator.getBalanceInfo(this.externalModelIds.get(name).replaceAll("\\#", "")));
//				results[5].addLine(resultsList);
			}

			rs.close();
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}


		for(String pathway:this.getPathways(id)) {

			resultsList = new ArrayList<String>();
			resultsList.add(pathway);
			results[4].addLine(resultsList);
		}

		return results;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getReactionName(String id) {

		return this.namesIndex.get(id);
	}

	/**
	 * @param name
	 * @return
	 */
	public String getReactionID(String name) {

		String id = null;
		Statement stmt;

		try {

			ResultSet rs;
			stmt = this.connection.createStatement();

			rs = stmt.executeQuery("SELECT idreaction FROM reaction WHERE name = '" + MySQL_Utilities.mysqlStrConverter(name)+ "'");
			if(rs.next())
			{
				id=rs.getString(1);
			}
			else
			{
				id="";
			}
			rs.close();
			stmt.close();

		}
		catch (Exception e) {

			e.printStackTrace();
		}
		return id;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getFormula(String id) {

		if(this.formulasIndex.containsKey(id)) return this.formulasIndex.get(id);
		else return null;
	}

	/**
	 * @param selectedRow
	 * 
	 * duplicate a given reaction
	 */
	public void duplicateReaction(String  reaction_id) {

		Map<String, Set<String>> selectedEnzymesPathway = new TreeMap<String, Set<String>>();

		Statement stmt;

		try  {

			stmt = this.connection.createStatement();

			ResultSet rs = stmt.executeQuery(
					"SELECT reaction.name, equation, reversible, pathway.name, enzyme_ecnumber, inModel, compartment.name, "
							+ "isSpontaneous, isNonEnzymatic, isGeneric, lowerBoundary, upperBoundary " +
							"FROM reaction " +
							"LEFT JOIN pathway_has_reaction ON idreaction = reaction_idreaction " +
							"LEFT JOIN pathway ON pathway_idpathway = pathway.idpathway " +
							"LEFT JOIN compartment ON idcompartment = compartment_idcompartment " +
							"LEFT JOIN reaction_has_enzyme ON idreaction = reaction_has_enzyme.reaction_idreaction " +
							"WHERE idreaction='"+reaction_id+"'"
					);

			rs.next();

			String name=rs.getString(1), equation=rs.getString(2), compartment_name = rs.getString(7),  lowerBoundary = rs.getString(11), upperBoundary = rs.getString(12);
			boolean reversibility = rs.getBoolean(3), inModel = rs.getBoolean(6), isSpontaneous = rs.getBoolean(8),  isNonEnzymatic = rs.getBoolean(9), isGeneric = rs.getBoolean(10);

			Map<String, String> chains=new TreeMap<String, String>(), compartment=new TreeMap<String, String>(),
					metabolites=new TreeMap<String, String>();
			rs = stmt.executeQuery("SELECT compartment.name, stoichiometric_coefficient, numberofchains, compound_idcompound " +
					"FROM stoichiometry " +
					"LEFT JOIN compartment ON idcompartment = compartment_idcompartment " +
					"WHERE reaction_idreaction = '" + reaction_id+"'");

			while(rs.next()) {

				if(rs.getString(2).startsWith("-")) {

					metabolites.put("-"+rs.getString(4),rs.getString(2));
					chains.put("-"+rs.getString(4),rs.getString(3));
					compartment.put("-"+rs.getString(4), rs.getString(1));
				}
				else {

					metabolites.put(rs.getString(4),rs.getString(2));
					chains.put(rs.getString(4),rs.getString(3));
					compartment.put(rs.getString(4), rs.getString(1));
				}
			}

			for(String pathway: this.getPathways(reaction_id)) {

				Set<String> enzymesSet=new TreeSet<String>();
				enzymesSet.addAll(new TreeSet<String>(Arrays.asList(this.getEnzymes(reaction_id, this.getPathwayID(pathway)))));
				selectedEnzymesPathway.put(pathway, enzymesSet);
			}

			if(selectedEnzymesPathway.isEmpty()) {

				selectedEnzymesPathway.put("-1allpathwaysinreaction", this.getEnzymesForReaction(reaction_id));
			}

			this.insertNewReaction(incrementName(name,stmt), equation, reversibility, 
					chains, compartment, metabolites, inModel, selectedEnzymesPathway, 
					compartment_name, isSpontaneous, isNonEnzymatic, isGeneric, lowerBoundary, upperBoundary);
			rs.close();
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

	}

	/**
	 * Insert a new reaction on the database
	 * 
	 * @param name
	 * @param equation
	 * @param reversibility
	 * @param chains
	 * @param compartment
	 * @param metabolites
	 * @param inModel
	 * @param selectedEnzymesPathway
	 * @param localisation
	 * @param isSpontaneous
	 * @param isNonEnzymatic
	 * @param isGeneric
	 * @param lowerBoundary
	 * @param upperBoundary
	 */
	public void insertNewReaction(String name, String equation, boolean reversibility, //Set<String> pathways, Set<String> enzymes, 
			Map<String,String> chains, Map<String, String > compartment, Map<String, String> metabolites, boolean inModel, Map<String, 
			Set<String>> selectedEnzymesPathway, String localisation, boolean isSpontaneous, boolean isNonEnzymatic,
			boolean isGeneric, String lowerBoundary, String upperBoundary) {

		Statement stmt;

		try {

			ResultSet rs;
			stmt = this.connection.createStatement();

			if(!name.startsWith("R") && !name.startsWith("T")&& !name.startsWith("K") && !name.toLowerCase().contains("biomass")) {

				name = "R_"+name;
			}

			rs = stmt.executeQuery("SELECT idreaction FROM reaction WHERE name = '" + MySQL_Utilities.mysqlStrConverter(name)+ "'");
			if(rs.next()) {

				Workbench.getInstance().error("Reaction with the same name already exists. Aborting operation!");
			}
			else {

				boolean originalReaction = true;

				if(this.getProject().isCompartmentalisedModel()) {

					originalReaction = false;
				}

				rs = stmt.executeQuery("SELECT idcompartment FROM compartment WHERE name = '" + localisation + "'");
				rs.next();
				String idCompartment = rs.getString(1);

				stmt.execute("INSERT INTO Reaction (name, equation, reversible, inModel, compartment_idcompartment, " +
						"source, isSpontaneous, isNonEnzymatic, originalReaction, isGeneric, lowerBoundary, upperBoundary) " +
						"VALUES('" + MySQL_Utilities.mysqlStrConverter(name) + "', '" + MySQL_Utilities.mysqlStrConverter(equation) + "', " 
						+ reversibility + ", "+ inModel+","+idCompartment+",'MANUAL', "+isSpontaneous+","+isNonEnzymatic+", "
						+originalReaction+", "+isGeneric+", "+lowerBoundary+", "+upperBoundary+")");

				String idNewReaction = (this.select("SELECT LAST_INSERT_ID()"))[0][0];

				//PATHWAYS AND ENZYMES PROCESSING
				{
					Map<String,Set<String>> newPathwaysID = new TreeMap<String,Set<String>>();
					selectedEnzymesPathway.remove("");
					{
						if(selectedEnzymesPathway.containsKey("-1allpathwaysinreaction") && selectedEnzymesPathway.get("-1allpathwaysinreaction").size()>0) {

							for(String ecnumber: selectedEnzymesPathway.get("-1allpathwaysinreaction")) {

								rs = stmt.executeQuery("SELECT protein_idprotein FROM enzyme WHERE ecnumber = '" + ecnumber+ "'");
								rs.next();
								String protein_id=rs.getString(1);

								rs = stmt.executeQuery("SELECT * FROM reaction_has_enzyme WHERE enzyme_ecnumber='" + ecnumber+ "' AND reaction_idreaction='"+idNewReaction+"'");

								if(!rs.next()) {

									stmt.execute("INSERT INTO reaction_has_enzyme (enzyme_ecnumber,enzyme_protein_idprotein,reaction_idreaction) " +
											"VALUES ('" + ecnumber + "', " +protein_id+", "+idNewReaction+") ");
								}
							}
						}
						selectedEnzymesPathway.remove("-1allpathwaysinreaction");
					}

					if(selectedEnzymesPathway.size()>0) {

						for(String pathway:selectedEnzymesPathway.keySet()) {

							rs = stmt.executeQuery("SELECT idpathway FROM pathway WHERE name = '" + MySQL_Utilities.mysqlStrConverter(pathway)+ "'");
							rs.next();
							newPathwaysID.put(rs.getString(1), new TreeSet<String>(selectedEnzymesPathway.get(pathway)));
						}

						//when pathways are deleted, they are just removed from the pathway has reaction association
						//insert the new pathways

						for(String pathway:newPathwaysID.keySet()) {

							stmt.execute("INSERT INTO pathway_has_reaction (pathway_idpathway, reaction_idreaction) " +
									"VALUES ("+pathway+","+idNewReaction+")");

							for(String enzyme: newPathwaysID.get(pathway)) {

								String ecnumber = enzyme.split("___")[0];

								String idProtein = enzyme.split("___")[2];

								rs = stmt.executeQuery("SELECT * FROM pathway_has_enzyme WHERE enzyme_ecnumber='" + ecnumber+ "' AND pathway_idpathway = "+pathway+ " AND enzyme_protein_idprotein = "+idProtein);

								if(!rs.next()) {

									stmt.execute("INSERT INTO pathway_has_enzyme (pathway_idpathway, enzyme_ecnumber,enzyme_protein_idprotein) " +
											"VALUES ("+pathway+",'"+ecnumber+"',"+idProtein+")");
								}

								rs = stmt.executeQuery("SELECT * FROM reaction_has_enzyme WHERE enzyme_ecnumber = '"+ecnumber+"' AND reaction_idreaction = "+idNewReaction+" AND enzyme_protein_idprotein = "+idProtein);

								if(!rs.next()) {

									stmt.execute("INSERT INTO reaction_has_enzyme (enzyme_ecnumber,enzyme_protein_idprotein,reaction_idreaction) " +
											"VALUES ('"+ecnumber+"',"+idProtein+","+idNewReaction+") ");
								}
							}
						}
					}
				}

				int biomass_id = -1;
				rs = stmt.executeQuery("SELECT idcompound FROM compound WHERE name LIKE 'Biomass'");
				if(rs.next()) {

					biomass_id = rs.getInt("idcompound");
				}

				for(String m :metabolites.keySet()) {

					rs = stmt.executeQuery("SELECT idcompartment FROM compartment WHERE name = '" + compartment.get(m) + "'");
					rs.next();
					idCompartment = rs.getString(1);
					stmt.execute("INSERT INTO stoichiometry (stoichiometric_coefficient, compartment_idcompartment, compound_idcompound, reaction_idreaction,numberofchains) " +
							"VALUES('" + metabolites.get(m) + "', '" + idCompartment +	"', '" + m.replace("-", "") + "', '" + idNewReaction + "', '" + chains.get(m) + "')");


					if(m.replace("-", "").equalsIgnoreCase(biomass_id+"")) {

						rs = stmt.executeQuery("SELECT * FROM pathway WHERE name = 'Biomass Pathway'");
						if(!rs.next()) {						

							stmt.execute("INSERT INTO pathway (name) VALUES('Biomass Pathway')");
							rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
							rs.next();
						}
						String idBiomassPath= rs.getString(1);
						rs = stmt.executeQuery("SELECT * FROM pathway_has_reaction WHERE pathway_idpathway = "+idBiomassPath+ " AND reaction_idreaction = "+idNewReaction);

						if(!rs.next()) {
							stmt.execute("INSERT INTO pathway_has_reaction (pathway_idpathway, reaction_idreaction) " +
									"VALUES ("+idBiomassPath+","+idNewReaction+")");
						}
					}
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
	}


	/**
	 * @param rowID
	 * @param name
	 * @param equation
	 * @param reversibility
	 * @param chains
	 * @param compartment
	 * @param metabolites
	 * @param inModel
	 * @param selectedEnzymesPathway
	 * @param localisation
	 * @param isSpontaneous
	 * @param isNonEnzymatic
	 * @param isGeneric
	 * @param lowerBoundary
	 * @param upperBoundary
	 */
	public void updateReaction(String rowID, String name, String equation, boolean reversibility, //Set<String> enzymes,
			Map<String, String> chains, Map<String, String > compartment, Map<String, String> metabolites, boolean inModel, 
			Map<String, Set<String>> selectedEnzymesPathway, String localisation, boolean isSpontaneous, boolean isNonEnzymatic,
			boolean isGeneric, String lowerBoundary, String upperBoundary) {

		Statement stmt;
		try {

			if(equation.contains(" <= ")) {

				String [] equationArray = equation.split(" <= ");
				equation = equationArray[1]+" => "+equationArray[0];
			}

			if(!name.startsWith("R") && !name.startsWith("T") && !name.startsWith("K") && !name.toLowerCase().contains("biomass")) {

				name = "R_"+name;
			}

			stmt = this.connection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT idcompartment FROM compartment WHERE name = '" + localisation + "'");
			rs.next();
			String idCompartment = rs.getString(1);

			String aux="";

			rs = stmt.executeQuery("SELECT inModel FROM reaction WHERE idreaction='"+rowID+"'");
			rs.next();

			boolean inModelReaction=rs.getBoolean(1);

			if(inModelReaction != inModel) {

				aux=", inModel="+ inModel ;
			}
			stmt.execute("UPDATE reaction SET name = '" + MySQL_Utilities.mysqlStrConverter(name) + 
					"', equation = '" + MySQL_Utilities.mysqlStrConverter(equation) + 
					"', reversible = " + reversibility + " " +
					", compartment_idcompartment = "+idCompartment+
					", isSpontaneous = "+isSpontaneous+
					", isNonEnzymatic = "+isNonEnzymatic+
					", isGeneric = "+isNonEnzymatic+
					", lowerBound = "+lowerBoundary+
					", upperBound = "+upperBoundary+
					aux+
					" WHERE idreaction='"+rowID+"'");


			//PATHWAYS AND ENZYMES PROCESSING
			{
				Map<String,String> existingPathwaysID = new TreeMap<String,String>();
				Map<String,Set<String>> newPathwaysID = new TreeMap<String,Set<String>>();
				Map<String,Set<String>>  editedPathwaysID = new TreeMap<String,Set<String>> ();
				selectedEnzymesPathway.remove("");

				rs = stmt.executeQuery("SELECT pathway_idpathway FROM pathway_has_reaction WHERE reaction_idreaction = "+rowID);

				while(rs.next()) {

					existingPathwaysID.put(rs.getString(1),"");
				}

				// IF There are enzymes and no pathway! or add to all pathways
				List<String> existingEnzymesID = new ArrayList<String>();

				if(selectedEnzymesPathway!= null && selectedEnzymesPathway.get("-1allpathwaysinreaction")!= null && selectedEnzymesPathway.get("-1allpathwaysinreaction").size()>0) {

					rs = stmt.executeQuery("SELECT enzyme_ecnumber, reaction_has_enzyme.enzyme_protein_idprotein, protein.name FROM reaction_has_enzyme " +
							" LEFT JOIN protein ON protein.idprotein = reaction_has_enzyme.enzyme_protein_idprotein " +
							" WHERE reaction_idreaction = "+rowID);

					while(rs.next()) {

						existingEnzymesID.add(rs.getString(1)+"___"+rs.getString(3)+"___"+rs.getString(2));
					}

					for(String enzyme: new ArrayList<String>(selectedEnzymesPathway.get("-1allpathwaysinreaction"))) {

						if(existingEnzymesID.contains(enzyme)) {

							existingEnzymesID.remove(enzyme);
							selectedEnzymesPathway.get("-1allpathwaysinreaction").remove(enzyme);
						}
					}

					for(String enzyme: selectedEnzymesPathway.get("-1allpathwaysinreaction")) {

						String ecnumber = enzyme.split("___")[0];

						String idProtein = enzyme.split("___")[2];

						rs = stmt.executeQuery("SELECT * FROM reaction_has_enzyme WHERE enzyme_ecnumber='" + ecnumber+ "' AND enzyme_protein_idprotein = "+idProtein+" AND reaction_idreaction = "+rowID );

						if(!rs.next()) {

							stmt.execute("INSERT INTO reaction_has_enzyme (enzyme_ecnumber,enzyme_protein_idprotein,reaction_idreaction) VALUES ('" + ecnumber + "'," +idProtein+","+rowID+") ");
						}
					}

					for(String enzyme:existingEnzymesID) {

						String ecnumber = enzyme.split("___")[0];

						String idProtein = enzyme.split("___")[2];

						stmt.execute("DELETE FROM reaction_has_enzyme WHERE reaction_idreaction = "+rowID+" AND enzyme_protein_idprotein = "+idProtein+" AND enzyme_ecnumber='"+ecnumber+"'");
					}
				}
				else {

					ResultSet rss = stmt.executeQuery("SELECT enzyme_ecnumber, protein.idprotein, protein.name" +
							" FROM reaction_has_enzyme " +
							" LEFT JOIN protein ON protein.idprotein = enzyme_protein_idprotein " +
							" WHERE reaction_idreaction = "+rowID);

					while(rss.next()) {

						existingEnzymesID.add(rss.getString(1)+"___"+rss.getString(3)+"___"+rss.getString(2));
					}

					for(String enzyme:existingEnzymesID) {

						String ecnumber = enzyme.split("___")[0];

						String idProtein = enzyme.split("___")[2];

						stmt.execute("DELETE FROM reaction_has_enzyme WHERE reaction_idreaction = "+rowID+" AND enzyme_protein_idprotein = "+idProtein+" AND enzyme_ecnumber='"+ecnumber+"'");
					}
					rss.close();
				}

				Set<String> genericECNumbers = new TreeSet<String>();
				if(selectedEnzymesPathway.get("-1allpathwaysinreaction") != null) {

					genericECNumbers.addAll(selectedEnzymesPathway.get("-1allpathwaysinreaction"));
				}

				selectedEnzymesPathway.remove("-1allpathwaysinreaction");

				if(selectedEnzymesPathway.size()>0) {

					rs = stmt.executeQuery("SELECT pathway_idpathway FROM pathway_has_reaction WHERE reaction_idreaction = "+rowID);

					while(rs.next()) {

						existingPathwaysID.put(rs.getString(1),"");
					}

					for(String pathway:selectedEnzymesPathway.keySet()) {

						rs = stmt.executeQuery("SELECT idpathway FROM pathway WHERE name = '" + MySQL_Utilities.mysqlStrConverter(pathway)+ "'");
						rs.next();
						Set<String> enzymes = new TreeSet<String>(selectedEnzymesPathway.get(pathway));
						if(enzymes.isEmpty()) {

							enzymes.addAll(genericECNumbers);
						}
						newPathwaysID.put(rs.getString(1), enzymes);
					}

					for(String pathway: new ArrayList<String>(existingPathwaysID.keySet())) {

						if(newPathwaysID.containsKey(pathway)) {

							editedPathwaysID.put(pathway, new TreeSet<String>(newPathwaysID.get(pathway)));
							newPathwaysID.remove(pathway);
							existingPathwaysID.remove(pathway);
						}
					}

					//when pathways are deleted, they are just removed from the pathway has reaction association
					for(String pathway:existingPathwaysID.keySet()) {

						stmt.execute("DELETE FROM pathway_has_reaction WHERE reaction_idreaction = "+rowID+" AND pathway_idpathway = "+pathway);
					}

					Map<String,Set<String>> pathsEnzymesIn = new TreeMap<String, Set<String>>();
					Map<String,Set<String>> pathsReactionsIn = new TreeMap<String, Set<String>>();
					//insert the new pathways

					for(String pathway:newPathwaysID.keySet()) {

						stmt.execute("INSERT INTO pathway_has_reaction (pathway_idpathway, reaction_idreaction) VALUES ("+pathway+","+rowID+")");

						for(String enzyme: newPathwaysID.get(pathway)) {

							Set<String> existsEnzReaction=new TreeSet<String>();
							Set<String> existsEnzPathway=new TreeSet<String>();

							String ecnumber = enzyme.split("___")[0];

							String idProtein = enzyme.split("___")[2];


							rs = stmt.executeQuery("SELECT * FROM pathway_has_enzyme WHERE enzyme_ecnumber='" + ecnumber+ "' AND enzyme_protein_idprotein = "+idProtein+" AND pathway_idpathway = "+pathway);

							if(!rs.next()) {

								stmt.execute("INSERT INTO pathway_has_enzyme (pathway_idpathway, enzyme_ecnumber,enzyme_protein_idprotein) " +
										"VALUES ("+pathway+",'"+ecnumber+"',"+idProtein+")");
								existsEnzPathway.add(idProtein);
								pathsEnzymesIn.put(pathway, existsEnzPathway);
							}

							rs = stmt.executeQuery("SELECT * FROM reaction_has_enzyme WHERE enzyme_ecnumber='" + ecnumber+ "' AND enzyme_protein_idprotein = "+idProtein+" AND reaction_idreaction = "+rowID);
							if(!rs.next()) {

								stmt.execute("INSERT INTO reaction_has_enzyme (enzyme_ecnumber,enzyme_protein_idprotein,reaction_idreaction) " +
										"VALUES ('" + ecnumber + "',"+idProtein+","+rowID+") ");
								existsEnzReaction.add(idProtein);
								pathsReactionsIn.put(pathway, existsEnzReaction);
							}
						}
					}
					// edited pathways
					for(String pathway:editedPathwaysID.keySet()) {

						existingEnzymesID = new ArrayList<String>(Arrays.asList(this.getEnzymes(rowID,Integer.parseInt(pathway))));
						editedPathwaysID.get(pathway).remove("");

						for(String ecnumber: new TreeSet<String>(editedPathwaysID.get(pathway))) {

							for(String existingEcnumber :new TreeSet<String>(existingEnzymesID)) {

								if(existingEcnumber.equals(ecnumber)) {

									editedPathwaysID.get(pathway).remove(ecnumber);
									existingEnzymesID.remove(existingEcnumber);
								}
							}
						}

						for(String enzyme: new TreeSet<String>(editedPathwaysID.get(pathway))) {

							//System.out.println("undeleted ecnumber "+ecnumber);
							Set<String> existsEnzReaction=new TreeSet<String>();
							Set<String> existsEnzPathway=new TreeSet<String>();
							//							rs = stmt.executeQuery("SELECT protein_idprotein FROM enzyme WHERE ecnumber ='"+ecnumber+"'");
							//							Set<String> idProteins=new TreeSet<String>();
							//
							//							while(rs.next()) {
							//
							//								idProteins.add(rs.getString(1));
							//							}

							//for(String idProtein: idProteins) {

							String ecnumber = enzyme.split("___")[0];

							String idProtein = enzyme.split("___")[2];

							rs = stmt.executeQuery("SELECT * FROM pathway_has_enzyme WHERE enzyme_ecnumber ='" + ecnumber+ "' AND enzyme_protein_idprotein = "+idProtein+" AND pathway_idpathway = "+pathway);

							if(!rs.next()) {

								stmt.execute("INSERT INTO pathway_has_enzyme (pathway_idpathway, enzyme_ecnumber,enzyme_protein_idprotein) " +
										"VALUES ('" + pathway + "' , '"+ecnumber+ "' , '"+idProtein+ "')");
								existsEnzPathway.add(idProtein);
								pathsEnzymesIn.put(pathway, existsEnzPathway);
							}

							rs = stmt.executeQuery("SELECT * FROM reaction_has_enzyme WHERE enzyme_ecnumber='" + ecnumber+ "' AND enzyme_protein_idprotein = "+idProtein+" AND reaction_idreaction = "+rowID);

							if(!rs.next()) {

								stmt.execute("INSERT INTO reaction_has_enzyme (enzyme_ecnumber,enzyme_protein_idprotein,reaction_idreaction) " +
										"VALUES ('" + ecnumber + "', '" +idProtein+"', '"+rowID+" ') ");
								existsEnzReaction.add(idProtein);
								pathsReactionsIn.put(pathway, existsEnzReaction);
							}
							//}

						}

						for(String enzyme: new TreeSet<String>(existingEnzymesID)) {

							String ecnumber = enzyme.split("___")[0];

							String idProtein = enzyme.split("___")[2];

							Set<String> reactionsID = new TreeSet<String>();
							rs = stmt.executeQuery("SELECT reaction_has_enzyme.reaction_idreaction FROM pathway_has_enzyme " +
									"LEFT JOIN reaction_has_enzyme ON pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein AND pathway_has_enzyme.enzyme_ecnumber = reaction_has_enzyme.enzyme_ecnumber " +
									"LEFT JOIN pathway_has_reaction ON pathway_has_enzyme.pathway_idpathway = pathway_has_reaction.pathway_idpathway " +
									"WHERE reaction_has_enzyme.reaction_idreaction = pathway_has_reaction.reaction_idreaction " +
									"AND pathway_has_enzyme.enzyme_ecnumber = '"+ecnumber+"' AND pathway_has_enzyme.enzyme_protein_idprotein = '"+idProtein+"' AND pathway_has_enzyme.pathway_idpathway = "+pathway);

							while(rs.next()) {

								reactionsID.add(rs.getString(1));
							}

							reactionsID.remove(rowID);

							if(reactionsID.size()==0) {

								stmt.execute("DELETE FROM pathway_has_enzyme WHERE pathway_has_enzyme.enzyme_ecnumber = '"+ecnumber+"'  AND pathway_has_enzyme.enzyme_protein_idprotein = '"+idProtein+"' " +
										" AND pathway_has_enzyme.pathway_idpathway = "+pathway);
							}

							Set<String> pathwayID = new TreeSet<String>();
							rs = stmt.executeQuery("SELECT pathway_has_enzyme.pathway_idpathway FROM pathway_has_enzyme " +
									"LEFT JOIN reaction_has_enzyme ON pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein AND pathway_has_enzyme.enzyme_ecnumber = reaction_has_enzyme.enzyme_ecnumber " +
									"LEFT JOIN pathway_has_reaction ON pathway_has_enzyme.pathway_idpathway = pathway_has_reaction.pathway_idpathway " +
									"WHERE reaction_has_enzyme.reaction_idreaction = pathway_has_reaction.reaction_idreaction " +
									"AND pathway_has_enzyme.enzyme_ecnumber = '"+ecnumber+"'  AND pathway_has_enzyme.enzyme_protein_idprotein = '"+idProtein+"' AND reaction_has_enzyme.reaction_idreaction = "+rowID);

							while(rs.next()){pathwayID.add(rs.getString(1));}

							pathwayID.remove(pathway);

							if(reactionsID.size()==0) {

								stmt.execute("DELETE FROM reaction_has_enzyme WHERE reaction_has_enzyme.enzyme_ecnumber='"+ecnumber+"'  AND reaction_has_enzyme.enzyme_protein_idprotein = '"+idProtein+"' " +
										" AND reaction_has_enzyme.reaction_idreaction='"+rowID+"'");								
							}
						}
					}
				}
				else {

					//when pathways are deleted, they are just removed from the pathway has reaction association
					for(String pathway:existingPathwaysID.keySet()) {

						stmt.execute("DELETE FROM pathway_has_reaction WHERE reaction_idreaction = "+rowID+" AND pathway_idpathway = "+pathway);
					}
				}
			}

			Map<String,String> existingMetabolitesID = new HashMap<String,String>();
			rs = stmt.executeQuery("SELECT compound_idcompound, stoichiometric_coefficient FROM stoichiometry WHERE reaction_idreaction = "+rowID);

			while(rs.next()) {

				if(rs.getString(2).startsWith("-")) {

					existingMetabolitesID.put("-"+rs.getString(1),rs.getString(2));
				}
				else {

					existingMetabolitesID.put(rs.getString(1),rs.getString(2));
				}
			}

			for(String m: new ArrayList<String>(metabolites.keySet())) {

				if(existingMetabolitesID.keySet().contains(m) && existingMetabolitesID.get(m).equalsIgnoreCase(metabolites.get(m))) {

					existingMetabolitesID.remove(m);
				}
			}

			for(String compound:existingMetabolitesID.keySet()) {

				stmt.execute("DELETE FROM stoichiometry " +
						"WHERE reaction_idreaction = "+rowID+
						" AND compound_idcompound = "+compound.replace("-", "") +
						" AND stoichiometric_coefficient = '"+existingMetabolitesID.get(compound)+"'");
			}

			for(String m :metabolites.keySet()) {

				rs = stmt.executeQuery("SELECT idcompartment FROM compartment WHERE name = '" + compartment.get(m)+ "'");
				rs.next();
				idCompartment = rs.getString(1);


				rs = stmt.executeQuery("SELECT idstoichiometry FROM stoichiometry " +
						"WHERE reaction_idreaction = "+rowID+" AND compound_idcompound = '" + m.replace("-", "") +
						"' AND stoichiometric_coefficient = '" + metabolites.get(m) + "'");

				if(rs.next()) {

					String idstoichiometry = rs.getString(1);

					stmt.execute("UPDATE stoichiometry SET " +
							"stoichiometric_coefficient = '" + metabolites.get(m) + "', " +
							"compartment_idcompartment = " + idCompartment + ", " +
							"compound_idcompound = " + m.replace("-", "") + ", " +
							"numberofchains = '" + chains.get(m) + "' " +
							"WHERE idstoichiometry ='"+idstoichiometry+ "'");
				}
				else {

					stmt.execute("INSERT INTO stoichiometry (stoichiometric_coefficient, reaction_idreaction, compartment_idcompartment,compound_idcompound,numberofchains) " +
							"VALUES('" + metabolites.get(m) + "',"+rowID+","+idCompartment+", '" + m.replace("-", "") + "', '" + chains.get(m) + "')" );
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
	}


	/**
	 * @param rowID
	 * @return
	 */
	public String[] getReaction(String rowID) {

		Statement stmt;
		String[] res = null;

		try  {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT reaction.name, equation, reversible, pathway.name, inModel, compartment.name, isSpontaneous, isNonEnzymatic, isGeneric,"
							+ " lowerBound, upperBound  " +
							"FROM reaction " +
							"LEFT JOIN pathway_has_reaction ON idreaction=reaction_idreaction " +
							"LEFT JOIN pathway ON pathway_idpathway = idpathway " +
							"LEFT JOIN compartment ON compartment_idcompartment = compartment.idcompartment " +
							"WHERE idreaction = "+rowID
					);

			ResultSetMetaData rsmd = rs.getMetaData();
			boolean exists = rs.last();

			if(exists) {

				res = new String[rsmd.getColumnCount()];
				rs.first();
				int col=1;

				while(col<rsmd.getColumnCount()+1) {

					res[col-1] = rs.getString(col);
					col++;
				}

				rs.close();
				stmt.close();
			}
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * @param rowID
	 * @return
	 */
	public Map<String, MetaboliteContainer> getMetabolites(String rowID) {

		Statement stmt;
		Map<String, MetaboliteContainer> res = new TreeMap<String, MetaboliteContainer>();
		try  {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT idcompound, compound.name, compound.formula, " +
							"stoichiometric_coefficient, " +
							"numberofchains, " +
							"compartment.name " +
							", idstoichiometry " +
							"FROM stoichiometry " +
							"JOIN compound " +
							"ON idcompound=compound_idcompound " +
							"JOIN compartment " +
							"ON (compartment_idcompartment=idcompartment) " +
							"WHERE reaction_idreaction = '"+rowID+"'"
					);

			while(rs.next()) {

				MetaboliteContainer metaboliteContainer = new MetaboliteContainer(rs.getString(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getString(5), rs.getString(6)) ;
				res.put(rs.getString(7), metaboliteContainer);

			}
			rs.close();
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		return res;
	}

	/**
	 * @return
	 */
	public String[][] getAllMetabolites() {

		Statement stmt;
		String[][] res = null;

		try  {

			res = new String[4][];
			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT name, formula, idcompound, kegg_id FROM compound ORDER BY IF(ISNULL(name),1,0),name; ");
			rs.last();
			res[0] = new String[rs.getRow()+1];
			res[1] = new String[rs.getRow()+1];
			res[2] = new String[rs.getRow()+1];
			res[3] = new String[rs.getRow()+1];
			rs.beforeFirst();

			res[0][0]= "";
			res[1][0]= "";
			res[2][0]= "";
			res[3][0]= "";

			int m=1;
			while(rs.next()) {

				res[0][m] = rs.getString(3);

				if(rs.getString(1)!= null && rs.getString(2)!= null) {

					res[1][m] = rs.getString(1)+"__"+rs.getString(2)+"__"+rs.getString(4);
					res[3][m] = rs.getString(4)+"_"+rs.getString(1)+"_"+rs.getString(2);
				}
				else if(rs.getString(1) == null && rs.getString(2)!= null) {

					res[1][m] = rs.getString(2)+"__"+rs.getString(4);
					res[3][m] = rs.getString(4)+"_"+rs.getString(2);
				}
				else if(rs.getString(1)!= null && rs.getString(2) == null) {

					res[1][m] = rs.getString(1)+"__"+rs.getString(4);
					res[3][m] = rs.getString(4)+"_"+rs.getString(1);
				}
				else {

					res[1][m] = rs.getString(4);
					res[3][m] = rs.getString(4);
				}

				res[2][m] = rs.getString(1);

				m++;
			}
			rs.close();
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		return res;
	}

	/**
	 * colorize pathways
	 */
	public void colorPaths(){
		//this.setPaths(this.getPathsBoolean(false));
		List<Color> usedColors = new ArrayList<Color>(); 
		this.pathwayColors= new TreeMap<Integer,Color>();
		String[] paths = getPathsBoolean(false);
		for(Integer path=0; path<paths.length; path++)
		{
			usedColors = this.newColor(usedColors, generateColor(), path);
		}
	}

	/**
	 * @param usedColors
	 * @param color
	 * @param path
	 * @return
	 */
	private List <Color> newColor(List <Color> usedColors, Color color, Integer path){
		if(usedColors.contains(color) || color.equals(new Color(0,0,0)) || color.equals(new Color(255,255,255)))
		{
			newColor(usedColors, generateColor(), path);
		}
		else
		{
			usedColors.add(color);
			this.pathwayColors.put(path, color);
		}
		return usedColors;		
	}

	/**
	 * @return
	 */
	private Color generateColor(){
		//		int red = new Random().nextInt(70);
		int red = new Random().nextInt(256);
		int green = new Random().nextInt(256);
		//		while(green<200){green = new Random().nextInt(225);}
		int blue = new Random().nextInt(256);
		//		while(blue<112){blue = new Random().nextInt(225);}

		return new Color(red, green, blue);
	}

	public Map<Integer, Color> getPathwayColors() {
		return pathwayColors;
	}

	/**
	 * @param pathwayID
	 * @return
	 */
	public List<Set<String>> get_enzymes_id_list(int pathwayID) {

		Statement stmt;
		List<Set<String>> result = new ArrayList<Set<String>>();
		Set<String> enzymes = new HashSet<String>();
		Map<String,Set<String>> removedEnzymes = new HashMap<String,Set<String>>();
		Set<String> reactions = new HashSet<String>();

		try {

			stmt = this.connection.createStatement();

			String aux = "";
			if(this.getProject().isCompartmentalisedModel()) {

				aux = aux.concat(" WHERE NOT originalReaction ");
			}
			else {

				aux = aux.concat(" WHERE originalReaction ");
			}

			ResultSet rs = stmt.executeQuery("SELECT reaction_has_enzyme.enzyme_ecnumber, reaction_has_enzyme.enzyme_protein_idprotein, reaction.inModel, reaction.name, enzyme.inModel, protein.name FROM pathway_has_enzyme "+
					"LEFT JOIN reaction_has_enzyme ON (pathway_has_enzyme.enzyme_ecnumber = reaction_has_enzyme.enzyme_ecnumber AND pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein) "+
					"LEFT JOIN reaction ON (reaction.idreaction = reaction_idreaction) "+
					"LEFT JOIN pathway_has_reaction ON (pathway_has_reaction.reaction_idreaction = reaction.idreaction) "+
					"LEFT JOIN enzyme ON (enzyme.ecnumber = reaction_has_enzyme.enzyme_ecnumber AND enzyme.protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein) "+
					" LEFT JOIN protein ON protein.idprotein = protein_idprotein " +
					aux+
					"AND pathway_has_enzyme.pathway_idpathway = "+pathwayID+" AND pathway_has_reaction.pathway_idpathway = "+pathwayID);

			while(rs.next()) {

				String reactions_id = rs.getString(4);

				if(reactions_id.contains("_")) {

					reactions_id=reactions_id.substring(0,reactions_id.indexOf("_"));
				}

				if(rs.getBoolean(3) && rs.getBoolean(5)) {

					enzymes.add(rs.getString(1)+"___"+rs.getString(6)+"___"+rs.getString(2));

					reactions.add(reactions_id);
				}

				if(rs.getBoolean(3)) {

					reactions.add(reactions_id);
				}

				String surrogateEnzID = rs.getString(1)+"___"+rs.getString(6)+"___"+rs.getString(2);

				Set<String> reactionsSet = new HashSet<String>();
				if(removedEnzymes.containsKey(surrogateEnzID)) {

					reactionsSet = removedEnzymes.get(surrogateEnzID);
				}

				reactionsSet.add(reactions_id);
				removedEnzymes.put(surrogateEnzID, reactionsSet);
			}

			for(String ecn:removedEnzymes.keySet()) {

				boolean remove = false;

				for(String rea : removedEnzymes.get(ecn))
					for(String r : this.getGapReactions())
						if(r.contains(rea))
							remove = true;

				removedEnzymes.get(ecn).removeAll(reactions);

				if(remove)
					enzymes.remove(ecn);

				else if(!removedEnzymes.get(ecn).isEmpty()) {

					enzymes.remove(ecn);
				}
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		result.add(0,enzymes);
		result.add(1,reactions);
		return result;
	}

	/**
	 * @param pathwayID
	 * @return
	 */
	public List<Set<String>> get_reactions_id_list(int pathwayID) {

		Statement stmt;
		Set<String> reactions = new HashSet<String>(), removeReactions = new HashSet<String>();

		try {

			stmt = this.connection.createStatement();

			String aux = "";
			if(this.getProject().isCompartmentalisedModel()) {

				aux = aux.concat(" WHERE NOT originalReaction ");
			}
			else {

				aux = aux.concat(" WHERE originalReaction ");
			}

			ResultSet rs = stmt.executeQuery("SELECT name FROM pathway_has_reaction "+
					"LEFT JOIN reaction ON (pathway_has_reaction.reaction_idreaction = reaction.idreaction) "+
					"LEFT JOIN reaction_has_enzyme ON (reaction_has_enzyme.reaction_idreaction = reaction.idreaction) "+
					//"LEFT JOIN pathway_has_enzyme ON (pathway_has_enzyme.enzyme_ecnumber = reaction_has_enzyme.enzyme_ecnumber AND pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein)"+					
					aux+
					" AND inModel " +
					" AND (isNonEnzymatic OR isSpontaneous OR source='MANUAL' OR reaction_has_enzyme.enzyme_ecnumber IS NULL) " +
					" AND pathway_idpathway = "+pathwayID);


			while(rs.next()) {

				String composed_id = rs.getString(1);
				String reactions_id = rs.getString(1);

				if(reactions_id.contains("_")) {

					reactions_id=reactions_id.substring(0,reactions_id.indexOf("_"));
				}

				if(this.getGapReactions().contains(composed_id))
					removeReactions.add(reactions_id);

				if(removeReactions.contains(reactions_id) && !this.getGapReactions().contains(composed_id))
					removeReactions.remove(reactions_id);

				reactions.add(reactions_id);
			}

			//			List<String[]> proteins = new ArrayList<String[]>();
			//
			//			rs = stmt.executeQuery("SELECT enzyme_protein_idprotein, enzyme_ecnumber FROM pathway_has_enzyme "+
			//					"WHERE pathway_idpathway = "+pathwayID);
			//
			//			while(rs.next()) {
			//
			//				proteins.add(new String[] {rs.getString(1),rs.getString(2)});
			//			}
			//
			//			for(String[] protein:proteins) {
			//
			//				rs = stmt.executeQuery("SELECT name  FROM reaction_has_enzyme "+
			//						"LEFT JOIN reaction ON (reaction_has_enzyme.reaction_idreaction = reaction.idreaction) "+
			//						"WHERE inModel AND enzyme_protein_idprotein= "+protein[0]+" AND enzyme_ecnumber = '"+protein[1]+"'");
			//				
			//				System.out.println("SELECT name  FROM reaction_has_enzyme "+
			//						"LEFT JOIN reaction ON (reaction_has_enzyme.reaction_idreaction = reaction.idreaction) "+
			//						"WHERE inModel AND enzyme_protein_idprotein= "+protein[0]+" AND enzyme_ecnumber = '"+protein[1]+"'");
			//
			//				while(rs.next()) {
			//
			//					if(reactions.contains(rs.getString(1))) {
			//
			//						reactions.remove(rs.getString(1));
			//					}
			//				}
			//
			//			}

			rs.close();
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		List<Set<String>> ret =  new ArrayList<Set<String>>();
		ret.add(0, reactions);
		ret.add(1, removeReactions);

		return ret;
	}

	/**
	 * @return
	 */
	public String[] getCompartments(boolean isMetabolites) {

		Statement stmt;
		ArrayList<String> cls = new ArrayList<String>();

		try {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT idcompartment, name FROM compartment ");

			while(rs.next()) {

				boolean addCompartment = true; 

				if(super.getProject().isCompartmentalisedModel() && (rs.getString(2).contains("inside") || rs.getString(2).contains("ouside"))) {

					addCompartment = false;
				}

				if(isMetabolites && rs.getString(2).contains("membrane")) {
					addCompartment = false;
				}

				if(addCompartment) {

					cls.add(rs.getString(2));
				}
			}
			rs.close();
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		String[] res = new String[cls.size()];

		for(int i=0;i<cls.size();i++) {

			res[i] = cls.get(i);
		}

		return res;
	}

	/**
	 * @return the selectedPathIndexID
	 */
	public Map<Integer, Integer> getSelectedPathIndexID() {
		return selectedPathIndexID;
	}


	/**
	 * @return
	 */
	public Map<Integer,String> getIds() {

		return ids;
	}

	/**
	 * @param name
	 * @param stmt
	 * @return
	 * @throws SQLException
	 */
	private String incrementName(String name, Statement stmt) throws SQLException {

		if(name.contains(".")) {

			String[] rName = name.split("\\.");
			int version = Integer.parseInt(rName[1]);
			version=version+1;
			name=name.replace("."+rName[1], "."+version);
		}
		else{name=name.concat(".1");}

		ResultSet rs = stmt.executeQuery("SELECT * FROM reaction WHERE name = '"+name+"'");

		if(rs.next()){name = incrementName(name, stmt);}

		rs.close();
		stmt.close();

		return name;
	}

	/**
	 * @return
	 */
	public String[] getPaths() {

		return paths;
	}


	/**
	 * @param paths
	 */
	public void setPaths(String[] paths) {
		this.paths = paths;
	}

	/**
	 * @return
	 */
	public boolean existGenes(){

		Statement stmt; 

		try {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM gene");
			boolean result = rs.next();

			rs.close();
			stmt.close();

			return result;
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return false;
	}


	/**
	 * @return the tableColumnsSize
	 */
	public Integer[] getTableColumnsSize() {

		return tableColumnsSize;
	}


	/**
	 * @param tableColumnsSize the tableColumnsSize to set
	 */
	public void setTableColumnsSize(Integer[] tableColumnsSize) {
		this.tableColumnsSize = tableColumnsSize;
	}


	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getName()
	 */
	public String getName() {

		return "Reactions";

	}

	/**
	 * @return
	 */
	public String getDefaultCompartment() {

		if(super.getProject().isCompartmentalisedModel()) {

			Statement stmt;
			String interior = "inside";
			try {

				stmt = this.connection.createStatement();

				ResultSet rs = stmt.executeQuery("SELECT idcompartment, abbreviation FROM compartment;");

				while(rs.next()) {


					if( rs.getString(2).equalsIgnoreCase("cyto")) {

						interior = "cyto";
					}

					if( rs.getString(2).equalsIgnoreCase("cytop")) {

						interior = "cytop";
					}
				}

				rs.close();
				stmt.close();
			} 
			catch (SQLException e) {

				e.printStackTrace();
			}
			return interior;
		}
		else {

			return "inside";
		}
	}


	public void updateReactionProperties(String reactionID, int columnNumber, Object object) {

		try {

			Statement stmt = this.connection.createStatement();

			if(columnNumber==5) {

				stmt.execute("UPDATE reaction SET notes = '"+MySQL_Utilities.mysqlStrConverter((String) object)+"' WHERE idreaction='"+reactionID+"'");
			}
			else {

				boolean value = (Boolean) object;

				ResultSet rs = stmt.executeQuery("SELECT reversible, isGeneric, inModel FROM reaction WHERE idreaction='"+reactionID+"'");
				rs.next();

				if((columnNumber == 6 && value!=rs.getBoolean(1)) || (columnNumber == 7 && value!=rs.getBoolean(2)) || (columnNumber == 8 && value!=rs.getBoolean(3))) {

					String equation="", source="";

					rs = stmt.executeQuery("SELECT equation, source FROM reaction WHERE idreaction='"+reactionID+"'");

					if(rs.next()) {

						equation = rs.getString(1);
						source = rs.getString(2);
					}

					if(columnNumber==6) {

						if(value) {

							equation=equation.replace(" => ", " <=> ").replace(" <= ", " <=> ");
						}
						else {

							equation=equation.replace("<=>", "=>");
						}
						stmt.execute("UPDATE reaction SET equation = '"+MySQL_Utilities.mysqlStrConverter(equation)+"', reversible = " + value + " WHERE idreaction='"+reactionID+"'");	
					}
					else if(columnNumber==7) {

						stmt.execute("UPDATE reaction SET isGeneric = " + value + " WHERE idreaction='"+reactionID+"'");
					}
					else {

						if(source.equalsIgnoreCase("KEGG")) {

							source = "MANUAL";
						}

						stmt.execute("UPDATE reaction SET inModel = " + value + ", source='"+source+"' WHERE idreaction='"+reactionID+"'");
					}
				}
				rs.close();
			}
			stmt.close();
		} 
		catch (SQLException e) {

			e.printStackTrace();
		}
	}


	/**
	 * @return the activeReactions
	 */
	public Set<String> getActiveReactions() {
		return activeReactions;
	}


	/**
	 * @param activeReactions the activeReactions to set
	 */
	public void setActiveReactions(Set<String> activeReactions) {
		this.activeReactions = activeReactions;
	}


	/**
	 * @return the gapReactions
	 */
	public Set<String> getGapReactions() {
		return gapReactions;
	}


	/**
	 * @param gapReactions the gapReactions to set
	 */
	public void setGapReactions(Set<String> gapReactions) {
		this.gapReactions = gapReactions;
	}


	/**
	 * @return the balanceValidator
	 */
	public BalanceValidator getBalanceValidator() {
		return balanceValidator;
	}


	/**
	 * @param balanceValidator the balanceValidator to set
	 */
	public void setBalanceValidator(BalanceValidator balanceValidator) {
		this.balanceValidator = balanceValidator;
	}


	/**
	 * @return the externalModelIds
	 */
	public Map<String, String> getExternalModelIds() {
		return externalModelIds;
	}


	/**
	 * @param externalModelIds the externalModelIds to set
	 */
	public void setExternalModelIds(Map<String, String> externalModelIds) {
		this.externalModelIds = externalModelIds;
	}
}
