package edu.umd.cfar.lamp.viper.gui.canvas;

import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.logging.*;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.gui.canvas.datatypes.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;

/**
 * @author clin
 */
// Should this be a Singleon class?
public class ViperEditorManager extends PInputManager {
	private static final boolean CLICKS_PAUSE_VIDEO = false;

	private static Logger logger = Logger
			.getLogger("edu.umd.cfar.lamp.viper.gui.canvas");

	ViperDataPLayer layer = null;


	private static int SELECT_THRESHOLD = 15;

	private static int CYCLE_THRESHOLD = 10;

	Point2D saveSelectPoint;

	// Checks status of mouse for purpose of dragging while
	// frame is changing

	public ViperEditorManager(ViperDataPLayer layerIn) {
		layer = layerIn;
	}

	private CanvasEditor getActiveEditor() {
		return layer.getActiveEditor();
	}

	private ViperViewMediator getMediator() {
		return layer.getMediator();
	}
	
	/**
	 * This method pauses the mediator, if possible, and 
	 * is called by the mouse click and mouse pressed methods
	 * automatically. 
	 */
	public void pauseIfPossible() {
		ViperViewMediator m = layer.getMediator();
		if (m != null && m.getPlayControls() != null) {
			m.getPlayControls().humanPause();
		}
	}

	boolean isKeySet = false;

	private void doKeyboardStuff(PInputEvent e) {
		if (!isKeySet) {
			e.getInputManager().setKeyboardFocus(this);
			isKeySet = true;
		}
	}

	public void mousePressed(PInputEvent e) {
		if (CLICKS_PAUSE_VIDEO) {
			pauseIfPossible();
		}
		// Locate the nearest shape
		doKeyboardStuff(e);
		if ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == 0) return;

		layer.setLiveEditLock();
		boolean cycleToNext = false;
		if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
			cycleToNext = true;
		}
		
		Point2D select = e.getPosition();
		logger.finer("----------------------mouse Pressed");
		logger.fine("EditorManager: mouseClicked " + select);

		//		debugErrPrint( "cycle = " + cycleToNext ) ;

		// TODO Check for cycle selection after editing

		if (cycleToNext && selectSave != null
				&& selectSave.distance(select) < CYCLE_THRESHOLD) {
			logger.finer("In cycling: selectIndex = " + selectIndex);
			CanvasEditor nextEditor = editorArray[selectIndex];
			selectIndex = (selectIndex + 1) % numValidEditors;
			layer.setActiveEditor(nextEditor);
			getActiveEditor().mousePressed(e);
			return;
		}
		// Don't pass events to shape if it's in DWRT mode, but only
		// if this shape has been selected (if another shape is selected,
		// pass mouse event to new shape)
		if (getActiveEditor() != null && getActiveEditor().isLocked(e)
				&& getActiveEditor().inRangeOfInterest(select)) {
			// this is to turn off the mouseIsPressed stuff
			layer.releaseLiveEditLock();
			return;
		}
		// Check to see if editor is interested in the mousePressed
		// event
		boolean newEditorSelected = false;
		if (getActiveEditor() == null
				|| !getActiveEditor().inRangeOfInterest(select)) {
			// No, it's not interested
			layer.setActiveEditor(findNearestShape(select, SELECT_THRESHOLD));
			newEditorSelected = true;
		}

		// Check to make sure the editor isn't locked
		if (getActiveEditor() != null && newEditorSelected) {
			if (getActiveEditor().isDisplayWRTmode()) {
				// this is to turn off the mouseIsPressed stuff
				layer.releaseLiveEditLock();
				// Need to update the spreadsheet to indicate this is now
				// the active attribute. This only has to be done with
				// DWRT editor that is selected. Normally, attribute
				// is selected by updating the data to that attribute, but
				// since editing can't be done on the DWRT/selected attribute,
				// we need to call this method explicitly.
				Attribute attrToSelect = layer.getActiveEditor().getAttributable()
						.getAttribute();
				getMediator().getSelection().setTo(attrToSelect);
				return;
			} else if (getActiveEditor().isLocked(e)) {
				return;
			}
		}

		if (getActiveEditor() == null) {
			// No editor found
			logger.finer("CLEAR >>>>>>>>>>>>");
			Config cfg = getMediator().getPrimarySelection().getFirstConfig();
			getMediator().getSelection().setTo(cfg);
			return;
		} else {
			getActiveEditor().mousePressed(e);
		}
	}

	public void mouseMoved(PInputEvent e) {
		doKeyboardStuff(e);
		CanvasEditor editor = getActiveEditor();
		if (editor == null || editor.isLocked(e)) {
			return;
		}
		editor.mouseMoved(e);
	}

	public void mouseDragged(PInputEvent e) {
		doKeyboardStuff(e);
		// System.out.println( "Mouse dragged" ) ;
		CanvasEditor editor = getActiveEditor();
		if (editor == null || editor.isLocked(e))
			return;
		editor.mouseDragged(e);
	}

	public void mouseReleased(PInputEvent e) {
		doKeyboardStuff(e);
		logger.finer("----------------------mouse Released");
		logger.fine("Mouse released " + position(e.getPosition()));
		layer.releaseLiveEditLock();

		if (getActiveEditor() == null)
			return;

		// Don't pass events to shape if it's in DWRT mode
		if (getActiveEditor().isLocked(e))
			return;

		getActiveEditor().mouseReleased(e);
		Attributable a = getActiveEditor().getAttributable();
		logger.fine("Updating attributable");
		Instant now = getMediator().getMajorMoment();
		getActiveEditor().updateAttributable(now, now, (Instant) now.next());
		logger.fine("There are " + layer.numEditors() + " editors");
	}

	public String position(Point2D pt) {
		String s = "( " + pt.getX() + ", " + pt.getY() + " )";
		return s;
	}

	public void unselectAll() {
		logger.fine("unselect all called");

		if (getActiveEditor() == null)
			return;

		// TODO Check this again
		getActiveEditor().setSelected(false);
		layer.setActiveEditor(null);
	}

	CanvasEditor findNearestShape(Point2D select) {
		return findNearestShape(select, -1.0);
	}

	CanvasEditor[] editorArray;

	int selectIndex;

	int numValidEditors;

	Point2D selectSave;

	CanvasEditor findNearestShape(Point2D select, double threshold) {
		if (layer.numEditors() == 0)
			return null;

		// Make an array of editors so it can sort
		editorArray = new CanvasEditor[layer.numEditors()];
		for (int i = 0; i < layer.numEditors(); i++) {
			editorArray[i] = layer.getEditorAt(i);
			//			System.out.println( "Editor is: " + editorArray[ i ].toString() )
			// ;
		}

		Arrays.sort(editorArray, (Comparator) new EditorComparator(select));
		numValidEditors = countNumValidEditors(select, threshold);
		logger.fine("num valid editors = " + numValidEditors);
		if (threshold == -1) {
			// set index ready for next
			selectIndex = (selectIndex + 1) % numValidEditors;
			selectSave = select;
			return editorArray[0];
		} else {
			if (validEditor(0, select, threshold)) {
				// set index ready for next
				selectIndex = (selectIndex + 1) % numValidEditors;
				selectSave = select;
				return editorArray[0];
			} else // not near any shape and not inside
			{
				selectSave = null;
				return null;
			}
		}
	}

	boolean validEditor(int index, Point2D select, double threshold) {
		return editorArray[index].contains(select)
				|| editorArray[index].minDist(select) <= threshold;
	}

	int countNumValidEditors(Point2D select, double threshold) {
		int count = 0;
		for (int i = 0; i < editorArray.length; i++) {
			if (validEditor(i, select, threshold))
				count++;
		}
		return count;
	}

	class EditorComparator implements Comparator {
		Point2D select;

		public EditorComparator(Point2D selectIn) {
			select = selectIn;
		}

		public int compare(Object first, Object second) {
			CanvasEditor shapeOne = (CanvasEditor) first;
			CanvasEditor shapeTwo = (CanvasEditor) second;
			if (shapeOne.contains(select) && !shapeTwo.contains(select))
				return -1;
			else if (!shapeOne.contains(select) && shapeTwo.contains(select)) {
				return 1;
			} else // both in bounds or both out of bounds, so pick closer
			{
				if (shapeOne.minDist(select) < shapeTwo.minDist(select))
					return -1;
				else if (shapeOne.minDist(select) > shapeTwo.minDist(select))
					return 1;
				else
					return 0;
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.piccolo.event.PBasicInputEventHandler#keyPressed(edu.umd.cs.piccolo.event.PInputEvent)
	 */
	public void keyPressed(PInputEvent event) {
		// TODO Auto-generated method stub
		super.keyPressed(event);
		if (getActiveEditor() == null)
			return;
		// Do not allow deletion for editors that are in DWRT mode
		if (getActiveEditor().isLocked(event)) {
			return;
		}

		// If key is delete, then call zap
		if (event.getKeyCode() == KeyEvent.VK_DELETE
				|| event.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			getActiveEditor().zap();
		} else
			getActiveEditor().keyPressed(event);
	}

	public void keyReleased(PInputEvent event) {
		// TODO Auto-generated method stub
		super.keyReleased(event);
		if (getActiveEditor() == null)
			return;

		getActiveEditor().keyReleased(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.umd.cs.piccolo.event.PBasicInputEventHandler#keyTyped(edu.umd.cs.piccolo.event.PInputEvent)
	 */
	public void keyTyped(PInputEvent event) {
		// TODO Auto-generated method stub
		super.keyTyped(event);
		logger.fine("Key typed");
		if (getActiveEditor() == null)
			return;
		getActiveEditor().keyTyped(event);
	}

}