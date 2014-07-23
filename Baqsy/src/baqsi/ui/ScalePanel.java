package baqsi.ui;
import java.awt.Dimension;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
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
public class ScalePanel extends JPanel {
	Controllers c;

	public JTable scalelistw;
	public JButton fitTransposition;

	public ScalePartition scalespart;

	public int lastmouseselected = 0;

	ScalePanel(Controllers c) {
		this.c = c;
		build();
		place();
	}

	private void build() {
		fitTransposition = new JButton(c.patternctl.fittranspose);
		fitTransposition.setText("fit transposition");
		fitTransposition.setFocusable(false);
		
		scalelistw = new JTable(c.m.scaletable);
		scalelistw.setFocusable(false);
		scalelistw.getColumnModel().getColumn(0).setMaxWidth(25);
		scalelistw.getColumnModel().getColumn(0).setMinWidth(20);
		scalelistw.getColumnModel().getColumn(1).setMaxWidth(70);
		scalelistw.getColumnModel().getColumn(1).setMinWidth(60);
		scalelistw.getColumnModel().getColumn(2).setMaxWidth(70);
		scalelistw.getColumnModel().getColumn(2).setMinWidth(60);
		scalelistw.getColumnModel().getColumn(3).setMaxWidth(140);
		scalelistw.getColumnModel().getColumn(3).setMinWidth(75);

		scalelistw.setRowHeight(18);

		scalelistw.setDefaultRenderer(Boolean.class, new MyScaleRenderer(this));
		scalelistw.setDefaultRenderer(Integer.class, new MyScaleRenderer(this));

		scalelistw.setDefaultRenderer(String.class, new MyScaleRenderer(this));
		scalelistw.setCellSelectionEnabled(true);
		scalelistw.setDefaultEditor(Integer.class, new MyScaleEditor());

		scalelistw.setBorder(null);

		scalespart = new ScalePartition(c);
		scalespart.setFocusable(false);

	}

	private void place() {

		JScrollPane scpane = new JScrollPane(scalelistw);

		scpane.setPreferredSize(new Dimension(300, 150));
		scpane.setMinimumSize(new Dimension(100, 100));
		setLayout(new GridBagLayout());
		GridBagConstraints ctr = new GridBagConstraints();
		ctr.fill = GridBagConstraints.BOTH;
		ctr.weightx = 1;
		ctr.weighty = 10;
		ctr.gridx = 0;
		ctr.gridy = 0;

		add(scpane, ctr);
		ctr.weighty=1;
		ctr.gridy++;
		add(fitTransposition,ctr);
		
		

		JScrollPane scalescrollpane = new JScrollPane(scalespart);
		scalescrollpane.setPreferredSize(new Dimension(400, 150));
		scalescrollpane.setMinimumSize(new Dimension(100, 100));
		ctr.gridx = 1;
		ctr.gridy=0;
		ctr.gridheight=2;
		add(scalescrollpane, ctr);

	}

	public class MyScaleRenderer implements TableCellRenderer {
		ScalePanel s;

		MyScaleRenderer(ScalePanel s) {
			this.s = s;

		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int col) {
			if (value != null) {
				if (col == 4 && isSelected && row != s.lastmouseselected) {
					s.lastmouseselected = row;
					s.repaint();
					return null;
				} else {

					Component res = null;
					switch (col) {
					case 0:
						res = new JCheckBox();
						if (value != null)
							((JCheckBox) res).setSelected((Boolean) value);
						((JCheckBox) res).setBackground(Color.white);
						break;
					case 1:
						res = new JSpinner(new SpinnerNumberModel(
								(int) (Integer) value, -10000, 10000, 1));
						((JSpinner) res)
								.setToolTipText("set the selectivity of the histogram : the range of notes being considered as the same degree");

						break;
					case 2:
						res = new JSpinner(new SpinnerNumberModel(
								(int) (Integer) value, 1, 50, 1));
						((JSpinner) res)
								.setToolTipText("set the number of degrees in scale");

						break;
					case 3:

						res = new JSpinner(new SpinnerNumberModel(
								(int) (Integer) value, -10000, 10000, 1));
						((JSpinner) res).setToolTipText("transpose all");

						break;

					case 4:
						res = new JTextArea();
						((JTextArea) res).setText((String) value);

						((JTextArea) res).setForeground(c.getViewableColors()[row]);
						if (s.lastmouseselected == row)
							((JTextArea) res).setBorder(new LineBorder(
									Color.blue));
						else
							((JTextArea) res).setBorder(null);
						break;

					default:
						break;
					}

					return res;

				}

			} else
				return null;

		}

	}

	public class MyScaleEditor extends DefaultCellEditor implements
			ChangeListener {
		JSpinner spinner;
		int currow;
		int curcol;

		public MyScaleEditor() {
			super(new JTextField());

			spinner = new JSpinner();
			spinner.setBorder(null);
			spinner.addChangeListener(this);
			spinner.addChangeListener(c.m.scaletable);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			curcol = column;
			currow = row;

			switch (column) {

			case 1:
				spinner.setModel(new SpinnerNumberModel(1, 1, 500, 5));
				spinner.setToolTipText("set the selectivity of the histogram : the range of notes being considered as the same note for the scale");
				break;
			case 2:
				spinner.setModel(new SpinnerNumberModel(1, 1, 50, 1));
				spinner.setToolTipText("set the number of degrees in the scale");
				break;

			case 3:
				spinner.setModel(new SpinnerNumberModel(0, -10000, 10000, 10));
				spinner.setToolTipText("transpose all");
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
			case 3:
				curAudio.updateTransposition((Integer) ((JSpinner) e
						.getSource()).getValue());
				break;

			case 2:
				curAudio.scale
						.searchmaxidx((Integer) ((JSpinner) e.getSource())
								.getValue());
				break;

			case 1:
				curAudio.histogram.selectivity = (Integer) ((JSpinner) e
						.getSource()).getValue();
				curAudio.histogram.updateHistogram(curAudio.partition);
				curAudio.scale.computeScale();
				break;

			default:
				System.out.println("erreur colonne");
				break;
			}
		}

	}
}
