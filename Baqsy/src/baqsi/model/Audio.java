package baqsi.model;

import java.awt.Color;

import java.io.*;
import java.util.ArrayList;

import baqsi.model.Note;
import baqsi.utils.Utils;




// only L
// traiter pitch fs differents
public class Audio  implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3492784400732838996L;
	
	
	
	public String name;
	public String path;
	public int channel;
	public int numchannel;
	public int fs;

	public int inlength = 0;

	public Pitchs pitch;

	public Partition partition;

	public Histogram histogram;

	public Scale scale;
	
	public PatternList pattlist;
	
	public ArrayList<Integer> volume;
	
	public ArrayList<Marker> markers; 

	public Color color;
	public boolean isSelectedForView;
	public boolean isSelectedForScale;
	public boolean isSelectedForPartition;
	public boolean isReducedToScale;
	

	// Meta data

	public int notewidth;
	public int minnotelength;
	public int transposition;

	public Audio() {

	}

	public Audio(File f) {
		name = f.getName().split(".wav")[0];
		path = f.getPath();
		channel = 0;
		notewidth = 110;
		minnotelength = 120;
		fs = 0;
		transposition = 0;
		volume = new ArrayList<Integer>();
		markers = new ArrayList<Marker>();
		pattlist = new PatternList();

		// récupere la fréquence d'echantillonage
		try {
			// Open the current wav file
			WavFile wavFile = WavFile.openWavFile(f);

			// Display information about the wav file
			// wavFile.display();
			fs = (int) wavFile.getSampleRate();
			numchannel = wavFile.getNumChannels();
			wavFile.close();

		} catch (Exception e) {

		}

	}

	@Override
	public Audio clone() {
		Audio a = new Audio();
		a.numchannel = numchannel;
		a.channel = channel;
		a.fs = fs;
		a.name = name;
		a.path = path;
		a.notewidth = notewidth;
		a.minnotelength = minnotelength;
		a.transposition = transposition;
		a.pattlist = new PatternList();
		a.pattlist.plist=(ArrayList<Pattern>) pattlist.plist.clone();
		a.volume = new ArrayList<Integer>();
		a.markers = new ArrayList<Marker>();
		return a;
	}

	public double[] readfile(int downsample) {

		double[] res;
		try {
			// Open the current wav file
			WavFile wavFile = WavFile.openWavFile(new File(path));

			// Display information about the wav file
			wavFile.display();
			fs = (int) wavFile.getSampleRate() / downsample;

			// Get the number of audio channels in the wav file
			int numChannels = wavFile.getNumChannels();
			long size = wavFile.getNumFrames();
			int bufsize = (int) size / downsample;
			// Create
			res = new double[bufsize + 1];
			double[] bufframe = new double[numChannels];

			int framesRead = 1;

			int i = 0;
			do {
				// Read frames into buffer

				framesRead = wavFile.readFrames(bufframe, 1);
				if (i % downsample == 0) {
					res[i / downsample] = bufframe[channel == 0 ? 0
							: channel - 1];

				}
				i++;

			} while (framesRead != 0);

			// Close the wavFile
			wavFile.close();

			// normalize (obligé??)

			Utils.normalize(res);

			inlength = res.length;
			return res;

		} catch (Exception e) {
			System.err.println(e);
			return null;
		}

	}

	@Override
	public String toString() {
		return name;
	}

	public boolean isViewable() {
		return isSelectedForView;
	}

	public void setViewable(boolean isSelected) {
		this.isSelectedForView = isSelected;
		this.isSelectedForPartition=isSelected;
		this.isSelectedForScale=isSelected;

	}

	private void transpose(int cts) {
		if (cts != 0) {
			for (int k = 0; k < pitch.viewablevalues.length; k++) {
				if (pitch.viewablevalues[k] != 0) {
					pitch.viewablevalues[k] += cts;
				}
			}
		}

		partition.compute(this);
		histogram.computeHistogram(partition);
		scale.computeScale();

	}
	public Partition getPartition(){
		Partition res;
	if(isReducedToScale)res = new Partition(getPartReducedToScale());
	else res =partition;
	return res;
	
	}
	public ArrayList<Marker> getmarkers(){
		return markers;
	}

	public ArrayList<Note> getPartReducedToScale() {
		ArrayList<Note> res = new ArrayList<Note>();
		for (int k = 0; k < partition.size(); k++) {
			Note nbuf = partition.get(k);
			
			boolean added = false;
			for (int j = 0; j < scale.size; j++) {
				if (Math.abs(nbuf.cents - scale.scale.get(j)) < 2 * histogram.selectivity) {
					
					if (j < scale.size - 1
							&& Math.abs(nbuf.cents - scale.scale.get(j)) > Math
									.abs(nbuf.cents - scale.scale.get(j + 1)))
						res.add(new Note(nbuf.start, scale.scale
								.get(j + 1), nbuf.duration));
					else
						res.add(new Note(nbuf.start, scale.scale
								.get(j), nbuf.duration));
					added=true;
					break;
				}
			}
			if(!added){
				res.add(nbuf);
			}

		}
		return res;
	}

	public void updateTransposition(int newValue) {
		// TODO Auto-generated method stub

		transpose(newValue - transposition);
		transposition = newValue;

	}
	public void pitchUpdated(){
		partition.compute(this);
		histogram.computeHistogram(partition);
		scale.computeScale();
	}

	public void updateCorrVolThresh(double cthresh,int vthresh){
		pitch.setCorThresh(cthresh);
		setVolumeThresh(vthresh);
		transpose(transposition);
		pitchUpdated();
		
		
	}
	
	public void setVolumeThresh(int thresh){
	for (int k = 0;k<Math.min(pitch.viewablevalues.length,volume.size());k++){
		
		pitch.viewablevalues[k]= volume.get(k)>thresh? pitch.viewablevalues[k]:0;
	}
	}
	
}
