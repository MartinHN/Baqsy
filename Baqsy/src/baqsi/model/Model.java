package baqsi.model;

import java.util.ArrayList;
import javax.swing.AbstractListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import baqsi.controllers.Controllers;
import baqsi.utils.MediaPlayer;
import baqsi.utils.Utils;



@SuppressWarnings("serial")
 public class Model extends AbstractListModel {

	public ArrayList<Audio> audiolist;

	public ScaleTable scaletable;
	public PartTable partTable;
	 public PatternTable pattTable;

	public MediaPlayer mp;
	public Controllers c;

	public Model() {
		super();

		audiolist = new ArrayList<Audio>();
		scaletable = new ScaleTable();
		partTable = new PartTable();
		pattTable = new PatternTable(); 

		mp = new MediaPlayer();

		// importaudio(new File("media/farrokhnamaorig.wav"));
	}

	public void importaudio(Audio a) {

		a.color = Utils.audiocolors[audiolist.size() % Utils.audiocolors.length];
		audiolist.add(a);

		fireIntervalAdded(this, audiolist.size() - 1, audiolist.size() - 1);
		scaletable.fireTableRowsInserted(audiolist.size() - 1,
				audiolist.size() - 1);
		partTable.fireTableRowsInserted(audiolist.size() - 1,
				audiolist.size() - 1);
		

	}

	public void deleteAudio(Audio a) {

		if (audiolist.remove(a))
			fireContentsChanged(this, 0, audiolist.size());
		for (int i = 0; i < audiolist.size(); i++) {
			audiolist.get(i).color = Utils.audiocolors[i
					% Utils.audiocolors.length];
		}
		scaletable.fireTableDataChanged();
		partTable.fireTableDataChanged();

	}

	public void setViewable(int i, boolean sel) {
		audiolist.get(i).setViewable(sel);
		fireContentsChanged(this, i - 1, i);

	}

	public int containsname(Audio a) {
		int res = 0;
		for (int k = 0; k < audiolist.size(); k++) {
			if (audiolist.get(k).name.split("__")[0].contains(a.name
					.split("__")[0]))
				res++;

		}
		return res;
	}

	public int getIdx(Audio a) {
		int idx = -1;
		if (audiolist.contains(a)) {

			for (int k = 0; k < audiolist.size(); k++) {
				if (audiolist.get(k) == a) {
					idx = k;
					break;
				}
			}

		} else
			System.out.println("wrong search of index");

		return idx;
	}

	public Audio get(int i) {
		return audiolist.get(i);
	}

	@Override
	public Object getElementAt(int arg0) {
		return audiolist.get(arg0);
	}

	@Override
	public int getSize() {
		return audiolist.size();
	}

	public class PartTable extends AbstractTableModel implements ChangeListener {
		final String[] columnNames = { " ", "Note width [cts]",
				"Min. duration [ms]", "Scaled", "Name" };

		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return c.getViewableAudioList().size();
		}

		@Override
		public Object getValueAt(int arg0, int arg1) {
			// TODO Auto-generated method stub
			if (c.getViewableAudioList().size() > 0) {
				Audio selAudio = c.getViewableAudioList().get(arg0);

				switch (arg1) {
				case 0:
					return selAudio.isSelectedForPartition;
				case 1:
					return selAudio.notewidth;
				case 2:
					return selAudio.minnotelength;
				case 3:
					return selAudio.isReducedToScale;
				case 4:
					return selAudio.name;
				default:
					break;
				}
			}
			return null;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			// super.setValueAt(aValue, rowIndex, columnIndex);
			// System.out.println(aValue);
			// Check
			if (rowIndex >= 0&&rowIndex<c.getViewableAudioList().size()) {
				switch (columnIndex) {
				case 0:
					c.getViewableAudioList().get(rowIndex).isSelectedForPartition = !c.getViewableAudioList()
							.get(rowIndex).isSelectedForPartition;
					break;
				case 1:

					c.getViewableAudioList().get(rowIndex).notewidth = (Integer) aValue;

					break;

				case 2:
					c.getViewableAudioList().get(rowIndex).minnotelength = (Integer) aValue;
					break;
				case 3:
					c.getViewableAudioList().get(rowIndex).isReducedToScale = !c.getViewableAudioList().get(rowIndex).isReducedToScale;

				default:
					break;
				}
			}

			fireTableCellUpdated(rowIndex, columnIndex);
			c.partctl.updatew();

		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col].toString();
		}

		@Override
		public Class<?> getColumnClass(int arg0) {
			return getValueAt(0, arg0).getClass();
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {
			
			if (arg1 == 4)
				return false;
			return true;

		}

		@Override
		public void stateChanged(ChangeEvent e) {
			
			c.partctl.updatew();
		}

	}

	public class ScaleTable extends AbstractTableModel implements ChangeListener {
		final String[] columnNames = { " ", "Range [cts]", "Degrees",
				"transp. [cts]", "Name" };

		public ScaleTable() {

		}

		public Scale get(int idx) {
			return c.getViewableAudioList().get(idx).scale;
		}

		public Audio getaudio(int idx) {
			return c.getViewableAudioList().get(idx);
		}

		@Override
		public int getColumnCount() {

			return columnNames.length;
		}

		@Override
		public int getRowCount() {

			return c.getViewableAudioList().size();
		}

		@Override
		public Object getValueAt(int arg0, int arg1) {

			Object res = null;
			if (getRowCount() > 0) {

				switch (arg1) {

				case 4:
					res = c.getViewableAudioList().get(arg0).name;
					break;

				case 3:
					res = c.getViewableAudioList().get(arg0).transposition;
					break;
				case 2:
					res = c.getViewableAudioList().get(arg0).scale.size;
					break;

				case 1:
					res = c.getViewableAudioList().get(arg0).histogram.selectivity;
					break;
				case 0:
					res = c.getViewableAudioList().get(arg0).isSelectedForScale;
					break;

				default:
					break;
				}

			} else
				return null;
			return res;
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col].toString();
		}

		@Override
		public Class<?> getColumnClass(int arg0) {

			return getValueAt(0, arg0).getClass();
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {

			if (arg1 == 4)
				return false;
			return true;

		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

			// Check
			if (rowIndex >= 0) {
				switch (columnIndex) {
				case 0:
					c.getViewableAudioList().get(rowIndex).isSelectedForScale = !c.getViewableAudioList()
							.get(rowIndex).isSelectedForScale;
					break;
				case 1:

					c.getViewableAudioList().get(rowIndex).histogram.selectivity = (Integer) aValue;
					// audiolist.get(rowIndex).histogram.computeHistogram(audiolist.get(rowIndex).partition);
					break;

				case 2:
					c.getViewableAudioList().get(rowIndex).scale.size = (Integer) aValue;
					break;
				case 3:

					c.getViewableAudioList().get(rowIndex).transposition = (Integer) aValue;
					break;
				default:
					break;
				}
			}

			fireTableCellUpdated(rowIndex, columnIndex);
			c.scalectl.updatew();

		}

		@Override
		public void stateChanged(ChangeEvent e) {

			// System.out.println(e);
			c.scalectl.updatew();

		}

	}

	public void registerControllers(Controllers c) {
		mp.c = c;
		this.c = c;
	}
	
	
	
	public class PatternTable extends AbstractTableModel{

		final private String[] columnNames = {"Name","Patterns"};
		
		
		
		@Override
		public int getColumnCount() {

			return columnNames.length;
		}

		@Override
		public int getRowCount() {

			return audiolist.size();
		}

		@Override
		public Object getValueAt(int arg0, int arg1) {

			Object res = null;
			if (getRowCount() > 0) {

				switch (arg1) {

				case 0:
					res = audiolist.get(arg0).name;
					break;

				case 1:
					res = audiolist.get(arg0).pattlist;
					break;
				

				default:
					break;
				}

			} else
				return null;
			return res;
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col].toString();
		}

		@Override
		public Class<?> getColumnClass(int arg0) {

			return getValueAt(0, arg0).getClass();
		}

		@Override
		public boolean isCellEditable(int arg0, int arg1) {

		
				return true;
	

		}
		
		public int getMaxTime(){
			int time = 1000;
			
			for (int k = 0; k<getSize() ; k++){
				PatternList pattl = get(k).pattlist;
				for (int j = 0 ; j< pattl.size();j++){
					time = Math.max(time , pattl.get(j).references.get(1).time +pattl.get(j).references.get(1).length  );	
				}
				
			}
			
			
			return time;
		}

	

	}

}
