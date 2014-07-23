package baqsi.ui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import baqsi.controllers.Controllers;
import baqsi.model.Audio;



// sharp + flat preference

@SuppressWarnings("serial")
public class ScalePartition extends JPanel {

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

	public ScalePartition(Controllers c) {
		this.c = c;
		try {
			gkey = ImageIO.read(getClass().getResource("Gkey.png"));
			// note = ImageIO.read(getClass().getResource("note.png"));
			sharp = ImageIO.read(getClass().getResource("sharp.png"));
			sharp14 = ImageIO.read(getClass().getResource("sharp14.png"));
			sharp34 = ImageIO.read(getClass().getResource("sharp34.png"));

			// flat = ImageIO.read(getClass().getResource("flat.png"));

		} catch (IOException ex) {
			System.out.println("Erreur img");
		}

		setMinimumSize(new Dimension(100, 100));
		setPreferredSize(new Dimension(400, 100));
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

		ArrayList<Integer> scl = new ArrayList<Integer>();
		ArrayList<Color> coll = new ArrayList<Color>();
		boolean init = true;
		for (int j = 0; j < c.getViewableAudioList().size(); j++) {
			Audio curAudio = c.getViewableAudioList().get(j);
			if (curAudio.isSelectedForScale) {
				if (init) {
					scl.addAll(c.getViewableAudioList().get(j).scale.scale);
					for (int k = 0; k < scl.size(); k++) {
						coll.add(c.getViewableAudioList().get(j).color);
					}
					init = false;
				} else {
					for (int k = 0; k < curAudio.scale.size; k++) {
						int curcnt = curAudio.scale.scale.get(k);

						for (int l = 0; l < scl.size(); l++) {

							/*
							 * if(l==0&&curcnt<scl.get(0)) {scl.add(0, curcnt);
							 * coll.add(0,curAudio.color); }
							 */
							if (l == 0) {
								if (curcnt <= scl.get(l)) {
									scl.add(l, curcnt);
									coll.add(l, curAudio.color);
									break;
								}

							}
							if (l == scl.size() - 1) {
								if (curcnt <= scl.get(l)) {
									scl.add(l, curcnt);
									coll.add(l, curAudio.color);
									break;
								}

								else {
									scl.add(curcnt);
									coll.add(curAudio.color);
									break;
								}
							}

							if (curcnt <= scl.get(l)
									&& curcnt >= scl.get(l - 1)) {
								scl.add(l, curcnt);
								coll.add(l, curAudio.color);
								break;
							}

						}
					}

				}
			}
		}

		// Scale s=c.getScales().get(j);

		for (int k = 0, x = gkeyw + notew / 2; k < scl.size(); k++, x = x + 2
				* notew) {

			// g.drawImage(ImagesetColors(note,
			// coll.get(k)),x,ctstopix(scl.get(k))-noteh/2,null);

			g.setPaint(coll.get(k));
			g.rotate(-0.3, x, ctstopix(scl.get(k)));
			g.fillOval(x, ctstopix(scl.get(k)) - noteh / 2, notew, noteh);
			g.rotate(0.3, x, ctstopix(scl.get(k)));
			g.setPaint(Color.black);
			if ((scl.get(k) + 50) / 100 <= 48) {
				for (int l = ctstopix(4800) + yint / 4; l < ctstopix(scl.get(k)) + 1; l = l
						+ yint) {
					g.drawLine(x - 2, l, x + notew + 1, l);
				}
			} else if (scl.get(k) > 6875) {
				for (int l = ctstopix(6900); l > ctstopix(scl.get(k)) - 1; l = l
						- yint) {

					g.drawLine(x - 2, l - 3, x + notew + 1, l - 3);
				}

			}

			switch (((scl.get(k) + 25) / 50) % 24) {
			case 1:
			case 5:
			case 9:
			case 11:
			case 15:
			case 19:
			case 23:
				g.drawImage(ImagesetColors(sharp14, coll.get(k)), x - altw / 3,
						ctstopix(scl.get(k)) - alth / 2, null);
				break;
			case 2:
			case 6:
			case 12:
			case 16:
			case 20:
				g.drawImage(ImagesetColors(sharp, coll.get(k)), x - altw,
						ctstopix(scl.get(k)) - alth / 2, null);

				break;
			case 3:
			case 7:
			case 13:
			case 17:
			case 21:
				g.drawImage(ImagesetColors(sharp34, coll.get(k)), x - altw,
						ctstopix(scl.get(k)) - alth / 2, null);
				break;

			default:
				break;
			}

			if (k == scl.size() - 1)
				innerwinsize = x + notew;

		}

		setPreferredSize(new Dimension(innerwinsize, 100));
		revalidate();

	}

	// convert cts into piel on Y axis in the partition

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
}
