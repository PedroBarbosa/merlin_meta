package gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.text.NumberFormatter;

import pt.uminho.sysbio.common.biocomponents.container.io.readers.merlinAux.MetaboliteContainer;
import utilities.CreateImageIcon;
import datatypes.metabolic.ReactionsContainer;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;



/**
 * @author ODias
 *
 */
public class InsertEditReaction extends JDialog {

	private static final long serialVersionUID = -8712785333276055182L;
	private ReactionsContainer reactionsContainer;
	private String[][] metabolitesModel;
	private String[] enzymesModel, pathwaysModel, reactants, reactantsStoichiometry, 
	productsStoichiometry, products, reactantsChains, productsChains, reactantsCompartments, productsCompartments, metabolitesCompartmentsModel;	
	private JPanel jPanelDialogReaction, jPanelName, jPanelEquation, jPanelReversible, jPanelSaveClose;
	private JTextField jTextFieldName, jTextFieldEquation;
	private JCheckBox jNonEnzymatic;
	private JCheckBox jSpontaneous;
	private JComboBox<String> jComboBoxLocalisation;
	private JPanel jPanelCompartmentReaction;
	private JRadioButton jBackword;
	private JRadioButton jForward;
	private JScrollPane jScrollPaneEnzymes, jScrollPanePathways, jScrollPaneReactants, jScrollPaneProducts;
	private JButton jApply, jButtonSave, jButtonCancel;
	private JCheckBox jCheckBoxInModel;
	private JRadioButton jRadioButtonReversible, jRadioButtonIrreversible;
	private ButtonGroup reversibility, direction;
	private JComboBox<String>[] reactantsField, productsCompartmentsBox, enzymesField, pathwaysField, reactantsCompartmentsBox, productsField;
	private JTextField[] reactantsStoichiometryField, productsStoichiometryField, reactantsChainsField, productsChainsField;
	private Map<String, MetaboliteContainer> reactionMetabolites;
	private String rowID, selectedPathway;
	protected Map<String, Set<String>> selectedEnzymesAndPathway;
	private boolean newECnumber;
	private boolean applyPressed;
	private JPanel panelReactants, panelProducts;
	private String[] reactionsCompartmentsModel;
	private String defaultCompartment;
	protected boolean initialInModel, inModelNotChanged;
	private JFormattedTextField lowerBoundary, upperBoundary;
	private JCheckBox jIsGeneric;

	/**
	 * @param reactionsContainer
	 * @param rowID
	 */
	public InsertEditReaction(ReactionsContainer reactionsContainer, String rowID) {

		super(Workbench.getInstance().getMainFrame());
		this.newECnumber=false;
		this.reactionsContainer = reactionsContainer;
		this.applyPressed=false;
		this.metabolitesModel = reactionsContainer.getAllMetabolites();
		this.reactionMetabolites = reactionsContainer.getMetabolites(rowID);
		this.metabolitesCompartmentsModel = reactionsContainer.getCompartments(true);
		this.reactionsCompartmentsModel = reactionsContainer.getCompartments(false);
		this.pathwaysModel = reactionsContainer.getPathways();
		this.enzymesModel = reactionsContainer.getEnzymesModel();
		Set<String> enzymesSet = new TreeSet<String>();
		Set<String> allEnzymes = new TreeSet<String>();
		this.selectedPathway="-1allpathwaysinreaction";
		this.selectedEnzymesAndPathway = new TreeMap<String, Set<String>>();

		this.defaultCompartment = reactionsContainer.getDefaultCompartment();

		this.rowID=rowID;

		if(rowID.equals("-10")) {

			this.setTitle("Insert Reaction");
			enzymesSet=new TreeSet<String>();
			this.selectedEnzymesAndPathway.put("-1allpathwaysinreaction", enzymesSet);
			//this.selectedEnzymesAndPathway.put("", new HashSet<String>());
			initGUI();
			this.jRadioButtonReversible.setSelected(true);
			this.jCheckBoxInModel.setSelected(true);
		}
		else {

			this.setTitle("Edit Reaction");
			String[] pathways = reactionsContainer.getPathways(rowID);
			//			this.enzymes = reactionsContainer.getEnzymes(rowID);

			if(pathways == null || pathways.length==0) {

				allEnzymes = reactionsContainer.getEnzymesForReaction(rowID);
			} 
			else {

				for(String pathway : pathways) {

					enzymesSet=new TreeSet<String>();
					enzymesSet.addAll(new TreeSet<String>(Arrays.asList(reactionsContainer.getEnzymes(rowID, reactionsContainer.getPathwayID(pathway)))));
					allEnzymes.addAll(enzymesSet);
					this.selectedEnzymesAndPathway.put(pathway, enzymesSet);
				}
			}

			this.selectedEnzymesAndPathway.put(this.selectedPathway, allEnzymes);

			initGUI();
			this.startFields();
		}

		enzymesSet=new TreeSet<String>();
		Utilities.centerOnOwner(this);
		this.setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
		this.setVisible(true);		
		this.setAlwaysOnTop(true);
		this.toFront();
		Utilities.centerOnOwner(this);
	}

	/**
	 * 
	 */
	private void initGUI() {

		GroupLayout thisLayout = new GroupLayout((JPanel)getContentPane());
		getContentPane().setLayout(thisLayout);
		jPanelDialogReaction = new JPanel(new GridBagLayout());
		GridBagLayout jPanelDialogReactionLayout = new GridBagLayout();
		jPanelDialogReactionLayout.columnWeights = new double[] {0.0, 0.0, 0.0, 0.1, 0.1, 0.0, 0.0, 0.0};
		jPanelDialogReactionLayout.columnWidths = new int[] {8, 400, 15, 15, 7, 150, 15, 8};
		jPanelDialogReactionLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.0, 0.1, 0.0, 0.0, 0.1, 0.0, 0.0};
		jPanelDialogReactionLayout.rowHeights = new int[] {7, 20, 7, 20, 7, 15, 7, 12, 145, 12, 170, 7, 12, 170, 10, 15};
		jPanelDialogReaction.setLayout(jPanelDialogReactionLayout);
		thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
				.addContainerGap().addComponent(jPanelDialogReaction, 0, 740, Short.MAX_VALUE));
		thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup().addComponent(jPanelDialogReaction, 0, 720, Short.MAX_VALUE));

		{
			jPanelName = new JPanel();
			GridBagLayout jPanelNameLayout = new GridBagLayout();
			jPanelDialogReaction.add(jPanelName, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jPanelNameLayout.rowWeights = new double[] {0.1};
			jPanelNameLayout.rowHeights = new int[] {7};
			jPanelNameLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
			jPanelNameLayout.columnWidths = new int[] {7, 7, 7};
			jPanelName.setLayout(jPanelNameLayout);
			jPanelName.setBorder(BorderFactory.createTitledBorder(null, "Reaction Name", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
			{
				jTextFieldName = new JTextField();
				jTextFieldName.setPreferredSize(new Dimension(180,26));
				jPanelName.add(jTextFieldName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}
		}
		{
			jScrollPanePathways = new JScrollPane();
			jScrollPanePathways.setViewportView(addPathways());
			jPanelDialogReaction.add(jScrollPanePathways, new GridBagConstraints(1, 7, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jPanelDialogReaction.add(jButtonAddPathways(), new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			JPanel boundaries = new JPanel();
			GridBagLayout jPanelBoundariesLayout = new GridBagLayout();
			jPanelBoundariesLayout.rowWeights = new double[] {0.1, 0.0, 0.1};
			jPanelBoundariesLayout.rowHeights = new int[] {20, 7, 20};
			jPanelBoundariesLayout.columnWeights = new double[] {0.1, 0.0, 0.1};
			jPanelBoundariesLayout.columnWidths = new int[] {20, 7, 20};
			boundaries.setLayout(jPanelBoundariesLayout);
			boundaries.setBorder(BorderFactory.createTitledBorder(null, "Flux Boundaries", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
			jPanelDialogReaction.add(boundaries, new GridBagConstraints(5, 4, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			JLabel lowerBoundaries = new JLabel("Lower");
			boundaries.add(lowerBoundaries, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			JLabel upperBoundaries = new JLabel("Upper");
			boundaries.add(upperBoundaries, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			
			NumberFormat format = NumberFormat.getInstance();
		    NumberFormatter formatter = new NumberFormatter(format);
		    formatter.setValueClass(Integer.class);
		    formatter.setMinimum(Integer.MIN_VALUE);
		    formatter.setMaximum(Integer.MAX_VALUE);
		    formatter.setCommitsOnValidEdit(true);
			
		    this.lowerBoundary = new JFormattedTextField(formatter);
		    this.lowerBoundary.setText("-10000");
			boundaries.add(this.lowerBoundary, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			this.upperBoundary = new JFormattedTextField(formatter);
			this.upperBoundary.setText("10000");
			boundaries.add(this.upperBoundary, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			jPanelReversible = new JPanel();
			GridBagLayout jPanelReversibleLayout = new GridBagLayout();
			jPanelDialogReaction.add(jPanelReversible, new GridBagConstraints(5, 1, 1, 3, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jPanelReversibleLayout.rowWeights = new double[] {0.1, 0.1, 0.1};
			jPanelReversibleLayout.rowHeights = new int[] {7, 20, 20};
			jPanelReversibleLayout.columnWeights = new double[] {0.0, 0.1};
			jPanelReversibleLayout.columnWidths = new int[] {75, 20};
			jPanelReversible.setLayout(jPanelReversibleLayout);
			jPanelReversible.setBorder(BorderFactory.createTitledBorder(null, "Reversibility", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
			{
				jRadioButtonReversible = new JRadioButton();
				jPanelReversible.add(jRadioButtonReversible, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jRadioButtonReversible.setText("Reversible");
				jRadioButtonReversible.setToolTipText("Reversible");
				jRadioButtonReversible.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {

						if(jRadioButtonReversible.isSelected()) {

							String equation = jTextFieldEquation.getText().replace(" <= ", " <=> ").replace(" => ", " <=> ");
							jTextFieldEquation.setText(equation);
							jTextFieldEquation.setToolTipText(equation);

							jForward.setEnabled(false);
							jBackword.setEnabled(false);
							panelReactants.setBorder(BorderFactory.createTitledBorder(null, "Reactants", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
							panelProducts.setBorder(BorderFactory.createTitledBorder(null, "Products", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
						}
					}
				});
			}
			{
				jRadioButtonIrreversible = new JRadioButton();
				jPanelReversible.add(jRadioButtonIrreversible, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jRadioButtonIrreversible.setText("Irreversible");
				jRadioButtonIrreversible.setToolTipText("Irreversible");
				jRadioButtonIrreversible.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {

						if(jRadioButtonIrreversible.isSelected()) {

							jForward.setEnabled(true);
							jForward.setSelected(true);
							String equation  = jTextFieldEquation.getText().replace(" <=> ", " => ").replace(" <= ", " => ");
							jTextFieldEquation.setText(equation);
							jTextFieldEquation.setToolTipText(equation);
							jBackword.setEnabled(true);
							panelReactants.setBorder(BorderFactory.createTitledBorder(null, "Reactants", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
							panelProducts.setBorder(BorderFactory.createTitledBorder(null, "Products", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
						}
					}
				});
			}
			{
				reversibility = new ButtonGroup();
				reversibility.add(jRadioButtonIrreversible);
				reversibility.add(jRadioButtonReversible);
			}

			{
				jForward = new JRadioButton();
				jForward.setEnabled(false);
				jForward.setText("=>");
				jForward.setToolTipText("Forward");
				jPanelReversible.add(jForward, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jForward.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {

						if(jForward.isSelected()) {

							String equation  = jTextFieldEquation.getText().replace(" <=> ", " => ").replace(" <= ", " => ");
							jTextFieldEquation.setText(equation);
							jTextFieldEquation.setToolTipText(equation);
							panelReactants.setBorder(BorderFactory.createTitledBorder(null, "Reactants", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
							panelProducts.setBorder(BorderFactory.createTitledBorder(null, "Products", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
						}
					}
				});

				jBackword = new JRadioButton();
				jBackword.setEnabled(false);
				jBackword.setText("<=");
				jBackword.setToolTipText("Backward");
				jPanelReversible.add(jBackword, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jBackword.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {

						if(jBackword.isSelected()) {

							String equation = jTextFieldEquation.getText().replace(" <=> ", " <= ").replace(" => ", " <= ");
							jTextFieldEquation.setText(equation);
							jTextFieldEquation.setToolTipText(equation);
							panelReactants.setBorder(BorderFactory.createTitledBorder(null, "Products", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
							panelProducts.setBorder(BorderFactory.createTitledBorder(null, "Reactants", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
						}
					}
				});
			}
			{
				direction = new ButtonGroup();
				direction.add(jForward);
				direction.add(jBackword);
			}
		}

		{
			jPanelEquation = new JPanel();
			GridBagLayout jPanelEquationLayout = new GridBagLayout();
			jPanelDialogReaction.add(jPanelEquation, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			jPanelEquationLayout.rowWeights = new double[] {0.1};
			jPanelEquationLayout.rowHeights = new int[] {7};
			jPanelEquationLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
			jPanelEquationLayout.columnWidths = new int[] {7, 7, 7};
			jPanelEquation.setLayout(jPanelEquationLayout);
			jPanelEquation.setBorder(BorderFactory.createTitledBorder(null, "Equation", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
			{
				jTextFieldEquation = new JTextField();
				jPanelEquation.add(jTextFieldEquation, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jTextFieldEquation.setToolTipText(jTextFieldEquation.getText());
			}
		}

		{
			jPanelSaveClose = new JPanel(new GridBagLayout());
			GridBagLayout jPanelSaveCloseLayout = new GridBagLayout();
			jPanelSaveCloseLayout.rowWeights = new double[] {0.1, 0.1, 0.1};
			jPanelSaveCloseLayout.rowHeights = new int[] {5, 10, 5};
			jPanelSaveCloseLayout.columnWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
			jPanelSaveCloseLayout.columnWidths = new int[] {8, 86, 13, 86, 13, 86, 8};
			//set layout
			jPanelSaveClose.setLayout(jPanelSaveCloseLayout);
			{
				jButtonSave = new JButton();
				jPanelSaveClose.add(jButtonSave, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jButtonSave.setText("Save");
				jButtonSave.setToolTipText("Save");
				jButtonSave.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Save.png")),0.1).resizeImageIcon());
				jButtonSave.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						boolean metabolites = false;

						if(jTextFieldName.getText().isEmpty()) {

							Workbench.getInstance().warn("Please name the reaction.");
						}
						else
						{
							for(int s=0;s<reactantsField.length;s++) {

								if(reactantsField[s].getSelectedItem().toString().isEmpty() && reactantsStoichiometryField[s].getText().toString().isEmpty()) {

									metabolites=true;
								}
							}

							for(int s=0;s<productsField.length;s++) {

								if(productsField[s].getSelectedItem().toString().isEmpty() &&
										productsStoichiometryField[s].getText().toString().isEmpty()) {

									metabolites=true;
								}
							}
							if(metabolites) {

								Workbench.getInstance().warn("One or more metabolites or their stoichiometry is(are) empty!");							
							}
							else {

								saveData();
								closeAndUpdate();
							}
						}
					}
				});
			}
			{
				jButtonCancel = new JButton();
				jPanelSaveClose.add(jButtonCancel, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jButtonCancel.setText("Close");
				jButtonCancel.setToolTipText("Close");
				jButtonCancel.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")), 0.1).resizeImageIcon());
				jButtonCancel.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0) {
						if(applyPressed)
						{
							closeAndUpdate();
						}
						else
						{
							close();
						}
					}
				});			}
			{
				jApply = new JButton();
				jPanelSaveClose.add(jApply, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jApply.setText("Apply");
				jApply.setToolTipText("Apply");
				jApply.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")), 0.1).resizeImageIcon());
				jApply.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						boolean metabolites = false;

						if(jTextFieldName.getText().isEmpty()) {

							Workbench.getInstance().warn("Please name the reaction.");
						}
						else {

							for(int s=0;s<reactantsField.length;s++) {

								if(reactantsField[s].getSelectedItem().toString().isEmpty())// && reactantsStoichiometryField[s].getText().toString().isEmpty())
								{metabolites=true;}
							}

							for(int s=0;s<productsField.length;s++) {

								if(productsField[s].getSelectedItem().toString().isEmpty())// && productsStoichiometryField[s].getText().toString().isEmpty())
								{metabolites=true;}
							}

							if(metabolites) {

								Workbench.getInstance().warn("One or more metabolites is(are) not selected!");							
							}
							else {

								saveData();
								rowID = reactionsContainer.getReactionID(jTextFieldName.getText());
							}
							applyPressed=true;
						}
					}
				});

			}
			jPanelDialogReaction.add(jPanelSaveClose, new GridBagConstraints(1, 15, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}

		{
			reactants = new String[0]; //reactants = new String[2][0];
			reactantsStoichiometry = new String[0];
			reactantsChains= new String[0]; reactantsCompartments= new String[0];
			products = new String[0]; //products = new String[2][0];
			productsStoichiometry = new String[0];
			productsChains= new String[0];	productsCompartments= new String[0];
			List<String> r = new ArrayList<String>();
			List<String> p = new ArrayList<String>();
			List<String> rs = new ArrayList<String>();
			List<String> rc = new ArrayList<String>();
			List<String> ps = new ArrayList<String>();
			List<String> pc = new ArrayList<String>();
			List<String> compartmentReactant = new ArrayList<String>();
			List<String> compoundID_R = new ArrayList<String>();
			List<String> compartmentProduct = new ArrayList<String>();
			List<String> compoundID_P = new ArrayList<String>();

			for(String m : reactionMetabolites.keySet()) {


				if((reactionMetabolites.get(m).getStoichiometric_coefficient()+"").startsWith("-")) {

					r.add(reactionMetabolites.get(m).getCompartment_name());
					rs.add(reactionMetabolites.get(m).getStoichiometric_coefficient()+"");
					rc.add(reactionMetabolites.get(m).getNumberofchains());
					compartmentReactant.add(reactionMetabolites.get(m).getCompartment_name());
					compoundID_R.add(reactionMetabolites.get(m).getMetaboliteID());
				}
				else {

					p.add(reactionMetabolites.get(m).getCompartment_name());
					ps.add(reactionMetabolites.get(m).getStoichiometric_coefficient()+"");
					pc.add(reactionMetabolites.get(m).getNumberofchains());
					compartmentProduct.add(reactionMetabolites.get(m).getCompartment_name());
					compoundID_P.add(reactionMetabolites.get(m).getMetaboliteID());
				}
			}

			if(!rowID.equals("-10")) {

				reactants = compoundID_R.toArray(reactants);
				reactantsStoichiometry = rs.toArray(reactantsStoichiometry);
				reactantsChains = rc.toArray(reactantsChains);
				reactantsCompartments = compartmentReactant.toArray(reactantsCompartments);

				products = compoundID_P.toArray(products);
				productsStoichiometry = ps.toArray(productsStoichiometry);
				productsChains = pc.toArray(productsChains);
				productsCompartments = compartmentProduct.toArray(productsCompartments);
			}

			jScrollPaneReactants = new JScrollPane();
			panelReactants = this.addReactantsPanel();
			jScrollPaneReactants.setViewportView(panelReactants);
			jScrollPaneProducts = new JScrollPane();
			panelProducts = this.addProductsPanel();
			jScrollPaneProducts.setViewportView(panelProducts);

			jPanelDialogReaction.add(jScrollPaneReactants, new GridBagConstraints(1, 9, 5, 2, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jPanelDialogReaction.add(jScrollPaneProducts, new GridBagConstraints(1, 12, 5, 2, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}

		//enzymes pane
		{
			jScrollPaneEnzymes = new JScrollPane();
			jScrollPaneEnzymes.setViewportView(addEnzymes());
			jPanelDialogReaction.add(jScrollPaneEnzymes, new GridBagConstraints(3, 7, 3, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jPanelDialogReaction.add(jButtonAddEnzyme(), new GridBagConstraints(6, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		//ButtonAddReactant
		{
			jPanelDialogReaction.add(jButtonAddReactant(), new GridBagConstraints(6, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		//ButtonAddProduct
		{
			jPanelDialogReaction.add(jButtonAddProduct(), new GridBagConstraints(6, 12, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		}
		//  Check Box In Model
		{
			
			GridBagLayout jPanelPropertiesLayout = new GridBagLayout();
			jPanelPropertiesLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
			jPanelPropertiesLayout.rowHeights = new int[] {10, 5, 10, 5, 10, 5, 10};
			jPanelPropertiesLayout.columnWeights = new double[] {0.1, 0.1};
			jPanelPropertiesLayout.columnWidths = new int[] {20, 20};
			JPanel jPanelProperties = new JPanel(jPanelPropertiesLayout);
			jPanelProperties.setBorder(BorderFactory.createTitledBorder(null, "Properties", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
			jPanelDialogReaction.add(jPanelProperties, new GridBagConstraints(2, 1, 2, 5, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			
			jCheckBoxInModel = new JCheckBox();
			jCheckBoxInModel.setText("In Model");
			jCheckBoxInModel.setToolTipText("In Model");
			jPanelProperties.add(jCheckBoxInModel, new GridBagConstraints(2, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jSpontaneous = new JCheckBox();
			jSpontaneous.setText("Spontaneous");
			jSpontaneous.setToolTipText("Spontaneous");
			jPanelProperties.add(jSpontaneous, new GridBagConstraints(2, 2, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jNonEnzymatic = new JCheckBox();
			jNonEnzymatic.setText("Non Enzymatic");
			jNonEnzymatic.setToolTipText("Non Enzymatic");
			jPanelProperties.add(jNonEnzymatic, new GridBagConstraints(2, 4, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jIsGeneric = new JCheckBox();
			jIsGeneric.setText("Is Generic");
			jIsGeneric.setToolTipText("Is Generic");
			jPanelProperties.add(jIsGeneric, new GridBagConstraints(2, 6, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			
		}
		//reactionlocalisation
		{
			ComboBoxModel<String> jComboBoxLocalisationModel = new DefaultComboBoxModel<>(reactionsCompartmentsModel);
			jComboBoxLocalisation = new JComboBox<>();
			jComboBoxLocalisation.setModel(jComboBoxLocalisationModel);
			jPanelCompartmentReaction = new JPanel();
			GridBagLayout jPanelCompartmentReactionLayout = new GridBagLayout();
			jPanelCompartmentReaction.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder(""), "Localisation", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION));
			jPanelCompartmentReactionLayout.rowWeights = new double[] {0.0};
			jPanelCompartmentReactionLayout.rowHeights = new int[] {7};
			jPanelCompartmentReactionLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
			jPanelCompartmentReactionLayout.columnWidths = new int[] {7, 7, 7};
			jPanelCompartmentReaction.setLayout(jPanelCompartmentReactionLayout);
			jPanelCompartmentReaction.add(jComboBoxLocalisation, new GridBagConstraints(1, -1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jPanelDialogReaction.add(jPanelCompartmentReaction, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}

		this.setModal(true);
		pack();

	}

	/**
	 * 
	 */
	public void close(){}

	/**
	 * 
	 */
	public void closeAndUpdate(){}

	/**
	 * @return reactants panel
	 */
	private JPanel addReactantsPanel() {

		JPanel panelReactants = new JPanel();
		GridBagLayout jPanelReactantsLayout = new GridBagLayout();

		//number of rows (array size) 
		if(reactants.length==0) {

			jPanelReactantsLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1};
			jPanelReactantsLayout.rowHeights = new int[] {7, 7, 7, 7};
		}
		else {

			jPanelReactantsLayout.rowWeights = new double[reactants.length*2+2];
			jPanelReactantsLayout.rowHeights  = new int[reactants.length*2+2];

			for(int rh=0; rh<reactants.length*2+2; rh++) {

				jPanelReactantsLayout.rowWeights[rh]=0.1;
				jPanelReactantsLayout.rowHeights[rh]=7;
			}
		}
		//number of columns (array size)
		jPanelReactantsLayout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0};
		jPanelReactantsLayout.columnWidths = new int[] {1, 7, 1, 7, 1, 7, 1, 7, 1};
		//set layout
		panelReactants.setLayout(jPanelReactantsLayout);
		panelReactants.setBorder(BorderFactory.createTitledBorder(null, "Reactants", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
		panelReactants.add(new JLabel("Metabolite"), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelReactants.add(new JLabel("Stoichiometry"), new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelReactants.add(new JLabel("Chains number"), new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelReactants.add(new JLabel("Localization"), new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelReactants.add(this.addCompartmentButton(), new GridBagConstraints(7, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		if(reactants.length==0) {

			reactants = new String[1];
			reactants[0]= "";
			reactantsField = new JComboBox[1];
			reactantsField[0]=new JComboBox<String>(metabolitesModel[1]);
			reactantsField[0].setPreferredSize(new Dimension(250, 26));
			reactantsField[0].setSelectedIndex(0);
			reactantsField[0].setToolTipText(reactantsField[0].getSelectedItem().toString());
			reactantsField[0].addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					comboBoxActionListener(reactantsField,(JComboBox<String>) arg0.getSource());
					((JComponent) arg0.getSource()).setToolTipText(((JComboBox<String>) arg0.getSource()).getSelectedItem().toString());
				}
			});

			reactantsStoichiometry = new String[1];
			reactantsStoichiometry[0] = "1";
			reactantsStoichiometryField = new JTextField[1];
			reactantsStoichiometryField[0] = new JTextField();
			reactantsStoichiometryField[0].setText(reactantsStoichiometry[0]);

			reactantsChainsField = new JTextField[1];
			reactantsChains = new String[1];
			reactantsChains[0]="1";
			reactantsChainsField[0]= new JTextField();
			reactantsChainsField[0].setText(reactantsChains[0]);

			reactantsCompartments=new String[1];
			reactantsCompartments[0]=this.defaultCompartment;
			reactantsCompartmentsBox = new JComboBox[1];
			reactantsCompartmentsBox[0] = new JComboBox<String>(metabolitesCompartmentsModel);
			//reactantsCompartmentsBox[0].setSelectedItem(reactantsCompartments[0]);
			reactantsCompartmentsBox[0].setSelectedIndex(0);

			panelReactants.add(reactantsField[0], new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			panelReactants.add(reactantsStoichiometryField[0], new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			panelReactants.add(reactantsChainsField[0], new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			panelReactants.add(reactantsCompartmentsBox[0], new GridBagConstraints(7, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		else {

			reactantsField = new JComboBox[reactants.length];
			reactantsStoichiometryField = new JTextField[reactantsStoichiometry.length];
			reactantsChainsField =  new JTextField[reactantsStoichiometry.length];
			reactantsCompartmentsBox = new JComboBox[reactantsCompartments.length];

			for(int s=0; s<reactants.length; s++) {

				reactantsField[s]= new JComboBox<String>(metabolitesModel[1]);
				reactantsField[s].setPreferredSize(new Dimension(250, 26));
				reactantsField[s].setSelectedIndex(Arrays.asList(metabolitesModel[0]).indexOf(reactants[s]));
				reactantsField[s].setToolTipText( reactantsField[s].getSelectedItem().toString());
				reactantsField[s].addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {

						comboBoxActionListener(reactantsField,(JComboBox<String>) arg0.getSource());
						((JComponent) arg0.getSource()).setToolTipText(((JComboBox<String>) arg0.getSource()).getSelectedItem().toString());
					}
				});

				reactantsStoichiometryField[s] = new JTextField();
				reactantsChainsField[s] = new JTextField();;
				if(reactantsStoichiometry.length<=s)
				{
					reactantsStoichiometry[s]="1";
					reactantsChains[s]="1";
				}
				reactantsStoichiometryField[s].setText(reactantsStoichiometry[s]);
				reactantsChainsField[s].setText(reactantsChains[s]);

				reactantsCompartmentsBox[s] = new JComboBox<String>(metabolitesCompartmentsModel);
				reactantsCompartmentsBox[s].setSelectedItem(reactantsCompartments[s]);

				int r =s*2+2;
				panelReactants.add(reactantsField[s], new GridBagConstraints(1, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				panelReactants.add(reactantsStoichiometryField[s], new GridBagConstraints(3, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				panelReactants.add(reactantsChainsField[s], new GridBagConstraints(5, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				panelReactants.add(reactantsCompartmentsBox[s], new GridBagConstraints(7, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			}
		}

		return panelReactants;
	}

	/**
	 * @return products panel
	 */
	private JPanel addProductsPanel() {

		JPanel panelProducts = new JPanel();
		GridBagLayout jPanelProductsLayout = new GridBagLayout();

		//number of rows (array size)
		if(products.length==0) {

			jPanelProductsLayout.rowWeights = new double[] {0.1, 0.0, 0.1};
			jPanelProductsLayout.rowHeights = new int[] {7, 7, 7};
		}
		else {

			jPanelProductsLayout.rowWeights = new double[products.length*2+1];
			jPanelProductsLayout.rowHeights  = new int[products.length*2+1];

			for(int rh=0; rh<products.length*2+1; rh++) {

				jPanelProductsLayout.rowWeights[rh]=0.1;
				jPanelProductsLayout.rowHeights[rh]=7;
			}
		}

		//number of columns (array size)
		jPanelProductsLayout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0};
		jPanelProductsLayout.columnWidths = new int[] {1, 7, 1, 7, 1, 7, 1, 7, 1};
		//set layout
		panelProducts.setLayout(jPanelProductsLayout);
		panelProducts.setBorder(BorderFactory.createTitledBorder(null, "Products", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
		panelProducts.add(new JLabel("Metabolite"), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelProducts.add(new JLabel("Stoichiometry"), new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelProducts.add(new JLabel("Chains number"), new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelProducts.add(new JLabel("Localization"), new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelProducts.add(this.addCompartmentButton(), new GridBagConstraints(7, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		if(products.length==0) {

			products = new String[1];
			products[0]= "";
			productsField = new JComboBox[1];
			productsField[0]=new JComboBox<String>(metabolitesModel[1]);
			productsField[0].setPreferredSize(new Dimension(250, 26));
			productsField[0].setSelectedIndex(0);
			productsField[0].setToolTipText(productsField[0].getSelectedItem().toString());
			productsField[0].addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					comboBoxActionListener(productsField,(JComboBox<String>) arg0.getSource());
					((JComponent) arg0.getSource()).setToolTipText(((JComboBox<String>) arg0.getSource()).getSelectedItem().toString());
				}});

			productsStoichiometry = new String[1];
			productsStoichiometry[0]= "1";
			productsStoichiometryField = new JTextField[1];
			productsStoichiometryField[0] = new JTextField();
			productsStoichiometryField[0].setText("1");

			productsChains = new String[1];
			productsChains[0]="1";
			productsChainsField = new JTextField[1];
			productsChainsField[0] = new JTextField();
			productsChainsField[0].setText("1");

			productsCompartments=new String[1];
			productsCompartments[0]=this.defaultCompartment;
			productsCompartmentsBox = new JComboBox[1];
			productsCompartmentsBox[0] = new JComboBox<String>(metabolitesCompartmentsModel);
			productsCompartmentsBox[0].setSelectedIndex(0);


			panelProducts.add(productsField[0], new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			panelProducts.add(productsStoichiometryField[0], new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			panelProducts.add(productsChainsField[0], new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			panelProducts.add(productsCompartmentsBox[0], new GridBagConstraints(7, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		}
		else {

			productsField = new JComboBox[products.length];
			productsStoichiometryField = new JTextField[products.length];
			productsChainsField = new JTextField[products.length];
			productsCompartmentsBox = new JComboBox[productsCompartments.length];

			for(int s=0; s<products.length; s++) {

				productsField[s]= new JComboBox<String>(metabolitesModel[1]);
				productsField[s].setPreferredSize(new Dimension(250, 26));
				productsField[s].setSelectedIndex(Arrays.asList(metabolitesModel[0]).indexOf((products[s])));
				productsField[s].setToolTipText(productsField[s].getSelectedItem().toString());
				productsField[s].addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {

						comboBoxActionListener(productsField,(JComboBox<String>) arg0.getSource());
						((JComponent) arg0.getSource()).setToolTipText(((JComboBox<String>) arg0.getSource()).getSelectedItem().toString());
					}});

				productsStoichiometryField[s] = new JTextField();
				productsChainsField[s] = new JTextField();
				if(productsStoichiometry.length<=s) {

					productsStoichiometry[s]="1";
					productsChains[s]="1";
				}
				productsStoichiometryField[s].setText(productsStoichiometry[s]);
				productsChainsField[s].setText(productsChains[s]);
				productsCompartmentsBox[s] = new JComboBox<String>(metabolitesCompartmentsModel);
				productsCompartmentsBox[s].setSelectedItem(productsCompartments[s]);

				int r =s*2+2;
				panelProducts.add(productsField[s], new GridBagConstraints(1, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				panelProducts.add(productsStoichiometryField[s], new GridBagConstraints(3, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				panelProducts.add(productsChainsField[s], new GridBagConstraints(5, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				panelProducts.add(productsCompartmentsBox[s], new GridBagConstraints(7, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			}
		}

		return panelProducts;
	}

	/**
	 * @param pathway
	 * @return
	 */
	private JPanel addEnzymes() {

		JPanel jPanelEnzyme;
		jPanelEnzyme = new JPanel();
		GridBagLayout jPanelEnzymeLayout = new GridBagLayout();
		boolean noEnzyme=false;

		String[] enzymes = new String[0];

		if(this.selectedEnzymesAndPathway.containsKey(selectedPathway)) {

			enzymes = new String[this.selectedEnzymesAndPathway.get(selectedPathway).size()];

			int i = 0;
			for(String enzyme : this.selectedEnzymesAndPathway.get(selectedPathway)) {

				enzymes[i] = enzyme;
				i++;
			}
		}

		if(enzymes.length==0) {

			jPanelEnzymeLayout.rowWeights = new double[] {0.1, 0.0, 0.1};
			jPanelEnzymeLayout.rowHeights = new int[] {7, 7, 7};
			enzymes=new String[1];
			noEnzyme=true;
		}
		else {

			jPanelEnzymeLayout.rowWeights = new double[enzymes.length*2+1];
			jPanelEnzymeLayout.rowHeights  = new int[enzymes.length*2+1];

			for(int rh=0; rh<enzymes.length*2+1; rh++) {

				jPanelEnzymeLayout.rowWeights[rh]=0.1;
				jPanelEnzymeLayout.rowHeights[rh]=7;
			}
		}
		jPanelEnzymeLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
		jPanelEnzymeLayout.columnWidths = new int[] {7, 7, 7};
		jPanelEnzyme.setLayout(jPanelEnzymeLayout);

		if(selectedPathway.equals("-1allpathwaysinreaction")) {

			jPanelEnzyme.setBorder(BorderFactory.createTitledBorder(null, "Enzymes", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
		}
		else {

			jPanelEnzyme.setBorder(BorderFactory.createTitledBorder(null, "Enzymes in "+selectedPathway, TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
		}


		if(noEnzyme) {

			enzymesField = new JComboBox[1];
			enzymesField[0] = new JComboBox<String>(enzymesModel);

			if(enzymes[0]!=null && !enzymes[0].equals("")) {

				enzymesField[0].setSelectedItem(enzymes[0]);
				enzymesField[0].setToolTipText(enzymesField[0].getSelectedItem().toString());
			}

			jPanelEnzyme.add(enzymesField[0], new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			Set<String> enzymesSet=new TreeSet<String>();

			if(!enzymesField[0].getSelectedItem().toString().equals("")) {

				enzymesSet.add(enzymesField[0].getSelectedItem().toString());
			}

			if(selectedEnzymesAndPathway.size()>0) {

				selectedEnzymesAndPathway.put(selectedPathway, enzymesSet);
			}

			enzymesField[0].addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {

					enzymesAction(arg0);
				}
			});
		}
		else {

			Set<String> enzymesSet=new TreeSet<String>();
			enzymesField = new JComboBox[enzymes.length];

			for(int e=0; e<enzymes.length;e++) {

				int r =e*2+1;
				enzymesField[e] = new JComboBox<String>(enzymesModel);

				if(enzymes[e]!=null && !enzymes[e].equals("")) {

					enzymesField[e].setSelectedItem(enzymes[e]);
					enzymesField[e].setToolTipText(enzymesField[e].getSelectedItem().toString());
				}

				jPanelEnzyme.add(enzymesField[e], new GridBagConstraints(1, r, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

				if(!enzymesField[e].getSelectedItem().toString().equals("")) {

					enzymesSet.add(enzymesField[e].getSelectedItem().toString());
				}

				if(selectedEnzymesAndPathway.size()>0) {

					selectedEnzymesAndPathway.put(selectedPathway, enzymesSet);
				}

				enzymesField[e].addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent arg0) {

						enzymesAction(arg0);
					}
				});
			}
		}
		//System.out.println(selectedEnzymesAndPathway);
		return jPanelEnzyme;
	}

	/**
	 * @return
	 */
	private JPanel addPathways() {

		JPanel jPanelPathway;
		GridBagLayout jPanelPathwayLayout = new GridBagLayout();
		boolean noPathway=false;
		String[] pathways = new String[this.selectedEnzymesAndPathway.size()-1];
		int i = 0;

		for(String pathway : this.selectedEnzymesAndPathway.keySet()) {

			if(pathway.equals("-1allpathwaysinreaction")) {

				//pathways[0] = pathway;
			}
			else
			{

				pathways[i] = pathway;
				i++;
			}
		}
		if(pathways.length == 0) {

			jPanelPathwayLayout.rowWeights = new double[] {0.1, 0.0, 0.1};
			jPanelPathwayLayout.rowHeights = new int[] {7, 7, 7};
			pathways=new String[1];
			noPathway=true;
		}
		else {

			jPanelPathwayLayout.rowWeights = new double[pathways.length*2+1];
			jPanelPathwayLayout.rowHeights  = new int[pathways.length*2+1];

			for(int rh = 0; rh < pathways.length *2+1; rh++) {

				jPanelPathwayLayout.rowWeights[rh]=0.1;
				jPanelPathwayLayout.rowHeights[rh]=7;
			}
		}
		jPanelPathwayLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
		jPanelPathwayLayout.columnWidths = new int[] {7, 7, 7};
		jPanelPathway= new JPanel();
		jPanelPathway.setLayout(jPanelPathwayLayout);
		jPanelPathway.setBorder(BorderFactory.createTitledBorder(null, "Pathways", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));

		if(noPathway) {

			pathwaysField = new JComboBox[1];
			pathwaysField[0] = new JComboBox<String>(pathwaysModel);

			if(pathways[0]!= null && !pathways[0].equals("")) {

				pathwaysField[0].setSelectedItem(pathways[0]);
				pathwaysField[0].setToolTipText(pathwaysField[0].getSelectedItem().toString());
			}

			pathwaysField[0].setPreferredSize(new Dimension(28, 26));
			jPanelPathway.add(pathwaysField[0], new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			pathwaysField[0].addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) {}
				@Override
				public void mousePressed(MouseEvent e) {}
				@Override
				public void mouseExited(MouseEvent e) {}
				@Override
				public void mouseEntered(MouseEvent e) {}
				@Override
				public void mouseClicked(MouseEvent e) {

					pathwaysClick(((JComboBox<String>) e.getSource()).getSelectedItem().toString());
				}
			});
			pathwaysField[0].addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent arg0) {

					List<String> paths = new ArrayList<String>();
					paths.add("-1allpathwaysinreaction");

					for(JComboBox<String> jPathway : pathwaysField) {

						String pathway = jPathway.getSelectedItem().toString();

						if(!pathway.equals("")){

							paths.add(pathway);
						}
					}

					pathwaysAction(paths, ((JComboBox<String>) arg0.getSource()).getSelectedItem().toString());
				}
			});
		}
		else {

			pathwaysField = new JComboBox[pathways.length];

			for(int p = 0 ; p < pathways.length ; p++) {

				pathwaysField[p] = new JComboBox<String>(pathwaysModel);

				if(pathways[p]!= null && !pathways[p].equals("")) {

					pathwaysField[p].setSelectedItem(pathways[p]);
					pathwaysField[p].setToolTipText(pathwaysField[p].getSelectedItem().toString());
				}
				pathwaysField[p].addMouseListener(new MouseListener() {
					@Override
					public void mouseReleased(MouseEvent e) {}
					@Override
					public void mousePressed(MouseEvent e) {}
					@Override
					public void mouseExited(MouseEvent e) {}
					@Override
					public void mouseEntered(MouseEvent e) {}
					@Override
					public void mouseClicked(MouseEvent e) {

						pathwaysClick(((JComboBox<String>) e.getSource()).getSelectedItem().toString());
					}
				});
				pathwaysField[p].addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent arg0) {


						List<String> paths = new ArrayList<String>();
						//paths.add("-1allpathwaysinreaction");

						for(JComboBox<String> jPathway : pathwaysField) {

							String pathway = jPathway.getSelectedItem().toString();

							if(!pathway.equals("")){

								paths.add(pathway);
							}
						}

						pathwaysAction(paths, ((JComboBox<String>) arg0.getSource()).getSelectedItem().toString());
					}
				});

				pathwaysField[p].setPreferredSize(new Dimension(28, 26));
				int r =p*2+1;
				jPanelPathway.add(pathwaysField[p], new GridBagConstraints(1, r, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			}
		}
		jPanelPathway.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {

				pathwaysPanelClick();
			}
		});
		return jPanelPathway;
	}

	/**
	 * @return
	 */
	private Component addCompartmentButton() {

		JButton add = new JButton();
		add.setIcon((new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")), 0.03).resizeImageIcon()));
		add.setText("Compartment");
		add.setToolTipText("add Compartment");
		add.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				new InsertCompartment(reactionsContainer)
				{
					private static final long serialVersionUID = 4850184611256705194L;
					public void finish() {
						this.setVisible(false);
						this.dispose();
						metabolitesCompartmentsModel = reactionsContainer.getCompartments(true);
						jScrollPaneReactants.setViewportView(addReactantsPanel());
						jScrollPaneProducts.setViewportView(addProductsPanel());
						System.gc();
					}						
				};

			}});
		return add;
	}

	/**
	 * Save data 
	 */
	private void saveData() {

		Map<String, String> metabolites = new TreeMap<String, String>();
		Map<String, String > chains = new TreeMap<String, String >(), compartments = new TreeMap<String, String >();

		for(int i=0; i< reactantsField.length; i++) {

			int selectedIndex = reactantsField[i].getSelectedIndex();

			if(selectedIndex>0) {

				String signal = "-";

				if(jBackword.isSelected()) {

					signal = "";
				}

				reactants[i]= signal+metabolitesModel[0][selectedIndex];
				//metabolites.put(reactants[i], Math.abs(Integer.parseInt(reactantsStoichiometryField[i].getText()))*(-1));
				String stoich = reactantsStoichiometryField[i].getText();

				if(stoich.startsWith("-")) {

					if(signal.isEmpty()) {

						stoich=stoich.substring(1);
					}
				}
				else {

					if(signal.equalsIgnoreCase("-")) {

						stoich=signal+stoich;
					}
				}

				metabolites.put(reactants[i], stoich);
				chains.put(reactants[i], reactantsChainsField[i].getText());
				compartments.put(reactants[i], reactantsCompartmentsBox[i].getSelectedItem().toString());
			}
		}

		for(int i=0; i< productsField.length; i++) {

			int selectedIndex = productsField[i].getSelectedIndex();

			if(selectedIndex>0) {

				String signal = "";

				if(jBackword.isSelected()) {

					signal = "-";
				}

				products[i]= signal+metabolitesModel[0][selectedIndex];
				String stoich = productsStoichiometryField[i].getText();


				if(stoich.startsWith("-")) {

					if(signal.isEmpty()) {

						stoich=stoich.substring(1);
					}
				}
				else {

					if(signal.equalsIgnoreCase("-")) {

						stoich=signal+stoich;
					}
				}

				metabolites.put(products[i], stoich);
				chains.put(products[i], productsChainsField[i].getText());
				compartments.put(products[i], productsCompartmentsBox[i].getSelectedItem().toString());
			}
		}

		//		for(int i=0; i< pathwaysField.length; i++) {
		//
		//			String pathName=pathwaysField[i].getSelectedItem().toString();
		//
		//			if(!pathName.equals("")) {
		//
		//				//paths.add(pathName);
		//				if(!selectedEnzymesAndPathway.containsKey(pathName)) {
		//
		//					String[] enzymesECnumbers=reactionsContainer.getEnzymes(rowID,reactionsContainer.getPathwayID(pathwaysField[i].getSelectedItem().toString()));
		//
		//					if(enzymesECnumbers.length==0) {
		//
		//						enzymesECnumbers=reactionsContainer.getEnzymes(rowID);// select item in comboBox
		//					}
		//					Set<String> enzymesECnumbersSet=new TreeSet<String>(Arrays.asList(enzymesECnumbers));//set for later recording
		//					selectedEnzymesAndPathway.put(pathName,enzymesECnumbersSet);
		//				}
		//			}
		//		}
		//
		//		for(int i=0; i< enzymesField.length; i++) {
		//
		//			if(!enzymesField[i].getSelectedItem().toString().equals("")) {
		//
		//				//enzs.add(enzymesField[i].getSelectedItem().toString());
		//				enzymesSet.add(enzymesField[i].getSelectedItem().toString());
		//
		//				if(selectedEnzymesAndPathway.size()>0) {
		//
		//					selectedEnzymesAndPathway.put(selectedPathway, enzymesSet);
		//				}
		//			}
		//		}

		if(rowID.equals("-10")) {

			reactionsContainer.insertNewReaction(jTextFieldName.getText(), jTextFieldEquation.getText(), jRadioButtonReversible.isSelected(), //paths, enzymesSet,
					chains, compartments, metabolites, jCheckBoxInModel.isSelected(), selectedEnzymesAndPathway, jComboBoxLocalisation.getSelectedItem().toString(),
					jSpontaneous.isSelected(), jNonEnzymatic.isSelected(), jIsGeneric.isSelected(),
					lowerBoundary.getText(), upperBoundary.getText());
		}
		else {


			reactionsContainer.updateReaction(rowID, jTextFieldName.getText(), jTextFieldEquation.getText(), jRadioButtonReversible.isSelected(), //enzymesSet,
					chains, compartments, metabolites, jCheckBoxInModel.isSelected(), selectedEnzymesAndPathway, jComboBoxLocalisation.getSelectedItem().toString(), 
					jSpontaneous.isSelected(), jNonEnzymatic.isSelected(), jIsGeneric.isSelected(),
					lowerBoundary.getText(), upperBoundary.getText());

			this.inModelNotChanged = initialInModel==jCheckBoxInModel.isSelected();
		}
	}

	/**
	 * 
	 */
	private void startFields() {

		String[] data = reactionsContainer.getReaction(rowID);
		jTextFieldName.setText(data[0]);
		jTextFieldEquation.setText(data[1]);
		jTextFieldEquation.setToolTipText(data[1]);

		if(data[2].equals("1")) {

			jRadioButtonReversible.setSelected(true);
		}
		else {

			jRadioButtonIrreversible.setSelected(true);
			jForward.setSelected(true);
			jForward.setEnabled(true);
			jBackword.setEnabled(true);
		}

		if(data[4].equals("1")) {

			jCheckBoxInModel.setSelected(true);
			this.initialInModel=true;
		}
		else {

			jCheckBoxInModel.setSelected(false);
			this.initialInModel=false;
		}

		jComboBoxLocalisation.setSelectedItem(data[5]);

		if(data[6].equals("1"))
			jSpontaneous.setSelected(true);
		else
			jSpontaneous.setSelected(false);

		if(data[7].equals("1"))
			jNonEnzymatic.setSelected(true);
		else

			jNonEnzymatic.setSelected(false);
		
		if(data[8].equals("1"))
			jIsGeneric.setSelected(true);
		else
			jIsGeneric.setSelected(false);
		
		if(jRadioButtonReversible.isSelected())
			this.lowerBoundary.setText("-10000");
		else
			this.lowerBoundary.setText("0");
		
		if(data[9]!=null && !data[9].isEmpty())
			this.lowerBoundary.setText(data[9]);
		
		this.upperBoundary.setText("10000");
		if(data[10]!=null && !data[10].isEmpty())
			this.upperBoundary.setText(data[10]);
			
	}

	/**
	 * @return
	 */
	private JButton jButtonAddReactant() {

		JButton jButtonAddReactant = new JButton();
		jButtonAddReactant.setIcon((new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")), 0.04).resizeImageIcon()));
		jButtonAddReactant.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String[] newReactants = new String[reactantsField.length+1];
				String[] newReactantsStoichiometry = new String[reactantsStoichiometryField.length+1];
				String[] newReactantsChains = new String[reactantsChainsField.length+1];
				String[] newReactantsComp = new String[reactantsCompartmentsBox.length+1];

				for(int i=0; i<reactantsField.length;i++)
				{
					newReactants[i]=Arrays.asList(metabolitesModel[0]).get(reactantsField[i].getSelectedIndex()).toString();
					newReactantsStoichiometry[i] = reactantsStoichiometryField[i].getText().toString();
					newReactantsChains[i] = reactantsChainsField[i].getText().toString();
					newReactantsComp[i] = reactantsCompartmentsBox[i].getSelectedItem().toString();
				}
				newReactants[reactantsField.length]="";
				newReactantsStoichiometry[reactantsStoichiometryField.length]="-1";
				newReactantsChains[reactantsChainsField.length] = "1";
				newReactantsComp[reactantsCompartmentsBox.length] = defaultCompartment;
				reactants=newReactants;
				reactantsStoichiometry = newReactantsStoichiometry;
				reactantsChains=newReactantsChains;
				reactantsCompartments=newReactantsComp;
				jScrollPaneReactants.setViewportView(addReactantsPanel());
			}
		});
		return jButtonAddReactant;
	}

	/**
	 * @return
	 */
	private JButton jButtonAddProduct() {

		JButton jButtonAddProduct = new JButton();
		jButtonAddProduct.setIcon((new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")), 0.04).resizeImageIcon()));
		jButtonAddProduct.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String[] newProduct = new String[productsField.length+1];
				String[] newProductStoichiometry = new String[productsStoichiometryField.length+1];
				String[] newProductChains = new String[productsChainsField.length+1];
				String[] newProductComp = new String[productsCompartmentsBox.length+1];

				for(int i=0; i<productsField.length;i++)
				{
					newProduct[i]=Arrays.asList(metabolitesModel[0]).get(productsField[i].getSelectedIndex()).toString();
					newProductStoichiometry[i] = productsStoichiometryField[i].getText().toString();
					newProductChains[i] = productsChainsField[i].getText().toString();
					newProductComp[i] = productsCompartmentsBox[i].getSelectedItem().toString();
				}
				newProduct[productsField.length]="";
				newProductStoichiometry[productsStoichiometryField.length]="1";
				newProductChains[productsChainsField.length] = "1";
				newProductComp[productsCompartmentsBox.length] = defaultCompartment;

				products=newProduct;
				productsStoichiometry = newProductStoichiometry;
				productsChains=newProductChains;
				productsCompartments=newProductComp;
				jScrollPaneProducts.setViewportView(addProductsPanel());					
			}
		});

		return jButtonAddProduct ;
	}

	/**
	 * @return
	 */
	private JButton jButtonAddEnzyme() {

		JButton jButtonAddEnzyme = new JButton();
		jButtonAddEnzyme.setIcon((new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")), 0.04).resizeImageIcon()));
		jButtonAddEnzyme.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {

				//				int j=0;
				//				enzymes=new String[selectedEnzymesAndPathway.get(selectedPathway).size()];
				//
				//				for(String ecnumber:selectedEnzymesAndPathway.get(selectedPathway)) {
				//
				//					enzymes[j]=ecnumber;
				//					j++;
				//				}
				//
				//				String[] newEnzyme = new String[enzymes.length+1];
				//
				//				for(int i=0; i<enzymes.length;i++) {
				//
				//					newEnzyme[i]=enzymes[i];
				//				}
				//				newEnzyme[enzymes.length]="";
				//				enzymes=newEnzyme;
				selectedEnzymesAndPathway.get(selectedPathway).add("");
				jScrollPaneEnzymes.setViewportView(addEnzymes());
			}
		});
		return jButtonAddEnzyme;
	}

	/**
	 * @return
	 */
	private JButton jButtonAddPathways() {

		JButton jButtonAddPathways = new JButton();
		jButtonAddPathways.setIcon((new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")), 0.04).resizeImageIcon()));
		jButtonAddPathways.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {

				//				if(rowID.equals("-10")) {
				//
				//					int i=0;
				//					for(String pathway:selectedEnzymesAndPathway.keySet()) {
				//
				//						if(!pathway.equals("-1allpathwaysinreaction")) {
				//
				//							pathways[i]=pathway;
				//							i++;
				//						}
				//					}
				//				}
				//				String[] newPathways = new String[pathways.length+1];
				//				for(int i=0; i<pathways.length;i++) {
				//
				//					newPathways[i]=pathwaysField[i].getSelectedItem().toString();
				//				}
				//
				//				newPathways[pathwaysField.length]="";
				//				pathways=newPathways;

				selectedEnzymesAndPathway.put("", new HashSet<String>());
				jScrollPanePathways.setViewportView(addPathways());					
			}
		});
		return jButtonAddPathways;
	}

	/**
	 * @param comboArray
	 * @param comboBox
	 */
	private boolean comboBoxActionListener(JComboBox<String>[] comboArray, JComboBox<String> comboBox) {

		for(JComboBox<String> r:comboArray) {

			if(!comboBox.equals(r)) {

				if(comboBox.getSelectedIndex()==r.getSelectedIndex() && !comboBox.getSelectedItem().toString().equals("")) {

					Workbench.getInstance().warn("Entity already selected!");
					comboBox.setSelectedIndex(0);
					return false;
				}
			}
		}
		return true;
	}


	/**
	 * @param arg0
	 */
	private void pathwaysAction(List<String> paths, String editedPathway) {

		Map<String, Set<String>> newSelectedEnzymesAndPathway = new HashMap<String, Set<String>>();

		for(String pathway : paths) {

			Set<String> enz = new TreeSet<String>();

			if(selectedEnzymesAndPathway.containsKey(pathway)) {

				enz = selectedEnzymesAndPathway.get(pathway);
			}
			newSelectedEnzymesAndPathway.put(pathway, enz);
		}

		//			if(!selectedPathway.equals("-1allpathwaysinreaction")) {
		//
		//				selectedEnzymesAndPathway.remove(selectedPathway);
		//			}
		//			selectedEnzymesAndPathway.put(selectedPathway,enz);
		selectedEnzymesAndPathway = newSelectedEnzymesAndPathway;
		pathwaysClick(editedPathway);
	}

	//	/**
	//	 * @param arg0
	//	 */
	//	private void pathwaysAction(MouseEvent e) {
	//
	//		if(e.getButton() == MouseEvent.BUTTON1) {
	//
	//			if(comboBoxActionListener(pathwaysField, (JComboBox) e.getSource())) {
	//
	//				this.selectedPathway = ((JComboBox) e.getSource()).getSelectedItem().toString();
	//
	//				System.out.println(selectedPathway);
	//	JComboBox comboBox = null;
	//	Object obj = ((ItemEvent) arg0).getItemSelectable();
	//
	//	if(obj instanceof JComboBox) {
	//
	//		comboBox = (JComboBox) obj;
	//	}
	//	this.selectedPathway = (String) comboBox.getSelectedItem();
	//
	//	if(comboBoxActionListener(pathwaysField, (JComboBox) arg0.getSource())) {
	//
	//		if(this.selectedPathway.equals("")) {
	//
	//			selectedEnzymesAndPathway.remove(this.selectedPathway);
	//		}
	//		else {
	//
	//			Set<String> enz ;
	//
	//			if(selectedEnzymesAndPathway.containsKey(selectedPathway)) {
	//
	//				enz = selectedEnzymesAndPathway.get(selectedPathway);
	//			}
	//			else {
	//
	//				enz = new TreeSet<String>();
	//			}
	//
	//			if(!selectedPathway.equals("-1allpathwaysinreaction")) {
	//
	//				selectedEnzymesAndPathway.remove(selectedPathway);
	//			}
	//			selectedEnzymesAndPathway.put(selectedPathway,enz);
	//		}
	//		this.pathwaysClick(selectedPathway);
	//	}
	//			}
	//		}
	//	}


	/**
	 * @param pathway
	 */
	private void pathwaysClick(String pathway) {

		if(pathway.equals("")) {

			this.selectedPathway="-1allpathwaysinreaction";
		}
		else {

			this.selectedPathway=pathway;
		}
		//
		//		if(this.rowID.equals("-10")) {
		//
		//			this.enzymesSet=new TreeSet<String>();
		//
		//			if(this.selectedEnzymesAndPathway.containsKey(this.selectedPathway)) {
		//
		//				this.enzymesSet=this.selectedEnzymesAndPathway.get(selectedPathway);
		//			}
		//
		//			int i=0;
		//			enzymes = new String[this.enzymesSet.size()];
		//
		//			for(String ecnumber:this.enzymesSet) {
		//
		//				enzymes[i]=ecnumber;
		//				i++;
		//			}
		//		}
		//		else {
		//
		//			if(this.selectedPathway.equals("-1allpathwaysinreaction")) {
		//
		//				enzymes = this.reactionsContainer.getEnzymes(this.rowID);
		//			}// select item in comboBox
		//			else {
		//
		//				enzymes = reactionsContainer.getEnzymes(rowID,reactionsContainer.getPathwayID(this.selectedPathway));
		//			}// select item in comboBox
		//			this.enzymesSet=new TreeSet<String>(Arrays.asList(this.enzymes));//set for later recording
		//		}

		//		this.selectedEnzymesAndPathway.put(this.selectedPathway,this.enzymesSet);

		this.jScrollPaneEnzymes.setViewportView(addEnzymes());		
	}

	/**
	 * @param enz
	 */
	//private void enzymesAction(ItemEvent arg0) {
	//
	//		if(arg0.getStateChange()==ItemEvent.DESELECTED) {
	//
	//			if(this.newECnumber) {
	//				
	//				this.selectedEnzyme=arg0.getItem().toString();
	//			}
	//			else {
	//				
	//				this.selectedEnzyme="";
	//			}
	//			this.newECnumber=false;
	//		}
	//		else {
	//
	//			this.newECnumber=comboBoxActionListener(this.enzymesField, (JComboBox) arg0.getSource());
	//
	//			if(this.newECnumber) {
	//
	//				Set<String> enzs;
	//				enzs = this.selectedEnzymesAndPathway.get(this.selectedPathway);
	//				enzs.remove(selectedEnzyme);
	//				if(!arg0.getItem().toString().equals("")) {
	//					
	//					enzs.add(arg0.getItem().toString());
	//				}
	//				this.selectedEnzymesAndPathway.put(this.selectedPathway,enzs);
	//
	//				if(this.selectedPathway.equals("-1allpathwaysinreaction")) {
	//
	//					if(arg0.getItem().toString().equals("")) {
	//
	//						for(String pathway:this.selectedEnzymesAndPathway.keySet()) {
	//
	//							Set<String> enzymes = this.selectedEnzymesAndPathway.get(pathway);
	//							enzymes.remove(selectedEnzyme);
	//							this.selectedEnzymesAndPathway.put(pathway, enzymes);
	//						}
	//					}
	//				}
	//
	//			}
	//		}
	//	}


	/**
	 * @param arg0
	 */
	private void enzymesAction(ItemEvent arg0) {

		if(arg0.getStateChange()==ItemEvent.SELECTED) {

			Set<String> enzs = new HashSet<String>();

			for(JComboBox<String> jEnzymes : this.enzymesField) {

				String enzyme = jEnzymes.getSelectedItem().toString();

				if(!enzyme.equals("")){

					enzs.add(enzyme);
				}
			}

			this.newECnumber=comboBoxActionListener(this.enzymesField, (JComboBox<String>) arg0.getSource());
			if(this.newECnumber) {

				this.selectedEnzymesAndPathway.put(this.selectedPathway,enzs);
			}

			if(this.selectedPathway.equalsIgnoreCase("-1allpathwaysinreaction")) {

				Set<String> allEnzymes = this.selectedEnzymesAndPathway.get("-1allpathwaysinreaction");

				for(String pathway : this.selectedEnzymesAndPathway.keySet()) {

					if(!pathway.equalsIgnoreCase("-1allpathwaysinreaction")) {

						Set<String> enzymes = this.selectedEnzymesAndPathway.get(pathway);
						enzymes.retainAll(allEnzymes);
						this.selectedEnzymesAndPathway.put(pathway, enzymes);
					}
				}
			}
			else {


				// collect ec numbers for this reaction
				Set<String> newEnzymes = new HashSet<String> ();
				for(String pathway : this.selectedEnzymesAndPathway.keySet()) {

					Set<String> enzymes = this.selectedEnzymesAndPathway.get(pathway);
					newEnzymes.addAll(enzymes);
				}

				this.selectedEnzymesAndPathway.put("-1allpathwaysinreaction", newEnzymes);
			}
		}
	}

	/**
	 * 
	 */
	private void pathwaysPanelClick() {

		this.selectedPathway="-1allpathwaysinreaction";

		//		if(rowID.equals("-10")) {
		//
		//			this.enzymesSet=new TreeSet<String>();
		//			this.enzymesSet=this.selectedEnzymesAndPathway.get(this.selectedPathway);
		//
		//			int i=0;
		//			this.enzymes=new String[selectedEnzymesAndPathway.get(selectedPathway).size()];
		//
		//			for(String ecnumber:selectedEnzymesAndPathway.get(selectedPathway)) {
		//
		//				this.enzymes[i]=ecnumber;
		//				i++;
		//			}
		//		}
		//		else {
		//
		////			this.enzymes = this.reactionsContainer.getEnzymes(this.rowID);
		////
		////			if(this.enzymes.length==0) {
		////
		////				this.enzymes=reactionsContainer.getEnzymes(this.rowID);
		////			}// select item in comboBox
		////			this.enzymesSet=new TreeSet<String>(Arrays.asList(this.enzymes));//set for later recording
		//		
		//			this.enzymesSet=this.selectedEnzymesAndPathway.get(this.selectedPathway);
		//		}
		//this.selectedEnzymesAndPathway.put(this.selectedPathway,this.enzymesSet);
		this.jScrollPaneEnzymes.setViewportView(addEnzymes());
	}
}
