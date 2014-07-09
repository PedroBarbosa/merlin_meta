package views;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import datatypes.DataTable;
import datatypes.Table;

/**
 * @author ODias
 *
 */
public class TableView extends javax.swing.JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane1;
	private JPanel jPanel1;
	private JPanel jPanel2;
	private JPanel jPanel3;
	private JTable jTable1;


	/**
	 * @param dbt
	 */
	public TableView(Table dbt) {
		super();
		initGUI(dbt);
	}
	
	/**
	 * @param dbt
	 */
	private void initGUI(Table dbt) {
		try {
			GridBagLayout jPanel1Layout = new GridBagLayout();
			this.setLayout(jPanel1Layout);
			
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;

			c.weightx = 1.0;
			c.weighty = 0.08;
			c.gridx = 0;
			c.gridy = 1;
			
			jPanel2 = new JPanel();
			jPanel2.setLayout(new GridBagLayout());
			this.add(jPanel2, c);

			GridBagConstraints c2 = new GridBagConstraints();
			
			
			DataTable qrt = dbt.getValues();
			
			
			JLabel jLabel1 = new JLabel("Number of entries: "+qrt.getRowCount());
			c2.weightx = 0.95;
			c2.weighty = 1;
			c2.gridx = 0;
			c2.gridy = 1;
			c2.anchor = GridBagConstraints.WEST;
			jPanel2.add(jLabel1, c2);
			
			c.weightx = 1.0;
			c.weighty = 0.92;
			c.gridx = 0;
			c.gridy = 0;
			
			jPanel1 = new JPanel();
			GridBagLayout thisLayout = new GridBagLayout();
			jPanel1.setLayout(thisLayout);
			this.add(jPanel1, c);
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;

			gbc.weightx = 1.0;
			gbc.weighty = 1;
			gbc.gridx = 0;
			gbc.gridy = 0;
			
			
			
			jPanel3 = new JPanel();
			BorderLayout jPanel3Layout = new BorderLayout();
			jPanel3.setLayout(jPanel3Layout);
			jPanel1.add(jPanel3, gbc);
			jScrollPane1 = new JScrollPane();
			jPanel3.add(jScrollPane1);
			
			this.setBorder(BorderFactory.createTitledBorder(dbt.getName()+" Table"));
			jTable1 = new JTable();
			jScrollPane1.setViewportView(jTable1);
			jTable1.setModel(qrt);
			
			
			this.setPreferredSize(new java.awt.Dimension(400, 560));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
