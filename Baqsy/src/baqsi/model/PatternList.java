package baqsi.model;
import java.awt.List;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import baqsi.controllers.Controllers;
import baqsi.utils.Utils;

/// add searh of best pattern when adding

public class PatternList   implements Iterable<Pattern>,Serializable{
	

	private static final long serialVersionUID = 1753593502667356747L;
	
	
	public ArrayList<Pattern> plist;


	
	
	
	
	
	
	public PatternList() {
		
		plist = new ArrayList<Pattern>();
		
	}
	
	public int getBestPattern(){
		int kidx=-1;
		int refnum=0;
		for(int k = 0 ; k< plist.size();k++){
			
			if(plist.get(k).references.size()>refnum){
				refnum=plist.get(k).references.size();
				kidx=k;
			}
			
			if(plist.get(k).references.size()==refnum){
				double sumold = 0;
				double sumnew = 0.0;
				for(int j = 0 ; j< plist.get(kidx).references.size();j++){
					sumold += plist.get(kidx).references.get(j).distance;
				}
				for(int j = 0 ; j< plist.get(k).references.size();j++){
					sumnew += plist.get(k).references.get(j).distance;
				}
				
				kidx = sumnew>sumold?k:kidx;
			}
			
		}
		return kidx;
	}
	public int size(){
		return plist.size();
	}
	public void add(Pattern p){
		//p.color = Utils.patterncolors[plist.size()%Utils.patterncolors.length];
		plist.add(p);
		
	}
	
	public void clear(){
		plist.clear();
	}
	public Pattern get(int idx){
		return plist.get(idx);
		
	}


	public void remove(Pattern pbuf) {
		
		plist.remove(pbuf);
	
	}

	@Override
	public Iterator<Pattern> iterator() {
		// TODO Auto-generated method stub
		return plist.iterator();
	}

	public boolean contains(Pattern p){
		return plist.contains(p);
	}

	public void remove(int num) {
		plist.remove(num);
		
	}

	
}
