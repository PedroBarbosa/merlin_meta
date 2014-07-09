package datatypes.metabolic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import datatypes.DataTable;
import datatypes.GenericDataTable;

/**
 * @author ODias
 *
 */
public class PathwayReaction extends DataTable implements Serializable{

	private static final long serialVersionUID = 2890353542858037836L;
	protected ArrayList<String> ids;
	protected ArrayList<String> pathway;
	protected Map <String, String> pathways;
	protected boolean encodedOnly;

	/**
	 * @param columnsNames
	 * @param name
	 * @param pathways
	 * @param encodedOnly
	 */
	public PathwayReaction(ArrayList<String> columnsNames, String name, Map <String, String> pathways, boolean encodedOnly) {
		
		super(columnsNames, name);
		this.ids = new ArrayList<String>();
		this.pathway = new ArrayList<String>();
		this.pathways = pathways;
		this.encodedOnly=encodedOnly;
	}

//	/**
//	 * @param line
//	 * @param id
//	 * @param pathway
//	 */
//	public void addLine(ArrayList<String> line, String id, String pathway){
//		super.addLine(line);
//		this.ids.add(id);
//		this.pathway.add(pathway);
//	}
	
	/**
	 * @param line
	 * @param id
	 * @param pathway
	 */
	public void addLine(ArrayList<Object> line, String id, String pathway){
		super.addLine(line);
		this.ids.add(id);
		this.pathway.add(pathway);
	}

	/**
	 * @param row
	 * @return
	 */
	public String getRowPathway(int row){
		return pathway.get(row);
	}

	/**
	 * @param row
	 * @return
	 */
	public String getRowId(int row){
		return ids.get(row);
	}

	/**
	 * @param path
	 * @return
	 */
	public GenericDataTable getReactionsData(int path) {
		
		try {
			
			ArrayList<String> columnsNames = new ArrayList<String>();
			columnsNames.add("Info");
			columnsNames.add("Pathway Name");
			columnsNames.add("Reaction Name");
			columnsNames.add("Equation");
			columnsNames.add("Source");
			columnsNames.add("notes");
			columnsNames.add("Reversible");
			columnsNames.add("Generic");
			//if(!encodedOnly){
				columnsNames.add("In model");
				//}

			GenericDataTable qrt = new GenericDataTable(columnsNames, "Reactions", "") {
				
				private static final long serialVersionUID = 6629060675011336218L;
				@Override
				public boolean isCellEditable(int row, int col) {
					
					if (col==0 || col>4) {
						
						return true;
					}
					else return false;
				}
			};

			for(int i=0;i<pathway.size();i++) {
				
				if(pathway.get(i).equals(path+"")) {
					
					qrt.addLine((Object[])super.getRow(i), this.ids.get(i));
				}
			}
			return qrt;
		}
		catch(Exception e) {
		
			e.printStackTrace();
		}

		return null;
	}
}
