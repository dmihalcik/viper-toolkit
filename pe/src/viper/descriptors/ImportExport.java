/***************************************
 *            ViPER                    *
 *  The Video Processing               *
 *         Evaluation Resource         *
 *                                     *
 *  Distributed under the GPL license  *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package viper.descriptors;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.xml.parsers.*;

import org.apache.xerces.dom.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import viper.api.*;
import viper.api.extensions.*;
import viper.api.impl.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Methods for importing and exporting to the 
 * old .gtf file format.
 */
public class ImportExport {
	private static File startDirectory = null;
	
	/**
	 * Imports the configuration from the given file.
	 * @param v the data to import the config into
	 * @param gtc the location of the configuration
	 * @return if the data changed
	 * @throws IOException if the file has an error 
	 * or is missing
	 */
	public static boolean importConfig(ViperData v, File gtc) throws IOException {
		ViperParser vp = new ViperParser();
		ViperData v2 = vp.parseDoc(convertToModern(gtc));
		return importConfig(v, v2);
	}
	
	/**
	 * Copies the configuration from the source metadata set into
	 * the target metadata set. It will not overwrite existing
	 * attribute definitions.
	 * @param target the metadata to modify
	 * @param source the new stuff to copy
	 * @return if the target was modified
	 */
	private static boolean importConfig(ViperData target, ViperData source) {
		return importConfig(target, source.getConfigsNode());
	}

	/**
	 * Copies the configuration from the source metadata set into
	 * the target metadata set. It will not overwrite existing
	 * attribute definitions.
	 * @param target the metadata to modify
	 * @param source the new stuff to copy
	 * @return if the target was modified
	 */
	public static boolean importConfig(ViperData target, Configs cfgs) {
		TransactionalNode.Transaction trans = null;
		boolean success = false;
		boolean changed = false;
		try {
			if (target instanceof TransactionalNode) {
				trans = ((TransactionalNode) target).begin(ViperParser.IMPL + "importConfig");
			}
			List toAdd = new LinkedList();
			for (int i = 0; i < cfgs.getNumberOfChildren(); i++) {
				Config toCopy = (Config) cfgs.getChild(i);
				Config oldCopy = target.getConfig(toCopy.getDescType(), toCopy.getDescName());
				if (oldCopy == null) {
					toAdd.add(toCopy);
					changed = true;
				} else {
					boolean ac = copyConfigIntoAnother(oldCopy, toCopy);
					changed = changed || ac;
				}
			}
			cfgs = target.getConfigsNode();
			for(Iterator iter = toAdd.iterator(); iter.hasNext(); ) {
				Config toCopy = (Config) iter.next();
				cfgs.addChild(toCopy);
			}
			success = true;
		} finally {
			if (trans != null) {
				if (success) {
					trans.commit();
				} else {
					trans.rollback();
					changed = false;
				}
			}
		}
		return changed;
	}

	/**
	 * Prompts the user to open a file from which to import 
	 * viper configuration information.
	 * TODO: return a list of attribute configs that were not imported
	 * because of a merge conflict
	 * @param v the data to import into
	 * @return if the configuration of <code>v</code> has changed
	 * @throws IOException
	 */
	public static boolean importConfig(ViperData v) throws IOException {
		JFileChooser dialog = initFileChooser();
		int returnValue = dialog.showOpenDialog(null);
		switch (returnValue) {
			case JFileChooser.APPROVE_OPTION :
				File f = dialog.getSelectedFile();
				setStartDirectory(f.getParentFile());
				return importConfig(v, f);
		}
		return false;
	}
	
	/**
	 * @return
	 */
	private static JFileChooser initFileChooser() {
		JFileChooser dialog = new JFileChooser();
		if (getStartDirectory() != null) {
			dialog.setCurrentDirectory(getStartDirectory());
		}
		dialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		return dialog;
	}

	private static boolean copyConfigIntoAnother(Config target, Config toCopy) {
		boolean changed = false;
		for (int j = 0; j < toCopy.getNumberOfChildren(); j++) {
			AttrConfig ac2 = (AttrConfig) toCopy.getChild(j);
			AttrConfig ac1 = target.getAttrConfig(ac2.getAttrName());
			if (ac1 == null) {
				changed = true;
				target.addChild(ac2);
			} else if (!ac1.equals(ac2)) {
				// Warning - disagreement between the two attribute types
				System.err.println("Error while importing config: Conflicting attribute definitions");
				System.err.println("      target: " + ac1);
				System.err.println("    imported: " + ac2);
			}
		}
		return changed;
	}
	
	public static Element convertToModern (InputStream inFile) throws IOException {
		Element root;
		if (StringHelp.isXMLFormat(inFile)) {
			Document document;
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder;
				try {
					builder = factory.newDocumentBuilder();
				} catch (ParserConfigurationException e) {
					throw new IOException(e.getLocalizedMessage());
				}
				try {
					document = builder.parse(inFile);
				} catch (SAXException e) {
					throw new IOException(e.getLocalizedMessage());
				}
			} finally {
				if (inFile != null) {
					inFile.close();
				}
			}
			root = document.getDocumentElement();
		} else {
			// Parser requires a RandomAccessFile. Convert inFile to one.
			File tempFile = File.createTempFile("temp", "gtf");
			byte[] input = new byte[512];
			int bytesRead = 0;
			OutputStream output =
				new FileOutputStream(tempFile);
			while ((bytesRead = inFile.read(input)) > 0) {
				output.write(input, 0, bytesRead);
			}
			output.close();
			inFile.close();
			Vector files = new Vector(1);
			files.add(tempFile.getAbsolutePath());
			DescVector dv;
			DescHolder data;
			try {
				data = new DescHolder();
				dv = new DescVector(data);
				DescriptorConfigs cfgs =
					new DescriptorConfigs(null);
				cfgs.parseConfig(
					files,
					false,
					true,
					true,
					true,
					false);
				data.setDescriptorConfigs(cfgs);
				dv.parseData(files, null);
			} finally {
				tempFile.delete();
			}
			DOMImplementation domI = DOMImplementationImpl.getDOMImplementation();
			DocumentType dtd =
				domI.createDocumentType(
					"viper",
					"viper",
					DescriptorData.NAMESPACE_URI);
			Document doc =
				domI.createDocument(DescriptorData.NAMESPACE_URI, "viper", dtd);
			root = data.getXMLFormat(doc);
		}
		return root;
	}
	
	private static Element convertToModern(File gtf) throws IOException {
		if(StringHelp.isXMLFormat(gtf)) {
			InputStream inFile = null;
			Document document;
			try {
				inFile = new FileInputStream(gtf);
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder;
				try {
					builder = factory.newDocumentBuilder();
				} catch (ParserConfigurationException e) {
					throw new IOException(e.getLocalizedMessage());
				}
				try {
					document = builder.parse(inFile);
				} catch (SAXException e) {
					throw new IOException(e.getLocalizedMessage());
				}
			} finally {
				if (inFile != null) {
					inFile.close();
				}
			}
			return ViperParser.correctDocumentForOldNamespace(document);
		} else {
			Vector files = new Vector(1);
			files.add(gtf.getAbsolutePath());
			DescriptorConfigs cfgs = new DescriptorConfigs(null);
			cfgs.parseConfig(files, false, true, true, true, false);
			DescHolder dh = new DescHolder();
			dh.setDescriptorConfigs(cfgs);
			DescVector dv = new DescVector(dh);
			dv.parseData(files, null);
			String fname = "";
			dh.addFileName(fname);
			dh.setDataForFile(fname, dv);
			DOMImplementation domI = DOMImplementationImpl.getDOMImplementation();
			DocumentType dtd =
				domI.createDocumentType(
					"viper",
					"viper",
					DescriptorData.NAMESPACE_URI);
			Document root =
				domI.createDocument(DescriptorData.NAMESPACE_URI, "viper", dtd);
			return dh.getXMLFormat(root);
		}
	}
	
	/**
	 * Imports the given gtf file as the named sourcefile.
	 * @param asFile
	 * @param gtf
	 * @return <code>true</code> if the import was successful
	 */
	public static boolean importDataFromGTF(Sourcefile asFile, File gtf) {
		Element sf = null;
		try {
			if (StringHelp.isXMLFormat(gtf)) {
				Element viperEl = ViperParser.file2correctDOM(gtf.toURI());
				NodeList nlist =
					viperEl.getElementsByTagNameNS(ViperData.ViPER_SCHEMA_URI, "sourcefile");
				for (int i = 0; i < nlist.getLength(); i++) {
					Element e = (Element) nlist.item(i);
					if (asFile.getReferenceMedia().getSourcefileName().equals(e.getAttribute("filename"))) {
						sf = e;
						break;
					}
				}
			}
			if (sf == null) {
				// UNABLE TO FIND MATCHING SOURCEFILE NAME
				return false;
			}
		} catch (IOException e) {
		}
		
		if (sf == null) {
			Vector files = new Vector(1);
			files.add(gtf.getAbsolutePath());
			DescriptorConfigs cfgs = new DescriptorConfigs(null);
			cfgs.parseConfig(files, false, true, true, true, false);
			DescHolder dh = new DescHolder();
			dh.setDescriptorConfigs(cfgs);
			DescVector dv = new DescVector(dh);
			dv.parseData(files, null);
			DOMImplementation domI = DOMImplementationImpl.getDOMImplementation();
			DocumentType dtd =
				domI.createDocumentType(
					"viper",
					"viper",
					DescriptorData.NAMESPACE_URI);
			Document root =
				domI.createDocument(DescriptorData.NAMESPACE_URI, "viper", dtd);
			sf = dv.getXMLFormat(root);
		}
		ViperParser vp = new ViperParser();
		boolean success = false;
		TransactionalNode.Transaction trans = null;
		try {
			if (asFile instanceof TransactionalNode) {
				trans = ((TransactionalNode) asFile).begin(ViperParser.IMPL + "importData");
			}
			vp.parseIntoSourcefile(sf, (SourcefileImpl) asFile);
			success = true;
		} finally {
			if (trans != null) {
				if (success) {
					trans.commit();
				} else {
					trans.rollback();
				}
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param target
	 * @param source
	 */
	public static boolean importDataFromGTF(Sourcefile target, Sourcefile source) {
		ViperData v = target.getRoot();
		SourcefileImpl t = (SourcefileImpl) target;
		SourcefileImpl s = (SourcefileImpl) source;
		boolean changed = false;
		Iterator iter = s.getDescriptors();
		while (iter.hasNext()) {
			changed = true;
			DescriptorImpl toCopy = (DescriptorImpl) iter.next();
			toCopy = (DescriptorImpl) toCopy.clone();
			int id = t.findFreeIdFor(v.getConfig(toCopy.getDescType(), toCopy.getDescName()));
			toCopy.setDescId(id);
			t.addChild(toCopy);
		}
		return changed;
	}

	public static boolean importAllDataFromXGTF(ViperData v) {
		JFileChooser dialog = initFileChooser();
		int returnValue = dialog.showOpenDialog(null);
		switch (returnValue) {
			case JFileChooser.APPROVE_OPTION :
				File f = dialog.getSelectedFile();
				setStartDirectory(f.getParentFile());
				// TODO need to add 'single frame edit' mode
				// and auto-create/edit .info files
				try {
					importConfig(v, f);
					return importAllDataFromXGTF(v, f);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
		}
		return false;
	}
	
	private static boolean importAllDataFromXGTF(ViperData target, File f) {
		try {
			ViperData source = new ViperParser().parseFromTextFile(f.toURI());
			return importAllData(target, source);
		} catch (IOException e) {
			return false;
		}
	}

	private static boolean importAllData(ViperData target, ViperData source) {
		TransactionalNode.Transaction trans = null;
		boolean success = false;
		boolean changed = false;
		try {
			if (target instanceof TransactionalNode) {
				trans = ((TransactionalNode) target).begin(ViperParser.IMPL + "importAll");
			}
			changed = changed || importConfig(target, source);
			Iterator sources = source.getSourcefiles();
			while (sources.hasNext()) {
				Sourcefile toImport = (Sourcefile) sources.next();
				String sfName = toImport.getReferenceMedia().getSourcefileName();
				Sourcefile toModify = target.getSourcefile(sfName);
				if (toModify == null) {
					toModify = target.createSourcefile(sfName);
				}
				changed = changed || importDataFromGTF(toModify, toImport);
			}
			success = true;
		} finally {
			if (trans != null) {
				if (success && changed) {
					trans.commit();
				} else {
					trans.rollback();
					changed = false;
				}
			}
		}
		return changed;
	}

	/**
	 * TODO: currently, does not convert the config. 
	 * @param sf
	 * @return
	 */
	public static boolean importDataFromGTF(Sourcefile sf) {
		JFileChooser dialog = initFileChooser();
		int returnValue = dialog.showOpenDialog(null);
		switch (returnValue) {
			case JFileChooser.APPROVE_OPTION :
				File f = dialog.getSelectedFile();
				setStartDirectory(f.getParentFile());
				// TODO need to add 'single frame edit' mode
				// and auto-create/edit .info files
				try {
					importConfig(sf.getRoot(), f);
					return importDataFromGTF(sf, f);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
		}
		return false;
	}
	
	/**
	 * Saves the file as a gtf file.
	 * @param sf the viper-api representation of a single file's 
	 * video metadata 
	 * @param target where to save the file
	 * @return the file was written successfully
	 * @throws IOException
	 * @throws BadDataException
	 */
	public static boolean exportSourcefileAsGTF(Sourcefile sf, File target)
		throws IOException, BadDataException {
		DOMImplementation domI = DOMImplementationImpl.getDOMImplementation();
		Element el =
			XmlSerializer.toXmlSingleSourcefile(sf.getRoot(), sf, domI);
		///XXX:davidm:this should spawn a worker to do this.
		DescHolder dh = new DescHolder();
		DescriptorConfigs dcfgs = new DescriptorConfigs(dh);
		NodeList cfgNodes = el.getElementsByTagNameNS(ViperData.ViPER_SCHEMA_URI, "config");
		dcfgs.addDesConfig((Element) cfgNodes.item(0));
		dh.setDescriptorConfigs(dcfgs);
		DescVector dv = new DescVector(dh);
		dv.parseData(el, null);
		PrintWriter outStream;
		outStream = new PrintWriter(new FileOutputStream(target));
		dcfgs.printOut(outStream);
		dv.printOut(outStream);
		outStream.close();
		return true;
	}
	
	/**
	 * Asks the user where, then saves the file as a gtf file.
	 * @param sf the file to serialize.
	 * @return if the file was written successfully
	 * @throws IOException if there is an error in writing
	 * @throws BadDataException if there was a viper api/ 
	 * viper-pe data mismatch
	 */
	public static boolean exportSourcefileAsGTF(Sourcefile sf) throws IOException, BadDataException {
		JFileChooser dialog = initFileChooser();
			int returnValue = dialog.showSaveDialog(null);
			switch (returnValue) {
				case JFileChooser.APPROVE_OPTION :
					File f = dialog.getSelectedFile();
					setStartDirectory(f.getParentFile());

					// TODO need to add 'single frame edit' mode
					// and auto-create/edit .info files
					return exportSourcefileAsGTF(sf, f);
			}
		return false;
	}
	
	/**
	 * Gets the directory the Import/Export plugin will start from
	 * when opening a file. 
	 * @return the last directory a user found a gtf file in, or whatever
	 * value has been set in {@link setStartDirectory()}, whichever 
	 * happened more recently
	 */
	public static File getStartDirectory() {
		return startDirectory;
	}
	
	/**
	 * Sets the directory to look in. <code>null</code> means
	 * to use the java default (usually the user's home directory).
	 * @param startDirectory the directory to open when necessary
	 */
	public static void setStartDirectory(File startDirectory) {
		ImportExport.startDirectory = startDirectory;
	}
}
