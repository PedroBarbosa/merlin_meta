package datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Observable;

import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.ListElements;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

@Datatype(structure = Structure.LIST, namingMethod="getName")
public class Tables extends Observable implements Serializable {

	private static final long serialVersionUID = -3494349774553720072L;
	private ArrayList<Table> list = null;

	public Tables() {
		list = new ArrayList<Table>();
	}

	@ListElements
	public ArrayList<Table> getList() {
		return list;
	}

	public void addToList(Table b){
		list.add(b);
		setChanged();
		notifyObservers();
	}
	
	public void removeAllTables() {
		
		list.clear();
	}
	
	public String[][] getTableNumbers() throws Exception {
		
		String[][] res = new String[list.size()][];
		
		for(int i=0;i<res.length;i++) {
			Table dbt = list.get(i);
			
			res[i] = new String[2];
			res[i][0] = dbt.getName();
			res[i][1] = dbt.getSize();
		}
		
		return res;
	}
	
	public String getName() {
		return "Tables";
	}
}
