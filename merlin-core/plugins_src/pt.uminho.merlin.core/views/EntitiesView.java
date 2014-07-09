package views;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import datatypes.Entities;
import datatypes.metabolic_regulatory.Entity;

/**
 * @author ODias
 *
 */
public class EntitiesView extends javax.swing.JPanel {

	private static final long serialVersionUID = -8392087095290637656L;
	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane2;
	private JScrollPane jScrollPane3;
	//	private JTextPane jTextPane1;
	private JCheckBox jCheckBox1;
	private JList<Entity> jList1;
	private JList<Entity> jList2;
	private JPanel jPanel11;
	private Entities ent;
	private JTable jTable1;


	/**
	 * @param ent
	 */
	public EntitiesView(Entities ent) {
		super();
		initGUI();
		this.ent = ent;
		fillComponents();
	}

	/**
	 * 
	 */
	private void initGUI() {

		try {
			{
				GridBagLayout jPanel1Layout = new GridBagLayout();
				this.setLayout(jPanel1Layout);

				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.BOTH;

				c.weightx = 1.0;
				c.weighty = 0.95;
				c.gridx = 0;
				c.gridy = 0;

				jPanel11 = new JPanel();
				jPanel11.setLayout(null);

				this.add(jPanel11, new GridBagConstraints(0, 0, 2, 2, 1.0, 0.95, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				{
				}
				{
					jScrollPane1 = new JScrollPane();
					jPanel11.add(jScrollPane1);
					jScrollPane1.setBorder(new TitledBorder("Entities"));
					jScrollPane1.setBounds(12, 12, 340, 500);
				}
				{
					jScrollPane2 = new JScrollPane();
					jPanel11.add(jScrollPane2);
					jScrollPane2.setBounds(379, 12, 487, 250);
					jScrollPane2.setBorder(BorderFactory.createTitledBorder("Subunits"));
				}
				{
					jScrollPane3 = new JScrollPane();
					jTable1 = new JTable();
					TableModel jTable1Model = 
							new DefaultTableModel(
									new String[][]{},
									new String[] { "", "" });
					jScrollPane3.setViewportView(jTable1);
					jTable1.setModel(jTable1Model);
					jPanel11.add(jScrollPane3);
					jScrollPane3.setBounds(379, 262, 487, 250);
					jScrollPane3.setBorder(BorderFactory.createTitledBorder("Table statistics"));
				}
				{
					jCheckBox1 = new JCheckBox();
					jPanel11.add(jCheckBox1);
					jCheckBox1.setText("Hide subentities");
					jCheckBox1.setBounds(12, 529, 501, 18);
					jCheckBox1.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							hideNoHide();
						}
					});
				}
				jList1 = new JList<>();
				jScrollPane1.setViewportView(jList1);
				jList1.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent evt) {
						jList1MouseClicked(evt);
					}
				});
				jList2 = new JList<>();
				jScrollPane2.setViewportView(jList2);
				jList2.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent evt) {
						jList2MouseClicked(evt);
					}
				});
				jScrollPane2.setEnabled(false);

			}
			setSize(400, 300);
			this.setPreferredSize(new java.awt.Dimension(1006, 576));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void addUnit(LinkedList<Entity> lis, Entity ent)
	{
		lis.add(ent);

		ArrayList<Entity> entities = ent.getSubenties();

		for(int i=0;i<entities.size();i++)
		{
			addUnit(lis, entities.get(i));
		}
	}

	public void fillComponents() {

		ArrayList<Entity> entities = ent.getEntitiesList();

		LinkedList<Entity> ents = new LinkedList<Entity>();

		for(int i=0;i<entities.size();i++)
		{
			addUnit(ents, entities.get(i));
		}

		Entity[] elis = new Entity[ents.size()];

		//		Entity e = elis[0];

		for(int i=0;i<ents.size();i++)
		{
			elis[i] = ents.get(i);
		}

		ListModel<Entity> jList1Model = new DefaultComboBoxModel<>(elis);
		jList1.setModel(jList1Model);
	}

	private void jList1MouseClicked(MouseEvent evt) {
		Entity en = (Entity)jList1.getSelectedValue();

		setTable(en);

		ArrayList<Entity> subs = en.getSubenties();

		if(subs.size()>0)
		{
			LinkedList<Entity> ents = new LinkedList<Entity>();

			for(int i=0;i<subs.size();i++)
			{
				addUnit(ents, subs.get(i));
			}

			Entity[] elis = new Entity[ents.size()];

			//			Entity e = elis[0];

			for(int i=0;i<ents.size();i++)
			{
				elis[i] = ents.get(i);
			}

			ListModel<Entity> jList2Model = new DefaultComboBoxModel<>(elis);
			jList2.setModel(jList2Model);
			jScrollPane2.setEnabled(true);
		}
		else
		{
			Entity[] a = new Entity[0];
			ListModel<Entity> jList2Model = new DefaultComboBoxModel<>(a);
			jList2.setModel(jList2Model);
			jScrollPane2.setEnabled(false);
		}
	}

	/**
	 * @param e
	 */
	private void setTable(Entity e)
	{
		TableModel jTable1Model = 
				new DefaultTableModel(
						e.getStats(),
						new String[] { "", "" });
		jTable1 = new JTable();
		jTable1.setModel(jTable1Model);
		jScrollPane3.setViewportView(jTable1);
	}

	private void jList2MouseClicked(MouseEvent evt) {

		if(jList2.getSelectedValue()!=null)
		{
			Entity en = (Entity)jList2.getSelectedValue();

			setTable(en);
		}
	}

	private void hideNoHide() {
		if(this.jCheckBox1.isSelected())
		{
			ArrayList<Entity> entities = ent.getEntitiesList();

			LinkedList<Entity> ents = new LinkedList<Entity>();

			for(int i=0;i<entities.size();i++)
			{
				ents.add(entities.get(i));
			}

			Entity[] elis = new Entity[ents.size()];

			//			Entity e = elis[0];

			for(int i=0;i<ents.size();i++)
			{
				elis[i] = ents.get(i);
			}

			ListModel<Entity> jList1Model = new DefaultComboBoxModel<>(elis);
			jList1.setModel(jList1Model);
		}
		else fillComponents();
	}

}
