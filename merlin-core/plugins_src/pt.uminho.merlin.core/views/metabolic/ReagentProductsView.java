package views.metabolic;

import java.awt.Color;
import java.awt.Component;
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

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import utilities.ButtonColumn;
import utilities.CreateImageIcon;
import utilities.SaveToTxt;
import utilities.SearchInTable;
import views.UpdatablePanel;
import views.windows.EquationDetailWindow;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.metabolic.ReagentProducts;
import es.uvigo.ei.aibench.workbench.Workbench;

/**
 * @author ODias
 * 
 */
public class ReagentProductsView extends UpdatablePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane1;
	private JButton jButton1ExportTxt;
	private JPanel jPanel1;
	private JPanel jPanel2;
	private JTable jTable;
	private ReagentProducts reagentProducts;
	private JRadioButton jRadioAllReactions;
	private JRadioButton jRadioButtonEncoded;
	private JPanel jPanelReactions;
	private JPanel jPanel5;
	private JPanel jPanel4;
	// private JTextField searchTextField;
	// private JComboBox typeComboBox;
	private GenericDataTable dataTable;
	private JPanel jPanel3;
	private JRadioButton jRadioButton1;
	private JRadioButton jRadioButton2;
	private JRadioButton jRadioButton3;
	private ButtonGroup buttonGroup1;
	private ButtonGroup buttonGroup2;
	private String selectedRowID;
	private ButtonColumn buttonColumn;
	private SearchInTable searchInTable;

	/**
	 * @param rp
	 */
	public ReagentProductsView(ReagentProducts rp) {
		super(rp);
		this.reagentProducts = rp;
		this.searchInTable = new SearchInTable();
		initGUI();
		fillList(true);
	}

	/**
	 * 
	 */
	private void initGUI() {

		try {

			GridBagLayout jPanel1Layout = new GridBagLayout();
			jPanel1Layout.columnWeights = new double[] { 0.0, 0.1, 0.0 };
			jPanel1Layout.columnWidths = new int[] { 7, 7, 7 };
			jPanel1Layout.rowWeights = new double[] { 0.0, 200.0, 0.0, 0.0, 0.0 };
			jPanel1Layout.rowHeights = new int[] { 7, 50, 7, 3, 7 };
			// jPanel1Layout.columnWeights = new double[] {0.0, 0.1, 0.0};
			// jPanel1Layout.columnWidths = new int[] {7, 7, 7};
			// jPanel1Layout.rowWeights = new double[] {0.0, 3.5, 0.0, 0.1,
			// 0.0};
			// jPanel1Layout.rowHeights = new int[] {5, 25, 5, 5, 5};
			this.setLayout(jPanel1Layout);

			jPanel2 = new JPanel();
			GridBagLayout jPanel2Layout = new GridBagLayout();
			jPanel2Layout.rowWeights = new double[] { 0.1 };
			jPanel2Layout.rowHeights = new int[] { 7 };
			jPanel2Layout.columnWeights = new double[] { 0.1, 0.0, 0.0, 0.0,
					0.0, 0.0, 0.0 };
			jPanel2Layout.columnWidths = new int[] { 50, 7, 6, 7, 6, 7, 6 };
			jPanel2.setLayout(jPanel2Layout);
			this.add(jPanel2, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.5,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));

			jPanel3 = new JPanel();
			jPanel2.add(jPanel3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			GridBagLayout jPanel3Layout = new GridBagLayout();
			jPanel3Layout.rowWeights = new double[] { 0.0, 0.0, 0.0 };
			jPanel3Layout.rowHeights = new int[] { 7, 7, 7 };
			jPanel3Layout.columnWeights = new double[] { 0.1 };
			jPanel3Layout.columnWidths = new int[] { 6 };
			jPanel3.setLayout(jPanel3Layout);
			jPanel3.setBorder(BorderFactory.createTitledBorder("Types"));

			jPanel4 = new JPanel();
			jPanel2.add(jPanel4, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0), 0, 0));
			GridBagLayout searchPanelLayout = new GridBagLayout();
			searchPanelLayout.rowWeights = new double[] { 0.0, 0.0, 0.0 };
			searchPanelLayout.rowHeights = new int[] { 7, 7, 7 };
			searchPanelLayout.columnWeights = new double[] { 0.1 };
			searchPanelLayout.columnWidths = new int[] { 7 };
			jPanel4.setLayout(searchPanelLayout);
			jPanel4.setBorder(BorderFactory.createTitledBorder("Export"));
			jPanel4.setBounds(30, 26, 676, 34);

			{
				jPanel5 = new JPanel();
				GridBagLayout jPanel5Layout = new GridBagLayout();
				jPanel2.add(jPanel5, new GridBagConstraints(0, 0, 1, 1, 0.0,
						0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanel5Layout.rowWeights = new double[] { 0.1 };
				jPanel5Layout.rowHeights = new int[] { 7 };
				jPanel5Layout.columnWeights = new double[] { 0.1, 0.0 };
				jPanel5Layout.columnWidths = new int[] { 15, 3 };
				jPanel5.setBorder(BorderFactory.createTitledBorder("Search"));
				jPanel5.setLayout(jPanel5Layout);
				{
					// searchTextField = new JTextField();
					jPanel5.add(searchInTable.addPanel(),
							new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER,
									GridBagConstraints.HORIZONTAL, new Insets(
											0, 0, 0, 0), 0, 0));
					// searchTextField.setBorder(BorderFactory.createCompoundBorder(
					// BorderFactory.createEtchedBorder(SoftBevelBorder.LOWERED),
					// null)
					// );
					//
					// searchTextField.addKeyListener(new KeyAdapter() {
					// @Override
					// public void keyTyped(KeyEvent evt) {
					// searchInTable(evt);
					// }
					// });

					// ComboBoxModel typeComboBoxModel = new
					// DefaultComboBoxModel(new String[]{"All", "Reagents",
					// "Products"});
					// typeComboBox = new JComboBox();
					// jPanel5.add(typeComboBox, new GridBagConstraints(1, 0, 1,
					// 1, 0.0, 0.0, GridBagConstraints.CENTER,
					// GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					// typeComboBox.setModel(typeComboBoxModel);

					jPanel2.add(jPanel4, new GridBagConstraints(4, 0, 1, 1,
							0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,
							0));
					jPanel4.setBounds(30, 26, 676, 34);
					{
						jButton1ExportTxt = new JButton();
						jPanel4.add(jButton1ExportTxt, new GridBagConstraints(
								0, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER,
								GridBagConstraints.NONE,
								new Insets(0, 0, 0, 0), 0, 0));
						jButton1ExportTxt.setText("text file");
						jButton1ExportTxt
								.setToolTipText("Export to text file (txt)");
						jButton1ExportTxt.setIcon(new CreateImageIcon(
								new ImageIcon((getClass().getClassLoader()
										.getResource("icons/Download.png"))),
								0.1).resizeImageIcon());
						jButton1ExportTxt.setBounds(532, 72, 174, 38);
						jButton1ExportTxt
								.addActionListener(new ActionListener() {

									public void actionPerformed(ActionEvent arg0) {

										try {

											JFileChooser fc = new JFileChooser();
											fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
											fc.setDialogTitle("Select directory");
											int returnVal = fc
													.showOpenDialog(new JTextArea());

											if (returnVal == JFileChooser.APPROVE_OPTION) {

												File file = fc
														.getSelectedFile();
												String filePath = file
														.getAbsolutePath();
												Calendar cal = new GregorianCalendar();

												// Get the components of the
												// time
												int hour24 = cal
														.get(Calendar.HOUR_OF_DAY); // 0..23
												int min = cal
														.get(Calendar.MINUTE); // 0..59
												int day = cal
														.get(Calendar.DAY_OF_YEAR); // 0..365

												filePath += "/"
														+ reagentProducts
																.getName()
														+ "_"
														+ reagentProducts
																.getProject()
																.getName()
														+ "_" + hour24 + "_"
														+ min + "_" + day
														+ ".xls";

												String[][] results = SaveToTxt
														.qrtableToMatrix(dataTable);

												String header = "";
												TableColumnModel tc = jTable
														.getColumnModel();
												int headerSize = tc
														.getColumnCount();
												int i = 0;
												while (i < headerSize) {

													header += tc.getColumn(i)
															.getHeaderValue()
															.toString()
															+ "\t";
													i++;
												}

												SaveToTxt.save_matrix(filePath,
														header.trim(), results,
														reagentProducts
																.getName());
												Workbench
														.getInstance()
														.info("Data successfully exported.");
											}
										} catch (Exception e) {

											Workbench.getInstance().error(
													"An error occurred while performing this operation. Error "
															+ e.getMessage());
											e.printStackTrace();
										}
									}

								});
					}
				}
			}
			{
				jPanelReactions = new JPanel();
				jPanel2.add(jPanelReactions, new GridBagConstraints(6, 0, 1, 1,
						0.0, 0.0, GridBagConstraints.CENTER,
						GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0,
						0));
				GridBagLayout jPanelReactionsLayout = new GridBagLayout();
				jPanelReactionsLayout.columnWidths = new int[] { 7, 7 };
				jPanelReactionsLayout.rowHeights = new int[] { 7, 7 };
				jPanelReactionsLayout.columnWeights = new double[] { 0.1, 0.1 };
				jPanelReactionsLayout.rowWeights = new double[] { 0.1, 0.1 };
				jPanelReactions.setBorder(BorderFactory
						.createTitledBorder("Metabolites"));
				jPanelReactions.setLayout(jPanelReactionsLayout);
				{
					jRadioButtonEncoded = new JRadioButton();
					jPanelReactions.add(jRadioButtonEncoded,
							new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER,
									GridBagConstraints.BOTH, new Insets(0, 0,
											0, 0), 0, 0));
					jRadioButtonEncoded.setText("In Model");
					jRadioButtonEncoded.setToolTipText("In Model");
					jRadioButtonEncoded.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {

							fillList(true);
						}
					});
				}
				{
					jRadioAllReactions = new JRadioButton();
					jPanelReactions.add(jRadioAllReactions,
							new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER,
									GridBagConstraints.BOTH, new Insets(0, 0,
											0, 0), 0, 0));
					jRadioAllReactions.setText("All Metabolites");
					jRadioAllReactions.setToolTipText("All Metabolites");
					jRadioAllReactions.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {

							fillList(false);
						}
					});
				}

				buttonGroup2 = new ButtonGroup();
				buttonGroup2.add(jRadioButtonEncoded);
				jRadioButtonEncoded.setSelected(true);
				buttonGroup2.add(jRadioAllReactions);

			}
			{
				jRadioButton1 = new JRadioButton();
				jPanel3.add(jRadioButton1, new GridBagConstraints(0, 0, 1, 1,
						0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jRadioButton1.setText("All");
			}
			{
				jRadioButton2 = new JRadioButton();
				jPanel3.add(jRadioButton2, new GridBagConstraints(0, 1, 1, 1,
						0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jRadioButton2.setText("Reagents");
				jRadioButton2.setForeground(Color.decode("#008000"));
			}
			{
				jRadioButton3 = new JRadioButton();
				jPanel3.add(jRadioButton3, new GridBagConstraints(0, 2, 1, 1,
						0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jRadioButton3.setText("Products");
				jRadioButton3.setForeground(Color.decode("#0000FF"));
			}

			buttonGroup1 = new ButtonGroup();
			buttonGroup1.add(jRadioButton1);
			jRadioButton1.setSelected(true);
			jRadioButton1.setBounds(17, 22, 124, 18);
			jRadioButton1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					fillList(jRadioButtonEncoded.isSelected());
				}
			});
			buttonGroup1.add(jRadioButton2);
			jRadioButton2.setBounds(17, 45, 124, 18);
			jRadioButton2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					fillList(jRadioButtonEncoded.isSelected());
				}
			});
			buttonGroup1.add(jRadioButton3);
			jRadioButton3.setBounds(17, 70, 124, 18);
			jRadioButton3.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					fillList(jRadioButtonEncoded.isSelected());
				}
			});

			jPanel1 = new JPanel();
			GridBagLayout thisLayout = new GridBagLayout();
			jPanel1.setLayout(thisLayout);
			this.add(jPanel1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			{
				jScrollPane1 = new JScrollPane();
				this.add(jScrollPane1, new GridBagConstraints(1, 1, 1, 1, 0.0,
						0.0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				{
					jTable = new JTable();
					jScrollPane1.setViewportView(jTable);
				}
			}

			this.setPreferredSize(new java.awt.Dimension(887, 713));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void fillList(boolean encoded) {

		// 1 - get ALl
		// 2 - get Reagents
		// 3 - get Products
		
		jTable = new JTable();
		jScrollPane1.setViewportView(jTable);

		if (jRadioButton1.isSelected()) {

			dataTable = this.reagentProducts.getDataReagentProduct(1, encoded);
		}
		else if (jRadioButton2.isSelected()) {

			dataTable = this.reagentProducts.getDataReagentProduct(2, encoded);
		}
		else {

			dataTable = this.reagentProducts.getDataReagentProduct(3, encoded);
		}

		jTable.setModel(dataTable);
		jTable.setAutoCreateRowSorter(true);
		this.searchInTable.setJTable(jTable);
		this.searchInTable.setMainTableData(dataTable);
		if (!searchInTable.getSearchTextField().getText().trim().equals("")) {

			searchInTable.refreshSearch();
		}

		jTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

			private static final long serialVersionUID = 5992139213095950691L;

			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {

				int modelRow = jTable.convertRowIndexToModel(row);
				super.getTableCellRendererComponent(table, value, isSelected,
						hasFocus, modelRow, column);

				GenericDataTable dtt = (GenericDataTable) jTable.getModel();

				if (isSelected) {

					setForeground(Color.decode("#DCDCDC"));
					setBackground(Color.decode("#696969"));
				} else {

					if (dtt.getRowType(modelRow).equals("Reagent")) {

						setBackground(Color.decode("#90EE90"));
					} else if (dtt.getRowType(modelRow).equals("Product")) {
						setBackground(Color.decode("#87CEEB"));
					} else {
						setBackground(Color.decode("#778899"));
					}
					setForeground(Color.decode("#000000"));
				}
				return this;
			}
		});

		// searchTextField.setText("");

		buttonColumn = new ButtonColumn(jTable, 0, new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

				processButton(arg0);
			}
		}, new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				// {
				// get the coordinates of the mouse click
				Point p = e.getPoint();

				// get the row index that contains that coordinate
				int rowNumber = jTable.rowAtPoint(p);
				int columnNumber = jTable.columnAtPoint(p);
				jTable.setColumnSelectionInterval(columnNumber, columnNumber);
				// Get the ListSelectionModel of the JTable
				ListSelectionModel model = jTable.getSelectionModel();
				// set the selected interval of rows. Using the "rowNumber"
				// variable for the beginning and end selects only that one row.
				model.setSelectionInterval(rowNumber, rowNumber);
				processButton(e);
			}
		});
		TableColumnModel tc = jTable.getColumnModel();
		tc.getColumn(0).setMaxWidth(35); // button
		tc.getColumn(0).setResizable(false);
		tc.getColumn(0).setModelIndex(0);
		
		this.searchInTable.setSearchTextField("");
	}

	/**
	 * @param arg0
	 */
	private void processButton(EventObject arg0) {

		JButton button = null;

		if (arg0.getClass() == ActionEvent.class) {

			button = (JButton) ((ActionEvent) arg0).getSource();
		}

		if (arg0.getClass() == MouseEvent.class) {

			button = (JButton) ((ActionEvent) arg0).getSource();
		}

		ListSelectionModel model = jTable.getSelectionModel();
		model.setSelectionInterval(buttonColumn.getSelectIndex(button),
				buttonColumn.getSelectIndex(button));

		selectedRowID = dataTable.getRowId(jTable.convertRowIndexToModel(jTable.getSelectedRow()));

		DataTable table = reagentProducts.getReaction(selectedRowID, (String) jTable.getValueAt(jTable.getSelectedRow(), 2));

		new EquationDetailWindow(table, (String) dataTable.getValueAt(
				jTable.convertRowIndexToModel(jTable.getSelectedRow()), 1));

	}

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#updateGraphicalObject()
	 */
	@Override
	public void updateTableUI() {

		this.fillList(this.jRadioButtonEncoded.isSelected());
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
