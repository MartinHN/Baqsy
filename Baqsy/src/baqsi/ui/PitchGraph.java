package baqsi.ui;


import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Polygon;

import java.awt.Point;
import java.awt.Rectangle;

import java.awt.RenderingHints;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import baqsi.controllers.Controllers;
import baqsi.model.Marker;
import baqsi.model.Model;
import baqsi.model.Note;
import baqsi.model.Partition;
import baqsi.model.Pitchs;
import baqsi.ui.ViewPanel;
import baqsi.ui.Window;
import baqsi.utils.Utils;




import java.awt.Color;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;

import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

// ajouter polyline thick decroissant avec le zoom

@SuppressWarnings("serial")
public class PitchGraph extends JPanel implements MouseWheelListener,
		MouseMotionListener, MouseListener {

	Controllers c;
	public GraphPlot graphplot;

	double xscale;
	double yscale;

	// affichage
	int w;
	int h;

	final int PADleft = 60;
	final int PADright = 10;
	final int PADup = 20;
	final int PADdown = 17;

	int nticksx = 15;
	int nticksy = 40;
	double fs;

	// drag mouse/zoom
	public int xinit;
	public int yinit;
	public int currentw;
	public int currenth;
	public Rectangle rect = new Rectangle(0, 0, 0, 0);
	public long lastmovetime = System.currentTimeMillis();
	public boolean mouseMoved = false;

	
	
	// hint
	Point mouse = new Point();

	MouseHint mousehintcomp;
	
	
	public class MouseHint extends JComponent {
		public MouseHint() {

			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {

			setBounds(0, 0, getWidth(), getHeight());

			if (isingraph(mouse.x, mouse.y)) {
				drawhint(g);
			}
			// draw mouse drag

			drawdrag(g);
		//	System.out.println("repaintmouuuuse");

		}

		private void drawhint(Graphics g) {

			g.setFont(new Font("Arial", Font.BOLD, 11));
			g.setColor(Color.black);
			g.drawString(
					Utils.ctsToString((int) ((this.getHeight() - PADdown - mouse.y)
							/ (float) yscale + c.graphctl.curymin)),
					mouse.x + 10, mouse.y);

		}

		private void drawdrag(Graphics g) {

			int x = xinit;
			int y = yinit;
			int w = currentw;
			int h = currenth;
			if (currentw < 0) {
				if (xinit + currentw < PADleft) {
					x = PADleft + 1;
					w = xinit - PADleft;
				}

				else {
					x = xinit + currentw;
					w = -currentw;
				}
			}

			if (currenth < 0) {
				y = yinit - 4;
				x = xinit - 4;
				w = 4;
				h = 4;

			}

			rect = new Rectangle(x, y, w, h);
			g.setColor(Color.red);
			g.drawRect(x, y, w, h);

		}

	}

	// Transport
	int curx = 10;

	// freq hints
	int freqhint = 0;
	int hintwidth = 20;
	int hintheigth = 20;
	Rectangle hintframe = new Rectangle(0, 0, hintwidth, hintheigth);
	boolean mousein = false;

	public PitchGraph(Controllers cin) {
		super();

		c = cin;

		setLayout(null);

		mousehintcomp = new MouseHint();
		mousehintcomp.setVisible(true);
		// mousehintcomp.setBounds(0, 0, 300, 300);

		add(mousehintcomp, JLayeredPane.DRAG_LAYER);

		graphplot = new GraphPlot();
		graphplot.setVisible(true);
		// graphplot.setBounds(0, 0, 300, 300);
		add(graphplot, JLayeredPane.DEFAULT_LAYER);

		setVisible(true);

	}

	public class GraphPlot extends JComponent {
		// painting

		@Override
		protected void paintComponent(Graphics g) {
			g.clearRect(0, 0, getWidth(), getHeight());

			if (!c.selectionIsEmpty()) {
				initvalues();

				//System.out.println("repaintcore");

				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				// draw frame box
				drawframe(g2);

				// transport
				drawcursor(g2);

				// The origin location.
				int x0 = PADleft;
				int y0 = h - PADdown;
				// draw notes
				drawpartitions(g2, x0, y0);

				// draw pitchline
				drawpitchs(g2, x0, y0);
				drawmarkers(g2);
			}

		}
		private void drawmarkers(Graphics2D g){
			g.setColor(getBackground());
			g.fill3DRect(PADleft, 0, getWidth()-PADright-PADleft, PADup,true);
			if (c.getViewableAudioList() != null) {
				//int kidx = c.getCurrentselectionIdx();
				
				for (int j = 0; j < c.getAudioList().size(); j++) {
					if (c.getAudioList().get(j).isSelectedForView) {
						g.setColor(c.getColors()[j]);
						ArrayList<Marker> l = c.getAudioList().get(j).markers;
						if(l.size()>0){
							for(Marker m:l){
						
							if(m.time<c.graphctl.curxmax&&m.time>c.graphctl.curxmin){
								int xmark = (int)(PADleft + (m.time-c.graphctl.curxmin)*xscale);
								int[] xcurmark = {xmark-2,xmark-2,xmark,xmark+2,xmark+2};
								int[] ycurmark = {PADup-2,PADup+2,PADup+4,PADup+2,PADup-2};
								
								g.fillPolygon(new Polygon(xcurmark, ycurmark, xcurmark.length));
								g.drawLine(xmark, PADup, xmark, getHeight()-PADdown);
								g.drawString(m.name, xmark, PADup-4);
							
							}
							
							
							}
						}
						
					}
				}
			}

		}

		private void drawframe(Graphics2D g) {

			g.setPaint(Utils.sheetColor);
			g.fillRect(PADleft, PADup, w - (PADleft + PADright), h
					- (PADdown + PADup));
			g.setPaint(Color.BLACK);
			g.drawLine(PADleft, PADup, PADleft, h - PADdown);
			g.setPaint(Color.LIGHT_GRAY);
			// add ticks
			int m = 3;

			g.setFont(new Font("Arial", Font.PLAIN, 11));

			if (c.viewctl.normal) {
				for (int i = 10 * (int) Math.ceil(c.graphctl.curymin / 10); i < 10 * (int) Math
						.floor(c.graphctl.curymax / 10); i = i + 10) {

					if (i % 100 == 0) {
						int y = (int) (h - PADdown - (10
								* Math.ceil(c.graphctl.curymin / 10)
								- c.graphctl.curymin + i - c.graphctl.curymin)
								* yscale);

						g.setPaint(Color.black);
						g.drawString(Utils.ctsToString(i), 0, y);
						g.setPaint(Utils.ticksColor);
						g.drawLine(PADleft - 2, y, w - PADright, y);

					}

				}
			} else if (c.viewctl.rescaled) {
				ArrayList<Integer> overwritingbuf = new ArrayList<Integer>();
				for (int l = 0; l < c.getAudioList().size(); l++) {
					if (c.m.audiolist.get(l).isSelectedForScale) {

						
							ArrayList<Integer> curscale = c.getAudioList().get(l).scale.scale;
							for (int k = 0; k < curscale.size(); k++) {

								if (curscale.get(k)<c.graphctl.curymax&& curscale.get(k)>c.graphctl.curymin ) {
									int y = (int) (h - PADdown - (curscale.get(k) - c.graphctl.curymin)
											* yscale);
									
									if( Utils.getMinDist(y, overwritingbuf)>10){

									g.setPaint(Color.black);
									g.drawString(Utils.ctsToString(curscale.get(k)), 0, y);
									overwritingbuf.add(y);
									}
									g.setPaint(c.getColors()[l]);
									g.drawLine(PADleft - 2, y, w - PADright, y);
								
								
									
							}

						}
					}
				}

			}

			
			m = 1;
			int i = 0;
			g.setPaint(Color.black);
			g.drawLine(PADleft, h - PADdown, w - PADright, h - PADdown);

			// add ticks
			ArrayList<Integer> overwritingbuf = new ArrayList<Integer>();
			do {
				int ndraw = (int) Math
						.floor((c.graphctl.curxmax - c.graphctl.curxmin)
								/ (1000.0 * Math.pow(2, m)));
				if (ndraw >= 1) {

					for (int k = 0; k < ndraw; k++) {
						
						double htick = 1000.0
								* Math.pow(2, m)
								* (Math.floor(c.graphctl.curxmin
										/ (1000.0 * Math.pow(2, m)) + 1) + k)
								- c.graphctl.curxmin;
						
						if(Utils.getMinDist((int)(htick *xscale*1.0), overwritingbuf)>30){
						g.setPaint(Color.black);
						g.drawString(
								(htick + c.graphctl.curxmin) % 1000 == 0 ? Integer
										.toString((int) ((htick + c.graphctl.curxmin) / 1000))
										: Float.toString((float) ((htick + c.graphctl.curxmin) / 1000.0)),
								(int) (PADleft + htick * xscale * 1.0), h);
						g.setPaint(Color.LIGHT_GRAY);
						g.drawLine((int) (PADleft + 1.0 * htick * xscale), h
								- PADdown + 2, (int) (PADleft + 1.0 * htick
								* xscale), h - PADdown - 2);
						i++;
						overwritingbuf.add((int) ( 1.0 * htick * xscale));
						}
					
					}
				}

				m--;

			} while (i * 2 < nticksx);
		}

		private void drawcursor(Graphics2D g) {
			int curpos = (int) ((curx - c.graphctl.curxmin) * xscale + PADleft);
			if (isingraph(
					curpos,
					PADup + 2)) {
				g.setColor(Color.red);
				g.drawLine(
						curpos,
						h - PADdown, curpos, 0);

			}
		}

		private void drawpartitions(Graphics g, int x0, int y0) {

			if (c.getViewablePartition() != null) {
				int kidx = c.getCurrentselectionIdx();
				
				for (int j = 0; j < c.getPartitions().size(); j++) {
					if (j != kidx&&c.getAudioList().get(j).isSelectedForView) {
						g.setColor(c.getColors()[j]);
						int height = (int) (c.getAudioList().get(j).notewidth * yscale);
						if(height<15)height = 15;

						Partition part = c.getPartitions().get(j);

						for (int k = 0; k < part.size(); k++) {
							Note note = part.get(k);
							if (note.start >= c.graphctl.curxmin
									&& note.start + note.duration <= c.graphctl.curxmax) {
								g.fill3DRect(
										(int) (x0 + xscale
												* (note.start - c.graphctl.curxmin)),
										(int) (y0 - height / 2 - (note.cents - c.graphctl.curymin)
												* yscale),
										(int) (note.duration * xscale), height,
										true);
							} else if (note.start + note.duration > c.graphctl.curxmin
									&& note.start < c.graphctl.curxmin) {
								g.fill3DRect(
										x0,
										(int) (y0 - height / 2 - (note.cents - c.graphctl.curymin)
												* yscale),
										(int) ((note.duration - (c.graphctl.curxmin - note.start)) * xscale),
										height, true);

							} else if (note.start < c.graphctl.curxmax
									&& note.start + note.duration > c.graphctl.curxmax) {
								g.fill3DRect(
										(int) (x0 + (note.start - c.graphctl.curxmin)
												* xscale),
										(int) (y0 - height / 2 - (note.cents - c.graphctl.curymin)
												* yscale),
										(int) (xscale * (c.graphctl.curxmax - note.start)),
										height, true);

							}

						}

					}
				}
				if (kidx >= 0&&c.getAudioList().get(kidx).isSelectedForView) {
					g.setColor(c.getColors()[kidx]);
					int height = (int) (c.getAudioList().get(kidx).notewidth * yscale);
					if(height<15 )height = 15;

					Partition part = c.getPartitions().get(kidx);

					for (int k = 0; k < part.size(); k++) {
						Note note = part.get(k);
						if (note.start >= c.graphctl.curxmin
								&& note.start + note.duration <= c.graphctl.curxmax) {
							g.fill3DRect(
									(int) (x0 + xscale
											* (note.start - c.graphctl.curxmin)),
									(int) (y0 - height / 2 - (note.cents - c.graphctl.curymin)
											* yscale),
									(int) (note.duration * xscale), height,
									true);
						} else if (note.start + note.duration > c.graphctl.curxmin
								&& note.start < c.graphctl.curxmin) {
							g.fill3DRect(
									x0,
									(int) (y0 - height / 2 - (note.cents - c.graphctl.curymin)
											* yscale),
									(int) ((note.duration - (c.graphctl.curxmin - note.start)) * xscale),
									height, true);

						} else if (note.start < c.graphctl.curxmax
								&& note.start + note.duration > c.graphctl.curxmax) {
							g.fill3DRect(
									(int) (x0 + (note.start - c.graphctl.curxmin)
											* xscale),
									(int) (y0 - height / 2 - (note.cents - c.graphctl.curymin)
											* yscale),
									(int) (xscale * (c.graphctl.curxmax - note.start)),
									height, true);

						}

					}
				}
			}
		}

		private void drawpitchs(Graphics g, int x0, int y0) {

			if (c.getViewablePitchs().size() != 0) {

				for (int j = 0; j < c.getViewablePitchs().size(); j++) {

					Pitchs pl = c.getViewablePitchs().get(j);
					g.setColor(new Color(0.7F, .7F, .7F));
					int size = pl.viewablevalues.length;

					
					double xscaletmp = xscale * 1000.0 / pl.pitchfs;
					int kstart = (int) (c.graphctl.curxmin * pl.pitchfs / 1000.0);
					int cursize = (int)((c.graphctl.curxmax+1000-c.graphctl.curxmin) * pl.pitchfs / 1000.0);
					
					int[] xpoints = new int[cursize];
					int[] ypoints = new int[cursize];

					int x = 0;
					int l = 0;
					

					//
					// vision note only
					// //

					if (!c.viewctl.notePitch) {
						Partition part = c.getViewablePartition().get(j);

						for (int k = 0; k < cursize; k++) {

							do {
								while (k + kstart < size
										&& x < getWidth() - PADright
										&& (!part.isPlayTime((int) ((k * 1000.0/ pl.pitchfs + c.graphctl.curxmin)))||pl.viewablevalues[(k + kstart)] == 0 )) {
									
									k += xscaletmp < 1 ? (int) (4.0 / xscaletmp)
											: 1;
								}

								l = 0;

								while (k + kstart < size
										&& x < getWidth() - PADright
										&& (pl.viewablevalues[(k + kstart)] != 0 && part
												.isPlayTime((int) ((k * 1000.0
														/ pl.pitchfs + c.graphctl.curxmin))))) {

									x = (int) (x0 + k * xscaletmp);

									// > 0 pour filtrage, >1 pour visu

									xpoints[l] = x;
									ypoints[l] = (int) (y0 - ((float) yscale * (pl.viewablevalues[(k + kstart)] - c.graphctl.curymin)));
									l++;

									k += xscaletmp < 1 ? (int) (2.0 / xscaletmp)
											: 1;
								}

								g.drawPolyline(xpoints, ypoints, l);

							} while (k + kstart < size
									&& x < getWidth() - PADright);
						}
					}

					// ///
					// / whole pitch line
					// ////

					else {

						for (int k = 0; k < cursize; k++) {

							do {
								while (k + kstart < size
										&& pl.viewablevalues[(k + kstart)] == 0
										&& x < getWidth() - PADright) {
									k += xscaletmp < 1 ? (int) (4.0 / (xscaletmp))
											: 1;
								}

								l = 0;

								while (k + kstart < size
										&& pl.viewablevalues[(k + kstart)] != 0
										&& x < getWidth() - PADright) {

									x = (int) (x0 + k * xscaletmp);

									// > 0 pour filtrage, >1 pour visu

									xpoints[l] = x;
									ypoints[l] = (int) (y0 - ((float) yscale * (pl.viewablevalues[(k + kstart)] - c.graphctl.curymin)));
									l++;

									k += xscaletmp < 1 ? (int) (2.0 / (xscaletmp))
											: 1;
								}

								g.drawPolyline(xpoints, ypoints, l);

							} while (k + kstart < size
									&& x < getWidth() - PADright);
						}

					}

				}
			}
		}

		// Utils

	}

	@Override
	protected void paintComponent(Graphics g) {

		if (!c.selectionIsEmpty()) {

			mousehintcomp.setBounds(0, 0, getWidth(), getHeight());

			graphplot.setBounds(0, 0, getWidth(), getHeight());

		}
	}

	private boolean isingraph(int x, int y) {
		if (x > PADleft && x < this.getWidth() - PADright
				&& y < getHeight() - PADdown && y > PADup) {
			return true;
		}
		return false;

	}

	// global methods

	private void initvalues() {
		w = getWidth();
		h = getHeight();

		// take the higher fs
		fs = 0;
		for (int k = 0; k < c.getViewablePitchs().size(); k++) {
			fs = Math.max(c.getPitchs().get(k).pitchfs, fs);
		}
		xscale = ((w - (PADleft + PADright)) / (float) (c.graphctl.curxmax - c.graphctl.curxmin));
		yscale = ((h - (PADup + PADdown)) / (float) (c.graphctl.curymax - c.graphctl.curymin));

	}

	public void setcurx(int ms) {
		int delta = c.graphctl.curxmax - c.graphctl.curxmin;
		int delta2 = delta/2;
  		if (c.viewctl.isFollowing&&(c.graphctl.curxmax - ms) < delta2) {
			curx = ms;
			int adv = c.graphctl.curxmax - curx;
			if (adv > 0) {
				c.graphctl.curxmin += delta2 - adv;			//105 * (delta/3) / adv;
				c.graphctl.curxmax +=delta2 -adv;			// 105 * (delta/3) / adv;
			} else {
				
				c.graphctl.curxmax = curx + delta2;
				c.graphctl.curxmin = curx - delta2;
			}
			repaint();}
		else if(c.viewctl.isFollowing&& ms<c.graphctl.curxmin){
		
			curx = ms;
			c.graphctl.curxmin = Math.max(ms-1000, 0);
			c.graphctl.curxmax = c.graphctl.curxmin+delta;
			repaint();
		}
  		
  		// repaint efficiently
	else if(ms<=c.graphctl.curxmax+100&&ms>c.graphctl.curxmin){
			int lastpos=(int) ((curx - c.graphctl.curxmin) * xscale + PADleft);
			curx = ms;
			int curpos = (int) ((curx - c.graphctl.curxmin) * xscale + PADleft);
		 int width = Math.abs(curpos-lastpos)<2? 2 : Math.abs(curpos-lastpos);
			repaint(Math.min(curpos, lastpos) , 0,2*width,h);
			
			
		}
		
	}

	public double getcurx() {
		return curx / 1000.0;
	}

	public void setzoom(int x, int y, int h, int w) {
		int xmint = (int) ((x - PADleft) / (float) xscale + c.graphctl.curxmin);
		int ymaxt = (int) ((this.getHeight() - y - PADdown) / (float) yscale + c.graphctl.curymin);
		int xmaxt = (int) (xmint + w / (float) xscale);
		int ymint = (int) (ymaxt - h / (float) yscale);

		if (ymaxt - ymint > 1 && xmaxt - xmint > 25) {
			c.graphctl.curxmin = xmint;
			c.graphctl.curymin = ymint;
			c.graphctl.curxmax = xmaxt;
			c.graphctl.curymax = ymaxt;
		}

	}

	public void zoom(Point center, int direction) {
		
		double zoomstep = (c.graphctl.curxmax - c.graphctl.curxmin) / 106.0;
		
		int xmint = (int) (c.graphctl.curxmin - direction
				* zoomstep);
		int xmaxt = (int) (c.graphctl.curxmax + direction
				* zoomstep );
		
		
		
		// left/right zooming 1.0 - 2.0*...  == exact scrolling
		if(direction<0){
		double distfromcenter =  1.0 - 2.0*((center.x-PADleft))*1.0/(getWidth()-PADleft-PADright);

		xmint+=distfromcenter*direction*zoomstep;
		xmaxt+=distfromcenter*direction*zoomstep; 
		}

		if (xmaxt - xmint > 25) {

			if (xmint >= 0) {
				c.graphctl.curxmin = xmint;
			} else
				c.graphctl.curxmin = 0;

			if (xmaxt <= c.graphctl.xmax) {
				c.graphctl.curxmax = xmaxt;
			} else
				c.graphctl.curxmax = c.graphctl.xmax;

			// auto zoom y
			int ymint = c.getMinValue(c.graphctl.curxmin, c.graphctl.curxmax,
					true);
			int ymaxt = c.getMaxValue(c.graphctl.curxmin, c.graphctl.curxmax,
					true);

			if (ymaxt - ymint > 1) {
				if (ymint >= c.graphctl.ymin) {
					c.graphctl.curymin = ymint - 100;
				} else
					c.graphctl.curymin = c.graphctl.ymin;

				if (ymaxt <= c.graphctl.ymax) {
					c.graphctl.curymax = ymaxt + 100;
				} else
					c.graphctl.curymax = c.graphctl.ymax;

			}

		}

	}

	// Listeners
	@Override
	public void mouseClicked(MouseEvent e) {
		if(c.getCurrentselection()!=null){
		ArrayList<Marker> lm = c.getCurrentselection().markers;
		int mx = e.getPoint().x;
		int my = e.getPoint().y;
		
		if(e.getClickCount()==2&&my<PADup){
			boolean create = true;
			int t = (int) ((mx-PADleft)/xscale + c.graphctl.curxmin);
		
		for(int k=0;k<lm.size();k++){
			Marker m=lm.get(k);
			if(m.time>c.graphctl.curxmin&&m.time<c.graphctl.curxmax){
				if(Math.abs((mx-PADleft)-(m.time-c.graphctl.curxmin)*xscale)<10){
					create = false;
					lm.remove(m);
					break;
				}
			}
		}

		if(create == true){
	String s = (String) JOptionPane.showInputDialog(c.w,"Marker's name","Marker creator",JOptionPane.PLAIN_MESSAGE,null,null,null);
	if(s!=null){	lm.add(new Marker(t,s));}
	}
		}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		requestFocusInWindow();
		if (isingraph(e.getX(), e.getY())) {
			xinit = e.getX();
			yinit = e.getY();

		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {

		if (currenth > 0) {

			setzoom(rect.x, rect.y, rect.height, rect.width);
		} else if (currenth == 0 && currentw == 0 && xinit != 0) {
			curx = (int) ((xinit - PADleft) / (xscale) + c.graphctl.curxmin);
			c.w.partitionpanel.part.setcurx(curx);
			c.m.mp.setCurrentTime((xinit - PADleft) / (1000.0 * xscale) + 1.0
					* c.graphctl.curxmin / 1000.0);

		} else if (currenth < 0) {
			c.graphctl.curxmin = c.graphctl.xmin;
			c.graphctl.curymin = c.graphctl.ymin;
			c.graphctl.curxmax = c.graphctl.xmax;
			c.graphctl.curymax = c.graphctl.ymax;
		}
		currentw = 0;
		currenth = 0;
		xinit = 0;
		yinit = 0;
		c.graphctl.repaint();
		// System.out.println("released"+e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		if (isingraph(xinit, yinit)) {

			currentw = e.getX() - xinit;
			currenth = e.getY() - yinit;
			repaint();

		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (!c.selectionIsEmpty()
				&& System.currentTimeMillis() - lastmovetime > 45) {
			Point pmouse = (Point) mouse.clone();
			mouse = e.getPoint();

			Rectangle rectangle = new Rectangle();
			rectangle.x = -hintwidth
					+ (pmouse.x < mouse.x ? pmouse.x : mouse.x);
			rectangle.y = -hintheigth
					+ (pmouse.y < mouse.y ? pmouse.y : mouse.y);
			rectangle.width = 6 * hintwidth + Math.abs(pmouse.x - mouse.x);
			rectangle.height = 2 * hintheigth + Math.abs(pmouse.y - mouse.y);
			// revalidate();
			// mousehintcomp.paintComponent(g2);
			// mouseMoved = true;
			repaint(rectangle);
			// repaint(-30 + pmouse.x<mouse.x?pmouse.x : mouse.x,-30 +
			// pmouse.y<mouse.y?pmouse.y : mouse.y, 60
			// +Math.abs(pmouse.x-mouse.x),40 + Math.abs(pmouse.y-mouse.y));
			// repaint(mouse.x-getWidth()/4,mouse.y-getHeight()/4,getWidth()/2,getHeight()/2);
			lastmovetime = System.currentTimeMillis();

		}
		// graphplot.setIgnoreRepaint(true);
		// mousehintcomp.repaint();

		// update(g2);
		// graphplot.setVisible(true);
		// validate();
		// System.out.println("mousemoved");

		// }

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {

		zoom(arg0.getPoint(), arg0.getWheelRotation());
		c.graphctl.repaint();

	}

}
