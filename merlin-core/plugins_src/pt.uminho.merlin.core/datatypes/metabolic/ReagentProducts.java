package datatypes.metabolic;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.Table;
import datatypes.metabolic_regulatory.Entity;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

@Datatype(structure=Structure.SIMPLE,namingMethod="getName",removable=true,removeMethod ="remove")
public class ReagentProducts extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Connection connection;

	/**
	 * @param dbt
	 * @param name
	 */
	public ReagentProducts(Table dbt, String name) {
		super(dbt, name);
		this.connection=table.getConnection();
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getName()
	 */
	public String getName() {
		return "Reagent/Products";
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		List<String[]> res = new ArrayList<String[]>();
		Statement stmt;

		try {

			stmt = this.connection.createStatement();

			String aux = "";
			if(this.getProject().isCompartmentalisedModel()) {

				aux = aux.concat(" WHERE NOT originalReaction");
			}
			else {

				aux = aux.concat(" WHERE originalReaction");
			}	

			ResultSet rs = stmt.executeQuery("SELECT compound_idcompound,stoichiometric_coefficient,reaction_idreaction " +
					"FROM stoichiometry "+
					"LEFT JOIN reaction ON (reaction.idreaction=reaction_idreaction) "+
					aux);

			Set<String> reactants = new HashSet<String>();
			Set<String> products = new HashSet<String>();
			Set<String> reactionsReactants = new HashSet<String>();
			Set<String> productsReactants = new HashSet<String>();

			while(rs.next()) {

				if(rs.getString(2).startsWith("-")){reactants.add(rs.getString(1));reactionsReactants.add(rs.getString(3));}
				else{products.add(rs.getString(1));productsReactants.add(rs.getString(3));}
			}

			res.add(new String[] {"Number of reagents", ""+reactants.size()});
			res.add(new String[] {"Number of products", ""+products.size()});
			res.add(new String[] {"Number of reactions with reagents associated", ""+reactionsReactants.size()});
			res.add(new String[] {"Number of reactions with products associated", ""+productsReactants.size()});

			rs = stmt.executeQuery("SELECT compartment.name, count(distinct(compound_idcompound)) " +
					"FROM stoichiometry " +
					"JOIN compartment ON compartment.idcompartment =  stoichiometry.compartment_idcompartment " +
					"LEFT JOIN reaction ON (reaction.idreaction=reaction_idreaction) "+
					aux+
					" AND stoichiometric_coefficient REGEXP '(^-)' " +
					"GROUP BY compartment.name"
					);
			res.add(new String[] {});
			while(rs.next()) {

				res.add(new String[] {"Number of reagents in compartment "+rs.getString(1),	rs.getString(2)});
			}

			rs = stmt.executeQuery("SELECT compartment.name, count(distinct(compound_idcompound)) " +
					"FROM stoichiometry " +
					"JOIN compartment ON compartment.idcompartment =  stoichiometry.compartment_idcompartment " +
					"LEFT JOIN reaction ON (reaction.idreaction=reaction_idreaction) "+
					aux+
					" AND  stoichiometric_coefficient NOT REGEXP '(^-)' " +
					"GROUP BY compartment.name"
					);
			res.add(new String[] {});
			while(rs.next()) {

				res.add(new String[] {"Number of products in compartment "+rs.getString(1),	rs.getString(2)});
			}

			Set<String> metabolites = new HashSet<String>(products);
			metabolites.retainAll(reactants);

			res.add(new String[] {});
			res.add(new String[] {"Metabolites that are reagents and products", ""+metabolites.size()});

			rs.close();
			stmt.close();
		}
		catch(Exception e)
		{e.printStackTrace();}

		String[][] newRes = new String[res.size()][];

		for(int i=0; i<res.size() ;i++) {

			newRes[i] = res.get(i);
		}

		return newRes;
	}

	/**
	 * @param selection
	 * @return
	 */
	public GenericDataTable getDataReagentProduct(int selection, boolean encoded) {

		ArrayList<String> index = new ArrayList<String>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Info");
		columnsNames.add("Metabolite");
		columnsNames.add("Compartment");
		columnsNames.add("Formula");
		columnsNames.add("KEGG ID");
		columnsNames.add("Number of Metabolic Reactions");
		columnsNames.add("Number of Transport Reactions");

		GenericDataTable qrt = new GenericDataTable(columnsNames, this.name, "Reagents/Products") {
			
			private static final long serialVersionUID = -7338505361235325298L;
			@Override
			public boolean isCellEditable(int row, int col) {

				if (col==0) {

					return true;
				}
				else return false;
			}};

			Statement stmt;

			try {

				stmt = this.connection.createStatement();
				ResultSet rs;

				String aux = "";

				if(this.getProject().isCompartmentalisedModel()) {

					aux = aux.concat(" WHERE NOT originalReaction");
				}
				else {

					aux = aux.concat(" WHERE originalReaction");
				}
				
				if(encoded) {

					aux = aux.concat(" AND inModel ");
				}


				rs = stmt.executeQuery("SELECT compound.name, formula, COUNT(DISTINCT SIGN(stoichiometric_coefficient)), SIGN(stoichiometric_coefficient)," +
						" compound.idcompound, kegg_id , COUNT(DISTINCT(idreaction)), " +
						" COUNT(DISTINCT(IF(reaction.name NOT LIKE 'T%', idreaction, NULL))) AS sum_not_transport, "+
						" COUNT(DISTINCT(IF(reaction.name LIKE 'T%', idreaction, NULL))) AS sum_transport, " +
						" reversible, compartment.name " +
						" FROM stoichiometry " +
						" JOIN compound ON compound_idcompound=idcompound " +
						" JOIN compartment ON stoichiometry.compartment_idcompartment=compartment.idcompartment " +
						" LEFT JOIN reaction ON (reaction.idreaction=reaction_idreaction) "+
						aux+
						" GROUP BY kegg_id, stoichiometry.compartment_idcompartment " +
						" ORDER BY compound.name AND kegg_id ;"
						);

				while(rs.next()) {

					if(!index.contains(rs.getString(5)+"_"+rs.getString(11))) {

						List<Object> ql = new ArrayList<Object>();

						if(rs.getInt(3)>1 || rs.getBoolean(10)) {

							index.add(rs.getString(5)+"_"+rs.getString(11));
							ql.add("");
							ql.add(rs.getString(1));
							ql.add(rs.getString(11));
							ql.add(rs.getString(2));
							ql.add(rs.getString(6));
							ql.add(rs.getInt(8));
							ql.add(rs.getInt(9));
							qrt.addLine(ql, "Both", rs.getString(5));
						}
						else if(rs.getDouble(4)>0 && selection != 2) {

							index.add(rs.getString(5)+"_"+rs.getString(11));
							ql.add("");
							ql.add(rs.getString(1));
							ql.add(rs.getString(11));
							ql.add(rs.getString(2));
							ql.add(rs.getString(6));
							ql.add(rs.getInt(8));
							ql.add(rs.getInt(9));
							qrt.addLine(ql, "Product", rs.getString(5));
						}
						else if(rs.getDouble(4)<0 && selection != 3) {

							index.add(rs.getString(5)+"_"+rs.getString(11));
							ql.add("");
							ql.add(rs.getString(1));
							ql.add(rs.getString(11));
							ql.add(rs.getString(2));
							ql.add(rs.getString(6));
							ql.add(rs.getInt(8));
							ql.add(rs.getInt(9));
							qrt.addLine(ql, "Reagent", rs.getString(5));
						}
					}
				}
				rs.close();
				stmt.close();
			} 
			catch (SQLException e) {e.printStackTrace();}

			return qrt;
	}

	/**
	 * @param rec
	 * @return
	 */
	public DataTable getReaction(String rec, String compartment) {

		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Reaction Name");
		columnsNames.add("Equations");
		columnsNames.add("Source");
		columnsNames.add("In Model");
		columnsNames.add("Reversible");

		DataTable qrt = new GenericDataTable(columnsNames, this.name, "");

		Statement stmt;

		try {

			stmt = this.connection.createStatement();

			String aux = "";
			if(this.getProject().isCompartmentalisedModel()) {

				aux = aux.concat(" WHERE NOT originalReaction ");
			}
			else {

				aux = aux.concat(" WHERE originalReaction ");
			}	

			ResultSet rs = stmt.executeQuery(
					"SELECT distinct(idreaction), reaction.name, equation, source, inModel, reversible FROM reaction " +
							"JOIN stoichiometry ON reaction.idreaction = stoichiometry.reaction_idreaction " +
							"JOIN compartment ON compartment.idcompartment =  stoichiometry.compartment_idcompartment " +
							aux+" AND compartment.name='"+compartment+"' " +
							" AND stoichiometry.compound_idcompound = "+rec+
							" ORDER BY inModel DESC, source, reversible DESC, name ASC"
					);
			
			while(rs.next()) {

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(2));
				ql.add(rs.getString(3));
				ql.add(rs.getString(4));
				
				if(rs.getBoolean(5)) {

					ql.add(rs.getBoolean(5)+"");
				}
				else {

					ql.add("-");
				}

				if(rs.getBoolean(6)) {

					ql.add(rs.getBoolean(6)+"");
				}
				else {

					ql.add("-");
				}


				qrt.addLine(ql);
			}
			rs.close();
			stmt.close();
		} 
		catch (SQLException e) {e.printStackTrace();}
		return qrt;
	}
}
