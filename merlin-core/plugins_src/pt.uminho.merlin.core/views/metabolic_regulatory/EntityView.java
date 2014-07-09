package views.metabolic_regulatory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import utilities.AIBenchUtils;
import views.UpdatablePanel;
import datatypes.metabolic_regulatory.Entity;

/**
 * @author ODias
 *
 */
public class EntityView extends UpdatablePanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane1;
	private JTable jTable;
	private Entity entity;

	/**
	 * @param e
	 */
	public EntityView(Entity e) {

		super(e);
		initGUI(e);
		this.addListenersToGraphicalObjects();
	}

	/**
	 * @param ent
	 */
	private void initGUI(Entity ent) {

		try {

			this.entity = ent;

			BorderLayout thisLayout = new BorderLayout();
			this.setLayout(thisLayout);
			setPreferredSize(new Dimension(400, 300));
			{
				jScrollPane1 = new JScrollPane();
				this.add(jScrollPane1, BorderLayout.CENTER);
				jTable = new JTable();
				jTable.setEnabled(false);
				this.setTable();
				jTable.setToolTipText("Click to refresh");
				jScrollPane1.setViewportView(jTable);
				jScrollPane1.setBorder(new TitledBorder(ent.getName()+" data"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void setTable() {

		TableModel jTable1Model = 
				new DefaultTableModel(
						entity.getStats(),
						new String[] { "", "" });
		jTable.setModel(jTable1Model);

	}

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#updateGraphicalObject()
	 */
	@Override
	public void updateTableUI() {

		this.setTable();
		this.updateUI();
		this.revalidate();
		this.repaint();
	}

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#addListenersToGraphicalObjects(javax.swing.JPanel, javax.swing.JTable)
	 */
	@Override
	public void addListenersToGraphicalObjects() {
	
		jTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {

				updateTableUI();
				AIBenchUtils.updateView(entity.getProject().getName(), entity.getClass());
			}
		});

		this.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {}

			@Override
			public void focusGained(FocusEvent arg0) {

				updateTableUI();
				System.out.println(entity.getProject().getName());
				System.out.println(entity.getClass());
				AIBenchUtils.updateView(entity.getProject().getName(), entity.getClass());
			}
		});
	}
}
