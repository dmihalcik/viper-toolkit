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

package edu.umd.cfar.lamp.viper.gui.canvas;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.event.*;

import viper.api.*;
import viper.api.time.*;

import com.hp.hpl.jena.rdf.model.*;

import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolox.swing.*;

/**
 * This is a view of what a ViperViewMediator calls the 'majorMoment', hopefully
 * the frame of interest to the user.
 */
public class ViperDataPLayer extends PLayer {
	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas");

	static boolean debug = false;

	private ViperViewMediator mediator;

	private DataViewGenerator generator;

	private PImage currFrame;

	private PNode metadataNode; // parent of all nodes

	private MomentFocusListener mfl;

	// For notifying when display wrt changes
	private ViperDisplayWRTListener dwrtListener;

	// Keeps track of the last two time instants
	// oneBack is the previous time instance, and twoBack is the one
	// before that.
	private Instant oneBack = null, twoBack = null;

	private boolean currentlyRedrawingDisplay = false;

	static public void debugPrint(String s) {
		if (debug)
			System.out.println(s);
	}

	static public void debugErrPrint(String s) {
		if (debug)
			debugErrPrint(s);
	}

	static public void debugPrint(boolean alwaysPrint, String s) {
		if (true)
			return;
		if (alwaysPrint || debug)
			System.out.println(s);
	}

	static public void debugErrPrint(boolean alwaysPrint, String s) {
		if (true)
			return;
		if (alwaysPrint || debug)
			debugErrPrint(s);
	}

	boolean liveEditLocked = false;

	/**
	 * This lock is used while frames are changing as editing. Basically, this
	 * means that the attribute that currently has an editor should not be
	 * replaced when the frame is reset.
	 */
	public void setLiveEditLock() {
		liveEditLocked = true;
	}

	/**
	 * Indicate that it is okay to recycle the current editor when a frame
	 * change or external data modification notification comes in.
	 */
	public void releaseLiveEditLock() {
		liveEditLocked = false;
	}

	/**
	 * Tests to see if the live edit lock is set.
	 * 
	 * @return <code>true</code> when the current editor should not be
	 *         replaced
	 */
	public boolean isLiveEditLocked() {
		return liveEditLocked;
	}

	private final class DefaultViperCanvasInputEventListener implements PInputEventListener {
		public void processEvent(PInputEvent aEvent, int type) {
			if (mode.isCreateMode()) {
				creatorManager.processEvent(aEvent, type);
			} else if (mode.isEditMode()) {
				editManager.processEvent(aEvent, type);
			} else {
				logger.warning("Event without a home");
			}
		}
	}

	private class MomentFocusListener implements ViperMediatorChangeListener {
		public void dataChanged(ViperMediatorChangeEvent e) {
			resetView();
		}

		// For now, this event is changed only when the frame is
		// changed
		public void frameChanged(ViperMediatorChangeEvent e) {
			logger.config("FRAME CHANGED");
			resetView();
		}

		public void currFileChanged(ViperMediatorChangeEvent o) {
			resetView();
		}

		public void schemaChanged(ViperMediatorChangeEvent e) {
			resetView();
		}

		public void mediaChanged(ViperMediatorChangeEvent e) {
			resetFrame();
		}
	}

	private class ViperDisplayWRTListener extends DisplayWithRespectToAdapter {
		public ViperDisplayWRTListener() {
		}

		public void displayWRTEventOccurred(ChangeEvent event) {
			// When user turns on/off display with respect to
			// this method will handle the centering
			rescaleFrame();
		}
	}

	PInputEventListener viperEditorEventListener = new DefaultViperCanvasInputEventListener();
	
	private ViperEditorManager editManager = new ViperEditorManager(this);

	private ViperCreatorManager creatorManager = new ViperCreatorManager(this);

	TableSelectListener tableSelectListener = new TableSelectListener();

	TableVisibleListener tableVisibleListener = new TableVisibleListener();

	public ViperDataPLayer(DataViewGenerator gen, ViperViewMediator med)
			throws IOException {
		super();
		// logger.setLevel( Level.ALL ) ;
		// ConsoleHandler handler = new ConsoleHandler() ;
		// handler.setLevel( Level.ALL ) ;
		// logger.addHandler( handler ) ;
		Handler[] stuff = logger.getHandlers();
		debugPrint("*** num handlers " + stuff.length);
		// addInputEventListener( editManager ) ;

		this.generator = gen;
		this.mfl = new MomentFocusListener();
		this.dwrtListener = new ViperDisplayWRTListener();

		this.setMediator(med);
		med.getSelection().addChangeListener(tableSelectListener);
		med.getHiders().addChangeListener(tableVisibleListener);

		// parent to all pnodes in current frame
		metadataNode = new PNode();
		addChild(metadataNode);

		resetView();
	}

	private void resetView() {
		if (!currentlyRedrawingDisplay) {
			currentlyRedrawingDisplay = true;
			try {
				resetFrame();
				resetMetadata();
			} finally {
				currentlyRedrawingDisplay = false;
			}
		}
	}

	private String printMoment(Instant curr) {
		if (curr != null)
			return curr.toString();
		else
			return "X";
	}

	private void resetFrame() {
		Image img = null;
		if (mediator.getMajorMoment() != null
				&& mediator.getDataPlayer() != null) {
			img = mediator.getDataPlayer().getImage(mediator.getMajorMoment());
		}
		if (img == null) {
			if (currFrame != null) {
				removeChild(currFrame);
				currFrame = null;
			}
			return;
		} else if (currFrame == null) {
			currFrame = new PImage(img);
			addChild(0, currFrame);
		} else if (currFrame.getImage() != img) {
			currFrame.setTransform(new AffineTransform());
			currFrame.setImage(img);
		}
		Rational par = mediator.getDataPlayer().getPixelAspectRatio();
		if (!par.isZero() && !par.equals(1)) {
			// FIXME the pixel aspect ratio should be applied to the camera, not the image node
			if (par.lessThan(1)) {
				currFrame.setTransform(AffineTransform.getScaleInstance(1, par
						.reciprocate().doubleValue()));
			} else {
				currFrame.setTransform(AffineTransform.getScaleInstance(par
						.doubleValue(), 1));
			}
		}

		// Print out current frame
		Instant now = mediator.getMajorMoment();
		debugPrint("Frame is: " + printMoment(twoBack) + "-"
				+ printMoment(oneBack) + "-" + now);
		twoBack = oneBack;
		oneBack = now;
		rescaleFrame();
	}

	private PCamera myCamera;

	private AffineTransform oldTransform;

	private boolean lastDisplayWithRespectTo = false;

	PPath dummy = new PPath();

	private void allowForCentering() {
		Attribute a = mediator.getDisplayWRTManager().getAttribute();
		boolean currDisplayWithRespectTo = (a != null);
		// If we've changed in status
		if (lastDisplayWithRespectTo ^ currDisplayWithRespectTo) {
			// Update the information
			lastDisplayWithRespectTo = currDisplayWithRespectTo;
			// Did we just enter into this new mode
			if (currDisplayWithRespectTo) {
				// Get bounds for image in canvas
				PBounds bounds = currFrame.getBounds();

				// Create shape that is 3 times the width and height
				// whose origin is (-width, -height). This allows the
				// image to be centered in a 3*width, 3*height canvas
				// which should allow for centering
				double w = bounds.width, h = bounds.height;
				Rectangle2D rect = new Rectangle2D.Double(bounds.x - w,
						bounds.y - h, 3 * w, 3 * h);

				// Add the dummy shape to allow centering to work
				dummy.setBounds(rect);

				// Add as child to image
				addChild(dummy);

				// Set background color to make it obvious that we are
				// in the display with respect to mode
				canvas.setBackground(ColorUtilities.getColor("lightpink"));
			} else {
				// Remove dummy shape so the canvas is back to original
				// size
				removeChild(dummy);

				// Set background color back to the default white
				canvas.setBackground(ColorUtilities.getColor("white"));
			}
		}
	}

	private CanvasEditor findEditorForThisAttr(Attribute attr) {
		for (int i = 0; i < editors.size(); i++) {
			CanvasEditor editor = (CanvasEditor) editors.get(i);
			Attributable attrble = editor.getAttributable();
			if (attrble.getAttribute() == attr)
				return editor;
		}
		return null;
	}

	// TODO May need to set dwrtEditor to null when frame changes
	// since this editor may not appear in subsequent frames
	CanvasEditor dwrtEditor = null;

	private void rescaleFrame() {
		// This is the attribute selected by the user (shows up as red)
		Attribute a = mediator.getDisplayWRTManager().getAttribute();
		allowForCentering();
		if (myCamera != null && a == null) {
			// System.out.println( "Got here" ) ;
			// No attribute is dwrt
			if (dwrtEditor != null)
				dwrtEditor.setDisplayWRTmode(true);
			// if ( dwrtEditor != theEditor )
			// dwrtEditor.displayUnselected() ;
			// else
			// dwrtEditor.displaySelected() ;
		}

		else if (myCamera != null && a != null) {
			// Attribute contains values over entire span of video
			// Get the value at the current instant (call to mediator does this)
			Object attrVal = a
					.getAttrValueAtInstant(mediator.getCurrentFrame());

			if (attrVal instanceof HasCentroid) {
				CanvasEditor editor = findEditorForThisAttr(a);

				setDwrtEditor(editor);

				Pnt centroid = ((HasCentroid) attrVal).getCentroid();
				// animateViewToCenterBounds requires a rectangle
				// It centers the camera (which views the canvas) at the
				// center of the rectangle (which should be "centroid")
				// So create a tiny rectangle centered about the centroid
				Rectangle2D.Double rect = new Rectangle2D.Double();
				rect.x = centroid.pointValue().getX() - 1;
				rect.y = centroid.pointValue().getY() - 1;
				rect.width = 2;
				rect.height = 2;

				// Call method to center camera at centroid
				myCamera.animateViewToCenterBounds(rect, false, 0);
				return;
			}
			// if (oldTransform != null) {
			// myCamera.setViewTransform(oldTransform);
			// } else {
			// oldTransform = myCamera.getViewTransform();
			// }
			// if (a != null) {
			// Instant i = mediator.getDisplayedWithRespectToInstant();
			// Object value = a.getAttrValueAtInstant(i);
			// if (value instanceof BoxInformation) {
			// BoxInformation bi = (BoxInformation) value;
			// BoxInformation bin = (BoxInformation)
			// a.getAttrValueAtInstant(mediator.getCurrentFrame());
			// if (bi != null && bin != null) {
			// double tx = bin.getX() - bi.getX();
			// double ty = bin.getY() - bi.getY();
			// double r = Math.toRadians(bin.getRotation() - bi.getRotation());
			// double sw = bi.getWidth() / bin.getWidth();
			// double sh = bi.getHeight() / bin.getHeight();
			// AffineTransform at = (AffineTransform) oldTransform.clone();
			// at.concatenate(AffineTransform.getTranslateInstance(-tx, -ty));
			// at.concatenate(AffineTransform.getScaleInstance(sw, sh));
			// at.concatenate(AffineTransform.getRotateInstance(r));
			// at.concatenate(AffineTransform.getTranslateInstance(tx, ty));
			//						
			// myCamera.setViewTransform(at);
			// }
			// }
			// } else {
			// oldTransform = null;
			// }
		}
	}

	private void resetMetadata() {
		logger.finer(StringHelp.banner("RESET METADATA", 30));
		debugPrint(true, "RESET METADATA");

		// Clear current display of all shapes
		metadataNode.removeAllChildren();

		// Remove editors from this page
		clearEditors();
		if (mediator == null || mediator.getMajorMoment() == null
				|| mediator.getCurrFile() == null) {
			currFrame = null;
			return;
		}

		boolean added = false;
		Instant now = mediator.getMajorMoment();
		Iterator iter = mediator.getCurrFile().getDescriptorsBy(now);
		NodeVisibilityManager H = mediator.getHiders();
		while (iter.hasNext()) {
			Descriptor currDesc = (Descriptor) iter.next();
			if (NodeVisibilityManager.HIDDEN == H
					.getDescriptorVisibility(currDesc)) {
				continue;
			}
			Iterator ats = currDesc.getAttributes();
			added = false;
			while (ats.hasNext()) {
				Attribute currAttr = (Attribute) ats.next();
				int v = H.getAttributeVisibility(currAttr);
				if (NodeVisibilityManager.HIDDEN == v) {
					continue;
				}
				AttrConfig cfg = currAttr.getAttrConfig();
				// System.out.println( "ViperDataPLayer: " ) ;
				// System.out.println( "---attrType = " + cfg.getAttrType()
				// ) ;
				Object here = currAttr.getAttrValueAtInstant(now);
				if (here == null) {
					continue;
				}
				Attributable obj = getAttributable(cfg.getAttrType(), currAttr);
				if (isLiveEditLocked()
						&& theEditor != null
						&& obj != null
						&& theEditor.getAttributable().getAttribute() == obj
								.getAttribute()) {
					// Keep the same editor as last frame
					PNode pnode = (PNode) theEditor.getAttributable();
					// Key step: Add the Attributable to the canvas
					metadataNode.addChild(pnode);
					addEditor(theEditor);
					added = true;
				} else if (obj instanceof PNode) {
					boolean l = v == NodeVisibilityManager.LOCKED;
					boolean s = mediator.getSelection().isSelected(currAttr);
					obj
							.setDisplayProperties(s ? (l ? HighlightSingleton.STYLE_LOCKED_SELECTED
									: HighlightSingleton.STYLE_SELECTED)
									: (l ? HighlightSingleton.STYLE_LOCKED_UNSELECTED
											: HighlightSingleton.STYLE_UNSELECTED));
					addEditorToPLayer(obj, cfg.getAttrType());
				} else if (obj == null) {
					// Most likely not a visible node
					logger.finest("No PNode type found for for "
							+ cfg.getAttrType());
				} else {
					logger
							.severe("Error in code: received non-PNode from getViewable for "
									+ here);
				}
			}
		}
		if (isLiveEditLocked()) {
			// If the editor wasnt added, then the object isn't in
			// in the current frame. If the editor is not null, then
			// add it to the current frame.
			if (theEditor != null) {
				if (!added) {
					// Keep the same editor as last frame
					PNode pnode = (PNode) theEditor.getAttributable();
					// Key step: Add the Attributable to the canvas
					metadataNode.addChild(pnode);
					addEditor(theEditor);
					added = true;
				}

				// Calls resetMetadata
				debugPrint(true, "BEFORE updateAttributable");
				theEditor.updateAttributable(twoBack, oneBack, (Instant) now
						.next());
				debugPrint(true, "AFTER updateAttributable");
			}
			// There may be something already selected, so highlight it
			debugPrint(true, "BEFORE highlightSelectedEditor");
			highlightSelectedEditor(mediator.getSelection(),
					"RESET METADATA FRAME");
			debugPrint(true, "AFTER highlightSelectedEditor");
		} else {
			// There may be something already selected, so highlight it
			boolean editorHighlighted = highlightSelectedEditor(mediator
					.getSelection(), "RESET METADATA");

			// If no editor is picked, set it to NULL
			if (!editorHighlighted) {
				debugPrint(true, "NO EDITOR WAS PICKED FOR ");
				theEditor = null;
				resetSelectedAttribute();
			}
			// FIXME get directly through mediator to allow filters
			// Collection descs =
			// mediator.getCurrFile().getDescByInstant(mediator.getMajorMoment());
		}

		boolean isDWRTon = checkDwrtEditor(mediator.getDisplayWRTManager()
				.getAttribute());
		if (!isDWRTon) {
			dwrtEditor = null;
		}
	}

	/**
	 * Gets the Attributable PNode. Based on the attrType.
	 * 
	 * @param attrType
	 *            The name of a "shape" like obox, bbox, etc
	 * @param currAttr
	 *            The attribute, which allows access to current value, in this
	 *            frame
	 * @return Attributable is basically a PNode combined with an Attribute.
	 *         This is placed on the canvas
	 */
	public Attributable getAttributable(String attrType, Attribute currAttr) {
		Attributable node = null;
		Resource attrTypeR = mediator.getPrefs().model.getResource(attrType);
		Statement stmt = attrTypeR.getProperty(GT.visualNode);
		if (stmt != null) {
			try {
				RDFNode r = stmt.getObject();
				if (r instanceof Resource) {
					node = (Attributable) mediator.getPrefs().getCore()
							.rdfNodeToValue(r, currAttr);
				} else {
					Class c = Class.forName(r.toString());
					Constructor con = c
							.getConstructor(new Class[] { ViperViewMediator.class });
					node = (Attributable) con
							.newInstance(new Object[] { mediator });
				}
				node.setAttribute(currAttr);
			} catch (PreferenceException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			} catch (ClassNotFoundException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			} catch (SecurityException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			} catch (NoSuchMethodException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			} catch (IllegalArgumentException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			} catch (InstantiationException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			} catch (IllegalAccessException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			} catch (InvocationTargetException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			}
		}
		return node;
	}

	/**
	 * An editor manages the mouse events, and updates the Attributable which is
	 * contained in the editor
	 * 
	 * @param attr
	 *            Attributable is a combination of a PPath plus an Attribute
	 * @param attrType
	 *            This indicates what kind of Attribute, e.g., obox, bbox, etc
	 */
	public void addEditorToPLayer(Attributable attr, String attrType) {
		PNode pnode = (PNode) attr;
		// Key step: Add the Attributable to the canvas
		metadataNode.addChild(pnode);
		CanvasEditor editor = null;
		Resource attrTypeR = mediator.getPrefs().model.getResource(attrType);
		Statement stmt = attrTypeR.getProperty(GT.visualEditor);
		if (stmt != null) {
			RDFNode r = stmt.getObject();
			try {
				if (r instanceof Resource) {
					editor = (CanvasEditor) mediator.getPrefs().getCore()
							.rdfNodeToValue(r, attr);
				} else {
					Class c = Class.forName(r.toString());
					Constructor con = c
							.getConstructor(new Class[] { Attributable.class });
					editor = (CanvasEditor) con
							.newInstance(new Object[] { attr });
				}
				editor.setMediator(mediator);
				addEditor(editor);
			} catch (PreferenceException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			} catch (ClassNotFoundException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			} catch (SecurityException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			} catch (NoSuchMethodException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			} catch (IllegalArgumentException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			} catch (InstantiationException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			} catch (IllegalAccessException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			} catch (InvocationTargetException e) {
				logger.log(Level.SEVERE, "Unable to load editor for "
						+ attrType, e);
			}
		} else {
			logger.config("No visual editor for " + attrType);
		}
	}

	private PNode samplebbox() {
		BboxNodeWrapper wr = new BboxNodeWrapper();
		BoundingBox bbox = new BoundingBox();
		bbox.set(0, 0, 600, 300);
		return wr.getViewable(bbox);
	}

	public static void printUsage() {
		debugErrPrint("Usage: viperview <metadata.xml>");
	}

	/**
	 * Create a simple view of the viper file passed in the command line
	 * argument. Attempts to create a view of the first sourcefile found in the
	 * metadatafile, searching the user's file system if necessary.
	 */
	public static void main(String[] args) throws URISyntaxException {
		if (args.length != 1) {
			debugErrPrint("Args.length == " + args.length);
			printUsage();
			System.exit(2);
		}
		try {
			URI fileURI = new File(args[0]).toURI();
			DataViewGenerator gen = new DataViewGenerator();
			gen.addViewConverter("bbox", new BboxNodeWrapper());
			// FIXME repath viperdata using properties and LostFileFinder to
			// find right media files.
			// need a lookup table of sourcefile names to
			// java.io.Files. Right now needs correct absolute paths

			ViperViewMediator m = new ViperViewMediator();
			m.setFileName(fileURI);
			String firstFile = ((Sourcefile) m.getViperData()
					.getAllSourcefiles().get(0)).getReferenceMedia()
					.getSourcefileName();
			m.setFocalFile(new URI(firstFile));

			ViperDataPLayer viperView = new ViperDataPLayer(gen, m);
			PCanvas root = new PCanvas();
			root.getLayer().addChild(viperView);
			root.getCamera().setViewConstraint(PCamera.VIEW_CONSTRAINT_ALL);

			JFrame container = new JFrame("Viper Data View");
			container.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			PBounds b = viperView.getFullBounds();
			debugErrPrint("Bounds are " + b);
			Rectangle r = b.getBounds();
			r.setSize(r.width + 12, r.height + 25);
			// don't know why, but seems to start with the box a little too
			// small.
			container.setBounds(r);
			PScrollPane scrollPane = new PScrollPane(root);
			container.getContentPane().add(scrollPane);
			container.validate();
			root.requestFocus();
			container.setVisible(true);
		} catch (IOException iox) {
			iox.printStackTrace();
		}
	}

	public ViperViewMediator getMediator() {
		return mediator;
	}

	public CanvasCreator getActiveCreator() {
		return theCreator;
	}

	public int numEditors() {
		return editors.size();
	}

	public void setMediator(ViperViewMediator mediator) {
		if (null != this.mediator) {
			this.mediator.removeViperMediatorChangeListener(this.mfl);
			this.mediator.getSelection().removeChangeListener(
					tableSelectListener);
			this.mediator.getHiders()
					.removeChangeListener(tableVisibleListener);
			this.mediator.getDisplayWRTManager().removeDisplayWRTListener(
					dwrtListener);
		}
		this.mediator = mediator;
		if (null != this.mediator) {
			this.mediator.addViperMediatorChangeListener(this.mfl);
			this.mediator.getSelection().addChangeListener(tableSelectListener);
			this.mediator.getHiders().addChangeListener(tableVisibleListener);
			this.mediator.getDisplayWRTManager().addDisplayWRTListener(
					dwrtListener);
		}
	}

	/*
	 * @author clin
	 */
	ArrayList editors = new ArrayList(); // keeps track of the various shapes

	public void clearEditors() {
		editors.clear();
	}

	/**
	 * Add editor object to current frame
	 * 
	 * @param e
	 *            The editor object being added
	 */
	public void addEditor(CanvasEditor e) {
		if (e != null)
			editors.add(e);
	}

	// Currently selected editable shape
	CanvasEditor theEditor = null;

	CanvasCreator theCreator = null;

	private class CreatorAssistantImpl implements CreatorAssistant {
		PNode shapeDrawn = null;

		/**
		 * Adds shape to canvas so you can see and edit it
		 */
		public void addShape(PNode node) {
			shapeDrawn = node;
			metadataNode.addChild(node);
		}

		/**
		 * Called to set the shape to the attribute value This will cause the
		 * mediator to reload everything on the frame, so we should remove the
		 * node.
		 * 
		 * @param obj
		 *            the value to set in the attribute
		 * @param selectedAttr
		 *            the attribute to modify.
		 */
		public void setAttrValueInMediator(Object obj, Attribute selectedAttr) {
			debugPrint(true, "  ENTERING setAttrValueInMediator");
			// careful - this will fire a data change event iff the data is
			// changed
			mediator.setAttributeValueAtCurrentFrame(obj, selectedAttr);
			// now select the editor
			int edCount = editors.size();
			debugPrint(true, "   setAttrValueInMediator  editors = " + edCount);
			for (int i = 0; i < edCount; i++) {
				CanvasEditor editor = (CanvasEditor) editors.get(i);
				Attributable attr = editor.getAttributable();
				Attribute attribute = attr.getAttribute();
				// TO DO: check to see if it should be equals()
				if (attribute == selectedAttr) {
					setActiveEditor(editor);
					break;
				}
			}
		}

		public void switchListener() {
			debugErrPrint(true, "In switchListener()");
			removeShape();
			switchToEditorManager();
			theCreator = null;
			invalidatePaint();
		}

		/**
		 * 
		 */
		public void removeShape() {
			if (shapeDrawn != null
					&& metadataNode.equals(shapeDrawn.getParent()))
				metadataNode.removeChild(shapeDrawn);
		}
	}

	class Mode {
		int mode;

		public static final int CREATE_MODE = 0;

		public static final int EDIT_MODE = 1;

		public Mode(int initMode) {
			mode = initMode;
		}

		public void setEditMode() {
			mode = EDIT_MODE;
			theCreator = null;
		}

		public void setCreateMode() {
			mode = CREATE_MODE;
			theEditor = null;
		}

		public void setEditMode(String debug) {
			debugPrint(true, "Converting to EDIT mode");
			debugPrint(true, debug);
			setEditMode();
		}

		public void setCreateMode(String debug) {
			debugPrint(true, "Converting to CREATE mode");
			debugPrint(true, debug);
			setCreateMode();
		}

		public boolean isEditMode() {
			return mode == EDIT_MODE;
		}

		public boolean isCreateMode() {
			return mode == CREATE_MODE;
		}

		public String toString() {
			if (isEditMode())
				return "EDIT_MODE";
			else
				return "CREATE_MODE";
		}
	}

	Mode mode = new Mode(Mode.EDIT_MODE);

	private void switchToCreatorManager() {
		if (mode.isEditMode()) {
			mode.setCreateMode("switchToCreatorManager");
		}
	}

	private void switchToEditorManager() {
		if (mode.isCreateMode()) {
			mode.setEditMode("Switching to select manager");
		}
	}

	private boolean highlightSelectedEditor(ViperSelection selectedAttr,
			String mesg) {
		boolean isEditorSelected = false;
		debugPrint(true, "***highlightSelected: " + mesg);
		if (!selectedAttr.isEmpty()) {
			debugPrint(true, "***highlightSelected, selectedAttr not null");
			int edCount = editors.size();
			debugPrint(true, "There are currently " + edCount + " editors.");
			for (int i = 0; i < edCount; i++) {
				CanvasEditor editor = (CanvasEditor) editors.get(i);
				Attributable attr = editor.getAttributable();
				Attribute attribute = attr.getAttribute();
				// TO DO: check to see if it should be equals()
				if (selectedAttr.isSelected(attribute)) {
					debugPrint(true, "MATCHES");
					setActiveEditor(editor);
					isEditorSelected = true;
					break;
				}
			}
		}
		return isEditorSelected;
	}

	/**
	 * @param attribute
	 * @return true if this attribute is being display with respect to
	 */
	private boolean checkDwrtEditor(Attribute attrIn) {
		boolean isDwrtOn = false;
		if (attrIn != null) {
			debugPrint(true, "***highlightSelected, attribute not null");
			int edCount = editors.size();
			for (int i = 0; i < edCount; i++) {
				CanvasEditor editor = (CanvasEditor) editors.get(i);
				Attributable attr = editor.getAttributable();
				Attribute attribute = attr.getAttribute();
				// TO DO: check to see if it should be equals()
				if (attribute.equals(attrIn)) {
					setDwrtEditor(editor);
					isDwrtOn = true;
					break;
				}
			}
		}
		return isDwrtOn;
	}

	private void resetSelectedAttribute() {
		// Clear any selected objects
		editManager.unselectAll();
		if (!mediator.getPrimarySelection().isFilteredBy(Attribute.class)) {
			// XXX unselectAll doesn't remove theCreator
			switchToEditorManager();
			return;
		}

		boolean isEditorSelected = highlightSelectedEditor(mediator
				.getSelection(), "resetSelected");
		// if no editor is associated with the selected attribute,
		// this means there's a null value associated with the attribute
		// (which is NOT the same a null attribute) then you want to
		// create that shape on canvas. This sets up a creator object
		// to assist in drawing that shape.
		debugPrint(true, "resetSelectedAttribute");
		if (!isEditorSelected) {
			debugPrint(true, "resetSelectedAttribute");
			setNewCreator(mediator.getPrimarySelection().getFirstAttribute());
		}
	}

	private void setNewCreator(Attribute selectedAttr) {
		if (theCreator != null && selectedAttr.equals(theCreator.getAttribute()) && selectedAttr.getRoot() != null) {
			return;
		}
		CanvasCreator newCreator = null;
		
		String attrType = selectedAttr.getAttrConfig().getAttrType();
		Resource attrTypeR = mediator.getPrefs().model.getResource(attrType);
		Statement stmt = attrTypeR.getProperty(GT.visualCreator);
		if (stmt != null) {
			try {
				RDFNode r = stmt.getObject();
				if (r instanceof Resource) {
					newCreator = (CanvasCreator) mediator.getPrefs().getCore()
							.rdfNodeToValue(r, selectedAttr);
				} else {
					Class c = Class.forName(r.toString());
					Constructor con = c.getConstructor(new Class[] {
							CreatorAssistant.class, Attribute.class });
					newCreator = (CanvasCreator) con.newInstance(new Object[] {
							new CreatorAssistantImpl(), selectedAttr });
				}
			} catch (PreferenceException e) {
				logger.log(Level.SEVERE, "Unable to load creator for "
						+ attrType, e);
			} catch (ClassNotFoundException e) {
				logger.log(Level.SEVERE, "Unable to load creator for "
						+ attrType, e);
			} catch (SecurityException e) {
				logger.log(Level.SEVERE, "Unable to load creator for "
						+ attrType, e);
			} catch (NoSuchMethodException e) {
				logger.log(Level.SEVERE, "Unable to load creator for "
						+ attrType, e);
			} catch (IllegalArgumentException e) {
				logger.log(Level.SEVERE, "Unable to load creator for "
						+ attrType, e);
			} catch (InstantiationException e) {
				logger.log(Level.SEVERE, "Unable to load creator for "
						+ attrType, e);
			} catch (IllegalAccessException e) {
				logger.log(Level.SEVERE, "Unable to load creator for "
						+ attrType, e);
			} catch (InvocationTargetException e) {
				logger.log(Level.SEVERE, "Unable to load creator for "
						+ attrType, e);
			}
		}
		
		if (newCreator != null) {
			if (theCreator != null) {
				theCreator.stop();
				theCreator = null;
			}
			theCreator = newCreator;
			switchToCreatorManager();
		} else {
			logger.config("No creator specified for " + attrType);
		}
	}

	private class TableSelectListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			debugPrint("$$$$ resetSelectedAttribute");
			// System.out.println( "TableSelectListner: stateChanged") ;
			resetSelectedAttribute();
		}

	}

	private class TableVisibleListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			resetMetadata();
		}
	}

	public void setActiveEditor(CanvasEditor editor) {
		// Unhighlight previous shape
		if (theEditor != null) {
			// System.out.println( "Display inactive" ) ;
			theEditor.setSelected(false);
		}
		theEditor = editor;
		// Highlight shape selected
		if (theEditor != null) {
			debugPrint(true, "In setActiveEditor");
			theEditor.setSelected(true);
			switchToEditorManager();
			debugPrint("EDITOR selected: " + theEditor.getName());
		}
		invalidatePaint();
	}

	public void setDwrtEditor(CanvasEditor editor) {
		// Unhighlight previous shape
		if (dwrtEditor != null) {
			// System.out.println( "Display inactive" ) ;
			dwrtEditor.setDisplayWRTmode(false);
		}
		dwrtEditor = editor;
		// Highlight shape selected
		if (dwrtEditor != null) {
			dwrtEditor.setDisplayWRTmode(true);
		}
	}

	public CanvasEditor getEditorAt(int index) {
		return (CanvasEditor) editors.get(index);
	}

	public CanvasEditor getActiveEditor() {
		return theEditor;
	}

	/**
	 * @return
	 */
	public ViperData getViperData() {
		if (mediator != null) {
			return mediator.getViperData();
		} else {
			return null;
		}
	}

	/**
	 * 
	 */
	public void stop() {
		// TODO Auto-generated method stub
		if (theCreator != null) {
			// System.out.println( "ViperDataPLayer: Calling stop" ) ;
			theCreator.stop();
		}
	}

	/**
	 * @return
	 */
	public PCamera getMyCamera() {
		return myCamera;
	}

	/**
	 * @param camera
	 */
	public void setMyCamera(PCamera camera) {
		myCamera = camera;
	}

	/**
	 * @param remote
	 *            Mostly used to change background color of canvas when display
	 *            with respect to is active
	 */
	ViperDataCanvas canvas;

	public void setViperDataCanvas(ViperDataCanvas canvas) {
		this.canvas = canvas;
	}

	public ViperDataCanvas getViperDataCanvas() {
		return canvas;
	}
}