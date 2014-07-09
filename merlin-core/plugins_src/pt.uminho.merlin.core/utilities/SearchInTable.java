package utilities;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import datatypes.DataTable;
import es.uvigo.ei.aibench.workbench.Workbench;

/**
 * @author ODias
 *
 */
public class SearchInTable {

	private JComboBox<String> searchComboBox;
	private JTextField jTextFieldTotal, jTextFieldResult, searchTextField;
	private JTable jTable;
	private DataTable mainTableData;
	private int presentRow;
	private  List<Integer> rowsList;
	private JButton jButtonPrevious;
	private JButton jButtonNext;
	private JLabel jLabel1;

	/**
	 * 
	 */
	public SearchInTable () {

		searchTextField = new JTextField();
	}

	/**
	 * @param text
	 */
	public void searchInTable(String text) {

		text=text.toLowerCase();
		rowsList = new ArrayList<Integer>();
		Set<Integer> rows = new TreeSet<Integer>();

		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
		//		int i=0;
		presentRow = 0;
		ArrayList<Object[]> tab = mainTableData.getTable();
		jTable.setAutoCreateRowSorter(true);

		switch(searchComboBox.getSelectedIndex())
		{
		case 0:
		{
			for(int z=0;z<tab.size();z++) {

				Object[] subtab = tab.get(z);
				if((String)subtab[1]!=null && ((String)subtab[1]).toLowerCase().contains(text)) {

					int modelRow = jTable.getRowSorter().convertRowIndexToView(new Integer(z));
					rows.add(modelRow);
				}
			}
			break;
		}
		case 1:
		{
			for(int z=0;z<tab.size();z++) {

				Object[] subtab = tab.get(z);

				for(Object obj:subtab) {

					if(obj != null && obj.getClass().equals(String.class)) {

						if(((String)obj).toLowerCase().contains(text)) {

							int modelRow = jTable.getRowSorter().convertRowIndexToView(new Integer(z));
							rows.add(modelRow);
						}
					}

					List<String> found = new ArrayList<String>();

					if(obj != null && obj.getClass().equals(String[].class)) {

						found.addAll(Arrays.asList(((String[])obj)));

						for(String s: found) {

							if(s.toLowerCase().contains(text)) {

								int modelRow = jTable.getRowSorter().convertRowIndexToView(new Integer(z));
								rows.add(modelRow);
							}
						}
					}
				}
			}
			break;
		}
		default:
		{
			for(int z=0;z<tab.size();z++) {

				Object[] subtab = tab.get(z);
				if(((String) subtab[1]).toLowerCase().contains(text)) {

					int modelRow = jTable.getRowSorter().convertRowIndexToView(new Integer(z));
					rows.add(modelRow);
				}
			}
			break;
		}

		}
		rowsList.addAll(rows);

		int row = 0;
		for(Integer r: rowsList) {

			row = r.intValue();
			selectionModel.addSelectionInterval(row, row);
		}

		jTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jTable.setSelectionModel(selectionModel);

		if(selectionModel.isSelectionEmpty() && (searchTextField.getText().compareTo("")!=0)) {

			searchTextField.setForeground(new java.awt.Color(255,0,0));
			searchTextField.setBackground(new java.awt.Color(174,174,174));
			jTextFieldResult.setText("");
			jTextFieldTotal.setText("");
			rowsList = new ArrayList<Integer>();
		}
		else {

			searchTextField.setForeground(Color.BLACK);
			searchTextField.setBackground(Color.WHITE);
		}

		if(rowsList.size()!=0) {

			jTextFieldResult.setText(""+1);
			jTextFieldTotal.setText(""+rowsList.size());
			jTable.scrollRectToVisible(jTable.getCellRect(rowsList.get(0), 0, true));
		}
		else {

			//this.setSearchTextField("");
		}
	}

	/**
	 * @param evt
	 */
	public void searchInTable(KeyEvent evt) {

		String text;
		//		ArrayList<Integer> rows = new ArrayList<Integer>();
		//		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();

		if(searchTextField.getText().compareTo("")!=0 && evt.getKeyChar() == KeyEvent.VK_BACK_SPACE) {

			text = searchTextField.getText();
		}
		else {

			text = searchTextField.getText()+evt.getKeyChar();
		}
		searchInTable(text);
	}

	/**
	 * @return
	 */
	public JPanel addPanel() {

		JPanel jPanelSearchBox = new JPanel();
		GridBagLayout jPanel3Layout = new GridBagLayout();
		jPanelSearchBox.setBorder(BorderFactory.createTitledBorder("Search"));
		jPanel3Layout.rowWeights = new double[] {0.0};
		jPanel3Layout.rowHeights = new int[] {3};
		jPanel3Layout.columnWeights = new double[] {1.1, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.0, 0.1};
		jPanel3Layout.columnWidths = new int[] {100, 20, 7, 7, 3, 3, 7, 6, 3, 6};
		jPanelSearchBox.setLayout(jPanel3Layout);
		{
			jButtonPrevious = new JButton();
			jButtonPrevious .setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Previous.png")),0.05).resizeImageIcon());
			jPanelSearchBox.add(jButtonPrevious, new GridBagConstraints(4, -1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jButtonPrevious.setToolTipText("Previous");
			jButtonPrevious.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0){

					if(rowsList.size()>0) {

						if(presentRow!=0) {

							presentRow-=1;
						}
						else {

							presentRow = rowsList.size()-1;
						}
						jTextFieldResult.setText(""+(presentRow+1));
						jTable.setRowSelectionInterval(rowsList.get(presentRow), rowsList.get(presentRow));
						jTable.scrollRectToVisible(jTable.getCellRect(rowsList.get(presentRow), 0, true));
					}
				}});
		}
		{
			jButtonNext = new JButton();
			jButtonNext .setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Next.png")),0.05).resizeImageIcon());
			jPanelSearchBox.add(jButtonNext, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jButtonNext.setToolTipText("Next");
			jButtonNext.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {

					if(rowsList.size()>0) {

						if(presentRow!=rowsList.size()-1) {

							presentRow+=1;
							jTextFieldResult.setText(""+(presentRow+1));
							jTable.setRowSelectionInterval(rowsList.get(presentRow), rowsList.get(presentRow));
							jTable.scrollRectToVisible(jTable.getCellRect(rowsList.get(presentRow), 0, true));
						}
						else {

							if(rowsList.size()>1) {

								presentRow=0;
								jTextFieldResult.setText(""+(presentRow+1));
								jTable.setRowSelectionInterval(rowsList.get(presentRow), rowsList.get(presentRow));
								jTable.scrollRectToVisible(jTable.getCellRect(rowsList.get(presentRow), 0, true));
								Workbench.getInstance().info("The end was reached!\n Starting from the top.");
							}
						}
					}
				}});
		}
		{
			jTextFieldResult = new JTextField();
			jTextFieldResult.setEditable(false);
			jPanelSearchBox.add(jTextFieldResult, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			jLabel1 = new JLabel();
			jPanelSearchBox.add(jLabel1, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			jLabel1.setText("of");
			jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel1.setHorizontalTextPosition(SwingConstants.CENTER);
		}
		{
			jTextFieldTotal = new JTextField();
			jTextFieldTotal.setEditable(false);
			jPanelSearchBox.add(jTextFieldTotal, new GridBagConstraints(9, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		{

			searchTextField.setBounds(14, 12, 604, 20);
			searchTextField.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEtchedBorder(BevelBorder.LOWERED), null)
					);

			searchTextField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent evt) {
					searchInTable(evt);
				}
			});

			ComboBoxModel<String> searchComboBoxModel = new DefaultComboBoxModel<>(
					new String[] { "Name", "All" });
			searchComboBox = new JComboBox<>();
			jPanelSearchBox.add(searchComboBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			jPanelSearchBox.add(searchTextField, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
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
		return jPanelSearchBox;
	}

	/**
	 * @return the searchTextField
	 */
	public JTextField getSearchTextField() {

		return searchTextField;
	}

	/**
	 * 
	 */
	public void refreshSearch() {

		searchInTable(searchTextField.getText());
	}

	/**
	 * @param searchTextField the string to set
	 */
	public void setSearchTextField(String searchTextField) {

		if(searchTextField.isEmpty()) {

			jTextFieldResult.setText("");
			jTextFieldTotal.setText("");
			jTextFieldResult.setToolTipText("");
			jTextFieldTotal.setToolTipText("");
			rowsList = new ArrayList<Integer>();
		}

		this.searchTextField.setText(searchTextField);
		this.jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		if(this.jTable.getSelectedRow()>-1) {

			this.jTable.setRowSelectionInterval(jTable.getSelectedRow(), jTable.getSelectedRow());
			this.jTable.scrollRectToVisible(this.jTable.getCellRect(jTable.getSelectedRow(), -1, true));
		}


	}

	public void setMainTableData(DataTable mainTableData) {
		this.mainTableData = mainTableData;

	}

	public void setJTable(JTable jTable) {
		this.jTable = jTable;

	}
}
