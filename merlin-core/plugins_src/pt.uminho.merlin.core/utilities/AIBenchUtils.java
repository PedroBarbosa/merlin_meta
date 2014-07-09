/**
 * 
 */
package utilities;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import views.UpdatablePanel;
import datatypes.Project;
import datatypes.metabolic.EnzymesContainer;
import datatypes.metabolic.ReactionsContainer;
import datatypes.metabolic_regulatory.Entity;
import datatypes.metabolic_regulatory.Genes;
import datatypes.metabolic_regulatory.HomologyDataContainer;
import datatypes.metabolic_regulatory.Proteins;
import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.workbench.MainWindow;
import es.uvigo.ei.aibench.workbench.Workbench;

/**
 * @author ODias
 *
 */
public class AIBenchUtils {

	/**
	 * @return
	 */
	public static List<String> getProjectNames() {

		List <String> projectNames = new ArrayList<String>();

		List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);

		for(int i=0; i<cl.size(); i++) {

			ClipboardItem item = cl.get(i);
			projectNames.add(item.getName());
		}

		return projectNames;
	}


	/**
	 * @param projectName
	 */
	public static void updateAllViews(String projectName) {

		AIBenchUtils.updateEntity(projectName);
		AIBenchUtils.updateProteinView(projectName);
		AIBenchUtils.updateGeneView(projectName);
		AIBenchUtils.updateHomologyDataView(projectName);
		AIBenchUtils.updateMetabolicViews(projectName);
		AIBenchUtils.updateProjectView(projectName);
	}



	/**
	 * @param projectName
	 */
	public static void updateMetabolicViews(String projectName) {

		AIBenchUtils.updateReactionsView(projectName);
		AIBenchUtils.updateEnzymeView(projectName);
		AIBenchUtils.updateReagentProductsView(projectName);
	}

	/**
	 * @param projectName
	 */
	public static void updateHomologyDataView(String projectName) {

		AIBenchUtils.updateView(projectName, HomologyDataContainer.class);
	}

	/**
	 * @param projectName
	 */
	public static void updateProjectView(String projectName) {

		AIBenchUtils.updateView(projectName, Project.class);
	}
	/**
	 * @param projectName
	 */
	public static void updateGeneView(String projectName) {

		AIBenchUtils.updateView(projectName, Genes.class);
	}

	/**
	 * @param projectName
	 */
	public static void updateProteinView(String projectName) {

		AIBenchUtils.updateView(projectName, Proteins.class);
	}


	/**
	 * @param projectName
	 */
	public static void updateEnzymeView(String projectName) {

		AIBenchUtils.updateView(projectName, EnzymesContainer.class);
	}

	/**
	 * @param projectName
	 */
	public static void updateReactionsView(String projectName) {

		AIBenchUtils.updateView(projectName, ReactionsContainer.class);
	}

	/**
	 * @param projectName
	 */
	public static void updateReagentProductsView(String projectName) {

		AIBenchUtils.updateView(projectName, ReactionsContainer.class);
	}


	/**
	 * @param projectName
	 */
	public static void updateEntity(String projectName) {

		AIBenchUtils.updateView(projectName, Entity.class);
	}

	/**
	 * @param projectName
	 * @param datatype
	 */
	public static void updateView(String projectName, Class<?> datatype) {

		ClipboardItem item = null;

		List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(datatype);

		for(int i=0; i<cl.size(); i++) {

			if(datatype.equals(cl.get(i).getUserData().getClass())) {

				if( cl.get(i).getName().equals(projectName)) {

					item = cl.get(i);
				}
				else {
					
					if(cl.get(i).getUserData().getClass().getSuperclass().equals(Entity.class) ||
						cl.get(i).getUserData().getClass().equals(Entity.class)) {
						
						Entity entity = (Entity) cl.get(i).getUserData();

						if(entity.getProject().getName().equals(projectName))
							item = cl.get(i);
					}
				}
			}
			else {

				Entity entity = (Entity) cl.get(i).getUserData();

				if(entity.getProject().getName().equals(projectName))
					item = cl.get(i);
			}
		}

		if(item!=null) {

			MainWindow window = (MainWindow) Workbench.getInstance().getMainFrame();

			List<Component> list = window.getDataViews(item);

			for(Component component : list) {

				UpdatablePanel view = (UpdatablePanel) component;

				if(view.getProjectName().equals(projectName)) {

					view.updateTableUI();
				}
			}
		}
	}

}
