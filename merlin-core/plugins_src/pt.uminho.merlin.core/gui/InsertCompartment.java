package gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import utilities.CreateImageIcon;
import datatypes.metabolic.ReactionsContainer;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;


/**
 * @author ODias
 *
 */
public class InsertCompartment extends JDialog  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jPanel1;
	private JTextField jTextField1;
	private JButton jButton1;
	private JButton jButton2;
	private Connection connection;

	public InsertCompartment(ReactionsContainer reactionsContainer) {
		
		super(Workbench.getInstance().getMainFrame());
		this.connection = reactionsContainer.getConnection();
		initGUI("New Compartment");
		Utilities.centerOnOwner(this);
		this.setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
		this.setVisible(true);		
		this.setAlwaysOnTop(true);
		this.toFront();
	}


	/**
	 * @param windowName
	 */
	private void initGUI(String windowName) {
		
		this.setTitle(windowName);
		jPanel1 = new JPanel();
		getContentPane().add(jPanel1, BorderLayout.NORTH);
		GridBagLayout jPanel1Layout = new GridBagLayout();
		jPanel1Layout.columnWeights = new double[] {0.0, 0.1, 0.2, 0.0, 0.2, 0.1, 0.0};
		jPanel1Layout.columnWidths = new int[] {7, 20, 20, 7, 20, 20, 7};
		jPanel1Layout.rowWeights = new double[] {0.0, 0.3, 0.1, 0.0};
		jPanel1Layout.rowHeights = new int[] {7, 7, 20, 7};
		jPanel1.setLayout(jPanel1Layout);
		jPanel1.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));
		jPanel1.setPreferredSize(new java.awt.Dimension(431, 156));
		{
			jTextField1 = new JTextField();
			jPanel1.add(jTextField1, new GridBagConstraints(2, 1, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			jTextField1.setBorder(BorderFactory.createTitledBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null), "Insert Manual Entry", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION));
		}
		{
			jButton1 = new JButton();
			jPanel1.add(jButton1, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jButton1.setText("Save");
			jButton1.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Save.png")),0.1).resizeImageIcon());
			jButton1.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent evt) {insertData();}});
		}
		{
			jButton2 = new JButton();
			jPanel1.add(jButton2, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jButton2.setText("Cancel");
			jButton2.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")),0.1).resizeImageIcon());			
			jButton2.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent evt) {simpleFinish();}});
		}
		this.setModal(true);
		this.pack();
	}

	public void simpleFinish()
	{
		this.setVisible(false);
		this.dispose();
	}

	public void finish() {};

	/**
	 * 
	 */
	public void insertData(){
		//MySQLMultiThread dsa =  new MySQLMultiThread( this.host,this.port, this.dbName, this.user, this.pass);
		Statement stmt;
		try
		{
			stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT idcompartment FROM compartment WHERE name = '" + jTextField1.getText()+ "'");
			if(rs.next())
			{
				Workbench.getInstance().warn("Compartment "+jTextField1.getText()+" already exists in database!");
			}
			else
			{
				String abbreviation;
				if(jTextField1.getText().length()>3)
				{
					abbreviation=jTextField1.getText().substring(0,3);
				}
				else
				{
					abbreviation=jTextField1.getText().concat("_");
				}
				
				stmt.execute("INSERT INTO compartment (name, abbreviation) VALUES('" + jTextField1.getText() + "','" + abbreviation + "')");
				finish();
			}
		}
		catch (SQLException e){e.printStackTrace();}
	}


}
