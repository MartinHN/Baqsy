package baqsi.utils;
import java.io.File;

import javax.media.Controller;
import javax.media.ControllerAdapter;

import javax.media.CachingControlEvent;
import javax.media.Control;
import javax.media.ControllerErrorEvent;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.InternalErrorEvent;
import javax.media.Manager;
import javax.media.Player;
import javax.media.PlugInManager;
import javax.media.PrefetchCompleteEvent;
import javax.media.StartEvent;
import javax.media.StopEvent;
import javax.media.Time;
import javax.media.format.AudioFormat;
import javax.media.format.FormatChangeEvent;
import javax.swing.JOptionPane;

import baqsi.controllers.Controllers;


// supporte pas le 24bit
public class MediaPlayer {
	public Controllers c;
	private Player player;
	private Time pauseTime = new Time(0.0);
	private double totalTime = 0;
	public boolean Isplaying = false;

	public File f;
	private int stoptime=0;

	public MediaPlayer() {

		Format input1 = new AudioFormat(AudioFormat.MPEGLAYER3);
		Format input2 = new AudioFormat(AudioFormat.MPEG);

		Format output = new AudioFormat(AudioFormat.LINEAR);
		PlugInManager.addPlugIn("com.sun.media.codec.audio.mp3.JavaDecoder",
				new Format[] { input1, input2 }, new Format[] { output },
				PlugInManager.CODEC);

	}

	public void setFile(String pn) {

		if (f != null) {
			f = null;
		}
		// f =new File(pn);

	}

	public void scheduleStop(int time){
		//System.out.println(time +" stop :"+player.getStopTime().getSeconds()*1000.0+"/ now //"+player.getMediaTime().getSeconds()*1000.0);
		if(time>0){
			stoptime=time;
			
			
		}
	}
	public void play() {
		// setPan(1);
		if (c.getCurrentselection() != null) {
			f = new File(c.getCurrentselection().path);
			if(Isplaying){
			try {
				player.stop();
				
			} catch (Exception e) {
			}
			}

			try {
				// System.out.println("play"+player);
				player = Manager.createRealizedPlayer(f.toURI().toURL());

				player.addControllerListener(new ControllerAdapter() {
@Override
public void start(StartEvent e) {
											// TODO Auto-generated method stub
											super.start(e);
											Isplaying=true;
											c.mediactl.timer.start();
											if(stoptime!=0){
												player.setStopTime(new Time(stoptime/1000.0));
												}
											System.out.println(stoptime+"st evt  "+player.getStopTime().getSeconds());
											
											
										}
@Override
public void stop(StopEvent e) {
System.out.println("stop");
	super.stop(e);
	c.mediactl.timer.stop();
	Isplaying=false;
	stoptime=0;
}


@Override
public void controllerError(ControllerErrorEvent e) {
	JOptionPane.showMessageDialog(c.w, e.getMessage()+"\n check file format --> it should be : Wav PCM, 16bit,44.1kHz",
            "erreur :"+ e.getSource(), JOptionPane.ERROR_MESSAGE );
	
}
					@Override
					public void endOfMedia(EndOfMediaEvent e) {
						Controller controller = (Controller) e.getSource();
						controller.stop();

						c.mediactl.resetGraph();
						c.mediactl.timer.stop();
						controller.setMediaTime(new Time(0));
						controller.deallocate();
					}
				});


				player.start();
	

	


				Isplaying = true;

				totalTime = player.getDuration().getSeconds();

			} 
			catch (Exception ex) {
				ex.printStackTrace();
			
			}
		} else {
			System.out.println("erreur fichier vide...");
		}

	}

	public void stop() {
		// System.out.println(player);
		if (Isplaying) {
			player.stop();

		}
		Isplaying = false;

		// System.out.println(player);
	}

	public void setVolumedB(float v) {
		if (Isplaying)
			player.getGainControl().setDB(v);

	}

	public void setPan(int pct) {
		for (int k = 0; k < player.getControls().length; k++) {
			System.out.println(player.getControls()[k]);
		}
		// if(Isplaying)player.getControls()
	}

	public void incrVolume() {
		setVolumedB(player.getGainControl().getLevel() + 1);
	}

	public double getTotalTime() {
		return totalTime;
	}

	public void playAt(int tin){
		play();
		
		player.setMediaTime(new Time(tin/1000.0));
	
	}
	public Time playpause(double tin) {
		// System.out.println("Pause :"+Isplaying+"//"+f);
		if (c.getCurrentselection() != null) {

			if (Isplaying) {

				pauseTime = player.getMediaTime();
				stop();
				return pauseTime;

			} else {

				play();
				player.setMediaTime(new Time(tin));

				return null;
			}

		}
		return null;
	}

	public void setCurrentTime(double tin) {

		if (Isplaying) {
			Time t = new Time(tin);
			player.setMediaTime(t);
		}
	}

	public double getCurrentTime() {
		if (Isplaying) {
			return player.getMediaTime().getSeconds();
		}
		return 0.0;
	}

}
