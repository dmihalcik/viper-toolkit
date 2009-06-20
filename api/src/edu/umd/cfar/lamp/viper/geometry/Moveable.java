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

package edu.umd.cfar.lamp.viper.geometry;

/**
 * Instances of this interface allow movement along the 8 cardinal
 * directions.
 * @author clin
 */
public interface Moveable {
	/**
	 * North direction.
	 * @see Moveable#move(int, int)
	 */
	public static final int NORTH = 0 ;

	/**
	 * Northeast direction.
	 * @see Moveable#move(int, int)
	 */
	public static final int NORTHEAST = 1 ;

	/**
	 * East direction.
	 * @see Moveable#move(int, int)
	 */
	public static final int EAST = 2 ;

	/**
	 * Southeast direction.
	 * @see Moveable#move(int, int)
	 */
	public static final int SOUTHEAST = 3 ;

	/**
	 * South direction.
	 * @see Moveable#move(int, int)
	 */
	public static final int SOUTH = 4;

	/**
	 * Southwest direction.
	 * @see Moveable#move(int, int)
	 */
	public static final int SOUTHWEST = 5 ;

	/**
	 * West direction.
	 * @see Moveable#move(int, int)
	 */
	public static final int WEST = 6 ;

	/**
	 * Northwest direction.
	 * @see Moveable#move(int, int)
	 */
	public static final int NORTHWEST = 7 ;
	
	/**
	 * Creates a new copy of this movable item, shifted in the 
	 * given direction by the given number of pixels.
	 * @param direction the direction to move the item
	 * @param distance the distance to move it
	 * @return the new item, the same as the old one, but shifted
	 */
	public Moveable move( int direction, int distance ) ;
	
	/**
	 * Shifts the moveable by the given amount.
	 * @param x
	 * @param y
	 * @return
	 */
	public Moveable shift(int x, int y);
}
