package edu.umd.cfar.lamp.viper.examples.persontracking;

import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;


public class PersonAnnotationTransferrable implements Transferable {
	public static DataFlavor[] flavors = {DataFlavor.stringFlavor};

	private int evidenceId;
	private PersonGallery gallery;
	
	/**
	 * @param id
	 * @param gallery
	 */
	public PersonAnnotationTransferrable(int id, PersonGallery gallery) {
		super();
		evidenceId = id;
		this.gallery = gallery;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return Arrays.asList(flavors).contains(flavor);
	}
	public String toString() {
		return String.valueOf(evidenceId);
	}
	
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor.equals(flavors[0])) {
			return toString();
		}
		throw new UnsupportedFlavorException(flavor);
	}
}
