package baqsi.ui;

import java.awt.Color;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import baqsi.controllers.Controllers;
import baqsi.model.Audio;
import baqsi.model.Model;
import baqsi.utils.Utils;




@SuppressWarnings("serial")
public class InformationPanel extends JPanel implements ListSelectionListener,
		ChangeListener {

	Controllers c;

	JTextArea name;
	JTextArea length;
	JTextArea detectedreference;
	JTextArea transposition;

	public InformationPanel(Controllers cin) {
		c = cin;
		name = new JTextArea();
		name.setBackground(getBackground());
		length = new JTextArea();
		length.setBackground(getBackground());
		detectedreference = new JTextArea();
		detectedreference.setBackground(getBackground());
		transposition = new JTextArea();
		transposition.setBackground(getBackground());

		setLayout(new GridBagLayout());

		GridBagConstraints ctr = new GridBagConstraints();
		ctr.anchor = GridBagConstraints.NORTHWEST;
		ctr.weightx = 1;
		ctr.gridx = 0;
		ctr.gridy = 0;
		add(name, ctr);
		ctr.anchor = GridBagConstraints.WEST;
		ctr.gridy = 1;
		add(length, ctr);
		ctr.gridy = 2;
		add(detectedreference, ctr);
		ctr.gridy = 3;
		add(transposition, ctr);

	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub

		Model m = (Model) ((SampleList) (e.getSource())).getModel();
		ListSelectionModel lsm = ((SampleList) (e.getSource()))
				.getSelectionModel();
		// System.out.println(lsm.getMinSelectionIndex()+" / "+lsm.getMaxSelectionIndex()
		// + " / "+ e.getValueIsAdjusting() );

		if (lsm.getMinSelectionIndex() < m.getSize()
				&& lsm.getMinSelectionIndex() >= 0) {

			Audio selAudio = m.get(lsm.getMinSelectionIndex());
			update(selAudio);
		}

		// repaint();
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		// TODO Auto-generated method stub

		update(c.getCurrentselection());

	}

	public void update(Audio a) {
		if (a != null) {
			name.setText("Name : " + a.name);
			length.setText("Length : "
					+ Integer.toString(a.inlength / a.fs) + "s");

			if (a.histogram != null) {
				detectedreference.setText("Reference tunning detected : "
						+ (6900 - a.scale.refcent) + "cts (A4="
						+ (float) Utils.ctstof(a.scale.refcent) + "Hz)");
				transposition.setText("transposition :" + a.transposition
						+ "cts");

			}

			else {
				detectedreference.setForeground(Color.red);
				detectedreference.setText("Pitch not found");
			}

		}
	}

}
