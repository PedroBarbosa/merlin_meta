package views.metabolic;
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
import javax.swing.table.TableColumnModel;

import utilities.ButtonColumn;
import utilities.CreateImageIcon;
import utilities.SaveToTxt;
import utilities.SearchInTable;
import views.UpdatablePanel;
import views.windows.GenericDetailWindow;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.metabolic.EnzymesContainer;
import es.uvigo.ei.aibench.workbench.Workbench;


/**
 * @author ODias
 *
 */
public class EnzymeView extends UpdatablePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane1;
	private JPanel jPanel1;
	private JPanel jPanel2;
	private JTable jTable;
	private EnzymesContainer enzymesContainer;
	private GenericDataTable mainTableData;
	private JPanel jPanel4;
	private JButton jButton1ExportTxt;
	private JPanel jPanelReactions;
	private JRadioButton jRadioButtonEncoded;
	private JRadioButton jRadioButton1;
	private SearchInTable searchInEnzyme;
	private ButtonColumn buttonColumn;

	/**
	 * @param enzymesContainer
	 */
	public EnzymeView(EnzymesContainer enzymesContainer) {

		super(enzymesContainer);
		this.enzymesContainer = enzymesContainer;
		this.searchInEnzyme = new SearchInTable();
		initGUI();
		fillList();
	}

	/**
	 * 
	 */
	private void initGUI() {

		GridBagLayout jPanel1Layout = new GridBagLayout();
		jPanel1Layout.rowWeights = new double[] {0.0, 3.5, 0.0, 0.1, 0.0};
		jPanel1Layout.rowHeights = new int[] {7, 7, 7, 7, 7};
		jPanel1Layout.columnWeights = new double[] {0.0, 0.1, 0.0};
		jPanel1Layout.columnWidths = new int[] {7, 7, 7};
		this.setLayout(jPanel1Layout);

		jPanel2 = new JPanel();
		GridBagLayout jPanel2Layout = new GridBagLayout();
		this.add(jPanel2, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		jPanel2Layout.rowWeights = new double[] {};
		jPanel2Layout.rowHeights = new int[] {};
		jPanel2Layout.columnWeights = new double[] {0.1, 0.0, 0.0, 0.0, 0.0};
		jPanel2Layout.columnWidths = new int[] {7, 7, 7, 7, 7};
		jPanel2.setLayout(jPanel2Layout);
		{
			jPanel2.add(searchInEnzyme.addPanel(), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			jPanel4 = new JPanel();
			jPanel2.add(jPanel4, new GridBagConstraints(2, -1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			GridBagLayout searchPanelLayout = new GridBagLayout();
			searchPanelLayout.rowWeights = new double[] {0.0};
			searchPanelLayout.rowHeights = new int[] {22};
			searchPanelLayout.columnWeights = new double[] {0.1};
			searchPanelLayout.columnWidths = new int[] {100};
			jPanel4.setLayout(searchPanelLayout);
			jPanel4.setBorder(BorderFactory.createTitledBorder("Export"));
			jPanel4.setBounds(30, 26, 676, 34);
			{
				jButton1ExportTxt = new JButton();
				jPanel4.add(jButton1ExportTxt, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jButton1ExportTxt.setText("text file");
				jButton1ExportTxt.setToolTipText("Export to text file (txt)");
				jButton1ExportTxt.setIcon(new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Download.png"))),0.1).resizeImageIcon());
				jButton1ExportTxt.setBounds(532, 72, 174, 38);
				jButton1ExportTxt.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0)  {

						try  {

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

								filePath += "/"+enzymesContainer.getName()+"_"+enzymesContainer.getProject().getName()+"_"+hour24+"_"+min+"_"+day+".xls";

								String[][] results = SaveToTxt.qrtableToMatrix(mainTableData);

								String header ="";
								TableColumnModel tc = jTable.getColumnModel();
								int headerSize = tc.getColumnCount();
								int i = 0;
								while (i < headerSize) {

									header+=tc.getColumn(i).getHeaderValue().toString()+"\t";
									i++;
								}
								SaveToTxt.save_matrix(filePath, header.trim(), results, enzymesContainer.getName());
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
			jPanelReactions = new JPanel();
			jPanel2.add(jPanelReactions, new GridBagConstraints(4, -1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			jPanelReactions.setBorder(BorderFactory.createTitledBorder("Proteins"));
			GridBagLayout jPanelReactionsLayout = new GridBagLayout();
			jPanelReactions.setLayout(jPanelReactionsLayout);
			jPanelReactions.setBounds(718, 6, 157, 115);
			jPanelReactionsLayout.columnWidths = new int[] {7, -1};
			jPanelReactionsLayout.rowHeights = new int[] {-1, 7};
			jPanelReactionsLayout.columnWeights = new double[] {0.1, 0.0};
			jPanelReactionsLayout.rowWeights = new double[] {0.0, 0.1};
			{
				jRadioButtonEncoded = new JRadioButton();
				jPanelReactions.add(jRadioButtonEncoded, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jRadioButtonEncoded.setText("Encoded");
				jRadioButtonEncoded.setToolTipText("Encoded");
				jRadioButtonEncoded.setSelected(true);
				jRadioButtonEncoded.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {fillList();}});
			}
			{
				jRadioButton1 = new JRadioButton();
				jPanelReactions.add(jRadioButton1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jRadioButton1.setText("All Enzymes");
				jRadioButton1.setToolTipText("All Enzymes");
				jRadioButton1.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {fillList();}});
			}
			ButtonGroup buttonGroup = new ButtonGroup();
			buttonGroup.add(jRadioButtonEncoded);
			buttonGroup.add(jRadioButton1);
		}

		try {

			jPanel1 = new JPanel();
			GridBagLayout thisLayout = new GridBagLayout();
			thisLayout.rowWeights = new double[] {0.0, 0.1, 0.0};
			thisLayout.rowHeights = new int[] {7, 7, 7};
			thisLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
			thisLayout.columnWidths = new int[] {7, 7, 7};
			jPanel1.setLayout(thisLayout);
			this.add(jPanel1, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			jScrollPane1 = new JScrollPane();
			jTable = new JTable();
			jScrollPane1.setViewportView(jTable);
			jPanel1.add(jScrollPane1, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			this.setPreferredSize(new java.awt.Dimension(887, 713));
		}
		catch(Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void fillList() {

		try {
			
			jTable = new JTable();
			jScrollPane1.setViewportView(jTable);

			mainTableData = this.enzymesContainer.getAllEnzymes(this.jRadioButtonEncoded.isSelected());
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
			searchInEnzyme.setJTable(jTable);
			searchInEnzyme.setMainTableData(mainTableData);
			this.searchInEnzyme.setSearchTextField("");

		}
		catch(Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * @param arg0
	 */
	private void processButton(EventObject arg0){

		JButton button = null;

		if(arg0.getClass()==ActionEvent.class) {

			button = (JButton)((ActionEvent) arg0).getSource();
		}

		if(arg0.getClass()==MouseEvent.class) {

			button = (JButton)((ActionEvent) arg0).getSource();
		}

		ListSelectionModel model = jTable.getSelectionModel();
		model.setSelectionInterval( buttonColumn.getSelectIndex(button), buttonColumn.getSelectIndex(button));

		String selectedRowID = mainTableData.getRowId(jTable.convertRowIndexToModel(jTable.getSelectedRow()));
		String ecnumber = (String) jTable.getValueAt(jTable.getSelectedRow(), 2);
		DataTable[] q = enzymesContainer.getRowInfo(ecnumber,selectedRowID);

		new GenericDetailWindow(q, "Enzyme info", "Protein: "+jTable.getValueAt(jTable.getSelectedRow(), 1));
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
