package views.regulatory;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import utilities.SaveToTxt;
import views.windows.GenericDetailWindow;
import datatypes.DataTable;
import datatypes.GenericDataTable;
import datatypes.regulatory.TranscriptionUnit;

public class TranscriptionUnitView extends javax.swing.JPanel {

	private static final long serialVersionUID = 7348937284724896584L;
	private JScrollPane jScrollPane1;
	private JButton jButton1ExportTxt;
	private JRadioButton jRadioButton2;
	private JRadioButton jRadioButton1;
	private ButtonGroup buttonGroup1;
	private JPanel jPanel1;
	private JPanel jPanel2;
	private JPanel jPanel3;
	private JTable jTable1;
	private TranscriptionUnit e;
	private JPanel jPanel4;
	private JTextField searchTextField;
	private JComboBox searchComboBox = new JComboBox();
	private GenericDataTable querydata;
	private HashMap<Integer,Integer[]> index;


	public TranscriptionUnitView(TranscriptionUnit e) {
		super();
		this.e = e;
		this.index = e.getSearchData();
		initGUI();
		fillList();
	}

	private void initGUI() {
		try {
			GridBagLayout jPanel1Layout = new GridBagLayout();
			jPanel1Layout.columnWeights = new double[] {0.0, 0.1, 0.0};
			jPanel1Layout.columnWidths = new int[] {7, 7, 7};
			jPanel1Layout.rowWeights = new double[] {0.0, 200.0, 0.0, 0.0, 0.0};
			jPanel1Layout.rowHeights = new int[] {7, 50, 7, 3, 7};
			this.setLayout(jPanel1Layout);

			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;

			c.weightx = 1.0;
			c.weighty = 0.5;
			c.gridx = 0;
			c.gridy = 1;

			jPanel2 = new JPanel();
			jPanel2.setLayout(null);
			this.add(jPanel2, c);
			this.add(jPanel2, c);
			{
				jPanel3 = new JPanel();
				jPanel2.add(jPanel3);
				jPanel3.setBounds(718, 12, 157, 115);
				jPanel3.setBorder(BorderFactory.createTitledBorder("Sort By"));
				jPanel3.setLayout(null);
				{
					jRadioButton1 = new JRadioButton();
					jPanel3.add(jRadioButton1);
					jRadioButton1.setText("Genes");
				}
				{
					jRadioButton2 = new JRadioButton();
					jPanel3.add(jRadioButton2);
					jRadioButton2.setText("Promoters");
				}
				buttonGroup1 = new ButtonGroup();
				buttonGroup1.add(jRadioButton1);
				jRadioButton1.setSelected(true);
				jRadioButton1.setBounds(17, 22, 124, 18);
				jRadioButton1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						fillList();
					}
				});
				buttonGroup1.add(jRadioButton2);
				jRadioButton2.setBounds(17, 45, 124, 18);
				jRadioButton2.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						fillList();
					}
				});
			}

			jPanel4 = new JPanel();
			GridBagLayout seachPanelLayout = new GridBagLayout();
			seachPanelLayout.rowWeights = new double[] {0.0, 0.0};
			seachPanelLayout.rowHeights = new int[] {22};
			seachPanelLayout.columnWeights = new double[] {0.1, 0.0};
			seachPanelLayout.columnWidths = new int[] {100, 7};
			jPanel4.setLayout(seachPanelLayout);

			searchTextField = new JTextField();
			jPanel4.add(searchTextField, 
					new GridBagConstraints(
							0, 0, 1, 2, 0.0, 0.0, 
							GridBagConstraints.CENTER, GridBagConstraints.BOTH, 
							new Insets(0, 0, 0, 0), 0, 0)
					);
			searchTextField.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEtchedBorder(BevelBorder.LOWERED), null)
					);

			searchTextField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent evt) {
					searchInTable(evt);
				}
			});

			ComboBoxModel searchComboBoxModel = new DefaultComboBoxModel(this.e.getSearchDataIds());
			searchComboBox = new JComboBox();
			jPanel4.add(searchComboBox, 
					new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, 
							GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0)
					);
			searchComboBox.setModel(searchComboBoxModel);

			jPanel2.add(jPanel4);
			jPanel4.setBounds(30, 26, 676, 34);
			{
				jButton1ExportTxt = new JButton();
				jPanel2.add(jButton1ExportTxt);
				jButton1ExportTxt.setText("Export to Txt");
				jButton1ExportTxt.setToolTipText("Export to text file (txt)");
				jButton1ExportTxt.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/export.png")));
				jButton1ExportTxt.setBounds(547, 72, 159, 38);
				jButton1ExportTxt.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0) 
					{
						String[][] results = SaveToTxt.qrtableToMatrix(querydata);
						String file = "C:\\TUs.txt"; 
						try {
							//SaveToTxt.save_matrix(file, results, "Transcription Units");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				});
			}

			c.weightx = 1.0;
			c.weighty = 0.5;
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

			jScrollPane1 = new JScrollPane();
			jTable1 = new JTable();
			jScrollPane1.setViewportView(jTable1);
			jTable1.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent evt) {
					jTable1MouseClicked(evt);
				}
			});
			jPanel1.add(jScrollPane1, gbc);

			this.setPreferredSize(new java.awt.Dimension(887, 713));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fillList()
	{
		try {
			if(this.jRadioButton1.isSelected()) this.e.setSort(1);
			else if(this.jRadioButton2.isSelected()) this.e.setSort(2);

			querydata = this.e.getData();

			jTable1.setModel(querydata);

			searchTextField.setText("");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected boolean checkData(Integer[] zag, Object[] subtab, String text)
	{
		if(subtab[zag[0].intValue()]==null) return false;

		boolean res = ((String)subtab[zag[0].intValue()]).contains(text);
		for(int z=1;z<zag.length;z++)
		{
			res = res || ((String)subtab[zag[z].intValue()]).contains(text);
		}
		return res;
	}

	@SuppressWarnings("static-access")
	public void searchInTable(KeyEvent evt){

		String text;
		ArrayList<Integer> rows = new ArrayList<Integer>();
		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();

		if(searchTextField.getText().compareTo("")!=0 && evt.getKeyChar() == KeyEvent.VK_BACK_SPACE)
			text = searchTextField.getText();
		else
			text = searchTextField.getText()+evt.getKeyChar();

		int i=0;
		ArrayList<Object[]> tab = this.querydata.getTable();

		Integer[] zag = this.index.get(searchComboBox.getSelectedIndex());

		for(int z=0;z<tab.size();z++)
		{
			Object[] subtab = tab.get(i);
			if(checkData(zag, subtab, text))
				rows.add(new Integer(i));
			i++;
		}

		int row = 0;
		for(Integer r: rows)
		{
			row = r.intValue();
			selectionModel.addSelectionInterval(row, row);
		}

		this.jTable1.setSelectionMode(selectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.jTable1.setSelectionModel(selectionModel);
		if(selectionModel.isSelectionEmpty()&& (this.searchTextField.getText().compareTo("")!=0))
		{
			this.searchTextField.setForeground(new java.awt.Color(255,0,0));
			searchTextField.setBackground(new java.awt.Color(174,174,174));
		}
		else
		{
			this.searchTextField.setForeground(Color.BLACK);
			this.searchTextField.setBackground(Color.WHITE);
		}

		this.jTable1.scrollRectToVisible(this.jTable1.getCellRect(row, 0, true));

	}

	private void jTable1MouseClicked(MouseEvent evt) {

		if(e.hasWindow()) {
			
			SwingUtilities.invokeLater(
					new Runnable() {
						public void run() {

							int row = jTable1.getSelectedRow();

							String id = querydata.getRowId(row);

							try {
								DataTable[] q = e.getRowInfo(id);

								new GenericDetailWindow(q, querydata.getWindowName(), e.getSingular() + e.getName(id));
							} 
							catch(Exception e) {

								e.printStackTrace();
							}
						}
					}
					);
		}
	}



	public String getSingular()
	{
		return "Metabolite: ";
	}
}
