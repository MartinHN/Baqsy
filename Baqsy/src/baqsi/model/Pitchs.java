package baqsi.model;
import java.io.Serializable;
import java.util.Arrays;

public class Pitchs implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4779969297943047545L;
	public int[] viewablevalues;
	private int[] values;
	public int[] strength;
	public double pitchfs;

	public Pitchs() {

	}

	public Pitchs(int[] val,int[] cor) {
		values = val;
		viewablevalues = new int[val.length];
		strength = cor;
	}
	public int[] getValues(){
		return values;
	}

public void setCorThresh(double thresh){
		for(int k=0;k<viewablevalues.length;k++){
			viewablevalues[k] =   strength[k]/1000.0>thresh+0.2*values[k]/7000.0? values[k]:0;
		}
		//constraints(0.05, 0.05);
		
	}


	
	public void constraints(double mintime, double maxtime) {

		int[] sorted = viewablevalues.clone();
		Arrays.sort(sorted);

		int kidx = 0;
		while (sorted[kidx] == 0) {
			kidx++;
		}
		int last = sorted[kidx];
		int blob = 0;
		int current = 0;
		int min = 0;
		int max = 0;
		boolean ok = false;
		for (int k = kidx + 1; k < sorted.length; k++) {
			if (sorted[k] - last < 150) {
				blob++;
			} else {
				if (blob > mintime * pitchfs) {
					min = sorted[kidx + current];
					ok = true;
					break;
				}

				else {
					current += blob;
					blob = 0;
				}
			}

			last = sorted[k];
		}
		if (!ok) {
			min = 0;
		}
		last = sorted[sorted.length - 1];
		blob = 0;
		current = 0;
		ok = false;
		for (int k = sorted.length - 2; k > kidx; k--) {
			if (last - sorted[k] < 150) {
				blob++;
			} else {
				if ( blob > maxtime * pitchfs) {
					max = sorted[sorted.length - 1 - current];
					ok = true;
					break;
				}

				else {
					current += blob;
					blob = 0;
				}
			}

			last = sorted[k];
		}
		if (!ok) {
			max = 9999999;
		}
		// int min = sorted[(int) (minpercentage*(sorted.length-kidx))+kidx];
		// int max = sorted[(int)
		// ((1.0-maxpercentage)*(sorted.length-kidx))+kidx];

		for (int i = 0; i < viewablevalues.length; i++) {
			if (viewablevalues[i] > max || viewablevalues[i] < min)
				viewablevalues[i] = 0;
		}
	}

	public int size() {
		return viewablevalues.length;
	}

	public int get(int i) {
		return viewablevalues[i];
	}
}
