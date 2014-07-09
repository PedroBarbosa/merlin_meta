/**
 * 
 */
package utilities;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.WindowConstants;

import org.platonos.pluginengine.PluginLifecycle;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.workbench.Workbench;

/**
 * @author paulo maia, 09/05/2007
 *
 */
public class Lifecycle extends PluginLifecycle {
	@Override
	public void start(){
		Workbench.getInstance().getMainFrame().setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
		//Workbench.getInstance().getMainFrame().setIconImage((new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png")),10).resizeImageIcon()).getImage());
		Workbench.getInstance().getMainFrame().addWindowListener(new WindowListener(){

			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void windowClosing(WindowEvent e) {
				Workbench.getInstance().getMainFrame().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				//find quit
				for (OperationDefinition<?> def : Core.getInstance().getOperations()){
					if (def.getID().equals("operations.QuitOperation.ID"))
					{			
						Workbench.getInstance().executeOperation(def);
						return;
					}
				}
				
			}

			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
}
