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



class MacroblockType
{
	private boolean
		macroblock_quant,
		macroblock_motion_forward,
		macroblock_motion_backward,
		macroblock_pattern,
		macroblock_intra;


	public MacroblockType()
	{
	}
	
	public void setValues(boolean macroblock_quant, boolean macroblock_motion_forward, boolean macroblock_motion_backward, boolean macroblock_pattern, boolean macroblock_intra)
	{
		this.macroblock_quant           = macroblock_quant;
		this.macroblock_motion_forward  = macroblock_motion_forward;
		this.macroblock_motion_backward = macroblock_motion_backward;
		this.macroblock_pattern         = macroblock_pattern;
		this.macroblock_intra           = macroblock_intra;
	}

	public boolean getMacroblockQuant()
	{
		return macroblock_quant;
	}

	public boolean getMacroblockMotionForward()
	{
		return macroblock_motion_forward;
	}

	public boolean getMacroblockMotionBackward()
	{
		return macroblock_motion_backward;
	}

	public boolean getMacroblockPattern()
	{
		return macroblock_pattern;
	}

	public boolean getMacroblockIntra()
	{
		return macroblock_intra;
	}
}
