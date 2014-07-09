/**
 * 
 */
package gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.SoftBevelBorder;

import operations.CleanDatabase.SchemaType;
import utilities.CreateImageIcon;
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
public class Remove_database_gui extends javax.swing.JDialog implements InputGUI{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jPanel12;
	private JPanel jPanel1;
	private JPanel jPanel11;
	private JComboBox<String> jComboBox1;
	private JComboBox<String> project;
	private String[] projects;
	private ParamsReceiver rec;


	/**
	 * New project Gui constructor
	 */
	public Remove_database_gui() {
		
		super(Workbench.getInstance().getMainFrame());
		this.initGUI();
		Utilities.centerOnOwner(this);
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
			jPanel1Layout.columnWeights = new double[] {0.0, 0.1, 0.0};
			jPanel1Layout.columnWidths = new int[] {7, 7, 7};
			jPanel1Layout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
			jPanel1Layout.rowHeights = new int[] {7, 20, 7, 20, 7};
			jPanel1.setLayout(jPanel1Layout);
			//jPanel1.setPreferredSize(new java.awt.Dimension(426, 297));

			jPanel11 = new JPanel();
			GridBagLayout jPanel11Layout = new GridBagLayout();
			jPanel11.setLayout(jPanel11Layout);
			jPanel11Layout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
			jPanel11Layout.columnWidths = new int[] {7, 7, 7, 7, 7};
			jPanel11Layout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
			jPanel11Layout.rowHeights = new int[] {7, 7, 7, 7, 7};
			
			JLabel jLabel5 = new JLabel();
			jLabel5.setText("Select Information");
			jPanel11.add(jLabel5, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));
			{
				DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
				model.addElement("Forget it!");
				model.addElement("Remove KEGG Data!");
				model.addElement("Remove NCBI Homology Data!");
				model.addElement("Remove Transport Proteins Data!");
				model.addElement("Remove Transport Reactions Data!");
				model.addElement("Remove Compartments Allocation Data!");
				model.addElement("Remove All Data From Database!");
				jComboBox1 = new JComboBox<>(model);
				jPanel11.add(jComboBox1, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}
			
			JLabel jLabel1 = new JLabel();
			jLabel1.setText("Select Project");
			jPanel11.add(jLabel1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));

			List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);
			projects = new String[cl.size()];
			for(int i=0; i<cl.size(); i++)
			{
				projects[i]=(cl.get(i).getName());
			}
			project = new JComboBox<>(projects);
			jPanel11.add(project, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			if(projects.length>0)
			{
				Project projectData = (Project) Core.getInstance().getClipboard().getItemsByClass(Project.class).get(0).getUserData();
				project.setSelectedItem(projectData.getName());
			}

			jPanel1.add(jPanel11, new GridBagConstraints(1, 1, 3, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			jPanel12 = new JPanel();
			GridBagLayout jPanel12Layout = new GridBagLayout();
			jPanel12.setLayout(jPanel12Layout);
			jPanel12Layout.columnWeights = new double[] {0.0, 0.1, 0.0};
			jPanel12Layout.columnWidths = new int[] {7, 20, 7};
			jPanel12Layout.rowWeights = new double[] {0.1};
			jPanel12Layout.rowHeights = new int[] {7};
			jPanel12.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));

			jPanel1.add(jPanel12, new GridBagConstraints(1, 3, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

			JButton button1 = new JButton("Ok");
			button1.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")),0.1).resizeImageIcon());
			jPanel12.add(button1, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt){
					int selected_script = jComboBox1.getSelectedIndex();
					
					SchemaType database = null;
					switch (selected_script)
					{
					default:
					{
						database = SchemaType.IGNORE;
						break;
					}
					case 0:
					{
						database = SchemaType.IGNORE;
						break;
					}
					case 1:
					{
						database = SchemaType.KEGG_INFORMATION;
						break;
					}
					case 2:
					{
						database = SchemaType.HOMOLOGY_INFORMATION;
						break;
					}
					case 3:
					{
						database = SchemaType.SW_TCDB_INFORMATION;
						break;
					}
					case 4:
					{
						database = SchemaType.TRANSPORTERS_DATABASE;
						break;
					}
					case 5:
					{
						database = SchemaType.COMPARTMENTS_DATABASE;
						break;
					}
					case 6:
					{
						database = SchemaType.ALL_INFORMATION;
						break;
					}
					}
					
					Project projectData =null;
					List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);
					for(int i=0; i<cl.size(); i++)
					{
						if(project.getSelectedItem().equals(cl.get(i).getName()))
						{
							projectData = (Project) cl.get(i).getUserData();
						}
					}

					rec.paramsIntroduced(
							new ParamSpec[]{
									new ParamSpec("Project",Project.class,projectData,null),
									new ParamSpec("Select Information",SchemaType.class,database,null)
							}
							);
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

		}
		this.setSize(450, 200);
		//} catch (Exception e) {e.printStackTrace();}
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

}
