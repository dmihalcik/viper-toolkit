package edu.umd.cfar.lamp.viper.gui.players;


import java.awt.*;
import java.awt.image.*;
import java.nio.ByteBuffer;

import edu.umd.cfar.lamp.nmpeg.*;

/**
 * Simple wrapper for the VirtualDub4Java MPEG decoder.
 */
public class MpegTest {
	private static boolean loaded;
	InputFileMPEG file;
	private int w;
	private int h;
	private ByteBufferAsIntBufferSource bufferSource;

	static{
		try {
			System.loadLibrary("VirtualDub4Java");
			loaded = true;
		} catch (UnsatisfiedLinkError e) {
			loaded = false;
		}
	}
	
/*
	private void initialize(){
		Runtime rt = Runtime.getRuntime();
//		File libPath = new File("../viper-vdub/VirtualDub4Java/Debug/VirtualDub4Java.dll");
//		rt.load(libPath.getAbsolutePath());
		
		//rt.loadLibrary("VirtualDub4Java");
		//nmpeg.InitVdub();
		if(!initialized){
			rt.addShutdownHook(new Thread() {
				public void run() {
					nmpeg.DeinitVdub();
				}});
		}
		initialized = true;
	}
*/
	
	//private static boolean initialized = false;

	public MpegTest(String path) {
		if (!loaded) {
			throw new IllegalStateException("Native MPEG library failed to open.");
		}
		System.gc();
		/*
		if (!initialized) {
			initialize();
		} else {
			nmpeg.DeinitVdub();
			initialize();
		}
		*/

		if(path == null)
			throw new NullPointerException();
		file = new InputFileMPEG();
		file.InitS(new String[]{path});
		BITMAPINFOHEADER bih = file.getVideoMPEGSource().getImageFormat();
		w = bih.getBiWidth();
		h = bih.getBiHeight();

		//long bcompression = bih.getBiCompression();
		//int bdepth = bih.getBiBitCount();
		//("Image is " + w + "x" + h + " for " + getNumFrames() + " frames  at " + bdepth + "bpp");
	}
	
	public MpegTest(String path, String indexPath) {
		if (!loaded) {
			throw new IllegalStateException("Native MPEG library failed to open.");
		}
		System.gc();
		/*
		if (!initialized) {
			//initialize();
		} else {
			//nmpeg.DeinitVdub();
			//initialize();
		}
		*/

		if(path == null || indexPath == null)
			throw new NullPointerException();
		
		file = new InputFileMPEG();
		file.InitWIndexFile(new String[]{path}, new String[]{indexPath});
		BITMAPINFOHEADER bih = file.getVideoMPEGSource().getImageFormat();
		w = bih.getBiWidth();
		h = bih.getBiHeight();
		//long bcompression = bih.getBiCompression();
		//int bdepth = bih.getBiBitCount();
		//("Image is " + w + "x" + h + " for " + getNumFrames() + " frames  at " + bdepth + "bpp");
	}
	public int getNumFrames() {
		return file.getFrameCount();
	}
	public char getFrameTypeChar(int f) {
		return file.getVideoMPEGSource().getFrameTypeChar(f);
	}
	public Image getFrame(int f) {
		ColorModel m;// = ColorModel.getRGBdefault();
		m = new DirectColorModel(32,
				0x0000ff00,	// Red
				0x00ff0000,	// Green
				0xff000000,	// Blue
				0x00000000	// Alpha
				);
		file.getVideoMPEGSource().goToFrame(f);
		ByteBuffer buff = file.getVideoMPEGSource().getFrameBuffer();

		if(bufferSource == null)
			bufferSource = new ByteBufferAsIntBufferSource(w, h, m, buff);
		else
			bufferSource.newPixels(buff, m);
		
		Toolkit myTool = Toolkit.getDefaultToolkit();
		Image i = myTool.createImage(bufferSource);
		return i;
	}
	
	public int clipFile(String newFile, int beginFrame, int endFrame){
		return file.clipFile(new String[]{newFile}, beginFrame, endFrame);
	}
	
	public static void main(String[] args) {
		long start, end;
		start = System.currentTimeMillis();
		System.out.println("Loading toTest");

//		MpegTest toTest = new MpegTest("C:/Documents and Settings/jnewman/Desktop/20020627.mpg", 
//		"C:/Documents and Settings/jnewman/Desktop/20020627.mpg$");
		
//		MpegTest toTest = new MpegTest("C:/Documents and Settings/jnewman/Desktop/star-trek.mpg", 
//				"C:/Documents and Settings/jnewman/Desktop/star-trek.mpg$");
		MpegTest toTest = new MpegTest("C:/Documents and Settings/jnewman/Desktop/clipped.mpg", 
		"C:/Documents and Settings/jnewman/Desktop/clipped.mpg$");

		
		end = System.currentTimeMillis();
		System.out.println("Elapsed time: " + (end - start));
		System.out.println("frames = " + toTest.getNumFrames());
		start = System.currentTimeMillis();
		Image i = toTest.getFrame(50);
		end = System.currentTimeMillis();
		System.out.println("Load frame time: " + (end - start));
		Viewer view = new Viewer(i);

		//toTest.file.clipFile(new String[]{"C:/Documents and Settings/jnewman/Desktop/clipped.mpg"},50 ,102);
		//MpegTest second = new MpegTest("C:/Documents and Settings/jnewman/Desktop/clipped.mpg",
		//		"C:/Documents and Settings/jnewman/Desktop/clipped.mpg$");
		//System.out.println("Num frames: " + second.getNumFrames());
		
		//		toTest.file.getVideoMPEGSource().copyBufferToClipboard();
	}
}
