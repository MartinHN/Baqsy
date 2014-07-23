package baqsi.ui;


import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.Point;
import java.awt.Rectangle;

import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import baqsi.controllers.Controllers;
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
public class PatternGraph extends JPanel implements MouseWheelListener,
		MouseMotionListener, MouseListener {

	Controllers c;
	public GraphPlot graphplot;

	double xscale;
	double yscale;

	// affichage
	int w;
	int h;

	final int PADleft = 33;
	final int PADright = 2;
	final int PADup = 2;
	final int PADdown = 10;

	int nticksx = 5;
	int nticksy = 4;
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
							/ (float) yscale + c.patterngraphctl.curymin)),
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

	public PatternGraph(Controllers cin) {
		super();

		c = cin;

		setLayout(null);

	//	mousehintcomp = new MouseHint();
		//mousehintcomp.setVisible(true);
		// mousehintcomp.setBounds(0, 0, 300, 300);

		//add(mousehintcomp, JLayeredPane.DRAG_LAYER);

		graphplot = new GraphPlot();
		graphplot.setVisible(true);
		// graphplot.setBounds(0, 0, 300, 300);
		add(graphplot);

		setVisible(true);

	}

	public class GraphPlot extends JComponent {
		// painting

		@Override
		protected void paintComponent(Graphics g) {
			g.clearRect(0, 0, getWidth(), getHeight());

			if (c.patterngraphctl.curparts.size()>0) {
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
				for (int i = 10 * (int) Math.ceil(c.patterngraphctl.curymin / 10); i < 10 * (int) Math
						.floor(c.patterngraphctl.curymax / 10); i = i + 10) {

					if (i % 100 == 0) {
						int y = (int) (h - PADdown - (10
								* Math.ceil(c.patterngraphctl.curymin / 10)
								- c.patterngraphctl.curymin + i - c.patterngraphctl.curymin)
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

								if (curscale.get(k)<c.patterngraphctl.curymax&& curscale.get(k)>c.patterngraphctl.curymin ) {
									int y = (int) (h - PADdown - (curscale.get(k) - c.patterngraphctl.curymin)
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
						.floor((c.patterngraphctl.curxmax - c.patterngraphctl.curxmin)
								/ (1000.0 * Math.pow(2, m)));
				if (ndraw >= 1) {

					for (int k = 0; k < ndraw; k++) {
						
						double htick = 1000.0
								* Math.pow(2, m)
								* (Math.floor(c.patterngraphctl.curxmin
										/ (1000.0 * Math.pow(2, m)) + 1) + k)
								- c.patterngraphctl.curxmin;
						
						if(Utils.getMinDist((int)(htick *xscale*1.0), overwritingbuf)>30){
						g.setPaint(Color.black);
						g.drawString(
								(htick + c.patterngraphctl.curxmin) % 1000 == 0 ? Integer
										.toString((int) ((htick + c.patterngraphctl.curxmin) / 1000))
										: Float.toString((float) ((htick + c.patterngraphctl.curxmin) / 1000.0)),
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
			int curpos = (int) ((curx - c.patterngraphctl.curxmin) * xscale + PADleft);
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
int foreground=-1;
			if (c.patterngraphctl.curparts!=null) {
				
				//System.out.println(c.patterngraphctl.curparts.size());
				for (int j = 0; j < c.patterngraphctl.curparts.size(); j++) {
					
					
						g.setColor(c.patterngraphctl.curcollist.get(j));
						if(g.getColor()!=Color.red){
						int cheight = (int) (5.0+(10.0*(1.0-c.patterngraphctl.curdistlist.get(j))) );

						Partition part = c.patterngraphctl.curparts.get(j);

						for (int k = 0; k < part.size(); k++) {
							Note note = part.get(k);
							if (note.start >= c.patterngraphctl.curxmin
									&& note.start + note.duration <= c.patterngraphctl.curxmax) {
								g.fill3DRect(
										(int) (x0 + xscale
												* (note.start - c.patterngraphctl.curxmin)),
										(int) (y0 - cheight / 2 - (note.cents - c.patterngraphctl.curymin)
												* yscale),
										(int) (note.duration * xscale), cheight,
										true);
							} else if (note.start + note.duration > c.patterngraphctl.curxmin
									&& note.start < c.patterngraphctl.curxmin) {
								g.fill3DRect(
										x0,
										(int) (y0 - cheight / 2 - (note.cents - c.patterngraphctl.curymin)
												* yscale),
										(int) ((note.duration - (c.patterngraphctl.curxmin - note.start)) * xscale),
										cheight, true);

							} else if (note.start < c.patterngraphctl.curxmax
									&& note.start + note.duration > c.patterngraphctl.curxmax) {
								g.fill3DRect(
										(int) (x0 + (note.start - c.patterngraphctl.curxmin)
												* xscale),
										(int) (y0 - cheight / 2 - (note.cents - c.patterngraphctl.curymin)
												* yscale),
										(int) (xscale * (c.patterngraphctl.curxmax - note.start)),
										cheight, true);

							}

						}

					}
						else {
							foreground = j;
						}
			
				
			}
				
				if (foreground>=0){
					g.setColor(Color.red);
					int cheight = (int) (5.0+(10.0*(1.0-c.patterngraphctl.curdistlist.get(foreground))) );

					Partition part = c.patterngraphctl.curparts.get(foreground);

					for (int k = 0; k < part.size(); k++) {
						Note note = part.get(k);
						g.setColor(Color.red);
						if (note.start >= c.patterngraphctl.curxmin
								&& note.start + note.duration <= c.patterngraphctl.curxmax) {
							g.fill3DRect(
									(int) (x0 + xscale
											* (note.start - c.patterngraphctl.curxmin)),
									(int) (y0 - cheight / 2 - (note.cents - c.patterngraphctl.curymin)
											* yscale),
									(int) (note.duration * xscale), cheight,
									true);
							g.setColor(Color.black);
							g.drawRect((int) (x0 + xscale
											* (note.start - c.patterngraphctl.curxmin)),
									(int) (y0 - cheight / 2 - (note.cents - c.patterngraphctl.curymin)
											* yscale),
									(int) (note.duration * xscale), cheight);
						} else if (note.start + note.duration > c.patterngraphctl.curxmin
								&& note.start < c.patterngraphctl.curxmin) {
							g.fill3DRect(
									x0,
									(int) (y0 - cheight / 2 - (note.cents - c.patterngraphctl.curymin)
											* yscale),
									(int) ((note.duration - (c.patterngraphctl.curxmin - note.start)) * xscale),
									cheight, true);
							g.setColor(Color.black);
							g.drawRect(
									x0,
									(int) (y0 - cheight / 2 - (note.cents - c.patterngraphctl.curymin)
											* yscale),
									(int) ((note.duration - (c.patterngraphctl.curxmin - note.start)) * xscale),
									cheight);
						} else if (note.start < c.patterngraphctl.curxmax
								&& note.start + note.duration > c.patterngraphctl.curxmax) {
							g.fill3DRect(
									(int) (x0 + (note.start - c.patterngraphctl.curxmin)
											* xscale),
									(int) (y0 - cheight / 2 - (note.cents - c.patterngraphctl.curymin)
											* yscale),
									(int) (xscale * (c.patterngraphctl.curxmax - note.start)),
									cheight, true);
g.setColor(Color.black);
g.drawRect((int) (x0 + (note.start - c.patterngraphctl.curxmin)
											* xscale),
									(int) (y0 - cheight / 2 - (note.cents - c.patterngraphctl.curymin)
											* yscale),
									(int) (xscale * (c.patterngraphctl.curxmax - note.start)),
									cheight);
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

			//mousehintcomp.setBounds(0, 0, getWidth(), getHeight());

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
		xscale = ((w - (PADleft + PADright)) / (float) (c.patterngraphctl.curxmax - c.patterngraphctl.curxmin));
		yscale = ((h - (PADup + PADdown)) / (float) (c.patterngraphctl.curymax - c.patterngraphctl.curymin));

	}

	public void setcurx(int ms) {
		
		if ((c.patterngraphctl.curxmax - ms) < 1000) {
			curx = ms;
			int adv = c.patterngraphctl.curxmax - curx;
			if (adv > 0) {
				c.patterngraphctl.curxmin += 50 * 1000 / adv;
				c.patterngraphctl.curxmax += 50 * 1000 / adv;
			} else {
				int delta = c.patterngraphctl.curxmax - c.patterngraphctl.curxmin;
				c.patterngraphctl.curxmax = curx + 4000;
				c.patterngraphctl.curxmin = c.patterngraphctl.curxmax - delta;
			}
			repaint();}
		else if( ms<c.patterngraphctl.curxmin){
			int delta = c.patterngraphctl.curxmax - c.patterngraphctl.curxmin;
			c.patterngraphctl.curxmin = ms;
			c.patterngraphctl.curxmax = ms+delta;
			repaint();
		}
		else {
			int lastpos=(int) ((curx - c.patterngraphctl.curxmin) * xscale + PADleft);
			curx = ms;
			int curpos = (int) ((curx - c.patterngraphctl.curxmin) * xscale + PADleft);
		 int width = Math.abs(curpos-lastpos)<2? 2 : Math.abs(curpos-lastpos);
			repaint(Math.min(curpos, lastpos) , 0,2*width,h);
			
			
		}
		
	}

	public double getcurx() {
		return curx / 1000.0;
	}

	public void setzoom(int x, int y, int h, int w) {
		int xmint = (int) ((x - PADleft) / (float) xscale + c.patterngraphctl.curxmin);
		int ymaxt = (int) ((this.getHeight() - y - PADdown) / (float) yscale + c.patterngraphctl.curymin);
		int xmaxt = (int) (xmint + w / (float) xscale);
		int ymint = (int) (ymaxt - h / (float) yscale);

		if (ymaxt - ymint > 1 && xmaxt - xmint > 25) {
			c.patterngraphctl.curxmin = xmint;
			c.patterngraphctl.curymin = ymint;
			c.patterngraphctl.curxmax = xmaxt;
			c.patterngraphctl.curymax = ymaxt;
		}

	}

	public void zoom(Point center, int direction) {
		if (direction > 0) {
			center.x = getWidth() / 2;
			center.y = (getHeight() / 2);
		}
		int xmint = (int) (c.patterngraphctl.curxmin - ((getWidth() / 2) - center.x)
				/ (16.0 * xscale) - direction
				* (c.patterngraphctl.curxmax - c.patterngraphctl.curxmin) / 106.0);
		int xmaxt = (int) (c.patterngraphctl.curxmax - ((getWidth() / 2) - center.x)
				/ (16.0 * xscale) + direction
				* (c.patterngraphctl.curxmax - c.patterngraphctl.curxmin) / 106.0);

		if (xmaxt - xmint > 25) {

			if (xmint >= 0) {
				c.patterngraphctl.curxmin = xmint;
			} else
				c.patterngraphctl.curxmin = 0;

			if (xmaxt <= c.patterngraphctl.xmax) {
				c.patterngraphctl.curxmax = xmaxt;
			} else
				c.patterngraphctl.curxmax = c.patterngraphctl.xmax;

			// auto zoom y
			int ymint = c.getMinValue(c.patterngraphctl.curxmin, c.patterngraphctl.curxmax,
					true);
			int ymaxt = c.getMaxValue(c.patterngraphctl.curxmin, c.patterngraphctl.curxmax,
					true);

			if (ymaxt - ymint > 1) {
				if (ymint >= c.patterngraphctl.ymin) {
					c.patterngraphctl.curymin = ymint - 100;
				} else
					c.patterngraphctl.curymin = c.patterngraphctl.ymin;

				if (ymaxt <= c.patterngraphctl.ymax) {
					c.patterngraphctl.curymax = ymaxt + 100;
				} else
					c.patterngraphctl.curymax = c.patterngraphctl.ymax;

			}

		}

	}

	// Listeners
	@Override
	public void mouseClicked(MouseEvent e) {

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
			curx = (int) ((xinit - PADleft) / (xscale) + c.patterngraphctl.curxmin);
			c.w.partitionpanel.part.setcurx(curx);
			c.m.mp.setCurrentTime((xinit - PADleft) / (1000.0 * xscale) + 1.0
					* c.patterngraphctl.curxmin / 1000.0);

		} else if (currenth < 0) {
			c.patterngraphctl.curxmin = c.patterngraphctl.xmin;
			c.patterngraphctl.curymin = c.patterngraphctl.ymin;
			c.patterngraphctl.curxmax = c.patterngraphctl.xmax;
			c.patterngraphctl.curymax = c.patterngraphctl.ymax;
		}
		currentw = 0;
		currenth = 0;
		xinit = 0;
		yinit = 0;
		c.patterngraphctl.repaint();
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
		c.patterngraphctl.repaint();

	}

}
