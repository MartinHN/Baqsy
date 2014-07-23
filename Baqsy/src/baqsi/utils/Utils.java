package baqsi.utils;
import java.awt.Color;

import java.util.ArrayList;
import java.util.Arrays;

//add fs(static??)..,scaleb

public class Utils {

	// variables globales

	public final static Color bgColor = Color.lightGray;
	public final static Color sheetColor = Color.white;//new Color(0.9608F, 0.9609F, 0.8478F);
	public final static Color ticksColor = new Color(1, .9F, .5F);

	public final static Color[] audiocolors = {
			// new Color(Color.HSBtoRGB((float).1, (float).7, (float).8)),
			// new Color(Color.HSBtoRGB((float).2, (float).7, (float).8)),
			// new Color(Color.HSBtoRGB((float).3, (float).7, (float).8)),

			//new Color(1.F, 0.52922F, .1922F), new Color(0.F, .1725F, .9098F),
			//new Color(.9882F, 0.F, .4196F), new Color(0.F, .7569F, .3490F),

			//new Color(Color.HSBtoRGB((float) .5, (float) .7, (float) .8))
		
	new Color(27,	121,	180), new Color(254,	126,	15), new Color(46,	158,	45), new Color(212	,39,	40), new Color(145,	103,	186),
	new Color(139,	84,	77), new Color(223,	120,	193), new Color(128,	128,	128), new Color(183,	189,	35), new Color(26,	189,	208)

	
	
	
	
	};
	
	public final static Color[] patterncolors = {
		new Color(Color.HSBtoRGB((float).1, (float).7, (float).8)),new Color(Color.HSBtoRGB((float).2, (float).7, (float).8)),new Color(Color.HSBtoRGB((float).3, (float).7, (float).8)),
		//new Color(0x5A1F00),	new Color(0xD1570D), new Color(0xFDE792), new Color(0x477725), new Color(0xA9CC66)
		 				

		//new Color(1.F, 0.52922F, .1922F), new Color(0.F, .1725F, .9098F),
		//new Color(.9882F, 0.F, .4196F), new Color(0.F, .7569F, .3490F),

		//new Color(Color.HSBtoRGB((float) .5, (float) .7, (float) .8)) 
		};
	
	

	// table processing

	static public void comp(double[] in, double thresh, int ratio) {

		for (int i = 0; i < in.length; i++) {
			if (Math.abs(in[i]) > thresh) {
				in[i] = Math.signum(in[i])
						* (thresh + (Math.abs(in[i]) - thresh) / ratio);
			}

		}
		normalize(in);

	}

	static public void wcomp2(double[] in, double thresh, int ratio, int wsize) {

		double[] env = enveloppe(in, wsize);

		for (int i = 0; i < env.length; i++) {
			if (Math.abs(env[i]) > thresh) {
				for (int k = 0; k < wsize; k++) {

					in[i * wsize + k] = Math.signum(in[i * wsize + k])
							* (thresh + (Math.abs(env[i]) - thresh)
									/ ratio);
				}
			}

		}
		normalize(in);

	}

	static public double[] enveloppe(double[] in, int wsize) {
		double[] res = new double[in.length / wsize];
		double envbuf = 0;

		for (int i = 0; i < in.length / wsize - 1; i++) {
			envbuf = 0;
			for (int k = 0; k < wsize; k++) {
				envbuf += in[i * wsize + k] * in[i * wsize + k];
			}
			envbuf = Math.sqrt(envbuf) / wsize;
			res[i] = envbuf;
		}

		return res;
	}

	static public void normalize(double[] in) {
		double maxsample = 0;

		for (int i = 0; i < in.length; i++) {

			maxsample = Math.max(maxsample, Math.abs(in[i]));

		}
		if (maxsample >= 0.01) {

			for (int i = 0; i < in.length; i++) {
				in[i] = in[i] / maxsample;
			}

		}

	}

	static public void wnormalize(double[] in, int w, double thresh) {

		double[] env = enveloppe(in, w);
		normalize(env);

		int start = 0;
		int length = 0;
		for (int i = 0; i < env.length - 1; i++) {
			if (env[i] > thresh) {
				if (length == 0) {
					start = i;
					length++;
				} else
					length++;
			} else {

				if (length >= 100) {
					double max = max(in, start * w, (length + start) * w);
					for (int k = 0; k < Math.min(length * w, start * w
							- in.length); k++) {

						in[start * w + k] = in[start * w + k] / max;

					}

					// System.out.println("chop :"
					// +stotime(start*w)+"   "+stotime(length*w));

				}
				length = 0;

			}

		}

	}

	static public int[] medfilt(int[] in, int wsize) {
		int[] res = in.clone();
		int w2 = (int) Math.floor(wsize / 2);
		int[] buf = new int[wsize];
		for (int i = w2; i < in.length - w2 - 1; i++) {
			for (int k = 0; k < wsize; k++) {
				buf[k] = in[i - w2 + k];
			}
			Arrays.sort(buf);
			res[i] = buf[w2];

		}
		return res;

	}

	static public int[] errflt(int[] in, int pkw) {
		int[] res = in;
		int i = 0;

		while (i < in.length - pkw) {

			if (Math.abs(res[i] - res[i + 1]) > 100 && res[i] * res[i + 1] != 0) {
				for (int l = 1; l < pkw; l++) {
					if (Math.abs(res[i] - res[i + l]) < 100) {
						for (int k = 1; k < l; k++) {
							res[i + k] = 0;
							// res[i+k]=(int)(res[i]+k*(res[i+l]-res[i])/l);
						}
						i = i + l - 1;
						break;
					}
				}

			}
			i++;
		}
		return res;
	}

	static public int max(int[] in, int x1, int x2) {

		int maxval = 0;

		if (in != null && x1 >= 0) {

			for (int i = x1; i < Math.min(in.length, x2); i++) {

				maxval = Math.max(in[i], maxval);

			}
		} else {
			System.out.println("erreur dimensionnement maxvalue");
		}
		return maxval;

	}

	static public double max(double[] in, int x1, int x2) {

		double maxval = 0;

		if (in != null && x1 >= 0) {

			for (int i = x1; i < Math.min(in.length, x2); i++) {

				maxval = Math.max(in[i], maxval);

			}
		} else {
			System.out.println("erreur dimensionnement maxvalue");
		}
		return maxval;

	}

	static public int min(int[] in, int x1, int x2) {

		int minval = 999999;

		if (in != null && x1 >= 0) {

			for (int i = x1; i < Math.min(in.length, x2); i++) {

				if (in[i] != 0)
					minval = Math.min(in[i], minval);

			}
		}

		return minval;

	}

	static public int max(int[] in) {

		int maxval = 0;

		if (in != null) {

			for (int i = 0; i < in.length; i++) {

				maxval = Math.max(in[i], maxval);

			}
		}

		return maxval;

	}

	static public int min(int[] in) {

		int minval = 999999;

		if (in != null) {

			for (int i = 0; i < in.length; i++) {

				if (in[i] != 0)
					minval = Math.min(in[i], minval);

			}
		}

		return minval;

	}

	static public double mean(int[] in, int x1, int x2) {
		double res = 0;
		for (int i = x1; i < Math.min(in.length, x2); i++) {
			res += in[i];
		}
		return (1.0 * res) / (x2 - x1);
	}

	static public double meanWithoutZeros(int[] in, int x1, int x2) {
		double res = 0;
		int k = 0;
		for (int i = x1; i < Math.min(in.length, x2); i++) {
			if (in[i] != 0) {
				res += in[i];
				k++;
			}
		}
		return (1.0 * res) / k;
	}

	static public double meanWithoutZerosCentered(int[] in, int center,
			int width) {
		if (in.length > width) {
			double res = 0;
			int i = 1;
			int k = 1;
			res = in[center];
			while (k < width && (center + i < in.length || center - i > 0)) {
				if (center + i < in.length && in[center + i] != 0) {
					res += in[center + i];
					k++;
				}
				if (center - i > 0 && in[center - i] != 0) {
					res += in[center - i];
					k++;
				}
				i++;
			}

			return res * 1.0 / k;
		} else {
			System.out.println("faute meanzerocentered");
			return in[center];
		}
	}

	static public double[] multiply(double[] t1, double[] t2) {

		if (t1.length != t2.length && t1 != null && t2 != null) {
			System.err.println("incorrect array multiplication");
			return null;
		} else {
			double[] res = new double[t2.length];
			for (int i = 0; i < t1.length; i++) {
				res[i] = t1[i] * t2[i];
			}
			return res;
		}

	}

	static public double[] split(double[] t, int min, int max) {
		if (min < max && min >= 0 && max <= t.length) {
			double[] res = new double[max - min + 1];
			for (int i = min; i <= max; i++) {
				res[i - min] = t[i];
			}
			return res;
		} else {
			System.err.println("incorrect array split");
			return null;

		}

	}

	static public double sum(double[] t) {
		if (t == null) {
			System.err.println("empty array sum");
			return 0;
		} else {
			double sum = 0;
			for (int i = 0; i < t.length; i++) {
				sum = sum + t[i];
			}
			return sum;
		}

	}

	static public double[] multiplycst(int k, double[] t) {
		if (t == null) {
			System.err.println("empty array for multiplying by constant");
			return null;
		} else {
			double[] res = new double[t.length];
			for (int i = 0; i < t.length; i++) {
				res[i] = t[i] * k;
			}
			return res;
		}
	}

	static public double[] divide(double[] t1, double[] t2) {

		if (t1.length != t2.length && t1 != null && t2 != null) {
			System.err.println("incorrect array multiplication");
			return null;
		} else {
			double[] res = new double[t2.length];
			for (int i = 0; i < t1.length; i++) {
				if (t2[i] != 0)
					res[i] = t1[i] / t2[i];
				else
					res[i] = 9999999;
			}
			return res;
		}

	}
	
	static public int getMinDist(int a,ArrayList<Integer> tin){
		int distbuf = 99999999;
		for (int j = 0 ; j< tin.size();j++){
			distbuf = Math.min(distbuf, Math.abs(a-tin.get(j)));
		}
		return distbuf;
	}

	static public double[] clear(double[] t) {
		if (t != null) {
			for (int i = 1; i < t.length; i++) {
				t[i] = 0;
			}
			return t;
		} else {
			return null;
		}
	}

	// functions
	static public double nOctaves(int fmin, int fmax) {

		return (Math.log10(fmax / fmin) / Math.log10(2));
	}

	static public int ftocts(double f) {
		return (int) (100.0 * (69.0 + 12.0 * Math.log10(f / 440.0)
				/ Math.log10(2)));
	}

	static public double ctstof(int cts) {
		return 440.0 * (Math.pow(2, (cts - 6900) / 1200.0));
	}

	static public String ctsToString(int cts) {
		String res = new String();

		switch (((cts + 49) / 100) % 12) {
		case 0:
			res = res.concat("Do");

			break;

		case 1:
			res = res.concat("Do#");
			break;
		case 2:
			res = res.concat("Re");
			break;

		case 3:
			res = res.concat("Re#");
			break;

		case 4:
			res = res.concat("Mi");
			break;

		case 5:
			res = res.concat("Fa");
			break;
		case 6:
			res = res.concat("Fa#");
			break;

		case 7:
			res = res.concat("Sol");
			break;
		case 8:
			res = res.concat("Sol#");
			break;
		case 9:
			res = res.concat("La");
			break;
		case 10:
			res = res.concat("La#");
			break;
		case 11:
			res = res.concat("Si");
			break;

		default:

			break;
		}
		res = res.concat(Integer.toString((cts + 50) / 1200 - 1));
		if (cts % 100 != 0) {
			if (cts % 100 > 50)
				res = res.concat(" " + Integer.toString((cts % 100) - 100));
			else if (cts % 100 <= 50)
				res = res.concat(" +" + Integer.toString(cts % 100));
		}

		return res;
	}

	// Others
	
	

	public static double[] hamming(int ws) {
		double[] win = new double[ws];
		for (int i = 0; i < ws; i++) {
			win[i] = ((1 - Math.cos(2 * Math.PI * i / ws)) / 2);
		}

		return win;
	}

	public static double hamming(int x, int ws) {
		double res = ((1 + Math.cos(2 * Math.PI * x / ws)) / 2);
		return res;
	}

}
