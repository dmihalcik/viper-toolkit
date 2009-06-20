/*
 * Created on Jun 2, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.umd.cfar.lamp.viper.gui.canvas;


import java.awt.*;

import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cs.piccolox.util.*;

/**
 * @author clin
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HighlightSingleton implements Highlightable {
	public static HighlightSingleton colorTable = new HighlightSingleton() ;
	
	public static final ShapeDisplayProperties STYLE_HIDDEN = new BasicShapeDisplayProperties() {
		public Paint getStrokePaint() {
			return colorTable.hiddenColor;
		}
		public Stroke getStroke() {
			return colorTable.hiddenStroke;
		}
	};
	
	public static ShapeDisplayProperties STYLE_UNSELECTED = new BasicShapeDisplayProperties() {
		public Paint getStrokePaint() {
			return colorTable.unselected;
		}
		public Stroke getStroke() {
			return colorTable.thinStroke;
		}
	};
	public static ShapeDisplayProperties STYLE_SELECTED = new BasicShapeDisplayProperties() {
		public Paint getStrokePaint() {
			return colorTable.selected;
		}
		public Stroke getStroke() {
			return colorTable.mediumStroke;
		}
	};
	public static ShapeDisplayProperties STYLE_LOCKED_UNSELECTED = new BasicShapeDisplayProperties() {
		public Paint getStrokePaint() {
			return colorTable.unselectedDisplayWRTColor;
		}
		public Stroke getStroke() {
			return colorTable.thinStroke;
		}
	};
	public static ShapeDisplayProperties STYLE_LOCKED_SELECTED = new BasicShapeDisplayProperties() {
		public Paint getStrokePaint() {
			return colorTable.selectedDisplayWRTColor;
		}
		public Stroke getStroke() {
			return colorTable.mediumStroke;
		}
	};
	public static ShapeDisplayProperties STYLE_HOVER = new BasicShapeDisplayProperties() {
		public Paint getStrokePaint() {
			return colorTable.boldColor;
		}
		public Stroke getStroke() {
			return colorTable.thickStroke;
		}
	};
	public static ShapeDisplayProperties STYLE_HANDLE = new BasicShapeDisplayProperties() {
		public Paint getStrokePaint() {
			return colorTable.handleColor;
		}
		public Stroke getStroke() {
			return colorTable.fixedMediumStroke;
		}
	};
	public static ShapeDisplayProperties STYLE_HOVER_HANDLE = new BasicShapeDisplayProperties() {
		public Paint getStrokePaint() {
			return colorTable.handleColor;
		}
		public Stroke getStroke() {
			return colorTable.fixedThickStroke;
		}
	};

	private Color hiddenColor = new Color(255, 255, 255, 0);
	private Color unselected = ColorUtilities.getColor( "lime" ) ;
	private Color selected = ColorUtilities.getColor( "red" ) ;
	private Color selectedDisplayWRTColor = ColorUtilities.getColor( "cyan" ) ;
	private Color unselectedDisplayWRTColor = ColorUtilities.getColor( "gold" ) ;
	private Color boldColor = ColorUtilities.getColor( "magenta" ) ;
	private Color unselectedHandleColor = ColorUtilities.getColor( "orange" );
	private Color handleColor = ColorUtilities.changeAlpha(ColorUtilities.getColor( "orange" ), .5);

	private Stroke thickStroke = new BasicStroke( 3.0f ) ;
	private Stroke mediumStroke = new BasicStroke( 2.0f ) ;
	private Stroke thinStroke = new BasicStroke( 1.4f ) ;
	private Stroke hiddenStroke = new BasicStroke( 0 ) ;
	private Stroke fixedThickStroke = new PFixedWidthStroke( 3.0f ) ;
	private Stroke fixedMediumStroke = new PFixedWidthStroke( 2.0f ) ;
	private Stroke fixedThinStroke = new PFixedWidthStroke( 1.4f ) ;


	private HighlightSingleton()
	{
		float[] C = unselectedHandleColor.getRGBColorComponents(null);
		unselectedHandleColor = new Color(C[0], C[1], C[2], .75f);
	}
	
	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.Highlightable#getUnselectedColor()
	 */
	public Color getUnselectedColor() {
		return unselected;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.Highlightable#getUnselectedStroke()
	 */
	public Stroke getUnselectedStroke() {
		return thinStroke ;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.Highlightable#getSelectedColor()
	 */
	public Color getSelectedColor() {
		return selected ;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.Highlightable#getSelectedStroke()
	 */
	public Stroke getSelectedStroke() {
		return thinStroke ;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.Highlightable#getDisplayWRTColor()
	 */
	public Color getUnselectedDisplayWRTColor() {
		return unselectedDisplayWRTColor ;
	}

	public Color getSelectedDisplayWRTColor() {
		return selectedDisplayWRTColor ;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.Highlightable#getDisplayWRTStroke()
	 */
	public Stroke getDisplayWRTStroke() {
		return thinStroke ;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.Highlightable#getBoldSelectedColor()
	 */
	public Color getHighlightColor() {
		return boldColor ;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.Highlightable#getBoldSelectedStroke()
	 */
	public Stroke getThickHighlightStroke() {
		return thickStroke ;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.Highlightable#getSemiboldSelectedStroke()
	 */
	public Stroke getMediumHighlightStroke() {
		// TODO Auto-generated method stub
		return mediumStroke ;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.Highlightable#getThinHighlightStroke()
	 */
	public Stroke getThinHighlightStroke() {
		// TODO Auto-generated method stub
		return thinStroke ;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.Highlightable#getUnselectedHandleColor()
	 */
	public Color getUnselectedHandleColor() {
		return unselectedHandleColor;
	}

	/* (non-Javadoc)
	 * @see edu.umd.cfar.lamp.viper.gui.canvas.Highlightable#getHandleColor()
	 */
	public Color getHandleColor() {
		return handleColor;
	}

}
