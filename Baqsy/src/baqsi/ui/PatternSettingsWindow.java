package baqsi.ui;



import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;

import baqsi.controllers.Controllers;

@SuppressWarnings("serial")
public class PatternSettingsWindow extends JFrame{
Controllers c;
	public JSlider melodicThreshold;
	public JSlider timeThreshold;
	public JSlider minimumlength;
	public JSlider maximumlength;
	JSlider xscale;
JCheckBox disttype;
	
	public PatternSettingsWindow(Controllers cin) {
		c=cin;
		
		
		
		setTitle("Pattern search settings");
		setSize(300, 500);
		setLocationRelativeTo(null);
		setResizable(true);
		
		setMinimumSize(new Dimension(300, 300));
		build();
		place();
		
		setVisible(false);
	}
	public void initvalues(){
		melodicThreshold.setValue(20);
		timeThreshold.setValue(35);
	}
	
	private void build(){
		
		melodicThreshold = new JSlider(0,100);
		melodicThreshold.setFocusable(false);
		melodicThreshold.setName("melodicThreshold");
		melodicThreshold.setMajorTickSpacing(10); 
		melodicThreshold.setPaintLabels(true); 
		melodicThreshold.addChangeListener(c.patternctl);
		
		timeThreshold = new JSlider(0,100);
		timeThreshold.setFocusable(false);
		timeThreshold.setName("timeThreshold");
		timeThreshold.setMajorTickSpacing(10); 
		timeThreshold.setPaintLabels(true); 
		timeThreshold.addChangeListener(c.patternctl);
		
		
		minimumlength = new JSlider(1,8);
		minimumlength.setFocusable(false);
		minimumlength.setName("minlength");
		minimumlength.setMajorTickSpacing(1); 
		minimumlength.setSnapToTicks(true);
		minimumlength.setPaintLabels(true); 
		minimumlength.addChangeListener(c.patternctl);
		
		maximumlength = new JSlider(2,14);
		maximumlength.setFocusable(false);
		maximumlength.setName("maxlength");
		maximumlength.setSnapToTicks(true);
		maximumlength.setMajorTickSpacing(2); 
		maximumlength.setMinorTickSpacing(1);
		maximumlength.setPaintLabels(true); 
		maximumlength.addChangeListener(c.patternctl);
		
		xscale = new JSlider(100,600);
		xscale.setFocusable(false);
		xscale.setName("xscale");
		xscale.setMajorTickSpacing(100); 
		xscale.setPaintLabels(true); 
		xscale.addChangeListener(c.patternctl);
		
		
		disttype = new JCheckBox();
		disttype.setName("disttype");
		disttype.setText("Inter Onsets Interval");
		disttype.setActionCommand("disttype");
		disttype.addChangeListener(c.patternctl);
		
		
	}
	
	private void place(){
		
		
	
		
		JPanel distpan = new JPanel();
		distpan.setLayout(new BoxLayout(distpan, BoxLayout.Y_AXIS));
		
		 distpan.setBorder(BorderFactory.createTitledBorder("Distance settings") );
		 distpan.add(new JLabel("melodic threshold [%]"));
		distpan.add(melodicThreshold);
		distpan.add(new JLabel("time threshold [%]"));
		distpan.add(timeThreshold);
		distpan.add(disttype);
		
		
		JPanel autohyp = new JPanel();
		autohyp.setBorder(BorderFactory.createTitledBorder("Auto-search settings"));
		autohyp.setLayout(new BoxLayout(autohyp, BoxLayout.Y_AXIS));
		autohyp.add(new JLabel("minimum Pattern length [s]"));
		autohyp.add(minimumlength);
		autohyp.add(new JLabel("maximum Pattern length [s]"));
		autohyp.add(maximumlength);
		
		
		
		
		setLayout(new GridBagLayout());
		GridBagConstraints ctr = new GridBagConstraints();
ctr.gridx=0;
ctr.gridy=0;
ctr.weighty=1;
ctr.weightx=1;
ctr.fill = GridBagConstraints.BOTH;
ctr.insets= new Insets(0, 0, 25, 0);

		add(distpan,ctr);
		ctr.gridy++;
		add(autohyp,ctr);
		ctr.gridy++;
		add(new JLabel("table time scale [px/ms]"),ctr);
		ctr.gridy++;
		add(xscale,ctr);
		
		//add()
	}
	
	
	
}
