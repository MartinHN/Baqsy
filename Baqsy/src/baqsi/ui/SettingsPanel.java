package baqsi.ui;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import baqsi.controllers.Controllers;


@SuppressWarnings("serial")
public class SettingsPanel extends JPanel {

	// parameters

	public JCheckBox stereo;
	public JLabel maxPName;
	public JSlider maxPitch;
	public JLabel minPName;
	public JSlider minPitch;

	public JLabel precisionName;
	public JSlider corrthresh;
	public JLabel volName;
	public JSlider volthresh;
	
	public JButton compute;

	
	public JButton save;
	
	public SettingsPanel(Controllers c) {
		// TODO Auto-generated constructor stub

		build(c);
		place();

	}

	private void build(Controllers c) {

		stereo = new JCheckBox();
		stereo.setActionCommand("stereo");
		stereo.setText("mono / stereo");
		stereo.setVerticalTextPosition(SwingConstants.BOTTOM);
		stereo.addItemListener(c.settingspanelctl);
		stereo.setMinimumSize(new Dimension(180, 40));
		stereo.setToolTipText("mono / stereo import");
		stereo.setIcon(new ImageIcon(getClass().getResource("mono.png")));
		stereo.setSelectedIcon(new ImageIcon(getClass().getResource("stereo.png")));
		stereo.setFocusable(false);

		maxPName = new JLabel("Maximum Pitch");

		maxPitch = new JSlider(54, 96);
		maxPitch.setFocusable(false);
		maxPitch.setName("maxPitch");
		maxPitch.setPaintLabels(true);
		maxPitch.setPaintTicks(true);
		maxPitch.addChangeListener(c.settingspanelctl);
		//

		minPName = new JLabel("Minimum Pitch");
		
		minPitch = new JSlider(12, 53);
		minPitch.setFocusable(false);
		minPitch.setName("minPitch");
		minPitch.setPaintLabels(true);
		minPitch.setPaintTicks(true);
		minPitch.addChangeListener(c.settingspanelctl);
		//

		precisionName = new JLabel("Precision");
	
		corrthresh = new JSlider(0, 1001);
		corrthresh.setFocusable(false);
		corrthresh.setName("corthresh");
		corrthresh.setPaintLabels(true);
		corrthresh.setPaintTicks(true);	
		corrthresh.addChangeListener(c.settingspanelctl);
		
		volName = new JLabel("Volume Threshold");
		volthresh = new JSlider(-100, 0);
		volthresh.setFocusable(false);
		volthresh.setName("volthresh");
		volthresh.setPaintLabels(true);
		volthresh.setPaintTicks(true);	
		volthresh.addChangeListener(c.settingspanelctl);

		compute = new JButton("Recompute!");
		compute.addActionListener(c.settingspanelctl);
		compute.setFocusable(false);
		
		
		save = new JButton("Save");
		save.setActionCommand("Save");
		save.addActionListener(c.savectl);
		save.setFocusable(false);
		
		

	}

	private void place() {

		setLayout(new GridBagLayout());
		GridBagConstraints ctr = new GridBagConstraints();
		ctr.anchor = GridBagConstraints.EAST;
		ctr.weightx = 1;
		ctr.weighty = 1;
		ctr.gridx = 0;
		ctr.gridy = 0;
		ctr.gridheight=3;
		add(stereo, ctr);
		
		ctr.gridheight=1;
		ctr.gridy=0;
		ctr.gridx++;
		add(maxPitch, ctr);
		ctr.gridy ++;	
		add(minPitch, ctr);
		ctr.gridy++;
		add(corrthresh, ctr);
		ctr.gridy++;
		add(volthresh,ctr);

		ctr.anchor=GridBagConstraints.WEST;
		ctr.gridy=0;
		ctr.gridx++;
		add(maxPName, ctr);
		ctr.gridy++;
		add(minPName, ctr);
		ctr.gridy++;
		add(precisionName, ctr);
		ctr.gridy++;
		add(volName,ctr);
		
		
		
		
		ctr.anchor = GridBagConstraints.CENTER;
		ctr.gridx++;
		ctr.gridy = 0;
		add(compute, ctr);
		ctr.gridy ++;
		add(save, ctr);
		

	}

}
