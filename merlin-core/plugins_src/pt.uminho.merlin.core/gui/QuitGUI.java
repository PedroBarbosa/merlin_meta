package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import utilities.CreateImageIcon;
import datatypes.MySQLProcess;
import datatypes.Project;
import es.uvigo.ei.aibench.core.Core;
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
public class QuitGUI extends JDialog implements InputGUI{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel questionLabel;
	private JButton noButton;
	private JButton yesButton;
	private JLabel questionLabel2;

	public QuitGUI(){
		super(Workbench.getInstance().getMainFrame());
		Utilities.centerOnOwner(this);
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.InputGUI#init(es.uvigo.ei.aibench.workbench.ParamsReceiver, es.uvigo.ei.aibench.core.operation.OperationDefinition)
	 */
	public void init(ParamsReceiver arg0, OperationDefinition<?> arg1) {
		this.setTitle("Quit");
		initGUI();
		//this.setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
		//this.setVisible(true);		
		this.setAlwaysOnTop(true);
		this.toFront();
		
	}

	public void onValidationError(Throwable arg0) {
		Workbench.getInstance().error(arg0);
	}

	public void initGUI() {
		
		GridBagLayout quitConfirmationDialogLayout = new GridBagLayout();
		quitConfirmationDialogLayout.rowWeights = new double[] {0.1, 1, 0.1, 1, 0.1, 1, 0.1};
		quitConfirmationDialogLayout.rowHeights = new int[] { 7, 7, 7, 7, 7, 7, 7};
		quitConfirmationDialogLayout.columnWeights = new double[] {0.1, 1, 0.1, 1, 0.1};
		quitConfirmationDialogLayout.columnWidths = new int[] { 7, 7, 7 , 7, 7};
		getContentPane().setLayout(quitConfirmationDialogLayout);
		{
			yesButton = new JButton();
			getContentPane().add(yesButton, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0));
			yesButton.setText("Yes");
			yesButton.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Log Out.png")),0.1).resizeImageIcon());
			yesButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					closeAll(0);
				}
			});
		}
		{
			noButton = new JButton();
			getContentPane().add(noButton, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0));
			noButton.setText("No");
			noButton.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")),0.1).resizeImageIcon());
			noButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					finish();
				}
			});
		}
		{
			questionLabel = new JLabel();
			this.getContentPane().add(questionLabel, new GridBagConstraints(1, 1,3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			questionLabel.setText("Are you sure you want to quit?");
			questionLabel.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/User.png")),0.1).resizeImageIcon());
		}
		{
			questionLabel2 = new JLabel();
			this.getContentPane().add(questionLabel2, new GridBagConstraints(1, 2, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			questionLabel2.setText("          All unsaved data will be lost.");
		}
		{
			this.setEnabled(true);
			Utilities.centerOnOwner(this);
			this.setTitle("Quit Confirmation");
			this.setSize(248, 132);
			this.setModal(true); //SEGURA A IMAGEM
			this.setVisible(true);
		}
	}

	public void finish(){
		this.setVisible(false);
		this.dispose();	
	}

	private void closeAll(int j){
		//		try
		//		{
		//			i++;
		//			StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"),";");
		//			Process pro = Runtime.getRuntime().exec(st.nextToken()+"/../../utilities/mysql_shutdown.bat");
		//			pro.waitFor();
		//			readStream(pro.getErrorStream(), "\nPROCESS ERROR STREAM:\t");
		//			readStream(pro.getInputStream(), "\nPROCESS INPUT STREAM:\t");
		//			
		//			if(pro.exitValue()==0 || i==10)
		//			{
		////				Workbench.getInstance().warn("Exiting...ok!");
		//				finish();	
		//				System.exit(0);
		//			}
		//			else
		//			{
		////				Workbench.getInstance().warn("Exiting...error!");
		//				closeAll(i);
		//			}
		//		}
		//		catch (IOException e) {e.printStackTrace();}
		//		catch (InterruptedException e) {e.printStackTrace();}


		String os_name = System.getProperty("os.name");
		if(os_name.contains("Windows"))
		{
			if(closeConnection())
			{
				List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);
				if(cl.size()==0)
				{
					Map<String, String> pidSessionPost = MySQLProcess.listMySQLProcess();

					if(pidSessionPost.size()>0)
					{
						String pid="";
						for(String key: pidSessionPost.keySet())
						{pid=key;}
						String session = MySQLProcess.listMySQLProcess().get(pid);
						int x = 1;
						while(x > 0)
						{
							x=MySQLProcess.terminateMySQLProcess(pid, session);
						}
						try
						{
							Runtime.getRuntime().exec("NET START MySQL").waitFor();
						}
						catch (IOException e) {e.printStackTrace();}
						catch (InterruptedException e) {e.printStackTrace();}
					}

				}
				else
				{
					for(int i=0; i<cl.size(); i++)
					{
						ClipboardItem item = cl.get(i);
						String pid = ((Project) item.getUserData()).getMysqlPID();
						String session = MySQLProcess.listMySQLProcess().get(pid);
						if(session!=null)
						{
							if(!session.trim().equals("Service"))
							{
								int x = 1;
								while(x > 0)
								{
									x=MySQLProcess.terminateMySQLProcess(pid, session);
								}
								if(((Project) item.getUserData()).getOldPID().containsValue("Service"))
								{
									try
									{
										Runtime.getRuntime().exec("NET START MySQL").waitFor();
									}
									catch (IOException e) {e.printStackTrace();}
									catch (InterruptedException e) {e.printStackTrace();}
								}
							}
						}
					}
				}
			}
		}
		finish();
		System.exit(0);
	}
	/**
	 * @return
	 * 
	 */
	private boolean closeConnection(){
		int i =CustomGUI.stopQuestion("merlin MySQL connections",
				"Close active MySQL connections?",
				new String[]{"Yes", "No", "Info"});
		if(i<2)
		{
			switch (i)
			{
			case 0:return true;
			default:return false;
			}
		}
		else
		{
			Workbench.getInstance().info(
					"The user may leave the MySQL connections available to other software.");
			return closeConnection();
		}
	}
}

