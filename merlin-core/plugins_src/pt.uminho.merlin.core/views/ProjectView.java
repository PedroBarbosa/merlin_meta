package views;
import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import datatypes.Project;

/**
 * @author ODias
 *
 */
public class ProjectView extends UpdatablePanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jPanel1;
	private JLabel jLabel1;
	private JLabel jLabel2;
	private JLabel jLabel3;
	private JLabel jLabel4;
	private JLabel jLabel5;
	private JLabel jLabel6;
	private JLabel jLabel7;
	private JLabel jLabel8;
	private JLabel jLabel9;
	private JLabel jLabel10;
	private JLabel jLabel12;
	private JLabel jLabel11;
	private JLabel jLabel13;
	private JLabel jLabel14;
	//private JLabel jLabel15;
	private JLabel jLabel16;
	private JLabel jLabel17;
	private Project project;
	private JLabel jLabel18;
	private JLabel jLabel19;
	private int counter;


	/**
	 * @param p
	 */
	public ProjectView(Project project) {

		this.project = project;
		initGUI();
		this.addListenersToGraphicalObjects();
	}

	/**
	 * 
	 */
	private void initGUI() {

		try  {

			BorderLayout thisLayout = new BorderLayout();
			this.setLayout(thisLayout);
			this.jPanel1 = new JPanel();
			this.add(jPanel1, BorderLayout.CENTER);
			jPanel1.setBorder(BorderFactory.createTitledBorder("Project "+project.getName()+" data"));
			jPanel1.setLayout(null);

			jLabel1 = new JLabel();
			jPanel1.add(jLabel1);
			jLabel1.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel2 = new JLabel();
			jPanel1.add(jLabel2);
			jLabel2.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel3 = new JLabel();
			jPanel1.add(jLabel3);
			jLabel3.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel4 = new JLabel();
			jPanel1.add(jLabel4);
			jLabel4.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel6 = new JLabel();
			jPanel1.add(jLabel6);
			jLabel6.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel7 = new JLabel();
			jPanel1.add(jLabel7);
			jLabel7.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel5 = new JLabel();
			jPanel1.add(jLabel5);
			jLabel5.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel8 = new JLabel();
			jPanel1.add(jLabel8);
			jLabel8.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel12 = new JLabel();
			jPanel1.add(jLabel12);
			jLabel12.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel9 = new JLabel();
			jPanel1.add(jLabel9);
			jLabel9.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel10 = new JLabel();
			jPanel1.add(jLabel10);
			jLabel10.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel11 = new JLabel();
			jPanel1.add(jLabel11);
			jLabel11.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel13 = new JLabel();
			jPanel1.add(jLabel13);
			jLabel13.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel14 = new JLabel();
			jPanel1.add(jLabel14);
			jLabel14.setBounds(24, this.increaseCounter(35), 489, 20);

//			jLabel15 = new JLabel();
//			jPanel1.add(jLabel15);
//			jLabel15.setBounds(24, this.increaseCounter(35), 489, 20);
			
			jLabel16 = new JLabel();
			jPanel1.add(jLabel16);
			jLabel16.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel17 = new JLabel();
			jPanel1.add(jLabel17);
			jLabel17.setBounds(24, this.increaseCounter(35), 489, 20);
			
			jLabel18 = new JLabel();
			jPanel1.add(jLabel18);
			jLabel18.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel19 = new JLabel();
			jPanel1.add(jLabel19);
			jLabel19.setBounds(24, this.increaseCounter(35), 489, 20);

			this.fillList();

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void fillList() {

		this.setToolTipText("Click to refresh");
		
		jLabel1.setText("Database: "+project.getDatabase().getMySqlCredentials().get_database_name());

		jLabel2.setText("Host: "+project.getDatabase().getMySqlCredentials().get_database_host());

		jLabel3.setText("Port: "+project.getDatabase().getMySqlCredentials().get_database_port());

		jLabel4.setText("Database login: "+project.getDatabase().getMySqlCredentials().get_database_user());

		if(project.getGenomeCodeName() != null)
		{
			if(project.getGenomeCodeName().equals("")) jLabel6.setText("");
			else jLabel6.setText("Genome Codename: "+project.getGenomeCodeName());
		}

		jLabel7.setText("Is NCBI genome: "+project.isNCBIGenome());

		if(project.getFileName().equals(""))
			jLabel5.setText("Saved project file:");
		else
			jLabel5.setText("Saved project file: "+project.getFileName());

		jLabel8.setText("Is Database Gene data available: "+project.isGeneDataAvailable());

		jLabel12.setText("Is Metabolic Data available: "+project.isMetabolicDataAvailable());

		jLabel9.setText("Is Transporters Search performed: "+project.isSW_TransportersSearch());

		jLabel10.setText("Are Transporters Reactions generated: "+(project.getTransportContainer()!=null));

		jLabel11.setText("Are Transporters Reactions loaded: "+project.isTransporterLoaded());

		jLabel13.setText("Is Compartments Prediction loaded: "+project.isCompartmentsLoaded());

		jLabel14.setText("Is Model compartmentalised: "+project.isCompartmentalisedModel());

		//jLabel15.setText("Using proxy server: "+project.isUseProxy());

		if(project.getProjectID()>0) {

			jLabel16.setText("Project ID: "+project.getProjectID());
		}
		
		if(project.getTaxonomyID()>0) {

			jLabel17.setText("Organism: "+project.getOrganismName());

			jLabel18.setText("Lineage: "+project.getOrganismLineage());

			jLabel19.setText("Taxonomy ID: "+project.getTaxonomyID());
		}

		
	}


	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#updateGraphicalObject()
	 */
	@Override
	public void updateTableUI() {

		this.fillList();
		this.updateUI();
		this.revalidate();
		this.repaint();
	}

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#addListenersToGraphicalObjects(javax.swing.JPanel, javax.swing.JTable)
	 */
	@Override
	public void addListenersToGraphicalObjects() {

		this.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {

				updateTableUI();
			}
		});

		this.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {}

			@Override
			public void focusGained(FocusEvent arg0) {

				updateTableUI();
			}
		});
	}

	@Override
	public String getProjectName() {

		return this.project.getName();
	}

	/**
	 * @return the counter
	 */
	private int increaseCounter(int step) {
		counter = counter+step;
		return counter;
	}
}
