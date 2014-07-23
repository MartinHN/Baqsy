package baqsi.utils;
import java.io.File;
import java.io.IOException;

import javax.sound.midi.*;

import baqsi.model.Audio;
import baqsi.model.Note;
import baqsi.model.Partition;
import baqsi.model.Pitchs;


public class MidiUtils {
	static Sequence sequence;

	static Track track;

	static final int cc = 0;
	static final int tempo = 120;
	static final int velocity = 100;
	static final int NOTEON = 144;
	static final int NOTEOFF = 128;
	static final int PB = 0xE0;

	public static void createMidiPart(Audio a, String pathname, boolean PBOn) {
		track = null;
		sequence = null;
		try {
			sequence = new Sequence(Sequence.PPQ, 10);
		} catch (InvalidMidiDataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		track = sequence.createTrack();
		int resolution = (int) (sequence.getResolution() * (tempo / 60.0));

		Partition pbuf = a.getPartition();

		/**
		 * given 120 bpm: (120 bpm) / (60 seconds per minute) = 2 beats per
		 * second 2 / 1000 beats per millisecond (2 * resolution) ticks per
		 * second (2 * resolution)/1000 ticks per millisecond, or (resolution /
		 * 500) ticks per millisecond ticks = milliseconds * resolution / 500
		 */

		for (int k = 0; k < pbuf.size(); k++) {

			try {

				Note nbuf = pbuf.get(k);
				ShortMessage messon = new ShortMessage();
				int curnote = (nbuf.cents + 50) / 100;
				messon.setMessage(NOTEON, curnote, velocity);
				long tickson = (int) (nbuf.start * resolution / 1000.0);
				track.add(new MidiEvent(messon, tickson));
				long ticksoff = (int) ((nbuf.start + nbuf.duration)
						* resolution / 1000.0);

				if (PBOn) {
					Pitchs pitchbuf = a.pitch;
					for (int j = (int) tickson + 1; j < ticksoff; j++) {
						ShortMessage pbmess = new ShortMessage();
						int pitchstart = (int) (j * pitchbuf.pitchfs / resolution);
						long pbvalue = 8192 + (pitchbuf.get(pitchstart) - curnote * 100) * 8192 / 200;

						pbmess.setMessage(PB, (int) pbvalue & 0x7F,
								(int) (pbvalue >> 7) & 0x7F);
						track.add(new MidiEvent(pbmess, j));
					}
					ShortMessage pbmess = new ShortMessage();
					long pbvalue = 8192;
					pbmess.setMessage(PB, (int) pbvalue & 0x7F,
							(int) (pbvalue >> 7) & 0x7F);
					track.add(new MidiEvent(pbmess, ticksoff + 1));
				}

				ShortMessage messoff = new ShortMessage();
				messoff.setMessage(NOTEOFF, (nbuf.cents + 50) / 100, 0);
				track.add(new MidiEvent(messoff, ticksoff));

			} catch (InvalidMidiDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		try {

			File file = new File(pathname);
			if (!file.exists()) {
				file.createNewFile();

			}
			saveMidiFile(file);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void saveMidiFile(File file) {
		try {
			int[] fileTypes = MidiSystem.getMidiFileTypes(sequence);
			if (fileTypes.length == 0) {
				System.out.println("Can't save sequence");
			} else {
				if (MidiSystem.write(sequence, fileTypes[0], file) == -1) {
					throw new IOException("Problems writing to file");
				}
			}
		} catch (SecurityException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
