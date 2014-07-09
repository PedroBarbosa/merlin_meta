package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

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
public class SetProxyGui extends javax.swing.JDialog implements InputGUI{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ParamsReceiver receiver = null;
	private JPanel jPanel1;
	private JCheckBox useProxy;
	private JTextField port;
	private JPanel jPanel3, jPanel4;
	private JLabel jLabel4;
	private JCheckBox useAuthentication;
	private JLabel jLabel3;
	private JPasswordField password;
	private JTextField username;
	private JPanel jPanel2;
	private JLabel jLabel2;
	private JLabel jLabel1;
	private JComboBox<String> project;
	private JTextField host;
	private JButton jButtonCancel;
	private JButton jButtonSave;
	private String[] projects;

	/**
	 * 
	 */
	public  SetProxyGui() {
		super(Workbench.getInstance().getMainFrame());
		initGUI();
		Utilities.centerOnOwner(this);
	}

	/**
	 * 
	 */
	private void initGUI() {
		//try
		{
			GridBagLayout thisLayout = new GridBagLayout();
			thisLayout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0};
			thisLayout.rowHeights = new int[] {7, 7, 7, 7, 7, 7, 7, 7, 7};
			thisLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
			thisLayout.columnWidths = new int[] {7, 7, 7};
			this.getContentPane().setPreferredSize(new java.awt.Dimension(300, 400));
			getContentPane().setLayout(thisLayout);
			{
				jPanel1 = new JPanel();
				GridBagLayout jPanel1Layout = new GridBagLayout();
				getContentPane().add(jPanel1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanel1Layout.rowWeights = new double[] {0.1, 0.1, 0.1};
				jPanel1Layout.rowHeights = new int[] {7, 7, 7};
				jPanel1Layout.columnWeights = new double[] {0.1, 0.0, 1.0};
				jPanel1Layout.columnWidths = new int[] {7, 7, 7};

				jPanel1.setLayout(jPanel1Layout);
				{
					useProxy = new JCheckBox();
					jPanel1.add(useProxy, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					useProxy.setText("Use Proxy?");
					useProxy.setSelected(false);
					useProxy.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							if (((JCheckBox)arg0.getSource()).isSelected()==true)
							{
								host.setEnabled(true);
								port.setEnabled(true);
								useAuthentication.setEnabled(true);
								if (useAuthentication.isSelected()==true)
								{
									username.setEnabled(true);
									password.setEnabled(true);
								}
							}
							else
							{
								host.setEnabled(false);
								port.setEnabled(false);
								useAuthentication.setEnabled(false);
								username.setEnabled(false);
								password.setEnabled(false);
							}
						}
					});
				}
				{
					host = new JTextField();
					host.setEnabled(false);
					jPanel1.add(host, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				}
				{
					port = new JTextField();
					port.setEnabled(false);
					jPanel1.add(port, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				}
				{
					jLabel1 = new JLabel();
					jPanel1.add(jLabel1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					jLabel1.setText("Proxy Port");
				}
				{
					jLabel2 = new JLabel();
					jPanel1.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					jLabel2.setText("HTTP Proxy Host ");
				}
			}
			{
				jPanel2 = new JPanel();
				getContentPane().add(jPanel2, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				GridBagLayout jPanel2Layout = new GridBagLayout();
				jPanel2Layout.columnWidths = new int[] {7, 7, 7};
				jPanel2Layout.rowHeights = new int[] {7, 7, 7};
				jPanel2Layout.columnWeights = new double[] {0.1, 0.0, 1.0};
				jPanel2Layout.rowWeights = new double[] {0.1, 0.1, 0.1};
				jPanel2.setLayout(jPanel2Layout);
				{
					useAuthentication = new JCheckBox();
					jPanel2.add(useAuthentication, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					useAuthentication.setText("Proxy Requires Authentication?");
					useAuthentication.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {
							if (((JCheckBox)arg0.getSource()).isSelected()==true)
							{
								username.setEnabled(true);
								password.setEnabled(true);
							}
							else
							{
								username.setEnabled(false);
								password.setEnabled(false);
							}
						}
					});
				}
				{
					username = new JTextField();
					username.setEnabled(false);
					jPanel2.add(username, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				}
				{
					password = new JPasswordField();
					password.setEnabled(false);
					jPanel2.add(password, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				}
				{
					jLabel3 = new JLabel();
					jPanel2.add(jLabel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					jLabel3.setText("Proxy Password");
				}
				{
					jLabel4 = new JLabel();
					jPanel2.add(jLabel4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					jLabel4.setText("Proxy User Name");
				}
			}
			{
				jPanel4 = new JPanel();
				GridBagLayout jPanel4Layout = new GridBagLayout();
				getContentPane().add(jPanel4, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanel4Layout.rowWeights = new double[] {0.0, 0.1, 0.0};
				jPanel4Layout.rowHeights = new int[] {7, 7, 7};
				jPanel4Layout.columnWeights = new double[] {0.0, 0.1, 0.0};
				jPanel4Layout.columnWidths = new int[] {7, 7, 7};
				jPanel4.setLayout(jPanel4Layout);
				{
					List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);
					projects = new String[cl.size()];
					for(int i=0; i<cl.size(); i++)
					{
						projects[i]=(cl.get(i).getName());
					}
					project = new JComboBox<>(projects);
					jPanel4.add(project, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					if(projects.length>0)
					{
						Project projectData = (Project) Core.getInstance().getClipboard().getItemsByClass(Project.class).get(0).getUserData();
						project.setSelectedItem(projectData.getName());
						this.useProxy.setSelected(projectData.isUseProxy());
						if(this.useProxy.isSelected())
						{
							this.host.setEnabled(true);
							this.port.setEnabled(true);
						}
						this.host.setText(projectData.getProxy_host());
						this.port.setText(projectData.getProxy_port());
						this.useAuthentication.setSelected(projectData.isUseAuthentication());
						if(this.useAuthentication.isSelected())
						{
							this.username.setEnabled(true);
							this.password.setEnabled(true);
						}
						this.username.setText(projectData.getProxy_username());
						this.password.setText(projectData.getProxy_password());
					}
					project.addItemListener(new ItemListener() {

						@Override
						public void itemStateChanged(ItemEvent e) {
							if(((ItemEvent) e).getStateChange()== ItemEvent.SELECTED)
							{
								@SuppressWarnings("unchecked")
								JComboBox<String> comboBox = (JComboBox<String>) ((ItemEvent) e).getItemSelectable();
								String selectedItem = (String) comboBox.getSelectedItem();

								List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);
								for(int i=0; i<cl.size(); i++)
								{
									if(selectedItem.equals(cl.get(i).getName()))
									{
										Project projectData = (Project) cl.get(i).getUserData();
										project.setSelectedItem(projectData.getName());
										useProxy.setSelected(projectData.isUseProxy());
										host.setText(projectData.getProxy_host());
										port.setText(projectData.getProxy_port());
										useAuthentication.setSelected(projectData.isUseAuthentication());
										username.setText(projectData.getProxy_username());
										password.setText(projectData.getProxy_password());
									}
								}

							}
						}
					});
				}

			}
			{
				jPanel3 = new JPanel();
				GridBagLayout jPanel3Layout = new GridBagLayout();
				getContentPane().add(jPanel3, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanel3Layout.rowWeights = new double[] {0.0, 0.1, 0.0};
				jPanel3Layout.rowHeights = new int[] {7, 7, 7};
				jPanel3Layout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
				jPanel3Layout.columnWidths = new int[] {7, 7, 7, 7, 7};
				jPanel3.setLayout(jPanel3Layout);
				{
					jButtonSave = new JButton();
					jPanel3.add(jButtonSave, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jButtonSave.setText("Save");
					jButtonSave.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Save.png")),0.1).resizeImageIcon());
					jButtonSave.addActionListener(new ActionListener(){

						@Override
						public void actionPerformed(ActionEvent e) {
							Integer portInt=0;
							boolean go=true;
							if(!(port+"").isEmpty())
							{
								try
								{
									portInt = new Integer(port.getText());

								}
								catch (NumberFormatException ex)
								{
									go=false;
									//ex.printStackTrace();
									Workbench.getInstance().error("Please Insert a number for the proxy port!");
								}
							}
							if(go)
							{
								Project projectData =null;
								List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);
								for(int i=0; i<cl.size(); i++)
								{
									if(project.getSelectedItem().equals(cl.get(i).getName()))
									{
										projectData = (Project) cl.get(i).getUserData();
									}
								}

								receiver.paramsIntroduced(
										new ParamSpec[]{
												new ParamSpec("Select Project", Project.class, projectData, null),
												new ParamSpec("Use Proxy?",boolean.class,useProxy.isSelected(),null),
												new ParamSpec("HTTP Proxy Host",String.class,host.getText(),null),
												new ParamSpec("Proxy Port",Integer.class,portInt,null),
												new ParamSpec("Proxy Requires Authentication?",boolean.class,useAuthentication.isSelected(),null),
												new ParamSpec("Proxy User Name",String.class,username.getText(),null),
												new ParamSpec("Proxy Password",String.class,password.getPassword().toString(),null)
										}
								);
								finish();
							}

						}});
				}
				{
					jButtonCancel = new JButton();
					jPanel3.add(jButtonCancel, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jButtonCancel.setText("Close");
					jButtonCancel.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")), 0.1).resizeImageIcon());
					jButtonCancel.addActionListener(new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent arg0) {
							finish();
						}
					});
				}
			}
			this.setSize(400, 350);
			this.setModal(true);
		}
	}

	@Override
	public void finish() {
		this.setVisible(false);
		this.dispose();		
	}

	@Override
	public void init(ParamsReceiver arg0, OperationDefinition<?> arg1) {
		this.receiver = arg0;
		this.setTitle(arg1.getName());
		this.setVisible(true);
	}

	@Override
	public void onValidationError(Throwable arg0) {
		Workbench.getInstance().error(arg0);
	}
}
