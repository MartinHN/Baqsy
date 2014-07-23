package baqsi.ui;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import baqsi.controllers.Controllers;
import baqsi.model.Audio;
import baqsi.model.Pattern;
import baqsi.model.PatternList;
import baqsi.utils.Utils;



@SuppressWarnings("serial")
public class PatternPanel extends JPanel{
	Controllers c;
	
	
	public JTable pattTable;

	public JButton searchPattern;
	public JButton addPattern;
	public JButton settings;
	
	public PatternSettingsWindow settingsw;
	
	
	final int rowHeight = 20;
	
	public PatternGraph  pattgrph;
	
	public PatternPanel(Controllers cin) {
		
		c = cin;
		build();
		place();		
	}
	
	private void build(){
		
		
		searchPattern = new JButton(c.patternctl.searchPattern);
		searchPattern.setText("Search Patterns");
		searchPattern.setFocusable(false);
		
		addPattern = new JButton(c.patternctl.addPattern);
		addPattern.setText("Add Pattern");
		addPattern.setFocusable(false);
		
		settingsw = new PatternSettingsWindow(c);
		settingsw.setVisible(false);
		settingsw.setFocusable(false);
		settings = new JButton(new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				settingsw.setVisible(true);
			}
		});
		settings.setText("Settings...");
		settings.setFocusable(false);
		
		pattTable = new JTable(c.m.pattTable);
		pattTable.setBorder(null);
		pattTable.setFocusable(false);
		pattTable.setAutoscrolls(false);
		pattTable.getColumnModel().getColumn(0).setMaxWidth(60);
		pattTable.getColumnModel().getColumn(0).setMinWidth(50);
		pattTable.setDefaultRenderer(PatternList.class,new MyPatternRenderer());
		pattTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int x = e.getPoint().x-pattTable.getColumnModel().getColumn(0).getWidth();
				int y = e.getPoint().y;
				int selrow = pattTable.getSelectedRow();
				if(pattTable.getSelectedColumn()==1&&selrow>=0){
					
					PatternList curpattlist =  c.getAudioList().get(selrow).pattlist;
					int pattnum = ((PatternViewer)pattTable.getCellRenderer(selrow,1).getTableCellRendererComponent(pattTable,curpattlist, true, false, selrow, 1)).isOnPattern(x, y);
						if(pattnum>=0){
							
						if(e.getButton()==1){
							// delete
							if (e.isControlDown())	{
								
								//if master
								if(curpattlist.get(pattnum).references.get(1).distance==0.0){
									c.patternctl.deleteMasterPattern(curpattlist, pattnum);

								}
								
								// if selected pattern corresponds to any occurence found by the search algorithm
								else c.patternctl.deleteSlavePattern(curpattlist, pattnum);
							}
							
							//select
							else c.patternctl.patternClicked(curpattlist,pattnum,e.getClickCount()==2);
						}
						
						//System.out.println(pattnum + " num : "+curpattlist.get(pattnum).number + "x" + x);
					}
						else{
							c.patternctl.unSelectAll();
						}
				}
				}
			
		});
		//pattTable.setDefaultEditor(PatternList.class, new MyPatternEditor());
		
	pattTable.setRowHeight(rowHeight);
		pattTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		
		
		pattgrph= new PatternGraph(c);
	}
	
	private void place(){
	
	JPanel jpl = new JPanel(new GridBagLayout());
	GridBagConstraints ctr = new GridBagConstraints();
	ctr.fill = GridBagConstraints.BOTH;
	ctr.weightx = 1;
	ctr.weighty=30;
	ctr.gridx = 0;
	ctr.gridy = 0;
	ctr.gridwidth=3;
	JScrollPane scpane = new JScrollPane(pattTable);
	scpane.getHorizontalScrollBar().setUnitIncrement(3);
	
	
	jpl.add(scpane,ctr);
	
	
	
	
	ctr.gridwidth=1;
	ctr.weightx = 1;
	ctr.weighty=1;
	ctr.fill = GridBagConstraints.NONE;
	
	ctr.gridy = 1;
	
	ctr.gridx=0;
	
	jpl.add(addPattern,ctr);

	ctr.gridx ++;
	jpl.add(searchPattern, ctr);
	
	ctr.gridx ++;
	jpl.add(settings, ctr);
	
	pattgrph.setMinimumSize(new Dimension(300,200));
	
	JSplitPane spane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			jpl, pattgrph);
	setLayout(new GridBagLayout());
	ctr.gridx=0;
	ctr.gridy=0;
	ctr.weightx=1;
	ctr.weighty=1;
	ctr.fill = GridBagConstraints.BOTH;
	add(spane,ctr);
	/*ctr.gridheight = 2;
	ctr.fill =GridBagConstraints.BOTH;
	ctr.gridx=4;
	ctr.gridy=0;
	ctr.weightx=6;
	
	add(pattgrph,ctr);
	*/	
		
	}
	
	
	public class MyPatternRenderer implements TableCellRenderer{

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
		
			// Utils.patterncolors
			
			
			return new PatternViewer((PatternList)value);
		}
		
	}
	/*
	public class MyPatternEditor implements TableCellEditor{

		@Override
		public void addCellEditorListener(CellEditorListener arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void cancelCellEditing() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object getCellEditorValue() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isCellEditable(EventObject arg0) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public void removeCellEditorListener(CellEditorListener arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean shouldSelectCell(EventObject arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean stopCellEditing() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Component getTableCellEditorComponent(JTable arg0, Object value,
				boolean isSelected, int row, int col) {
			// TODO Auto-generated method stub
			PatternList patlist = (PatternList)value;
			PatternViewer pv = new PatternViewer(patlist);
			if(arg0.getMousePosition(false)!=null){
				if()
			int x = arg0.getMousePosition(false).x-arg0.getColumnModel().getColumn(0).getWidth();
			int y = arg0.getMousePosition(false).y;
			int pattnum = isOnPattern(pv.plist,x,y);
			if(pattnum>=0){
				c.corpusctl.PatternSelected(c.getAudioList().get(row),patlist.get(pattnum).references.get(1).time , patlist.get(pattnum).references.get(1).length);
				System.out.println(pattnum);
			}
			}
			return null;
		}
		private int isOnPattern(ArrayList<Point> plist,int x,int y){
			int pattnum = -1;
			for (int k = 0 ; k< plist.size();k++){
				Point curpoint = plist.get(k);
				if(x>curpoint.x && x<curpoint.x +curpoint.y && ((k<plist.size()-1&&plist.get(k+1).x>x) || k==plist.size()-1) ){
					
					pattnum = k;
					break;
				}
			}
			
			
			return pattnum;
		}
		
	}*/
	
	
	
	private class PatternViewer extends JComponent {
		
		ArrayList<Point> plist;
		ArrayList<Color> colorlist;
		ArrayList<Integer> numlist;
		ArrayList<Double> distlist;
		ArrayList<Boolean> sellist;
		int width;
	
	
	
		PatternViewer(PatternList pl){
			
		
		
		
		build(pl);
		//place();
		}
		private void build(PatternList pl){
			
		
			
			plist = new 	ArrayList<Point>() ;
			colorlist = new ArrayList<Color> ();
			numlist = new ArrayList<Integer>();
			sellist  = new ArrayList<Boolean>();
			distlist = new ArrayList<Double>();
		if(!c.patternctl.isProcessing&&pl!=null&&pl.size()!=0){
			width = (int) (c.patternctl.xscale * (pl.get(pl.size()-1).references.get(1).time +pl.get(pl.size()-1).references.get(1).length)); 
			for(int k = 0 ; k< pl.size();k++){
				numlist.add(pl.get(k).number);
				distlist.add(pl.get(k).references.get(1).distance);
				sellist.add(pl.get(k).isSelected);
				Color col = Utils.patterncolors[pl.get(k).number%Utils.patterncolors.length];
				if (col == null)
				{System.out.println("colnul");}
				else{
				colorlist.add(new Color(col.getRed(),col.getGreen(),col.getBlue(),(int)(50 +205.0*(1-pl.get(k).references.get(1).distance))));
				plist.add(new Point ((int) (c.patternctl.xscale*pl.get(k).references.get(1).time),(int) (c.patternctl.xscale*pl.get(k).references.get(1).length)));
				}
			}
			
			setPreferredSize(new Dimension(width,rowHeight));
		}
		}
		
		
		
		
		// return idx of pattern on audio pattern list 
		public int isOnPattern(int x,int y){
			int pattnum = -1;
			for (int k = 0 ; k< plist.size();k++){
				Point curpoint = plist.get(k);
				if(x>curpoint.x && x<curpoint.x +curpoint.y && ((k<plist.size()-2&&(plist.get(k+1).x>x|| (plist.get(k+1).x+plist.get(k+1).y<x&&plist.get(k+2).x>x))|| k>=plist.size()-2) )){
					
					pattnum = k;
					break;
				}
			}
			return pattnum;
			}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if(!c.patternctl.isProcessing){
			
			for(int k =0 ; k< plist.size();k++){
				
			g.setColor(colorlist.get(k));
			if(sellist.get(k)){
				g.setColor(Color.red);
			}
			g.fill3DRect(plist.get(k).x, 0, plist.get(k).y, rowHeight-2,true);
			
		
			
			g.setColor(Color.black);
			g.setFont(new Font("Arial",Font.BOLD,14));
			g.drawString(Integer.toString(numlist.get(k)), plist.get(k).x + 5, 3*rowHeight/4);
			g.setFont(new Font("Arial",Font.ITALIC,8));
			g.drawString(Integer.toString((int)(100.0*distlist.get(k))), plist.get(k).x + 14, 3*rowHeight/4);
			}
			for(int k =0 ; k< plist.size();k++){
				g.setColor(Color.black);
				if(sellist.get(k)&&colorlist.get(k).getAlpha()>=254){
					g.drawRect(plist.get(k).x, 0, plist.get(k).y, rowHeight-2);
					g.drawRect(plist.get(k).x+1, 1, plist.get(k).y-2, rowHeight-3);
					
				}
				g.setColor(Color.gray);
				
			}
			}
		}
		
		

}
}
