package baqsi.model;

import java.io.Serializable;




public class Reference implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 256484048872921292L;
	public Audio a;
	public int time;
	public double distance;
	public int length;
	public double timestretch;
	public int transpose;
	
	public Reference(Audio ain,int timein,int lengthin, double distancein,double timestretchin,int transpin) {
		a=ain;
		time = timein;
		distance=distancein;
		length = lengthin;
		timestretch = timestretchin;
		transpose = transpin;
		
	}
	
	
	public Partition getPattern(){
		Partition p = a.getPartition().getChunk(time, time + length,timestretch);
		p.transpose(transpose);
		return p;
	}
	
	
	
	
}