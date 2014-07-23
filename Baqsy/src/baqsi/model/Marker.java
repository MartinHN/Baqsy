package baqsi.model;

import java.io.Serializable;

public class Marker implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8106911334822013291L;
	
	public int time;
	public String name;
	
	public Marker(int t,String n){
		time=t;
		name=n;
	}

	
	
	
	

}
