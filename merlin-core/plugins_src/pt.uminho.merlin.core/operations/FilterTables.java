package operations;

import es.uvigo.ei.aibench.core.operation.annotation.Operation;


@Operation
public class FilterTables {
	
//	TableTypes type;
//	
//	@Port(direction=Direction.INPUT, name="Type", order=1)
//	public void setType(TableTypes type){
//		this.type = type;
//	}
//	
//	@Port(direction=Direction.INPUT, name="Connection", order=1)
//	public void setDbc(DataBaseConnector dbc) {
//		MySqlLink dsa = dbc.getDsa();
//		
//		DataBaseTables new_dbts = new DataBaseTables();
//		
//		DataBaseTables dbts = dbc.getDbt();
//		
//		ArrayList<DataBaseTable> dbt_list = dbts.getList();
//		
//		Regex r;
//		
//		switch(type) {
//		case NONE:
//			r = new Regex(" \\(.{0,}\\)$");
//		break;
//		case BRENDA:
//			r = new Regex(" \\(.{0,}\\)$");
//		break;
//		case KEGG:
//			r = new Regex(" \\(.{0,}\\)$");
//		break;
//		case BIOCYC:
//			r = new Regex(" \\(.{0,}\\)$");
//		break;
//		case CHEBI:
//			r = new Regex(" \\(.{0,}\\)$");
//		break;
//			
//		}
//		
//		
//		
//		for(int i=0;i<dbt_list.size();i++)
//		{
//			DataBaseTable dbt = dbt_list.get(i);
//			
//			if(new Integer(dat[0][0]).intValue()>0) new_dbts.addToList(dbt);
//		}
//		
//		dbc.setDbt(new_dbts);
//	}
}
