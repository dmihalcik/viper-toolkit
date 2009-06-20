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
import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;

import viper.api.*;
import viper.api.impl.*;

import com.hp.hpl.jena.rdf.model.*;

import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.chronicle.extras.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolox.swing.*;

/**
 */
public class ViperDataCanvas extends PCanvas {
	private static Logger logger = Logger.getLogger("edu.umd.cfar.lamp.viper.gui.canvas");
	private ViperDataPLayer viperView;

	public ViperDataCanvas() throws IOException {
		super();
		this.setPreferredSize(new Dimension(360, 240));
		// Use the middle mouse button for panning
		ScrollWheelPanEventHandler swHandler = new ScrollWheelPanEventHandler();
		super.setPanEventHandler(swHandler);

		// Use the scroll wheel for zooming
		ScrollWheelZoomHandler zHandler 
			= new ScrollWheelZoomHandler() ;
		zHandler.setMaxScale( 16.0 ) ;
		zHandler.setMinScale( 1 / 16.0 ) ;
		super.setZoomEventHandler(zHandler);
		
		addFocusListener( new StopOnLoseFocusAdapter() ) ;
		cameraTransformHistory = new LinkedList();
		cameraTransformHistory.add(new PAffineTransform());
		cameraTransformCursor = 1;
		this.getCamera().addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, listenForZooms);
	}

	private class StopOnLoseFocusAdapter extends FocusAdapter
	{
		public void focusLost( FocusEvent e )
		{
//			System.out.println( "focusLost: Calling stop()" ) ;
			viperView.stop() ;
//			ViperViewMediator mediator = viperView.getMediator() ;
//			viper.api.time.Frame now = mediator.getCurrentFrame() ;
//			Object obj 
//			   = mediator.getSelection().getFirstAttribute().getAttrValueAtInstant(now) ;
//			if ( obj == null )
//			{
//				System.out.println( "focusLost: going into creator mode" ) ;
//			}
//			else
//			{
//				System.out.println( "focusLost: going into editor mode" ) ;
//			}
		}
	}
	
	public static void main(String[] args) throws RDFException, URISyntaxException {
		if (args.length != 2) {
			System.err.println("Args.length == " + args.length);
			printUsage();
			System.exit(2);
		}
		try {
			URI fileURI = new File(args[0]).toURI();
			URI prefsURI = new File(args[1]).toURI();
			ViperData data = new ViperParser().parseFromTextFile(fileURI);
			PrefsManager prefs = new PrefsManager();
			prefs.setSystemPrefs(prefsURI);
			// FIXME repath viperdata using properties and LostFileFinder to
			//   find right media files.
			// need a lookup table of sourcefile names to 
			//   java.io.Files. Right now needs correct absolute paths
			ViperViewMediator mediator = new ViperViewMediator();
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
			ViperDataCanvas root = new ViperDataCanvas();
			root.setViperView(new ViperDataPLayer(gen, mediator));
			JFrame container = new JFrame("Viper Data View");
			container.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			Rectangle r = container.getBounds();
			r.height = root.getPreferredSize().height;
			r.width = root.getPreferredSize().width;
			container.setBounds(r);
			PScrollPane scrollPane = new PScrollPane(root);
			container.getContentPane().add(scrollPane);
			container.validate();
			root.requestFocus();
			container.setVisible(true);
		} catch (IOException iox) {
			logger.log(Level.WARNING, "Error starting canvas.", iox);
		} catch (PreferenceException px) {
			logger.log(Level.WARNING, "Error while starting canvas", px);
		}
	}
	public static void printUsage() {
		System.err.println("Usage: vipercanvas <metadata.xml>");
	}

	public ViperDataPLayer getViperView() {
		return viperView;
	}
	
	private PInputEventListener dataInputListener = null;

	/**
	 * @param view
	 */
	public void setViperView(ViperDataPLayer view) {
		if ( viperView != null ) {
			removeInputEventListener(getDataInputListener()) ;
		}
		
		viperView = view;
		getLayer().removeAllChildren();
		if (viperView != null) {
			viperView.setMyCamera(this.getCamera());
			getLayer().addChild(viperView);
			getCamera().setViewConstraint(PCamera.VIEW_CONSTRAINT_ALL);

			PBounds b = viperView.getFullBounds();
			Rectangle r = b.getBounds();
			this.setPreferredSize(new Dimension(r.width + 12, r.height + 25));
			
			// Allow ViperDataPLayer to set background color
			viperView.setViperDataCanvas( this ) ;
			
			addInputEventListener(getDataInputListener()) ;
			getRoot().getDefaultInputManager().setKeyboardFocus(getDataInputListener());
		}
	}

	public ViperViewMediator getMediator() {
		if (viperView != null) {
			return viperView.getMediator();
		} else {
			return null;
		}
	}

	public void setMediator(ViperViewMediator mediator) throws IOException {
		if (viperView != null) {
			viperView.setMediator(mediator);
		} else if (mediator != null) {
			DataViewGenerator gen = new DataViewGenerator();
			gen.setPrefs(mediator.getPrefs());
			setViperView(new ViperDataPLayer(gen, mediator));
		} else {
			setViperView(null);
		}
	}

	private LinkedList cameraTransformHistory;
	private int cameraTransformCursor;
	
	/// listens for requests to set the zoom level
	private ActionListener setZoomLevelActionListener = new ActionListener() {
		/**
		 * Sets the zoom level to the amount specified in the event's
		 * action command property. 
		 */
		public void actionPerformed(ActionEvent e) {
			String zoomStr = e.getActionCommand();
			PCamera cam = getCamera();
			PAffineTransform currZoom = cam.getViewTransform();
			if (cameraTransformHistory.isEmpty()) {
				PAffineTransform identity = new PAffineTransform();
				cameraTransformHistory.add(identity);
			}
			try {
				// First update the history, then animate the change
				int newCursorLocation = -1;
				if ("first".equals(zoomStr)) {
					if (!cameraTransformHistory.get(cameraTransformHistory.size()-1).equals(currZoom)) {
						cameraTransformHistory.add(cam.getViewTransform());
					}
					newCursorLocation = 0;
				} else if ("previous".equals(zoomStr)) {
					if (cameraTransformCursor > 0) {
						newCursorLocation = cameraTransformCursor - 2;
					}
				} else if ("next".equals(zoomStr)) {
					if (cameraTransformCursor < cameraTransformHistory.size()) {
						newCursorLocation = cameraTransformCursor;
					}
				} else if ("end".equals(zoomStr)) {
					newCursorLocation = cameraTransformHistory.size() - 1;
				} else {
					// Not a transform history navigation command, but a 
					// zoom command. This involves will likely involve pushing a new
					// transform onto the history stack
					PAffineTransform newZoom = null;
					if ("actual".equals(zoomStr)) {
						newZoom = new PAffineTransform();
					} else if ("fit".equals(zoomStr)) {
						// XXX: zoom to fit should take into acount scroll bars, if they exist
						//  ^^^^ also, remember to be careful that on macs, the scroll bars are always visible
						Rectangle visibleR = getVisibleRect();
						PBounds fullBounds = getLayer().getFullBounds();
						double scaleX = visibleR.width / fullBounds.getWidth();
						double scaleY = visibleR.height / fullBounds.getHeight();
						double scale = 1;
						double x = fullBounds.x;
						double y = fullBounds.y;
						if (scaleX > scaleY) {
							scale = scaleY;
						} else {
							scale = scaleX;
						}
						newZoom = new PAffineTransform(AffineTransform.getScaleInstance(scale, scale));
					} else {
						double zoomFactor = currZoom.getScale();
						double x = currZoom.getTranslateX();
						double y = currZoom.getTranslateX();
						Point2D center = currZoom.inverseTransform(cam.getBoundsReference().getCenter2D(), null);
						double Y = cam.getBoundsReference().getCenterY();
						if (zoomStr.startsWith("x")) {
							zoomFactor *= Double.parseDouble(zoomStr.substring(1));
						} else if (zoomStr.endsWith("%")){
							zoomFactor = Double.parseDouble(zoomStr.substring(0, zoomStr.length()-1));
							zoomFactor /= 100.0;
							zoomFactor = zoomFactor / newZoom.getScale();
						} else {
							logger.log(Level.SEVERE, "Unable to zoom by specified amount: " + zoomStr);
							return;
						}
						newZoom = new PAffineTransform();
						newZoom.scaleAboutPoint(zoomFactor, center.getX(), center.getY());
					}
					if (newZoom != null) {
						if (newZoom.equals(currZoom)) {
							return;
						}
						if (cameraTransformCursor < cameraTransformHistory.size()) {
							if (!cameraTransformHistory.get(cameraTransformCursor-1).equals(currZoom)) {
								cameraTransformHistory.add(cameraTransformCursor, currZoom);
								cameraTransformCursor++;
							}
							if (!cameraTransformHistory.get(cameraTransformCursor-1).equals(newZoom)) {
								cameraTransformHistory.subList(cameraTransformCursor, cameraTransformHistory.size()).clear();
								newCursorLocation = cameraTransformHistory.size();
								cameraTransformHistory.add(newZoom);
							}
						} else {
							if (!cameraTransformHistory.getLast().equals(currZoom)) {
								cameraTransformHistory.add(currZoom);
							}
							newCursorLocation = cameraTransformHistory.size();
							cameraTransformHistory.add(newZoom);
						}
					}
				}
				if (0 <= newCursorLocation) {
					AffineTransform at = (PAffineTransform) cameraTransformHistory.get(newCursorLocation);
					cam.animateViewToTransform(at, 500);
					lastSetTransformTime = System.currentTimeMillis();
					cameraTransformCursor = newCursorLocation + 1;
				}
			} catch (IllegalArgumentException iax) {
				logger.log(Level.SEVERE, "Unable to zoom by specified amount: " + zoomStr, iax);
			} catch (NoninvertibleTransformException ntx) {
				logger.log(Level.SEVERE, "Unexpected error while setting zoom.", ntx);
			}
		}
		
	};
	private long lastSetTransformTime = 0;
	private PAffineTransform lastSetTransformValue = null;
	private PropertyChangeListener listenForZooms = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			long currTime = System.currentTimeMillis();
			if (currTime - 1024 > lastSetTransformTime && lastSetTransformValue != null) {
				if (!cameraTransformHistory.get(cameraTransformCursor-1).equals(lastSetTransformValue)) {
					cameraTransformHistory.add(cameraTransformCursor, lastSetTransformValue);
					cameraTransformCursor++;
				}
			}
			lastSetTransformValue = (PAffineTransform) evt.getNewValue();
			lastSetTransformTime = currTime;
		}};

	/**
	 * <dl>
	 *  <dt>first</dt>
	 *   <dd>Goes to the original zoom; like <code>actual</code>, but goes to start of list instead of adding to it.</dd>
	 *  <dt>previous</dt>
	 *   <dd>Goes to the last saved zoom point.</dd>
	 *  <dt>next</dt>
	 *   <dd>Goes to the next saved zoom point.</dd>
	 *  <dt>end</dt>
	 *   <dd>Goes to the last zoom in the zoom history.</dd>
	 *  <dt>actual</dt>
	 *   <dd>Puts the image at actual size in the upper-left corner.</dd>
	 *  <dt>fit</dt>
	 *   <dd>Fits the image to the window, centering it (or should it be in the upper-left corner?)</dd>
	 *  <dt>x<i>number</i></dt>
	 *   <dd>Multiplies the zoom by the specified amount, keeping the center point fixed</dd>
	 *  <dt><i>number</i>%</dt>
	 *   <dd>Sets the zoom to the specified amount, keeping the center point fixed.</dd>
	 * </dl>
	 * @return Returns the setZoomLevelActionListener.
	 */
	public ActionListener getSetZoomLevelActionListener() {
		return setZoomLevelActionListener;
	}
	public PInputEventListener getDefaultDataInputListener() {
		return viperView.viperEditorEventListener;
	}
	public PInputEventListener getDataInputListener() {
		if (dataInputListener == null) {
			return getDefaultDataInputListener();
		}
		return dataInputListener;
	}
	public void setDataInputListener(PInputEventListener dataInputListener) {
		removeInputEventListener(getDataInputListener());
		this.dataInputListener = dataInputListener;
		addInputEventListener(getDataInputListener()) ;
		getRoot().getDefaultInputManager().setKeyboardFocus(getDataInputListener());
	}
}
