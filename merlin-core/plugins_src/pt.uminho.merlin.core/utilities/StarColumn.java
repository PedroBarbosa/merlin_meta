/**
 * 
 */
package utilities;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * @author ODias
 *
 */
public class StarColumn {


		private EventListener eventListener;
		private Map<Integer, JButton> valueArray;
		private MouseListener mouseListener;
		private Map<Integer, Integer> starsColorMap;


		/**
		 * @param jTable
		 * @param column
		 * @param eventListener
		 * @param mouseListener
		 */
		public StarColumn(JTable jTable, int column, EventListener eventListener, MouseListener mouseListener, Map<Integer, Integer> starsColorMap) {
			
			super();
			this.eventListener = eventListener;
			this.mouseListener = mouseListener;
			this.starsColorMap = starsColorMap;
			//button.setFocusPainted( false );
			//button.setSize(new Dimension(1,1));
			TableColumnModel columnModel = jTable.getColumnModel();
			TableColumn dataColumn = columnModel.getColumn(column);
			
			valueArray = new TreeMap<Integer,JButton>();
			this.build(dataColumn);
		}

		/**
		 * @param dataColumn
		 */
		private void build(TableColumn dataColumn){

			dataColumn.setCellRenderer(new TableCellRenderer(){
				public Component getTableCellRendererComponent(JTable table, Object value, 
						boolean isSelected, boolean hasFocus, int row, int column) {
					
					JButton button;
					if(valueArray.containsKey(row)) {
						
						button = valueArray.get(row);
						return button;
					}
					else {
						
						if(row != -1 && row < table.getRowCount()) {
							
							button = createButton(value, row);
							valueArray.put(row, button);
							return button;
						}
					}
					return null;
				}
			});
			dataColumn.setCellEditor(new TableCellEditor() {
				
				public Component getTableCellEditorComponent(JTable table, Object value, boolean flag, int row, int column) {
					this.isCellEditable(row, column);
					return valueArray.get(row);
				}
				public void addCellEditorListener(CellEditorListener arg0) {}
				public void cancelCellEditing() {}
				public Object getCellEditorValue() {return null;}
				public boolean isCellEditable(EventObject arg0) {return true;}
				public boolean isCellEditable(int row, int column) {return true;}
				public void removeCellEditorListener(CellEditorListener arg0) {}
				public boolean shouldSelectCell(EventObject arg0) {return true;}
				public boolean stopCellEditing() {return true;}	
			});

		}

		/**
		 * @param value
		 * @param row 
		 * @return
		 */
		private JButton createButton(Object value, int row) {
			
			if(value == null) {
				
				value = "-1";
			}
			JButton button = new JButton();
			button.setEnabled(true);
			
			if(((String)value).equals("1")) {
				
				String path = "icons/StarGold.png";
				button.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource(path)),0.05).resizeImageIcon());
				
				if(this.starsColorMap!= null && this.starsColorMap.containsKey(row)) {
					
					//button.setIcon(this.getIcon(this.starsColorMap.get(row), path));
					button.setBackground(StarColumn.getBackgroundColor(this.starsColorMap.get(row)));
				}

				button.setToolTipText("UniProt Reviewed");
				addListenerToButton(button);
				addMouseListenerToButton(button);
			}
			else if(((String)value).equals("0")) {
				
				String path = "icons/StarSilver.png";
				button.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource(path)),0.05).resizeImageIcon());
				
				if(this.starsColorMap!= null && this.starsColorMap.containsKey(row)) {
					
					//button.setIcon(this.getIcon(this.starsColorMap.get(row), path));
					button.setBackground(StarColumn.getBackgroundColor(this.starsColorMap.get(row)));
				}
				button.setToolTipText("UniProt Unreviewed");
				addListenerToButton(button);
				addMouseListenerToButton(button);
			}
			else {
				
				button.setEnabled(false);
			}
			
			return button;
		}

		/**
		 * @param button
		 */
		private void addListenerToButton(JButton button){
			button.addActionListener((ActionListener) eventListener);

		}

		/**
		 * @param button
		 */
		private void addMouseListenerToButton(JButton button){
			button.addMouseListener(mouseListener);
		}

		/**
		 * @param button
		 * @return
		 */
		public int getSelectIndex(JButton button){
			for(int i : valueArray.keySet())
			{
				if(valueArray.get(i)==button)
				{
					return i;
				}
			}
			return -1;
		}

		/**
		 * @return
		 */
		public Map<Integer, JButton> getValueArray() {
			
			return valueArray;
		}
		
		/**
		 * @param icon
		 * @return
		
		private ImageIcon getIcon(int icon, String path) {
			
			if(icon == -1) {
				
				path = "icons/StarOrange.png";
			}
			
			if(icon == 0) {
				
				path = "icons/StarRed.png";
			}
			
			if(icon == 1) {
				
				path = "icons/StarGreen.png";
			}
			
			if(icon == 2) {
				
				path = "icons/StarLightGreen.png";
			}
			
			return new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource(path)),0.05).resizeImageIcon();
		}
		 */
		
		/**
		 * @param colorInt
		 * @return
		 */
		public static Color getBackgroundColor(int colorInt) {
			
			Color color = UIManager.getColor( "Button.background" );
			
			if(colorInt == -1) {
				
				color = new Color(245,222,179);
			}
			
			if(colorInt == 0) {
				
				color = new Color(250,128,114);
			}
			
			if(colorInt == 1) {
				
				//color = new Color(143,188,143);
				color = new Color(128,128,0);
			}
			
			if(colorInt == 2) {
				
				//color = new Color(152,251,152);
				color = new Color(154, 205, 50);
			}
			 
			return  color;
		}
	}
