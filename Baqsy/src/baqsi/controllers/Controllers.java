package baqsi.controllers;

import java.awt.Color;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import javax.media.Time;
import javax.media.rtp.event.NewParticipantEvent;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import javax.swing.ListSelectionModel;

import javax.swing.JSlider;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import baqsi.controllers.Controllers.MediaCtl.UpdateTime;
import baqsi.model.Audio;
import baqsi.model.Histogram;
import baqsi.model.Model;
import baqsi.model.Partition;
import baqsi.model.Pattern;
import baqsi.model.PatternList;
import baqsi.model.Pitchs;
import baqsi.model.Reference;
import baqsi.model.Scale;
import baqsi.model.Note;
import baqsi.ui.Window;
import baqsi.utils.MediaPlayer;
import baqsi.utils.MidiUtils;
import baqsi.utils.Utils;


// name contains.



public class Controllers {
	public Model m;
	public Window w;

	// additional Controllers
	public ViewCtl viewctl;
	public ProgressCtl progctl;
	public ScaleCtl scalectl;
	public GraphCtl graphctl;
	public ExportCtl expctl;
	public MediaCtl mediactl;
	public PartitionCtl partctl;
	public PatternCtl patternctl;
	public PatternGraphCtl patterngraphctl;
	public SaveCtl savectl;

	//
	public SettingsPanelCtl settingspanelctl;

	public Controllers(Model m) {
		this.m = m;
		progctl = new ProgressCtl();

		settingspanelctl = new SettingsPanelCtl();
		viewctl = new ViewCtl();
		scalectl = new ScaleCtl();
		graphctl = new GraphCtl();
		expctl = new ExportCtl();
		mediactl = new MediaCtl();
		partctl = new PartitionCtl();
		patternctl = new PatternCtl();
		patterngraphctl = new PatternGraphCtl();
		savectl = new SaveCtl();

	}

	public void registerWindow(Window w) {

		this.w = w;
	}

	public void initvalues() {

		w.settingspane.maxPitch.setValue(82);
		w.settingspane.minPitch.setValue(40);
		// w.partitionpanel.minNoteLength.setValue(40);
		// w.partitionpanel.noteWidth.setValue(70);
		w.settingspane.stereo.setSelected(false);
		w.settingspane.corrthresh.setValue(200);
		w.patternpanel.settingsw.initvalues();
		w.patternpanel.settingsw.maximumlength.setValue(10);
		
		w.patternpanel.settingsw.minimumlength.setValue(4);
		
		
	
	}

	public ArrayList<Audio> getAudioList() {
		return m.audiolist;
	}

	public ArrayList<Audio> getViewableAudioList() {
		ArrayList<Audio> res = new ArrayList<Audio>();
		for (int k = 0; k < m.audiolist.size(); k++) {
			if (m.audiolist.get(k).isSelectedForView){
				res.add(m.audiolist.get(k));
		}}
		return res;
	}

	
	
	public void importPitch(ArrayList<Audio> l) {
		int selidx = m.getSize();
		for (int k = 0; k < l.size(); k++) {
			Audio a = l.get(k);

			if (a.pitch != null) {

				// histogram = new Histogram(pitch.values);

				// .pitch.constraints(0.10,0.10);
				a.pitch.setCorThresh(settingspanelctl.corthresh);
				a.setVolumeThresh(settingspanelctl.volumethresh);
				
				
		
				a.partition = new Partition();
				a.partition.compute(a);
		
				if (a.partition.size() > 0) {
					a.histogram = new Histogram(a.partition);
					a.scale = new Scale(a.histogram);
				}
				else{
					a.histogram = new Histogram();
					a.scale = new Scale();
				}

				
				

			}
			a.pitchUpdated();
			if(selidx+k<1){
				a.setViewable(true);
			}
			else{
				a.setViewable(false);
			}
			m.importaudio(l.get(k));
			//savectl.saveAudio(l.get(k));
		}
		w.samplelist.setSelectedIndex(selidx);
	}

	public void processAudio(ArrayList<Audio> l) {
		ArrayList<Audio> stereol = new ArrayList<Audio>();

		// import left and right part
		if (settingspanelctl.stereo) {

			for (int i = 0; i < l.size(); i++) {
				Audio a = l.get(i);
				// if
				// (m.containsname(a)!=0)a.name.concat("_"+m.containsname(a));
				stereol.add(a);
				if (a.numchannel == 2) {
					if (a.channel == 0) {
						Audio b = a.clone();

						a.name = a.name.concat(".L");
						a.channel = 1;

						b.name = b.name.concat(".R");
						b.channel = 2;
						stereol.add(b);
					}

				} else
					a.channel = 1;

			}
		} else {
			stereol = l;
		}

		for (int i = 0; i < stereol.size(); i++) {
			Audio a = stereol.get(i);
			if (m.containsname(a) != 0)
				a.name = a.name.split("__")[0].concat("__" + m.containsname(a));
		}

		PitchExtractors p = new PitchExtractors(stereol, this);

		p.mpm.addPropertyChangeListener(progctl);
		p.mpm.execute();

	}

	public boolean isEmpty() {
		return m.getSize() == 0;
	}

	public boolean selectionIsEmpty() {
		return getViewablePitchs().size() == 0;
	}

	public int getmaxtime(boolean viewable) {

		int maxtime = 0;

		if (m.audiolist != null) {
			ArrayList<Pitchs> plbuf = viewable ? getViewablePitchs()
					: getPitchs();

			for (int i = 0; i < plbuf.size(); i++) {

				maxtime = (int) Math.max(plbuf.get(i).size()
						/ plbuf.get(i).pitchfs * 1000.0, maxtime);
			}
		}

		return maxtime;
	}

	public int getMaxValue(boolean viewable) {
		int maxval = 0;
		if (!selectionIsEmpty()) {
			ArrayList<Histogram> histbuf = viewable ? getViewableHistogram()
					: getHistograms();

			for (int i = 0; i < histbuf.size(); i++) {
				Histogram hist = histbuf.get(i);
				if(hist.data.size()>0){
				maxval = Math.max(hist.get(hist.data.size() - 1).x
						+ hist.selectivity, maxval);
				}
			}
		}

		return maxval;
	}

	public int getMinValue(boolean viewable) {

		int minval = 999999;

		if (m.audiolist != null) {
			ArrayList<Histogram> histbuf = viewable ? getViewableHistogram()
					: getHistograms();
			for (int i = 0; i < histbuf.size(); i++) {
				Histogram hist = histbuf.get(i);
				if(hist.data.size()>0){
				minval = Math.min(hist.get(0).x - hist.selectivity, minval);
				}
			}
		}
		return minval;
	}

	public int getMaxValue(int x1, int x2, boolean viewable) {
		int max = 0;
		ArrayList<Partition> pbuf = viewable?getViewablePartition():getPartitions();
		int k = 0;
		for(int j=0;j<pbuf.size();j++){
			k=0;
		while(k<pbuf.get(j).size()&&pbuf.get(j).get(k).getNoteOff()<x1){k++;}
		while(k<pbuf.get(j).size()&&pbuf.get(j).get(k).start<x2){
			max = Math.max(max, pbuf.get(j).get(k).cents);
			k++;}
		
		
		}
		return max;
	}

	public int getMinValue(int x1, int x2, boolean viewable) {
		int min = 999999;
		ArrayList<Partition> pbuf = viewable?getViewablePartition():getPartitions();
		int k = 0;
		for(int j=0;j<pbuf.size();j++){
			k=0;
		while(k<pbuf.get(j).size()&&pbuf.get(j).get(k).getNoteOff()<x1){k++;}
		while(k<pbuf.get(j).size()&&pbuf.get(j).get(k).start<x2){
			min = Math.min(min, pbuf.get(j).get(k).cents);
			k++;
			int a=1;}
		
		
		}
		return min;
	}

	public ArrayList<Scale> getScales() {

		ArrayList<Scale> res = new ArrayList<Scale>();
		for (int i = 0; i < m.getSize(); i++) {
			res.add(m.get(i).scale);
		}
		return res;
	}

	public ArrayList<Pitchs> getPitchs() {

		ArrayList<Pitchs> res = new ArrayList<Pitchs>();
		for (int i = 0; i < m.getSize(); i++) {
			res.add(m.get(i).pitch);
		}
		return res;
	}

	public ArrayList<Histogram> getHistograms() {

		ArrayList<Histogram> res = new ArrayList<Histogram>();
		for (int i = 0; i < m.getSize(); i++) {
			res.add(m.get(i).histogram);
		}
		return res;
	}

	public ArrayList<Partition> getPartitions() {

		ArrayList<Partition> res = new ArrayList<Partition>();
		for (int i = 0; i < m.getSize(); i++) {
			res.add(m.get(i).getPartition());
		}
		return res;
	}

	public void setViewable(int i, boolean sel) {
		m.setViewable(i, sel);
		w.partitionpanel.part.repaint();
		w.scalepanel.scalespart.repaint();
		
	}

	public ArrayList<Histogram> getViewableHistogram() {
		ArrayList<Histogram> res = new ArrayList<Histogram>();
		for (int i = 0; i < m.getSize(); i++) {
			if (m.get(i).isSelectedForView && m.get(i).histogram != null)
				res.add(m.get(i).histogram);
		}
		return res;
	}

	public ArrayList<Pitchs> getViewablePitchs() {
		ArrayList<Pitchs> res = new ArrayList<Pitchs>();
		for (int i = 0; i < m.getSize(); i++) {
			if (m.get(i).isSelectedForView && m.get(i).pitch != null)
				res.add(m.get(i).pitch);
		}
		return res;
	}

	public ArrayList<Partition> getViewablePartition() {
		ArrayList<Partition> res = new ArrayList<Partition>();
		for (int i = 0; i < m.getSize(); i++) {
			if (m.get(i).isSelectedForView && m.get(i).partition != null) {
				if (m.get(i).isReducedToScale)
					res.add(new Partition(m.get(i).getPartReducedToScale()));
				else
					res.add(m.get(i).partition);
			}
		}
		return res;
	}

	public Color[] getViewableColors() {
		Color[] res = new Color[getViewablePitchs().size()];
		int k = 0;
		for (int i = 0; i < m.getSize(); i++) {
			if (m.get(i).isSelectedForView) {
				res[k] = m.get(i).color;
				k++;
			}

		}

		return res;

	}

	public void deleteAudio(Audio a) {
		//savectl.deleteAudio(a);
		m.deleteAudio(a);
	}

	public Audio getMouseSelectedAudio() {
		ListSelectionModel lsm = w.samplelist.getSelectionModel();
		if (lsm.getMinSelectionIndex() < m.getSize()
				&& lsm.getMinSelectionIndex() >= 0) {
			return m.get(lsm.getMinSelectionIndex());

		}
		return null;
	}

	// MediaPlayer

	public MediaPlayer getMediaPlayer() {
		return m.mp;
	}

	public void stopMediaPlayer() {
		m.mp.stop();
	}

	//

	public Audio getCurrentselection() {
		// System.out.println(w.samplelist.getSelectedIndex());
		if (w.samplelist.getSelectedIndex() >= 0
				&& w.samplelist.getSelectedIndex() < m.getSize())
			return m.get(w.samplelist.getSelectedIndex());
		else
			return null;
	}
	
	public void setCurrentselection(Audio a){
		w.samplelist.setSelectedIndex(m.audiolist.indexOf(a));
	}

	public int getCurrentselectionIdx() {
		// System.out.println(w.samplelist.getSelectedIndex());
		if (w.samplelist.getSelectedIndex() >= 0
				&& w.samplelist.getSelectedIndex() < m.getSize())
			return w.samplelist.getSelectedIndex();
		else
			return -1;
	}

	// progressbar & schedulding Pitchextraction
	public class ProgressCtl implements PropertyChangeListener {

		public ArrayList<File> list;
		public int curidx = 0;
		public int curchnl = 0;

		public ProgressCtl() {
			// TODO Auto-generated constructor stub
			// list = new ArrayList<File>();

		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("progress".equals(evt.getPropertyName())) {
				if(!w.progbar.isVisible()){w.progbar.setVisible(true);}
				w.progbar.setValue((Integer) evt.getNewValue());
				if((Integer) evt.getNewValue()==100){
					w.progbar.setVisible(false);
				}
			}
		}

	}

	public class GraphCtl implements ListDataListener {
		public int xmax;
		public int xmin;
		public int ymax;
		public int ymin;

		public int curxmax;
		public int curxmin;

		public int curymax;
		public int curymin;

		GraphCtl() {
			m.addListDataListener(this);
		}

		public void repaint() {
			w.graphw.mouseMoved = false;
			w.graphw.graphplot.setIgnoreRepaint(false);
			w.graphw.repaint();
			w.histw.repaint();
		}

		@Override
		public void contentsChanged(ListDataEvent arg0) {

			repaint();

		}

		@Override
		public void intervalAdded(ListDataEvent arg0) {
			xmax = getmaxtime(false);
			ymax = getMaxValue(false);
			ymin = getMinValue(false);
			curxmin = 0;
			curxmax = xmax;

			if (ymin == ymax) {
				curymin = ymin - 10;
				curymax = ymin + 10;
			} else {
				curymin = ymin - 100;
				curymax = ymax + 100;
			}

			repaint();

		}

		@Override
		public void intervalRemoved(ListDataEvent arg0) {
			// repaint();
			// System.out.println("remov");
		}
	}

	public class ViewCtl implements ActionListener {
		public boolean rescaled;
		public boolean normal;

		public boolean notePitch;
		
		//follow cursor
		public boolean isFollowing=true;

		public ViewCtl() {
			rescaled = false;
			normal = true;

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand().contains("follow")){
				isFollowing = !isFollowing;
			}
			else if (e.getActionCommand().contains("notepitch"))
				notePitch = !notePitch;
			else {
				rescaled = false;
				normal = false;
				if (e.getActionCommand().contains("normal"))
					normal = true;
				if (e.getActionCommand().contains("rescaled"))
					rescaled = true;
				w.histw.repaint();
			}
			w.graphw.repaint();

		}

	}

	public class SettingsPanelCtl implements ItemListener, ChangeListener,
	ActionListener {

		public boolean stereo;
		public int mincents;
		public int maxcents;
		public int minlength;
		public int notewidth;
		public double corthresh = 0.4;
		public int volumethresh = -40;

		public SettingsPanelCtl() {
			// dumb initiation, see Controllers.initvalues
			/*
			 * stereo = false; mincents = 2400; maxcents = 8400; minlength = 30;
			 * notewidth = 50; precision = 2;
			 */
		}

		@Override
		public void itemStateChanged(ItemEvent e) {

			if (e.getSource().getClass() == JCheckBox.class) {
				if (((JCheckBox) e.getSource()).getActionCommand().contains(
						"stereo"))
					stereo = (e.getStateChange() == 1);
				// System.out.println(stereo);
			}

		}

		@Override
		public void stateChanged(ChangeEvent arg0) {
			// TODO Auto-generated method stub

			if (arg0.getSource().getClass() == JSlider.class) {

				JSlider s = ((JSlider) arg0.getSource());
				if(s.getValueIsAdjusting()){
				if (s.getName().contains("maxPitch")) {
					maxcents = 100 * (s.getValue() + 1);
					w.settingspane.maxPName.setText("Maximum Pitch : "
							+ Utils.ctsToString(100 * s.getValue()));

				}

				else if (s.getName().contains("minPitch")) {
					mincents = 100 * (s.getValue() - 1);
					w.settingspane.minPName.setText("Minimum Pitch : "
							+ Utils.ctsToString(100 * s.getValue()));
				}


				else if (s.getName().contains("corthresh")&&getCurrentselection()!=null) {
					corthresh =  s.getValue()/1000.0;
					w.settingspane.precisionName.setText("Pitch Sensitivity : "
							+ (s.getValue()));
					
					getCurrentselection().updateCorrVolThresh(corthresh, volumethresh);		
					w.graphw.repaint();
					w.histw.repaint();
					

				}
				
				else if (s.getName().contains("volthresh")&&getCurrentselection()!=null) {
					volumethresh =  s.getValue();
					w.settingspane.volName.setText("Volume threshold : "
							+ (s.getValue()));
					
					
					
					getCurrentselection().updateCorrVolThresh(corthresh, volumethresh);					
					w.graphw.repaint();
					w.histw.repaint();
					

				}
			}
			}

		}

		public void recompute() {
			ArrayList<Audio> filelist = new ArrayList<Audio>();
			int kmax = getViewableAudioList().size();
			for (int k = 0; k < kmax; k++) {
				Audio audiobuf = getViewableAudioList().get(0);

				Audio audiobuf2 = new Audio(new File(audiobuf.path));
				if (audiobuf.name.contains(".L"))
					audiobuf2.channel = 1;
				else if (audiobuf.name.contains(".R"))
					audiobuf2.channel = 2;
				audiobuf2.name = audiobuf.name;
				audiobuf2.notewidth = audiobuf.notewidth;
				audiobuf2.minnotelength = audiobuf.minnotelength;
				
				filelist.add(audiobuf2);
				m.deleteAudio(audiobuf);
			}
			processAudio(filelist);

		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			recompute();
		}

	}

	public class PartitionCtl {

		public void updateTable(){
			m.partTable.fireTableDataChanged();
			}
		public void updatew() {

			if (getMinValue(true) != graphctl.curymin)
				graphctl.curymin = getMinValue(true);
			if (getMaxValue(true) != graphctl.curymax)
				graphctl.curymax = getMaxValue(true);

			w.partitionpanel.part.repaint();

			w.graphw.repaint();
			w.histw.repaint();

		}

	}

	public class ScaleCtl {

		public int getEditingRow() {
			return w.scalepanel.scalelistw.getSelectedRow();

		}
		
		public void updateTable(){
		m.scaletable.fireTableDataChanged();
		}

		public void updatew() {
			int newmin = getMinValue(true);
			int newmax = getMaxValue(true);
			if (newmin != graphctl.curymin){
				if(newmin<graphctl.ymin)graphctl.ymin = newmin;
				graphctl.curymin = getMinValue(true);

			}
			if (getMaxValue(true) != graphctl.curymax){
				if(newmax<graphctl.ymax)graphctl.ymax = newmax;
				graphctl.curymax = getMaxValue(true);


			}
			w.scalepanel.scalespart.repaint();

			w.graphw.repaint();
			w.histw.repaint();

		}

	}

	public Color[] getColors() {
		Color[] res = new Color[m.getSize()];
		for (int k = 0; k < m.getSize(); k++) {
			res[k] = m.get(k).color;
		}
		return res;
	}

	public class MediaCtl implements KeyListener {
		// Update time
		public Timer timer;
		UpdateTime updateTime;

		public MediaCtl() {
			updateTime = new UpdateTime();
			timer = new Timer(75, updateTime);
		}

		public void resetGraph() {
			w.graphw.setcurx(0);

		}

		public class UpdateTime implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (m.mp.Isplaying) {
					int tbuf = (int) (m.mp.getCurrentTime() * 1000.0);
					w.graphw.setcurx(tbuf);
					w.partitionpanel.part.setcurx(tbuf);
					
				}
				

				// System.out.println("eee"+mediaplayer.getCurrentTime());
			}

		}

		
		
		@Override
		public void keyPressed(KeyEvent e) {
			// TODO Auto-generated method stub
			// c.print(e.getKeyCode());

			switch (e.getKeyCode()) {

			case KeyEvent.VK_SPACE: // 32
				Time t = m.mp.playpause(w.graphw.getcurx());

				
				break;

			case KeyEvent.VK_BACK_SPACE: // 8

				deleteAudio(getCurrentselection());
				w.samplelist.repaint();
				break;
			default:
				break;

			}

		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub

		}

	}

	public class ExportCtl implements ActionListener {
		final JFileChooser fc;
		boolean vibrato =true;

		ExportCtl() {
			fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setDialogType(JFileChooser.SAVE_DIALOG);
			fc.setApproveButtonText("Choose folder");

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
			if (e.getActionCommand().contains("export")){
			int returnval = fc.showSaveDialog(null);
			if (returnval == JFileChooser.APPROVE_OPTION) {
				File folder = fc.getSelectedFile().getParentFile();

				for (int k = 0; k < getAudioList().size(); k++) {
					if(getAudioList().get(k).isSelectedForPartition){
						MidiUtils.createMidiPart(getAudioList().get(k),
								folder.getPath() + "/"
										+ getAudioList().get(k).name
										+ ".mid", vibrato);

					}}

			}
		}
			else if(e.getActionCommand().contains("vibrato")){
				vibrato = !vibrato;
			}

	}
		}


	public class PatternCtl implements ChangeListener{



		public AbstractAction fittranspose;
		public AbstractAction searchPattern;
		public AbstractAction addPattern;
		public AbstractAction settings;

		public int disttype=0;
		// Params

		//fit transpose
		final int widthsrch = 1500;

		// search patterns params
		int maxw = 400;
		int minpatternlength = 4000;
		int maxpatternlength = 10000;
		
		final int minPatternSize = 3;		
		final int slvTimePrecision = 150;
		final int mstTimePrecision = 250;
		final int minPercentagePlayed = 80;
		public double melodicThresh = 0.25;
		public double timeThresh = 0.25;
		public boolean isProcessing = false;
		double timeStretchMin = 0.85;
		double timeStretchMax = 1.15;
		
		
		// User Pattern
		private PatternList userpattl;

// ui
		
		public double xscale = 50/5000.0;


		@SuppressWarnings("serial")
		public PatternCtl() {
			userpattl = new PatternList();
		

			fittranspose = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {if(!isEmpty()){fitTranspose();
				w.scalepanel.scalelistw.repaint();
				}}
			};
			
			addPattern = new AbstractAction() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Audio curAudio = getCurrentselection();
					if(curAudio!=null){
					Pattern curPattern = new Pattern(curAudio, graphctl.curxmin, graphctl.curxmax-graphctl.curxmin, 1.0);
					curPattern.references.add(new Reference(curAudio, graphctl.curxmin, graphctl.curxmax-graphctl.curxmin, 0.0,1.0,0));
					
					curPattern.number = userpattl.size();
					
					
					if( userpattl.size()==0){
						for (Audio cura : m.audiolist) {
							cura.pattlist.clear();
						}
					}
					userpattl.add(curPattern);
					
					curAudio.pattlist.plist.add(curPattern);
					
					m.pattTable.fireTableDataChanged();
					int maxtime = m.pattTable.getMaxTime();
					
					w.patternpanel.pattTable.getColumnModel().getColumn(1).setMaxWidth((int) (maxtime*xscale)+2);
					w.patternpanel.pattTable.getColumnModel().getColumn(1).setMinWidth((int) (maxtime*xscale));
					
					w.patternpanel.pattTable.revalidate();
					
					
				}
				}
				};
			

			searchPattern = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {if(!isEmpty()){
					ArrayList<Audio> audiol = getViewableAudioList();
					
					// AUto search patterns
					if (userpattl.size()==0){
					Audio mstaudio = getCurrentselection();

					// new ArrayList<Audio>();

					//for(int k = 0 ; k<getViewableAudioList().size();k++){
						//if( getAudioList().get(k)!= getCurrentselection()){
						//	audiol.add(getViewableAudioList().get(k));
						//}
					//}
					
					Autosearchpattern searchthread =new Autosearchpattern(mstaudio, audiol);
					searchthread.addPropertyChangeListener(progctl);
					searchthread.execute();

					}
					
					// Search selected patterns
					else{
						SearchPatterns searchthread =new SearchPatterns(userpattl, audiol);
						searchthread.addPropertyChangeListener(progctl);
						searchthread.execute();
					}



				}}
			};


		}

public double computeDist(Pattern a,Pattern p,int maxw,double rythmet,double melodicthresh){
		
		
			
			Partition l = p.getSourcePattern();			
			Partition sourcepattern = a.getSourcePattern();
			int lidx=0;
			
			int intime = 0;
			int restime = 0;
			

			
			
		
			double res = 1.0;
			boolean init = true;
		
if(Math.abs(p.length-a.length)<1000&&Math.abs(p.getTimePlayed()-a.getTimePlayed())<1000){
	
	switch(disttype){
	
	//" direct distance "
	case 0:
	
	
	//two partitions are sourcepattern and l
	//respective index sidx and lidx
	
			
	// iterate sidx over sourcepattern
			for(int sidx = 0 ; sidx< sourcepattern.size();sidx++){
	
	// searching common silence intervals
				
				// for a given note from source pattern, iterate lidx over l note's that
				//are before the beginning of current sourcepattern note
				
				while (lidx<l.size()&&sourcepattern.get(sidx).start>l.get(lidx).start){
					
					// take 0 as beginning of considered interval if sidx or lidx == 0 
					// else take last note off
					int smin = sidx==0? 0 : sourcepattern.get(sidx-1).getNoteOff();
					int lmin = lidx==0? 0 : l.get(lidx).getNoteOff();
					int st = Math.max(smin, lmin);
					
					// end of interval is the minimum start of current notes
					int end = Math.min(sourcepattern.get(sidx).start, l.get(lidx).start);
					
					
					//////////
					// if the interval is positive, it's a common silence interval
					//////////
					if (end>st){	
					intime+=end-st;
					}
					
					
					
					// exits if current l note crosses current sourcepattern note 
					if(l.get(lidx).getNoteOff()>sourcepattern.get(sidx).start){
					break;
					}	
					
					lidx++;		
					}
							
		// searching common played intervals
				
				//iterate over l note's that are before current sourcepattern note off
				while(lidx<l.size()&&sourcepattern.get(sidx).getNoteOff()>l.get(lidx).start){
					
					// definition of the current interval
					int st = Math.max(sourcepattern.get(sidx).start,l.get(lidx).start);
					int end = Math.min(sourcepattern.get(sidx).getNoteOff(),l.get(lidx).getNoteOff());
					
					// if current interval is positive, this is a common interval of played notes
					if(end>st){
						
						// initialisation of melodic distance result that is maximum by default (1.0) 
						if(init){
							res = 0.0;
							init = false;
						}
						
						
							
							///////////////
							// if melodic distance is acceptable add distance to a 
							//normalized mean of distance over common played time
							// if the melodic distance too big, it will be considered as a rythm fault
							//by not incrementing intime, the rythm indicator
							///////////////
						int dist = Math.abs(sourcepattern.get(sidx).cents-l.get(lidx).cents);
						if(dist<maxw){
							dist = dist< 40? 0:dist;
							int time = end-st;
							res+=time*dist*1.0/maxw;
							restime+= time;
							
							intime+= end-st;
						}
						}
						
					//exits if l note crosses sourcepattern note off
						if(l.get(lidx).getNoteOff()>sourcepattern.get(sidx).getNoteOff()){
							break;
						}
						lidx++;
					
				}
				
				
			}
				
			
				
			// melodic distance
				double space;
				if(restime>0)space= res/restime;
				else space = 1.0;
				
				// rythmic distance
				int maxlength = (a.getTimePlayed()+p.getTimePlayed())/2;
				double timedif =Math.max(0.0,(1.0-intime*1.0/maxlength));// (1.0-totdur/(1.0*maxTimeplayed));
				
				// global distance is the sum of both distance normalized by their threshold
				// values of res above 1.0 will be considered as bad 
				// each distance is contributing to the global one, depending on their threshold
				res = (timedif/rythmet+space/melodicthresh);
		


	
	break;
	// IOI distance
	case 1:
		int sidx=0;
		res=0.0;
		int sst=sourcepattern.get(0).start;
		
		int lst = l.get(0).start;
		while(sidx+1<sourcepattern.size()&&lidx+1<l.size()){
			int dist = Math.abs(sourcepattern.get(sidx).cents- l.get(lidx).cents);
			int last = Math.max(sst, lst);
			sst = sidx<sourcepattern.size() ? sourcepattern.get(sidx+1).start:sourcepattern.getmaxtime();
			lst = lidx<l.size() ? l.get(lidx+1).start:l.getmaxtime();
			
			if(sst<lst){
				int time = sst-last;
				res+= dist*time*1.0/maxw;
				restime+=time;
				sidx++;
			}
			else{
				int time = lst-last;
				res+= dist*time*1.0/maxw;
				restime+=time;
				lidx++;
			}
		}
		
		res=res/(restime*melodicthresh);
		
		break;
		
	default:
		System.out.println("wrong disttype");
			break;
}
	
}
			return res;
			
			
		}

public void addmerge(PatternList plist,Pattern p,double thresh, int maxw,double mintimes,double maxtimes,double timethresh){
	boolean referenced = false;
	for(int k = 0 ; k<plist.size();k++){
		double dist=99999;
		double times=1;
		int transp=0;
	
		for (int l = (int) (mintimes*20);l<maxtimes*20;l++){
			Pattern pbuf = new Pattern(p.getAudio(), p.getSourceTime(), p.getlengthinit(), l/20.0);
			int transp1 = plist.get(k).computeTransp(pbuf);
			pbuf.getSourcePattern().transpose(transp1);
			double dist1 = computeDist(plist.get(k),pbuf,maxw,timethresh*1.5,thresh*1.5);
		
		if (dist1<dist){
			dist = dist1;
			times = l/20.0;
			transp=transp1;
		}
		}
		if(dist<1.0&&p!=plist.get(k)){
			// check if its a better pattern
			
			PatternList tpl = new PatternList();
			tpl.add(plist.get(k));
			tpl.add(p);
			int c = tpl.getBestPattern();
			System.out.println("case :"+c);
			switch (c) {
			case 0:
				//plist.remove(p);
				plist.get(k).references.add(new Reference(p.getAudio(), p.references.get(0).time,p.getlengthinit(), dist,times,transp));
				referenced = true;
				break;
			case 1:
				//
				plist.remove(k);
				referenced = false;
				break;
			default:
				
				break;
			}

			//plist.get(k).addReferences(p);
			
			break;
			
		}
		
	}
	if(!referenced){
		
		
				p.number = plist.size();
				//for (int l =1 ; l<p.references.size();l++){
					//p.references.get(l).distance = p.references.get(l).distance/thresh;
				//}
				plist.add(p);
	}
}


		

		private void updatePatterns(PatternList pl){
			

			for(int k=0;k<m.audiolist.size();k++){
				//if(k!=getCurrentselectionIdx()){
				Audio curaudio = m.get(k);
				curaudio.pattlist.clear();

				for(int j = 0 ; j<pl.size();j++){
					Pattern curp = pl.get(j);

					
					for(int l = 0 ; l<curp.references.size();l++){
						Reference curref = curp.references.get(l);
						if(curref.a==curaudio){
							Pattern curaudiop = new Pattern(curp.getAudio(), curp.getSourceTime(), curp.getlength(),curp.getTimestretch());
							if(l==0)
							{curaudiop.references.add(new Reference(curp.getAudio(), curp.getSourceTime(), curp.getlength(),0,1,0));}
							else 
							{curaudiop.references.add(curref);}
						
							curaudiop.number = curp.number;
							curaudio.pattlist.add(curaudiop);
							
						}
					}
				}
				Collections.sort(curaudio.pattlist.plist);
	
			}
			m.pattTable.fireTableDataChanged();
			int max = m.pattTable.getMaxTime();
			w.patternpanel.pattTable.getColumnModel().getColumn(1).setMaxWidth((int)(max*xscale)+2);
			w.patternpanel.pattTable.getColumnModel().getColumn(1).setMinWidth((int)(max*xscale));
			w.patternpanel.pattTable.revalidate();
			
			
		}
		
		public void unSelectAll(){
			for(int k = 0 ; k< getAudioList().size();k++){
				PatternList curpl = getAudioList().get(k).pattlist; 
				for (int j =0 ; j< curpl.size();j++){
					curpl.get(j).isSelected =  false;
				}
			}
			w.patternpanel.pattTable.repaint();
		}
		
		public void deleteMasterPattern(PatternList p, int pattnum){
			
			PatternList curpattlist = p;
			int num = curpattlist.get(pattnum).number;
			if(userpattl.size()>0)userpattl.remove(num);
			
			for(Audio a : m.audiolist){
				curpattlist = a.pattlist;
				int k = 0 ;
			while ( k<curpattlist.size()) {
				Pattern patt = curpattlist.get(k);
				if(patt.number == num){
					
					curpattlist.remove(patt);
					k--;
					
				}
				else if(patt.number>num){
					patt.number--;
				}
				
				k++;
			}
			}
			m.pattTable.fireTableDataChanged();
			w.patternpanel.pattgrph.repaint();
		}

		public void deleteSlavePattern(PatternList p , int pattnum){
			//userpattl.remove(p.get(pattnum));
			p.remove(p.get(pattnum));
			
			m.pattTable.fireTableDataChanged();
			w.patternpanel.pattgrph.repaint();
			
		}
		
		public void patternClicked(PatternList curpattlist,int pattnum,  boolean doubleClick){
			Audio a=curpattlist.get(pattnum).references.get(1).a;
			int time=curpattlist.get(pattnum).references.get(1).time;
			int length=(int) (curpattlist.get(pattnum).references.get(1).length);
			double ts = curpattlist.get(pattnum).references.get(1).timestretch;
			length = (int)(length*ts);
			int num = curpattlist.get(pattnum).number;
			//System.out.println("t : "+time+ "//  l : "+length + "// ts : " + ts );
			
			
			
			for(int k = 0 ; k< getAudioList().size();k++){
				PatternList curpl = getAudioList().get(k).pattlist; 
				for (int j =0 ; j< curpl.size();j++){
					if(curpl == curpattlist&&j==pattnum){
						curpl.get(j).isClicked=true;
					}
					else{
						curpl.get(j).isClicked=false;
					}
					curpl.get(j).isSelected =  ( curpl.get(j).number==num);
				}
			}
			
			
			m.pattTable.fireTableChanged(new TableModelEvent(m.pattTable,0,m.getSize()-1));
			//w.corpuspanel.pattTable.repaint();
			
			
			w.graphw.setcurx(time);
			setCurrentselection(a);
			
			
			if(doubleClick){
			
			mediactl.timer.start();
			m.mp.playAt(time);
			m.mp.scheduleStop(time+length);
			
			}
			
		}
		private class  SearchPatterns extends SwingWorker<PatternList, int[]>{
			
			 PatternList mpl;
			 ArrayList<Audio> audiol;
			 
			 PatternList res;
			 
			 
			 public SearchPatterns(PatternList pl, ArrayList<Audio> audiolin) {
				 
				mpl	=pl;
				audiol = audiolin;
			}
			
			
			@Override
			protected PatternList doInBackground() throws Exception {
				// TODO Auto-generated method stub
		
		isProcessing = true;
		
		res = new PatternList();

		
		

		for (int j = 0 ; j<mpl.size() ; j++){
			//notify progressbar
			int progress =(int)Math.max(j*100.0/mpl.size(),1);
			
			setProgress(progress);
			Pattern pbuf = mpl.get(j);
			
			
			pbuf.references.subList(1, pbuf.references.size()).clear();
			
					

						for(int k = 0 ; k<audiol.size();k++){
							searchpattern(pbuf, audiol.get(k));
							
							
						}
						res.add(pbuf);
		}
		
		return res;
	}

				
				
				
			@Override
				protected void done() {
				setProgress(100);
				try {
					updatePatterns(get());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				isProcessing = false;
				}	
			
		}

		
		private class  Autosearchpattern extends SwingWorker<PatternList, int[]>{
			
			 Audio mstaudio;
			 ArrayList<Audio> audiol;
			 
			 PatternList res;
			 
			 
			 public Autosearchpattern(Audio mstaudioin, ArrayList<Audio> audiolin) {
				 
				mstaudio=mstaudioin;
				audiol = audiolin;
			}
			
			
			@Override
			protected PatternList doInBackground() throws Exception {
				// TODO Auto-generated method stub
		
		isProcessing = true;
		
		res = new PatternList();

		Partition mstprt = mstaudio.getPartition();
		//int tstop=0;
		int lstop=0;

		// l = master pattern  start
		int t = 0 ;
		while(t < mstprt.getmaxtime()){
			//notify progressbar
			int progress =(int)Math.max((100* (1.0*t)/(mstprt.getmaxtime())), 1);
			
			setProgress(progress);
					
			// buffer comparing patterns of different length for the same startpoint 
			PatternList tmpres = new PatternList();
			tmpres.clear();

			// l = differents lengths
			int lastrefnum  =1;
			double lastmeandist = 1.0;
			for(int l = Math.min(maxpatternlength,mstprt.getmaxtime()-t) ; l> minpatternlength ; l -=mstTimePrecision){

				
					// create new pattern instance
					Pattern pbuf = new Pattern( mstaudio, t,l,1);

					if(pbuf.getTimePlayed()*100.0/pbuf.getlength() >minPercentagePlayed&&pbuf.getSourcePattern().size()>=minPatternSize){

						for(int k = 0 ; k<audiol.size();k++){
							searchpattern(pbuf, audiol.get(k));
							
							
						}
						// if  references created, add to the list and exit the for loop (testing different length)

						if(pbuf.references.size()>lastrefnum||(lastrefnum>1&&pbuf.references.size()==lastrefnum&&pbuf.getMeandist()<lastmeandist)){
							tmpres.add(pbuf);
							lstop = l;
							lastrefnum = pbuf.references.size();
							lastmeandist = pbuf.getMeandist();
						
						}


					
				}


			}
			if(tmpres.size()>0){
			Pattern pbuf = tmpres.get(tmpres.size()-1);
			tmpres.clear();
			tmpres.add(pbuf);
			
			// searching if this pattern is only a piece of anotherone best fitted
			
			//
			
			  
			
			 
			if(t+lstop+lstop/2<mstprt.getmaxtime()){
				// create new pattern instance
				Pattern pbuf1 = new Pattern(mstaudio, t+mstTimePrecision, lstop,1);

				if(pbuf1.getTimePlayed() >minPercentagePlayed&&pbuf1.getSourcePattern().size()>minPatternSize){

					for(int k = 0 ; k<audiol.size();k++){
						searchpattern(pbuf1, audiol.get(k));
					}
					
					
						
					
				}
				tmpres.add(pbuf1);
			
			
				// create new pattern instance
				Pattern pbuf2 = new Pattern(mstaudio, t+lstop/2, lstop,1);
/*
				if(pbuf2.getTimePlayed() >minPercentagePlayed&&pbuf2.getSourcePattern().size()>minPatternSize){

					for(int k = 0 ; k<audiol.size();k++){
						searchpattern(pbuf2, audiol.get(k));
					}
					
						
					
				}*/
				tmpres.add(pbuf2);
			}
			
			int bestidx =tmpres.getBestPattern();
			if(tmpres.size()>1){
			switch (bestidx) {
			case 0:
				addmerge(res,tmpres.get(bestidx),melodicThresh,maxw,timeStretchMin,timeStretchMax,timeThresh);
				t+=lstop;
				break;

			case 1:
				
				t+=mstTimePrecision;
				break;
		/*	case 2:
				
				t+=lstop/2;
				break;*/
			default:
				t+=mstTimePrecision;
				break;
			}
			
			


		
		}
			else{
				if (tmpres.size()==1){
					addmerge(res,tmpres.get(0),melodicThresh,maxw,timeStretchMin,timeStretchMax,timeThresh);
					t+=lstop;
				}
				
				else {
					t+=mstTimePrecision;;
					
					//System.out.println("tmpres.size= "+tmpres.size());
				
					
				}
				
			}
		}
			else t+=mstTimePrecision;
			}
			

		
		return res;
	}

				
				
				
			@Override
				protected void done() {
				setProgress(100);
				try {
					updatePatterns(get());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				isProcessing = false;
				}	
			
		}



			


		// search one given pattern inside one audio instance, adds references to the given pattern
		private void searchpattern(Pattern patt,Audio slvaudio){

			int tidx = 0 ;
			
			//avoid compare to itself
			int tvoidmin=0;
			int tvoidmax=0;
			if(patt.getAudio()==slvaudio){
				tvoidmin = patt.getSourceTime();
				tvoidmax = patt.getlengthinit()+patt.getSourceTime();
			}
			
			
			while(tidx<slvaudio.partition.getmaxtime()-patt.getlength()){
				if(tidx>40000){
					int allah=0;
					allah++;
				}
				if(tidx>tvoidmin-patt.getlength()/2&&tidx<tvoidmax){
					tidx=tvoidmax;
				}
				else{
					double dist=999999;
					double times = 1;
					int transp = 0;
					// if note number is enough
					if(new Pattern(slvaudio, tidx, patt.getlengthinit(),1).getSourcePattern().size()>=minPatternSize){
					// search best time stretching for this tidx
			for(int l = (int) (timeStretchMin*20) ; l<timeStretchMax*20;l++){
				// cut a chunk in the slave (same as patt but time stretched by l/20)
				Pattern slvp = new Pattern(slvaudio, tidx, patt.getlengthinit(),l/20.0);
				
				// Compute distance
				int ctransp = patt.computeTransp(slvp);
				slvp.getSourcePattern().transpose(ctransp);
				double cdist = computeDist(patt,slvp,maxw,timeThresh,melodicThresh);
				if (cdist<dist){
					dist = cdist;
					times = l/20.0;
					transp = ctransp;
					
				}
				}
		}

				// add references if necessary
				if(dist<1.0){
					
					double dist2 = 99999;
					//double times2=1;
					/*for(int l = (int) (timeStretchMin*20) ; l<timeStretchMax*20;l++){
						// cut a chunk in the slave (same as patt but time stretched by l/20)
						Pattern slvp2 = new Pattern(slvaudio, tidx+patt.getlength()/2, patt.getlength(),l/20.0);
						// Compute distance
						int ctransp = patt.computeTransp(slvp2);
						slvp2.getSourcePattern().transpose(ctransp);
						double cdist2 = patt.computeDist(slvp2,maxw,timeThresh);
						if (cdist2<dist2){
							dist2 = cdist2;
							
							
						}
						}
						*/
						double dist3 = 99999;
						
					
							
							// cut a chunk in the slave (same as patt but time stretched by current time stretch)
							Pattern slvp3 = new Pattern(slvaudio, tidx+slvTimePrecision, patt.getlengthinit(),times);
							// Compute distance
							int ctransp = patt.computeTransp(slvp3);
							slvp3.getSourcePattern().transpose(ctransp);
							double cdist3 = computeDist(patt,slvp3,maxw,timeThresh,melodicThresh);
							if (cdist3<dist3){
								dist3 = cdist3;
							}
						
				
					
					if((dist<=dist2&&dist<=dist3)){//||(tidx+patt.getlength()/2+slvTimePrecision>slvaudio.partition.getmaxtime()-patt.getlength())){
					patt.addReferences(new Reference(slvaudio, tidx,patt.getlengthinit(), dist,times,transp));
					tidx+=(int)(patt.getlengthinit());
					
					}
					/*else if(dist2<dist&&dist2<dist3){
						//patt.addReferences(new Reference(slvaudio, tidx+patt.getlength()/2,patt.getlength(), dist2));
						tidx+=(int)(patt.getlengthinit()/2);
						
					}*/
					else{
						tidx+=slvTimePrecision;
					}
				
				}
				else{
					tidx+=slvTimePrecision;
				}
				

			}
				
			}
			
		

		}


		private void fitTranspose(){
			
				ArrayList<Integer> mstscl = getCurrentselection().scale.scale;

				int selectivity = getCurrentselection().histogram.selectivity;


				for(int k = 0 ; k<getViewableAudioList().size();k++){
					if(k!=getCurrentselectionIdx()){	
						getViewableAudioList().get(k).updateTransposition(0);
						ArrayList<Integer> curscl = getViewableAudioList().get(k).scale.scale;
						Histogram curhist = getViewableHistogram().get(k);
						int trbuf = 0;
						double scorebuf = 0;

						for(int tr = -widthsrch/2 ; tr<widthsrch/2;tr ++){
							double score=0;
							int n=0;
							for(int j=0;j<curscl.size();j++){
								int dist = Utils.getMinDist(curscl.get(j)+tr, mstscl);
								if(dist <selectivity){
									score += curhist.getSmoothedValue(curscl.get(j)) /(1.0*curhist.smootheddatamax) *(selectivity-dist)/(1.0*(selectivity+dist));
									n++;
								}

							}
							if(n>0){
								score = score*n;
								if ( score>scorebuf){
									trbuf = tr;
									scorebuf = score;
								}
							}






						}

						getViewableAudioList().get(k).updateTransposition(trbuf);




					}

					scalectl.updatew();
				}

			}


		@Override
		public void stateChanged(ChangeEvent e) {
				if((e.getSource()).getClass()==JSlider.class){
				
				JSlider js = (JSlider) e.getSource();
				if(!js.getValueIsAdjusting()){
				if (js.getName().contains("melodicThreshold")){
					melodicThresh = js.getValue()/100.0;
				}
				else if (js.getName().contains("timeThreshold")){
					timeThresh = js.getValue()/100.0;
				}
				else if(js.getName().contains("minlength")){
					minpatternlength = 1000*js.getValue();
					System.out.println(minpatternlength+"/"+maxpatternlength);
					if (maxpatternlength<=minpatternlength){
						maxpatternlength=minpatternlength+1000;
						w.patternpanel.settingsw.maximumlength.setValue(maxpatternlength/1000);
					}
				}
				else if(js.getName().contains("maxlength")){
					System.out.println(minpatternlength+"/"+maxpatternlength);
					maxpatternlength = 1000*js.getValue();
					if (maxpatternlength<=minpatternlength){
						minpatternlength=maxpatternlength-1000;
						w.patternpanel.settingsw.minimumlength.setValue(minpatternlength/1000);
					}
				}
				
				
				else if(js.getName().contains("xscale")){
					xscale = 0.01*(js.getValue()+1)/100.0;
					m.pattTable.fireTableDataChanged();
					
					
					
					//w.corpuspanel.pattTable.setBounds(0, 0, , w.corpuspanel.pattTable.getHeight());
					w.patternpanel.pattTable.getColumnModel().getColumn(1).setMaxWidth((int)(m.pattTable.getMaxTime()*xscale+2));
					w.patternpanel.pattTable.getColumnModel().getColumn(1).setMinWidth((int)(m.pattTable.getMaxTime()*xscale));
					w.patternpanel.pattTable.revalidate();
					
					
								
				}
				}
			}
				else if((e.getSource()).getClass()==JCheckBox.class){
					JCheckBox ch = (JCheckBox)e.getSource();
					if(ch.getName().contains("disttype")){
					disttype = ch.isSelected()? 1:0;
					w.patternpanel.settingsw.timeThreshold.setEnabled(!ch.isSelected());
					}
				}
		
		}
	}

public class PatternGraphCtl implements TableModelListener {
	public ArrayList<Partition> curparts;
	public ArrayList<Double> curdistlist;
	public ArrayList<Color> curcollist;
	
	
	public int xmax;
	public int xmin;
	public int ymax;
	public int ymin;

	public int curxmax;
	public int curxmin;

	public int curymax;
	public int curymin;
	
	
	
	public PatternGraphCtl() {
	 curparts = new ArrayList<Partition>();
	 
	 curdistlist = new ArrayList<Double>();
	 curcollist = new ArrayList<Color>();
	 m.pattTable.addTableModelListener(this);
	}
	
	public void repaint(){
		w.patternpanel.pattgrph.repaint();
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		// TODO Auto-generated method stub
		curparts.clear();
		curdistlist.clear();
		curcollist.clear();
		xmax = 1;
		xmin = 0;
		ymax = 1;
		ymin = 0;
		
		for (int k = 0;k<getAudioList().size();k++){
			PatternList curpattl = getAudioList().get(k).pattlist;
			for (int j = 0 ; j< curpattl.size();j++){
				if(curpattl.get(j).isSelected){
					Partition curpart = curpattl.get(j).getPattern();
					Color curcol = getAudioList().get(k).color;
					if(curpattl.get(j).isClicked)
					{curcollist.add( Color.red);}
					else{curcollist.add(new Color(curcol.getRed(),curcol.getGreen(),curcol.getBlue(),(int)(10+55.0*(1-curpattl.get(j).references.get(1).distance))));}
					curdistlist.add( curpattl.get(j).references.get(1).distance);
					curparts.add(curpart);
					xmax = Math.max(curpart.getmaxtime(), xmax);
					ymax = Math.max(curpart.getmaxvalue(), ymax);
					if(ymin == 0){ymin=999999;}
					ymin = Math.min(curpart.getminvalue(), ymin);
					
					
				}
			}
			
		}
		
		ymin -= 100;
		ymax+=100;
		
		
		curxmax = xmax;
		curxmin = xmin;
		curymax = ymax;
		curymin = ymin;
		
		repaint();
	}
	
	
}
public class SaveCtl implements ActionListener{
	final private String folderName = "Baqsy_files";
	
	public SaveCtl() {
		// TODO Auto-generated constructor stub
	}
	
	public void deleteAudio(Audio a){
		File f = new File(a.path);
		String name = a.name;
		
		String destfolder = f.getParent()+"/"+folderName;
		File folderout = new File(destfolder);
		if(folderout.exists()){
			String destpath = folderout.getAbsolutePath()+"/"+name+".bqsy";
			File fileout = new File(destpath);
			if(fileout.exists()){
				fileout.delete();
			}
		}
	}
	
	public void saveAudio(Audio a){
		File f = new File(a.path);
		String name = a.name;
		
		String destfolder = f.getParent()+"/"+folderName;
		File folderout = new File(destfolder);
		if(!folderout.exists()){
			folderout.mkdir();
		}
		
		String destpath = folderout.getAbsolutePath()+"/"+name+".bqsy";
		File fileout = new File(destpath);
	
		
		try {
			// Write to disk with FileOutputStream
			FileOutputStream f_out = new 
				FileOutputStream(fileout);

			// Write object with ObjectOutputStream
			ObjectOutputStream obj_out = new
				ObjectOutputStream (f_out);

			// Write object out to disk
			obj_out.writeObject ( a );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		}
		
	}
	
	public Audio RestoreAudio(String path){
		Audio res=new Audio();
		try{
			// Read from disk using FileInputStream
		
		FileInputStream f_in = new 
			FileInputStream(path);

		// Read object using ObjectInputStream
		ObjectInputStream obj_in = 
			new ObjectInputStream (f_in);

		// Read an object
		Object obj = obj_in.readObject();

		if (obj instanceof Audio)
		{
			
			// Cast object to a Vector
			res = (Audio) obj;
			
			}
			// Do something with vector....
		
		}
		 catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return res;
		
	}
	
public boolean checkandImport(Audio a){
	boolean res= false;
	File f = new File(a.path);
	File fold  = new File(f.getParent()+"/"+folderName);
	if(fold.exists()){
		File[] list  =fold.listFiles();

		for(File s : list){
				if(!settingspanelctl.stereo&&s.getName().contentEquals(a.name+".bqsy")){
					Audio atmp = RestoreAudio(s.getAbsolutePath());
					atmp.path=a.path;
					m.importaudio(atmp);
					setCurrentselection(m.get(0));
					
					res = true;
				}
				else if(settingspanelctl.stereo&s.getName().contentEquals(a.name+".L.bqsy")||s.getName().contentEquals(a.name+".R.bqsy")){
					Audio atmp = RestoreAudio(s.getAbsolutePath());	
					atmp.path=a.path;
					m.importaudio(atmp);
						setCurrentselection(m.get(0));
						
						res = true;
					}
					
				
			
		}
		
	}
	if(res){
		m.pattTable.fireTableDataChanged();
		int max = m.pattTable.getMaxTime();
		w.patternpanel.pattTable.getColumnModel().getColumn(1).setMaxWidth((int)(max*patternctl.xscale)+2);
		w.patternpanel.pattTable.getColumnModel().getColumn(1).setMinWidth((int)(max*patternctl.xscale));
		w.patternpanel.pattTable.revalidate();
	}
	return res;
	
	
}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getActionCommand().contains("Save")){
			for(Audio a : m.audiolist){
				if(a.isSelectedForView)	saveAudio(a);
			}
		}
	}
		
	
}
}



