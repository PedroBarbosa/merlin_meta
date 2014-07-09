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

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.SoftBevelBorder;

import utilities.AIBenchUtils;
import utilities.CreateImageIcon;
import datatypes.IntegrateHomologyData;
import datatypes.IntegrateHomologyData.IntegrationType;
import datatypes.metabolic_regulatory.HomologyDataContainer;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;



/**
 * @author ODias
 *
 */
public class HomologyIntegrationGui extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private JPanel jPanel1;
	private JComboBox<IntegrationType> jComboBoxProducts;
	private JCheckBox jCheckPartial;
	private JCheckBox jCheckFull;
	private JLabel jLabelReports;
	private JComboBox<String> jComboBoxReports;
	private JCheckBox jCheckBoxProducts;
	private JLabel jLabelGenes;
	private JComboBox<IntegrationType> jComboBoxEnzymes;
	private JComboBox<IntegrationType> jComboBoxGenes;
	private JPanel jPanel11;
	private JPanel jPanel12;
	private JLabel jLabelEnzymes;
	private JLabel jLabelProducts;
	private HomologyDataContainer homologyDataContainer;

	/**

	/**
	 * @param owner
	 * @param title
	 * @param modal
	 */
	public HomologyIntegrationGui(String title, HomologyDataContainer homologyDataContainer) {
		
		super(Workbench.getInstance().getMainFrame());
		this.homologyDataContainer = homologyDataContainer;
		initGUI(title);
		Utilities.centerOnOwner(this);
		this.setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
		this.setVisible(true);		
		//this.setAlwaysOnTop(true);
		this.toFront();
	}

	/**
	 * Initiate gui method
	 * 
	 * @param title
	 */
	private void initGUI(String title) {

		{
			this.setTitle(title);	
			jPanel1 = new JPanel();
			getContentPane().add(jPanel1, BorderLayout.CENTER);
			GridBagLayout jPanel1Layout = new GridBagLayout();
			jPanel1Layout.columnWeights = new double[] {0.1, 0.0, 0.1, 0.1};
			jPanel1Layout.columnWidths = new int[] {7, 7, 7, 7};
			jPanel1Layout.rowWeights = new double[] {0.1, 0.0, 0.1, 0.0, 0.1, 0.0};
			jPanel1Layout.rowHeights = new int[] {7, 7, 20, 7, 20, 7};
			jPanel1.setLayout(jPanel1Layout);
			jPanel1.setPreferredSize(new java.awt.Dimension(386, 326));


			jPanel11 = new JPanel();
			GridBagLayout jPanel11Layout = new GridBagLayout();
			jPanel11.setLayout(jPanel11Layout);
			jPanel11Layout.columnWeights = new double[] {0.0, 0.1, 0.0};
			jPanel11Layout.columnWidths = new int[] {7, 7, 7};
			jPanel11Layout.rowWeights = new double[] {0.0, 0.1, 0.1, 0.0, 0.0, 0.1, 0.0, 0.1, 0.1, 0.1, 0.0, 0.1, 0.1, 0.0};
			jPanel11Layout.rowHeights = new int[] {7, 20, 7, 7, 7, 7, 7, 20, 20, 7, 7, 20, 20, 7};

			jPanel1.add(jPanel11, new GridBagConstraints(0, 0, 3, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			{
				ComboBoxModel<IntegrationType> jComboBoxGenesModel = new DefaultComboBoxModel<> (IntegrationType.values());
				jComboBoxGenesModel.setSelectedItem(IntegrationType.LOCAL_DATABASE);
				jComboBoxGenes = new JComboBox<>();
				jPanel11.add(jComboBoxGenes, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				jComboBoxGenes.setModel(jComboBoxGenesModel);
				jLabelGenes = new JLabel();
				jPanel11.add(jLabelGenes, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jLabelGenes.setText("Gene names integration preferences");
			}
			{
				ComboBoxModel<IntegrationType> jComboBoxEnzymesModel = 
						new DefaultComboBoxModel<>(IntegrationType.values());
				jComboBoxEnzymes = new JComboBox<>();
				jPanel11.add(jComboBoxEnzymes, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				jComboBoxEnzymes.setModel(jComboBoxEnzymesModel);
				jLabelEnzymes = new JLabel();
				jPanel11.add(jLabelEnzymes, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jLabelEnzymes.setText("Enzymes integration preferences");
			}
			{

				ComboBoxModel<IntegrationType> jComboBoxProductsModel = 
						new DefaultComboBoxModel<>(IntegrationType.values());
				jComboBoxProductsModel.setSelectedItem(IntegrationType.LOCAL_DATABASE);
				jComboBoxProducts = new JComboBox<>();
				jPanel11.add(jComboBoxProducts, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				jComboBoxProducts.setModel(jComboBoxProductsModel);
				jLabelProducts = new JLabel();
				jPanel11.add(jLabelProducts, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jLabelProducts.setText("Product names integration preferences");
				jCheckBoxProducts = new JCheckBox();
				jPanel11.add(jCheckBoxProducts, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jCheckBoxProducts.setText("Load product names for genes not encoding enzymes");
			}
			{
				jLabelReports = new JLabel();
				jPanel11.add(jLabelReports, new GridBagConstraints(1, 11, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jLabelReports.setText("Generate integration reports or Integrate database");
			}
			{
				ComboBoxModel<String> jComboBoxReportsModel = 
						new DefaultComboBoxModel<>(
								new String[] { "Reports", "Integration" });
				jComboBoxReports = new JComboBox<>();
				jPanel11.add(jComboBoxReports, new GridBagConstraints(1, 12, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				jComboBoxReports.setModel(jComboBoxReportsModel);
			}

			jPanel12 = new JPanel();
			GridBagLayout jPanel12Layout = new GridBagLayout();
			jPanel12.setLayout(jPanel12Layout);
			jPanel12Layout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.0};
			jPanel12Layout.columnWidths = new int[] {3, 20, 7, 50};
			jPanel12Layout.rowWeights = new double[] {0.1};
			jPanel12Layout.rowHeights = new int[] {7};
			jPanel12.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));

			jPanel1.add(jPanel12, new GridBagConstraints(0, 4, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			{
				jCheckFull = new JCheckBox();
				jCheckFull.setSelected(true);
				jPanel1.add(jCheckFull, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jCheckFull.setText("Integrate Full EC numbers");
			}
			{
				jCheckPartial = new JCheckBox();
				jCheckPartial.setSelected(true);
				jPanel1.add(jCheckPartial, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jCheckPartial.setText("Integrate Partial EC numbers");
			}

			JButton button1 = new JButton("Ok");
			button1.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")),0.1).resizeImageIcon());
			jPanel12.add(button1, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			button1.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent evt){
					
					IntegrateHomologyData integration = new IntegrateHomologyData(homologyDataContainer, (IntegrationType) jComboBoxGenes.getSelectedItem(), (IntegrationType)jComboBoxEnzymes.getSelectedItem(),
							(IntegrationType)jComboBoxProducts.getSelectedItem(), jCheckFull.isSelected(), jCheckPartial.isSelected());
					
					if(integration.isExistshomologyInstance()) {

						if(integration.performIntegration()) {
							
							if(jComboBoxReports.getSelectedItem().equals("Integration")) {

								integration.loadLocalDatabase(jCheckBoxProducts.isSelected());
								integration.generateReports();
								AIBenchUtils.updateAllViews(homologyDataContainer.getProject().getName());
							}
							else {

								//reports generated separately because there is some error when trying to produce them before the actual integration 
								//(some genes assigned with enzymes loose their assignment ex: KLLA0A00759g	[3.5.4.4] looses the enzymes after some operations- uncomment System.out.println() on IntegrateBLASTData)
								integration.generateReports();
								Workbench.getInstance().info("Reports generated!");
							}
						}
						else {

							Workbench.getInstance().warn("An error occurred while performing the integration!");
						}
					}
					simpleFinish();
				}
			});

			JButton button2 = new JButton("Cancel");
			button2.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")),0.1).resizeImageIcon());
			jPanel12.add(button2, new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			button2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					simpleFinish();
				}
			});

		}
		this.setSize(400, 350);
		//} catch (Exception e) {e.printStackTrace();}
	}

	public void simpleFinish() {

		this.setVisible(false);
		this.dispose();
	}

}
