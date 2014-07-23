package baqsi.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.JPanel;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import baqsi.controllers.Controllers;
import baqsi.model.Histogram;
import baqsi.utils.Utils;




// la list s'Žfface ˆ chaquefois, pour recharger tout
// ajouter suivi des couleur 

@SuppressWarnings("serial")
public class Histograph extends JPanel implements ListDataListener {

	Controllers c;

	// affichage

	int h;
	int w;

	float yscale;
	// float xscale ;

	int nticksy = 40;

	final int PADleft = 60;
	final int PADright = 0;
	final int PADup = 20;
	final int PADdown = 20;

	public Histograph(Controllers c) {
		super();
		this.c = c;

	}

	@Override
	protected void paintComponent(Graphics gin) {
		gin.clearRect(0, 0, getWidth(), getHeight());

		Graphics2D g = (Graphics2D) gin;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (c.getViewableHistogram().size() != 0) {
			initvalues();
			drawHistFrame(g);
			drawHist(g);

		}

	}

	private void initvalues() {
		h = getHeight() - (PADup + PADdown);
		w = getWidth() - (PADleft + PADright);

		yscale = (float) (1.0 * h / (c.graphctl.curymax - c.graphctl.curymin));
		// xscale = (float)(1.0*w/(maxhist*1.1));

	}

	private void drawHistFrame(Graphics2D g) {

		g.setPaint(Utils.sheetColor);
		g.fillRect(PADleft, PADup, w, h);

		g.setPaint(Color.BLACK);
		g.drawLine(PADleft, PADup, PADleft, PADup + h);
		g.drawLine(PADleft, PADup + h, PADleft + w, PADup + h);
		g.setPaint(Color.LIGHT_GRAY);
		// add ticks
		int m = 4;

		if (c.viewctl.normal) {
			int a = 0;
			while (a * 9 < nticksy) {
				m--;
				int mult = m == 3 ? 1000 : m == 2 ? 100 : m == 1 ? 50 : 10;
				int nb = (int) (Math.floor(c.graphctl.curymax * 1.0 / mult) - (Math
						.ceil(c.graphctl.curymin * 1.0 / mult)));
				for (int k = 0; k <= nb; k++) {
					int curf = (int) (mult * (Math.ceil(c.graphctl.curymin
							* 1.0 / mult) + k));
					int y = (int) (h + PADup - (curf - c.graphctl.curymin)
							* yscale);

					g.setPaint(Color.black);
					g.setFont(new Font("Arial", Font.PLAIN, 12));
					g.drawString("" + curf + "cts", 0, y + 6);
					g.setPaint(Color.lightGray);
					g.drawLine(PADleft - 2, y, w + PADleft, y);
					a++;
				}

			}
			/*
			 * for(int
			 * i=10*(int)Math.ceil(c.graphctl.curymin/10);i<10*(int)Math.
			 * floor(c.graphctl.curymax/10);i=i+10){
			 * 
			 * if(i%100==0){ int y=(int)
			 * (h+PADup-(10*Math.ceil(c.graphctl.curymin
			 * /10)-c.graphctl.curymin+i-c.graphctl.curymin)*yscale) ;
			 * 
			 * g.setPaint(Color.black); g.setFont(new
			 * Font("Arial",Font.PLAIN,12)); g.drawString(""+i+"cts", 0, y+6);
			 * g.setPaint(Color.lightGray); g.drawLine(PADleft-2, y, w+PADleft,
			 * y);
			 * 
			 * }
			 * 
			 * }
			 */
		} else if (c.viewctl.rescaled) {

			ArrayList<Integer> overwritebuf = new ArrayList<Integer>();
			for (int l = 0; l < c.getScales().size(); l++) {
				if (c.m.get(l).isSelectedForScale) {

					ArrayList<Integer> sclbuf = c.getScales().get(l).scale;
						for (int k = 0; k < c.getScales().get(l).scale.size(); k++) {

							if ( sclbuf.get(k)<c.graphctl.curymax&& sclbuf.get(k)>c.graphctl.curymin) {
								int y = (int) (h + PADup - (sclbuf.get(k) - c.graphctl.curymin)* yscale);

								if(Utils.getMinDist(y, overwritebuf)>10){
								g.setPaint(Color.black);
								g.drawString(Integer.toString(sclbuf.get(k)), 0, y);
								overwritebuf.add(y);
								}
								g.setPaint(c.getColors()[l]);
								g.drawLine(PADleft - 2, y, w + PADleft, y);

							

						}

					}
				}
			}

		}

	}

	private void drawHist(Graphics2D g) {

		int[] ypoints = new int[h + 1];
		int[] xpoints = new int[h + 1];
		for (int k = 0; k <= h; k++) {
			ypoints[k] = PADup + h - k;
		}
		int kidx = c.getCurrentselectionIdx();

		for (int i = 0; i < c.getViewableHistogram().size(); i++) {
			// double[] xpointsbuf = new double[h];

			g.setPaint(c.getViewableColors()[i]);

			Histogram hist = c.getViewableHistogram().get(i);

			xpoints[0] = PADleft;
			xpoints[h] = PADleft;
			double xscale = (w * 1.0) / (1.1 * hist.smootheddatamax);
			for (int l = 1; l < h; l++) {

				if (l / yscale + c.graphctl.curymin >= hist.smootheddatastart
						&& l / yscale + c.graphctl.curymin < hist.smootheddatastart
								+ hist.smootheddata.length) {

					xpoints[l] = PADleft
							+ (int) (xscale * hist.smootheddata[(int) (l
									/ yscale + c.graphctl.curymin - hist.smootheddatastart)]);
				} else
					xpoints[l] = PADleft;
			}

			/*
			 * for(int l=0;l<hist.size();l++){ int cents = hist.get(l).x;
			 * 
			 * for(int k=(int) Math.max(0,
			 * yscale*(cents-hist.selectivity-c.graphctl
			 * .curymin));k<Math.min(h-1,
			 * yscale*(cents+hist.selectivity-c.graphctl.curymin));k++){
			 * xpointsbuf[k]+= hist.get(l).y*Utils.hamming((int)(( k/yscale+
			 * c.graphctl.curymin)-cents), 2*hist.selectivity);
			 * 
			 * 
			 * }
			 * 
			 * } for (int j = 0; j < xpointsbuf.length; j++) {
			 * xpoints[j]=PADleft+(int)(xscale/2*xpointsbuf[j]);
			 * 
			 * }
			 */
			g.fillPolygon(xpoints, ypoints, h + 1);
		}

		if (kidx >= 0 && c.m.get(kidx).isSelectedForView) {
			g.setPaint(c.getColors()[kidx]);

			Histogram hist = c.getHistograms().get(kidx);
			xpoints[0] = PADleft;
			xpoints[h] = PADleft;
			double xscale = (w * 1.0) / (1.1 * hist.smootheddatamax);
			for (int l = 1; l < h; l++) {

				if (l / yscale + c.graphctl.curymin >= hist.smootheddatastart
						&& l / yscale + c.graphctl.curymin < hist.smootheddatastart
								+ hist.smootheddata.length)

					xpoints[l] = PADleft
							+ (int) (xscale * hist.smootheddata[(int) (l
									/ yscale + c.graphctl.curymin - hist.smootheddatastart)]);
				else
					xpoints[l] = PADleft;
			}
			g.fillPolygon(xpoints, ypoints, h + 1);
			g.setColor(Color.black);

			g.drawPolyline(xpoints, ypoints, h + 1);

		}

	}

	/*
	 * private void drawHist(Graphics2D g){ g.setPaint(Color.black); float
	 * valuebuf = 0;
	 * 
	 * 
	 * 
	 * for(int i=0;i<c.getSelectedHistogram().size();i++){
	 * g.setPaint(c.getSelectedColors()[i]);
	 * 
	 * Histogram histbuf = c.getSelectedHistogram().get(i); int selectivity =
	 * histbuf.selectivity; int[] xpoints = new int[histbuf.size()*3]; int[]
	 * ypoints = new int[histbuf.size()*3];
	 * 
	 * 
	 * valuebuf = histbuf.get(0).x; int k=0; int v=1; //premierspoints du
	 * polygone xpoints[0] = PADleft; ypoints[0] = (int)(PADup+h+selectivity-(
	 * 1.0*yscale*(valuebuf-c.graphctl.curymin)));
	 * 
	 * while(k<histbuf.size()){
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * if(Math.abs(histbuf.get(k).x-valuebuf)<selectivity){
	 * 
	 * //frŽquence / note ypoints[v]=(int) (PADup+h-( 1.0*yscale *
	 * (histbuf.get(k).x-c.graphctl.curymin)));
	 * 
	 * // weight xpoints[v] = (int) (PADleft + 1.0*xscale*histbuf.get(k).y);}
	 * 
	 * else{ // return to Zero ypoints[v] = (int)(PADup+h-1-(
	 * 1.0*yscale*(valuebuf-c.graphctl.curymin))); xpoints[v]=PADleft; v++;
	 * ypoints[v] = (int)(PADup+h + 1 -(
	 * 1.0*yscale*(histbuf.get(k).x-c.graphctl.curymin))); xpoints[v]=PADleft;
	 * v++; ypoints[v] = (int)(PADup+h-(
	 * 1.0*yscale*(histbuf.get(k).x-c.graphctl.curymin))); xpoints[v]=(int)
	 * (PADleft + 1.0*xscale*histbuf.get(k).y);
	 * 
	 * 
	 * 
	 * }
	 * 
	 * valuebuf = histbuf.get(k).x; k++; v++; }
	 * 
	 * xpoints[v] = PADleft; ypoints[v] = ypoints[v-1]-3; g.fillPolygon(xpoints,
	 * ypoints,v+1); g.setPaint(Color.black); //g.drawPolyline(xpoints,
	 * ypoints,v+1 ); }
	 * 
	 * 
	 * 
	 * }
	 */

	@Override
	public void contentsChanged(ListDataEvent e) {

		repaint();
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		// TODO Auto-generated method stub

		repaint();
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		// TODO Auto-generated method stub

	}

}
