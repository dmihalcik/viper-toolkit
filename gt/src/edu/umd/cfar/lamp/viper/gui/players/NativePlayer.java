/*
 * Created on Feb 12, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.umd.cfar.lamp.viper.gui.players;

import java.awt.*;
import java.io.*;

import edu.umd.cfar.lamp.apploader.prefs.PreferenceException;
import edu.umd.cfar.lamp.apploader.prefs.PrefsManager;

import viper.api.time.*;
import viper.api.time.Frame;

/**
 * @author davidm
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class NativePlayer extends DataPlayerHelper {
	private MpegTest mt;
	private String path;
	private Frame now;
	private static boolean used = false;
	private boolean initialized;

	public NativePlayer(String path) {
		super(path);
		if (used) {
			//throw new IllegalStateException("Unfortunately, you can only use the native player once.");
		}
		String x = path.toLowerCase();
		if ((x.endsWith("g") && (x.endsWith("mpg") || x.endsWith("mpeg")))
			|| x.endsWith("avi") || x.endsWith("mov") || x.endsWith("mp4")
			|| x.endsWith("mp2")) {
			used = true;
			
			this.path = path;
			initialized = false;
			
		} else {
			throw new IllegalStateException("Not a recognized file extension: " + path);
		}
	}

	public void setPrefs(PrefsManager prefs) {
		super.setPrefs(prefs);
		init();
	}

	protected void init(){
		if(!initialized){
			PrefsManager prefs = getPrefs();
			try{
				//Index filenames are placed in the indices subfolder of .viper 
				//They are formatted as the original filename plus a '$', plus the simple hash of the original path,
				//plus the ".vind" ending

				File userDirectory = new File(prefs.getUserDirectory());
				File movieFile = new File(path);
				
				File indexFileLocation = new File(userDirectory, "indices");
				if(!indexFileLocation.isDirectory())
					indexFileLocation.mkdirs();
				
				String splits[] = path.split("\\\\");
				String fileName = splits[splits.length - 1];
				
				String almostFinalPath = indexFileLocation.toString() + "\\" + fileName + "$";
				String addedHash =  "" + movieFile.lastModified() + path + "Magic Number: 1";
				mt = new MpegTest(path, almostFinalPath + addedHash.hashCode() + ".vind");
				initialized = true;
			} catch (PreferenceException e){
				//Just load the file in
				mt = new MpegTest(path);
				initialized = true;
			}
			
			setNow(getSpan().getStartInstant());
		}
	}
	
	
	protected Image helpGetImage(Frame f)
		throws IOException, InterruptedException {

		init();
		
		return mt.getFrame(f.intValue()-1);
	}

	public Span getSpan() {
		init();
		
		return new Span(new Frame(1), new Frame(mt.getNumFrames()+1));
	}

	public Instant getNow() {
		return now;
	}

	public void setNow(Instant i) {
		now = (Frame) i;
	}

	public FrameRate getRate() {
		return new RationalFrameRate(1);
	}
	
	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.players.DataPlayer#getImageType(viper.api.time.Instant)
	 */
	public String getImageType(Instant i) {
		init();
		
		Frame f = getRate().asFrame(i);
		char c = mt.getFrameTypeChar(f.intValue()-1);
		switch(c) {
			case 'B':
				return DataPlayer.B_FRAME;
			case 'P':
				return DataPlayer.P_FRAME;
			case 'I':
				return DataPlayer.I_FRAME;
			default:
				return DataPlayer.UNKNOWN_FRAME;
		}
	}
	
	/**
	 * Clips the current file, guarenteeing that all from beginFrame 
	 * to endFrame, inclusive, are included in the new file.  It is 
	 * likely that some additional frames will also be included.  
	 * @param newFile The file path and name for the new, clipped file
	 * @param beginFrame The beginning frame to clip from
	 * @param endFrame The final frame to clip to
	 * @return The actual beginning frame that was used
	 */
	public int clipFile(String newFile, int beginFrame, int endFrame){
		return mt.clipFile(newFile, beginFrame - 1, endFrame - 1);
	}
}
