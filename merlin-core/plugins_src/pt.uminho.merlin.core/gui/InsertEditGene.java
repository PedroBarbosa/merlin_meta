package gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import utilities.CreateImageIcon;
import datatypes.metabolic_regulatory.Genes;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;

/**
 * @author ODias
 *
 */
public class InsertEditGene extends javax.swing.JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jPanelDialogGene;
	private JButton jButtonSave;
	private JTextField jTextFieldName;
	private JLabel jLabel2;
	private JLabel jLabel1;
	private JTextField jTextFieldLocus;
	private JComboBox<String> jComboBoxDirection;
	private JPanel jPanel3;
	private JPanel jPanel2;
	private JPanel jPanel1;
	private JScrollPane jScrollPaneProtein;
	private JTextField jPosition2;
	private JTextField jPosition1;
	private JComboBox<String>  jComboBoxChromosome;
	private JButton jButtonCancel;
	//private JPanel jPanelBoolean;
	private JPanel jPanelTranscription_direction;
	private JPanel jPanelName;
	private JPanel jPanelChromosome;
	private Genes genes;
	private String[][] proteinData;
	private String[][] chromosomeData;
	private int selectedRow;
	private JPanel jPanelSaveClose;
	private String[] subunits;
	private JComboBox<String>[] proteinsCBox;
	private String[] oldSubunits;


	/**
	 * @param selectedRow
	 * @param genes
	 */
	public InsertEditGene(int selectedRow, Genes genes) {

		super(Workbench.getInstance().getMainFrame());
		this.genes = genes;
		chromosomeData = genes.getChromosomes();

		if(chromosomeData[0].length>1 && chromosomeData[1] != null && !chromosomeData[1][1].equalsIgnoreCase("null")) {

			jComboBoxChromosome = new JComboBox<String>(chromosomeData[1]);
		}
		else {

			jComboBoxChromosome = new JComboBox<String>();
		}
		//get protein array data (used to determine relative position of sqlIndex and sql String on the comboBox)
		proteinData = genes.getProteins();

		if(selectedRow!=-10) {

			this.setTitle("Edit Gene");
			//get proteins associated to such gene, and store new ones
			subunits=genes.getSubunits(selectedRow);
			//store original proteins associated to such gene, immutable
			oldSubunits = genes.getSubunits(selectedRow);
			initGUI();
			
			this.startFields(genes.getGeneData(selectedRow));
		}
		else {

			this.setTitle("Insert Gene");
			subunits= new String[0];
			initGUI();	
		}
		
		Utilities.centerOnOwner(this);
		this.selectedRow = selectedRow;
		this.setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
		this.setVisible(true);
		this.setAlwaysOnTop(true);
		this.toFront();

	}

	/**
	 * 
	 */
	private void initGUI() {

		try {

			GroupLayout thisLayout = new GroupLayout((JComponent)getContentPane());
			getContentPane().setLayout(thisLayout);
			{
				jPanelDialogGene = new JPanel(new GridBagLayout());
				GridBagLayout jPanelDialogGeneLayout = new GridBagLayout();
				jPanelDialogGeneLayout.columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.0};
				jPanelDialogGeneLayout.columnWidths = new int[] {8, 50, 7, 50, 7, 50, 7, 50, 20, 8};
				jPanelDialogGeneLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.0, 0.1, 0.1, 0.0, 0.1, 0.0};
				jPanelDialogGeneLayout.rowHeights = new int[] {7, 7, 7, 7, 7, 20, 7, 7, 7};
				jPanelDialogGene.setLayout(jPanelDialogGeneLayout);
				jPanelDialogGene.setSize(300, 650);

				{
					jPanelChromosome = new JPanel();
					GridBagLayout jPanelChromosomeLayout = new GridBagLayout();
					jPanelDialogGene.add(jPanelChromosome, new GridBagConstraints(1, 1, 3, 1, 0.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jPanelChromosome.setBorder(BorderFactory.createTitledBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null), "Chromosome", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION));
					jPanelChromosome.setSize(100, 20);
					jPanelChromosomeLayout.rowWeights = new double[] {0.1, 0.1, 0.1};
					jPanelChromosomeLayout.rowHeights = new int[] {7, 7, 7};
					jPanelChromosomeLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
					jPanelChromosomeLayout.columnWidths = new int[] {7, 7, 7};
					jPanelChromosome.setLayout(jPanelChromosomeLayout);
					{

						//jComboBoxChromosome = new JComboBox();
						jPanelChromosome.add(jComboBoxChromosome, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

						jComboBoxChromosome.setBounds(73, 37, 141, 26);
						jComboBoxChromosome.setPreferredSize(new java.awt.Dimension(120, 25));
					}
				}
				{
					jPanelName = new JPanel();
					GridBagLayout jPanelNameLayout = new GridBagLayout();
					jPanelDialogGene.add(jPanelName, new GridBagConstraints(1, 2, 3, 1, 0.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jPanelName.setBorder(BorderFactory.createTitledBorder("Gene Names"));
					jPanelName.setBounds(12, 101, 267, 54);
					jPanelNameLayout.rowWeights = new double[] {0.1, 0.1, 0.0, 0.0, 0.1};
					jPanelNameLayout.rowHeights = new int[] {7, 7, 7, 7, 7};
					jPanelNameLayout.columnWeights = new double[] {0.0, 0.0, 0.1, 0.0};
					jPanelNameLayout.columnWidths = new int[] {7, 6, 7, 7};
					jPanelName.setLayout(jPanelNameLayout);
					{
						jTextFieldLocus = new JTextField();
						jLabel1 = new JLabel("name");
						jLabel2 = new JLabel("locus");
						jTextFieldName = new JTextField();
						jPanelName.add(jTextFieldName, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						jPanelName.add(jTextFieldLocus, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						jPanelName.add(jLabel1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						jPanelName.add(jLabel2, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						jTextFieldName.setBounds(54, 36, 179, 27);
						jTextFieldName.setPreferredSize(new java.awt.Dimension(120, 25));
					}
				}

				{
					jPanelTranscription_direction = new JPanel();
					GridBagLayout jPanelTranscription_directionLayout = new GridBagLayout();
					jPanelDialogGene.add(jPanelTranscription_direction, new GridBagConstraints(1, 4, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jPanelTranscription_direction.setBorder(BorderFactory.createTitledBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null), "Transcription", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION));
					jPanelTranscription_direction.setBounds(10, 180, 266, 139);
					jPanelTranscription_directionLayout.rowWeights = new double[] {0.1, 0.1, 0.1};
					jPanelTranscription_directionLayout.rowHeights = new int[] {7, 7, 6};
					jPanelTranscription_directionLayout.columnWeights = new double[] {0.1};
					jPanelTranscription_directionLayout.columnWidths = new int[] {7};
					jPanelTranscription_direction.setLayout(jPanelTranscription_directionLayout);
					{
						jPanel1 = new JPanel();
						GridBagLayout jPanel1Layout = new GridBagLayout();
						jPanelTranscription_direction.add(jPanel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						jPanel1.setBorder(BorderFactory.createTitledBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null), "Left End", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION));
						jPanel1Layout.rowWeights = new double[] {0.0};
						jPanel1Layout.rowHeights = new int[] {7};
						jPanel1Layout.columnWeights = new double[] {0.1};
						jPanel1Layout.columnWidths = new int[] {7};
						jPanel1.setLayout(jPanel1Layout);
						{
							jPosition1 = new JTextField();
							jPanel1.add(jPosition1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));


						}
					}

					{
						jPanel2 = new JPanel();
						GridBagLayout jPanel2Layout = new GridBagLayout();
						jPanelTranscription_direction.add(jPanel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						jPanel2.setBorder(BorderFactory.createTitledBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null), "Right End", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION));
						jPanel2Layout.rowWeights = new double[] {0.0};
						jPanel2Layout.rowHeights = new int[] {7};
						jPanel2Layout.columnWeights = new double[] {0.1};
						jPanel2Layout.columnWidths = new int[] {7};
						jPanel2.setLayout(jPanel2Layout);
						//jPanel2.setPreferredSize(new java.awt.Dimension(144, 73));
						{
							//						ComboBoxModel jComboBoxPosition2Model = 
							//							new DefaultComboBoxModel(
							//									);
							jPosition2 = new JTextField();
							jPanel2.add(jPosition2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

						}
					}

					{
						jPanel3 = new JPanel();
						GridBagLayout jPanel3Layout = new GridBagLayout();
						jPanelTranscription_direction.add(jPanel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						jPanel3.setBorder(BorderFactory.createTitledBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null), "Transcription Direction", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION));
						jPanel3Layout.rowWeights = new double[] {0.1};
						jPanel3Layout.rowHeights = new int[] {7};
						jPanel3Layout.columnWeights = new double[] {0.1};
						jPanel3Layout.columnWidths = new int[] {7};
						jPanel3.setLayout(jPanel3Layout);
						jComboBoxDirection = new JComboBox<String>(new String[] { "", "3-5", "5-3" });
						jPanel3.add(jComboBoxDirection, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

					}
				}
				//				{
				//					//BOOLEAN RULE THIS.DATA[6]
				//					jPanelBoolean = new JPanel();
				//					GridBagLayout jPanelBooleanLayout = new GridBagLayout();
				//					jPanelDialogGene.add(jPanelBoolean, new GridBagConstraints(1, 8, 3, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				//					jPanelBoolean.setBorder(BorderFactory.createTitledBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null), "Boolean Rule", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION));
				//					jPanelBoolean.setBounds(10, 335, 270, 45);
				//					jPanelBooleanLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1};
				//					jPanelBooleanLayout.rowHeights = new int[] {7, 7, 7, 7};
				//					jPanelBooleanLayout.columnWeights = new double[] {0.1, 0.1, 0.1, 0.1};
				//					jPanelBooleanLayout.columnWidths = new int[] {7, 7, 7, 7};
				//					jPanelBoolean.setLayout(jPanelBooleanLayout);
				//				}
				{
					jPanelSaveClose = new JPanel(new GridBagLayout());
					GridBagLayout jPanelSaveCloseLayout = new GridBagLayout();
					jPanelSaveCloseLayout.rowWeights = new double[] {0.1, 0.1, 0.1};
					jPanelSaveCloseLayout.rowHeights = new int[] {5, 10, 5};
					jPanelSaveCloseLayout.columnWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1};
					jPanelSaveCloseLayout.columnWidths = new int[] {8, 130, 24, 130, 8};
					//set layout
					jPanelSaveClose.setLayout(jPanelSaveCloseLayout);

					{
						jButtonSave = new JButton();
						jPanelSaveClose.add(jButtonSave, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						//jPanelDialogGene.add(jButtonSave, new GridBagConstraints(1, 12, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						jButtonSave.setText("Save");
						jButtonSave.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Save.png")),0.1).resizeImageIcon());
						jButtonSave.addActionListener(new ActionListener(){

							@Override
							public void actionPerformed(ActionEvent e) {

								String idChromosome="1", name, transcription_direction, left_end_position, right_end_position, locusTag;
								boolean go = true;

								if(jComboBoxChromosome.getModel().getSize()>0) {

									if(jComboBoxChromosome.getSelectedItem().toString().equalsIgnoreCase("")) {

										go = false;
									}
									else {

										idChromosome = chromosomeData[0][jComboBoxChromosome.getSelectedIndex()];
									}
								}

								name = jTextFieldName.getText();
								transcription_direction=  (String) jComboBoxDirection.getSelectedItem();
								left_end_position = jPosition1.getText();
								right_end_position = jPosition2.getText();
								locusTag=jTextFieldLocus.getText();
								
								if(/*name.isEmpty() ||*/ locusTag.isEmpty()) {
									
									go = false;
								}
								
								if(go) {

									if(selectedRow == -10) {

										genes.insertNewGene(idChromosome, name, transcription_direction, left_end_position, right_end_position, subunits, locusTag);
									}
									else {

										genes.updateGene(idChromosome, name, transcription_direction,left_end_position, right_end_position, subunits, selectedRow, oldSubunits, locusTag);
									}
									close();
								}
								else {

									Workbench.getInstance().warn("Please verify the chromosome and locus tag fields.");
								}
							}
						});

					}
					{
						jButtonCancel = new JButton();
						jPanelSaveClose.add(jButtonCancel, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						jButtonCancel.setText("Cancel");
						jButtonCancel.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")), 0.1).resizeImageIcon());
						jButtonCancel.addActionListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent e) {close();}
						});
					}
					jPanelDialogGene.add(jPanelSaveClose, new GridBagConstraints(0, 7, 9, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				}

				//ScrollPaneProtein
				{
					jScrollPaneProtein = new JScrollPane();
					jScrollPaneProtein.setViewportView(this.addProteinPanel());
					//					jScrollPaneProtein.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
					jPanelDialogGene.add(jScrollPaneProtein, new GridBagConstraints(5, 1, 3, 4, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

				}


				//ButtonAddProtein
				{
					JButton jButtonAddProtein = new JButton();
					jButtonAddProtein.setIcon((new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")), 0.04).resizeImageIcon()));
					jPanelDialogGene.add(jButtonAddProtein, new GridBagConstraints(8, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jButtonAddProtein.addActionListener(new ActionListener(){

						@Override
						public void actionPerformed(ActionEvent e) {
							
							String[] newSubunits = new String[subunits.length+1];
							
							for(int i=0; i<subunits.length;i++) {
								
								newSubunits[i]=subunits[i];
							}

							newSubunits[subunits.length]="dummy"; 

							subunits=newSubunits;

							jScrollPaneProtein.setViewportView(addProteinPanel());
						}
					});
				}
			}
			thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(jPanelDialogGene, 0, 500, Short.MAX_VALUE));
			thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
					.addComponent(jPanelDialogGene, 0, 600, Short.MAX_VALUE));
			this.setModal(true);
			pack();
		}
		catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * Close window 
	 */
	private void close(){
		this.setVisible(false);
		this.dispose();
	}

	/**
	 * Retrieve the index where a given protein is located on the proteins array, given its position in the database
	 * 
	 * @param subunit
	 * @return
	 */
	private int getIndex(String subunit) {

		for(int i=0;i< proteinData[0].length;i++) {

			if(subunit.equals(proteinData[0][i])){return i;}
		}
		return -1;
	}

	/**
	 * @return
	 */
	private JPanel addProteinPanel() {

		JPanel panelProtein = new JPanel(new GridBagLayout());
		GridBagLayout jPanelProteinLayout = new GridBagLayout();
		panelProtein.setBorder(BorderFactory.createTitledBorder(null, "Protein", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));

		//number of rows (array size)
		if(subunits.length==0) {

			jPanelProteinLayout.rowWeights = new double[] {0.1, 0.1, 0.1};
			jPanelProteinLayout.rowHeights = new int[] {7, 7, 7};
		}
		else {

			jPanelProteinLayout.rowWeights = new double[subunits.length*2+1];
			jPanelProteinLayout.rowHeights  = new int[subunits.length*2+1];

			for(int rh=0; rh<subunits.length*2+1; rh++) {

				jPanelProteinLayout.rowWeights[rh]=0.1;
				jPanelProteinLayout.rowHeights[rh]=7;
			}
		}
		//number of columns (array size)
		jPanelProteinLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
		jPanelProteinLayout.columnWidths = new int[] {7, 10, 7};
		//set layout
		panelProtein.setLayout(jPanelProteinLayout);
		//		panelProtein.setPreferredSize(new java.awt.Dimension(281, 197));

		//set the comboBoxes
		//		JComboBox[] proteins;
		if(subunits.length==0) {

			subunits = new String[1];
			subunits[0]="";
			proteinsCBox = new JComboBox[1];
			proteinsCBox[0]= addComboBoxProtein(0);
			panelProtein.add( proteinsCBox[0], new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		else {

			proteinsCBox = new JComboBox[subunits.length];

			for(int s=0; s<subunits.length; s++) {

				proteinsCBox[s]= addComboBoxProtein(this.getIndex(subunits[s]));
				int r =s*2+1;
				panelProtein.add(proteinsCBox[s], new GridBagConstraints(1, r, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			}
		}

		return panelProtein;
	} 

	/**
	 * 
	 * @param selectedIndex
	 * @return
	 */
	private JComboBox<String> addComboBoxProtein(int selectedIndex) {

		ComboBoxModel<String> model = new DefaultComboBoxModel<String>(proteinData[1]);
		JComboBox<String> comboBoxProtein = new JComboBox<String>(model);
		comboBoxProtein.setPreferredSize(new Dimension(290, 26));
		comboBoxProtein.setSelectedIndex(selectedIndex);
		comboBoxProtein.setToolTipText((String) comboBoxProtein.getSelectedItem());
		//comboBoxProtein.setAutoscrolls(true);
		comboBoxProtein.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				
				if(arg0.getStateChange()==ItemEvent.SELECTED) {
				
				JComboBox<?> eventComboBoxProtein = (JComboBox<?>) arg0.getSource();
				int selectedBox = 0;
				eventComboBoxProtein.setToolTipText((String) eventComboBoxProtein.getSelectedItem());

				for(int i=0; i<proteinsCBox.length;i++) {
					
					if(proteinsCBox[i]==eventComboBoxProtein) {
						
						selectedBox=i;
						i=proteinsCBox.length;
					}
				}	

				for(int i=0; i<subunits.length;i++) {
					
					if(!subunits[i].trim().equalsIgnoreCase("dummy")
							&& subunits[i].equalsIgnoreCase((String) proteinData[0][eventComboBoxProtein.getSelectedIndex()])) {
						
						Workbench.getInstance().warn("Protein already selected on Box "+(i+1)+"!\nPlease Select another protein!");
						eventComboBoxProtein.setSelectedIndex(getIndex(subunits[selectedBox]));
						i=subunits.length;
					}
				}

				if(eventComboBoxProtein.getSelectedIndex()>-1) {
					
					subunits[selectedBox]=(String) proteinData[0][eventComboBoxProtein.getSelectedIndex()];
				}
				else {
					
					subunits[selectedBox]=(String) proteinData[0][0];
				}

				int width=290;
				if(eventComboBoxProtein!=null){width = ((String) eventComboBoxProtein.getSelectedItem()).length()*8;}

				if(width<290) {
					
					eventComboBoxProtein.setPreferredSize(new Dimension(290, 26));
				}
				else {
					
					eventComboBoxProtein.setPreferredSize(new Dimension(width,26));
				}

				//eventComboBoxProtein.updateUI();
				}}});
		
		return comboBoxProtein;
	}

	/**
	 * @param data
	 */
	private void startFields(String[] data) {

		if(data[1]!=null){jComboBoxChromosome.setSelectedItem(data[1]);}
		if(data[2]==null){jTextFieldName.setText("");}else{jTextFieldName.setText(data[2]);}
		jTextFieldName.setAlignmentX(LEFT_ALIGNMENT);
		if(data[3]!=null){jComboBoxDirection.setSelectedItem(data[3]);}
		if(data[4]==null){jPosition1.setText("");}else{jPosition1.setText(data[4]);}
		jPosition1.setAlignmentX(LEFT_ALIGNMENT);
		if(data[5]==null){jPosition2.setText("");}else{jPosition2.setText(data[5]);}
		jPosition2.setAlignmentX(LEFT_ALIGNMENT);
		if(data[7]==null){jTextFieldLocus.setText("");}else{jTextFieldLocus.setText(data[7]);}
		jTextFieldLocus.setAlignmentX(LEFT_ALIGNMENT);	
	}


}