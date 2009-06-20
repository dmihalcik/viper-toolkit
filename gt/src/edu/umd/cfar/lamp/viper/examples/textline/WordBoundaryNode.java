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

package edu.umd.cfar.lamp.viper.examples.textline;

import java.awt.*;
import java.awt.geom.*;

import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;

/**
 * @author spikes51@umiacs.umd.edu
 * @since Mar 7, 2005
 *
 * This is a special PPath that has a PTextNode attached to it 
 * displaying its index.
 */

public class WordBoundaryNode extends PPath {
	// constants used to identify which type of boundary
	public static final int MIDDLE_BOUNDARY = 1;
	public static final int LEFT_BOUNDARY = 2;
	public static final int RIGHT_BOUNDARY = 3;
	
	private int label;
	private int type;
	private int stackCount;
	private TextlineNode parent; // pointer to parent for callbacks
	private PTextLabel numLabel; // OBSOLETE -- replaced by circle
	private PTextLabel textLabel; // contains the word following this boundary
	private PPath circle = new PPath(); // graphical representation of the circle handle
	private Ellipse2D circleModel; // underlying representation of circle--used to determine if mouse is inside
	
	/**
	 * Default constructor
	 *
	 */
	public WordBoundaryNode() {
		super();
		label = -1;
		type = MIDDLE_BOUNDARY;
	}
	
	/**
	 * Default-type constructor
	 * 
	 * @param p1 start point
	 * @param p2 end point
	 * @param labelIn label to display
	 */
	public WordBoundaryNode(Point2D p1, Point2D p2, String text, int labelIn, int stackCountIn, TextlineNode parentIn) {
		super();
		label = labelIn;
		type = MIDDLE_BOUNDARY;
		stackCount = stackCountIn;
		parent = parentIn;
		//createNumLabel(p1);
		createCircle(p1);
		createTextLabel(p2, text);
		setPathTo(new Line2D.Double(p1,p2));
	}
	
	/**
	 * Type-specific constructor
	 * 
	 * @param p1 start point
	 * @param p2 end point
	 * @param labelIn label to display
	 * @param typeIn type of this boundary (SINGLE_BOUNDARY or MULTIPLE_BOUNDARY)
	 */
	public WordBoundaryNode(Point2D p1, Point2D p2, String text, int labelIn, int typeIn, int stackCountIn, TextlineNode parentIn) {
		super();
		label = labelIn;
		type = typeIn;
		stackCount = stackCountIn;
		parent = parentIn;
		//createNumLabel(p1);
		createCircle(p1);
		createTextLabel(p2, text);
		setPathTo(new Line2D.Double(p1,p2));
	}
	
	void mouseMoved(PInputEvent e) {
		System.out.println("mouse moved over WordBoundaryNode");
	}
	
	void mousePressed(PInputEvent e) {
		System.out.println("mouse pressed on WordBoundaryNode");
	}
	
	/**
	 * Creates a PTextLabel with the word text
	 * 
	 * @param p1 used to calcuate the position of the label
	 * @param text word to display
	 * @param trans AffineTransform to use on the text
	 */
	private void createTextLabel(Point2D p1, String text) {
		if(text == "") textLabel = null;
		textLabel = new PTextLabel();
		textLabel.setHOffset(-5);
		textLabel.setWOffset(-2);
		textLabel.setText(text);
		addChild(textLabel);
		Point2D p = new Point2D.Double(p1.getX(), p1.getY() - textLabel.getHeight() + 1);
		textLabel.setOffset(p);
		/*if(type == MULTIPLE_BOUNDARY) {
		 // TODO: this doesn't work...nothing happens
		  //System.out.println("Setting textPaint to BLUE..."); // DEBUG
		   textLabel.setLine(Color.RED);
		   textLabel.setTextPaint(Color.WHITE);
		   textLabel.setFill(Color.BLUE);
		   //textLabel.repaint();
		    }*/
	}
	
	/**
	 * Creates a PTextLabel with the index/boundary count
	 * 
	 * @param p1 used to calcuate the position of the label
	 */
	private void createNumLabel(Point2D p1) {
		String dots = "";
		if(type == LEFT_BOUNDARY || type == RIGHT_BOUNDARY) dots = "..."; 
		if(label != -1) {
			numLabel = new PTextLabel(" " + dots + label + " ");
		} else {
			numLabel = new PTextLabel(" ");
		}
		addChild(numLabel);
		numLabel.setOffset(p1.getX() - (numLabel.getWidth() / 2), p1.getY() + 3);
	}
	
	/**
	 * Creates the circle below this boundary
	 * 
	 * @param p1 used to calcuate the position of the circle
	 */
	private void createCircle(Point2D p1) {
		final int DIAMETER = 8; // circles are 8 px big
		final int X_SHIFT = 2; // slant the stack by 2 px at each row
		
		int offset = 0;
		if(type == MIDDLE_BOUNDARY) offset = stackCount;
		else if(type == LEFT_BOUNDARY) offset = (parent.getOffscreenCountL()-1);
		else if(type == RIGHT_BOUNDARY) offset = (parent.getOffscreenCountR()-1);
		
		circleModel = new Ellipse2D.Float((float) (p1.getX() - (DIAMETER/2) + (offset * X_SHIFT)), (float) (p1.getY() + 1 + (offset * DIAMETER)), (float) DIAMETER, (float) DIAMETER);
		circle.setPathTo(circleModel);
		addChild(circle);
	}
	
	/**
	 * Determines whether the given point lies within the circle handle.
	 * 
	 * @param p
	 * @return true if p is contained in the handle for this boundary
	 */
	public boolean handleContains(Point2D p) {
		return circleModel.contains(p);
	}
	
	public void setSelected() {
		textLabel.setVisible(true);
		circle.setVisible(true);
	}
	
	public void setUnselected() {
		textLabel.setVisible(false);
		circle.setVisible(false);
	}
	
	/* (non-Javadoc)
	 * @see edu.umd.cs.piccolo.nodes.PPath#setStroke(java.awt.Stroke)
	 */
	public void setStroke(Stroke arg0) {
		super.setStroke(arg0);
		circle.setStroke(arg0); // pass it on to the circle
	}
	
	/* (non-Javadoc)
	 * @see edu.umd.cs.piccolo.nodes.PPath#setStrokePaint(java.awt.Paint)
	 */
	public void setStrokePaint(Paint arg0) {
		super.setStrokePaint(arg0);
		circle.setStrokePaint(arg0); // pass it on to the circle
	}
}
