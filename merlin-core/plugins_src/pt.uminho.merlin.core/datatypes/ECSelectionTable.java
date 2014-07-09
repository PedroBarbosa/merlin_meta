package datatypes;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class ECSelectionTable extends AbstractTableModel {

	private static final long serialVersionUID = 1668479692937438812L;
	protected String[] columnsNames = null;
	protected ArrayList <Object[]> table = null;
	protected String name;
	
	
	public ECSelectionTable(ArrayList<String> columnsNames, String name){
		this.table = new ArrayList <Object[]>();

		this.columnsNames = new String[columnsNames.size()];

		for(int i=0;i<columnsNames.size();i++) this.columnsNames[i] = columnsNames.get(i); 
		this.name = name;
	}
	
	@Override
	public int getColumnCount() {
		return this.columnsNames.length;
	}

	@Override
	public int getRowCount() {
		return table.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		return this.table.get(row)[column];
	}

}
