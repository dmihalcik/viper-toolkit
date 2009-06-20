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

package edu.umd.cfar.lamp.viper.gui.remote;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import viper.api.*;
import viper.api.time.*;
import viper.api.time.Frame;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.chronicle.markers.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.chronology.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * Sets the playback rate, and allows skipping to frame. It has a 
 * visual display like a remote control.
 */
public class ViperRemoteWidgets {
	private static final int SPEED_SLIDER_MAX = 6;

	private static final int SPEED_SLIDER_MIN = -4;

	private ViperControls vc;
	
	private Markers markers;

	private Class unitPreference = Frame.class;
	private Action skipBackAction;
	private Action smartBackAction;
	private Action decelerateAction;
	private Action pauseAction;
	private Action accelerateAction;
	private Action smartForwardAction;
	private Action skipForwardAction;
	private Action addMarkerAction;
	private Action nextFrameAction;
	private Action previousFrameAction;
	
	private Action snapBackAction;
	private Action tivoPlayAction;
	private Action tivoPauseAction;
	private Action tivoFastForwardAction;
	
	private Action toggleLoopingAction;
	
	private JInstantField frameField;
	private JSpinner speedSpinner;
	private JSlider speedSlider;
	private JLabel frameCountLabel;
	private JCheckBox loopingCheckBox;
	
	public Icon getAdvanceIcon() {
		return advanceIcon;
	}
	public void setAdvanceIcon(Icon advanceIcon) {
		this.advanceIcon = advanceIcon;
		resetButtons();
	}
	public Icon getBackIcon() {
		return backIcon;
	}
	public void setBackIcon(Icon backIcon) {
		this.backIcon = backIcon;
		resetButtons();
	}
	public Icon getFastforwardIcon() {
		return fastforwardIcon;
	}
	public void setFastforwardIcon(Icon fastforwardIcon) {
		this.fastforwardIcon = fastforwardIcon;
		resetButtons();
	}
	public Icon getForwardIcon() {
		return forwardIcon;
	}
	public void setForwardIcon(Icon forwardIcon) {
		this.forwardIcon = forwardIcon;
		resetButtons();
	}
	public void setLoopingIcon(Icon loopingIcon) {
		this.loopingIcon = loopingIcon;
		resetButtons();
	}
	public Icon getPauseIcon() {
		return pauseIcon;
	}
	public void setPauseIcon(Icon pauseIcon) {
		this.pauseIcon = pauseIcon;
		resetButtons();
	}
	public Icon getRetreatIcon() {
		return retreatIcon;
	}
	public void setRetreatIcon(Icon retreatIcon) {
		this.retreatIcon = retreatIcon;
		resetButtons();
	}
	public Icon getRewindIcon() {
		return rewindIcon;
	}
	public void setRewindIcon(Icon rewindIcon) {
		this.rewindIcon = rewindIcon;
		resetButtons();
	}
	private Icon retreatIcon;
	private Icon rewindIcon;
	private Icon backIcon;
	private Icon pauseIcon;
	private Icon forwardIcon;
	private Icon fastforwardIcon;
	private Icon advanceIcon;
	private Icon loopingIcon;
	

	public void pause() {
		if (!vc.isPaused()) {
			vc.pause();
			resetButtons();
			resetRateLabel();
			resetEnabledControls();
		}
	}
	private static void resetAction(Action act, Icon icn, String txt, String desc) {
		if (icn != null) {
			act.putValue(Action.SMALL_ICON, icn);
			act.putValue(Action.NAME, null);
		} else {
			act.putValue(Action.SMALL_ICON, null);
			act.putValue(Action.NAME, txt);
		}
		act.putValue(Action.SHORT_DESCRIPTION, desc);
	}
	
	private void resetButtons() {
		resetAction(snapBackAction, retreatIcon, "|<", "Snap Back");
		resetAction(tivoPauseAction, pauseIcon, "||", "Pause");
		resetAction(tivoPlayAction, forwardIcon, ">", "Play");
		resetAction(tivoFastForwardAction, fastforwardIcon, ">>", "Faster");
		
		resetAction(skipBackAction, retreatIcon, "|<", "Skip Back a Section");
		resetAction(smartBackAction, rewindIcon, vc.isPaused() ? "<" : "<<", vc.isPaused() ? "Previous Frame" : "Decelerate Playback");
		if (!vc.isPaused()) {
			resetAction(pauseAction, pauseIcon, "<#>", "Pause");
		} else if (vc.isForward()) {
			resetAction(pauseAction, forwardIcon, "#>", "Play");
		} else {
			resetAction(pauseAction, backIcon, "<#", "Play");
		}
		resetAction(smartForwardAction, fastforwardIcon, vc.isPaused() ? ">" : ">>", vc.isPaused() ? "Next Frame" : "Accelerate Playback");
		resetAction(skipForwardAction, advanceIcon, ">|", "Skip Ahead a Section");
		resetAction(toggleLoopingAction, loopingIcon, "Looping", vc.isLooping() ? "Turn Off Looping" : "Loop after End");
	}
	public void play() {
		if (vc.isPaused()) {
			vc.play();
			resetButtons();
			resetRateLabel();
			resetEnabledControls();
		}
	}
	
	public void resetEnabledControls() {
		ViperViewMediator mediator = getMediator();
		boolean enable = false;
		if (getMediator() != null) {
			Sourcefile sf = getMediator().getCurrFile();
			enable = sf != null && getMediator().getCurrentFrame() != null;
			enable = enable && !sf.getReferenceMedia().getSpan().isEmpty();
		}
		boolean atStart = false;
		boolean atEnd = false;
		boolean multiframe = false;
		if (enable) {
			TemporalRange r = vc.getPlaybackRange();
			assert getMediator() != null;
			assert getMediator().getDataPlayer() != null;
			FrameRate fr = getMediator().getDataPlayer().getRate();
			if (r != null) {
				Interval ii = r.getExtrema();
				if (ii != null) {
					Instant now = getMediator().getCurrentFrame();
					Instant start = (Instant) ii.getStart();
					Instant end = (Instant) ii.getEnd();
					atStart = fr.compare(now, start) <= 0;
					atEnd = fr.compare(now, end.previous()) >= 0;
					multiframe = fr.compare(start, end) < 0;
				}
			}
		}
		snapBackAction.setEnabled(enable && !atStart);
		tivoPauseAction.setEnabled(enable && !isPaused());
		tivoPlayAction.setEnabled(enable && (isPaused() || !vc.getFrameRateMultiplier().equals(1)));
		tivoFastForwardAction.setEnabled(enable);
		skipForwardAction.setEnabled(enable && !atEnd);
		accelerateAction.setEnabled(enable && (!isPaused() || !atEnd) && multiframe);
		pauseAction.setEnabled(enable && multiframe);
		decelerateAction.setEnabled(enable && (!isPaused() || !atStart) && multiframe);
		skipBackAction.setEnabled(enable && !atStart);
		previousFrameAction.setEnabled(enable && !atStart);
		nextFrameAction.setEnabled(enable && !atEnd);
		smartBackAction.setEnabled(enable && (isPaused() ? decelerateAction.isEnabled():previousFrameAction.isEnabled()));
		smartForwardAction.setEnabled(enable && (isPaused() ? accelerateAction.isEnabled():nextFrameAction.isEnabled()));
		frameField.setEnabled(enable && multiframe);
		addMarkerAction.setEnabled(enable && multiframe);
		if (loopingCheckBox != null) {
			loopingCheckBox.setSelected(vc.isLooping());
		}
	}

	/**
	 * @return
	 */
	public ViperViewMediator getMediator() {
		return vc.getMediator();
	}

	/**
	 * @return
	 */
	public boolean isPaused() {
		return vc.isPaused();
	}

	/**
	 * @return
	 */
	public long getRate() {
		return vc.getRate();
	}

	/**
	 * @return
	 */
	public long getResolution() {
		return vc.getResolution();
	}

	/**
	 * @param mediator
	 */
	public void setMediator(ViperViewMediator mediator) {
		if (null != vc.getMediator()) {
			vc.getMediator().removeViperMediatorChangeListener(focusListener);
			vc.getMediator().setPlayControls(null);
		}
		vc.setMediator(mediator);
		this.pause();
		this.resetEnabledControls();
		if (null != mediator) {
			mediator.addViperMediatorChangeListener(focusListener);
			mediator.setPlayControls(vc);
		}
	}

	private ViperMediatorChangeListener focusListener =
		new ViperMediatorChangeListener() {
		public void frameChanged(ViperMediatorChangeEvent e) {
			if (!frameField.isFocusOwner()) {
				frameField.setValue(vc.getMediator().getMajorMoment());
			}
			resetEnabledControls();
		}
		public void dataChanged(ViperMediatorChangeEvent e) {
			frameChanged(e);
		}
		public void currFileChanged(ViperMediatorChangeEvent e) {
			pause();
			vc.setRate(1); //TODO: save playback rate with file?
			resetEnabledControls();
			if (markers != null) {
				ChronicleMarkerModel mm = markers.getRelatedMarkerModel();
				for (int i = mm.getSize()-1; i > 0; i--) {
					// don't remove the major moment marker
					mm.removeMarker(i);
				}
				// TODO: save the markers when the file closes
			}
			frameChanged(e);
		}
		public void schemaChanged(ViperMediatorChangeEvent e) {
			pause();
			resetEnabledControls();
			frameChanged(e);
		}
		public void mediaChanged(ViperMediatorChangeEvent e) {
		}
	};

	/**
	 * @param b
	 */
	public void setPaused(boolean b) {
		if (b) {
			pause();
		} else {
			play();
		}
	}

	private void resetRateLabel() {
		Rational rate;
		int ratePower = 0;
		if (vc.isRealtimePlayback()) {
			rate = vc.getFrameRateMultiplier();
			ratePower = (int) (Math.log(rate.doubleValue()) /Math.log(2));
		} else {
			rate = new Rational(vc.getRate());
		}
		if (vc.getMediator() != null) {
			Instant i = vc.getMediator().getFocusInterval().getEndInstant();
			int count = i.intValue() - 1;
			frameCountLabel.setText(" / " + count + " frame" + (count > 1 ? "s" : ""));
		} else {
			frameCountLabel.setText("");
		}
		getSpeedSpinner().setValue(rate);
		getSpeedSlider().setValue(ratePower);
//		speedLabel.setEnabled(vc.isPaused());
	}

	/**
	 * @param l
	 */
	public void setRate(long l) {
		vc.setRate(l);
	}

	/**
	 * @param l
	 */
	public void setResolution(int l) {
		vc.setResolution(l);
	}

	private class NextSectionAction extends AbstractAction {
		public void actionPerformed(ActionEvent event) {
			vc.nextSection();
		}
	}

	private class NextFrameAction extends AbstractAction {
		public void actionPerformed(ActionEvent event) {
			vc.nextFrame();
		}
	}

	private class PreviousFrameAction extends AbstractAction {
		public void actionPerformed(ActionEvent event) {
			vc.previousFrame();
		}
	}

	private class PreviousSectionAction extends AbstractAction {
		public void actionPerformed(ActionEvent event) {
			vc.previousSection();
		}
	}

	
	private class AccelerateAction extends AbstractAction {
		public void actionPerformed(ActionEvent event) {
			vc.accelerate();
		}
	}

	private class DecelerateAction extends AbstractAction {
		public void actionPerformed(ActionEvent event) {
			vc.decelerate();
		}
	}
	
	private class TivoPauseAction extends AbstractAction {
		public void actionPerformed(ActionEvent event) {
			vc.humanPause();
		}
	}
	private class TivoPlayAction extends AbstractAction {
		public void actionPerformed(ActionEvent event) {
			vc.humanPlay();
		}
	}
	private class TivoFastForwardAction extends AbstractAction {
		public void actionPerformed(ActionEvent event) {
			vc.humanAccelerate();
		}
	}
	private class TivoSnapBackAction extends AbstractAction {
		public void actionPerformed(ActionEvent event) {
			vc.snapBack();
		}
	}
	private class ToggleLoopingAction extends AbstractAction {
		public void actionPerformed(ActionEvent event) {
			vc.setLooping(!vc.isLooping());
		}
	}

	private class SmartForwardAction extends AbstractAction {
		public void actionPerformed(ActionEvent event) {
			if (isPaused()) {
				getNextFrameAction().actionPerformed(event);
			} else {
				getAccelerateAction().actionPerformed(event);
			}
		}
	}

	private class SmartBackAction extends AbstractAction {
		public void actionPerformed(ActionEvent event) {
			if (isPaused()) {
				getPreviousFrameAction().actionPerformed(event);
			} else {
				getDecelerateAction().actionPerformed(event);
			}
		}
	}
	

	/**
	 * Adds a marker right here.
	 */
	private class AddMarkerHereAction extends AbstractAction {
		public AddMarkerHereAction(String name) {
			super(name);
		}
		public void actionPerformed(ActionEvent event) {
			if (markers != null) {
				markers.addMarker();
			}
		}
	}

	/**
	 * When this listener hears something, it toggles the 
	 * paused/play state of the remote.
	 */
	private class PauseToggleAction extends AbstractAction {
		public void actionPerformed(ActionEvent event) {
			setPaused(!isPaused());
		}
	}

	public ViperRemoteWidgets() {
		vc = new ViperControls();
		vc.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				resetRateLabel();
				resetEnabledControls();
			}
		});
		skipBackAction = new PreviousSectionAction();
		skipForwardAction = new NextSectionAction();

		nextFrameAction = new NextFrameAction();
		previousFrameAction = new PreviousFrameAction();

		accelerateAction = new AccelerateAction();
		decelerateAction = new DecelerateAction();
		
		smartBackAction = new SmartBackAction();
		smartForwardAction = new SmartForwardAction();

		snapBackAction = new TivoSnapBackAction();
		tivoPlayAction = new TivoPlayAction();
		tivoPauseAction = new TivoPauseAction();
		tivoFastForwardAction = new TivoFastForwardAction();
		
		toggleLoopingAction = new ToggleLoopingAction();
		
		pauseAction = new PauseToggleAction();
		addMarkerAction = new AddMarkerHereAction("Mark");
		addMarkerAction.setEnabled(false);
		addMarkerAction.putValue(Action.SHORT_DESCRIPTION, "Place a Mark in the Timeline");
		frameField = new JInstantField();
		frameField.setUnitPreference(unitPreference);
		frameField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Instant goal = (Instant) frameField.getValue();
				if (goal != null) {
					vc.smartGo(goal);
					frameField.setValue(vc.getMediator().getCurrentFrame());
				}
			}
		});

		frameCountLabel = new JLabel();
		resetRateLabel();
		resetButtons();
		pause();
	}


	private ActionListener goFrameActionListener = new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
			pause();
			String command = ae.getActionCommand();
			if (null != command && command.startsWith("prev")) {
				vc.previousFrame();
			} else {
				vc.nextFrame();
			}
		}
	};

	/**
	 * @return
	 */
	public Markers getMarkers() {
		return markers;
	}

	/**
	 * @param markers
	 */
	public void setMarkers(Markers markers) {
		this.markers = markers;
		addMarkerAction.setEnabled(this.markers != null);
	}
 
	/**
	 * Gets the playback controller.
	 * @return Returns the underlying controller.
	 */
	public ViperControls getViperControls() {
		return vc;
	}
	public Action getAccelerateAction() {
		return accelerateAction;
	}
	public Action getAddMarkerAction() {
		return addMarkerAction;
	}
	
	/**
	 * Gets ab action listener that slows down playback (or speeds up 
	 * reverse playback). 
	 * @return
	 */
	public Action getDecelerateAction() {
		return decelerateAction;
	}
	public JLabel getFrameCountLabel() {
		return frameCountLabel;
	}
	public JInstantField getFrameField() {
		return frameField;
	}
	public Action getPauseAction() {
		return pauseAction;
	}
	public Action getSkipBackAction() {
		return skipBackAction;
	}
	public Action getNextFrameAction() {
		return nextFrameAction;
	}
	public Action getPreviousFrameAction() {
		return previousFrameAction;
	}
	public Action getSkipForwardAction() {
		return skipForwardAction;
	}
	public JSlider getSpeedSlider() { 
		if (null == speedSlider) {
			speedSlider = new JSlider(SPEED_SLIDER_MIN, SPEED_SLIDER_MAX, 0);
			speedSlider.setMinorTickSpacing(1);
			speedSlider.setPaintTicks(true);
			speedSlider.setSnapToTicks(true);
			Dictionary d = new Hashtable();
			d.put(new Integer(0), new JLabel("Normal"));
			d.put(new Integer(SPEED_SLIDER_MIN), new JLabel("Slow"));
			d.put(new Integer(SPEED_SLIDER_MAX), new JLabel("Fast"));
			
			speedSlider.setLabelTable(d);
			speedSlider.setPaintLabels(true);
			speedSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					int pow = speedSlider.getValue();
					if (pow < 0) {
						vc.setMultiplier(new Rational(1, 1 << (-pow)));
					} else {
						vc.setMultiplier(new Rational(1 << pow));
					}
				}
			});
		}
		return speedSlider;
	}
	
	public JCheckBox getLoopCheckBox() {
		if (null == loopingCheckBox) {
			loopingCheckBox = new JCheckBox(toggleLoopingAction);
			loopingCheckBox.setSelected(vc.isLooping());
		}
		return loopingCheckBox;
	}
	
	public JSpinner getSpeedSpinner() {
		if (speedSpinner == null) {
			speedSpinner = new JSpinner(new SpinnerListModel(ViperControls.MULTIPLIERS));
			speedSpinner.setPreferredSize(null);
			Dimension oldDim = speedSpinner.getPreferredSize();
			double maxW = oldDim.getWidth(), maxH = oldDim.getHeight();
			for (int i = 0; i < ViperControls.MULTIPLIERS.length; i++) {
				speedSpinner.setValue(ViperControls.MULTIPLIERS[i]);
				Dimension d = speedSpinner.getPreferredSize();
				maxW = Math.max(maxW, d.getWidth());
				maxH = Math.max(maxH, d.getHeight());
			}
			if (maxW > oldDim.getWidth() || maxH > oldDim.getHeight()) {
				speedSpinner.setPreferredSize(new Dimension((int) maxW, (int) maxH));
			}
			speedSpinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					vc.setMultiplier((Rational) speedSpinner.getValue());
				}
			});
		}
		return speedSpinner;
	}
	public void setPlaybackSelected(ChronicleSelectionModel playbackSelected) {
		vc.setPlaybackSelected(playbackSelected);
	}
	public Action getSmartBackAction() {
		return smartBackAction;
	}
	public Action getSmartForwardAction() {
		return smartForwardAction;
	}
	public Action getSnapBackAction() {
		return snapBackAction;
	}
	public Action getTivoFastForwardAction() {
		return tivoFastForwardAction;
	}
	public Action getTivoPauseAction() {
		return tivoPauseAction;
	}
	public Action getTivoPlayAction() {
		return tivoPlayAction;
	}
}