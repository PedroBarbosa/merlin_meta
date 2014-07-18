package views.metagenomics;
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
import java.util.EventObject;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;

import utilities.ButtonColumn;
import utilities.CreateImageIcon;
import utilities.SearchInTable;
import views.windows.GenericDetailWindow;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.metagenomics.EnzymesMetaContainer;
import es.uvigo.ei.aibench.workbench.Workbench;


/**
 * @author pedro
 *
 */
public class EnzymesMetaView extends javax.swing.JPanel {

	private static final long serialVersionUID = 7348937284724896584L;
	private JScrollPane jScrollPane1;
	private JButton jButtonExportTxt;
	private ButtonGroup buttonGroup1;
	private JPanel jPanel1;
	private JPanel jPanel2;
	private JTable jTable;
	private GenericDataTable mainTableData;
	private JPanel jPanelExport;
	private String selectedRowID;
	private SearchInTable searchInGene;
	private ButtonColumn buttonColumn;
	private EnzymesMetaContainer enzymesMetacontainer;

	public EnzymesMetaView(EnzymesMetaContainer metaenzymes) {

		super();
		this.enzymesMetacontainer = metaenzymes;
		this.searchInGene = new SearchInTable();

		if(this.enzymesMetacontainer.isTaxonomyDefined()){
			this.initGUI();
			this.fillList();		
		}
		
		else Workbench.getInstance().error("No taxonomy information is available.\nPlease generate community taxonomy first and re-open the enzymes entity");		

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
			this.setLayout(jPanelLayout);
			jPanel2 = new JPanel();
			GridBagLayout jPanel2Layout = new GridBagLayout();
			jPanel2Layout.columnWeights = new double[] {0.1, 0.0, 0.1, 0.1};
			jPanel2Layout.columnWidths = new int[] {7, 7, 7, 7};
			jPanel2Layout.rowWeights = new double[] {0.1, 0.0};
			jPanel2Layout.rowHeights = new int[] {7, 7};
			jPanel2.setLayout(jPanel2Layout);
			this.add(jPanel2, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			{
				buttonGroup1 = new ButtonGroup();
			}
			{	


				jPanelExport = new JPanel();
				GridBagLayout jPanelExportLayout = new GridBagLayout();
				jPanelExportLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
				jPanelExportLayout.columnWidths = new int[] {7, 7, 7};
				jPanelExportLayout.rowWeights = new double[] {0.0};
				jPanelExportLayout.rowHeights = new int[] {5};
				jPanelExport.setLayout(jPanelExportLayout);
				jPanel2.add(jPanelExport, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelExport.setBounds(567, 56, 139, 61);
				jPanelExport.setBorder(BorderFactory.createTitledBorder("Export"));
				{
					jButtonExportTxt = new JButton();
					jPanelExport.add(jButtonExportTxt, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonExportTxt.setText("Enzymes coverage");
					jButtonExportTxt.setToolTipText("Export to txt tabbed file");
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

									enzymesMetacontainer.exportEnzymesCoverage(new File(filePath+"/"+enzymesMetacontainer.getProject().getName()+"_EnzymesCoverage.txt"));
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
				jPanelExport.setBounds(567, 56, 139, 61);
			}
			{
				jPanel2.add(this.searchInGene.addPanel(), new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
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
	public void fillList() {

		try {

			mainTableData = this.enzymesMetacontainer.getEnzymesData();
			jTable = new JTable();
			jTable.setModel(mainTableData);
			//System.out.println(mainTableData);
			jScrollPane1.setViewportView(jTable);
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
			tc.getColumn(0).setModelIndex(0);
			tc.getColumn(0).setResizable(false);
			
			tc.getColumn(2).setMinWidth(80);				//ecnumber			
			tc.getColumn(2).setModelIndex(2);
			
			tc.getColumn(3).setMinWidth(85);				//reactions
			tc.getColumn(3).setModelIndex(3);
			
			tc.getColumn(4).setMinWidth(80);				//encoding genes
			tc.getColumn(4).setModelIndex(4);
			
			tc.getColumn(5).setMinWidth(85);				//abundance
			tc.getColumn(5).setModelIndex(5);
			
			tc.getColumn(6).setMinWidth(200);				//genus encoding
			tc.getColumn(6).setModelIndex(6);
			
			tc.getColumn(7).setMinWidth(160);;				//no genus
			tc.getColumn(7).setModelIndex(7);
			
			searchInGene.setJTable(jTable);
			searchInGene.setMainTableData(mainTableData);
			this.searchInGene.setSearchTextField("");

		}
		catch(Exception e) {

			e.printStackTrace();
		}
	}




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
		DataTable[] q = enzymesMetacontainer.getRowInfo(selectedRowID);

		new GenericDetailWindow(q,  mainTableData.getWindowName(), "Enzyme: "+jTable.getValueAt(jTable.getSelectedRow(), 2));
	}



}