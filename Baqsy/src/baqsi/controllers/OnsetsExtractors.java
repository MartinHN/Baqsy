package baqsi.controllers;
import java.util.ArrayList;

import baqsi.utils.Utils;


import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

//Ajouter IA avec kurtosis
public class OnsetsExtractors {

	static final int wsize = 256;
	static final int fftwsize = 2 * wsize;
	static DoubleFFT_1D fft = new DoubleFFT_1D(fftwsize);

	static final int fs = 22050;

	static final int[] barkedges = { 0, 100, 200, 300, 400, 510, 630, 770, 920,
			1080, 1270, 1480, 1720, 2000, 2320, 2700, 3150, 3700, 4400, 5300,
			6400, 7700, 9500, 12000, 15500 };
	static final int[] loudnessfreq = { 20, 25, 31, 40, 50, 63, 80, 100, 125,
			160, 200, 250, 315, 400, 500, 630, 800, 1000, 1250, 1600, 2000,
			2500, 3150, 4000, 5000, 6300, 8000, 10000, 12500 };
	static final double[] loudnesswheight = { -69.9, -60.4, -51.4, -43.3,
			-36.6, -30.3, -24.3, -19.5, -14.8, -10.7, -7.5, -4.8, -2.6, -0.8,
			0.0, 0.6, 0.5, 0.0, -0.1, 0.5, 1.5, 3.6, 5.9, 6.5, 4.2, -2.6,
			-10.2, -10.0, -2.8 };
	static final boolean loudness = true;
	static final double hops = 0.25;

	static final double threshold = 5;
	static final int debounce = (int) ((0.01 * fs) / (wsize * hops));

	// fft

	public static double[] getMag(double[] a) {
		double[] buf = new double[fftwsize];
		for (int k = 0; k < a.length; k++) {
			buf[k] = a[k];
		}
		fft.realForward(buf);

		double[] res = new double[wsize];
		res[0] = 0;
		for (int k = 1; k < wsize; k++) {

			res[k] = buf[2 * k] * buf[2 * k] + buf[2 * k + 1] * buf[2 * k + 1];
		}

		return res;

	}

	public static double bintof(int b) {
		return b * fs / (fftwsize);
	}

	public static int ftobin(double f) {
		return (int) (f * fftwsize / fs);
	}

	public static ArrayList<Double> barkattacks(double[][] b) {
		int k = 1;
		double[] growthbuf = new double[barkedges.length];
		double[] mean = b[0];
		int length = 0;
		double[] newone = b[1];
		int deb = 0;

		ArrayList<Double> res = new ArrayList<Double>();
		while (k < b.length) {
			if (deb < debounce)
				deb++;
			else {
				length++;
				newone = b[k];

				if (length == 1)
					mean = b[k];

				if (length > 2) {
					for (int j = 0; j < barkedges.length; j++) {
						mean[j] = (0.9 * mean[j] * (length - 2) + b[k - 1][j])
								/ (0.9 * (length - 2) + 1);
					}
				}
				// mean = b[k-1];
				int s = 0;
				for (int l = 0; l < barkedges.length; l++) {
					if (newone[l] > mean[l]) {
						growthbuf[l] = newone[l] / mean[l];
						s++;
					} else
						growthbuf[l] = 0;

				}

				if (Utils.sum(growthbuf) / s > threshold) {
					res.add(k * fftwsize * hops / fs);
					length = 0;
					deb = 0;
				}

			}

			k++;
		}

		return res;

	}

	public static double getLoudness(double f) {
		if (loudness) {
			int k = 1;

			if (f <= 20)
				return 0;
			while (k <= loudnessfreq.length && f > loudnessfreq[k]) {
				k++;
			}
			if (k >= loudnessfreq.length - 1)
				return Math.pow(10, loudnesswheight[k] * 0.1);
			else {
				double dB = loudnesswheight[k] + (loudnessfreq[k] - f)
						/ (loudnessfreq[k] - loudnessfreq[k - 1])
						* (loudnesswheight[k - 1] - loudnesswheight[k]);
				return Math.pow(2, dB / 3);
			}
		} else
			return 1;
	}

	public static double getfiltercoef(int bidx, double[] a) {
		int size = 0;
		double res = 0;
		for (int k = ftobin(barkedges[bidx]); k < ftobin(barkedges[bidx + 1]); k++) {
			if (bidx + 1 > barkedges.length - 1 || k < 0 || k > a.length - 1) {

				break;
			}
			size++;
			double e = (barkedges[bidx + 1] - barkedges[bidx]) / 2;
			double c = (barkedges[bidx + 1] + barkedges[bidx]) / 2;
			res += getLoudness(bintof(k)) * a[k]
					* (1 - 0.5 * Math.abs(bintof(k) - c) / e);

		}
		if (size != 0)
			res = res / size;
		return res;
	}

	public static double[][] getSTFT(double[] a) {
		int nsteps = (int) (a.length / (fftwsize * hops) - (1 / hops - 1));
		double[][] res = new double[nsteps][wsize];
		for (int i = 0; i < nsteps; i++) {
			res[i] = getMag(Utils.multiply(
					hammingw(),
					Utils.split(a, (int) (i * fftwsize * hops), (int) (i
							* fftwsize * hops + fftwsize - 1))));

		}
		return res;
	}

	public static double[][] getSTBark(double[] a) {
		double[][] fft = getSTFT(a);
		double[][] res = new double[fft.length][barkedges.length];

		for (int k = 0; k < fft.length; k++) {
			for (int i = 0; i < barkedges.length - 1; i++) {
				res[k][i] = getfiltercoef(i, fft[k]);
			}

		}
		return res;
	}

	public static double[] hammingw() {
		double[] res = new double[fftwsize];
		for (int k = 0; k < fftwsize; k++) {
			res[k] = (1 - Math.cos(Math.PI * 2 * k / (fftwsize))) / 2;
		}
		return res;
	}

	// return onsets;

}
