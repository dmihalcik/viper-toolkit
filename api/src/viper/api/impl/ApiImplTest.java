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

package viper.api.impl;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import junit.framework.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import viper.api.*;
import viper.api.Node;
import viper.api.datatypes.*;
import viper.api.extensions.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.geometry.*;

/**
 * Tests the functionality of an implementation of the ViPER API. 
 */
public class ApiImplTest extends TestCase {
	private static final String[] sourcetypes =
		new String[] { "SEQUENCE", "FRAMES" };
	private static final String[] cuttypes =
		new String[] { "cut", "fade", "star swipe" };

	private ViperData create() {
		return new ViperDataImpl();
	}
	private ViperData createFileInformationConfig(ViperData v) {
		Config fi = v.createConfig(Config.FILE, "Information");
		fi.createAttrConfig(
			"SOURCEDIR",
			ViperData.ViPER_DATA_URI + "svalue",
			false,
			null,
			Svalue.SV);
		fi.createAttrConfig(
			"SOURCEFILES",
			ViperData.ViPER_DATA_URI + "svalue",
			false,
			null,
			Svalue.SV);
		fi.createAttrConfig(
			"SOURCETYPE",
			ViperData.ViPER_DATA_URI + "lvalue",
			false,
			null,
			new Lvalue(sourcetypes));
		fi.createAttrConfig(
			"NUMFRAMES",
			ViperData.ViPER_DATA_URI + "dvalue",
			false,
			null,
			new Dvalue());
		fi.createAttrConfig(
			"H_FRAME_SIZE",
			ViperData.ViPER_DATA_URI + "dvalue",
			false,
			null,
			new Dvalue());
		fi.createAttrConfig(
			"V_FRAME_SIZE",
			ViperData.ViPER_DATA_URI + "dvalue",
			false,
			null,
			new Dvalue());
		return v;
	}
	private ViperData createContentCutConfig(ViperData v) {
		Config ci = v.createConfig(Config.CONTENT, "Cut");
		ci.createAttrConfig(
			"TYPE",
			ViperData.ViPER_DATA_URI + "lvalue",
			false,
			null,
			new Lvalue(cuttypes));
		return v;
	}
	private ViperData createObjectFaceConfig(ViperData v) {
		Config oi = v.createConfig(Config.OBJECT, "Face");
		oi.createAttrConfig(
			"LOCATION",
			ViperData.ViPER_DATA_URI + "bbox",
			true,
			null,
			new AttributeBbox());
		return v;
	}
	private ViperData createAllConfigs(ViperData v) {
		createFileInformationConfig(v);
		createContentCutConfig(v);
		createObjectFaceConfig(v);
		return v;
	}
	private ViperData addSourcefiles(ViperData v) {
		v.createSourcefile("a.mpg");
		v.createSourcefile("b.info");
		v.createSourcefile("c.mpeg");
		return v;
	}
	private ViperData addFileInformationDescriptors(ViperData v) {
		for (Iterator iter = v.getAllSourcefiles().iterator();
			iter.hasNext();
			) {
			Sourcefile curr = (Sourcefile) iter.next();
			Descriptor d =
				curr.createDescriptor(v.getConfig(Config.FILE, "Information"));
			d.getAttribute("SOURCEDIR").setAttrValue("~/media");
			d.getAttribute("SOURCEFILES").setAttrValue(
				"1 " + curr.getReferenceMedia().getSourcefileName());
			d.getAttribute("SOURCETYPE").setAttrValue(
				curr.getReferenceMedia().getSourcefileName().endsWith("g") ? "SEQUENCE" : "FRAMES");
			d.getAttribute("NUMFRAMES").setAttrValue(new Integer(10));
		}
		return v;
	}
	private ViperData addContentCutDescriptors(ViperData v) {
		int i = 0;
		for (Iterator iter = v.getAllSourcefiles().iterator();
			iter.hasNext();
			) {
			Sourcefile curr = (Sourcefile) iter.next();
			Descriptor d =
				curr.createDescriptor(v.getConfig(Config.CONTENT, "Cut"));
			InstantRange r = new InstantRange();
			r.add(new Frame(1), new Frame(11));
			d.setValidRange(r);
			d.getAttribute("TYPE").setAttrValue(
				cuttypes[i++ % cuttypes.length]);
		}
		return v;
	}
	private ViperData addObjectFaceDescriptors(ViperData v) {
		for (Iterator iter = v.getAllSourcefiles().iterator();
			iter.hasNext();
			) {
			Sourcefile curr = (Sourcefile) iter.next();
			Descriptor d =
				curr.createDescriptor(v.getConfig(Config.OBJECT, "Face"));
			InstantRange r = new InstantRange();
			r.add(new Frame(1), new Frame(11));
			d.setValidRange(r);
			d.getAttribute("LOCATION").setAttrValueAtSpan(
				new BoundingBox(10, 10, 40, 40),
				new Span(new Frame(1), new Frame(11)));
		}
		return v;
	}

	private ViperData populateViperData(ViperData v) {
		addSourcefiles(v);
		addFileInformationDescriptors(v);
		addContentCutDescriptors(v);
		addObjectFaceDescriptors(v);
		return v;
	}

	/**
	 * Constructor for ApiImplTest.
	 * @param arg0
	 */
	public ApiImplTest(String arg0) {
		super(arg0);
	}

	void testViperData() {
		ViperData v = create();
		// Test Node functions
		Iterator iter = v.getChildren();
		assertTrue(v.getParent() == null);
		assertTrue(v.getNumberOfChildren() == 2);
		int i = 0;
		while (iter.hasNext()) {
			Node child = (Node) iter.next();
			assertTrue(
				child.getChildren() == null
					|| child.getNumberOfChildren() == 0);
			assertTrue(child.getParent().equals(v));
			i++;
			assertTrue(i <= 2);
		}
	}

	void testConfigAndAttrConfig() {
		ViperData v = createAllConfigs(create());
		((ViperDataImpl) v).printViperData(new java.io.PrintWriter(System.err));
		Configs cfgs = v.getConfigsNode();

		// Test getting Configs
		assertTrue(cfgs.equals(v.getChild(0)));
		assertTrue(!cfgs.equals(v.getChild(1)));

		// Test File Information
		Config ci = v.getConfig(Config.FILE, "Information");
		assertTrue(ci.getDescName().equals("Information"));
		assertTrue(((Node) v.getChild(0)).hasChild(ci));

		assertTrue(ci.hasAttrConfig("NUMFRAMES"));
		AttrConfig nfac = ci.getAttrConfig("NUMFRAMES");
		assertTrue(nfac.getAttrName().equals("NUMFRAMES"));
		assertTrue(nfac.getAttrType().equals(ViperData.ViPER_DATA_URI+"dvalue"));

		assertTrue(ci.hasAttrConfig("SOURCEFILES"));
		AttrConfig sfac = ci.getAttrConfig("SOURCEFILES");
		assertTrue(sfac.getAttrName().equals("SOURCEFILES"));
		assertTrue(sfac.getAttrType().equals(ViperData.ViPER_DATA_URI+"svalue"));

		// Test Content Cut
		// Test Object Face
	}

	void testData() {
		// FIXME add get tests 
		ViperData v = createAllConfigs(create());
		Descriptor d;
		Sourcefile s;
		addSourcefiles(v);
		s = v.getSourcefile("a.mpg");
		assertTrue(s != null);
		addFileInformationDescriptors(v);
		addContentCutDescriptors(v);
		addObjectFaceDescriptors(v);

		d = (Descriptor) s.getDescriptorsBy(Config.FILE).next();
		assertTrue(d != null);
		assertEquals(d.getAttribute("SOURCEDIR").getAttrValue(), "~/media");

		d = (Descriptor) s.getDescriptorsBy(Config.CONTENT).next();
		assertTrue(d != null);
		assertEquals(d.getAttribute("TYPE").getAttrValue(), "cut");

		d = (Descriptor) s.getDescriptorsBy(Config.OBJECT).next();
		assertTrue(d != null);
		Attribute att = d.getAttribute("LOCATION");
		try {
			att.getAttrValue();
			assertTrue("Should have thrown a 'NotStatic' exception", false);
		} catch (NotStaticException nsx) {
		}
		assertTrue(att.getAttrValueAtInstant(new Frame(100)) == null);
		assertTrue(
			att.getAttrValueAtInstant(new Frame(5)) instanceof BoundingBox);
		assertTrue(
			((BoundingBox) att.getAttrValueAtInstant(new Frame(5))).area()
				.equals(1600));
	}

	void testEquality() {
		System.out.println("Testing equality");
		ViperData v1 = create();
		ViperData v2 = create();
		assertTrue(v1 == v1);
		assertTrue(v1.equals(v1));
		assertTrue(v1 != v2);
		assertTrue(v1.equals(v2));

		System.out.println(" -- with configurations");
		v1 = createAllConfigs(v1);
		assertTrue(!v1.equals(v2));
		v2 = createAllConfigs(v2);
		assertTrue(v1 != v2);
		assertTrue(v1.equals(v2));

		System.out.println(" -- with empty sourcefiles");
		addSourcefiles(v1);
		assertTrue(!v1.equals(v2));
		addSourcefiles(v2);
		assertTrue(v1.equals(v2));

		System.out.println(" -- with file descriptors");
		addFileInformationDescriptors(v1);
		assertTrue(!v1.equals(v2));
		addFileInformationDescriptors(v2);
		assertTrue(v1.equals(v2));

		System.out.println(" -- with content descriptors");
		addContentCutDescriptors(v1);
		assertTrue(!v1.equals(v2));
		addContentCutDescriptors(v2);
		assertTrue(v1.equals(v2));

		System.out.println(" -- with object descriptors");
		addObjectFaceDescriptors(v1);
		assertTrue(!v1.equals(v2));
		addObjectFaceDescriptors(v2);
		assertTrue(v1.equals(v2));

		System.out.println(" -- of sections of them");
		Sourcefile s1 = v1.getSourcefile("a.mpg");
		Descriptor d1 =
			(Descriptor) s1.getDescriptorsBy(Config.OBJECT).next();
		Attribute att1 = d1.getAttribute("LOCATION");
		att1.setAttrValueAtSpan(
			new BoundingBox(20, 20, 20, 20),
			new Span(new Frame(5), new Frame(7)));
		assertTrue(
			((BoundingBox) att1.getAttrValueAtInstant(new Frame(5))).area()
				.equals(400));
		assertTrue(!v1.equals(v2));
		int i = 0;
		for (Iterator iter = att1.iterator(); iter.hasNext();) {
			iter.next();
			i++;
		}
		assertTrue(i == 3);

		System.out.println(" -- with more changes");
		Sourcefile s2 = v2.getSourcefile("a.mpg");
		Descriptor d2 =
			(Descriptor) s2.getDescriptorsBy(Config.OBJECT).next();
		Attribute att2 = d2.getAttribute("LOCATION");
		att2.setAttrValueAtSpan(
			new BoundingBox(20, 20, 20, 20),
			new Span(new Frame(5), new Frame(7)));
		assertEquals(v1, v2);
	}

	void testEqualityAndSerialization() {
		System.out.println("Testing equality and serialization");
		ViperData v = populateViperData(createAllConfigs(create()));
		StringWriter sw = new StringWriter();
		try {
			XmlSerializer.toWriter(v, new PrintWriter(sw));
		} catch (IOException iox) {
			assertTrue(iox.getMessage(), false);
		}
		StringReader sr = new StringReader(sw.toString());
		System.err.println("\n\n" + sw + "\n\n");
		InputSource is = new InputSource(sr);
		Element documentEl = null;
		try {
			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
			fact.setNamespaceAware(true);
			documentEl =
				fact.newDocumentBuilder().parse(is).getDocumentElement();
		} catch (IOException iox) {
			assertTrue(iox.getMessage(), false);
		} catch (SAXException sx) {
			assertTrue(sx.getMessage() + "\n" + sw.toString() + "\n", false);
		} catch (ParserConfigurationException pcx) {
			assertTrue(pcx.getMessage(), false);
		} catch (FactoryConfigurationError fce) {
			assertTrue(fce.getMessage(), false);
		}
		ViperData copy = new ViperParser().parseDoc(documentEl);
		try {
			if (false)
				XmlSerializer.toWriter(copy, new PrintWriter(System.err));
		} catch (IOException iox) {
			assertTrue(iox.getMessage(), false);
		}
		for (Iterator i = v.getAllConfigs().iterator(); i.hasNext();) {
			Config c = (Config) i.next();
			Config alt = copy.getConfig(c.getDescType(), c.getDescName());
			assertTrue("alt == " + alt, alt != null);
			assertTrue(c.getDescType() == alt.getDescType());
			assertEquals(c.getDescName(), alt.getDescName());
			assertTrue(alt.getNumberOfChildren() == c.getNumberOfChildren());
			for (Iterator iter = alt.getAttributeConfigs(); iter.hasNext();) {
				AttrConfig ac = (AttrConfig) iter.next();
				AttrConfig bc = c.getAttrConfig(ac.getAttrName());
				assertEquals(ac.getAttrName(), bc.getAttrName());
				assertEquals(ac.getAttrType(), bc.getAttrType());
				assertEquals(ac.getParams(), bc.getParams());
				assertTrue(ac.isDynamic() == bc.isDynamic());
				assertEquals(ac, bc);
			}
			assertEquals(c, alt);
		}
		assertTrue(copy.getConfigsNode().equals(v.getConfigsNode()));
		assertTrue(v.getConfigsNode().equals(copy.getConfigsNode()));
		assertTrue(copy.getSourcefilesNode().equals(v.getSourcefilesNode()));
		assertTrue(v.getSourcefilesNode().equals(copy.getSourcefilesNode()));
		assertTrue(copy.equals(v));
		assertTrue(v.equals(copy));
	}

	void testDataUndo() {
		ViperUndoableEvent last;
		ViperData v = populateViperData(createAllConfigs(create()));
		ViperData v2 = populateViperData(createAllConfigs(create()));
		ViperListener vdListener = new ViperListener();
		((EventfulNode) v).addNodeListener(vdListener);
		
		// Test removing sourcefile nodes
		Sourcefile a = v.getSourcefile("a.mpg");
		v.getSourcefilesNode().removeChild(a);
		last = (ViperUndoableEvent) vdListener.last;
		assertTrue(v.getSourcefile("a.mpg") == null);
		assertTrue(!v2.equals(v));
		last.getUndoable().undo();
		ViperChangeEvent second = vdListener.last;
		assertTrue(!second.equals(last));
		assertTrue(v.getSourcefile("a.mpg") != null);
		assertTrue(v2.equals(v));
		last.getUndoable().redo(); // Delete a.mpg again
		assertTrue(!vdListener.last.equals(last));
		assertTrue(!second.equals(last));
		assertTrue(v.getSourcefile("a.mpg") == null);
		assertTrue(!v2.equals(v));
		last.getUndoable().undo();
		assertTrue(v2.equals(v));

		// Test removing descriptors
		a = v.getSourcefile("a.mpg");
		Iterator iter = a.getDescriptorsBy(Config.OBJECT);
		Descriptor face = (Descriptor) iter.next();
		int size = a.getNumberOfChildren();
		a.removeChild(face);
		last = (ViperUndoableEvent) vdListener.last;
		assertTrue(a.getNumberOfChildren() == size-1);
		assertTrue(!v2.equals(v));
		assertTrue(null == a.getDescriptor(Config.OBJECT, face.getDescName(), face.getDescId()));
		last.getUndoable().undo();
		second = vdListener.last;
		assertTrue(!second.equals(last));
		assertTrue(a.getNumberOfChildren() == size);
		assertTrue(v2.equals(v));
		assertTrue(face.equals(a.getDescriptor(Config.OBJECT, face.getDescName(), face.getDescId())));
		last.getUndoable().redo(); // Delete a.mpg again
		assertTrue(!vdListener.last.equals(last));
		assertTrue(!second.equals(last));
		assertTrue(a.getNumberOfChildren() == size-1);
		assertTrue(!v2.equals(v));
		assertTrue(null == a.getDescriptor(Config.OBJECT, face.getDescName(), face.getDescId()));
		last.getUndoable().undo();
		assertTrue(v2.equals(v));
		
		// Test attribute value changes
		/// Static attributes
		Descriptor cut = (Descriptor) a.getDescriptorsBy(Config.CONTENT).next();
		cut.getAttribute("TYPE");

		
		
		/// Dynamic attributes
		face = a.getDescriptor(Config.OBJECT, face.getDescName(), face.getDescId());
		Attribute location = face.getAttribute("LOCATION");
		
		BoundingBox smallBox = new BoundingBox(20, 20, 20, 20);
		Span afterSpan = new Span(new Frame(21), new Frame(23));

		location.setAttrValueAtSpan(smallBox, afterSpan);
		last = (ViperUndoableEvent) vdListener.last;
		assertTrue(!v2.equals(v));
		last.getUndoable().undo();
		assertTrue(v2.equals(v));
		last.getUndoable().redo();
		assertTrue(!v2.equals(v));
		location.setAttrValueAtSpan(null, afterSpan);
		assertTrue(v2.equals(v));
	}
	
	void testDataEvents() {
		ViperChangeEvent last;
		ViperData v = populateViperData(createAllConfigs(create()));
		assertTrue(v instanceof EventfulNode);
		ViperListener vdListener = new ViperListener();
		((EventfulNode) v).addNodeListener(vdListener);

		assertTrue(v.getSourcefilesNode() instanceof EventfulNode);
		ViperListener snListener = new ViperListener();
		((EventfulNode) v.getSourcefilesNode()).addNodeListener(snListener);

		Sourcefile a = v.getSourcefile("a.mpg");
		assertTrue(a instanceof EventfulNode);
		ViperListener sfListener = new ViperListener();
		((EventfulNode) a).addNodeListener(sfListener);

		Iterator fileDescs = a.getDescriptorsBy(Config.FILE);
		assertTrue(fileDescs.hasNext());
		Descriptor fd = (Descriptor) fileDescs.next();
		assertTrue(!fileDescs.hasNext());
		assertTrue(fd instanceof EventfulNode);
		ViperListener fdListener = new ViperListener();
		((EventfulNode) fd).addNodeListener(fdListener);

		Attribute width = fd.getAttribute("H_FRAME_SIZE");
		assertTrue(width instanceof EventfulNode);
		ViperListener widthListener = new ViperListener();
		((EventfulNode) width).addNodeListener(widthListener);

		// Test some change events
		width.setAttrValue(new Integer(640));
		ViperListener[] listeners =
			new ViperListener[] {
				widthListener,
				fdListener,
				sfListener,
				snListener,
				vdListener };
		for (int i = 0; i < listeners.length; i++) {
			last = (ViperChangeEvent) listeners[i].last;
			assertTrue(
				"listeners[" + i + "].count == " + listeners[i].count,
				listeners[i].count == 1);
//			assertTrue(
//				((Attribute) last.getNewValue())
//					.getAttrValue()
//					.equals(
//					newAttrVal));
//			assertTrue(
//				((Attribute) last.getOldValue()).getAttrValue()
//					+ " =? null",
//				((Attribute) last.getOldValue()).getAttrValue()
//					== null);
//			assertTrue(last.getParent().equals(fd));
		}
		width.setAttrValue(new Integer(352));
		for (int i = 0; i < listeners.length; i++) {
			last = (NodeChangeEvent) listeners[i].last;
			assertTrue(
				"listeners[" + i + "].count == " + listeners[i].count,
				listeners[i].count == 2);
//			assertTrue(
//				"listeners["
//					+ i
//					+ "].last.getNewValue() == "
//					+ ((Attribute) last.getNewValue())
//						.getAttrValue(),
//				((Attribute) last.getNewValue())
//					.getAttrValue()
//					.equals(
//					newAttrVal));
//			assertTrue(
//				"listeners["
//					+ i
//					+ "].last.getOldValue() == "
//					+ ((Attribute) last.getOldValue())
//						.getAttrValue(),
//				((Attribute) last.getOldValue())
//					.getAttrValue()
//					.equals(
//					oldAttrVal));
			assertTrue(
				"listeners["
					+ i
					+ "].last.getParent() == "
					+ last.getParent(),
				last.getParent().equals(fd));
		}

		// Test removes
		a.removeChild(fd);
		assertTrue(listeners[0].count == 2);
		assertTrue(listeners[1].count == 2);
		for (int i = 2; i < listeners.length; i++) {
			last = (NodeChangeEvent) listeners[i].last;
			assertTrue(listeners[i].count == 3);
//			assertTrue(last.getNewValue() == null);
//			assertTrue(last.getOldValue().equals(fd));
			assertTrue(last.getParent().equals(a));
		}

		v.getSourcefilesNode().removeChild(a);
		assertTrue(listeners[0].count == 2);
		assertTrue(listeners[1].count == 2);
		assertTrue(listeners[2].count == 3);
		for (int i = 3; i < listeners.length; i++) {
			last = (NodeChangeEvent) listeners[i].last;
			assertTrue(listeners[i].count == 4);
//			assertTrue(last.getNewValue() == null);
//			assertTrue(last.getOldValue().equals(a));
			assertTrue(last.getParent().equals(v.getSourcefilesNode()));
		}
		// Test additions
		v.getSourcefilesNode().addChild(a);
		for (int i = 3; i < listeners.length; i++) {
			last = (NodeChangeEvent) listeners[i].last;
			assertTrue(listeners[i].count == 5);
//			assertTrue(last.getNewValue().equals(a));
//			assertTrue(last.getOldValue() == null);
			assertTrue(last.getParent().equals(v.getSourcefilesNode()));
		}

		assertTrue(v.getSourcefile("a.mpg") == a);
		// FIXME add tests for events on create methods, and finish adding
		//  checks on addNode methods.
	}

	void testConfigEvents() {
		NodeChangeEvent last;
		ViperData v = createAllConfigs(create());
		assertTrue(v instanceof EventfulNode);
		ViperListener vdListener = new ViperListener();
		((EventfulNode) v).addNodeListener(vdListener);

		assertTrue(v.getConfigsNode() instanceof EventfulNode);
		ViperListener cfgsListener = new ViperListener();
		((EventfulNode) v.getConfigsNode()).addNodeListener(cfgsListener);

		Config fic = v.getConfig(Config.FILE, "Information");
		assertTrue(fic instanceof EventfulNode);
		ViperListener ficListener = new ViperListener();
		((EventfulNode) fic).addNodeListener(ficListener);

		// Test some change events
		AttrConfig nac =
			fic.createAttrConfig(
				"SIZE",
				ViperData.ViPER_DATA_URI + "dvalue",
				false,
				null,
				null);
		ViperListener[] listeners =
			new ViperListener[] { ficListener, cfgsListener, vdListener };
		for (int i = 0; i < listeners.length; i++) {
			last = (NodeChangeEvent) listeners[i].last;
			assertTrue(
				"listeners[" + i + "].count == " + listeners[i].count,
				listeners[i].count == 1);
			assertEquals(last.getNewValue(), nac);
			assertTrue(
				"listeners["
					+ i
					+ "].last.getOldValue() = "
					+ last.getOldValue(),
				last.getOldValue() == null);
		}

		fic.removeChild(nac);
		for (int i = 0; i < listeners.length; i++) {
			last = (NodeChangeEvent) listeners[i].last;
			assertTrue(
				"listeners[" + i + "].count == " + listeners[i].count,
				listeners[i].count == 2);
			assertEquals(last.getOldValue(), nac);
			assertTrue(
				"listeners["
					+ i
					+ "].last.getNewValue() = "
					+ last.getNewValue(),
				last.getNewValue() == null);
		}

		AttrConfig xyac =
			fic.createAttrConfig(
				"DIMENSIONS",
				ViperData.ViPER_DATA_URI + "point",
				false,
				null,
				null);
		for (int i = 0; i < listeners.length; i++) {
			last = (NodeChangeEvent) listeners[i].last;
			assertTrue(
				"listeners[" + i + "].count == " + listeners[i].count,
				listeners[i].count == 3);
			assertEquals(last.getNewValue(), xyac);
			assertTrue(
				"listeners["
					+ i
					+ "].last.getOldValue() = "
					+ last.getOldValue(),
				last.getOldValue() == null);
		}
	}

	// FIXME add undo tests and combined data/config event tests

	private static class ViperListener implements NodeListener {
		ViperChangeEvent last = null;
		int count = 0;
		/**
		 * @see viper.api.extensions.NodeListener#nodeChanged(viper.api.extensions.NodeChangeEvent)
		 */
		public void nodeChanged(NodeChangeEvent nce) {
			System.err.println("Heard an event: " + nce);
			last = nce;
			count++;
		}

		/**
		 * @see viper.api.extensions.NodeListener#minorNodeChanged(viper.api.extensions.MinorNodeChangeEvent)
		 */
		public void minorNodeChanged(MinorNodeChangeEvent mnce) {
			System.err.println("Heard a minor event: " + mnce);
			last = mnce;
			count++;
		}

		/**
		 * @see viper.api.extensions.NodeListener#majorNodeChanged(viper.api.extensions.MajorNodeChangeEvent)
		 */
		public void majorNodeChanged(MajorNodeChangeEvent mnce) {
			System.err.println("Heard a major event: " + mnce);
			last = mnce;
			count++;
		}
	}
}
