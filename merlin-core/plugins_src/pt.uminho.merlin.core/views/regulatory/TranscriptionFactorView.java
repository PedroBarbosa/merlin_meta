package views.regulatory;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import utilities.SaveToTxt;
import datatypes.DataTable;
import datatypes.regulatory.TranscriptionFactor;

public class TranscriptionFactorView extends javax.swing.JPanel {

	private static final long serialVersionUID = 7348937284724896584L;
	private JScrollPane jScrollPane1;
	private JPanel jPanel1;
	private JPanel jPanel2;
	private JTable jTable1;
	private TranscriptionFactor t;
	private JPanel jPanel4;
	private JTextField searchTextField;
	private JButton jButton1ExportTxt;
	private JComboBox searchComboBox = new JComboBox();
	private DataTable querydata;
	

	public TranscriptionFactorView(TranscriptionFactor t) {
		super();
		this.t = t;
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
			{
				jPanel4 = new JPanel();
				GridBagLayout seachPanelLayout = new GridBagLayout();
				seachPanelLayout.rowWeights = new double[] {0.0, 0.0};
				seachPanelLayout.rowHeights = new int[] {22};
				seachPanelLayout.columnWeights = new double[] {0.1, 0.0};
				seachPanelLayout.columnWidths = new int[] {100, 7};
				jPanel4.setLayout(seachPanelLayout);
				

				searchTextField = new JTextField();
				jPanel4.add(searchTextField, new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				searchTextField.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEtchedBorder(BevelBorder.LOWERED), null)
				);
				
				searchTextField.addKeyListener(new KeyAdapter() {
					@Override
					public void keyTyped(KeyEvent evt) {
						searchInTable(evt);
					}
				});
				
				ComboBoxModel searchComboBoxModel = new DefaultComboBoxModel(
						new String[] { "Name", "Encoding genes", "All" });
				searchComboBox = new JComboBox();
				jPanel4.add(searchComboBox, 
					new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, 
					GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0)
				);
				searchComboBox.setModel(searchComboBoxModel);
				
				
				jPanel2.add(jPanel4);
				jPanel4.setBounds(30, 26, 676, 34);
			}
			{
				jButton1ExportTxt = new JButton();
				jPanel2.add(jButton1ExportTxt);
				jButton1ExportTxt.setText("Export to Txt");
				jButton1ExportTxt.setToolTipText("Export to text file (txt)");
				jButton1ExportTxt.setIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/export.png")));
				jButton1ExportTxt.setBounds(557, 72, 149, 38);
				jButton1ExportTxt.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent arg0) 
					{
						String[][] results = SaveToTxt.qrtableToMatrix(querydata);
						String file = "C:\\TFs.txt"; 
						try {
							//SaveToTxt.save_matrix(file, results, "Transcription Factors");
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
			jPanel1.add(jScrollPane1, gbc);
			
			this.setPreferredSize(new java.awt.Dimension(887, 713));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void fillList()
	{
		try {
			querydata = this.t.getAllTFs();

			jTable1.setModel(querydata);
			
			searchTextField.setText("");
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		
		switch(this.searchComboBox.getSelectedIndex())
		{
			case 0:
			{
				for(int z=0;z<tab.size();z++)
				{
					Object[] subtab = tab.get(i);
					if(((String)subtab[0]).contains(text))
						rows.add(new Integer(i));
					i++;
				}
				break;
			}
			case 1:
			{
				for(int z=0;z<tab.size();z++)
				{
					Object[] subtab = tab.get(i);
					if(((String)subtab[1]).contains(text))
						rows.add(new Integer(i));
					i++;
				}
				break;
			}
			default:
			{
				for(int z=0;z<tab.size();z++)
				{
					Object[] subtab = tab.get(i);
					if(((String)subtab[0]).contains(text) || ((String)subtab[1]).contains(text))
						rows.add(new Integer(i));
					i++;
				}
				break;
			}
				
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
}
