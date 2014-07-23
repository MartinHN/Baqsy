package baqsi.model;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;


public class Scale implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1270202353301132587L;
	public ArrayList<Integer> scale;
	public int size=1;
	public int refcent;


	public Histogram histo;

	public Scale() {
		scale = new ArrayList<Integer>();
	
	}

	public Scale(Histogram hist) {
		this.histo = hist;
		scale = new ArrayList<Integer>();
		autosearchmax();
		getreferencecents();
	}

	public void computeScale() {
		if (scale != null)
			scale.clear();
		searchmaxidx();
		getreferencecents();
	}





	public void getreferencecents() {

		double delta = 0;
		double denominateur = 0;
		int cents = 6900;
		for (int i = 0; i < size; i++) {
			int d = (scale.get(i) + 50) % 100 - 50;
			if (Math.abs(d) < 25) {
				delta += 10 * d
						* histo.smootheddata[scale.get(i) - histo.smootheddatastart];
				denominateur += 10 * histo.smootheddata[scale.get(i)
						- histo.smootheddatastart];
			} else {
				delta += d * histo.smootheddata[scale.get(i) - histo.smootheddatastart];
				denominateur += histo.smootheddata[scale.get(i)
						- histo.smootheddatastart];
			}

		}
		if (size > 0)
			cents += (int) (delta / denominateur);

		refcent = cents;
	}

	public void autosearchmax() {

		size = 20;
		searchmaxidx();

		ArrayList<Integer> res = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {

			if (histo.smootheddata[scale.get(i) - histo.smootheddatastart] > histo.smootheddatamax / 10.0)
				res.add(scale.get(i));

		}
		size = Math.max(1, res.size());
		scale.clear();
		scale.addAll(res);

		scale.trimToSize();
		Collections.sort(scale);

	}

	public void searchmaxidx(int n) {
		size = n;
		searchmaxidx();

	}

	public void searchmaxidx() {

		scale.clear();

		boolean in = false;

		for (int i = 0; i < size; i++) {
			int maxbuf = 0;
			int kidx = 0;

			for (int k = histo.selectivity; k < histo.smootheddata.length
					- histo.selectivity; k++) {

				for (int l = 0; l < scale.size(); l++) {
					if (k + histo.smootheddatastart < scale.get(l) + histo.selectivity
							&& k + histo.smootheddatastart > scale.get(l)
									- histo.selectivity) {
						in = true;
						break;
					} else
						in = false;

				}

				if (!in) {
					if (histo.smootheddata[k - histo.selectivity] < histo.smootheddata[k]
							&& histo.smootheddata[k] > histo.smootheddata[k
									+ histo.selectivity]
							&& maxbuf < histo.smootheddata[k]) {
						maxbuf = histo.smootheddata[k];
						kidx = k;
					}
				}

			}

			if (kidx > 0)
				scale.add(kidx + histo.smootheddatastart);
			else {
				size = scale.size();
				break;
			}

		}
		scale.trimToSize();
		Collections.sort(scale);

	}

}
