package baqsi.ui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;

import baqsi.controllers.Controllers;
import baqsi.utils.MediaPlayer;
//import org.jdesktop.swingx.JXCollapsiblePane;




// delete multiple

//syncrocurseur

// ajouter boutton de lecture

@SuppressWarnings("serial")
public class Window extends JFrame {
	// attributes
	public Controllers c;
	// static Listeners wlist;
	// GUI's
	public SampleList samplelist;
	public PitchGraph graphw;
	public Histograph histw;


	public JProgressBar progbar;

	public JPanel controlpanel;
	public JTabbedPane tabpane;
	public SettingsPanel settingspane;
	public InformationPanel infopane;
	public ViewPanel viewpanel;
	public ScalePanel scalepanel;
	public PartitionPanel partitionpanel;
	public PatternPanel patternpanel;
	
	final JFrame thisw = this;
	private WindowListener wl = new WindowListener() {
		
		@Override
		public void windowOpened(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void windowIconified(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void windowDeiconified(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void windowDeactivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void windowClosing(WindowEvent arg0) {
			// TODO Auto-generated method stub
			Object[] choices = {"Save","No thanks","Cancel" };
			int n = JOptionPane.showOptionDialog(thisw, "Save before close?","Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,choices ,choices[0]);
			switch(n){
			case JOptionPane.YES_OPTION:
				c.savectl.actionPerformed(new ActionEvent(thisw, 0, "Save"));
				
				System.exit(0);
				break;
			case JOptionPane.NO_OPTION:
				System.exit(0);
				break;
			case JOptionPane.CANCEL_OPTION:
				break;
				default:
					System.exit(0);
					break;
			}
		}
		
		@Override
		public void windowClosed(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void windowActivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	};


	// constructor

	public Window() {
		super();

		build();

	}

	public Window(Controllers c) {
		super();
		this.c = c;

		// setBackground(Utils.bgColor);
		
		// objets
		progbar = new JProgressBar(0, 100);
		progbar.setStringPainted(true);
		progbar.setVisible(false);
		
		

		// ctl=c;
		samplelist = new SampleList(c);

		graphw = new PitchGraph(c);
		histw = new Histograph(c);
		viewpanel = new ViewPanel(c);

		// traitement = new JTextArea("<-- Drag your file on the left pane..");
		// traitement.setBackground(Color.gray);

		build();
		Listeners(c);
		
		addWindowListener(wl);
	

	}

	// methods

	private void build() {
		setTitle("Baqsy");
		setSize(1260, 700);
		setLocationRelativeTo(null);
		setResizable(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setMinimumSize(new Dimension(400, 300));
		setContentPane(buildContentPane());

	}

	private JSplitPane buildContentPane() {

		// / sample list
		JScrollPane samplepanel = new JScrollPane(samplelist);

		// visu histogram/graph


		controlpanel = new JPanel();
		

		tabpane = new JTabbedPane();
		settingspane = new SettingsPanel(c);
		infopane = new InformationPanel(c);
		
		partitionpanel = new PartitionPanel(c);
		scalepanel = new ScalePanel(c);
		patternpanel = new PatternPanel(c);
		
		
		
		tabpane.add("Info", infopane);
		tabpane.add("Transcription", partitionpanel);
		tabpane.add("Scale & Transposition", scalepanel);
		
		tabpane.add("Patterns",patternpanel);

		tabpane.add("Settings", settingspane);
		tabpane.addChangeListener(infopane);
		// information panel

		// GridBagConstraints rdctr = new GridBagConstraints();

		controlpanel.setLayout(new GridBagLayout());
		GridBagConstraints cpctr = new GridBagConstraints();
		cpctr.fill = GridBagConstraints.BOTH;
		cpctr.weightx = 1;
		cpctr.weighty = 10;
		cpctr.gridy = 0;
		controlpanel.add(tabpane, cpctr);

		cpctr.weightx = 1;
		cpctr.weighty = 1;
		cpctr.gridy = 1;
		controlpanel.add(progbar, cpctr);

		
		
		
	 
		// Split panels
		
		
		

		JSplitPane graphhisto = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				histw, graphw);
		
		JSplitPane viewgraph = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				viewpanel, graphhisto);
		//viewgraph.addMouseListener(viewpanel);

		JSplitPane rpanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				viewgraph, controlpanel);

		rpanel.setBackground(Color.lightGray);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				samplepanel, rpanel);
		splitPane.setBackground(Color.lightGray);

		// Dimensions
		graphhisto.setOneTouchExpandable(true);
		rpanel.setOneTouchExpandable(true);
		splitPane.setOneTouchExpandable(true);
		viewgraph.setOneTouchExpandable(true);
		
		viewgraph.setDividerLocation(30);
		viewgraph.setBorder(null);
		graphhisto.setBorder(null);
		splitPane.setDividerLocation(200);
		rpanel.setDividerLocation(450);
		graphhisto.setDividerLocation(100);
		Dimension minimumSize = new Dimension(100, 50);

		
		samplepanel.setMinimumSize(minimumSize);
		graphw.setMinimumSize(minimumSize);
		controlpanel.setMinimumSize(minimumSize);

		return splitPane;
	}

	private void Listeners(Controllers c) {
		// mouse listeners
		graphw.addMouseListener(graphw);
		graphw.addMouseMotionListener(graphw);
		graphw.addMouseWheelListener(graphw);
		


		// Model updates listeners

		c.m.addListDataListener(histw);

		// c.m.scalelist.addTableModelListener(scalepanel.scalelistw);

		// Selectionlisteners
		samplelist.addListSelectionListener(infopane);

		// key listeners
		graphw.addKeyListener(c.mediactl);
		samplelist.addKeyListener(c.mediactl);

	}

}
