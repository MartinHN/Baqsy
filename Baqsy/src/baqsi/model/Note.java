package baqsi.model;

import java.io.Serializable;


	public class Note implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8288467480252749848L;
		public int cents;
		public int duration;
		public int start;

		public Note(int s, int f, int d) {
			start = s;
			cents = f;
			duration = d;

		}
		
		public int getNoteOff(){
			return duration+start;
		}

	

}
