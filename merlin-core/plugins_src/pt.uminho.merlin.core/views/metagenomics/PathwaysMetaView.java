package views.metagenomics;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Calendar;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.jar.Attributes.Name;

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
import views.windows.GenericDetailWindow;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.Table;
import datatypes.metabolic_regulatory.Proteins;
import datatypes.metagenomics.PathwaysMetaContainer;
import datatypes.metagenomics.TaxonomyMetaContainer;
import es.uvigo.ei.aibench.workbench.Workbench;
import gui.InsertEditProtein;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.DropMode;


/**
 * @author pedro
 *
 */
public class PathwaysMetaView extends javax.swing.JPanel {

	private static final long serialVersionUID = 7348937284724896584L;
	private JScrollPane jScrollPane1;
	private JButton jButtonExportTxt;
	private ButtonGroup buttonGroup1;
	private JPanel jPanel1;
	private JPanel jPanel2;
	private JTable jTable;
	private Proteins proteins;
	private GenericDataTable mainTableData;
	private JPanel jPanelRecalculate;
	private JButton jButtonRecalculate;
	private JPanel jPanelSetparameters;
	private JPanel jPanelExport;
	private String selectedRowID;
	private SearchInTable searchInPathway;
	private ButtonColumn buttonColumn;
	private JLabel lblpvalue;
	private JTextField textpvalue;
	private JLabel lblgenusproportionscore;
	private JTextField textgenusproportion;
	private PathwaysMetaContainer pathwaysMetaContainer;
	private JButton jButtonExportpathwaysPerGenus;


	public PathwaysMetaView(PathwaysMetaContainer pathwaysMetaContainer) {
		super();
		this.pathwaysMetaContainer = pathwaysMetaContainer;
		this.searchInPathway = new SearchInTable();
		if(this.pathwaysMetaContainer.isENzymesDefined()){
			this.initGUI();
			this.fillList();		
		}

		else Workbench.getInstance().error("No metagenomic enzymes information is available.\nPlease generate community enzymes first and re-open the pathways entity");		

	}



	/**
	 * 
	 */
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

				{
					jPanelSetparameters = new JPanel();
					GridBagLayout jPanelInsertEditLayout = new GridBagLayout();
					jPanelInsertEditLayout.columnWeights = new double[] {0.0, 0.1, 1.0};
					jPanelInsertEditLayout.columnWidths = new int[] {7, 7, 7,};
					jPanelInsertEditLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0};
					jPanelInsertEditLayout.rowHeights = new int[] {5, 0, 0, 0};
					jPanelSetparameters.setLayout(jPanelInsertEditLayout);
					jPanel2.add(jPanelSetparameters, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jPanelSetparameters.setBounds(7, 56, 150, 61);
					jPanelSetparameters.setBorder(BorderFactory.createTitledBorder("Set parameters"));
					//					{
					//						lblcoveragescore = new JLabel("Minimum  score for patwhay coverage:");
					//						lblcoveragescore.setHorizontalAlignment(SwingConstants.LEFT);
					//						GridBagConstraints gbc_lblcoveragescore = new GridBagConstraints();
					//						gbc_lblcoveragescore.gridy = 1;
					//						gbc_lblcoveragescore.insets = new Insets(0, 0, 5, 5);
					//						gbc_lblcoveragescore.gridx = 0;
					//						jPanelSetparameters.add(lblcoveragescore, gbc_lblcoveragescore);
					//					}
					{
						lblpvalue = new JLabel("Threshold for p-value:");
						lblpvalue.setHorizontalAlignment(SwingConstants.LEFT);
						GridBagConstraints gbc_lblcoveragescore = new GridBagConstraints();
						gbc_lblcoveragescore.gridy = 1;
						gbc_lblcoveragescore.insets = new Insets(0, 0, 5, 5);
						gbc_lblcoveragescore.gridx = 0;
						jPanelSetparameters.add(lblpvalue, gbc_lblcoveragescore);
					}
					{
						//						{
						//							textcoveragescore = new JTextField();
						//							textcoveragescore.setHorizontalAlignment(SwingConstants.CENTER);
						//							textcoveragescore.setText("0.5");
						//							GridBagConstraints gbc_textcoveragescore = new GridBagConstraints();
						//							gbc_textcoveragescore.insets = new Insets(0, 0, 5, 5);
						//							gbc_textcoveragescore.fill = GridBagConstraints.HORIZONTAL;
						//							gbc_textcoveragescore.gridx = 1;
						//							gbc_textcoveragescore.gridy = 1;
						//							jPanelSetparameters.add(textcoveragescore, gbc_textcoveragescore);
						//							textcoveragescore.setColumns(10);
						//						}
						{
							textpvalue = new JTextField();
							textpvalue.setHorizontalAlignment(SwingConstants.CENTER);
							textpvalue.setText("0.1");
							GridBagConstraints gbc_textcoveragescore = new GridBagConstraints();
							gbc_textcoveragescore.insets = new Insets(0, 0, 5, 5);
							gbc_textcoveragescore.fill = GridBagConstraints.HORIZONTAL;
							gbc_textcoveragescore.gridx = 1;
							gbc_textcoveragescore.gridy = 1;
							jPanelSetparameters.add(textpvalue, gbc_textcoveragescore);
							textpvalue.setColumns(10);
						}
						{
							lblgenusproportionscore = new JLabel("Proportion of encoded enzymes in genus:");
							GridBagConstraints gbc_lblgenusproportionscore = new GridBagConstraints();
							gbc_lblgenusproportionscore.insets = new Insets(0, 0, 0, 5);
							gbc_lblgenusproportionscore.gridx = 0;
							gbc_lblgenusproportionscore.gridy = 2;
							jPanelSetparameters.add(lblgenusproportionscore, gbc_lblgenusproportionscore);
						}
						{
							textgenusproportion = new JTextField();
							textgenusproportion.setHorizontalAlignment(SwingConstants.CENTER);
							textgenusproportion.setText("0.75");
							GridBagConstraints gbc_textgenusproportion = new GridBagConstraints();
							gbc_textgenusproportion.anchor = GridBagConstraints.NORTH;
							gbc_textgenusproportion.insets = new Insets(0, 0, 0, 5);
							gbc_textgenusproportion.fill = GridBagConstraints.HORIZONTAL;
							gbc_textgenusproportion.gridx = 1;
							gbc_textgenusproportion.gridy = 2;
							jPanelSetparameters.add(textgenusproportion, gbc_textgenusproportion);
							textgenusproportion.setColumns(10);
						}
					}
				}



				jPanelExport = new JPanel();
				GridBagLayout jPanelExportLayout = new GridBagLayout();
				jPanelExportLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
				jPanelExportLayout.columnWidths = new int[] {7, 7, 7};
				jPanelExportLayout.rowWeights = new double[] {0.0, 0.0};
				jPanelExportLayout.rowHeights = new int[] {5, 0};
				jPanelExport.setLayout(jPanelExportLayout);
				jPanel2.add(jPanelExport, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelExport.setBounds(567, 56, 139, 61);
				jPanelExport.setBorder(BorderFactory.createTitledBorder("Export"));
				{
					jButtonExportTxt = new JButton();
					jPanelExport.add(jButtonExportTxt, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));
					jButtonExportTxt.setText("Pathway coverage");
					jButtonExportTxt.setToolTipText("Export pathway presence/absence to txt file");
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

									pathwaysMetaContainer.exportPathwayCoverage(new File(filePath+"/"+pathwaysMetaContainer.getProject().getName()+"PathwaysCoverage.txt"));
									Workbench.getInstance().info("Data successfully exported.");
								}
							} catch (Exception e) {

								Workbench.getInstance().error("An error occurred while performing this operation. Error "+e.getMessage());
								e.printStackTrace();
							}
						}
					});
				}

				jPanelExport.setBounds(567, 56, 139, 61);
				{
					jButtonExportpathwaysPerGenus = new JButton("Pathway/Genus info");
					GridBagConstraints gbc_pathwaysPerGenus = new GridBagConstraints();
					gbc_pathwaysPerGenus.insets = new Insets(0, 0, 0, 5);
					gbc_pathwaysPerGenus.fill = GridBagConstraints.HORIZONTAL;
					gbc_pathwaysPerGenus.gridx = 1;
					gbc_pathwaysPerGenus.gridy = 1;
					jPanelExport.add(jButtonExportpathwaysPerGenus, gbc_pathwaysPerGenus);
					jButtonExportpathwaysPerGenus.setFont(new Font("Dialog", Font.PLAIN, 11));
					jButtonExportpathwaysPerGenus.setToolTipText("Export pathway / genus information to txt file");
					jButtonExportpathwaysPerGenus.setBounds(11, 8, 118, 48);
					jButtonExportpathwaysPerGenus.setIcon(new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Download.png"))),0.1).resizeImageIcon());
					jButtonExportpathwaysPerGenus.setPreferredSize(new Dimension(90, 40));
					jButtonExportpathwaysPerGenus.setSize(90, 40);
					jButtonExportpathwaysPerGenus.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent arg0)  {

							try {


								JFileChooser fc = new JFileChooser();
								fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
								fc.setDialogTitle("Select directory");
								int returnVal = fc.showOpenDialog(new JTextArea());

								if (returnVal == JFileChooser.APPROVE_OPTION) {

									File file = fc.getSelectedFile();
									String filePath = file.getAbsolutePath();

									pathwaysMetaContainer.exportPathwaysPerGenus(new File(filePath+"/"+pathwaysMetaContainer.getProject().getName()+"pathway_genus.txt"));
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
				jPanelRecalculate = new JPanel();
				GridBagLayout jPanelRemoveLayout = new GridBagLayout();
				jPanelRemoveLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
				jPanelRemoveLayout.columnWidths = new int[] {7, 7, 7};
				jPanelRemoveLayout.rowWeights = new double[] {0.0};
				jPanelRemoveLayout.rowHeights = new int[] {5};
				jPanelRecalculate.setLayout(jPanelRemoveLayout);
				jPanel2.add(jPanelRecalculate, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

				jPanelRecalculate.setBorder(BorderFactory.createTitledBorder("Recalculate"));

				{

					jButtonRecalculate = new JButton();
					jPanelRecalculate.add(jButtonRecalculate, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonRecalculate.setText("Pathways data");
					jButtonRecalculate.setToolTipText("Recalculate with the new parameters");
					jButtonRecalculate.setBounds(11, 8, 118, 48);
					jButtonRecalculate.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")),0.1).resizeImageIcon());
					jButtonRecalculate.setPreferredSize(new Dimension(90, 40));
					jButtonRecalculate.setSize(90, 40);
					jButtonRecalculate.addActionListener(new ActionListener() {


						public void actionPerformed(ActionEvent arg0) {

							recalculate();

						}
					});




				}
			}


			{
				jPanel2.add(this.searchInPathway.addPanel(), new GridBagConstraints(0, 1, 7, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
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

	public void fillList() {

		mainTableData = this.pathwaysMetaContainer.getPathwaysData();
		jTable.setModel(mainTableData);
		//System.out.println(mainTableData);
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

		//		tc.getColumn(2).setMinWidth(95);		//nº enzimes			
		//	//	tc.getColumn(2).setMaxWidth(100);
		//		tc.getColumn(2).setModelIndex(2);
		//		
		//		tc.getColumn(3).setMinWidth(110);		//codified enzimes
		//	//	tc.getColumn(3).setMaxWidth(115);
		//		tc.getColumn(3).setModelIndex(3);
		//		
		//		tc.getColumn(4).setMinWidth(60);	
		//		tc.getColumn(4).setMaxWidth(60);		//score
		//		tc.getColumn(4).setModelIndex(4);		

		//tc.getColumn(4).setMinWidth(80);				//p-value
		//tc.getColumn(4).setMaxWidth(65);
		//tc.getColumn(4).setModelIndex(4);

		tc.getColumn(5).setMinWidth(125);				//is significantly present?
		//tc.getColumn(5).setMaxWidth(110);
		tc.getColumn(5).setModelIndex(5);

		tc.getColumn(6).setMinWidth(70);				//abundance
		//tc.getColumn(5).setMaxWidth(95);
		tc.getColumn(6).setModelIndex(6);

		tc.getColumn(7).setMinWidth(225);				//number of genus
		tc.getColumn(7).setModelIndex(7);
		////	tc.getColumn(6).setMaxWidth(225);				
		////	tc.getColumn(6).setResizable(false);

		//		
		//		tc.getColumn(7).setMinWidth(190);;				//genus
		//		tc.getColumn(7).setModelIndex(7);

		this.searchInPathway.setJTable(jTable);
		this.searchInPathway.setMainTableData(mainTableData);
		this.searchInPathway.setSearchTextField("");
	}


	private void processButton(EventObject arg0){
		JButton button = null;
		if(arg0.getClass()==ActionEvent.class)
		{
			button = (JButton)((ActionEvent) arg0).getSource();
		}

		if(arg0.getClass()==MouseEvent.class)
		{
			button = (JButton)((ActionEvent) arg0).getSource();
		}			
		button.setSelected(true);

		ListSelectionModel model = jTable.getSelectionModel();
		model.setSelectionInterval( buttonColumn.getSelectIndex(button), buttonColumn.getSelectIndex(button));

		selectedRowID = mainTableData.getRowId(jTable.convertRowIndexToModel(jTable.getSelectedRow()));
		DataTable[] table = this.pathwaysMetaContainer.getRowInfo(selectedRowID);

		new GenericDetailWindow(table, mainTableData.getWindowName(), "Pathway: "+jTable.getValueAt(jTable.getSelectedRow(), 1)+" (KEGG code: "+(String) this.pathwaysMetaContainer.getAllpathwaydata().get(selectedRowID)[0] + ")" );
	}



	private void recalculate() {
		boolean cov_scr = false, genus_prop = false;

		if(Double.parseDouble(this.textpvalue.getText()) > 0.1 || Double.parseDouble(this.textpvalue.getText()) < 0 ){
			Workbench.getInstance().error("The pvalue must be between 0 and 0.1");

		}
		else{
			cov_scr = true;
			this.pathwaysMetaContainer.setpValue(Double.parseDouble(this.textpvalue.getText()));
		}


		if(Double.parseDouble(this.textgenusproportion.getText()) > 1 || Double.parseDouble(this.textgenusproportion.getText()) < 0){
			Workbench.getInstance().error("The value of the present enzymes proportion codified by each genus must be between 0 and 1");
		}
		else{
			genus_prop = true;
			this.pathwaysMetaContainer.setGenusProportion(Double.parseDouble(this.textgenusproportion.getText()));
		}


		if(cov_scr && genus_prop == true){
			this.pathwaysMetaContainer.setPopulationSize(0);
			this.pathwaysMetaContainer.setSampleSize(0);
			this.fillList();
		}

	}

}