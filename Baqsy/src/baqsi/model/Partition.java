package baqsi.model;
import java.io.Serializable;
import java.util.ArrayList;

// attention k2 getchunks
public class Partition implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2410418768992363961L;

	public ArrayList<Note> notelist;

	

	public Partition() {
		notelist = new ArrayList<Note>();
	}

	public Partition(ArrayList<Note> list) {
		notelist = list;
	}

	public void compute(Audio a) {
		if (notelist != null)
			notelist.clear();
		

		Pitchs pitch = a.pitch;
		int minlength = a.minnotelength;
		int centsw = a.notewidth;
		int durmin = (int) (minlength / 1000.0 * pitch.pitchfs);
		int[] pitcht = pitch.viewablevalues.clone();
		double pitchfs = pitch.pitchfs;

		int i = 0;
		float mean = 0;
		// boolean note=false;
		int length = 0;
		
		int hole=0;
		int holemax =minlength/3;
		while (i < pitcht.length) {
			if (length == 0) {
				// note = true;
				length = 1;
				mean = pitcht[i];
			}

			// moyenne retardŽe d un echantillon
			int transsample = (int) (pitchfs / (minlength / 3.0));
			int nontransweight = 4;
			if (length == 2)
				mean = pitcht[i - 1];
			else if (length >= 3) {
				if(pitcht[i-1]!=0){
				if (length - 1 > transsample) {
					
					mean = (mean
							* (transsample + nontransweight
									* (length - 2 - transsample)) + nontransweight
							* pitcht[i - 1])
							/ (transsample + nontransweight
									* (length - 1 - transsample));
				} else
					mean = (mean * (length - 2) + pitcht[i - 1]) / (length - 1);
				}
			}
			if(pitcht[i]==0)hole+=1000.0/pitchfs;
			if (((length <= transsample && Math.abs(mean - pitcht[i]) < 3 * centsw / 2) || (length > transsample && Math.abs(mean - pitcht[i]) < centsw)||(pitcht[i]==0&&hole<holemax)) && mean != 0){
			
				length++;}
			// validation de la note
			else if (mean != 0) {
				hole=0;
				if (length < durmin) {
					mean = pitcht[i];
					
					length = 0;
				}
				// assignation de la note
				else {/*
					 * double var1=0; double var2 =0; for(int k
					 * =2;k<length-1;k++){
					 * if(k<length/2)var1+=pitch[i-length+k]; else
					 * var2+=pitch[i-length+k]; } var1 = var1/(length/2-2); var2
					 * = var2/(length/2);
					 * 
					 * 
					 * if(Math.abs(pitch[i-5*length/6]-pitch[i-length/6])<=centsw
					 * )
					 */
					notelist.add(new Note((int) ((i - length) * 1000.0 / pitchfs),
							(int) mean, (int) (length * 1000.0 / pitchfs)));
					length = 0;
				}

			} else if (mean == 0)
				length = 0;

			i++;
		}

	}

	
	public boolean isPlayTime(int t) {

		boolean res = false;
		for (int i = 0; i < notelist.size(); i++) {
			if (notelist.get(i).start <= t
					&& notelist.get(i).start + notelist.get(i).duration >= t) {
				res = true;
				break;
			}
		}

		return res;
	}
	
	public Note get(int i) {
		if (notelist.size() != 0)
			return notelist.get(i);
		else
			return null;
	}
	public ArrayList<Note> get(int x1, int x2){
		ArrayList<Note> res = new ArrayList<Note>();
		if(x1>=0&&x2<notelist.size()){
			for( int k = x1 ; k<x2;k++){
				res.add(notelist.get(k));
			}
		}
		return res;
	}
	
	public Partition getChunk(int t1,int t2,double timestretch){
		ArrayList<Note> res = new ArrayList<Note>();
		t2 = Math.min(t2, notelist.get(notelist.size()-1).getNoteOff()-1);
	if(t1>=0){
		int k1 =getNoteIdxsigned(t1);
		if (k1<0){
			k1=-k1-1;
			
		}
		else if (k1 >0){
			k1 = k1-1;
			res.add(new Note(0,notelist.get(k1).cents,(int) ((notelist.get(k1).getNoteOff()-t1)*timestretch)));
			k1++;
		}
		else{
			System.out.println("k1 = 0");
		}
		int k2 = getNoteIdxsigned(t2);
		boolean cutlast = false;
		
			
		if (k2>0){
			k2=k2-2;
			cutlast=true;
		}
		else if (k2<0){
			k2 = - k2-1;
		}
		else{
			System.out.println("k2 = 0");
			
		}
		if(k2>k1){
		for (int k = k1;k<=k2;k++){
			res.add(new Note((int)((notelist.get(k).start-t1)*timestretch),notelist.get(k).cents,(int)(timestretch*notelist.get(k).duration)));
			
		}
		}
		
		if(cutlast){
			k2++;
			res.add(new Note((int)((notelist.get(k2).start-t1)*timestretch),notelist.get(k2).cents,(int)((notelist.get(k2).duration-(notelist.get(k2).getNoteOff()-t2))*timestretch)));
		}
		
	}
	return new Partition(res);
	}
	
	public int getcts(int ms){
		int res = 0;
		if( ms>0&&ms<getmaxtime()){
			for (int i = 0; i < notelist.size(); i++) {
				if (notelist.get(i).start <= ms && notelist.get(i).start + notelist.get(i).duration >= ms) {
					res = notelist.get(i).cents;
					break;
				}
		}
			}
		return res;
	}
	
	public int getNoteIdxsigned(int ms){
		int res = 0;
		if( ms>=0&&ms<getmaxtime()){
			for (int i = 0; i < notelist.size(); i++) {
				if (notelist.get(i).start <= ms && notelist.get(i).start + notelist.get(i).duration >= ms) {
					res = i+1;
					break;
				}
				else if (notelist.get(i).start>ms){
					res = -(i+1);
					break;
				}
		}
			}
		return res;
	}

	public void transpose(int cts){
		for (int k = 0 ; k< notelist.size();k++){
			notelist.get(k).cents+=cts;
		}
	}
	public int size() {
		return notelist.size();
	}

	

	public int getmaxtime() {
		if (notelist != null) {
			int last = notelist.size() - 1;
			return notelist.get(last).start + notelist.get(last).duration;
		} else {
			System.out.println("empty partition size querying");
			return 0;
		}

	}

	public int getmaxvalue() {

		int maxbuf = 0;
		int maxval = 0;

		if (notelist != null) {

			for (int i = 0; i < notelist.size(); i++) {
				maxbuf = notelist.get(i).cents;
				maxval = Math.max(maxbuf, maxval);

			}
		}

		return maxval;

	}

	public int getminvalue() {

		int minbuf = 0;
		int minval = 9999999;

		if (notelist != null) {

			for (int i = 0; i < notelist.size(); i++) {
				minbuf = notelist.get(i).cents;
				if (minbuf != 0)
					minval = Math.min(minbuf, minval);

			}
		}

		return minval;

	}

}
