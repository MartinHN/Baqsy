package baqsi.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;


import javax.imageio.ImageIO;
import javax.swing.JPanel;

import baqsi.controllers.Controllers;
import baqsi.model.Partition;
import baqsi.model.Note;




//sharp + flat preference
@SuppressWarnings("serial")
public class AudioPartition extends JPanel implements MouseListener {

	Controllers c;

	private BufferedImage gkey;
	// private BufferedImage note;
	private BufferedImage sharp;
	private BufferedImage sharp34;
	private BufferedImage sharp14;
	// private BufferedImage flat;

	final int ystart = 23;
	final int yint = 11;
	final int gkeyh = 88;
	final int gkeyw = 32;

	final int noteh = yint - 3;
	final int notew = yint + 2;

	final int alth = 20;
	final int altw = 7;

	int innerwinsize = 400;

	boolean colored = true;

	double xscale = 200 / 3000.0;
	int curx = gkeyw;

	public AudioPartition(Controllers c) {
		this.c = c;
		try {
			gkey = ImageIO.read(getClass().getResource("Gkey.png"));
			
			sharp = ImageIO.read(getClass().getResource("sharp.png"));
			sharp14 = ImageIO.read(getClass().getResource("sharp14.png"));
			sharp34 = ImageIO.read(getClass().getResource("sharp34.png"));

			// flat = ImageIO.read(getClass().getResource("flat.png"));

		} catch (IOException ex) {
			System.out.println("Erreur img");
		}

		setMinimumSize(new Dimension(100, 100));
		setPreferredSize(new Dimension(400, 100));
		addMouseListener(this);
	}

	@Override
	public void paintComponent(Graphics g2) {
		super.paintComponents(g2);
		Graphics2D g = (Graphics2D) g2;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.clearRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());

		g.setColor(Color.black);
		g.drawImage(gkey, 0, getHeight() / 2 - gkeyh / 2, null);

		g.setColor(Color.black);

		for (int k = 0; k < 5; k++) {
			g.drawLine(0, getHeight() / 2 + gkeyh / 2 - (ystart + k * yint),
					getWidth(), getHeight() / 2 + gkeyh / 2
							- (ystart + k * yint));
		}
		g.setColor(Color.red);
		g.drawLine(curx, 0, curx, getHeight());

		for (int j = 0; j < c.getViewablePartition().size(); j++) {
			if (c.getViewableAudioList().get(j).isSelectedForPartition) {
				ArrayList<Note> aul = c.getViewablePartition().get(j).notelist;
				Color col = c.getViewableAudioList().get(j).color;

				for (int k = 0; k < aul.size(); k++) {

					int x = gkeyw + (int) (aul.get(k).start * xscale);
					int curcnt = aul.get(k).cents;

					// g.drawImage(ImagesetColors(note,
					// coll.get(k)),x,ctstopix(scl.get(k))-noteh/2,null);

					g.setPaint(col);
					g.rotate(-0.3, x, ctstopix(curcnt));
					g.fillOval(x, ctstopix(curcnt) - noteh / 2, notew, noteh);
					g.rotate(0.3, x, ctstopix(curcnt));
					g.setPaint(Color.black);
					if ((curcnt + 50) / 100 <= 48) {
						for (int l = ctstopix(4800) + yint / 4; l < ctstopix(curcnt) + 1; l = l
								+ yint) {
							g.drawLine(x - 2, l, x + notew + 1, l);
						}
					} else if (curcnt > 6875) {
						for (int l = ctstopix(6900); l > ctstopix(curcnt) - 1; l = l
								- yint) {

							g.drawLine(x - 2, l - 3, x + notew + 1, l - 3);
						}

					}

					switch (((curcnt + 25) / 50) % 24) {
					case 1:
					case 5:
					case 9:
					case 11:
					case 15:
					case 19:
					case 23:
						g.drawImage(ImagesetColors(sharp14, col), x - altw / 3,
								ctstopix(curcnt) - alth / 2, null);
						break;
					case 2:
					case 6:
					case 12:
					case 16:
					case 20:
						g.drawImage(ImagesetColors(sharp, col), x - altw,
								ctstopix(curcnt) - alth / 2, null);

						break;
					case 3:
					case 7:
					case 13:
					case 17:
					case 21:
						g.drawImage(ImagesetColors(sharp34, col), x - altw,
								ctstopix(curcnt) - alth / 2, null);
						break;

					default:
						break;
					}

					if (k == aul.size() - 1)
						innerwinsize = Math.max(innerwinsize, x + notew);

				}
			}

			if (j == c.getPartitions().size() - 1) {

				setPreferredSize(new Dimension(innerwinsize, 100));
				revalidate();
			}
		}

	}

	public void setcurx(int ms) {
		if(ms>0){
		curx = (int) (gkeyw + ms * xscale);
		Rectangle r = new Rectangle((int) ((ms) * xscale), 0,
				(int) (1000 * xscale), getHeight());
		scrollRectToVisible(r);
		repaint();
		}
	}

	// convert cts into pixel on Y axis in the partition

	private int ctstopix(int cts) {
		// note in semitones starting from C3
		int note = (cts + 25) / 100 - 48;

		// octave start height ( Octave number * drawing interval of an octave)
		int hht = (int) ((note / 12) * (3.5 * yint));
		if (note < 0)
			hht -= 3.5 * yint;
		while (note % 12 < 0) {
			note += 12;
		}

		// shifts height in pixels for note > E (missing semitone)
		int a = 0;
		if (note % 12 > 4) {
			a++;

		}

		// rounding height to nearest semitones (alterations are drawn in
		// Paintcomponent method) and convert into pixel numbers
		hht += Math.ceil(((note % 12) + a) / 2) * yint / 2 - yint / 4;

		int pix = (getHeight() / 2 + gkeyh / 2 - (ystart - yint + hht));

		// errors...
		if (pix < 0 || pix > getHeight())
			pix = 0;
		return pix;

	}

	public BufferedImage ImagesetColors(BufferedImage im, Color c) {
		if (colored) {
			WritableRaster rast = im.copyData(null);
			BufferedImage res = new BufferedImage(im.getColorModel(), rast,
					im.isAlphaPremultiplied(), null);
			for (int i = 0; i < im.getWidth(); i++) {
				for (int j = 0; j < im.getHeight(); j++) {
					int argb = im.getRGB(i, j);
					int alpha = (argb >> (8 * 3)) & 0xFF;

					if (alpha > 30) {
						int r = (argb >> 16) & 0xFF;

						int g = (argb >> 8) & 0xFF;

						int b = argb & 0xFF;

						if (Math.abs((r + g + b) / 3.0 - r) < 60 && r < 100) {
							res.setRGB(i, j, c.getRGB());
						}
					}
				}

			}
			return res;
		} else
			return im;

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		c.w.graphw.setcurx((int) ((arg0.getPoint().x-gkeyw )/ xscale));
		setcurx((int)( (arg0.getPoint().x-gkeyw )/ xscale));
	}
}
