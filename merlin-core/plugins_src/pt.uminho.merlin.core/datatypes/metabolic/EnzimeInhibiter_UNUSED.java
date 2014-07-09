package datatypes.metabolic;

import java.io.Serializable;

import datatypes.Table;
import datatypes.metabolic_regulatory.Entity;

public class EnzimeInhibiter_UNUSED extends Entity implements Serializable {

	private static final long serialVersionUID = 7041578259945388261L;

	public EnzimeInhibiter_UNUSED(Table dbt, String name)
	{
		super(dbt, name);
	}
	
	public String[][] getStats()
	{
		String[][] res = new String[0][];
		
		return res;
	}
}
