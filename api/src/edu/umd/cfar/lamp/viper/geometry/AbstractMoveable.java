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
 * Implements the 'move' method using the 'shift' method.
 */
public abstract class AbstractMoveable implements Moveable {
	/**
	 * Gets a copy of the box, shifted by the given amount in the specified
	 * direction.
	 * 
	 * @param direction
	 *            the direction to remove the box, e.g. {@link Moveable#NORTH}
	 * @param distance
	 *            the distance to move the box
	 * @return a new box, that is a copy of this box, shifted as specified
	 */
	public Moveable move(int direction, int distance) {
		switch (direction) {
			case Moveable.NORTH :
				return shift(0, distance);
			case Moveable.NORTHEAST :
				return shift(distance, distance);
			case Moveable.EAST :
				return shift(distance, 0);
			case Moveable.SOUTHEAST :
				return shift(distance, -distance);
			case Moveable.SOUTH :
				return shift(0, -distance);
			case Moveable.SOUTHWEST :
				return shift(-distance, -distance);
			case Moveable.WEST :
				return shift(-distance, 0);
			case Moveable.NORTHWEST :
				return shift(-distance, distance);
		}
		throw new IllegalArgumentException(
			"Not a cardinal direction: " + direction);
	}
	abstract public Moveable shift(int x, int y);
}
