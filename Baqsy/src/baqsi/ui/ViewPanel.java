package baqsi.ui;


import java.awt.Dimension;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ButtonGroup;

import javax.swing.ImageIcon;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import baqsi.controllers.Controllers;


@SuppressWarnings("serial")
public class ViewPanel extends JPanel {

	// Components

	
	
	JRadioButton griddisabled;
	JRadioButton grid;
	JRadioButton gridrescaled;

	JCheckBox notePitch;

	JCheckBox followCursor;
	
	
	
	public ViewPanel(Controllers c) {

		build(c);
		place();
		setMinimumSize(new Dimension(100,30));
		//addMouseListener(this);
		//System.out.println(getMouseListeners().getClass().toString());
		//setMaximumSize(new Dimension(6000,31));
	}

	public void build(Controllers c) {
		
		griddisabled = new JRadioButton("none", false);
		griddisabled.setFocusable(false);
		griddisabled.setActionCommand("none");
		griddisabled.setIcon(new ImageIcon(getClass().getResource("emptyset_nok.png")));
		griddisabled.setSelectedIcon(new ImageIcon(getClass().getResource("emptyset_ok.png")));

		grid = new JRadioButton("chromatic", true);
		grid.setFocusable(false);
		grid.setActionCommand("normal");
		grid.setIcon(new ImageIcon(getClass().getResource("Gkey_nok.png")));
		grid.setSelectedIcon(new ImageIcon(getClass().getResource("Gkey_ok.png")));
		
		gridrescaled = new JRadioButton("scale", false);
		gridrescaled.setFocusable(false);
		gridrescaled.setActionCommand("rescaled");
		gridrescaled.setIcon(new ImageIcon(getClass().getResource("ladder_nok.png")));
		gridrescaled.setSelectedIcon(new ImageIcon(getClass().getResource("ladder_ok.png")));
		
		
		ButtonGroup gridgroup = new ButtonGroup();

		gridgroup.add(griddisabled);
		gridgroup.add(grid);
		gridgroup.add(gridrescaled);

		griddisabled.addActionListener(c.viewctl);
		grid.addActionListener(c.viewctl);
		gridrescaled.addActionListener(c.viewctl);

		notePitch = new JCheckBox();
		notePitch.setFocusable(false);
		notePitch.setIcon(new ImageIcon(getClass().getResource("detectedpitch.png")));
		notePitch.setSelectedIcon(new ImageIcon(getClass().getResource("undetectedpitch.png")));
		notePitch.setText("show undetected pitch");
		
		notePitch.setActionCommand("notepitch");
		notePitch.addActionListener(c.viewctl);
		
		
		followCursor = new JCheckBox();
		followCursor.setFocusable(false);
		followCursor.setSelected(true);
followCursor.setIcon(new ImageIcon(getClass().getResource("Arrow_nok.png")));
followCursor.setSelectedIcon(new ImageIcon(getClass().getResource("Arrow_ok.png")));
followCursor.setActionCommand("follow");
followCursor.addActionListener(c.viewctl);
followCursor.setText("follow time cursor");

	}

	public void place() {

		setLayout(new GridBagLayout());
		GridBagConstraints ctr = new GridBagConstraints();
		
		ctr.gridx = 0;
		ctr.gridy = 0;
		ctr.fill = GridBagConstraints.BOTH;
		ctr.weightx =1;
		ctr.weighty=1;
		ctr.anchor = GridBagConstraints.WEST;
		
		
		add(griddisabled, ctr);
		ctr.gridx ++;
		add(grid, ctr);
		ctr.gridx++;
		add(gridrescaled, ctr);

		
		ctr.gridx++;
		
		
		add(notePitch, ctr);
		
		
		//ctr.gridheight =3;
		ctr.gridx++;
		//ctr.gridy=0;
		add(followCursor,ctr);

	}

	

}
