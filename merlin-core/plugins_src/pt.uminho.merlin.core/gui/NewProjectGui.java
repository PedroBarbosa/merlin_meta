package gui;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.SoftBevelBorder;
import javax.swing.filechooser.FileFilter;

import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.sysbio.common.database.connector.datatypes.MySQL_Schemas;
import pt.uminho.sysbio.common.utilities.io.FileUtils;
import utilities.CreateImageIcon;
import utilities.ProjectUtils;
import datatypes.MySQLProcess;
import datatypes.Project;
import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.workbench.InputGUI;
import es.uvigo.ei.aibench.workbench.ParamsReceiver;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;


/**
 * @author ODias
 *
 */
public class NewProjectGui extends javax.swing.JDialog implements InputGUI{

	private static final long serialVersionUID = 2799696219875931154L;
	private JPanel jPanel1;
	private JPasswordField jPasswordField1;
	private JLabel jTaxonomyID;
	private JSpinner jTextField4;
	private JComboBox jComboBox1;
	private JTextField jTextField1;
	private JTextField jTextField2;
	private JTextField jTextField3;
	private JTextField jTextField5;
	private String pid = null;
	private Map <String,String> oldPID;
	private JPanel jPanel11;
	private JPanel jPanel12;
	private ParamsReceiver rec = null;
	private File file;
	private JTextField jTextField6;
	private JCheckBox isSetFastaFiles;
	private JButton jbutton;
	private JFileChooser jFileChooser;
	private JCheckBox isNCBIGenome;
	private JCheckBox isMetagenomicProject;
	/**
	 * New project Gui constructor
	 */
	public NewProjectGui() {

		super(Workbench.getInstance().getMainFrame());
		//Utilities.centerOnOwner(this);
		this.file=null;
		initGUI();
		Utilities.centerOnOwner(this);
		try  {

			this.setComboBox();
		}
		catch (SQLException e)  {

			e.printStackTrace();
		}
	}

	/**
	 * Initiate gui method
	 */
	private void initGUI() {
		//		try
		//		{
		this.setModal(true);
		{

			jPanel1 = new JPanel();
			getContentPane().add(jPanel1, BorderLayout.CENTER);
			GridBagLayout jPanel1Layout = new GridBagLayout();
			jPanel1Layout.columnWeights = new double[] {0.1, 0.0, 0.1};
			jPanel1Layout.columnWidths = new int[] {7, 7, 7};
			jPanel1Layout.rowWeights = new double[] {0.1, 0.0, 0.1, 0.0};
			jPanel1Layout.rowHeights = new int[] {7, 7, 20, 7};
			jPanel1.setLayout(jPanel1Layout);
			jPanel1.setPreferredSize(new java.awt.Dimension(426, 297));


			jPanel11 = new JPanel();
			GridBagLayout jPanel11Layout = new GridBagLayout();
			jPanel11.setLayout(jPanel11Layout);
			jPanel11Layout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
			jPanel11Layout.columnWidths = new int[] {7, 7, 7, 7, 7};
			jPanel11Layout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0};
			jPanel11Layout.rowHeights = new int[] {7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 20, 7, 7, 7};

			jPanel1.add(jPanel11, new GridBagConstraints(0, 0, 3, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));


			jPanel12 = new JPanel();
			GridBagLayout jPanel12Layout = new GridBagLayout();
			jPanel12.setLayout(jPanel12Layout);
			jPanel12Layout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.0};
			jPanel12Layout.columnWidths = new int[] {3, 20, 7, 50};
			jPanel12Layout.rowWeights = new double[] {0.1};
			jPanel12Layout.rowHeights = new int[] {7};
			jPanel12.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));

			jPanel1.add(jPanel12, new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

			JButton button1 = new JButton("Ok");
			button1.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")),0.1).resizeImageIcon());
			jPanel12.add(button1, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt){
					String host = jTextField1.getText();
					String port = jTextField2.getText();
					String user = jTextField3.getText();
					String password = new String(jPasswordField1.getPassword());
					String database = jComboBox1.getSelectedItem().toString();
					String name = jTextField5.getText();
					String genomeID = null;

					List <String> projectNames = new ArrayList<String>();

					List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);

					for(int i=0; i<cl.size(); i++) {

						ClipboardItem item = cl.get(i);
						projectNames.add(item.getName());
					}
					boolean go=true;

					if(name.isEmpty()) {

						while(projectNames.contains(name)||name.isEmpty()) {

							name = buildName(name);
						}
					}
					else {

						for(int i=0; i<projectNames.size(); i++) {

							String itemName = projectNames.get(i);
							if(name.equals(itemName)) {

								go=false;
								Workbench.getInstance().error("Project with the same name already exists!\nPlease insert another name.");
							}
						}
					}


					for (ClipboardItem item : cl) {

						String host_previous = ((Project)item.getUserData()).getDatabase().getMySqlCredentials().get_database_host();
						String databaseName_previous = ((Project)item.getUserData()).getDatabase().getMySqlCredentials().get_database_name();

						if(database.equals(databaseName_previous) && host.equals(host_previous)) {
							go=false;
							Workbench.getInstance().error("Project connected to the same data base already exists!\nPlease select another database.");
						}
					}
					
					List<File> faafastaFiles = new ArrayList<File>(), fnafastaFiles = new ArrayList<File>();

					if(file == null || file.toString().isEmpty() || (!file.isFile() && !file.isDirectory())) {

						Workbench.getInstance().warn("Fasta files directory not set!");
					}
					else {

						if(file.getName().contains("\"")) {

							file = new File(file.getAbsolutePath().replace("\"", ""));
						}

						if(!file.isDirectory()) {

							file = new File(file.getParent().toString());
						}

						
						for(File f: file.listFiles()) {

							if(f.getAbsolutePath().endsWith(".faa")) {

								faafastaFiles.add(f);
							}
						}
						
						for(File f: file.listFiles()) {

							if(f.getAbsolutePath().endsWith(".fna")) {

								fnafastaFiles.add(f);
							}
						}

						if(faafastaFiles.isEmpty() && fnafastaFiles.isEmpty()) {

							go=false;
							Workbench.getInstance().error("Please Select a directory with '.faa' or '.fna' files!");
						}
						else {

							if(!faafastaFiles.isEmpty()) {
								
								try  {

									CreateGenomeFile createGenomeFile = new CreateGenomeFile("genome_"+name+"_"+faafastaFiles.size(), faafastaFiles,".faa");
									createGenomeFile.setNCBIGenome(isNCBIGenome.isSelected());
									genomeID=createGenomeFile.getGenomeID();
								} 
								catch (Exception e) {

									Workbench.getInstance().error("Error creating '.faa' files!");
									e.printStackTrace();
								}
							}
							
							if(!fnafastaFiles.isEmpty()) {
								
								try  {

									CreateGenomeFile createGenomeFile = new CreateGenomeFile("genome_"+name+"_"+fnafastaFiles.size(), fnafastaFiles,".fna");
									createGenomeFile.setNCBIGenome(isNCBIGenome.isSelected());
									genomeID=createGenomeFile.getGenomeID();
								} 
								catch (Exception e) {

									Workbench.getInstance().error("Error creating '.fna' files!");
									e.printStackTrace();
								}
							}
						}

						if(!isNCBIGenome.isSelected() && (jTextField4.getValue().toString().isEmpty() || Integer.parseInt(jTextField4.getValue().toString()) <= 0)) {

							Workbench.getInstance().warn("Taxonomy ID for non NCBI genome not set!");
						}
					}
					//verifyMySQLProcess();

					if(go)
					{
						rec.paramsIntroduced(
								new ParamSpec[]{
										new ParamSpec("Host",String.class,host,null),
										new ParamSpec("Port",String.class,port,null),
										new ParamSpec("User",String.class,user,null),
										new ParamSpec("Password",String.class,password,null),
										new ParamSpec("Database",String.class,database,null),
										new ParamSpec("New project name",String.class,name,null),
										new ParamSpec("PID",String.class,pid,null),
										new ParamSpec("oldPID",Map.class,oldPID,null),
										new ParamSpec("genomeID",String.class,genomeID,null),
										new ParamSpec("isNCBIGenome",boolean.class,isNCBIGenome.isSelected(),null),
										new ParamSpec("isMetagenomicProject",boolean.class,isMetagenomicProject.isSelected(),null),
										new ParamSpec("TaxonomyID",long.class,Long.parseLong(jTextField4.getValue().toString()),null),
										new ParamSpec("isFaaFastaFiles",boolean.class,!faafastaFiles.isEmpty(),null),
										new ParamSpec("isFnaFastaFiles",boolean.class,!fnafastaFiles.isEmpty(),null)
								}
								);
					}
				}
			});

			JButton button2 = new JButton("Cancel");
			button2.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")),0.1).resizeImageIcon());
			jPanel12.add(button2, new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			button2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					finish();
				}
			});

			JLabel jLabel1 = new JLabel();
			jLabel1.setText("Host\t");
			jPanel11.add(jLabel1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

			jTextField1 = new JTextField();
			jTextField1.setText("127.0.0.1");
			jTextField1.setSize(232, 33);
			jPanel11.add(jTextField1, new GridBagConstraints(3, 1, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			this.addListener(jTextField1);

			JLabel jLabel2 = new JLabel();
			jLabel2.setText("Port\t");
			jPanel11.add(jLabel2, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));

			jTextField2 = new JTextField();
			jTextField2.setText("3306");
			jTextField2.setSize(232, 33);
			jPanel11.add(jTextField2, new GridBagConstraints(3, 3, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			this.addListener(jTextField2);

			JLabel jLabel3 = new JLabel();
			jLabel3.setText("User\t");
			jPanel11.add(jLabel3, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));

			jTextField3 = new JTextField();
			jTextField3.setText("root");
			jTextField3.setSize(232, 33);
			jPanel11.add(jTextField3, new GridBagConstraints(3, 5, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			this.addListener(jTextField3);

			JLabel jLabel4 = new JLabel();
			jLabel4.setText("Password\t");
			jPanel11.add(jLabel4, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));

			jPasswordField1 = new JPasswordField();
			jPasswordField1.setText("password");
			jPasswordField1.setSize(232, 33);
			jPanel11.add(jPasswordField1, new GridBagConstraints(3, 7, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			this.addListener(jPasswordField1);

			JLabel jLabel5 = new JLabel();
			jLabel5.setText("Database");
			jPanel11.add(jLabel5, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));
			{
				jComboBox1 = new JComboBox();
				jPanel11.add(jComboBox1, new GridBagConstraints(3, 9, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}

			JLabel jLabel6 = new JLabel();
			jLabel6.setText("New project name");
			jPanel11.add(jLabel6, new GridBagConstraints(1, 11, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));

			jTextField5 = new JTextField();
			jTextField5.setSize(232, 33);
			jPanel11.add(jTextField5, new GridBagConstraints(3, 11, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			
			isMetagenomicProject = new JCheckBox();
			jPanel11.add(isMetagenomicProject, new GridBagConstraints(3, 13, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			isMetagenomicProject.setText("Is this a metagenomics project?");
			isMetagenomicProject.setEnabled(true);
			isMetagenomicProject.setSelected(false);
//			isMetagenomicProject.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent arg0) {
//					
//						jTextField4.setValue(131567);
//						isNCBIGenome.setEnabled(false);
//					}
//			});
			
			
			isSetFastaFiles = new JCheckBox();
			jPanel11.add(isSetFastaFiles, new GridBagConstraints(3, 15, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			isSetFastaFiles.setText("Set fasta files directory.");
			isSetFastaFiles.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					if(isMetagenomicProject.isSelected()){
						jTextField6.setEnabled(((JCheckBox)arg0.getSource()).isSelected());
						jbutton.setEnabled(((JCheckBox)arg0.getSource()).isSelected());
						jTaxonomyID.setEnabled(((JCheckBox)arg0.getSource()).isSelected());
						jTextField4.setEnabled(((JCheckBox)arg0.getSource()).isSelected());
						jTextField4.setValue(131567);
					}
					else{
						
						jTextField6.setEnabled(((JCheckBox)arg0.getSource()).isSelected());
						jbutton.setEnabled(((JCheckBox)arg0.getSource()).isSelected());
						isNCBIGenome.setEnabled(((JCheckBox)arg0.getSource()).isSelected());
						jTaxonomyID.setEnabled(((JCheckBox)arg0.getSource()).isSelected());	
						jTextField4.setEnabled(((JCheckBox)arg0.getSource()).isSelected());
						jTextField4.setValue(0);
						
					}
				
				}
			});


			isNCBIGenome = new JCheckBox();
			jPanel11.add(isNCBIGenome, new GridBagConstraints(3, 17, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			isNCBIGenome.setText("Genome downloaded from NCBI website?");
			isNCBIGenome.setToolTipText("Genome downloaded from\nNCBI FTP webSite?");
			isNCBIGenome.setEnabled(false);
			isNCBIGenome.setSelected(false);
			isNCBIGenome.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					jTaxonomyID.setEnabled(!((JCheckBox)arg0.getSource()).isSelected());
					jTextField4.setEnabled(!((JCheckBox)arg0.getSource()).isSelected());
				}
			});
			
			
			jbutton= new JButton();
			jPanel11.add(jbutton, new GridBagConstraints(1, 21, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));
			jbutton.setText("Browse fasta files");
			jbutton.setToolTipText("Browse fasta files");
			jbutton.setEnabled(false);
			jbutton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0) {
					openFileChooser();
				}});
			jFileChooser = new JFileChooser();
			jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			jFileChooser.setFileFilter(new FileFilter() {
				@Override
				public String getDescription() {

					return "fasta files";
				}
				@Override
				public boolean accept(File f) {return f.isDirectory() || f.getName().toLowerCase().endsWith("fna") || f.getName().toLowerCase().endsWith("faa");}
			});
			jTextField6= new JTextField();
			jTextField6.setEnabled(false);
			jTextField6.setEditable(false);
			jPanel11.add(jTextField6, new GridBagConstraints(3, 21, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jTextField6.setSize(232, 33);
			jTextField6.addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent arg0) {}
				@Override
				public void mousePressed(MouseEvent arg0) {}
				@Override
				public void mouseExited(MouseEvent arg0) {}
				@Override
				public void mouseEntered(MouseEvent arg0) {}
				@Override
				public void mouseClicked(MouseEvent arg0) {openFileChooser();}
			});


			{
				jTextField4 = new JSpinner();
				jPanel11.add(jTextField4, new GridBagConstraints(3, 19, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jTextField4.setEnabled(false);
				jTaxonomyID = new JLabel();
				jPanel11.add(jTaxonomyID, new GridBagConstraints(1, 19, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jTaxonomyID.setText("Taxonomy ID");
				jTaxonomyID.setEnabled(false);
			}


			String os_name = System.getProperty("os.name");
			if(os_name.contains("Windows"))
			{
				try {

					verifyMySQLProcess();
				} 
				catch (SQLException e) {
					this.oldPID=null;
					this.pid=null;
				}
			}
			else
			{
				this.oldPID=null;
				this.pid=null;
			}
		}
		this.setSize(450, 450);
		//} catch (Exception e) {e.printStackTrace();}
	}

	/**
	 * @return
	 */
	private void openFileChooser(){

		jFileChooser.setDialogTitle("Select a directory containing genome fasta files.");
		int returnVal = jFileChooser.showOpenDialog(new JTextArea());

		if (returnVal == JFileChooser.APPROVE_OPTION) {

			file = jFileChooser.getSelectedFile();
			jTextField6.setText(jFileChooser.getSelectedFile().getPath());
		}
	}


	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.InputGUI#finish()
	 */
	public void finish() {

		this.setVisible(false);
		this.dispose();
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.InputGUI#init(es.uvigo.ei.aibench.workbench.ParamsReceiver, es.uvigo.ei.aibench.core.operation.OperationDefinition)
	 */
	public void init(ParamsReceiver arg0, OperationDefinition<?> arg1) {

		this.rec = arg0;
		this.setTitle(arg1.getName());
		this.setVisible(true);
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.InputGUI#onValidationError(java.lang.Throwable)
	 */
	public void onValidationError(Throwable arg0) {

		Workbench.getInstance().error(arg0);
	}

	/**
	 * @return
	 * @throws SQLException 
	 */
	private boolean getSchemas() throws SQLException {

		String host = jTextField1.getText();
		String port = jTextField2.getText();
		String user = jTextField3.getText();
		String password = new String(jPasswordField1.getPassword());

		MySQL_Schemas mSchemas = new MySQL_Schemas(user, password, host, port);

		return mSchemas.isConnected();
	}

	/**
	 * @param field
	 */
	private void addListener(JComponent field) {

		field.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent arg0) {}
			@Override
			public void keyPressed(KeyEvent arg0) {}
			@Override
			public void keyReleased(KeyEvent arg0) {{try {
				setComboBox();
			} catch (SQLException e) {
				Workbench.getInstance().error("No MySQL connection!");
				e.printStackTrace();
			}}}
		});
	}

	/**
	 * @throws SQLException 
	 * 
	 */
	private void setComboBox() throws SQLException {

		DefaultComboBoxModel sch = new DefaultComboBoxModel();
		if(getSchemas()) {

			String host = jTextField1.getText();
			String port = jTextField2.getText();
			String user = jTextField3.getText();
			String password = new String(jPasswordField1.getPassword());
			MySQL_Schemas mSchemas = new MySQL_Schemas(user, password, host, port);
			List<String> schemas = mSchemas.getSchemas();

			if(schemas.isEmpty()) {

				//StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"),";");
				//String filePath=st.nextToken()+"/../../utilities/sysbio.sql";
				String[] filePath=new String[6];
				String path = FileUtils.getCurrentLibDirectory()+"/../utilities/";
				filePath[0]=path +"/../../utilities/sysbio_KEGG.sql";
				filePath[1]=path +"/../../utilities/sysbio_blast.sql";
				filePath[2]=path +"/../../utilities/sysbio_metabolites_transporters.sql";
				filePath[4]=path +"/../../utilities/sysbio_sw_tcdb.sql";
				filePath[3]=path +"/../../utilities/sysbio_compartments.sql";
				filePath[5]=path +"/../../utilities/sysbio_metabolites_backup.sql";

				if(mSchemas.newSchemaAndScript("merlinDB", filePath)) {

					Workbench.getInstance().info("Database merlinDB successfuly created.");
				}
				else {

					Workbench.getInstance().error("There was an error when trying to create merlinDB!!");
				}
			}
			sch = new DefaultComboBoxModel(schemas.toArray(new String[schemas.size()]));

		}

		jComboBox1.setModel(sch);
		jComboBox1.updateUI();
	}

	/**
	 * @param name
	 * @return
	 */
	private String buildName(String name) {

		Project.setCounter(Project.getCounter()+1);
		name="Project_"+Project.getCounter();
		return name;
	}

	/**
	 * @throws SQLException 
	 * 
	 */
	private void verifyMySQLProcess() throws SQLException {

		this.oldPID=MySQLProcess.listMySQLProcess();
		this.pid=MySQLProcess.starMySQLProcess(jTextField3.getText(), new String(jPasswordField1.getPassword()), jTextField1.getText(), jTextField2.getText());
	}
}
