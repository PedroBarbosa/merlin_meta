/**
 * 
 */
package utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ODias
 *
 */
public class ImportHomologyData {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws IOException 
	 */
	//	public static void main(String[] args) throws SQLException, IOException {
	//
	//		Connection connection = new Connection("127.0.0.1", "3306", "kla_model", "root", "password");
	//		//new Connection("127.0.0.1", "3306", "transporters", "root", "password");
	//
	//		Statement stmt = connection.createStatement();
	//
	//		File file = //new File("C:/Users/ODias/Desktop/locus_names.txt");
	//				//new File("C:/Users/ODias/Desktop/tc_numbers.txt");
	//				new File("D:/My Dropbox/WORK/database_reactions/irr_reactions.txt");
	//
	//		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
	//
	//		String text;
	//		while ((text = bufferedReader.readLine()) != null) {
	//
	//			String[] data = text.split("\t");
	//			//			stmt.execute("UPDATE geneblast SET gene = '"+MySQL_Utilities.mysqlStrConverter(data[1])+"' " +
	//			//					"WHERE locusTag ='"+MySQL_Utilities.mysqlStrConverter(genes[0])+"'");
	//
	//			//			stmt.execute("UPDATE tcnumber SET tc_id = '"+MySQL_Utilities.mysqlStrConverter(data[1])+"' " +
	//			//					", tc_family = '"+MySQL_Utilities.mysqlStrConverter(data[2])+"' " +
	//			//					"WHERE uniprot_id ='"+MySQL_Utilities.mysqlStrConverter(data[0])+"'");
	//			//			ResultSet rs = stmt.executeQuery("SELECT * FROM tcnumber WHERE uniprot_id ='"+MySQL_Utilities.mysqlStrConverter(gedatanes[0])+"'");
	//			//			rs.next();
	//
	//			ResultSet rs = stmt.executeQuery("SELECT * FROM reaction WHERE name ='"+MySQL_Utilities.mysqlStrConverter(data[0])+"'");
	//			if(rs.next()) {
	//
	//				String equation = rs.getString(3).replace("<=>", "=>");
	//				stmt.execute("UPDATE reaction SET reversible = false, equation= '"+MySQL_Utilities.mysqlStrConverter(equation)+"'  WHERE name ='"+MySQL_Utilities.mysqlStrConverter(data[0])+"'");
	//			}
	//		}
	//
	//		bufferedReader.close();
	//	}

	//	/**
	//	 * @param args
	//	 * @throws SQLException 
	//	 * @throws IOException 
	//	 */
	//	public static void main(String[] args) throws SQLException, IOException {
	//
	//		Connection connection = new Connection("127.0.0.1", "3306", "kla_model", "root", "password");
	//
	//		Statement stmt = connection.createStatement();
	//
	//		String aux = "";
	//		//		if(this.getProject().isCompartmentalisedModel()) {
	//
	//		aux = aux.concat(" WHERE NOT originalReaction AND inModel ");
	//		//		}
	//		//		else {
	//		//
	//		//			aux = aux.concat(" WHERE originalReaction AND inModel ");
	//		//		}
	//
	//		List<String> removed_reactions = new ArrayList<String> ();
	//
	//		int counter = -1;
	//		int transportCounter = -1;
	//		int reactionsCounter = -1;
	//		int globalTransportCounter = -1;
	//		int globalReactionsCounter = -1;
	//		int globalcounter = 0;
	//		Set<String> removedReactions = new HashSet<String>();
	//		boolean oneIteration = false;
	//		boolean go=true;
	//
	//		while (go) {
	//
	//			counter = 0;
	//			transportCounter = 0;
	//			reactionsCounter = 0;
	//
	//			ResultSet rs = stmt.executeQuery("SELECT compound.idcompound, stoichiometry.compartment_idcompartment, " +
	//					" COUNT(DISTINCT(idreaction)), " +
	//					" COUNT(DISTINCT(IF(reaction.name NOT LIKE 'T%', idreaction, NULL))) AS sum_not_transport, "+
	//					" COUNT(DISTINCT(IF(reaction.name LIKE 'T%', idreaction, NULL))) AS sum_transport " +
	//					", compartment.name" +
	//					" FROM stoichiometry " +
	//					" JOIN compound ON compound_idcompound=idcompound " +
	//					" JOIN compartment ON stoichiometry.compartment_idcompartment=idcompartment " +
	//					" LEFT JOIN reaction ON (reaction.idreaction=reaction_idreaction) "+
	//					aux+
	//					" GROUP BY kegg_id, stoichiometry.compartment_idcompartment " +
	//					" ORDER BY compound.name AND kegg_id ;");
	//
	//			List<String[]> compoundList = new ArrayList<String[]>();
	//
	//			while(rs.next()) {
	//
	//				if(rs.getInt("sum_not_transport")==1 && rs.getInt("sum_transport")==0) {
	//
	//					String[] data = new String[2];
	//
	//					data[0] = rs.getString(1);
	//					data[1] = rs.getString(2);
	//					compoundList.add(data);
	//				}
	//
	//				if(rs.getInt("sum_not_transport")==0 && !rs.getString(6).equalsIgnoreCase("extracellular")) {
	//
	//					String[] data = new String[2];
	//					data[0] = rs.getString(1);
	//					data[1] = rs.getString(2);
	//					compoundList.add(data);
	//				}
	//			}
	//
	//			Map<String, List<String[]>> data = new HashMap<String, List<String[]>>();
	//			rs = stmt.executeQuery("SELECT compound_idcompound, stoichiometry.compartment_idcompartment, idreaction," +
	//					" reaction.name, compound.kegg_id FROM reaction " +
	//					" JOIN stoichiometry ON (reaction.idreaction = reaction_idreaction) "+ 
	//					" LEFT JOIN compound ON (compound.idcompound = stoichiometry.compound_idcompound) "+
	//					aux);
	//
	//			while(rs.next()) {
	//
	//				List<String[]> dataList;
	//
	//				if(!removedReactions.contains(rs.getString(3))) {
	//
	//					if(data.containsKey(rs.getString(1)+"_"+rs.getString(2))) {
	//
	//						dataList = data.get(rs.getString(1)+"_"+rs.getString(2));
	//					}
	//					else {
	//
	//						dataList = new ArrayList<String[]>();
	//					}
	//
	//					String[] datum = new String[3];
	//					datum[0] = rs.getString(3);
	//					datum[1] = rs.getString(4);
	//					datum[2] = rs.getString(5);
	//					dataList.add(datum);
	//					data.put(rs.getString(1)+"_"+rs.getString(2), dataList);
	//				}
	//			}
	//
	//			for (String [] cList : compoundList ) {
	//
	//				if(data.containsKey(cList[0]+"_"+cList[1])) {
	//
	//					for(String[] dataArray : data.get(cList[0]+"_"+cList[1])) {
	//
	//						if(dataArray[1].toLowerCase().contains("biomass")) {
	//
	//							System.out.println("compound\t"+dataArray[2]+"_"+cList[1]);
	//						}
	//						else {
	//
	//							stmt.execute("UPDATE reaction SET inModel = false WHERE idreaction ="+dataArray[0]);
	//							counter++;
	//							globalcounter++;
	//							if(dataArray[1].startsWith("T")) {
	//
	//								transportCounter++;
	//								globalTransportCounter++;
	//							}
	//							else {
	//
	//								reactionsCounter++;
	//								globalReactionsCounter++;
	//							}
	//
	//							removed_reactions.add(dataArray[0]+"_"+dataArray[1]);
	//						}
	//					}
	//				}
	//			}
	//
	//			System.out.println("\t"+counter+" reactions removed!\t\t"+reactionsCounter+" other reactions\t"+transportCounter+" transport reactions");
	//
	//			if(oneIteration || counter==0) {
	//
	//				go=false;
	//			}
	//		}
	//
	//		System.out.println(globalReactionsCounter+" other reactions removed!");
	//		System.out.println(globalTransportCounter+" transport reactions removed!");
	//		System.out.println(globalcounter+" reactions removed!");
	//		
	//		System.out.println(removed_reactions);
	//	}

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws SQLException, IOException {

		File file = new File("D:/My Dropbox/WORK/KLA/good_bad.txt");

		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		Set<String> reactionsOut = new HashSet<String>();

		String text;

		while ((text = bufferedReader.readLine()) != null) {

			boolean addDrain=false;

			Set<String> reactions1 = new HashSet<String>();
			Set<String> reactions2 = new HashSet<String>();

			text=text.replace(" = 0", "");

			String[] data = text.split("\t");

			String id = data[0];

			String[] badreactionsString1 = data[1].split(" - ");

			boolean addMinus=false;
			if(data[1].trim().startsWith("-")) {

				addMinus=true;
			}

			for(String react: badreactionsString1) {

				if(addMinus) {

					react = "-"+react;
				}
				String[]  badreactionsString2 = react.split(" \\+ ");

				for(String r: badreactionsString2) {

					r=r.replace("--", "-");
					reactions1.add(r.trim());
					if(r.contains("EX")) {

						addDrain = true;
					}
				}
				addMinus=true;
			}

			if(addDrain) {

				reactions1.add(data[4]);
			}

			addDrain=false;
			addMinus=false;
			String[]  goodreactionsString1 = data[3].split(" - ");

			if(data[3].trim().startsWith("-")) {

				addMinus=true;
			}
			for(String react:goodreactionsString1) {

				if(addMinus) {

					react = "-"+react;
				}
				String[] goodreactionsString2 = react.split(" \\+ ");

				for(String r:goodreactionsString2) {

					r=r.replace("--", "-");
					reactions2.add(r.trim());
					if(r.contains("EX")) {

						addDrain = true;
					}
				}
				addMinus=true;
			}

			if(addDrain) {

				reactions2.add(data[2]);
			}

			if(reactions1.equals(reactions2)) {

				//System.out.println(id+"\tOK!");
			}
			else {

				System.out.println(id);

				Set<String> reactionsClone1 = new HashSet<String>();
				Set<String> reactionsClone2 = new HashSet<String>();
				
				
				reactionsClone1.removeAll(reactions2);
				reactionsClone2.removeAll(reactions1);

				for(String r:reactions1) {

					if(!reactions2.contains(r)) {

						reactionsClone1.add(r);
						reactionsOut.add(r.replace("-",""));
					}

				}

				for(String r:reactions2) {

					if(!reactions1.contains(r)) {

						reactionsClone2.add(r);
						reactionsOut.add(r.replace("-",""));
					}

				}
				System.out.println(reactionsClone1);
				System.out.println(reactionsClone2);
				System.out.println();
				System.out.println();
			}
		}
		System.out.println(reactionsOut);
		bufferedReader.close();
	}
}
