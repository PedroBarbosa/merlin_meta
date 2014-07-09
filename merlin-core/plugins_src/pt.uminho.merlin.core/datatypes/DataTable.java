package datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

@Datatype(structure = Structure.SIMPLE, namingMethod="getName")
public class DataTable extends AbstractTableModel implements Serializable {

	private static final long serialVersionUID = -6331330169542965847L;

	protected String[] columnsNames = null;


	protected ArrayList <Object[]> table = null;
	protected String name;

	/**
	 * @param columnsNames
	 * @param name
	 */
	public DataTable(List<String> columnsNames, String name) {
		
		this.table = new ArrayList <Object[]>();

		this.columnsNames = new String[columnsNames.size()];

		for(int i=0;i<columnsNames.size();i++) this.columnsNames[i] = columnsNames.get(i); 
		this.name = name;
	}

	/**
	 * @param columnsNames
	 * @param name
	 */
	public DataTable(String[] columnsNames, String name) {
		
		this.table = new ArrayList <Object[]>();

		this.columnsNames = columnsNames;
		this.name = name;
	}

	/**
	 * @param line
	 */
	public void addLine(ArrayList<String> line) {
		
		String[] ln = new String[line.size()];

		for(int i=0;i<line.size();i++) {
			
			ln[i] = line.get(i);
		}

		this.table.add(ln);
	}

	/**
	 * @param line
	 */
	public void addLine(List<Object> line) {
		
		Object[] ln = new Object[line.size()];

		for(int i=0;i<line.size();i++) {
			
			ln[i] = line.get(i);
		}

		this.table.add(ln);
	}

	/**
	 * @param line
	 */
	public void addLine(String[] line) {

		this.table.add(line);
	}

	/**
	 * @param line
	 */
	public void addLine(Object[] line) {

		this.table.add(line);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		
		return this.columnsNames.length;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		
		return table.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object value, int row, int col) {
		
		table.get(row)[col] = value;
		fireTableCellUpdated(row, col);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int column) {
		return this.table.get(row)[column];
	}

	public String getColumnName(int col) {
		return columnsNames[col];
	}

	public String getName()
	{
		return this.name;
	}

	public boolean isCellEditable(int row, int col){
		if (this.getColumnClass(col).equals(Boolean.class)||this.getColumnClass(col).equals(String[].class))
		{
			return true;
		}
		else return false;
	}

	/**
	 * @return
	 */
	public ArrayList<Object[]> getTable() {
		
		return table;
	}

	/**
	 * @param row
	 * @return
	 */
	public Object[] getRow(int row) {
	
		return this.table.get(row);
	}

	/**
	 * Returns class of values in column <tt>columnIndex</tt>
	 * used to render each cell datatype
	 * @param columnIndex Number of column
	 * @return Class of values in column <tt>columnIndex</tt>
	 */
	@Override
	public Class<?> getColumnClass(int c) {

		if(this.getValueAt(0,c)!=null)
		{
			return this.getValueAt(0,c).getClass();
		}
		else return String.class;
	}

	/**
	 * @return
	 */
	public String[] getColumnsNames() {
		
		return columnsNames;
	}

	@Override
	public String toString() {
		
		return "DataTable [columnsNames=" + Arrays.toString(columnsNames)
				+ ", table=" + table + ", name=" + name + "]";
	}

}
