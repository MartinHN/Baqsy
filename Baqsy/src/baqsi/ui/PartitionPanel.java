package baqsi.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import javax.swing.JPanel;


import javax.swing.ImageIcon;
import javax.swing.JScrollPane;

import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellRenderer;

import baqsi.controllers.Controllers;
import baqsi.model.Audio;





@SuppressWarnings("serial")
public class PartitionPanel extends JPanel {
	public Controllers c;
	

	public JScrollPane partjsp;

	public AudioPartition part;

	public JTable partlist;

	public JButton midiExport;
	
	public JCheckBox vibrato;

	public int lastmouseselected;

	public PartitionPanel(Controllers cin) {
		c = cin;
		build(c);
		place();

	}

	private void build(Controllers c) {
		

		partlist = new JTable(c.m.partTable);
		partlist.setFocusable(false);
		partlist.getColumnModel().getColumn(0).setMaxWidth(25);
		partlist.getColumnModel().getColumn(0).setMinWidth(20);
		partlist.getColumnModel().getColumn(1).setMaxWidth(70);
		partlist.getColumnModel().getColumn(1).setMinWidth(60);
		partlist.getColumnModel().getColumn(2).setMaxWidth(70);
		partlist.getColumnModel().getColumn(2).setMinWidth(60);
		partlist.getColumnModel().getColumn(3).setMaxWidth(25);
		partlist.getColumnModel().getColumn(3).setMinWidth(20);
		partlist.setDefaultEditor(Integer.class, new MyPartEditor());
		partlist.setDefaultRenderer(Boolean.class, new MyScaleRenderer(this));
		partlist.setDefaultRenderer(Integer.class, new MyScaleRenderer(this));
		partlist.setDefaultRenderer(String.class, new MyScaleRenderer(this));
		partlist.setCellSelectionEnabled(true);
		partlist.setBorder(null);
		partlist.setRowHeight(18);

		part = new AudioPartition(c);
		part.setFocusable(false);

		midiExport = new JButton(new ImageIcon(getClass().getResource("MidiRec.png")));
		midiExport.setFocusable(false);
		midiExport.setContentAreaFilled(false);
		midiExport.setBorderPainted(false);
		midiExport.setRolloverEnabled(false);
		midiExport.setFocusPainted(false);
		midiExport.addActionListener(c.expctl);
		midiExport.setActionCommand("export");
		midiExport.setToolTipText("Export displayed partition in the desired folder");
		
		vibrato = new JCheckBox();
		vibrato.setText("Export vibrato");
		vibrato.setFocusable(false);
		
		vibrato.setSelected(true);
		vibrato.addActionListener(c.expctl);
		vibrato.setActionCommand("vibrato");
		
		
	}

	private void place() {
		setLayout(new GridBagLayout());
		GridBagConstraints ctr = new GridBagConstraints();
		ctr.fill = GridBagConstraints.BOTH;
		ctr.weightx = 1;
		ctr.weighty = 1;

		partjsp = new JScrollPane(part);
		partjsp.setPreferredSize(new Dimension(400, 150));
		partjsp.setMinimumSize(new Dimension(100, 100));

		JScrollPane listjsp = new JScrollPane(partlist);
		listjsp.setPreferredSize(new Dimension(300, 150));
		listjsp.setMinimumSize(new Dimension(100, 100));

		ctr.gridy = 0;
		ctr.gridx = 0;
		add(listjsp, ctr);

		ctr.gridx = 1;
ctr.gridwidth=2;
		add(partjsp, ctr);
		
		ctr.gridwidth=1;
		ctr.gridy = 1;
		ctr.gridx=1;
		add(midiExport,ctr);
		ctr.gridx=2;
		add(vibrato,ctr);
	}

	public class MyScaleRenderer implements TableCellRenderer {
		PartitionPanel p;

		MyScaleRenderer(PartitionPanel p) {
			this.p = p;

		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int col) {
			if (value != null) {
				if (col == 4 && isSelected && row != p.lastmouseselected) {
					p.lastmouseselected = row;
					p.repaint();
					return null;
				} else {

					Component res = null;
					switch (col) {
					case 0:
					case 3:
						res = new JCheckBox();
						if (value != null)
							((JCheckBox) res).setSelected((Boolean) value);
						((JCheckBox) res).setBackground(Color.white);
						break;
					case 1:
						res = new JSpinner(new SpinnerNumberModel(
								(int) (Integer) value, -10000, 10000, 1));
						((JSpinner) res)
								.setToolTipText("set the note width : tolerance about vibratos");

						break;
					case 2:
						res = new JSpinner(new SpinnerNumberModel(
								(int) (Integer) value, -10000, 10000, 1));
						((JSpinner) res)
								.setToolTipText("set the minimum length of a note");

						break;

					case 4:
						res = new JTextArea();
						((JTextArea) res).setText((String) value);

						((JTextArea) res).setForeground(c.getViewableColors()[row]);
						if (p.lastmouseselected == row)
							((JTextArea) res).setBorder(new LineBorder(
									Color.blue));
						else
							((JTextArea) res).setBorder(null);
						break;

					default:
						break;
					}
res.setFocusable(false);
					return res;

				}

			} else
				return null;

		}

	}

	public class MyPartEditor extends DefaultCellEditor implements
			ChangeListener {
		JSpinner spinner;
		int currow;
		int curcol;

		public MyPartEditor() {
			super(new JTextField());

			spinner = new JSpinner();
			spinner.setFocusable(false);
			spinner.setBorder(null);
			spinner.addChangeListener(this);
			spinner.addChangeListener(c.m.partTable);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			curcol = column;
			currow = row;

			switch (column) {

			case 1:
				spinner.setModel(new SpinnerNumberModel(1, 1, 500, 1));
				spinner.setToolTipText("set the maximal range of pitch variation for a note");
				break;
			case 2:
				spinner.setModel(new SpinnerNumberModel(0, 0, 500, 1));
				spinner.setToolTipText("set the minimal length of a note");
				break;

			default:
				break;
			}
			spinner.setValue(value);

			// spinner.addChangeListener(((Model.ScaleList)table.getModel()).getaudio(row));
			return spinner;
		}

		@Override
		public Object getCellEditorValue() {

			return spinner.getValue();
		}

		@Override
		public boolean isCellEditable(EventObject evt) {
			return true;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			Audio curAudio = c.getViewableAudioList().get(currow);

			switch (curcol) {

			case 2:
				curAudio.minnotelength = (Integer) ((JSpinner) e.getSource())
						.getValue();
				curAudio.partition.compute(curAudio);
				break;

			case 1:
				curAudio.notewidth = (Integer) ((JSpinner) e.getSource())
						.getValue();
				curAudio.partition.compute(curAudio);
				break;

			default:
				System.out.println("erreur colonne");
				break;
			}
		}

	}

}
