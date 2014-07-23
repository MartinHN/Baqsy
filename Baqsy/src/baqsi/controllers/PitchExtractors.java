package baqsi.controllers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import java.util.Observable;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import baqsi.model.Audio;
import baqsi.model.Pitchs;
import baqsi.utils.Utils;




// retourne un indicateur de véracité
// reglage d'auto compression mpm (sample by sample ou fenetré)
// cents en float?

public class PitchExtractors extends Observable {

	Controllers c;
	// input sound

	ArrayList<Audio> list;

	// pitchs extractors
	Mpm mpm;

	boolean isRunning;
	// params

	int fs;
	// int wsize;
	double hops;
	int fmin;
	int fmax;
	
	
	final boolean printout = false;

	// result
	// int[] peaks;

	public PitchExtractors(ArrayList<Audio> l, Controllers c) {
		// a=new Audio();
		this.list = l;
		this.c = c;
		// params
		// pitch extractor parameters
		if (c.settingspanelctl.mincents == 0)
			fmin = 50;
		else
			fmin = (int) Utils.ctstof(c.settingspanelctl.mincents);
		if (c.settingspanelctl.maxcents == 0)
			fmax = 1000;
		else
			fmax = (int) Utils.ctstof(c.settingspanelctl.maxcents + 300);
		
		

		// algo type

		mpm = new Mpm();

	}

	/*
	 * 
	 * public static double[] hamming(int ws){ double[] win = new double[ws];
	 * for (int i=0;i<ws;i++){ win[i]= (double)((1-Math.cos(2*Math.PI*i/ws))/2);
	 * }
	 * 
	 * 
	 * return win; }
	 */

	public class Mpm extends SwingWorker<Integer, int[]> {

		@Override
		public Integer doInBackground() {

			int curidx = 0;

			while (curidx < list.size()) {

				double[] in = list.get(curidx).readfile(1);// c.importpanelctl.precision);
				fs = list.get(curidx).fs;
				hops = 1.0 / 2;

				int slength = in.length;

				/*
				 * 
				 * float kr=0; int ksilence =0; int envwsize = 64; // cuts
				 * sounds into parts (enveloppe > 0.05% max(envelope) ) then
				 * normalizes each parts // allow a multiscale normalization
				 * that recreate human ear dynamic adaptation
				 * 
				 * //Utils.wnormalize(in, envwsize, 0.0005);
				 * 
				 * 
				 * 
				 * 
				 * // (auto) compression... double[] env=new
				 * double[in.length/envwsize]; env = Utils.enveloppe(in,
				 * envwsize);
				 * 
				 * Arrays.sort(env); Utils.normalize(env); ksilence =
				 * Arrays.binarySearch(env, (float)0.001);
				 * 
				 * kr =Arrays.binarySearch(env, (float)0.307);//507 kr =
				 * (env.length- Math.abs(kr))/(env.length-Math.abs(ksilence));
				 * /*while (kr<0.000001){ //0.001 //float[] test = new
				 * float[in.length];
				 * 
				 * Utils.comp(in, 0.017, 2); //for(int i = 0;i<in.length;i++){
				 * // test[i]=Math.abs(in[i]); //}
				 * 
				 * env = Utils.enveloppe(in, envwsize);
				 * 
				 * Arrays.sort(env); Utils.normalize(env); ksilence =
				 * Arrays.binarySearch(env, (float)0.01); kr
				 * =Arrays.binarySearch(env, (float)0.407); kr = (env.length-
				 * Math.abs(kr))/(env.length-Math.abs(ksilence)); }
				 */

				// create window

				int taumax = (int) Math.ceil(fs / fmin);
				int taumin = (int) Math.floor(fs / fmax);
				int wsize = 2 * taumax;

				// double[] win= hamming(wsize);

				// # iteration , split in chunks to avoid too much memory space
				// on large files
				//
				int steps = (int) Math.floor((slength) / (wsize * hops) - 1
						/ hops + 1);

				double pitchfs = fs * 1.0 * steps / slength;

				int splitlength = 2000;
				int nsplit = (steps - 1) / splitlength + 1;

				int[] peaks = new int[steps];
				int[] corvalue = new int[steps];
				
				ArrayList<Integer> amp = new ArrayList<Integer>();

				// process each chunks
				for (int iter = 0; iter < nsplit; iter++) {

					// on last iteration we surely need less than splitlength
					int split = Math.min(splitlength, steps - iter
							* splitlength);

					double[][] cor = new double[split][taumax];

					double[] buf = new double[wsize];
					double[] acf = new double[taumax];
					double[] m = new double[taumax];

					for (int i = 1; i < split; i++) {

						acf = Utils.clear(acf);
						// on se place à l'endroit de l'échantillon
						int sidx = (int) Math.floor((iter * splitlength + i)
								* hops * wsize);

						buf = Utils.split(in, sidx, sidx + wsize - 1);
						// buf = Utils.multiply(win,buf);
						int rms = (int) (10.0 * Math.log( Utils.sum(Utils.multiply(buf, buf))*1.0 /wsize)/Math.log(10));
						amp.add(rms);
						//if ( rms > -43){// wsize * 0.0005) {

							Utils.normalize(buf);
							// Utils.comp(buf, 0.017, 2);

							// calculate acf and m

							acf[0] = Utils.sum(Utils.multiply(buf, buf));

							m[0] = 2 * acf[0];

							cor[i][0] = 1.0;

							for (int tau = 1; tau < taumax; tau++) {

								for (int k = 0; k < (wsize - tau); k++) {
									acf[tau] = acf[tau] + buf[k] * buf[k + tau];
								}

								acf[tau] = acf[tau] * 2.0;

								m[tau] = m[tau - 1] - buf[tau - 1]
										* buf[tau - 1];

								cor[i][tau] = acf[tau] / m[tau];
							}

							// cor[i] = Utils.multiplycst(2,acf);
							// cor[i]=Utils.divide(acf,m);
						//}

						//else {
						//	cor[i] = Utils.multiplycst(0, cor[i]);
							// System.out.println("too small"+(iter*splitlength+i)*hops*wsize/(1.0*fs));
						//}

						// set total progression percentage every percent up
						double prog = 100
								* (curidx + (i + splitlength * iter)
										/ (1.0 * steps)) / list.size();
						if ((int) prog > 0 && ((int) (10 * prog)) % 10 == 0) {

							setProgress((int) prog);

						}

					}

					// /////
					// detection des max
					// ///////

					double[] max = new double[split];
					int kidx = 0;
					int kmin = 0;

					for (int i = 0; i < split; i++) {
						max[i] = 0;

						kidx = 0;
						kmin = taumin;

						//wheight correlation
						//for (int k = 0; k < taumax; k++) {
						//	cor[i][k] = cor[i][k] * (0.9 + 0.1 * k / taumax);
						//}
						// jump over first lobe
						while (kmin < taumax && cor[i][kmin] > 0.01) {
							kmin++;
						}
						while (kmin < taumax && cor[i][kmin] < 0.01) {
							kmin++;
						}
						if (kmin != taumin && kmin != taumax) {

							for (int k = kmin; k < taumax; k++) {

								if (cor[i][k] > max[i]) {
									max[i] = cor[i][k];
									kidx = k;
								}

							}

							if (interpolMaxParabol(cor[i], kidx, 2) != null) {
								peaks[iter * splitlength + i] = Utils
										.ftocts(1.0
												* fs
												/ interpolMaxParabol(cor[i],
														kidx, 2)[0]);
								corvalue[iter * splitlength + i] = (int) (1000.0*interpolMaxParabol(cor[i], kidx, 2)[1]);

								// detects strong sub harmonics

								if (interpolMaxParabol(cor[i], kidx * 2, 2) != null
										&& cor[i][kidx * 2] > max[i] * 0.92) {
									if (interpolMaxParabol(cor[i], 2 * kidx, 2)[1] > max[i] * 0.99) {
										peaks[iter * splitlength + i] = Utils
												.ftocts(1.0
														* fs
														/ interpolMaxParabol(
																cor[i],
																2 * kidx, 2)[0]);
										corvalue[iter * splitlength + i] = (int)(1000.0*interpolMaxParabol(cor[i],
												2 * kidx, 2)[1]);
										if(printout)System.out
												.println("subharm t :"
														+ ((iter * splitlength + i) / pitchfs));
									}

								}

							}

							else {
								
								corvalue[iter * splitlength + i] = 0;
								if(printout)System.out.println("erreur interpolation t = "
										+ ((iter * splitlength + i) / pitchfs));
							}

						}

					}
					// else System.out.println("nofirstlobe t:"+
					// ((iter*splitlength + i) / pitchfs));

				/*	// detects weak correlation coeffs
					double last = peaks[iter * splitlength];
					for (int k = 1; k < split; k++) {
						int currentcts = peaks[iter * splitlength + k];
					if(currentcts>0)	{
					if (Math.abs(last-currentcts)>1000&&max[k] < ((currentcts / 7000.0) * 0.6 + 0.2)){ // 7000.0 o.6 0.2
							peaks[iter * splitlength + k] = 0;

					}
					//else if(max[k] < ((currentcts / 7000.0) * 0.5 + 0)){
						//peaks[iter * splitlength + k] = 0;
					//}
					}
					
					last = currentcts;
					}*/

					// detection de faux maximums
					int maxwsize = (int) (pitchfs * 2);

					for (int l = maxwsize / 2; l < split - maxwsize / 2; l++) {
						// temps moyen de correlation
						double meanp = Utils.meanWithoutZerosCentered(peaks,
								iter * splitlength + l, maxwsize);// (peaks,iter*splitlength+l-maxwsize/2
																	// ,
																	// iter*splitlength+l+maxwsize/2));

						if (corvalue[iter * splitlength + l] > 0
								&& Math.abs(peaks[iter * splitlength + l]
										- meanp) > 900) {// &&peaks[iter*splitlength+l-1]!=0
															// &&
															// Math.abs(peaks[iter*splitlength+l]-peaks[iter*splitlength+l-1])>500){
							// si un autre maximum est proche de la hauteur
							// moyenne calculée on choisi celui ci
							// max proche de hauteur moyenne
							double maxbuf = 0;
							int maxbufidx = 0;

							// meanf frequence moyenne

							double meanf = Utils.ctstof((int) meanp);
							// maximum de correlation aux alentours de la
							// fréquence moyenne
							for (int k = -5; k <= 5; k++) {
								int curi = (int) (fs * 1.0 / meanf + k);
								if (curi >= taumin && maxbuf < cor[l][curi]) {
									maxbuf = cor[l][curi];
									maxbufidx = curi;
								}

							}

							// si le maximum proche de la moyenne est conséquent
							// on le choisi
							if ((maxbuf *1000.0/ corvalue[iter * splitlength + l]) > 0.6) {
								if(printout)System.out.println("faux maxs:"
										+ (iter * splitlength + l) / (pitchfs));
								
								peaks[iter * splitlength + l] = Utils.ftocts(fs
										* 1.0 / maxbufidx);
								corvalue[iter * splitlength + l]=(int)(1000.0*maxbuf);

							} else {
								if(printout)System.out.println("high pitch jump error:"
										+ (iter * splitlength + l) / (pitchfs));
								//corvalue[iter * splitlength + l] /= 10;
							}
						}
					}

				}

				// peaks=Utils.errflt(peaks, 2);
				peaks = Utils.medfilt(peaks, 3);

				// ///////
				// attacks
				// /////
				// ArrayList<Double> attacks =
				// OnsetsExtractors.barkattacks(OnsetsExtractors.getSTBark(in));

				// for(int k =0 ; k<attacks.size();k++){
				// peaks[(int) (attacks.get(k)*pitchfs)] =0;

				// }

				// peaks = Utils.medfilt(peaks, 2);

				// ///////
				// pitch détécté?
				// //////

				int check = 0;

				for (int last = 0; last < peaks.length; last++) {
					check += peaks[last];
				}

				if (check != 0) {
					list.get(curidx).pitch = new Pitchs(peaks.clone(),corvalue.clone());
					list.get(curidx).pitch.pitchfs = pitchfs;
					list.get(curidx).volume.addAll(amp);
				}

				else {
					list.get(curidx).pitch = null;
					if(printout)System.out.println("pas de pitch détecté");

				}
				amp=null;
				curidx++;

			}

			return 1;
			

		}

		@Override
		protected void done() {
			setProgress(100);
			// model.list.addAll(list) ;
			c.importPitch(list);
		

			// setChanged();
			// notifyObservers();
		}

	}

	public static double[] interpolMaxParabol(double[] in, int kidx,
			int widthinterpol) {
		// parabolic interpolation

		if (kidx + widthinterpol < in.length
				&& in[kidx + widthinterpol] < in[kidx]
				&& in[kidx - widthinterpol] < in[kidx]) {
			double[] res = new double[2];

			// double d1 = (widthinterpol) * (in[kidx-widthinterpol] -
			// in[kidx]);
			// double d2 = (widthinterpol) * (in[kidx+widthinterpol] -
			// in[kidx]);
			double a = in[kidx - widthinterpol] - 2 * in[kidx]
					+ in[kidx + widthinterpol];
			double b = widthinterpol
					* (in[kidx + widthinterpol] - in[kidx - widthinterpol]);
			// = (2*kidx+widthinterpol) * d1 + (2*kidx-widthinterpol) * d2;

			// if (denominator >= 0.0)
			// {System.out.println("erreur max parabole") ;
			// return 0;}
			// /else {
			double xmax = -0.5 * b / a + kidx;

			res[0] = xmax;

			double c = 2 * in[kidx] * widthinterpol * widthinterpol;
			xmax = xmax - kidx;
			double ymax = 1 / (2.0 * widthinterpol * widthinterpol)
					* (a * xmax * xmax + b * xmax + c);
			res[1] = ymax;

			return res;

		} else {
			// System.out.println("erreur max parabole location") ;
			return null;
		}
	}

}
