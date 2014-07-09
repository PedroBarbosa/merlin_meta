package gui;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import utilities.CreateImageIcon;
import datatypes.DataTable;
import datatypes.metabolic_regulatory.HomologyDataContainer;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;

/**
 * @author ODias
 *
 */
public class InsertRemoveDataWindow extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -736419914729276231L;
	private JPanel jPanel1;
	private JScrollPane jScrollPane1;
	private JTable jTable1;
	private JPanel jPanel11;
	private JPanel jPanel12;
	private JButton remove;
	private JButton insert;
	private JButton close;
	private DataTable newDataModel;
	private HomologyDataContainer homologyData;
	private int key;
	private boolean enzyme,updateGui=false;
	private String windowName;
	private String locusTag;

	/**
	 * @param homologyData-
	 * @param row
	 * @param windowName
	 * @param enzyme
	 */
	public InsertRemoveDataWindow(HomologyDataContainer homologyData, int row, String windowName, boolean enzyme) {

		super(Workbench.getInstance().getMainFrame());
		this.homologyData = homologyData;
		this.windowName = windowName;
		this.key=Integer.parseInt(homologyData.getKeys().get(row));
		this.locusTag = homologyData.getInitialLocus().get(row);
		this.enzyme = enzyme;
		this.initGUI(windowName);
		Utilities.centerOnOwner(this);
		this.fillDataWindowList();
		this.setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
		this.setVisible(true);		
		this.setAlwaysOnTop(true);
		this.toFront();
	}


	/**
	 * @param windowName
	 */
	private void initGUI(String windowName) {

		try  {

			this.setTitle(windowName);
			jPanel1 = new JPanel();
			getContentPane().add(jPanel1);
			GridBagLayout jPanel1Layout = new GridBagLayout();
			jPanel1Layout.columnWeights = new double[] {0.0, 1, 0.0};
			jPanel1Layout.columnWidths = new int[] { 7, 20, 7};
			jPanel1Layout.rowWeights = new double[] {0.0, 0.1, 0.0};
			jPanel1Layout.rowHeights = new int[] {7, 7, 7};
			//	jPanel1.setPreferredSize(new java.awt.Dimension(426, 297));

			jPanel1.setLayout(jPanel1Layout);
			jPanel1.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));

			//			getContentPane().add(jPanel1, BorderLayout.CENTER);

			{
				jPanel11 = new JPanel();
				jPanel1.add(jPanel11, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.92, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 0, 0, 0), 0, 0));
				GridBagLayout jPanel11Layout = new GridBagLayout();
				jPanel11Layout.columnWeights = new double[] {1};
				jPanel11Layout.columnWidths = new int[] { 20};
				jPanel11Layout.rowWeights = new double[] {1};
				jPanel11Layout.rowHeights = new int[] { 7};
				jPanel11.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
				jPanel11.setLayout(jPanel11Layout);
				{
					jScrollPane1 = new JScrollPane();
					jPanel11.add(jScrollPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					{
						jTable1 = new JTable();
						jScrollPane1.setViewportView(jTable1);
					}
				}
			}
			{
				jPanel12 = new JPanel();
				jPanel1.add(jPanel12, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.05, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				GridBagLayout jPanel12Layout = new GridBagLayout();
				jPanel12Layout.columnWeights = new double[] {0.0, 0.0, 0.1, 0.0, 0.0, 0.1, 0.0, 0.0, 0.1, 0.0, 0.0};
				jPanel12Layout.columnWidths = new int[] {7, 7, 20, 7, 7, 20, 7, 7, 20, 7, 7};
				jPanel12Layout.rowWeights = new double[] {0.0, 0.1, 0.0};
				jPanel12Layout.rowHeights = new int[] {7, 7, 7};
				jPanel12.setLayout(jPanel12Layout);
				jPanel12.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
				{
					close = new JButton();
					jPanel12.add(close, new GridBagConstraints(8, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					close.setText("Close");
					close.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")),0.1).resizeImageIcon());	
					close.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {

							if(updateGui) {

								finishClose();
							}
							else {

								simpleFinish();
							}
						}
					});
					close.setPreferredSize(new Dimension(120,45));
				}
				{
					insert = new JButton();
					insert.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")),0.1).resizeImageIcon());
					jPanel12.add(insert, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					insert.setText("Insert");
					insert.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {

							insert();
						}
					});
					insert.setPreferredSize(new Dimension(120,45));
				}
				{
					remove = new JButton();
					jPanel12.add(remove, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					remove.setText("Remove");
					remove.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Delete.png")),0.1).resizeImageIcon());	
					remove.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent evt) {

							remove();
						}
					});
					remove.setPreferredSize(new Dimension(120,45));
				}
			}
			this.setModal(true);
			if(this.enzyme)
			{
				this.setSize(500, 385);			
			}
			else
			{
				this.setSize(1000, 385);		
			}

		}
		catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void finishClose() {}

	/**
	 * 
	 */
	public void simpleFinish() {

		this.setVisible(false);
		this.dispose();
	}

	/**
	 * 
	 */
	public void insert() {

		List<String> keys = new ArrayList<String>();
		for (int i=0; i<newDataModel.getRowCount();i++) {

			String datum = (String) newDataModel.getValueAt(i,0);
			
			if(!keys.contains(datum) && !datum.isEmpty()) {

				keys.add(datum);
			}
		}

		String [] data = new String[keys.size()+1];
		data[0]="";
		for (int i=0; i<keys.size();i++) {

			data[i+1]=keys.get(i);
		}

		if(enzyme) {

			homologyData.setEditedEnzymeData(key ,data);
		}
		else {

			homologyData.setEditedProductData(key ,data);
		}

		/**
		 * @author ODias
		 *
		 */
		new InsertNewEntryDialog(enzyme, key, homologyData) {

			private static final long serialVersionUID = -7075058626735407587L;

			public void finish() {

				this.setVisible(false);
				this.dispose();
				fillDataWindowList();
				updateGui=true;
			}						
		};

	}

	/**
	 * 
	 */
	public void remove() {

		if(jTable1.getSelectedRow()>-1) {

			updateGui=true;
			String removed = (String) newDataModel.getValueAt(jTable1.getSelectedRow(), 0);
			newDataModel.getTable().remove(jTable1.getSelectedRow());
			jTable1.setModel(newDataModel);
			jScrollPane1.setViewportView(jTable1);

			String [] data = new String[newDataModel.getRowCount()];

			for (int i=0; i<newDataModel.getRowCount();i++) {

				data[i]=(String) newDataModel.getValueAt(i,0);
			}

			boolean hasEmptyString = false;

			for(String string : data) {

				if(string.trim().isEmpty()) {

					hasEmptyString = true;
				}
			}

			if(!hasEmptyString) {

				data = new String[newDataModel.getRowCount()+1];
				data[0]="";
				for (int i=0; i<newDataModel.getRowCount();i++) {

					data[i+1]=(String) newDataModel.getValueAt(i,0);
				}
			}

			if(enzyme) {

				homologyData.setEditedEnzymeData(key, data);

				if(homologyData.getEnzymesList().containsKey(key) && homologyData.getEnzymesList().get(key).equals(removed)) {

					if(data.length>1) {

						homologyData.getEnzymesList().put(key, data[1]);
					}
					else {

						homologyData.getEnzymesList().put(key, data[0]);
					}
				}
			}
			else {

				homologyData.setEditedProductData(key ,data);				

				if((homologyData.getProductList().containsKey(key) && homologyData.getProductList().get(key).equals(removed)) ) {

					if(data.length>1) {

						homologyData.getProductList().put(key, data[1]);
					}
					else {

						homologyData.getProductList().put(key, data[0]);
					}
				}
			}
		}
	}

	/**
	 * 
	 */
	public  void fillDataWindowList() {

		ArrayList<String> data = new ArrayList<String>();
		DataTable oldData;

		if(enzyme) {

			oldData = homologyData.getECBlastSelectionPane(this.locusTag);
			
			if(homologyData.getEditedEnzymeData().get(key)==(null)) {

				newDataModel=oldData;
			}
			else {
				
				for(String s:homologyData.getEditedEnzymeData().get(key)) {

					data.add(s);
				}
				this.processData(oldData, data);
			}
		}
		else {

			oldData=homologyData.getProductSelectionPane(this.locusTag);

			if(homologyData.getEditedProductData().get(key)==(null)) {

				newDataModel=oldData;
			}
			else {

				for(String s:homologyData.getEditedProductData().get(key)) {

					data.add(s);
				}
				this.processData(oldData, data);
			}
		}
		jTable1 = new JTable();
		jTable1.setModel(newDataModel);
		jScrollPane1.setViewportView(jTable1);

	}

	/**
	 * @param oldData
	 * @param data
	 */
	private void processData(DataTable oldData, ArrayList<String> data) {

		newDataModel = new DataTable(oldData.getColumnsNames(),windowName);

		List<String> remove_keys = new ArrayList<String>();
		for (int i=0; i<oldData.getRowCount();i++) {

			String old_datum = (String)oldData.getValueAt(i,0);
			if(data.contains(old_datum)) {

				newDataModel.addLine((String[])oldData.getRow(i));
				remove_keys.add(old_datum);
			}
		}

		data.removeAll(remove_keys);
		ArrayList<String> dataClone = new ArrayList<String>(data);

		for(String datum:dataClone) {

			String[] s= new String[7];
			s[0]=datum;
			s[1]="manual";
			s[2]="manual";
			s[3]="manual";
			s[4]="manual";
			s[5]="manual";
			s[6]="manual";
			newDataModel.addLine(s);
			data.remove(datum);
		}
	}

}
