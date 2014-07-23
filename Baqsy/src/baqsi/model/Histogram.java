package baqsi.model;

import java.io.Serializable;
import java.util.ArrayList;

import baqsi.utils.Utils;


public class Histogram implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -116760995072336268L;
	public ArrayList<Couple> data;
	public int[] smootheddata;
	public int smootheddatastart;
	public int smootheddatamax;
	public int selectivity;

	public Histogram() {
		data = new ArrayList<Couple>();

		selectivity = 50;
	}

	/*
	 * Histogram(int[] in){ this(); computeHistogram(in);
	 * //computesmootheddata(); getreferencecents();
	 * 
	 * }
	 */

	public Histogram(Partition in) {
		this();
		computeHistogram(in);

	}

	public static class Couple implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7070805559302987344L;
		public int x;
		public int y;

		Couple(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getx() {
			return x;
		}

		public int gety() {

			return y;
		}

	}

	/*
	 * 
	 * public void computeHistogram(int[] in){ int[] buf = in.clone(); int
	 * mean=buf[0]; int length =0;
	 * 
	 * Arrays.sort(buf); data = new ArrayList<Couple>(); for (int i
	 * =1;i<buf.length;i++){ if(buf[i]!=0){ length++;
	 * 
	 * if(length>=2){mean = (mean*(length-1) + buf[i-1])/length;} else {mean =
	 * buf[i];}
	 * 
	 * //on ne prend pas les silences
	 * if(Math.abs(buf[i]-mean)>selectivity&&mean!=0||i==buf.length-1){
	 * data.add(new Couple(mean,length)); length=0; }}
	 * 
	 * 
	 * }
	 * 
	 * }
	 */
	public void computeHistogram(Partition part) {
		if(data!=null){
			data.clear();
		}
		if (part!=null && part.get(0) != null){
		data = new ArrayList<Couple>();
		data.add(new Couple(part.get(0).cents, part.get(0).duration));

		for (int i = 1; i < part.size(); i++) {
			int cents = part.get(i).cents;
			int duration = part.get(i).duration;

			for (int k = 0; k < data.size(); k++) {

				if (cents == data.get(k).x) {
					data.get(k).y = data.get(k).y + duration;
					break;

				} else if (cents < data.get(k).x) {

					if (k == 0) {
						data.add(0, new Couple(cents, duration));
						break;
					}

					else if (data.get(k - 1).x < cents) {
						data.add(k, new Couple(cents, duration));
						break;
					}

				} else if (k == data.size() - 1) {
					data.add(new Couple(cents, duration));
					break;
				}
			}

		}

		computesmootheddata();
		}
	}

	public int maxhisty() {
		int maxbuf = 0;
		for (int i = 0; i < data.size(); i++) {
			maxbuf = Math.max(data.get(i).y, maxbuf);
		}
		return maxbuf;
	}

	public int getsum() {
		int sum = 0;
		for (int i = 0; i < data.size(); i++) {

			sum += data.get(i).y;
		}
		return sum;
	}

	public int getSmoothedValue(int cts){
		if(cts-smootheddatastart>0&&cts-smootheddatastart<smootheddata.length)return smootheddata[cts-smootheddatastart];
		else return 0;
	}
	public int size() {
		return data.size();
	}

	public Couple get(int i) {

		return data.get(i);
	}

	public int getminintegral(double thresh) {
		int min = 0;

		int threshmin = (int) (thresh * this.getsum());

		int bufsum = 0;
		for (int i = 0; i < data.size(); i++) {
			bufsum += data.get(i).y;
			if (bufsum > threshmin) {
				min = data.get(i).x;
				break;
			}

		}
		return min;
	}

	public int getmaxintegral(double thresh) {

		int max = 0;

		int threshmax = (int) ((1.0 - thresh) * this.getsum());
		int bufsum = 0;
		for (int i = 0; i < data.size(); i++) {
			bufsum += data.get(i).y;
			if (bufsum > threshmax) {
				max = data.get(i).x;
				break;
			}

		}
		return max;
	}

	public void computesmootheddata() {

		smootheddatamax = 0;

		int histfreqrange = get(data.size() - 1).x - get(0).x;
		smootheddata = new int[histfreqrange + 2 * selectivity];
		smootheddatastart = data.get(0).x - selectivity;

		// if(3*selectivity>histfreqrange) selectivity = histfreqrange/5;

		for (int i = 0; i < data.size(); i++) {
			for (int k = Math.max(smootheddatastart, data.get(i).x - selectivity); 
					k < Math.min(smootheddatastart + histfreqrange + 2 * selectivity, data.get(i).x + selectivity)
					; k++) {
				// convolve with hamming window to smooth histogram

				smootheddata[k - smootheddatastart] += data.get(i).y
						* Utils.hamming(k - data.get(i).x, 2 * selectivity);

				if (smootheddata[k - smootheddatastart] > smootheddatamax)
					smootheddatamax = smootheddata[k - smootheddatastart];

			}

		}

	}

	public void updateHistogram(Partition part) {
		computeHistogram(part);
	}

}
