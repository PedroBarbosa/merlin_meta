package views.metagenomics;
import java.awt.Dimension;
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
public class TaxonomyMetaView extends javax.swing.JPanel {

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
	private SearchInTable searchInGene;
	private ButtonColumn buttonColumn;
	private JLabel lblNewLabel;
	private JTextField textphylumscor;
	private JLabel lblMinimumGenusScore;
	private JTextField textgenusscore;
	private JLabel lblMinimumNumberOf;
	private TaxonomyMetaContainer taxonomicMetacontainer;
	private JTextField textnumhomolog;

	public TaxonomyMetaView(TaxonomyMetaContainer taxon) {
		super();
		this.taxonomicMetacontainer = taxon;
		this.searchInGene = new SearchInTable();
		this.initGUI();
		this.fillList();
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
				jPanel2.add(jPanelExport, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelExport.setBounds(567, 56, 139, 61);
				jPanelExport.setBorder(BorderFactory.createTitledBorder("Export"));
				{
					jButtonExportTxt = new JButton();
					jPanelExport.add(jButtonExportTxt, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonExportTxt.setText("Text file");
					jButtonExportTxt.setToolTipText("Export taxonomy composition to txt file");
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

									taxonomicMetacontainer.exportFinalTaxonomicComposition(new File(filePath+"/"+taxonomicMetacontainer.getProject().getName()+"_composition.txt"));
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
				jPanelExport.setBounds(567, 56, 139, 61);
				jPanelRecalculate.setBorder(BorderFactory.createTitledBorder("Recalculate"));

				{

					jButtonRecalculate = new JButton();
					jPanelRecalculate.add(jButtonRecalculate, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonRecalculate.setText("Taxonomic composition");
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
				jPanelSetparameters = new JPanel();
				GridBagLayout jPanelInsertEditLayout = new GridBagLayout();
				jPanelInsertEditLayout.columnWeights = new double[] {0.0, 0.1, 1.0, 1.0, 1.0};
				jPanelInsertEditLayout.columnWidths = new int[] {7, 7, 7, 7, 7};
				jPanelInsertEditLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0};
				jPanelInsertEditLayout.rowHeights = new int[] {5, 0, 0, 0};
				jPanelSetparameters.setLayout(jPanelInsertEditLayout);
				jPanel2.add(jPanelSetparameters, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelSetparameters.setBounds(7, 56, 277, 61);
				jPanelSetparameters.setBorder(BorderFactory.createTitledBorder("Set parameters"));
				{
					lblMinimumNumberOf = new JLabel("Minimum number of homologues:");
					lblMinimumNumberOf.setHorizontalAlignment(SwingConstants.LEFT);
					GridBagConstraints gbc_lblMinimumNumberOf = new GridBagConstraints();
					gbc_lblMinimumNumberOf.insets = new Insets(0, 0, 5, 5);
					gbc_lblMinimumNumberOf.gridx = 0;
					gbc_lblMinimumNumberOf.gridy = 1;
					jPanelSetparameters.add(lblMinimumNumberOf, gbc_lblMinimumNumberOf);
				}
				{
					textnumhomolog = new JTextField();
					textnumhomolog.setHorizontalAlignment(SwingConstants.CENTER);
					textnumhomolog.setText("5");
					GridBagConstraints gbc_textnumhomolog = new GridBagConstraints();
					gbc_textnumhomolog.insets = new Insets(0, 0, 5, 5);
					gbc_textnumhomolog.fill = GridBagConstraints.HORIZONTAL;
					gbc_textnumhomolog.gridx = 3;
					gbc_textnumhomolog.gridy = 1;
					jPanelSetparameters.add(textnumhomolog, gbc_textnumhomolog);
					textnumhomolog.setColumns(10);
				}
				{
					lblNewLabel = new JLabel("Minimum phylum score:");
					lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
					GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
					gbc_lblNewLabel.gridy = 2;
					gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
					gbc_lblNewLabel.gridx = 0;
					jPanelSetparameters.add(lblNewLabel, gbc_lblNewLabel);
				}
				{
					{
						textphylumscor = new JTextField();
						textphylumscor.setHorizontalAlignment(SwingConstants.CENTER);
						textphylumscor.setText("0.5");
						GridBagConstraints gbc_textphylumscor = new GridBagConstraints();
						gbc_textphylumscor.insets = new Insets(0, 0, 5, 5);
						gbc_textphylumscor.fill = GridBagConstraints.HORIZONTAL;
						gbc_textphylumscor.gridx = 3;
						gbc_textphylumscor.gridy = 2;
						jPanelSetparameters.add(textphylumscor, gbc_textphylumscor);
						textphylumscor.setColumns(10);
					}
					{
						lblMinimumGenusScore = new JLabel("Minimum genus score:");
						GridBagConstraints gbc_lblMinimumGenusScore = new GridBagConstraints();
						gbc_lblMinimumGenusScore.insets = new Insets(0, 0, 0, 5);
						gbc_lblMinimumGenusScore.gridx = 0;
						gbc_lblMinimumGenusScore.gridy = 3;
						jPanelSetparameters.add(lblMinimumGenusScore, gbc_lblMinimumGenusScore);
					}
					{
						textgenusscore = new JTextField();
						textgenusscore.setHorizontalAlignment(SwingConstants.CENTER);
						textgenusscore.setText("0.3");
						GridBagConstraints gbc_textgenusscore = new GridBagConstraints();
						gbc_textgenusscore.anchor = GridBagConstraints.NORTH;
						gbc_textgenusscore.insets = new Insets(0, 0, 0, 5);
						gbc_textgenusscore.fill = GridBagConstraints.HORIZONTAL;
						gbc_textgenusscore.gridx = 3;
						gbc_textgenusscore.gridy = 3;
						jPanelSetparameters.add(textgenusscore, gbc_textgenusscore);
						textgenusscore.setColumns(10);
					}
				}
			}
			{
				jPanel2.add(this.searchInGene.addPanel(), new GridBagConstraints(0, 1, 7, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
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
			mainTableData = this.taxonomicMetacontainer.getPhylogenyData();
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
			tc.getColumn(0).setResizable(false);
			tc.getColumn(0).setModelIndex(0);
			
			tc.getColumn(2).setMinWidth(100);				//phylum
			tc.getColumn(2).setModelIndex(2);
			
			tc.getColumn(3).setMinWidth(100);				//phylum score
			tc.getColumn(3).setMaxWidth(100);
			tc.getColumn(3).setModelIndex(3);
			
			tc.getColumn(5).setMinWidth(100);				//genus score
			tc.getColumn(5).setMaxWidth(100);
			tc.getColumn(5).setModelIndex(5);
			
			tc.getColumn(6).setMinWidth(150);				//concordance
			tc.getColumn(6).setModelIndex(6);
			
			
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

		DataTable[] q = taxonomicMetacontainer.getRowInfo(selectedRowID);

		new GenericDetailWindow(q, mainTableData.getWindowName(), "Gene: "+jTable.getValueAt(jTable.getSelectedRow(), 1));
	}

	
	private void recalculate() {
		boolean num_homo = false, phylum_scr = false, genus_scr = false;
		if(Integer.parseInt(this.textnumhomolog.getText()) < 1 || Integer.parseInt(this.textnumhomolog.getText()) > 100 ){
			Workbench.getInstance().error("Please select a valid value for minimum number of homologues per gene");
			//throw new IllegalArgumentException("Please select a valid value for minimum number of homologues per gene");
		}
		else{
			num_homo = true;
			this.taxonomicMetacontainer.setMin_numberHomologues(Integer.parseInt(this.textnumhomolog.getText()));
		}


		if(Double.parseDouble(this.textphylumscor.getText()) < 0 || Double.parseDouble(this.textphylumscor.getText()) > 1){
			Workbench.getInstance().error("The value of the phylum score must be between 0 and 1");
		}
		else{
			phylum_scr = true;
			this.taxonomicMetacontainer.setThresholdpPhylum(Double.parseDouble(this.textphylumscor.getText()));
		}


		if(Double.parseDouble(this.textgenusscore.getText()) < 0 || Double.parseDouble(this.textgenusscore.getText()) > 1){
			Workbench.getInstance().error("The value of the genus score must be between 0 and 1");
		}
		else{
			genus_scr = true;
			this.taxonomicMetacontainer.setThresholdGenus(Double.parseDouble(this.textgenusscore.getText()));
		}
		
		if(num_homo && phylum_scr && genus_scr == true) this.fillList();

	}

}