package edu.umd.cfar.lamp.viper.gui.players;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import edu.umd.cfar.lamp.apploader.prefs.PrefsManager;
import viper.api.time.Frame;
import viper.api.time.FrameRate;
import viper.api.time.Instant;
import viper.api.time.RationalFrameRate;
import viper.api.time.Span;

public class MatlabPlayer extends DataPlayerHelper {

	private boolean initialized;
	private MatlabProxy proxy;
	private String path;
	private int h;
	private int w;
	private int numFrames;
	private Frame now;
	private double fps;

	protected MatlabPlayer(String path) {
		super(path);
		this.path = path;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void setPrefs(PrefsManager prefs) {
		super.setPrefs(prefs);
		init();
	}

	private void init() {
		if (!initialized) {
		    MatlabProxyFactory factory = new MatlabProxyFactory();
		    try {
		    	proxy = factory.getProxy();
				proxy.setVariable("videoPath", path);
			    proxy.eval("info = aviinfo(videoPath);");
			    h = (int) ((double[]) proxy.getVariable("info.Height"))[0];
			    w = (int) ((double[]) proxy.getVariable("info.Width"))[0];
			    numFrames = (int) ((double[]) proxy.getVariable("info.NumFrames"))[0];
			    fps = ((double[]) proxy.getVariable("info.FramesPerSecond"))[0];
			} catch (MatlabInvocationException | MatlabConnectionException e) {
				throw new IllegalStateException(e);
			}
			initialized = true;
		}
	}
	
	@Override
	public void destroy() {
	    proxy.disconnect();
		super.destroy();
	}

	@Override
	protected Image helpGetImage(Frame f) throws IOException {
		init();
		try {
			proxy.setVariable("currentFrame", f.getFrame());
		    proxy.eval("movie = aviread(videoPath,currentFrame); frame = movie(1).cdata;");
		    byte[] frame = (byte[]) proxy.getVariable("frame");
		    assert frame.length == 3 * w * h;
		    BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		    for (int k = 0; k < 3; k++) {
			    for (int col = 0; col < w; col++) {
			    	for (int row = 0; row < h; row++) {
			    		int v = image.getRGB(col, row);
			    		int v2 = v << 8 | (0xff&frame[k * w * h + col * h + row]);
			    		image.setRGB(col, row, v2);
			    	}
			    }
		    }
		    // Integer.toHexString(image.getRGB(0,0))
		    // Integer.toHexString(image.getRGB(w-1,h-1))
		    return image;
		} catch (MatlabInvocationException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Span getSpan() {
		init();
		return new Span(new Frame(1), new Frame(numFrames + 1));
	}

	@Override
	public Instant getNow() {
		return now;
	}

	@Override
	public void setNow(Instant i) {
		now = (Frame) i;
	}

	@Override
	public FrameRate getRate() {
		return new RationalFrameRate(fps);
	}

}
