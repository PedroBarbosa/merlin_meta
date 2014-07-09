package views.metabolic_regulatory;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Calendar;
import java.util.EventObject;
import java.util.GregorianCalendar;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;

import utilities.ButtonColumn;
import utilities.CreateImageIcon;
import utilities.SaveToTxt;
import utilities.SearchInTable;
import views.UpdatablePanel;
import views.windows.GenericDetailWindow;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.metabolic_regulatory.Proteins;
import es.uvigo.ei.aibench.workbench.Workbench;
import gui.InsertEditProtein;


/**
 * @author ODias
 *
 */
public class ProteinView extends UpdatablePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane1;
	private JButton jButtonExportTxt;
	//	private JRadioButton jRadioButton3;
	//	private JRadioButton jRadioButton2;
	private JRadioButton jRadioButton1;
	private ButtonGroup buttonGroup1;
	private JPanel jPanel1;
	private JPanel jPanel2;
	private JTable jTable;
	private Proteins proteins;
	private GenericDataTable mainTableData;
	private JPanel jPanelProteins;
	private JPanel jPanelRemove;
	private JButton jButtonRemove;
	private JComboBox<String> jComboBoxRemove;
	private JPanel jPanelInsertEdit;
	private JButton jButtonInsert;
	private JButton jButtonEdit;
	private JPanel jPanelExport;
	private String selectedRowID;
	private AbstractButton jRadioButtonEncoded;
	private SearchInTable searchInProteins;
	private ButtonColumn buttonColumn;


	/**
	 * @param proteins
	 */
	public ProteinView(Proteins proteins) {
		super(proteins);
		this.proteins = proteins;
		this.searchInProteins = new SearchInTable();
		this.initGUI();
		this.fillList();
		this.addListenersToGraphicalObjects();
	}

	/**
	 * 
	 */
	private void initGUI() {
		
		try {
			
			GridBagLayout jPanelLayout = new GridBagLayout();
			jPanelLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
			jPanelLayout.columnWidths = new int[] {7, 7, 7};
			jPanelLayout.rowWeights = new double[] {0.0, 200.0, 0.0, 0.0, 0.0};
			jPanelLayout.rowHeights = new int[] {7, 50, 7, 3, 7};
			//			jPanelLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
			//			jPanelLayout.columnWidths = new int[] {7, 7, 7};
			//			jPanelLayout.rowWeights = new double[] {0.0, 2.5, 0.0, 0.1, 0.0};
			//			jPanelLayout.rowHeights = new int[] {5, 25, 5, 5, 5};
			this.setLayout(jPanelLayout);
			jPanel2 = new JPanel();
			GridBagLayout jPanel2Layout = new GridBagLayout();
			jPanel2Layout.columnWeights = new double[] {0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1};
			jPanel2Layout.columnWidths = new int[] {7, 7, 7, 7, 7, 7, 7};
			jPanel2Layout.rowWeights = new double[] {0.1, 0.0};
			jPanel2Layout.rowHeights = new int[] {7, 7};
			jPanel2.setLayout(jPanel2Layout);
			this.add(jPanel2, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			{
				jPanelProteins = new JPanel();
				GridBagLayout jPanelGenesLayout = new GridBagLayout();
				jPanelGenesLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
				jPanelGenesLayout.columnWidths = new int[] {7, 7, 7};
				jPanelGenesLayout.rowWeights = new double[] {0.1, 0.1, 0.1};
				jPanelGenesLayout.rowHeights = new int[] {5, 5, 5};
				jPanelProteins.setLayout(jPanelGenesLayout);
				jPanel2.add(jPanelProteins, new GridBagConstraints(6, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelProteins.setBounds(718, 6, 157, 115);
				jPanelProteins.setBorder(BorderFactory.createTitledBorder("Protein Types"));
				{
					jRadioButtonEncoded = new JRadioButton();
					jPanelProteins.add(jRadioButtonEncoded, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jRadioButtonEncoded.setText("In Model");
				}
				{
					jRadioButton1 = new JRadioButton();
					jRadioButton1.setSelected(!this.proteins.existGenes());
					jPanelProteins.add(jRadioButton1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jRadioButton1.setText("All proteins");
				}
				buttonGroup1 = new ButtonGroup();
				buttonGroup1.add(jRadioButtonEncoded);
				jRadioButtonEncoded.setSelected(this.proteins.existGenes());
				jRadioButtonEncoded.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						fillList();
					}
				});
				buttonGroup1.add(jRadioButton1);
				jRadioButton1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						fillList();
					}
				});
			}
			{
				jPanelExport = new JPanel();
				GridBagLayout jPanelExportLayout = new GridBagLayout();
				jPanelExportLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
				jPanelExportLayout.columnWidths = new int[] {7, 7, 7};
				jPanelExportLayout.rowWeights = new double[] {0.0};
				jPanelExportLayout.rowHeights = new int[] {5};
				jPanelExport.setLayout(jPanelExportLayout);
				jPanel2.add(jPanelExport, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelExport.setBounds(567, 56, 139, 61);
				jPanelExport.setBorder(BorderFactory.createTitledBorder("Export"));
				{
					jButtonExportTxt = new JButton();
					jPanelExport.add(jButtonExportTxt, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonExportTxt.setText("text file");
					jButtonExportTxt.setToolTipText("Export to xls tabbed file");
					jButtonExportTxt.setBounds(11, 8, 118, 48);
					jButtonExportTxt.setIcon(new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Download.png"))),0.1).resizeImageIcon());
					jButtonExportTxt.setPreferredSize(new Dimension(90, 40));
					jButtonExportTxt.setSize(90, 40);
					jButtonExportTxt.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent arg0)  {

							try {

								JFileChooser fc = new JFileChooser();
								fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
								fc.setDialogTitle("Select directory");
								int returnVal = fc.showOpenDialog(new JTextArea());

								if (returnVal == JFileChooser.APPROVE_OPTION) {

									File file = fc.getSelectedFile();
									String filePath = file.getAbsolutePath();
									Calendar cal = new GregorianCalendar();

									// Get the components of the time
									int hour24 = cal.get(Calendar.HOUR_OF_DAY);     // 0..23
									int min = cal.get(Calendar.MINUTE);             // 0..59
									int day = cal.get(Calendar.DAY_OF_YEAR);		//0..365

									filePath += "/"+proteins.getName()+"_"+proteins.getProject().getName()+"_"+hour24+"_"+min+"_"+day+".xls";

									String[][] results = SaveToTxt.qrtableToMatrix(mainTableData);

									String header ="";
									TableColumnModel tc = jTable.getColumnModel();
									int headerSize = tc.getColumnCount();
									int i = 0;
									while (i < headerSize) {

										header+=tc.getColumn(i).getHeaderValue().toString()+"\t";
										i++;
									}
									SaveToTxt.save_matrix(filePath, header.trim(), results, proteins.getName());
									Workbench.getInstance().info("Data successfully exported.");
								}
							} catch (Exception e) {

								Workbench.getInstance().error("An error occurred while performing this operation. Error "+e.getMessage());
								e.printStackTrace();
							}
						}
					});
				}
			}
			{
				jPanelRemove = new JPanel();
				GridBagLayout jPanelRemoveLayout = new GridBagLayout();
				jPanelRemoveLayout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
				jPanelRemoveLayout.columnWidths = new int[] {7, 7, 7, 7, 7};
				jPanelRemoveLayout.rowWeights = new double[] {0.0};
				jPanelRemoveLayout.rowHeights = new int[] {5};
				jPanelRemove.setLayout(jPanelRemoveLayout);
				jPanel2.add(jPanelRemove, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelRemove.setBounds(297, 56, 256, 61);
				jPanelRemove.setBorder(BorderFactory.createTitledBorder("Remove"));
				{
					jButtonRemove = new JButton();
					jPanelRemove.add(jButtonRemove, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonRemove.setText("Remove");
					jButtonRemove.setToolTipText("Remove data");
					jButtonRemove.setPreferredSize(new Dimension(90, 40));
					jButtonRemove.setSize(90, 40);
					jButtonRemove.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent arg0) {
							
							try {
								
								if(jComboBoxRemove.getSelectedItem()=="Remove Row" && jTable.getSelectedRow()!=-1) {
									
									proteins.RemoveProtein(jTable.convertRowIndexToModel(jTable.getSelectedRow()), jRadioButtonEncoded.isSelected());
									fillList();
								}
								else {
								
									if(jComboBoxRemove.getSelectedItem()=="Remove All") {
										
										proteins.RemoveProtein(-1, jRadioButtonEncoded.isSelected());
									}
									else {
										
										Workbench.getInstance().warn("Please Select a Row,\n or Select Remove all!");	
									}
								}
							}
							catch (Exception e) {
								
								e.printStackTrace();
							}
							fillList();
						}
					});
				}
				jButtonRemove.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Remove.png")),0.1).resizeImageIcon());
				{
					ComboBoxModel<String> jComboBox1Model = 
							new DefaultComboBoxModel<>(
									new String[] { "Remove Row", "Remove All" });
					jComboBoxRemove = new JComboBox<>();
					jPanelRemove.add(jComboBoxRemove, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jComboBoxRemove.setBounds(8, 19, 107, 26);
					jComboBoxRemove.setModel(jComboBox1Model);
				}
			}
			{
				jPanelInsertEdit = new JPanel();
				GridBagLayout jPanelInsertEditLayout = new GridBagLayout();
				jPanelInsertEditLayout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
				jPanelInsertEditLayout.columnWidths = new int[] {7, 7, 7, 7, 7};
				jPanelInsertEditLayout.rowWeights = new double[] {0.0};
				jPanelInsertEditLayout.rowHeights = new int[] {5};
				jPanelInsertEdit.setLayout(jPanelInsertEditLayout);
				jPanel2.add(jPanelInsertEdit, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelInsertEdit.setBounds(7, 56, 277, 61);
				jPanelInsertEdit.setBorder(BorderFactory.createTitledBorder("Insert/Edit"));
				{
					jButtonInsert = new JButton();
					jPanelInsertEdit.add(jButtonInsert, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonInsert.setIcon( new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Upload.png")),0.1).resizeImageIcon());
					jButtonInsert.setText("Insert");
					jButtonInsert.setToolTipText("Insert new protein");
					jButtonInsert.setPreferredSize(new Dimension(90, 40));
					jButtonInsert.setSize(90, 40);
					jButtonInsert.addActionListener( new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {
							//InsertProteinDialog inst = new InsertProteinDialog(proteins);
							//							InsertEditProtein inst = 
							new InsertEditProtein(-10, proteins);
							//							inst.setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
							//							inst.setVisible(true);		
							//							inst.setAlwaysOnTop(true);
							fillList();
						}
					}
							);
				}
				{
					jButtonEdit = new JButton();
					jPanelInsertEdit.add(jButtonEdit, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonEdit.setText("Edit");
					jButtonEdit.setToolTipText("Edit protein data");
					//					jButtonEdit.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/export.png"))));
					jButtonEdit.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Edit.png")),0.1).resizeImageIcon());
					jButtonEdit.setPreferredSize(new Dimension(90, 40));
					jButtonEdit.setSize(90, 40);
					jButtonEdit.addActionListener( new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {
							if(jTable.getSelectedRow()>-1)
							{
								//InsertEditProtein inst = 
								new InsertEditProtein(jTable.convertRowIndexToModel(jTable.getSelectedRow()), proteins);
								//								inst.setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
								//								inst.setVisible(true);		
								//								inst.setAlwaysOnTop(true);
								fillList();
							}
							else
							{
								Workbench.getInstance().warn("Please Select a Row!");
							}
						}
					}
							);
				}
			}
			{
				jPanel2.add(this.searchInProteins.addPanel(), new GridBagConstraints(0, 1, 7, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			}

			jPanel1 = new JPanel();
			GridBagLayout thisLayout = new GridBagLayout();
			jPanel1.setLayout(thisLayout);
			this.add(jPanel1, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			jScrollPane1 = new JScrollPane();
			jTable = new JTable();
			jScrollPane1.setViewportView(jTable);
			//			jTable.addMouseListener(new MouseAdapter() {
			//				public void mouseClicked(MouseEvent evt) {
			//					jTable1MouseClicked(evt);
			//				}
			//			});
			jPanel1.add(jScrollPane1,new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			this.setPreferredSize(new java.awt.Dimension(887, 713));
		}
		catch (Exception e) {e.printStackTrace();}
	}

	/**
	 * 
	 */
	public void fillList(){
		
		jTable = new JTable();
		jScrollPane1.setViewportView(jTable);

		if(this.jRadioButtonEncoded.isSelected()) {
			
			mainTableData = this.proteins.getAllProteins(true);
		}
		else if(this.jRadioButton1.isSelected()) {
			
			mainTableData = this.proteins.getAllProteins(false);
		}

		jTable.setModel(mainTableData);
		jTable.setAutoCreateRowSorter(true);
		
		buttonColumn =  new ButtonColumn(jTable,0, new ActionListener(){

			public void actionPerformed(ActionEvent arg0){
				processButton(arg0);
			}},
			new MouseAdapter(){
				public void mouseClicked(MouseEvent e) {
					// {
					// get the coordinates of the mouse click
					Point p = e.getPoint();

					// get the row index that contains that coordinate
					int rowNumber = jTable.rowAtPoint(p);
					int  columnNumber = jTable.columnAtPoint(p);
					jTable.setColumnSelectionInterval(columnNumber, columnNumber);
					// Get the ListSelectionModel of the JTable
					ListSelectionModel model = jTable.getSelectionModel();
					// set the selected interval of rows. Using the "rowNumber"
					// variable for the beginning and end selects only that one row.
					model.setSelectionInterval( rowNumber, rowNumber );
					processButton(e);
				}
			});
		TableColumnModel tc = jTable.getColumnModel();
		tc.getColumn(0).setMaxWidth(35);				//button
		tc.getColumn(0).setResizable(false);
		tc.getColumn(0).setModelIndex(0);

		this.searchInProteins.setJTable(jTable);
		this.searchInProteins.setMainTableData(mainTableData);
		this.searchInProteins.setSearchTextField("");
	}


	/**
	 * @param arg0
	 */
	private void processButton(EventObject arg0) {
		
		JButton button = null;
		if(arg0.getClass()==ActionEvent.class) {
			
			button = (JButton)((ActionEvent) arg0).getSource();
		}

		if(arg0.getClass()==MouseEvent.class) {
			
			button = (JButton)((ActionEvent) arg0).getSource();
		}

		ListSelectionModel model = jTable.getSelectionModel();
		model.setSelectionInterval( buttonColumn.getSelectIndex(button), buttonColumn.getSelectIndex(button));

		selectedRowID = mainTableData.getRowId(jTable.convertRowIndexToModel(jTable.getSelectedRow()));

		DataTable[] q = proteins.getRowInfo(selectedRowID);

		new GenericDetailWindow(q, "Protein data", "Protein: "+proteins.getReactionName(selectedRowID));
	}

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#updateGraphicalObject()
	 */
	@Override
	public void updateTableUI() {

		this.fillList();
		this.updateUI();
		this.revalidate();
		this.repaint();
	}

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#addListenersToGraphicalObjects(javax.swing.JPanel, javax.swing.JTable)
	 */
	@Override
	public void addListenersToGraphicalObjects() {}
}
