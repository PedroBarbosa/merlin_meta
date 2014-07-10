package views.metabolic_regulatory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import datatypes.metabolic_regulatory.Entity;

public class EntityView extends javax.swing.JPanel {

	private static final long serialVersionUID = -2251856656594718565L;
	private JScrollPane jScrollPane1;
	private JTable jTable1;
	private Entity ent;

	public EntityView(Entity e) {
		super();
		initGUI(e);
	}

	private void initGUI(Entity enta) {

		this.ent = enta;
		try {

			BorderLayout thisLayout = new BorderLayout();
			this.setLayout(thisLayout);
			setPreferredSize(new Dimension(400, 300));
			{
				jScrollPane1 = new JScrollPane();
				this.add(jScrollPane1, BorderLayout.CENTER);
				{

					TableModel jTable1Model = 
							new DefaultTableModel(
									ent.getStats(),
									new String[] { "", "" });
					
					jTable1 = new JTable();
					jTable1.setEnabled(false);
					jTable1.setModel(jTable1Model);
					jTable1.setToolTipText("Click to refresh.");
					jTable1.addMouseListener(new MouseAdapter() {

						@Override
						public void mouseClicked(MouseEvent arg0) {
							
							TableModel jTable1Model = 
									new DefaultTableModel(
											ent.getStats(),
											new String[] { "", "" });
						
							jTable1.setModel(jTable1Model);
							
							jTable1.updateUI();
						}
					});
					jScrollPane1.setViewportView(jTable1);
				}

				jScrollPane1.setBorder(new TitledBorder(ent.getName()+" data"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
