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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.SoftBevelBorder;

import pt.uminho.sysbio.common.bioapis.externalAPI.kegg.KeggAPI;
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
public class Database_loader_gui extends javax.swing.JDialog implements InputGUI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private JPanel jPanel1;
	private JPanel jPanel11;
	private JPanel jPanel12;
	private JComboBox<String> jComboBox1;
	private Map<String,String> organimsMap;
	private ParamsReceiver rec = null;
	private JComboBox<String> project;
	private String[] projects;

	/**
	 * 
	 */
	public Database_loader_gui() {

		super(Workbench.getInstance().getMainFrame());
		this.setComboBox();
		initGUI();
		Utilities.centerOnOwner(this);
	}

	/**
	 * 
	 */
	private void initGUI() {
		this.setModal(true);
		{
			this.setTitle("Load database from KEGG");
			jPanel1 = new JPanel();
			getContentPane().add(jPanel1, BorderLayout.CENTER);
			GridBagLayout jPanel1Layout = new GridBagLayout();
			jPanel1Layout.columnWeights = new double[] {0.1, 0.0, 0.1};
			jPanel1Layout.columnWidths = new int[] {7, 7, 7};
			jPanel1Layout.rowWeights = new double[] {0.1, 0.0, 0.1, 0.0};
			jPanel1Layout.rowHeights = new int[] {7, 7, 20, 7};
			jPanel1.setLayout(jPanel1Layout);
			jPanel1.setPreferredSize(new java.awt.Dimension(227, 314));


			jPanel11 = new JPanel();
			GridBagLayout jPanel11Layout = new GridBagLayout();
			jPanel11.setLayout(jPanel11Layout);
			jPanel11Layout.columnWeights = new double[] {0.0, 0.1, 0.0};
			jPanel11Layout.columnWidths = new int[] {7, 7, 7};
			jPanel11Layout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0};
			jPanel11Layout.rowHeights = new int[] {7, 7, 7, 7, 7, 7, 7};

			jPanel1.add(jPanel11, new GridBagConstraints(0, 0, 3, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));


			jPanel12 = new JPanel();
			GridBagLayout jPanel12Layout = new GridBagLayout();
			jPanel12.setLayout(jPanel12Layout);
			jPanel12Layout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.0};
			jPanel12Layout.columnWidths = new int[] {3, 20, 7, 50};
			jPanel12Layout.rowWeights = new double[] {0.1};
			jPanel12Layout.rowHeights = new int[] {7};
			jPanel12.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));

			{
				List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);
				projects = new String[cl.size()];
				for(int i=0; i<cl.size(); i++)
				{
					projects[i]=(cl.get(i).getName());
				}
				project = new JComboBox<>(projects);
				jPanel11.add(project, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(6, 5, 6, 5), 0, 0));
				if(projects.length>0)
				{
					Project projectData = (Project) Core.getInstance().getClipboard().getItemsByClass(Project.class).get(0).getUserData();
					project.setSelectedItem(projectData.getName());
				}
				project.addItemListener(new ItemListener() {

					@Override
					public void itemStateChanged(ItemEvent e) {
						if(((ItemEvent) e).getStateChange()== ItemEvent.SELECTED)
						{
							JComboBox<String> comboBox = (JComboBox<String>) ((ItemEvent) e).getItemSelectable();
							String selectedItem = (String) comboBox.getSelectedItem();

							List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);
							for(int i=0; i<cl.size(); i++)
							{
								if(selectedItem.equals(cl.get(i).getName()))
								{
									Project projectData = (Project) cl.get(i).getUserData();
									project.setSelectedItem(projectData.getName());
								}
							}

						}
					}
				});
			}

			jPanel1.add(jPanel12, new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

			JButton button1 = new JButton("Ok");
			button1.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")),0.1).resizeImageIcon());
			jPanel12.add(button1, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			button1.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent evt) {

					Project projectData =null;
					List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);

					for(int i=0; i<cl.size(); i++) {

						if(project.getSelectedItem().equals(cl.get(i).getName())) {

							projectData = (Project) cl.get(i).getUserData();
						}
					}

					if(jComboBox1.getSelectedItem().toString().isEmpty()) {

						Workbench.getInstance().info("No organism related information will be loaded!");
					}

					rec.paramsIntroduced(
							new ParamSpec[]{
									new ParamSpec("Project", Project.class, projectData, null),
									new ParamSpec("organism",String.class,organimsMap.get(jComboBox1.getSelectedItem().toString()),null)
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

			JLabel jLabel5 = new JLabel();
			jLabel5.setText("Select Organism");
			jPanel11.add(jLabel5, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(6, 5, 6, 5), 0, 0));

			jPanel11.add(jComboBox1, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(6, 5, 6, 5), 0, 0));

		}
		this.setSize(250, 200);

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
	 * 
	 */
	private void setComboBox() {

		List<String[]> organisms;
		try {
			organisms = KeggAPI.getOrganisms();

			List<String> organismsList = new ArrayList<String>();
			this.organimsMap = new HashMap<String,String>();

			organismsList.add("");
			this.organimsMap.put("", "no_org");
			for(String[] organism : organisms)
			{
				organismsList.add(organism[0].replace("__", " "));
				this.organimsMap.put(organism[0], organism[1]);
			}

			Collections.sort(organismsList);

			String[] model = new String[organismsList.size()];

			for(int i =0; i<organismsList.size();i++)
			{
				model[i] = organismsList.get(i).replace("__", " ");
			}

			jComboBox1=new JComboBox(model);
			jComboBox1.updateUI();
		}
		catch (Exception e) {
			
			Workbench.getInstance().error(e);
			e.printStackTrace();
		}
	}

}
