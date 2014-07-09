package datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Observable;

import datatypes.metabolic_regulatory.Entity;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.ListElements;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

@Datatype(structure = Structure.LIST,namingMethod="getName")
public class Entities extends Observable implements Serializable {

	private static final long serialVersionUID = -7994284815651741979L;

	private ArrayList<Entity> entities = null;
	
	public ArrayList<Entity> getEntities() {
		return entities;
	}

	public Entities()
	{
		this.entities = new ArrayList<Entity>();
	}
	
	@ListElements
	public ArrayList<Entity> getEntitiesList() {
		return entities;
	}

	public void setEnties(ArrayList<Entity> enties) {
		this.entities = enties;
		setChanged();
		notifyObservers();
	}
	
	public String getName() {
		return "Entities";
	}
}