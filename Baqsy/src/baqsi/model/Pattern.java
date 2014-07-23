package baqsi.model;

import java.awt.Color;
import java.awt.Container;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.InputMap;

import baqsi.model.Note;


// different norms?


public class Pattern implements Comparable<Pattern>,Serializable{
		/**
	 * 
	 */
	private static final long serialVersionUID = 5380553157029663752L;
		private Partition sourcepattern;
		public int length;
		

		public int number;

		public ArrayList<Reference> references;
		
		
		public boolean isSelected;
		public boolean isClicked;
		
		
	
		
		
		
		
		
		
		
		
		public Pattern(Audio ain, int sourcetimein, int lengthin,double timestretch){
			
			sourcepattern = ain.getPartition().getChunk(sourcetimein, sourcetimein+lengthin,timestretch);
			length= (int) (1.0*lengthin*timestretch);
			references = new ArrayList<Reference>();
			references.add(new Reference(ain, sourcetimein,length, 0.0,timestretch,0));
			isSelected=false;
		
		}
		@SuppressWarnings("unchecked")
		public Pattern clone(){
			Pattern res = new Pattern(getAudio(),getSourceTime(),getlengthinit(),getTimestretch());
			res.references = (ArrayList<Reference>) references.clone();
			return res;
		}
		
		public int getScore(){
			return references.size();
		}
		public int size(){
			return sourcepattern.size();
		}
		public Audio getAudio(){
			return references.get(0).a;
		}
		public double getTimestretch(){
			return references.get(0).timestretch;
		}
		
		public Partition getSourcePattern(){
			return sourcepattern;
		}
		
		public Partition getPattern(){
			return references.get(1).getPattern();
		}
		public int getSourceTime(){
			return references.get(0).time;
		}
		public int getlength(){
			
			return length;
		}
		
		public int getlengthinit(){
			return (int) (length/getTimestretch());
		}
		public double getMeandist(){
			double res =0.0;
			for(int k = 1 ; k<references.size();k++){
				res+=references.get(k).distance;
				
			}
			if(references.size()<=1) res=1.0;
			else res = res/(references.size()-1);
			return res;
		}

		
		public int getTimePlayed(){
			int time=0;
			for(int k = 0 ; k<sourcepattern.size();k++){
				time+=sourcepattern.get(k).duration;
			}
		return time;
		}
		public int computeTransp(Pattern p){
			Partition l = p.getSourcePattern();
			int transp = 0;
			int totdur= 0;			
			int lidx=0;
			

				for(int sidx = 0 ; sidx< sourcepattern.size();sidx++){
					while(lidx<l.size()&&sidx<sourcepattern.size()&&l.get(lidx).start<sourcepattern.get(sidx).getNoteOff()){
						int st = Math.max(l.get(lidx).start,sourcepattern.get(sidx).start);
						int end = Math.min(l.get(lidx).getNoteOff(), sourcepattern.get(sidx).getNoteOff());
						int dist = l.get(lidx).cents-sourcepattern.get(sidx).cents;
						if(end>st){
							int time = (int) ((end-st)*Math.pow((end-st)/200.0,2));
							transp += time*dist;
							totdur += time;
						}
			
						lidx++;
						
					}
			}
				if(totdur>0)transp =  transp/totdur;
		return -transp;
		}
	
		
		
		
		public void addReferences(Reference reference) {
			references.add(reference);
			
		}
		@Override
		public int compareTo(Pattern o) {
			
			if(o.references.get(1).time>references.get(1).time) return -1;
			else if(o.references.get(1).time<references.get(1).time)  return 1;
			
			return 0;
		}
	
	}
