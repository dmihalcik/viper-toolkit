package edu.umd.cfar.lamp.viper.gui.core;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.logging.*;

import javax.swing.*;

import viper.api.*;
import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.chronicle.extras.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.chronology.*;
import edu.umd.cs.piccolox.swing.*;

/**
 * Simple viewer of viper files. Useful for debugging,
 * especially when the error might be in the application
 * loader.
 */
public class SimpleLoader {
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.core");
	
	/**
	 * Create a simple view of the viper file passed in 
	 * the command line argument. Attempts to create a
	 * view of the first sourcefile found in the metadatafile,
	 * searching the user's file system if necessary.
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Args.length == " + args.length);
			printUsage();
			System.exit(2);
		}
		try {
			URI fileURI = new File(args[0]).toURI();
			URI prefsURI = new File(args[1]).toURI();
			PrefsManager prefs = new PrefsManager();
			prefs.setSystemPrefs(prefsURI);

			ViperViewMediator mediator = new ViperViewMediator();
			mediator.setPrefs(prefs);
			mediator.setFileName(fileURI);
			String firstFile =
				((Sourcefile) mediator
					.getViperData()
					.getAllSourcefiles()
					.get(0))
					.getReferenceMedia()
					.getSourcefileName();
			mediator.setFocalFile(new URI(firstFile));
			DataViewGenerator gen = new DataViewGenerator();
			gen.setPrefs(prefs);

			// set up canvas
			ViperDataCanvas root = new ViperDataCanvas();
			root.setViperView(new ViperDataPLayer(gen, mediator));
			JFrame cantainer = new JFrame("Viper Data View");
			cantainer.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			Rectangle r = cantainer.getBounds();
			r.height = root.getPreferredSize().height;
			r.width = root.getPreferredSize().width;
			cantainer.setBounds(r);
			PScrollPane scrollPane = new PScrollPane(root);
			cantainer.getContentPane().add(scrollPane);
			cantainer.validate();
			root.requestFocus();

			// set up chronicle
			ViperChronicleView chronoView = new ViperChronicleView();
			chronoView.setMediator(mediator);
			((TreeChronicleViewModel) chronoView.getModel()).expandAll();
			JFrame chrontainer = new JFrame("Viper Data View");
			chrontainer.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			chrontainer.setSize(chronoView.getPreferredSize());
			System.err.println(
				"Preferred size: " + chronoView.getPreferredSize());
			chrontainer.getContentPane().add(chronoView);
			chrontainer.validate();

			// Appear!
			cantainer.setVisible(true);
			chrontainer.setVisible(true);
		} catch (IOException iox) {
			logger.log(Level.SEVERE, "Error starting SimpleLoader.", iox);
		} catch (PreferenceException px) {
			logger.log(Level.SEVERE, "Error starting SimpleLoader", px);
		} catch (URISyntaxException urisx) {
			logger.log(Level.SEVERE, "Error starting SimpleLoader", urisx);
		}
	}
	
	/**
	 * Prints usage error string.
	 */
	public static void printUsage() {
		System.err.println("Usage: vipercanvas <metadata.xml>");
	}
}
