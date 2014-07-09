package datatypes;

import java.util.ArrayList;
import java.util.Observable;

import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.ListElements;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

@Datatype(structure = Structure.LIST)
public class QueryLine extends Observable{

	private ArrayList<String> line = null;

	public QueryLine() {
		line = new ArrayList<String>();
	}
	
	@ListElements
	public ArrayList<String> getIntList() {
		return line;
	}
	
	public void addEl(String b){
		line.add(b);
		setChanged();
		notifyObservers();
	}
}
