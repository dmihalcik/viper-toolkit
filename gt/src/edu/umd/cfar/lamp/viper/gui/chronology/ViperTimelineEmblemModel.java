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


package edu.umd.cfar.lamp.viper.gui.chronology;
import java.awt.*;

import viper.api.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.chronicle.extras.emblems.*;

/**
 * 
 */
public class ViperTimelineEmblemModel extends TreeEmblemModel {
	private Image playbackSelectedIcon;
	private Image playbackUnselectedIcon;
	private ViperChronicleSelectionModel selection;

	public ViperTimelineEmblemModel(ViperChronicleModel vcm, ViperChronicleSelectionModel selection) {
		super(vcm);
		this.selection = selection;
	}
	/**
	 * @return Returns the playbackSelectedIcon.
	 */
	public Image getPlaybackSelectedIcon() {
		return playbackSelectedIcon;
	}
	/**
	 * @param playbackSelectedIcon The playbackSelectedIcon to set.
	 */
	public void setPlaybackSelectedIcon(Image playbackSelectedIcon) {
		this.playbackSelectedIcon = playbackSelectedIcon;
		fireChangeEvent();
	}
	/**
	 * @return Returns the playbackUnselectedIcon.
	 */
	public Image getPlaybackUnselectedIcon() {
		return playbackUnselectedIcon;
	}
	/**
	 * @param playbackUnselectedIcon The playbackUnselectedIcon to set.
	 */
	public void setPlaybackUnselectedIcon(Image playbackUnselectedIcon) {
		this.playbackUnselectedIcon = playbackUnselectedIcon;
		fireChangeEvent();
	}
	public void click(TimeLine tqe, int i) {
		if (i == 0) {
			Node tn = null;
			if (tqe instanceof ViperNodeTimeLine) {
				tn = ((ViperNodeTimeLine) tqe).getNode();
			}
			if (tn != null) {
				if (!tn.equals(selection.getNodeWhoseTimeToSelect())) {
					selection.setNodeWhoseTimeToSelect(tn);
				} else {
					selection.setNodeWhoseTimeToSelect(null);
				}
			}
		} else {
			super.click(tqe, i-1);
		}
	}
	public Image getEmblemFor(TimeLine tqe, int i) {
		if (i == 0) {
			Node tn = null;
			if (tqe instanceof ViperNodeTimeLine) {
				tn = ((ViperNodeTimeLine) tqe).getNode();
			}
			if (tn != null) {
				if (tn.equals(selection.getNodeWhoseTimeToSelect())) {
					return getPlaybackSelectedIcon();
				} else {
					return getPlaybackUnselectedIcon();
				}
			}
			return null;
		} else {
			return super.getEmblemFor(tqe, i-1);
		}
	}
	public int getMaxEmblemCount() {
		return super.getMaxEmblemCount() + 1; // whatever tree emblems, plus playback-selecting
	}
	public String getTextEmblemFor(TimeLine tqe, int i) {
		if (i == 0) {
			return "Play Only Frames Where This Is Valid";
		} else {
			return super.getTextEmblemFor(tqe, i-1);
		}
	}
}
