package edu.umd.cfar.lamp.viper.gui.players;

import javax.media.control.*;

import viper.api.time.*;

/**
 * @author davidm
 */
class MediaFrameRate extends AbstractFrameRate {
	private FramePositioningControl help;

	public MediaFrameRate(FramePositioningControl h) {
//		super(1, h.mapFrameToTime(1).getNanoseconds());
		help = h;
	}

	public Frame asFrame(viper.api.time.Time i)  {
		return new Frame(
			help.mapTimeToFrame(
				new javax.media.Time(i.getTime())));
	}
	public viper.api.time.Time asTime(Frame i) {
		return new viper.api.time.Time(
			help.mapFrameToTime(((Frame) i).getFrame()).getNanoseconds());
	}
}
