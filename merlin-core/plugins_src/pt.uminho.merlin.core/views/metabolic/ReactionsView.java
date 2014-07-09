package views.metabolic;

import gui.InsertEditReaction;
import gui.InsertPathway;
import gui.RemovePathwayFromModel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import pt.uminho.sysbio.common.biocomponents.validation.chemestry.BalanceValidator;
import pt.uminho.sysbio.merlin.utilities.OpenBrowser;
import utilities.ButtonColumn;
import utilities.CreateImageIcon;
import utilities.SearchInTable;
import views.UpdatablePanel;
import views.windows.GenericDetailWindow;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.metabolic.PathwayReaction;
import datatypes.metabolic.ReactionsContainer;
import es.uvigo.ei.aibench.workbench.Workbench;


/**
 * @author ODias
 *
 */
public class ReactionsView extends UpdatablePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int REACTION_NAME_COLUMN = 2;
	private static final int IN_MODEL_COLUMN = 8;
	private JScrollPane jScrollPane1;
	private JPanel jPanel1, jPanel2;
	private JTable jTable;
	private ReactionsContainer reactionsContainer;
	private JComboBox<String> pathsComboBox;
	private JPanel jPanelReactions;
	private JButton jButtonColor;
	private JRadioButton jRadioAllReactions, jRadioButtonEncoded;
	private DataTable mainTableData;
	private GenericDataTable specificPathwayData;
	private JPanel jPanelPathways;
	private JButton jButtonDuplicate, jButtonRemovePath;
	private JPanel jPanelReactionsEdition;
	private JButton jButtonInsert, jButtonEdit;
	private JPanel jPanelExport;
	private JButton jButtonExportTxt;
	private JPanel jPanelPaths;
	private ButtonGroup buttonGroup;
	private SearchInTable searchInReaction;
	private Integer[] tableColumnsSize;
	//private int selectedRow;
	private JRadioButton jRadioButtonReactionsOnly, jRadioButtonByPathway;
	private ButtonColumn buttonColumn;
	private ButtonGroup buttonGroup3;
	private JButton jButtonNewPathway;
	private boolean encodedSelected;
	protected String previousSelectedPathway;
	private JButton jViewInBrowser;
	private JRadioButton jRadioButtonGapsOnly;
	private JRadioButton jRadioButtonUnbalancedOnly;


	/**
	 * @param reactionsContainer
	 */
	public ReactionsView(ReactionsContainer reactionsContainer) {

		super(reactionsContainer);
		this.reactionsContainer = reactionsContainer;
		this.encodedSelected=this.reactionsContainer.existGenes();
		this.mainTableData = this.reactionsContainer.getReactionsData(this.encodedSelected);
		this.searchInReaction = new SearchInTable();
		this.initGUI();
		//this.selectedRow=-1;
		this.tableColumnsSize = reactionsContainer.getTableColumnsSize();
		this.fillList(false, false);
	}

	/**
	 * intitiate gui
	 */
	private void initGUI() {

		try  {

			GridBagLayout jPanelLayout = new GridBagLayout();
			jPanelLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
			jPanelLayout.columnWidths = new int[] {7, 7, 7};
			jPanelLayout.rowWeights = new double[] {0.0, 200.0, 0.0, 0.0, 0.0};
			jPanelLayout.rowHeights = new int[] {7, 50, 7, 3, 7};
			this.setLayout(jPanelLayout);

			jPanel2 = new JPanel();
			GridBagLayout jPanel2Layout = new GridBagLayout();
			jPanel2Layout.columnWeights = new double[] {0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1};
			jPanel2Layout.columnWidths = new int[] {7, 7, 7, 7, 7, 7, 7};
			jPanel2Layout.rowWeights = new double[] {0.1, 0.1};
			jPanel2Layout.rowHeights = new int[] {7, 7};
			jPanel2.setLayout(jPanel2Layout);
			this.add(jPanel2, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			jPanelPaths = new JPanel();
			GridBagLayout jPanelGenesLayout = new GridBagLayout();
			jPanelGenesLayout.columnWeights = new double[] {0.1, 0.0, 0.0};
			jPanelGenesLayout.columnWidths = new int[] {30, 7, 7};
			jPanelGenesLayout.rowWeights = new double[] {0.1};
			jPanelGenesLayout.rowHeights = new int[] {5};
			jPanelPaths.setLayout(jPanelGenesLayout);
			jPanel2.add(jPanelPaths, new GridBagConstraints(4, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			jPanelPaths.setBounds(718, 6, 157, 115);
			jPanelPaths.setBorder(BorderFactory.createTitledBorder("View Pathway"));
			{
				jPanelPathways = new JPanel();
				GridBagLayout jPanelRemoveLayout = new GridBagLayout();
				jPanelRemoveLayout.columnWeights = new double[] {0.1, 0.0, 0.1, 0.0, 0.1};
				jPanelRemoveLayout.columnWidths = new int[] {20, 7, 20, 7, 20};
				jPanelRemoveLayout.rowWeights = new double[] {0.1};
				jPanelRemoveLayout.rowHeights = new int[] {5};
				jPanelPathways.setLayout(jPanelRemoveLayout);
				jPanel2.add(jPanelPathways, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelPathways.setBounds(297, 56, 100, 61);
				jPanelPathways.setBorder(BorderFactory.createTitledBorder("Pathway"));
				{
					jButtonRemovePath = new JButton();
					jPanelPathways.add(jButtonRemovePath, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonRemovePath.setText("Remove");
					jButtonRemovePath.setToolTipText("Remove pathway");
					jButtonRemovePath.setPreferredSize(new Dimension(90, 40));
					jButtonRemovePath.setIcon( new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Remove.png"))),0.1).resizeImageIcon());
					jButtonRemovePath.addActionListener( new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {
							//							RemovePathwayFromModel inst = 
							new RemovePathwayFromModel(reactionsContainer){
								private static final long serialVersionUID = 7268015065845897254L;
								public void simpleFinish() {

									this.setVisible(false);
									this.dispose();
								}
								public void finish() {

									this.setVisible(false);
									this.dispose();
									fillList(true, false);
								}
							};
						}
					}
							);
				}
				{
					jButtonColor = new JButton();
					jPanelPathways.add(jButtonColor, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonColor.setText("Colors");
					jButtonColor.setToolTipText("Change Pathways colors");
					jButtonColor.setPreferredSize(new java.awt.Dimension(90, 40));
					jButtonColor.setIcon(new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Synchronize.png"))),0.1).resizeImageIcon());
					jButtonColor.addActionListener(new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {

							reactionsContainer.colorPaths();
							fillList(true, true);
						}});
				}
				{
					jButtonNewPathway = new JButton();
					jPanelPathways.add(jButtonNewPathway, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonNewPathway.setText("New");
					jButtonNewPathway.setToolTipText("Add new Pathway");
					jButtonNewPathway.setPreferredSize(new java.awt.Dimension(90, 40));
					jButtonNewPathway.setIcon(new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Add.png"))),0.1).resizeImageIcon());
					jButtonNewPathway.addActionListener( new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {

							new InsertPathway(reactionsContainer) {

								private static final long serialVersionUID = -2922923291027913821L;
								public void finish() {

									reactionsContainer.setPaths(reactionsContainer.getPathsBoolean(jRadioButtonEncoded.isSelected()));
									setPathwaysComboBox();
									this.setVisible(false);
									this.dispose();
								}
							};
						}
					}
							);
				}
			}
			{
				jPanelReactionsEdition = new JPanel();
				GridBagLayout jPanelInsertEditLayout = new GridBagLayout();
				jPanelInsertEditLayout.columnWeights = new double[] {0.1, 0.0, 0.1, 0.0, 0.1};
				jPanelInsertEditLayout.columnWidths = new int[] {7, 7, 7, 7, 20};
				jPanelInsertEditLayout.rowWeights = new double[] {0.1};
				jPanelInsertEditLayout.rowHeights = new int[] {5};
				jPanelReactionsEdition.setLayout(jPanelInsertEditLayout);
				jPanel2.add(jPanelReactionsEdition, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelReactionsEdition.setBounds(7, 56, 277, 61);
				jPanelReactionsEdition.setBorder(BorderFactory.createTitledBorder("Reactions"));
				{
					jButtonInsert = new JButton();
					jPanelReactionsEdition.add(jButtonInsert, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonInsert.setIcon( new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Upload.png"))),0.1).resizeImageIcon());
					jButtonInsert.setText("Insert");
					jButtonInsert.setToolTipText("Insert");
					jButtonInsert.setPreferredSize(new Dimension(90, 40));
					jButtonInsert.setSize(90, 40);
					jButtonInsert.addActionListener( new ActionListener(){

						@Override
						public void actionPerformed(ActionEvent e) {
							//							InsertEditReaction inst =
							new InsertEditReaction(reactionsContainer,"-10") {

								private static final long serialVersionUID = -3511734775559556146L;
								public void close(){
									this.setVisible(false);
									this.dispose();
								}
								public void closeAndUpdate() {

									if(jTable.getSelectedRow()>-1) {

										//selectedRow=jTable.convertRowIndexToModel(jTable.getSelectedRow());
									}
									removeGapLabelling();
									fillList(true,false);
									this.setVisible(false);
									this.dispose();
								}
							};
						}
					}
							);
				}
				{
					jButtonEdit = new JButton();
					jPanelReactionsEdition.add(jButtonEdit, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonEdit.setText("Edit");
					jButtonEdit.setToolTipText("Edit");
					jButtonEdit.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Edit.png")),0.1).resizeImageIcon());
					jButtonEdit.setPreferredSize(new Dimension(90, 40));
					jButtonEdit.setSize(90, 40);
					jButtonEdit.addActionListener( new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {

							if(jTable.getSelectedRow()>-1) {

								//int selected = jTable.convertRowIndexToModel(jTable.getSelectedRow());
								String rowID;

								if(pathsComboBox.getSelectedIndex()>0) {

									rowID=specificPathwayData.getIds().get(jTable.convertRowIndexToModel(jTable.getSelectedRow()));
								}
								else {

									rowID=reactionsContainer.getIds().get(jTable.convertRowIndexToModel(jTable.getSelectedRow()));
								}

								new InsertEditReaction(reactionsContainer, rowID) {

									private static final long serialVersionUID = -3511734775559556146L;
									public void close()  {

										this.setVisible(false);
										this.dispose();
									}

									public void closeAndUpdate() {

										if(jTable.getSelectedRow()>-1) {

											//selectedRow=jTable.convertRowIndexToModel(jTable.getSelectedRow());
										}
										removeGapLabelling();
										fillList(true,this.inModelNotChanged);
										this.setVisible(false);
										this.dispose();
									}
								};
							}
							else
							{
								Workbench.getInstance().warn("Please Select a Row!");
							}
						}
					}
							);
				}
				{
					jButtonDuplicate = new JButton();
					jPanelReactionsEdition.add(jButtonDuplicate, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonDuplicate.setText("Duplicate");
					jButtonDuplicate.setToolTipText("Duplicate");
					jButtonDuplicate.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Duplicate.png")),0.1).resizeImageIcon());
					jButtonDuplicate.setPreferredSize(new Dimension(90, 40));
					jButtonDuplicate.setSize(90, 40);
					jButtonDuplicate.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent arg0) {

							try {

								String rowID;

								if(jTable.getSelectedRow()!=-1) {

									if(pathsComboBox.getSelectedIndex()>0) {

										rowID=specificPathwayData.getIds().get(jTable.convertRowIndexToModel(jTable.getSelectedRow()));
									}
									else {

										rowID=reactionsContainer.getIds().get(jTable.convertRowIndexToModel(jTable.getSelectedRow()));
									}

									reactionsContainer.duplicateReaction(rowID);
									if(jTable.getSelectedRow()>-1) {

										//selectedRow=jTable.convertRowIndexToModel(jTable.getSelectedRow());
									}
									fillList(true, true);
								}
								else {

									Workbench.getInstance().warn("Please Select a Row to duplicate!");	
								}
							}
							catch (Exception e){e.printStackTrace();}
						}
					});
				}
			}
			{
				jPanelExport = new JPanel();
				GridBagLayout jPanelExportLayout = new GridBagLayout();
				jPanelExportLayout.columnWeights = new double[] {0.1, 0.0, 0.1};
				jPanelExportLayout.columnWidths = new int[] {7, 7, 7};
				jPanelExportLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1};
				jPanelExportLayout.rowHeights = new int[] {20, 20, 20, 20};
				jPanelExport.setLayout(jPanelExportLayout);
				jPanel2.add(jPanelExport, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelExport.setBorder(BorderFactory.createTitledBorder("Export"));
				{
					jButtonExportTxt = new JButton();
					jPanelExport.add(jButtonExportTxt, new GridBagConstraints(2, 0, 1, 3, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonExportTxt.setText("xls tabbed file");
					jButtonExportTxt.setToolTipText("Export to text file (txt)");
					//					jButtonExportTxt.setBounds(11, 8, 118, 48);
					jButtonExportTxt.setIcon(new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Download.png"))),0.1).resizeImageIcon());
					jButtonExportTxt.setPreferredSize(new Dimension(90, 40));
					jButtonExportTxt.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent arg0) {

							JFileChooser fc = new JFileChooser();
							fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							fc.setDialogTitle("Select directory");
							int returnVal = fc.showOpenDialog(new JTextArea());

							if (returnVal == JFileChooser.APPROVE_OPTION) {

								File file = fc.getSelectedFile();
								exportToXls(file.getAbsolutePath());
							}
						}});

				}
				{
					jRadioButtonByPathway = new JRadioButton();
					jRadioButtonByPathway.setSelected(true);
					jPanelExport.add(jRadioButtonByPathway, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jRadioButtonByPathway.setText("By Pathway");
					jRadioButtonByPathway.setToolTipText("By Pathway");
				}
				{
					jRadioButtonReactionsOnly = new JRadioButton();
					jPanelExport.add(jRadioButtonReactionsOnly, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jRadioButtonReactionsOnly.setText("Reactions Only");
					jRadioButtonReactionsOnly.setToolTipText("Reactions Only");
				}
				{
					jRadioButtonGapsOnly = new JRadioButton();
					jRadioButtonGapsOnly.setEnabled(false);
					jPanelExport.add(jRadioButtonGapsOnly, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jRadioButtonGapsOnly.setText("Gap Reactions");
					jRadioButtonGapsOnly.setToolTipText("Gaps Reactions");
				}
				
				{
					jRadioButtonUnbalancedOnly = new JRadioButton();
					jRadioButtonUnbalancedOnly.setEnabled(false);
					jPanelExport.add(jRadioButtonUnbalancedOnly, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jRadioButtonUnbalancedOnly.setText("Unbalanced Reactions");
					jRadioButtonUnbalancedOnly.setToolTipText("Unbalanced Reactions");
				}
			}
			{
				jPanelReactions = new JPanel();
				jPanel2.add(jPanelReactions, new GridBagConstraints(6, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelReactions.setBorder(BorderFactory.createTitledBorder("Reaction"));
				GridBagLayout jPanelReactionsLayout = new GridBagLayout();
				jPanelReactions.setLayout(jPanelReactionsLayout);
				//jPanelReactions.setBounds(718, 6, 157, 115);
				jPanelReactionsLayout.columnWidths = new int[] {7, 7};
				jPanelReactionsLayout.rowHeights = new int[] {7, 7};
				jPanelReactionsLayout.columnWeights = new double[] {0.1, 0.1};
				jPanelReactionsLayout.rowWeights = new double[] {0.1, 0.1};
				{
					jRadioButtonEncoded = new JRadioButton();
					jPanelReactions.add(jRadioButtonEncoded, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jRadioButtonEncoded.setText("In Model");
					jRadioButtonEncoded.setToolTipText("In Model");
					jRadioButtonEncoded.setSelected(this.reactionsContainer.existGenes());
					jRadioButtonEncoded.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent evt) {

							if(!encodedSelected) {

								previousSelectedPathway = pathsComboBox.getSelectedItem().toString();
								fillList(true,false);
								previousSelectedPathway = null;
								encodedSelected=true;
							}
						}		
					});
				}
				{
					jRadioAllReactions = new JRadioButton();
					jPanelReactions.add(jRadioAllReactions, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jRadioAllReactions.setText("All Reactions");
					jRadioAllReactions.setToolTipText("All Reactions");
					jRadioAllReactions.setSelected(!this.reactionsContainer.existGenes());

					jRadioAllReactions.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent evt) {

							if(encodedSelected) {

								previousSelectedPathway = pathsComboBox.getSelectedItem().toString();
								fillList(true,false);
								previousSelectedPathway = null;
								encodedSelected=false;
							}
						}
					});
				}
			}
			this.pathsComboBox = new JComboBox<>(); 
			this.setPathwaysComboBox();
			this.jPanelPaths.add(pathsComboBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

			this.jViewInBrowser = new JButton();
			this.jViewInBrowser.setIcon(new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Web _256.png"))),0.1).resizeImageIcon());
			this.jViewInBrowser.setText("Draw In Browser");
			this.jViewInBrowser.setEnabled(false);
			this.jViewInBrowser.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent evt) {

					if(pathsComboBox.getSelectedIndex()>0 
							&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Transporters Pathway")
							&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Biomass Pathway")
							&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Non enzymatic")
							&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Spontaneous")) {

						String pathway_id = "map"+reactionsContainer.getPathwayCode(pathsComboBox.getSelectedItem().toString());

						String buildQuery = pathway_id;

						List<Set<String>> enzymes_id_list = reactionsContainer.get_enzymes_id_list(reactionsContainer.getPathwayID(pathsComboBox.getSelectedItem().toString()));

						if(enzymes_id_list!= null) {

							for(String enzymes_id : enzymes_id_list.get(0)) {

								buildQuery  = buildQuery.concat("/"+enzymes_id.split("___")[0]+"%09,green");
							}
						}

						List<Set<String>> reactions_id_lists = reactionsContainer.get_reactions_id_list(reactionsContainer.getPathwayID(pathsComboBox.getSelectedItem().toString()));
						
						Set<String> reactions_id_list = reactions_id_lists.get(0);
						Set<String> removed_reactions_id_list = reactions_id_lists.get(1);
						
						if(reactions_id_list!= null) {

							reactions_id_list.addAll(enzymes_id_list.get(1));

							for(String reactions_id : reactions_id_list) {

								if(!reactions_id.startsWith("R_") && (reactions_id.startsWith("R") || reactions_id.startsWith("K")) && reactions_id.length()==6) {

									boolean red = false;

									for(String r : removed_reactions_id_list)
										if(r.equals(reactions_id))
											red=true;

									if(red)
										buildQuery  = buildQuery.concat("/"+reactions_id+"%09,red");
									else
										buildQuery  = buildQuery.concat("/"+reactions_id+"%09,blue");

								}
							}
						}

						if(enzymes_id_list.isEmpty() && reactions_id_list.isEmpty()) {

							Workbench.getInstance().info("Unfortunately the reactions listed as present in this pathway are being catalysed by enzymes\n" +
									"that although being encoded in the genome are not linked to this pathway.\n" +
									"Therefore the KEGG pathway cannot be drawn.");
						}
						else {

							String url="http://www.kegg.jp/pathway/"+buildQuery;
							OpenBrowser  openUrl = new OpenBrowser();
							openUrl.setUrl(url);
							openUrl.openURL();
						}
					}
				}
			});
			this.jPanelPaths.add(jViewInBrowser, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

			this.buttonGroup = new ButtonGroup();
			this.buttonGroup.add(this.jRadioButtonEncoded);
			this.buttonGroup.add(this.jRadioAllReactions);

			this.buttonGroup3 = new ButtonGroup();
			this.buttonGroup3.add(this.jRadioButtonByPathway);
			this.buttonGroup3.add(this.jRadioButtonReactionsOnly);
			this.buttonGroup3.add(this.jRadioButtonGapsOnly);
			this.buttonGroup3.add(this.jRadioButtonUnbalancedOnly);

			this.jPanel2.add(searchInReaction.addPanel(), new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

			this.jPanel1 = new JPanel();
			GridBagLayout thisLayout = new GridBagLayout();
			this.jPanel1.setLayout(thisLayout);
			this.add(this.jPanel1, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			this.jScrollPane1 = new JScrollPane();
			this.jScrollPane1.setViewportView(this.jTable);
			this.jPanel1.add(this.jScrollPane1,new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			this.setPreferredSize(new java.awt.Dimension(887, 713));
		}
		catch (Exception e){e.printStackTrace();}
	}

	/**
	 * @param update
	 * @param keepRow
	 */
	public void fillList(boolean update, boolean keepRow) {

		try {

			int selectedRowFill = -1;

			if(keepRow) {

				if(jTable.getSelectedRow()>-1) {

					selectedRowFill = this.jTable.convertRowIndexToModel(jTable.getSelectedRow());
				}
			}

			this.jTable = new JTable();
			jTable.setAutoCreateRowSorter(true);
			this.searchInReaction.setJTable(jTable);
			this.searchInReaction.setSearchTextField("");

			if(update) {

				this.mainTableData = this.reactionsContainer.getReactionsData(this.jRadioButtonEncoded.isSelected());//, jComplete.isSelected());
				String selectedPathway = (String) this.pathsComboBox.getSelectedItem();
				this.setPathwaysComboBox();
				this.pathsComboBox.setSelectedItem(selectedPathway);
			}

			if(this.pathsComboBox.getSelectedIndex()==0) {

				this.jTable.setModel(this.mainTableData);
				this.searchInReaction.setMainTableData(this.mainTableData);
				this.jTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
					private static final long serialVersionUID = 1L;
					public Component getTableCellRendererComponent(JTable table, Object value, 
							boolean isSelected, boolean hasFocus, int row, int column) {
						super.getTableCellRendererComponent(table, value, 
								isSelected, hasFocus, row, column);
						PathwayReaction qd = (PathwayReaction)mainTableData;

						if(isSelected) {

							setBackground(new Color(237, 240, 242));
							setForeground(Color.BLACK);
						}
						else {
							
							setBackground(reactionsContainer.getPathwayColors().get(new Integer(qd.getRowPathway(jTable.convertRowIndexToModel(row))).intValue()));
						}
						
						if(reactionsContainer.getGapReactions()!=null && reactionsContainer.getGapReactions().contains(jTable.getValueAt(row, REACTION_NAME_COLUMN)))
							setForeground(Color.red);
						else
							setForeground(Color.BLACK);
						
						if(column==REACTION_NAME_COLUMN && reactionsContainer.getBalanceValidator()!= null &&
								!BalanceValidator.CORRECT_TAG_REACTION.equals(reactionsContainer.getBalanceValidator().getReactionTags().get(reactionsContainer.getExternalModelIds().get(jTable.getValueAt(row, REACTION_NAME_COLUMN))))
								&&
								!BalanceValidator.BALANCED_TAG_REACTION.equals(reactionsContainer.getBalanceValidator().getReactionTags().get(reactionsContainer.getExternalModelIds().get(jTable.getValueAt(row, REACTION_NAME_COLUMN))))
								) {
							setFont(new Font(getFont().getFontName(), Font.BOLD + Font.ITALIC, getFont().getSize()));
						}

						return this;             
					}
				});	
			}
			else {

				PathwayReaction spd = (PathwayReaction)mainTableData;
				this.specificPathwayData = spd.getReactionsData(reactionsContainer.getSelectedPathIndexID().get(pathsComboBox.getSelectedIndex()));
				this.jTable.setModel(this.specificPathwayData);
				this.searchInReaction.setMainTableData(this.specificPathwayData);
				this.jTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
					private static final long serialVersionUID = 1L;
					public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
						super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column ); 

						if(isSelected) {

							setBackground(new Color(237, 240, 242));
							setForeground(Color.BLACK);
						}
						else
							setBackground(reactionsContainer.getPathwayColors().get(reactionsContainer.getSelectedPathIndexID().get(pathsComboBox.getSelectedIndex())));

						if(reactionsContainer.getGapReactions()!= null && reactionsContainer.getGapReactions().contains(jTable.getValueAt(row, REACTION_NAME_COLUMN)))
							setForeground(Color.red);
						else 
							setForeground(Color.BLACK);
							
							if(column==REACTION_NAME_COLUMN && reactionsContainer.getBalanceValidator()!= null &&
								!BalanceValidator.CORRECT_TAG_REACTION.equals(reactionsContainer.getBalanceValidator().getReactionTags().get(reactionsContainer.getExternalModelIds().get(jTable.getValueAt(row, REACTION_NAME_COLUMN))))
								&&
								!BalanceValidator.BALANCED_TAG_REACTION.equals(reactionsContainer.getBalanceValidator().getReactionTags().get(reactionsContainer.getExternalModelIds().get(jTable.getValueAt(row, REACTION_NAME_COLUMN))))
								) {
							setFont(new Font(getFont().getFontName(), Font.BOLD + Font.ITALIC, getFont().getSize()));
						}
						
						
						return this;             
					}
				});	
			}

			if(reactionsContainer.getGapReactions()!= null && reactionsContainer.getGapReactions().size()>0)
				this.jRadioButtonGapsOnly.setEnabled(true);
			
			if(reactionsContainer.getBalanceValidator()!=null)
				this.jRadioButtonUnbalancedOnly.setEnabled(true);

			this.buttonColumn =  new ButtonColumn(jTable,0, new ActionListener() {

				public void actionPerformed(ActionEvent arg0){
					processButton(arg0);
				}},
				new MouseAdapter() {

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

			this.tableColumnsSize = this.reactionsContainer.getTableColumnsSize();

			TableColumnModel tc = jTable.getColumnModel();
			tc.getColumn(0).setMaxWidth(35);				//button
			tc.getColumn(0).setResizable(false);
			tc.getColumn(0).setModelIndex(0);

			if(this.tableColumnsSize!=null) {

				for(int i = 0 ; i < tableColumnsSize.length ; i ++) {

					int j = this.tableColumnsSize[i];
					tc.getColumn(i+1).setPreferredWidth(j);
					tc.getColumn(i+1).setMaxWidth(j);
				}
			}

			jTable.getModel().addTableModelListener(new TableModelListener() {

				@Override
				public void tableChanged(TableModelEvent e) {

					if(jTable.getSelectedRow()>-1) {

						//selectedRow=jTable.convertRowIndexToModel(jTable.getSelectedRow());

						if (e.getFirstRow() == e.getLastRow()) {

							int rowNumber = e.getFirstRow();
							int  columnNumber = e.getColumn();

							String id;

							if(pathsComboBox.getSelectedIndex()==0) {

								PathwayReaction qrrt = (PathwayReaction)mainTableData;
								id = qrrt.getRowId(rowNumber);
							}
							else
								id = specificPathwayData.getRowId(rowNumber);


							reactionsContainer.updateReactionProperties(id, columnNumber, jTable.getValueAt(rowNumber, columnNumber));

							if(columnNumber==5 || columnNumber==6)
								fillList(true,true);	

							if(columnNumber>0 && columnNumber<6)
								searchInReaction.setSearchTextField("");

							if(columnNumber==IN_MODEL_COLUMN) 
								removeGapLabelling();
						}
					}
				}});


			//			this.jPanel1.add(this.jScrollPane1,new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			if(selectedRowFill>-1 && selectedRowFill<this.jTable.getRowCount()) {

				this.jTable.setRowSelectionInterval(selectedRowFill, selectedRowFill);
				this.jTable.scrollRectToVisible(this.jTable.getCellRect(selectedRowFill, -1, true));

			}
			else {

				this.jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			}
			this.jScrollPane1.setViewportView(this.jTable);
		}
		catch(Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * set the pathways selection box 
	 */
	private void setPathwaysComboBox() {

		this.pathsComboBox.setModel(new DefaultComboBoxModel<String>(this.reactionsContainer.getPaths()));

		if(this.previousSelectedPathway != null) {

			if(Arrays.asList(reactionsContainer.getPaths()).contains(previousSelectedPathway)) {

				pathsComboBox.setSelectedItem(previousSelectedPathway);
			}
			else {

				pathsComboBox.setSelectedIndex(0);
			}
		}

		int width = (pathsComboBox.getSelectedItem().toString()).length()*7;

		if(width<200) {

			pathsComboBox.setPreferredSize(new Dimension(200, 26));
		}
		else {

			pathsComboBox.setPreferredSize(new Dimension(width,26));
		}

		this.pathsComboBox.setToolTipText(pathsComboBox.getSelectedItem().toString());
		this.pathsComboBox.addItemListener(new ItemListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void itemStateChanged(ItemEvent arg0) {

				if(arg0.getStateChange()==ItemEvent.SELECTED) {

					pathsComboBox.setToolTipText(((JComboBox<String>) arg0.getSource()).getSelectedItem().toString());
					int width = (pathsComboBox.getSelectedItem().toString()).length()*7;

					if(width<200) {

						pathsComboBox.setPreferredSize(new Dimension(200, 26));
					}
					else {

						pathsComboBox.setPreferredSize(new Dimension(width,26));
					}
					pathsComboBox.setToolTipText(pathsComboBox.getSelectedItem().toString());
					fillList(false,false);
				}

				if(pathsComboBox.getSelectedIndex()>0
						&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Transporters Pathway")
						&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Biomass Pathway")
						&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Non enzymatic")
						&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Spontaneous")
						&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("")) {

					jViewInBrowser.setEnabled(true);
				}
				else {

					jViewInBrowser.setEnabled(false);
				}
			}
		});
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

			button = (JButton)((MouseEvent) arg0).getSource();
		}

		ListSelectionModel model = jTable.getSelectionModel();
		model.setSelectionInterval( buttonColumn.getSelectIndex(button), buttonColumn.getSelectIndex(button));

		jButtonClicked();
	}

	/**
	 * 
	 */
	private void jButtonClicked() {

		int row = jTable.convertRowIndexToModel(jTable.getSelectedRow());

		String id;

		if(pathsComboBox.getSelectedIndex()==0) {

			PathwayReaction qrrt = (PathwayReaction)mainTableData;

			id = qrrt.getRowId(row);
		}
		else {

			id = specificPathwayData.getRowId(row);
		}

		DataTable[] informationTable = reactionsContainer.getRowInfo(id);
		new GenericDetailWindow(informationTable, "Reaction data", "Reaction: "+reactionsContainer.getReactionName(id), reactionsContainer.getFormula(id));
	}

	public void removeGapLabelling() {

		this.jTable.setForeground(Color.BLACK);
		this.jRadioButtonGapsOnly.setEnabled(false);
		this.jRadioButtonUnbalancedOnly.setEnabled(false);
		this.reactionsContainer.setExternalModelIds(new HashMap<String,String>());
		this.reactionsContainer.setBalanceValidator(null);
		this.reactionsContainer.setGapReactions(new HashSet<String>());
	}

	/**
	 * 
	 */
	//	private void tableSizes() {
	//
	//		for(int i = 0 ; i < tableColumnsSize.length ; i++) {
	//
	//			tableColumnsSize[i]=jTable.getColumnModel().getColumn(i+1).getWidth();
	//		}
	//
	//		this.selectedRow = this.jTable.convertRowIndexToModel(jTable.getSelectedRow());
	//		this.reactionsContainer.setTableColumnsSize(this.tableColumnsSize);
	//	}

	/**
	 * @param path
	 * 
	 * Export Data to xls tabbed files
	 * 
	 */
	public void exportToXls(String path) {

		String file = System.getProperty("user.home");

		if(!path.equals("")) {

			file=path;
		}

		Calendar cal = new GregorianCalendar();

		// Get the components of the time
		int hour24 = cal.get(Calendar.HOUR_OF_DAY);     // 0..23
		int min = cal.get(Calendar.MINUTE);             // 0..59
		int day = cal.get(Calendar.DAY_OF_YEAR);		//0..365

		file+="/reactions_"+reactionsContainer.getProject().getName()+"_"+hour24+"_"+min+"_"+day+".xls";


		try {

			FileWriter fw = new FileWriter (file);
			BufferedWriter bw = new BufferedWriter(fw);
			Set<String> aux = new HashSet<String>();

			String header ="";
			TableColumnModel tc = jTable.getColumnModel();
			int headerSize = tc.getColumnCount();
			DataTable export;

			int h;
			if(pathsComboBox.getSelectedIndex()==0) {

				export=this.mainTableData;

				if(jRadioButtonByPathway.isSelected()) {

					h = 1; // skip info column
				}
				else {

					h = 2;
				}

			}
			else {

				export=this.specificPathwayData;
				h = 2; // skip info column
			}

			while (h < headerSize) {

				header+=tc.getColumn(h).getHeaderValue().toString()+"\t";
				h++;
			}
			bw.write(header);
			bw.newLine();

			for(int i=0; i < export.getTable().size(); i++) {			

				int j=1;// j=1 because the first column (j=0) is the info column

				String stringBuffer = new String();

				boolean newLine = false;
				while (j < export.getRow(i).length) {

					if(jRadioButtonByPathway.isSelected()) {

						stringBuffer = stringBuffer.concat((export.getRow(i)[j])+"").concat("\t");
						newLine = true;
					}
					else {

						if(j+1 < export.getRow(i).length && !aux.contains(export.getRow(i)[2].toString())) {

							stringBuffer = stringBuffer.concat((export.getRow(i)[j+1])+"").concat("\t");
							newLine = true;
						}
						//	if(j+1 == export.getRow(i).length){bw.write("\n");}
					}
					j++;
				}

				if(newLine) {

					if(this.jRadioButtonGapsOnly.isSelected()) {

						if(reactionsContainer.getGapReactions()!= null && this.reactionsContainer.getGapReactions().contains(export.getRow(i)[2])) {

							bw.write(stringBuffer);
							bw.newLine();
						}
					}
					else if(this.jRadioButtonUnbalancedOnly.isSelected()) {
						
						if(!this.reactionsContainer.getBalanceValidator().getAllBalancedReactions(null).contains(export.getRow(i)[2])) {

							bw.write(stringBuffer);
							bw.newLine();
						}
					}
					else {

						bw.write(stringBuffer);
						bw.newLine();
					}
				}
				aux.add(export.getRow(i)[2].toString());
			}
			bw.flush();
			bw.close();

			Workbench.getInstance().info("Data successfully exported.");
		} 
		catch (Exception e)  {

			Workbench.getInstance().error("An error occurred while performing this operation. Error "+e.getMessage());
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#updateGraphicalObject()
	 */
	@Override
	public void updateTableUI() {

		this.reactionsContainer.setPaths(reactionsContainer.getPathsBoolean(jRadioButtonEncoded.isSelected()));
		this.fillList(true, false);
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
