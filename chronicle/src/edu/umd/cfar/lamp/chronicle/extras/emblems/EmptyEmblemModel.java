package edu.umd.cfar.lamp.chronicle.extras.emblems;

import java.awt.*;

import edu.umd.cfar.lamp.chronicle.*;

/**
 * The basic emblem model, which is the default
 * model for the chronicle. 
 */
public class EmptyEmblemModel extends AbstractEmblemModel {
	/**
	 * {@inheritDoc}
	 * @return zero
	 */
	public int getMaxEmblemCount() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 * @return <code>null</code>
	 */
	public Image getEmblemFor(TimeLine tqe, int i) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * @return the empty string
	 */
	public String getTextEmblemFor(TimeLine tqe, int i) {
		return "";
	}

	/**
	 * Does nothing.
	 * {@inheritDoc}
	 */
	public void click(TimeLine tqe, int i) {
	}
}
