package views.metabolic_regulatory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;

import pt.uminho.sysbio.merlin.utilities.OpenBrowser;
import utilities.ButtonColumn;
import utilities.ComboBoxColumn;
import utilities.CreateImageIcon;
import utilities.LinkOut;
import utilities.StarColumn;
import views.UpdatablePanel;
import views.windows.GenericDetailWindowBlast;
import alignment.blast.WriteGBFile;
import datatypes.GenericDataTable;
import datatypes.metabolic_regulatory.HomologyDataContainer;
import es.uvigo.ei.aibench.workbench.Workbench;
import gui.CustomGUI;
import gui.HomologyIntegrationGui;
import gui.InsertRemoveDataWindow;

/**
 * @author oDias
 *
 */
public class HomologyDataViewer extends UpdatablePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane;
	private JButton jButtonIntegration;
	private JButton jButton2;
	private JTextField jTextField4;
	private JButton jButtonMinHits;
	private JButton jButtonBeta;
	private JTextField jTextField3;
	private JTextField jTextField2;
	private JButton jButton1;
	private JTextField jTextField1;
	private JPanel jPanel3;
	private JTextField jTextFieldTotal, jTextFieldResult, jTextFieldThreshold, searchTextField;
	private JTable jTable;
	private JButton commitButton, jButton1ExportXLS, jButtonGbk, jButtonSetThreshold, jButtonPrevious, jButtonNext;
	private JRadioButton jRadioButtonMETAGENES, jRadioButtonTresh, jRadioButtonSelAll, jRadioButtonManSel;
	private ButtonGroup buttonGroupGbk; //buttonGroup, 
	private JPanel jPanel1, jPanel2, jPanel21,  jPanel23, jPanel24, commitPane;//jPanel22,
	private JLabel jLabel1;
	private JComboBox<String> searchComboBox;
	private GenericDataTable mainTableData;
	private HomologyDataContainer homologyDataContainer;
	private JFileChooser fc;
	private int selectedModelRow, presentRow;
	private List<Integer> rows;
	private ComboBoxColumn productsColumn, enzymesColumn;
	private ButtonColumn buttonColumn;
	private StarColumn buttonStarColumn;
	private Map<Integer, List<String>> getUniprotECnumbersTable;
	private int infoColumnNumber, uniprotStarColumnNumber, locus_tagColumnNumber, geneNameColumnNumber, chromosomeColumnNumber = -1, 
			namesColumnNumber, namesScoreColumnNumber, ecnumbersColumnNumber, ecScoreColumnNumber, selectColumnNumber, notesColumnNumber;
	private ItemListener namesItemListener, enzymesItemListener;
	private MouseAdapter namesMouseAdapter, enzymesMouseAdapter, buttonMouseAdapter, starMouseAdapter;
	private ActionListener buttonActionListener, starActionListener;
	private PopupMenuListener namesPopupMenuListener, enzymesPopupMenuListener;

	/**
	 * @param homologyDataContainer
	 */
	public HomologyDataViewer(HomologyDataContainer homologyDataContainer) {

		super(homologyDataContainer);

		try {

			this.homologyDataContainer = homologyDataContainer;
			
			if(this.homologyDataContainer.getProject().isMetagenomicProject()){
				this.homologyDataContainer.setAlpha(1.0);
				this.homologyDataContainer.setThreshold(0.3);
			}
			
			this.mainTableData = this.homologyDataContainer.getAllGenes();

			this.rows = new ArrayList<Integer>();

			if(this.homologyDataContainer.getProject().isInitialiseHomologyData()) {

				this.homologyDataContainer.getProject().setInitialiseHomologyData(false);
				this.initialiser();
			}

			this.initGUI();

			Rectangle visible = null;

			if(this.selectedModelRow>-1 && jTable.getRowCount()>0 && jTable.getRowCount()> this.selectedModelRow) {

				visible = this.jTable.getCellRect(this.selectedModelRow, -1, true);
			}

			this.fillList(visible);

			if(this.selectedModelRow>-1 && jTable.getRowCount()>this.selectedModelRow) {

				this.jTable.setRowSelectionInterval(this.selectedModelRow, this.selectedModelRow);
				this.jTable.scrollRectToVisible(this.jTable.getCellRect(this.selectedModelRow, -1, true));
			}

		}
		catch(Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void intitialiseTableColumns() {

		int number=0;
		this.infoColumnNumber = number++;
		this.locus_tagColumnNumber = number++;
		this.uniprotStarColumnNumber = number++;
		this.geneNameColumnNumber = number++;
		if(homologyDataContainer.isEukaryote()) {

			this.chromosomeColumnNumber = number++;
		}
		this.namesColumnNumber = number++;
		this.namesScoreColumnNumber = number++;
		this.ecnumbersColumnNumber = number++;
		this.ecScoreColumnNumber = number++;
		this.notesColumnNumber = number++;
		this.selectColumnNumber = number++;
	}

	/**
	 * 
	 */
	private void addListeners() {

		this.addMouseListener();
		this.addTableModelListener();
		this.namesItemListener = this.getComboBoxNamesItemListener();
		this.enzymesItemListener = this.getComboBoxEnzymesItemListener();
		this.namesMouseAdapter = this.getComboBoxNamesMouseListener();
		this.enzymesMouseAdapter = this.getComboBoxEnzymesMouseListener();
		this.namesPopupMenuListener = this.getComboBoxNamesPopupMenuListener();
		this.enzymesPopupMenuListener = this.getComboBoxEnzymesPopupMenuListener();
		this.buttonActionListener = this.getButtonActionListener();
		this.buttonMouseAdapter = this.getButtonMouseAdapter();
		this.starActionListener = this.getStarActionListener();
		this.starMouseAdapter = this.getStarMouseAdapter();
	}

	/**
	 * initiate graphical user interface
	 */
	private void initGUI() {

		try {

			GridBagLayout thisLayout = new GridBagLayout();
			thisLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
			thisLayout.columnWidths = new int[] {7, 7, 7};
			thisLayout.rowWeights = new double[] {0.0, 200.0, 0.0, 0.0, 0.0};
			thisLayout.rowHeights = new int[] {7, 50, 7, 3, 7};
			this.setLayout(thisLayout);
			this.setPreferredSize(new Dimension(875, 585));

			{
				jPanel1 = new JPanel();
				GridBagLayout jPanel1Layout = new GridBagLayout();
				jPanel1Layout.rowWeights = new double[] {0.1};
				jPanel1Layout.rowHeights = new int[] {7};
				jPanel1Layout.columnWeights = new double[] {0.1};
				jPanel1Layout.columnWidths = new int[] {7};
				jPanel1.setLayout(jPanel1Layout);
				this.add(jPanel1, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

				jScrollPane = new JScrollPane();
				jPanel1.add(jScrollPane, new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jScrollPane.setPreferredSize(new java.awt.Dimension(7, 7));
				jScrollPane.setSize(900, 420);
			}
			{				
				jPanel2 = new JPanel();
				GridBagLayout jPanel2Layout = new GridBagLayout();
				jPanel2Layout.rowWeights = new double[] {0.0, 0.0};
				jPanel2Layout.rowHeights = new int[] {3, 3};
				jPanel2Layout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1};
				jPanel2Layout.columnWidths = new int[] {7, 7, 7, 7, 7, 7, 7, 7};
				jPanel2.setLayout(jPanel2Layout);
				this.add(jPanel2, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				{
					{
						{
							jPanel21 = new JPanel();
							jPanel2.add(jPanel21, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
							GridBagLayout jPanel21Layout = new GridBagLayout();
							jPanel21.setBounds(14, 41, 294, 63);
							jPanel21.setBorder(BorderFactory.createTitledBorder("Export"));
							jPanel21Layout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
							jPanel21Layout.rowHeights = new int[] {7, 20, 7, 20, 7};
							jPanel21Layout.columnWeights = new double[] {0.1};
							jPanel21Layout.columnWidths = new int[] {7};
							jPanel21.setLayout(jPanel21Layout);
							{
								jButton1ExportXLS = new JButton();
								//this.add(jButton1ExportXLS, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
								jButton1ExportXLS.setText("xls tabbed file");
								jButton1ExportXLS.setToolTipText("Export to Excel file (xls)");
								jPanel21.add(jButton1ExportXLS, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
								jButton1ExportXLS.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Upload.png")),0.1).resizeImageIcon());
								jButton1ExportXLS.addActionListener(new ActionListener()
								{
									public void actionPerformed(ActionEvent arg0) {

										fc.setDialogTitle("Select directory");
										int returnVal = fc.showOpenDialog(new JTextArea());
										if (returnVal == JFileChooser.APPROVE_OPTION) {

											File file = fc.getSelectedFile();
											String path;
											if(file.isDirectory()) {

												path = file.getAbsolutePath();
											}
											else {

												path = file.getParentFile().getPath();
											}

											exportToXls(exportAllData(),path);
										}
									}	
								});
							}
							{
								jButtonGbk = new JButton();
								//jPanel21.add(jButtonGbk, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
								jPanel21.add(jButtonGbk, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
								jButtonGbk.setText("genbank file");
								jButtonGbk.setToolTipText("Update genbank file");
								jButtonGbk.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Upload.png")),0.1).resizeImageIcon());
								jButtonGbk.addActionListener(new ActionListener(){
									public void actionPerformed(ActionEvent arg0) {
										try {

											saveGenbankFile();
										} 
										catch (IOException e) {

											e.printStackTrace();
										}
									}});
							}
						}
					}
				}
				{
					fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				}
				{
					jPanel23 = new JPanel();
					GridBagLayout jPanel23Layout = new GridBagLayout();
					jPanel2.add(jPanel23, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jPanel23.setBounds(325, 41, 376, 79);
					jPanel23.setBorder(BorderFactory.createTitledBorder("Gene Selection"));
					jPanel23Layout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
					jPanel23Layout.rowHeights = new int[] {7, 7, 7, 7, 7};
					jPanel23Layout.columnWeights = new double[] {0.1, 0.1, 0.1, 0.1};
					jPanel23Layout.columnWidths = new int[] {7, 20, 7, 7};
					jPanel23.setLayout(jPanel23Layout);
					{
						buttonGroupGbk = new ButtonGroup();
						{
							jRadioButtonSelAll = new JRadioButton();
							jPanel23.add(jRadioButtonSelAll, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
							buttonGroupGbk.add(jRadioButtonSelAll);
							jRadioButtonSelAll.setText("Select All");
							jRadioButtonSelAll.setBounds(15, 11, 91, 20);
							jRadioButtonSelAll.addActionListener(new ActionListener() {

								public void actionPerformed(ActionEvent arg0) {

									Rectangle visible = jTable.getVisibleRect();

									for(int i=0; i < mainTableData.getTable().size(); i++) {

										mainTableData.setValueAt(true, i, selectColumnNumber);
									}

									for(int i=0; i<jTable.getRowCount();i++) {

										homologyDataContainer.getSelectedGene().put(Integer.parseInt(homologyDataContainer.getKeys().get(i)), (Boolean)jTable.getValueAt(i, selectColumnNumber));
									}
									fillList(visible);
								}
							});
						}
						{
							jRadioButtonManSel = new JRadioButton();
							jPanel23.add(jRadioButtonManSel, new GridBagConstraints(1, 1, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
							buttonGroupGbk.add(jRadioButtonManSel);
							jRadioButtonManSel.setText("Manual Selection");
							jRadioButtonManSel.setBounds(15, 34, 144, 20);
						}
						{
							jTextFieldThreshold = new JTextField();
							jPanel23.add(jTextFieldThreshold, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
							jTextFieldThreshold.setText(homologyDataContainer.getThreshold().toString());
							jTextFieldThreshold.setToolTipText("Threshold for enzyme selection");
							jTextFieldThreshold.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED), null));
							jTextFieldThreshold.setBounds(164, 57, 36, 20);
							jRadioButtonTresh = new JRadioButton();
							jPanel23.add(jRadioButtonTresh, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
							buttonGroupGbk.add(jRadioButtonTresh);
							jRadioButtonTresh.setText("Threshold");
							jRadioButtonTresh.setBounds(15, 57, 144, 20);
							jRadioButtonTresh.setSelected(true);
							jRadioButtonTresh.addActionListener(new ActionListener() {

								public void actionPerformed(ActionEvent arg0) {

									selectThreshold();
								}});
						}
						{
							jButtonSetThreshold = new JButton();
							jPanel23.add(jButtonSetThreshold, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
							jButtonSetThreshold.setText("Set");
							jButtonSetThreshold.setText("Set Threshold");
							jButtonSetThreshold.setBounds(199, 57, 42, 20);
							jButtonSetThreshold.addActionListener(new ActionListener() {

								public void actionPerformed(ActionEvent arg0) {

									selectThreshold();
								}});
						}
						{
							jRadioButtonMETAGENES = new JRadioButton();
							jPanel23.add(jRadioButtonMETAGENES, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
							buttonGroupGbk.add(jRadioButtonMETAGENES);
							jRadioButtonMETAGENES.setText("Metabolic");
							jRadioButtonMETAGENES.setBounds(17, 22, 124, 18);
							jRadioButtonMETAGENES.addActionListener(new ActionListener() {

								public void actionPerformed(ActionEvent evt) {

									Rectangle visible = jTable.getVisibleRect();

									for(int row = 0; row < jTable.getRowCount(); row++) {

										int key = Integer.parseInt(homologyDataContainer.getKeys().get(row));

										if(enzymesColumn.getValues().containsKey(row) && !enzymesColumn.getValues().get(row).trim().isEmpty()) {

											jTable.setValueAt(true, row, selectColumnNumber);
										}
										else {

											jTable.setValueAt(false, row, selectColumnNumber);
										}
										homologyDataContainer.getSelectedGene().put(key, (Boolean)jTable.getValueAt(row,selectColumnNumber));
									}
									fillList(visible);
								}
							});
						}
					}
				}
				{
					commitPane = new JPanel();
					GridBagLayout commitPaneLayout = new GridBagLayout();
					commitPane.setLayout(commitPaneLayout);
					commitPaneLayout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
					commitPaneLayout.rowHeights = new int[] {7, 7, 7, 20, 7};
					commitPaneLayout.columnWeights = new double[] {0.1};
					commitPaneLayout.columnWidths = new int[] {7};
					jPanel2.add(commitPane, new GridBagConstraints(7, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					commitPane.setToolTipText("Database");
					commitPane.setBorder(BorderFactory.createTitledBorder("Database"));
					{
						commitButton = new JButton();
						commitPane.add(commitButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						commitButton.setText("Commit");
						commitButton.setToolTipText("Commit to metabolic database");
						commitButton.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Commit.png")),0.1).resizeImageIcon());
						commitButton.addActionListener(new ActionListener(){

							public void actionPerformed(ActionEvent arg0) {	

								Rectangle visible = jTable.getVisibleRect();

								if(homologyDataContainer.commitToDatabase()) {

									Workbench.getInstance().info("Data successfully loaded into database!");
									fillList(visible);
								}
								else{

									Workbench.getInstance().warn("An error occurred while performing the operation!");
								}
								//mainTableData = homologyDataContainer.getAllGenes();

							}});
					}
					{
						jButtonIntegration = new JButton();
						commitPane.add(jButtonIntegration, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						jButtonIntegration.setText("Integration");
						jButtonIntegration.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Upload.png")),0.1).resizeImageIcon());
						jButtonIntegration.addActionListener(new ActionListener() {

							public void actionPerformed(ActionEvent arg0) {	

								Rectangle visible = jTable.getVisibleRect();

								if(homologyDataContainer == null || homologyDataContainer.getInitialLocus().size()==0) {

									Workbench.getInstance().error("No homology information on the selected Project!");
									//this.setExistshomologyInstance(false);
								}
								else if(!homologyDataContainer.getProject().isMetabolicDataAvailable()) {

									Workbench.getInstance().error("No metabolic information on the selected Project!");
								}
								else {

									fillList(visible);
									new HomologyIntegrationGui("Integrate homology data to database", homologyDataContainer);
								}
							}
						});
					}
				}
				{
					jPanel24 = new JPanel();
					GridBagLayout jPanel3Layout = new GridBagLayout();
					jPanel2.add(jPanel24, new GridBagConstraints(1, 0, 7, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jPanel24.setBorder(BorderFactory.createTitledBorder("Search"));
					jPanel3Layout.rowWeights = new double[] {0.0};
					jPanel3Layout.rowHeights = new int[] {3};
					jPanel3Layout.columnWeights = new double[] {1.1, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.0, 0.1};
					jPanel3Layout.columnWidths = new int[] {100, 20, 7, 7, 3, 3, 7, 6, 3, 6};
					jPanel24.setLayout(jPanel3Layout);
					{
						jButtonPrevious = new JButton();
						jButtonPrevious .setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Previous.png")),0.05).resizeImageIcon());
						jPanel24.add(jButtonPrevious, new GridBagConstraints(4, -1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						jButtonPrevious.setToolTipText("Previous");
						jButtonPrevious.addActionListener(new ActionListener(){

							public void actionPerformed(ActionEvent arg0) {

								if(rows.size()>0) {

									if(presentRow!=0) {

										presentRow-=1;
									}
									else {

										presentRow=rows.size()-1;
									}
									jTextFieldResult.setText(""+(presentRow+1));
									jTable.setRowSelectionInterval(rows.get(presentRow), rows.get(presentRow));
									jTable.scrollRectToVisible(jTable.getCellRect(rows.get(presentRow), 0, true));
									selectedModelRow = rows.get(presentRow);
									homologyDataContainer.setSelectedRow(selectedModelRow);
								}
							}});
					}
					{
						jButtonNext = new JButton();
						jButtonNext .setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Next.png")),0.05).resizeImageIcon());
						jPanel24.add(jButtonNext, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						jButtonNext.setToolTipText("Next");
						jButtonNext.addActionListener(new ActionListener() {

							public void actionPerformed(ActionEvent arg0) {

								if(rows.size()>0) {

									if(presentRow!=rows.size()-1) {

										presentRow+=1;
									}
									else {

										Workbench.getInstance().info("The end was reached!\n Starting from the top.");
										presentRow=0;
									}
									jTextFieldResult.setText(""+(presentRow+1));
									jTable.setRowSelectionInterval(rows.get(presentRow), rows.get(presentRow));
									jTable.scrollRectToVisible(jTable.getCellRect(rows.get(presentRow), 0, true));
									selectedModelRow = rows.get(presentRow);
									homologyDataContainer.setSelectedRow(selectedModelRow);
								}
							}});
					}
					{
						jTextFieldResult = new JTextField();
						jTextFieldResult.setEditable(false);
						jPanel24.add(jTextFieldResult, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					}
					{
						jLabel1 = new JLabel();
						jPanel24.add(jLabel1, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						jLabel1.setText("of");
						jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
						jLabel1.setHorizontalTextPosition(SwingConstants.CENTER);
					}
					{
						jTextFieldTotal = new JTextField();
						jTextFieldTotal.setEditable(false);
						jPanel24.add(jTextFieldTotal, new GridBagConstraints(9, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					}
					{

						searchTextField = new JTextField();
						searchTextField.setBounds(14, 12, 604, 20);
						searchTextField.addKeyListener(new KeyAdapter() {

							@Override
							public void keyTyped(KeyEvent evt) {

								searchInTable(evt);
							}
						});

						ComboBoxModel<String> searchComboBoxModel = new DefaultComboBoxModel<> (new String[] { "Name", "All" });
						searchComboBox = new JComboBox<>();
						jPanel24.add(searchComboBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						jPanel24.add(searchTextField, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						//				searchComboBox.setBounds(624, 12, 77, 20);

						searchComboBox.setModel(searchComboBoxModel);
						searchComboBox.addActionListener(new ActionListener()
						{
							public void actionPerformed(ActionEvent arg0){
								if(searchTextField.getText().compareTo("")!=0)
								{
									searchInTable(searchTextField.getText());
								}
							}
						});
					}
				}
				{
					jPanel3 = new JPanel();
					jPanel2.add(jPanel3, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					GridBagLayout jPanel3Layout1 = new GridBagLayout();
					jPanel3Layout1.columnWidths = new int[] {7, 7, 7, 7, 7, 7, 7};
					jPanel3Layout1.rowHeights = new int[] {7,20, 7, 20, 7};
					jPanel3Layout1.columnWeights = new double[] {0.0, 0.1, 0.1, 0.0, 0.1, 0.1, 0.0};
					jPanel3Layout1.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
					jPanel3.setBorder(BorderFactory.createTitledBorder("merlin scorer"));
					jPanel3.setLayout(jPanel3Layout1);
					jPanel3.setBounds(325, 41, 376, 79);
					{
						jButton1 = new JButton();
						jButton1.setText("blast/hmmer");
						jPanel3.add(jButton1, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						jButton1.setToolTipText("Relative weigth for BLAST and HMMER");
						jTextField1 = new JTextField();
						jPanel3.add(jTextField1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						jTextField1.setToolTipText("Relative weigth for BLAST and HMMER");
						jTextField1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED),null));
						jTextField1.setBounds(164, 57, 36, 20);
						if(homologyDataContainer.isBlastPAvailable() && homologyDataContainer.isHmmerAvailable()) {

							jTextField1.setText(""+homologyDataContainer.getBlastHmmerWeight());
							jButton1.addActionListener(new ActionListener() {

								public void actionPerformed(ActionEvent arg0) {

									Rectangle visible = jTable.getVisibleRect();

									if(Double.parseDouble(jTextField1.getText())<0 || Double.parseDouble(jTextField1.getText())>1) {
										jTextField1.setText(homologyDataContainer.getBlastHmmerWeight().toString());
										Workbench.getInstance().warn("The value must be between 0 and 1");
									}
									else {

										if(discardData()){initialiser();}
										homologyDataContainer.setBlastHmmerWeight(Double.parseDouble(jTextField1.getText()));
										mainTableData = homologyDataContainer.getAllGenes();
										jTable.setModel(mainTableData);
										fillList(visible);
									}
								}
							});
						}
						else {

							jTextField1.setEnabled(false);
							jButton1.setEnabled(false);
						}
					}
					{
						jTextField2 = new JTextField();
						jPanel3.add(jTextField2, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						jTextField2.setText(""+this.homologyDataContainer.getBeta());
						jTextField2.setToolTipText("Enter the beta value for enzyme selection");
						jTextField2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED),null));
						jTextField2.setBounds(164, 57, 36, 20);
						jButtonBeta = new JButton();
						jPanel3.add(jButtonBeta, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						jButtonBeta.setText("beta value");
						jButtonBeta.setToolTipText("Enter the beta value for enzyme selection");
						jButtonBeta.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {

								Rectangle visible = jTable.getVisibleRect();

								if(Double.parseDouble(jTextField2.getText())<0 || Double.parseDouble(jTextField2.getText())>1) {

									jTextField2.setText(homologyDataContainer.getBeta().toString());
									Workbench.getInstance().warn("The value must be between 0 and 1");
								}
								else {
									if( Double.parseDouble(jTextField2.getText())> new Double(1.0/new Double(homologyDataContainer.getMinimumNumberofHits()))) {
										jTextField2.setText(homologyDataContainer.getBeta()+"");
										Workbench.getInstance().warn("The maximum beta value for "+homologyDataContainer.getMinimumNumberofHits()+" minimum number of hits cannot be higher than "+new Double(1.0/new Double(homologyDataContainer.getMinimumNumberofHits()))+".");
									}
									else {
										if(discardData()){initialiser();}
										homologyDataContainer.setBeta(Double.parseDouble(jTextField2.getText()));
										mainTableData = homologyDataContainer.getAllGenes();
										jTable.setModel(mainTableData);
										fillList(visible);
									}
								}
							}
						});

					}
					{
						jTextField3 = new JTextField();
						jPanel3.add(jTextField3, new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						jTextField3.setText(""+this.homologyDataContainer.getMinimumNumberofHits());
						jTextField3.setToolTipText("Enter the minimum number of required hits for enzyme selection");
						jTextField3.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED),null));
						jTextField3.setBounds(164, 57, 36, 20);
						jButtonMinHits = new JButton();
						jPanel3.add(jButtonMinHits, new GridBagConstraints(5, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						jButtonMinHits.setText("Min Homologies");
						jButtonMinHits.setToolTipText("Enter the minimum number of required Homologies for enzyme selection");
						jButtonMinHits.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {

								Rectangle visible = jTable.getVisibleRect();

								if(Double.parseDouble(jTextField3.getText())<0 || Double.parseDouble(jTextField3.getText())%1!=0) {
									jTextField3.setText(homologyDataContainer.getMinimumNumberofHits()+"");
									Workbench.getInstance().warn("The value has to be a positive Integer");
								}
								else {
									if( Double.parseDouble(jTextField3.getText())> (1.0/new Double(homologyDataContainer.getBeta()))) {
										jTextField3.setText(homologyDataContainer.getMinimumNumberofHits()+"");
										Workbench.getInstance().warn("The minimum number of hits for a beta value of "+homologyDataContainer.getBeta()+" cannot be higher than "+(1.0/new Double(homologyDataContainer.getBeta()))+".");
									}
									else {

										if(discardData()) {

											initialiser();
										}

										homologyDataContainer.setMinimumNumberofHits(Integer.parseInt(jTextField3.getText()));
										mainTableData = homologyDataContainer.getAllGenes();
										jTable.setModel(mainTableData);
										fillList(visible);
									}
								}
							}
						});
					}
					{
						jTextField4 = new JTextField();
						jPanel3.add(jTextField4, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						jTextField4.setText(""+this.homologyDataContainer.getAlpha());
						jTextField4.setToolTipText("Enter the alpha value for enzyme selection");
						jTextField4.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED),null));
						jTextField4.setBounds(164, 57, 36, 20);
						jButton2 = new JButton();
						jPanel3.add(jButton2, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						jButton2.setText("alpha value");
						jButton2.setToolTipText("Enter the alpha value for enzyme selection");
						jButton2.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {

								Rectangle visible = jTable.getVisibleRect();

								if(Double.parseDouble(jTextField4.getText())<0 || Double.parseDouble(jTextField4.getText())>1) {

									jTextField4.setText(homologyDataContainer.getAlpha().toString());
									Workbench.getInstance().warn("The value must be between 0 and 1");
								}
								else {

									if(discardData()) {

										initialiser();
									}

									homologyDataContainer.setAlpha(Double.parseDouble(jTextField4.getText()));
									mainTableData = homologyDataContainer.getAllGenes();
									jTable.setModel(mainTableData);
									fillList(visible);
								}
							}
						});
					}
				}
			}
			selectedModelRow=this.homologyDataContainer.getSelectedRow();

			jTable = new JTable();
			jTable.setModel(mainTableData);
			jScrollPane.setViewportView(jTable);
		}
		catch(Exception e){

			e.printStackTrace();
		}
	}

	/**
	 *	fill entities lists 
	 */
	public void fillList(Rectangle visible) {

		try {

			this.intitialiseTableColumns();
			this.selectedModelRow = this.homologyDataContainer.getSelectedRow();

			int myRow = this.selectedModelRow;

			jTable= new JTable();
			jTable.setModel(mainTableData);
			List<Map<Integer, String>> itemsList = this.updateData();

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			this.addListeners();

			{
				this.buttonColumn = this.buildButtonColumn(this.infoColumnNumber);
				
				//if(this.getUniprotECnumbersTable == null) 
				{

					this.getUniprotECnumbersTable = this.getUniprotECnumbersTable(itemsList.get(2));
				}
				this.buttonStarColumn = this.buildStarColumn(this.uniprotStarColumnNumber, this.compareAnnotations(itemsList.get(1)));
				this.productsColumn = this.buildComboBoxColumn(this.namesColumnNumber, itemsList.get(0));
				this.enzymesColumn = this.buildComboBoxColumn(this.ecnumbersColumnNumber, itemsList.get(1));
			}

			this.jScrollPane.setViewportView(jTable);

			if(visible!=null) {

				//this.selectedRow = this.homologyDataContainer.getSelectedRow();

				this.jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

				if(myRow>-1 && jTable.getRowCount()>myRow) {

					this.jTable.setRowSelectionInterval(myRow, myRow);
				}

				scrollToVisible(visible);
			}

			//System.out.println("List filled");

			this.setTableColumunModels();
			searchTextField.setText("");
		}
		catch (Exception e) {

			e.printStackTrace();
		}

	}

	/**
	 * @param evt
	 */
	public void searchInTable(KeyEvent evt) {

		String text;

		if(searchTextField.getText().compareTo("")!=0 && evt.getKeyChar() == KeyEvent.VK_BACK_SPACE) {

			text = searchTextField.getText();
		}
		else {

			text = searchTextField.getText()+evt.getKeyChar();
		}

		searchInTable(text);

	}

	/**
	 * @param text
	 */
	private void searchInTable(String text) {

		this.rows = new ArrayList<Integer>();
		Set<Integer> rows = new TreeSet<Integer>();
		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
		int i=0;
		this.presentRow = 0;
		ArrayList<Object[]> tab = this.mainTableData.getTable();

		switch(this.searchComboBox.getSelectedIndex())
		{
		case 0:
		{
			for(int z=0;z<tab.size();z++) {

				Object[] subtab = tab.get(i);
				if(((String)subtab[locus_tagColumnNumber]) != null &&  ((String)subtab[locus_tagColumnNumber]).contains(text)) {

					int modelRow = new Integer(z);
					rows.add(modelRow);
				}

				if(((String)subtab[geneNameColumnNumber]) != null && ((String)subtab[geneNameColumnNumber]).contains(text)) {

					int modelRow = new Integer(i);
					rows.add(modelRow);
				}

				i++;
			}
			break;
		}
		case 1:
		{
			for(int z=0;z<tab.size();z++) {

				Object[] subtab = tab.get(i);

				List<String> product = new ArrayList<String>();
				List<String> ecnumber = new ArrayList<String>();

				if(((String[])subtab[namesColumnNumber]) != null && ((String[])subtab[namesColumnNumber]).length>0) {

					product.addAll(Arrays.asList(((String[])subtab[namesColumnNumber])));
				}

				for(String s: product) {

					if(s.contains(text)) {

						int modelRow = new Integer(i);
						rows.add(modelRow);
					}
				}

				if(((String[])subtab[ecnumbersColumnNumber]) != null && ((String[])subtab[ecnumbersColumnNumber]).length>0) {

					ecnumber.addAll(Arrays.asList(((String[])subtab[ecnumbersColumnNumber])));
				}

				for(String s: ecnumber) {

					if(s.contains(text)) {

						int modelRow = new Integer(i);
						rows.add(modelRow);
					}
				}

				if(((String)subtab[locus_tagColumnNumber]) != null && ((String)subtab[locus_tagColumnNumber]).contains(text)) {

					int modelRow = new Integer(i);
					rows.add(modelRow);
				}

				if(((String)subtab[geneNameColumnNumber]) != null && ((String)subtab[geneNameColumnNumber]).contains(text)) {

					int modelRow = new Integer(i);
					rows.add(modelRow);
				}

				if(((String)subtab[notesColumnNumber]) != null && ((String)subtab[notesColumnNumber]).contains(text)) {

					int modelRow = new Integer(i);
					rows.add(modelRow);
				}

				if(homologyDataContainer.isEukaryote() && ((String)subtab[chromosomeColumnNumber]) != null && ((String)subtab[chromosomeColumnNumber]).contains(text)) {

					int modelRow = new Integer(i);
					rows.add(modelRow);
				}

				i++;
			}
			break;
		}
		default:
		{
			for(int z=0;z<tab.size();z++) {

				Object[] subtab = tab.get(i);
				if(((String)subtab[locus_tagColumnNumber]) != null && ((String) subtab[locus_tagColumnNumber]).contains(text)) {
					rows.add(new Integer(i));
				}

				if(((String)subtab[geneNameColumnNumber]) != null && ((String) subtab[geneNameColumnNumber]).contains(text)) {
					rows.add(new Integer(i));
				}
				i++;
			}
			break;
		}

		}
		this.rows.addAll(rows);

		int row = 0;
		for(Integer r: this.rows) {

			row = r.intValue();
			selectionModel.addSelectionInterval(row, row);
		}


		this.jTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.jTable.setSelectionModel(selectionModel);
		if(selectionModel.isSelectionEmpty()&& (this.searchTextField.getText().compareTo("")!=0)) {

			this.searchTextField.setForeground(new java.awt.Color(255,0,0));
			searchTextField.setBackground(new java.awt.Color(174,174,174));
			this.jTextFieldResult.setText("");
			this.jTextFieldTotal.setText("");
			this.rows = new ArrayList<Integer>();
		}
		else {

			this.searchTextField.setForeground(Color.BLACK);
			this.searchTextField.setBackground(Color.WHITE);
		}

		if(this.rows.size()!=0) {

			jTextFieldResult.setText(""+1);
			jTextFieldTotal.setText(""+this.rows.size());
			this.scrollToVisible(this.jTable.getCellRect(this.rows.get(0), 0, true));
		}
	}

	/**
	 * update data lists 
	 * @return 
	 */
	private List<Map<Integer,String>> updateData() {

		try {

			if(this.homologyDataContainer.hasCommittedData()) {

				this.homologyDataContainer.getCommittedData();
				this.jRadioButtonManSel.setSelected(true);
			}

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			if(this.homologyDataContainer.getCommittedProductList()!= null) {

				for(int row : this.homologyDataContainer.getCommittedProductList().keySet()) {

					this.jTable.setValueAt(homologyDataContainer.getCommittedProductList().get(row), row, this.namesColumnNumber);
				}
			}

			for(int key : this.homologyDataContainer.getEditedProductData().keySet()) {

				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {

					int row = homologyDataContainer.getReverseKeys().get(key);
					this.jTable.setValueAt(homologyDataContainer.getEditedProductData().get(key), row, this.namesColumnNumber);
				}
			}

			Map<Integer, String> mappedProdItem = this.homologyDataContainer.getInitialProdItem();

			if(this.homologyDataContainer.getCommittedProdItem()!= null) {

				for(int row : this.homologyDataContainer.getCommittedProdItem().keySet()) {

					if(this.homologyDataContainer.getCommittedProdItem().get(row)!=null && !this.homologyDataContainer.getCommittedProdItem().get(row).equalsIgnoreCase("null")) {

						mappedProdItem.put(row, this.homologyDataContainer.getCommittedProdItem().get(row));

						if(!homologyDataContainer.getProductList().containsKey(Integer.parseInt(this.homologyDataContainer.getKeys().get(row)))) {

							this.homologyDataContainer.getProductList().put(Integer.parseInt(this.homologyDataContainer.getKeys().get(row)),mappedProdItem.get(row));
						}

					}
				}
			}

			for(int key : this.homologyDataContainer.getProductList().keySet()) {

				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {

					int row = homologyDataContainer.getReverseKeys().get(key);

					mappedProdItem.put(row,this.homologyDataContainer.getProductList().get(key));

					String pdWeigth = this.homologyDataContainer.getProductPercentage(mappedProdItem.get(row), row);

					this.jTable.setValueAt(pdWeigth, row, this.namesScoreColumnNumber);

					this.homologyDataContainer.getProductList().put(key,mappedProdItem.get(row));
				}
			}

			//			for(int row :mappedProdItem.keySet()) {
			//				
			//				if(mappedProdItem.get(row) != null) {
			//					
			//					this.homologyDataContainer.getProductList().put(Integer.parseInt(this.homologyDataContainer.getKeys().get(row)),mappedProdItem.get(row));
			//				}
			//			}

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			if(this.homologyDataContainer.getCommittedEnzymeList()!= null) {

				for(int row : this.homologyDataContainer.getCommittedEnzymeList().keySet()) {

					this.jTable.setValueAt(this.homologyDataContainer.getCommittedEnzymeList().get(row), row, this.ecnumbersColumnNumber);
				}
			}

			for(int key : this.homologyDataContainer.getEditedEnzymeData().keySet()) {

				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {

					int row = homologyDataContainer.getReverseKeys().get(key);
					this.jTable.setValueAt(this.homologyDataContainer.getEditedEnzymeData().get(key), row, this.ecnumbersColumnNumber);

				}
			}

			Map<Integer, String> mappedEcItem = this.homologyDataContainer.getInitialEcItem();

			if(this.homologyDataContainer.getCommittedEcItem()!= null) {

				for(int row : this.homologyDataContainer.getCommittedEcItem().keySet()) {

					if(this.homologyDataContainer.getCommittedEcItem().get(row)!=null && !this.homologyDataContainer.getCommittedEcItem().get(row).equalsIgnoreCase("null")) {

						mappedEcItem.put(row, this.homologyDataContainer.getCommittedEcItem().get(row));

						if(!homologyDataContainer.getEnzymesList().containsKey(Integer.parseInt(this.homologyDataContainer.getKeys().get(row)))) {

							this.homologyDataContainer.getEnzymesList().put(Integer.parseInt(this.homologyDataContainer.getKeys().get(row)),mappedEcItem.get(row));
						}
					}
				}
			}

			for(int key : this.homologyDataContainer.getEnzymesList().keySet()) {

				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {

					int row = homologyDataContainer.getReverseKeys().get(key);

					mappedEcItem.put(row,this.homologyDataContainer.getEnzymesList().get(key));

					String ecWeigth = this.homologyDataContainer.getECPercentage(mappedEcItem.get(row), row);

					if(!ecWeigth.equalsIgnoreCase("manual") && Double.parseDouble(ecWeigth) < this.homologyDataContainer.getThreshold()) {

						//ecWeigth = "<"+this.homologyDataContainer.getThreshold();
						//this.homologyDataContainer.getSelectedGene().put(key, false);
					}
					else {

						//this.homologyDataContainer.getSelectedGene().put(key, true);
					}
					this.jTable.setValueAt(ecWeigth, row, this.ecScoreColumnNumber);

					this.homologyDataContainer.getEnzymesList().put(key,mappedEcItem.get(row));
				}
			}

			//			for(int row :mappedEcItem.keySet()) {
			//				
			//				if(mappedEcItem.get(row) != null) {
			//					
			//					this.homologyDataContainer.getEnzymesList().put(Integer.parseInt(this.homologyDataContainer.getKeys().get(row)),mappedEcItem.get(row));
			//				}
			//			}


			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			Map<Integer, String> mappedLocusList = this.homologyDataContainer.getInitialLocus();

			if(this.homologyDataContainer.getCommittedLocusList() != null && jTable.getRowCount()>0) {

				for(int row : this.homologyDataContainer.getCommittedLocusList().keySet()) {

					if(this.homologyDataContainer.getCommittedLocusList().get(row)!=null && !this.homologyDataContainer.getCommittedLocusList().get(row).equalsIgnoreCase("null")) {

						mappedLocusList.put(row, this.homologyDataContainer.getCommittedLocusList().get(row));
						this.jTable.setValueAt(mappedLocusList.get(row), row, this.locus_tagColumnNumber);
					}
				}
			}

			for(int key : this.homologyDataContainer.getLocusList().keySet()) {

				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {

					int row = homologyDataContainer.getReverseKeys().get(key);
					mappedLocusList.put(row,this.homologyDataContainer.getLocusList().get(key));
					this.jTable.setValueAt(mappedLocusList.get(row), row, this.locus_tagColumnNumber);
				}
			}

			Map<Integer, String> mappedNamesList = this.homologyDataContainer.getInitialNames();

			if(this.homologyDataContainer.getCommittedNamesList() != null && jTable.getRowCount()>0) {

				for(int row : this.homologyDataContainer.getCommittedNamesList().keySet()) {

					if(this.homologyDataContainer.getCommittedNamesList().get(row)!=null && !this.homologyDataContainer.getCommittedNamesList().get(row).equalsIgnoreCase("null")) {

						mappedNamesList.put(row, this.homologyDataContainer.getCommittedNamesList().get(row));
						this.jTable.setValueAt(mappedNamesList.get(row), row, this.geneNameColumnNumber);
					}
				}
			}

			for(int key : this.homologyDataContainer.getNamesList().keySet()) {

				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {

					int row = homologyDataContainer.getReverseKeys().get(key);

					mappedNamesList.put(row,this.homologyDataContainer.getNamesList().get(key));

					this.jTable.setValueAt(mappedNamesList.get(row), row, this.geneNameColumnNumber);
				}
			}

			Map<Integer, String> mappedNotesMap = new TreeMap<Integer, String>();

			if(this.homologyDataContainer.getCommittedNotesMap() != null && jTable.getRowCount()>0) {

				for(int row : this.homologyDataContainer.getCommittedNotesMap().keySet()) {

					if(this.homologyDataContainer.getCommittedNotesMap().get(row) != null) {

						mappedNotesMap.put(row, this.homologyDataContainer.getCommittedNotesMap().get(row));
						this.jTable.setValueAt(mappedNotesMap.get(row), row, this.notesColumnNumber);
					}
				}
			}

			for(int key : this.homologyDataContainer.getNotesMap().keySet()) {

				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {
					int row = homologyDataContainer.getReverseKeys().get(key);

					mappedNotesMap.put(row,this.homologyDataContainer.getNotesMap().get(key));
					this.jTable.setValueAt(mappedNotesMap.get(row), row, this.notesColumnNumber);
				}
			}

			Map<Integer, String> mappedChromosome = null ;
			if(homologyDataContainer.isEukaryote()) {

				mappedChromosome = this.homologyDataContainer.getInitialChromosome();

				if(this.homologyDataContainer.getCommittedChromosome() != null) {

					for(int row : this.homologyDataContainer.getCommittedChromosome().keySet()) {

						if(this.homologyDataContainer.getCommittedChromosome().get(row) != null) {

							mappedChromosome.put(row, this.homologyDataContainer.getCommittedChromosome().get(row));
							this.jTable.setValueAt(mappedChromosome.get(row), row, this.chromosomeColumnNumber);
						}
					}
				}

				for(int key : this.homologyDataContainer.getChromosome().keySet()) {

					if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {

						int row = homologyDataContainer.getReverseKeys().get(key);
						mappedChromosome.put(row,this.homologyDataContainer.getChromosome().get(key));
						this.jTable.setValueAt(mappedChromosome.get(row), row, this.chromosomeColumnNumber);
					}
				}
			}

			Map<Integer, Boolean> mappedSelectedGene = this.homologyDataContainer.getInitialSelectedGene();
			if(this.homologyDataContainer.getCommittedSelected() != null && jTable.getRowCount()>0) {

				for(int row : this.homologyDataContainer.getCommittedSelected().keySet()) {

					if(this.homologyDataContainer.getCommittedSelected().get(row) != null) {

						mappedSelectedGene.put(row,this.homologyDataContainer.getCommittedSelected().get(row));
						this.jTable.setValueAt(mappedSelectedGene.get(row), row, this.selectColumnNumber);
					}
				}
			}

			for(int key : this.homologyDataContainer.getSelectedGene().keySet()) {

				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {

					int row = homologyDataContainer.getReverseKeys().get(key);
					mappedSelectedGene.put(row,this.homologyDataContainer.getSelectedGene().get(key));
					this.jTable.setValueAt(mappedSelectedGene.get(row), row, this.selectColumnNumber);
				}
			}

			// just to be sure that all selected or unselected genes are mapped
			for(int row=0; row < jTable.getRowCount(); row++) {

				int key =Integer.parseInt(homologyDataContainer.getKeys().get(row)) ;
				mappedSelectedGene.put(row,this.homologyDataContainer.getSelectedGene().get(key));
				this.homologyDataContainer.getSelectedGene().put(key, (Boolean)this.jTable.getValueAt(row,this.selectColumnNumber));
			}

			// prepate items for integration
			{
				this.homologyDataContainer.setIntegrationLocusList(mappedLocusList);
				this.homologyDataContainer.setIntegrationNamesList(mappedNamesList);
				this.homologyDataContainer.setIntegrationProdItem(mappedProdItem);
				this.homologyDataContainer.setIntegrationEcItem(mappedEcItem);
				this.homologyDataContainer.setIntegrationSelectedGene(mappedSelectedGene);
				if(homologyDataContainer.isEukaryote()) {

					this.homologyDataContainer.setIntegrationChromosome(mappedChromosome);
				}
			}

			List<Map<Integer, String>> object = new ArrayList<Map<Integer,String>>();
			object.add(0,mappedProdItem);
			object.add(1,mappedEcItem);
			object.add(2,mappedLocusList);

			return object;
		}
		catch (Exception e) {

			this.jTable.setModel(this.mainTableData);
			this.selectedModelRow=-1;
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 */
	private void addMouseListener() {

		jTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {

				selectedModelRow=jTable.getSelectedRow();
				homologyDataContainer.setSelectedRow(selectedModelRow);

				if(jTable.getSelectedRow()>-1 && jTable.getRowCount()>0 && jTable.getRowCount()> jTable.getSelectedRow()) {

					jTable.setRowSelectionInterval(jTable.getSelectedRow(), jTable.getSelectedRow());
					scrollToVisible(jTable.getCellRect(jTable.getSelectedRow(), -1, true));
				}

				jTextFieldResult.setText(""); jTextFieldTotal.setText(""); searchTextField.setText(""); rows = new ArrayList<Integer>();

				int selectedColumn=-1;
				{
					Point p = arg0.getPoint();
					int  columnNumber = jTable.columnAtPoint(p);
					jTable.setColumnSelectionInterval(columnNumber, columnNumber);
					selectedColumn=columnNumber;
				}

				//EC number popup
				if(selectedColumn==(new Integer(ecScoreColumnNumber))) {

					selectedModelRow=jTable.getSelectedRow();
					homologyDataContainer.setSelectedRow(selectedModelRow);

					new InsertRemoveDataWindow(homologyDataContainer, selectedModelRow ,"Scores description", true) {

						private static final long serialVersionUID = -7075058626735407587L;
						public void finishClose() {

							Rectangle visible = jTable.getVisibleRect();

							this.setVisible(false);
							this.dispose();
							homologyDataContainer.getSelectedGene().put(Integer.parseInt(homologyDataContainer.getKeys().get(selectedModelRow)), true);
							fillList(visible);
						}						
					};
				}

				// products popup
				if(selectedColumn==namesScoreColumnNumber) {

					selectedModelRow=jTable.getSelectedRow();
					homologyDataContainer.setSelectedRow(selectedModelRow);

					new InsertRemoveDataWindow(homologyDataContainer, selectedModelRow, "Scores description", false) {

						private static final long serialVersionUID = -100365966778626951L;

						public void finishClose() {

							Rectangle visible = jTable.getVisibleRect();

							this.setVisible(false);
							this.dispose();
							homologyDataContainer.getSelectedGene().put(Integer.parseInt(homologyDataContainer.getKeys().get(selectedModelRow)), true);
							fillList(visible);
						}						
					};
				}

				//genes Linkout
				if(selectedColumn==locus_tagColumnNumber || selectedColumn==geneNameColumnNumber) {

					if(arg0.getButton()==MouseEvent.BUTTON3 && jTable.getSelectedRow()>0) {

						List<Integer> dbs = new ArrayList<Integer>();
						dbs.add(0);
						dbs.add(1);
						new LinkOut(dbs, (String)jTable.getValueAt(jTable.getSelectedRow(), selectedColumn)).show(arg0.getComponent(),arg0.getX(), arg0.getY());
					}
				}

				//proteins linkout
				if(selectedColumn==namesColumnNumber || selectedColumn==ecnumbersColumnNumber) {

					if(arg0.getButton()==MouseEvent.BUTTON3 && jTable.getSelectedRow()>0) {

						List<Integer> dbs = new ArrayList<Integer>();

						String text=null;
						if(selectedColumn==namesColumnNumber) {

							dbs.add(1);
							dbs.add(2);
							text=productsColumn.getSelectItem(selectedModelRow);
						}

						if(selectedColumn==ecnumbersColumnNumber) {
							dbs.add(1);
							dbs.add(3);
							text=enzymesColumn.getSelectItem(jTable.getSelectedRow());
						}

						if(text!=null) {

							new LinkOut(dbs, text).show(arg0.getComponent(),arg0.getX(), arg0.getY());
						}
					}
				}
			}
		});
	}

	/**
	 * 
	 */
	private void addTableModelListener() {

		jTable.getModel().addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {

				if(jTable.getSelectedRow()>-1) {

					selectedModelRow=jTable.getSelectedRow();
					homologyDataContainer.setSelectedRow(selectedModelRow);
					int key = Integer.parseInt(homologyDataContainer.getKeys().get(jTable.getSelectedRow()));
					homologyDataContainer.getLocusList().put(key , (String) jTable.getValueAt(jTable.getSelectedRow(), locus_tagColumnNumber));
					homologyDataContainer.getNamesList().put(key , (String) jTable.getValueAt(jTable.getSelectedRow() , geneNameColumnNumber));
					homologyDataContainer.getNotesMap().put(key , (String) jTable.getValueAt(jTable.getSelectedRow(), notesColumnNumber));
					if(homologyDataContainer.isEukaryote()) {

						homologyDataContainer.getChromosome().put(key , (String) jTable.getValueAt(jTable.getSelectedRow(), chromosomeColumnNumber));
					}

					if(e.getFirstRow()!= e.getLastRow()) {

						for(int i=0;i<jTable.getRowCount();i++) {

							homologyDataContainer.getSelectedGene().put(Integer.parseInt(homologyDataContainer.getKeys().get(i)), (Boolean) jTable.getValueAt(i, selectColumnNumber));
						}
					}
					else {

						homologyDataContainer.getSelectedGene().put(Integer.parseInt(homologyDataContainer.getKeys().get(e.getFirstRow())), (Boolean) jTable.getValueAt(e.getFirstRow(), selectColumnNumber));
					}
				}
			}});
	}

	/**
	 * 
	 */
	private void setTableColumunModels() {

		TableColumnModel tc = jTable.getColumnModel();
		tc.getColumn(infoColumnNumber).setMaxWidth(35);				
		tc.getColumn(infoColumnNumber).setResizable(false);
		tc.getColumn(infoColumnNumber).setModelIndex(infoColumnNumber);

		tc.getColumn(uniprotStarColumnNumber).setMaxWidth(50);				
		tc.getColumn(uniprotStarColumnNumber).setResizable(false);
		tc.getColumn(uniprotStarColumnNumber).setModelIndex(uniprotStarColumnNumber);

		tc.getColumn(locus_tagColumnNumber).setMinWidth(120);
		tc.getColumn(locus_tagColumnNumber).setModelIndex(locus_tagColumnNumber);

		tc.getColumn(geneNameColumnNumber).setMinWidth(100);
		tc.getColumn(geneNameColumnNumber).setModelIndex(geneNameColumnNumber);

		if(this.homologyDataContainer.isEukaryote()) {

			tc.getColumn(chromosomeColumnNumber).setMinWidth(100);
			tc.getColumn(chromosomeColumnNumber).setModelIndex(chromosomeColumnNumber);
		}

		tc.getColumn(namesColumnNumber).setMinWidth(210);
		tc.getColumn(namesColumnNumber).setModelIndex(namesColumnNumber);

		tc.getColumn(namesScoreColumnNumber).setMinWidth(90);
		tc.getColumn(namesScoreColumnNumber).setMaxWidth(120);
		tc.getColumn(namesScoreColumnNumber).setModelIndex(namesScoreColumnNumber);

		tc.getColumn(ecnumbersColumnNumber).setMinWidth(135);
		tc.getColumn(ecnumbersColumnNumber).setModelIndex(ecnumbersColumnNumber);

		tc.getColumn(ecScoreColumnNumber).setMinWidth(90);
		tc.getColumn(ecScoreColumnNumber).setMaxWidth(120);
		tc.getColumn(ecScoreColumnNumber).setModelIndex(ecScoreColumnNumber);

		tc.getColumn(notesColumnNumber).setResizable(true);
		tc.getColumn(notesColumnNumber).setModelIndex(notesColumnNumber);


		tc.getColumn(selectColumnNumber).setPreferredWidth(75);		
		tc.getColumn(selectColumnNumber).setResizable(true);
		tc.getColumn(selectColumnNumber).setModelIndex(selectColumnNumber);

		jTable.setColumnModel(tc);
		jTable.setRowHeight(20);
		jTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	}

	/**
	 * @return
	 */
	private boolean discardData() {

		int i =CustomGUI.stopQuestion("Discard manually selected data?",
				"Do you wish to discard all the edited information?" +
						"\n(If you select 'No' edited data will available for later use)",
						new String[]{"Yes", "No", "Info"});
		if(i<2)
		{
			switch (i)
			{
			case 0:return true;
			default:return false;
			}
		}
		else
		{
			Workbench.getInstance().warn(
					"If you discard the edited information, all previously selected genes, enzymes and gene products" +
							"\nwill be returned their default alpha values, as well as edited gene names, identifiers (locus tag)" +
							"\nand chromosomes (if available)."+
							"\nUser inserted data such as ec numbers or product names unavailable on BLAST for a certain gene," +
							"\nwill also be discarded, as well as deleted ec numbers and products."+
							"\nIf you do not discard the edited data, you can revert to your previously reviewed information" +
					"\nwhen selecting the 'Manual Selection'.");
			return discardData();
		}
	}

	/**
	 * 
	 */
	private void initialiser(){

		this.homologyDataContainer.setProductList(new TreeMap<Integer, String>()); 
		this.homologyDataContainer.setEnzymesList(new TreeMap<Integer, String>());  
		this.homologyDataContainer.setNamesList(new TreeMap<Integer, String>()); 
		this.homologyDataContainer.setLocusList(new TreeMap<Integer, String>()); 
		this.homologyDataContainer.setSelectedGene(new TreeMap<Integer, Boolean>());
		this.homologyDataContainer.setEditedProductData(new TreeMap<Integer, String[]>());
		this.homologyDataContainer.setEditedEnzymeData(new TreeMap<Integer, String[]>());
		this.homologyDataContainer.setNotesMap(new TreeMap<Integer, String>());
	}

	/**
	 * @param i
	 * @return
	 */
	private ButtonColumn buildButtonColumn(final int i) {

		return new ButtonColumn(jTable, i, this.buttonActionListener, this.buttonMouseAdapter);
	}

	/**
	 * @return
	 */
	private ActionListener getButtonActionListener() {

		return new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

				processButton(arg0);}
		};
	}

	/**
	 * @return
	 */
	private MouseAdapter getButtonMouseAdapter() {

		return new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				Point p = e.getPoint();

				int  columnNumber = jTable.columnAtPoint(p);
				jTable.setColumnSelectionInterval(columnNumber, columnNumber);
				selectedModelRow=jTable.getSelectedRow();
				homologyDataContainer.setSelectedRow(selectedModelRow);
				processButton(e);
			}
		};
	}

	/**
	 * @param i
	 * @param starsColorMap 
	 * @return
	 */
	private StarColumn buildStarColumn(final int i, Map<Integer, Integer> starsColorMap) {

		return new StarColumn(jTable, i, this.starActionListener, this.starMouseAdapter, starsColorMap);
	}

	/**
	 * @return
	 */
	private ActionListener getStarActionListener() {

		return new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

				processStarButton(arg0);
			}
		};
	}

	/**
	 * @return
	 */
	private MouseAdapter getStarMouseAdapter() {

		return new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				Point p = e.getPoint();
				int  columnNumber = jTable.columnAtPoint(p);
				jTable.setColumnSelectionInterval(columnNumber, columnNumber);
				selectedModelRow=jTable.getSelectedRow();
				homologyDataContainer.setSelectedRow(selectedModelRow);
				processStarButton(e);
			}
		};
	}

	/**
	 * @param column
	 * @param items
	 * @return
	 */
	private ComboBoxColumn buildComboBoxColumn(final int column, Map<Integer,String> items) {

		if(column == this.namesColumnNumber) {

			return  new ComboBoxColumn(jTable, column, items , this.namesItemListener, this.namesMouseAdapter, this.namesPopupMenuListener);
		}
		else {

			return  new ComboBoxColumn(jTable, column, items , this.enzymesItemListener, this.enzymesMouseAdapter, this.enzymesPopupMenuListener);
		}
	}

	/**
	 * @return
	 */
	private ItemListener getComboBoxNamesItemListener() {

		return new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {

				processProductComboBoxChange(e);
			}
		};
	}

	/**
	 * @return
	 */
	private PopupMenuListener getComboBoxNamesPopupMenuListener() {

		return new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

				processProductComboBoxChange(e);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {

			}
		};
	}

	/**
	 * @return
	 */
	private ItemListener getComboBoxEnzymesItemListener() {

		return new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {

				processEnzymesComboBoxChange(e);
			}
		};
	}

	/**
	 * @return
	 */
	private PopupMenuListener getComboBoxEnzymesPopupMenuListener() {

		return new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

				processEnzymesComboBoxChange(e);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {

			}
		};
	}

	/**
	 * @return
	 */
	private MouseAdapter getComboBoxNamesMouseListener() {

		return new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				Point p = e.getPoint();

				int  columnNumber = jTable.columnAtPoint(p);
				jTable.setColumnSelectionInterval(columnNumber, columnNumber);

				selectedModelRow = jTable.getSelectedRow();
				homologyDataContainer.setSelectedRow(selectedModelRow);

				int myRow = jTable.getSelectedRow();

				if(myRow>-1 && jTable.getRowCount()>0 && jTable.getRowCount()> myRow) {

					jTable.setRowSelectionInterval(myRow, myRow);
					scrollToVisible(jTable.getCellRect(myRow, -1, true));
				}

				processProductComboBoxChange(e);
			}
		};		
	}

	/**
	 * @return
	 */
	private MouseAdapter getComboBoxEnzymesMouseListener() {

		return new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				Point p = e.getPoint();

				int  columnNumber = jTable.columnAtPoint(p);
				jTable.setColumnSelectionInterval(columnNumber, columnNumber);

				selectedModelRow = jTable.getSelectedRow();
				homologyDataContainer.setSelectedRow(selectedModelRow);

				int myRow = jTable.getSelectedRow();

				if(myRow>-1 && jTable.getRowCount()>0 && jTable.getRowCount()> myRow) {

					jTable.setRowSelectionInterval(myRow, myRow);
					scrollToVisible(jTable.getCellRect(myRow, -1, true));
				}

				processEnzymesComboBoxChange(e);
			}
		};		
	}

	/**
	 * @param e
	 */
	@SuppressWarnings("unchecked")
	private void processProductComboBoxChange(EventObject e) {

		boolean go = false;
		JComboBox<String> comboBox = null;

		if(e.getClass()==MouseEvent.class) {

			Object obj = ((MouseEvent) e).getSource();

			if(obj instanceof JComboBox) {

				comboBox = (JComboBox<String>) obj;
			}

			ListSelectionModel model = jTable.getSelectionModel();
			model.setSelectionInterval( productsColumn.getSelectIndex(comboBox), productsColumn.getSelectIndex(comboBox));

			if(comboBox != null) {

				go = true;
			}

			if(((MouseEvent) e).getButton()==MouseEvent.BUTTON3 ) {

				List<Integer> dbs = new ArrayList<Integer>();

				String text=null;

				dbs.add(1);
				dbs.add(2);
				text=comboBox.getSelectedItem().toString();

				if(text!=null) {

					new LinkOut(dbs, text).show(((MouseEvent) e).getComponent(),((MouseEvent) e).getX(), ((MouseEvent) e).getY());
				}
			}
		}
		else if((e.getClass()==ItemEvent.class && ((ItemEvent) e).getStateChange() == ItemEvent.SELECTED) ) {

			Object obj = ((ItemEvent) e).getSource();

			if(obj instanceof JComboBox) {

				comboBox = (JComboBox<String>) obj;
			}
			if(comboBox != null) {

				go = true;
			}
		}

		else if(e.getClass() == PopupMenuEvent.class) {

			Object obj = ((PopupMenuEvent) e).getSource();

			if(obj instanceof JComboBox) {

				comboBox = (JComboBox<String>) obj;
			}

			if(comboBox != null) {

				go = true;
			}
		}

		if(go) {

			this.selectedModelRow = productsColumn.getSelectIndex(comboBox);
			this.homologyDataContainer.setSelectedRow(this.selectedModelRow);

			if(this.selectedModelRow < 0) {

				this.selectedModelRow = Integer.parseInt(comboBox.getName());
			}

			String selectedItem = (String) comboBox.getSelectedItem();

			if(this.selectedModelRow>-1 && productsColumn.getValues().containsKey(selectedModelRow) && !selectedItem.trim().equals(productsColumn.getValues().get(selectedModelRow))) {

				this.homologyDataContainer.setSelectedRow(this.selectedModelRow);
				int row = this.homologyDataContainer.getSelectedRow();
				this.updateProductsComboBox(comboBox, selectedItem, row);
				this.selectedModelRow = row;
			}
		}
	}

	/**
	 * @param comboBox
	 * @param selectedItem
	 * @param row
	 */
	private void updateProductsComboBox(JComboBox<String> comboBox, String selectedItem, int row ) {

		comboBox.setToolTipText((String) comboBox.getSelectedItem());
		this.productsColumn.getValues().put(row , selectedItem);

		String pdWeigth = this.homologyDataContainer.getProductPercentage(selectedItem, row );
		this.jTable.setValueAt(pdWeigth, row , namesScoreColumnNumber);
		this.jTable.setValueAt(new Boolean(true), row , selectColumnNumber);
		int key = Integer.parseInt(homologyDataContainer.getKeys().get(row));
		this.homologyDataContainer.getProductList().put(key, selectedItem);
		this.homologyDataContainer.getSelectedGene().put(key, true);
	}

	/**
	 * @param e
	 */
	@SuppressWarnings("unchecked")
	private void processEnzymesComboBoxChange(EventObject e) {

		boolean go = false;
		JComboBox<String> comboBox = null;

		if(e.getClass()==MouseEvent.class) {

			Object obj = ((MouseEvent) e).getSource();

			if(obj instanceof JComboBox) {

				comboBox = (JComboBox<String>) obj;
			}

			ListSelectionModel model = jTable.getSelectionModel();
			model.setSelectionInterval( enzymesColumn.getSelectIndex(comboBox), enzymesColumn.getSelectIndex(comboBox));

			if(((MouseEvent) e).getButton()==MouseEvent.BUTTON3 ) {

				List<Integer> dbs = new ArrayList<Integer>();

				String text=null;

				dbs.add(1);
				dbs.add(3);
				text=comboBox.getSelectedItem().toString();

				if(text!=null) {

					new LinkOut(dbs, text).show(((MouseEvent) e).getComponent(),((MouseEvent) e).getX(), ((MouseEvent) e).getY());
				}
			}
		}

		else if((e.getClass()==ItemEvent.class && ((ItemEvent) e).getStateChange() == ItemEvent.SELECTED) ) {

			Object obj = ((ItemEvent) e).getSource();

			if(obj instanceof JComboBox) {

				comboBox = (JComboBox<String>) obj;
			}
			if(comboBox != null) {

				go = true;
			}
		}

		else if(e.getClass() == PopupMenuEvent.class) {

			Object obj = ((PopupMenuEvent) e).getSource();

			if(obj instanceof JComboBox) {

				comboBox = (JComboBox<String>) obj;
			}

			if(comboBox != null) {

				go = true;
			}
		}

		if(go) {

			this.selectedModelRow = enzymesColumn.getSelectIndex(comboBox);

			if(this.selectedModelRow < 0) {

				this.selectedModelRow = Integer.parseInt(comboBox.getName());
			}

			String selectedItem = (String) comboBox.getSelectedItem();

			if(this.selectedModelRow>-1 && enzymesColumn.getValues().containsKey(selectedModelRow) && !selectedItem.trim().equalsIgnoreCase(enzymesColumn.getValues().get(selectedModelRow))) {



				this.homologyDataContainer.setSelectedRow(this.selectedModelRow);
				int row = this.homologyDataContainer.getSelectedRow();
				this.updateEnzymesComboBox(comboBox, selectedItem, row );
				this.selectedModelRow = row;
			}
		}
	}

	/**
	 * @param comboBox
	 * @param selectedItem
	 * @param row
	 */
	private void updateEnzymesComboBox(JComboBox<String> comboBox, String selectedItem, int row ) {

		List<String> merlin_ecs = new ArrayList<String>();
		String[] ecs = ((String) comboBox.getSelectedItem()).split(",");

		for(String ec : ecs) {

			merlin_ecs.add(ec.trim());
		}

		int result = -10;
		if(this.getUniprotECnumbersTable.containsKey(row)) {

			List<String> uniprot_ecs = new ArrayList<String>(this.getUniprotECnumbersTable.get(row));
			result = this.compareAnnotationsLists(merlin_ecs, uniprot_ecs);
		}
		else {

			if(!merlin_ecs.get(0).equalsIgnoreCase("null") && !merlin_ecs.get(0).equalsIgnoreCase("")) {

				result = 0;
			}
		}

		this.buttonStarColumn.getValueArray().get(row).setBackground(StarColumn.getBackgroundColor(result));
		this.buttonStarColumn.getValueArray().get(row).updateUI();
		this.buttonStarColumn.getValueArray().get(row).repaint();
		this.buttonStarColumn.getValueArray().get(row).paintImmediately(this.buttonStarColumn.getValueArray().get(row).getBounds());

		comboBox.setToolTipText((String) comboBox.getSelectedItem());
		this.enzymesColumn.getValues().put(row, selectedItem);

		String ecWeigth = homologyDataContainer.getECPercentage(selectedItem,row);
		if(selectedItem.isEmpty()) {

			ecWeigth = "";
		}
		jTable.setValueAt(ecWeigth, row, ecScoreColumnNumber);
		jTable.setValueAt(new Boolean(true), row, selectColumnNumber);
		jRadioButtonManSel.setSelected(true);
		int key = Integer.parseInt(homologyDataContainer.getKeys().get(row));
		homologyDataContainer.getEnzymesList().put(key, selectedItem);
		homologyDataContainer.getSelectedGene().put(key , true);
	}

	/**
	 * @param arg0
	 */
	private void processButton(EventObject arg0) {

		JButton button = null;
		if(arg0.getClass()==ActionEvent.class) {

			button = (JButton)((ActionEvent) arg0).getSource();
			ListSelectionModel model = jTable.getSelectionModel();

			int row = buttonColumn.getSelectIndex(button);
			model.setSelectionInterval(row, row);
		}		
		else if(arg0.getClass()==MouseEvent.class) {

			Point p = ((MouseEvent) arg0).getPoint();
			int  columnNumber = jTable.columnAtPoint(p);
			jTable.setColumnSelectionInterval(columnNumber, columnNumber);
			button = (JButton) buttonColumn.getValueArray().get(jTable.getSelectedRow());
			selectedModelRow = jTable.getSelectedRow();
		}

		if(button!=null) {

			int row = buttonColumn.getSelectIndex(button);
			selectedModelRow = jTable.getSelectedRow();

			new GenericDetailWindowBlast(homologyDataContainer.getRowInfo(row), 
					"Homology Data", "Gene: " + homologyDataContainer.getGeneLocus(row));

			if(jTable.isEditing()) {

				jTable.getCellEditor().stopCellEditing();
			}
		}
	}

	/**
	 * @param arg0
	 */
	private void processStarButton(EventObject arg0) {

		JButton button = null;
		if(arg0.getClass()==ActionEvent.class) {

			button = (JButton)((ActionEvent) arg0).getSource();
			ListSelectionModel model = jTable.getSelectionModel();
			int row = buttonColumn.getSelectIndex(button);
			model.setSelectionInterval(row, row);

		}		
		else if(arg0.getClass()==MouseEvent.class) {

			Point p = ((MouseEvent) arg0).getPoint();
			int  columnNumber = jTable.columnAtPoint(p);
			jTable.setColumnSelectionInterval(columnNumber, columnNumber);
			button = (JButton) buttonStarColumn.getValueArray().get(jTable.getSelectedRow());
			selectedModelRow = jTable.getSelectedRow();
		}

		if(button!=null) {

			OpenBrowser  openUrl = new OpenBrowser();
			openUrl.setUrl("http://www.uniprot.org/uniprot/?query="+(String)jTable.getValueAt(jTable.getSelectedRow(), locus_tagColumnNumber)+"&sort=score");
			openUrl.openURL();
			if(jTable.isEditing()) {

				jTable.getCellEditor().stopCellEditing();
			}
		}
	}

	/**
	 * @return
	 */
	private boolean exportAllData() {

		int i =CustomGUI.stopQuestion("Export all available data?",
				"Do you wish to export all information, including the data available inside the dropdown boxes?",
				new String[]{"Yes", "No", "Info"});
		if(i<2) {

			switch (i)
			{
			case 0:return true;
			default:return false;
			}
		}
		else {

			Workbench.getInstance().warn("If you select 'No' only data selected on the dropdown boxes will be exported.\n" +
					"If you select yes all homology data, including the one inside dropdown boxes, will be exported.");
			return exportAllData();
		}
	}

	/**
	 * @param path
	 * 
	 * Export Data to xls tabbed files
	 * 
	 */
	public void exportToXls(boolean allData, String path) {

		String file = System.getProperty("user.home");

		if(!path.equals("")) {

			file=path;
		}

		Calendar cal = new GregorianCalendar();

		// Get the components of the time
		int hour24 = cal.get(Calendar.HOUR_OF_DAY);     // 0..23
		int min = cal.get(Calendar.MINUTE);             // 0..59
		int day = cal.get(Calendar.DAY_OF_YEAR);		//0..365
		if(jRadioButtonMETAGENES.isSelected()) {

			file+="/homologyData_"+homologyDataContainer.getProject().getName()+"_"+hour24+"_"+min+"_"+day+".xls";
		}
		else {

			file+="/homologyData_"+homologyDataContainer.getProject().getName()+"_"+hour24+"_"+min+"_"+day+".xls";
		}

		try {

			FileWriter fw = new FileWriter (file);
			BufferedWriter bw = new BufferedWriter(fw);

			String header ="";
			TableColumnModel tc = jTable.getColumnModel();
			int headerSize = tc.getColumnCount();
			int h = 1; // skip inof column
			while (h < headerSize-1) {

				header+=tc.getColumn(h).getHeaderValue().toString()+"\t";
				h++;
			}

			bw.write(header+"\n");
			String dataSpacer;
			if(homologyDataContainer.isEukaryote()) {

				//bw.write("Gene\tName\tChromosome\tProduct\tScore\tEC Number\tScore\n");
				dataSpacer="\t\t\t\t";
			}
			else {

				//bw.write("Gene\tName\tProduct\tScore\tEC Number\tScore\n");
				dataSpacer="\t\t\t";
			}


			bw.newLine();
			bw.newLine();
			//			}

			for(int i=0; i < mainTableData.getTable().size(); i++) {

				if((Boolean)mainTableData.getRow(i)[selectColumnNumber] == true) {

					String[] productsToXLS = null, enzymesToXLS = null;
					String pdWeigth, ecWeigth;
					boolean existsProduct=false, existsEnzyme=false;
					String product=null, enzyme=null;

					// j=1 because the first column (j=0) is the button column; -1 because of the select column
					for (int j=1; j < mainTableData.getRow(i).length-1; j++) {

						if(j==namesColumnNumber && mainTableData.getRow(i)[j].getClass()==String[].class) {


							productsToXLS = (String[])mainTableData.getRow(i)[j]; 

							product = this.productsColumn.getSelectItem(i);
							bw.write("\t"+product+"\t");
							productsToXLS = this.removeElement(productsToXLS, product);
							existsProduct=true;
						}
						else if(j==namesScoreColumnNumber && existsProduct) {

							existsProduct=false;
							bw.write(homologyDataContainer.getProductPercentage(product,i)+"");
							product=null;
						}
						else if(j==ecnumbersColumnNumber && mainTableData.getRow(i)[j].getClass()==String[].class) {

							enzymesToXLS = (String[])mainTableData.getRow(i)[j];
							enzyme = this.enzymesColumn.getSelectItem(i);

							if(enzyme!=null) {

								bw.write("\t"+enzyme+"\t");
								enzymesToXLS = this.removeElement(enzymesToXLS, enzyme);
								existsEnzyme=true;
							}
						}
						else if(j==ecScoreColumnNumber && existsEnzyme) {

							existsEnzyme=false;
							bw.write(homologyDataContainer.getECPercentage(enzyme,i)+"");
							enzyme=null;
						}
						else {

							if(j==locus_tagColumnNumber) {

								bw.write(mainTableData.getRow(i)[j]+"");
							}
							else if(j==uniprotStarColumnNumber) {

								String text = mainTableData.getRow(i)[j]+"";
								if(text.equalsIgnoreCase("1")) {

									text = "reviewed";
								}
								if(text.equalsIgnoreCase("0")) {

									text = "unreviewed";
								}
								if(text.equalsIgnoreCase("-1")) {

									text = "unavailable";
								}

								bw.write("\t"+text);
							}
							else if(homologyDataContainer.isEukaryote() && j==chromosomeColumnNumber) {

								bw.write("\t"+mainTableData.getRow(i)[j]);
							}
							else {

								bw.write("\t"+mainTableData.getRow(i)[j]);
							}
						}
					}

					if(allData) {

						if(productsToXLS!=null && enzymesToXLS!=null) {

							bw.newLine();
							int maxlength=0;

							if(productsToXLS.length>enzymesToXLS.length) {

								maxlength=productsToXLS.length;
							}
							else {

								maxlength=enzymesToXLS.length;
							}

							for(int k=0;k<maxlength;k++) {

								if(k<productsToXLS.length&&k<enzymesToXLS.length) {

									pdWeigth = homologyDataContainer.getProductPercentage(productsToXLS[k].trim(),i);
									ecWeigth = homologyDataContainer.getECPercentage(enzymesToXLS[k].trim(),i);
									if(productsToXLS[k].trim()==""){productsToXLS[k]="\t";}
									if(enzymesToXLS[k].trim()==""){enzymesToXLS[k]="\t";}	
									bw.write("\t\t\t"+productsToXLS[k].trim()+"\t"+pdWeigth+"\t"+enzymesToXLS[k].trim()+"\t"+ecWeigth+"\n");
								}
								else if(k<productsToXLS.length) {

									pdWeigth = homologyDataContainer.getProductPercentage(productsToXLS[k].trim(),i);
									if(productsToXLS[k].trim()==""){productsToXLS[k]="\t";}
									bw.write(dataSpacer+productsToXLS[k].trim()+"\t"+pdWeigth+"\n");
								}
								else if(k<enzymesToXLS.length) {

									ecWeigth = homologyDataContainer.getECPercentage(enzymesToXLS[k],i);
									if(enzymesToXLS[k].trim()==""){enzymesToXLS[k]="\t";}
									bw.write(dataSpacer+"\t\t"+enzymesToXLS[k].trim()+"\t"+ecWeigth+"\n");
								}
							}
						}
						else if(productsToXLS!=null) {

							bw.newLine();
							for(int k=1;k<productsToXLS.length;k++) {

								pdWeigth = homologyDataContainer.getProductPercentage(productsToXLS[k].trim(),i);
								if(productsToXLS[k].trim()==""){productsToXLS[k]="\t";}
								bw.write(dataSpacer+productsToXLS[k].trim()+"\t"+pdWeigth+"\n");									
							}
						}
						else if(enzymesToXLS!=null) {

							bw.newLine();
							for(int k=1;k<enzymesToXLS.length;k++) {

								ecWeigth = homologyDataContainer.getECPercentage(enzymesToXLS[k].trim(),i);
								if(enzymesToXLS[k].trim()==""){enzymesToXLS[k]="\t";}
								if(!jRadioButtonMETAGENES.isSelected())
								{
									bw.write(dataSpacer+"\t\t"+enzymesToXLS[k].trim()+"\t"+ecWeigth+"\n");
								}
								else
								{
									bw.write(dataSpacer+enzymesToXLS[k].trim()+"\t"+ecWeigth+"\n");
								}
							}
						}
					}

					bw.newLine();	
				}
			}
			bw.flush();
			bw.close();
			Workbench.getInstance().info("Data successfully exported.");
		} 
		catch (Exception e) {

			Workbench.getInstance().error("An error occurred while performing this operation. Error "+e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * @author ODias
	 * Filter GenBank files
	 */
	class GBKFileFilter extends javax.swing.filechooser.FileFilter {

		public boolean accept(File f) {

			return f.isDirectory() || f.getName().toLowerCase().endsWith(".gbk");
		}

		public String getDescription() {

			return ".gbk files";
		}
	}

	/**
	 * Save re-annotated GenBank file
	 * @throws IOException 
	 */
	private void saveGenbankFile() throws IOException{

		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setFileFilter(new GBKFileFilter());
		fc.setDialogTitle("Select gbk files directory");
		int returnVal = fc.showOpenDialog(new JTextArea());

		if (returnVal == JFileChooser.APPROVE_OPTION) {

			File[]	files;
			if(fc.getSelectedFile().isDirectory()) {

				files = fc.getSelectedFile().listFiles();
			}
			else {

				files = (new File(fc.getSelectedFile().getParent())).listFiles();
			}

			int countFiles=0;

			for(File file:files) {

				if(file.getAbsolutePath().endsWith(".gbk"))
					countFiles+=1;
			}
			if(countFiles<=0) {

				Workbench.getInstance().error("The selected directory does not contain any Genbank file (*.gbk)");
			}
			else {

				TreeMap<String,String> gene = new TreeMap<String, String>();
				TreeMap<String,String> ecn = new TreeMap<String, String>();
				TreeMap<String,String> prod = new TreeMap<String, String>();

				for(int i=0; i < mainTableData.getTable().size(); i++) {

					if((Boolean) mainTableData.getRow(i)[selectColumnNumber]) {

						if(this.productsColumn.getSelectItem(i)!=null) {

							prod.put((String) mainTableData.getRow(i)[locus_tagColumnNumber], this.productsColumn.getSelectItem(i));
						}

						if(jTable.getValueAt(i,geneNameColumnNumber)!=null && !((String) jTable.getValueAt(i,geneNameColumnNumber)).isEmpty()) {

							gene.put((String) mainTableData.getRow(i)[locus_tagColumnNumber], ((String) jTable.getValueAt(i,geneNameColumnNumber)));
						}

						if(this.enzymesColumn.getSelectItem(i)!=null) {

							ecn.put((String) mainTableData.getRow(i)[locus_tagColumnNumber], this.enzymesColumn.getSelectItem(i));
						}

						//						p=""; e=""; g="";
						//						if(!((String) mainTableData.getRow(i)[2]).isEmpty())
						//						{
						//							g = ((String) jTable.getValueAt(i,2));
						//						}
						//						if(((String[]) mainTableData.getRow(i)[namesColumnNumber]).length>0)
						//						{
						//							if(prodItem.containsKey(i))
						//							{
						//								for(String s: (String[]) mainTableData.getRow(i)[namesColumnNumber])
						//								{
						//									if(s==prodItem.get(i))
						//									{
						//										p=s;
						//									}
						//								}
						//							}
						//							else
						//							{
						//								p = ((String[]) mainTableData.getRow(i)[namesColumnNumber])[0];
						//							}
						//						}
						//						if(((String[]) mainTableData.getRow(i)[ecnumbersColumnNumber]).length>0)
						//						{
						//							if(ecItem.containsKey(i))
						//							{
						//								for(String s: (String[]) mainTableData.getRow(i)[ecnumbersColumnNumber])
						//								{
						//									if(s==ecItem.get(i))
						//									{
						//										e=s;
						//									}
						//								}
						//							}
						//							else
						//							{
						//								e = ((String[]) mainTableData.getRow(i)[ecnumbersColumnNumber])[0];
						//							}
						//						}
						//						if(!p.isEmpty()) prod.put((String) mainTableData.getRow(i)[1], p);
						//						if(!g.isEmpty()) gene.put((String) mainTableData.getRow(i)[1], g);
						//						if(!e.isEmpty()) ecn.put((String) mainTableData.getRow(i)[1], e);
					}						                             
				}

				for(File f: files)
				{
					if(f.getAbsolutePath().endsWith(".gbk"))
					{
						WriteGBFile wgbf = new WriteGBFile(f, ecn, prod, gene);
						wgbf.writeFile();
					}
				}
			}
		}
	}

	/**
	 * 
	 */
	private void selectThreshold() {

		this.homologyDataContainer.setSelectedGene(new HashMap<Integer, Boolean>());
		this.jRadioButtonTresh.setSelected(true);

		if(Double.parseDouble(this.jTextFieldThreshold.getText())<0 || Double.parseDouble(this.jTextFieldThreshold.getText())>1) {

			this.jTextFieldThreshold.setText(this.homologyDataContainer.getThreshold().toString());
			Workbench.getInstance().warn("The value must be between 0 and 1");
		}
		else {

			Rectangle visible = jTable.getVisibleRect();

			this.homologyDataContainer.setThreshold(Double.parseDouble(this.jTextFieldThreshold.getText()));
			this.mainTableData = this.homologyDataContainer.getAllGenes();
			//this.jTable.setModel(this.mainTableData);
			this.fillList(visible);
		}
	}

	/**
	 * @param locusMap
	 * @return
	 */
	private Map<Integer, List<String>> getUniprotECnumbersTable(Map<Integer, String> locusMap) {

		Map<Integer, List<String>> result = new HashMap<Integer, List<String>>();
		Map<String, List<String>> uniprotData = this.homologyDataContainer.get_uniprot_ecnumbers();

		Map<String, Integer> inv = new HashMap<String, Integer>(); 

		for (Entry<Integer, String> entry : locusMap.entrySet()) {

			inv.put(entry.getValue(), entry.getKey());
		}

		if(uniprotData != null) {

			for(String locus : uniprotData.keySet()) {

				if(inv.containsKey(locus)) {

					result.put(inv.get(locus), uniprotData.get(locus));
				}
			}
		}
		return result;
	}

	/**
	 * @param uniprot
	 * @param merlin
	 * @return
	 * 
	 * 
	 *  0 distinct
	 * -1 partial match uniprot more
	 *  1 match
	 *  2 partial match merlin more
	 * 
	 */
	private Map<Integer, Integer> compareAnnotations(Map<Integer, String> ecItems) {

		Map<Integer, Integer> result = new HashMap<Integer, Integer>();

		Map<Integer, List<String>> merlin = new HashMap<Integer, List<String>>();
		for(int row : ecItems.keySet()) {
			
			if(ecItems.get(row) != null) {

				List<String> ecnumbers = new ArrayList<String>();
				String[] ecs = ecItems.get(row).split(", ");

				for(String ec : ecs) {

					ecnumbers.add(ec.trim());
				}
				merlin.put(row, ecnumbers);
			}
		}

		List<Integer> merlinKeySet = new ArrayList<Integer>(merlin.keySet());
		Map<Integer, List<String>> uniprotECnumbersTable_clone = new HashMap<Integer, List<String>>(this.getUniprotECnumbersTable);
		
		for(int row : merlinKeySet) {
			
			List<String> merlin_ecs = new ArrayList<String> (merlin.get(row));

			if(uniprotECnumbersTable_clone.containsKey(row) && !uniprotECnumbersTable_clone.get(row).get(0).equalsIgnoreCase("null") && !uniprotECnumbersTable_clone.get(row).get(0).equalsIgnoreCase("")) {

				List<String> uni_ecs = new ArrayList<String> (uniprotECnumbersTable_clone.get(row));
				
				result.put(row, this.compareAnnotationsLists(merlin_ecs, uni_ecs));
				uniprotECnumbersTable_clone.remove(row);
			}
			else{

				if(!merlin_ecs.get(0).equalsIgnoreCase("null") && !merlin_ecs.get(0).equalsIgnoreCase("")) {

					result.put(row, 0);
				}
			}
			merlin.remove(row);
		}

		List<Integer> uniprotKeySet = new ArrayList<Integer>(uniprotECnumbersTable_clone.keySet());

		for(int row : uniprotKeySet) {

			if(!uniprotECnumbersTable_clone.get(row).get(0).equalsIgnoreCase("null") && !uniprotECnumbersTable_clone.get(row).get(0).equalsIgnoreCase("")) {

				result.put(row, 0);
			}
			uniprotECnumbersTable_clone.remove(row);
		}

		return result;
	}


	/**
	 * @param merlin_ecs
	 * @param uni_ecs
	 * @return
	 */
	private int compareAnnotationsLists(List<String> merlin_ecs, List<String> uniprot_ecs) {

		List<String> uni_ecs = new ArrayList<String>(uniprot_ecs);
		List<String> merlin_ecs_clone = new ArrayList<String> (merlin_ecs);
		List<String> uni_ecs_clone = new ArrayList<String> (uni_ecs);
		int uni_initial_size = uni_ecs_clone.size();
		int merlin_initial_size =  merlin_ecs_clone.size();

		if(merlin_ecs.size() == uni_ecs.size()) {

			for(String ecnumber :  merlin_ecs_clone) {

				merlin_ecs.remove(ecnumber);
				if(uni_ecs.contains(ecnumber)) {

					uni_ecs.remove(ecnumber);
				}
			}

			if(uni_ecs.isEmpty()) {

				return 1;
			}
			else if(!uni_ecs.isEmpty() && !uni_ecs.get(0).equalsIgnoreCase("null") && !uni_ecs.get(0).equalsIgnoreCase("")) {

				return 0;
			}
		}
		else {

			if(merlin_ecs.size() > uni_ecs.size()) {

				for(String ecnumber :  merlin_ecs_clone) {

					merlin_ecs.remove(ecnumber);
					if(uni_ecs.contains(ecnumber)) {

						uni_ecs.remove(ecnumber);
					}
				}

				if(uni_ecs.isEmpty()) {

					return 2;
				}
				else if(!uni_ecs.isEmpty() && !uni_ecs.get(0).equalsIgnoreCase("null") && !uni_ecs.get(0).equalsIgnoreCase("")) {

					if(uni_initial_size == uni_ecs.size()) {

						return 0;
					}
					else {

						return -1;
					}
				}
			}
			else {

				for(String ecnumber :  uni_ecs_clone) {

					uni_ecs.remove(ecnumber);
					if(merlin_ecs.contains(ecnumber)) {

						merlin_ecs.remove(ecnumber);
					}
				}

				if(merlin_ecs.isEmpty()) {

					return -1;
				}
				else if(!merlin_ecs.isEmpty() && !merlin_ecs.get(0).equalsIgnoreCase("null") && !merlin_ecs.get(0).equalsIgnoreCase("")) {

					if(merlin_initial_size == merlin_ecs.size()) {

						return 0;
					}
					else {

						return -1;
					}
				}
			}

		}
		return -10;
	}

	/**
	 * @param array
	 * @param element
	 * @return
	 */
	private String[] removeElement(String[] array, String element) {
		if(Arrays.asList(array).contains(element)) {

			String[] newArray = new String[array.length-1];
			boolean reachedElement = false;

			for(int i=0; i<(array.length-1); i++) {

				if(!array[i].equals(element)) {

					if(reachedElement) {

						newArray[i]=array[i+1];
					}
					else {

						newArray[i]=array[i];
					}
				}
				else {

					reachedElement = true;
					newArray[i]=array[i+1];
				}
			}
			return newArray;
		}
		return array;
	}

	/**
	 * @param visible
	 */
	private void scrollToVisible(final Rectangle visible) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				jTable.scrollRectToVisible(visible);
			}
		});
	}

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#updateGraphicalObject()
	 */
	@Override
	public void updateTableUI() {

		Rectangle visible = null;

		if(this.selectedModelRow>-1 && jTable.getRowCount()>0 && jTable.getRowCount()> this.selectedModelRow) {

			visible = this.jTable.getCellRect(this.selectedModelRow, -1, true);
		}

		this.mainTableData = this.homologyDataContainer.getAllGenes();
		this.fillList(visible);
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
