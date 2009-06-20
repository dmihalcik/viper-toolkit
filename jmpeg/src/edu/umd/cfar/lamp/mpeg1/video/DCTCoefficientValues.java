/***************************************
 *            ViPER-MPEG               *
 *  The Video Processing               *
 *         Evaluation Resource         *
 *             MPEG-1 Decoder          *
 * Distributed under the LGPL license  *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.mpeg1.video;


class DCTCoefficientValues
{
	private int run   = 0;
	private int level = 0;

	
	public DCTCoefficientValues()
	{
	}

	public DCTCoefficientValues(int run, int level)
	{
		setRun(run);
		setLevel(level);
	}

	public void setRun(int run)
	{
		this.run = run;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

	public int getRun()
	{
		return run;
	}

	public int getLevel()
	{
		return level;
	}
}
