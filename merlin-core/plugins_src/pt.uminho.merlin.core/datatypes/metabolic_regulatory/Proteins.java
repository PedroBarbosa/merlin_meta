package datatypes.metabolic_regulatory;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.Table;

public class Proteins extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String,String> namesIndex;
	private TreeMap<Integer,String> ids;
	private TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy;
	private Connection connection;

	/**
	 * @param dbt
	 * @param name
	 * @param ultimlyComplexComposedBy
	 */
	public Proteins(Table dbt, String name, TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy) {

		super(dbt, name);
		this.ultimlyComplexComposedBy = ultimlyComplexComposedBy;
		this.connection=dbt.getConnection();
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		int num=0;
		int noname=0;

		Statement stmt;
		String[][] res = new String[11][];
		
		try {
			
			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM protein");

			while(rs.next()) {
				
				num++;
				if(rs.getString("name")==null) {
					
					noname++;
				}
			}

			res[0] = new String[] {"Number of proteins", ""+num};
			
			res[1] = new String[] {"Number of proteins with no name associated", ""+noname};

			rs = stmt.executeQuery("SELECT count(*) FROM aliases where class = 'p'");
			rs.next();
			double snumproteins = rs.getDouble(1);
			res[3] = new String[] {"Number of proteins synonyms", snumproteins+""};

			double synmed = (snumproteins/num);
			res[4] = new String[] {"Average number synonyms by protein", ""+synmed};

			rs = stmt.executeQuery("SELECT count(distinct(protein_idprotein)) FROM enzyme WHERE source NOT LIKE 'TRANSPORTERS'");
			rs.next();
			res[6] = new String[] {"Number of proteins that are enzymes", rs.getString(1)};

			rs = stmt.executeQuery("SELECT count(distinct(protein_idprotein)) FROM enzyme WHERE source LIKE 'TRANSPORTERS'");
			rs.next();
			res[7] = new String[] {"Number of proteins that are transporters", rs.getString(1)};
			
		
//			rs = stmt.executeQuery("SELECT count(distinct(protein_idprotein)) FROM regulatory_event");
//			rs.next();
//			res[8] = new String[] {"Number of proteins that are transcription factors", rs.getString(1)};

		
			rs = stmt.executeQuery("SELECT count(distinct(protein_idprotein)) FROM protein_composition");
			rs.next();
			res[9] = new String[] {"Number of proteins that are complexes", rs.getString(1)};

		
			rs = stmt.executeQuery("SELECT * FROM subunit GROUP BY enzyme_protein_idprotein, enzyme_ecnumber");
			int p_g = 0;
			while(rs.next()) {
				
				p_g++;
			}
			res[10] = new String[] {"Number of proteins associated to genes",p_g+""};

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
	 * @return
	 */
	public GenericDataTable getAllProteins(boolean encodedOnly) {

		namesIndex = new HashMap<String,String>();
		ids = new TreeMap<Integer,String>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Info");
		columnsNames.add("Name");
		columnsNames.add("Class");
		//		columnsNames.add("InChi");
		columnsNames.add("Encoding genes");

		HashMap<String,Integer> index = new HashMap<String,Integer>();

		GenericDataTable qrt = new GenericDataTable(columnsNames, "Proteins", ""){
			private static final long serialVersionUID = 6629060675011336218L;
			@Override
			public boolean isCellEditable(int row, int col){
				
				if (col==0)
				{
					return true;
				}
				else return false;
			}
		};

		try {

			Statement stmt = this.connection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT subunit.enzyme_protein_idprotein " +
					"FROM gene JOIN subunit ON gene.idgene = subunit.gene_idgene");

			while(rs.next()) {
				
				if(!index.containsKey(rs.getString(1))) {
					

					index.put(rs.getString(1), new Integer(1));
				}
				else {
					
					Integer ne = new Integer(index.get(rs.getString(1)).intValue() + 1);
					index.put(rs.getString(1), ne);
				}

			}

			//stmt = dsa.createStatement();
			//		rs = stmt.executeQuery("SELECT idprotein, name, class, inchi FROM protein ORDER BY name");
			if(encodedOnly) {
				
				rs = stmt.executeQuery("SELECT DISTINCT(idprotein), name, class FROM protein " +
						"JOIN enzyme ON(enzyme.protein_idprotein=protein.idprotein) where inModel=1 ORDER BY name");
			}
			else {
				
				rs = stmt.executeQuery("SELECT idprotein, name, class FROM protein ORDER BY name");
			}

			int p=0;
			while(rs.next()) {
				
				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add("");
				ql.add(rs.getString(2));
				ql.add(rs.getString(3));        	

				String idp = rs.getString(1);
				if(this.ultimlyComplexComposedBy.containsKey(idp))
				{}
				else
				{}

				ids.put(p, idp);
				p++;


				if(index.containsKey(rs.getString(1)))
				{
					ql.add(index.get(rs.getString(1)).intValue()+"");
				}
				else
				{
					ql.add("0");
				}

				qrt.addLine(ql, rs.getString(1));

				if(rs.getString(2).equals("-"))
				{
					this.namesIndex.put(rs.getString(1), rs.getString(3));
				}
				else
				{
					this.namesIndex.put(rs.getString(1), rs.getString(2));
				}
			}

			rs.close();
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();}

		return qrt;
	}

	/**
	 * @return
	 */
	public GenericDataTable getEnzymes() {

		namesIndex = new HashMap<String,String>();
		ids = new TreeMap<Integer,String>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Info");
		columnsNames.add("Name");
		columnsNames.add("InChi");
		columnsNames.add("Encoding genes");

		HashMap<String,Integer> index = new HashMap<String,Integer>();

		GenericDataTable qrt = new GenericDataTable(columnsNames, "Proteins", ""){
			private static final long serialVersionUID = 6629060675011336218L;
			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;
			}
		};

		try {

			Statement stmt = this.connection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT subunit.enzyme_protein_idprotein, name " +
					"FROM gene JOIN subunit ON gene.idgene = subunit.gene_idgene");

			while(rs.next())
			{
				if(!index.containsKey(rs.getString(1)))
				{
					index.put(rs.getString(1), new Integer(1));
				}
				else
				{
					Integer ne = new Integer(index.get(rs.getString(1)).intValue() + 1);
					index.put(rs.getString(1), ne);
				}
			}

			//	stmt = dsa.createStatement();

			rs = stmt.executeQuery("SELECT distinct(idprotein), name, inchi " +
					"FROM protein JOIN enzyme ON enzyme_protein_idprotein=idprotein ORDER BY name");

			int p=0;
			while(rs.next())
			{
				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add("");
				ql.add(rs.getString(2));
				ql.add(rs.getString(3));
				ql.add(rs.getString(4));
				ql.add(rs.getString(5));

				String idp = rs.getString(1);

				if(index.containsKey(idp)) ql.add(index.get(idp).intValue()+"");
				else ql.add("0");

				this.namesIndex.put(idp, rs.getString(2));
				this.ids.put(p, idp);
				p++;
				qrt.addLine(ql, idp);
			}

			rs.close();
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return qrt;
	}

	/**
	 * @return
	 */
	public GenericDataTable getTFs() {

		namesIndex = new HashMap<String,String>();
		ids = new TreeMap<Integer,String>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Info");
		columnsNames.add("Name");
		columnsNames.add("InChi");
		columnsNames.add("Number of encoding genes");

		HashMap<String,Integer> index = new HashMap<String,Integer>();

		GenericDataTable qrt = new GenericDataTable(columnsNames, "Proteins", ""){
			private static final long serialVersionUID = 6629060675011336218L;
			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;
			}
		};

		try {

			Statement stmt = this.connection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT subunit.enzyme_protein_idprotein, name " +
					"FROM gene JOIN subunit ON gene.idgene = subunit.gene_idgene");

			while(rs.next())
			{
				if(!index.containsKey(rs.getString(1)))
				{
					index.put(rs.getString(1), new Integer(1));
				}
				else
				{
					Integer ne = new Integer(index.get(rs.getString(1)).intValue() + 1);
					index.put(rs.getString(1), ne);
				}
			}

			//stmt = dsa.createStatement();

			rs = stmt.executeQuery("SELECT distinct(idprotein), name, inchi " +
					"FROM protein " +
					"JOIN regulatory_event ON enzyme_protein_idprotein=idprotein ORDER BY name");

			int p=0;
			while(rs.next())
			{
				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add("");
				ql.add(rs.getString(2));
				ql.add(rs.getString(3));
				ql.add(rs.getString(4));
				ql.add(rs.getString(5));

				String idp = rs.getString(1);

				if(index.containsKey(idp)) ql.add(index.get(idp).intValue()+"");
				else ql.add("0");

				ids.put(p, idp);
				p++;

				qrt.addLine(ql, rs.getString(1));

				this.namesIndex.put(rs.getString(1), rs.getString(2));
			}

			rs.close();
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return qrt;
	}

	/**
	 * @return
	 */
	public GenericDataTable getSigmas() {

		namesIndex = new HashMap<String,String>();
		ids = new TreeMap<Integer,String>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Info");
		columnsNames.add("Name");
		columnsNames.add("InChi");
		columnsNames.add("Number of encoding genes");

		HashMap<String,Integer> index = new HashMap<String,Integer>();

		GenericDataTable qrt = new GenericDataTable(columnsNames, "Proteins", ""){
			private static final long serialVersionUID = 6629060675011336218L;
			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;
			}
		};

		try
		{
			//MySQLMultiThread dsa =  new MySQLMultiThread( this.host,this.port, this.dbName, this.user, this.pass);
			Statement stmt = this.connection.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT subunit.enzyme_protein_protein_idprotein, name " +
					"FROM gene JOIN subunit ON gene.idgene = subunit.gene_idgene");

			while(rs.next())
			{
				if(!index.containsKey(rs.getString(1)))
				{
					Integer ne = new Integer(1);
					index.put(rs.getString(1), ne);
				}
				else
				{
					Integer ne = new Integer(index.get(rs.getString(1)).intValue() + 1);
					index.put(rs.getString(1), ne);
				}
			}

			//	stmt = dsa.createStatement();
			rs = stmt.executeQuery("SELECT distinct(idprotein), name, iupac_name, inchi, " +
					"cas_registry_name FROM protein " +
					"JOIN sigma_promoter ON protein_protein_idprotein=idprotein ORDER BY name");

			int p=0;
			while(rs.next())
			{
				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add("");
				ql.add(rs.getString(2));
				ql.add(rs.getString(3));
				ql.add(rs.getString(4));
				ql.add(rs.getString(5));

				String idp = rs.getString(1);

				if(index.containsKey(idp)) ql.add(index.get(idp).intValue()+"");
				else ql.add("0");

				ids.put(p, idp);
				p++;

				qrt.addLine(ql, rs.getString(1));

				this.namesIndex.put(rs.getString(1), rs.getString(2));
			}

			rs.close();
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return qrt;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getReactionName(String id) {

		return this.namesIndex.get(id);
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getRowInfo(java.lang.String)
	 */
	public DataTable[] getRowInfo(String id) {

		DataTable[] res = new DataTable[2];

		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Synonyms");

		res[1] = new DataTable(columnsNames, "Synonyms");

		columnsNames = new ArrayList<String>();

		columnsNames.add("Encoding Genes");
		columnsNames.add("Locus Tag");

		res[0] = new DataTable(columnsNames, "Encoding Genes");

		Statement stmt;
		try {
			
			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT alias FROM aliases WHERE class = 'p' AND entity = " + id);
			
			while(rs.next()) {
				
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(1));
				res[1].addLine(ql);
			}

			rs = stmt.executeQuery("SELECT name, locusTag FROM gene JOIN subunit ON " +
					"gene.idgene = subunit.gene_idgene WHERE enzyme_protein_idprotein = " + id);
			while(rs.next())
			{
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(rs.getString(1));
				ql.add(rs.getString(2));
				res[0].addLine(ql);
			}
			rs.close();
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();}

		return res;
	}

	/**
	 * @param row
	 */
	public void RemoveProtein(int row, boolean encodedOnly) {

		Statement stmt;
		try {

			stmt= this.connection.createStatement();
			ResultSet rs ;

			if(row==-1) {

				String aux ="";
				if(encodedOnly) {
					
					aux = " RIGHT JOIN enzyme ON (protein.idprotein = protein_idprotein) WHERE inModel";
				}
				rs = stmt.executeQuery("SELECT * FROM protein"+aux);
				
				Set<String> proteins = new HashSet<String>();

				while(rs.next()) {

					proteins.add(rs.getString(1));
				}

				for(String protein_id : proteins) {

					rs = stmt.executeQuery( "SELECT ecnumber, inModel FROM enzyme WHERE protein_idprotein ="+protein_id);
					
					List<String> enzymes_ids = new ArrayList<String>();

					int i = 0;
					rs.last();
					Boolean[] inModel = new Boolean[rs.getRow()];
					rs.beforeFirst();
					while(rs.next()) {

						enzymes_ids.add(i,rs.getString(1));
						inModel[i] = rs.getBoolean(2);
					}

					for(String enz : enzymes_ids) {

						Proteins.removeEnzymesAssignmensts(enz,enzymes_ids,inModel,stmt,protein_id,true);
					}
					stmt.execute("DELETE FROM protein where idprotein = "+protein_id);
				}
			}
			else {

				rs = stmt.executeQuery( "SELECT ecnumber, inModel FROM enzyme WHERE protein_idprotein ="+ids.get(row));
				List<String> enzymes_ids = new ArrayList<String>();

				int i = 0;
				rs.last();
				Boolean[] inModel = new Boolean[rs.getRow()];
				rs.beforeFirst();
				while(rs.next()) {

					enzymes_ids.add(i,rs.getString(1));
					inModel[i] = rs.getBoolean(2);
				}

				for(String enz : enzymes_ids) {
					
					Proteins.removeEnzymesAssignmensts(enz,enzymes_ids,inModel,stmt,ids.get(row), true);
				}

				stmt.execute("DELETE FROM protein where idprotein = '"+ids.get(row)+"'");
			}
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
	}

	/**
	 * @param selectedRow
	 * @return
	 */
	public String[] getProteinData(int selectedRow) {

		Statement stmt;
		String[][] res = null;
		String[] data = new String[11];

		try {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM protein WHERE idprotein ="+ids.get(selectedRow));
			ResultSetMetaData rsmd = rs.getMetaData();
			rs.last();
			res = new String[rs.getRow()][rsmd.getColumnCount()];
			rs.first();
			int row=0;

			while(row<res.length) {

				int col=1;
				while(col<rsmd.getColumnCount()+1) {

					res[row][col-1] = rs.getString(col);
					col++;
				}
				rs.next();
				row++;
			}

			for(int i=0; i<res[0].length;i++)
			{
				data[i]=res[0][i];
			}

			rs = stmt.executeQuery("SELECT ecnumber,inModel FROM enzyme WHERE protein_idprotein ="+ids.get(selectedRow));
			String temp = "", tempBoolean="";
			
			while(rs.next()) {
				
				temp+=(rs.getString(1)+";");
				if(rs.getString(2).equals("1"))
				{tempBoolean+=("true;");}
				else{tempBoolean+=("false;");}
			}
			data[9]= temp;
			data[10]= tempBoolean;

			rs.close();            
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		return data;
	}

	/**
	 * @param selectedRow
	 * @return
	 */
	public String[] getSynonyms(int selectedRow) {

		Statement stmt;
		String[][] res = null; 
		try 
		{
			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT alias FROM aliases where class = 'p' AND entity ="+ids.get(selectedRow));
			ResultSetMetaData rsmd = rs.getMetaData();
			rs.last();
			res = new String[rs.getRow()][rsmd.getColumnCount()];
			rs.first();

			int row=0;
			while(row<res.length)
			{
				int col=1;
				while(col<rsmd.getColumnCount()+1)
				{

					res[row][col-1] = rs.getString(col);
					col++;
				}
				rs.next();
				row++;
			}

			rs.close();            
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		String[] data = new String[res.length];

		int i=0;
		while(i<res.length)
		{
			data[i]=res[i][0];
			i++;
		}

		return data;
	}

	/**
	 * @param name
	 * @param clas
	 * @param inchi
	 * @param molecular_weight
	 * @param molecular_weight_kd
	 * @param molecular_weight_exp
	 * @param molecular_weight_seq
	 * @param pi
	 * @param selectedRow
	 * @param synonyms
	 * @param oldSynonyms
	 * @param enzymes
	 * @param oldEnzymes
	 * @param inModel
	 * @param oldInModel
	 */
	public void updateProtein(String name, String clas, String inchi, String molecular_weight, String molecular_weight_kd,
			String molecular_weight_exp, String molecular_weight_seq, String pi, int selectedRow, String[] synonyms,
			String[] oldSynonyms, String[] enzymes, String[] oldEnzymes, Boolean[] inModel, Boolean[] oldInModel) {

		Statement stmt;

		try {
			String protein_id = ids.get(selectedRow);
			stmt = this.connection.createStatement();
			stmt.execute("UPDATE protein SET name = '" + Connection.mysqlStrConverter(name)+ "'" +
					//", inchi = '" + inchi + "', " +
					//					"molecular_weight ='" + molecular_weight +  "', " +
					//					"molecular_weight_exp='" + molecular_weight_exp + "', "+ 
					//					"molecular_weight_kd = '" + molecular_weight_kd + "', " +
					//					"molecular_weight_seq = '" + molecular_weight_seq + "', " +
					//					"pi = '" + pi + "' " +
					"WHERE  idprotein = "+protein_id);
			
			if(!clas.equals("")){stmt.execute("UPDATE protein SET class = '" + clas + "' WHERE idprotein = "+protein_id);}
			if(!inchi.equals("")){stmt.execute("UPDATE protein SET inchi = '" + inchi + "' WHERE idprotein = "+protein_id);}
			if(!molecular_weight.equals("")){stmt.execute("UPDATE protein SET molecular_weight ='" + molecular_weight +  "' WHERE idprotein = "+protein_id);}
			if(!molecular_weight_exp.equals("")){stmt.execute("UPDATE protein SET molecular_weight_exp='" + molecular_weight_exp + "' WHERE idprotein = "+protein_id);}
			if(!molecular_weight_kd.equals("")){stmt.execute("UPDATE protein SET molecular_weight_kd='" + molecular_weight_kd + "' WHERE idprotein = "+protein_id);}
			if(!molecular_weight_seq.equals("")){stmt.execute("UPDATE protein SET molecular_weight_seq='" + molecular_weight_seq + "' WHERE idprotein = "+protein_id);}
			if(!molecular_weight_seq.equals("")){stmt.execute("UPDATE protein SET pi = '" + pi + "' " + "' WHERE  idprotein = "+protein_id);}

			for(int s=0; s<synonyms.length; s++) {

				if(oldSynonyms.length>s) {

					if(!synonyms[s].equals("")) {

						if(!synonyms[s].equals(oldSynonyms[s])) {

							stmt.execute("UPDATE aliases SET alias ='" + Connection.mysqlStrConverter(synonyms[s]) +"' " +
									"WHERE class='p' entity = "+protein_id+" AND alias ='" + Connection.mysqlStrConverter(oldSynonyms[s]) + "'");
						}
					}
					else {

						stmt.execute("Delete from aliases WHERE entity = "+protein_id+" AND alias ='" + Connection.mysqlStrConverter(oldSynonyms[s]) + "'");
					}

				}			
				else {

					if(!synonyms[s].equals("")) {

						stmt.execute("INSERT INTO aliases (class, alias, entity) VALUES('p','" + Connection.mysqlStrConverter(synonyms[s]) +"', "+protein_id+")");
					}
				}
			}

			List<String> old_enzymes_ids = new ArrayList<String>();
			List<String> enzymes_ids = new ArrayList<String>();

			int i = 0;
			for(String id : oldEnzymes) {

				old_enzymes_ids.add(i,id);
				i++;
			}

			i = 0;
			for(String id : enzymes) {

				enzymes_ids.add(i,id);
				i++;
			}

			List<String> enzymes_ids_add = new ArrayList<String>();

			for(String id : enzymes_ids) {

				if(!id.equals("dummy") && !id.isEmpty()) {

					if(old_enzymes_ids.contains(id)) {

						old_enzymes_ids.remove(id);
					}
					else {

						enzymes_ids_add.add(id);
					}
				}
			}

			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			for(String id : old_enzymes_ids) {

				Proteins.removeEnzymesAssignmensts(id, enzymes_ids_add, inModel, stmt, protein_id, false);
			}

			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			for(String id : enzymes_ids) {

				if(enzymes_ids_add.contains(id)) {

					Proteins.insertEnzymes(ids.get(selectedRow), id, stmt, false);
				}
				else {

					if(inModel[enzymes_ids.indexOf(id)]) {
						
						Proteins.insertEnzymes(ids.get(selectedRow), id, stmt, true);
					}
					else {
						
						Proteins.removeEnzymesAssignmensts(id, enzymes_ids_add, inModel, stmt, protein_id, false);
					}
				}
			}

			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			//			for(int s=0; s<enzymes.length; s++) {
			//
			//				if(oldEnzymes.length>s) {
			//
			//					if(!enzymes[s].equals("")) {
			//
			//						if(!enzymes[s].equals(oldEnzymes[s])) {
			//
			//							stmt.execute("UPDATE enzyme SET ecnumber ='" +Connection.mysqlStrConverter(enzymes[s]) +"' " +
			//									"WHERE protein_idprotein='"+ids.get(selectedRow)+"' AND ecnumber ='" + Connection.mysqlStrConverter(oldEnzymes[s]) + "'");
			//						}
			//					}
			//					else {
			//
			//						stmt.execute("DELETE FROM enzyme WHERE protein_idprotein='"+ids.get(selectedRow)+"' AND ecnumber ='" + Connection.mysqlStrConverter(oldEnzymes[s]) + "'");
			//					}
			//
			//				}
			//				else {
			//
			//					if(!enzymes[s].equals("")) {
			//
			//						stmt.execute("INSERT INTO enzyme (protein_idprotein, ecnumber, inModel) VALUES("+ids.get(selectedRow)+",'"+Connection.mysqlStrConverter(enzymes[s])+"',"+inModel[s]+")");
			//					}
			//				}
			//			}
			//
			//			for(int s=0; s<inModel.length; s++) {
			//
			//				stmt.execute("UPDATE enzyme SET inModel = " + inModel[s]+" WHERE protein_idprotein= "+ids.get(selectedRow)+" AND ecnumber ='" + enzymes[s] + "',");
			//
			//				ResultSet rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reaction " +
			//						"LEFT JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = idreaction " +
			//						"INNER JOIN pathway_has_enzyme ON pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein  " +
			//						"INNER JOIN pathway_has_reaction ON pathway_has_enzyme.pathway_idpathway = pathway_has_reaction.pathway_idpathway  " +
			//						"WHERE pathway_has_reaction.reaction_idreaction = idreaction " +
			//						"AND reaction_has_enzyme.enzyme_protein_idprotein = '"+ids.get(selectedRow)+"' " +
			//						"AND reaction_has_enzyme.enzyme_ecnumber = '"+enzymes[s]+"'");
			//
			//				List<String> reactions_ids = new ArrayList<String>();
			//
			//				while(rs.next()) {
			//
			//					reactions_ids.add(rs.getString(1));
			//				}
			//
			//				rs= stmt.executeQuery("SELECT idreaction FROM reactions_view_noPath_or_noEC " +
			//						"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction=idreaction " +
			//						"WHERE enzyme_protein_idprotein = '"+ids.get(selectedRow)+"' AND enzyme_ecnumber = '"+enzymes[s]+"'");
			//
			//				while(rs.next()) {
			//
			//					reactions_ids.add(rs.getString(1));
			//				}
			//
			//				for(String idreaction: reactions_ids) {
			//
			//					stmt.execute("UPDATE reaction SET inModel = true, source = 'MANUAL' WHERE idreaction = '"+idreaction+"'");
			//				}
			//			}

			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
	}

	/**
	 * @param name
	 * @param class_string
	 * @param inchi
	 * @param molecular_weight
	 * @param molecular_weight_kd
	 * @param molecular_weight_exp
	 * @param molecular_weight_seq
	 * @param pi
	 * @param synonyms
	 * @param enzymes
	 * @param inModel
	 */
	public void insertProtein(String name, String class_string, String inchi, String molecular_weight,
			String molecular_weight_kd, String molecular_weight_exp, String molecular_weight_seq, String pi,
			String[] synonyms, String[] enzymes, Boolean[] inModel) {

		Statement stmt;

		try {

			stmt = this.connection.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM protein WHERE name='" + Connection.mysqlStrConverter(name) + "' AND class = '"+class_string+"'");
			String idNewProtein = "";
			if(rs.next()) {
				
				idNewProtein = rs.getString(1);
			}
			else {
				
				stmt.execute("INSERT INTO protein (name,class) VALUES('" + Connection.mysqlStrConverter(name) + "','" + class_string + "')");
				idNewProtein = (this.select("SELECT LAST_INSERT_ID()"))[0][0];
			}

			//if(!class_string.equals("")){stmt.execute("UPDATE protein SET class = '" + class_string + "' WHERE  idprotein ='"+idNewprotein+"'");}
			if(!inchi.equals("")){stmt.execute("UPDATE protein SET inchi = '" + inchi + "' WHERE  idprotein ='"+idNewProtein+"'");}
			if(!molecular_weight.equals("")){stmt.execute("UPDATE protein SET molecular_weight ='" + molecular_weight +  "' WHERE  idprotein ='"+idNewProtein+"'");}
			if(!molecular_weight_exp.equals("")){stmt.execute("UPDATE protein SET molecular_weight_exp='" + molecular_weight_exp + "' WHERE  idprotein ='"+idNewProtein+"'");}
			if(!molecular_weight_kd.equals("")){stmt.execute("UPDATE protein SET molecular_weight_kd='" + molecular_weight_kd + "' WHERE  idprotein ='"+idNewProtein+"'");}
			if(!molecular_weight_seq.equals("")){stmt.execute("UPDATE protein SET molecular_weight_seq='" + molecular_weight_seq + "' WHERE  idprotein ='"+idNewProtein+"'");}
			if(!molecular_weight_seq.equals("")){stmt.execute("UPDATE protein SET pi = '" + pi + "' " + "' WHERE  idprotein ='"+idNewProtein+"'");}

			for(int s=0; s<synonyms.length; s++) {

				if(!synonyms[s].equals("")) {

					rs = stmt.executeQuery("SELECT * FROM aliases WHERE class='p' AND  alias='" + Connection.mysqlStrConverter(synonyms[s]) +"' AND  entity=" + idNewProtein);
					if(!rs.next()) {
						
						stmt.execute("INSERT INTO aliases (class, alias, entity) VALUES('p','" + Connection.mysqlStrConverter(synonyms[s]) +"', "+idNewProtein+")");
					}
				}
			}

			List<String> enzymes_ids = new ArrayList<String>();
			int i = 0;
			for(String id : enzymes) {

				enzymes_ids.add(i,id);
				i++;
			}

			for(String id : enzymes_ids) {

				rs = stmt.executeQuery("SELECT * FROM enzyme " +
						"WHERE inModel=true AND  source='MANUAL' " +
						"AND  protein_idprotein=" + idNewProtein+" AND ecnumber ='"+id+"'");
				if(!rs.next()) {
				
					stmt.execute("INSERT INTO enzyme (inModel, source, ecnumber, protein_idprotein) " +
							"VALUES (true, 'MANUAL', '"+id+"', '"+idNewProtein+"') ");
				}
				
				Proteins.insertEnzymes(idNewProtein, id, stmt, false);
			}

			//			for(int s=0; s<enzymes.length; s++) {
			//
			//				if(!enzymes[s].equals("")) {
			//					
			//					stmt.execute("INSERT INTO enzyme (protein_idprotein, ecnumber, inModel, source) VALUES('"+idNewProtein+"','" + enzymes[s] +"', "+inModel[s]+",'MANUAL')");
			//					
			//					ResultSet rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reaction " +
			//							"LEFT JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = idreaction " +
			//							"INNER JOIN pathway_has_enzyme ON pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein  " +
			//							"INNER JOIN pathway_has_reaction ON pathway_has_enzyme.pathway_idpathway = pathway_has_reaction.pathway_idpathway  " +
			//							"WHERE pathway_has_reaction.reaction_idreaction = idreaction " +
			//							"AND reaction_has_enzyme.enzyme_protein_idprotein = "+idNewProtein+ " " +
			//							"AND reaction_has_enzyme.enzyme_ecnumber = '"+enzymes[s]+"'");
			//
			//					Set<String> reactions_ids = new HashSet<String>();
			//
			//					while(rs.next()) {
			//
			//						reactions_ids.add(rs.getString(1));
			//					}
			//
			//					rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reactions_view_noPath_or_noEC " +
			//							"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction=idreaction " +
			//							"WHERE enzyme_protein_idprotein = "+idNewProtein+" AND enzyme_ecnumber = '"+enzymes[s]+"'");
			//
			//					while(rs.next()) {
			//
			//						reactions_ids.add(rs.getString(1));
			//					}
			//
			//					for(String idreaction: reactions_ids) {
			//
			//						stmt.execute("UPDATE reaction SET inModel = true, source = 'MANUAL' WHERE idreaction = "+idreaction);
			//					}
			//					
			//				}
			//			}
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
	}

	/**
	 * @param selectedRow
	 * @param ecnumber
	 * @param enzymes_ids
	 * @param inModel
	 * @param stmt
	 * @throws SQLException 
	 */
	public static void removeEnzymesAssignmensts(String ecnumber, List<String> enzymes_ids, Boolean[] inModel, Statement stmt, String protein_id, boolean removeReaction) throws SQLException {

		if(removeReaction) {
			
			stmt.execute("DELETE FROM enzyme WHERE protein_idprotein = "+protein_id+" AND ecnumber ='" + Connection.mysqlStrConverter(ecnumber) + "'");
		}
		else {
			
			stmt.execute("UPDATE enzyme SET inModel=false WHERE protein_idprotein = "+protein_id+" AND ecnumber ='" + Connection.mysqlStrConverter(ecnumber) + "'");
		}

		ResultSet rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reaction " +
				"LEFT JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = idreaction " +
				"INNER JOIN pathway_has_reaction ON reaction.idreaction = pathway_has_reaction.reaction_idreaction  " +
				"WHERE pathway_has_reaction.reaction_idreaction = idreaction " +
				"AND reaction_has_enzyme.enzyme_protein_idprotein = "+protein_id +" " +
				"AND reaction_has_enzyme.enzyme_ecnumber = '"+Connection.mysqlStrConverter(ecnumber)+"'");
		
		Set<String> reactions_ids = new HashSet<String>();

		while(rs.next()) {

			reactions_ids.add(rs.getString(1));
		}

		rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reactions_view_noPath_or_noEC " +
				"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction=idreaction " +
				"WHERE enzyme_protein_idprotein = "+protein_id+" AND enzyme_ecnumber = '"+Connection.mysqlStrConverter(ecnumber)+"'");
		
		while(rs.next()) {

			reactions_ids.add(rs.getString(1));
		}
		
		for(String idreaction: reactions_ids) {

			List<String[]> proteins_array = new ArrayList<String[]>();

			rs= stmt.executeQuery("SELECT enzyme_protein_idprotein, enzyme_ecnumber FROM reaction_has_enzyme " +
					"LEFT JOIN enzyme ON (enzyme_protein_idprotein = enzyme.protein_idprotein AND enzyme_ecnumber = enzyme.ecnumber)"+
					"WHERE inModel AND reaction_idreaction = "+idreaction);

			while(rs.next()) {
				
				if(rs.getString(1).equalsIgnoreCase(protein_id) && ecnumber.equalsIgnoreCase(rs.getString(2))) {}
				else {

					if(rs.getString(1).equalsIgnoreCase(protein_id) && enzymes_ids.contains(rs.getString(2))) {

						if(inModel[enzymes_ids.indexOf(rs.getString(2))]) {

							proteins_array.add(new String[] {rs.getString(1),rs.getString(2)});
						}
					}
					else {

						proteins_array.add(new String[] {rs.getString(1),rs.getString(2)});
					}
				}
			}

			if(proteins_array.isEmpty()) {

				stmt.execute("UPDATE reaction SET inModel = false, source = 'MANUAL' WHERE idreaction = "+idreaction);
			}
		}
	}
	
	/**
	 * @param idProtein
	 * @param ecnumber
	 * @param stmt
	 * @throws SQLException
	 */
	public static void insertEnzymes(String idProtein, String ecnumber, Statement stmt, boolean editedReaction) throws SQLException {

		String aux = "";
		
		
		if(editedReaction) {
			
			aux =", source = MANUAL " ;
		}
		
		stmt.execute("UPDATE enzyme SET inModel = true, source = 'MANUAL' WHERE ecnumber = '"+ecnumber+"' AND protein_idprotein = " + idProtein);

		
		if(editedReaction) {
			
			aux ="AND reaction_has_enzyme.enzyme_protein_idprotein = "+idProtein + " " ;
		}
		
		ResultSet rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reaction " +
				"LEFT JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = idreaction " +
				//"INNER JOIN pathway_has_enzyme ON pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein  " +
				"INNER JOIN pathway_has_reaction ON idreaction = pathway_has_reaction.reaction_idreaction  " +
				"WHERE pathway_has_reaction.reaction_idreaction = idreaction " +
				aux+
				"AND reaction_has_enzyme.enzyme_ecnumber = '"+ecnumber+"'");

		Set<String> reactions_ids = new HashSet<String>();

		while(rs.next()) {

			reactions_ids.add(rs.getString(1));
		}
		
		if(editedReaction) {
			
			aux =" AND enzyme_protein_idprotein = "+idProtein ;
		}
		rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reactions_view_noPath_or_noEC " +
				"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction=idreaction " +
				"WHERE enzyme_ecnumber = '"+ecnumber+"'"+aux);

		while(rs.next()) {

			reactions_ids.add(rs.getString(1));
		}
		
		for(String idreaction: reactions_ids) {
			
			if(!editedReaction) {
				
				rs = stmt.executeQuery("SELECT * FROM enzyme WHERE protein_idprotein = "+idProtein+" AND ecnumber = '"+ecnumber+"';");
				
				if(!rs.next()) {

					stmt.execute("INSERT INTO enzyme (inModel, source, protein_idprotein, ecnumber) VALUES (true,'MANUAL',"+idProtein+",'"+ecnumber+"') ");
				}
				
				stmt.execute("INSERT INTO reaction_has_enzyme (reaction_idreaction, enzyme_protein_idprotein, enzyme_ecnumber) VALUES ("+idreaction+","+idProtein+",'"+ecnumber+"') ");
			}
			stmt.execute("UPDATE reaction SET inModel = true, source = 'MANUAL' WHERE idreaction = "+idreaction);
		}
	}

	/**
	 * @return
	 */
	public boolean existGenes() {

		Statement stmt;
		try {

			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM gene");
			boolean result = rs.next();


			return result;
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return false;
	}
}
